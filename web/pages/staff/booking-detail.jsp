<%@ page contentType="text/html; charset=UTF-8" pageEncoding="UTF-8" %>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<c:set var="ctx" value="${pageContext.request.contextPath}" />

<!DOCTYPE html>
<html lang="vi">
    <head>
        <meta charset="UTF-8">
        <meta name="viewport" content="width=device-width, initial-scale=1">
        <title>Chi tiết booking - RapViet Cinema</title>
        <link rel="stylesheet" href="${ctx}/assets/css/style.css">
        <link rel="stylesheet" href="${ctx}/assets/css/movie.css">
    </head>
    <body>
        <jsp:include page="/pages/common/header.jsp">
            <jsp:param name="active" value="staff-bookings"/>
        </jsp:include>

        <div class="page-wrap">
            <div class="container">
                <p style="margin:0 0 16px;">
                    <a href="${ctx}/staff/bookings" style="color:#9aa0aa;">← Danh sách booking</a>
                </p>

                <c:choose>
                    <c:when test="${notFound or empty bk}">
                        <div class="empty">
                            <h3>Không tìm thấy booking trong chi nhánh của bạn</h3>
                        </div>
                    </c:when>
                    <c:otherwise>
                        <c:if test="${param.msg == 'cancelled'}">
                            <div class="notice ok">Đã hủy booking.</div>
                        </c:if>
                        <c:if test="${param.msg == 'checked_in'}">
                            <div class="notice ok">Đã check-in booking.</div>
                        </c:if>
                        <c:if test="${param.msg == 'used'}">
                            <div class="notice ok">Đã chuyển booking sang đã sử dụng.</div>
                        </c:if>
                        <c:if test="${param.msg == 'cancel_failed' or param.msg == 'checkin_failed' or param.msg == 'use_failed'}">
                            <div class="notice err">Không thể cập nhật booking này.</div>
                        </c:if>

                        <div class="booking-detail">
                            <div class="bd-head">
                                <h1>#${bk.booking.id} · <c:out value="${bk.movieTitle}"/></h1>
                                <span class="badge ${bk.statusBadgeClass}">${bk.statusLabel}</span>
                            </div>

                            <div class="detail-table">
                                <div><span class="k">Khách hàng</span> <c:out value="${bk.customerName}"/> · <c:out value="${bk.customerEmail}"/></div>
                                <div><span class="k">Suất chiếu</span> ${bk.showTimeLabel}</div>
                                <div><span class="k">Chi nhánh</span> <c:out value="${bk.branchName}"/></div>
                                <div><span class="k">Phòng</span> <c:out value="${bk.hallName}"/></div>
                                <div><span class="k">Ghế</span> <c:out value="${bk.seatLabels}"/> (${bk.seatCount} ghế)</div>
                                <div><span class="k">Tổng tiền</span> ${bk.totalPriceLabel}</div>
                                <div><span class="k">Nguồn</span> <c:out value="${bk.booking.source}"/></div>
                                <div><span class="k">Đặt lúc</span> ${bk.bookedAtLabel}</div>
                            </div>

                            <c:if test="${not empty bk.booking.qrCode}">
                                <div class="qr-box">
                                    <span class="k">Mã vé:</span>
                                    <code><c:out value="${bk.booking.qrCode}"/></code>
                                </div>
                            </c:if>

                            <div class="bd-actions">
                                <c:if test="${bk.cancellable}">
                                    <form method="post" action="${ctx}/staff/booking-staff/cancel"
                                          onsubmit="return confirm('Hủy booking này?');">
                                        <input type="hidden" name="id" value="${bk.booking.id}">
                                        <button class="btn btn-ghost" type="submit">Hủy booking</button>
                                    </form>
                                </c:if>

                                <c:if test="${bk.booking.status == 'CONFIRMED'}">
                                    <form method="post" action="${ctx}/staff/booking-staff/check-in">
                                        <input type="hidden" name="id" value="${bk.booking.id}">
                                        <button class="btn btn-primary" type="submit">Check-in</button>
                                    </form>
                                </c:if>

                                <c:if test="${bk.booking.status == 'CHECKED_IN'}">
                                    <form method="post" action="${ctx}/staff/booking-staff/use">
                                        <input type="hidden" name="id" value="${bk.booking.id}">
                                        <button class="btn btn-primary" type="submit">Đánh dấu đã dùng</button>
                                    </form>
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
