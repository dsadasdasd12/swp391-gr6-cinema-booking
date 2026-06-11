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
        <link rel="stylesheet" href="${ctx}/assets/css/style.css">
        <link rel="stylesheet" href="${ctx}/assets/css/auth.css">

    </head>
    <script>
    function togglePassword() {

        const password =
                document.getElementById("password");

        if (password.type === "password") {
            password.type = "text";
        } else {
            password.type = "password";
        }
    }
</script>
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
                        <input type="text" name="fullname" value="${fullname}">
                    </div>

                    <div class="form-group">
                        <label>Email</label>
                        <input type="email" name="email" value="${email}">
                    </div>

                    <div class="form-group">
                        <label>Số điện thoại</label>
                        <input type="text" name="phone" value="${phone}">
                    </div>

                    <div class="form-group">
                        <label>Mật khẩu</label>

                        <div class="password-wrapper">
                            <input type="password"
                                   id="password"
                                   name="password"
                                   minlength="8">

                            <span class="toggle-password"
                                  onclick="togglePassword('password', this)">
                                👁
                            </span>
                        </div>
                    </div>

                    <div class="form-group">
                        <label>Xác nhận mật khẩu</label>

                        <div class="password-wrapper">
                            <input type="password"
                                   id="confirmPassword"
                                   name="confirmPassword"
                                   minlength="8">

                            <span class="toggle-password"
                                  onclick="togglePassword('confirmPassword', this)">
                                👁
                            </span>
                        </div>
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
