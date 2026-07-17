<%@ page contentType="text/html; charset=UTF-8" pageEncoding="UTF-8" %>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<c:set var="ctx" value="${pageContext.request.contextPath}" />

<!DOCTYPE html>
<html lang="vi">
    <head>
        <meta charset="UTF-8">
        <meta name="viewport" content="width=device-width, initial-scale=1">
        <title>Chọn rạp - RapViet Cinema</title>
        <link rel="stylesheet" href="${ctx}/assets/css/style.css">
        <link rel="stylesheet" href="${ctx}/assets/css/movie.css">
    </head>
    <body>
        <jsp:include page="/pages/common/header.jsp">
            <jsp:param name="active" value="booking"/>
        </jsp:include>

        <div class="page-wrap">
            <div class="container">
                <div class="flow-head">
                    <div>
                        <div class="flow-step">Bước 1 / 5</div>
                        <h1 class="page-title">Chọn rạp</h1>
                    </div>
                    <a class="btn btn-ghost" href="${ctx}/my-bookings">Vé của tôi</a>
                </div>

                <c:choose>
                    <c:when test="${empty branches}">
                        <div class="empty">
                            <h3>Chưa có chi nhánh đang hoạt động</h3>
                        </div>
                    </c:when>
                    <c:otherwise>
                        <div class="branch-grid">
                            <c:forEach var="b" items="${branches}">
                                <div class="branch-card">
                                    <h3><c:out value="${b.branch.name}"/></h3>
                                    <div class="branch-chain"><c:out value="${b.cinemaName}"/></div>
                                    <ul class="branch-info">
                                        <li><c:out value="${b.branch.address}"/></li>
                                            <c:if test="${not empty b.branch.phone}">
                                            <li><c:out value="${b.branch.phone}"/></li>
                                            </c:if>
                                            <c:if test="${not empty b.openHoursLabel}">
                                            <li>${b.openHoursLabel}</li>
                                            </c:if>
                                        <li>${b.hallCount} phòng chiếu</li>
                                    </ul>
                                    <a class="btn btn-primary" href="${ctx}/booking/movies?branchId=${b.branch.id}">
                                        Chọn rạp
                                    </a>
                                </div>
                            </c:forEach>
                        </div>
                    </c:otherwise>
                </c:choose>
            </div>
        </div>

        <jsp:include page="/pages/common/footer.jsp" />
    </body>
</html>
