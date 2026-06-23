<%-- 
    Document   : verify-email
    Created on : Jun 3, 2026, 2:59:44 PM
    Author     : tttru
--%>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<%@ page contentType="text/html; charset=UTF-8" pageEncoding="UTF-8" %>
<c:set var="ctx" value="${pageContext.request.contextPath}" />


<!DOCTYPE html>
<html>
    <head>
        <title>Xác thực Email</title>
        <link rel="stylesheet" href="${ctx}/assets/css/style.css">
<link rel="stylesheet" href="${ctx}/assets/css/auth.css?v=10"> 
    </head>
    <body>

        <jsp:include page="/pages/common/header.jsp">
            <jsp:param name="active" value="home"/>
        </jsp:include>

        <div class="auth-container">
            <div class="auth-card">
                <h2>Xác thực Email</h2>

                <c:if test="${not empty error}">
                    <div class="error-message">${error}</div>
                </c:if>

                <c:if test="${not empty success}">
                    <div class="success-message">${success}</div>
                </c:if>
                <p class="auth-link">
                    Vui lòng nhập mã OTP đã gửi tới email của bạn.
                </p>

                <form action="${ctx}/verify-email" method="post">
                    <div class="form-group">
                        <label>Mã OTP</label>
                        <input type="text"
                               name="otp"
                               maxlength="6">
                    </div>

                    <button type="submit" class="btn-primary">
                        Xác thực
                    </button>
                </form>
                <div class="resend-container">
                    <form action="${ctx}/resend-otp" method="post">
                        <button type="submit" class="btn btn-ghost">
                            Gửi lại OTP
                        </button>
                    </form>
                </div>

            </div>
        </div>

    </body>
</html>