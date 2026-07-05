<%@ page contentType="text/html; charset=UTF-8" pageEncoding="UTF-8" %>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<c:set var="ctx" value="${pageContext.request.contextPath}" />

<!DOCTYPE html>
<html lang="vi">
    <head>
        <meta charset="UTF-8">
        <title>Lịch sử giao dịch - RapViet Cinema</title>
        <link rel="stylesheet" href="${ctx}/assets/css/style.css">
        <link rel="stylesheet" href="${ctx}/assets/css/customerprofile.css">
    </head>
    <body>

        <jsp:include page="/pages/common/header.jsp">
            <jsp:param name="active" value="customerprofile"/>
        </jsp:include>

        <div class="profile-page">
            <aside class="profile-sidebar">
                <div class="profile-user-box">
                    <div class="profile-avatar">${sessionScope.user.fullName.substring(0,1)}</div>
                    <h3>${sessionScope.user.fullName}</h3>
                    <p>Thành viên RapViet Cinema</p>
                </div>

                <ul class="profile-menu">
                    <li><a href="${ctx}/profile">👤 Personal Detail</a></li>
                    <li><a href="${ctx}/favorite-movie">❤️ Favorite Film</a></li>
                    <li><a href="${ctx}/transaction-history" class="active">🧾 Transaction History</a></li>
                </ul>
            </aside>

            <section class="profile-content">
                <div class="section-head">
                    <h2>Lịch sử giao dịch</h2>
                </div>

                <div class="transaction-table-wrap">
                    <table class="transaction-table">
                        <thead>
                            <tr>
                                <th>Mã vé</th>
                                <th>Phim</th>
                                <th>Ngày đặt</th>
                                <th>Ghế</th>
                                <th>Tổng tiền</th>
                                <th>Trạng thái</th>
                                <th>Hành động</th>
                            </tr>
                        </thead>

                        <tbody>
                            <c:choose>
                                <c:when test="${empty transactions}">
                                    <tr>
                                        <td colspan="6" style="text-align:center; padding:24px;">
                                            Bạn chưa có giao dịch nào.
                                        </td>
                                    </tr>
                                </c:when>

                                <c:otherwise>
                                    <c:forEach var="t" items="${transactions}">
                                        <tr>
                                            <td>#RV${t.booking.id}</td>
                                            <td><c:out value="${t.movieTitle}"/></td>
                                            <td>${t.bookedAtLabel}</td>
                                            <td><c:out value="${t.seatLabels}"/></td>
                                            <td>${t.totalPriceLabel}</td>
                                            <td>
                                                <span class="status ${t.statusBadgeClass}">
                                                    ${t.statusLabel}
                                                </span>
                                            </td>

                                            <td>
                                                <c:choose>
                                                    <c:when test="${t.booking.status == 'PENDING'}">
                                                        <a class="btn btn-primary btn-sm"
                                                           href="${ctx}/my-booking?id=${t.booking.id}">
                                                            Thanh toán
                                                        </a>
                                                    </c:when>

                                                    <c:when test="${t.booking.status == 'CONFIRMED'}">
                                                        <a class="btn btn-success btn-sm"
                                                           href="${ctx}/booking/success?bookingId=${t.booking.id}">
                                                            Xem vé
                                                        </a>
                                                    </c:when>

                                                    <c:when test="${t.booking.status == 'CANCELLED'}">
                                                        <span class="text-muted">—</span>
                                                    </c:when>

                                                    <c:otherwise>
                                                        <span class="text-muted">—</span>
                                                    </c:otherwise>
                                                </c:choose>
                                            </td>
                                        </tr>
                                    </c:forEach>
                                </c:otherwise>
                            </c:choose>
                        </tbody>
                    </table>
                    <c:if test="${totalPages > 1}">
                        <div class="pager">
                            <c:forEach begin="1" end="${totalPages}" var="p">
                                <a class="${p == currentPage ? 'current' : ''}"
                                   href="${ctx}/transaction-history?page=${p}">
                                    ${p}
                                </a>
                            </c:forEach>
                        </div>
                    </c:if>
                </div>
            </section>
        </div>

        <jsp:include page="/pages/common/footer.jsp"/>

    </body>
</html>