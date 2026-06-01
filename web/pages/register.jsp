<%-- 
    Document   : register.jsp
    Created on : Jun 1, 2026, 3:55:23 PM
    Author     : tttru
--%>
<%@ page contentType="text/html; charset=UTF-8" pageEncoding="UTF-8" %>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<c:set var="ctx" value="${pageContext.request.contextPath}" />
<!DOCTYPE html>
<html>
    <head>
        <meta http-equiv="Content-Type" content="text/html; charset=UTF-8">
        <title>JSP Page</title>
        <link rel="stylesheet" href="${ctx}/assets/css/auth.css">
        <link rel="stylesheet" href="${ctx}/assets/css/style.css">
    </head>
    <body>
        <jsp:include page="/pages/common/header.jsp">
        <jsp:param name="active" value="home"/>
    </jsp:include>
        <div class="auth-container">
    <div class="auth-card">
        <h2>Đăng ký tài khoản</h2>
        <c:if test="${not empty error}">
    <div class="error-message">
        ${error}
    </div>
</c:if>
        <form action="${ctx}/register" method="post">

            <div class="form-group">
                <label>Họ tên</label>
                <input type="text" name="fullname">
            </div>

            <div class="form-group">
                <label>Email</label>
                <input type="email" name="email">
            </div>

            <div class="form-group">
                <label>Số điện thoại</label>
                <input type="text" name="phone">
            </div>

            <div class="form-group">
                <label>Mật khẩu</label>
                <input type="password" name="password">
            </div>

            <div class="form-group">
                <label>Xác nhận mật khẩu</label>
                <input type="password" name="confirmPassword">
            </div>

            <button type="submit" class="btn-primary">
                Đăng ký
            </button>

            <p class="auth-link">
                Đã có tài khoản?
                <a href="${ctx}/login">Đăng nhập</a>
            </p>
        </form>
    </div>
</div>
    </body>
</html>
