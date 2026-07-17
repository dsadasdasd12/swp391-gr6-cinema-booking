<%@ page contentType="text/html; charset=UTF-8" pageEncoding="UTF-8" %>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<c:set var="ctx" value="${pageContext.request.contextPath}" />

<!DOCTYPE html>
<html>
    <head>
        <title>Quên mật khẩu</title>
        <link rel="stylesheet" href="${ctx}/assets/css/style.css">
        <link rel="stylesheet" href="${ctx}/assets/css/auth.css?v=10"> 
    </head>
    <body>

        <jsp:include page="/pages/common/header.jsp">
            <jsp:param name="active" value="home"/>
        </jsp:include>

        <div class="auth-container">
            <div class="auth-card">
                <h2>Quên mật khẩu</h2>

                <c:if test="${not empty error}">
                    <div class="error-message">${error}</div>
                </c:if>

                <form action="${ctx}/forgot-password" method="post">
                    <div class="form-group">
                        <label>Email</label>
                        <input type="email"
                               name="email"
                               value="${email}"
                               placeholder="Nhập email"
                               required>
                    </div>

                    <button type="submit" class="btn-primary">
                        Gửi OTP
                    </button>
                </form>
            </div>
        </div>

    </body>
</html>