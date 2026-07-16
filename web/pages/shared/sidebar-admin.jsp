<%--
    Rạp Việt CMS — Premium Admin Sidebar Navigation
    Include sau header-admin.jsp trên mỗi trang admin.
    Active state được detect tự động qua request.getRequestURI().
--%>
<%@ page contentType="text/html; charset=UTF-8" pageEncoding="UTF-8" %>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<c:set var="ctx"    value="${pageContext.request.contextPath}" />
<c:set var="forwardUri" value="${requestScope['javax.servlet.forward.request_uri']}" />
<c:set var="uri"    value="${not empty forwardUri ? forwardUri : pageContext.request.requestURI}" />

<%-- Helper checks for active menus --%>
<c:set var="isMovie"        value="${uri.contains('/admin/moviesmanagement') || uri.contains('/moviesmanagement')}" />
<c:set var="isGenre"        value="${uri.contains('/admin/genres')}" />
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
<c:set var="isFnb"          value="${uri.contains('/admin/fnb-dashboard')}" />

<aside class="rv-sidebar">
    <!-- 🏠 Dashboard -->
    <div class="rv-nav__group">
        <a href="${ctx}/${isAdminRole ? 'admin' : 'manager'}/dashboard" class="rv-nav__item ${uri.contains('/admin/dashboard') || uri.contains('/manager/dashboard') ? 'active' : ''}">
            <i class="bi bi-grid-1x2-fill"></i>
            Dashboard
        </a>
    </div>

    <!-- 🎬 Movie Management (Admin / Manager / Staff view only) -->
    <div class="rv-nav__group ${isMovie || isGenre || isLanguage ? 'open' : ''}">
        <div class="rv-nav__item ${isMovie || isGenre || isLanguage ? 'active' : ''}">
            <i class="bi bi-film"></i>
            Quản lý phim
            <i class="bi bi-chevron-right rv-nav__arrow"></i>
        </div>
        <div class="rv-nav__sub">
            <a href="${ctx}/admin/moviesmanagement?action=list" class="rv-nav__sub-item ${isMovie && (param.action == 'list' || empty param.action || param.action == 'detail') ? 'active' : ''}">
                Danh sách phim
            </a>
            <c:if test="${isAdminRole || isManagerRole}">
                <a href="${ctx}/admin/moviesmanagement?action=new" class="rv-nav__sub-item ${isMovie && param.action == 'new' ? 'active' : ''}">
                    Thêm phim mới
                </a>
            </c:if>
            <c:if test="${isAdminRole}">
                <a href="${ctx}/admin/genres" class="rv-nav__sub-item ${isGenre ? 'active' : ''}">
                    Thể loại
                </a>
                <a href="${ctx}/admin/languages" class="rv-nav__sub-item ${isLanguage ? 'active' : ''}">
                    Ngôn ngữ
                </a>
            </c:if>
        </div>
    </div>


    <c:if test="${isAdminRole}">
        <div class="rv-nav__group">
            <a href="${ctx}/admin/tickets?action=list" class="rv-nav__item ${isTicket ? 'active' : ''}">
                <i class="bi bi-qr-code"></i>
                Quản lý vé &amp; Booking
            </a>
        </div>



        <div class="rv-nav__group">
            <a href="${ctx}/admin/branches" class="rv-nav__item ${uri.contains('/admin/branches') ? 'active' : ''}">
                <i class="bi bi-geo-alt-fill"></i>
                Quản lý Chi nhánh
            </a>
        </div>

        <div class="rv-nav__group">
            <a href="${ctx}/admin/seat-types" class="rv-nav__item ${uri.contains('/admin/seat-types') ? 'active' : ''}">
                <i class="bi bi-grid-3x3-gap-fill"></i>
                Quản lý loại ghế &amp; giá
            </a>
        </div>

        <div class="rv-nav__group">
            <a href="${ctx}/admin/notifications?action=list" class="rv-nav__item ${isNotif ? 'active' : ''}">
                <i class="bi bi-envelope-check-fill"></i>
                Lịch sử thông báo
            </a>
        </div>
    </c:if>

    <c:if test="${isManagerRole}">
        <div class="rv-nav__group">
            <a href="${ctx}/manager/halls" class="rv-nav__item ${uri.contains('/manager/halls') ? 'active' : ''}">
                <i class="bi bi-door-open-fill"></i>
                Quản lý phòng chiếu
            </a>
        </div>

        <div class="rv-nav__group">
            <a href="${ctx}/manager/seat-config" class="rv-nav__item ${uri.contains('/manager/seat-config') ? 'active' : ''}">
                <i class="bi bi-grid-3x3-gap-fill"></i>
                Cấu hình ghế
            </a>
        </div>

        <c:set var="isMovieAssign" value="${uri.contains('/manager/movie-assignments') || uri.contains('/manager/movie-durations')}" />
        <div class="rv-nav__group ${isMovieAssign ? 'open' : ''}">
            <div class="rv-nav__item ${isMovieAssign ? 'active' : ''}">
                <i class="bi bi-film"></i>
                Phân bổ phim
                <i class="bi bi-chevron-right rv-nav__arrow"></i>
            </div>
            <div class="rv-nav__sub">
                <a href="${ctx}/manager/movie-assignments/branches" class="rv-nav__sub-item ${uri.contains('/manager/movie-assignments/branches') ? 'active' : ''}">
                    Phim tại chi nhánh
                </a>
                <a href="${ctx}/manager/movie-assignments/halls" class="rv-nav__sub-item ${uri.contains('/manager/movie-assignments/halls') ? 'active' : ''}">
                    Phim tại phòng chiếu
                </a>
                <a href="${ctx}/manager/movie-durations" class="rv-nav__sub-item ${uri.contains('/manager/movie-durations') ? 'active' : ''}">
                    Thời lượng phim
                </a>
            </div>
        </div>

        <div class="rv-nav__group">
            <a href="${ctx}/manager/showtimesmanagement" class="rv-nav__item ${uri.contains('/manager/showtimesmanagement') ? 'active' : ''}">
                <i class="bi bi-calendar-week-fill"></i>
                Quản lý lịch chiếu
            </a>
        </div>
    </c:if>


    <div class="rv-nav__group">
        <a href="${ctx}/admin/fnb-dashboard"
           class="rv-nav__item ${isFnb ? 'active' : ''}">
            <i class="bi bi-cup-straw"></i>
            Quản lý F&amp;B
        </a>
    </div>
    <c:if test="${isAdminRole}">
        <div class="rv-nav__group">
            <a href="${ctx}/DiscountManager" class="rv-nav__item ${uri.contains('/DiscountManager') ? 'active' : ''}">
                <i class="bi bi-tags-fill"></i>
                Quản lý mã giảm giá
            </a>
        </div>
    </c:if>

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

    <div class="rv-nav__group ${isAccount ? 'open' : ''}">
        <div class="rv-nav__item ${isAccount ? 'active' : ''}">
            <i class="bi bi-people-fill"></i>
            Quản trị tài khoản
            <i class="bi bi-chevron-right rv-nav__arrow"></i>
        </div>
        <div class="rv-nav__sub">
            <a href="${ctx}/admin/accounts/customers" class="rv-nav__sub-item ${uri.contains('/admin/accounts/customers') ? 'active' : ''}">
                Khách hàng
            </a>
            <c:if test="${isAdminRole}">
                <a href="${ctx}/admin/accounts/staff" class="rv-nav__sub-item ${uri.contains('/admin/accounts/staff') ? 'active' : ''}">
                    Nhân viên &amp; Quản lý
                </a>
                <a href="${ctx}/admin/accounts/roles" class="rv-nav__sub-item ${uri.contains('/admin/accounts/roles') ? 'active' : ''}">
                    Vai trò &amp; Quyền hạn
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
