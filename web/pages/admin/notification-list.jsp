<%--
    RapViet Admin — Lịch sử thông báo (notification-list.jsp)
    Servlet: NotificationController ?action=list
--%>
<%@ page contentType="text/html; charset=UTF-8" pageEncoding="UTF-8" %>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<%@ taglib prefix="fn" uri="http://java.sun.com/jsp/jstl/functions" %>
<c:set var="ctx" value="${pageContext.request.contextPath}" />
<c:set var="pageTitle" value="Lịch sử thông báo — RapViet Admin" scope="request" />

<%@ include file="/pages/shared/header-admin.jsp" %>
<%@ include file="/pages/shared/sidebar-admin.jsp" %>

<c:if test="${not empty sessionScope.flashSuccess}">
    <div class="flash-success"><i class="bi bi-check-circle-fill"></i> <c:out value="${sessionScope.flashSuccess}"/></div>
    <c:remove var="flashSuccess" scope="session"/>
</c:if>

<div class="mb-3">
    <h1 style="font-size:1.5rem;font-weight:700;margin:0;">Lịch sử thông báo</h1>
    <p style="color:var(--clr-muted);font-size:.85rem;margin:.25rem 0 0;">
        Tổng: <strong style="color:var(--clr-text)">${totalItems}</strong> bản ghi
    </p>
</div>

<div class="rv-toolbar mb-4">
    <form method="get" action="${ctx}/admin/notifications" class="d-flex align-items-center flex-wrap gap-3 w-100" style="margin: 0; padding: 0; border: none; background: none;">
        <input type="hidden" name="action" value="list">

        <!-- Search -->
        <div class="rv-toolbar__search">
            <i class="bi bi-search"></i>
            <input type="text" name="keyword" id="logSearch" placeholder="Email, tiêu đề..." value="<c:out value='${keyword}'/>"/>
        </div>

        <!-- Filter Type -->
        <div class="rv-toolbar__filter">
            <select name="type" id="typeFilter">
                <option value="">Tất cả loại</option>
                <option value="BOOKING_CONFIRM" ${type == 'BOOKING_CONFIRM' ? 'selected' : ''}>Xác nhận đặt vé</option>
                <option value="PAYMENT_CONFIRM" ${type == 'PAYMENT_CONFIRM' ? 'selected' : ''}>Xác nhận thanh toán</option>
                <option value="SYSTEM" ${type == 'SYSTEM' ? 'selected' : ''}>Hệ thống</option>
            </select>
        </div>

        <!-- Filter Status -->
        <div class="rv-toolbar__filter">
            <select name="status" id="statusFilter">
                <option value="">Tất cả trạng thái</option>
                <option value="SENT" ${status == 'SENT' ? 'selected' : ''}>Đã gửi</option>
                <option value="FAILED" ${status == 'FAILED' ? 'selected' : ''}>Thất bại</option>
                <option value="PENDING" ${status == 'PENDING' ? 'selected' : ''}>Đang chờ</option>
            </select>
        </div>

        <!-- Action buttons -->
        <div class="d-flex align-items-center gap-2 ms-auto">
            <button type="submit" class="rv-btn rv-btn--secondary rv-btn--sm">
                Lọc dữ liệu
            </button>
            <a href="${ctx}/admin/notifications?action=list" class="rv-btn rv-btn--ghost rv-btn--sm">
                Xóa lọc
            </a>
            <a href="javascript:location.reload();" class="rv-btn rv-btn--refresh" title="Làm mới">
                <i class="bi bi-arrow-clockwise"></i>
            </a>
        </div>
    </form>
</div>

<div class="rv-card">
    <c:choose>
        <c:when test="${empty logs}">
            <div class="rv-empty">
                <i class="bi bi-envelope rv-empty__icon"></i>
                <div class="rv-empty__title">Không tìm thấy dữ liệu</div>
                <div class="rv-empty__message">Chưa có thông báo giao dịch nào được gửi.</div>
            </div>
        </c:when>
        <c:otherwise>
            <div class="rv-table-responsive">
                <table class="rv-table" id="logTable">
                    <thead>
                        <tr>
                            <th style="width: 50px;">#</th>
                            <th>Loại</th>
                            <th>Người nhận</th>
                            <th>Tiêu đề</th>
                            <th>Trạng thái</th>
                            <th style="text-align:center;">Thử lại</th>
                            <th>Ngày gửi</th>
                            <th>Ghi chú</th>
                            <th style="text-align:right;">Thao tác</th>
                        </tr>
                    </thead>
                    <tbody id="logTbody">
                        <c:forEach var="l" items="${logs}" varStatus="i">
                            <tr class="log-row"
                                data-type="${l.notificationType}"
                                data-status="${l.status}"
                                data-search="${fn:toLowerCase(l.recipientEmail)} ${fn:toLowerCase(l.subject)}">
                                <td style="color:var(--clr-muted);font-size:.8rem;">${l.id}</td>
                                <td>
                                    <span style="font-size:.78rem;padding:.2rem .5rem;border-radius:6px;
                                        ${l.notificationType == 'BOOKING_CONFIRM' ? 'background:rgba(25,195,125,.12);color:#19c37d;' : ''}
                                        ${l.notificationType == 'PAYMENT_CONFIRM' ? 'background:rgba(59,130,246,.12);color:#3b82f6;' : ''}
                                        ${l.notificationType == 'SYSTEM'          ? 'background:rgba(139,143,168,.12);color:#8b8fa8;' : ''}">
                                        <c:out value="${l.typeLabel}"/>
                                    </span>
                                </td>
                                <td style="font-size:.85rem;"><c:out value="${l.recipientEmail}"/></td>
                                <td style="font-size:.85rem;max-width:250px;overflow:hidden;text-overflow:ellipsis;white-space:nowrap;">
                                    <c:out value="${l.subject}"/>
                                </td>
                                <td>
                                    <span class="badge-status ${l.statusBadgeClass}">
                                        <c:out value="${l.statusLabel}"/>
                                    </span>
                                </td>
                                <td style="text-align:center;font-size:.85rem;">${l.retryCount}</td>
                                <td style="font-size:.8rem;color:var(--n-500);">
                                    <c:out value="${l.sentAtLabel}"/>
                                </td>
                                <td style="font-size:.78rem;color:${l.status == 'FAILED' ? '#ff6b6b' : 'var(--n-400)'};max-width:150px;overflow:hidden;text-overflow:ellipsis;white-space:nowrap;"
                                    title="${l.errorMessage}">
                                    <c:out value="${l.errorMessage}" default="—"/>
                                </td>
                                <td style="text-align:right;">
                                    <form action="${ctx}/admin/notifications" method="post" class="d-inline"
                                          onsubmit="return confirm('Bạn có chắc chắn muốn xóa thông báo này?');">
                                        <input type="hidden" name="action" value="delete">
                                        <input type="hidden" name="id" value="${l.id}">
                                        <button type="submit" class="rv-btn rv-btn--icon" title="Xóa thông báo">
                                            <i class="bi bi-trash" style="color: #ff6b6b;"></i>
                                        </button>
                                    </form>
                                </td>
                            </tr>
                        </c:forEach>
                    </tbody>
                </table>
            </div>
        </c:otherwise>
    </c:choose>

    <div style="margin-top: 18px;">
        <c:if test="${totalPages > 1}">
            <jsp:include page="/pages/shared/pagination.jsp">
                <jsp:param name="currentPage" value="${currentPage}" />
                <jsp:param name="totalPages" value="${totalPages}" />
                <jsp:param name="totalItems" value="${totalItems}" />
                <jsp:param name="pageSize" value="${pageSize}" />
                <jsp:param name="baseUrl" value="${ctx}/admin/notifications?action=list&keyword=${param.keyword}&type=${param.type}&status=${param.status}" />
            </jsp:include>
        </c:if>
    </div>
</div>

</main>
</div>

<script src="https://cdn.jsdelivr.net/npm/bootstrap@5.3.3/dist/js/bootstrap.bundle.min.js"
        integrity="sha384-YvpcrYf0tY3lHB60NNkmXc5s9fDVZLESaAA55NDzOxhy9GkcIdslK1eN7N6jIeHz"
        crossorigin="anonymous"></script>

</body>
</html>
