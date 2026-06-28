<%-- 
    Document   : showtime-form
    Created on : Jun 10, 2026, 9:07:37 PM
    Author     : MSI
--%>

<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<%@ page contentType="text/html; charset=UTF-8" pageEncoding="UTF-8" %>

<c:set var="ctx" value="${pageContext.request.contextPath}" />
<c:set var="isEdit" value="${formMode == 'edit'}" />

<!DOCTYPE html>
<html lang="vi">
<head>
    <meta charset="UTF-8">

    <title>
        ${isEdit ? 'Cập nhật suất chiếu' : 'Tạo suất chiếu'}
        - RapViet Cinema
    </title>

    <link rel="stylesheet"
          href="${ctx}/assets/css/style.css">

    <link rel="stylesheet"
          href="${ctx}/assets/css/admin.css">

    <style>
        .field-note {
            display: block;
            margin-top: 7px;
            color: #94a3b8;
            font-size: 13px;
            line-height: 1.5;
        }

        .schedule-information {
            margin-top: 20px;
            padding: 16px;
            border: 1px solid #2b303b;
            border-radius: 10px;
            background: #11141a;
        }

        .schedule-information h3 {
            margin: 0 0 12px;
            color: #ffffff;
            font-size: 15px;
        }

        .schedule-information p {
            margin: 6px 0;
            color: #b7bdc8;
            font-size: 14px;
        }

        .schedule-information strong {
            color: #ffffff;
        }

        .movie-assignment-warning {
            display: none;
            margin-top: 10px;
            padding: 12px 14px;
            border: 1px solid rgba(245, 158, 11, 0.4);
            border-radius: 8px;
            background: rgba(245, 158, 11, 0.08);
            color: #facc15;
            font-size: 13px;
            line-height: 1.5;
        }

        .movie-assignment-warning a {
            color: #ffffff;
            font-weight: 700;
            text-decoration: underline;
        }

        .movie-autocomplete {
            position: relative;
            width: 100%;
        }

        .movie-suggestion-list {
            display: none;
            position: absolute;
            top: calc(100% + 6px);
            right: 0;
            left: 0;
            z-index: 1000;
            max-height: 260px;
            overflow-y: auto;
            padding: 6px;
            border: 1px solid #2b303b;
            border-radius: 10px;
            background: #1b1f27;
            box-shadow: 0 14px 28px rgba(0, 0, 0, 0.4);
        }

        .movie-suggestion-item {
            display: block;
            width: 100%;
            padding: 11px 12px;
            border: 0;
            border-radius: 8px;
            background: transparent;
            color: #ffffff;
            text-align: left;
            cursor: pointer;
        }

        .movie-suggestion-item:hover,
        .movie-suggestion-item.active {
            background: rgba(255, 255, 255, 0.13);
        }

        .movie-suggestion-title {
            display: block;
            font-size: 14px;
            font-weight: 700;
            line-height: 1.4;
        }

        .movie-suggestion-duration {
            display: block;
            margin-top: 4px;
            color: #cbd5e1;
            font-size: 12px;
        }

        .movie-suggestion-empty {
            padding: 12px;
            color: #94a3b8;
            font-size: 13px;
        }
    </style>
</head>
<body>
<div class="admin-shell">

    <aside class="admin-sidebar">
        <div class="admin-brand">RAPVIET SYSTEM</div>

        <div class="admin-role">
            <p>Phân hệ</p>
            <strong>Manager Dashboard</strong>
            <span>Quyền: Branch Manager</span>
        </div>

        <nav class="admin-menu">
            <a href="${ctx}/manager/dashboard">Dashboard</a>
            <a href="${ctx}/manager/halls">Quản lý phòng chiếu</a>
            <a class="active" href="${ctx}/manager/showtimes">Lịch chiếu</a>
            <a href="#">Gán phim</a>
            <a href="#">Cài đặt chi nhánh</a>
            <a href="${ctx}/logout">Đăng xuất</a>
        </nav>
    </aside>

    <main class="admin-main">
        <div class="admin-topbar">
            <div>
                <strong>${isEdit ? 'Cập nhật suất chiếu' : 'Tạo suất chiếu mới'}</strong>
                <span>Chọn phim, phòng chiếu và thời gian chiếu</span>
            </div>
        </div>

        <section class="admin-content">
            <div class="page-heading">
                <div>
                    <h1>${isEdit ? 'Cập nhật suất chiếu' : 'Tạo suất chiếu mới'}</h1>
                    <p>Hệ thống sẽ tự tính giờ kết thúc theo thời lượng phim và kiểm tra trùng lịch.</p>
                </div>

                <a class="btn btn-ghost"
                   href="${ctx}/manager/showtimes">
                    Quay lại
                </a>

            </div>

            <c:if test="${not empty error}">
                <div class="alert alert-error">
                    <c:out value="${error}" />
                </div>
            </c:if>

            <div class="panel">
                <div class="panel-header">Thông tin suất chiếu</div>

                <div class="panel-body">
                    <form method="post" action="${ctx}${isEdit ? '/manager/showtimes/edit' : '/manager/showtimes/create'}">

                        <c:if test="${isEdit}">
                            <input type="hidden" name="id" value="${showtime.id}">
                        </c:if>

                        <div class="form-grid">

                            <div class="form-group">
                                <label for="movieId">Phim *</label>
                                <select id="movieId" name="movieId" class="select-field" required>
                                    <option value="">-- Chọn phim --</option>

                                <label>
                                    Chi nhánh được phân công
                                </label>

                                <input type="text"
                                       class="input-field"
                                       value="${branch.name}"
                                       readonly>

                                <span class="field-note">
                                    Manager chỉ có thể tạo suất chiếu cho các phòng
                                    thuộc chi nhánh này.
                                </span>

                            </div>

                            <div class="form-group">

                                <label for="hallId">
                                    Phòng chiếu *
                                </label>

                                <select id="hallId"
                                        name="hallId"
                                        class="select-field"
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

                            <div class="form-group">
                                <label for="hallId">Phòng chiếu *</label>
                                <select id="hallId" name="hallId" class="select-field" required>
                                    <option value="">-- Chọn phòng chiếu --</option>

                                <label for="movieTitleInput">
                                    Phim *
                                </label>

                                <div id="movieAutocomplete"
                                     class="movie-autocomplete">

                                    <input id="movieTitleInput"
                                           class="input-field"
                                           type="text"
                                           placeholder="Nhập hoặc chọn phim..."
                                           autocomplete="off"
                                           required
                                           disabled
                                           role="combobox"
                                           aria-autocomplete="list"
                                           aria-expanded="false"
                                           aria-controls="movieSuggestionList">

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
                                    Chọn phòng chiếu trước. Sau đó bấm vào ô Phim
                                    để xem gợi ý hoặc nhập tên phim để tìm.
                                </span>

                                <div id="movieAssignmentWarning"
                                     class="movie-assignment-warning">

                                    Phòng chiếu này chưa được phân bổ phim.

                                    <br>

                                    <a id="assignmentLink"
                                       href="${ctx}/manager/movie-assignments/halls">
                                        Đi đến phân bổ phim cho phòng
                                    </a>

                                </div>

                            </div>

                            <div class="form-group">
                                <label for="startTime">Thời gian bắt đầu *</label>
                                <input id="startTime"
                                       name="startTime"
                                       class="input-field"
                                       type="datetime-local"
                                       required
                                       value="${showtime.startInputValue}">
                            </div>

                            <div class="form-group">
                                <label for="basePrice">Giá vé cơ bản *</label>
                                <input id="basePrice"
                                       name="basePrice"
                                       class="input-field"
                                       type="number"
                                       min="0"
                                       step="1000"
                                       required
                                       value="${showtime.basePrice}">
                            </div>

                            <div class="form-group">

                                <label for="status">
                                    Trạng thái *
                                </label>

                                <select id="status"
                                        name="status"
                                        class="select-field"
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

                        </div>

                        <div class="schedule-information">

                            <h3>
                                Thời gian suất chiếu
                            </h3>

                            <p>
                                Thời lượng phim:

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
                                Hệ thống sẽ từ chối lưu nếu phòng đã có
                                suất chiếu bị chồng thời gian.
                            </p>

                        </div>

                        <div class="form-actions">

                            <a class="btn btn-ghost"
                               href="${ctx}/manager/showtimes">
                                Hủy
                            </a>

                            <button class="btn btn-primary"
                                    type="submit">
                                ${isEdit ? 'Cập nhật' : 'Lưu'}
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
        </section>
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
