<%@ page contentType="text/html; charset=UTF-8" pageEncoding="UTF-8" %>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<c:set var="ctx" value="${pageContext.request.contextPath}" />
<!DOCTYPE html>
<html>
    <head>
        <meta http-equiv="Content-Type" content="text/html; charset=UTF-8">
        <title>Đăng nhập</title>
        <link rel="stylesheet" href="${ctx}/assets/css/style.css">
        <link rel="stylesheet" href="${ctx}/assets/css/auth.css"> 
    </head>
    <body>
        <c:if test="${not empty sessionScope.successMessage}">

    <div id="toast-success"
         style="
            position: fixed;
            top: 20px;
            left: 50%;
            transform: translateX(-50%);
            background: #22c55e;
            color: white;
            padding: 14px 24px;
            border-radius: 10px;
            font-size: 15px;
            font-weight: 600;
            box-shadow: 0 8px 25px rgba(0,0,0,.25);
            z-index: 99999;
        ">
        ✓ ${sessionScope.successMessage}
    </div>

    <script>
        setTimeout(function () {
            const toast = document.getElementById("toast-success");
            if (toast) {
                toast.style.display = "none";
            }
        }, 3000);
    </script>

    <c:remove var="successMessage" scope="session"/>

</c:if>
        <jsp:include page="/pages/common/header.jsp">
        <jsp:param name="active" value="home"/>
    </jsp:include>
        <div class="auth-container">
    <div class="auth-card">
        <h2>Đăng nhập</h2>
        <c:if test="${not empty error}">
    <div class="error-message">
        ${error}
    </div>
</c:if>
        <form action="${ctx}/login" method="post">
            <div class="form-group">
                <label>Email</label>
                <input type="email" name="email" placeholder="Nhập email">
            </div>

            <div class="form-group">
                <label>Mật khẩu</label>
                <input type="password" name="password" placeholder="Nhập mật khẩu">
            </div>
            </p>
            <p class="auth-link-forgotpassword">
    <a href="${ctx}/forgot-password">Quên mật khẩu?</a>
           </p>

            <button type="submit" class="btn-primary">
                Đăng nhập
            </button>

            <p class="auth-link">
                Chưa có tài khoản?
                <a href="${ctx}/register">Đăng ký</a>
            </p>
        </form>
    </div>
</div>
    </body>
</html>
