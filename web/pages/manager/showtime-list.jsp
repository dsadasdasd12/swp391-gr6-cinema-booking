<%-- 
    Document   : showtime-list
    Created on : Jun 10, 2026, 9:03:03 PM
    Author     : MSI
--%>

<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<%@ page contentType="text/html; charset=UTF-8" pageEncoding="UTF-8" %>

<c:set var="ctx" value="${pageContext.request.contextPath}" />

<!DOCTYPE html>
<html lang="vi">
<head>
    <meta charset="UTF-8">
    <title>Quản lý lịch chiếu - RapViet Cinema</title>

    <link rel="stylesheet" href="${ctx}/assets/css/style.css">
    <link rel="stylesheet" href="${ctx}/assets/css/admin.css">
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

            <a href="${ctx}/manager/dashboard">
                Dashboard
            </a>

            <a href="${ctx}/manager/halls">
                Quản lý phòng chiếu
            </a>

            <a href="${ctx}/manager/seat-config">
                Cấu hình ghế
            </a>

            <a class="active" href="${ctx}/manager/showtimesmanagement">
                Quản lý lịch chiếu
            </a>

            <a href="${ctx}/manager/movie-assignments/branches">
                Phim tại chi nhánh
            </a>

            <a href="${ctx}/manager/movie-assignments/halls">
                Phim tại phòng chiếu
            </a>

            <a 
               href="${ctx}/manager/movie-durations">
                Quản lý thời lượng phim
            </a>



            <a href="${ctx}/logout">
                Đăng xuất
            </a>

        </nav>
    </aside>

    <main class="admin-main">

        <div class="admin-topbar">
            <div>
                <strong>Quản lý lịch chiếu</strong>

                <span>
                    Tạo, chỉnh sửa và hủy suất chiếu trong chi nhánh được phân công.
                </span>
            </div>
        </div>

        <section class="admin-content">

            <div class="page-heading">
                <div>
                    <h1>Quản lý lịch chiếu</h1>

                    <p>
                        Manager chỉ quản lý suất chiếu thuộc chi nhánh
                        được Admin phân công.
                    </p>
                </div>

                <c:if test="${not empty branch}">
                    <a class="btn btn-primary"
                       href="${ctx}/manager/showtimesmanagement/create">
                        + Tạo suất chiếu
                    </a>
                </c:if>
            </div>

            <%-- FLASH MESSAGE --%>
            <c:if test="${not empty sessionScope.flashMessage}">
                <div class="alert
                     ${sessionScope.flashType eq 'success'
                       ? 'alert-success'
                       : 'alert-error'}">

                    <c:out value="${sessionScope.flashMessage}" />
                </div>

                <c:remove var="flashMessage" scope="session" />
                <c:remove var="flashType" scope="session" />
            </c:if>

            <c:choose>

                <%-- Manager chưa được Admin gán Branch --%>
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

                                Bạn chưa thể tạo, chỉnh sửa hoặc hủy
                                suất chiếu.
                            </div>
                        </div>
                    </div>

                </c:when>

                <%-- Manager đã được gán đúng một Branch --%>
                <c:otherwise>

                    <div class="panel" style="margin-bottom: 24px;">
                        <div class="panel-header">
                            Chi nhánh đang quản lý
                        </div>

                        <div class="panel-body">
                            <strong style="font-size: 18px;">
                                <c:out value="${branch.name}" />
                            </strong>

                            <div style="font-size: 12px;
                                        color: #94a3b8;
                                        margin-top: 5px;">

                                <c:out value="${branch.address}" />
                            </div>
                        </div>
                    </div>

                    <div class="panel">
                        <div class="panel-header">
                            Danh sách suất chiếu
                        </div>

                        <div class="panel-body">

                            <c:choose>
                                <c:when test="${empty showtimes}">
                                    <div class="empty-admin">
                                        Chi nhánh này chưa có suất chiếu nào.
                                    </div>
                                </c:when>

                                <c:otherwise>
                                    <table class="admin-table">
                                        <thead>
                                        <tr>
                                            <th>ID</th>
                                            <th>Phim</th>
                                            <th>Chi nhánh</th>
                                            <th>Phòng chiếu</th>
                                            <th>Thời gian</th>
                                            <th>Giá vé</th>
                                            <th>Trạng thái</th>
                                            <th>Thao tác</th>
                                        </tr>
                                        </thead>

                                        <tbody>
                                        <c:forEach var="s" items="${showtimes}">
                                            <tr>
                                                <td>
                                                    #<c:out value="${s.id}" />
                                                </td>

                                                <td>
                                                    <strong>
                                                        <c:out value="${s.movieTitle}" />
                                                    </strong>

                                                    <div style="font-size: 12px; color: #94a3b8;">
                                                        <c:out value="${s.movieDurationMin}" /> phút
                                                    </div>
                                                </td>

                                                <td>
                                                    <strong>
                                                        <c:out value="${s.branchName}" />
                                                    </strong>

                                                    <div style="font-size: 12px; color: #94a3b8;">
                                                        <c:out value="${s.branchAddress}" />
                                                    </div>
                                                </td>

                                                <td>
                                                    <strong>
                                                        <c:out value="${s.hallName}" />
                                                    </strong>

                                                    <div style="font-size: 12px; color: #94a3b8;">
                                                        <c:out value="${s.hallType}" />
                                                    </div>
                                                </td>

                                                <td>
                                                    <strong>
                                                        <c:out value="${s.showDate}" />
                                                    </strong>

                                                    <div style="font-size: 12px; color: #94a3b8;">
                                                        <c:out value="${s.startHour}" />
                                                        -
                                                        <c:out value="${s.endHour}" />
                                                    </div>
                                                </td>

                                                <td>
                                                    <c:out value="${s.basePrice}" />
                                                </td>

                                                <td>
                                                    <span class="badge-status
                                                        ${s.status eq 'CANCELLED'
                                                          ? 'badge-inactive'
                                                          : s.status eq 'ON_SALE'
                                                            ? 'badge-active'
                                                            : 'badge-warning'}">

                                                        <c:out value="${s.status}" />
                                                    </span>
                                                </td>

                                                <td>
                                                    <div class="action-inline">

                                                        <c:if test="${s.status ne 'CANCELLED'}">

                                                            <a class="btn btn-ghost btn-small"
                                                               href="${ctx}/manager/showtimesmanagement/edit?id=${s.id}">
                                                                Sửa
                                                            </a>

                                                            <form method="post"
                                                                  action="${ctx}/manager/showtimesmanagement/cancel"
                                                                  onsubmit="return confirm('Bạn có chắc muốn hủy suất chiếu này?');">

                                                                <input type="hidden"
                                                                       name="id"
                                                                       value="${s.id}">

                                                                <button type="submit"
                                                                        class="btn btn-danger btn-small">
                                                                    Hủy
                                                                </button>
                                                            </form>

                                                        </c:if>

                                                        <c:if test="${s.status eq 'CANCELLED'}">
                                                            <span style="font-size: 13px; color: #94a3b8;">
                                                                Đã hủy
                                                            </span>
                                                        </c:if>

                                                    </div>
                                                </td>
                                            </tr>
                                        </c:forEach>
                                        </tbody>
                                    </table>
                                </c:otherwise>
                            </c:choose>

                        </div>
                    </div>

                </c:otherwise>

            </c:choose>

        </section>
    </main>
</div>
</body>
</html>