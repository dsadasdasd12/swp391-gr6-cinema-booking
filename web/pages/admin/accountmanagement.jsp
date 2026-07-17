<%@ page contentType="text/html; charset=UTF-8" pageEncoding="UTF-8" %>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<c:set var="ctx" value="${pageContext.request.contextPath}" />

<!DOCTYPE html>
<html lang="vi">
    <head>
        <meta charset="UTF-8">
        <title>Quản lý tài khoản - RapViet Cinema</title>

        <link rel="stylesheet" href="${ctx}/assets/css/admin/admin.css?v=1">
        <link rel="stylesheet" href="${ctx}/assets/css/admin/accountmanagement.css?v=1">
    </head>

    <body class="rv-admin-body">

        <div class="rv-admin-layout">

            <%@ include file="/pages/shared/sidebar-admin.jsp" %>

            <main class="rv-admin-main">

                <%@ include file="/pages/shared/header-admin.jsp" %>

                <section class="management-page">

                    <div class="management-header">
                        <div>
                            <h1>Quản lý tài khoản</h1>
                            <p>Quản lý người dùng, vai trò, trạng thái hoạt động và xác thực email.</p>
                        </div>

                        <button type="button"
                                class="management-btn management-btn-primary"
                                onclick="openAddModal()">
                            + Thêm tài khoản
                        </button>
                    </div>

                    <c:if test="${not empty message}">
                        <div class="management-alert management-alert-success">
                            <c:out value="${message}" />
                        </div>
                    </c:if>

                    <c:if test="${not empty error}">
                        <div class="management-alert management-alert-error">
                            <c:out value="${error}" />
                        </div>
                    </c:if>

                    <div class="management-card">

                        <div class="management-card-header">
                            <div>
                                <h2>Danh sách tài khoản</h2>
                                <span>Tìm kiếm và cập nhật thông tin người dùng</span>
                            </div>

                            <input type="text"
                                   id="searchInput"
                                   class="management-search"
                                   placeholder="Tìm theo tên, email, số điện thoại..."
                                   onkeyup="filterUsers()">
                        </div>

                        <div class="management-table-wrap">
                            <table class="management-table" id="userTable">
                                <thead>
                                    <tr>
                                        <th>ID</th>
                                        <th>Người dùng</th>
                                        <th>Email</th>
                                        <th>Số điện thoại</th>
                                        <th>Vai trò</th>
                                        <th>Trạng thái</th>
                                        <th>Xác thực email</th>
                                        <th>Hành động</th>
                                    </tr>
                                </thead>

                                <tbody>
                                    <c:choose>
                                        <c:when test="${empty users}">
                                            <tr>
                                                <td colspan="8">
                                                    <div class="management-empty">
                                                        Chưa có tài khoản nào trong hệ thống.
                                                    </div>
                                                </td>
                                            </tr>
                                        </c:when>

                                        <c:otherwise>
                                            <c:forEach var="u" items="${users}">
                                                <tr>
                                                    <td>#${u.id}</td>

                                                    <td>
                                                        <div class="management-user">
                                                            <div class="management-avatar">
                                                                <c:choose>
                                                                    <c:when test="${not empty u.fullName}">
                                                                        ${u.fullName.substring(0,1)}
                                                                    </c:when>
                                                                    <c:otherwise>?</c:otherwise>
                                                                </c:choose>
                                                            </div>

                                                            <div>
                                                                <strong><c:out value="${u.fullName}" /></strong>
                                                                <span>User account</span>
                                                            </div>
                                                        </div>
                                                    </td>

                                                    <td>
                                                        <c:out value="${u.email}" />
                                                    </td>

                                                    <td>
                                                        <c:choose>
                                                            <c:when test="${empty u.phone}">
                                                                <span class="management-muted">Chưa cập nhật</span>
                                                            </c:when>
                                                            <c:otherwise>
                                                                <c:out value="${u.phone}" />
                                                            </c:otherwise>
                                                        </c:choose>
                                                    </td>

                                                    <td>
                                                        <c:choose>
                                                            <c:when test="${u.role == 'ADMIN'}">
                                                                <span class="management-badge management-badge-admin">ADMIN</span>
                                                            </c:when>

                                                            <c:when test="${u.role == 'MANAGER'}">
                                                                <span class="management-badge management-badge-manager">MANAGER</span>
                                                            </c:when>

                                                            <c:when test="${u.role == 'STAFF'}">
                                                                <span class="management-badge management-badge-staff">STAFF</span>
                                                            </c:when>

                                                            <c:otherwise>
                                                                <span class="management-badge management-badge-customer">CUSTOMER</span>
                                                            </c:otherwise>
                                                        </c:choose>
                                                    </td>

                                                    <td>
                                                        <c:choose>
                                                            <c:when test="${u.active}">
                                                                <span class="management-status management-status-active">
                                                                    Hoạt động
                                                                </span>
                                                            </c:when>

                                                            <c:otherwise>
                                                                <span class="management-status management-status-blocked">
                                                                    Bị khóa
                                                                </span>
                                                            </c:otherwise>
                                                        </c:choose>
                                                    </td>

                                                    <td>
                                                        <c:choose>
                                                            <c:when test="${u.emailVerified}">
                                                                <span class="management-status management-status-active">
                                                                    Đã xác thực
                                                                </span>
                                                            </c:when>

                                                            <c:otherwise>
                                                                <span class="management-status management-status-pending">
                                                                    Chưa xác thực
                                                                </span>
                                                            </c:otherwise>
                                                        </c:choose>
                                                    </td>

                                                    <td>
                                                        <div class="management-actions">
                                                            <button type="button"
                                                                    class="management-btn management-btn-small management-btn-ghost"
                                                                    onclick="openEditModal(
                                                                                    '${u.id}',
                                                                                    '${u.fullName}',
                                                                                    '${u.email}',
                                                                                    '${u.phone}',
                                                                                    '${u.role}'
                                                                                    )">
                                                                Sửa
                                                            </button>

                                                            <c:choose>
                                                                <c:when test="${u.active}">
                                                                    <a class="management-btn management-btn-small management-btn-danger"
                                                                       href="${ctx}/admin/users/lock?id=${u.id}">
                                                                        Khóa
                                                                    </a>
                                                                </c:when>

                                                                <c:otherwise>
                                                                    <a class="management-btn management-btn-small management-btn-success"
                                                                       href="${ctx}/admin/users/unlock?id=${u.id}">
                                                                        Mở
                                                                    </a>
                                                                </c:otherwise>
                                                            </c:choose>
                                                        </div>
                                                    </td>
                                                </tr>
                                            </c:forEach>
                                        </c:otherwise>
                                    </c:choose>
                                </tbody>
                            </table>
                        </div>

                    </div>

                </section>

            </main>

        </div>

        <!-- ADD MODAL -->
        <div class="management-modal-overlay" id="addModal">
            <div class="management-modal">

                <div class="management-modal-header">
                    <h2>Thêm tài khoản</h2>
                    <button type="button" onclick="closeAddModal()">×</button>
                </div>

                <form action="${ctx}/admin/users/create" method="post">

                    <div class="management-form-group">
                        <label>Họ tên</label>
                        <input type="text" name="fullName" required>
                    </div>

                    <div class="management-form-group">
                        <label>Email</label>
                        <input type="email" name="email" required>
                    </div>

                    <div class="management-form-group">
                        <label>Số điện thoại</label>
                        <input type="text" name="phone" placeholder="Ví dụ: 0901234567">
                    </div>

                    <div class="management-form-group">
                        <label>Vai trò</label>
                        <select name="role" required>
                            <option value="CUSTOMER">CUSTOMER</option>
                            <option value="STAFF">STAFF</option>
                            <option value="MANAGER">MANAGER</option>
                            <option value="ADMIN">ADMIN</option>
                        </select>
                    </div>

                    <div class="management-form-actions">
                        <button type="button"
                                class="management-btn management-btn-ghost"
                                onclick="closeAddModal()">
                            Hủy
                        </button>

                        <button type="submit"
                                class="management-btn management-btn-primary">
                            Thêm
                        </button>
                    </div>

                </form>

            </div>
        </div>

        <!-- EDIT MODAL -->
        <div class="management-modal-overlay" id="editModal">
            <div class="management-modal">

                <div class="management-modal-header">
                    <h2>Cập nhật tài khoản</h2>
                    <button type="button" onclick="closeEditModal()">×</button>
                </div>

                <form action="${ctx}/admin/users/update" method="post">

                    <input type="hidden" id="editId" name="id">

                    <div class="management-form-group">
                        <label>Họ tên</label>
                        <input type="text" id="editFullName" name="fullName" required>
                    </div>

                    <div class="management-form-group">
                        <label>Email</label>
                        <input type="email" id="editEmail" name="email" required>
                    </div>

                    <div class="management-form-group">
                        <label>Số điện thoại</label>
                        <input type="text" id="editPhone" name="phone">
                    </div>

                    <div class="management-form-group">
                        <label>Vai trò</label>
                        <select id="editRole" name="role" required>
                            <option value="CUSTOMER">CUSTOMER</option>
                            <option value="STAFF">STAFF</option>
                            <option value="MANAGER">MANAGER</option>
                            <option value="ADMIN">ADMIN</option>
                        </select>
                    </div>

                    <div class="management-form-actions">
                        <button type="button"
                                class="management-btn management-btn-ghost"
                                onclick="closeEditModal()">
                            Hủy
                        </button>

                        <button type="submit"
                                class="management-btn management-btn-primary">
                            Lưu
                        </button>
                    </div>

                </form>

            </div>
        </div>

        <script>
            function filterUsers() {
                const keyword = document.getElementById("searchInput").value.toLowerCase();
                const rows = document.querySelectorAll("#userTable tbody tr");

                rows.forEach(row => {
                    const text = row.innerText.toLowerCase();
                    row.style.display = text.includes(keyword) ? "" : "none";
                });
            }

            function openAddModal() {
                document.getElementById("addModal").classList.add("show");
            }

            function closeAddModal() {
                document.getElementById("addModal").classList.remove("show");
            }

            function openEditModal(id, fullName, email, phone, role) {
                document.getElementById("editId").value = id;
                document.getElementById("editFullName").value = fullName;
                document.getElementById("editEmail").value = email;
                document.getElementById("editPhone").value = phone || "";
                document.getElementById("editRole").value = role;

                document.getElementById("editModal").classList.add("show");
            }

            function closeEditModal() {
                document.getElementById("editModal").classList.remove("show");
            }
        </script>

    </body>
</html>