<%-- 
    Document   : branch-form
    Created on : Jun 5, 2026, 8:30:21 PM
    Author     : MSI
--%>

<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<%@ page contentType="text/html; charset=UTF-8" pageEncoding="UTF-8" %>

<c:set var="ctx" value="${pageContext.request.contextPath}" />
<c:set var="isEdit" value="${formMode == 'edit'}" />

<!DOCTYPE html>
<html lang="vi">
<head>
    <meta charset="UTF-8">
    <title>${isEdit ? 'Cập nhật chi nhánh' : 'Thêm chi nhánh'} - RapViet Cinema</title>
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
                <strong>${isEdit ? 'Cập nhật chi nhánh' : 'Thêm chi nhánh'}</strong>
                <span>Admin quản lý thông tin chi nhánh</span>
            </div>
        </div>

        <section class="admin-content">
            <div class="page-heading">
                <div>
                    <h1>${isEdit ? 'Cập nhật chi nhánh' : 'Thêm chi nhánh mới'}</h1>
                    <p>Nhập thông tin chi nhánh và giờ hoạt động.</p>
                </div>

                <a class="btn btn-ghost" href="${ctx}/admin/branches">Quay lại</a>
            </div>

            <c:if test="${not empty error}">
                <div class="alert alert-error">
                    <c:out value="${error}" />
                </div>
            </c:if>

            <div class="panel">
                <div class="panel-header">Thông tin chi nhánh</div>

                <div class="panel-body">
                    <form method="post" action="${ctx}${isEdit ? '/admin/branches/edit' : '/admin/branches/create'}">
                        <c:if test="${isEdit}">
                            <input type="hidden" name="id" value="${branch.id}">
                        </c:if>

                        <input type="hidden" name="cinemaId" value="${branch.cinemaId == 0 ? 1 : branch.cinemaId}">

                        <div class="form-grid">
                            <div class="form-group">
                                <label for="name">Tên chi nhánh *</label>
                                <input id="name"
                                       name="name"
                                       class="input-field"
                                       type="text"
                                       required
                                       value="${branch.name}">
                            </div>

                            <div class="form-group">
                                <label for="phone">Số điện thoại</label>
                                <input id="phone"
                                       name="phone"
                                       class="input-field"
                                       type="text"
                                       placeholder="Ví dụ: 0901234567"
                                       value="${branch.phone}">
                            </div>

                            <div class="form-group full">
                                <label for="address">Địa chỉ *</label>
                                <input id="address"
                                       name="address"
                                       class="input-field"
                                       type="text"
                                       required
                                       value="${branch.address}">
                            </div>

                            <div class="form-group">
                                <label for="openTime">Giờ mở cửa</label>
                                <input id="openTime"
                                       name="openTime"
                                       class="input-field"
                                       type="time"
                                       value="${branch.openTime}">
                            </div>

                            <div class="form-group">
                                <label for="closeTime">Giờ đóng cửa</label>
                                <input id="closeTime"
                                       name="closeTime"
                                       class="input-field"
                                       type="time"
                                       value="${branch.closeTime}">
                            </div>

                            <div class="form-group">
                                <label for="status">Trạng thái *</label>
                                <select id="status" name="status" class="select-field" required>
                                    <option value="ACTIVE" ${branch.status == 'ACTIVE' ? 'selected' : ''}>
                                        Đang hoạt động
                                    </option>
                                    <option value="INACTIVE" ${branch.status == 'INACTIVE' ? 'selected' : ''}>
                                        Ngưng hoạt động
                                    </option>
                                </select>
                            </div>
                        </div>

                        <div class="form-actions">
                            <a class="btn btn-ghost" href="${ctx}/admin/branches">Hủy</a>
                            <button class="btn btn-primary" type="submit">Lưu</button>
                        </div>
                    </form>
                </div>
            </div>
        </section>
    </main>
</div>
</body>
</html>