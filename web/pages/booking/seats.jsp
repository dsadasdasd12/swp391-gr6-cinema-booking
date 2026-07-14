<%@ page contentType="text/html; charset=UTF-8" pageEncoding="UTF-8" %>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<c:set var="ctx" value="${pageContext.request.contextPath}" />
<c:set var="st" value="${seatMap.showtime}" />

<!DOCTYPE html>
<html lang="vi">
<head>
    <meta charset="UTF-8">
    <meta name="viewport" content="width=device-width, initial-scale=1">
    <title>Chọn ghế - RapViet Cinema</title>
    <style>
        <c:forEach items="${allSeatTypes}" var="seatType">
        .seat.available.${seatType.code}:not(.selected) { background-color: ${seatType.color} !important; }
        .seat-choice input:checked + .seat.available.${seatType.code} {
            background-color: #f59e0b !important;
            border-color: #fef3c7 !important;
            color: #111827 !important;
            box-shadow: 0 0 0 3px #b45309 !important;
        }
        </c:forEach>
    </style>
    <link rel="stylesheet" href="${ctx}/assets/css/style.css">
    <link rel="stylesheet" href="${ctx}/assets/css/movie.css">
</head>
<body>
    <jsp:include page="/pages/common/header.jsp">
        <jsp:param name="active" value="booking"/>
    </jsp:include>

    <div class="page-wrap">
        <div class="container">
            <c:choose>
                <c:when test="${notFound or empty seatMap}">
                    <div class="empty">
                        <h3>Không tìm thấy suất chiếu còn mở đặt vé</h3>
                        <p><a class="btn btn-primary" href="${ctx}/booking/start">Chọn lại</a></p>
                    </div>
                </c:when>
                <c:otherwise>
                    <div class="flow-head">
                        <div>
                            <div class="flow-step">Bước 4 / 5</div>
                            <h1 class="page-title"><c:out value="${st.movieTitle}"/></h1>
                            <div class="result-meta">
                                <c:out value="${st.branchName}"/> · <c:out value="${st.hallName}"/> · ${st.showDate} ${st.startHour}
                            </div>
                        </div>
                    </div>

                    <c:if test="${not empty error}">
                        <div class="notice err"><c:out value="${error}"/></div>
                    </c:if>

                    <form method="post" action="${ctx}/booking/seats">
                        <input type="hidden" name="showtimeId" value="${st.id}">

                        <div class="seat-legend" style="flex-wrap: wrap; gap: 10px 20px;">
                            <span><i class="seat available"></i> Còn trống</span>
                            <span><i class="seat booked"></i> Đã đạt</span>
                            <span><i class="seat maintenance"></i> Bảo trì</span>
                            <c:forEach items="${allSeatTypes}" var="seatType">
                                <c:if test="${seatType.status == 'ACTIVE' && seatType.code != 'STANDARD'}">
                                    <span><i class="seat available ${seatType.code}"></i> ${seatType.name}</span>
                                </c:if>
                            </c:forEach>
                        </div>

                        <div class="screen">MÀN HÌNH</div>

                        <div class="seat-map booking-seat-map">
                            <c:forEach var="row" items="${seatMap.rows}">
                                <div class="seat-row">
                                    <span class="row-label"><c:out value="${row.rowLabel}"/></span>
                                    <c:forEach var="sv" items="${row.seats}">
                                        <c:choose>
                                            <c:when test="${sv.selectable}">
                                                <label class="seat-choice">
                                                    <input type="checkbox" name="seatIds" value="${sv.seat.id}">
                                                    <span class="seat ${sv.statusClass} ${sv.seat.seatType}"
                                                          title="${sv.seat.seatCode} - ${sv.statusLabel}">
                                                        ${sv.seat.seatNumber}
                                                    </span>
                                                </label>
                                            </c:when>
                                            <c:otherwise>
                                                <span class="seat ${sv.statusClass} ${sv.seat.seatType}"
                                                      title="${sv.seat.seatCode} - ${sv.statusLabel}">
                                                    ${sv.seat.seatNumber}
                                                </span>
                                            </c:otherwise>
                                        </c:choose>
                                    </c:forEach>
                                </div>
                            </c:forEach>
                        </div>

                        <div class="bd-actions">
                            <a class="btn btn-ghost" href="${ctx}/booking/showtimes?branchId=${st.branchId}&movieId=${st.movieId}">Quay lại</a>
                            <button type="submit" class="btn btn-primary">Tiếp tục</button>
                        </div>
                    </form>
                </c:otherwise>
            </c:choose>
        </div>
    </div>

    <jsp:include page="/pages/common/footer.jsp" />
</body>
</html>
