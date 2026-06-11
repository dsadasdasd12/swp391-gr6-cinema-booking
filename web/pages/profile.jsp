<%@ page contentType="text/html; charset=UTF-8" pageEncoding="UTF-8" %>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>

<c:set var="ctx" value="${pageContext.request.contextPath}" />

<!DOCTYPE html>
<html lang="vi">
    <head>
        <meta charset="UTF-8">
        <title>Thông tin cá nhân - RapViet Cinema</title>

        <link rel="stylesheet" href="${ctx}/assets/css/style.css">
        <link rel="stylesheet" href="${ctx}/assets/css/staffprofile.css">
    </head>

    <body>

        <jsp:include page="/pages/common/header.jsp">
            <jsp:param name="active" value="profile"/>
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

        <div class="profile-container">

            <div class="profile-card-simple">

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

                    <div class="profile-row">
                        <span>Vị trí</span>
                        <strong>

                            <c:choose>

                                <c:when test="${sessionScope.user.role == 'ADMIN'}">
                                    Quản trị viên
                                </c:when>

                                <c:when test="${sessionScope.user.role == 'MANAGER'}">
                                    Quản lý
                                </c:when>

                                <c:when test="${sessionScope.user.role == 'STAFF'}">
                                    Nhân viên
                                </c:when>

                                <c:otherwise>
                                    ${sessionScope.user.role}
                                </c:otherwise>

                            </c:choose>

                        </strong>
                    </div>

                </div>

                <div class="profile-actions">

                    <a href="${ctx}/home" class="btn btn-ghost">
                        Quay lại trang chủ
                    </a>

                    <c:if test="${sessionScope.user.role != 'ADMIN'}">
                        <a href="${ctx}/profile/edit" class="btn btn-primary">
                            Chỉnh sửa thông tin
                        </a>
                    </c:if>

                    <button type="button"
                            class="btn btn-ghost"
                            onclick="openPasswordModal()">
                        Đổi mật khẩu
                    </button>

                </div>

            </div>

        </div>

        <!-- PASSWORD MODAL -->

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