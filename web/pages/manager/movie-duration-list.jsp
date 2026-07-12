<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<%@ page contentType="text/html; charset=UTF-8" pageEncoding="UTF-8" %>

<c:set var="ctx" value="${pageContext.request.contextPath}" />
<c:set var="topUser" value="${sessionScope.user}" />

<!DOCTYPE html>
<html lang="vi">

<head>
    <meta charset="UTF-8">
    <meta name="viewport" content="width=device-width, initial-scale=1">

    <title>Quản lý thời lượng phim - Rạp Việt CMS</title>

    <%-- Dùng design system hiện có của Admin, không sửa file CSS gốc. --%>
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

    <link rel="stylesheet" href="${ctx}/assets/css/manager/movie-management.css?v=1">

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

<body class="manager-duration-page">

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
                   class="rv-nav__sub-item">
                    Phim tại phòng chiếu
                </a>

                <a href="${ctx}/manager/movie-durations"
                   class="rv-nav__sub-item active">
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
            <a href="${ctx}/DiscountManager"
               class="rv-nav__item">
                <i class="bi bi-tags-fill"></i>
                Quản lý mã giảm giá
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

                    <a href="${ctx}/manager/movie-assignments/branches">
                        Phân bổ phim
                    </a>

                    <i class="bi bi-chevron-right rv-breadcrumb__sep"></i>

                    <span class="rv-breadcrumb__current">
                        Thời lượng phim
                    </span>
                </div>

                <h1 class="rv-page-title">
                    Quản lý thời lượng phim
                </h1>

                <p class="rv-page-subtitle">
                    Thiết lập số phút chạy chính thức của từng Movie để hệ thống tự tính giờ kết thúc suất chiếu.
                </p>
            </div>

            <div class="rv-page-header__right">
                <a class="rv-btn rv-btn--ghost"
                   href="${ctx}/manager/dashboard">

                    <i class="bi bi-arrow-left"></i>
                    Quay lại Dashboard
                </a>
            </div>
        </div>

        <div class="manager-duration-note">
            <i class="bi bi-info-circle-fill"></i>

            <div>
                <strong>Lưu ý:</strong>

                Thời lượng phải là số nguyên từ
                <strong>1 đến 600 phút</strong>.

                Khi tạo hoặc cập nhật suất chiếu, hệ thống tính:

                <strong>
                    Giờ kết thúc = Giờ bắt đầu + Thời lượng phim
                </strong>.
            </div>
        </div>

        <div class="rv-card">

            <div class="rv-card__header">
                <span class="rv-card__title">
                    <i class="bi bi-clock-history manager-duration-title-icon"></i>
                    Danh sách phim
                </span>
            </div>

            <div class="rv-card__body">

                <div class="manager-duration-toolbar">

                    <div class="rv-form-group manager-search-group">

                        <label class="rv-label"
                               for="movieSearch">
                            Tìm kiếm phim
                        </label>

                        <div class="manager-search-input">
                            <i class="bi bi-search"></i>

                            <input id="movieSearch"
                                   type="text"
                                   class="rv-input"
                                   placeholder="Nhập tên phim..."
                                   autocomplete="off"
                                   oninput="filterMovies()">
                        </div>
                    </div>

                    <div class="manager-total-movies">
                        <i class="bi bi-film"></i>

                        Tổng số phim:

                        <strong>
                            <c:out value="${movies.size()}" />
                        </strong>
                    </div>
                </div>

                <c:choose>

                    <%-- Chưa có Movie trong hệ thống. --%>
                    <c:when test="${empty movies}">

                        <div class="manager-empty-state">
                            <div class="manager-empty-state__content">

                                <div class="manager-empty-state__icon">
                                    <i class="bi bi-film"></i>
                                </div>

                                <h2>Chưa có Movie trong hệ thống</h2>

                                <p>
                                    Danh sách Movie sẽ hiển thị tại đây sau khi được thêm vào hệ thống.
                                </p>
                            </div>
                        </div>
                    </c:when>

                    <c:otherwise>

                        <div class="rv-table-wrapper manager-duration-table-wrapper">

                            <table class="rv-table"
                                   id="movieDurationTable">

                                <thead>
                                    <tr>
                                        <th>
                                            Phim
                                        </th>

                                        <th class="manager-duration-status-col">
                                            Trạng thái
                                        </th>

                                        <th class="manager-duration-current-col">
                                            Thời lượng hiện tại
                                        </th>

                                        <th class="manager-duration-update-col">
                                            Cập nhật thời lượng
                                        </th>
                                    </tr>
                                </thead>

                                <tbody>

                                    <c:forEach var="movie"
                                               items="${movies}">

                                        <tr class="movie-duration-row"
                                            data-movie-title="${movie.title}">

                                            <td>

                                                <div class="movie-information">

                                                    <c:choose>

                                                        <c:when test="${not empty movie.posterUrl}">

                                                            <img class="movie-poster-small"
                                                                 src="${movie.posterUrl}"
                                                                 alt="${movie.title}"
                                                                 onerror="this.style.display='none';
                                                                          this.nextElementSibling.style.display='flex';">

                                                            <div class="movie-poster-placeholder is-hidden">

                                                                Không có
                                                                <br>
                                                                ảnh
                                                            </div>
                                                        </c:when>

                                                        <c:otherwise>

                                                            <div class="movie-poster-placeholder">
                                                                Không có
                                                                <br>
                                                                ảnh
                                                            </div>
                                                        </c:otherwise>
                                                    </c:choose>

                                                    <div>

                                                        <span class="movie-name">
                                                            <c:out value="${movie.title}" />
                                                        </span>

                                                        <span class="movie-id">
                                                            Mã phim:
                                                            <c:out value="${movie.id}" />
                                                        </span>
                                                    </div>
                                                </div>
                                            </td>

                                            <td>

                                                <c:choose>

                                                    <c:when test="${movie.status == 'NOW_SHOWING'}">
                                                        <span class="rv-badge rv-badge--nowshowing">
                                                            <i class="bi bi-play-circle-fill"></i>
                                                            Đang chiếu
                                                        </span>
                                                    </c:when>

                                                    <c:when test="${movie.status == 'COMING_SOON'}">
                                                        <span class="rv-badge rv-badge--comingsoon">
                                                            <i class="bi bi-calendar-event-fill"></i>
                                                            Sắp chiếu
                                                        </span>
                                                    </c:when>

                                                    <c:when test="${movie.status == 'ENDED'}">
                                                        <span class="rv-badge rv-badge--ended">
                                                            <i class="bi bi-stop-circle-fill"></i>
                                                            Đã kết thúc
                                                        </span>
                                                    </c:when>

                                                    <c:otherwise>
                                                        <span class="rv-badge rv-badge--inactive">
                                                            <c:out value="${movie.status}" />
                                                        </span>
                                                    </c:otherwise>
                                                </c:choose>
                                            </td>

                                            <td>

                                                <span class="current-duration">
                                                    <c:out value="${movie.durationMin}" />
                                                    phút
                                                </span>

                                                <span class="duration-hours">
                                                    <i class="bi bi-clock"></i>
                                                    <c:out value="${movie.durationHours}" />
                                                    giờ
                                                    <c:out value="${movie.durationRemainingMinutes}" />
                                                    phút
                                                </span>
                                            </td>

                                            <td>

                                                <%--
                                                    Giữ nguyên:
                                                    - POST action
                                                    - movieId
                                                    - durationMin
                                                    - JS confirmDurationUpdate(this)
                                                --%>
                                                <form method="post"
                                                      action="${ctx}/manager/movie-durations/update"
                                                      class="duration-form"
                                                      onsubmit="return confirmDurationUpdate(this)">

                                                    <input type="hidden"
                                                           name="movieId"
                                                           value="${movie.id}">

                                                    <input type="hidden"
                                                           class="movie-title-hidden"
                                                           value="${movie.title}">

                                                    <input type="hidden"
                                                           class="old-duration-hidden"
                                                           value="${movie.durationMin}">

                                                    <div class="duration-input-wrapper">

                                                        <input type="number"
                                                               name="durationMin"
                                                               class="rv-input duration-input"
                                                               value="${movie.durationMin}"
                                                               min="1"
                                                               max="600"
                                                               step="1"
                                                               required
                                                               autocomplete="off">

                                                        <span class="duration-unit">
                                                            phút
                                                        </span>
                                                    </div>

                                                    <button type="submit"
                                                            class="rv-btn rv-btn--primary">

                                                        <i class="bi bi-check-lg"></i>
                                                        Cập nhật
                                                    </button>
                                                </form>
                                            </td>
                                        </tr>
                                    </c:forEach>
                                </tbody>
                            </table>
                        </div>

                        <div id="noSearchResult"
                             class="manager-no-search-result">

                            <i class="bi bi-search"></i>

                            Không tìm thấy Movie phù hợp.
                        </div>
                    </c:otherwise>
                </c:choose>
            </div>
        </div>
    </main>
</div>

<script>
    /*
     * Chuẩn hóa chữ để tìm được có dấu hoặc không dấu.
     * Ví dụ: "Lật Mặt" và "lat mat" đều thành "lat mat".
     */
    function normalizeMovieSearchText(value) {
        return (value || "")
                .toLowerCase()
                .normalize("NFD")
                .replace(/[\u0300-\u036f]/g, "")
                .replace(/đ/g, "d")
                .replace(/[^a-z0-9]+/g, " ")
                .trim()
                .replace(/\s+/g, " ");
    }

    /*
     * Lọc danh sách phim theo tên.
     */
    function filterMovies() {
        const searchInput
                = document.getElementById("movieSearch");

        const keyword
                = normalizeMovieSearchText(
                        searchInput.value
                );

        const rows = Array.from(
                document.querySelectorAll(
                        ".movie-duration-row"
                )
        );

        let visibleCount = 0;

        rows.forEach(function (row) {
            const movieTitle
                    = normalizeMovieSearchText(
                            row.dataset.movieTitle || ""
                    );

            let matched = !keyword
                    || movieTitle.includes(keyword);

            /*
             * Cho phép gõ các từ không cần theo đúng thứ tự.
             * Ví dụ: "thanh tran" vẫn tìm được "Mai - Trấn Thành".
             */
            if (!matched) {
                const searchWords = keyword.split(" ");

                matched = searchWords.every(function (word) {
                    return movieTitle.includes(word);
                });
            }

            row.style.display
                    = matched ? "" : "none";

            if (matched) {
                visibleCount++;
            }
        });

        const noSearchResult
                = document.getElementById("noSearchResult");

        if (noSearchResult) {
            noSearchResult.style.display
                    = visibleCount === 0
                    ? "block"
                    : "none";
        }
    }

    /*
     * Kiểm tra dữ liệu và xác nhận trước khi cập nhật.
     */
    function confirmDurationUpdate(form) {
        const durationInput
                = form.querySelector(".duration-input");

        const movieTitle
                = form.querySelector(
                        ".movie-title-hidden"
                ).value;

        const oldDuration
                = parseInt(
                        form.querySelector(
                                ".old-duration-hidden"
                        ).value
                );

        const newDuration
                = parseInt(durationInput.value);

        if (!Number.isInteger(newDuration)) {
            alert(
                    "Thời lượng phim phải là số nguyên."
            );

            durationInput.focus();
            return false;
        }

        if (newDuration < 1 || newDuration > 600) {
            alert(
                    "Thời lượng phim phải từ 1 đến 600 phút."
            );

            durationInput.focus();
            return false;
        }

        if (newDuration === oldDuration) {
            return confirm(
                    "Thời lượng của phim \""
                    + movieTitle
                    + "\" không thay đổi. Bạn vẫn muốn tiếp tục?"
            );
        }

        return confirm(
                "Cập nhật thời lượng phim \""
                + movieTitle
                + "\" từ "
                + oldDuration
                + " phút thành "
                + newDuration
                + " phút?"
        );
    }
</script>

</body>
</html>
