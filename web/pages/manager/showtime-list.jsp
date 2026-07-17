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
        <link rel="stylesheet"
              href="${ctx}/assets/css/manager/showtime.css?v=1">

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
                            <div class="manager-dropdown-name">
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
                       class="rv-nav__item active">

                        <i class="bi bi-calendar-week-fill"></i>
                        Quản lý lịch chiếu
                    </a>
                </div>


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
                               href="${ctx}/manager/showtimesmanagement/create">

                                <i class="bi bi-plus-lg"></i>
                                Tạo suất chiếu
                            </a>
                        </c:if>
                    </div>
                </div>

                <c:choose>

                    <%-- Manager chưa được Admin phân công Branch. --%>
                    <c:when test="${empty branch}">

<<<<<<< HEAD
                        <span class="rv-card__title">
                            <i class="bi bi-calendar-week-fill manager-title-icon"></i>
                            Danh sách suất chiếu
                            
                            <span class="rv-badge rv-badge--manager">
                                <c:out value="${totalShowtimes}" /> suất
                            </span>
                        </span>
                    </div>
=======
                        <div class="rv-card">
>>>>>>> fcc055ef20be3a823b6c57794761574d2e2ba952

                            <div class="rv-card__header">
                                <span class="rv-card__title">
                                    <i class="bi bi-building-x manager-warning-icon"></i>
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

                        <div class="rv-card manager-card-spacing">

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
                                    <i class="bi bi-calendar-week-fill manager-title-icon"></i>
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

                                                <a class="rv-btn rv-btn--primary manager-empty-action"
                                                   href="${ctx}/manager/showtimesmanagement/create">

                                                    <i class="bi bi-plus-lg"></i>
                                                    Tạo suất chiếu đầu tiên
                                                </a>
                                            </div>
                                        </div>
                                    </c:when>

                                    <c:otherwise>

                                        <div class="showtime-search-bar">
                                            <div class="showtime-search-box">
                                                <i class="bi bi-search"></i>
                                                <input id="showtimeSearch"
                                                       type="search"
                                                       class="rv-input"
                                                       list="showtimeSuggestions"
                                                       autocomplete="off"
                                                       placeholder="Tìm phim, phòng, ngày, giờ, trạng thái hoặc ID...">
                                                <button id="clearShowtimeSearch"
                                                        type="button"
                                                        class="showtime-search-clear"
                                                        aria-label="Xóa tìm kiếm"
                                                        hidden>
                                                    <i class="bi bi-x-lg"></i>
                                                </button>
                                            </div>

                                            <span id="showtimeResultCount" class="showtime-result-count"></span>

                                            <datalist id="showtimeSuggestions">
                                                <c:forEach var="s" items="${showtimes}">
                                                    <option value='<c:out value="${s.movieTitle}" />'></option>
                                                    <option value='<c:out value="${s.hallName}" />'></option>
                                                    <option value='<c:out value="${s.showDate}" />'></option>
                                                    <option value='<c:out value="${s.status}" />'></option>
                                                </c:forEach>
                                            </datalist>
                                        </div>

                                        <div id="showtimeGrid" class="showtime-grid">
                                            <c:forEach var="s" items="${showtimes}">
                                                <article class="showtime-card">
                                                    <span class="showtime-search-extra">
                                                        <c:out value="${s.branchName}" />
                                                        <c:out value="${s.branchAddress}" />
                                                        <c:choose>
                                                            <c:when test="${s.status eq 'ON_SALE'}">đang bán</c:when>
                                                            <c:when test="${s.status eq 'CANCELLED'}">đã hủy</c:when>
                                                            <c:otherwise>đã lên lịch</c:otherwise>
                                                        </c:choose>
                                                    </span>

                                                    <div class="showtime-card__header">
                                                        <span class="manager-showtime-id">
                                                            #<c:out value="${s.id}" />
                                                        </span>

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
                                                    </div>

                                                    <div class="showtime-card__body">
                                                        <h3 class="showtime-card__title">
                                                            <c:out value="${s.movieTitle}" />
                                                        </h3>

                                                        <div class="showtime-card__duration">
                                                            <i class="bi bi-clock"></i>
                                                            <c:out value="${s.movieDurationMin}" /> phút
                                                        </div>

                                                        <div class="showtime-card__details">
                                                            <div>
                                                                <i class="bi bi-door-open"></i>
                                                                <span>
                                                                    <strong><c:out value="${s.hallName}" /></strong>
                                                                    <small><c:out value="${s.hallType}" /></small>
                                                                </span>
                                                            </div>

                                                            <div>
                                                                <i class="bi bi-calendar-event"></i>
                                                                <span>
                                                                    <strong><c:out value="${s.showDate}" /></strong>
                                                                    <small>
                                                                        <c:out value="${s.startHour}" /> -
                                                                        <c:out value="${s.endHour}" />
                                                                    </small>
                                                                </span>
                                                            </div>

                                                            <div>
                                                                <i class="bi bi-ticket-perforated"></i>
                                                                <span>
                                                                    <strong><c:out value="${s.basePrice}" /></strong>
                                                                    <small>Giá vé cơ bản</small>
                                                                </span>
                                                            </div>
                                                        </div>
                                                    </div>

                                                    <div class="showtime-card__actions manager-action-group">
                                                        <c:if test="${s.status ne 'CANCELLED'}">
                                                            <a class="rv-btn rv-btn--ghost rv-btn--sm"
                                                               href="${ctx}/manager/showtimesmanagement/edit?id=${s.id}">
                                                                <i class="bi bi-pencil-square"></i>
                                                                Sửa
                                                            </a>

                                                            <form method="post"
                                                                  action="${ctx}/manager/showtimesmanagement/cancel"
                                                                  onsubmit="return confirm('Bạn có chắc muốn hủy suất chiếu này?');">
                                                                <input type="hidden" name="id" value="${s.id}">
                                                                <button type="submit" class="rv-btn rv-btn--danger rv-btn--sm">
                                                                    <i class="bi bi-x-circle-fill"></i>
                                                                    Hủy
                                                                </button>
                                                            </form>
                                                        </c:if>

                                                        <c:if test="${s.status eq 'CANCELLED'}">
                                                            <span class="manager-cancelled-text">Đã hủy</span>
                                                        </c:if>
                                                    </div>
                                                </article>
                                            </c:forEach>
                                        </div>

                                        <div id="showtimeNoResult" class="showtime-no-result" hidden>
                                            <i class="bi bi-search"></i>
                                            <strong>Không tìm thấy suất chiếu</strong>
                                            <span>Hãy thử tên phim, phòng, ngày, giờ hoặc trạng thái khác.</span>
                                        </div>
                                    </c:otherwise>
                                </c:choose>
                            </div>
                        </div>
                    </c:otherwise>
                </c:choose>
            </main>
        </div>

        <script>
            document.addEventListener('DOMContentLoaded', function () {
                const input = document.getElementById('showtimeSearch');
                if (!input)
                    return;

                const cards = Array.from(document.querySelectorAll('.showtime-card'));
                const count = document.getElementById('showtimeResultCount');
                const empty = document.getElementById('showtimeNoResult');
                const clear = document.getElementById('clearShowtimeSearch');

                const normalize = text => String(text || '')
                            .toLowerCase()
                            .normalize('NFD')
                            .replace(/[\u0300-\u036f]/g, '')
                            .replace(/đ/g, 'd')
                            .replace(/\s+/g, ' ')
                            .trim();

                function filterShowtimes() {
                    const words = normalize(input.value).split(' ').filter(Boolean);
                    let visible = 0;

                    cards.forEach(card => {
                        const matched = words.every(word => normalize(card.textContent).includes(word));
                        card.hidden = !matched;
                        if (matched)
                            visible++;
                    });

                    count.textContent = `Hiển thị ${visible} / ${cards.length} suất chiếu`;
                    empty.hidden = visible !== 0;
                    clear.hidden = input.value.length === 0;
                }

                input.addEventListener('input', filterShowtimes);
                clear.addEventListener('click', function () {
                    input.value = '';
                    input.focus();
                    filterShowtimes();
                });

                filterShowtimes();
            });
        </script>

    </body>
</html>
