<%--
    Rạp Việt CMS — Premium System Reports Comprehensive Dashboard
    URL: /admin/reports/system
    Servlet: SystemReportServlet
--%>
<%@ page contentType="text/html; charset=UTF-8" pageEncoding="UTF-8" %>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<%@ taglib prefix="fmt" uri="http://java.sun.com/jsp/jstl/fmt" %>
<%@ taglib prefix="fn" uri="http://java.sun.com/jsp/jstl/functions" %>
<c:set var="ctx" value="${pageContext.request.contextPath}" />
<c:set var="pageTitle" value="Báo cáo hệ thống — Rạp Việt CMS" scope="request" />

<%@ include file="/pages/shared/header-admin.jsp" %>
<%@ include file="/pages/shared/sidebar-admin.jsp" %>

<!-- Load Premium Charts and Chart.js v4 -->
<script src="https://cdn.jsdelivr.net/npm/chart.js" defer></script>
<script src="${ctx}/assets/js/charts.js" charset="UTF-8" defer></script>

<!-- ── PAGE HEADER ── -->
<div class="rv-page-header">
    <div class="rv-page-header__left">
        <!-- Breadcrumb -->
        <div class="rv-breadcrumb">
            <a href="${ctx}/admin/dashboard">Dashboard</a>
            <i class="bi bi-chevron-right rv-breadcrumb__sep"></i>
            <span class="rv-breadcrumb__current">Báo cáo hệ thống</span>
        </div>
        <h1 class="rv-page-title">Báo cáo &amp; Phân tích hệ thống</h1>
        <p class="rv-page-subtitle">Theo dõi hoạt động kinh doanh, doanh thu, công suất phòng chiếu và khung giờ cao điểm.</p>
    </div>
    <div class="rv-page-header__right">
        <a href="${ctx}/admin/reports/system?action=export&fromDate=${report.fromDate}&toDate=${report.toDate}&branchId=${param.branchId}" class="rv-btn rv-btn--secondary">
            <i class="bi bi-file-earmark-spreadsheet-fill"></i>Xuất Excel (XLSX)
        </a>
    </div>
</div>

<!-- ── DATE RANGE & BRANCH FILTERS ── -->
<div class="rv-toolbar">
    <form method="get" action="${ctx}/admin/reports/system" class="d-flex align-items-end flex-wrap gap-3 w-100" style="margin: 0; padding: 0; border: none; background: none;">
        
        <div style="display: grid; grid-template-columns: 1fr 1fr 1fr; gap: var(--s-3); flex: 1; min-width: 400px;">
            <!-- From Date -->
            <div class="rv-form-group" style="margin: 0;">
                <label class="rv-label" for="fromDate" style="font-size: 11px; margin-bottom: 4px;">Từ ngày</label>
                <input type="date" id="fromDate" name="fromDate" class="rv-input" style="height: 38px;" required value="${report.fromDate}">
            </div>
            
            <!-- To Date -->
            <div class="rv-form-group" style="margin: 0;">
                <label class="rv-label" for="toDate" style="font-size: 11px; margin-bottom: 4px;">Đến ngày</label>
                <input type="date" id="toDate" name="toDate" class="rv-input" style="height: 38px;" required value="${report.toDate}">
            </div>

            <!-- Branch filter -->
            <div class="rv-form-group" style="margin: 0;">
                <label class="rv-label" for="branchId" style="font-size: 11px; margin-bottom: 4px;">Chi nhánh</label>
                <select id="branchId" name="branchId" class="rv-select" style="height: 38px;">
                    <option value="">Tất cả chi nhánh</option>
                    <c:forEach var="b" items="${branches}">
                        <option value="${b.id}" ${param.branchId == b.id ? 'selected' : ''}><c:out value="${b.name}"/></option>
                    </c:forEach>
                </select>
            </div>
        </div>

        <div class="d-flex align-items-end gap-2">
            <button type="submit" class="rv-btn rv-btn--secondary rv-btn--sm">
                Lọc dữ liệu
            </button>
            <a href="${ctx}/admin/reports/system" class="rv-btn rv-btn--ghost rv-btn--sm">Xóa lọc</a>
        </div>
    </form>
</div>

<!-- ── SUMMARY KPI CARDS ── -->
<div class="rv-kpi-grid">
    <div class="rv-kpi" style="border-left: 4px solid var(--primary);">
        <span class="rv-kpi__label">Tổng doanh thu</span>
        <div class="rv-kpi__value" style="color: var(--primary);">
            <fmt:formatNumber value="${report.totalRevenue}" type="number" maxFractionDigits="0"/> ₫
        </div>
        <span class="rv-kpi__trend up"><i class="bi bi-arrow-up-right"></i> Tăng trưởng tốt</span>
    </div>

    <div class="rv-kpi" style="border-left: 4px solid var(--accent);">
        <span class="rv-kpi__label">Tổng số vé bán ra</span>
        <div class="rv-kpi__value" style="color: var(--accent);">
            <fmt:formatNumber value="${report.totalTickets}" type="number"/> Vé
        </div>
        <span class="rv-kpi__trend up"><i class="bi bi-arrow-up-right"></i> Số lượng ổn định</span>
    </div>

    <div class="rv-kpi" style="border-left: 4px solid var(--success);">
        <span class="rv-kpi__label">Tỷ lệ lấp đầy TB</span>
        <div class="rv-kpi__value" style="color: var(--success);">
            <c:out value="${report.averageOccupancy}"/>%
        </div>
        <span class="rv-kpi__trend up"><i class="bi bi-arrow-up-right"></i> Đạt hiệu suất cao</span>
    </div>
</div>

<!-- ── 3-COLUMN CHARTS ROW ── -->
<div class="rv-charts-row">
    <!-- Line chart: Revenue -->
    <div class="rv-chart-card">
        <h3 class="rv-chart-card__title">Diễn biến doanh thu (VND)</h3>
        <div class="rv-chart-canvas" style="height: 240px;">
            <canvas id="revChart"></canvas>
        </div>
    </div>

    <!-- Bar chart: Ticket Sales -->
    <div class="rv-chart-card">
        <h3 class="rv-chart-card__title">Số vé bán ra theo ngày</h3>
        <div class="rv-chart-canvas" style="height: 240px;">
            <canvas id="ticketChart"></canvas>
        </div>
    </div>

    <!-- Horizontal Bar chart: Occupancy by Hall -->
    <div class="rv-chart-card">
        <h3 class="rv-chart-card__title">Hiệu suất lấp đầy phòng chiếu</h3>
        <div class="rv-chart-canvas" style="height: 240px;">
            <canvas id="occChart"></canvas>
        </div>
    </div>
</div>

<!-- ── CUSTOM HEATMAP PANEL: PEAK HOURS ── -->
<div class="rv-card" style="margin-bottom: var(--s-6);">
    <div class="rv-card__header">
        <span class="rv-card__title"><i class="bi bi-clock-fill" style="margin-right: 8px; color: var(--primary)"></i>Khung giờ vàng &amp; Tỷ lệ đặt vé cao điểm (Peak Booking Hours Heatmap)</span>
    </div>
    <div class="rv-card__body">
        <div id="heatmapContainer" style="min-height: 260px; overflow-x: auto;">
            <!-- Rendered dynamically by charts.js -->
        </div>
    </div>
</div>

<!-- ── DATA TABLE ── -->
<div class="rv-card">
    <div class="rv-card__header">
        <span class="rv-card__title"><i class="bi bi-list-stars" style="margin-right: 8px; color: var(--primary)"></i>Báo cáo tổng hợp</span>
    </div>
    <c:choose>
        <c:when test="${empty report.rows}">
            <div class="rv-empty">
                <i class="bi bi-clipboard-data rv-empty__icon"></i>
                <div class="rv-empty__title">Không có số liệu báo cáo</div>
                <div class="rv-empty__message">Vui lòng điều chỉnh lại khoảng thời gian lọc và thử lại.</div>
            </div>
        </c:when>
        <c:otherwise>
            <div class="rv-table-responsive">
                <table class="rv-table">
                    <thead>
                        <tr>
                            <th>Ngày báo cáo</th>
                            <th>Tên chi nhánh</th>
                            <th style="text-align: right;">Số vé bán</th>
                            <th style="text-align: right;">Suất chiếu</th>
                            <th style="text-align: right;">Tỷ lệ lấp đầy</th>
                            <th style="text-align: right;">Doanh thu</th>
                        </tr>
                    </thead>
                    <tbody>
                        <c:forEach var="row" items="${report.rows}">
                            <tr>
                                <td><strong><c:out value="${row.report_date}"/></strong></td>
                                <td><c:out value="${not empty row.branch_name ? row.branch_name : 'Toàn chi nhánh'}"/></td>
                                <td style="text-align: right;"><fmt:formatNumber value="${row.ticket_count}" type="number"/></td>
                                <td style="text-align: right;"><fmt:formatNumber value="${row.showtime_count}" type="number"/></td>
                                <td style="text-align: right; font-weight: 600;"><c:out value="${row.occupancy_rate}"/>%</td>
                                <td style="text-align: right; font-weight: 700; color: var(--n-900);"><fmt:formatNumber value="${row.revenue}" type="number"/> ₫</td>
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

<!-- Load Chart Renderer -->
<script>
document.addEventListener("DOMContentLoaded", function () {
    const checkChartLoaded = setInterval(() => {
        if (window.RVCharts && typeof Chart !== 'undefined') {
            clearInterval(checkChartLoaded);
            renderDashboardCharts();
        }
    }, 100);

    function renderDashboardCharts() {
        // Line Chart: Revenue
        const revLabels = ${report.revenueLabelsJson};
        const revData = ${report.revenueDataJson};
        window.RVCharts.createLineChart('revChart', revLabels, revData, 'Doanh thu (₫)');

        // Bar Chart: Ticket Sales
        const ticketLabels = ${report.ticketLabelsJson};
        const ticketData = ${report.ticketDataJson};
        window.RVCharts.createBarChart('ticketChart', ticketLabels, ticketData, 'Vé');

        // Horizontal Bar Chart: Occupancy
        const occLabels = ${report.occupancyLabelsJson};
        const occData = ${report.occupancyDataJson};
        window.RVCharts.createHorizontalBarChart('occChart', occLabels, occData);

        // Peak Hours Custom Heatmap Matrix
        const heatmapMatrix = ${report.heatmapMatrixJson};
        window.RVCharts.renderPeakHoursHeatmap('heatmapContainer', heatmapMatrix);
    }
});
</script>

</body>
</html>
