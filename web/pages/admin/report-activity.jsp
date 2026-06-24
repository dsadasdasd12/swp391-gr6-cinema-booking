<%--
    RapViet Admin — Báo cáo hoạt động khách hàng (report-activity.jsp)
    Servlet: ReportController ?type=activity
    (Long — )
--%>
<%@ page contentType="text/html; charset=UTF-8" pageEncoding="UTF-8" %>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<%@ taglib prefix="fmt" uri="http://java.sun.com/jsp/jstl/fmt" %>
<c:set var="ctx" value="${pageContext.request.contextPath}" />
<c:set var="pageTitle" value="Báo cáo hoạt động khách hàng — RapViet Admin" scope="request" />

<%@ include file="/pages/shared/header-admin.jsp" %>
<%@ include file="/pages/shared/sidebar-admin.jsp" %>

<div class="rv-page-header">
    <div class="rv-page-header__left">
        <div class="rv-breadcrumb">
            <a href="${ctx}/admin/dashboard">Dashboard</a>
            <i class="bi bi-chevron-right rv-breadcrumb__sep"></i>
            <span class="rv-breadcrumb__current">Báo cáo &amp; Phân tích</span>
        </div>
        <h1 class="rv-page-title">Hoạt động khách hàng</h1>
        <p class="rv-page-subtitle">Theo dõi tần suất đặt vé, chi tiêu và lần giao dịch gần nhất của thành viên.</p>
    </div>
    <div class="rv-page-header__right">
        <a href="${ctx}/admin/reports?type=activity&action=export&fromDate=${report.fromDate}&toDate=${report.toDate}" class="rv-btn rv-btn--secondary">
            <i class="bi bi-file-earmark-spreadsheet-fill"></i>Xuất Excel (XLSX)
        </a>
    </div>
</div>

<div class="rv-tabs">
    <a href="${ctx}/admin/reports?type=revenue" class="rv-tab">Doanh thu</a>
    <a href="${ctx}/admin/reports?type=sales" class="rv-tab">Bán vé theo rạp</a>
    <a href="${ctx}/admin/reports?type=occupancy" class="rv-tab">Lấp đầy phòng</a>
    <a href="${ctx}/admin/reports?type=popular" class="rv-tab">Phim phổ biến</a>
    <a href="${ctx}/admin/reports?type=peak" class="rv-tab">Giờ cao điểm</a>
    <a href="${ctx}/admin/reports?type=activity" class="rv-tab active">Hoạt động KH</a>
</div>

<div class="rv-toolbar mb-4">
    <form method="get" action="${ctx}/admin/reports" class="d-flex align-items-end flex-wrap gap-3 w-100" style="margin: 0; padding: 0; border: none; background: none;">
        <input type="hidden" name="type" value="activity">
        <div class="col-md-3">
            <label class="rv-label" for="fromDate" style="font-size: 11px; margin-bottom: 4px;">Từ ngày</label>
            <input type="date" id="fromDate" name="fromDate" class="rv-input" required value="${report.fromDate}">
        </div>
        <div class="col-md-3">
            <label class="rv-label" for="toDate" style="font-size: 11px; margin-bottom: 4px;">Đến ngày</label>
            <input type="date" id="toDate" name="toDate" class="rv-input" required value="${report.toDate}">
        </div>
        <div class="d-flex align-items-end gap-2">
            <button type="submit" class="rv-btn rv-btn--secondary rv-btn--sm">
                Lọc dữ liệu
            </button>
            <a href="${ctx}/admin/reports?type=activity" class="rv-btn rv-btn--ghost rv-btn--sm">Xóa lọc</a>
        </div>
    </form>
</div>

<div class="row g-4 mb-4">
    <div class="col-md-4">
        <div class="admin-card">
            <p style="color:var(--clr-muted);font-size:.78rem;margin:0;">Tổng giao dịch</p>
            <h3 style="font-size:1.4rem;font-weight:700;margin:.25rem 0 0;">
                <fmt:formatNumber value="${report.totalBookings}" type="number"/>
            </h3>
        </div>
    </div>
    <div class="col-md-4">
        <div class="admin-card">
            <p style="color:var(--clr-muted);font-size:.78rem;margin:0;">Tổng vé đã mua</p>
            <h3 style="font-size:1.4rem;font-weight:700;margin:.25rem 0 0;">
                <fmt:formatNumber value="${report.totalTickets}" type="number"/> vé
            </h3>
        </div>
    </div>
    <div class="col-md-4">
        <div class="admin-card">
            <p style="color:var(--clr-muted);font-size:.78rem;margin:0;">Tổng chi tiêu</p>
            <h3 style="font-size:1.4rem;font-weight:700;margin:.25rem 0 0;">
                <fmt:formatNumber value="${report.totalRevenue}" type="number"/> ₫
            </h3>
        </div>
    </div>
</div>

<div class="admin-card">
    <h2 style="font-size:1rem;font-weight:600;margin-bottom:1rem;">
        <i class="bi bi-people-fill" style="color:var(--clr-primary);margin-right:.4rem;"></i> Chi tiết theo khách hàng
    </h2>
    <c:choose>
        <c:when test="${empty report.rows}">
            <div class="text-center py-5" style="color:var(--clr-muted);">
                <i class="bi bi-person-x" style="font-size:3rem;opacity:.3;"></i>
                <p class="mt-3">Chưa có hoạt động đặt vé trong khoảng thời gian này.</p>
            </div>
        </c:when>
        <c:otherwise>
            <div style="overflow-x:auto;">
                <table class="admin-table">
                    <thead>
                        <tr>
                            <th>#</th>
                            <th>Khách hàng</th>
                            <th>Email</th>
                            <th style="text-align:right;">Giao dịch</th>
                            <th style="text-align:right;">Vé mua</th>
                            <th style="text-align:right;">Tổng chi tiêu</th>
                            <th>Đặt gần nhất</th>
                        </tr>
                    </thead>
                    <tbody>
                        <c:forEach var="row" items="${report.rows}" varStatus="st">
                            <tr>
                                <td>${st.index + 1}</td>
                                <td><c:out value="${row.customer_name}"/></td>
                                <td><c:out value="${row.customer_email}"/></td>
                                <td style="text-align:right;"><fmt:formatNumber value="${row.booking_count}" type="number"/></td>
                                <td style="text-align:right;"><fmt:formatNumber value="${row.ticket_count}" type="number"/></td>
                                <td style="text-align:right;"><fmt:formatNumber value="${row.total_spent}" type="number"/> ₫</td>
                                <td><c:out value="${row.last_booking_at}"/></td>
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
</body>
</html>
