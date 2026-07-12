<%-- 
    Document   : hall-movie-assignment
    Created on : Jun 18, 2026, 4:13:51 PM
    Author     : MSI
--%>

<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<%@ page contentType="text/html; charset=UTF-8" pageEncoding="UTF-8" %>

<c:set var="ctx" value="${pageContext.request.contextPath}" />

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

    <title>
        Phân bổ phim cho phòng chiếu - RapViet Cinema
    </title>

    <link rel="stylesheet"
          href="${ctx}/assets/css/style.css">

    <link rel="stylesheet"
          href="${ctx}/assets/css/admin.css">

    <style>
        .assignment-filter {
            display: grid;
            grid-template-columns: repeat(2, minmax(0, 1fr));
            gap: 16px;
            align-items: end;
        }

        .assignment-summary {
            display: flex;
            justify-content: space-between;
            align-items: center;
            gap: 16px;
            margin-bottom: 18px;
        }

        .assignment-note {
            color: #9aa0aa;
            font-size: 14px;
            line-height: 1.5;
        }

        .movie-checkbox {
            width: 18px;
            height: 18px;
            cursor: pointer;
            accent-color: #e50914;
        }

        .movie-title {
            color: #ffffff;
            font-weight: 700;
        }

        .movie-duration {
            color: #94a3b8;
            font-size: 13px;
        }

        .select-all-box {
            display: inline-flex;
            align-items: center;
            gap: 8px;
            color: #d2d5db;
            cursor: pointer;
            font-size: 14px;
            white-space: nowrap;
        }

        .assignment-footer {
            display: flex;
            justify-content: flex-end;
            gap: 12px;
            padding-top: 20px;
            margin-top: 18px;
            border-top: 1px solid #252832;
        }

        .badge-coming {
            background: rgba(202, 138, 4, 0.16);
            color: #facc15;
        }

        .hall-information {
            padding: 14px 16px;
            margin-bottom: 18px;
            background: #101217;
            border: 1px solid #272b35;
            border-radius: 9px;
            color: #d2d5db;
            font-size: 14px;
            line-height: 1.6;
        }

        .hall-information strong {
            color: #ffffff;
        }

        @media (max-width: 900px) {
            .assignment-filter {
                grid-template-columns: 1fr;
            }

            .assignment-summary {
                flex-direction: column;
                align-items: flex-start;
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

            <a class="active" href="${ctx}/manager/movie-assignments/halls">
                Phim tại phòng chiếu
            </a>

            <a 
               href="${ctx}/manager/movie-durations">
                Quản lý thời lượng phim
            </a>



            <a href="${ctx}/DiscountManager">
                Quản lý mã giảm giá
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
                    Phân bổ phim cho phòng chiếu
                </strong>

                <span>
                    Manager chỉ quản lý Hall thuộc chi nhánh được Admin phân công.
                </span>
            </div>

        </div>

        <section class="admin-content">

            <div class="page-heading">

                <div>
                    <h1>Phim tại phòng chiếu</h1>

                    <p>
                        Phim phải được phân bổ cho chi nhánh trước
                        khi được phân bổ cho từng phòng chiếu.
                    </p>
                </div>

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

            <%-- REQUEST ERROR --%>
            <c:if test="${not empty error}">

                <div class="alert alert-error">
                    <c:out value="${error}" />
                </div>

            </c:if>

            <c:choose>

                <%-- MANAGER CHƯA ĐƯỢC ADMIN GÁN BRANCH --%>
                <c:when test="${empty branch}">

                    <div class="panel">

                        <div class="panel-header">
                            Chưa được phân công chi nhánh
                        </div>

                        <div class="panel-body">

                            <div class="empty-admin">

                                Tài khoản Manager này chưa được Admin
                                phân công chi nhánh.

                                <br><br>

                                Không thể phân bổ phim cho phòng chiếu
                                cho đến khi có một Branch được gán.

                            </div>

                        </div>

                    </div>

                </c:when>

                <c:otherwise>

                    <%-- HIỂN THỊ BRANCH ĐƯỢC GÁN VÀ CHỌN HALL --%>
                    <div class="panel"
                         style="margin-bottom: 24px;">

                        <div class="panel-header">
                            Chi nhánh và phòng chiếu
                        </div>

                        <div class="panel-body">

                            <form id="assignmentFilterForm"
                                  method="get"
                                  action="${ctx}/manager/movie-assignments/halls"
                                  class="assignment-filter"
                                  autocomplete="off">

                                <%-- BRANCH CHỈ HIỂN THỊ, KHÔNG CHO MANAGER ĐỔI --%>
                                <div class="form-group">

                                    <label>
                                        Chi nhánh được phân công
                                    </label>

                                    <input type="text"
                                           class="input-field"
                                           value="${branch.name}"
                                           readonly>

                                </div>

                                <%-- MANAGER ĐƯỢC CHỌN HALL TRONG BRANCH CỦA MÌNH --%>
                                <div class="form-group">

                                    <label for="hallId">
                                        Phòng chiếu
                                    </label>

                                    <select id="hallId"
                                            name="hallId"
                                            class="select-field"
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

                                <noscript>

                                    <button type="submit"
                                            class="btn btn-ghost">

                                        Xem danh sách phim

                                    </button>

                                </noscript>

                            </form>

                        </div>

                    </div>

                    <c:choose>

                        <%-- BRANCH CHƯA CÓ HALL --%>
                        <c:when test="${empty halls}">

                            <div class="panel">

                                <div class="panel-header">
                                    Không có phòng chiếu
                                </div>

                                <div class="panel-body">

                                    <div class="empty-admin">

                                        Chi nhánh được phân công
                                        chưa có phòng chiếu.

                                        <br><br>

                                        Hãy tạo phòng chiếu trước khi
                                        phân bổ phim.

                                    </div>

                                </div>

                            </div>

                        </c:when>

                        <c:otherwise>

                            <%-- DANH SÁCH PHIM CỦA HALL ĐANG CHỌN --%>
                            <div class="panel">

                                <div class="panel-header">
                                    Danh sách phim của phòng chiếu
                                </div>

                                <div class="panel-body">

                                    <div class="hall-information">

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

                                    <c:choose>

                                        <%-- BRANCH CHƯA CÓ MOVIE ĐỂ GÁN CHO HALL --%>
                                        <c:when test="${empty movieItems}">

                                            <div class="empty-admin">

                                                Chi nhánh này chưa được
                                                phân bổ phim nào.

                                                <br><br>

                                                <a class="btn btn-primary"
                                                   href="${ctx}/manager/movie-assignments/branches">

                                                    Phân bổ phim cho chi nhánh

                                                </a>

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

                                                <div class="assignment-summary">

                                                    <div class="assignment-note">

                                                        Các lựa chọn bên dưới chỉ
                                                        áp dụng cho phòng đang hiển thị.

                                                        <br>

                                                        Chuyển sang phòng khác sẽ
                                                        tải lại dữ liệu riêng của phòng đó.

                                                        <br>

                                                        Lựa chọn chỉ được lưu sau khi
                                                        nhấn nút Lưu phân bổ.

                                                    </div>

                                                    <label class="select-all-box">

                                                        <input type="checkbox"
                                                               id="selectAll"
                                                               class="movie-checkbox"
                                                               autocomplete="off"
                                                               onchange="toggleAllMovies(this)">

                                                        Chọn tất cả

                                                    </label>

                                                </div>

                                                <table class="admin-table">

                                                    <thead>

                                                    <tr>

                                                        <th style="width: 80px;">
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

                                                            <td>

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

                                                                <span class="movie-title">

                                                                    <c:out value="${item.title}" />

                                                                </span>

                                                            </td>

                                                            <td>

                                                                <span class="movie-duration">

                                                                    <c:out value="${item.durationLabel}" />

                                                                </span>

                                                            </td>

                                                            <td>

                                                                <span class="badge-status
                                                                    ${item.status == 'NOW_SHOWING'
                                                                      ? 'badge-active'
                                                                      : item.status == 'COMING_SOON'
                                                                        ? 'badge-coming'
                                                                        : 'badge-inactive'}">

                                                                    <c:out value="${item.statusLabel}" />

                                                                </span>

                                                            </td>

                                                            <td>

                                                                <c:choose>

                                                                    <c:when test="${item.assigned}">

                                                                        <span class="badge-status badge-active">
                                                                            Đã phân bổ
                                                                        </span>

                                                                    </c:when>

                                                                    <c:otherwise>

                                                                        <span style="color: #94a3b8;
                                                                                     font-size: 13px;">

                                                                            Chưa phân bổ

                                                                        </span>

                                                                    </c:otherwise>

                                                                </c:choose>

                                                            </td>

                                                        </tr>

                                                    </c:forEach>

                                                    </tbody>

                                                </table>

                                                <div class="assignment-footer">

                                                    <a class="btn btn-ghost"
                                                       href="${ctx}/manager/dashboard">

                                                        Quay lại

                                                    </a>

                                                    <button type="submit"
                                                            class="btn btn-primary">

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

        </section>

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