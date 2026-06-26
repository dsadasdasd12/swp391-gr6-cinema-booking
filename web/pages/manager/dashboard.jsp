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

    <link rel="stylesheet"
          href="${ctx}/assets/css/style.css">

    <link rel="stylesheet"
          href="${ctx}/assets/css/admin.css">
</head>

<body>

<div class="admin-shell">

    <%-- SIDEBAR --%>
    <aside class="admin-sidebar">

        <div class="admin-brand">
            RAPVIET SYSTEM
        </div>

        <div class="admin-role">
            <p>Phân hệ</p>
            <strong>Manager Dashboard</strong>
            <span>Quyền: Branch Manager</span>
        </div>

        <nav class="admin-menu">

            <a class="active"
               href="${ctx}/manager/dashboard">
                Dashboard
            </a>

            <a href="${ctx}/manager/halls">
                Quản lý phòng chiếu
            </a>

            <a href="${ctx}/manager/movie-assignments/branches">
                Phim tại chi nhánh
            </a>

            <a href="${ctx}/manager/movie-assignments/halls">
                Phim tại phòng chiếu
            </a>

            <a href="${ctx}/manager/movie-durations">
                Quản lý thời lượng phim
            </a>

            <a href="${ctx}/manager/showtimes">
                Quản lý lịch chiếu
            </a>

            <a href="${ctx}/logout">
                Đăng xuất
            </a>

        </nav>

    </aside>

    <%-- MAIN CONTENT --%>
    <main class="admin-main">

        <div class="admin-topbar">

            <div>
                <strong>Manager Dashboard</strong>

                <span>
                    Quản lý hoạt động của chi nhánh rạp
                </span>
            </div>

        </div>

        <section class="admin-content">

            <div class="page-heading">

                <div>

                    <h1>
                        Xin chào
                        <c:out value="${sessionScope.user.fullName}" />
                    </h1>

                    <p>
                        Chọn chức năng quản lý bên dưới để bắt đầu.
                    </p>

                </div>

            </div>

            <div class="dashboard-grid">

                <%-- QUẢN LÝ PHÒNG CHIẾU --%>
                <a class="dashboard-card"
                   href="${ctx}/manager/halls">

                    <h3>Quản lý phòng chiếu</h3>

                    <p>
                        Thêm, chỉnh sửa, xóa và cập nhật trạng thái
                        các phòng chiếu thuộc chi nhánh.
                    </p>

                </a>

                <%-- PHÂN BỔ PHIM CHO CHI NHÁNH --%>
                <a class="dashboard-card"
                   href="${ctx}/manager/movie-assignments/branches">

                    <h3>Phân bổ phim cho chi nhánh</h3>

                    <p>
                        Chọn những phim được phép hoạt động tại
                        các chi nhánh đang được phân công quản lý.
                    </p>

                </a>

                <%-- PHÂN BỔ PHIM CHO PHÒNG CHIẾU --%>
                <a class="dashboard-card"
                   href="${ctx}/manager/movie-assignments/halls">

                    <h3>Phân bổ phim cho phòng chiếu</h3>

                    <p>
                        Phân bổ phim riêng cho từng phòng chiếu
                        trước khi tạo lịch chiếu.
                    </p>

                </a>

                <%-- QUẢN LÝ THỜI LƯỢNG PHIM --%>
                <a class="dashboard-card"
                   href="${ctx}/manager/movie-durations">

                    <h3>Quản lý thời lượng phim</h3>

                    <p>
                        Thiết lập và cập nhật số phút chạy của từng phim
                        để hệ thống tự động tính giờ kết thúc suất chiếu.
                    </p>

                </a>

                <%-- QUẢN LÝ LỊCH CHIẾU --%>
                <a class="dashboard-card"
                   href="${ctx}/manager/showtimes">

                    <h3>Quản lý lịch chiếu</h3>

                    <p>
                        Tạo, chỉnh sửa và hủy suất chiếu.
                        Hệ thống tự động tính thời gian kết thúc
                        và ngăn lịch chiếu bị trùng.
                    </p>

                </a>

            </div>

        </section>

    </main>

</div>

</body>

</html>