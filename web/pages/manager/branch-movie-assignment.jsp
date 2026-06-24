<%-- 
    Document   : branch-movie-assignment
    Created on : Jun 18, 2026, 4:11:32 PM
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
        Phân bổ phim cho chi nhánh - RapViet Cinema
    </title>

    <link rel="stylesheet"
          href="${ctx}/assets/css/style.css">

    <link rel="stylesheet"
          href="${ctx}/assets/css/admin.css">

    <style>
        .assignment-filter {
            display: flex;
            align-items: flex-end;
            gap: 14px;
        }

        .assignment-filter .form-group {
            width: 360px;
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
            display: block;
            color: #ffffff;
            font-weight: 700;
            margin-bottom: 4px;
        }

        .movie-duration {
            color: #94a3b8;
            font-size: 12px;
        }

        .select-all-box {
            display: inline-flex;
            align-items: center;
            gap: 8px;
            color: #d2d5db;
            cursor: pointer;
            font-size: 14px;
        }

        .badge-coming {
            background: rgba(202, 138, 4, 0.16);
            color: #facc15;
        }

        .assignment-footer {
            display: flex;
            justify-content: flex-end;
            gap: 12px;
            padding-top: 20px;
            margin-top: 18px;
            border-top: 1px solid #252832;
        }

        @media (max-width: 700px) {
            .assignment-filter {
                flex-direction: column;
                align-items: stretch;
            }

            .assignment-filter .form-group {
                width: 100%;
            }

            .assignment-summary {
                align-items: flex-start;
                flex-direction: column;
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

            <a href="${ctx}/manager/showtimes">
                Lịch chiếu
            </a>

            <a class="active"
               href="${ctx}/manager/movie-assignments/branches">
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

    <%-- MAIN CONTENT --%>
    <main class="admin-main">

        <div class="admin-topbar">

            <div>
                <strong>
                    Phân bổ phim cho chi nhánh
                </strong>

                <span>
                    Quản lý danh sách phim được phép hoạt động tại chi nhánh được phân công
                </span>
            </div>

        </div>

        <section class="admin-content">

            <div class="page-heading">

                <div>
                    <h1>Phim tại chi nhánh</h1>

                    <p>
                        Manager chỉ có thể phân bổ phim cho chi nhánh được Admin phân công quản lý.
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

                <%-- MANAGER CHƯA ĐƯỢC PHÂN CÔNG CHI NHÁNH --%>
                <c:when test="${empty branch}">

                    <div class="panel">

                        <div class="panel-header">
                            Không có chi nhánh
                        </div>

                        <div class="panel-body">

                            <div class="empty-admin">
                                Tài khoản Manager này chưa được Admin phân công chi nhánh.
                                Không thể phân bổ phim cho đến khi có một Branch được gán.
                            </div>

                        </div>

                    </div>

                </c:when>

                <c:otherwise>
                    
                    <div class="panel"
                         style="margin-bottom: 24px;">
                        
                        <div class="panel-header">
                            Chi nhánh được phân công
                        </div>
                        
                        <div class="panel-body">
                            <div class="assignment-summary">
                                <div>
                                    <strong style="font-size: 18px;">
                                        <c:out value="${branch.name}" />
                                    </strong>
                                    
                                    <div class="assignment-note"
                                         style="margin-top: 6px;">
                                        
                                        <c:out value="${branch.address}" />
                                    </div>
                                
                                </div>
                                    
                                    <span class="badge-status badge-active">
                                        Branch của bạn
                                    </span>
                            
                            </div>
                        
                        </div>
                    </div>

                    <%-- DANH SÁCH PHIM --%>
                    <div class="panel">

                        <div class="panel-header">
                            Danh sách phim
                        </div>

                        <div class="panel-body">

                            <c:choose>

                                <%-- HỆ THỐNG CHƯA CÓ PHIM --%>
                                <c:when test="${empty movieItems}">

                                    <div class="empty-admin">
                                        Hiện tại chưa có phim nào
                                        trong hệ thống.
                                    </div>

                                </c:when>

                                <c:otherwise>

                                    <form id="branchAssignmentForm"
                                          method="post"
                                          action="${ctx}/manager/movie-assignments/branches"
                                          autocomplete="off"
                                          onsubmit="return confirmSaveAssignment()">
                                        
                                        <div class="assignment-summary">

                                            <div class="assignment-note">
                                                Đánh dấu những phim được phép hoạt động tại
                                                <strong>
                                                    <c:out value="${branch.name}" />
                                                </strong>.
                                                
                                                <br>
                                                Những lựa chọn chỉ được ghi nhận sau khi nhấn
                                                nút Lưu phân bổ.
                                                
                                                <br>
                                                
                                                Khi quay lại trang này, hệ thống sẽ hiển thị
                                                các phim đã được lưu cho Branch của bạn.

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

        </section>

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
