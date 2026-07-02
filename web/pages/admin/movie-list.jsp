<%--
    Rạp Việt CMS — Premium Movie Management List
    URL: /admin/moviesmanagement?action=list
    Servlet: AdminMovieController
--%>
<%@ page contentType="text/html; charset=UTF-8" pageEncoding="UTF-8" %>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<%@ taglib prefix="fn" uri="http://java.sun.com/jsp/jstl/functions" %>
<c:set var="ctx" value="${pageContext.request.contextPath}" />
<c:set var="pageTitle" value="Quản lý phim — Rạp Việt CMS" scope="request" />

<%@ include file="/pages/shared/header-admin.jsp" %>
<%@ include file="/pages/shared/sidebar-admin.jsp" %>

<!-- ── PAGE HEADER ── -->
<div class="rv-page-header">
    <div class="rv-page-header__left">
        <!-- Breadcrumb -->
        <div class="rv-breadcrumb">
            <a href="${ctx}/admin/dashboard">Dashboard</a>
            <i class="bi bi-chevron-right rv-breadcrumb__sep"></i>
            <span class="rv-breadcrumb__current">Quản lý phim</span>
        </div>
        <h1 class="rv-page-title">Danh sách phim</h1>
        <p class="rv-page-subtitle">Quản lý, chỉnh sửa và phân loại danh mục phim toàn hệ thống.</p>
    </div>
    <c:if test="${sessionScope.user.admin}">
        <div class="rv-page-header__right">
            <a href="${ctx}/admin/moviesmanagement?action=new" class="rv-btn rv-btn--primary">
                <i class="bi bi-plus-lg"></i>Thêm phim mới
            </a>
        </div>
    </c:if>
</div>

<!-- ── TOOLBAR / FILTERS ── -->
<div class="rv-toolbar">
    <form method="get" action="${ctx}/admin/moviesmanagement" class="d-flex align-items-center flex-wrap gap-3 w-100" style="margin: 0; padding: 0; border: none; background: none;">
        <input type="hidden" name="action" value="list">
        
        <!-- Search Input -->
        <div class="rv-toolbar__search">
            <i class="bi bi-search"></i>
            <input type="text" name="keyword" placeholder="Tìm theo tên phim, đạo diễn..." value="<c:out value='${keyword}'/>" autofocus="">
        </div>

        <!-- Filter Status -->
        <div class="rv-toolbar__filter">
            <select name="status">
                <option value="">Tất cả trạng thái</option>
                <option value="NOW_SHOWING" ${status == 'NOW_SHOWING' ? 'selected' : ''}>Đang chiếu (Now Showing)</option>
                <option value="COMING_SOON" ${status == 'COMING_SOON' ? 'selected' : ''}>Sắp chiếu (Coming Soon)</option>
                <option value="ENDED" ${status == 'ENDED' ? 'selected' : ''}>Đã kết thúc (Ended)</option>
            </select>
        </div>

        <!-- Reset & Refresh Buttons -->
        <div class="d-flex align-items-center gap-2 ms-auto">
            <button type="submit" class="rv-btn rv-btn--secondary rv-btn--sm">
                Lọc dữ liệu
            </button>
            <a href="${ctx}/admin/moviesmanagement?action=list" class="rv-btn rv-btn--ghost rv-btn--sm" title="Xóa lọc">
                Xóa lọc
            </a>
            <a href="javascript:location.reload();" class="rv-btn rv-btn--refresh" title="Làm mới bảng">
                <i class="bi bi-arrow-clockwise"></i>
            </a>
        </div>
    </form>
</div>

<!-- ── DATA TABLE ── -->
<div class="rv-card">
    <c:choose>
        <c:when test="${empty movies}">
            <div class="rv-empty">
                <i class="bi bi-collection-play rv-empty__icon"></i>
                <div class="rv-empty__title">Không tìm thấy phim nào</div>
                <div class="rv-empty__message">Hệ thống chưa có bộ phim nào khớp với điều kiện tìm kiếm hoặc dữ liệu trống.</div>
                <c:if test="${sessionScope.user.admin}">
                    <a href="${ctx}/admin/moviesmanagement?action=new" class="rv-btn rv-btn--primary rv-btn--sm" style="margin-top: 20px;">
                        <i class="bi bi-plus-lg"></i>Tạo phim ngay
                    </a>
                </c:if>
            </div>
        </c:when>
        <c:otherwise>
            <div class="rv-table-responsive">
                <table class="rv-table">
                    <thead>
                        <tr>
                            <th style="width: 60px; text-align: center;">No.</th>
                            <th style="width: 80px; text-align: center;">Poster</th>
                            <th>Tên phim</th>
                            <th>Thể loại</th>
                            <th>Thời lượng</th>
                            <th>Trạng thái</th>
                            <c:if test="${sessionScope.user.admin}">
                                <th style="width: 140px; text-align: center;">Thao tác</th>
                            </c:if>
                        </tr>
                    </thead>
                    <tbody>
                        <c:forEach var="m" items="${movies}" varStatus="status">
                            <tr id="movie-row-${m.id}">
                                <!-- Column: No -->
                                <td style="text-align: center; font-weight: 600; color: var(--n-500);">
                                    ${status.index + 1}
                                </td>
                                
                                <!-- Column: Poster Thumbnail -->
                                <td style="text-align: center;">
                                    <div class="rv-table-poster">
                                        <c:choose>
                                            <c:when test="${not empty m.posterWebPath}">
                                                <img src="${ctx}/${m.posterWebPath}" alt="poster" class="rv-skeleton rv-skeleton--img" onload="this.classList.remove('rv-skeleton', 'rv-skeleton--img')">
                                            </c:when>
                                            <c:otherwise>
                                                <div class="placeholder">
                                                    <i class="bi bi-image"></i>
                                                </div>
                                            </c:otherwise>
                                        </c:choose>
                                    </div>
                                </td>

                                <!-- Column: Movie Title -->
                                <td>
                                    <div style="font-weight: 600; font-size: 14px; color: var(--n-900);">
                                        <c:out value="${m.title}"/>
                                    </div>
                                    <div style="font-size: 12px; color: var(--n-400); margin-top: 2px;">
                                        Đạo diễn: <c:out value="${not empty m.director ? m.director : 'Chưa cập nhật'}"/>
                                    </div>
                                </td>

                                <!-- Column: Category tags -->
                                <td>
                                    <c:out value="${not empty m.categoryNames ? m.categoryNames : '—'}"/>
                                </td>

                                <!-- Column: Duration -->
                                <td>
                                    <div style="font-weight: 500;">
                                        <i class="bi bi-clock-history" style="color: var(--n-400); margin-right: 4px;"></i>
                                        <c:out value="${m.durationLabel}"/>
                                    </div>
                                </td>

                                <!-- Column: Status badge -->
                                <td>
                                    <c:choose>
                                        <c:when test="${m.status == 'NOW_SHOWING'}">
                                            <span class="rv-badge rv-badge--nowshowing">Đang chiếu</span>
                                        </c:when>
                                        <c:when test="${m.status == 'COMING_SOON'}">
                                            <span class="rv-badge rv-badge--comingsoon">Sắp chiếu</span>
                                        </c:when>
                                        <c:otherwise>
                                            <span class="rv-badge rv-badge--ended">Đã kết thúc</span>
                                        </c:otherwise>
                                    </c:choose>
                                </td>

                                <!-- Column: Actions (Admin CRUD options) -->
                                <c:if test="${sessionScope.user.admin}">
                                    <td>
                                        <div class="d-flex align-items-center justify-content-center gap-2">
                                            <a href="${ctx}/admin/moviesmanagement?action=detail&id=${m.id}" class="rv-btn rv-btn--ghost rv-btn--icon" data-tooltip="Xem chi tiết" style="border:none">
                                                <i class="bi bi-eye"></i>
                                            </a>
                                            <a href="${ctx}/admin/moviesmanagement?action=edit&id=${m.id}" class="rv-btn rv-btn--ghost rv-btn--icon" data-tooltip="Sửa thông tin" style="border:none">
                                                <i class="bi bi-pencil-square"></i>
                                            </a>
                                            <c:choose>
                                                <c:when test="${m.hasActiveShowtimes}">
                                                    <!-- Disabled delete button if movie has active showtimes -->
                                                    <button class="rv-btn rv-btn--ghost rv-btn--icon disabled" style="opacity: 0.35; border:none" data-tooltip="Không thể xóa: Có suất chiếu hoạt động" disabled>
                                                        <i class="bi bi-trash3-fill"></i>
                                                    </button>
                                                </c:when>
                                                <c:otherwise>
                                                    <a href="${ctx}/admin/moviesmanagement?action=delete&id=${m.id}" class="rv-btn rv-btn--ghost rv-btn--icon" style="color: var(--danger); border:none"
                                                       data-tooltip="Xóa phim"
                                                       data-confirm
                                                       data-confirm-title="Xóa phim này?"
                                                       data-confirm-message="Bạn chắc chắn muốn xóa phim '<strong>${m.title}</strong>'? Hành động này không thể hoàn tác và sẽ xóa toàn bộ dữ liệu liên quan."
                                                       data-confirm-type="danger"
                                                       data-confirm-text="Xóa phim">
                                                        <i class="bi bi-trash3-fill"></i>
                                                    </a>
                                                </c:otherwise>
                                            </c:choose>
                                        </div>
                                    </td>
                                </c:if>
                            </tr>
                        </c:forEach>
                    </tbody>
                </table>
            </div>
            
            <!-- Simple Pagination Integration -->
            <jsp:include page="/pages/shared/pagination.jsp">
                <jsp:param name="currentPage" value="${not empty currentPage ? currentPage : 1}" />
                <jsp:param name="totalPages" value="${not empty totalPages ? totalPages : 1}" />
                <jsp:param name="totalItems" value="${not empty totalItems ? totalItems : fn:length(movies)}" />
                <jsp:param name="pageSize" value="${not empty pageSize ? pageSize : 10}" />
                <jsp:param name="baseUrl" value="${ctx}/admin/moviesmanagement?action=list&keyword=${keyword}&status=${status}" />
            </jsp:include>
        </c:otherwise>
    </c:choose>
</div>

<!-- Layout ends -->
</main>
</div>

</body>
</html>
