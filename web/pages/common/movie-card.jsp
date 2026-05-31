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
<c:set var="ph" value="data:image/svg+xml;charset=UTF-8,%3Csvg%20xmlns='http://www.w3.org/2000/svg'%20width='300'%20height='450'%3E%3Crect%20width='100%25'%20height='100%25'%20fill='%2323262d'/%3E%3Ctext%20x='50%25'%20y='50%25'%20fill='%237d828c'%20font-family='Arial'%20font-size='20'%20text-anchor='middle'%20dominant-baseline='middle'%3ENo%20Image%3C/text%3E%3C/svg%3E" />
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
