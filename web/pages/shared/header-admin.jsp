<%--
    Rạp Việt CMS - Premium Admin Header
    Include: <%@ include file="/pages/shared/header-admin.jsp" %>
    Requires: sessionScope.adminUser or sessionScope.user
--%>
<%@ page contentType="text/html; charset=UTF-8" pageEncoding="UTF-8" %>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<c:set var="ctx" value="${pageContext.request.contextPath}" />
<!DOCTYPE html>
<html lang="vi">
<head>
    <meta charset="UTF-8">
    <meta name="viewport" content="width=device-width, initial-scale=1">
    <title><c:out value="${not empty pageTitle ? pageTitle : 'Rạp Việt CMS - Quản trị hệ thống'}" /></title>

    <link href="https://cdn.jsdelivr.net/npm/bootstrap@5.3.3/dist/css/bootstrap.min.css"
          rel="stylesheet"
          integrity="sha384-QWTKZyjpPEjISv5WaRU9OFeRpok6YctnYmDr5pNlyT2bRjXh0JMhjY6hW+ALEwIH"
          crossorigin="anonymous">
    <link href="https://cdn.jsdelivr.net/npm/bootstrap-icons@1.11.3/font/bootstrap-icons.css" rel="stylesheet">
    <link href="https://fonts.googleapis.com/css2?family=Inter:wght@400;500;600;700&family=Outfit:wght@600;700;800&display=swap" rel="stylesheet">

    <link rel="stylesheet" href="${ctx}/assets/css/admin/variables.css?v=redblack">
    <link rel="stylesheet" href="${ctx}/assets/css/admin/base.css?v=redblack">
    <link rel="stylesheet" href="${ctx}/assets/css/admin/layout.css?v=redblack">
    <link rel="stylesheet" href="${ctx}/assets/css/admin/components.css?v=redblack">
    <link rel="stylesheet" href="${ctx}/assets/css/admin/tables.css?v=redblack">
    <link rel="stylesheet" href="${ctx}/assets/css/admin/forms.css?v=redblack">

    <script src="${ctx}/assets/js/main.js" charset="UTF-8" defer></script>
    <script src="${ctx}/assets/js/confirm.js" charset="UTF-8" defer></script>
    <script src="https://cdn.jsdelivr.net/npm/bootstrap@5.3.3/dist/js/bootstrap.bundle.min.js"
            integrity="sha384-YvpcrYf0tY3lHB60NNkmXc5s9fDVZLESaAA55NDzOxhy9GkcIdslK1eN7N6jIeHz"
            crossorigin="anonymous" defer></script>

    <c:if test="${not empty extraCss}"><c:out value="${extraCss}" escapeXml="false"/></c:if>
</head>
<body>

<div class="rv-toast-container">
    <c:if test="${not empty sessionScope.flashSuccess}">
        <div class="rv-toast rv-toast--success">
            <i class="bi bi-check-circle-fill rv-toast__icon"></i>
            <div class="rv-toast__content">
                <div class="rv-toast__title">Thành công</div>
                <div class="rv-toast__message"><c:out value="${sessionScope.flashSuccess}"/></div>
            </div>
            <button type="button" class="rv-toast__close">
                <i class="bi bi-x"></i>
            </button>
        </div>
        <c:remove var="flashSuccess" scope="session"/>
    </c:if>
    <c:if test="${not empty sessionScope.flashError}">
        <div class="rv-toast rv-toast--error">
            <i class="bi bi-exclamation-octagon-fill rv-toast__icon"></i>
            <div class="rv-toast__content">
                <div class="rv-toast__title">Lỗi hệ thống</div>
                <div class="rv-toast__message"><c:out value="${sessionScope.flashError}"/></div>
            </div>
            <button type="button" class="rv-toast__close">
                <i class="bi bi-x"></i>
            </button>
        </div>
        <c:remove var="flashError" scope="session"/>
    </c:if>
</div>

<header class="rv-topbar">
    <button type="button" class="rv-topbar__toggle" title="Menu">
        <i class="bi bi-list"></i>
    </button>

    <a class="rv-topbar__brand" href="${ctx}/admin/movies?action=list">
        <div class="rv-topbar__brand-icon">
            <i class="bi bi-film"></i>
        </div>
        <span class="rv-topbar__brand-text">RẠP VIỆT <span>CMS</span></span>
    </a>

    <div class="rv-topbar__actions">
        <button type="button" class="rv-btn rv-btn--ghost rv-btn--icon" style="border:none; border-radius:50%" title="Thông báo">
            <i class="bi bi-bell"></i>
        </button>

        <div class="rv-topbar__user">
            <c:set var="topUser" value="${not empty sessionScope.user ? sessionScope.user : sessionScope.adminUser}" />
            <div class="rv-topbar__avatar">
                <c:choose>
                    <c:when test="${not empty topUser}">
                        <c:out value="${topUser.fullName.substring(0,1).toUpperCase()}" />
                    </c:when>
                    <c:otherwise>A</c:otherwise>
                </c:choose>
            </div>
            <div class="rv-topbar__user-info">
                <span class="rv-topbar__user-name">
                    <c:out value="${not empty topUser ? topUser.fullName : 'Hệ Thống'}" />
                </span>
                <span class="rv-topbar__user-role">
                    <c:choose>
                        <c:when test="${empty topUser}">-</c:when>
                        <c:when test="${topUser.role == 'ADMIN'}">Quản trị viên (Admin)</c:when>
                        <c:when test="${topUser.role == 'MANAGER'}">Quản lý chi nhánh</c:when>
                        <c:otherwise>Nhân viên</c:otherwise>
                    </c:choose>
                </span>
            </div>
            <i class="bi bi-chevron-down rv-topbar__user-arrow"></i>

            <div class="rv-topbar__dropdown">
                <div class="rv-topbar__dropdown-header">
                    <div style="font-weight: 600; color: var(--n-800);">
                        <c:out value="${not empty topUser ? topUser.fullName : 'Admin User'}" />
                    </div>
                    <div class="email">
                        <c:out value="${not empty topUser ? topUser.email : 'admin@rapviet.vn'}" />
                    </div>
                </div>
                <a href="${ctx}/admin/profile" class="rv-topbar__dropdown-item">
                    <i class="bi bi-person-fill"></i>Thông tin cá nhân
                </a>
                <a href="${ctx}/admin/settings" class="rv-topbar__dropdown-item">
                    <i class="bi bi-gear-fill"></i>Cài đặt hệ thống
                </a>
                <div class="rv-topbar__dropdown-divider"></div>
                <a href="${ctx}/logout" class="rv-topbar__dropdown-item danger">
                    <i class="bi bi-box-arrow-right"></i>Đăng xuất
                </a>
            </div>
        </div>
    </div>
</header>

<div class="rv-wrapper">
