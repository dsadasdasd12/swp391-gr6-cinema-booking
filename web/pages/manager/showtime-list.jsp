<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<%@ page contentType="text/html; charset=UTF-8" pageEncoding="UTF-8" %>

<c:set var="ctx" value="${pageContext.request.contextPath}" />
<c:set var="topUser" value="${sessionScope.user}" />

<!DOCTYPE html>
<html lang="vi">

<head>
    <meta charset="UTF-8">
    <meta name="viewport" content="width=device-width, initial-scale=1">

    <title>Quản lý lịch chiếu - Rạp Việt CMS</title>

    <%-- Dùng design system có sẵn của Admin, không chỉnh sửa CSS gốc. --%>
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
        .manager-showtime-list-page .manager-topbar-context {
            display: inline-flex;
            align-items: center;
            gap: var(--s-2);
            padding-right: var(--s-4);
            border-right: 1px solid var(--border);
            color: var(--n-500);
            font-size: var(--text-sm);
        }

        .manager-showtime-list-page .manager-topbar-context i {
            color: var(--primary-light);
        }

        .manager-showtime-list-page .manager-sidebar-info {
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

        .manager-showtime-list-page .manager-sidebar-info__label {
            color: rgba(255, 255, 255, 0.42);
            font-size: 10px;
            font-weight: 700;
            letter-spacing: 0.08em;
        }

        .manager-showtime-list-page .manager-sidebar-info strong {
            color: #ffffff;
            font-size: var(--text-base);
        }

        .manager-showtime-list-page .manager-showtime-note {
            display: flex;
            align-items: flex-start;
            gap: var(--s-3);
            margin-bottom: var(--s-6);
            padding: var(--s-4);
            border: 1px solid #BFDBFE;
            border-radius: var(--r-md);
            background: #EFF6FF;
            color: #1E40AF;
            font-size: var(--text-base);
            line-height: 1.65;
        }

        .manager-showtime-list-page .manager-showtime-note i {
            margin-top: 2px;
            color: #2563EB;
            font-size: var(--text-md);
        }

        .manager-showtime-list-page .manager-showtime-note strong {
            color: #1E3A8A;
        }

        .manager-showtime-list-page .manager-branch-card {
            display: flex;
            align-items: center;
            justify-content: space-between;
            gap: var(--s-4);
            flex-wrap: wrap;
        }

        .manager-showtime-list-page .manager-branch-card__info {
            display: flex;
            align-items: center;
            gap: var(--s-3);
            min-width: 0;
        }

        .manager-showtime-list-page .manager-branch-card__icon {
            display: inline-flex;
            width: 42px;
            height: 42px;
            align-items: center;
            justify-content: center;
            flex-shrink: 0;
            border-radius: var(--r-md);
            background: rgba(229, 9, 20, 0.14);
            color: var(--primary-light);
            font-size: 19px;
        }

        .manager-showtime-list-page .manager-branch-card__name {
            margin: 0;
            color: var(--n-900);
            font-size: var(--text-md);
            font-weight: 700;
            line-height: 1.35;
        }

        .manager-showtime-list-page .manager-branch-card__address {
            margin-top: 3px;
            color: var(--n-500);
            font-size: var(--text-sm);
            line-height: 1.45;
        }

        .manager-showtime-list-page .manager-showtime-id {
            color: var(--n-500);
            font-size: var(--text-sm);
            font-weight: 600;
        }

        .manager-showtime-list-page .manager-movie-name,
        .manager-showtime-list-page .manager-hall-name,
        .manager-showtime-list-page .manager-branch-name {
            display: block;
            color: var(--n-900);
            font-weight: 700;
            line-height: 1.45;
        }

        .manager-showtime-list-page .manager-row-subtext {
            display: block;
            margin-top: 3px;
            color: var(--n-500);
            font-size: var(--text-sm);
            line-height: 1.45;
        }

        .manager-showtime-list-page .manager-showtime-time {
            display: block;
            color: var(--n-900);
            font-weight: 700;
            line-height: 1.45;
        }

        .manager-showtime-list-page .manager-showtime-price {
            color: var(--n-900);
            font-weight: 700;
        }

        .manager-showtime-list-page .manager-action-group {
            display: flex;
            align-items: center;
            gap: var(--s-2);
            flex-wrap: wrap;
        }

        .manager-showtime-list-page .manager-action-group form {
            display: inline-flex;
            margin: 0;
        }

        .manager-showtime-list-page .manager-cancelled-text {
            color: var(--n-500);
            font-size: var(--text-sm);
            font-style: italic;
        }

        .manager-showtime-list-page .manager-empty-state {
            display: flex;
            min-height: 250px;
            align-items: center;
            justify-content: center;
            padding: var(--s-8);
            text-align: center;
        }

        .manager-showtime-list-page .manager-empty-state__content {
            max-width: 480px;
        }

        .manager-showtime-list-page .manager-empty-state__icon {
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

        .manager-showtime-list-page .manager-empty-state h2 {
            margin: 0 0 var(--s-2);
            color: var(--n-800);
            font-size: var(--text-md);
            font-weight: 700;
        }

        .manager-showtime-list-page .manager-empty-state p {
            margin: 0;
            color: var(--n-500);
            font-size: var(--text-base);
            line-height: 1.65;
        }

        .manager-showtime-list-page .manager-alert {
            display: flex;
            align-items: flex-start;
            gap: var(--s-3);
            margin-bottom: var(--s-6);
            padding: var(--s-4);
            border-radius: var(--r-md);
            font-size: var(--text-base);
            line-height: 1.55;
        }

        .manager-showtime-list-page .manager-alert--success {
            border: 1px solid #BBF7D0;
            background: var(--success-bg);
            color: #166534;
        }

        .manager-showtime-list-page .manager-alert--error {
            border: 1px solid #FECACA;
            background: var(--danger-bg);
            color: #B91C1C;
        }

        .manager-showtime-list-page .manager-alert i {
            margin-top: 2px;
            font-size: var(--text-md);
        }

        @media (max-width: 900px) {
            .manager-showtime-list-page .manager-topbar-context {
                display: none;
            }
        }

        @media (max-width: 680px) {
            .manager-showtime-list-page .manager-branch-card {
                align-items: stretch;
            }

            .manager-showtime-list-page .manager-branch-card > .rv-badge {
                align-self: flex-start;
            }

            .manager-showtime-list-page .manager-action-group {
                min-width: 180px;
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

<body class="manager-showtime-list-page">

<%-- Flash message sau khi tạo, sửa hoặc hủy suất chiếu. --%>
<div class="rv-toast-container">

    <c:if test="${not empty sessionScope.flashMessage}">
        <div class="rv-toast
             ${sessionScope.flashType eq 'success'
               ? 'rv-toast--success'
               : 'rv-toast--error'}">

            <i class="bi
               ${sessionScope.flashType eq 'success'
                 ? 'bi-check-circle-fill'
                 : 'bi-exclamation-octagon-fill'}
               rv-toast__icon"></i>

            <div class="rv-toast__content">
                <div class="rv-toast__title">
                    <c:out value="${sessionScope.flashType eq 'success'
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
            <a href="${ctx}/manager/showtimes"
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

                    <span class="rv-breadcrumb__current">
                        Quản lý lịch chiếu
                    </span>
                </div>

                <h1 class="rv-page-title">
                    Quản lý lịch chiếu
                </h1>

                <p class="rv-page-subtitle">
                    Tạo, chỉnh sửa và hủy các suất chiếu thuộc chi nhánh bạn được phân công quản lý.
                </p>
            </div>

            <div class="rv-page-header__right">

                <c:if test="${not empty branch}">
                    <a class="rv-btn rv-btn--primary"
                       href="${ctx}/manager/showtimes/create">

                        <i class="bi bi-plus-lg"></i>
                        Tạo suất chiếu
                    </a>
                </c:if>
            </div>
        </div>

        <c:choose>

            <%-- Manager chưa được Admin phân công Branch. --%>
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

                                <h2>Chưa thể quản lý lịch chiếu</h2>

                                <p>
                                    Tài khoản Manager này chưa được Admin phân công chi nhánh.
                                    Bạn chưa thể tạo, chỉnh sửa hoặc hủy suất chiếu.
                                </p>
                            </div>
                        </div>
                    </div>
                </div>
            </c:when>

            <%-- Manager được gán một Branch. --%>
            <c:otherwise>

                <div class="manager-showtime-note">
                    <i class="bi bi-info-circle-fill"></i>

                    <div>
                        <strong>Quy tắc tạo suất chiếu:</strong>

                        Hall phải thuộc chi nhánh của bạn, đang hoạt động
                        và Movie phải được phân bổ cho Hall đó trước khi tạo lịch chiếu.

                        Hệ thống cũng tự kiểm tra trùng thời gian trong cùng một Hall.
                    </div>
                </div>

                <div class="rv-card"
                     style="margin-bottom: var(--s-6);">

                    <div class="rv-card__header">

                        <div class="manager-branch-card">

                            <div class="manager-branch-card__info">

                                <div class="manager-branch-card__icon">
                                    <i class="bi bi-building-check-fill"></i>
                                </div>

                                <div>
                                    <h2 class="manager-branch-card__name">
                                        <c:out value="${branch.name}" />
                                    </h2>

                                    <div class="manager-branch-card__address">
                                        <i class="bi bi-geo-alt-fill"></i>
                                        <c:out value="${branch.address}" />
                                    </div>
                                </div>
                            </div>

                            <span class="rv-badge rv-badge--manager">
                                <i class="bi bi-person-workspace"></i>
                                Chi nhánh đang quản lý
                            </span>
                        </div>
                    </div>
                </div>

                <div class="rv-card">

                    <div class="rv-card__header">

                        <span class="rv-card__title">
                            <i class="bi bi-calendar-week-fill"
                               style="margin-right: 8px; color: var(--primary-light);"></i>
                            Danh sách suất chiếu
                        </span>
                    </div>

                    <div class="rv-card__body">

                        <c:choose>

                            <c:when test="${empty showtimes}">

                                <div class="manager-empty-state">
                                    <div class="manager-empty-state__content">

                                        <div class="manager-empty-state__icon">
                                            <i class="bi bi-calendar-x"></i>
                                        </div>

                                        <h2>Chi nhánh chưa có suất chiếu</h2>

                                        <p>
                                            Hãy tạo suất chiếu đầu tiên sau khi đã phân bổ Movie
                                            cho chi nhánh và Hall phù hợp.
                                        </p>

                                        <a class="rv-btn rv-btn--primary"
                                           style="margin-top: var(--s-5);"
                                           href="${ctx}/manager/showtimes/create">

                                            <i class="bi bi-plus-lg"></i>
                                            Tạo suất chiếu đầu tiên
                                        </a>
                                    </div>
                                </div>
                            </c:when>

                            <c:otherwise>

                                <div class="rv-table-wrapper"
                                     style="overflow-x: auto;">

                                    <table class="rv-table">

                                        <thead>
                                            <tr>
                                                <th class="col-no">
                                                    ID
                                                </th>

                                                <th>
                                                    Phim
                                                </th>

                                                <th>
                                                    Chi nhánh
                                                </th>

                                                <th>
                                                    Phòng chiếu
                                                </th>

                                                <th>
                                                    Thời gian
                                                </th>

                                                <th>
                                                    Giá vé
                                                </th>

                                                <th>
                                                    Trạng thái
                                                </th>

                                                <th class="col-actions">
                                                    Thao tác
                                                </th>
                                            </tr>
                                        </thead>

                                        <tbody>

                                            <c:forEach var="s"
                                                       items="${showtimes}">

                                                <tr>

                                                    <td class="col-no">
                                                        <span class="manager-showtime-id">
                                                            #<c:out value="${s.id}" />
                                                        </span>
                                                    </td>

                                                    <td>
                                                        <span class="manager-movie-name">
                                                            <c:out value="${s.movieTitle}" />
                                                        </span>

                                                        <span class="manager-row-subtext">
                                                            <i class="bi bi-clock"></i>
                                                            <c:out value="${s.movieDurationMin}" />
                                                            phút
                                                        </span>
                                                    </td>

                                                    <td>
                                                        <span class="manager-branch-name">
                                                            <c:out value="${s.branchName}" />
                                                        </span>

                                                        <span class="manager-row-subtext">
                                                            <i class="bi bi-geo-alt"></i>
                                                            <c:out value="${s.branchAddress}" />
                                                        </span>
                                                    </td>

                                                    <td>
                                                        <span class="manager-hall-name">
                                                            <c:out value="${s.hallName}" />
                                                        </span>

                                                        <span class="manager-row-subtext">
                                                            <i class="bi bi-stars"></i>
                                                            <c:out value="${s.hallType}" />
                                                        </span>
                                                    </td>

                                                    <td>
                                                        <span class="manager-showtime-time">
                                                            <i class="bi bi-calendar-event"></i>
                                                            <c:out value="${s.showDate}" />
                                                        </span>

                                                        <span class="manager-row-subtext">
                                                            <i class="bi bi-clock-history"></i>
                                                            <c:out value="${s.startHour}" />
                                                            -
                                                            <c:out value="${s.endHour}" />
                                                        </span>
                                                    </td>

                                                    <td>
                                                        <span class="manager-showtime-price">
                                                            <c:out value="${s.basePrice}" />
                                                        </span>
                                                    </td>

                                                    <td>

                                                        <c:choose>

                                                            <c:when test="${s.status eq 'CANCELLED'}">
                                                                <span class="rv-badge rv-badge--inactive">
                                                                    <i class="bi bi-x-circle-fill"></i>
                                                                    <c:out value="${s.status}" />
                                                                </span>
                                                            </c:when>

                                                            <c:when test="${s.status eq 'ON_SALE'}">
                                                                <span class="rv-badge rv-badge--active">
                                                                    <i class="bi bi-check-circle-fill"></i>
                                                                    <c:out value="${s.status}" />
                                                                </span>
                                                            </c:when>

                                                            <c:otherwise>
                                                                <span class="rv-badge rv-badge--pending">
                                                                    <i class="bi bi-clock-fill"></i>
                                                                    <c:out value="${s.status}" />
                                                                </span>
                                                            </c:otherwise>
                                                        </c:choose>
                                                    </td>

                                                    <td class="col-actions">

                                                        <div class="manager-action-group">

                                                            <c:if test="${s.status ne 'CANCELLED'}">

                                                                <a class="rv-btn rv-btn--ghost rv-btn--sm"
                                                                   href="${ctx}/manager/showtimes/edit?id=${s.id}">

                                                                    <i class="bi bi-pencil-square"></i>
                                                                    Sửa
                                                                </a>

                                                                <form method="post"
                                                                      action="${ctx}/manager/showtimes/cancel"
                                                                      onsubmit="return confirm('Bạn có chắc muốn hủy suất chiếu này?');">

                                                                    <input type="hidden"
                                                                           name="id"
                                                                           value="${s.id}">

                                                                    <button type="submit"
                                                                            class="rv-btn rv-btn--danger rv-btn--sm">

                                                                        <i class="bi bi-x-circle-fill"></i>
                                                                        Hủy
                                                                    </button>
                                                                </form>
                                                            </c:if>

                                                            <c:if test="${s.status eq 'CANCELLED'}">
                                                                <span class="manager-cancelled-text">
                                                                    Đã hủy
                                                                </span>
                                                            </c:if>
                                                        </div>
                                                    </td>
                                                </tr>
                                            </c:forEach>
                                        </tbody>
                                    </table>
                                </div>
                            </c:otherwise>
                        </c:choose>
                    </div>
                </div>
            </c:otherwise>
        </c:choose>
    </main>
</div>

</body>
</html>