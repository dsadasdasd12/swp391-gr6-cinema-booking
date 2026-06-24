<%--
    Rạp Việt CMS — Premium Administrative Dashboard
    URL: /admin/dashboard
    Servlet: DashboardServlet
--%>
<%@ page contentType="text/html; charset=UTF-8" pageEncoding="UTF-8" %>
<%
    model.User u = (model.User) session.getAttribute("user");
    if (u == null || !"ADMIN".equals(u.getRole())) {
        response.sendRedirect(request.getContextPath() + "/login");
        return;
    }
%>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<%@ taglib prefix="fmt" uri="http://java.sun.com/jsp/jstl/fmt" %>
<c:set var="ctx" value="${pageContext.request.contextPath}" />
<c:set var="pageTitle" value="Bảng điều khiển — Rạp Việt CMS" scope="request" />

<%@ include file="/pages/shared/header-admin.jsp" %>
<%@ include file="/pages/shared/sidebar-admin.jsp" %>

<!-- ── PAGE HEADER ── -->
<div class="rv-page-header">
    <div class="rv-page-header__left">
        <h1 class="rv-page-title">Hệ thống quản trị Rạp Việt CMS</h1>
        <p class="rv-page-subtitle">
            Xin chào <strong><c:out value="${sessionScope.user.fullName}"/></strong> —
            theo dõi tổng quát chỉ số kinh doanh và vận hành rạp chiếu ngày hôm nay.
        </p>
    </div>
    <div class="rv-page-header__right">
        <span class="rv-badge rv-badge--active" style="padding: var(--s-2) var(--s-3); font-weight: 600;">
            <i class="bi bi-clock-fill" style="margin-right: 6px;"></i>Hệ thống Online
        </span>
    </div>
</div>

<!-- ── 4× QUICK STATS CARDS ROW ── -->
<div class="rv-kpi-grid">
    <!-- Active Movies -->
    <div class="rv-kpi" style="border-left: 4px solid var(--primary-light);">
        <div class="d-flex align-items-center justify-content-between">
            <div>
                <span class="rv-kpi__label">Phim Đang Chiếu</span>
                <div class="rv-kpi__value" style="color: var(--primary-dark);">${activeMovies}</div>
            </div>
            <i class="bi bi-film" style="font-size: 2.2rem; color: var(--primary-light); opacity: 0.35;"></i>
        </div>
        <span class="rv-kpi__trend up"><i class="bi bi-arrow-up-right"></i> Đang hoạt động tốt</span>
    </div>

    <!-- Registered Customers -->
    <div class="rv-kpi" style="border-left: 4px solid var(--info);">
        <div class="d-flex align-items-center justify-content-between">
            <div>
                <span class="rv-kpi__label">Thành Viên Đăng Ký</span>
                <div class="rv-kpi__value" style="color: var(--info);">${customerCount}</div>
            </div>
            <i class="bi bi-people-fill" style="font-size: 2.2rem; color: var(--info); opacity: 0.35;"></i>
        </div>
        <span class="rv-kpi__trend up"><i class="bi bi-arrow-up-right"></i> +12 thành viên mới</span>
    </div>

    <!-- Total Bookings -->
    <div class="rv-kpi" style="border-left: 4px solid var(--accent);">
        <div class="d-flex align-items-center justify-content-between">
            <div>
                <span class="rv-kpi__label">Tổng Giao Dịch Vé</span>
                <div class="rv-kpi__value" style="color: var(--accent);">${bookingCount}</div>
            </div>
            <i class="bi bi-ticket-perforated-fill" style="font-size: 2.2rem; color: var(--accent); opacity: 0.35;"></i>
        </div>
        <span class="rv-kpi__trend up"><i class="bi bi-arrow-up-right"></i> Đặt vé ổn định</span>
    </div>

    <!-- Total Revenue -->
    <div class="rv-kpi" style="border-left: 4px solid var(--success);">
        <div class="d-flex align-items-center justify-content-between">
            <div>
                <span class="rv-kpi__label">Doanh Thu Tích Lũy</span>
                <div class="rv-kpi__value" style="color: var(--success);">
                    <fmt:formatNumber value="${totalRevenue}" type="number" maxFractionDigits="0"/> ₫
                </div>
            </div>
            <i class="bi bi-cash-stack" style="font-size: 2.2rem; color: var(--success); opacity: 0.35;"></i>
        </div>
        <span class="rv-kpi__trend up"><i class="bi bi-arrow-up-right"></i> Đạt mục tiêu tháng</span>
    </div>
</div>

<!-- ── MAIN DASHBOARD ARCHITECTURE ── -->
<div class="d-flex flex-wrap gap-4" style="margin-top: var(--s-5);">
    
    <!-- LEFT PANEL: Recent Bookings (Flex-1) -->
    <div class="rv-card" style="flex: 2; min-width: 500px;">
        <div class="rv-card__header">
            <span class="rv-card__title">
                <i class="bi bi-list-stars" style="margin-right: 8px; color: var(--primary);"></i>
                Giao dịch đặt vé gần đây
            </span>
            <a href="${ctx}/admin/tickets" class="rv-btn rv-btn--ghost rv-btn--sm">Xem tất cả vé</a>
        </div>
        <div class="rv-card__body" style="padding: 0;">
            <c:choose>
                <c:when test="${empty recentBookings}">
                    <div class="rv-empty" style="padding: 40px;">
                        <i class="bi bi-cart-x rv-empty__icon"></i>
                        <div class="rv-empty__title">Chưa có giao dịch nào</div>
                        <div class="rv-empty__message">Hệ thống chưa ghi nhận lượt đặt vé nào từ khách hàng.</div>
                    </div>
                </c:when>
                <c:otherwise>
                    <div class="rv-table-responsive">
                        <table class="rv-table" style="font-size: 13px;">
                            <thead>
                                <tr>
                                    <th>Khách hàng</th>
                                    <th>Phim đặt</th>
                                    <th>Thời gian</th>
                                    <th style="text-align: right;">Thành tiền</th>
                                    <th>Trạng thái</th>
                                </tr>
                            </thead>
                            <tbody>
                                <c:forEach var="b" items="${recentBookings}">
                                    <tr>
                                        <td><strong><c:out value="${b.fullName}"/></strong></td>
                                        <td><c:out value="${b.movieTitle}"/></td>
                                        <td style="color: var(--n-500);"><c:out value="${b.bookedAt}"/></td>
                                        <td style="text-align: right; font-weight: 700; color: var(--n-900);">
                                            <fmt:formatNumber value="${b.totalPrice}" type="number"/> ₫
                                        </td>
                                        <td>
                                            <c:choose>
                                                <c:when test="${b.status == 'CHECKED_IN'}">
                                                    <span class="rv-badge rv-badge--active">Đã check-in</span>
                                                </c:when>
                                                <c:when test="${b.status == 'CONFIRMED'}">
                                                    <span class="rv-badge rv-badge--manager">Đã xác nhận</span>
                                                </c:when>
                                                <c:when test="${b.status == 'PENDING'}">
                                                    <span class="rv-badge rv-badge--pending">Đang chờ</span>
                                                </c:when>
                                                <c:otherwise>
                                                    <span class="rv-badge rv-badge--blocked">Bị hủy</span>
                                                </c:otherwise>
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
    </div>

    <!-- RIGHT PANEL: Coming Soon Movies (280px) -->
    <div class="rv-card" style="flex: 1; min-width: 280px; max-width: 380px;">
        <div class="rv-card__header">
            <span class="rv-card__title">
                <i class="bi bi-clock-history" style="margin-right: 8px; color: var(--accent);"></i>
                Phim sắp công chiếu
            </span>
        </div>
        <div class="rv-card__body" style="display: flex; flex-direction: column; gap: var(--s-3);">
            <c:forEach var="m" items="${comingMovies}">
                <div style="display: flex; gap: var(--s-3); align-items: center; padding-bottom: var(--s-2); border-bottom: 1px solid var(--border);">
                    <img src="${ctx}/${not empty m.posterUrl ? m.posterUrl : 'assets/img/default-poster.jpg'}" 
                         alt="Poster" 
                         style="width: 45px; height: 60px; object-fit: cover; border-radius: var(--r-md); border: 1px solid var(--border);">
                    <div>
                        <div style="font-weight: 600; font-size: 13px; color: var(--n-900);"><c:out value="${m.title}"/></div>
                        <div style="font-size: 11px; color: var(--n-500); margin-top: 4px;">
                            Dự kiến: <fmt:parseDate value="${m.releaseDate}" pattern="yyyy-MM-dd" var="relDate" type="date" />
                            <fmt:formatDate value="${relDate}" pattern="dd/MM/yyyy" />
                        </div>
                    </div>
                </div>
            </c:forEach>
            <c:if test="${empty comingMovies}">
                <div style="text-align: center; color: var(--n-400); padding: 40px 0; font-size: 13px; font-style: italic;">Không có phim sắp công chiếu.</div>
            </c:if>
        </div>
    </div>

</div>

</main>
</div>

</body>
</html>
