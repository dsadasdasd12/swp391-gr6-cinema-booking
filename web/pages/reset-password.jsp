<%@ page contentType="text/html; charset=UTF-8" pageEncoding="UTF-8" %>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<c:set var="ctx" value="${pageContext.request.contextPath}" />

<!DOCTYPE html>
<html>
    <head>
        <title>Đặt lại mật khẩu</title>
        <link rel="stylesheet" href="${ctx}/assets/css/style.css">
        <link rel="stylesheet" href="${ctx}/assets/css/auth.css">
    </head>
    <script>
function togglePassword(inputId, icon) {
    const input = document.getElementById(inputId);

    if (input.type === "password") {
        input.type = "text";
        icon.textContent = "🙈";
    } else {
        input.type = "password";
        icon.textContent = "👁";
    }
}
</script>
    <body>

        <jsp:include page="/pages/common/header.jsp">
            <jsp:param name="active" value="home"/>
        </jsp:include>

        <div class="auth-container">
            <div class="auth-card">
                <h2>Đặt lại mật khẩu</h2>

                <c:if test="${not empty error}">
                    <div class="error-message">${error}</div>
                </c:if>

                <form action="${ctx}/reset-password" method="post">
                    <div class="form-group">
                        <label>Mật khẩu mới</label>

                        <div class="password-field">
                            <input type="password"
                                   id="newPassword"
                                   name="newPassword"
                                   >

                            <span class="toggle-password"
                                  onclick="togglePassword('newPassword', this)">
                                👁
                            </span>
                        </div>
                    </div>

                    <div class="form-group">
                        <label>Xác nhận mật khẩu</label>

                        <div class="password-field">
                            <input type="password"
                                   id="confirmPassword"
                                   name="confirmPassword"
                                   >

                            <span class="toggle-password"
                                  onclick="togglePassword('confirmPassword', this)">
                                👁
                            </span>
                        </div>
                    </div>

                    <button type="submit" class="btn-primary">
                        Đổi mật khẩu
                    </button>
                </form>
            </div>
        </div>

    </body>
</html>