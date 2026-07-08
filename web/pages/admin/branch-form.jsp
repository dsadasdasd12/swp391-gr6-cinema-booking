<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<%@ page contentType="text/html; charset=UTF-8" pageEncoding="UTF-8" %>

<c:set var="ctx" value="${pageContext.request.contextPath}" />
<c:set var="isEdit" value="${formMode == 'edit'}" />

<c:set var="formTitle" value="${isEdit ? 'Cập nhật chi nhánh' : 'Thêm chi nhánh mới'}" />
<c:set var="pageHeading" value="${isEdit ? 'Cập nhật thông tin chi nhánh' : 'Thêm chi nhánh mới'}" />
<c:set var="sectionTitle" value="${isEdit ? 'Thông tin chi nhánh cần cập nhật' : 'Thông tin chi nhánh mới'}" />
<c:set var="sectionIcon" value="${isEdit ? 'bi-pencil-square' : 'bi-building-add'}" />
<c:set var="submitIcon" value="${isEdit ? 'bi-check-lg' : 'bi-plus-lg'}" />
<c:set var="submitText" value="${isEdit ? 'Lưu thay đổi' : 'Tạo chi nhánh'}" />
<c:set var="formAction" value="${ctx}${isEdit ? '/admin/branches/edit' : '/admin/branches/create'}" />

<c:set var="pageTitle" value="${formTitle} — Rạp Việt CMS" scope="request" />

<c:set var="extraCss" scope="request">
    <link rel="stylesheet" href="${ctx}/assets/css/admin/branchmanagement.css?v=2">
</c:set>

<%@ include file="/pages/shared/header-admin.jsp" %>
<%@ include file="/pages/shared/sidebar-admin.jsp" %>

<div class="branch-form-page">
    <div class="rv-page-header">
        <div class="rv-page-header__left">
            <div class="rv-breadcrumb">
                <a href="${ctx}/admin/dashboard">Bảng điều khiển</a>
                <i class="bi bi-chevron-right rv-breadcrumb__sep"></i>
                <a href="${ctx}/admin/branches">Quản lý chi nhánh</a>
                <i class="bi bi-chevron-right rv-breadcrumb__sep"></i>
                <span class="rv-breadcrumb__current">${formTitle}</span>
            </div>

            <h1 class="rv-page-title">${pageHeading}</h1>

            <p class="rv-page-subtitle">
                Quản lý thông tin chi nhánh, trạng thái hoạt động và khung giờ mở cửa trong hệ thống RapViet Cinema.
            </p>
        </div>

        <div class="rv-page-header__right">
            <a class="rv-btn rv-btn--ghost" href="${ctx}/admin/branches">
                <i class="bi bi-arrow-left"></i>
                Quay lại danh sách
            </a>
        </div>
    </div>

    <c:if test="${not empty error}">
        <div class="branch-alert">
            <i class="bi bi-exclamation-octagon-fill"></i>
            <span><c:out value="${error}" /></span>
        </div>
    </c:if>

    <form method="post" action="${formAction}">
        <c:if test="${isEdit}">
            <input type="hidden" name="id" value="${branch.id}">
        </c:if>

        <input type="hidden"
               name="cinemaId"
               value="${empty branch.cinemaId or branch.cinemaId == 0 ? 1 : branch.cinemaId}">

        <div class="branch-form-grid">
            <div class="rv-card">
                <div class="rv-card__header">
                    <div class="branch-form-section-title">
                        <div class="branch-form-section-icon">
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
                        <div class="rv-form-group">
                            <label class="rv-label" for="name">
                                Tên chi nhánh
                                <span class="required">*</span>
                            </label>

                            <div class="branch-input-prefix">
                                <i class="bi bi-building"></i>
                                <input id="name"
                                       name="name"
                                       class="rv-input"
                                       type="text"
                                       required
                                       maxlength="150"
                                       placeholder="Ví dụ: RapViet Cầu Giấy"
                                       value="<c:out value='${branch.name}'/>">
                            </div>

                            <span class="branch-form-note">
                                Tên chi nhánh nên ngắn gọn, dễ nhận biết khi Manager và khách hàng tra cứu.
                            </span>
                        </div>

                        <div class="rv-form-group">
                            <label class="rv-label" for="phone">
                                Số điện thoại
                            </label>

                            <div class="branch-input-prefix">
                                <i class="bi bi-telephone"></i>
                                <input id="phone"
                                       name="phone"
                                       class="rv-input"
                                       type="tel"
                                       maxlength="20"
                                       placeholder="Ví dụ: 0901234567"
                                       value="<c:out value='${branch.phone}'/>">
                            </div>

                            <span class="branch-form-note">
                                Dùng để liên hệ vận hành và hiển thị khi cần hỗ trợ khách hàng.
                            </span>
                        </div>

                        <div class="rv-form-group branch-form-full-row">
                            <label class="rv-label" for="address">
                                Địa chỉ
                                <span class="required">*</span>
                            </label>

                            <div class="branch-input-prefix">
                                <i class="bi bi-geo-alt"></i>
                                <input id="address"
                                       name="address"
                                       class="rv-input"
                                       type="text"
                                       required
                                       maxlength="255"
                                       placeholder="Nhập địa chỉ chi tiết của chi nhánh"
                                       value="<c:out value='${branch.address}'/>">
                            </div>
                        </div>

                        <div class="rv-form-group">
                            <label class="rv-label" for="openTime">
                                Giờ mở cửa
                            </label>

                            <div class="branch-input-prefix">
                                <i class="bi bi-clock"></i>
                                <input id="openTime"
                                       name="openTime"
                                       class="rv-input"
                                       type="time"
                                       value="<c:out value='${branch.openTime}'/>">
                            </div>
                        </div>

                        <div class="rv-form-group">
                            <label class="rv-label" for="closeTime">
                                Giờ đóng cửa
                            </label>

                            <div class="branch-input-prefix">
                                <i class="bi bi-clock-history"></i>
                                <input id="closeTime"
                                       name="closeTime"
                                       class="rv-input"
                                       type="time"
                                       value="<c:out value='${branch.closeTime}'/>">
                            </div>
                        </div>

                        <div class="rv-form-group branch-form-full-row">
                            <label class="rv-label" for="status">
                                Trạng thái
                                <span class="required">*</span>
                            </label>

                            <select id="status"
                                    name="status"
                                    class="rv-select"
                                    required>
                                <option value="ACTIVE" ${branch.status == 'ACTIVE' ? 'selected' : ''}>
                                    ACTIVE - Đang hoạt động
                                </option>
                                <option value="INACTIVE" ${branch.status == 'INACTIVE' ? 'selected' : ''}>
                                    INACTIVE - Ngưng hoạt động
                                </option>
                            </select>

                            <span class="branch-form-note">
                                Chi nhánh ACTIVE mới nên được dùng cho các nghiệp vụ vận hành như phòng chiếu và lịch chiếu.
                            </span>
                        </div>
                    </div>

                    <div class="branch-form-actions">
                        <a class="rv-btn rv-btn--ghost" href="${ctx}/admin/branches">
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

            <div class="branch-side-column">
                <div class="branch-info-box">
                    <i class="bi bi-info-circle-fill"></i>
                    <div>
                        <strong>Lưu ý vận hành</strong><br>
                        Sau khi tạo chi nhánh, Admin có thể phân công Manager, còn Manager sẽ tiếp tục tạo Hall, phân bổ phim và tạo lịch chiếu cho chi nhánh đó.
                    </div>
                </div>

                <div class="rv-card">
                    <div class="rv-card__header">
                        <span class="rv-card__title">Checklist dữ liệu</span>
                    </div>

                    <div class="rv-card__body">
                        <ul class="branch-summary-list">
                            <li>
                                <i class="bi bi-check-circle-fill"></i>
                                <span>Tên và địa chỉ chi nhánh là thông tin bắt buộc.</span>
                            </li>
                            <li>
                                <i class="bi bi-check-circle-fill"></i>
                                <span>Giờ mở cửa / đóng cửa có thể để trống nếu chi nhánh chưa vận hành chính thức.</span>
                            </li>
                            <li>
                                <i class="bi bi-check-circle-fill"></i>
                                <span>Trạng thái INACTIVE dùng khi tạm ngưng hoạt động hoặc chưa sẵn sàng bán vé.</span>
                            </li>
                        </ul>
                    </div>
                </div>
            </div>
        </div>
    </form>
</div>

</main>
</div>
</body>
</html>