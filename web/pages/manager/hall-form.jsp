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
        <title>${isEdit ? 'Cập nhật phòng chiếu' : 'Thêm phòng chiếu'} - RapViet Cinema</title>
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
                    <a class="active" href="${ctx}/manager/halls">Quản lý phòng chiếu</a>
                    <a href="${ctx}/manager/showtimes">Lịch chiếu</a>
                    <a href="#">Gán phim</a>
                    <a href="#">Cài đặt chi nhánh</a>
                    <a href="${ctx}/logout">Đăng xuất</a>
                </nav>
            </aside>

            <main class="admin-main">
                <div class="admin-topbar">
                    <div>
                        <strong>${isEdit ? 'Cập nhật phòng chiếu' : 'Thêm phòng chiếu'}</strong>
                        <span>Branch Manager quản lý thông tin phòng chiếu</span>
                    </div>
                </div>

                <section class="admin-content">
                    <div class="page-heading">
                        <div>
                            <h1>${isEdit ? 'Cập nhật phòng chiếu' : 'Thêm phòng chiếu mới'}</h1>
                            <p>Nhập thông tin phòng chiếu, sức chứa, loại phòng và trạng thái.</p>
                        </div>

                        <a class="btn btn-ghost" href="${ctx}/manager/halls">Quay lại</a>
                    </div>

                    <c:if test="${not empty error}">
                        <div class="alert alert-error">
                            <c:out value="${error}" />
                        </div>
                    </c:if>

                    <div class="panel">
                        <div class="panel-header">Thông tin phòng chiếu</div>

                        <div class="panel-body">
                            <form method="post" action="${ctx}${isEdit ? '/manager/halls/edit' : '/manager/halls/create'}">
                                <c:if test="${isEdit}">
                                    <input type="hidden" name="id" value="${hall.id}">
                                </c:if>

                                <input type="hidden" name="branchId" value="${hall.branchId}">

                                <div class="form-grid">
                                    <div class="form-group">
                                        <label for="name">Tên phòng chiếu *</label>
                                        <input id="name"
                                               name="name"
                                               class="input-field"
                                               type="text"
                                               required
                                               placeholder="Ví dụ: Phòng 01"
                                               value="${hall.name}">
                                    </div>

                                    <div class="form-group">
                                        <label for="totalSeats">Tổng số ghế *</label>
                                        <input id="totalSeats"
                                               name="totalSeats"
                                               class="input-field"
                                               type="number"
                                               min="1"
                                               required
                                               placeholder="Ví dụ: 120"
                                               value="${hall.totalSeats == 0 ? '' : hall.totalSeats}">
                                    </div>

                                    <div class="form-group">
                                        <label for="hallType">Loại phòng *</label>
                                        <select id="hallType" name="hallType" class="select-field" required>
                                            <option value="STANDARD" ${hall.hallType == 'STANDARD' ? 'selected' : ''}>
                                                STANDARD
                                            </option>
                                            <option value="VIP" ${hall.hallType == 'VIP' ? 'selected' : ''}>
                                                VIP
                                            </option>
                                            <option value="IMAX" ${hall.hallType == 'IMAX' ? 'selected' : ''}>
                                                IMAX
                                            </option>
                                            <option value="4DX" ${hall.hallType == '4DX' ? 'selected' : ''}>
                                                4DX
                                            </option>
                                            <option value="PREMIUM" ${hall.hallType == 'PREMIUM' ? 'selected' : ''}>
                                                PREMIUM
                                            </option>
                                        </select>
                                    </div>

                                    <div class="form-group">
                                        <label for="status">Trạng thái *</label>
                                        <select id="status" name="status" class="select-field" required>
                                            <option value="ACTIVE" ${hall.status == 'ACTIVE' ? 'selected' : ''}>
                                                ACTIVE - Đang hoạt động
                                            </option>
                                            <option value="MAINTENANCE" ${hall.status == 'MAINTENANCE' ? 'selected' : ''}>
                                                MAINTENANCE - Bảo trì
                                            </option>
                                            <option value="INACTIVE" ${hall.status == 'INACTIVE' ? 'selected' : ''}>
                                                INACTIVE - Ngưng hoạt động
                                            </option>
                                        </select>
                                    </div>
                                </div>

                                <div class="form-actions">

                                    <a class="btn btn-secondary"
                                       href="${pageContext.request.contextPath}/manager/halls">
                                        Quay lại
                                    </a>

                                    <c:if test="${hall.id > 0}">
                                        <a class="btn btn-secondary"
                                           href="${pageContext.request.contextPath}/manager/seat-config?hallId=${hall.id}">
                                            Cấu hình ghế
                                        </a>
                                    </c:if>

                                    <button type="submit" class="btn btn-primary">
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