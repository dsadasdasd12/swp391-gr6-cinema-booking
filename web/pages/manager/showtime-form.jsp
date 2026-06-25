<%-- 
    Document   : showtime-form
    Created on : Jun 10, 2026, 9:07:37 PM
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
    <title>${isEdit ? 'Cập nhật suất chiếu' : 'Tạo suất chiếu'} - RapViet Cinema</title>
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
            <a href="${ctx}/manager/dashboard">Dashboard</a>
            <a href="${ctx}/manager/halls">Quản lý phòng chiếu</a>
            <a class="active" href="${ctx}/manager/showtimes">Lịch chiếu</a>
            <a href="#">Gán phim</a>
            <a href="#">Cài đặt chi nhánh</a>
            <a href="${ctx}/logout">Đăng xuất</a>
        </nav>
    </aside>

    <main class="admin-main">
        <div class="admin-topbar">
            <div>
                <strong>${isEdit ? 'Cập nhật suất chiếu' : 'Tạo suất chiếu mới'}</strong>
                <span>Chọn phim, phòng chiếu và thời gian chiếu</span>
            </div>
        </div>

        <section class="admin-content">
            <div class="page-heading">
                <div>
                    <h1>${isEdit ? 'Cập nhật suất chiếu' : 'Tạo suất chiếu mới'}</h1>
                    <p>Hệ thống sẽ tự tính giờ kết thúc theo thời lượng phim và kiểm tra trùng lịch.</p>
                </div>

                <a class="btn btn-ghost" href="${ctx}/manager/showtimes">Quay lại</a>
            </div>

            <c:if test="${not empty error}">
                <div class="alert alert-error">
                    <c:out value="${error}" />
                </div>
            </c:if>

            <div class="panel">
                <div class="panel-header">Thông tin suất chiếu</div>

                <div class="panel-body">
                    <form method="post" action="${ctx}${isEdit ? '/manager/showtimes/edit' : '/manager/showtimes/create'}">

                        <c:if test="${isEdit}">
                            <input type="hidden" name="id" value="${showtime.id}">
                        </c:if>

                        <div class="form-grid">
                            <div class="form-group">
                                <label for="movieId">Phim *</label>
                                <select id="movieId" name="movieId" class="select-field" required>
                                    <option value="">-- Chọn phim --</option>

                                    <c:forEach var="m" items="${movies}">
                                        <option value="${m.id}" ${showtime.movieId == m.id ? 'selected' : ''}>
                                            <c:out value="${m.title}" /> - ${m.durationMin} phút
                                        </option>
                                    </c:forEach>
                                </select>
                            </div>

                            <div class="form-group">
                                <label for="hallId">Phòng chiếu *</label>
                                <select id="hallId" name="hallId" class="select-field" required>
                                    <option value="">-- Chọn phòng chiếu --</option>

                                    <c:forEach var="group" items="${branchHallGroups}">
                                        <optgroup label="${group.branch.name}">
                                            <c:forEach var="h" items="${group.halls}">
                                                <option value="${h.id}" ${showtime.hallId == h.id ? 'selected' : ''}>
                                                    <c:out value="${h.name}" /> - <c:out value="${h.hallType}" />
                                                </option>
                                            </c:forEach>
                                        </optgroup>
                                    </c:forEach>
                                </select>
                            </div>

                            <div class="form-group">
                                <label for="startTime">Thời gian bắt đầu *</label>
                                <input id="startTime"
                                       name="startTime"
                                       class="input-field"
                                       type="datetime-local"
                                       required
                                       value="${showtime.startInputValue}">
                            </div>

                            <div class="form-group">
                                <label for="basePrice">Giá vé cơ bản *</label>
                                <input id="basePrice"
                                       name="basePrice"
                                       class="input-field"
                                       type="number"
                                       min="0"
                                       step="1000"
                                       required
                                       value="${showtime.basePrice}">
                            </div>

                            <div class="form-group">
                                <label for="status">Trạng thái *</label>
                                <select id="status" name="status" class="select-field" required>
                                    <option value="SCHEDULED" ${showtime.status == 'SCHEDULED' ? 'selected' : ''}>
                                        SCHEDULED - Đã lên lịch
                                    </option>

                                    <option value="ON_SALE" ${showtime.status == 'ON_SALE' ? 'selected' : ''}>
                                        ON_SALE - Đang bán vé
                                    </option>
                                </select>
                            </div>
                        </div>

                        <div class="form-actions">
                            <a class="btn btn-ghost" href="${ctx}/manager/showtimes">Hủy</a>
                            <button class="btn btn-primary" type="submit">
                                Lưu
                            </button>
                        </div>
                    </form>
                </div>
            </div>
        </section>
    </main>
</div>
</body>
</html>