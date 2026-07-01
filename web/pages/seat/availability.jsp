<%--
    RapViet Cinema - Sơ đồ ghế của một suất chiếu (Xem tình trạng ghế)
    Module: Duyệt phim - View seat availability   (Group6 - Huy)
    Được phục vụ bởi controller.SeatAvailabilityController  ->  URL /seats?showtimeId=N
    View chỉ dùng JSTL + EL + component, KHÔNG nhúng code Java.
--%>
<%@ page contentType="text/html; charset=UTF-8" pageEncoding="UTF-8" %>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<c:set var="ctx" value="${pageContext.request.contextPath}" />
<c:set var="st" value="${seatMap.showtime}" />

<!DOCTYPE html>
<html lang="vi">
<head>
    <meta charset="UTF-8">
    <meta name="viewport" content="width=device-width, initial-scale=1">
    <title><c:out value="${empty st ? 'Không tìm thấy suất chiếu' : st.movieTitle}"/> - Sơ đồ ghế - RapViet Cinema</title>
    <link rel="stylesheet" href="${ctx}/assets/css/style.css">
    <link rel="stylesheet" href="${ctx}/assets/css/movie.css">
</head>
<body>
    <jsp:include page="/pages/common/header.jsp">
        <jsp:param name="active" value="showtimes"/>
    </jsp:include>

    <div class="page-wrap">
    <div class="container">
        <c:choose>
            <%-- ── Không tìm thấy suất chiếu ── --%>
            <c:when test="${notFound or empty seatMap}">
                <div class="empty">
                    <h3>Không tìm thấy suất chiếu</h3>
                    <p>Suất chiếu bạn chọn không tồn tại hoặc đã bị gỡ.</p>
                    <p><a class="btn btn-primary" href="${ctx}/showtimes">← Về lịch chiếu</a></p>
                </div>
            </c:when>

            <%-- ── Sơ đồ ghế ── --%>
            <c:otherwise>
                <%-- Ngữ cảnh suất chiếu: phim / rạp / phòng / ngày giờ --%>
                <div class="seat-context">
                    <h1><c:out value="${st.movieTitle}"/></h1>
                    <ul class="seat-meta">
                        <li>🏢 <c:out value="${st.branchName}"/></li>
                        <li>🎬 Phòng <c:out value="${st.hallName}"/> (<c:out value="${st.hallType}"/>)</li>
                        <li>📅 ${st.showDate}</li>
                        <li>🕒 ${st.startHour} - ${st.endHour}</li>
                    </ul>
                    <div class="result-meta">
                        Còn trống <strong>${seatMap.availableSeats}</strong> / ${seatMap.totalSeats} ghế
                    </div>
                </div>

                <%-- Chú thích màu trạng thái --%>
                <div class="seat-legend">
                    <span><i class="seat available"></i> Còn trống</span>
                    <span><i class="seat booked"></i> Đã đặt</span>
                    <span><i class="seat maintenance"></i> Bảo trì</span>
                    <span><i class="seat available VIP"></i> Ghế VIP</span>
                    <span><i class="seat available COUPLE"></i> Ghế đôi</span>
                </div>

                <%-- Biểu tượng màn hình --%>
                <div class="screen">MÀN HÌNH</div>

                <%-- Lưới ghế: vòng ngoài theo hàng, vòng trong theo ghế --%>
                <div class="seat-map">
                    <c:forEach var="row" items="${seatMap.rows}">
                        <div class="seat-row">
                            <span class="row-label"><c:out value="${row.rowLabel}"/></span>
                            <%-- sv là dto.SeatView: sv.seat = entity Seat (cột DB),
                                 sv.statusClass/statusLabel = trạng thái suy theo suất chiếu --%>
                            <c:forEach var="sv" items="${row.seats}">
                                <span class="seat ${sv.statusClass} ${sv.seat.seatType}"
                                      title="${sv.seat.seatCode} - ${sv.statusLabel}">
                                    ${sv.seat.seatNumber}
                                </span>
                            </c:forEach>
                        </div>
                    </c:forEach>
                </div>

                <p style="margin-top:24px;">
                    <a href="${ctx}/showtimes?branchId=${st.branchId}" style="color:#9aa0aa;">← Về lịch chiếu</a>
                </p>
            </c:otherwise>
        </c:choose>
    </div>
    </div>

    <jsp:include page="/pages/common/footer.jsp" />
</body>
</html>
