<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<%@ page contentType="text/html; charset=UTF-8" pageEncoding="UTF-8" %>

<c:set var="ctx" value="${pageContext.request.contextPath}" />
<c:set var="isEdit" value="${formMode == 'edit'}" />
<c:set var="topUser" value="${sessionScope.user}" />

<!DOCTYPE html>
<html lang="vi">

<head>
    <meta charset="UTF-8">

    <meta http-equiv="Cache-Control"
          content="no-cache, no-store, must-revalidate">

    <meta http-equiv="Pragma"
          content="no-cache">

    <meta http-equiv="Expires"
          content="0">

    <meta name="viewport"
          content="width=device-width, initial-scale=1">

    <title>
        ${isEdit ? 'Cập nhật suất chiếu' : 'Tạo suất chiếu'}
        - Rạp Việt CMS
    </title>

    <%-- Dùng design system giao diện Admin hiện có, không chỉnh sửa file CSS gốc. --%>
    <link href="https://cdn.jsdelivr.net/npm/bootstrap@5.3.3/dist/css/bootstrap.min.css"
          rel="stylesheet"
          integrity="sha384-QWTKZyjpPEjISv5WaRU9OFeRpok6YctnYmDr5pNlyT2bRjXh0JMhjY6hW+ALEwIH"
          crossorigin="anonymous">

    <link href="https://cdn.jsdelivr.net/npm/bootstrap-icons@1.11.3/font/bootstrap-icons.css"
          rel="stylesheet">

    <link href="https://fonts.googleapis.com/css2?family=Inter:wght@400;500;600;700&family=Outfit:wght@600;700;800&display=swap"
          rel="stylesheet">

    <link rel="stylesheet"
          href="${ctx}/assets/css/admin/variables.css?v=redblack">

    <link rel="stylesheet"
          href="${ctx}/assets/css/admin/base.css?v=redblack">

    <link rel="stylesheet"
          href="${ctx}/assets/css/admin/layout.css?v=redblack">

    <link rel="stylesheet"
          href="${ctx}/assets/css/admin/components.css?v=redblack">

    <link rel="stylesheet"
          href="${ctx}/assets/css/admin/tables.css?v=redblack">

    <link rel="stylesheet"
          href="${ctx}/assets/css/admin/forms.css?v=redblack">

    <style>
        /*
         * CSS chỉ áp dụng riêng cho showtime-form.jsp.
         * Không ảnh hưởng Admin, Customer hay các trang Manager khác.
         */

        .manager-showtime-form-page .manager-topbar-context {
            display: inline-flex;
            align-items: center;
            gap: var(--s-2);
            padding-right: var(--s-4);
            border-right: 1px solid var(--border);
            color: var(--n-500);
            font-size: var(--text-sm);
        }

        .manager-showtime-form-page .manager-topbar-context i {
            color: var(--primary-light);
        }

        .manager-showtime-form-page .manager-sidebar-info {
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

        .manager-showtime-form-page .manager-sidebar-info__label {
            color: rgba(255, 255, 255, 0.42);
            font-size: 10px;
            font-weight: 700;
            letter-spacing: 0.08em;
        }

        .manager-showtime-form-page .manager-sidebar-info strong {
            color: #ffffff;
            font-size: var(--text-base);
        }

        .manager-showtime-form-page .manager-showtime-info {
            display: flex;
            align-items: flex-start;
            gap: var(--s-3);
            margin-bottom: var(--s-6);
            padding: var(--s-4);
            border: 1px solid #bfdbfe;
            border-radius: var(--r-md);
            background: #eff6ff;
            color: #1e40af;
            font-size: var(--text-base);
            line-height: 1.65;
        }

        .manager-showtime-form-page .manager-showtime-info i {
            margin-top: 2px;
            color: #2563eb;
            font-size: var(--text-md);
        }

        .manager-showtime-form-page .manager-showtime-info strong {
            color: #1e3a8a;
        }

        .manager-showtime-form-page .manager-form-card-heading {
            display: flex;
            align-items: center;
            gap: var(--s-3);
        }

        .manager-showtime-form-page .manager-form-card-heading__icon {
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

        .manager-showtime-form-page .manager-form-card-heading__title {
            margin: 0;
            color: var(--n-900);
            font-size: var(--text-md);
            font-weight: 700;
            line-height: 1.4;
        }

        .manager-showtime-form-page .manager-form-card-heading__description {
            margin-top: 3px;
            color: var(--n-500);
            font-size: var(--text-sm);
            line-height: 1.45;
        }

        .manager-showtime-form-page .field-note {
            display: block;
            margin-top: var(--s-2);
            color: var(--n-500);
            font-size: var(--text-sm);
            line-height: 1.55;
        }

        .manager-showtime-form-page .manager-input-icon {
            position: relative;
        }

        .manager-showtime-form-page .manager-input-icon i {
            position: absolute;
            top: 50%;
            left: var(--s-3);
            z-index: 1;
            color: var(--n-400);
            transform: translateY(-50%);
            pointer-events: none;
        }

        .manager-showtime-form-page .manager-input-icon .rv-input,
        .manager-showtime-form-page .manager-input-icon .rv-select {
            padding-left: 42px;
        }

        .manager-showtime-form-page .manager-input-icon--top i {
            top: 23px;
        }

        .manager-showtime-form-page .manager-readonly-input {
            background: var(--n-50);
            color: var(--n-600);
        }

        .manager-showtime-form-page .schedule-information {
            display: grid;
            grid-template-columns: repeat(3, minmax(0, 1fr));
            gap: var(--s-4);
            margin-top: var(--s-6);
            padding: var(--s-5);
            border: 1px solid var(--border);
            border-radius: var(--r-lg);
            background: var(--n-50);
        }

        .manager-showtime-form-page .schedule-information h3 {
            grid-column: 1 / -1;
            display: flex;
            align-items: center;
            gap: var(--s-2);
            margin: 0;
            color: var(--n-900);
            font-size: var(--text-md);
            font-weight: 700;
        }

        .manager-showtime-form-page .schedule-information h3 i {
            color: var(--primary-light);
        }

        .manager-showtime-form-page .schedule-information p {
            margin: 0;
            padding: var(--s-4);
            border: 1px solid var(--border);
            border-radius: var(--r-md);
            background: var(--surface);
            color: var(--n-600);
            font-size: var(--text-sm);
            line-height: 1.55;
        }

        .manager-showtime-form-page .schedule-information strong {
            display: block;
            margin-top: var(--s-2);
            color: var(--n-900);
            font-size: var(--text-base);
        }

        .manager-showtime-form-page .movie-assignment-warning {
            display: none;
            margin-top: var(--s-3);
            padding: var(--s-3) var(--s-4);
            border: 1px solid #fed7aa;
            border-radius: var(--r-md);
            background: var(--warning-bg);
            color: #9a3412;
            font-size: var(--text-sm);
            line-height: 1.55;
        }

        .manager-showtime-form-page .movie-assignment-warning a {
            color: #7c2d12;
            font-weight: 700;
            text-decoration: underline;
        }

        .manager-showtime-form-page .movie-autocomplete {
            position: relative;
            width: 100%;
        }

        .manager-showtime-form-page .movie-suggestion-list {
            display: none;
            position: absolute;
            top: calc(100% + 6px);
            right: 0;
            left: 0;
            z-index: 1000;
            max-height: 260px;
            overflow-y: auto;
            padding: var(--s-2);
            border: 1px solid var(--border);
            border-radius: var(--r-lg);
            background: var(--surface);
            box-shadow: var(--shadow-lg);
        }

        .manager-showtime-form-page .movie-suggestion-item {
            display: block;
            width: 100%;
            padding: var(--s-3);
            border: 0;
            border-radius: var(--r-md);
            background: transparent;
            color: var(--n-800);
            text-align: left;
            cursor: pointer;
        }

        .manager-showtime-form-page .movie-suggestion-item:hover,
        .manager-showtime-form-page .movie-suggestion-item.active {
            background: rgba(229, 9, 20, 0.1);
        }

        .manager-showtime-form-page .movie-suggestion-title {
            display: block;
            color: var(--n-900);
            font-size: var(--text-base);
            font-weight: 700;
            line-height: 1.4;
        }

        .manager-showtime-form-page .movie-suggestion-duration {
            display: block;
            margin-top: 4px;
            color: var(--n-500);
            font-size: var(--text-sm);
        }

        .manager-showtime-form-page .movie-suggestion-empty {
            padding: var(--s-3);
            color: var(--n-500);
            font-size: var(--text-sm);
        }

        .manager-showtime-form-page .manager-form-actions {
            display: flex;
            justify-content: flex-end;
            gap: var(--s-3);
            margin-top: var(--s-6);
            padding-top: var(--s-5);
            border-top: 1px solid var(--border);
        }

        .manager-showtime-form-page .manager-alert {
            display: flex;
            align-items: flex-start;
            gap: var(--s-3);
            margin-bottom: var(--s-6);
            padding: var(--s-4);
            border: 1px solid #fecaca;
            border-radius: var(--r-md);
            background: var(--danger-bg);
            color: #b91c1c;
            font-size: var(--text-base);
            line-height: 1.55;
        }

        .manager-showtime-form-page .manager-alert i {
            margin-top: 2px;
            font-size: var(--text-md);
        }

        @media (max-width: 1024px) {
            .manager-showtime-form-page .schedule-information {
                grid-template-columns: 1fr;
            }
        }

        @media (max-width: 900px) {
            .manager-showtime-form-page .manager-topbar-context {
                display: none;
            }

            .manager-showtime-form-page .rv-form-container {
                grid-template-columns: 1fr;
            }
        }

        @media (max-width: 680px) {
            .manager-showtime-form-page .manager-form-actions {
                flex-direction: column-reverse;
                align-items: stretch;
            }

            .manager-showtime-form-page .manager-form-actions .rv-btn {
                width: 100%;
            }
        }
    </style>

    <script src="${ctx}/assets/js/main.js"
            charset="UTF-8"
            defer></script>

    <script src="${ctx}/assets/js/confirm.js"
            charset="UTF-8"
            defer></script>

    <script src="https://cdn.jsdelivr.net/npm/bootstrap@5.3.3/dist/js/bootstrap.bundle.min.js"
            integrity="sha384-YvpcrYf0tY3lHB60NNkmXc5s9fDVZLESaAA55NDzOxhy9GkcIdslK1eN7N6jIeHz"
            crossorigin="anonymous"
            defer></script>
</head>

<body class="manager-showtime-form-page">

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
                <c:choose>
                    <c:when test="${not empty topUser and not empty topUser.fullName}">
                        <c:out value="${topUser.fullName.substring(0, 1).toUpperCase()}" />
                    </c:when>

                    <c:otherwise>M</c:otherwise>
                </c:choose>
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
               class="rv-nav__item">

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
               class="rv-nav__item active">

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

                    <a href="${ctx}/manager/showtimesmanagement">
                        Quản lý lịch chiếu
                    </a>

                    <i class="bi bi-chevron-right rv-breadcrumb__sep"></i>

                    <span class="rv-breadcrumb__current">
                        ${isEdit ? 'Cập nhật suất chiếu' : 'Tạo suất chiếu'}
                    </span>
                </div>

                <h1 class="rv-page-title">
                    ${isEdit
                      ? 'Cập nhật suất chiếu'
                      : 'Tạo suất chiếu mới'}
                </h1>

                <p class="rv-page-subtitle">
                    Chọn Hall, Movie và thời gian bắt đầu trong chi nhánh được phân công.
                </p>
            </div>

            <div class="rv-page-header__right">
                <a class="rv-btn rv-btn--ghost"
                   href="${ctx}/manager/showtimesmanagement">

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

        <div class="manager-showtime-info">
            <i class="bi bi-info-circle-fill"></i>

            <div>
                <strong>Nguyên tắc lập lịch:</strong>

                Hệ thống tự tính giờ kết thúc từ thời lượng Movie và tự từ chối
                nếu Hall đã có suất chiếu bị chồng thời gian.
            </div>
        </div>

        <div class="rv-card">

            <div class="rv-card__header">

                <div class="manager-form-card-heading">

                    <div class="manager-form-card-heading__icon">
                        <i class="bi ${isEdit
                                       ? 'bi-pencil-square'
                                       : 'bi-calendar-plus-fill'}"></i>
                    </div>

                    <div>
                        <h2 class="manager-form-card-heading__title">
                            ${isEdit
                              ? 'Thông tin suất chiếu cần cập nhật'
                              : 'Thông tin suất chiếu mới'}
                        </h2>

                        <div class="manager-form-card-heading__description">
                            Các trường có dấu * là bắt buộc.
                        </div>
                    </div>
                </div>
            </div>

            <div class="rv-card__body">

                <form id="showtimeForm"
                      method="post"
                      action="${ctx}${isEdit
                              ? '/manager/showtimesmanagement/edit'
                              : '/manager/showtimesmanagement/create'}">

                    <c:if test="${isEdit}">
                        <input type="hidden"
                               name="id"
                               value="${showtime.id}">
                    </c:if>

                    <div class="rv-form-container">

                        <div class="rv-form-group">

                            <label class="rv-label">
                                Chi nhánh được phân công
                            </label>

                            <div class="manager-input-icon">
                                <i class="bi bi-building"></i>

                                <input type="text"
                                       class="rv-input manager-readonly-input"
                                       value="${branch.name}"
                                       readonly>
                            </div>

                            <span class="field-note">
                                Manager chỉ có thể tạo suất chiếu cho Hall thuộc chi nhánh này.
                            </span>
                        </div>

                        <div class="rv-form-group">

                            <label class="rv-label"
                                   for="hallId">
                                Phòng chiếu
                                <span class="required">*</span>
                            </label>

                            <div class="manager-input-icon">
                                <i class="bi bi-door-open"></i>

                                <select id="hallId"
                                        name="hallId"
                                        class="rv-select"
                                        required>

                                    <option value="">
                                        -- Chọn phòng chiếu --
                                    </option>

                                    <c:forEach var="hall"
                                               items="${halls}">

                                        <option value="${hall.id}"
                                            ${showtime.hallId == hall.id
                                              ? 'selected'
                                              : ''}>

                                            <c:out value="${hall.name}" />
                                            -
                                            <c:out value="${hall.hallType}" />
                                        </option>
                                    </c:forEach>
                                </select>
                            </div>

                            <span class="field-note">
                                Chỉ hiển thị Hall thuộc Branch được Admin phân công cho Manager.
                            </span>
                        </div>

                        <div class="rv-form-group">

                            <label class="rv-label"
                                   for="movieTitleInput">
                                Phim
                                <span class="required">*</span>
                            </label>

                            <div id="movieAutocomplete"
                                 class="movie-autocomplete">

                                <div class="manager-input-icon">
                                    <i class="bi bi-film"></i>

                                    <input id="movieTitleInput"
                                           class="rv-input"
                                           type="text"
                                           placeholder="Nhập hoặc chọn phim..."
                                           autocomplete="off"
                                           required
                                           disabled
                                           role="combobox"
                                           aria-autocomplete="list"
                                           aria-expanded="false"
                                           aria-controls="movieSuggestionList">
                                </div>

                                <div id="movieSuggestionList"
                                     class="movie-suggestion-list"
                                     role="listbox">
                                </div>
                            </div>

                            <input type="hidden"
                                   id="movieId"
                                   name="movieId"
                                   value="">

                            <span class="field-note"
                                  id="movieNote">
                                Chọn Hall trước. Sau đó bấm vào ô Phim để xem gợi ý hoặc nhập tên phim để tìm.
                            </span>

                            <div id="movieAssignmentWarning"
                                 class="movie-assignment-warning">

                                Hall này chưa được phân bổ Movie.

                                <br>

                                <a id="assignmentLink"
                                   href="${ctx}/manager/movie-assignments/halls">
                                    Đi đến phân bổ phim cho phòng
                                </a>
                            </div>
                        </div>

                        <div class="rv-form-group">

                            <label class="rv-label"
                                   for="startTime">
                                Thời gian bắt đầu
                                <span class="required">*</span>
                            </label>

                            <div class="manager-input-icon">
                                <i class="bi bi-calendar-event"></i>

                                <input id="startTime"
                                       name="startTime"
                                       class="rv-input"
                                       type="datetime-local"
                                       required
                                       value="${showtime.startInputValue}">
                            </div>

                            <span class="field-note">
                                Giờ kết thúc được tự động tính theo thời lượng Movie đã chọn.
                            </span>
                        </div>

                        <div class="rv-form-group">

                            <label class="rv-label"
                                   for="basePrice">
                                Giá vé cơ bản
                                <span class="required">*</span>
                            </label>

                            <div class="manager-input-icon">
                                <i class="bi bi-cash-stack"></i>

                                <input id="basePrice"
                                       name="basePrice"
                                       class="rv-input"
                                       type="number"
                                       min="0"
                                       step="1000"
                                       required
                                       value="${showtime.basePrice}">
                            </div>

                            <span class="field-note">
                                Đơn vị: VNĐ.
                            </span>
                        </div>

                        <div class="rv-form-group">

                            <label class="rv-label"
                                   for="status">
                                Trạng thái
                                <span class="required">*</span>
                            </label>

                            <div class="manager-input-icon">
                                <i class="bi bi-toggle-on"></i>

                                <select id="status"
                                        name="status"
                                        class="rv-select"
                                        required>

                                    <option value="SCHEDULED"
                                        ${showtime.status == 'SCHEDULED'
                                          ? 'selected'
                                          : ''}>

                                        SCHEDULED - Đã lên lịch
                                    </option>

                                    <option value="ON_SALE"
                                        ${showtime.status == 'ON_SALE'
                                          ? 'selected'
                                          : ''}>

                                        ON_SALE - Đang bán vé
                                    </option>
                                </select>
                            </div>

                            <span class="field-note">
                                Chỉ suất chiếu đang hoạt động mới có thể được bán vé theo quy tắc hệ thống.
                            </span>
                        </div>
                    </div>

                    <div class="schedule-information">

                        <h3>
                            <i class="bi bi-clock-history"></i>
                            Thông tin thời gian suất chiếu
                        </h3>

                        <p>
                            Thời lượng Movie:

                            <strong id="durationPreview">
                                Chưa chọn phim
                            </strong>
                        </p>

                        <p>
                            Thời gian kết thúc dự kiến:

                            <strong id="endTimePreview">
                                Chưa xác định
                            </strong>
                        </p>

                        <p>
                            Kiểm tra lịch:

                            <strong>
                                Hệ thống từ chối lưu nếu Hall đã có suất chiếu bị chồng thời gian.
                            </strong>
                        </p>
                    </div>

                    <div class="manager-form-actions">

                        <a class="rv-btn rv-btn--ghost"
                           href="${ctx}/manager/showtimesmanagement">

                            <i class="bi bi-x-lg"></i>
                            Hủy
                        </a>

                        <button class="rv-btn rv-btn--primary"
                                type="submit">

                            <i class="bi ${isEdit
                                           ? 'bi-check-lg'
                                           : 'bi-plus-lg'}"></i>

                            ${isEdit ? 'Cập nhật suất chiếu' : 'Lưu suất chiếu'}
                        </button>
                    </div>
                </form>

                <select id="movieOptionSource"
                        style="display: none;"
                        aria-hidden="true">

                    <c:forEach var="entry"
                               items="${moviesByHall}">

                        <c:forEach var="movie"
                                   items="${entry.value}">

                            <option value="${movie.id}"
                                    data-hall-id="${entry.key}"
                                    data-duration="${movie.durationMin}"
                                    data-title="<c:out value='${movie.title}' />">

                                <c:out value="${movie.title}" />
                                - ${movie.durationMin} phút
                            </option>
                        </c:forEach>
                    </c:forEach>
                </select>
            </div>
        </div>
    </main>
</div>

<script>
    const contextPath = "${ctx}";
    const initialMovieId = "${showtime.movieId}";

    const showtimeForm
            = document.getElementById("showtimeForm");

    const hallSelect
            = document.getElementById("hallId");

    const movieAutocomplete
            = document.getElementById("movieAutocomplete");

    const movieTitleInput
            = document.getElementById("movieTitleInput");

    const movieSuggestionList
            = document.getElementById("movieSuggestionList");

    const movieIdInput
            = document.getElementById("movieId");

    const movieOptionSource
            = document.getElementById("movieOptionSource");

    const startTimeInput
            = document.getElementById("startTime");

    const durationPreview
            = document.getElementById("durationPreview");

    const endTimePreview
            = document.getElementById("endTimePreview");

    const movieNote
            = document.getElementById("movieNote");

    const warningBox
            = document.getElementById("movieAssignmentWarning");

    const assignmentLink
            = document.getElementById("assignmentLink");

    let currentHallMovies = [];
    let filteredMovies = [];
    let activeSuggestionIndex = -1;

    function normalizeSearchText(value) {
        return (value || "")
                .toLowerCase()
                .normalize("NFD")
                .replace(/[\u0300-\u036f]/g, "")
                .replace(/đ/g, "d")
                .replace(/[^a-z0-9]+/g, " ")
                .trim()
                .replace(/\s+/g, " ");
    }

    function getMoviesOfSelectedHall() {
        const hallId = hallSelect.value;

        return Array.from(movieOptionSource.options)
                .filter(function (option) {
                    return option.dataset.hallId === hallId;
                })
                .map(function (option) {
                    return {
                        id: option.value,
                        title: (option.dataset.title || "").trim(),
                        duration: parseInt(option.dataset.duration, 10) || 0
                    };
                });
    }

    function isMovieMatched(movie, query) {
        const normalizedQuery = normalizeSearchText(query);
        const normalizedTitle = normalizeSearchText(movie.title);

        if (!normalizedQuery) {
            return true;
        }

        if (normalizedTitle.includes(normalizedQuery)) {
            return true;
        }

        const searchWords = normalizedQuery.split(" ");

        return searchWords.every(function (word) {
            return normalizedTitle.includes(word);
        });
    }

    function hideMovieSuggestions() {
        movieSuggestionList.style.display = "none";
        movieTitleInput.setAttribute("aria-expanded", "false");
        activeSuggestionIndex = -1;
    }

    function setActiveSuggestion(index) {
        const suggestionItems = movieSuggestionList.querySelectorAll(
                ".movie-suggestion-item"
        );

        if (suggestionItems.length === 0) {
            activeSuggestionIndex = -1;
            return;
        }

        if (index < 0) {
            index = suggestionItems.length - 1;
        }

        if (index >= suggestionItems.length) {
            index = 0;
        }

        activeSuggestionIndex = index;

        suggestionItems.forEach(function (item, itemIndex) {
            const isActive = itemIndex === activeSuggestionIndex;

            item.classList.toggle("active", isActive);
            item.setAttribute("aria-selected", isActive ? "true" : "false");

            if (isActive) {
                item.scrollIntoView({
                    block: "nearest"
                });
            }
        });
    }

    function renderMovieSuggestions() {
        const query = movieTitleInput.value;

        filteredMovies = currentHallMovies.filter(function (movie) {
            return isMovieMatched(movie, query);
        });

        movieSuggestionList.innerHTML = "";
        activeSuggestionIndex = -1;

        if (filteredMovies.length === 0) {
            const emptyMessage = document.createElement("div");

            emptyMessage.className = "movie-suggestion-empty";
            emptyMessage.textContent = "Không tìm thấy phim phù hợp.";

            movieSuggestionList.appendChild(emptyMessage);
            return;
        }

        filteredMovies.forEach(function (movie, index) {
            const item = document.createElement("button");
            const title = document.createElement("span");
            const duration = document.createElement("span");

            item.type = "button";
            item.className = "movie-suggestion-item";
            item.dataset.index = index;
            item.setAttribute("role", "option");
            item.setAttribute("aria-selected", "false");

            title.className = "movie-suggestion-title";
            title.textContent = movie.title;

            duration.className = "movie-suggestion-duration";
            duration.textContent = movie.duration + " phút";

            item.appendChild(title);
            item.appendChild(duration);

            item.addEventListener("mousedown", function (event) {
                event.preventDefault();
                chooseMovie(movie);
            });

            movieSuggestionList.appendChild(item);
        });
    }

    function showMovieSuggestions() {
        if (movieTitleInput.disabled || currentHallMovies.length === 0) {
            return;
        }

        renderMovieSuggestions();

        movieSuggestionList.style.display = "block";
        movieTitleInput.setAttribute("aria-expanded", "true");
    }

    function chooseMovie(movie) {
        movieTitleInput.value = movie.title;
        movieIdInput.value = movie.id;

        movieTitleInput.setCustomValidity("");

        hideMovieSuggestions();
        updateSchedulePreview();
    }

    function getSelectedMovie() {
        const selectedMovieId = movieIdInput.value;

        return currentHallMovies.find(function (movie) {
            return movie.id === selectedMovieId;
        });
    }

    function handleMovieInput() {
        const typedValue = movieTitleInput.value;
        const normalizedTypedValue = normalizeSearchText(typedValue);

        if (!normalizedTypedValue) {
            movieIdInput.value = "";
            movieTitleInput.setCustomValidity("");
            updateSchedulePreview();
            showMovieSuggestions();
            return;
        }

        const exactMovie = currentHallMovies.find(function (movie) {
            return normalizeSearchText(movie.title)
                    === normalizedTypedValue;
        });

        if (exactMovie) {
            movieIdInput.value = exactMovie.id;
        } else {
            movieIdInput.value = "";
        }

        movieTitleInput.setCustomValidity("");

        updateSchedulePreview();
        showMovieSuggestions();
    }

    function loadMoviesByHall(keepInitialMovie) {
        const hallId = hallSelect.value;

        currentHallMovies = [];
        filteredMovies = [];

        movieTitleInput.value = "";
        movieIdInput.value = "";
        movieTitleInput.setCustomValidity("");

        hideMovieSuggestions();

        if (!hallId) {
            movieTitleInput.disabled = true;
            warningBox.style.display = "none";

            movieNote.textContent
                    = "Chọn phòng chiếu trước. Sau đó bấm vào ô Phim "
                    + "để xem gợi ý hoặc nhập tên phim để tìm.";

            updateSchedulePreview();
            return;
        }

        currentHallMovies = getMoviesOfSelectedHall();

        if (currentHallMovies.length === 0) {
            movieTitleInput.disabled = true;
            warningBox.style.display = "block";

            movieNote.textContent
                    = "Phòng này chưa có phim được phân bổ.";

            assignmentLink.href
                    = contextPath
                    + "/manager/movie-assignments/halls?hallId="
                    + encodeURIComponent(hallId);

            updateSchedulePreview();
            return;
        }

        movieTitleInput.disabled = false;
        warningBox.style.display = "none";

        movieNote.textContent
                = "Bấm vào ô để xem toàn bộ phim. "
                + "Bạn có thể gõ có dấu, không dấu hoặc một phần tên phim.";

        if (keepInitialMovie && initialMovieId) {
            const initialMovie = currentHallMovies.find(function (movie) {
                return movie.id === initialMovieId;
            });

            if (initialMovie) {
                movieTitleInput.value = initialMovie.title;
                movieIdInput.value = initialMovie.id;
            }
        }

        updateSchedulePreview();
    }

    function updateSchedulePreview() {
        const selectedMovie = getSelectedMovie();
        const duration = selectedMovie ? selectedMovie.duration : 0;

        if (!duration || duration <= 0) {
            durationPreview.textContent = "Chưa chọn phim";
            endTimePreview.textContent = "Chưa xác định";
            return;
        }

        durationPreview.textContent = duration + " phút";

        const startValue = startTimeInput.value;

        if (!startValue) {
            endTimePreview.textContent = "Chọn thời gian bắt đầu";
            return;
        }

        const startDate = parseLocalDateTime(startValue);

        if (!startDate) {
            endTimePreview.textContent = "Thời gian không hợp lệ";
            return;
        }

        startDate.setMinutes(
                startDate.getMinutes() + duration
        );

        endTimePreview.textContent = formatDateTime(startDate);
    }

    function parseLocalDateTime(value) {
        const parts = value.split("T");

        if (parts.length !== 2) {
            return null;
        }

        const dateParts = parts[0].split("-");
        const timeParts = parts[1].split(":");

        if (dateParts.length !== 3
                || timeParts.length < 2) {
            return null;
        }

        return new Date(
                parseInt(dateParts[0], 10),
                parseInt(dateParts[1], 10) - 1,
                parseInt(dateParts[2], 10),
                parseInt(timeParts[0], 10),
                parseInt(timeParts[1], 10)
        );
    }

    function formatDateTime(date) {
        const day = String(date.getDate()).padStart(2, "0");
        const month = String(date.getMonth() + 1).padStart(2, "0");
        const year = date.getFullYear();

        const hour = String(date.getHours()).padStart(2, "0");
        const minute = String(date.getMinutes()).padStart(2, "0");

        return day
                + "/" + month
                + "/" + year
                + " "
                + hour
                + ":"
                + minute;
    }

    hallSelect.addEventListener(
            "change",
            function () {
                loadMoviesByHall(false);
            }
    );

    movieTitleInput.addEventListener(
            "focus",
            function () {
                showMovieSuggestions();
            }
    );

    movieTitleInput.addEventListener(
            "click",
            function () {
                showMovieSuggestions();
            }
    );

    movieTitleInput.addEventListener(
            "input",
            function () {
                handleMovieInput();
            }
    );

    movieTitleInput.addEventListener(
            "keydown",
            function (event) {
                if (event.key === "ArrowDown") {
                    event.preventDefault();

                    if (movieSuggestionList.style.display !== "block") {
                        showMovieSuggestions();
                    }

                    setActiveSuggestion(
                            activeSuggestionIndex + 1
                    );
                }

                if (event.key === "ArrowUp") {
                    event.preventDefault();

                    if (movieSuggestionList.style.display !== "block") {
                        showMovieSuggestions();
                    }

                    setActiveSuggestion(
                            activeSuggestionIndex - 1
                    );
                }

                if (event.key === "Enter"
                        && activeSuggestionIndex >= 0
                        && filteredMovies[activeSuggestionIndex]) {

                    event.preventDefault();

                    chooseMovie(
                            filteredMovies[activeSuggestionIndex]
                    );
                }

                if (event.key === "Escape") {
                    hideMovieSuggestions();
                }
            }
    );

    movieTitleInput.addEventListener(
            "blur",
            function () {
                window.setTimeout(hideMovieSuggestions, 150);
            }
    );

    startTimeInput.addEventListener(
            "change",
            updateSchedulePreview
    );

    startTimeInput.addEventListener(
            "input",
            updateSchedulePreview
    );

    showtimeForm.addEventListener(
            "submit",
            function (event) {
                const selectedMovie = getSelectedMovie();

                if (!selectedMovie) {
                    event.preventDefault();

                    movieTitleInput.setCustomValidity(
                            "Vui lòng chọn một phim từ danh sách gợi ý."
                    );

                    movieTitleInput.reportValidity();
                    showMovieSuggestions();
                    return;
                }

                movieTitleInput.setCustomValidity("");
            }
    );

    document.addEventListener(
            "mousedown",
            function (event) {
                if (!movieAutocomplete.contains(event.target)) {
                    hideMovieSuggestions();
                }
            }
    );

    document.addEventListener(
            "DOMContentLoaded",
            function () {
                loadMoviesByHall(true);
            }
    );
</script>

</body>
</html>
