<%--
    RapViet Admin — Báo cáo tỉ lệ lấp đầy phòng chiếu (report-occupancy.jsp)
    Servlet: ReportController ?type=occupancy
    (Long — )
--%>
<%@ page contentType="text/html; charset=UTF-8" pageEncoding="UTF-8" %>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<%@ taglib prefix="fmt" uri="http://java.sun.com/jsp/jstl/fmt" %>
<%@ taglib prefix="fn" uri="http://java.sun.com/jsp/jstl/functions" %>
<c:set var="ctx"       value="${pageContext.request.contextPath}" />
<c:set var="pageTitle" value="Báo cáo lấp đầy phòng chiếu — RapViet Admin" scope="request" />

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
        <h1 class="rv-page-title">Hiệu suất lấp đầy phòng chiếu</h1>
        <p class="rv-page-subtitle">Hiệu suất lấp đầy chỗ ngồi trung bình trên tất cả các phòng và suất chiếu.</p>
    </div>
    <div class="rv-page-header__right">
        <a href="${ctx}/admin/reports/system" class="rv-btn rv-btn--ghost" style="height: 38px;">
            <i class="bi bi-arrow-left"></i>Quay lại
        </a>
        <a href="${ctx}/admin/reports?type=occupancy&action=export" class="rv-btn rv-btn--secondary">
            <i class="bi bi-file-earmark-spreadsheet-fill"></i>Xuất Excel (XLSX)
        </a>
    </div>
</div>

<!-- ── TABS NAVIGATION ── -->
<div class="rv-tabs">
    <a href="${ctx}/admin/reports?type=revenue" class="rv-tab">Doanh thu dòng tiền</a>
    <a href="${ctx}/admin/reports?type=sales" class="rv-tab">Tỷ lệ bán vé</a>
    <a href="${ctx}/admin/reports?type=occupancy" class="rv-tab active">Lấp đầy phòng</a>
    <a href="${ctx}/admin/reports?type=popular" class="rv-tab">Phim phổ biến</a>
    <a href="${ctx}/admin/reports?type=peak" class="rv-tab">Giờ cao điểm</a>
    <a href="${ctx}/admin/reports?type=activity" class="rv-tab">Hoạt động KH</a>
</div>

<!-- KPI Card lấp đầy trung bình -->
<div class="row g-4 mb-4">
    <div class="col-md-6 col-lg-4">
        <div class="admin-card d-flex align-items-center gap-4"
             style="background: linear-gradient(135deg, rgba(59,130,246,.05), rgba(59,130,246,.12)); border-color: rgba(59,130,246,.2);">
            <div style="width:54px;height:54px;background:rgba(59,130,246,.15);border-radius:12px;display:flex;align-items:center;justify-content:center;color:#3b82f6;font-size:1.75rem;">
                <i class="bi bi-percent"></i>
            </div>
            <div>
                <p style="color:var(--n-500);font-size:.78rem;margin:0;text-transform:uppercase;font-weight:600;letter-spacing:.05em;">Lấp đầy trung bình hệ thống</p>
                <h3 style="font-size:1.8rem;font-weight:800;margin:.2rem 0 0;color:var(--n-900);">
                    ${report.averageOccupancy}%
                </h3>
            </div>
        </div>
    </div>
    
    <div class="col-md-6 col-lg-8">
        <div class="admin-card d-flex align-items-center" style="height:100%;background:rgba(255,255,255,.01);">
            <div style="font-size:.85rem;color:var(--n-500);line-height:1.7;">
                <i class="bi bi-info-circle text-info me-1"></i> Tỷ lệ lấp đầy hiển thị tỷ số giữa <strong>số ghế thực tế đã bán (CONFIRMED/USED)</strong> và <strong>tổng số ghế cung cấp</strong> (tính bằng: <code>tổng số suất chiếu active</code> nhân với <code>sức chứa ghế của phòng đó (total_seats)</code>).
            </div>
        </div>
    </div>
</div>

<!-- Đồ thị trực quan (Chart.js) -->
<div class="admin-card mb-4">
    <h2 style="font-size:1rem;font-weight:600;margin-bottom:1.25rem;">
        <i class="bi bi-bar-chart-steps" style="color:var(--clr-primary);margin-right:.4rem;"></i> Biểu đồ so sánh tỷ lệ lấp đầy giữa các phòng chiếu
    </h2>
    <div class="chart-box" style="height:350px;">
        <canvas id="occupancyChart"></canvas>
    </div>
</div>

<!-- Bảng chi tiết -->
<div class="admin-card">
    <h2 style="font-size:1rem;font-weight:600;margin-bottom:1.25rem;">
        <i class="bi bi-table" style="color:var(--clr-primary);margin-right:.4rem;"></i> Số liệu chi tiết theo từng phòng chiếu
    </h2>
    <c:choose>
        <c:when test="${empty report.rows}">
            <div class="text-center py-5" style="color:var(--clr-muted);">
                <i class="bi bi-columns-gap" style="font-size:3rem;opacity:.3;"></i>
                <p class="mt-3">Không có thông tin thống kê phòng chiếu nào.</p>
            </div>
        </c:when>
        <c:otherwise>
            <div style="overflow-x:auto;">
                <table class="admin-table">
                    <thead>
                        <tr>
                            <th>Chi nhánh</th>
                            <th>Tên phòng</th>
                            <th style="text-align:right;">Sức chứa phòng</th>
                            <th style="text-align:right;">Số suất chiếu</th>
                            <th style="text-align:right;">Chỗ ngồi cung cấp</th>
                            <th style="text-align:right;">Ghế đã bán</th>
                            <th style="width:250px;">Tỷ lệ lấp đầy</th>
                        </tr>
                    </thead>
                    <tbody>
                        <c:forEach var="row" items="${report.rows}">
                            <tr>
                                <td><c:out value="${row.branch_name}"/></td>
                                <td>
                                    <span style="font-weight:600;color:var(--n-900);">
                                        <i class="bi bi-door-open-fill text-muted me-1"></i> <c:out value="${row.hall_name}"/>
                                    </span>
                                </td>
                                <td style="text-align:right;">
                                    <fmt:formatNumber value="${row.hall_seat_capacity}" type="number"/>
                                </td>
                                <td style="text-align:right;color:var(--clr-muted);">
                                    <fmt:formatNumber value="${row.showtime_count}" type="number"/>
                                </td>
                                <td style="text-align:right;">
                                    <fmt:formatNumber value="${row.total_capacity}" type="number"/>
                                </td>
                                <td style="text-align:right;font-weight:600;color:var(--n-900);">
                                    <fmt:formatNumber value="${row.booked_seats}" type="number"/>
                                </td>
                                <td>
                                    <div class="d-flex align-items-center gap-2">
                                        <!-- Thanh tiến trình tùy biến -->
                                        <div style="flex:1;height:8px;background:rgba(255,255,255,.05);border-radius:4px;overflow:hidden;border:1px solid rgba(255,255,255,.02);">
                                            <div style="height:100%;border-radius:4px;width:${row.occupancy_rate}%;
                                                ${row.occupancy_rate >= 70.0 ? 'background:#19c37d;' : ''}
                                                ${row.occupancy_rate >= 40.0 and row.occupancy_rate < 70.0 ? 'background:#ffc107;' : ''}
                                                ${row.occupancy_rate < 40.0 ? 'background:#ff6b6b;' : ''}">
                                            </div>
                                        </div>
                                        <span style="font-size:.8rem;font-weight:700;min-width:48px;text-align:right;
                                            ${row.occupancy_rate >= 70.0 ? 'color:#19c37d;' : ''}
                                            ${row.occupancy_rate >= 40.0 and row.occupancy_rate < 70.0 ? 'color:#ffc107;' : ''}
                                            ${row.occupancy_rate < 40.0 ? 'color:#ff6b6b;' : ''}">
                                            ${row.occupancy_rate}%
                                        </span>
                                    </div>
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
    var ctx = document.getElementById('occupancyChart').getContext('2d');
    var chartLabels = ${report.labelsJson};
    var chartData   = ${report.dataJson};

    if (!chartLabels || chartLabels.length === 0) {
        chartLabels = ["Chưa có dữ liệu"];
        chartData = [0];
    }

    new Chart(ctx, {
        type: 'bar',
        data: {
            labels: chartLabels,
            datasets: [{
                label: 'Tỷ lệ lấp đầy (%)',
                data: chartData,
                backgroundColor: function(context) {
                    var val = context.raw;
                    if (val >= 70) return 'rgba(25, 195, 125, 0.75)'; // Xanh lá
                    if (val >= 40) return 'rgba(255, 193, 7, 0.75)';  // Vàng
                    return 'rgba(255, 107, 107, 0.75)';              // Đỏ
                },
                borderColor: function(context) {
                    var val = context.raw;
                    if (val >= 70) return '#19c37d';
                    if (val >= 40) return '#ffc107';
                    return '#ff6b6b';
                },
                borderWidth: 1.2,
                borderRadius: 4
            }]
        },
        options: {
            indexAxis: 'y', // Biểu đồ dạng thanh nằm ngang cực kỳ phù hợp cho so sánh phòng
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
                            return 'Tỷ lệ lấp đầy: ' + context.raw + '%';
                        }
                    }
                }
            },
            scales: {
                x: {
                    max: 100,
                    grid: { color: 'rgba(0, 0, 0, 0.05)' },
                    ticks: { color: '#64748B', font: { size: 11 } }
                },
                y: {
                    grid: { display: false },
                    ticks: { color: '#64748B', font: { size: 10 } }
                }
            }
        }
    });
});
</script>
</body>
</html>
