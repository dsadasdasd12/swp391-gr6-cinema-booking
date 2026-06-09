<%-- 
    Document   : adminhome
    Created on : Jun 1, 2026, 5:14:51 PM
    Author     : tttru
--%>

<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<%@ page contentType="text/html; charset=UTF-8" pageEncoding="UTF-8" %>
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
<div class="admin-shell">
    <aside class="admin-sidebar">
        <div class="admin-brand">RAPVIET SYSTEM</div>

        <div class="admin-role">
            <p>Phân hệ</p>
            <strong>Admin Dashboard</strong>
            <span>Quyền: Administrator</span>
        </div>

        <nav class="admin-menu">
            <a class="active" href="${ctx}/admin/dashboard">Dashboard</a>
            <a href="${ctx}/admin/branches">Quản lý chi nhánh</a>
            <a href="#">Quản lý người dùng</a>
            <a href="#">Quản lý phim</a>
            <a href="#">Báo cáo hệ thống</a>
            <a href="${ctx}/logout">Đăng xuất</a>
        </nav>
    </aside>

    <main class="admin-main">
        <div class="admin-topbar">
            <div>
                <strong>Admin Dashboard</strong>
                <span>Quản trị hệ thống RapViet Cinema</span>
            </div>
        </div>

        <section class="admin-content">
            <div class="page-heading">
                <div>
                    <h1>Xin chào Admin</h1>
                    <p>Chọn chức năng quản trị bên dưới để bắt đầu.</p>
                </div>
            </div>

            <div class="dashboard-grid">
                <a class="dashboard-card" href="${ctx}/admin/branches">
                    <h3>Quản lý chi nhánh</h3>
                    <p>Thêm, sửa, xóa, cập nhật trạng thái và giờ hoạt động của chi nhánh.</p>
                </a>

                <a class="dashboard-card" href="#">
                    <h3>Quản lý người dùng</h3>
                    <p>Quản lý tài khoản và phân quyền người dùng.</p>
                </a>

                <a class="dashboard-card" href="#">
                    <h3>Quản lý phim</h3>
                    <p>Quản lý thông tin phim trong hệ thống.</p>
                </a>

                <a class="dashboard-card" href="#">
                    <h3>Báo cáo hệ thống</h3>
                    <p>Theo dõi tình hình hoạt động của hệ thống.</p>
                </a>
            </div>
        </section>
    </main>
</div>
</body>
</html>