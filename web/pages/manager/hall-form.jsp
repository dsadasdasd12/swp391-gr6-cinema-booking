<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<%@ page contentType="text/html; charset=UTF-8" pageEncoding="UTF-8" %>

<c:set var="ctx" value="${pageContext.request.contextPath}" />
<c:set var="topUser" value="${sessionScope.user}" />
<c:set var="isEdit" value="${formMode == 'edit'}" />

<!DOCTYPE html>
<html lang="vi">
<head>
    <meta charset="UTF-8">
    <meta name="viewport" content="width=device-width, initial-scale=1">

    <title>
        ${isEdit ? 'Cập nhật phòng chiếu' : 'Thêm phòng chiếu'}
        - Rạp Việt CMS
    </title>


    <link href="https://cdn.jsdelivr.net/npm/bootstrap@5.3.3/dist/css/bootstrap.min.css"
          rel="stylesheet"
          integrity="sha384-QWTKZyjpPEjISv5WaRU9OFeRpok6YctnYmDr5pNlyT2bRjXh0JMhjY6hW+ALEwIH"
          crossorigin="anonymous">

    <link href="https://cdn.jsdelivr.net/npm/bootstrap-icons@1.11.3/font/bootstrap-icons.css"
          rel="stylesheet">

    <link href="https://fonts.googleapis.com/css2?family=Inter:wght@400;500;600;700&family=Outfit:wght@600;700;800&display=swap"
          rel="stylesheet">

    <link rel="stylesheet" href="${ctx}/assets/css/admin/variables.css?v=redblack">
    <link rel="stylesheet" href="${ctx}/assets/css/admin/base.css?v=redblack">
    <link rel="stylesheet" href="${ctx}/assets/css/admin/layout.css?v=redblack">
    <link rel="stylesheet" href="${ctx}/assets/css/admin/components.css?v=redblack">
    <link rel="stylesheet" href="${ctx}/assets/css/admin/tables.css?v=redblack">
    <link rel="stylesheet" href="${ctx}/assets/css/admin/forms.css?v=redblack">

    <style>
        /*
         * CSS chỉ có hiệu lực trong hall-form.jsp.
         * Không ảnh hưởng Admin, Customer hoặc Manager page khác.
         */

        .manager-hall-form-page .manager-topbar-context {
            display: inline-flex;
            align-items: center;
            gap: var(--s-2);
            padding-right: var(--s-4);
            border-right: 1px solid var(--border);
            color: var(--n-500);
            font-size: var(--text-sm);
        }

        .manager-hall-form-page .manager-topbar-context i {
            color: var(--primary-light);
        }

        .manager-hall-form-page .manager-sidebar-info {
            display: flex;
            flex-direction: column;
            gap: 3px;
            margin: 0 var(--s-4) var(--s-4);
            padding: var(--s-4);
            border: 1px solid rgba(255, 255, 255, 0.08);
            border-radius: var(--r-lg);
            background: linear-gradient(
                145deg,
                rgba(229, 9, 20, 0.18),
                rgba(255, 255, 255, 0.02)
            );
            color: rgba(255, 255, 255, 0.76);
            font-size: var(--text-xs);
            line-height: 1.5;
        }

        .manager-hall-form-page .manager-sidebar-info__label {
            color: rgba(255, 255, 255, 0.42);
            font-size: 10px;
            font-weight: 700;
            letter-spacing: 0.08em;
        }

        .manager-hall-form-page .manager-sidebar-info strong {
            color: #ffffff;
            font-size: var(--text-base);
        }

        .manager-hall-form-page .manager-form-header {
            display: flex;
            align-items: center;
            gap: var(--s-3);
        }

        .manager-hall-form-page .manager-form-header__icon {
            display: inline-flex;
            width: 40px;
            height: 40px;
            align-items: center;
            justify-content: center;
            flex-shrink: 0;
            border-radius: var(--r-md);
            background: rgba(229, 9, 20, 0.14);
            color: var(--primary-light);
            font-size: 18px;
        }

        .manager-hall-form-page .manager-form-header__title {
            margin: 0;
            color: var(--n-900);
            font-size: var(--text-md);
            font-weight: 700;
            line-height: 1.35;
        }

        .manager-hall-form-page .manager-form-header__description {
            margin-top: 2px;
            color: var(--n-500);
            font-size: var(--text-sm);
            line-height: 1.45;
        }

        .manager-hall-form-page .manager-form-info {
            display: flex;
            align-items: flex-start;
            gap: var(--s-3);
            margin-bottom: var(--s-6);
            padding: var(--s-4);
            border: 1px solid #BFDBFE;
            border-radius: var(--r-md);
            background: #EFF6FF;
            color: #1E40AF;
            line-height: 1.55;
        }

        .manager-hall-form-page .manager-form-info i {
            margin-top: 2px;
            font-size: var(--text-md);
        }

        .manager-hall-form-page .manager-form-info strong {
            color: #1E3A8A;
        }

        .manager-hall-form-page .manager-form-note {
            display: block;
            margin-top: var(--s-2);
            color: var(--n-500);
            font-size: var(--text-sm);
            line-height: 1.55;
        }

        .manager-hall-form-page .manager-input-prefix {
            position: relative;
        }

        .manager-hall-form-page .manager-input-prefix i {
            position: absolute;
            top: 50%;
            left: var(--s-3);
            z-index: 1;
            color: var(--n-400);
            transform: translateY(-50%);
            pointer-events: none;
        }

        .manager-hall-form-page .manager-input-prefix .rv-input {
            padding-left: 42px;
        }

        .manager-hall-form-page .manager-total-seat {
            background: var(--n-50);
            color: var(--n-700);
            font-weight: 600;
        }

        .manager-hall-form-page .manager-status-hint {
            display: flex;
            align-items: flex-start;
            gap: var(--s-2);
            margin-top: var(--s-4);
            padding: var(--s-3);
            border: 1px solid var(--border);
            border-radius: var(--r-md);
            background: var(--n-50);
            color: var(--n-600);
            font-size: var(--text-sm);
            line-height: 1.55;
        }

        .manager-hall-form-page .manager-status-hint i {
            margin-top: 2px;
            color: var(--primary-light);
        }

        .manager-hall-form-page .manager-form-actions {
            display: flex;
            justify-content: flex-end;
            gap: var(--s-3);
            margin-top: var(--s-6);
            padding-top: var(--s-5);
            border-top: 1px solid var(--border);
        }

        .manager-hall-form-page .manager-alert {
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

        .manager-hall-form-page .manager-alert i {
            margin-top: 2px;
            font-size: var(--text-md);
        }

        @media (max-width: 900px) {
            .manager-hall-form-page .manager-topbar-context {
                display: none;
            }
        }

        @media (max-width: 680px) {
            .manager-hall-form-page .manager-form-actions {
                flex-direction: column-reverse;
                align-items: stretch;
            }

            .manager-hall-form-page .manager-form-actions .rv-btn {
                width: 100%;
            }
        }
    </style>

    <script src="${ctx}/assets/js/main.js" charset="UTF-8" defer></script>
    <script src="${ctx}/assets/js/confirm.js" charset="UTF-8" defer></script>

    <script src="https://cdn.jsdelivr.net/npm/bootstrap@5.3.3/dist/js/bootstrap.bundle.min.js"
            integrity="sha384-YvpcrYf0tY3lHB60NNkmXc5s9fDVZLESaAA55NDzOxhy9GkcIdslK1eN7N6jIeHz"
            crossorigin="anonymous"
            defer></script>
</head>

<body class="manager-hall-form-page">

<header class="rv-topbar">

    <button type="button"
            class="rv-topbar__toggle"
            title="Mở menu"
            aria-label="Mở menu">
        <i class="bi bi-list"></i>
    </button>

    <a class="rv-topbar__brand"
       href="${ctx}/manager/dashboard">

        <div class="rv-topbar__brand-icon">
            <i class="bi bi-film"></i>
        </div>

        <span class="rv-topbar__brand-text">
            RẠP VIỆT <span>CMS</span>
        </span>
    </a>

    <div class="rv-topbar__actions">

        <div class="manager-topbar-context">
            <i class="bi bi-building"></i>
            <span>Phân hệ Quản lý chi nhánh</span>
        </div>

        <div class="rv-topbar__user">

            <div class="rv-topbar__avatar">
                M
            </div>

            <div class="rv-topbar__user-info">
                <span class="rv-topbar__user-name">
                    <c:out value="${not empty topUser
                                   ? topUser.fullName
                                   : 'Branch Manager'}" />
                </span>

                <span class="rv-topbar__user-role">
                    Quản lý chi nhánh
                </span>
            </div>

            <i class="bi bi-chevron-down rv-topbar__user-arrow"></i>

            <div class="rv-topbar__dropdown">

                <div class="rv-topbar__dropdown-header">
                    <div style="font-weight: 600; color: var(--n-800);">
                        <c:out value="${not empty topUser
                                       ? topUser.fullName
                                       : 'Branch Manager'}" />
                    </div>

                    <div class="email">
                        <c:out value="${not empty topUser
                                       ? topUser.email
                                       : ''}" />
                    </div>
                </div>

                <a href="${ctx}/manager/dashboard"
                   class="rv-topbar__dropdown-item">
                    <i class="bi bi-grid-1x2-fill"></i>
                    Bảng điều khiển
                </a>

                <div class="rv-topbar__dropdown-divider"></div>

                <a href="${ctx}/logout"
                   class="rv-topbar__dropdown-item danger">
                    <i class="bi bi-box-arrow-right"></i>
                    Đăng xuất
                </a>
            </div>
        </div>
    </div>
</header>

<div class="rv-wrapper">

    <aside class="rv-sidebar">

        <div class="manager-sidebar-info">
            <span class="manager-sidebar-info__label">PHÂN HỆ</span>
            <strong>Branch Manager</strong>
            <span>Quản lý vận hành chi nhánh được phân công</span>
        </div>

        <div class="rv-nav__group">
            <a href="${ctx}/manager/dashboard"
               class="rv-nav__item">

                <i class="bi bi-grid-1x2-fill"></i>
                Bảng điều khiển
            </a>
        </div>

        <div class="rv-nav__label">
            Vận hành chi nhánh
        </div>

        <div class="rv-nav__group">
            <a href="${ctx}/manager/halls"
               class="rv-nav__item active">

                <i class="bi bi-door-open-fill"></i>
                Quản lý phòng chiếu
            </a>
        </div>

        <div class="rv-nav__group">

            <div class="rv-nav__item"
                 role="button"
                 tabindex="0">

                <i class="bi bi-film"></i>
                Phân bổ phim

                <i class="bi bi-chevron-right rv-nav__arrow"></i>
            </div>

            <div class="rv-nav__sub">

                <a href="${ctx}/manager/movie-assignments/branches"
                   class="rv-nav__sub-item">
                    Phim tại chi nhánh
                </a>

                <a href="${ctx}/manager/movie-assignments/halls"
                   class="rv-nav__sub-item">
                    Phim tại phòng chiếu
                </a>

                <a href="${ctx}/manager/movie-durations"
                   class="rv-nav__sub-item">
                    Thời lượng phim
                </a>
            </div>
        </div>

        <div class="rv-nav__group">
            <a href="${ctx}/manager/showtimesmanagement"
               class="rv-nav__item">

                <i class="bi bi-calendar-week-fill"></i>
                Quản lý lịch chiếu
            </a>
        </div>

        <div class="rv-nav__spacer"></div>

        <div class="rv-nav__divider"></div>

        <div class="rv-nav__group">
            <a href="${ctx}/logout"
               class="rv-nav__item logout">

                <i class="bi bi-box-arrow-right"></i>
                Đăng xuất
            </a>
        </div>
    </aside>

    <main class="rv-main">

        <div class="rv-page-header">

            <div class="rv-page-header__left">

                <div class="rv-breadcrumb">
                    <a href="${ctx}/manager/dashboard">
                        Quản lý chi nhánh
                    </a>

                    <i class="bi bi-chevron-right rv-breadcrumb__sep"></i>

                    <a href="${ctx}/manager/halls">
                        Quản lý phòng chiếu
                    </a>

                    <i class="bi bi-chevron-right rv-breadcrumb__sep"></i>

                    <span class="rv-breadcrumb__current">
                        ${isEdit ? 'Cập nhật phòng chiếu' : 'Thêm phòng chiếu'}
                    </span>
                </div>

                <h1 class="rv-page-title">
                    ${isEdit
                      ? 'Cập nhật phòng chiếu'
                      : 'Thêm phòng chiếu mới'}
                </h1>

                <p class="rv-page-subtitle">
                    Thiết lập tên phòng, cấu hình ghế, loại phòng và trạng thái hoạt động.
                </p>
            </div>

            <div class="rv-page-header__right">
                <a class="rv-btn rv-btn--ghost"
                   href="${ctx}/manager/halls">

                    <i class="bi bi-arrow-left"></i>
                    Quay lại danh sách
                </a>
            </div>
        </div>

        <c:if test="${not empty error}">
            <div class="manager-alert">
                <i class="bi bi-exclamation-octagon-fill"></i>

                <span>
                    <c:out value="${error}" />
                </span>
            </div>
        </c:if>

        <div class="manager-form-info">
            <i class="bi bi-building-check-fill"></i>

            <div>
                Chi nhánh đang quản lý:
                <strong>
                    <c:out value="${branch.name}" />
                </strong>
                <br>
                Chỉ phòng chiếu thuộc chi nhánh này mới được tạo hoặc cập nhật.
            </div>
        </div>

        <div class="rv-card">

            <div class="rv-card__header">

                <div class="manager-form-header">

                    <div class="manager-form-header__icon">
                        <i class="bi ${isEdit ? 'bi-pencil-square' : 'bi-door-open-fill'}"></i>
                    </div>

                    <div>
                        <h2 class="manager-form-header__title">
                            ${isEdit
                              ? 'Thông tin phòng chiếu cần cập nhật'
                              : 'Thông tin phòng chiếu mới'}
                        </h2>

                        <div class="manager-form-header__description">
                            Các trường có dấu * là bắt buộc.
                        </div>
                    </div>
                </div>
            </div>

            <div class="rv-card__body">

                <form method="post"
                      action="${ctx}${isEdit
                              ? '/manager/halls/edit'
                              : '/manager/halls/create'}">

                    <%-- Giữ nguyên branchId để Controller xác định đúng chi nhánh. --%>
                    <input type="hidden"
                           name="branchId"
                           value="${branch.id}">

                    <%-- Chỉ gửi id khi đang Edit Hall. --%>
                    <c:if test="${isEdit}">
                        <input type="hidden"
                               name="id"
                               value="${hall.id}">
                    </c:if>

                    <div class="rv-form-container">

                        <div class="rv-form-group">

                            <label class="rv-label">
                                Chi nhánh quản lý
                            </label>

                            <div class="manager-input-prefix">
                                <i class="bi bi-building"></i>

                                <input class="rv-input"
                                       type="text"
                                       value="${branch.name}"
                                       readonly>
                            </div>

                            <span class="manager-form-note">
                                Chi nhánh được lấy tự động từ tài khoản Manager đang đăng nhập.
                            </span>
                        </div>

                        <div class="rv-form-group">

                            <label class="rv-label"
                                   for="name">
                                Tên phòng chiếu
                                <span class="required">*</span>
                            </label>

                            <div class="manager-input-prefix">
                                <i class="bi bi-door-open"></i>

                                <input id="name"
                                       name="name"
                                       class="rv-input"
                                       type="text"
                                       required
                                       placeholder="Ví dụ: Phòng 01"
                                       value="${hall.name}">
                            </div>

                            <span class="manager-form-note">
                                Tên Hall cần dễ phân biệt trong cùng một chi nhánh.
                            </span>
                        </div>

                        <div class="rv-form-group">

                            <label class="rv-label"
                                   for="seatRows">
                                Số hàng ghế
                                <span class="required">*</span>
                            </label>

                            <div class="manager-input-prefix">
                                <i class="bi bi-layout-three-columns"></i>

                                <input id="seatRows"
                                       name="seatRows"
                                       class="rv-input"
                                       type="number"
                                       min="1"
                                       required
                                       placeholder="Ví dụ: 10"
                                       value="${hall.seatRows == 0
                                                ? ''
                                                : hall.seatRows}">
                            </div>
                        </div>

                        <div class="rv-form-group">

                            <label class="rv-label"
                                   for="seatsPerRow">
                                Số ghế mỗi hàng
                                <span class="required">*</span>
                            </label>

                            <div class="manager-input-prefix">
                                <i class="bi bi-grid-3x3-gap"></i>

                                <input id="seatsPerRow"
                                       name="seatsPerRow"
                                       class="rv-input"
                                       type="number"
                                       min="1"
                                       required
                                       placeholder="Ví dụ: 12"
                                       value="${hall.seatsPerRow == 0
                                                ? ''
                                                : hall.seatsPerRow}">
                            </div>
                        </div>

                        <div class="rv-form-group">

                            <label class="rv-label"
                                   for="totalSeatsPreview">
                                Tổng số ghế
                            </label>

                            <div class="manager-input-prefix">
                                <i class="bi bi-people"></i>

                                <input id="totalSeatsPreview"
                                       class="rv-input manager-total-seat"
                                       type="text"
                                       readonly
                                       placeholder="Tự động tính">
                            </div>

                            <span class="manager-form-note">
                                Hệ thống tự tính: số hàng ghế × số ghế mỗi hàng.
                            </span>
                        </div>

                        <div class="rv-form-group">

                            <label class="rv-label"
                                   for="hallType">
                                Loại phòng
                                <span class="required">*</span>
                            </label>

                            <select id="hallType"
                                    name="hallType"
                                    class="rv-select"
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

                            <span class="manager-form-note">
                                Loại phòng ảnh hưởng đến trải nghiệm và cấu hình vận hành của Hall.
                            </span>
                        </div>

                        <div class="rv-form-group">

                            <label class="rv-label"
                                   for="status">
                                Trạng thái
                                <span class="required">*</span>
                            </label>

                            <select id="status"
                                    name="status"
                                    class="rv-select"
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

                            <div class="manager-status-hint">
                                <i class="bi bi-info-circle-fill"></i>

                                <span>
                                    Hall ở trạng thái <strong>ACTIVE</strong> mới có thể được chọn khi phân bổ phim hoặc tạo lịch chiếu.
                                </span>
                            </div>
                        </div>

                    </div>

                    <div class="manager-form-actions">

                        <a class="rv-btn rv-btn--ghost"
                           href="${ctx}/manager/halls">

                            <i class="bi bi-x-lg"></i>
                            Hủy
                        </a>

                        <button class="rv-btn rv-btn--primary"
                                type="submit">

                            <i class="bi ${isEdit ? 'bi-check-lg' : 'bi-plus-lg'}"></i>

                            ${isEdit
                              ? 'Lưu thay đổi'
                              : 'Tạo phòng chiếu'}
                        </button>
                    </div>
                </form>

            </div>
        </div>

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
