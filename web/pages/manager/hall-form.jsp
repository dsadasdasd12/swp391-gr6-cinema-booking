<%-- 
    Document   : hall-form
    Created on : Jun 9, 2026, 10:13:47 PM
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

    <title>
        ${isEdit ? 'Cập nhật phòng chiếu' : 'Thêm phòng chiếu'}
        - RapViet Cinema
    </title>

    <link rel="stylesheet"
          href="${ctx}/assets/css/style.css">

    <link rel="stylesheet"
          href="${ctx}/assets/css/admin.css">
</head>

<body>

<div class="admin-shell">

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

            <a  href="${ctx}/manager/dashboard">
                Dashboard
            </a>

            <a class="active" href="${ctx}/manager/halls">
                Quản lý phòng chiếu
            </a>

            <a href="${ctx}/manager/showtimes">
                Quản lý lịch chiếu
            </a>

            <a href="${ctx}/manager/movie-assignments/branches">
                Phim tại chi nhánh
            </a>

            <a href="${ctx}/manager/movie-assignments/halls">
                Phim tại phòng chiếu
            </a>

            <a 
               href="${ctx}/manager/movie-durations">
                Quản lý thời lượng phim
            </a>

            <a href="${ctx}/logout">
                Đăng xuất
            </a>

        </nav>

    </aside>

    <main class="admin-main">

        <div class="admin-topbar">

            <div>
                <strong>
                    ${isEdit ? 'Cập nhật phòng chiếu' : 'Thêm phòng chiếu'}
                </strong>

                <span>
                    Branch Manager quản lý thông tin phòng chiếu
                </span>
            </div>

        </div>

        <section class="admin-content">

            <div class="page-heading">

                <div>
                    <h1>
                        ${isEdit
                          ? 'Cập nhật phòng chiếu'
                          : 'Thêm phòng chiếu mới'}
                    </h1>

                    <p>
                        Nhập thông tin phòng chiếu, cấu hình ghế,
                        loại phòng và trạng thái.
                    </p>
                </div>

                <a class="btn btn-ghost"
                   href="${ctx}/manager/halls">
                    Quay lại
                </a>

            </div>

            <c:if test="${not empty error}">

                <div class="alert alert-error">
                    <c:out value="${error}" />
                </div>

            </c:if>

            <div class="panel">

                <div class="panel-header">
                    Thông tin phòng chiếu
                </div>

                <div class="panel-body">

                    <form method="post"
                          action="${ctx}${isEdit
                                  ? '/manager/halls/edit'
                                  : '/manager/halls/create'}">
                        
                        <input type="hidden"
                               name="branchId"
                               value="${branch.id}">

                        <c:if test="${isEdit}">

                            <input type="hidden"
                                   name="id"
                                   value="${hall.id}">

                        </c:if>

                        <div class="form-grid">

                            <div class="form-group">

                                <label>
                                    Chi nhánh quản lý
                                </label>

                                <input class="input-field"
                                       type="text"
                                       value="${branch.name}"
                                       readonly>

                            </div>

                            <div class="form-group">

                                <label for="name">
                                    Tên phòng chiếu *
                                </label>

                                <input id="name"
                                       name="name"
                                       class="input-field"
                                       type="text"
                                       required
                                       placeholder="Ví dụ: Phòng 01"
                                       value="${hall.name}">

                            </div>

                            <div class="form-group">

                                <label for="seatRows">
                                    Số hàng ghế *
                                </label>

                                <input id="seatRows"
                                       name="seatRows"
                                       class="input-field"
                                       type="number"
                                       min="1"
                                       required
                                       placeholder="Ví dụ: 10"
                                       value="${hall.seatRows == 0
                                                ? ''
                                                : hall.seatRows}">

                            </div>

                            <div class="form-group">

                                <label for="seatsPerRow">
                                    Số ghế mỗi hàng *
                                </label>

                                <input id="seatsPerRow"
                                       name="seatsPerRow"
                                       class="input-field"
                                       type="number"
                                       min="1"
                                       required
                                       placeholder="Ví dụ: 12"
                                       value="${hall.seatsPerRow == 0
                                                ? ''
                                                : hall.seatsPerRow}">

                            </div>

                            <div class="form-group">

                                <label for="totalSeatsPreview">
                                    Tổng số ghế
                                </label>

                                <input id="totalSeatsPreview"
                                       class="input-field"
                                       type="text"
                                       readonly
                                       placeholder="Tự động tính">

                            </div>

                            <div class="form-group">

                                <label for="hallType">
                                    Loại phòng *
                                </label>

                                <select id="hallType"
                                        name="hallType"
                                        class="select-field"
                                        required>

                                    <option value="STANDARD"
                                        ${hall.hallType == 'STANDARD'
                                          ? 'selected'
                                          : ''}>
                                        STANDARD
                                    </option>

                                    <option value="VIP"
                                        ${hall.hallType == 'VIP'
                                          ? 'selected'
                                          : ''}>
                                        VIP
                                    </option>

                                    <option value="IMAX"
                                        ${hall.hallType == 'IMAX'
                                          ? 'selected'
                                          : ''}>
                                        IMAX
                                    </option>

                                    <option value="4DX"
                                        ${hall.hallType == '4DX'
                                          ? 'selected'
                                          : ''}>
                                        4DX
                                    </option>

                                    <option value="PREMIUM"
                                        ${hall.hallType == 'PREMIUM'
                                          ? 'selected'
                                          : ''}>
                                        PREMIUM
                                    </option>

                                </select>

                            </div>

                            <div class="form-group">

                                <label for="status">
                                    Trạng thái *
                                </label>

                                <select id="status"
                                        name="status"
                                        class="select-field"
                                        required>

                                    <option value="ACTIVE"
                                        ${hall.status == 'ACTIVE'
                                          ? 'selected'
                                          : ''}>
                                        ACTIVE - Đang hoạt động
                                    </option>

                                    <option value="MAINTENANCE"
                                        ${hall.status == 'MAINTENANCE'
                                          ? 'selected'
                                          : ''}>
                                        MAINTENANCE - Bảo trì
                                    </option>

                                    <option value="INACTIVE"
                                        ${hall.status == 'INACTIVE'
                                          ? 'selected'
                                          : ''}>
                                        INACTIVE - Ngưng hoạt động
                                    </option>

                                </select>

                            </div>

                        </div>

                        <div class="form-actions">

                            <a class="btn btn-ghost"
                               href="${ctx}/manager/halls">
                                Hủy
                            </a>

                            <button class="btn btn-primary"
                                    type="submit">
                                Lưu
                            </button>

                        </div>

                    </form>

                </div>

            </div>

        </section>

    </main>

</div>

<script>
    function calculateTotalSeats() {
        const seatRows = parseInt(
                document.getElementById("seatRows").value,
                10
        ) || 0;

        const seatsPerRow = parseInt(
                document.getElementById("seatsPerRow").value,
                10
        ) || 0;

        const totalSeats = seatRows * seatsPerRow;

        document.getElementById("totalSeatsPreview").value
                = totalSeats > 0
                ? totalSeats + " ghế"
                : "";
    }

    document.getElementById("seatRows").addEventListener(
            "input",
            calculateTotalSeats
    );

    document.getElementById("seatsPerRow").addEventListener(
            "input",
            calculateTotalSeats
    );

    document.addEventListener(
            "DOMContentLoaded",
            calculateTotalSeats
    );
</script>

</body>
</html>