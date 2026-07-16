<%--
    RapViet Admin — Danh sách E-Ticket (ticket-list.jsp)
    Servlet: TicketController ?action=list
    (Long — )
--%>
<%@ page contentType="text/html; charset=UTF-8" pageEncoding="UTF-8" %>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<%@ taglib prefix="fn" uri="http://java.sun.com/jsp/jstl/functions" %>
<c:set var="ctx" value="${pageContext.request.contextPath}" />
<c:set var="pageTitle" value="E-Ticket &amp; QR Code — Rạp Việt CMS" scope="request" />

<%@ include file="/pages/shared/header-admin.jsp" %>
<%@ include file="/pages/shared/sidebar-admin.jsp" %>

<!-- ── PAGE HEADER ── -->
<div class="rv-page-header">
    <div class="rv-page-header__left">
        <div class="rv-breadcrumb">
            <a href="${ctx}/admin/dashboard">Dashboard</a>
            <i class="bi bi-chevron-right rv-breadcrumb__sep"></i>
            <span class="rv-breadcrumb__current">E-Ticket &amp; Mã QR</span>
        </div>
        <h1 class="rv-page-title">E-Ticket &amp; Mã QR</h1>
        <p class="rv-page-subtitle">
            Quản lý vé điện tử, mã QR check-in và trạng thái phát hành vé toàn hệ thống.
        </p>
    </div>
    <div class="rv-page-header__right d-flex flex-wrap gap-2 align-items-center">
        <span class="rv-badge rv-badge--active">
            <i class="bi bi-ticket-perforated-fill me-1"></i>
            Tổng: <strong class="ms-1">${totalItems}</strong> vé
        </span>
    </div>
</div>

<!-- ── FILTER TOOLBAR ── -->
<div class="rv-toolbar mb-4">
    <form method="get" action="${ctx}/admin/tickets" class="d-flex align-items-center flex-wrap gap-3 w-100" style="margin: 0; padding: 0; border: none; background: none;">
        <input type="hidden" name="action" value="list">

        <!-- Search Input -->
        <div class="rv-toolbar__search">
            <i class="bi bi-search"></i>
            <input type="text" name="keyword" placeholder="Tên KH, email, UUID, tên phim..."
                   value="<c:out value='${keyword}'/>"/>
        </div>

        <!-- Filter Status -->
        <div class="rv-toolbar__filter">
            <select name="status">
                <option value="">Tất cả trạng thái</option>
                <option value="ISSUED" ${status == 'ISSUED' ? 'selected' : ''}>Đã phát hành</option>
                <option value="USED" ${status == 'USED' ? 'selected' : ''}>Đã sử dụng</option>
                <option value="PENDING_MANUAL" ${status == 'PENDING_MANUAL' ? 'selected' : ''}>Chờ xử lý</option>
            </select>
        </div>

        <!-- Action buttons -->
        <div class="d-flex align-items-center gap-2 ms-auto">
            <button type="submit" class="rv-btn rv-btn--secondary rv-btn--sm">
                Lọc dữ liệu
            </button>
            <a href="${ctx}/admin/tickets?action=list" class="rv-btn rv-btn--ghost rv-btn--sm">
                Xóa lọc
            </a>
            <a href="javascript:location.reload();" class="rv-btn rv-btn--refresh" title="Làm mới">
                <i class="bi bi-arrow-clockwise"></i>
            </a>
        </div>
    </form>
</div>

<!-- ── DATA TABLE ── -->
<div class="rv-card shadow-sm">
    <div class="rv-card__header d-flex align-items-center justify-content-between flex-wrap gap-2">
        <span class="rv-card__title">
            <i class="bi bi-qr-code-scan me-2 text-primary"></i>Danh sách vé điện tử
        </span>
        <span class="text-muted small" id="visibleCount"></span>
    </div>
    <c:choose>
        <c:when test="${empty tickets}">
            <div class="rv-empty">
                <i class="bi bi-qr-code rv-empty__icon"></i>
                <div class="rv-empty__title">Chưa có vé nào</div>
                <div class="rv-empty__message">Hệ thống chưa phát hành e-ticket nào sau thanh toán.</div>
            </div>
        </c:when>
        <c:otherwise>
            <div class="rv-table-responsive">
                <table class="table table-hover table-striped align-middle mb-0 rv-table" id="ticketTable">
                    <thead class="table-light">
                        <tr>
                            <th class="text-center" style="width:72px;">QR</th>
                            <th>UUID Vé</th>
                            <th>Khách hàng</th>
                            <th>Phim</th>
                            <th>Suất chiếu</th>
                            <th>Trạng thái</th>
                            <th>Ngày tạo</th>
                            <th class="text-center" style="width:100px;">Thao tác</th>
                        </tr>
                    </thead>
                    <tbody id="ticketTbody">
                        <c:forEach var="t" items="${tickets}">
                            <tr class="ticket-row"
                                data-status="${t.ticketStatus}"
                                data-search="${fn:toLowerCase(t.customerName)} ${fn:toLowerCase(t.customerEmail)} ${fn:toLowerCase(t.ticketUuid)} ${fn:toLowerCase(t.movieTitle)}">
                                <td class="text-center">
                                    <c:choose>
                                        <c:when test="${not empty t.qrCodeBase64}">
                                            <img src="data:image/png;base64,${t.qrCodeBase64}"
                                                 alt="QR vé"
                                                 class="rounded border shadow-sm"
                                                 style="width:52px;height:52px;object-fit:contain;background:#fff;">
                                        </c:when>
                                        <c:otherwise>
                                            <div class="d-inline-flex align-items-center justify-content-center rounded border border-danger border-opacity-25 bg-danger bg-opacity-10"
                                                 style="width:52px;height:52px;">
                                                <i class="bi bi-exclamation-triangle text-danger"></i>
                                            </div>
                                        </c:otherwise>
                                    </c:choose>
                                </td>
                                <td>
                                    <code class="small text-muted user-select-all">
                                        <c:out value="${fn:substring(t.ticketUuid, 0, 8)}"/>…
                                    </code>
                                </td>
                                <td>
                                    <div class="fw-semibold"><c:out value="${t.customerName}"/></div>
                                    <div class="small text-muted"><c:out value="${t.customerEmail}"/></div>
                                </td>
                                <td><c:out value="${t.movieTitle}"/></td>
                                <td class="text-muted small"><c:out value="${t.showtimeStart}"/></td>
                                <td>
                                    <c:choose>
                                        <c:when test="${t.ticketStatus == 'ISSUED'}">
                                            <span class="rv-badge rv-badge--success">Đã phát hành</span>
                                        </c:when>
                                        <c:when test="${t.ticketStatus == 'USED'}">
                                            <span class="rv-badge rv-badge--ended">Đã sử dụng</span>
                                        </c:when>
                                        <c:when test="${t.ticketStatus == 'PENDING_MANUAL'}">
                                            <span class="rv-badge rv-badge--pending">Chờ xử lý</span>
                                        </c:when>
                                        <c:otherwise>
                                            <span class="rv-badge rv-badge--inactive"><c:out value="${t.statusLabel}"/></span>
                                        </c:otherwise>
                                    </c:choose>
                                </td>
                                <td class="text-muted small"><c:out value="${t.createdAtLabel}"/></td>
                                <td class="text-center">
                                    <div class="d-flex justify-content-center gap-1">
                                        <a href="${ctx}/admin/tickets?action=detail&amp;id=${t.bookingId > 0 ? t.bookingId : t.id}"
                                           class="rv-btn rv-btn--ghost rv-btn--icon"
                                           data-bs-toggle="tooltip" title="Xem chi tiết">
                                            <i class="bi bi-eye"></i>
                                        </a>
                                        <c:if test="${t.ticketStatus == 'PENDING_MANUAL'}">
                                            <button type="button"
                                                    class="rv-btn rv-btn--ghost rv-btn--icon btn-retry-qr text-warning"
                                                    data-booking-id="${t.bookingId > 0 ? t.bookingId : t.id}"
                                                    data-bs-toggle="tooltip" title="Thử lại sinh QR">
                                                <i class="bi bi-arrow-clockwise"></i>
                                            </button>
                                        </c:if>
                                    </div>
                                </td>
                            </tr>
                        </c:forEach>
                    </tbody>
                </table>
            </div>
        </c:otherwise>
    </c:choose>

    <div style="margin-top: 18px;">
        <c:if test="${totalPages > 1}">
            <jsp:include page="/pages/shared/pagination.jsp">
                <jsp:param name="currentPage" value="${currentPage}" />
                <jsp:param name="totalPages" value="${totalPages}" />
                <jsp:param name="totalItems" value="${totalItems}" />
                <jsp:param name="pageSize" value="${pageSize}" />
                <jsp:param name="baseUrl" value="${ctx}/admin/tickets?action=list&keyword=${param.keyword}&status=${param.status}" />
            </jsp:include>
        </c:if>
    </div>
</div>

<!-- Toast -->
<div class="toast-container position-fixed bottom-0 end-0 p-3" style="z-index:1080">
    <div id="qrToast" class="toast text-bg-dark border-0 shadow" role="alert" aria-live="assertive">
        <div class="d-flex">
            <div class="toast-body" id="qrToastMsg">...</div>
            <button type="button" class="btn-close btn-close-white me-2 m-auto" data-bs-dismiss="toast"></button>
        </div>
    </div>
</div>

</main>
</div>

<script>
(function () {
    var CTX = '${ctx}';

    document.querySelectorAll('[data-bs-toggle="tooltip"]').forEach(function (el) {
        new bootstrap.Tooltip(el);
    });

    document.querySelectorAll('.btn-retry-qr').forEach(function (btn) {
        btn.addEventListener('click', function () {
            var bookingId = this.dataset.bookingId;
            var self = this;
            self.disabled = true;
            self.innerHTML = '<span class="spinner-border spinner-border-sm"></span>';

            fetch(CTX + '/admin/tickets', {
                method: 'POST',
                headers: { 'Content-Type': 'application/x-www-form-urlencoded' },
                body: 'action=retry&bookingId=' + encodeURIComponent(bookingId)
            })
            .then(function (r) { return r.json(); })
            .then(function (data) {
                var toast = document.getElementById('qrToast');
                document.getElementById('qrToastMsg').textContent = data.message;
                new bootstrap.Toast(toast, { delay: 4000 }).show();
                if (data.success) {
                    setTimeout(function () { location.reload(); }, 1500);
                } else {
                    self.disabled = false;
                    self.innerHTML = '<i class="bi bi-arrow-clockwise"></i>';
                }
            })
            .catch(function () {
                self.disabled = false;
                self.innerHTML = '<i class="bi bi-arrow-clockwise"></i>';
                alert('Lỗi kết nối.');
            });
        });
    });
})();
</script>
</body>
</html>
