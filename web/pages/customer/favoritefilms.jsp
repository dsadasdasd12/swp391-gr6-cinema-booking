<%@ page contentType="text/html; charset=UTF-8" pageEncoding="UTF-8" %>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<c:set var="ctx" value="${pageContext.request.contextPath}" />

<!DOCTYPE html>
<html lang="vi">
    <head>
        <meta charset="UTF-8">
        <title>Phim yêu thích - RapViet Cinema</title>
        <link rel="stylesheet" href="${ctx}/assets/css/style.css">
        <link rel="stylesheet" href="${ctx}/assets/css/customerprofile.css">
    </head>
    <body>

        <jsp:include page="/pages/common/header.jsp">
            <jsp:param name="active" value="customerprofile"/>
        </jsp:include>

        <div class="profile-page">
            <aside class="profile-sidebar">
                <div class="profile-user-box">
                    <div class="profile-avatar">${sessionScope.user.fullName.substring(0,1)}</div>
                    <h3>${sessionScope.user.fullName}</h3>
                    <p>Thành viên RapViet Cinema</p>
                </div>

                <ul class="profile-menu">
                    <li><a href="${ctx}/profile">👤 Personal Detail</a></li>
                    <li>
                        <a href="${ctx}/favorite-movie" class="active">❤️ Favorite Film</a>
                    </li>
                    <li><a href="${ctx}/transaction-history">🧾 Transaction History</a></li>
                </ul>
            </aside>

            <section class="profile-content">
                <div class="section-head">
                    <h2>Phim yêu thích</h2>
                </div>

                <div class="favorite-grid">

                    <c:choose>
                        <c:when test="${empty favoriteMovies}">
                            <div class="empty">
                                <h3>Chưa có phim yêu thích</h3>
                                <p>Bạn chưa thêm phim nào vào danh sách yêu thích.</p>
                                <a href="${ctx}/movies" class="btn btn-primary">Khám phá phim</a>
                            </div>
                        </c:when>

                        <c:otherwise>
                            <c:forEach var="movie" items="${favoriteMovies}">
                                <div class="favorite-card">

                                    <div class="favorite-poster">
                                        <c:choose>
                                            <c:when test="${not empty movie.posterUrl}">
                                                <img src="${movie.posterUrl}"
                                                     class="img-fluid rounded-start w-100 h-100 object-fit-cover" alt="Poster ${movie.title}">
                                            </c:when>
                                            <c:otherwise>
                                                🎬
                                            </c:otherwise>
                                        </c:choose>
                                    </div>

                                    <div class="favorite-info">
                                        <h3><c:out value="${movie.title}"/></h3>

                                        <p>
                                            <c:out value="${movie.categoryNames}"/>
                                            <c:if test="${not empty movie.categoryNames}">
                                                •
                                            </c:if>
                                            <c:out value="${movie.durationLabel}"/>
                                        </p>

                                        <a href="${ctx}/movie?id=${movie.id}"
                                           class="btn btn-primary">
                                            Xem chi tiết
                                        </a>
                                    </div>

                                </div>
                            </c:forEach>
                        </c:otherwise>
                    </c:choose>

                </div>
        </div>
    </section>
</div>

<jsp:include page="/pages/common/footer.jsp"/>

</body>
</html>
