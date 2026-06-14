<%--
    RapViet Cinema - Chi tiết đơn đặt vé + Hủy đơn (Booking details / Cancel) - KHÁCH
    Module: Đặt vé   (Group6 - Huy)
    Được phục vụ bởi controller.BookingDetailController  ->  URL /my-booking?id=N
    View chỉ dùng JSTL + EL + component, KHÔNG nhúng code Java.
--%>
<%@ page contentType="text/html; charset=UTF-8" pageEncoding="UTF-8" %>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<c:set var="ctx" value="${pageContext.request.contextPath}" />

<!DOCTYPE html>
<html lang="vi">
<head>
    <meta charset="UTF-8">
    <meta name="viewport" content="width=device-width, initial-scale=1">
    <title>Chi tiết đơn đặt vé - RapViet Cinema</title>
    <link rel="stylesheet" href="${ctx}/assets/css/style.css">
    <link rel="stylesheet" href="${ctx}/assets/css/movie.css">
</head>
<body>
    <jsp:include page="/pages/common/header.jsp">
        <jsp:param name="active" value="bookings"/>
    </jsp:include>

    <div class="page-wrap">
    <div class="container">
        <c:choose>
            <%-- ── Không tìm thấy đơn (hoặc không phải đơn của khách) ── --%>
            <c:when test="${notFound or empty bk}">
                <div class="empty">
                    <h3>Không tìm thấy đơn đặt vé</h3>
                    <p>Đơn không tồn tại hoặc không thuộc về tài khoản của bạn.</p>
                    <p><a class="btn btn-primary" href="${ctx}/my-bookings">← Về danh sách vé</a></p>
                </div>
            </c:when>

            <%-- ── Chi tiết đơn ── --%>
            <c:otherwise>
                <p style="margin:0 0 16px;">
                    <a href="${ctx}/my-bookings" style="color:#9aa0aa;">← Vé của tôi</a>
                </p>

                <%-- Thông báo kết quả hủy (từ ?msg=...) --%>
                <c:if test="${param.msg == 'cancelled'}">
                    <div class="notice ok">Đã hủy đơn thành công.</div>
                </c:if>
                <c:if test="${param.msg == 'cancel_failed'}">
                    <div class="notice err">Không thể hủy đơn này (đơn đã sử dụng/đã hủy hoặc không cho phép hủy).</div>
                </c:if>

                <div class="booking-detail">
                    <div class="bd-head">
                        <h1><c:out value="${bk.movieTitle}"/></h1>
                        <span class="badge ${bk.statusBadgeClass}">${bk.statusLabel}</span>
                    </div>

                    <div class="detail-table">
                        <div><span class="k">Mã đơn</span> #${bk.booking.id}</div>
                        <div><span class="k">Suất chiếu</span> ${bk.showTimeLabel}</div>
                        <div><span class="k">Chi nhánh</span> <c:out value="${bk.branchName}"/></div>
                        <div><span class="k">Phòng</span> <c:out value="${bk.hallName}"/></div>
                        <div><span class="k">Ghế</span> <c:out value="${bk.seatLabels}"/> (${bk.seatCount} ghế)</div>
                        <div><span class="k">Tổng tiền</span> ${bk.totalPriceLabel}</div>
                        <div><span class="k">Hình thức</span> <c:out value="${bk.booking.source}"/></div>
                        <div><span class="k">Đặt lúc</span> ${bk.bookedAtLabel}</div>
                    </div>

                    <%-- Mã QR (nếu đơn đã có) --%>
                    <c:if test="${not empty bk.booking.qrCode}">
                        <div class="qr-box">
                            <span class="k">Mã vé (QR):</span>
                            <code><c:out value="${bk.booking.qrCode}"/></code>
                        </div>
                    </c:if>

                    <div class="bd-actions">
                        <%-- Hủy đơn: chỉ hiện khi còn cho phép hủy --%>
                        <c:if test="${bk.cancellable}">
                            <form method="post" action="${ctx}/my-booking"
                                  onsubmit="return confirm('Bạn chắc chắn muốn hủy đơn này?');">
                                <input type="hidden" name="id" value="${bk.booking.id}">
                                <button type="submit" class="btn btn-ghost">Hủy đơn</button>
                            </form>
                        </c:if>
                        <%-- Đánh giá phim: chỉ khi đã xem (sẽ nối module Đánh giá) --%>
                        <c:if test="${bk.reviewable}">
                            <a class="btn btn-primary"
                               href="${ctx}/review?bookingId=${bk.booking.id}&movieId=${bk.movieId}">Đánh giá phim</a>
                        </c:if>
                    </div>
                </div>
            </c:otherwise>
        </c:choose>
    </div>
    </div>

    <jsp:include page="/pages/common/footer.jsp" />
</body>
</html>
