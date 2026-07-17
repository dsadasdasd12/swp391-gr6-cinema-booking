@ -1,571 +1,840 @@
<%@ page contentType="text/html; charset=UTF-8" pageEncoding="UTF-8" %>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>

<c:set var="ctx" value="${pageContext.request.contextPath}" />
<c:set var="topUser" value="${sessionScope.user}" />

<!DOCTYPE html>
<html lang="vi">
    <head>
        <meta charset="UTF-8">
        <meta name="viewport" content="width=device-width, initial-scale=1">

        <title>Bảng điều khiển chi nhánh - Rạp Việt CMS</title>

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
             * CSS chỉ nằm trong dashboard.jsp này.
             * Không ảnh hưởng Admin, Customer hoặc bất kỳ giao diện nào khác.
             */

            .manager-dashboard-link {
                min-height: 182px;
                color: var(--n-800);
                cursor: pointer;
                text-decoration: none;
                transition:
                    transform var(--ease),
                    box-shadow var(--ease),
                    border-color var(--ease);
            }

            .manager-dashboard-link:hover {
                border-color: var(--primary-light);
                box-shadow: var(--shadow-md);
                color: var(--n-800);
                transform: translateY(-3px);
            }

            .manager-dashboard-link:focus {
                color: var(--n-800);
                outline: 2px solid var(--primary-light);
                outline-offset: 3px;
            }

            .manager-dashboard-link__top {
                display: flex;
                align-items: flex-start;
                justify-content: space-between;
                gap: var(--s-3);
            }

            .manager-dashboard-link__icon {
                display: inline-flex;
                width: 42px;
                height: 42px;
                align-items: center;
                justify-content: center;
                border-radius: var(--r-md);
                background: rgba(229, 9, 20, 0.16);
                color: var(--primary-light);
                font-size: 19px;
            }

            .manager-dashboard-link__arrow {
                color: var(--primary-light);
                font-size: var(--text-md);
                transition: transform var(--ease);
            }

            .manager-dashboard-link:hover .manager-dashboard-link__arrow {
                transform: translateX(4px);
            }

            .manager-dashboard-link__description {
                min-height: 46px;
                margin: 0;
                color: var(--n-500);
                font-size: var(--text-sm);
                line-height: 1.6;
            }

            .manager-sidebar-info {
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

            .manager-sidebar-info__label {
                color: rgba(255, 255, 255, 0.42);
                font-size: 10px;
                font-weight: 700;
                letter-spacing: 0.08em;
            }

            .manager-sidebar-info strong {
                color: #ffffff;
                font-size: var(--text-base);
            }

            @media (max-width: 680px) {
                .manager-dashboard-link {
                    min-height: 164px;
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

    <body>

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

                <c:remove var="flashMessage" scope="session" />
                <c:remove var="flashType" scope="session" />
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

                <span class="d-none d-md-inline-flex align-items-center gap-2 text-muted">
                    <i class="bi bi-building"></i>
                    Phân hệ Quản lý chi nhánh
                </span>

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
                           class="rv-topbar__dropdown-item danger"
                           data-confirm
                           data-confirm-title="Đăng xuất?"
                           data-confirm-message="Bạn có chắc chắn muốn đăng xuất khỏi hệ thống?"
                           data-confirm-type="warning"
                           data-confirm-text="Đăng xuất">

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
                       class="rv-nav__item active">

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
                    <a href="${ctx}/manager/seat-config"
                       class="rv-nav__item">
                        <i class="bi bi-grid-3x3-gap-fill"></i>
                        Cấu hình ghế
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

                <div class="rv-nav__group">
                    <a href="${ctx}/manager/fnb"
                       class="rv-nav__item">

                        <i class="bi bi-cup-hot-fill"></i>
                        Quản lý kho F&amp;B
                    </a>
                </div>

                <div class="rv-nav__group">
                    <a href="${ctx}/logout"
                       class="rv-nav__item logout"
                       data-confirm
                       data-confirm-title="Đăng xuất?"
                       data-confirm-message="Bạn có chắc chắn muốn đăng xuất khỏi hệ thống?"
                       data-confirm-type="warning"
                       data-confirm-text="Đăng xuất">

                        <i class="bi bi-box-arrow-right"></i>
                        Đăng xuất
                    </a>
                </div>
            </aside>

            <main class="rv-main">

                <div class="rv-page-header">

                    <div class="rv-page-header__left">

                        <div class="rv-breadcrumb">
                            <span class="rv-breadcrumb__current">
                                Quản lý chi nhánh
                            </span>
                        </div>

                        <h1 class="rv-page-title">
                            Bảng điều khiển chi nhánh
                        </h1>

                        <p class="rv-page-subtitle">
                            Xin chào
                            <strong>
                                <c:out value="${sessionScope.user.fullName}" />
                            </strong>
                            — chọn chức năng quản lý để bắt đầu vận hành chi nhánh.
                        </p>
                    </div>

                    <div class="rv-page-header__right">
                        <span class="rv-badge rv-badge--manager"
                              style="padding: var(--s-2) var(--s-3); font-weight: 600;">
                            <i class="bi bi-person-workspace"></i>
                            Branch Manager
                        </span>
                    </div>
                </div>

                <div class="rv-kpi-grid">

                    <%-- HALL MANAGEMENT --%>
                    <a class="rv-kpi manager-dashboard-link"
                       href="${ctx}/manager/halls"
                       style="border-left: 4px solid var(--primary-light);">

                        <div class="manager-dashboard-link__top">
                            <div class="manager-dashboard-link__icon">
                                <i class="bi bi-door-open-fill"></i>
                            </div>

                            <i class="bi bi-arrow-right manager-dashboard-link__arrow"></i>
                        </div>

                        <span class="rv-kpi__label">
                            Quản lý phòng chiếu
                        </span>

                        <p class="manager-dashboard-link__description">
                            Thêm, chỉnh sửa, xóa, cấu hình sức chứa và loại phòng chiếu.
                        </p>
                    </a>

                    <%-- ASSIGN MOVIES TO BRANCH --%>
                    <a class="rv-kpi manager-dashboard-link"
                       href="${ctx}/manager/movie-assignments/branches"
                       style="border-left: 4px solid var(--info);">

                        <div class="manager-dashboard-link__top">
                            <div class="manager-dashboard-link__icon">
                                <i class="bi bi-building-check"></i>
                            </div>

                            <i class="bi bi-arrow-right manager-dashboard-link__arrow"></i>
                        </div>

                        <span class="rv-kpi__label">
                            Phim tại chi nhánh
                        </span>

                        <p class="manager-dashboard-link__description">
                            Chọn những phim được phép hoạt động tại chi nhánh bạn quản lý.
                        </p>
                    </a>

                    <%-- ASSIGN MOVIES TO HALL --%>
                    <a class="rv-kpi manager-dashboard-link"
                       href="${ctx}/manager/movie-assignments/halls"
                       style="border-left: 4px solid var(--accent);">

                        <div class="manager-dashboard-link__top">
                            <div class="manager-dashboard-link__icon">
                                <i class="bi bi-film"></i>
                            </div>

                            <i class="bi bi-arrow-right manager-dashboard-link__arrow"></i>
                        </div>

                        <span class="rv-kpi__label">
                            Phim tại phòng chiếu
                        </span>

                        <p class="manager-dashboard-link__description">
                            Phân bổ phim riêng cho từng phòng chiếu trước khi lên lịch chiếu.
                        </p>
                    </a>

                    <%-- MOVIE DURATION --%>
                    <a class="rv-kpi manager-dashboard-link"
                       href="${ctx}/manager/movie-durations"
                       style="border-left: 4px solid var(--warning);">

                        <div class="manager-dashboard-link__top">
                            <div class="manager-dashboard-link__icon">
                                <i class="bi bi-clock-history"></i>
                            </div>

                            <i class="bi bi-arrow-right manager-dashboard-link__arrow"></i>
                        </div>

                        <span class="rv-kpi__label">
                            Thời lượng phim
                        </span>

                        <p class="manager-dashboard-link__description">
                            Cập nhật số phút phim để hệ thống tự tính thời điểm kết thúc suất chiếu.
                        </p>
                    </a>

                    <%-- SHOWTIME MANAGEMENT --%>
                    <a class="rv-kpi manager-dashboard-link"
                       href="${ctx}/manager/showtimesmanagement"
                       style="border-left: 4px solid var(--success);">

                        <div class="manager-dashboard-link__top">
                            <div class="manager-dashboard-link__icon">
                                <i class="bi bi-calendar-week-fill"></i>
                            </div>

                            <i class="bi bi-arrow-right manager-dashboard-link__arrow"></i>
                        </div>

                        <span class="rv-kpi__label">
                            Quản lý lịch chiếu
                        </span>

                        <p class="manager-dashboard-link__description">
                            Tạo, chỉnh sửa, hủy suất chiếu và tự động kiểm tra trùng lịch.
                        </p>
                    </a>
                </div>

                <div class="rv-card">

                    <div class="rv-card__header">
                        <span class="rv-card__title">
                            <i class="bi bi-info-circle-fill"
                               style="margin-right: 8px; color: var(--primary-light);"></i>
                            Quy trình thiết lập lịch chiếu
                        </span>
                    </div>

                    <div class="rv-card__body">

                        <div class="d-flex flex-wrap gap-3 align-items-center"
                             style="color: var(--n-600); line-height: 1.7;">

                            <span class="rv-badge rv-badge--manager">
                                1. Phân bổ phim cho chi nhánh
                            </span>

                            <i class="bi bi-arrow-right text-muted"></i>

                            <span class="rv-badge rv-badge--manager">
                                2. Phân bổ phim cho phòng chiếu
                            </span>

                            <i class="bi bi-arrow-right text-muted"></i>

                            <span class="rv-badge rv-badge--manager">
                                3. Thiết lập thời lượng phim
                            </span>

                            <i class="bi bi-arrow-right text-muted"></i>

                            <span class="rv-badge rv-badge--manager">
                                4. Tạo lịch chiếu
                            </span>
                        </div>
                    </div>
                </div>

            </main>
        </div>

    </body>
</html>
