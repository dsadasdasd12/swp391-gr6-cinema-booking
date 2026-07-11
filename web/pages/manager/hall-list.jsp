<%--
    Document   : hall-list
    Purpose    : Hall Management - RapViet Cinema Branch Manager
--%>

<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<%@ taglib prefix="fn" uri="http://java.sun.com/jsp/jstl/functions" %>
<%@ page contentType="text/html; charset=UTF-8" pageEncoding="UTF-8" %>

<c:set var="ctx" value="${pageContext.request.contextPath}" />
<c:set var="pageTitle" value="Quản lý phòng chiếu — Rạp Việt CMS" scope="request" />

<c:set var="extraCss" scope="request">
    <link rel="stylesheet" href="${ctx}/assets/css/manager/hall.css?v=1">
</c:set>

<%@ include file="/pages/shared/header-admin.jsp" %>
<%@ include file="/pages/shared/sidebar-admin.jsp" %>

<section class="management-page hall-list-page">
    <div class="management-header">
        <div>
            <h1>Quản lý phòng chiếu</h1>
            <p>Thêm, chỉnh sửa, xóa, cấu hình sức chứa và trạng thái phòng chiếu của chi nhánh được phân công.</p>
        </div>

        <c:if test="${not empty branch}">
            <a class="management-btn management-btn-primary"
               href="${ctx}/manager/halls/create?branchId=${branch.id}">
                + Thêm phòng chiếu
            </a>
        </c:if>
    </div>

    <c:if test="${not empty sessionScope.flashMessage}">
        <div class="management-alert ${sessionScope.flashType == 'success' ? 'management-alert-success' : 'management-alert-error'}">
            <c:out value="${sessionScope.flashMessage}" />
        </div>

        <c:remove var="flashMessage" scope="session" />
        <c:remove var="flashType" scope="session" />
    </c:if>

    <c:if test="${not empty requestScope.errorMessage}">
        <div class="management-alert management-alert-error">
            <c:out value="${requestScope.errorMessage}" />
        </div>
    </c:if>

    <c:choose>
        <c:when test="${empty branch}">
            <div class="management-card">
                <div class="management-card-header">
                    <div>
                        <h2>Chưa được phân công chi nhánh</h2>
                        <span>Tài khoản Manager này chưa có chi nhánh để quản lý phòng chiếu.</span>
                    </div>
                </div>

                <div class="management-empty hall-empty-state">
                    <div class="hall-empty-icon">
                        <i class="bi bi-building-exclamation"></i>
                    </div>

                    <strong>Chưa thể quản lý phòng chiếu</strong>
                    <p>
                        Bạn chỉ có thể thêm, sửa hoặc quản lý phòng chiếu sau khi được Admin phân công Branch.
                    </p>
                </div>
            </div>
        </c:when>

        <c:otherwise>
            <div class="hall-branch-banner">
                <i class="bi bi-building-check-fill"></i>

                <div>
                    <strong>
                        Chi nhánh đang quản lý:
                        <c:out value="${branch.name}" />
                    </strong>

                    <span>
                        <i class="bi bi-geo-alt-fill"></i>
                        <c:out value="${branch.address}" />
                    </span>
                </div>
            </div>

            <div class="management-card">
                <div class="management-card-header">
                    <div>
                        <h2>Danh sách phòng chiếu</h2>
                        <span>
                            Quản lý các Hall thuộc chi nhánh
                            <strong><c:out value="${branch.name}" /></strong>
                        </span>
                    </div>

                    <input type="text"
                           id="hallSearch"
                           class="management-search"
                           placeholder="Tìm theo tên phòng, loại phòng, trạng thái...">
                </div>

                <div class="management-table-wrap">
                    <c:choose>
                        <c:when test="${empty halls}">
                            <div class="management-empty hall-empty-state">
                                <div class="hall-empty-icon">
                                    <i class="bi bi-door-closed"></i>
                                </div>

                                <strong>Chưa có phòng chiếu nào</strong>
                                <p>
                                    Chi nhánh này chưa có dữ liệu phòng chiếu. Hãy tạo phòng đầu tiên để tiếp tục phân bổ phim và lập lịch chiếu.
                                </p>

                                <a class="management-btn management-btn-primary hall-empty-action"
                                   href="${ctx}/manager/halls/create?branchId=${branch.id}">
                                    + Thêm phòng chiếu đầu tiên
                                </a>
                            </div>
                        </c:when>

                        <c:otherwise>
                            <table class="management-table hall-table" id="hallTable">
                                <thead>
                                    <tr>
                                        <th>ID</th>
                                        <th>Phòng chiếu</th>
                                        <th>Cấu hình ghế</th>
                                        <th>Loại phòng</th>
                                        <th>Trạng thái</th>
                                        <th>Thao tác</th>
                                    </tr>
                                </thead>

                                <tbody>
                                    <c:forEach var="h" items="${halls}">
                                        <tr>
                                            <td>#${h.id}</td>

                                            <td>
                                                <div class="management-user">
                                                    <div class="management-avatar">
                                                        <c:choose>
                                                            <c:when test="${not empty h.name}">
                                                                ${fn:substring(h.name, 0, 1)}
                                                            </c:when>
                                                            <c:otherwise>H</c:otherwise>
                                                        </c:choose>
                                                    </div>

                                                    <div>
                                                        <strong><c:out value="${h.name}" /></strong>
                                                        <span>RapViet Hall</span>
                                                    </div>
                                                </div>
                                            </td>

                                            <td>
                                                <div class="hall-seat-config">
                                                    <c:out value="${h.seatRows}" /> hàng ×
                                                    <c:out value="${h.seatsPerRow}" /> ghế
                                                </div>

                                                <div class="management-muted">
                                                    Tổng:
                                                    <strong><c:out value="${h.totalSeats}" /></strong>
                                                    ghế
                                                </div>
                                            </td>

                                            <td>
                                                <span class="hall-type-badge">
                                                    <i class="bi bi-stars"></i>
                                                    <c:out value="${h.hallType}" />
                                                </span>
                                            </td>

                                            <td>
                                                <c:choose>
                                                    <c:when test="${h.status == 'ACTIVE'}">
                                                        <span class="management-status management-status-active">
                                                            Hoạt động
                                                        </span>
                                                    </c:when>

                                                    <c:when test="${h.status == 'MAINTENANCE'}">
                                                        <span class="management-status management-status-warning">
                                                            Bảo trì
                                                        </span>
                                                    </c:when>

                                                    <c:otherwise>
                                                        <span class="management-status management-status-blocked">
                                                            Ngưng hoạt động
                                                        </span>
                                                    </c:otherwise>
                                                </c:choose>
                                            </td>

                                            <td>
                                                <div class="management-actions">
                                                    <a class="management-btn management-btn-small management-btn-ghost"
                                                       href="${ctx}/manager/halls/edit?id=${h.id}&branchId=${branch.id}">
                                                        Sửa
                                                    </a>

                                                    <form method="post"
                                                          action="${ctx}/manager/halls/status"
                                                          class="hall-action-form">
                                                        <input type="hidden" name="id" value="${h.id}">
                                                        <input type="hidden" name="branchId" value="${branch.id}">

                                                        <c:choose>
                                                            <c:when test="${h.status == 'ACTIVE'}">
                                                                <input type="hidden" name="status" value="MAINTENANCE">
                                                                <button type="submit"
                                                                        class="management-btn management-btn-small management-btn-ghost">
                                                                    Bảo trì
                                                                </button>
                                                            </c:when>

                                                            <c:otherwise>
                                                                <input type="hidden" name="status" value="ACTIVE">
                                                                <button type="submit"
                                                                        class="management-btn management-btn-small management-btn-success">
                                                                    Mở lại
                                                                </button>
                                                            </c:otherwise>
                                                        </c:choose>
                                                    </form>

                                                    <form method="post"
                                                          action="${ctx}/manager/halls/delete"
                                                          class="hall-action-form"
                                                          onsubmit="return confirm('Bạn có chắc muốn xóa phòng chiếu này?');">
                                                        <input type="hidden" name="id" value="${h.id}">
                                                        <input type="hidden" name="branchId" value="${branch.id}">

                                                        <button type="submit"
                                                                class="management-btn management-btn-small management-btn-danger">
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
        </c:otherwise>
    </c:choose>
</section>

<script>
    document.addEventListener('DOMContentLoaded', function () {
        var searchInput = document.getElementById('hallSearch');
        var hallTable = document.getElementById('hallTable');

        if (!searchInput || !hallTable) {
            return;
        }

        searchInput.addEventListener('input', function () {
            var keyword = searchInput.value.trim().toLowerCase();
            var rows = hallTable.querySelectorAll('tbody tr');

            rows.forEach(function (row) {
                var text = row.innerText.toLowerCase();
                row.style.display = text.includes(keyword) ? '' : 'none';
            });
        });
    });
</script>

</main>
</div>
</body>
</html>