<%--
    Rạp Việt CMS — Premium Branch Reports (Manager Dashboard)
    URL: /admin/reports/branch
    Servlet: BranchReportServlet
--%>
<%@ page contentType="text/html; charset=UTF-8" pageEncoding="UTF-8" %>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<%@ taglib prefix="fmt" uri="http://java.sun.com/jsp/jstl/fmt" %>
<%@ taglib prefix="fn" uri="http://java.sun.com/jsp/jstl/functions" %>
<c:set var="ctx" value="${pageContext.request.contextPath}" />
<c:set var="pageTitle" value="Báo cáo chi nhánh — Rạp Việt CMS" scope="request" />

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
            <span class="rv-breadcrumb__current">Báo cáo chi nhánh</span>
        </div>
        <h1 class="rv-page-title">Báo cáo hiệu suất chi nhánh</h1>
        <p class="rv-page-subtitle">Phân tích chuyên sâu về doanh thu, tỷ lệ lấp đầy ghế và phim phổ biến tại rạp.</p>
    </div>
    <div class="rv-page-header__right">
        <a href="${ctx}/admin/reports/branch?action=export&fromDate=${report.fromDate}&toDate=${report.toDate}&branchId=${selectedBranchId}" class="rv-btn rv-btn--secondary">
            <i class="bi bi-file-earmark-spreadsheet-fill"></i>Xuất Excel (XLSX)
        </a>
    </div>
</div>

<!-- ── CONTEXT BANNER ── -->
<div class="rv-banner rv-banner--info">
    <i class="bi bi-info-circle-fill"></i>
    <div>
        <c:choose>
            <c:when test="${adminAllBranches}">
                <strong>Quyền Admin — toàn hệ thống:</strong> Bạn có thể xem và so sánh báo cáo của mọi chi nhánh. Đang hiển thị: <span style="font-weight: 700; text-decoration: underline;"><c:out value="${assignedBranchName}"/></span>.
            </c:when>
            <c:otherwise>
                <strong>Phạm vi truy cập chi nhánh:</strong> Bạn đang xem số liệu cho chi nhánh <span style="font-weight: 700; text-decoration: underline;"><c:out value="${assignedBranchName}"/></span>.
            </c:otherwise>
        </c:choose>
    </div>
</div>

<!-- ── DATE RANGE FILTER ── -->
<div class="rv-toolbar">
    <form method="get" action="${ctx}/admin/reports/branch" class="d-flex align-items-end flex-wrap gap-3 w-100" style="margin: 0; padding: 0; border: none; background: none;">
        
        <c:if test="${adminAllBranches}">
            <div class="rv-form-group" style="min-width: 220px; margin: 0;">
                <label class="rv-label" for="branchId" style="font-size: 11px; margin-bottom: 4px;">Chi nhánh</label>
                <select id="branchId" name="branchId" class="rv-input" style="height: 38px;">
                    <c:forEach var="br" items="${allBranches}">
                        <option value="${br.id}" ${br.id == selectedBranchId ? 'selected' : ''}>
                            <c:out value="${br.name}"/>
                        </option>
                    </c:forEach>
                </select>
            </div>
        </c:if>

        <div style="display: flex; gap: var(--s-3); flex: 1; min-width: 280px; max-width: 480px;">
            <!-- From Date -->
            <div class="rv-form-group" style="flex: 1; margin: 0;">
                <label class="rv-label" for="fromDate" style="font-size: 11px; margin-bottom: 4px;">Từ ngày</label>
                <input type="date" id="fromDate" name="fromDate" class="rv-input" style="height: 38px;" required value="${report.fromDate}">
            </div>
            
            <!-- To Date -->
            <div class="rv-form-group" style="flex: 1; margin: 0;">
                <label class="rv-label" for="toDate" style="font-size: 11px; margin-bottom: 4px;">Đến ngày</label>
                <input type="date" id="toDate" name="toDate" class="rv-input" style="height: 38px;" required value="${report.toDate}">
            </div>
        </div>

        <div class="d-flex align-items-end gap-2">
            <button type="submit" class="rv-btn rv-btn--secondary rv-btn--sm">
                Lọc dữ liệu
            </button>
            <a href="${ctx}/admin/reports/branch" class="rv-btn rv-btn--ghost rv-btn--sm">Xóa lọc</a>
        </div>
    </form>
</div>

<!-- ── 4× KPI CARDS ROW ── -->
<div class="rv-kpi-grid" style="grid-template-columns: repeat(4, 1fr);">
    <!-- Doanh thu -->
    <div class="rv-kpi" style="border-left: 4px solid var(--primary);">
        <span class="rv-kpi__label">Doanh thu chi nhánh</span>
        <div class="rv-kpi__value" style="font-size: 20px; color: var(--primary);">
            <fmt:formatNumber value="${report.totalRevenue}" type="number" maxFractionDigits="0"/> ₫
        </div>
        <span class="rv-kpi__trend up"><i class="bi bi-arrow-up"></i> +4.2%</span>
    </div>

    <!-- Lấp đầy phòng -->
    <div class="rv-kpi" style="border-left: 4px solid var(--success);">
        <span class="rv-kpi__label">Hiệu suất lấp đầy TB</span>
        <div class="rv-kpi__value" style="font-size: 20px; color: var(--success);">
            <c:out value="${report.averageOccupancy}"/>%
        </div>
        <span class="rv-kpi__trend up"><i class="bi bi-arrow-up"></i> +1.5%</span>
    </div>

    <!-- Vé Bán Ra -->
    <div class="rv-kpi" style="border-left: 4px solid var(--accent);">
        <span class="rv-kpi__label">Số vé đã bán ra</span>
        <div class="rv-kpi__value" style="font-size: 20px; color: var(--accent);">
            <fmt:formatNumber value="${report.totalTickets}" type="number"/> Vé
        </div>
        <span class="rv-kpi__trend down" style="color: var(--danger);"><i class="bi bi-arrow-down"></i> -0.8%</span>
    </div>

    <!-- Suất Chiếu -->
    <div class="rv-kpi" style="border-left: 4px solid var(--info);">
        <span class="rv-kpi__label">Tổng số suất chiếu</span>
        <div class="rv-kpi__value" style="font-size: 20px; color: var(--info);">
            <fmt:formatNumber value="${report.totalShowtimes}" type="number"/> Suất
        </div>
        <span class="rv-kpi__trend up"><i class="bi bi-arrow-up"></i> +3 suất</span>
    </div>
</div>

<!-- ── CHARTS (2×2 GRID) ── -->
<div class="rv-charts-row" style="grid-template-columns: repeat(2, 1fr); gap: var(--s-5);">
    <!-- Chart 1: Occupancy Over Time -->
    <div class="rv-chart-card">
        <h3 class="rv-chart-card__title">Tỷ lệ lấp đầy ghế theo ngày (%)</h3>
        <div class="rv-chart-canvas" style="height: 220px;">
            <canvas id="occOverTimeChart"></canvas>
        </div>
    </div>

    <!-- Chart 2: Occupancy by Hall -->
    <div class="rv-chart-card">
        <h3 class="rv-chart-card__title">Hiệu suất lấp đầy theo phòng chiếu</h3>
        <div class="rv-chart-canvas" style="height: 220px;">
            <canvas id="occByHallChart"></canvas>
        </div>
    </div>

    <!-- Chart 3: Top 5 Popular Movies -->
    <div class="rv-chart-card">
        <h3 class="rv-chart-card__title">Phim phổ biến &amp; Đặt vé nhiều nhất</h3>
        <div style="display: flex; flex-direction: column; gap: var(--s-3); padding-top: var(--s-2);">
            <c:forEach var="mov" items="${report.popularMovies}" varStatus="status">
                <div>
                    <div style="display: flex; justify-content: space-between; font-size: 13px; font-weight: 500; margin-bottom: 4px;">
                        <span>${status.index + 1}. <c:out value="${mov.title}"/></span>
                        <span style="font-weight: 600; color: var(--primary);"><fmt:formatNumber value="${mov.ticketCount}" type="number"/> Vé</span>
                    </div>
                    <!-- Ranked progress bar indicator -->
                    <div style="width: 100%; height: 8px; background: var(--n-100); border-radius: var(--r-full); overflow: hidden;">
                        <div style="width: ${mov.percentage}%; height: 100%; background: linear-gradient(to right, var(--primary-light), var(--primary)); border-radius: var(--r-full);"></div>
                    </div>
                </div>
            </c:forEach>
            <c:if test="${empty report.popularMovies}">
                <div style="text-align: center; color: var(--n-400); padding: 40px 0; font-size: 13px;">Chưa ghi nhận vé bán ra.</div>
            </c:if>
        </div>
    </div>

    <!-- Chart 4: Showtime Performance Combo -->
    <div class="rv-chart-card">
        <h3 class="rv-chart-card__title">Hiệu suất Suất chiếu &amp; Lượng vé</h3>
        <div class="rv-chart-canvas" style="height: 220px;">
            <canvas id="performanceComboChart"></canvas>
        </div>
    </div>
</div>

<!-- ── DATA TABLE ── -->
<div class="rv-card">
    <div class="rv-card__header">
        <span class="rv-card__title"><i class="bi bi-grid-3x3-gap" style="margin-right: 8px; color: var(--primary)"></i>Bảng số liệu chi tiết chi nhánh</span>
    </div>
    
    <c:choose>
        <c:when test="${empty report.rows}">
            <div class="rv-empty">
                <i class="bi bi-bar-chart-fill rv-empty__icon"></i>
                <div class="rv-empty__title">Không tìm thấy dữ liệu</div>
                <div class="rv-empty__message">Không có hoạt động kinh doanh hay suất chiếu nào được thực hiện tại chi nhánh trong khoảng ngày này.</div>
            </div>
        </c:when>
        <c:otherwise>
            <div class="rv-table-responsive">
                <table class="rv-table">
                    <thead>
                        <tr>
                            <th>Ngày chiếu</th>
                            <th style="text-align: right;">Số suất chiếu</th>
                            <th style="text-align: right;">Tổng vé bán</th>
                            <th style="text-align: right;">Tỷ lệ lấp đầy</th>
                            <th style="text-align: right;">Giá vé trung bình</th>
                            <th style="text-align: right;">Doanh thu ngày</th>
                        </tr>
                    </thead>
                    <tbody>
                        <c:forEach var="row" items="${report.rows}">
                            <tr>
                                <td><strong><c:out value="${row.report_date}"/></strong></td>
                                <td style="text-align: right;"><fmt:formatNumber value="${row.showtime_count}" type="number"/></td>
                                <td style="text-align: right;"><fmt:formatNumber value="${row.ticket_count}" type="number"/></td>
                                <td style="text-align: right; font-weight: 600;"><c:out value="${row.occupancy_rate}"/>%</td>
                                <td style="text-align: right;"><fmt:formatNumber value="${row.avg_ticket_price}" type="number"/> ₫</td>
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
            renderBranchCharts();
        }
    }, 100);

    function renderBranchCharts() {
        // Line Chart: Occupancy Over Time
        const occLabels = ${report.occupancyLabelsJson};
        const occData = ${report.occupancyDataJson};
        window.RVCharts.createLineChart('occOverTimeChart', occLabels, occData, 'Tỷ lệ lấp đầy (%)');

        // Horizontal Bar Chart: Occupancy by Hall
        const hallLabels = ${report.hallLabelsJson};
        const hallData = ${report.hallDataJson};
        window.RVCharts.createHorizontalBarChart('occByHallChart', hallLabels, hallData);

        // Combo Chart: Ticket sales vs Showtimes
        const comboLabels = ${report.comboLabelsJson};
        const comboTickets = ${report.comboTicketsJson};
        const comboShowtimes = ${report.comboShowtimesJson};
        window.RVCharts.createComboChart('performanceComboChart', comboLabels, comboTickets, comboShowtimes);
    }
});
</script>

</body>
</html>
