<%-- 
    Document   : branch-list
    Created on : Jun 5, 2026, 8:30:15 PM
    Author     : MSI
--%>

<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<%@ page contentType="text/html; charset=UTF-8" pageEncoding="UTF-8" %>
<c:set var="ctx" value="${pageContext.request.contextPath}" />

<!DOCTYPE html>
<html lang="vi">
<head>
    <meta charset="UTF-8">
    <title>Quản lý chi nhánh - RapViet Cinema</title>
    <link rel="stylesheet" href="${ctx}/assets/css/style.css">
    <link rel="stylesheet" href="${ctx}/assets/css/admin.css">
</head>
<body>
<div class="admin-shell">
    <aside class="admin-sidebar">
        <div class="admin-brand">RAPVIET SYSTEM</div>

        <div class="admin-role">
            <p>Phân hệ</p>
            <strong>Admin Dashboard</strong>
            <span>Quyền: Administrator</span>
        </div>

        <nav class="admin-menu">
            <a href="${ctx}/admin/dashboard">Dashboard</a>
            <a class="active" href="${ctx}/admin/branches">Quản lý chi nhánh</a>
            <a href="#">Quản lý người dùng</a>
            <a href="#">Quản lý phim</a>
            <a href="#">Báo cáo hệ thống</a>
            <a href="${ctx}/logout">Đăng xuất</a>
        </nav>
    </aside>

    <main class="admin-main">
        <div class="admin-topbar">
            <div>
                <strong>Quản lý chi nhánh</strong>
                <span>Admin quản lý danh sách chi nhánh trong hệ thống</span>
            </div>
        </div>

        <section class="admin-content">
            <div class="page-heading">
                <div>
                    <h1>Quản lý chi nhánh</h1>
                    <p>Thêm, chỉnh sửa, xóa, cập nhật trạng thái và giờ hoạt động của chi nhánh.</p>
                </div>

                <a class="btn btn-primary" href="${ctx}/admin/branches/create">+ Thêm chi nhánh</a>
            </div>

            <c:if test="${not empty sessionScope.flashMessage}">
                <div class="alert ${sessionScope.flashType == 'success' ? 'alert-success' : 'alert-error'}">
                    <c:out value="${sessionScope.flashMessage}" />
                </div>

                <c:remove var="flashMessage" scope="session" />
                <c:remove var="flashType" scope="session" />
            </c:if>

            <div class="panel">
                <div class="panel-header">Danh sách chi nhánh</div>

                <div class="panel-body">
                    <c:choose>
                        <c:when test="${empty branches}">
                            <div class="empty-admin">
                                Chưa có chi nhánh nào trong hệ thống.
                            </div>
                        </c:when>

                        <c:otherwise>
                            <table class="admin-table">
                                <thead>
                                <tr>
                                    <th>ID</th>
                                    <th>Tên chi nhánh</th>
                                    <th>Địa chỉ</th>
                                    <th>Số điện thoại</th>
                                    <th>Giờ hoạt động</th>
                                    <th>Trạng thái</th>
                                    <th>Thao tác</th>
                                </tr>
                                </thead>

                                <tbody>
                                <c:forEach var="b" items="${branches}">
                                    <tr>
                                        <td>#${b.id}</td>

                                        <td>
                                            <strong><c:out value="${b.name}" /></strong>
                                        </td>

                                        <td>
                                            <c:out value="${b.address}" />
                                        </td>

                                        <td>
                                            <c:out value="${empty b.phone ? '-' : b.phone}" />
                                        </td>

                                        <td>
                                            <c:choose>
                                                <c:when test="${not empty b.openTime and not empty b.closeTime}">
                                                    ${b.openTime} - ${b.closeTime}
                                                </c:when>
                                                <c:otherwise>-</c:otherwise>
                                            </c:choose>
                                        </td>

                                        <td>
                                            <span class="badge-status ${b.status == 'ACTIVE' ? 'badge-active' : 'badge-inactive'}">
                                                <c:out value="${b.status}" />
                                            </span>
                                        </td>

                                        <td>
                                            <div class="action-inline">
                                                <a class="btn btn-ghost btn-small"
                                                   href="${ctx}/admin/branches/edit?id=${b.id}">
                                                    Sửa
                                                </a>

                                                <form method="post" action="${ctx}/admin/branches/status">
                                                    <input type="hidden" name="id" value="${b.id}">

                                                    <c:choose>
                                                        <c:when test="${b.status == 'ACTIVE'}">
                                                            <input type="hidden" name="status" value="INACTIVE">
                                                            <button type="submit" class="btn btn-warning btn-small">
                                                                Ngưng
                                                            </button>
                                                        </c:when>

                                                        <c:otherwise>
                                                            <input type="hidden" name="status" value="ACTIVE">
                                                            <button type="submit" class="btn btn-success btn-small">
                                                                Mở lại
                                                            </button>
                                                        </c:otherwise>
                                                    </c:choose>
                                                </form>

                                                <form method="post"
                                                      action="${ctx}/admin/branches/delete"
                                                      onsubmit="return confirm('Bạn có chắc muốn xóa chi nhánh này?');">
                                                    <input type="hidden" name="id" value="${b.id}">
                                                    <button type="submit" class="btn btn-danger btn-small">
                                                        Xóa
                                                    </button>
                                                </form>
                                            </div>
                                        </td>
                                    </tr>
                                </c:forEach>
                                </tbody>
                            </table>
                        </c:otherwise>
                    </c:choose>
                </div>
            </div>
        </section>
    </main>
</div>
</body>
</html>