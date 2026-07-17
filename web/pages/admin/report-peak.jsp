<%--
    RapViet Admin — Báo cáo giờ cao điểm (report-peak.jsp)
    Servlet: ReportController ?type=peak
    (Long — )
--%>
<%@ page contentType="text/html; charset=UTF-8" pageEncoding="UTF-8" %>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<%@ taglib prefix="fmt" uri="http://java.sun.com/jsp/jstl/fmt" %>
<%@ taglib prefix="fn" uri="http://java.sun.com/jsp/jstl/functions" %>
<c:set var="ctx"       value="${pageContext.request.contextPath}" />
<c:set var="pageTitle" value="Báo cáo giờ cao điểm — RapViet Admin" scope="request" />

<%@ include file="/pages/shared/header-admin.jsp" %>
<%@ include file="/pages/shared/sidebar-admin.jsp" %>

<!-- Tiêu đề trang & Tabs -->
<div class="rv-page-header">
    <div class="rv-page-header__left">
        <!-- Breadcrumb -->
        <div class="rv-breadcrumb">
            <a href="${ctx}/admin/dashboard">Dashboard</a>
            <i class="bi bi-chevron-right rv-breadcrumb__sep"></i>
            <span class="rv-breadcrumb__current">Báo cáo &amp; Phân tích</span>
        </div>
        <h1 class="rv-page-title">Phân tích giờ cao điểm</h1>
        <p class="rv-page-subtitle">Phân bổ lượng khách hàng đặt vé theo các khung giờ trong ngày.</p>
    </div>
    <div class="rv-page-header__right">
        <a href="${ctx}/admin/reports/system" class="rv-btn rv-btn--ghost" style="height: 38px;">
            <i class="bi bi-arrow-left"></i>Quay lại
        </a>
        <a href="${ctx}/admin/reports?type=peak&action=export&fromDate=${report.fromDate}&toDate=${report.toDate}" class="rv-btn rv-btn--secondary">
            <i class="bi bi-file-earmark-spreadsheet-fill"></i>Xuất Excel (XLSX)
        </a>
    </div>
</div>

<!-- ── TABS NAVIGATION ── -->
<div class="rv-tabs">
    <a href="${ctx}/admin/reports?type=revenue" class="rv-tab">Doanh thu dòng tiền</a>
    <a href="${ctx}/admin/reports?type=sales" class="rv-tab">Tỷ lệ bán vé</a>
    <a href="${ctx}/admin/reports?type=occupancy" class="rv-tab">Lấp đầy phòng</a>
    <a href="${ctx}/admin/reports?type=popular" class="rv-tab">Phim phổ biến</a>
    <a href="${ctx}/admin/reports?type=peak" class="rv-tab active">Giờ cao điểm</a>
    <a href="${ctx}/admin/reports?type=activity" class="rv-tab">Hoạt động KH</a>
</div>

<!-- Bộ lọc ngày -->
<div class="rv-toolbar mb-4">
    <form method="get" action="${ctx}/admin/reports" class="d-flex align-items-end flex-wrap gap-3 w-100" style="margin: 0; padding: 0; border: none; background: none;">
        <input type="hidden" name="type" value="peak">
        
        <div class="rv-form-group" style="margin: 0;">
            <label class="rv-label" for="fromDate" style="font-size: 11px; margin-bottom: 4px;">Từ ngày</label>
            <input type="date" id="fromDate" name="fromDate" class="rv-input" style="height: 38px;" required value="${report.fromDate}">
        </div>
        <div class="rv-form-group" style="margin: 0;">
            <label class="rv-label" for="toDate" style="font-size: 11px; margin-bottom: 4px;">Đến ngày</label>
            <input type="date" id="toDate" name="toDate" class="rv-input" style="height: 38px;" required value="${report.toDate}">
        </div>
        <div class="d-flex align-items-end gap-2">
            <button type="submit" class="rv-btn rv-btn--secondary rv-btn--sm">
                Lọc dữ liệu
            </button>
            <a href="${ctx}/admin/reports?type=peak" class="rv-btn rv-btn--ghost rv-btn--sm">Xóa lọc</a>
        </div>
    </form>
</div>

<!-- Đồ thị trực quan (Chart.js) -->
<div class="rv-card mb-4">
    <div class="rv-card__header">
        <span class="rv-card__title">
            <i class="bi bi-clock-history" style="color:var(--clr-primary);margin-right:.4rem;"></i> Biểu đồ lượng đặt vé theo giờ trong ngày (24 giờ)
        </span>
    </div>
    <div class="chart-box" style="height:320px; padding: 1rem;">
        <canvas id="peakChart"></canvas>
    </div>
</div>

<!-- Chi tiết giờ cao điểm -->
<div class="rv-card">
    <div class="rv-card__header">
        <span class="rv-card__title">
            <i class="bi bi-list-columns" style="color:var(--clr-primary);margin-right:.4rem;"></i> Số liệu chi tiết theo từng khung giờ
        </span>
    </div>
    <c:choose>
        <c:when test="${empty report.rows}">
            <div class="text-center py-5" style="color:var(--clr-muted);">
                <i class="bi bi-clock" style="font-size:3rem;opacity:.3;"></i>
                <p class="mt-3">Không có thông tin đặt vé trong ngày.</p>
            </div>
        </c:when>
        <c:otherwise>
            <div class="rv-table-responsive">
                <table class="rv-table">
                    <thead>
                        <tr>
                            <th>Khung giờ đặt vé</th>
                            <th style="text-align:right;">Số giao dịch</th>
                            <th style="text-align:right;">Số vé đã bán</th>
                            <th style="text-align:right;">Doanh thu phát sinh</th>
                        </tr>
                    </thead>
                    <tbody>
                        <c:forEach var="h" begin="0" end="23">
                            <!-- Tìm hàng dữ liệu tương ứng với khung giờ h -->
                            <c:set var="matchedRow" value="${null}" />
                            <c:forEach var="row" items="${report.rows}">
                                <c:if test="${row.booking_hour == h}">
                                    <c:set var="matchedRow" value="${row}" />
                                </c:if>
                            </c:forEach>
                            
                            <tr>
                                <td>
                                    <span style="font-weight:600;color:#fff;">
                                        <i class="bi bi-alarm text-muted me-2" style="font-size:.85rem;"></i>
                                        <fmt:formatNumber value="${h}" pattern="00"/>:00 - <fmt:formatNumber value="${h}" pattern="00"/>:59
                                    </span>
                                </td>
                                <td style="text-align:right;">
                                    <c:choose>
                                        <c:when test="${not empty matchedRow}">
                                            <fmt:formatNumber value="${matchedRow.booking_count}" type="number"/>
                                        </c:when>
                                        <c:otherwise>0</c:otherwise>
                                    </c:choose>
                                </td>
                                <td style="text-align:right;">
                                    <c:choose>
                                        <c:when test="${not empty matchedRow}">
                                            <fmt:formatNumber value="${matchedRow.ticket_count}" type="number"/>
                                        </c:when>
                                        <c:otherwise>0</c:otherwise>
                                    </c:choose>
                                </td>
                                <td style="text-align:right;font-weight:700;color:${not empty matchedRow ? '#fff' : 'var(--clr-muted)'};">
                                    <c:choose>
                                        <c:when test="${not empty matchedRow}">
                                            <fmt:formatNumber value="${matchedRow.revenue}" type="number"/> <span style="font-size:.75rem;color:var(--clr-muted)">đ</span>
                                        </c:when>
                                        <c:otherwise>0 đ</c:otherwise>
                                    </c:choose>
                                </td>
                            </tr>
                        </c:forEach>
                    </tbody>
                </table>
            </div>
        </c:otherwise>
    </c:choose>
</div>

</main>
</div>

<!-- Chart.js & Script -->
<script src="https://cdn.jsdelivr.net/npm/bootstrap@5.3.3/dist/js/bootstrap.bundle.min.js"
        integrity="sha384-YvpcrYf0tY3lHB60NNkmXc5s9fDVZLESaAA55NDzOxhy9GkcIdslK1eN7N6jIeHz"
        crossorigin="anonymous"></script>
<script src="https://cdn.jsdelivr.net/npm/chart.js"></script>
<script>
document.addEventListener("DOMContentLoaded", function () {
    var ctx = document.getElementById('peakChart').getContext('2d');
    var chartLabels = ${report.labelsJson};
    var chartData   = ${report.dataJson};

    if (!chartLabels || chartLabels.length === 0) {
        chartLabels = [];
        chartData = [];
        for (var i = 0; i < 24; i++) {
            chartLabels.push(i.toString().padStart(2, '0') + ':00');
            chartData.push(0);
        }
    }

    var gradient = ctx.createLinearGradient(0, 0, 0, 300);
    gradient.addColorStop(0, 'rgba(59, 130, 246, 0.35)');
    gradient.addColorStop(1, 'rgba(59, 130, 246, 0.01)');

    new Chart(ctx, {
        type: 'line',
        data: {
            labels: chartLabels,
            datasets: [{
                label: 'Số lượng đặt vé',
                data: chartData,
                borderColor: '#3b82f6',
                borderWidth: 3,
                backgroundColor: gradient,
                fill: true,
                tension: 0.4,
                pointBackgroundColor: '#fff',
                pointBorderColor: '#3b82f6',
                pointRadius: 4,
                pointHoverRadius: 6
            }]
        },
        options: {
            responsive: true,
            maintainAspectRatio: false,
            plugins: {
                legend: { display: false },
                tooltip: {
                    backgroundColor: '#1a1d27',
                    titleColor: '#fff',
                    bodyColor: '#e8eaf0',
                    borderColor: 'rgba(255,255,255,.08)',
                    borderWidth: 1,
                    callbacks: {
                        label: function(context) {
                            return ' Số lượng: ' + context.raw + ' đơn hàng';
                        }
                    }
                }
            },
            scales: {
                x: {
                    grid: { color: 'rgba(255, 255, 255, 0.08)' },
                    ticks: { color: '#94a3b8', font: { size: 11, family: "'Inter', sans-serif" } }
                },
                y: {
                    grid: { color: 'rgba(255, 255, 255, 0.08)' },
                    ticks: {
                        color: '#94a3b8',
                        font: { size: 12, family: "'Inter', sans-serif" },
                        stepSize: 1
                    }
                }
            }
        }
    });
});
</script>
</body>
</html>
