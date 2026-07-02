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
<a class="movie-card" href="${ctx}/movie?id=${m.id}">
    <div class="poster-wrap">
        <span class="badge ${m.statusBadgeClass}"><c:out value="${m.statusLabel}"/></span>
        <c:if test="${m.reviewCount > 0}">
            <span class="rating-chip">★ ${m.ratingRounded}</span>
        </c:if>
        <img src="${empty m.posterUrl ? m.posterFallbackUrl : m.posterUrl}"
             alt="<c:out value='${m.title}'/>"
             onerror="this.onerror=null;this.src='${m.posterFallbackUrl}'">
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
