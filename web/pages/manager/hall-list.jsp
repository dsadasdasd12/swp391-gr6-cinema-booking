<%@ page contentType="text/html; charset=UTF-8" pageEncoding="UTF-8" %>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>

<c:set var="ctx" value="${pageContext.request.contextPath}" />
<c:set var="topUser" value="${sessionScope.user}" />

<!DOCTYPE html>
<html lang="vi">
<head>
    <meta charset="UTF-8">
    <meta name="viewport" content="width=device-width, initial-scale=1">

    <title>Quản lý phòng chiếu - Rạp Việt CMS</title>

    <%-- Dùng bộ giao diện đang dùng cho Admin, không sửa các file CSS gốc. --%>
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

        .manager-hall-page .manager-topbar-context {
            display: inline-flex;
            align-items: center;
            gap: var(--s-2);
            padding-right: var(--s-4);
            border-right: 1px solid var(--border);
            color: var(--n-500);
            font-size: var(--text-sm);
        }

        .manager-hall-page .manager-topbar-context i {
            color: var(--primary-light);
        }

        .manager-hall-page .manager-sidebar-info {
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

        .manager-hall-page .manager-sidebar-info__label {
            color: rgba(255, 255, 255, 0.42);
            font-size: 10px;
            font-weight: 700;
            letter-spacing: 0.08em;
        }

        .manager-hall-page .manager-sidebar-info strong {
            color: #ffffff;
            font-size: var(--text-base);
        }

        .manager-hall-page .manager-branch-banner {
            display: flex;
            align-items: flex-start;
            gap: var(--s-3);
            margin-bottom: var(--s-6);
            padding: var(--s-4);
            border: 1px solid #BFDBFE;
            border-radius: var(--r-md);
            background: #EFF6FF;
            color: #1E40AF;
        }

        .manager-hall-page .manager-branch-banner i {
            margin-top: 2px;
            font-size: var(--text-md);
        }

        .manager-hall-page .manager-branch-banner strong {
            display: block;
            margin-bottom: 2px;
            color: #1E3A8A;
        }

        .manager-hall-page .manager-branch-banner span {
            display: block;
            font-size: var(--text-sm);
            line-height: 1.55;
        }

        .manager-hall-page .manager-branch-card__header {
            gap: var(--s-4);
            flex-wrap: wrap;
        }

        .manager-hall-page .manager-branch-card__title {
            display: flex;
            align-items: center;
            gap: var(--s-3);
            min-width: 0;
        }

        .manager-hall-page .manager-branch-card__icon {
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

        .manager-hall-page .manager-branch-card__name {
            margin: 0;
            color: var(--n-900);
            font-size: var(--text-md);
            font-weight: 700;
            line-height: 1.4;
        }

        .manager-hall-page .manager-branch-card__address {
            margin-top: 2px;
            color: var(--n-500);
            font-size: var(--text-sm);
            line-height: 1.45;
        }

        .manager-hall-page .manager-table-wrapper {
            overflow-x: auto;
        }

        .manager-hall-page .manager-hall-id {
            color: var(--n-500);
            font-size: var(--text-sm);
            font-weight: 600;
        }

        .manager-hall-page .manager-hall-name {
            color: var(--n-900);
            font-weight: 700;
        }

        .manager-hall-page .manager-seat-config {
            color: var(--n-800);
            font-weight: 600;
        }

        .manager-hall-page .manager-seat-total {
            margin-top: 3px;
            color: var(--n-500);
            font-size: var(--text-sm);
        }

        .manager-hall-page .manager-hall-type {
            display: inline-flex;
            align-items: center;
            gap: var(--s-2);
            color: var(--n-700);
            font-size: var(--text-sm);
            font-weight: 600;
        }

        .manager-hall-page .manager-hall-type i {
            color: var(--primary-light);
        }

        .manager-hall-page .manager-action-group {
            display: flex;
            align-items: center;
            gap: var(--s-2);
            flex-wrap: wrap;
        }

        .manager-hall-page .manager-action-group form {
            display: inline-flex;
            margin: 0;
        }

        .manager-hall-page .manager-empty-state {
            display: flex;
            min-height: 230px;
            align-items: center;
            justify-content: center;
            padding: var(--s-8);
            color: var(--n-500);
            text-align: center;
        }

        .manager-hall-page .manager-empty-state__content {
            max-width: 460px;
        }

        .manager-hall-page .manager-empty-state__icon {
            display: inline-flex;
            width: 56px;
            height: 56px;
            align-items: center;
            justify-content: center;
            margin-bottom: var(--s-4);
            border-radius: var(--r-lg);
            background: var(--n-100);
            color: var(--n-400);
            font-size: 24px;
        }

        .manager-hall-page .manager-empty-state h2 {
            margin: 0 0 var(--s-2);
            color: var(--n-800);
            font-size: var(--text-md);
            font-weight: 700;
        }

        .manager-hall-page .manager-empty-state p {
            margin: 0;
            color: var(--n-500);
            font-size: var(--text-base);
            line-height: 1.65;
        }

        .manager-hall-page .manager-alert {
            display: flex;
            align-items: flex-start;
            gap: var(--s-3);
            margin-bottom: var(--s-6);
            padding: var(--s-4);
            border-radius: var(--r-md);
            font-size: var(--text-base);
            line-height: 1.55;
        }

        .manager-hall-page .manager-alert--success {
            border: 1px solid #BBF7D0;
            background: var(--success-bg);
            color: #166534;
        }

        .manager-hall-page .manager-alert--error {
            border: 1px solid #FECACA;
            background: var(--danger-bg);
            color: #B91C1C;
        }

        .manager-hall-page .manager-alert i {
            margin-top: 2px;
            font-size: var(--text-md);
        }

        @media (max-width: 900px) {
            .manager-hall-page .manager-topbar-context {
                display: none;
            }
        }

        @media (max-width: 680px) {
            .manager-hall-page .manager-branch-card__header {
                align-items: stretch;
            }

            .manager-hall-page .manager-branch-card__header > .rv-btn {
                width: 100%;
            }

            .manager-hall-page .manager-action-group {
                min-width: 220px;
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

<body class="manager-hall-page">

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

                    <span class="rv-breadcrumb__current">
                        Quản lý phòng chiếu
                    </span>
                </div>

                <h1 class="rv-page-title">
                    Quản lý phòng chiếu
                </h1>

                <p class="rv-page-subtitle">
                    Quản lý các phòng chiếu thuộc chi nhánh được Admin phân công cho tài khoản Manager này.
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

        <%-- Flash message cũ được giữ lại dưới dạng alert trong nội dung trang. --%>
        <c:if test="${not empty requestScope.errorMessage}">
            <div class="manager-alert manager-alert--error">
                <i class="bi bi-exclamation-octagon-fill"></i>
                <span>
                    <c:out value="${requestScope.errorMessage}" />
                </span>
            </div>
        </c:if>

        <c:choose>

            <%-- Manager chưa được Admin phân Branch. --%>
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

                                <h2>Chưa thể quản lý phòng chiếu</h2>

                                <p>
                                    Tài khoản Manager này chưa được Admin phân công chi nhánh.
                                    Bạn chỉ có thể thêm, sửa hoặc quản lý phòng chiếu sau khi được gán Branch.
                                </p>
                            </div>
                        </div>
                    </div>
                </div>
            </c:when>

            <%-- Manager đã có một Branch được phân công. --%>
            <c:otherwise>

                <div class="manager-branch-banner">
                    <i class="bi bi-building-check-fill"></i>

                    <div>
                        <strong>
                            Chi nhánh đang quản lý:
                            <c:out value="${branch.name}" />
                        </strong>

                        <span>
                            <i class="bi bi-geo-alt-fill"></i>
                            <c:out value="${branch.address}" />
                        </span>
                    </div>
                </div>

                <div class="rv-card">

                    <div class="rv-card__header manager-branch-card__header">

                        <div class="manager-branch-card__title">

                            <div class="manager-branch-card__icon">
                                <i class="bi bi-door-open-fill"></i>
                            </div>

                            <div>
                                <h2 class="manager-branch-card__name">
                                    Danh sách phòng chiếu
                                </h2>

                                <div class="manager-branch-card__address">
                                    Theo chi nhánh
                                    <strong>
                                        <c:out value="${branch.name}" />
                                    </strong>
                                </div>
                            </div>
                        </div>

                        <%-- Giữ nguyên route và branchId. --%>
                        <a class="rv-btn rv-btn--primary"
                           href="${ctx}/manager/halls/create?branchId=${branch.id}">

                            <i class="bi bi-plus-lg"></i>
                            Thêm phòng chiếu
                        </a>
                    </div>

                    <div class="rv-card__body">

                        <c:choose>

                            <c:when test="${empty halls}">

                                <div class="manager-empty-state">
                                    <div class="manager-empty-state__content">

                                        <div class="manager-empty-state__icon">
                                            <i class="bi bi-door-closed"></i>
                                        </div>

                                        <h2>Chưa có phòng chiếu nào</h2>

                                        <p>
                                            Chi nhánh này chưa có dữ liệu phòng chiếu.
                                            Hãy tạo phòng đầu tiên để tiếp tục phân bổ phim và lập lịch chiếu.
                                        </p>

                                        <a class="rv-btn rv-btn--primary"
                                           style="margin-top: var(--s-5);"
                                           href="${ctx}/manager/halls/create?branchId=${branch.id}">

                                            <i class="bi bi-plus-lg"></i>
                                            Thêm phòng chiếu đầu tiên
                                        </a>
                                    </div>
                                </div>
                            </c:when>

                            <c:otherwise>

                                <div class="manager-table-wrapper">

                                    <table class="rv-table">

                                        <thead>
                                            <tr>
                                                <th class="col-no">ID</th>
                                                <th>Tên phòng</th>
                                                <th>Cấu hình ghế</th>
                                                <th>Loại phòng</th>
                                                <th>Trạng thái</th>
                                                <th class="col-actions">Thao tác</th>
                                            </tr>
                                        </thead>

                                        <tbody>

                                            <c:forEach var="h" items="${halls}">
                                                <tr>

                                                    <td class="col-no">
                                                        <span class="manager-hall-id">
                                                            #<c:out value="${h.id}" />
                                                        </span>
                                                    </td>

                                                    <td>
                                                        <span class="manager-hall-name">
                                                            <c:out value="${h.name}" />
                                                        </span>
                                                    </td>

                                                    <td>
                                                        <div class="manager-seat-config">
                                                            <c:out value="${h.seatRows}" />
                                                            hàng ×
                                                            <c:out value="${h.seatsPerRow}" />
                                                            ghế
                                                        </div>

                                                        <div class="manager-seat-total">
                                                            Tổng:
                                                            <strong>
                                                                <c:out value="${h.totalSeats}" />
                                                            </strong>
                                                            ghế
                                                        </div>
                                                    </td>

                                                    <td>
                                                        <span class="manager-hall-type">
                                                            <i class="bi bi-stars"></i>
                                                            <c:out value="${h.hallType}" />
                                                        </span>
                                                    </td>

                                                    <td>
                                                        <c:choose>

                                                            <c:when test="${h.status eq 'ACTIVE'}">
                                                                <span class="rv-badge rv-badge--active">
                                                                    <i class="bi bi-check-circle-fill"></i>
                                                                    <c:out value="${h.status}" />
                                                                </span>
                                                            </c:when>

                                                            <c:when test="${h.status eq 'MAINTENANCE'}">
                                                                <span class="rv-badge rv-badge--pending">
                                                                    <i class="bi bi-tools"></i>
                                                                    <c:out value="${h.status}" />
                                                                </span>
                                                            </c:when>

                                                            <c:otherwise>
                                                                <span class="rv-badge rv-badge--inactive">
                                                                    <i class="bi bi-pause-circle-fill"></i>
                                                                    <c:out value="${h.status}" />
                                                                </span>
                                                            </c:otherwise>
                                                        </c:choose>
                                                    </td>

                                                    <td class="col-actions">

                                                        <div class="manager-action-group">

                                                            <%-- Giữ nguyên route sửa Hall. --%>
                                                            <a class="rv-btn rv-btn--ghost rv-btn--sm"
                                                               href="${ctx}/manager/halls/edit?id=${h.id}&branchId=${branch.id}">

                                                                <i class="bi bi-pencil-square"></i>
                                                                Sửa
                                                            </a>

                                                            <%-- Giữ nguyên form đổi trạng thái Hall. --%>
                                                            <form method="post"
                                                                  action="${ctx}/manager/halls/status">

                                                                <input type="hidden"
                                                                       name="id"
                                                                       value="${h.id}">

                                                                <input type="hidden"
                                                                       name="branchId"
                                                                       value="${branch.id}">

                                                                <c:choose>

                                                                    <c:when test="${h.status eq 'ACTIVE'}">

                                                                        <input type="hidden"
                                                                               name="status"
                                                                               value="MAINTENANCE">

                                                                        <button type="submit"
                                                                                class="rv-btn rv-btn--ghost rv-btn--sm"
                                                                                title="Chuyển Hall sang bảo trì">

                                                                            <i class="bi bi-tools"></i>
                                                                            Bảo trì
                                                                        </button>
                                                                    </c:when>

                                                                    <c:otherwise>

                                                                        <input type="hidden"
                                                                               name="status"
                                                                               value="ACTIVE">

                                                                        <button type="submit"
                                                                                class="rv-btn rv-btn--success rv-btn--sm"
                                                                                title="Mở lại Hall">

                                                                            <i class="bi bi-play-circle-fill"></i>
                                                                            Mở lại
                                                                        </button>
                                                                    </c:otherwise>
                                                                </c:choose>
                                                            </form>

                                                            <%-- Giữ nguyên form xóa Hall và confirm cũ. --%>
                                                            <form method="post"
                                                                  action="${ctx}/manager/halls/delete"
                                                                  onsubmit="return confirm('Bạn có chắc muốn xóa phòng chiếu này?');">

                                                                <input type="hidden"
                                                                       name="id"
                                                                       value="${h.id}">

                                                                <input type="hidden"
                                                                       name="branchId"
                                                                       value="${branch.id}">

                                                                <button type="submit"
                                                                        class="rv-btn rv-btn--danger rv-btn--sm">

                                                                    <i class="bi bi-trash3-fill"></i>
                                                                    Xóa
                                                                </button>
                                                            </form>
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