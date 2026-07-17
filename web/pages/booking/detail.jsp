<%--
    CODE FLOW (Booking details / cancel / status timeline)
    GET /my-booking?id=N -> controller.BookingDetailController -> service.BookingService
    -> dao.BookingDAO and BOOKING_STATUS_HISTORY query -> booking detail + timeline -> JSP.
    POST cancel returns to the same controller; BookingService checks owner, current status,
    and cancellation policy before updating BOOKINGS. The status-history trigger/audit records
    the transition. Do not replace this with a direct JSP/SQL update.

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
        <style>
            .booking-tracker {
                margin-top:24px;
                padding:22px 26px;
                border:1px solid #2c3038;
                border-radius:14px;
                background:#16181d;
            }
            .booking-tracker h2 {
                margin:0 0 6px;
                font-size:20px;
            }
            .booking-tracker__hint {
                margin:0 0 20px;
                color:#9aa0aa;
                font-size:14px;
            }
            .status-timeline {
                list-style:none;
                margin:0;
                padding:0;
            }
            .status-timeline__item {
                position:relative;
                display:grid;
                grid-template-columns:18px 1fr auto;
                gap:12px;
                padding:0 0 20px;
            }
            .status-timeline__item:last-child {
                padding-bottom:0;
            }
            .status-timeline__item:not(:last-child)::before {
                content:"";
                position:absolute;
                left:8px;
                top:18px;
                bottom:0;
                width:2px;
                background:#343944;
            }
            .status-timeline__dot {
                width:18px;
                height:18px;
                border-radius:50%;
                margin-top:2px;
                background:#22c55e;
                box-shadow:0 0 0 4px rgba(34,197,94,.12);
            }
            .status-timeline__item.pending .status-timeline__dot {
                background:#f59e0b;
                box-shadow:0 0 0 4px rgba(245,158,11,.12);
            }
            .status-timeline__item.cancelled .status-timeline__dot {
                background:#ef4444;
                box-shadow:0 0 0 4px rgba(239,68,68,.12);
            }
            .status-timeline__title {
                font-weight:700;
                color:#f8fafc;
            }
            .status-timeline__note {
                margin-top:3px;
                color:#aeb6c5;
                font-size:14px;
            }
            .status-timeline__time {
                color:#9aa0aa;
                font-size:13px;
                white-space:nowrap;
            }
        </style>
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

                                    <!--                                    <div class="booking-summary-item">
                                                                            <div class="booking-summary-label"><b>Tổng tiền:</b> <span class="booking-summary-value">${bk.totalPriceLabel}</span></div>

                                                                        </div>-->
                                    <div class="booking-summary-item">
                                        <div class="booking-summary-label">
                                            <b>Tổng tiền ghế:</b>
                                            <span class="booking-summary-value">
                                                ${bk.seatSubtotalLabel}
                                            </span>
                                        </div>
                                    </div>

                                    <div class="booking-summary-item">
                                        <div class="booking-summary-label">
                                            <b>Ưu đãi mua 5 tặng 1:</b>
                                            <span class="booking-summary-value">
                                                <c:choose>
                                                    <c:when test="${bk.buyFiveDiscount > 0}">
                                                        -${bk.buyFiveDiscountLabel}
                                                        (${bk.freeTicketCount} vé miễn phí)
                                                    </c:when>
                                                    <c:otherwise>
                                                        0 đ
                                                    </c:otherwise>
                                                </c:choose>
                                            </span>
                                        </div>
                                    </div>

                                    <div class="booking-summary-item">
                                        <div class="booking-summary-label">
                                            <b>Voucher:</b>
                                            <span class="booking-summary-value">
                                                <c:choose>
                                                    <c:when test="${bk.voucherDiscount > 0}">
                                                        -${bk.voucherDiscountLabel}
                                                    </c:when>
                                                    <c:otherwise>
                                                        0 đ
                                                    </c:otherwise>
                                                </c:choose>
                                            </span>
                                        </div>
                                    </div>

                                    <div class="booking-summary-item">
                                        <div class="booking-summary-label">
                                            <b>Tổng cần thanh toán:</b>
                                            <span class="booking-summary-value">
                                                ${bk.totalPriceLabel}
                                            </span>
                                        </div>
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

                            <section class="booking-tracker" aria-label="Theo dõi trạng thái đơn vé">
                                <h2>Theo dõi trạng thái đơn vé</h2>
                                <p class="booking-tracker__hint">Các mốc được lưu tự động khi trạng thái đơn vé thay đổi.</p>
                                <%-- statusHistory is the audit timeline, separate from the current BOOKINGS.status badge. --%>
                                <c:choose>
                                    <c:when test="${empty statusHistory}">
                                        <p class="booking-tracker__hint">Chưa có lịch sử trạng thái cho đơn vé này.</p>
                                    </c:when>
                                    <c:otherwise>
                                        <ol class="status-timeline">
                                            <c:forEach var="event" items="${statusHistory}">
                                                <li class="status-timeline__item ${event.statusClass}">
                                                    <span class="status-timeline__dot" aria-hidden="true"></span>
                                                    <div>
                                                        <div class="status-timeline__title"><c:out value="${event.statusLabel}"/></div>
                                                        <c:if test="${not empty event.note}">
                                                            <div class="status-timeline__note"><c:out value="${event.note}"/></div>
                                                        </c:if>
                                                    </div>
                                                    <time class="status-timeline__time"><c:out value="${event.changedAtLabel}"/></time>
                                                </li>
                                            </c:forEach>
                                        </ol>
                                    </c:otherwise>
                                </c:choose>
                            </section>

                            <div class="bd-actions">
                                <%-- Hủy đơn: chỉ hiện khi còn cho phép hủy --%>
                                <%-- Never make this condition the only cancel protection; BookingService checks it again on POST. --%>
                                <c:if test="${bk.cancellable}">
                                    <form method="post" action="${ctx}/my-booking"
                                          onsubmit="return confirm('Bạn chắc chắn muốn hủy đơn này?');">
                                        <input type="hidden" name="id" value="${bk.booking.id}">
                                        <button type="submit" class="btn btn-ghost">Hủy đơn</button>
                                    </form>
                                </c:if>
                                <%-- Đánh giá phim: chỉ khi đã xem (sẽ nối module Đánh giá) --%>
                                <%-- A review link is shown only after attendance; ReviewController/ReviewDAO are the final gate. --%>
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
