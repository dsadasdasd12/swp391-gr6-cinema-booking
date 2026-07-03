<%-- 
    Document   : movie-duration-list
    Created on : Jun 18, 2026, 7:23:16 PM
    Author     : MSI
--%>

<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<%@ page contentType="text/html; charset=UTF-8" pageEncoding="UTF-8" %>

<c:set var="ctx" value="${pageContext.request.contextPath}" />

<!DOCTYPE html>
<html lang="vi">

<head>
    <meta charset="UTF-8">

    <title>
        Quản lý thời lượng phim - RapViet Cinema
    </title>

    <link rel="stylesheet"
          href="${ctx}/assets/css/style.css">

    <link rel="stylesheet"
          href="${ctx}/assets/css/admin.css">

    <style>
        .duration-toolbar {
            display: flex;
            justify-content: space-between;
            align-items: center;
            gap: 16px;
            margin-bottom: 20px;
        }

        .duration-search {
            width: 100%;
            max-width: 420px;
        }

        .duration-search input {
            width: 100%;
        }

        .duration-note {
            padding: 14px 16px;
            margin-bottom: 20px;
            border: 1px solid #2b303b;
            border-radius: 9px;
            background: #11141a;
            color: #aeb4bf;
            font-size: 14px;
            line-height: 1.6;
        }

        .duration-note strong {
            color: #ffffff;
        }

        .movie-information {
            display: flex;
            align-items: center;
            gap: 12px;
        }

        .movie-poster-small {
            width: 52px;
            height: 72px;
            flex-shrink: 0;
            object-fit: cover;
            border: 1px solid #30343f;
            border-radius: 7px;
            background: #171a21;
        }

        .movie-poster-placeholder {
            width: 52px;
            height: 72px;
            flex-shrink: 0;
            display: flex;
            align-items: center;
            justify-content: center;
            border: 1px solid #30343f;
            border-radius: 7px;
            background: #171a21;
            color: #7f8794;
            font-size: 11px;
            text-align: center;
        }

        .movie-name {
            display: block;
            margin-bottom: 5px;
            color: #ffffff;
            font-weight: 700;
        }

        .movie-id {
            color: #8f96a3;
            font-size: 12px;
        }

        .duration-form {
            display: flex;
            align-items: center;
            gap: 10px;
        }

        .duration-input-wrapper {
            position: relative;
            width: 150px;
        }

        .duration-input-wrapper input {
            width: 100%;
            padding-right: 48px;
        }

        .duration-unit {
            position: absolute;
            top: 50%;
            right: 13px;
            color: #9299a5;
            font-size: 13px;
            pointer-events: none;
            transform: translateY(-50%);
        }

        .current-duration {
            color: #ffffff;
            font-weight: 700;
        }

        .duration-hours {
            display: block;
            margin-top: 4px;
            color: #9299a5;
            font-size: 12px;
        }

        .badge-coming {
            background: rgba(202, 138, 4, 0.16);
            color: #facc15;
        }

        .table-responsive {
            overflow-x: auto;
        }

        .no-search-result {
            display: none;
            padding: 30px 20px;
            color: #9aa0aa;
            text-align: center;
        }

        @media (max-width: 900px) {
            .duration-toolbar {
                flex-direction: column;
                align-items: stretch;
            }

            .duration-search {
                max-width: none;
            }

            .duration-form {
                align-items: stretch;
                flex-direction: column;
            }

            .duration-input-wrapper {
                width: 100%;
            }
        }
    </style>
</head>

<body>

<div class="admin-shell">

    <%-- SIDEBAR --%>
    <aside class="admin-sidebar">

        <div class="admin-brand">
            RAPVIET SYSTEM
        </div>

        <div class="admin-role">
            <p>Phân hệ</p>
            <strong>Manager Dashboard</strong>
            <span>Quyền: Branch Manager</span>
        </div>

        <nav class="admin-menu">

            <a href="${ctx}/manager/dashboard">
                Dashboard
            </a>

            <a href="${ctx}/manager/halls">
                Quản lý phòng chiếu
            </a>

            <a href="${ctx}/manager/seat-config">
                Cấu hình ghế
            </a>

            <a href="${ctx}/manager/showtimesmanagement">
                Quản lý lịch chiếu
            </a>

            <a href="${ctx}/manager/movie-assignments/branches">
                Phim tại chi nhánh
            </a>

            <a href="${ctx}/manager/movie-assignments/halls">
                Phim tại phòng chiếu
            </a>

            <a class="active"
               href="${ctx}/manager/movie-durations">
                Quản lý thời lượng phim
            </a>



            <a href="${ctx}/logout">
                Đăng xuất
            </a>

        </nav>

    </aside>

    <%-- MAIN CONTENT --%>
    <main class="admin-main">

        <div class="admin-topbar">

            <div>
                <strong>
                    Quản lý thời lượng phim
                </strong>

                <span>
                    Thiết lập số phút chạy chính thức của từng bộ phim
                </span>
            </div>

        </div>

        <section class="admin-content">

            <div class="page-heading">

                <div>
                    <h1>Thời lượng phim</h1>

                    <p>
                        Thời lượng được sử dụng để tự động tính giờ kết thúc
                        khi tạo hoặc cập nhật suất chiếu.
                    </p>
                </div>

                <a class="btn btn-ghost"
                   href="${ctx}/manager/dashboard">

                    Quay lại Dashboard

                </a>

            </div>

            <%-- FLASH MESSAGE --%>
            <c:if test="${not empty sessionScope.flashMessage}">

                <div class="alert
                     ${sessionScope.flashType == 'success'
                       ? 'alert-success'
                       : 'alert-error'}">

                    <c:out value="${sessionScope.flashMessage}" />

                </div>

                <c:remove var="flashMessage"
                          scope="session" />

                <c:remove var="flashType"
                          scope="session" />

            </c:if>

            <div class="duration-note">

                <strong>Lưu ý:</strong>

                Thời lượng phải là số nguyên từ
                <strong>1 đến 600 phút</strong>.

                Khi tạo suất chiếu mới, hệ thống tính:

                <strong>
                    Giờ kết thúc = Giờ bắt đầu + Thời lượng phim
                </strong>.

            </div>

            <div class="panel">

                <div class="panel-header">
                    Danh sách phim
                </div>

                <div class="panel-body">

                    <div class="duration-toolbar">

                        <div class="duration-search">

                            <label for="movieSearch">
                                Tìm kiếm phim
                            </label>

                            <input id="movieSearch"
                                   type="text"
                                   class="input-field"
                                   placeholder="Nhập tên phim..."
                                   autocomplete="off"
                                   oninput="filterMovies()">

                        </div>

                        <div style="color: #9aa0aa; font-size: 14px;">

                            Tổng số phim:

                            <strong style="color: #ffffff;">
                                <c:out value="${movies.size()}" />
                            </strong>

                        </div>

                    </div>

                    <c:choose>

                        <%-- CHƯA CÓ PHIM TRONG HỆ THỐNG --%>
                        <c:when test="${empty movies}">

                            <div class="empty-admin">

                                Hiện tại chưa có phim nào trong hệ thống.

                            </div>

                        </c:when>

                        <c:otherwise>

                            <div class="table-responsive">

                                <table class="admin-table"
                                       id="movieDurationTable">

                                    <thead>

                                    <tr>

                                        <th>
                                            Phim
                                        </th>

                                        <th style="width: 190px;">
                                            Trạng thái
                                        </th>

                                        <th style="width: 190px;">
                                            Thời lượng hiện tại
                                        </th>

                                        <th style="width: 360px;">
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

                                                            <div class="movie-poster-placeholder"
                                                                 style="display: none;">

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

                                                        <span class="badge-status badge-active">
                                                            Đang chiếu
                                                        </span>

                                                    </c:when>

                                                    <c:when test="${movie.status == 'COMING_SOON'}">

                                                        <span class="badge-status badge-coming">
                                                            Sắp chiếu
                                                        </span>

                                                    </c:when>

                                                    <c:when test="${movie.status == 'ENDED'}">

                                                        <span class="badge-status badge-inactive">
                                                            Đã kết thúc
                                                        </span>

                                                    </c:when>

                                                    <c:otherwise>

                                                        <span class="badge-status badge-inactive">

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

                                                    <c:out value="${movie.durationHours}" />
                                                    giờ
                                                    <c:out value="${movie.durationRemainingMinutes}" />
                                                    phút

                                                </span>

                                            </td>

                                            <td>

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
                                                               class="input-field duration-input"
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
                                                            class="btn btn-primary">

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
                                 class="no-search-result">

                                Không tìm thấy phim phù hợp.

                            </div>

                        </c:otherwise>

                    </c:choose>

                </div>

            </div>

        </section>

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