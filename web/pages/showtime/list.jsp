<%--
    RapViet Cinema - Lịch chiếu theo chi nhánh (Xem suất chiếu)
    Module: Duyệt phim - View showtimes   (Group6 - Huy)
    Được phục vụ bởi controller.ShowtimeListController  ->  URL /showtimes
    View chỉ dùng JSTL + EL + component, KHÔNG nhúng code Java.
--%>
<%@ page contentType="text/html; charset=UTF-8" pageEncoding="UTF-8" %>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<c:set var="ctx" value="${pageContext.request.contextPath}" />
<c:set var="ph" value="data:image/svg+xml;charset=UTF-8,%3Csvg%20xmlns%3D%22http%3A%2F%2Fwww.w3.org%2F2000%2Fsvg%22%20width%3D%22300%22%20height%3D%22450%22%3E%3Crect%20width%3D%22100%25%22%20height%3D%22100%25%22%20fill%3D%22%2323262d%22%2F%3E%3Ctext%20x%3D%2250%25%22%20y%3D%2250%25%22%20fill%3D%22%237d828c%22%20font-family%3D%22Arial%22%20font-size%3D%2220%22%20text-anchor%3D%22middle%22%20dominant-baseline%3D%22middle%22%3ENo%20Image%3C%2Ftext%3E%3C%2Fsvg%3E" />

<!DOCTYPE html>
<html lang="vi">
<head>
    <meta charset="UTF-8">
    <meta name="viewport" content="width=device-width, initial-scale=1">
    <title>Lịch chiếu - RapViet Cinema</title>
    <link rel="stylesheet" href="${ctx}/assets/css/style.css">
    <link rel="stylesheet" href="${ctx}/assets/css/movie.css">
</head>
<body>
    <jsp:include page="/pages/common/header.jsp">
        <jsp:param name="active" value="showtimes"/>
    </jsp:include>

    <div class="page-wrap">
    <div class="container">
        <h1 class="page-title">Lịch chiếu</h1>

        <%-- ── Thanh chọn chi nhánh + ngày (đổi là tự gửi form) ── --%>
        <form class="st-toolbar" method="get" action="${ctx}/showtimes">
            <div class="filter-field">
                <label for="branchId">Chi nhánh</label>
                <select id="branchId" name="branchId" onchange="this.form.submit()">
                    <%-- b là dto.BranchView; id/tên lấy từ entity Branch bên trong --%>
                    <c:forEach var="b" items="${branches}">
                        <option value="${b.branch.id}" ${selectedBranchId == b.branch.id ? 'selected' : ''}>
                            <c:out value="${b.branch.name}"/>
                        </option>
                    </c:forEach>
                </select>
            </div>
            <div class="filter-field">
                <label for="date">Ngày chiếu</label>
                <input type="date" id="date" name="date" value="${selectedDate}"
                       onchange="this.form.submit()">
            </div>
            <%-- Dự phòng khi trình duyệt tắt JavaScript --%>
            <div class="filter-field">
                <label>&nbsp;</label>
                <button type="submit" class="btn btn-primary">Xem</button>
            </div>
        </form>

        <c:choose>
            <%-- ── Không có chi nhánh nào để hiển thị ── --%>
            <c:when test="${empty branches}">
                <div class="empty">
                    <h3>Chưa có chi nhánh</h3>
                    <p>Hệ thống rạp đang được cập nhật, vui lòng quay lại sau.</p>
                </div>
            </c:when>

            <%-- ── Chi nhánh có nhưng ngày này chưa có suất chiếu ── --%>
            <c:when test="${empty movieShowtimes}">
                <div class="empty">
                    <h3>Không có suất chiếu</h3>
                    <p>Chi nhánh này chưa có suất chiếu nào trong ngày đã chọn. Hãy thử ngày khác.</p>
                </div>
            </c:when>

            <%-- ── Danh sách suất chiếu, nhóm theo phim ── --%>
            <c:otherwise>
                <c:forEach var="ms" items="${movieShowtimes}">
                    <div class="st-movie">
                        <a class="st-poster" href="${ctx}/movie?id=${ms.movieId}">
                            <img src="${empty ms.posterUrl ? ph : ms.posterUrl}"
                                 alt="<c:out value='${ms.movieTitle}'/>"
                                 onerror="this.onerror=null;this.src='${ph}'">
                        </a>
                        <div class="st-body">
                            <h3><a href="${ctx}/movie?id=${ms.movieId}"><c:out value="${ms.movieTitle}"/></a></h3>
                            <div class="showtime-chips">
                                <%-- Mỗi giờ chiếu dẫn sang sơ đồ ghế của suất đó (Phần 3) --%>
                                <c:forEach var="st" items="${ms.showtimes}">
                                    <a class="showtime-chip" href="${ctx}/booking/seats?showtimeId=${st.id}">
                                        <div class="t">${st.startHour}</div>
                                        <div class="sub"><c:out value="${st.hallType}"/> &middot; <c:out value="${st.hallName}"/></div>
                                    </a>
                                </c:forEach>
                            </div>
                        </div>
                    </div>
                </c:forEach>
            </c:otherwise>
        </c:choose>
    </div>
    </div>

    <jsp:include page="/pages/common/footer.jsp" />
</body>
</html>
