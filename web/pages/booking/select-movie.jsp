<%--
    BOOKING STEP 2 - Select movie for the selected branch
    GET /booking/movies?branchId=N -> controller.BookingController#showMovies
    -> service.MovieService / BranchService -> dao.MovieDAO + branch assignment data -> JSP.
    Every movie link must carry branchId and movieId to /booking/showtimes; the controller
    still verifies both values because query parameters can be modified by the browser.
--%>
<%@ page contentType="text/html; charset=UTF-8" pageEncoding="UTF-8" %>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<c:set var="ctx" value="${pageContext.request.contextPath}" />
<c:set var="ph" value="data:image/svg+xml;charset=UTF-8,%3Csvg%20xmlns%3D%22http%3A%2F%2Fwww.w3.org%2F2000%2Fsvg%22%20width%3D%22300%22%20height%3D%22450%22%3E%3Crect%20width%3D%22100%25%22%20height%3D%22100%25%22%20fill%3D%22%2323262d%22%2F%3E%3Ctext%20x%3D%2250%25%22%20y%3D%2250%25%22%20fill%3D%22%237d828c%22%20font-family%3D%22Arial%22%20font-size%3D%2220%22%20text-anchor%3D%22middle%22%20dominant-baseline%3D%22middle%22%3ENo%20Image%3C%2Ftext%3E%3C%2Fsvg%3E" />

<!DOCTYPE html>
<html lang="vi">
    <head>
        <meta charset="UTF-8">
        <meta name="viewport" content="width=device-width, initial-scale=1">
        <title>Chọn phim - RapViet Cinema</title>
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
                    <a href="${ctx}/booking/start" style="color:#9aa0aa;">← Chọn rạp khác</a>
                </p>

                <div class="flow-head">
                    <div>
                        <div class="flow-step">Bước 2 / 5</div>
                        <h1 class="page-title">
                            Chọn phim tại
                            <c:out value="${branch.name}"/>
                        </h1>
                        <!--                    <h1 class="page-title">Chọn phim tại <c:out value="${branch.name}"/></h1>-->
                    </div>
                </div>

                <c:choose>
                    <c:when test="${empty movies}">
                        <div class="empty">
                            <h3>Rạp này chưa có phim còn suất chiếu</h3>
                        </div>
                    </c:when>
                    <c:otherwise>
                        <div class="movie-grid">
                            <%-- Movie list is already limited to the chosen branch by BookingController/MovieService. --%>
                            <c:forEach var="m" items="${movies}">
                                <div class="booking-movie-card">
                                    <a href="${ctx}/movie?id=${m.id}" class="booking-movie-poster">
                                        <img src="${empty m.posterUrl ? ph : m.posterUrl}"
                                             alt="<c:out value='${m.title}'/>"
                                             onerror="this.onerror=null;this.src='${ph}'">
                                    </a>
                                    <div class="movie-body">
                                        <h3><c:out value="${m.title}"/></h3>
                                        <div class="movie-meta">${m.durationLabel}</div>
                                        <div class="movie-genres"><c:out value="${m.categoryNames}"/></div>
                                        <a class="btn btn-primary"
                                           href="${ctx}/booking/showtimes?branchId=${branch.id}&movieId=${m.id}">
                                            Chọn phim
                                        </a>
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
