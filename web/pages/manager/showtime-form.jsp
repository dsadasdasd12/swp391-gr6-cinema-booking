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
    </style>
</head>

<body>

<div class="admin-shell">

    <!-- SIDEBAR -->
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

            <a class="active"
               href="${ctx}/manager/showtimes">
                Quản lý lịch chiếu
            </a>

            <a href="${ctx}/manager/movie-assignments/branches">
                Phim tại chi nhánh
            </a>

            <a href="${ctx}/manager/movie-assignments/halls">
                Phim tại phòng chiếu
            </a>

            <a href="${ctx}/logout">
                Đăng xuất
            </a>

        </nav>

    </aside>

    <!-- MAIN CONTENT -->
    <main class="admin-main">

        <div class="admin-topbar">

            <div>
                <strong>
                    ${isEdit
                      ? 'Cập nhật suất chiếu'
                      : 'Tạo suất chiếu mới'}
                </strong>

                <span>
                    Chọn phòng, phim và thời gian bắt đầu
                </span>
            </div>

        </div>

        <section class="admin-content">

            <div class="page-heading">

                <div>
                    <h1>
                        ${isEdit
                          ? 'Cập nhật suất chiếu'
                          : 'Tạo suất chiếu mới'}
                    </h1>

                    <p>
                        Hệ thống tự động tính giờ kết thúc theo thời lượng
                        phim và kiểm tra trùng lịch trong cùng phòng chiếu.
                    </p>
                </div>

                <a class="btn btn-ghost"
                   href="${ctx}/manager/showtimes">

                    Quay lại

                </a>

            </div>

            <!-- THÔNG BÁO LỖI -->
            <c:if test="${not empty error}">

                <div class="alert alert-error">
                    <c:out value="${error}" />
                </div>

            </c:if>

            <div class="panel">

                <div class="panel-header">
                    Thông tin suất chiếu
                </div>

                <div class="panel-body">

                    <form id="showtimeForm"
                          method="post"
                          action="${ctx}${isEdit
                                  ? '/manager/showtimes/edit'
                                  : '/manager/showtimes/create'}">

                        <c:if test="${isEdit}">

                            <input type="hidden"
                                   name="id"
                                   value="${showtime.id}">

                        </c:if>

                        <div class="form-grid">

                            <!-- PHÒNG CHIẾU -->
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

                                    <c:forEach var="group"
                                               items="${branchHallGroups}">

                                        <optgroup label="${group.branch.name}">

                                            <c:forEach var="hall"
                                                       items="${group.halls}">

                                                <option value="${hall.id}"
                                                        data-branch-id="${group.branch.id}"
                                                    ${showtime.hallId == hall.id
                                                      ? 'selected'
                                                      : ''}>

                                                    <c:out value="${hall.name}" />

                                                    -

                                                    <c:out value="${hall.hallType}" />

                                                </option>

                                            </c:forEach>

                                        </optgroup>

                                    </c:forEach>

                                </select>

                                <span class="field-note">
                                    Chỉ hiển thị các phòng thuộc chi nhánh
                                    mà Manager đang quản lý.
                                </span>

                            </div>

                            <!-- PHIM -->
                            <div class="form-group">

                                <label for="movieId">
                                    Phim *
                                </label>

                                <select id="movieId"
                                        name="movieId"
                                        class="select-field"
                                        required
                                        disabled>

                                    <option value="">
                                        -- Chọn phòng chiếu trước --
                                    </option>

                                </select>

                                <span class="field-note"
                                      id="movieNote">

                                    Danh sách phim phụ thuộc vào phòng chiếu
                                    đã chọn.

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

                            <!-- THỜI GIAN BẮT ĐẦU -->
                            <div class="form-group">

                                <label for="startTime">
                                    Thời gian bắt đầu *
                                </label>

                                <input id="startTime"
                                       name="startTime"
                                       class="input-field"
                                       type="datetime-local"
                                       required
                                       value="${showtime.startInputValue}">

                            </div>

                            <!-- GIÁ VÉ -->
                            <div class="form-group">

                                <label for="basePrice">
                                    Giá vé cơ bản *
                                </label>

                                <input id="basePrice"
                                       name="basePrice"
                                       class="input-field"
                                       type="number"
                                       min="0"
                                       step="1000"
                                       required
                                       value="${showtime.basePrice}">

                                <span class="field-note">
                                    Đơn vị: VNĐ.
                                </span>

                            </div>

                            <!-- TRẠNG THÁI -->
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

                        <!-- THÔNG TIN TÍNH GIỜ -->
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

                            <button id="submitButton"
                                    class="btn btn-primary"
                                    type="submit">

                                ${isEdit ? 'Cập nhật' : 'Lưu'}

                            </button>

                        </div>

                    </form>

                    <!--
                        Nguồn dữ liệu phim được phân bổ theo từng phòng.
                        JavaScript sẽ lấy các option phù hợp để đưa vào
                        select movieId.
                    -->
                    <select id="movieOptionSource"
                            style="display: none;"
                            aria-hidden="true">

                        <c:forEach var="entry"
                                   items="${moviesByHall}">

                            <c:forEach var="movie"
                                       items="${entry.value}">

                                <option value="${movie.id}"
                                        data-hall-id="${entry.key}"
                                        data-duration="${movie.durationMin}">

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

    const hallSelect
            = document.getElementById("hallId");

    const movieSelect
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

    const submitButton
            = document.getElementById("submitButton");

    /**
     * Lấy danh sách phim đã được phân bổ cho phòng đang chọn.
     */
    function loadMoviesByHall(keepInitialMovie) {
        const hallId = hallSelect.value;

        movieSelect.innerHTML = "";

        const defaultOption = document.createElement("option");
        defaultOption.value = "";

        if (!hallId) {
            defaultOption.textContent = "-- Chọn phòng chiếu trước --";

            movieSelect.appendChild(defaultOption);
            movieSelect.disabled = true;

            warningBox.style.display = "none";

            movieNote.textContent
                    = "Danh sách phim phụ thuộc vào phòng chiếu đã chọn.";

            updateSchedulePreview();
            return;
        }

        defaultOption.textContent = "-- Chọn phim --";
        movieSelect.appendChild(defaultOption);

        const sourceOptions = Array.from(
                movieOptionSource.options
        );

        const matchedOptions = sourceOptions.filter(
                function (option) {
                    return option.dataset.hallId === hallId;
                }
        );

        matchedOptions.forEach(function (sourceOption) {
            const newOption = document.createElement("option");

            newOption.value = sourceOption.value;
            newOption.textContent = sourceOption.textContent.trim();
            newOption.dataset.duration
                    = sourceOption.dataset.duration;

            movieSelect.appendChild(newOption);
        });

        if (matchedOptions.length === 0) {
            movieSelect.disabled = true;
            warningBox.style.display = "block";

            movieNote.textContent
                    = "Chưa có phim nào được phân bổ cho phòng này.";

            const selectedHallOption
                    = hallSelect.options[hallSelect.selectedIndex];

            const branchId
                    = selectedHallOption.dataset.branchId || "";

            assignmentLink.href
                    = contextPath
                    + "/manager/movie-assignments/halls"
                    + "?branchId=" + encodeURIComponent(branchId)
                    + "&hallId=" + encodeURIComponent(hallId);

        } else {
            movieSelect.disabled = false;
            warningBox.style.display = "none";

            movieNote.textContent
                    = "Chỉ hiển thị phim đã được phân bổ cho phòng.";

            if (keepInitialMovie && initialMovieId) {
                const initialOption = Array.from(
                        movieSelect.options
                ).find(function (option) {
                    return option.value === initialMovieId;
                });

                if (initialOption) {
                    movieSelect.value = initialMovieId;
                }
            }
        }

        updateSchedulePreview();
    }

    /**
     * Tính thời gian kết thúc dự kiến trên giao diện.
     *
     * Database vẫn lưu end_time do ShowtimeService tự tính.
     */
    function updateSchedulePreview() {
        const selectedMovieOption
                = movieSelect.options[movieSelect.selectedIndex];

        const duration = selectedMovieOption
                ? parseInt(selectedMovieOption.dataset.duration)
                : 0;

        if (!duration || duration <= 0) {
            durationPreview.textContent = "Chưa chọn phim";
            endTimePreview.textContent = "Chưa xác định";
            return;
        }

        durationPreview.textContent
                = duration + " phút";

        const startValue = startTimeInput.value;

        if (!startValue) {
            endTimePreview.textContent
                    = "Chọn thời gian bắt đầu";
            return;
        }

        const startDate
                = parseLocalDateTime(startValue);

        if (!startDate) {
            endTimePreview.textContent
                    = "Thời gian không hợp lệ";
            return;
        }

        startDate.setMinutes(
                startDate.getMinutes() + duration
        );

        endTimePreview.textContent
                = formatDateTime(startDate);
    }

    /**
     * Chuyển giá trị datetime-local thành Date
     * mà không làm thay đổi múi giờ.
     */
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
                parseInt(dateParts[0]),
                parseInt(dateParts[1]) - 1,
                parseInt(dateParts[2]),
                parseInt(timeParts[0]),
                parseInt(timeParts[1])
        );
    }

    /**
     * Hiển thị dạng dd/MM/yyyy HH:mm.
     */
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
                + ":" + minute;
    }

    hallSelect.addEventListener(
            "change",
            function () {
                loadMoviesByHall(false);
            }
    );

    movieSelect.addEventListener(
            "change",
            updateSchedulePreview
    );

    startTimeInput.addEventListener(
            "change",
            updateSchedulePreview
    );

    startTimeInput.addEventListener(
            "input",
            updateSchedulePreview
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
