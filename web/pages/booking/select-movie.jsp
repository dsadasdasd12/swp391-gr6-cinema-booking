<%@ page contentType="text/html; charset=UTF-8" pageEncoding="UTF-8" %>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<c:set var="ctx" value="${pageContext.request.contextPath}" />

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
                    <h1 class="page-title">Chọn phim tại <c:out value="${branch.name}"/></h1>
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
                        <c:forEach var="m" items="${movies}">
                            <div class="booking-movie-card">
                                <a href="${ctx}/movie?id=${m.id}" class="booking-movie-poster">
                                    <img src="${empty m.posterUrl ? m.posterFallbackUrl : m.posterUrl}"
                                         alt="<c:out value='${m.title}'/>"
                                         onerror="this.onerror=null;this.src='${m.posterFallbackUrl}'">
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
