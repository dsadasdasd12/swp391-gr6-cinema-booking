<%@ page contentType="text/html; charset=UTF-8" pageEncoding="UTF-8" %>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>

<c:set var="ctx" value="${pageContext.request.contextPath}" />

<!DOCTYPE html>
<html lang="vi">
<head>
    <meta charset="UTF-8">
    <title>Thông tin cá nhân - RapViet Cinema</title>

    <link rel="stylesheet" href="${ctx}/assets/css/style.css">
    <link rel="stylesheet" href="${ctx}/assets/css/customerprofile.css">
</head>

<body>

<jsp:include page="/pages/common/header.jsp">
    <jsp:param name="active" value="customerprofile"/>
</jsp:include>
<c:if test="${not empty sessionScope.profileSuccess}">
    <div class="success-message">
        ${sessionScope.profileSuccess}
        <c:remove var="profileSuccess" scope="session"/>
    </div>
</c:if>

<c:if test="${not empty sessionScope.profileError}">
    <div class="error-message">
        ${sessionScope.profileError}
        <c:remove var="profileError" scope="session"/>
    </div>
</c:if>
<div class="profile-page">

    <aside class="profile-sidebar">

        <div class="profile-user-box">
            <div class="profile-avatar">
                ${sessionScope.user.fullName.substring(0,1)}
            </div>

            <h3>${sessionScope.user.fullName}</h3>
            <p>Thành viên RapViet Cinema</p>
        </div>

        <ul class="profile-menu">
            <li>
                <a href="${ctx}/profile" class="active">
                    👤 Personal Detail
                </a>
            </li>

            <li>
                <a href="${ctx}/favorite-movie">
                    ❤️ Favorite Film
                </a>
            </li>

            <li>
                <a href="${ctx}/transaction-history">
                    🧾 Transaction History
                </a>
            </li>
        </ul>

    </aside>

    <section class="profile-content">

        <div class="section-head">
            <h2>Thông tin cá nhân</h2>
        </div>

        <div class="profile-info">

            <div class="profile-row">
                <span>Họ và tên</span>
                <strong>${sessionScope.user.fullName}</strong>
            </div>

            <div class="profile-row">
                <span>Email</span>
                <strong>${sessionScope.user.email}</strong>
            </div>

            <div class="profile-row">
                <span>Số điện thoại</span>
                <strong>
                    <c:choose>
                        <c:when test="${empty sessionScope.user.phone}">
                            Chưa cập nhật
                        </c:when>
                        <c:otherwise>
                            ${sessionScope.user.phone}
                        </c:otherwise>
                    </c:choose>
                </strong>
            </div>

        </div>

        <div class="profile-actions">

            <a href="${ctx}/home" class="btn btn-ghost">
                Quay lại trang chủ
            </a>

            <a href="${ctx}/profile/edit" class="btn btn-primary">
                Chỉnh sửa thông tin
            </a>

            <button type="button"
                    class="btn btn-ghost"
                    onclick="openPasswordModal()">
                Đổi mật khẩu
            </button>

        </div>

    </section>

</div>

<div id="passwordModal" class="modal-overlay">

    <div class="password-modal">

        <div class="modal-head">
            <h2>Đổi mật khẩu</h2>

            <button type="button"
                    class="modal-close"
                    onclick="closePasswordModal()">
                ×
            </button>
        </div>

        <jsp:include page="/pages/change-password.jsp"/>

    </div>

</div>

<jsp:include page="/pages/common/footer.jsp"/>

<script>
    function openPasswordModal() {
        document.getElementById("passwordModal").classList.add("show");
    }

    function closePasswordModal() {
        document.getElementById("passwordModal").classList.remove("show");
    }
</script>

</body>
</html>