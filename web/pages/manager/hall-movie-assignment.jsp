<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<%@ page contentType="text/html; charset=UTF-8" pageEncoding="UTF-8" %>

<c:set var="ctx" value="${pageContext.request.contextPath}" />
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

    <title>Phân bổ phim cho phòng chiếu - Rạp Việt CMS</title>

    <%-- Dùng CSS giao diện CMS/Admin hiện có, không chỉnh sửa CSS gốc. --%>
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

        .manager-hall-assignment-page .manager-topbar-context {
            display: inline-flex;
            align-items: center;
            gap: var(--s-2);
            padding-right: var(--s-4);
            border-right: 1px solid var(--border);
            color: var(--n-500);
            font-size: var(--text-sm);
        }

        .manager-hall-assignment-page .manager-topbar-context i {
            color: var(--primary-light);
        }

        .manager-hall-assignment-page .manager-sidebar-info {
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

        .manager-hall-assignment-page .manager-sidebar-info__label {
            color: rgba(255, 255, 255, 0.42);
            font-size: 10px;
            font-weight: 700;
            letter-spacing: 0.08em;
        }

        .manager-hall-assignment-page .manager-sidebar-info strong {
            color: #ffffff;
            font-size: var(--text-base);
        }

        .manager-hall-assignment-page .manager-info-banner {
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

        .manager-hall-assignment-page .manager-info-banner i {
            margin-top: 2px;
            font-size: var(--text-md);
        }

        .manager-hall-assignment-page .manager-info-banner strong {
            color: #1E3A8A;
        }

        .manager-hall-assignment-page .manager-info-banner span {
            display: block;
            margin-top: 3px;
            font-size: var(--text-sm);
        }

        .manager-hall-assignment-page .manager-filter-grid {
            display: grid;
            grid-template-columns: repeat(2, minmax(0, 1fr));
            gap: var(--s-5);
            align-items: end;
        }

        .manager-hall-assignment-page .manager-input-prefix {
            position: relative;
        }

        .manager-hall-assignment-page .manager-input-prefix i {
            position: absolute;
            top: 50%;
            left: var(--s-3);
            z-index: 1;
            color: var(--n-400);
            transform: translateY(-50%);
            pointer-events: none;
        }

        .manager-hall-assignment-page .manager-input-prefix .rv-input,
        .manager-hall-assignment-page .manager-input-prefix .rv-select {
            padding-left: 42px;
        }

        .manager-hall-assignment-page .manager-card-heading {
            display: flex;
            align-items: center;
            gap: var(--s-3);
        }

        .manager-hall-assignment-page .manager-card-heading__icon {
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

        .manager-hall-assignment-page .manager-card-heading__title {
            margin: 0;
            color: var(--n-900);
            font-size: var(--text-md);
            font-weight: 700;
            line-height: 1.35;
        }

        .manager-hall-assignment-page .manager-card-heading__subtitle {
            margin-top: 2px;
            color: var(--n-500);
            font-size: var(--text-sm);
            line-height: 1.45;
        }

        .manager-hall-assignment-page .manager-hall-information {
            display: flex;
            align-items: flex-start;
            gap: var(--s-3);
            margin-bottom: var(--s-5);
            padding: var(--s-4);
            border: 1px solid var(--border);
            border-radius: var(--r-md);
            background: var(--n-50);
            color: var(--n-600);
            font-size: var(--text-sm);
            line-height: 1.65;
        }

        .manager-hall-assignment-page .manager-hall-information i {
            margin-top: 2px;
            color: var(--primary-light);
            font-size: var(--text-md);
        }

        .manager-hall-assignment-page .manager-hall-information strong {
            color: var(--n-900);
        }

        .manager-hall-assignment-page .manager-assignment-summary {
            display: flex;
            align-items: center;
            justify-content: space-between;
            gap: var(--s-4);
            margin-bottom: var(--s-5);
            padding: var(--s-4);
            border: 1px solid var(--border);
            border-radius: var(--r-md);
            background: var(--n-50);
            flex-wrap: wrap;
        }

        .manager-hall-assignment-page .manager-assignment-note {
            max-width: 720px;
            color: var(--n-600);
            font-size: var(--text-sm);
            line-height: 1.65;
        }

        .manager-hall-assignment-page .manager-assignment-note strong {
            color: var(--n-900);
        }

        .manager-hall-assignment-page .manager-select-all-label {
            display: inline-flex;
            align-items: center;
            gap: var(--s-2);
            margin: 0;
            color: var(--n-700);
            cursor: pointer;
            font-size: var(--text-sm);
            font-weight: 600;
            white-space: nowrap;
        }

        .manager-hall-assignment-page .movie-checkbox {
            width: 18px;
            height: 18px;
            margin: 0;
            cursor: pointer;
            accent-color: var(--primary);
        }

        .manager-hall-assignment-page .manager-movie-title {
            display: block;
            color: var(--n-900);
            font-weight: 700;
            line-height: 1.45;
        }

        .manager-hall-assignment-page .manager-movie-duration {
            color: var(--n-500);
            font-size: var(--text-sm);
        }

        .manager-hall-assignment-page .manager-unassigned-text {
            color: var(--n-500);
            font-size: var(--text-sm);
        }

        .manager-hall-assignment-page .manager-assignment-footer {
            display: flex;
            justify-content: flex-end;
            gap: var(--s-3);
            margin-top: var(--s-6);
            padding-top: var(--s-5);
            border-top: 1px solid var(--border);
        }

        .manager-hall-assignment-page .manager-empty-state {
            display: flex;
            min-height: 240px;
            align-items: center;
            justify-content: center;
            padding: var(--s-8);
            text-align: center;
        }

        .manager-hall-assignment-page .manager-empty-state__content {
            max-width: 470px;
        }

        .manager-hall-assignment-page .manager-empty-state__icon {
            display: inline-flex;
            width: 56px;
            height: 56px;
            align-items: center;
            justify-content: center;
            margin-bottom: var(--s-4);
            border-radius: var(--r-lg);
            background: var(--n-100);
            color: var(--n-400);
            font-size: 25px;
        }

        .manager-hall-assignment-page .manager-empty-state h2 {
            margin: 0 0 var(--s-2);
            color: var(--n-800);
            font-size: var(--text-md);
            font-weight: 700;
        }

        .manager-hall-assignment-page .manager-empty-state p {
            margin: 0;
            color: var(--n-500);
            font-size: var(--text-base);
            line-height: 1.65;
        }

        .manager-hall-assignment-page .manager-alert {
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

        .manager-hall-assignment-page .manager-alert i {
            margin-top: 2px;
            font-size: var(--text-md);
        }

        @media (max-width: 900px) {
            .manager-hall-assignment-page .manager-topbar-context {
                display: none;
            }

            .manager-hall-assignment-page .manager-filter-grid {
                grid-template-columns: 1fr;
            }
        }

        @media (max-width: 680px) {
            .manager-hall-assignment-page .manager-assignment-summary {
                align-items: flex-start;
                flex-direction: column;
            }

            .manager-hall-assignment-page .manager-assignment-footer {
                flex-direction: column-reverse;
                align-items: stretch;
            }

            .manager-hall-assignment-page .manager-assignment-footer .rv-btn {
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

<body class="manager-hall-assignment-page">

<div class="rv-toast-container">

    <c:if test="${not empty sessionScope.flashMessage}">
        <div class="rv-toast
             ${sessionScope.flashType == 'success'
               ? 'rv-toast--success'
               : 'rv-toast--error'}">

            <i class="bi
               ${sessionScope.flashType == 'success'
                 ? 'bi-check-circle-fill'
                 : 'bi-exclamation-octagon-fill'}
               rv-toast__icon"></i>

            <div class="rv-toast__content">
                <div class="rv-toast__title">
                    <c:out value="${sessionScope.flashType == 'success'
                                   ? 'Thành công'
                                   : 'Thông báo'}" />
                </div>

                <div class="rv-toast__message">
                    <c:out value="${sessionScope.flashMessage}" />
                </div>
            </div>

            <button type="button"
                    class="rv-toast__close"
                    aria-label="Đóng thông báo">
                <i class="bi bi-x"></i>
            </button>
        </div>

        <c:remove var="flashMessage"
                  scope="session" />

        <c:remove var="flashType"
                  scope="session" />
    </c:if>
</div>

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
               class="rv-nav__item">

                <i class="bi bi-door-open-fill"></i>
                Quản lý phòng chiếu
            </a>
        </div>

        <div class="rv-nav__group open">

            <div class="rv-nav__item active"
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
                   class="rv-nav__sub-item active">
                    Phim tại phòng chiếu
                </a>

                <a href="${ctx}/manager/movie-durations"
                   class="rv-nav__sub-item">
                    Thời lượng phim
                </a>
            </div>
        </div>

        <div class="rv-nav__group">
            <a href="${ctx}/manager/showtimes"
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

                    <a href="${ctx}/manager/movie-assignments/branches">
                        Phân bổ phim
                    </a>

                    <i class="bi bi-chevron-right rv-breadcrumb__sep"></i>

                    <span class="rv-breadcrumb__current">
                        Phim tại phòng chiếu
                    </span>
                </div>

                <h1 class="rv-page-title">
                    Phân bổ phim cho phòng chiếu
                </h1>

                <p class="rv-page-subtitle">
                    Chọn những phim được phép chiếu trong từng Hall thuộc chi nhánh bạn quản lý.
                </p>
            </div>

            <div class="rv-page-header__right">
                <span class="rv-badge rv-badge--manager"
                      style="padding: var(--s-2) var(--s-3);">

                    <i class="bi bi-person-workspace"></i>
                    Branch Manager
                </span>
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

        <c:choose>

            <%-- Manager chưa được Admin gán Branch. --%>
            <c:when test="${empty branch}">

                <div class="rv-card">

                    <div class="rv-card__header">
                        <span class="rv-card__title">
                            <i class="bi bi-building-x"
                               style="margin-right: 8px; color: var(--warning);"></i>
                            Chưa được phân công chi nhánh
                        </span>
                    </div>

                    <div class="rv-card__body">

                        <div class="manager-empty-state">
                            <div class="manager-empty-state__content">

                                <div class="manager-empty-state__icon">
                                    <i class="bi bi-building-exclamation"></i>
                                </div>

                                <h2>Chưa thể phân bổ phim cho Hall</h2>

                                <p>
                                    Tài khoản Manager này chưa được Admin phân công chi nhánh.
                                    Bạn chưa thể xem hoặc lưu phân bổ phim cho phòng chiếu.
                                </p>
                            </div>
                        </div>
                    </div>
                </div>
            </c:when>

            <c:otherwise>

                <div class="manager-info-banner">
                    <i class="bi bi-info-circle-fill"></i>

                    <div>
                        <strong>Quy tắc phân bổ phim</strong>

                        <span>
                            Một Movie phải được phân bổ cho chi nhánh trước,
                            sau đó mới có thể được chọn để phân bổ vào Hall.
                        </span>
                    </div>
                </div>

                <%-- Branch hiển thị, Manager chỉ được đổi Hall trong Branch này. --%>
                <div class="rv-card"
                     style="margin-bottom: var(--s-6);">

                    <div class="rv-card__header">

                        <div class="manager-card-heading">

                            <div class="manager-card-heading__icon">
                                <i class="bi bi-building-check-fill"></i>
                            </div>

                            <div>
                                <h2 class="manager-card-heading__title">
                                    Chi nhánh và phòng chiếu
                                </h2>

                                <div class="manager-card-heading__subtitle">
                                    Chọn Hall để xem và cập nhật danh sách Movie riêng của Hall đó.
                                </div>
                            </div>
                        </div>
                    </div>

                    <div class="rv-card__body">

                        <form id="assignmentFilterForm"
                              method="get"
                              action="${ctx}/manager/movie-assignments/halls"
                              class="manager-filter-grid"
                              autocomplete="off">

                            <%-- Branch chỉ hiển thị, Manager không được đổi Branch. --%>
                            <div class="rv-form-group">

                                <label class="rv-label">
                                    Chi nhánh được phân công
                                </label>

                                <div class="manager-input-prefix">
                                    <i class="bi bi-building"></i>

                                    <input type="text"
                                           class="rv-input"
                                           value="${branch.name}"
                                           readonly>
                                </div>
                            </div>

                            <%-- Manager được chọn Hall ACTIVE thuộc Branch đang quản lý. --%>
                            <div class="rv-form-group">

                                <label class="rv-label"
                                       for="hallId">
                                    Phòng chiếu
                                </label>

                                <div class="manager-input-prefix">
                                    <i class="bi bi-door-open"></i>

                                    <select id="hallId"
                                            name="hallId"
                                            class="rv-select"
                                            required
                                            autocomplete="off"
                                            onchange="changeHall()"
                                            ${empty halls ? 'disabled' : ''}>

                                        <c:choose>

                                            <c:when test="${empty halls}">
                                                <option value="">
                                                    -- Chưa có phòng chiếu --
                                                </option>
                                            </c:when>

                                            <c:otherwise>

                                                <c:forEach var="hall"
                                                           items="${halls}">

                                                    <option value="${hall.id}"
                                                        ${selectedHallId == hall.id
                                                          ? 'selected'
                                                          : ''}>

                                                        <c:out value="${hall.name}" />
                                                        -
                                                        <c:out value="${hall.hallType}" />
                                                    </option>
                                                </c:forEach>
                                            </c:otherwise>
                                        </c:choose>
                                    </select>
                                </div>
                            </div>

                            <noscript>
                                <button type="submit"
                                        class="rv-btn rv-btn--ghost">

                                    <i class="bi bi-search"></i>
                                    Xem danh sách phim
                                </button>
                            </noscript>
                        </form>
                    </div>
                </div>

                <c:choose>

                    <%-- Branch chưa có Hall. --%>
                    <c:when test="${empty halls}">

                        <div class="rv-card">

                            <div class="rv-card__header">
                                <span class="rv-card__title">
                                    <i class="bi bi-door-closed"
                                       style="margin-right: 8px; color: var(--warning);"></i>
                                    Chưa có phòng chiếu
                                </span>
                            </div>

                            <div class="rv-card__body">

                                <div class="manager-empty-state">
                                    <div class="manager-empty-state__content">

                                        <div class="manager-empty-state__icon">
                                            <i class="bi bi-door-closed"></i>
                                        </div>

                                        <h2>Chi nhánh chưa có Hall</h2>

                                        <p>
                                            Hãy tạo phòng chiếu trước khi phân bổ Movie
                                            cho từng Hall.
                                        </p>

                                        <a class="rv-btn rv-btn--primary"
                                           style="margin-top: var(--s-5);"
                                           href="${ctx}/manager/halls">

                                            <i class="bi bi-door-open-fill"></i>
                                            Quản lý phòng chiếu
                                        </a>
                                    </div>
                                </div>
                            </div>
                        </div>
                    </c:when>

                    <c:otherwise>

                        <div class="rv-card">

                            <div class="rv-card__header">

                                <div class="manager-card-heading">

                                    <div class="manager-card-heading__icon">
                                        <i class="bi bi-film"></i>
                                    </div>

                                    <div>
                                        <h2 class="manager-card-heading__title">
                                            Danh sách phim của phòng chiếu
                                        </h2>

                                        <div class="manager-card-heading__subtitle">
                                            Chọn Movie được phép sử dụng cho Hall hiện tại.
                                        </div>
                                    </div>
                                </div>
                            </div>

                            <div class="rv-card__body">

                                <div class="manager-hall-information">
                                    <i class="bi bi-info-circle-fill"></i>

                                    <div>
                                        Chi nhánh:
                                        <strong>
                                            <c:out value="${branch.name}" />
                                        </strong>

                                        <br>

                                        Phòng đang phân bổ:
                                        <strong>

                                            <c:forEach var="hall"
                                                       items="${halls}">

                                                <c:if test="${hall.id == selectedHallId}">
                                                    <c:out value="${hall.name}" />
                                                    -
                                                    <c:out value="${hall.hallType}" />
                                                </c:if>
                                            </c:forEach>
                                        </strong>
                                    </div>
                                </div>

                                <c:choose>

                                    <%-- Branch chưa có Movie để gán cho Hall. --%>
                                    <c:when test="${empty movieItems}">

                                        <div class="manager-empty-state">
                                            <div class="manager-empty-state__content">

                                                <div class="manager-empty-state__icon">
                                                    <i class="bi bi-film"></i>
                                                </div>

                                                <h2>Chưa có Movie để phân bổ</h2>

                                                <p>
                                                    Chi nhánh này chưa được phân bổ Movie nào.
                                                    Hãy phân bổ Movie cho chi nhánh trước.
                                                </p>

                                                <a class="rv-btn rv-btn--primary"
                                                   style="margin-top: var(--s-5);"
                                                   href="${ctx}/manager/movie-assignments/branches">

                                                    <i class="bi bi-building-check-fill"></i>
                                                    Phân bổ phim cho chi nhánh
                                                </a>
                                            </div>
                                        </div>
                                    </c:when>

                                    <c:otherwise>

                                        <form id="hallAssignmentForm"
                                              method="post"
                                              action="${ctx}/manager/movie-assignments/halls"
                                              autocomplete="off"
                                              onsubmit="return confirmSaveAssignment()">

                                            <%--
                                                Giữ hallId vì Manager có nhiều Hall.
                                                Không gửi branchId.
                                                Server tự lấy Branch từ Manager login.
                                            --%>
                                            <input type="hidden"
                                                   name="hallId"
                                                   value="${selectedHallId}">

                                            <div class="manager-assignment-summary">

                                                <div class="manager-assignment-note">
                                                    Các lựa chọn bên dưới chỉ áp dụng cho
                                                    <strong>phòng đang hiển thị</strong>.

                                                    <br>

                                                    Khi đổi sang Hall khác, hệ thống sẽ tải lại
                                                    dữ liệu Movie riêng của Hall đó.

                                                    <br>

                                                    Danh sách chỉ được lưu sau khi nhấn
                                                    <strong>Lưu phân bổ cho phòng này</strong>.
                                                </div>

                                                <label class="manager-select-all-label">

                                                    <input type="checkbox"
                                                           id="selectAll"
                                                           class="movie-checkbox"
                                                           autocomplete="off"
                                                           onchange="toggleAllMovies(this)">

                                                    Chọn tất cả
                                                </label>
                                            </div>

                                            <div class="rv-table-wrapper">

                                                <table class="rv-table">

                                                    <thead>
                                                        <tr>
                                                            <th style="width: 90px; text-align: center;">
                                                                Chọn
                                                            </th>

                                                            <th>
                                                                Tên phim
                                                            </th>

                                                            <th>
                                                                Thời lượng
                                                            </th>

                                                            <th>
                                                                Trạng thái phim
                                                            </th>

                                                            <th>
                                                                Phân bổ
                                                            </th>
                                                        </tr>
                                                    </thead>

                                                    <tbody>

                                                        <c:forEach var="item"
                                                                   items="${movieItems}">

                                                            <tr>

                                                                <td style="text-align: center;">

                                                                    <input type="checkbox"
                                                                           name="movieIds"
                                                                           value="${item.movieId}"
                                                                           class="movie-checkbox movie-item-checkbox"
                                                                           autocomplete="off"
                                                                           data-assigned="${item.assigned}"
                                                                           onchange="updateSelectAllState()"
                                                                           ${item.assigned
                                                                             ? 'checked'
                                                                             : ''}>
                                                                </td>

                                                                <td>
                                                                    <span class="manager-movie-title">
                                                                        <c:out value="${item.title}" />
                                                                    </span>
                                                                </td>

                                                                <td>
                                                                    <span class="manager-movie-duration">
                                                                        <i class="bi bi-clock"></i>
                                                                        <c:out value="${item.durationLabel}" />
                                                                    </span>
                                                                </td>

                                                                <td>

                                                                    <c:choose>

                                                                        <c:when test="${item.status == 'NOW_SHOWING'}">
                                                                            <span class="rv-badge rv-badge--nowshowing">
                                                                                <i class="bi bi-play-circle-fill"></i>
                                                                                <c:out value="${item.statusLabel}" />
                                                                            </span>
                                                                        </c:when>

                                                                        <c:when test="${item.status == 'COMING_SOON'}">
                                                                            <span class="rv-badge rv-badge--comingsoon">
                                                                                <i class="bi bi-calendar-event-fill"></i>
                                                                                <c:out value="${item.statusLabel}" />
                                                                            </span>
                                                                        </c:when>

                                                                        <c:otherwise>
                                                                            <span class="rv-badge rv-badge--ended">
                                                                                <i class="bi bi-stop-circle-fill"></i>
                                                                                <c:out value="${item.statusLabel}" />
                                                                            </span>
                                                                        </c:otherwise>
                                                                    </c:choose>
                                                                </td>

                                                                <td>

                                                                    <c:choose>

                                                                        <c:when test="${item.assigned}">
                                                                            <span class="rv-badge rv-badge--active">
                                                                                <i class="bi bi-check-circle-fill"></i>
                                                                                Đã phân bổ
                                                                            </span>
                                                                        </c:when>

                                                                        <c:otherwise>
                                                                            <span class="manager-unassigned-text">
                                                                                Chưa phân bổ
                                                                            </span>
                                                                        </c:otherwise>
                                                                    </c:choose>
                                                                </td>
                                                            </tr>
                                                        </c:forEach>
                                                    </tbody>
                                                </table>
                                            </div>

                                            <div class="manager-assignment-footer">

                                                <a class="rv-btn rv-btn--ghost"
                                                   href="${ctx}/manager/dashboard">

                                                    <i class="bi bi-arrow-left"></i>
                                                    Quay lại
                                                </a>

                                                <button type="submit"
                                                        class="rv-btn rv-btn--primary">

                                                    <i class="bi bi-check-lg"></i>
                                                    Lưu phân bổ cho phòng này
                                                </button>
                                            </div>
                                        </form>
                                    </c:otherwise>
                                </c:choose>
                            </div>
                        </div>
                    </c:otherwise>
                </c:choose>
            </c:otherwise>
        </c:choose>
    </main>
</div>

<script>
    const contextPath = "${ctx}";

    /*
     * Reset checkbox đang hiển thị trên giao diện trước khi đổi Hall.
     * Không thay đổi dữ liệu trong database.
     */
    function clearVisibleCheckboxes() {
        const movieCheckboxes = document.querySelectorAll(
                ".movie-item-checkbox"
        );

        movieCheckboxes.forEach(function (checkbox) {
            checkbox.checked = false;
        });

        const selectAllCheckbox
                = document.getElementById("selectAll");

        if (selectAllCheckbox) {
            selectAllCheckbox.checked = false;
            selectAllCheckbox.indeterminate = false;
        }
    }

    function changeHall() {
        clearVisibleCheckboxes();

        const hallSelect
                = document.getElementById("hallId");

        if (!hallSelect || !hallSelect.value) {
            return;
        }

        window.location.href
                = contextPath
                + "/manager/movie-assignments/halls"
                + "?hallId="
                + encodeURIComponent(hallSelect.value);
    }

    function restoreSavedCheckboxState() {
        const movieCheckboxes = document.querySelectorAll(
                ".movie-item-checkbox"
        );

        movieCheckboxes.forEach(function (checkbox) {
            checkbox.checked
                    = checkbox.dataset.assigned === "true";
        });

        updateSelectAllState();
    }

    function toggleAllMovies(selectAllCheckbox) {
        const movieCheckboxes = document.querySelectorAll(
                ".movie-item-checkbox"
        );

        movieCheckboxes.forEach(function (checkbox) {
            checkbox.checked = selectAllCheckbox.checked;
        });

        updateSelectAllState();
    }

    function updateSelectAllState() {
        const selectAllCheckbox
                = document.getElementById("selectAll");

        if (!selectAllCheckbox) {
            return;
        }

        const movieCheckboxes = Array.from(
                document.querySelectorAll(
                        ".movie-item-checkbox"
                )
        );

        if (movieCheckboxes.length === 0) {
            selectAllCheckbox.checked = false;
            selectAllCheckbox.indeterminate = false;
            return;
        }

        const checkedCount = movieCheckboxes.filter(
                function (checkbox) {
                    return checkbox.checked;
                }
        ).length;

        selectAllCheckbox.checked
                = checkedCount === movieCheckboxes.length;

        selectAllCheckbox.indeterminate
                = checkedCount > 0
                && checkedCount < movieCheckboxes.length;
    }

    function confirmSaveAssignment() {
        return confirm(
                "Bạn có chắc muốn lưu danh sách phim "
                + "riêng cho phòng chiếu này?"
        );
    }

    document.addEventListener(
            "DOMContentLoaded",
            function () {
                restoreSavedCheckboxState();
            }
    );

    window.addEventListener(
            "pageshow",
            function () {
                restoreSavedCheckboxState();
            }
    );
</script>

</body>
</html>