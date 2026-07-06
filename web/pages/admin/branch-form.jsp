<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<%@ page contentType="text/html; charset=UTF-8" pageEncoding="UTF-8" %>

<c:set var="ctx" value="${pageContext.request.contextPath}" />
<c:set var="isEdit" value="${formMode == 'edit'}" />
<c:set var="pageTitle" value="${isEdit ? 'Cập nhật chi nhánh' : 'Thêm chi nhánh mới'} — Rạp Việt CMS" scope="request" />

<c:set var="extraCss" scope="request">
    <style>
        .branch-form-page .branch-form-grid {
            display: grid;
            grid-template-columns: minmax(0, 2fr) minmax(280px, 0.9fr);
            gap: var(--s-6);
            align-items: start;
        }

        .branch-form-page .branch-form-section-title {
            display: flex;
            align-items: center;
            gap: var(--s-3);
        }

        .branch-form-page .branch-form-section-icon {
            width: 40px;
            height: 40px;
            display: inline-flex;
            align-items: center;
            justify-content: center;
            flex-shrink: 0;
            border-radius: var(--r-md);
            background: rgba(var(--primary-rgb), 0.1);
            color: var(--primary);
            font-size: 18px;
        }

        .branch-form-page .branch-form-section-title h2 {
            margin: 0;
            color: var(--n-900);
            font-size: var(--text-md);
            font-weight: 700;
            line-height: 1.35;
        }

        .branch-form-page .branch-form-section-title p {
            margin: 2px 0 0;
            color: var(--n-500);
            font-size: var(--text-sm);
            line-height: 1.45;
        }

        .branch-form-page .branch-input-prefix {
            position: relative;
        }

        .branch-form-page .branch-input-prefix i {
            position: absolute;
            top: 50%;
            left: var(--s-3);
            z-index: 1;
            color: var(--n-400);
            transform: translateY(-50%);
            pointer-events: none;
        }

        .branch-form-page .branch-input-prefix .rv-input,
        .branch-form-page .branch-input-prefix .rv-select {
            padding-left: 42px;
        }

        .branch-form-page .branch-form-note {
            display: block;
            margin-top: var(--s-2);
            color: var(--n-500);
            font-size: var(--text-sm);
            line-height: 1.55;
        }

        .branch-form-page .branch-alert {
            display: flex;
            align-items: flex-start;
            gap: var(--s-3);
            margin-bottom: var(--s-6);
            padding: var(--s-4);
            border: 1px solid #FECACA;
            border-radius: var(--r-md);
            background: var(--danger-bg);
            color: #B91C1C;
            font-size: var(--text-base);
            line-height: 1.55;
        }

        .branch-form-page .branch-alert i {
            margin-top: 2px;
            font-size: var(--text-md);
        }

        .branch-form-page .branch-info-box {
            display: flex;
            align-items: flex-start;
            gap: var(--s-3);
            padding: var(--s-4);
            border: 1px solid #BFDBFE;
            border-radius: var(--r-md);
            background: #EFF6FF;
            color: #1E40AF;
            line-height: 1.55;
        }

        .branch-form-page .branch-info-box i {
            margin-top: 2px;
            font-size: var(--text-md);
        }

        .branch-form-page .branch-summary-list {
            display: flex;
            flex-direction: column;
            gap: var(--s-3);
            margin: 0;
            padding: 0;
            list-style: none;
        }

        .branch-form-page .branch-summary-list li {
            display: flex;
            align-items: flex-start;
            gap: var(--s-3);
            padding-bottom: var(--s-3);
            border-bottom: 1px solid var(--border);
            color: var(--n-600);
            font-size: var(--text-sm);
            line-height: 1.55;
        }

        .branch-form-page .branch-summary-list li:last-child {
            padding-bottom: 0;
            border-bottom: 0;
        }

        .branch-form-page .branch-summary-list i {
            margin-top: 2px;
            color: var(--primary);
            font-size: 16px;
        }

        .branch-form-page .branch-form-actions {
            display: flex;
            justify-content: flex-end;
            gap: var(--s-3);
            margin-top: var(--s-6);
            padding-top: var(--s-5);
            border-top: 1px solid var(--border);
        }

        @media (max-width: 1100px) {
            .branch-form-page .branch-form-grid {
                grid-template-columns: 1fr;
            }
        }

        @media (max-width: 680px) {
            .branch-form-page .branch-form-actions,
            .branch-form-page .rv-page-header__right {
                flex-direction: column-reverse;
                align-items: stretch;
            }

            .branch-form-page .branch-form-actions .rv-btn,
            .branch-form-page .rv-page-header__right .rv-btn {
                width: 100%;
            }
        }
    </style>
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
                <span class="rv-breadcrumb__current">
                    <c:choose>
                        <c:when test="${isEdit}">Cập nhật chi nhánh</c:when>
                        <c:otherwise>Thêm chi nhánh mới</c:otherwise>
                    </c:choose>
                </span>
            </div>

            <h1 class="rv-page-title">
                <c:choose>
                    <c:when test="${isEdit}">Cập nhật thông tin chi nhánh</c:when>
                    <c:otherwise>Thêm chi nhánh mới</c:otherwise>
                </c:choose>
            </h1>

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

    <form method="post"
          action="${ctx}${isEdit ? '/admin/branches/edit' : '/admin/branches/create'}">

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
                            <i class="bi ${isEdit ? 'bi-pencil-square' : 'bi-building-add'}"></i>
                        </div>

                        <div>
                            <h2>
                                <c:choose>
                                    <c:when test="${isEdit}">Thông tin chi nhánh cần cập nhật</c:when>
                                    <c:otherwise>Thông tin chi nhánh mới</c:otherwise>
                                </c:choose>
                            </h2>
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

                        <div class="rv-form-group" style="grid-column: 1 / -1;">
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

                        <div class="rv-form-group" style="grid-column: 1 / -1;">
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
                            <i class="bi ${isEdit ? 'bi-check-lg' : 'bi-plus-lg'}"></i>
                            <c:choose>
                                <c:when test="${isEdit}">Lưu thay đổi</c:when>
                                <c:otherwise>Tạo chi nhánh</c:otherwise>
                            </c:choose>
                        </button>
                    </div>
                </div>
            </div>

            <div style="display: flex; flex-direction: column; gap: var(--s-6);">
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