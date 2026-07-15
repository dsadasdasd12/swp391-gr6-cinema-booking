<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<%@ page contentType="text/html; charset=UTF-8" pageEncoding="UTF-8" %>

<c:set var="ctx" value="${pageContext.request.contextPath}" />
<c:set var="topUser" value="${sessionScope.user}" />

<!DOCTYPE html>
<html lang="vi">

<head>
    <meta charset="UTF-8">
    <meta name="viewport" content="width=device-width, initial-scale=1">

    <title>Phân bổ phim cho chi nhánh - Rạp Việt CMS</title>

    <%-- Dùng CSS CMS/Admin hiện tại, không sửa file CSS gốc. --%>
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

    <link rel="stylesheet" href="${ctx}/assets/css/manager/movie-management.css?v=1">

    <script src="${ctx}/assets/js/main.js" charset="UTF-8" defer></script>
    <script src="${ctx}/assets/js/confirm.js" charset="UTF-8" defer></script>

    <script src="https://cdn.jsdelivr.net/npm/bootstrap@5.3.3/dist/js/bootstrap.bundle.min.js"
            integrity="sha384-YvpcrYf0tY3lHB60NNkmXc5s9fDVZLESaAA55NDzOxhy9GkcIdslK1eN7N6jIeHz"
            crossorigin="anonymous"
            defer></script>
</head>

<body class="manager-branch-assignment-page">

<%-- Flash message từ Controller sau khi POST lưu assignment. --%>
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

    <%-- Sidebar chỉ có menu Manager. --%>
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
                   class="rv-nav__sub-item active">
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
                        Phim tại chi nhánh
                    </span>
                </div>

                <h1 class="rv-page-title">
                    Phân bổ phim cho chi nhánh
                </h1>

                <p class="rv-page-subtitle">
                    Chọn những phim được phép hoạt động tại chi nhánh được Admin phân công cho bạn.
                </p>
            </div>

            <div class="rv-page-header__right">
                <span class="rv-badge rv-badge--manager manager-page-badge">
                    <i class="bi bi-person-workspace"></i>
                    Branch Manager
                </span>
            </div>
        </div>

        <c:if test="${not empty error}">
            <div class="manager-alert manager-alert--error">
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

                                <h2>Chưa thể phân bổ phim</h2>

                                <p>
                                    Tài khoản Manager này chưa được Admin phân công chi nhánh.
                                    Bạn chưa thể lưu danh sách phim cho đến khi có một Branch được gán.
                                </p>
                            </div>
                        </div>
                    </div>
                </div>
            </c:when>

            <c:otherwise>

                <div class="manager-assignment-banner">
                    <i class="bi bi-info-circle-fill"></i>

                    <div>
                        <strong>Quy tắc phân bổ phim</strong>

                        <span>
                            Phim phải được phân bổ cho chi nhánh trước khi có thể
                            được phân bổ xuống từng phòng chiếu và tạo lịch chiếu.
                        </span>
                    </div>
                </div>

                <%-- Thông tin Branch được lấy từ Manager đang đăng nhập. --%>
                <div class="rv-card manager-card-spaced">

                    <div class="rv-card__header">

                        <div class="manager-branch-card-header">

                            <div class="manager-branch-card-info">

                                <div class="manager-branch-card-icon">
                                    <i class="bi bi-building-check-fill"></i>
                                </div>

                                <div>
                                    <h2 class="manager-branch-card-name">
                                        <c:out value="${branch.name}" />
                                    </h2>

                                    <div class="manager-branch-card-address">
                                        <i class="bi bi-geo-alt-fill"></i>
                                        <c:out value="${branch.address}" />
                                    </div>
                                </div>
                            </div>

                            <span class="rv-badge rv-badge--active">
                                <i class="bi bi-check-circle-fill"></i>
                                Chi nhánh được phân công
                            </span>
                        </div>
                    </div>
                </div>

                <div class="rv-card">

                    <div class="rv-card__header">

                        <span class="rv-card__title">
                            <i class="bi bi-film manager-primary-icon"></i>
                            Danh sách phim hệ thống
                        </span>
                    </div>

                    <div class="rv-card__body">

                        <c:choose>

                            <c:when test="${empty movieItems}">

                                <div class="manager-empty-state">
                                    <div class="manager-empty-state__content">

                                        <div class="manager-empty-state__icon">
                                            <i class="bi bi-film"></i>
                                        </div>

                                        <h2>Chưa có phim để phân bổ</h2>

                                        <p>
                                            Hiện tại hệ thống chưa có Movie nào.
                                            Danh sách sẽ hiển thị tại đây khi Admin thêm phim vào hệ thống.
                                        </p>
                                    </div>
                                </div>
                            </c:when>

                            <c:otherwise>

                                <%--
                                    Giữ nguyên action, method, id form và movieIds.
                                    Controller sẽ lấy Branch từ session Manager, không lấy branchId từ form.
                                --%>
                                <form id="branchAssignmentForm"
                                      method="post"
                                      action="${ctx}/manager/movie-assignments/branches"
                                      autocomplete="off"
                                      onsubmit="return confirmSaveAssignment()">

                                    <div class="manager-assignment-summary">

                                        <div class="manager-assignment-note">
                                            Đánh dấu những phim được phép hoạt động tại
                                            <strong>
                                                <c:out value="${branch.name}" />
                                            </strong>.

                                            <br>

                                            Danh sách chỉ được cập nhật sau khi bạn nhấn
                                            <strong>Lưu phân bổ</strong>.

                                            <br>

                                            Khi quay lại màn hình này, hệ thống sẽ tải lại
                                            các phim đã lưu trong <strong>BRANCH_MOVIES</strong>.
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
                                                    <th class="center manager-selection-col-branch">
                                                        Chọn
                                                    </th>

                                                    <th>
                                                        Tên phim
                                                    </th>

                                                    <th>
                                                        Thời lượng
                                                    </th>

                                                    <th>
                                                        Trạng thái
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

                                                        <td class="center">
                                                            
                                                            <%--submit các checkbox--%>
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
                                            Lưu phân bổ
                                        </button>
                                    </div>
                                </form>
                            </c:otherwise>
                        </c:choose>
                    </div>
                </div>
            </c:otherwise>
        </c:choose>
    </main>
</div>

<script>
    /*
     * Gán lại checkbox đúng theo dữ liệu được Controller
     * tải từ database.
     *
     * data-assigned=true nghĩa là phim đã được lưu
     * trong BRANCH_MOVIES của chi nhánh hiện tại.
     */
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

    /*
     * Chọn hoặc bỏ chọn toàn bộ phim.
     */
    function toggleAllMovies(selectAllCheckbox) {
        const movieCheckboxes = document.querySelectorAll(
                ".movie-item-checkbox"
        );

        movieCheckboxes.forEach(function (checkbox) {
            checkbox.checked = selectAllCheckbox.checked;
        });

        updateSelectAllState();
    }

    /*
     * Cập nhật trạng thái checkbox Chọn tất cả.
     */
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

    /*
     * Xác nhận trước khi lưu.
     */
    function confirmSaveAssignment() {
        return confirm(
                "Bạn có chắc muốn lưu danh sách phân bổ phim "
                + "cho chi nhánh này?"
        );
    }

    /*
     * Khi tải trang bình thường, gán checkbox từ database.
     */
    document.addEventListener(
            "DOMContentLoaded",
            function () {
                restoreSavedCheckboxState();
            }
    );

    /*
     * Trường hợp trình duyệt mở lại trang từ bộ nhớ Back/Forward Cache,
     * vẫn ép checkbox trở về đúng dữ liệu đã lưu.
     */
    window.addEventListener(
            "pageshow",
            function () {
                restoreSavedCheckboxState();
            }
    );
</script>

</body>
</html>
