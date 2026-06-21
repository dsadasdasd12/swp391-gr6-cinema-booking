<%--
    Component: Header dùng chung cho toàn site.
    Cách dùng:
        <jsp:include page="/pages/common/header.jsp">
            <jsp:param name="active" value="movies"/>   (home | movies)
        </jsp:include>
    (Group6 - DuyThai)
--%>
<%@ page contentType="text/html; charset=UTF-8" pageEncoding="UTF-8" %>
<%@ page import="model.User" %>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<c:set var="ctx" value="${pageContext.request.contextPath}" />
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

            <c:when test="${sessionScope.user == null}">
                <a href="${ctx}/login" class="btn btn-ghost">
                    Đăng nhập
                </a>

                <a href="${ctx}/register" class="btn btn-primary">
                    Đăng ký
                </a>
            </c:when>

            <c:otherwise>
                <span class="user-name" style="display:flex;align-items:center;">
                    Xin chào,
                    <a href="${ctx}/profile" style="
            color:#e50914 !important;
            font-weight:700;
            text-decoration:none;
            border-bottom:0px solid #e50914;
            padding-bottom:0px;
       ">
                        ${sessionScope.user.fullName}
                    </a>
                </span>

                <a href="${ctx}/logout" class="btn btn-primary">
                    Đăng xuất
                </a>

            </c:otherwise>

        </c:choose>
    </div>
</header>
