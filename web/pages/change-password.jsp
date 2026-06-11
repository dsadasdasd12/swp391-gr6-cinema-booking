<%@ page contentType="text/html; charset=UTF-8" pageEncoding="UTF-8" %>
<c:set var="ctx" value="${pageContext.request.contextPath}" />
<form action="${ctx}/change-password"
      method="post"
      class="change-password-form">

    <div class="form-group">
    <label>Mật khẩu hiện tại</label>

    <div class="password-field">
        <input type="password"
               id="oldPassword"
               name="oldPassword"
               >

        <span class="toggle-password"
              onclick="togglePassword('oldPassword', this)">
            👁
        </span>
    </div>
</div>

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
    <label>Xác nhận mật khẩu mới</label>

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

    <div class="profile-actions">
        <button type="button"
                class="btn btn-ghost"
                onclick="closePasswordModal()">
            Hủy
        </button>

        <button type="submit"
                class="btn btn-primary">
            Lưu mật khẩu
        </button>
    </div>
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
</form>