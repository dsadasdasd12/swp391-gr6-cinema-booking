<%--
    Component: 1 thẻ phim (poster + badge + rating + tên + thể loại).
    Cách dùng: bên trong <c:forEach var="m" ...> gọi include TĨNH để chia sẻ biến m:
        <c:forEach var="m" items="${danhSach}">
            <%@ include file="/pages/common/movie-card.jsp" %>
        </c:forEach>
    (Group6 - DuyThai)
--%>
<%@ page contentType="text/html; charset=UTF-8" pageEncoding="UTF-8" %>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<c:set var="ctx" value="${pageContext.request.contextPath}" />
<c:set var="ph" value="data:image/svg+xml;charset=UTF-8,%3Csvg%20xmlns%3D%22http%3A%2F%2Fwww.w3.org%2F2000%2Fsvg%22%20width%3D%22300%22%20height%3D%22450%22%3E%3Crect%20width%3D%22100%25%22%20height%3D%22100%25%22%20fill%3D%22%2323262d%22%2F%3E%3Ctext%20x%3D%2250%25%22%20y%3D%2250%25%22%20fill%3D%22%237d828c%22%20font-family%3D%22Arial%22%20font-size%3D%2220%22%20text-anchor%3D%22middle%22%20dominant-baseline%3D%22middle%22%3ENo%20Image%3C%2Ftext%3E%3C%2Fsvg%3E" />
<a class="movie-card" href="${ctx}/movie?id=${m.id}">
    <div class="poster-wrap">
        <span class="badge ${m.statusBadgeClass}"><c:out value="${m.statusLabel}"/></span>
        <c:if test="${m.reviewCount > 0}">
            <span class="rating-chip">★ ${m.ratingRounded}</span>
        </c:if>
        <img src="${empty m.posterUrl ? ph : m.posterUrl}"
             alt="<c:out value='${m.title}'/>"
             onerror="this.onerror=null;this.src='${ph}'">
    </div>
    <div class="movie-body">
        <h3><c:out value="${m.title}"/></h3>
        <div class="movie-meta">
            ${m.durationLabel}
            <c:if test="${m.releaseYear > 0}"> &middot; ${m.releaseYear}</c:if>
            </div>
            <div class="movie-genres"><c:out value="${m.categoryNames}"/></div>
    </div>
</a>
