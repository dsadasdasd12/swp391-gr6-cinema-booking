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

<style>
    /* Premium Table Redesign */
    .rv-table-premium {
        border-collapse: separate;
        border-spacing: 0 10px;
        width: 100%;
        margin-top: -10px;
    }
    .rv-table-premium thead th {
        background: transparent !important;
        border: none !important;
        font-size: 0.75rem;
        text-transform: uppercase;
        letter-spacing: 1px;
        color: #8b8fa8;
        font-weight: 600;
        padding: 0 1rem 0.5rem;
    }
    .rv-table-premium tbody tr {
        background: #1e2130;
        box-shadow: 0 4px 6px rgba(0,0,0,0.05);
        border-radius: 12px;
        transition: transform 0.2s ease, box-shadow 0.2s ease;
    }
    .rv-table-premium tbody tr:hover {
        transform: translateY(-2px);
        box-shadow: 0 6px 12px rgba(0,0,0,0.15);
        background: #252838;
    }
    .rv-table-premium tbody td {
        border: none !important;
        padding: 1rem;
        vertical-align: middle;
        color: #e4e6eb;
        font-size: 0.9rem;
    }
    .rv-table-premium tbody td:first-child {
        border-top-left-radius: 12px;
        border-bottom-left-radius: 12px;
    }
    .rv-table-premium tbody td:last-child {
        border-top-right-radius: 12px;
        border-bottom-right-radius: 12px;
    }
    
    /* Enhance the QR Thumbnail */
    .qr-thumb-box {
        width: 48px;
        height: 48px;
        background: #fff;
        border-radius: 8px;
        display: flex;
        align-items: center;
        justify-content: center;
        overflow: hidden;
        box-shadow: 0 2px 4px rgba(0,0,0,0.1);
        border: 2px solid rgba(255, 255, 255, 0.1);
    }
    .qr-thumb-img {
        width: 100%;
        height: 100%;
        object-fit: cover;
    }
    .qr-thumb-missing {
        background: rgba(229, 9, 20, 0.1);
        border-color: rgba(229, 9, 20, 0.3);
        color: #e50914;
        font-size: 1.25rem;
    }

    /* Customer Info cell */
    .customer-info {
        display: flex;
        flex-direction: column;
        gap: 2px;
    }
    .customer-name {
        font-weight: 600;
        font-size: 0.95rem;
        color: #fff;
        letter-spacing: 0.2px;
    }
    .customer-email {
        font-size: 0.8rem;
        color: #8b8fa8;
    }
    .movie-title-cell {
        font-weight: 500;
        color: #fff;
        font-size: 0.95rem;
    }
    .showtime-cell {
        font-size: 0.85rem;
        color: #adb5bd;
        background: rgba(255, 255, 255, 0.05);
        padding: 4px 10px;
        border-radius: 6px;
        display: inline-block;
        border: 1px solid rgba(255, 255, 255, 0.05);
    }
    .uuid-cell {
        font-family: 'SFMono-Regular', Consolas, 'Liberation Mono', Menlo, monospace;
        font-size: 0.8rem;
        color: #adb5bd;
        background: rgba(0, 0, 0, 0.2);
        padding: 3px 8px;
        border-radius: 4px;
        user-select: all;
    }
</style>

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

        <div class="d-flex align-items-center gap-2 ms-auto">
            <button type="submit" class="rv-btn rv-btn--secondary rv-btn--sm">
                Lọc dữ liệu
            </button>
            <a href="${ctx}/admin/tickets?action=list" class="rv-btn rv-btn--ghost rv-btn--sm">
                Xóa lọc
            </a>
            <button type="button" class="rv-btn rv-btn--primary rv-btn--sm" onclick="bulkGenerateQR()" id="btnBulkQR" title="Sinh QR cho tất cả vé chưa có mã">
                <i class="bi bi-qr-code me-1"></i>Tạo QR hàng loạt
            </button>
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
            <div class="rv-table-responsive" style="overflow-x: auto; padding-bottom: 1rem;">
                <table class="rv-table-premium" id="ticketTable">
                    <thead>
                        <tr>
                            <th class="text-center" style="width: 70px;">QR</th>
                            <th style="width: 120px;">Mã UUID</th>
                            <th>Khách hàng</th>
                            <th>Phim</th>
                            <th>Suất chiếu</th>
                            <th>Trạng thái</th>
                            <th>Ngày tạo</th>
                            <th class="text-center" style="width: 90px;">Thao tác</th>
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
                                            <div class="qr-thumb-box mx-auto">
                                                <img src="data:image/png;base64,${t.qrCodeBase64}"
                                                     alt="QR vé" class="qr-thumb-img">
                                            </div>
                                        </c:when>
                                        <c:otherwise>
                                            <div class="qr-thumb-box qr-thumb-missing mx-auto">
                                                <i class="bi bi-qr-code-scan"></i>
                                            </div>
                                        </c:otherwise>
                                    </c:choose>
                                </td>
                                <td>
                                    <span class="uuid-cell">
                                        <c:out value="${fn:substring(t.ticketUuid, 0, 8)}"/>
                                    </span>
                                </td>
                                <td>
                                    <div class="customer-info">
                                        <span class="customer-name"><c:out value="${t.customerName}"/></span>
                                        <span class="customer-email"><c:out value="${t.customerEmail}"/></span>
                                    </div>
                                </td>
                                <td class="movie-title-cell"><c:out value="${t.movieTitle}"/></td>
                                <td>
                                    <span class="showtime-cell"><c:out value="${t.showtimeStart}"/></span>
                                </td>
                                <td>
                                    <c:choose>
                                        <c:when test="${t.ticketStatus == 'ISSUED'}">
                                            <span class="rv-badge rv-badge--active">Đã phát hành</span>
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
    var CTX = '${ctx}';

(function () {
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

function bulkGenerateQR() {
    var btn = document.getElementById('btnBulkQR');
    if (!confirm('Sinh mã QR cho tất cả vé chưa có QR? Quá trình có thể mất vài giây.')) return;
    btn.disabled = true;
    btn.innerHTML = '<i class="bi bi-hourglass-split me-1"></i>Đang tạo...';
    fetch(CTX + '/admin/tickets', {
        method: 'POST',
        headers: { 'Content-Type': 'application/x-www-form-urlencoded' },
        body: 'action=bulk-generate'
    })
    .then(function (r) { return r.json(); })
    .then(function (data) {
        alert('Hoàn tất! Đã tạo QR cho ' + data.generated + ' vé. Thất bại: ' + data.failed);
        location.reload();
    })
    .catch(function () {
        btn.disabled = false;
        btn.innerHTML = '<i class="bi bi-qr-code me-1"></i>Tạo QR hàng loạt';
        alert('Lỗi kết nối.');
    });
}
</script>
</body>
</html>
