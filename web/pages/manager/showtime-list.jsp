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
                <strong>Quản lý lịch chiếu</strong>
                <span>Tạo, chỉnh sửa, hủy suất chiếu và gán phòng chiếu</span>
            </div>
        </div>

        <section class="admin-content">
            <div class="page-heading">
                <div>
                    <h1>Quản lý lịch chiếu</h1>
                    <p>Manager chỉ quản lý suất chiếu thuộc các chi nhánh được phân công.</p>
                </div>

                <a class="btn btn-primary" href="${ctx}/manager/showtimes/create">
                    + Tạo suất chiếu
                </a>
            </div>

            <c:if test="${not empty sessionScope.flashMessage}">
                <div class="alert ${sessionScope.flashType == 'success' ? 'alert-success' : 'alert-error'}">
                    <c:out value="${sessionScope.flashMessage}" />
                </div>

                <c:remove var="flashMessage" scope="session" />
                <c:remove var="flashType" scope="session" />
            </c:if>

            <div class="panel">
                <div class="panel-header">Danh sách suất chiếu</div>

                <div class="panel-body">
                    <c:choose>
                        <c:when test="${empty showtimes}">
                            <div class="empty-admin">
                                Chưa có suất chiếu nào.
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
                                        <td>#${s.id}</td>

                                        <td>
                                            <strong>
                                                <c:out value="${s.movieTitle}" />
                                            </strong>
                                            <div style="font-size: 12px; color: #94a3b8;">
                                                ${s.movieDurationMin} phút
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
                                            <strong>${s.showDate}</strong>
                                            <div style="font-size: 12px; color: #94a3b8;">
                                                ${s.startHour} - ${s.endHour}
                                            </div>
                                        </td>

                                        <td>
                                            ${s.basePrice}
                                        </td>

                                        <td>
                                            <span class="badge-status
                                                ${s.status == 'CANCELLED' ? 'badge-inactive' :
                                                  s.status == 'ON_SALE' ? 'badge-active' : 'badge-warning'}">
                                                <c:out value="${s.status}" />
                                            </span>
                                        </td>

                                        <td>
                                            <div class="action-inline">
                                                <c:if test="${s.status != 'CANCELLED'}">
                                                    <a class="btn btn-ghost btn-small"
                                                       href="${ctx}/manager/showtimes/edit?id=${s.id}">
                                                        Sửa
                                                    </a>

                                                    <form method="post"
                                                          action="${ctx}/manager/showtimes/cancel"
                                                          onsubmit="return confirm('Bạn có chắc muốn hủy suất chiếu này?');">
                                                        <input type="hidden" name="id" value="${s.id}">
                                                        <button type="submit" class="btn btn-danger btn-small">
                                                            Hủy
                                                        </button>
                                                    </form>
                                                </c:if>

                                                <c:if test="${s.status == 'CANCELLED'}">
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
        </section>
    </main>
</div>
</body>
</html>