<%--
    RapViet Admin — Báo cáo hoạt động khách hàng (report-activity.jsp)
    Servlet: ReportController ?type=activity
    (Long — )
--%>
<%@ page contentType="text/html; charset=UTF-8" pageEncoding="UTF-8" %>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<%@ taglib prefix="fmt" uri="http://java.sun.com/jsp/jstl/fmt" %>
<%@ taglib prefix="fn" uri="http://java.sun.com/jsp/jstl/functions" %>
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
    <!-- Tổng giao dịch -->
    <div class="col-md-4">
        <div class="rv-card d-flex align-items-center gap-3" 
             style="background: linear-gradient(135deg, rgba(59,130,246,.05), rgba(59,130,246,.12)); border-color: rgba(59,130,246,.2); padding: 1.5rem; border-radius: var(--r-xl); border: 1px solid rgba(59,130,246,.2);">
            <div style="width:48px;height:48px;background:rgba(59,130,246,.15);border-radius:10px;display:flex;align-items:center;justify-content:center;color:#3b82f6;font-size:1.5rem;">
                <i class="bi bi-receipt"></i>
            </div>
            <div>
                <p style="color:var(--n-500);font-size:.78rem;margin:0;text-transform:uppercase;font-weight:600;letter-spacing:.05em;">Tổng giao dịch</p>
                <h3 style="font-size:1.4rem;font-weight:700;margin:.2rem 0 0;color:var(--n-900);">
                    <fmt:formatNumber value="${report.totalBookings}" type="number"/> <span style="font-size:.9rem;font-weight:400;color:var(--n-500)">Đơn</span>
                </h3>
            </div>
        </div>
    </div>
    
    <!-- Tổng vé bán -->
    <div class="col-md-4">
        <div class="rv-card d-flex align-items-center gap-3"
             style="background: linear-gradient(135deg, rgba(255,107,53,.05), rgba(255,107,53,.12)); border-color: rgba(255,107,53,.2); padding: 1.5rem; border-radius: var(--r-xl); border: 1px solid rgba(255,107,53,.2);">
            <div style="width:48px;height:48px;background:rgba(255,107,53,.15);border-radius:10px;display:flex;align-items:center;justify-content:center;color:var(--clr-accent);font-size:1.5rem;">
                <i class="bi bi-ticket-detailed"></i>
            </div>
            <div>
                <p style="color:var(--n-500);font-size:.78rem;margin:0;text-transform:uppercase;font-weight:600;letter-spacing:.05em;">Tổng vé đã mua</p>
                <h3 style="font-size:1.4rem;font-weight:700;margin:.2rem 0 0;color:var(--n-900);">
                    <fmt:formatNumber value="${report.totalTickets}" type="number"/> <span style="font-size:.9rem;font-weight:400;color:var(--n-500)">Vé</span>
                </h3>
            </div>
        </div>
    </div>
    
    <!-- Tổng chi tiêu -->
    <div class="col-md-4">
        <div class="rv-card d-flex align-items-center gap-3"
             style="background: linear-gradient(135deg, rgba(25,195,125,.05), rgba(25,195,125,.12)); border-color: rgba(25,195,125,.2); padding: 1.5rem; border-radius: var(--r-xl); border: 1px solid rgba(25,195,125,.2);">
            <div style="width:48px;height:48px;background:rgba(25,195,125,.15);border-radius:10px;display:flex;align-items:center;justify-content:center;color:#19c37d;font-size:1.5rem;">
                <i class="bi bi-wallet2"></i>
            </div>
            <div>
                <p style="color:var(--n-500);font-size:.78rem;margin:0;text-transform:uppercase;font-weight:600;letter-spacing:.05em;">Tổng chi tiêu</p>
                <h3 style="font-size:1.4rem;font-weight:700;margin:.2rem 0 0;color:var(--n-900);">
                    <fmt:formatNumber value="${report.totalRevenue}" type="number"/> <span style="font-size:.9rem;font-weight:400;color:var(--n-500)">₫</span>
                </h3>
            </div>
        </div>
    </div>
</div>

<div class="rv-card">
    <div class="rv-card__header">
        <span class="rv-card__title">
            <i class="bi bi-people-fill" style="color:var(--clr-primary);margin-right:.4rem;"></i> Chi tiết theo khách hàng
        </span>
    </div>
    <c:choose>
        <c:when test="${empty report.rows}">
            <div class="rv-empty">
                <i class="bi bi-person-x rv-empty__icon"></i>
                <div class="rv-empty__title">Không tìm thấy dữ liệu</div>
                <div class="rv-empty__message">Chưa có hoạt động đặt vé trong khoảng thời gian này.</div>
            </div>
        </c:when>
        <c:otherwise>
            <div class="rv-table-responsive">
                <table class="rv-table">
                    <thead>
                        <tr>
                            <th style="width: 50px;">Hạng</th>
                            <th>Khách hàng</th>
                            <th style="text-align:right;">Giao dịch</th>
                            <th style="text-align:right;">Vé mua</th>
                            <th style="text-align:right;">Tổng chi tiêu</th>
                            <th style="text-align:right;">Lần đặt cuối</th>
                        </tr>
                    </thead>
                    <tbody>
                        <c:forEach var="row" items="${report.rows}" varStatus="st">
                            <tr>
                                <td>
                                    <span style="display:inline-flex;width:24px;height:24px;border-radius:50%;align-items:center;justify-content:center;font-size:.8rem;font-weight:700;
                                        ${st.index == 0 ? 'background:#ffd700;color:#000;' : ''}
                                        ${st.index == 1 ? 'background:#c0c0c0;color:#000;' : ''}
                                        ${st.index == 2 ? 'background:#cd7f32;color:#000;' : ''}
                                        ${st.index > 2 ? 'background:rgba(255,255,255,.05);color:var(--n-500);' : ''}">
                                        ${st.index + 1}
                                    </span>
                                </td>
                                <td>
                                    <div class="d-flex align-items-center gap-3">
                                        <div style="width:36px;height:36px;border-radius:50%;background:linear-gradient(135deg, #e50914, #ff6b35);color:#fff;display:flex;align-items:center;justify-content:center;font-weight:700;font-size:.9rem;text-transform:uppercase;">
                                            ${fn:substring(row.customer_name, 0, 1)}
                                        </div>
                                        <div>
                                            <div style="font-weight:600;color:var(--n-900);font-size:.95rem;"><c:out value="${row.customer_name}"/></div>
                                            <div style="font-size:.8rem;color:var(--n-500);"><c:out value="${row.customer_email}"/></div>
                                        </div>
                                    </div>
                                </td>
                                <td style="text-align:right;vertical-align:middle;">
                                    <span style="background:rgba(59,130,246,.15);color:#3b82f6;padding:4px 10px;border-radius:20px;font-size:.8rem;font-weight:600;">
                                        <fmt:formatNumber value="${row.booking_count}" type="number"/> Đơn
                                    </span>
                                </td>
                                <td style="text-align:right;vertical-align:middle;font-weight:600;">
                                    <fmt:formatNumber value="${row.ticket_count}" type="number"/>
                                </td>
                                <td style="text-align:right;vertical-align:middle;font-weight:700;color:#19c37d;font-size:1.05rem;">
                                    <fmt:formatNumber value="${row.total_spent}" type="number"/> ₫
                                </td>
                                <td style="text-align:right;vertical-align:middle;font-size:.85rem;color:var(--n-400);">
                                    <fmt:parseDate value="${fn:substring(row.last_booking_at, 0, 19)}" pattern="yyyy-MM-dd HH:mm:ss" var="parsedDate" type="both" />
                                    <fmt:formatDate value="${parsedDate}" pattern="dd/MM/yyyy HH:mm"/>
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
</body>
</html>
