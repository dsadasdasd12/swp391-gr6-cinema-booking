<%-- 
    Document   : managerhome
    Created on : Jun 1, 2026, 5:14:43 PM
    Author     : tttru
--%>

<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<%@ page contentType="text/html; charset=UTF-8" pageEncoding="UTF-8" %>
<c:set var="ctx" value="${pageContext.request.contextPath}" />

<!DOCTYPE html>
<html lang="vi">
<head>
    <meta charset="UTF-8">
    <title>Manager Dashboard - RapViet Cinema</title>
    <link rel="stylesheet" href="${ctx}/assets/css/style.css">
    <link rel="stylesheet" href="${ctx}/assets/css/admin.css">
</head>
<body>
<div class="admin-shell">
    <aside class="admin-sidebar">
        <div class="admin-brand">RAPVIET SYSTEM</div>

        <div class="admin-role">
            <p>Phân hệ</p>
            <strong>Manager Dashboard</strong>
            <span>Quyền: Branch Manager</span>
        </div>

        <nav class="admin-menu">
            <a class="active" href="${ctx}/manager/dashboard">Dashboard</a>
            <a href="${ctx}/manager/halls">Quản lý phòng chiếu</a>
            <a href="#">Lịch chiếu</a>
            <a href="#">Gán phim</a>
            <a href="#">Cài đặt chi nhánh</a>
            <a href="${ctx}/logout">Đăng xuất</a>
        </nav>
    </aside>

    <main class="admin-main">
        <div class="admin-topbar">
            <div>
                <strong>Manager Dashboard</strong>
                <span>Quản lý vận hành chi nhánh rạp</span>
            </div>
        </div>

        <section class="admin-content">
            <div class="page-heading">
                <div>
                    <h1>Xin chào Branch Manager</h1>
                    <p>Chọn chức năng quản lý bên dưới để bắt đầu.</p>
                </div>
            </div>

            <div class="dashboard-grid">
                <a class="dashboard-card" href="${ctx}/manager/halls">
                    <h3>Quản lý phòng chiếu</h3>
                    <p>Thêm, sửa, xóa, cấu hình sức chứa, loại phòng và trạng thái phòng chiếu.</p>
                </a>

                <a class="dashboard-card" href="${ctx}/manager/showtimes">
                    <h3>Lịch chiếu</h3>
                    <p>Tạo, chỉnh sửa, hủy suất chiếu và gán phòng chiếu cho suất chiếu.</p>
                </a>

                <a class="dashboard-card" href="#">
                    <h3>Gán phim</h3>
                    <p>Phân bổ phim vào chi nhánh, phòng chiếu và chuẩn bị lịch chiếu.</p>
                </a>

                <a class="dashboard-card" href="#">
                    <h3>Cài đặt chi nhánh</h3>
                    <p>Quản lý thông tin vận hành liên quan đến chi nhánh.</p>
                </a>
            </div>
        </section>
    </main>
</div>
</body>
</html>
