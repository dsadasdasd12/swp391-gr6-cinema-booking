<%--
    Component: Header dùng chung cho toàn site.
    Cách dùng:
        <jsp:include page="/pages/common/header.jsp">
            <jsp:param name="active" value="movies"/>   (home | movies)
        </jsp:include>
    (Group6 - DuyThai)
--%>
<%@ page contentType="text/html; charset=UTF-8" pageEncoding="UTF-8" %>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<c:set var="ctx" value="${pageContext.request.contextPath}"/>
<c:set var="currentUser" value="${sessionScope.user}"/>
<header class="site-header">
    <a href="${ctx}/home" class="brand">🎬 RapViet</a>
    <nav class="main-nav">
        <a href="${ctx}/home"   class="${param.active == 'home'   ? 'active' : ''}">Trang chủ</a>
        <a href="${ctx}/movies" class="${param.active == 'movies' ? 'active' : ''}">Phim</a>
        <a href="${ctx}/movies?status=NOW_SHOWING">Đang chiếu</a>
        <a href="${ctx}/movies?status=COMING_SOON">Sắp chiếu</a>
    </nav>
    <div class="header-actions">
        <c:choose>
            <c:when test="${not empty currentUser}">
                <span class="nav-username">
                    Xin chào, <strong><c:out value="${currentUser.fullName}"/></strong>
                </span>
                <c:if test="${currentUser.admin}">
                    <a href="${ctx}/admin/movies?action=list" class="btn btn-ghost">Quản trị</a>
                </c:if>
                <a href="${ctx}/logout" class="btn btn-primary">Đăng xuất</a>
            </c:when>
            <c:otherwise>
                <a href="${ctx}/login" class="btn btn-ghost">Đăng nhập</a>
                <a href="${ctx}/register" class="btn btn-primary">Đăng ký</a>
            </c:otherwise>
        </c:choose>
    </div>
</header>
