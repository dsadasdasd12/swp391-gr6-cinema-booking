<%@ page contentType="text/html; charset=UTF-8" pageEncoding="UTF-8" %>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<c:set var="ctx" value="${pageContext.request.contextPath}" />

<!DOCTYPE html>
<html>
    <head>
        <title>Xác nhận OTP</title>
        <link rel="stylesheet" href="${ctx}/assets/css/style.css">
        <link rel="stylesheet" href="${ctx}/assets/css/auth.css?v=10"> 
    </head>
    <body>

        <jsp:include page="/pages/common/header.jsp">
            <jsp:param name="active" value="home"/>
        </jsp:include>

        <div class="auth-container">
            <div class="auth-card">
                <h2>Xác nhận OTP</h2>

                <p class="auth-link">
                    OTP has been sent to
                    <strong>${sessionScope.resetEmail}</strong>
                </p>

                <c:if test="${not empty error}">
                    <div class="error-message">${error}</div>
                </c:if>

                <c:if test="${not empty success}">
                    <div class="success-message">${success}</div>
                </c:if>

                <form action="${ctx}/confirm-reset-otp" method="post">
                    <div class="form-group">
                        <label>OTP</label>
                        <input type="text"
                               name="otp"
                               maxlength="6"
                               placeholder="Nhập mã OTP"
                               >
                    </div>

                    <button type="submit" class="btn-primary">
                        Xác nhận OTP
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