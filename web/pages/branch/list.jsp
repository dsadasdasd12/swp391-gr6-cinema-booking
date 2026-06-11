<%--
    RapViet Cinema - Danh sách hệ thống rạp (Xem chi nhánh)
    Module: Duyệt phim - View cinema branches   (Group6 - DuyThai)
    Được phục vụ bởi controller.BranchListController  ->  URL /branches
    View chỉ dùng JSTL + EL + component, KHÔNG nhúng code Java.
--%>
<%@ page contentType="text/html; charset=UTF-8" pageEncoding="UTF-8" %>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<c:set var="ctx" value="${pageContext.request.contextPath}" />

<!DOCTYPE html>
<html lang="vi">
<head>
    <meta charset="UTF-8">
    <meta name="viewport" content="width=device-width, initial-scale=1">
    <title>Hệ thống rạp - RapViet Cinema</title>
    <link rel="stylesheet" href="${ctx}/assets/css/style.css">
    <link rel="stylesheet" href="${ctx}/assets/css/movie.css">
</head>
<body>
    <jsp:include page="/pages/common/header.jsp">
        <jsp:param name="active" value="branches"/>
    </jsp:include>

    <div class="page-wrap">
    <div class="container">
        <h1 class="page-title">Hệ thống rạp RapViet</h1>

        <c:choose>
            <%-- ── Chưa có chi nhánh nào ── --%>
            <c:when test="${empty branches}">
                <div class="empty">
                    <h3>Chưa có chi nhánh</h3>
                    <p>Hệ thống rạp đang được cập nhật, vui lòng quay lại sau.</p>
                </div>
            </c:when>

            <%-- ── Lưới các chi nhánh ── --%>
            <c:otherwise>
                <div class="branch-grid">
                    <%-- b là dto.BranchView: b.branch = entity Branch (cột DB),
                         b.cinemaName/b.hallCount/b.openHoursLabel = dữ liệu ghép để hiển thị --%>
                    <c:forEach var="b" items="${branches}">
                        <div class="branch-card">
                            <h3><c:out value="${b.branch.name}"/></h3>
                            <div class="branch-chain"><c:out value="${b.cinemaName}"/></div>
                            <ul class="branch-info">
                                <li>📍 <c:out value="${b.branch.address}"/></li>
                                <c:if test="${not empty b.branch.phone}">
                                    <li>📞 <c:out value="${b.branch.phone}"/></li>
                                </c:if>
                                <c:if test="${not empty b.openHoursLabel}">
                                    <li>🕒 ${b.openHoursLabel}</li>
                                </c:if>
                                <li>🎬 ${b.hallCount} phòng chiếu</li>
                            </ul>
                            <%-- Sang trang suất chiếu, lọc sẵn theo chi nhánh này (Phần 2) --%>
                            <a class="btn btn-primary" href="${ctx}/showtimes?branchId=${b.branch.id}">Xem suất chiếu</a>
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
