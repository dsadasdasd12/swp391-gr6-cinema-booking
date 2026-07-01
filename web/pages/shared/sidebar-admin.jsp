<%--
    Ráº¡p Viá»‡t CMS â€” Premium Admin Sidebar Navigation
    Include sau header-admin.jsp trÃªn má»—i trang admin.
    Active state Ä‘Æ°á»£c detect tá»± Ä‘á»™ng qua request.getRequestURI().
--%>
<%@ page contentType="text/html; charset=UTF-8" pageEncoding="UTF-8" %>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<c:set var="ctx"    value="${pageContext.request.contextPath}" />
<c:set var="uri"    value="${pageContext.request.requestURI}" />

<%-- Helper checks for active menus --%>
<c:set var="isMovie"        value="${uri.contains('/admin/movies') || uri.contains('/movie')}" />
<c:set var="isCategory"     value="${uri.contains('/admin/categories')}" />
<c:set var="isLanguage"     value="${uri.contains('/admin/languages')}" />
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
    <!-- ðŸ  Dashboard -->
    <div class="rv-nav__group">
        <a href="${ctx}/admin/dashboard" class="rv-nav__item ${uri.contains('/admin/dashboard') ? 'active' : ''}">
            <i class="bi bi-grid-1x2-fill"></i>
            Dashboard
        </a>
    </div>

    <!-- ðŸŽ¬ Movie Management (Admin / Manager / Staff view only) -->
    <div class="rv-nav__group ${isMovie ? 'open' : ''}">
        <div class="rv-nav__item ${isMovie ? 'active' : ''}">
            <i class="bi bi-film"></i>
            Quáº£n lÃ½ phim
            <i class="bi bi-chevron-right rv-nav__arrow"></i>
        </div>
        <div class="rv-nav__sub">
            <a href="${ctx}/admin/movies?action=list" class="rv-nav__sub-item ${isMovie && (param.action == 'list' || empty param.action || param.action == 'detail') ? 'active' : ''}">
                Danh sÃ¡ch phim
            </a>
            <c:if test="${isAdminRole || isManagerRole}">
                <a href="${ctx}/admin/movies?action=new" class="rv-nav__sub-item ${isMovie && param.action == 'new' ? 'active' : ''}">
                    ThÃªm phim má»›i
                </a>
            </c:if>
        </div>
    </div>

    <!-- ðŸŽ« Booking Management -->
    <div class="rv-nav__group">
        <a href="${ctx}/admin/tickets?action=list" class="rv-nav__item ${isTicket ? 'active' : ''}">
            <i class="bi bi-qr-code"></i>
            Quáº£n lÃ½ vÃ© &amp; Booking
        </a>
    </div>
            
            <!-- Cinema Management -->
    <div class="rv-nav__group">
    <a href="${ctx}/admin/cinemas"
       class="rv-nav__item ${uri.contains('/admin/cinemas') ? 'active' : ''}">
        <i class="bi bi-building-fill"></i>
        Quáº£n lÃ½ Chuá»—i ráº¡p
    </a>
</div>
        
        
        <!-- Branch Management -->
        <div class="rv-nav__group">
    <a href="${ctx}/admin/branches"
       class="rv-nav__item ${uri.contains('/admin/branches') ? 'active' : ''}">
        <i class="bi bi-geo-alt-fill"></i>
        Quáº£n lÃ½ Chi nhÃ¡nh
    </a>
</div>
        
    <!-- ðŸ“§ Notifications (Ä‘áº·t vÃ© / thanh toÃ¡n / há»‡ thá»‘ng â€” khÃ´ng cÃ³ khuyáº¿n mÃ£i) -->
    <div class="rv-nav__group">
        <a href="${ctx}/admin/notifications?action=list" class="rv-nav__item ${isNotif ? 'active' : ''}">
            <i class="bi bi-envelope-check-fill"></i>
            Lá»‹ch sá»­ thÃ´ng bÃ¡o
        </a>
    </div>

    <!-- â”€â”€ REPORTING & ANALYTICS â”€â”€ -->
    <div class="rv-nav__label">BÃ¡o cÃ¡o &amp; PhÃ¢n tÃ­ch</div>

    <div class="rv-nav__group ${isReport ? 'open' : ''}">
        <div class="rv-nav__item ${isReport ? 'active' : ''}">
            <i class="bi bi-bar-chart-line-fill"></i>
            BÃ¡o cÃ¡o thá»‘ng kÃª
            <i class="bi bi-chevron-right rv-nav__arrow"></i>
        </div>
        <div class="rv-nav__sub">
            <!-- System Reports: Admin only -->
            <c:if test="${isAdminRole}">
                <a href="${ctx}/admin/reports/system" class="rv-nav__sub-item ${uri.contains('/admin/reports/system') ? 'active' : ''}">
                    BÃ¡o cÃ¡o há»‡ thá»‘ng
                </a>
            </c:if>
            <!-- Branch Reports: Manager only -->
            <c:if test="${isManagerRole || isAdminRole}">
                <a href="${ctx}/admin/reports/branch" class="rv-nav__sub-item ${uri.contains('/admin/reports/branch') ? 'active' : ''}">
                    BÃ¡o cÃ¡o chi nhÃ¡nh
                </a>
            </c:if>
            <a href="${ctx}/admin/reports?type=revenue" class="rv-nav__sub-item ${isGeneralReport && param.type == 'revenue' ? 'active' : ''}">
                Doanh thu
            </a>
            <a href="${ctx}/admin/reports?type=sales" class="rv-nav__sub-item ${isGeneralReport && param.type == 'sales' ? 'active' : ''}">
                BÃ¡n vÃ© theo ráº¡p
            </a>
            <a href="${ctx}/admin/reports?type=occupancy" class="rv-nav__sub-item ${isGeneralReport && param.type == 'occupancy' ? 'active' : ''}">
                Láº¥p Ä‘áº§y phÃ²ng
            </a>
            <a href="${ctx}/admin/reports?type=popular" class="rv-nav__sub-item ${isGeneralReport && param.type == 'popular' ? 'active' : ''}">
                Phim phá»• biáº¿n
            </a>
            <a href="${ctx}/admin/reports?type=peak" class="rv-nav__sub-item ${isGeneralReport && param.type == 'peak' ? 'active' : ''}">
                Giá» cao Ä‘iá»ƒm
            </a>
            <a href="${ctx}/admin/reports?type=activity" class="rv-nav__sub-item ${isGeneralReport && param.type == 'activity' ? 'active' : ''}">
                Hoáº¡t Ä‘á»™ng KH
            </a>
        </div>
    </div>

    <!-- â”€â”€ ACCOUNTS & SECURITY â”€â”€ -->
    <div class="rv-nav__label">TÃ i khoáº£n &amp; Báº£o máº­t</div>

   <div class="rv-nav__group ${isAccount ? 'open' : ''}">
        <div class="rv-nav__item ${isAccount ? 'active' : ''}">
            <i class="bi bi-people-fill"></i>
            Quáº£n trá»‹ tÃ i khoáº£n
            <i class="bi bi-chevron-right rv-nav__arrow"></i>
        </div>
        <div class="rv-nav__sub">
            <a href="${ctx}/admin/accounts/customers" class="rv-nav__sub-item ${uri.contains('/admin/accounts/customers') ? 'active' : ''}">
                KhÃ¡ch hÃ ng
            </a>
            <c:if test="${isAdminRole}">
                <a href="${ctx}/admin/accounts/staff" class="rv-nav__sub-item ${uri.contains('/admin/accounts/staff') ? 'active' : ''}">
                    NhÃ¢n viÃªn &amp; Quáº£n lÃ½
                </a>
                <a href="${ctx}/admin/accounts/roles" class="rv-nav__sub-item ${uri.contains('/admin/accounts/roles') ? 'active' : ''}">
                    Vai trÃ² &amp; Quyá»n háº¡n
                </a>
            </c:if>
        </div>
    </div>

    <div class="rv-nav__spacer"></div>
    <div class="rv-nav__divider"></div>

    <!-- System Settings -->
    <c:if test="${isAdminRole}">
        <div class="rv-nav__group">
            <a href="${ctx}/admin/settings" class="rv-nav__item ${isSettings ? 'active' : ''}">
                <i class="bi bi-gear-fill"></i>
                CÃ i Ä‘áº·t há»‡ thá»‘ng
            </a>
        </div>
    </c:if>

    <!-- Logout -->
    <div class="rv-nav__group">
        <a href="${ctx}/logout" class="rv-nav__item logout" data-confirm data-confirm-title="ÄÄƒng xuáº¥t?" data-confirm-message="Báº¡n cÃ³ cháº¯c cháº¯n muá»‘n Ä‘Äƒng xuáº¥t khá»i há»‡ thá»‘ng Rap Viá»‡t CMS?" data-confirm-type="warning" data-confirm-text="ÄÄƒng xuáº¥t">
            <i class="bi bi-box-arrow-right"></i>
            ÄÄƒng xuáº¥t
        </a>
    </div>
</aside>

<!-- Admin main content area starts immediately after sidebar. Closed in page footer -->
<main class="rv-main">


