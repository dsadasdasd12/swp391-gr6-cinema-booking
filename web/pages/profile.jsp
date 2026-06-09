<%@ page contentType="text/html; charset=UTF-8" pageEncoding="UTF-8" %>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>

<c:set var="ctx" value="${pageContext.request.contextPath}" />

<!DOCTYPE html>
<html lang="vi">
<head>
    <meta charset="UTF-8">
    <title>Thông tin cá nhân - RapViet Cinema</title>
    <link rel="stylesheet" href="${ctx}/assets/css/style.css">
    <link rel="stylesheet" href="${ctx}/assets/css/profile.css">
</head>

<body>

<jsp:include page="/pages/common/header.jsp">
    <jsp:param name="active" value="profile"/>
</jsp:include>

<div class="page-wrap">

    <section class="profile-hero">
        <div class="profile-card">

            <div class="profile-left">
                <div class="profile-avatar">
                    ${sessionScope.user.fullName.substring(0,1)}
                </div>

                <h2>${sessionScope.user.fullName}</h2>
                <p>Thành viên RapViet Cinema</p>
            </div>

            <div class="profile-right">

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
                        <span>Chức vụ</span>
                        <strong>${sessionScope.user.role}</strong>
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

                    <a href="#" class="btn btn-primary">
                        Chỉnh sửa thông tin
                    </a>
                </div>

            </div>

        </div>
    </section>

</div>

<jsp:include page="/pages/common/footer.jsp"/>

</body>
</html>