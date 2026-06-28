<%-- 
    Document   : hall-list
    Created on : Jun 9, 2026, 10:12:26 PM
    Author     : MSI
--%>

<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<%@ page contentType="text/html; charset=UTF-8" pageEncoding="UTF-8" %>
<c:set var="ctx" value="${pageContext.request.contextPath}" />

<!DOCTYPE html>
<html lang="vi">
<head>
    <meta charset="UTF-8">
    <title>Quản lý phòng chiếu - RapViet Cinema</title>
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
            <a class="active" href="${ctx}/manager/halls">Quản lý phòng chiếu</a>
            <a href="${ctx}/manager/showtimes">Lịch chiếu</a>
            <a href="#">Gán phim</a>
            <a href="#">Cài đặt chi nhánh</a>
            <a href="${ctx}/logout">Đăng xuất</a>
        </nav>
    </aside>

    <main class="admin-main">
        <div class="admin-topbar">
            <div>
                <strong>Quản lý phòng chiếu</strong>
                <span>Hiển thị phòng chiếu theo từng chi nhánh được phân công</span>
            </div>
        </div>

        <section class="admin-content">
            <div class="page-heading">
                <div>
                    <h1>Quản lý phòng chiếu</h1>
                    <p>Manager chỉ quản lý các phòng chiếu thuộc những chi nhánh được Admin phân công.</p>
                </div>
            </div>

            <c:if test="${not empty sessionScope.flashMessage}">
                <div class="alert ${sessionScope.flashType == 'success' ? 'alert-success' : 'alert-error'}">
                    <c:out value="${sessionScope.flashMessage}" />
                </div>

                <c:remove var="flashMessage" scope="session" />
                <c:remove var="flashType" scope="session" />
            </c:if>

            <c:choose>
                <c:when test="${empty branchHallGroups}">
                    <div class="panel">
                        <div class="panel-header">Không có chi nhánh</div>
                        <div class="panel-body">
                            <div class="empty-admin">
                                Tài khoản Manager này chưa được phân công chi nhánh nào.
                            </div>
                        </div>
                    </div>
                </c:when>

                <c:otherwise>
                    <c:forEach var="group" items="${branchHallGroups}">
                        <div class="panel" style="margin-bottom: 24px;">
                            <div class="panel-header"
                                 style="display: flex; justify-content: space-between; align-items: center; padding-right: 24px;">
                                <div>
                                    <strong><c:out value="${group.branch.name}" /></strong>
                                    <div style="font-size: 13px; color: #94a3b8; margin-top: 4px;">
                                        <c:out value="${group.branch.address}" />
                                    </div>
                                </div>
                                    
                                    <a class="btn btn-primary btn-small"
                                       style="margin-right: 8px;"
                                       href="${ctx}/manager/halls/create?branchId=${group.branch.id}">
                                        + Thêm phòng chiếu
                                    </a>
                            </div>

                            <a class="btn btn-primary btn-small"
                               href="${ctx}/manager/halls/create">
                                + Thêm phòng chiếu
                            </a>
                        </div>

                        <div class="panel-body">

                            <c:choose>

                                <c:when test="${empty halls}">
                                    <div class="empty-admin">
                                        Chi nhánh này chưa có phòng chiếu nào.
                                    </div>
                                </c:when>

                                <c:otherwise>

                                    <table class="admin-table">
                                        <thead>
                                        <tr>
                                            <th>ID</th>
                                            <th>Tên phòng</th>
                                            <th>Cấu hình ghế</th>
                                            <th>Loại phòng</th>
                                            <th>Trạng thái</th>
                                            <th>Thao tác</th>
                                        </tr>
                                        </thead>

                                        <tbody>
                                        <c:forEach var="h" items="${halls}">
                                            <tr>
                                                <td>
                                                    #<c:out value="${h.id}" />
                                                </td>

                                                <td>
                                                    <strong>
                                                        <c:out value="${h.name}" />
                                                    </strong>
                                                </td>

                                                <td>
                                                    <strong>
                                                        <c:out value="${h.seatRows}" />
                                                        hàng ×
                                                        <c:out value="${h.seatsPerRow}" />
                                                        ghế
                                                    </strong>

                                                    <div style="margin-top: 4px;
                                                                color: #94a3b8;
                                                                font-size: 12px;">

                                                        Tổng:
                                                        <c:out value="${h.totalSeats}" />
                                                        ghế
                                                    </div>
                                                </td>

                                                <td>
                                                    <c:out value="${h.hallType}" />
                                                </td>

                                                <td>
                                                    <span class="badge-status
                                                        ${h.status eq 'ACTIVE'
                                                            ? 'badge-active'
                                                            : h.status eq 'MAINTENANCE'
                                                                ? 'badge-warning'
                                                                : 'badge-inactive'}">

                                                        <c:out value="${h.status}" />
                                                    </span>
                                                </td>

                                                <td>
                                                    <div class="action-inline">

                                                        <a class="btn btn-ghost btn-small"
                                                           href="${ctx}/manager/halls/edit?id=${h.id}">
                                                            Sửa
                                                        </a>

                                                        <%-- Đổi trạng thái Hall --%>
                                                        <form method="post"
                                                              action="${ctx}/manager/halls/status">

                                                            <input type="hidden"
                                                                   name="id"
                                                                   value="${h.id}">

                                                            <c:choose>
                                                                <c:when test="${h.status eq 'ACTIVE'}">
                                                                    <input type="hidden"
                                                                           name="status"
                                                                           value="MAINTENANCE">

                                                                    <button type="submit"
                                                                            class="btn btn-warning btn-small">
                                                                        Bảo trì
                                                                    </button>
                                                                </c:when>

                                                                <c:otherwise>
                                                                    <input type="hidden"
                                                                           name="status"
                                                                           value="ACTIVE">

                                                                    <button type="submit"
                                                                            class="btn btn-success btn-small">
                                                                        Mở lại
                                                                    </button>
                                                                </c:otherwise>
                                                            </c:choose>
                                                        </form>

                                                        <%-- Xóa Hall --%>
                                                        <form method="post"
                                                              action="${ctx}/manager/halls/delete"
                                                              onsubmit="return confirm('Bạn có chắc muốn xóa phòng chiếu này?');">

                                                            <input type="hidden"
                                                                   name="id"
                                                                   value="${h.id}">

                                                            <button type="submit"
                                                                    class="btn btn-danger btn-small">
                                                                Xóa
                                                            </button>
                                                        </form>

                                                    </div>
                                                </td>
                                            </tr>
                                            </thead>

                                            <tbody>
                                            <c:forEach var="h" items="${group.halls}">
                                                <tr>
                                                    <td>#${h.id}</td>

                                                    <td>
                                                        <strong><c:out value="${h.name}" /></strong>
                                                    </td>

                                                    <td>
                                                        <c:out value="${h.totalSeats}" />
                                                    </td>

                                                    <td>
                                                        <c:out value="${h.hallType}" />
                                                    </td>

                                                    <td>
                                                        <span class="badge-status
                                                            ${h.status == 'ACTIVE' ? 'badge-active' :
                                                              h.status == 'MAINTENANCE' ? 'badge-warning' : 'badge-inactive'}">
                                                            <c:out value="${h.status}" />
                                                        </span>
                                                    </td>

                                                    <td>
                                                        <div class="action-inline">
                                                            <a class="btn btn-ghost btn-small"
                                                               href="${ctx}/manager/halls/edit?id=${h.id}&branchId=${group.branch.id}">
                                                                Sửa
                                                            </a>

                                                            <form method="post" action="${ctx}/manager/halls/status">
                                                                <input type="hidden" name="id" value="${h.id}">
                                                                <input type="hidden" name="branchId" value="${group.branch.id}">

                                                                <c:choose>
                                                                    <c:when test="${h.status == 'ACTIVE'}">
                                                                        <input type="hidden" name="status" value="MAINTENANCE">
                                                                        <button type="submit" class="btn btn-warning btn-small">
                                                                            Bảo trì
                                                                        </button>
                                                                    </c:when>

                                                                    <c:otherwise>
                                                                        <input type="hidden" name="status" value="ACTIVE">
                                                                        <button type="submit" class="btn btn-success btn-small">
                                                                            Mở lại
                                                                        </button>
                                                                    </c:otherwise>
                                                                </c:choose>
                                                            </form>

                                                            <form method="post"
                                                                  action="${ctx}/manager/halls/delete"
                                                                  onsubmit="return confirm('Bạn có chắc muốn xóa phòng chiếu này?');">
                                                                <input type="hidden" name="id" value="${h.id}">
                                                                <input type="hidden" name="branchId" value="${group.branch.id}">
                                                                <button type="submit" class="btn btn-danger btn-small">
                                                                    Xóa
                                                                </button>
                                                            </form>
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
                    </c:forEach>
                </c:otherwise>
            </c:choose>
        </section>
    </main>
</div>
</body>
</html>
