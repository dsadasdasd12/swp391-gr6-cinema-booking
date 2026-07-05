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
                        <c:if test="${param.msg == 'booking_created'}">
                            <div class="notice ok">Đã tạo booking. Vui lòng hoàn tất thanh toán để vé được xác nhận.</div>
                        </c:if>
                        <c:if test="${param.msg == 'cancel_failed'}">
                            <div class="notice err">Không thể hủy đơn này (đơn đã sử dụng/đã hủy hoặc không cho phép hủy).</div>
                        </c:if>

                        <div class="booking-detail">
                            <div class="bd-head">
                                <h1><c:out value="${bk.movieTitle}"/></h1>
                                <span class="badge ${bk.statusBadgeClass}">${bk.statusLabel}</span>
                            </div>

                            <div class="booking-layout">

                                <div class="booking-summary">

                                    <div class="booking-summary-item">
                                        <div class="booking-summary-label"><b>Mã đơn:</b> <span class="booking-summary-value">#${bk.booking.id}</span></div>

                                    </div>

                                    <div class="booking-summary-item">
                                        <div class="booking-summary-label"><b>Suất chiếu:</b> <span class="booking-summary-value">${bk.showTimeLabel}</span></div>

                                    </div>

                                    <div class="booking-summary-item">
                                        <div class="booking-summary-label"><b>Chi nhánh:</b> <span class="booking-summary-value">${bk.branchName}</span></div>

                                    </div>

                                    <div class="booking-summary-item">
                                        <div class="booking-summary-label"><b>Phòng:</b> <span class="booking-summary-value">${bk.hallName}</span></div>

                                    </div>

                                    <div class="booking-summary-item">
                                        <div class="booking-summary-label"><b>Ghế:</b> <span class="booking-summary-value">${bk.seatLabels}</span></div>

                                    </div>

                                    <div class="booking-summary-item">
                                        <div class="booking-summary-label"><b>Tổng tiền:</b> <span class="booking-summary-value">${bk.totalPriceLabel}</span></div>

                                    </div>

                                </div>
                                <div class="booking-payment">
                                    <c:if test="${bk.booking.status == 'PENDING'}">
                                        <div class="booking-qr-right">
                                            <h3>Thanh toán đơn vé</h3>

                                            <img class="payment-qr-img"
                                                 src="${paymentQr}"
                                                 alt="QR thanh toán">

                                            <p><b>Số tiền:</b> ${bk.totalPriceLabel}</p>

                                            <p>
                                                <b>Nội dung CK:</b> <code>${transferContent}</code><br>
                                                
                                            </p>
                                        </div>
                                    </c:if>
                                </div>

                            </div>

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
    <c:if test="${bk.booking.status == 'PENDING'}">
        <script>
            const bookingId = "${bk.booking.id}";
            const ctx = "${ctx}";

            setInterval(function () {
                fetch(ctx + "/payment/status?bookingId=" + bookingId)
                        .then(function (response) {
                            return response.json();
                        })
                        .then(function (data) {
                            if (data.paid === true) {
                                window.location.href = ctx + "/booking/success?bookingId=" + bookingId;
                            }
                        })
                        .catch(function (error) {
                            console.log("Payment polling error:", error);
                        });
            }, 3000);
        </script>
    </c:if>
</html>
