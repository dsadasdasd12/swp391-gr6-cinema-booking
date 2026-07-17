<%--
    Rạp Việt CMS — Premium System Reports: Revenue Tab
    URL: /admin/reports?type=revenue
    Servlet: ReportController
--%>
<%@ page contentType="text/html; charset=UTF-8" pageEncoding="UTF-8" %>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<%@ taglib prefix="fmt" uri="http://java.sun.com/jsp/jstl/fmt" %>
<%@ taglib prefix="fn" uri="http://java.sun.com/jsp/jstl/functions" %>
<c:set var="ctx" value="${pageContext.request.contextPath}" />
<c:set var="pageTitle" value="Báo cáo doanh thu — Rạp Việt CMS" scope="request" />

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
            <span class="rv-breadcrumb__current">Báo cáo &amp; Phân tích</span>
        </div>
        <h1 class="rv-page-title">Báo cáo doanh thu hệ thống</h1>
        <p class="rv-page-subtitle">Xem, lọc và phân tích dữ liệu doanh thu chi tiết từ bán vé và dịch vụ rạp phim.</p>
    </div>
    <div class="rv-page-header__right">
        <a href="${ctx}/admin/reports/system" class="rv-btn rv-btn--ghost" style="height: 38px;">
            <i class="bi bi-arrow-left"></i>Quay lại
        </a>
        <a href="${ctx}/admin/reports?type=revenue&action=export&fromDate=${report.fromDate}&toDate=${report.toDate}" class="rv-btn rv-btn--secondary">
            <i class="bi bi-file-earmark-spreadsheet-fill"></i>Xuất Excel (XLSX)
        </a>
    </div>
</div>

<!-- ── TABS NAVIGATION ── -->
<div class="rv-tabs">
    <a href="${ctx}/admin/reports?type=revenue" class="rv-tab active">Doanh thu dòng tiền</a>
    <a href="${ctx}/admin/reports?type=sales" class="rv-tab">Tỷ lệ bán vé</a>
    <a href="${ctx}/admin/reports?type=occupancy" class="rv-tab">Lấp đầy phòng</a>
    <a href="${ctx}/admin/reports?type=popular" class="rv-tab">Phim phổ biến</a>
    <a href="${ctx}/admin/reports?type=peak" class="rv-tab">Giờ cao điểm</a>
    <a href="${ctx}/admin/reports?type=activity" class="rv-tab">Hoạt động KH</a>
</div>

<!-- ── DATE RANGE FILTER ── -->
<div class="rv-toolbar">
    <form method="get" action="${ctx}/admin/reports" class="d-flex align-items-end flex-wrap gap-3 w-100" style="margin: 0; padding: 0; border: none; background: none;">
        <input type="hidden" name="type" value="revenue">

        <div style="display: flex; gap: var(--s-3); flex: 1; min-width: 280px;">
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
            <a href="${ctx}/admin/reports?type=revenue" class="rv-btn rv-btn--ghost rv-btn--sm">Xóa lọc</a>
        </div>
    </form>
</div>

<!-- ── KPI SUMMARY CARDS ── -->
<div class="rv-kpi-grid">
    <!-- Doanh thu Summary -->
    <div class="rv-kpi" style="border-left: 4px solid var(--primary); background: linear-gradient(to right, var(--surface), var(--n-50));">
        <span class="rv-kpi__label">Tổng doanh thu hệ thống</span>
        <div class="rv-kpi__value" style="color: var(--primary);">
            <fmt:formatNumber value="${report.totalRevenue}" type="number" maxFractionDigits="0"/> ₫
        </div>
        <span class="rv-kpi__trend up"><i class="bi bi-arrow-up-right"></i> +12.4% vs tháng trước</span>
    </div>

    <!-- Vé Bán Ra Summary -->
    <div class="rv-kpi" style="border-left: 4px solid var(--accent); background: linear-gradient(to right, var(--surface), var(--n-50));">
        <span class="rv-kpi__label">Tổng vé đã xuất</span>
        <div class="rv-kpi__value" style="color: var(--accent);">
            <fmt:formatNumber value="${report.totalTickets}" type="number"/> Vé
        </div>
        <span class="rv-kpi__trend up"><i class="bi bi-arrow-up-right"></i> +8.2% vs tháng trước</span>
    </div>

    <!-- Số Giao Dịch Summary -->
    <div class="rv-kpi" style="border-left: 4px solid var(--success); background: linear-gradient(to right, var(--surface), var(--n-50));">
        <span class="rv-kpi__label">Tổng số giao dịch (Booking)</span>
        <div class="rv-kpi__value" style="color: var(--success);">
            <fmt:formatNumber value="${report.totalBookings}" type="number"/> Đơn
        </div>
        <span class="rv-kpi__trend up"><i class="bi bi-arrow-up-right"></i> +5.6% vs tháng trước</span>
    </div>
</div>

<!-- ── GRAPH CHART VIEW ── -->
<div class="rv-card" style="margin-bottom: var(--s-6);">
    <div class="rv-card__header">
        <span class="rv-card__title"><i class="bi bi-graph-up-arrow" style="margin-right: 8px; color: var(--primary)"></i>Đồ thị diễn biến doanh thu hàng ngày (VND)</span>
    </div>
    <div class="rv-card__body">
        <div class="rv-chart-canvas">
            <canvas id="revenueChartCanvas"></canvas>
        </div>
    </div>
</div>

<!-- ── DATA TABLE SUMMARY ── -->
<div class="rv-card">
    <div class="rv-card__header">
        <span class="rv-card__title"><i class="bi bi-list-columns" style="margin-right: 8px; color: var(--primary)"></i>Bảng số liệu chi tiết</span>
    </div>

    <c:choose>
        <c:when test="${empty report.rows}">
            <div class="rv-empty">
                <i class="bi bi-calendar-x rv-empty__icon"></i>
                <div class="rv-empty__title">Không tìm thấy dữ liệu</div>
                <div class="rv-empty__message">Vui lòng điều chỉnh lại khoảng thời gian lọc và thử lại.</div>
            </div>
        </c:when>
        <c:otherwise>
            <div class="rv-table-responsive">
                <table class="rv-table">
                    <thead>
                        <tr>
                            <th>Ngày báo cáo</th>
                            <th style="text-align: right;">Số giao dịch</th>
                            <th style="text-align: right;">Số vé bán ra</th>
                            <th style="text-align: right;">Tổng doanh thu ngày</th>
                        </tr>
                    </thead>
                    <tbody>
                        <c:forEach var="row" items="${report.rows}">
                            <tr>
                                <td>
                                    <div style="font-weight: 600; color: var(--n-800); display: flex; align-items: center; gap: 8px;">
                                        <i class="bi bi-calendar3" style="color: var(--primary); font-size: 14px;"></i>
                                        <c:out value="${row.report_date}"/>
                                    </div>
                                </td>
                                <td style="text-align: right; font-weight: 500;">
                                    <fmt:formatNumber value="${row.booking_count}" type="number"/>
                                </td>
                                <td style="text-align: right; font-weight: 500;">
                                    <fmt:formatNumber value="${row.ticket_count}" type="number"/>
                                </td>
                                <td style="text-align: right; font-weight: 700; color: var(--n-900);">
                                    <fmt:formatNumber value="${row.revenue}" type="number"/> ₫
                                </td>
                            </tr>
                        </c:forEach>

                        <!-- Totals Row -->
                        <tr style="background: var(--n-50); font-weight: 700;">
                            <td>TỔNG CỘNG HÀNG</td>
                            <td style="text-align: right;"><fmt:formatNumber value="${report.totalBookings}" type="number"/></td>
                            <td style="text-align: right;"><fmt:formatNumber value="${report.totalTickets}" type="number"/></td>
                            <td style="text-align: right; color: var(--primary); font-size: 15px;"><fmt:formatNumber value="${report.totalRevenue}" type="number"/> ₫</td>
                        </tr>
                    </tbody>
                </table>
            </div>
        </c:otherwise>
    </c:choose>
</div>

</main>
</div>

<!-- Render Line Chart script -->
<script>
    document.addEventListener("DOMContentLoaded", function () {
        const checkChartLoaded = setInterval(() => {
            if (window.RVCharts && typeof Chart !== 'undefined') {
                clearInterval(checkChartLoaded);
                initChart();
            }
        }, 100);

        function initChart() {
            let labels = ${report.labelsJson};
            let data = ${report.dataJson};

            if (!labels || labels.length === 0) {
                labels = ["Chưa có số liệu"];
                data = [0];
            }

            window.RVCharts.createLineChart('revenueChartCanvas', labels, data, 'Doanh thu ngày (₫)');
        }
    });
</script>

</body>
</html>
