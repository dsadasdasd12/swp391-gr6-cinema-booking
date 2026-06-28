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
            <li><a href="${ctx}/favorite-films" class="active">❤️ Favorite Film</a></li>
            <li><a href="${ctx}/transaction-history">🧾 Transaction History</a></li>
        </ul>
    </aside>

    <section class="profile-content">
        <div class="section-head">
            <h2>Phim yêu thích</h2>
        </div>

        <div class="favorite-grid">
            <div class="favorite-card">
                <div class="favorite-poster">🎬</div>
                <div>
                    <h3>Avengers: Endgame</h3>
                    <p>Hành động • 181 phút</p>
                    <a href="#" class="btn btn-primary">Xem chi tiết</a>
                </div>
            </div>

            <div class="favorite-card">
                <div class="favorite-poster">🍿</div>
                <div>
                    <h3>Inside Out 2</h3>
                    <p>Hoạt hình • 96 phút</p>
                    <a href="#" class="btn btn-primary">Xem chi tiết</a>
                </div>
            </div>

            <div class="favorite-card">
                <div class="favorite-poster">🎥</div>
                <div>
                    <h3>Mai</h3>
                    <p>Tâm lý • 131 phút</p>
                    <a href="#" class="btn btn-primary">Xem chi tiết</a>
                </div>
            </div>
        </div>
    </section>
</div>

<jsp:include page="/pages/common/footer.jsp"/>

</body>
</html>