<%@ page contentType="text/html; charset=UTF-8" pageEncoding="UTF-8" %>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<c:set var="ctx" value="${pageContext.request.contextPath}" />

<!DOCTYPE html>
<html lang="vi">
    <head>
        <meta charset="UTF-8">
        <meta name="viewport" content="width=device-width, initial-scale=1">
        <title>Booking theo chi nhánh - RapViet Cinema</title>
        <link rel="stylesheet" href="${ctx}/assets/css/style.css">
        <link rel="stylesheet" href="${ctx}/assets/css/movie.css">
    </head>
    <body>
        <jsp:include page="/pages/common/header.jsp">
            <jsp:param name="active" value="staff-bookings"/>
        </jsp:include>

        <div class="page-wrap">
            <div class="container">
                <div class="flow-head">
                    <div>
                        <h1 class="page-title">Booking theo chi nhánh</h1>
                        <div class="result-meta">Chỉ hiển thị booking thuộc chi nhánh được phân công.</div>
                    </div>
                </div>

                <form class="st-toolbar" method="get" action="${ctx}/staff/booking-staff-list">
                    <div class="filter-field grow">
                        <label for="q">Tìm kiếm</label>
                        <input id="q" name="q" type="text" value="<c:out value='${keyword}'/>"
                               placeholder="Mã booking, phim, khách hàng">
                    </div>
                    <div class="filter-field">
                        <label for="status">Trạng thái</label>
                        <select id="status" name="status">
                            <option value="">Tất cả</option>
                            <option value="PENDING" ${selectedStatus == 'PENDING' ? 'selected' : ''}>PENDING</option>
                            <option value="CONFIRMED" ${selectedStatus == 'CONFIRMED' ? 'selected' : ''}>CONFIRMED</option>
                            <option value="CHECKED_IN" ${selectedStatus == 'CHECKED_IN' ? 'selected' : ''}>CHECKED_IN</option>
                            <option value="USED" ${selectedStatus == 'USED' ? 'selected' : ''}>USED</option>
                            <option value="CANCELLED" ${selectedStatus == 'CANCELLED' ? 'selected' : ''}>CANCELLED</option>
                        </select>
                    </div>
                    <div class="filter-field">
                        <label>&nbsp;</label>
                        <button class="btn btn-primary" type="submit">Lọc</button>
                    </div>
                </form>

                <c:choose>
                    <c:when test="${empty bookings}">
                        <div class="empty">
                            <h3>Không có booking phù hợp</h3>
                        </div>
                    </c:when>
                    <c:otherwise>
                        <div class="booking-list">
                            <c:forEach var="bk" items="${bookings}">
                                <a class="booking-row" href="${ctx}/staff/booking-staff?id=${bk.booking.id}">
                                    <div class="booking-main">
                                        <h3>#${bk.booking.id} · <c:out value="${bk.movieTitle}"/></h3>
                                        <div class="booking-meta">
                                            <c:out value="${bk.customerName}"/> · <c:out value="${bk.customerEmail}"/>
                                        </div>
                                        <div class="booking-meta">
                                            ${bk.showTimeLabel} · <c:out value="${bk.branchName}"/> · <c:out value="${bk.hallName}"/>
                                        </div>
                                        <div class="booking-meta">
                                            ${bk.seatCount} ghế (<c:out value="${bk.seatLabels}"/>)
                                        </div>
                                    </div>
                                    <div class="booking-side">
                                        <span class="badge ${bk.statusBadgeClass}">${bk.statusLabel}</span>
                                        <div class="booking-total">${bk.totalPriceLabel}</div>
                                    </div>
                                </a>
                            </c:forEach>
                        </div>
                    </c:otherwise>
                </c:choose>
            </div>
        </div>

        <jsp:include page="/pages/common/footer.jsp" />
    </body>
</html>
