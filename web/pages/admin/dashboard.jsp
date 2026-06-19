<%@ page contentType="text/html; charset=UTF-8" pageEncoding="UTF-8" %>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>

<c:set var="ctx" value="${pageContext.request.contextPath}" />

<!DOCTYPE html>
<html lang="vi">
    <head>
        <meta charset="UTF-8">
        <title>Admin Dashboard - RapViet Cinema</title>

        <link rel="stylesheet" href="${ctx}/assets/css/style.css">
        <link rel="stylesheet" href="${ctx}/assets/css/admin.css">
    </head>

    <body>

        <jsp:include page="/pages/common/header.jsp">
            <jsp:param name="active" value="admin"/>
        </jsp:include>

        <div class="admin-container">

            <div class="admin-card">

                <div class="admin-header">
                    <h2>Danh sách người dùng</h2>

                    <button type="button"
                            class="btn btn-primary"
                            onclick="openAddUserModal()">
                        + Thêm tài khoản
                    </button>
                </div>

                <div class="admin-toolbar">
                    <input type="text"
                           id="userSearch"
                           class="admin-search"
                           placeholder="Tìm theo tên, email, số điện thoại..."
                           onkeyup="filterUsers()">
                </div>

                <div class="admin-table-wrap">
                    <table class="admin-table" id="userTable">

                        <thead>
                            <tr>
                                <th>ID</th>
                                <th>Họ tên</th>
                                <th>Email</th>
                                <th>Số điện thoại</th>
                                <th>Vai trò</th>
                                <th>Trạng thái</th>
                                <th>Xác thực email</th>
                                <th>Hành động</th>
                            </tr>
                        </thead>

                        <tbody>
                            <c:forEach var="u" items="${users}">
                                <tr>
                                    <td>${u.id}</td>
                                    <td>${u.fullName}</td>
                                    <td>${u.email}</td>

                                    <td>
                                        <c:choose>
                                            <c:when test="${empty u.phone}">
                                                Chưa cập nhật
                                            </c:when>
                                            <c:otherwise>
                                                ${u.phone}
                                            </c:otherwise>
                                        </c:choose>
                                    </td>

                                    <td>
                                        <c:choose>
                                            <c:when test="${u.role == 'CUSTOMER'}">
                                                <span class="role-badge role-customer">CUSTOMER</span>
                                            </c:when>

                                            <c:when test="${u.role == 'MANAGER'}">
                                                <span class="role-badge role-manager">MANAGER</span>
                                            </c:when>

                                            <c:when test="${u.role == 'STAFF'}">
                                                <span class="role-badge role-staff">STAFF</span>
                                            </c:when>

                                            <c:otherwise>
                                                <span class="role-badge">${u.role}</span>
                                            </c:otherwise>
                                        </c:choose>
                                    </td>

                                    <td>
                                        <c:choose>
                                            <c:when test="${u.active}">
                                                <span class="status-active">Hoạt động</span>
                                            </c:when>
                                            <c:otherwise>
                                                <span class="status-inactive">Bị khóa</span>
                                            </c:otherwise>
                                        </c:choose>
                                    </td>

                                    <td>
                                        <c:choose>
                                            <c:when test="${u.emailVerified}">
                                                Đã xác thực
                                            </c:when>
                                            <c:otherwise>
                                                Chưa xác thực
                                            </c:otherwise>
                                        </c:choose>
                                    </td>

                                    <td>
                                        <div class="admin-actions">

                                            <a href="#"
                                               class="action-edit"
                                               onclick="openEditUserModal(
                                                               '${u.id}',
                                                               '${u.fullName}',
                                                               '${u.email}',
                                                               '${u.phone}',
                                                               '${u.role}'
                                                               )">
                                                Sửa
                                            </a>

                                            <c:choose>
                                                <c:when test="${u.active}">
                                                    <a href="${ctx}/admin/user/toggle-active?id=${u.id}"
                                                       class="action-lock">
                                                        Khóa
                                                    </a>
                                                </c:when>

                                                <c:otherwise>
                                                    <a href="${ctx}/admin/user/toggle-active?id=${u.id}"
                                                       class="action-unlock">
                                                        Mở khóa
                                                    </a>
                                                </c:otherwise>
                                            </c:choose>

                                        </div>
                                    </td>
                                </tr>
                            </c:forEach>
                        </tbody>

                    </table>
                </div>

            </div>

        </div>


        <!-- ADD USER MODAL -->
        <div id="addUserModal" class="modal-overlay">

            <div class="admin-modal">

                <div class="modal-header">
                    <h2>Thêm tài khoản mới</h2>

                    <button type="button"
                            class="modal-close"
                            onclick="closeAddUserModal()">
                        ×
                    </button>
                </div>

                <form action="${ctx}/admin/user/save" method="post">

                    <div class="admin-form-group">
                        <label>Họ tên</label>
                        <input type="text"
                               name="fullName"
                               required>
                    </div>

                    <div class="admin-form-group">
                        <label>Email</label>
                        <input type="email"
                               name="email"
                               required>
                    </div>

                    <div class="admin-form-group">
                        <label>Số điện thoại</label>
                        <input type="text"
                               name="phone">
                    </div>

                    <div class="admin-form-group">
                        <label>Vai trò</label>

                        <select name="role" required>
                            <option value="CUSTOMER">
                                CUSTOMER
                            </option>

                            <option value="STAFF">
                                STAFF
                            </option>

                            <option value="MANAGER">
                                MANAGER
                            </option>
                        </select>
                    </div>

                    <div class="admin-info-box">
                        Mật khẩu mặc định:
                        <strong>12345678</strong>
                    </div>

                    <div class="admin-form-actions">

                        <button type="button"
                                class="btn btn-ghost"
                                onclick="closeAddUserModal()">
                            Hủy
                        </button>

                        <button type="submit"
                                class="btn btn-primary">
                            Thêm tài khoản
                        </button>

                    </div>

                </form>

            </div>

        </div>
        <!-- EDIT USER MODAL -->
        <div id="editUserModal" class="modal-overlay">

            <div class="admin-modal">

                <div class="modal-header">
                    <h2>Chỉnh sửa tài khoản</h2>

                    <button type="button"
                            class="modal-close"
                            onclick="closeEditUserModal()">
                        ×
                    </button>
                </div>

                <form action="${ctx}/admin/user/save"
                      method="post">

                    <input type="hidden"
                           id="editId"
                           name="id">

                    <div class="admin-form-group">
                        <label>Họ tên</label>

                        <input type="text"
                               id="editFullName"
                               name="fullName"
                               required>
                    </div>

                    <div class="admin-form-group">
                        <label>Email</label>

                        <input type="email"
                               id="editEmail"
                               name="email"
                               required>
                    </div>

                    <div class="admin-form-group">
                        <label>Số điện thoại</label>

                        <input type="text"
                               id="editPhone"
                               name="phone">
                    </div>

                    <div class="admin-form-group">
                        <label>Vai trò</label>

                        <select id="editRole"
                                name="role">

                            <option value="CUSTOMER">
                                CUSTOMER
                            </option>

                            <option value="STAFF">
                                STAFF
                            </option>

                            <option value="MANAGER">
                                MANAGER
                            </option>

                        </select>
                    </div>

                    <div class="admin-form-actions">

                        <button type="button"
                                class="btn btn-ghost"
                                onclick="closeEditUserModal()">
                            Hủy
                        </button>

                        <button type="submit"
                                class="btn btn-primary">
                            Cập nhật
                        </button>

                    </div>

                </form>

            </div>

        </div>
        <script>
            function filterUsers() {
                const keyword = document.getElementById("userSearch").value.toLowerCase();
                const rows = document.querySelectorAll("#userTable tbody tr");

                rows.forEach(row => {
                    const text = row.innerText.toLowerCase();
                    row.style.display = text.includes(keyword) ? "" : "none";
                });
            }

            function openAddUserModal() {
                document.getElementById("addUserModal").classList.add("show");
            }

            function closeAddUserModal() {
                document.getElementById("addUserModal").classList.remove("show");
            }
        </script>
        <script>

            function openEditUserModal(
                    id,
                    fullName,
                    email,
                    phone,
                    role) {

                document.getElementById("editId").value = id;
                document.getElementById("editFullName").value = fullName;
                document.getElementById("editEmail").value = email;
                document.getElementById("editPhone").value = phone;
                document.getElementById("editRole").value = role;

                document.getElementById("editUserModal")
                        .classList.add("show");
            }

            function closeEditUserModal() {

                document.getElementById("editUserModal")
                        .classList.remove("show");
            }

        </script>

    </body>
</html>