<%--
    Component: Header dung chung cho toan site.
--%>
<%@ page contentType="text/html; charset=UTF-8" pageEncoding="UTF-8" %>
<%@ page import="model.User" %>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<c:set var="ctx" value="${pageContext.request.contextPath}" />
<c:set var="currentUser" value="${sessionScope.user}" />

<header class="site-header">
    <a href="${ctx}/home" class="brand">RapViet</a>
    <nav class="main-nav">
        <a href="${ctx}/home" class="${param.active == 'home' ? 'active' : ''}">Trang chủ</a>
        <a href="${ctx}/movies" class="${param.active == 'movies' ? 'active' : ''}">Phim</a>
        <a href="${ctx}/showtimes" class="${param.active == 'showtimes' ? 'active' : ''}">Suất chiếu</a>
        <a href="${ctx}/movies?status=NOW_SHOWING">Đang chiếu</a>
        <a href="${ctx}/movies?status=COMING_SOON">Sắp chiếu</a>
        <a href="${ctx}/booking/start" class="${param.active == 'booking' ? 'active' : ''}">Đặt vé</a>
        <c:if test="${sessionScope.user != null}">
            <a href="${ctx}/my-bookings" class="${param.active == 'bookings' ? 'active' : ''}">Vé của tôi</a>
        </c:if>
        <c:if test="${sessionScope.user != null and (sessionScope.user.role == 'STAFF' or sessionScope.user.role == 'MANAGER' or sessionScope.user.role == 'ADMIN')}">
            <a href="${ctx}/staff/bookings" class="${param.active == 'staff-bookings' ? 'active' : ''}">Quản lý booking</a>
        </c:if>
    </nav>
    <div class="header-actions">
        <c:choose>
            <c:when test="${sessionScope.user == null}">
                <a href="${ctx}/login" class="btn btn-ghost">Đăng nhập</a>
                <a href="${ctx}/register" class="btn btn-primary">Đăng ký</a>
            </c:when>
            <c:otherwise>
                <div style="display:flex; align-items:center; gap:12px;">
                    <span style="
                          display:flex;
                          align-items:center;
                          gap:6px;
                          padding:8px 14px;
                          background:#16181d;
                          border:1px solid #2c3038;
                          border-radius:999px;
                          color:#fff;
                          font-size:14px;
                          ">
                        <span style="color:#cbd5e1;">Xin chào,</span>
                        <a href="${ctx}/profile"
                           style="
                           color:#e50914 !important;
                           font-weight:700;
                           text-decoration:none;
                           ">
                            ${sessionScope.user.fullName}
                        </a>
                    </span>
                    <a href="${ctx}/logout" class="btn btn-primary">Đăng xuất</a>
                </div>
            </c:otherwise>
        </c:choose>
    </div>
</header>
