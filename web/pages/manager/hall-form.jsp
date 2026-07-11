<%--
    Document   : hall-form
    Purpose    : Hall Create/Edit - RapViet Cinema Branch Manager
--%>

<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<%@ page contentType="text/html; charset=UTF-8" pageEncoding="UTF-8" %>

<c:set var="ctx" value="${pageContext.request.contextPath}" />
<c:set var="isEdit" value="${formMode == 'edit'}" />

<c:set var="formTitle" value="${isEdit ? 'Cập nhật phòng chiếu' : 'Thêm phòng chiếu mới'}" />
<c:set var="pageHeading" value="${isEdit ? 'Cập nhật thông tin phòng chiếu' : 'Thêm phòng chiếu mới'}" />
<c:set var="sectionTitle" value="${isEdit ? 'Thông tin phòng chiếu cần cập nhật' : 'Thông tin phòng chiếu mới'}" />
<c:set var="sectionIcon" value="${isEdit ? 'bi-pencil-square' : 'bi-door-open-fill'}" />
<c:set var="submitIcon" value="${isEdit ? 'bi-check-lg' : 'bi-plus-lg'}" />
<c:set var="submitText" value="${isEdit ? 'Lưu thay đổi' : 'Tạo phòng chiếu'}" />
<c:set var="formAction" value="${ctx}${isEdit ? '/manager/halls/edit' : '/manager/halls/create'}" />

<c:set var="pageTitle" value="${formTitle} — Rạp Việt CMS" scope="request" />

<c:set var="extraCss" scope="request">
    <link rel="stylesheet" href="${ctx}/assets/css/manager/hall.css?v=1">
</c:set>

<%@ include file="/pages/shared/header-admin.jsp" %>
<%@ include file="/pages/shared/sidebar-admin.jsp" %>

<div class="hall-form-page">
    <div class="rv-page-header">
        <div class="rv-page-header__left">
            <div class="rv-breadcrumb">
                <a href="${ctx}/manager/dashboard">Bảng điều khiển</a>
                <i class="bi bi-chevron-right rv-breadcrumb__sep"></i>
                <a href="${ctx}/manager/halls">Quản lý phòng chiếu</a>
                <i class="bi bi-chevron-right rv-breadcrumb__sep"></i>
                <span class="rv-breadcrumb__current">${formTitle}</span>
            </div>

            <h1 class="rv-page-title">${pageHeading}</h1>

            <p class="rv-page-subtitle">
                Thiết lập tên phòng, cấu hình ghế, loại phòng và trạng thái hoạt động của Hall trong chi nhánh được phân công.
            </p>
        </div>

        <div class="rv-page-header__right">
            <a class="rv-btn rv-btn--ghost" href="${ctx}/manager/halls">
                <i class="bi bi-arrow-left"></i>
                Quay lại danh sách
            </a>
        </div>
    </div>

    <c:if test="${not empty error}">
        <div class="hall-alert">
            <i class="bi bi-exclamation-octagon-fill"></i>
            <span><c:out value="${error}" /></span>
        </div>
    </c:if>

    <form method="post" action="${formAction}">
        <input type="hidden" name="branchId" value="${branch.id}">

        <c:if test="${isEdit}">
            <input type="hidden" name="id" value="${hall.id}">
        </c:if>

        <div class="hall-form-grid">
            <div class="rv-card">
                <div class="rv-card__header">
                    <div class="hall-form-section-title">
                        <div class="hall-form-section-icon">
                            <i class="bi ${sectionIcon}"></i>
                        </div>

                        <div>
                            <h2>${sectionTitle}</h2>
                            <p>Các trường có dấu * là bắt buộc.</p>
                        </div>
                    </div>
                </div>

                <div class="rv-card__body">
                    <div class="rv-form-container">
                        <div class="rv-form-group hall-form-full-row">
                            <label class="rv-label">Chi nhánh quản lý</label>

                            <div class="hall-input-prefix">
                                <i class="bi bi-building"></i>
                                <input class="rv-input"
                                       type="text"
                                       value="<c:out value='${branch.name}'/>"
                                       readonly>
                            </div>

                            <span class="hall-form-note">
                                Chi nhánh được lấy tự động từ tài khoản Manager đang đăng nhập.
                            </span>
                        </div>

                        <div class="rv-form-group hall-form-full-row">
                            <label class="rv-label" for="name">
                                Tên phòng chiếu
                                <span class="required">*</span>
                            </label>

                            <div class="hall-input-prefix">
                                <i class="bi bi-door-open"></i>
                                <input id="name"
                                       name="name"
                                       class="rv-input"
                                       type="text"
                                       required
                                       maxlength="100"
                                       placeholder="Ví dụ: Phòng 01"
                                       value="<c:out value='${hall.name}'/>">
                            </div>

                            <span class="hall-form-note">
                                Tên Hall cần dễ phân biệt trong cùng một chi nhánh.
                            </span>
                        </div>

                        <div class="rv-form-group">
                            <label class="rv-label" for="seatRows">
                                Số hàng ghế
                                <span class="required">*</span>
                            </label>

                            <div class="hall-input-prefix">
                                <i class="bi bi-layout-three-columns"></i>
                                <input id="seatRows"
                                       name="seatRows"
                                       class="rv-input"
                                       type="number"
                                       min="1"
                                       required
                                       placeholder="Ví dụ: 10"
                                       value="${hall.seatRows == 0 ? '' : hall.seatRows}">
                            </div>
                        </div>

                        <div class="rv-form-group">
                            <label class="rv-label" for="seatsPerRow">
                                Số ghế mỗi hàng
                                <span class="required">*</span>
                            </label>

                            <div class="hall-input-prefix">
                                <i class="bi bi-grid-3x3-gap"></i>
                                <input id="seatsPerRow"
                                       name="seatsPerRow"
                                       class="rv-input"
                                       type="number"
                                       min="1"
                                       required
                                       placeholder="Ví dụ: 12"
                                       value="${hall.seatsPerRow == 0 ? '' : hall.seatsPerRow}">
                            </div>
                        </div>

                        <div class="rv-form-group hall-form-full-row">
                            <label class="rv-label" for="totalSeatsPreview">
                                Tổng số ghế
                            </label>

                            <div class="hall-input-prefix">
                                <i class="bi bi-people"></i>
                                <input id="totalSeatsPreview"
                                       class="rv-input hall-total-seat"
                                       type="text"
                                       readonly
                                       placeholder="Tự động tính">
                            </div>

                            <span class="hall-form-note">
                                Hệ thống tự tính: số hàng ghế × số ghế mỗi hàng.
                            </span>
                        </div>

                        <div class="rv-form-group">
                            <label class="rv-label" for="hallType">
                                Loại phòng
                                <span class="required">*</span>
                            </label>

                            <select id="hallType"
                                    name="hallType"
                                    class="rv-select"
                                    required>
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

                            <span class="hall-form-note">
                                Loại phòng ảnh hưởng đến trải nghiệm và cấu hình vận hành của Hall.
                            </span>
                        </div>

                        <div class="rv-form-group">
                            <label class="rv-label" for="status">
                                Trạng thái
                                <span class="required">*</span>
                            </label>

                            <select id="status"
                                    name="status"
                                    class="rv-select"
                                    required>
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

                            <span class="hall-form-note">
                                Hall ACTIVE mới nên được dùng để phân bổ phim hoặc tạo lịch chiếu.
                            </span>
                        </div>
                    </div>

                    <div class="hall-form-actions">
                        <a class="rv-btn rv-btn--ghost" href="${ctx}/manager/halls">
                            <i class="bi bi-x-lg"></i>
                            Hủy
                        </a>

                        <button class="rv-btn rv-btn--primary" type="submit">
                            <i class="bi ${submitIcon}"></i>
                            ${submitText}
                        </button>
                    </div>
                </div>
            </div>

            <div class="hall-side-column">
                <div class="hall-info-box">
                    <i class="bi bi-info-circle-fill"></i>
                    <div>
                        <strong>Lưu ý vận hành</strong><br>
                        Sau khi tạo phòng chiếu, Manager có thể phân bổ phim vào Hall và tạo suất chiếu theo lịch vận hành của chi nhánh.
                    </div>
                </div>

                <div class="rv-card">
                    <div class="rv-card__header">
                        <span class="rv-card__title">Checklist dữ liệu</span>
                    </div>

                    <div class="rv-card__body">
                        <ul class="hall-summary-list">
                            <li>
                                <i class="bi bi-check-circle-fill"></i>
                                <span>Tên phòng chiếu là thông tin bắt buộc và không nên trùng trong cùng chi nhánh.</span>
                            </li>
                            <li>
                                <i class="bi bi-check-circle-fill"></i>
                                <span>Số hàng ghế và số ghế mỗi hàng phải lớn hơn 0 để hệ thống tự tính tổng số ghế.</span>
                            </li>
                            <li>
                                <i class="bi bi-check-circle-fill"></i>
                                <span>Trạng thái MAINTENANCE dùng khi phòng chiếu đang bảo trì hoặc tạm ngưng vận hành.</span>
                            </li>
                        </ul>
                    </div>
                </div>
            </div>
        </div>
    </form>
</div>

<script>
    document.addEventListener('DOMContentLoaded', function () {
        var seatRowsInput = document.getElementById('seatRows');
        var seatsPerRowInput = document.getElementById('seatsPerRow');
        var totalSeatsInput = document.getElementById('totalSeatsPreview');

        function calculateTotalSeats() {
            var seatRows = parseInt(seatRowsInput.value, 10) || 0;
            var seatsPerRow = parseInt(seatsPerRowInput.value, 10) || 0;
            var totalSeats = seatRows * seatsPerRow;

            totalSeatsInput.value = totalSeats > 0 ? totalSeats + ' ghế' : '';
        }

        if (!seatRowsInput || !seatsPerRowInput || !totalSeatsInput) {
            return;
        }

        seatRowsInput.addEventListener('input', calculateTotalSeats);
        seatsPerRowInput.addEventListener('input', calculateTotalSeats);
        calculateTotalSeats();
    });
</script>

</main>
</div>
</body>
</html>