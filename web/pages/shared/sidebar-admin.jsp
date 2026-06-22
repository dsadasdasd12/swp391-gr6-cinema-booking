<%--
    Rạp Việt CMS — Premium Admin Sidebar Navigation
    Include sau header-admin.jsp trên mỗi trang admin.
    Active state được detect tự động qua request.getRequestURI().
--%>
<%@ page contentType="text/html; charset=UTF-8" pageEncoding="UTF-8" %>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<c:set var="ctx"    value="${pageContext.request.contextPath}" />
<c:set var="uri"    value="${pageContext.request.requestURI}" />

<%-- Helper checks for active menus --%>
<c:set var="isMovie"        value="${uri.contains('/admin/movies') || uri.contains('/movie')}" />
<c:set var="isTicket"       value="${uri.contains('/admin/tickets') || uri.contains('/ticket')}" />
<c:set var="isNotif"        value="${uri.contains('/admin/notifications') || uri.contains('/notification')}" />
<c:set var="isReport"       value="${uri.contains('/admin/reports') || uri.contains('/report')}" />
<c:set var="isGeneralReport" value="${uri.contains('/admin/reports') && !uri.contains('/admin/reports/system') && !uri.contains('/admin/reports/branch')}" />
<c:set var="isAccount"      value="${uri.contains('/admin/accounts') || uri.contains('/account')}" />
<c:set var="isSettings"     value="${uri.contains('/admin/settings')}" />
<c:set var="sessionUser" value="${not empty sessionScope.user ? sessionScope.user : sessionScope.adminUser}" />
<c:set var="isAdminRole"    value="${sessionUser.role == 'ADMIN'}" />
<c:set var="isManagerRole"  value="${sessionUser.role == 'MANAGER'}" />

<aside class="rv-sidebar">
    <!-- 🏠 Dashboard -->
    <div class="rv-nav__group">
        <a href="${ctx}/admin/dashboard" class="rv-nav__item ${uri.contains('/admin/dashboard') ? 'active' : ''}">
            <i class="bi bi-grid-1x2-fill"></i>
            Dashboard
        </a>
    </div>

    <!-- 🎬 Movie Management (Admin / Manager / Staff view only) -->
    <div class="rv-nav__group ${isMovie ? 'open' : ''}">
        <div class="rv-nav__item ${isMovie ? 'active' : ''}">
            <i class="bi bi-film"></i>
            Quản lý phim
            <i class="bi bi-chevron-right rv-nav__arrow"></i>
        </div>
        <div class="rv-nav__sub">
            <a href="${ctx}/admin/movies?action=list" class="rv-nav__sub-item ${isMovie && (param.action == 'list' || empty param.action || param.action == 'detail') ? 'active' : ''}">
                Danh sách phim
            </a>
            <c:if test="${isAdminRole || isManagerRole}">
                <a href="${ctx}/admin/movies?action=new" class="rv-nav__sub-item ${isMovie && param.action == 'new' ? 'active' : ''}">
                    Thêm phim mới
                </a>
            </c:if>
        </div>
    </div>

    <!-- 📅 Showtime Management -->
    <div class="rv-nav__group">
        <a href="${ctx}/admin/showtimes?action=list" class="rv-nav__item ${uri.contains('/admin/showtimes') ? 'active' : ''}">
            <i class="bi bi-calendar-event"></i>
            Quản lý suất chiếu
        </a>
    </div>

    <!-- 🎫 Booking Management -->
    <div class="rv-nav__group">
        <a href="${ctx}/admin/tickets?action=list" class="rv-nav__item ${isTicket ? 'active' : ''}">
            <i class="bi bi-qr-code"></i>
            Quản lý vé &amp; Booking
        </a>
    </div>
            
            <!-- Cinema Management -->
    <div class="rv-nav__group">
    <a href="${ctx}/admin/cinemas"
       class="rv-nav__item ${uri.contains('/admin/cinemas') ? 'active' : ''}">
        <i class="bi bi-building-fill"></i>
        Quản lý Chuỗi rạp
    </a>
</div>
        
        
        <!-- Branch Management -->
        <div class="rv-nav__group">
    <a href="${ctx}/admin/branches"
       class="rv-nav__item ${uri.contains('/admin/branches') ? 'active' : ''}">
        <i class="bi bi-geo-alt-fill"></i>
        Quản lý Chi nhánh
    </a>
</div>
        
    <!-- 📧 Notifications (đặt vé / thanh toán / hệ thống — không có khuyến mãi) -->
    <div class="rv-nav__group">
        <a href="${ctx}/admin/notifications?action=list" class="rv-nav__item ${isNotif ? 'active' : ''}">
            <i class="bi bi-envelope-check-fill"></i>
            Lịch sử thông báo
        </a>
    </div>

    <!-- ── REPORTING & ANALYTICS ── -->
    <div class="rv-nav__label">Báo cáo &amp; Phân tích</div>

    <div class="rv-nav__group ${isReport ? 'open' : ''}">
        <div class="rv-nav__item ${isReport ? 'active' : ''}">
            <i class="bi bi-bar-chart-line-fill"></i>
            Báo cáo thống kê
            <i class="bi bi-chevron-right rv-nav__arrow"></i>
        </div>
        <div class="rv-nav__sub">
            <!-- System Reports: Admin only -->
            <c:if test="${isAdminRole}">
                <a href="${ctx}/admin/reports/system" class="rv-nav__sub-item ${uri.contains('/admin/reports/system') ? 'active' : ''}">
                    Báo cáo hệ thống
                </a>
            </c:if>
            <!-- Branch Reports: Manager only -->
            <c:if test="${isManagerRole || isAdminRole}">
                <a href="${ctx}/admin/reports/branch" class="rv-nav__sub-item ${uri.contains('/admin/reports/branch') ? 'active' : ''}">
                    Báo cáo chi nhánh
                </a>
            </c:if>
            <a href="${ctx}/admin/reports?type=revenue" class="rv-nav__sub-item ${isGeneralReport && param.type == 'revenue' ? 'active' : ''}">
                Doanh thu
            </a>
            <a href="${ctx}/admin/reports?type=sales" class="rv-nav__sub-item ${isGeneralReport && param.type == 'sales' ? 'active' : ''}">
                Bán vé theo rạp
            </a>
            <a href="${ctx}/admin/reports?type=occupancy" class="rv-nav__sub-item ${isGeneralReport && param.type == 'occupancy' ? 'active' : ''}">
                Lấp đầy phòng
            </a>
            <a href="${ctx}/admin/reports?type=popular" class="rv-nav__sub-item ${isGeneralReport && param.type == 'popular' ? 'active' : ''}">
                Phim phổ biến
            </a>
            <a href="${ctx}/admin/reports?type=peak" class="rv-nav__sub-item ${isGeneralReport && param.type == 'peak' ? 'active' : ''}">
                Giờ cao điểm
            </a>
            <a href="${ctx}/admin/reports?type=activity" class="rv-nav__sub-item ${isGeneralReport && param.type == 'activity' ? 'active' : ''}">
                Hoạt động KH
            </a>
        </div>
    </div>

    <!-- ── ACCOUNTS & SECURITY ── -->
    <div class="rv-nav__label">Tài khoản &amp; Bảo mật</div>

   <div class="rv-nav__group">
    <a href="${ctx}/admin/accounts"
       class="rv-nav__item ${isAccount ? 'active' : ''}">
        <i class="bi bi-people-fill"></i>
        Quản trị tài khoản
    </a>
</div>

    <div class="rv-nav__spacer"></div>
    <div class="rv-nav__divider"></div>

    <!-- System Settings -->
    <c:if test="${isAdminRole}">
        <div class="rv-nav__group">
            <a href="${ctx}/admin/settings" class="rv-nav__item ${isSettings ? 'active' : ''}">
                <i class="bi bi-gear-fill"></i>
                Cài đặt hệ thống
            </a>
        </div>
    </c:if>

    <!-- Logout -->
    <div class="rv-nav__group">
        <a href="${ctx}/logout" class="rv-nav__item logout" data-confirm data-confirm-title="Đăng xuất?" data-confirm-message="Bạn có chắc chắn muốn đăng xuất khỏi hệ thống Rap Việt CMS?" data-confirm-type="warning" data-confirm-text="Đăng xuất">
            <i class="bi bi-box-arrow-right"></i>
            Đăng xuất
        </a>
    </div>
</aside>

<!-- Admin main content area starts immediately after sidebar. Closed in page footer -->
<main class="rv-main">
