<%--
    BOOKING STEP 3 - Select showtime
    GET /booking/showtimes?branchId=&movieId=&date= -> controller.BookingController#showShowtimes
    -> service.ShowtimeService -> dao.ShowtimeDAO -> showtime data -> JSP.
    The date/week UI is only navigation. A showtime button must submit its real showtimeId
    to /booking/seats; never infer an id from the visible time or room text.
--%>
<%@ page contentType="text/html; charset=UTF-8" pageEncoding="UTF-8" %>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<c:set var="ctx" value="${pageContext.request.contextPath}" />

<!DOCTYPE html>
<html lang="vi">
<head>
    <meta charset="UTF-8">
    <meta name="viewport" content="width=device-width, initial-scale=1">
    <title>Chọn suất chiếu - RapViet Cinema</title>
    <link rel="stylesheet" href="${ctx}/assets/css/style.css">
    <link rel="stylesheet" href="${ctx}/assets/css/movie.css">
</head>
<body>
    <jsp:include page="/pages/common/header.jsp">
        <jsp:param name="active" value="booking"/>
    </jsp:include>

    <div class="page-wrap">
        <div class="container">
            <p style="margin:0 0 16px;">
                <a href="${ctx}/booking/movies?branchId=${branch.id}" style="color:#9aa0aa;">← Chọn phim khác</a>
            </p>

            <div class="flow-head">
                <div>
                    <div class="flow-step">Bước 3 / 5</div>
                    <h1 class="page-title"><c:out value="${movie.title}"/></h1>
                    <div class="result-meta"><c:out value="${branch.name}"/></div>
                </div>
            </div>

            <%-- Keep branchId + movieId hidden when changing date; only date should change in this GET request. --%>
            <form class="st-toolbar" method="get" action="${ctx}/booking/showtimes">
                <input type="hidden" name="branchId" value="${branch.id}">
                <input type="hidden" name="movieId" value="${movie.id}">
                <div class="filter-field">
                    <label for="date">Ngày chiếu</label>
                    <input type="date" id="date" name="date" value="${selectedDate}">
                </div>
                <div class="filter-field">
                    <label>&nbsp;</label>
                    <button class="btn btn-primary" type="submit">Xem suất</button>
                </div>
            </form>

            <%-- LinkedHashMap is prepared by BookingController: Monday first, then all seven dates. --%>
            <section class="booking-schedule" aria-label="Lịch chiếu trong tuần">
                <div class="booking-calendar-heading">
                    <span class="booking-calendar-heading__icon" aria-hidden="true">▣</span>
                    <div>
                        <strong>Lịch chiếu trong tuần</strong>
                        <span>Chọn một suất để tiếp tục chọn ghế</span>
                    </div>
                </div>
                <div class="week-showtimes">
                    <c:forEach var="day" items="${weeklyShowtimes}">
                        <section class="showtime-day ${day.key == selectedDayLabel ? 'is-selected' : ''}">
                            <h3 class="showtime-day__title">${day.key}</h3>
                            <c:choose>
                                <%-- For a Map.Entry, use `empty day.value`; `${day.empty}` is invalid EL. --%>
                                <c:when test="${empty day.value}">
                                    <p class="result-meta">Không có suất chiếu còn mở đặt vé.</p>
                                </c:when>
                                <c:otherwise>
                                    <div class="showtime-chips showtime-chips--large">
                                        <c:forEach var="st" items="${day.value}">
                                            <a class="showtime-chip" href="${ctx}/booking/seats?showtimeId=${st.id}">
                                                <div class="t">${st.startHour}</div>
                                                <div class="sub"><c:out value="${st.hallType}"/> · <c:out value="${st.hallName}"/></div>
                                            </a>
                                        </c:forEach>
                                    </div>
                                </c:otherwise>
                            </c:choose>
                        </section>
                    </c:forEach>
                </div>
            </section>

            <%-- Legacy one-day renderer retained below temporarily for comparison; do not execute it. --%>
            <%--
            <c:choose>
                <c:when test="${empty showtimes}">
                    <div class="empty">
                        <h3>Không có suất chiếu còn mở đặt vé trong ngày này</h3>
                    </div>
                </c:when>
                <c:otherwise>
                    <div class="showtime-chips showtime-chips--large">
                        <!-- Legacy note: st.id is the authoritative key required by Seat/Booking services. -->
                        <c:forEach var="st" items="${showtimes}">
                            <a class="showtime-chip" href="${ctx}/booking/seats?showtimeId=${st.id}">
                                <div class="t">${st.startHour}</div>
                                <div class="sub"><c:out value="${st.hallType}"/> · <c:out value="${st.hallName}"/></div>
                            </a>
                        </c:forEach>
                    </div>
                </c:otherwise>
            </c:choose>
            --%>
        </div>
    </div>

    <jsp:include page="/pages/common/footer.jsp" />
</body>
</html>
