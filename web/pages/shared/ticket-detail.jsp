<%--
    RapViet — Chi tiết E-Ticket (ticket-detail.jsp)
    Dùng chung cho cả admin (qua TicketController) và customer (qua email link).
    Servlet: TicketController ?action=detail&id=X
    (Long — )
--%>
<%@ page contentType="text/html; charset=UTF-8" pageEncoding="UTF-8" %>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<c:set var="ctx" value="${pageContext.request.contextPath}" />
<c:set var="pageTitle" value="E-Ticket #${ticket.bookingId} — RapViet" scope="request" />

<%@ include file="/pages/shared/header-admin.jsp" %>

<c:choose>
    <c:when test="${not empty sessionScope.adminUser}">
        <%@ include file="/pages/shared/sidebar-admin.jsp" %>
    </c:when>
    <c:otherwise>
        <main class="rv-main" style="margin-left: 0; padding: 2rem; max-width: 1200px; margin: 0 auto; min-height: 100vh; background: var(--bg-body, #141620);">
    </c:otherwise>
</c:choose>

<style>
    .ticket-container {
        max-width: 450px;
        margin: 1.5rem auto;
    }
    .premium-ticket {
        background: #1e2130;
        border-radius: 24px;
        box-shadow: 0 20px 40px rgba(0, 0, 0, 0.45);
        border: 1px solid rgba(255, 255, 255, 0.08);
        overflow: hidden;
        position: relative;
        font-family: 'Inter', sans-serif;
        color: #fff;
    }
    
    /* Ticket notch punch cutouts on sides */
    .premium-ticket::before,
    .premium-ticket::after {
        content: '';
        position: absolute;
        width: 24px;
        height: 24px;
        background: #141620; /* Match parent body background */
        border-radius: 50%;
        top: 360px; /* Position exactly at the perforation line */
        z-index: 10;
        box-shadow: inset 0 0 6px rgba(0,0,0,0.5);
    }
    .premium-ticket::before {
        left: -12px;
    }
    .premium-ticket::after {
        right: -12px;
    }
    
    .ticket-header {
        padding: 2.25rem 2rem 1.5rem;
        text-align: center;
        background: linear-gradient(to bottom, rgba(229, 9, 20, 0.15), transparent);
    }
    .ticket-brand {
        font-family: 'Outfit', sans-serif;
        font-weight: 800;
        font-size: 1.65rem;
        color: var(--primary, #e50914);
        display: flex;
        align-items: center;
        justify-content: center;
        gap: 0.5rem;
        text-transform: uppercase;
        letter-spacing: 1.5px;
    }
    .ticket-brand i {
        font-size: 1.45rem;
    }
    .ticket-type {
        font-size: 0.72rem;
        color: var(--clr-muted, #8b8fa8);
        text-transform: uppercase;
        letter-spacing: 2px;
        margin-top: 0.35rem;
        font-weight: 600;
    }
    
    .ticket-body {
        padding: 0 2rem;
        text-align: center;
    }
    .qr-box {
        background: #fff;
        padding: 14px;
        border-radius: 18px;
        display: inline-block;
        box-shadow: 0 10px 25px rgba(0,0,0,0.3);
        margin: 1.25rem 0;
        border: 1px solid rgba(255, 255, 255, 0.1);
        transition: transform 0.3s ease;
    }
    .qr-box:hover {
        transform: scale(1.04);
    }
    .qr-img {
        width: 190px;
        height: 190px;
        display: block;
        border-radius: 8px;
    }
    .perforation {
        border: none;
        border-top: 2px dashed rgba(255, 255, 255, 0.15);
        height: 0;
        margin: 1.5rem 0;
        position: relative;
    }
    .ticket-info-grid {
        display: grid;
        grid-template-columns: 1fr 1fr;
        gap: 1.25rem 1rem;
        padding: 0 2.25rem 2rem;
        text-align: left;
    }
    .info-item-label {
        font-size: 0.68rem;
        color: #8b8fa8;
        text-transform: uppercase;
        letter-spacing: 0.8px;
        margin-bottom: 0.25rem;
        font-weight: 500;
    }
    .info-item-value {
        font-size: 0.95rem;
        font-weight: 600;
        color: #fff;
    }
    
    .ticket-footer {
        background: rgba(255, 255, 255, 0.02);
        padding: 1.5rem 2.25rem 2.25rem;
        text-align: center;
        border-top: 1px solid rgba(255, 255, 255, 0.04);
        font-size: 0.72rem;
        color: #8b8fa8;
        line-height: 1.6;
    }
    .barcode-container {
        margin-top: 1.25rem;
        height: 38px;
        background: linear-gradient(90deg, 
            #fff 0%, #fff 2%, 
            #000 2%, #000 4%, 
            #fff 4%, #fff 5%, 
            #000 5%, #000 9%, 
            #fff 9%, #fff 10%, 
            #000 10%, #000 11%, 
            #fff 11%, #fff 14%, 
            #000 14%, #000 15%, 
            #fff 15%, #fff 18%, 
            #000 18%, #000 22%, 
            #fff 22%, #fff 23%, 
            #000 23%, #000 24%, 
            #fff 24%, #fff 27%, 
            #000 27%, #000 28%, 
            #fff 28%, #fff 30%, 
            #000 30%, #000 35%, 
            #fff 35%, #fff 37%, 
            #000 37%, #000 38%, 
            #fff 38%, #fff 42%, 
            #000 42%, #000 45%, 
            #fff 45%, #fff 48%, 
            #000 48%, #000 49%, 
            #fff 49%, #fff 52%, 
            #000 52%, #000 55%, 
            #fff 55%, #fff 57%, 
            #000 57%, #000 59%, 
            #fff 59%, #fff 62%, 
            #000 62%, #000 66%, 
            #fff 66%, #fff 68%, 
            #000 68%, #000 70%, 
            #fff 70%, #fff 72%, 
            #000 72%, #000 75%, 
            #fff 75%, #fff 78%, 
            #000 78%, #000 82%, 
            #fff 82%, #fff 83%, 
            #000 83%, #000 86%, 
            #fff 86%, #fff 88%, 
            #000 88%, #000 90%, 
            #fff 90%, #fff 94%, 
            #000 94%, #000 97%, 
            #fff 97%, #fff 100%
        );
        border-radius: 4px;
        opacity: 0.75;
    }
</style>

<!-- Breadcrumb Header (Only for Admin View) -->
<c:if test="${not empty sessionScope.adminUser}">
    <div class="rv-page-header" style="margin-bottom: 1.5rem;">
        <div class="rv-page-header__left">
            <div class="rv-breadcrumb">
                <a href="${ctx}/admin/dashboard">Dashboard</a>
                <i class="bi bi-chevron-right rv-breadcrumb__sep"></i>
                <a href="${ctx}/admin/tickets">E-Ticket &amp; QR</a>
                <i class="bi bi-chevron-right rv-breadcrumb__sep"></i>
                <span class="rv-breadcrumb__current">Chi tiết vé</span>
            </div>
            <h1 class="rv-page-title">Xem chi tiết E-Ticket</h1>
        </div>
    </div>
</c:if>

<div class="row g-4 justify-content-center">
    <div class="col-lg-6">
        <div class="ticket-container">
            <!-- ── Physical Styled E-Ticket ── -->
            <div class="premium-ticket" id="ticketCard">
                <!-- Header -->
                <div class="ticket-header">
                    <div class="ticket-brand">
                        <i class="bi bi-film"></i> Rạp Việt Cinema
                    </div>
                    <div class="ticket-type">E-Ticket — Vé điện tử</div>
                </div>

                <!-- Body (QR Code & Status) -->
                <div class="ticket-body">
                    <div class="qr-box">
                        <c:choose>
                            <c:when test="${not empty ticket.qrCodeBase64}">
                                <img src="data:image/png;base64,${ticket.qrCodeBase64}"
                                     alt="QR Code" class="qr-img">
                            </c:when>
                            <c:otherwise>
                                <div style="width:190px;height:190px;border-radius:8px;background:rgba(229,9,20,.05);
                                            border:2px dashed rgba(229,9,20,.25);
                                            display:flex;flex-direction:column;align-items:center;justify-content:center;
                                            color:var(--primary, #e50914);gap:.5rem;margin:0 auto;">
                                    <i class="bi bi-exclamation-triangle" style="font-size:2.8rem;"></i>
                                    <div style="font-size:.85rem;font-weight:600;">QR chưa sẵn sàng</div>
                                    <div style="font-size:.7rem;color:#8b8fa8;">Đang chờ xử lý</div>
                                </div>
                            </c:otherwise>
                        </c:choose>
                    </div>
                    
                    <div style="margin-top: 0.5rem; margin-bottom: 1.25rem;">
                        <span class="rv-badge rv-badge--${ticket.statusBadgeClass}" style="font-size: 13px; padding: 5px 15px; font-weight: 600;">
                            <c:out value="${ticket.statusLabel}"/>
                        </span>
                    </div>
                </div>

                <!-- Perforation Line -->
                <div class="perforation"></div>

                <!-- Info Grid -->
                <div class="ticket-info-grid">
                    <div>
                        <div class="info-item-label">Mã vé (UUID)</div>
                        <code class="info-item-value" style="font-size: 0.8rem; word-break: break-all; color: var(--accent, #ff6b35);">
                            <c:out value="${ticket.ticketUuid}"/>
                        </code>
                    </div>
                    <div>
                        <div class="info-item-label">Mã đặt vé (ID)</div>
                        <div class="info-item-value" style="font-size: 1.1rem; color: #fff;">#<c:out value="${ticket.bookingId}"/></div>
                    </div>
                    <div>
                        <div class="info-item-label">Khách hàng</div>
                        <div class="info-item-value"><c:out value="${ticket.customerName}" default="—"/></div>
                    </div>
                    <div>
                        <div class="info-item-label">Email liên hệ</div>
                        <div class="info-item-value" style="font-size: 0.85rem; font-weight: 400; color: #8b8fa8; overflow: hidden; text-overflow: ellipsis; white-space: nowrap;"><c:out value="${ticket.customerEmail}" default="—"/></div>
                    </div>
                    <div>
                        <div class="info-item-label">Phim chiếu</div>
                        <div class="info-item-value" style="color: #ffb703; font-weight: 700;"><c:out value="${ticket.movieTitle}" default="—"/></div>
                    </div>
                    <div>
                        <div class="info-item-label">Suất chiếu</div>
                        <div class="info-item-value" style="font-size: 0.88rem;"><c:out value="${ticket.showtimeStart}" default="—"/></div>
                    </div>
                </div>

                <!-- Footer note with barcode -->
                <div class="ticket-footer">
                    <div>Xuất trình mã QR tại quầy soát vé để vào phòng chiếu.</div>
                    <div style="font-size: 0.65rem; margin-top: 0.25rem;">Mỗi mã QR chỉ sử dụng một lần duy nhất.</div>
                    <div class="barcode-container"></div>
                </div>
            </div>

            <!-- Action buttons -->
            <div class="d-flex gap-3 mt-4 justify-content-center">
                <button class="rv-btn rv-btn--primary" onclick="printTicket()" id="btnPrint">
                    <i class="bi bi-printer-fill" style="margin-right: 6px;"></i>In vé
                </button>
                <c:if test="${not empty sessionScope.adminUser}">
                    <a href="${ctx}/admin/tickets?action=list" class="rv-btn rv-btn--ghost">
                        Quay lại danh sách
                    </a>
                </c:if>
                <c:if test="${ticket.ticketStatus == 'PENDING_MANUAL'}">
                    <button class="rv-btn rv-btn--primary" id="btnRetryQr"
                            style="background:#ffc107;color:#000;" data-uuid="${ticket.ticketUuid}">
                        <i class="bi bi-arrow-clockwise" style="margin-right: 6px;"></i>Thử lại QR
                    </button>
                </c:if>
            </div>
        </div>
    </div>
</div>

</main>
</div>

<script src="https://cdn.jsdelivr.net/npm/bootstrap@5.3.3/dist/js/bootstrap.bundle.min.js"
        integrity="sha384-YvpcrYf0tY3lHB60NNkmXc5s9fDVZLESaAA55NDzOxhy9GkcIdslK1eN7N6jIeHz"
        crossorigin="anonymous"></script>
<script>
// ── Print ticket ──────────────────────────────────────────────
function printTicket() {
    var card = document.getElementById('ticketCard');
    var w = window.open('', '_blank');
    w.document.write('<html><head><title>E-Ticket Rạp Việt</title>');
    w.document.write('<style>body{font-family:Inter,sans-serif;padding:2rem;background:#141620;color:#fff;}');
    w.document.write('.premium-ticket{background:#1e2130;border-radius:24px;border:1px solid rgba(255,255,255,0.08);overflow:hidden;position:relative;color:#fff;max-width:450px;margin:0 auto;}');
    w.document.write('.ticket-header{padding:2.25rem 2rem 1.5rem;text-align:center;background:linear-gradient(to bottom,rgba(229,9,20,0.15),transparent);}');
    w.document.write('.ticket-brand{font-weight:800;font-size:1.65rem;color:#e50914;text-transform:uppercase;letter-spacing:1.5px;}');
    w.document.write('.ticket-type{font-size:0.72rem;color:#8b8fa8;text-transform:uppercase;letter-spacing:2px;margin-top:0.35rem;}');
    w.document.write('.ticket-body{padding:0 2rem;text-align:center;}');
    w.document.write('.qr-box{background:#fff;padding:14px;border-radius:18px;display:inline-block;margin:1.25rem 0;}');
    w.document.write('.qr-img{width:190px;height:190px;display:block;border-radius:8px;}');
    w.document.write('.perforation{border-top:2px dashed rgba(255,255,255,0.15);margin:1.5rem 0;}');
    w.document.write('.ticket-info-grid{display:grid;grid-template-columns:1fr 1fr;gap:1.25rem 1rem;padding:0 2.25rem 2rem;text-align:left;}');
    w.document.write('.info-item-label{font-size:0.68rem;color:#8b8fa8;text-transform:uppercase;letter-spacing:0.8px;margin-bottom:0.25rem;}');
    w.document.write('.info-item-value{font-size:0.95rem;font-weight:600;color:#fff;}');
    w.document.write('.ticket-footer{background:rgba(255,255,255,0.02);padding:1.5rem 2.25rem 2.25rem;text-align:center;font-size:0.72rem;color:#8b8fa8;line-height:1.6;}');
    w.document.write('.barcode-container{margin-top:1.25rem;height:38px;background:linear-gradient(90deg,#fff 0%,#fff 2%,#000 2%,#000 4%,#fff 4%,#fff 5%,#000 5%,#000 9%,#fff 9%,#fff 10%,#000 10%,#000 11%,#fff 11%,#fff 14%,#000 14%,#000 15%,#fff 15%,#fff 18%,#000 18%,#000 22%,#fff 22%,#fff 23%,#000 23%,#000 24%,#fff 24%,#fff 27%,#000 27%,#000 28%,#fff 28%,#fff 30%,#000 30%,#000 35%,#fff 35%,#fff 37%,#000 37%,#000 38%,#fff 38%,#fff 42%,#000 42%,#000 45%,#fff 45%,#fff 48%,#000 48%,#000 49%,#fff 49%,#fff 52%,#000 52%,#000 55%,#fff 55%,#fff 57%,#000 57%,#000 59%,#fff 59%,#fff 62%,#000 62%,#000 66%,#fff 66%,#fff 68%,#000 68%,#000 70%,#fff 70%,#fff 72%,#000 72%,#000 75%,#fff 75%,#fff 78%,#000 78%,#000 82%,#fff 82%,#fff 83%,#000 83%,#000 86%,#fff 86%,#fff 88%,#000 88%,#000 90%,#fff 90%,#fff 94%,#000 94%,#000 97%,#fff 97%,#fff 100%);border-radius:4px;}');
    w.document.write('</style></head><body>');
    w.document.write(card.innerHTML);
    w.document.write('</body></html>');
    w.document.close();
    w.print();
}

// ── Retry QR ──────────────────────────────────────────────────
var retryBtn = document.getElementById('btnRetryQr');
if (retryBtn) {
    retryBtn.addEventListener('click', function() {
        var uuid = this.dataset.uuid;
        this.disabled = true;
        this.innerHTML = '<i class="bi bi-hourglass-split"></i> Đang thử...';
        fetch('${ctx}/admin/tickets', {
            method: 'POST',
            headers: { 'Content-Type': 'application/x-www-form-urlencoded' },
            body: 'action=retry&uuid=' + encodeURIComponent(uuid)
        })
        .then(function(r) { return r.json(); })
        .then(function(data) {
            alert(data.message);
            if (data.success) location.reload();
            else { retryBtn.disabled = false; retryBtn.innerHTML = '<i class="bi bi-arrow-clockwise"></i> Thử lại QR'; }
        })
        .catch(function() { alert('Lỗi kết nối.'); retryBtn.disabled = false; });
    });
}
</script>
</body>
</html>
