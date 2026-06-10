<%@ page contentType="text/html; charset=UTF-8" pageEncoding="UTF-8" %>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<c:set var="ctx" value="${pageContext.request.contextPath}" />
<!DOCTYPE html>
<html>
<head>
    <meta charset="UTF-8">
    <title>Đăng nhập — RapViet</title>
    <link rel="stylesheet" href="${ctx}/assets/css/auth.css">
    <link rel="stylesheet" href="${ctx}/assets/css/style.css">
</head>
<body>
    <jsp:include page="/pages/common/header.jsp">
        <jsp:param name="active" value="home"/>
    </jsp:include>
    <div class="auth-container">
        <div class="auth-card">
            <h2>Đăng nhập</h2>
            <c:if test="${not empty error}">
                <div class="error-message">${error}</div>
            </c:if>
            <form action="${ctx}/login" method="post">
                <div class="form-group">
                    <label>Email</label>
                    <input type="email" name="email"
                           placeholder="Nhập email" required>
                </div>
                <div class="form-group">
                    <label>Mật khẩu</label>
                    <input type="password" name="password"
                           placeholder="Nhập mật khẩu" required>
                </div>
                <button type="submit" class="btn-primary">Đăng nhập</button>
                <p class="auth-link">
                    Chưa có tài khoản?
                    <a href="${ctx}/register">Đăng ký</a>
                </p>
            </form>
        </div>
    </div>
</body>
</html>
