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

            <c:choose>
                <c:when test="${empty showtimes}">
                    <div class="empty">
                        <h3>Không có suất chiếu còn mở đặt vé trong ngày này</h3>
                    </div>
                </c:when>
                <c:otherwise>
                    <div class="showtime-chips showtime-chips--large">
                        <c:forEach var="st" items="${showtimes}">
                            <a class="showtime-chip" href="${ctx}/booking/seats?showtimeId=${st.id}">
                                <div class="t">${st.startHour}</div>
                                <div class="sub"><c:out value="${st.hallType}"/> · <c:out value="${st.hallName}"/></div>
                            </a>
                        </c:forEach>
                    </div>
                </c:otherwise>
            </c:choose>
        </div>
    </div>

    <jsp:include page="/pages/common/footer.jsp" />
</body>
</html>
