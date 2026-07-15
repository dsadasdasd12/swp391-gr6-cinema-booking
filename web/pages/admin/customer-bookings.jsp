<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c" %>
<%@ taglib uri="http://java.sun.com/jsp/jstl/fmt" prefix="fmt" %>
<c:set var="ctx" value="${pageContext.request.contextPath}"/>

<!DOCTYPE html>
<html lang="vi">
<head>
    <meta charset="UTF-8">
    <title>Lịch sử đặt vé khách hàng</title>
    <!-- Fonts -->
    <link rel="preconnect" href="https://fonts.googleapis.com">
    <link rel="preconnect" href="https://fonts.gstatic.com" crossorigin>
    <link href="https://fonts.googleapis.com/css2?family=Inter:wght@400;500;600;700&display=swap" rel="stylesheet">
    <!-- Icons -->
    <link rel="stylesheet" href="https://cdn.jsdelivr.net/npm/bootstrap-icons@1.11.3/font/bootstrap-icons.min.css">
    <!-- CSS -->
    <link rel="stylesheet" href="https://cdn.jsdelivr.net/npm/bootstrap@5.3.3/dist/css/bootstrap.min.css">
    <link rel="stylesheet" href="${ctx}/css/admin.css">
</head>
<body>

<div class="rv-admin-layout">
    <jsp:include page="/pages/shared/sidebar-admin.jsp"/>

    <main class="rv-admin-main">
        <jsp:include page="/pages/shared/header-admin.jsp"/>

        <div class="rv-admin-content">
            <div class="d-flex align-items-center justify-content-between mb-4">
                <div class="d-flex align-items-center">
                    <a href="${ctx}/admin/accounts/customers" class="rv-btn rv-btn--ghost me-3">
                        <i class="bi bi-arrow-left"></i> Quay lại
                    </a>
                    <h1 class="h3 mb-0" style="color: #fff;">
                        Lịch sử đặt vé: <span style="color: #fff;">${customer.fullName}</span>
                    </h1>
                </div>
                <form action="${ctx}/admin/bookings" method="get" class="d-flex align-items-center gap-2">
                    <input type="hidden" name="customerId" value="${customer.id}">
                    <select name="status" class="rv-input" style="width: auto; min-width: 150px;" onchange="this.form.submit()">
                        <option value="">Tất cả trạng thái</option>
                        <option value="CONFIRMED" ${statusFilter == 'CONFIRMED' ? 'selected' : ''}>Confirmed</option>
                        <option value="PENDING" ${statusFilter == 'PENDING' ? 'selected' : ''}>Pending</option>
                        <option value="CHECKED_IN" ${statusFilter == 'CHECKED_IN' ? 'selected' : ''}>Checked-in</option>
                        <option value="USED" ${statusFilter == 'USED' ? 'selected' : ''}>Used</option>
                        <option value="CANCELLED" ${statusFilter == 'CANCELLED' ? 'selected' : ''}>Cancelled</option>
                    </select>
                </form>
            </div>

            <!-- Customer Info -->
            <div class="rv-card mb-4">
                <div class="rv-card__body" style="color: #fff;">
                    <div class="row">
                        <div class="col-md-4">
                            <strong>Email:</strong> <span style="color: #fff;">${customer.email}</span>
                        </div>
                        <div class="col-md-4">
                            <strong>Điện thoại:</strong> <span style="color: #fff;">${not empty customer.phone ? customer.phone : '—'}</span>
                        </div>
                        <div class="col-md-4">
                            <strong>Trạng thái:</strong> 
                            <c:choose>
                                <c:when test="${!customer.active}">
                                    <span class="rv-badge rv-badge--blocked">Bị khóa</span>
                                </c:when>
                                <c:when test="${customer.emailVerified}">
                                    <span class="rv-badge rv-badge--success">Hoạt động</span>
                                </c:when>
                                <c:otherwise>
                                    <span class="rv-badge rv-badge--pending">Chờ xác thực</span>
                                </c:otherwise>
                            </c:choose>
                        </div>
                    </div>
                </div>
            </div>

            <!-- Bookings Table -->
            <div class="rv-card">
                <div class="rv-card__body p-0">
                    <div class="table-responsive">
                        <table class="rv-table">
                            <thead>
                                <tr>
                                    <th>Mã ĐV</th>
                                    <th>Thời gian đặt</th>
                                    <th>Phim</th>
                                    <th>Suất chiếu</th>
                                    <th>Ghế</th>
                                    <th>Tổng tiền</th>
                                    <th>Trạng thái</th>
                                </tr>
                            </thead>
                            <tbody>
                            <c:choose>
                                <c:when test="${empty bookings}">
                                    <tr>
                                        <td colspan="7" class="text-center py-4 text-muted">
                                            <i class="bi bi-inbox fs-2 d-block mb-2"></i>
                                            Khách hàng chưa có lượt đặt vé nào.
                                        </td>
                                    </tr>
                                </c:when>
                                <c:otherwise>
                                    <c:forEach var="b" items="${bookings}">
                                        <tr>
                                            <td><span class="fw-medium text-primary">#<fmt:formatNumber value="${b.booking.id}" pattern="000000"/></span></td>
                                            <td><fmt:formatDate value="${b.booking.bookedAt}" pattern="dd/MM/yyyy HH:mm"/></td>
                                            <td><span class="fw-medium">${b.movieTitle}</span></td>
                                            <td>
                                                <div class="small">
                                                    <div><i class="bi bi-clock me-1 text-muted"></i>${b.showTimeLabel}</div>
                                                    <div><i class="bi bi-geo-alt me-1 text-muted"></i>${b.branchName} - ${b.hallName}</div>
                                                </div>
                                            </td>
                                            <td style="max-width: 200px;" class="text-truncate" title="${b.seatLabels}">
                                                ${b.seatLabels}
                                            </td>
                                            <td><span class="text-danger fw-semibold"><fmt:formatNumber value="${b.booking.totalPrice}" pattern="#,###"/>đ</span></td>
                                            <td>
                                                <c:choose>
                                                    <c:when test="${b.booking.status == 'CONFIRMED'}">
                                                        <span class="rv-badge rv-badge--success">Confirmed</span>
                                                    </c:when>
                                                    <c:when test="${b.booking.status == 'PENDING'}">
                                                        <span class="rv-badge rv-badge--pending">Pending</span>
                                                    </c:when>
                                                    <c:when test="${b.booking.status == 'CANCELLED'}">
                                                        <span class="rv-badge rv-badge--blocked">Cancelled</span>
                                                    </c:when>
                                                    <c:when test="${b.booking.status == 'CHECKED_IN'}">
                                                        <span class="rv-badge rv-badge--success">Checked-in</span>
                                                    </c:when>
                                                    <c:when test="${b.booking.status == 'USED'}">
                                                        <span class="rv-badge" style="background: var(--n-800); color: var(--n-400); border-color: var(--n-700);">Used</span>
                                                    </c:when>
                                                    <c:otherwise>
                                                        <span class="rv-badge rv-badge--pending">${b.booking.status}</span>
                                                    </c:otherwise>
                                                </c:choose>
                                            </td>
                                        </tr>
                                    </c:forEach>
                                </c:otherwise>
                            </c:choose>
                            </tbody>
                        </table>
                    </div>
                </div>
            </div>

        </div>
    </main>
</div>

<script src="https://cdn.jsdelivr.net/npm/bootstrap@5.3.3/dist/js/bootstrap.bundle.min.js"></script>
</body>
</html>
