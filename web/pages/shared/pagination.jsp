<%--
    Rạp Việt CMS — Reusable Premium Pagination Component
    Include: <jsp:include page="/pages/shared/pagination.jsp">
                <jsp:param name="currentPage" value="${currentPage}" />
                <jsp:param name="totalPages" value="${totalPages}" />
                <jsp:param name="totalItems" value="${totalItems}" />
                <jsp:param name="pageSize" value="${pageSize}" />
                <jsp:param name="baseUrl" value="${baseUrl}" />
             </jsp:include>
--%>
<%@ page contentType="text/html; charset=UTF-8" pageEncoding="UTF-8" %>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>

<c:set var="curr" value="${not empty param.currentPage ? param.currentPage : 1}" />
<c:set var="total" value="${not empty param.totalPages ? param.totalPages : 1}" />
<c:set var="items" value="${not empty param.totalItems ? param.totalItems : 0}" />
<c:set var="size" value="${not empty param.pageSize ? param.pageSize : 10}" />
<c:set var="base" value="${not empty param.baseUrl ? param.baseUrl : ''}" />

<%-- Calculate showing item range --%>
<c:set var="startItem" value="${(curr - 1) * size + 1}" />
<c:set var="endItem" value="${curr * size}" />
<c:if var="isLastPage" test="${endItem > items}">
    <c:set var="endItem" value="${items}" />
</c:if>
<c:if test="${items == 0}">
    <c:set var="startItem" value="0" />
    <c:set var="endItem" value="0" />
</c:if>

<div class="rv-pagination">
    <!-- Showing items summary -->
    <div class="rv-pagination__summary">
        Hiển thị <span>${startItem}</span> - <span>${endItem}</span> trong tổng số <span>${items}</span> mục
    </div>

    <!-- Navigation links -->
    <c:if test="${total > 1}">
        <div class="rv-pagination__nav">
            <!-- First and Prev Page -->
            <c:choose>
                <c:when test="${curr > 1}">
                    <a href="${base}&page=1" class="rv-pagination__btn" title="Trang đầu">
                        <i class="bi bi-chevron-double-left"></i>
                    </a>
                    <a href="${base}&page=${curr - 1}" class="rv-pagination__btn" title="Trang trước">
                        <i class="bi bi-chevron-left"></i>
                    </a>
                </c:when>
                <c:otherwise>
                    <button type="button" class="rv-pagination__btn disabled" disabled>
                        <i class="bi bi-chevron-double-left"></i>
                    </button>
                    <button type="button" class="rv-pagination__btn disabled" disabled>
                        <i class="bi bi-chevron-left"></i>
                    </button>
                </c:otherwise>
            </c:choose>

            <!-- Page Numbers (Show max 5 pages centered around current page) -->
            <c:set var="startPage" value="${curr - 2}" />
            <c:if test="${startPage < 1}">
                <c:set var="startPage" value="1" />
            </c:if>
            <c:set var="endPage" value="${startPage + 4}" />
            <c:if test="${endPage > total}">
                <c:set var="endPage" value="${total}" />
                <c:set var="startPage" value="${endPage - 4}" />
                <c:if test="${startPage < 1}">
                    <c:set var="startPage" value="1" />
                </c:if>
            </c:if>

            <c:forEach var="p" begin="${startPage}" end="${endPage}">
                <c:choose>
                    <c:when test="${p == curr}">
                        <span class="rv-pagination__btn active">${p}</span>
                    </c:when>
                    <c:otherwise>
                        <a href="${base}&page=${p}" class="rv-pagination__btn">${p}</a>
                    </c:otherwise>
                </c:choose>
            </c:forEach>

            <!-- Next and Last Page -->
            <c:choose>
                <c:when test="${curr < total}">
                    <a href="${base}&page=${curr + 1}" class="rv-pagination__btn" title="Trang sau">
                        <i class="bi bi-chevron-right"></i>
                    </a>
                    <a href="${base}&page=${total}" class="rv-pagination__btn" title="Trang cuối">
                        <i class="bi bi-chevron-double-right"></i>
                    </a>
                </c:when>
                <c:otherwise>
                    <button type="button" class="rv-pagination__btn disabled" disabled>
                        <i class="bi bi-chevron-right"></i>
                    </button>
                    <button type="button" class="rv-pagination__btn disabled" disabled>
                        <i class="bi bi-chevron-double-right"></i>
                    </button>
                </c:otherwise>
            </c:choose>
        </div>
    </c:if>
</div>
