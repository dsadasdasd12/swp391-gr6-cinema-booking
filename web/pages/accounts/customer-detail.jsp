<%@ page contentType="text/html; charset=UTF-8" pageEncoding="UTF-8" %>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<%@ taglib prefix="fmt" uri="http://java.sun.com/jsp/jstl/fmt" %>
<%@ taglib prefix="fn" uri="http://java.sun.com/jsp/jstl/functions" %>
<c:set var="ctx" value="${pageContext.request.contextPath}" />
<c:set var="pageTitle" value="Chi tiết Khách Hàng #${c.id} — Rạp Việt CMS" scope="request" />

<%@ include file="/pages/shared/header-admin.jsp" %>
<%@ include file="/pages/shared/sidebar-admin.jsp" %>

<div class="rv-page-header">
    <div class="rv-page-header__left">
        <div class="rv-breadcrumb">
            <a href="${ctx}/admin/dashboard">Dashboard</a>
            <i class="bi bi-chevron-right rv-breadcrumb__sep"></i>
            <a href="${ctx}/admin/accounts/customers">Khách hàng</a>
            <i class="bi bi-chevron-right rv-breadcrumb__sep"></i>
            <span class="rv-breadcrumb__current">Chi tiết #${c.id}</span>
        </div>
        <h1 class="rv-page-title">Thông tin Khách Hàng</h1>
    </div>
    <div class="rv-page-header__right">
        <a href="${ctx}/admin/accounts/customers" class="rv-btn rv-btn--secondary">
            <i class="bi bi-arrow-left"></i> Quay lại
        </a>
    </div>
</div>

<div class="row g-4">
    <!-- Cột trái: Thông tin User -->
    <div class="col-lg-4">
        <div class="rv-card">
            <div class="rv-card__header">
                <h3 class="rv-card__title">Hồ sơ cá nhân</h3>
            </div>
            <div class="rv-card__body">
                <div class="d-flex align-items-center mb-4 pb-3 border-bottom">
                    <div style="width: 60px; height: 60px; border-radius: 50%; background: var(--n-200); color: var(--n-600); display: flex; align-items: center; justify-content: center; font-size: 24px; font-weight: 700; margin-right: 1rem;">
                        <c:out value="${c.fullName.substring(0,1).toUpperCase()}" />
                    </div>
                    <div>
                        <h4 style="margin: 0; font-size: 1.1rem; font-weight: 700;"><c:out value="${c.fullName}" /></h4>
                        <div style="font-size: 0.85rem; color: var(--n-600);"><c:out value="${c.email}" /></div>
                    </div>
                </div>

                <div class="mb-3">
                    <label class="rv-form__label" style="font-size: 0.8rem;">Số điện thoại</label>
                    <div style="font-weight: 500;"><c:out value="${not empty c.phone ? c.phone : 'Chưa cập nhật'}" /></div>
                </div>

                <div class="mb-3">
                    <label class="rv-form__label" style="font-size: 0.8rem;">Trạng thái tài khoản</label>
                    <div>
                        <c:choose>
                            <c:when test="${c.active}"><span class="rv-badge rv-badge--active">Đang hoạt động</span></c:when>
                            <c:otherwise><span class="rv-badge rv-badge--blocked">Bị khóa</span></c:otherwise>
                        </c:choose>
                        
                        <c:choose>
                            <c:when test="${c.emailVerified}"><span class="rv-badge rv-badge--active">Đã xác thực Email</span></c:when>
                            <c:otherwise><span class="rv-badge rv-badge--pending">Chưa xác thực Email</span></c:otherwise>
                        </c:choose>
                    </div>
                </div>

                <div class="mb-3">
                    <label class="rv-form__label" style="font-size: 0.8rem;">Ngày tham gia</label>
                    <div style="font-weight: 500;">
                        <c:choose>
                            <c:when test="${not empty c.createdAtLabel}">
                                <fmt:parseDate value="${c.createdAtLabel}" pattern="yyyy-MM-dd'T'HH:mm" var="parsedDate" type="both" />
                                <fmt:formatDate value="${parsedDate}" pattern="dd/MM/yyyy HH:mm" />
                            </c:when>
                            <c:otherwise>—</c:otherwise>
                        </c:choose>
                    </div>
                </div>
            </div>
        </div>
    </div>

    <!-- Cột phải: Lịch sử đặt vé -->
    <div class="col-lg-8" id="booking-history">
        <div class="rv-card">
            <div class="rv-card__header d-flex justify-content-between align-items-center">
                <h3 class="rv-card__title">Lịch sử đặt vé (${fn:length(bookingHistory)})</h3>
            </div>
            
            <c:choose>
                <c:when test="${empty bookingHistory}">
                    <div class="rv-card__body text-center py-5">
                        <i class="bi bi-ticket-detailed" style="font-size: 3rem; color: var(--n-300);"></i>
                        <h4 class="mt-3 mb-1" style="font-size: 1.1rem; color: var(--n-600);">Chưa có giao dịch</h4>
                        <p style="color: var(--n-500); font-size: 0.9rem;">Khách hàng này chưa từng đặt vé trên hệ thống.</p>
                    </div>
                </c:when>
                <c:otherwise>
                    <div class="rv-table-responsive">
                        <table class="rv-table">
                            <thead>
                                <tr>
                                    <th>Mã vé</th>
                                    <th>Phim</th>
                                    <th>Suất chiếu</th>
                                    <th>Tổng tiền</th>
                                    <th>Trạng thái</th>
                                </tr>
                            </thead>
                            <tbody>
                                <c:forEach var="b" items="${bookingHistory}">
                                    <tr>
                                        <td><strong style="color: var(--primary);">#<c:out value="${b.id}"/></strong></td>
                                        <td>
                                            <div style="font-weight: 600;"><c:out value="${b.movieTitle}"/></div>
                                        </td>
                                        <td>
                                            <c:out value="${b.startTimeLabel}"/><br>
                                            <small style="color: var(--n-500);"><c:out value="${b.branchName}"/> - <c:out value="${b.hallName}"/></small>
                                        </td>
                                        <td style="font-weight: 600;">
                                            <fmt:formatNumber value="${b.finalPrice}" pattern="#,##0"/> đ
                                        </td>
                                        <td>
                                            <c:choose>
                                                <c:when test="${b.status == 'SUCCESS'}"><span class="rv-badge rv-badge--success">Thành công</span></c:when>
                                                <c:when test="${b.status == 'PENDING'}"><span class="rv-badge rv-badge--pending">Chờ thanh toán</span></c:when>
                                                <c:when test="${b.status == 'CANCELLED'}"><span class="rv-badge rv-badge--danger">Đã hủy</span></c:when>
                                                <c:when test="${b.status == 'FAILED'}"><span class="rv-badge rv-badge--danger">Thất bại</span></c:when>
                                                <c:when test="${b.status == 'REFUNDED'}"><span class="rv-badge rv-badge--blocked">Hoàn tiền</span></c:when>
                                                <c:when test="${b.status == 'USED'}"><span class="rv-badge rv-badge--active">Đã sử dụng</span></c:when>
                                                <c:otherwise><span class="rv-badge"><c:out value="${b.status}"/></span></c:otherwise>
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
</div>

</main>
</div>
<script src="https://cdn.jsdelivr.net/npm/bootstrap@5.3.3/dist/js/bootstrap.bundle.min.js"></script>
</body>
</html>
