<%--
    Document   : branch-list
    Purpose    : Branch Management - RapViet Cinema Admin
--%>

<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<%@ taglib prefix="fn" uri="http://java.sun.com/jsp/jstl/functions" %>
<%@ page contentType="text/html; charset=UTF-8" pageEncoding="UTF-8" %>

<c:set var="ctx" value="${pageContext.request.contextPath}" />
<c:set var="pageTitle" value="Quản lý chi nhánh — Rạp Việt CMS" scope="request" />

<c:set var="extraCss" scope="request">
    <link rel="stylesheet" href="${ctx}/assets/css/admin/branchmanagement.css?v=2">
</c:set>

<%@ include file="/pages/shared/header-admin.jsp" %>
<%@ include file="/pages/shared/sidebar-admin.jsp" %>

<section class="management-page branch-list-page">
    <div class="management-header">
        <div>
            <h1>Quản lý chi nhánh</h1>
            <p>Thêm, chỉnh sửa, xóa, cập nhật trạng thái và giờ hoạt động của chi nhánh.</p>
        </div>

        <a class="management-btn management-btn-primary"
           href="${ctx}/admin/branches/create">
            + Thêm chi nhánh
        </a>
    </div>

    <c:if test="${not empty sessionScope.flashMessage}">
        <div class="management-alert ${sessionScope.flashType == 'success' ? 'management-alert-success' : 'management-alert-error'}">
            <c:out value="${sessionScope.flashMessage}" />
        </div>

        <c:remove var="flashMessage" scope="session" />
        <c:remove var="flashType" scope="session" />
    </c:if>

    <div class="management-card">
        <div class="management-card-header">
            <div>
                <h2>Danh sách chi nhánh</h2>
                <span>Quản lý toàn bộ chi nhánh trong hệ thống RapViet Cinema</span>
            </div>

            <input type="text"
                   id="branchSearch"
                   class="management-search"
                   placeholder="Tìm theo tên, địa chỉ, số điện thoại...">
        </div>

        <div class="management-table-wrap">
            <c:choose>
                <c:when test="${empty branches}">
                    <div class="management-empty">
                        Chưa có chi nhánh nào trong hệ thống.
                    </div>
                </c:when>

                <c:otherwise>
                    <table class="management-table" id="branchTable">
                        <thead>
                            <tr>
                                <th>ID</th>
                                <th>Chi nhánh</th>
                                <th>Địa chỉ</th>
                                <th>Số điện thoại</th>
                                <th>Giờ hoạt động</th>
                                <th>Trạng thái</th>
                                <th>Thao tác</th>
                            </tr>
                        </thead>

                        <tbody>
                            <c:forEach var="b" items="${branches}">
                                <tr>
                                    <td>#${b.id}</td>

                                    <td>
                                        <div class="management-user">
                                            <div class="management-avatar">
                                                <c:choose>
                                                    <c:when test="${not empty b.name}">
                                                        ${fn:substring(b.name, 0, 1)}
                                                    </c:when>
                                                    <c:otherwise>B</c:otherwise>
                                                </c:choose>
                                            </div>

                                            <div>
                                                <strong><c:out value="${b.name}" /></strong>
                                                <span>RapViet Branch</span>
                                            </div>
                                        </div>
                                    </td>

                                    <td>
                                        <c:out value="${b.address}" />
                                    </td>

                                    <td>
                                        <c:choose>
                                            <c:when test="${empty b.phone}">
                                                <span class="management-muted">Chưa cập nhật</span>
                                            </c:when>
                                            <c:otherwise>
                                                <c:out value="${b.phone}" />
                                            </c:otherwise>
                                        </c:choose>
                                    </td>

                                    <td>
                                        <c:choose>
                                            <c:when test="${not empty b.openTime and not empty b.closeTime}">
                                                ${b.openTime} - ${b.closeTime}
                                            </c:when>
                                            <c:otherwise>
                                                <span class="management-muted">Chưa cập nhật</span>
                                            </c:otherwise>
                                        </c:choose>
                                    </td>

                                    <td>
                                        <c:choose>
                                            <c:when test="${b.status == 'ACTIVE'}">
                                                <span class="management-status management-status-active">
                                                    Hoạt động
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
                                               href="${ctx}/admin/branches/edit?id=${b.id}">
                                                Sửa
                                            </a>

                                            <form method="post"
                                                  action="${ctx}/admin/branches/status"
                                                  class="branch-action-form">
                                                <input type="hidden" name="id" value="${b.id}">

                                                <c:choose>
                                                    <c:when test="${b.status == 'ACTIVE'}">
                                                        <input type="hidden" name="status" value="INACTIVE">
                                                        <button type="submit"
                                                                class="management-btn management-btn-small management-btn-danger">
                                                            Ngưng
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
                                                  action="${ctx}/admin/branches/delete"
                                                  class="branch-action-form"
                                                  onsubmit="return confirm('Bạn có chắc muốn xóa chi nhánh này?');">
                                                <input type="hidden" name="id" value="${b.id}">

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
</section>

<script>
    document.addEventListener('DOMContentLoaded', function () {
        var searchInput = document.getElementById('branchSearch');
        var branchTable = document.getElementById('branchTable');

        if (!searchInput || !branchTable) {
            return;
        }

        searchInput.addEventListener('input', function () {
            var keyword = searchInput.value.trim().toLowerCase();
            var rows = branchTable.querySelectorAll('tbody tr');

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