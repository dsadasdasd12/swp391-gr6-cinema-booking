<%--
    RapViet Admin — Báo cáo doanh số chi nhánh (report-sales.jsp)
    Servlet: ReportController ?type=sales
    (Long — )
--%>
<%@ page contentType="text/html; charset=UTF-8" pageEncoding="UTF-8" %>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<%@ taglib prefix="fmt" uri="http://java.sun.com/jsp/jstl/fmt" %>
<%@ taglib prefix="fn" uri="http://java.sun.com/jsp/jstl/functions" %>
<c:set var="ctx"       value="${pageContext.request.contextPath}" />
<c:set var="pageTitle" value="Báo cáo bán vé theo chi nhánh — RapViet Admin" scope="request" />

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
        <h1 class="rv-page-title">Doanh số theo chi nhánh</h1>
        <p class="rv-page-subtitle">So sánh hiệu suất bán vé và dòng doanh thu giữa các cụm rạp chiếu phim.</p>
    </div>
    <div class="rv-page-header__right">
        <a href="${ctx}/admin/reports/system" class="rv-btn rv-btn--ghost" style="height: 38px;">
            <i class="bi bi-arrow-left"></i>Quay lại
        </a>
        <a href="${ctx}/admin/reports?type=sales&action=export&fromDate=${report.fromDate}&toDate=${report.toDate}" class="rv-btn rv-btn--secondary">
            <i class="bi bi-file-earmark-spreadsheet-fill"></i>Xuất Excel (XLSX)
        </a>
    </div>
</div>

<!-- ── TABS NAVIGATION ── -->
<div class="rv-tabs">
    <a href="${ctx}/admin/reports?type=revenue" class="rv-tab">Doanh thu dòng tiền</a>
    <a href="${ctx}/admin/reports?type=sales" class="rv-tab active">Tỷ lệ bán vé</a>
    <a href="${ctx}/admin/reports?type=occupancy" class="rv-tab">Lấp đầy phòng</a>
    <a href="${ctx}/admin/reports?type=popular" class="rv-tab">Phim phổ biến</a>
    <a href="${ctx}/admin/reports?type=peak" class="rv-tab">Giờ cao điểm</a>
    <a href="${ctx}/admin/reports?type=activity" class="rv-tab">Hoạt động KH</a>
</div>

<!-- Bộ lọc ngày -->
<div class="rv-toolbar mb-4">
    <form method="get" action="${ctx}/admin/reports" class="d-flex align-items-end flex-wrap gap-3 w-100" style="margin: 0; padding: 0; border: none; background: none;">
        <input type="hidden" name="type" value="sales">

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
            <a href="${ctx}/admin/reports?type=sales" class="rv-btn rv-btn--ghost rv-btn--sm">Xóa lọc</a>
        </div>
    </form>
</div>

<!-- KPI Cards -->
<div class="row g-4 mb-4">
    <!-- Doanh thu chi nhánh -->
    <div class="col-md-4">
        <div class="admin-card d-flex align-items-center gap-3" 
             style="background: linear-gradient(135deg, rgba(25,195,125,.05), rgba(25,195,125,.12)); border-color: rgba(25,195,125,.2);">
            <div style="width:48px;height:48px;background:rgba(25,195,125,.15);border-radius:10px;display:flex;align-items:center;justify-content:center;color:#19c37d;font-size:1.5rem;">
                <i class="bi bi-shop"></i>
            </div>
            <div>
                <p style="color:var(--n-500);font-size:.78rem;margin:0;text-transform:uppercase;font-weight:600;letter-spacing:.05em;">Tổng doanh thu tất cả rạp</p>
                <h3 style="font-size:1.4rem;font-weight:700;margin:.2rem 0 0;color:var(--n-900);">
                    <fmt:formatNumber value="${report.totalRevenue}" type="number" maxFractionDigits="0"/> <span style="font-size:.9rem;font-weight:400;color:var(--n-500)">VNĐ</span>
                </h3>
            </div>
        </div>
    </div>

    <!-- Số vé bán -->
    <div class="col-md-4">
        <div class="admin-card d-flex align-items-center gap-3"
             style="background: linear-gradient(135deg, rgba(255,107,53,.05), rgba(255,107,53,.12)); border-color: rgba(255,107,53,.2);">
            <div style="width:48px;height:48px;background:rgba(255,107,53,.15);border-radius:10px;display:flex;align-items:center;justify-content:center;color:var(--clr-accent);font-size:1.5rem;">
                <i class="bi bi-ticket-perforated"></i>
            </div>
            <div>
                <p style="color:var(--n-500);font-size:.78rem;margin:0;text-transform:uppercase;font-weight:600;letter-spacing:.05em;">Tổng vé đã bán</p>
                <h3 style="font-size:1.4rem;font-weight:700;margin:.2rem 0 0;color:var(--n-900);">
                    <fmt:formatNumber value="${report.totalTickets}" type="number"/> <span style="font-size:.9rem;font-weight:400;color:var(--n-500)">vé</span>
                </h3>
            </div>
        </div>
    </div>

    <!-- Số giao dịch -->
    <div class="col-md-4">
        <div class="admin-card d-flex align-items-center gap-3"
             style="background: linear-gradient(135deg, rgba(59,130,246,.05), rgba(59,130,246,.12)); border-color: rgba(59,130,246,.2);">
            <div style="width:48px;height:48px;background:rgba(59,130,246,.15);border-radius:10px;display:flex;align-items:center;justify-content:center;color:#3b82f6;font-size:1.5rem;">
                <i class="bi bi-cart-check"></i>
            </div>
            <div>
                <p style="color:var(--n-500);font-size:.78rem;margin:0;text-transform:uppercase;font-weight:600;letter-spacing:.05em;">Tổng giao dịch</p>
                <h3 style="font-size:1.4rem;font-weight:700;margin:.2rem 0 0;color:var(--n-900);">
                    <fmt:formatNumber value="${report.totalBookings}" type="number"/> <span style="font-size:.9rem;font-weight:400;color:var(--n-500)">đơn</span>
                </h3>
            </div>
        </div>
    </div>
</div>

<!-- Biểu đồ -->
<div class="admin-card mb-4">
    <h2 style="font-size:1rem;font-weight:600;margin-bottom:1.25rem;">
        <i class="bi bi-bar-chart" style="color:var(--clr-primary);margin-right:.4rem;"></i> Doanh thu của từng cụm rạp
    </h2>
    <div class="chart-box" style="height:320px;">
        <canvas id="salesChart"></canvas>
    </div>
</div>

<!-- Danh sách -->
<div class="admin-card">
    <h2 style="font-size:1rem;font-weight:600;margin-bottom:1.25rem;">
        <i class="bi bi-list-stars" style="color:var(--clr-primary);margin-right:.4rem;"></i> Chi tiết xếp hạng doanh số
    </h2>
    <c:choose>
        <c:when test="${empty report.rows}">
            <div class="text-center py-5" style="color:var(--clr-muted);">
                <i class="bi bi-shop-window" style="font-size:3rem;opacity:.3;"></i>
                <p class="mt-3">Không có thông tin doanh thu theo chi nhánh.</p>
            </div>
        </c:when>
        <c:otherwise>
            <div style="overflow-x:auto;">
                <table class="admin-table">
                    <thead>
                        <tr>
                            <th>Hạng</th>
                            <th>Tên chi nhánh</th>
                            <th style="text-align:right;">Số giao dịch</th>
                            <th style="text-align:right;">Số vé bán ra</th>
                            <th style="text-align:right;">Tổng doanh thu</th>
                        </tr>
                    </thead>
                    <tbody>
                        <c:forEach var="row" items="${report.rows}" varStatus="status">
                            <tr>
                                <td>
                                    <span style="display:inline-flex;width:24px;height:24px;border-radius:50%;align-items:center;justify-content:center;font-size:.8rem;font-weight:700;
                                          ${status.index == 0 ? 'background:#ffd700;color:#000;' : ''}
                                          ${status.index == 1 ? 'background:#c0c0c0;color:#000;' : ''}
                                          ${status.index == 2 ? 'background:#cd7f32;color:#000;' : ''}
                                          ${status.index > 2 ? 'background:rgba(0,0,0,.08);color:var(--n-500);' : ''}">
                                        ${status.index + 1}
                                    </span>
                                </td>
                                <td>
                                    <strong><c:out value="${row.branch_name}"/></strong>
                                </td>
                                <td style="text-align:right;font-weight:500;">
                                    <fmt:formatNumber value="${row.booking_count}" type="number"/>
                                </td>
                                <td style="text-align:right;font-weight:500;">
                                    <fmt:formatNumber value="${row.ticket_count}" type="number"/>
                                </td>
                                <td style="text-align:right;font-weight:700;color:var(--n-900);">
                                    <fmt:formatNumber value="${row.revenue}" type="number"/> <span style="font-size:.75rem;color:var(--n-500)">đ</span>
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
        var ctx = document.getElementById('salesChart').getContext('2d');
        var chartLabels = ${report.labelsJson};
        var chartData = ${report.dataJson};

        if (!chartLabels || chartLabels.length === 0) {
            chartLabels = ["Chưa có dữ liệu"];
            chartData = [0];
        }

        new Chart(ctx, {
            type: 'bar',
            data: {
                labels: chartLabels,
                datasets: [{
                        label: 'Doanh thu (VNĐ)',
                        data: chartData,
                        backgroundColor: 'rgba(25, 195, 125, 0.75)',
                        borderColor: '#19c37d',
                        borderWidth: 1.5,
                        borderRadius: 6,
                        barThickness: 32
                    }]
            },
            options: {
                responsive: true,
                maintainAspectRatio: false,
                plugins: {
                    legend: {display: false},
                    tooltip: {
                        backgroundColor: '#1E293B',
                        titleColor: '#fff',
                        bodyColor: '#F8FAFC',
                        borderColor: 'rgba(0,0,0,.08)',
                        borderWidth: 1,
                        callbacks: {
                            label: function (context) {
                                return 'Doanh thu: ' + context.raw.toLocaleString('vi-VN') + ' VNĐ';
                            }
                        }
                    }
                },
                scales: {
                    x: {
                        grid: {color: 'rgba(0, 0, 0, 0.05)'},
                        ticks: {color: '#64748B', font: {size: 11}}
                    },
                    y: {
                        grid: {color: 'rgba(0, 0, 0, 0.05)'},
                        ticks: {
                            color: '#64748B',
                            font: {size: 11},
                            callback: function (value) {
                                if (value >= 1000000) {
                                    return (value / 1000000) + 'M';
                                }
                                return value.toLocaleString('vi-VN');
                            }
                        }
                    }
                }
            }
        });
    });
</script>
</body>
</html>
