<%--
    RapViet Cinema - Lịch sử đặt vé (Booking history) - KHÁCH
    Module: Đặt vé   (Group6 - Huy)
    Được phục vụ bởi controller.BookingHistoryController  ->  URL /my-bookings
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
    <title>Vé của tôi - RapViet Cinema</title>
    <link rel="stylesheet" href="${ctx}/assets/css/style.css">
    <link rel="stylesheet" href="${ctx}/assets/css/movie.css">
</head>
<body>
    <jsp:include page="/pages/common/header.jsp">
        <jsp:param name="active" value="bookings"/>
    </jsp:include>

    <div class="page-wrap">
    <div class="container">
        <h1 class="page-title">Lịch sử đặt vé</h1>
        <p class="result-meta">Theo dõi các đơn vé và trạng thái thanh toán của bạn.</p>

        <c:choose>
            <%-- ── Chưa có đơn nào ── --%>
            <c:when test="${empty bookings}">
                <div class="empty">
                    <h3>Bạn chưa có đơn đặt vé nào</h3>
                    <p>Hãy chọn phim và đặt vé để bắt đầu nhé.</p>
                    <p><a class="btn btn-primary" href="${ctx}/booking/start">Đặt vé ngay</a></p>
                </div>
            </c:when>

            <%-- ── Danh sách đơn (mỗi đơn = 1 BookingView) ── --%>
            <c:otherwise>
                <div class="booking-list">
                    <c:forEach var="bk" items="${bookings}">
                        <div class="booking-row">
                            <div class="booking-main">
                                <h3><a href="${ctx}/my-booking?id=${bk.booking.id}"><c:out value="${bk.movieTitle}"/></a></h3>
                                <div class="booking-meta">
                                    🕒 ${bk.showTimeLabel}
                                    &middot; 🏢 <c:out value="${bk.branchName}"/>
                                    &middot; 🎬 <c:out value="${bk.hallName}"/>
                                </div>
                                <div class="booking-meta">
                                    💺 ${bk.seatCount} ghế (<c:out value="${bk.seatLabels}"/>)
                                </div>
                            </div>
                            <div class="booking-side">
                                <span class="badge ${bk.statusBadgeClass}">${bk.statusLabel}</span>
                                <div class="booking-total">${bk.totalPriceLabel}</div>
                                <div class="booking-code">#${bk.booking.id}</div>
                                <c:if test="${bk.reviewable}">
                                    <a class="btn btn-primary booking-review-btn"
                                       href="${ctx}/review?bookingId=${bk.booking.id}&movieId=${bk.movieId}">★ Đánh giá</a>
                                </c:if>
                            </div>
                        </div>
                    </c:forEach>
                </div>
            </c:otherwise>
        </c:choose>
    </div>
    </div>

    <jsp:include page="/pages/common/footer.jsp" />
</body>
</html>
