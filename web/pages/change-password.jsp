<%@ page contentType="text/html; charset=UTF-8" pageEncoding="UTF-8" %>
<c:set var="ctx" value="${pageContext.request.contextPath}" />
<form action="${pageContext.request.contextPath}/change-password"
      method="post">

    <div class="form-group">
        <label for="oldPassword">Mật khẩu hiện tại</label>
        <input type="password"
               id="oldPassword"
               name="oldPassword"
               placeholder="Nhập mật khẩu hiện tại"
               required>
    </div>

    <div class="form-group">
        <label for="newPassword">Mật khẩu mới</label>
        <input type="password"
               id="newPassword"
               name="newPassword"
               placeholder="Nhập mật khẩu mới"
               required>
    </div>

    <div class="form-group">
        <label for="confirmPassword">Xác nhận mật khẩu mới</label>
        <input type="password"
               id="confirmPassword"
               name="confirmPassword"
               placeholder="Nhập lại mật khẩu mới"
               required>
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

</form>