<%--
    Rạp Việt CMS — Customer Accounts Management
    URL: /admin/accounts/customers
    Servlet: CustomerAccountServlet
--%>
<%@ page contentType="text/html; charset=UTF-8" pageEncoding="UTF-8" %>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<%@ taglib prefix="fmt" uri="http://java.sun.com/jsp/jstl/fmt" %>
<%@ taglib prefix="fn" uri="http://java.sun.com/jsp/jstl/functions" %>
<c:set var="ctx" value="${pageContext.request.contextPath}" />
<c:set var="pageTitle" value="Quản lý khách hàng — Rạp Việt CMS" scope="request" />

<%@ include file="/pages/shared/header-admin.jsp" %>
<%@ include file="/pages/shared/sidebar-admin.jsp" %>

<!-- ── PAGE HEADER ── -->
<div class="rv-page-header">
    <div class="rv-page-header__left">
        <!-- Breadcrumb -->
        <div class="rv-breadcrumb">
            <a href="${ctx}/admin/dashboard">Dashboard</a>
            <i class="bi bi-chevron-right rv-breadcrumb__sep"></i>
            <span class="rv-breadcrumb__current">Tài khoản khách hàng</span>
        </div>
        <h1 class="rv-page-title">Quản lý khách hàng</h1>
        <p class="rv-page-subtitle">Theo dõi, kích hoạt hoặc khóa tài khoản thành viên hệ thống.</p>
    </div>
</div>

<!-- ── TOOLBAR / FILTERS ── -->
<div class="rv-toolbar">
    <form method="get" action="${ctx}/admin/accounts/customers" class="d-flex align-items-center flex-wrap gap-3 w-100" style="margin: 0; padding: 0; border: none; background: none;">
        <!-- Search Input -->
        <div class="rv-toolbar__search">
            <i class="bi bi-search"></i>
            <input type="text" name="keyword" placeholder="Tìm tên hoặc email khách hàng..." value="<c:out value='${param.keyword}'/>">
        </div>

        <!-- Filter Status -->
        <div class="rv-toolbar__filter">
            <select name="status">
                <option value="">Tất cả trạng thái</option>
                <option value="ACTIVE" ${param.status == 'ACTIVE' ? 'selected' : ''}>Hoạt động (Active)</option>
                <option value="BLOCKED" ${param.status == 'BLOCKED' ? 'selected' : ''}>Đang khóa (Blocked)</option>
                <option value="PENDING" ${param.status == 'PENDING' ? 'selected' : ''}>Chờ xác thực (Pending)</option>
            </select>
        </div>

        <!-- Action buttons -->
        <div class="d-flex align-items-center gap-2 ms-auto">

            <button type="submit" class="rv-btn rv-btn--secondary rv-btn--sm">
                Lọc dữ liệu
            </button>
            <a href="${ctx}/admin/accounts/customers" class="rv-btn rv-btn--ghost rv-btn--sm">
                Xóa lọc
            </a>
            <a href="javascript:location.reload();" class="rv-btn rv-btn--refresh" title="Làm mới">
                <i class="bi bi-arrow-clockwise"></i>
            </a>
        </div>
    </form>
</div>



<!-- ── CUSTOMERS TABLE ── -->
<div class="rv-card">
    <c:choose>
        <c:when test="${empty customers}">
            <div class="rv-empty">
                <i class="bi bi-people rv-empty__icon"></i>
                <div class="rv-empty__title">Không tìm thấy khách hàng</div>
                <div class="rv-empty__message">Hệ thống chưa ghi nhận tài khoản khách hàng nào khớp với điều kiện tìm kiếm.</div>
            </div>
        </c:when>
        <c:otherwise>
            <div class="rv-table-responsive">
                <table class="rv-table">
                    <thead>
                        <tr>
                            <th style="width: 60px; text-align: center;">No.</th>
                            <th>
                                <a href="?keyword=${param.keyword}&status=${param.status}&pageSize=${pageSize}&sortField=fullName&sortOrder=${sortField == 'fullName' && sortOrder == 'ASC' ? 'DESC' : 'ASC'}" style="color: inherit; text-decoration: none;">
                                    Họ và tên <i class="bi bi-arrow-${sortField == 'fullName' ? (sortOrder == 'ASC' ? 'up' : 'down') : 'down-up'} ms-1"></i>
                                </a>
                            </th>
                            <th>
                                <a href="?keyword=${param.keyword}&status=${param.status}&pageSize=${pageSize}&sortField=email&sortOrder=${sortField == 'email' && sortOrder == 'ASC' ? 'DESC' : 'ASC'}" style="color: inherit; text-decoration: none;">
                                    Địa chỉ Email <i class="bi bi-arrow-${sortField == 'email' ? (sortOrder == 'ASC' ? 'up' : 'down') : 'down-up'} ms-1"></i>
                                </a>
                            </th>
                            <th>Số điện thoại</th>
                            <th>
                                <a href="?keyword=${param.keyword}&status=${param.status}&pageSize=${pageSize}&sortField=createdAt&sortOrder=${sortField == 'createdAt' && sortOrder == 'ASC' ? 'DESC' : 'ASC'}" style="color: inherit; text-decoration: none;">
                                    Ngày tham gia <i class="bi bi-arrow-${sortField == 'createdAt' ? (sortOrder == 'ASC' ? 'up' : 'down') : 'down-up'} ms-1"></i>
                                </a>
                            </th>
                            <th>Trạng thái</th>
                            <th style="width: 320px; text-align: center;">Thao tác hành động</th>
                        </tr>
                    </thead>
                    <tbody>
                        <c:forEach var="c" items="${customers}" varStatus="status">
                            <tr>

                                <!-- No. -->
                                <td style="text-align: center; font-weight: 600; color: var(--n-500);">
                                    ${status.index + 1}
                                </td>

                                <!-- Full Name -->
                                <td>
                                    <div style="font-weight: 600; color: var(--n-900); display: flex; align-items: center; gap: 8px;">
                                        <i class="bi bi-person-circle" style="color: var(--n-400); font-size: 16px;"></i>
                                        <c:out value="${c.fullName}"/>
                                    </div>
                                </td>

                                <!-- Email -->
                                <td>
                                    <c:out value="${c.email}"/>
                                </td>

                                <!-- Phone -->
                                <td>
                                    <c:out value="${not empty c.phone ? c.phone : '—'}"/>
                                </td>

                                <!-- Created Date -->
                                <td>
                                    <c:choose>
                                        <c:when test="${not empty c.createdAtLabel}">
                                            <fmt:parseDate value="${c.createdAtLabel}" pattern="yyyy-MM-dd'T'HH:mm" var="parsedDate" type="both" />
                                            <fmt:formatDate value="${parsedDate}" pattern="dd/MM/yyyy HH:mm" />
                                        </c:when>
                                        <c:otherwise>—</c:otherwise>
                                    </c:choose>
                                </td>

                                <!-- Status Badge -->
                                <td>
                                    <c:choose>
                                        <c:when test="${c.status == 'ACTIVE'}">
                                            <span class="rv-badge rv-badge--active">Hoạt động</span>
                                        </c:when>
                                        <c:when test="${c.status == 'BLOCKED'}">
                                            <span class="rv-badge rv-badge--blocked">Bị khóa</span>
                                        </c:when>
                                        <c:otherwise>
                                            <span class="rv-badge rv-badge--pending">Chờ xác thực</span>
                                        </c:otherwise>
                                    </c:choose>
                                </td>

                                <!-- Actions -->
                                <td>
                                    <div class="d-flex align-items-center justify-content-center gap-2">
                                        <!-- Booking History Button -->
                                        <a href="${ctx}/admin/accounts/customers?action=detail&id=${c.id}#booking-history" class="rv-btn rv-btn--secondary rv-btn--sm" style="font-size: 12px; padding: 4px 10px; height: 28px;">
                                            <i class="bi bi-card-list"></i>Lịch sử đặt vé
                                        </a>

                                        <!-- Lock/Unlock post request forms wrapped inside styled links -->
                                        <c:choose>
                                            <c:when test="${c.status == 'ACTIVE' || c.status == 'PENDING'}">
                                                <form method="post" action="${ctx}/admin/accounts/customers" style="margin: 0; padding: 0;">
                                                    <input type="hidden" name="action" value="block">
                                                    <input type="hidden" name="id" value="${c.id}">
                                                    <button type="submit" class="rv-btn rv-btn--danger rv-btn--sm" style="font-size: 12px; padding: 4px 10px; height: 28px;"
                                                            data-confirm
                                                            data-confirm-title="Khóa tài khoản?"
                                                            data-confirm-message="Khóa tài khoản khách hàng '<strong>${c.fullName}</strong>'? Họ sẽ không thể đăng nhập hoặc đặt vé trực tuyến."
                                                            data-confirm-type="danger"
                                                            data-confirm-text="Khóa tài khoản">
                                                        <i class="bi bi-lock-fill"></i>Khóa
                                                    </button>
                                                </form>
                                            </c:when>
                                            <c:otherwise>
                                                <form method="post" action="${ctx}/admin/accounts/customers" style="margin: 0; padding: 0;">
                                                    <input type="hidden" name="action" value="unblock">
                                                    <input type="hidden" name="id" value="${c.id}">
                                                    <button type="submit" class="rv-btn rv-btn--success rv-btn--sm" style="font-size: 12px; padding: 4px 10px; height: 28px;"
                                                            data-confirm
                                                            data-confirm-title="Mở khóa tài khoản?"
                                                            data-confirm-message="Mở khóa tài khoản khách hàng '<strong>${c.fullName}</strong>' để khôi phục quyền đăng nhập và đặt vé?"
                                                            data-confirm-type="success"
                                                            data-confirm-text="Mở khóa">
                                                        <i class="bi bi-unlock-fill"></i>Mở khóa
                                                    </button>
                                                </form>
                                            </c:otherwise>
                                        </c:choose>

                                        </div>
                                    </td>
                            </tr>
                        </c:forEach>
                    </tbody>
                </table>
            </div>

            <!-- Pagination inclusion -->
            <jsp:include page="/pages/shared/pagination.jsp">
                <jsp:param name="currentPage" value="${not empty currentPage ? currentPage : 1}" />
                <jsp:param name="totalPages" value="${not empty totalPages ? totalPages : 1}" />
                <jsp:param name="totalItems" value="${not empty totalItems ? totalItems : fn:length(customers)}" />
                <jsp:param name="pageSize" value="${not empty pageSize ? pageSize : 10}" />
                <jsp:param name="baseUrl" value="${ctx}/admin/accounts/customers?keyword=${param.keyword}&status=${param.status}&sortField=${sortField}&sortOrder=${sortOrder}" />
            </jsp:include>
        </c:otherwise>
    </c:choose>
</div>

</main>
</div>



</body>
</html>
