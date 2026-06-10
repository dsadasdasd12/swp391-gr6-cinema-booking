<%--
    RapViet Cinema - Danh sách phim (Duyệt / Tìm kiếm / Lọc)
    Module: Duyệt phim   (Group6 - DuyThai)
    Được phục vụ bởi controller.MovieListController  ->  URL /movies
    View chỉ dùng JSTL + EL, KHÔNG nhúng code Java.
--%>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<%@ page contentType="text/html; charset=UTF-8" pageEncoding="UTF-8" %>


<%-- Đường dẫn ngữ cảnh + ảnh placeholder khi phim chưa có poster --%>
<c:set var="ctx" value="${pageContext.request.contextPath}" />
<c:set var="ph" value="data:image/svg+xml;charset=UTF-8,%3Csvg%20xmlns='http://www.w3.org/2000/svg'%20width='300'%20height='450'%3E%3Crect%20width='100%25'%20height='100%25'%20fill='%2323262d'/%3E%3Ctext%20x='50%25'%20y='50%25'%20fill='%237d828c'%20font-family='Arial'%20font-size='20'%20text-anchor='middle'%20dominant-baseline='middle'%3ENo%20Image%3C/text%3E%3C/svg%3E" />

<!DOCTYPE html>
<html lang="vi">
<head>
    <meta charset="UTF-8">
    <meta name="viewport" content="width=device-width, initial-scale=1">
    <title>Phim - RapViet Cinema</title>
    <link rel="stylesheet" href="${ctx}/assets/css/style.css">
    <link rel="stylesheet" href="${ctx}/assets/css/movie.css">
</head>
<body>
    <jsp:include page="/pages/common/header.jsp">
        <jsp:param name="active" value="movies"/>
    </jsp:include>

    <div class="page-wrap">
    <div class="container">
        <h1 class="page-title">Danh sách phim</h1>
        
        <%-- ── Form tìm kiếm & lọc (dùng GET để URL chia sẻ được) ── --%>
        <form class="filter-bar" method="get" action="${ctx}/movies">
            <div class="filter-row">
                <div class="filter-field grow">
                    <label for="q">Tìm kiếm</label>
                    <input type="text" id="q" name="q"
                           placeholder="Tên phim, diễn viên, đạo diễn..."
                           value="<c:out value='${filter.keyword}'/>">
                </div>

                <div class="filter-field">
                    <label for="category">Thể loại</label>
                    <select id="category" name="category">
                        <option value="">Tất cả</option>
                        <c:forEach var="c" items="${categories}">
                            <option value="${c.id}" ${filter.categoryId == c.id ? 'selected' : ''}>
                                <c:out value="${c.name}"/>
                            </option>
                        </c:forEach>
                    </select>
                </div>

                <div class="filter-field">
                    <label for="language">Ngôn ngữ</label>
                    <select id="language" name="language">
                        <option value="">Tất cả</option>
                        <c:forEach var="l" items="${languages}">
                            <option value="${l.id}" ${filter.languageId == l.id ? 'selected' : ''}>
                                <c:out value="${l.name}"/>
                            </option>
                        </c:forEach>
                    </select>
                </div>

                <div class="filter-field">
                    <label for="status">Trạng thái</label>
                    <select id="status" name="status">
                        <option value="">Tất cả</option>
                        <option value="NOW_SHOWING" ${filter.status == 'NOW_SHOWING' ? 'selected' : ''}>Đang chiếu</option>
                        <option value="COMING_SOON" ${filter.status == 'COMING_SOON' ? 'selected' : ''}>Sắp chiếu</option>
                        <option value="ENDED"       ${filter.status == 'ENDED'       ? 'selected' : ''}>Đã chiếu</option>
                    </select>
                </div>

                <div class="filter-field">
                    <label for="format">Định dạng</label>
                    <select id="format" name="format">
                        <option value="">Tất cả</option>
                        <option value="STANDARD" ${filter.format == 'STANDARD' ? 'selected' : ''}>STANDARD</option>
                        <option value="VIP"      ${filter.format == 'VIP'      ? 'selected' : ''}>VIP</option>
                        <option value="IMAX"     ${filter.format == 'IMAX'     ? 'selected' : ''}>IMAX</option>
                        <option value="4DX"      ${filter.format == '4DX'      ? 'selected' : ''}>4DX</option>
                        <option value="PREMIUM"  ${filter.format == 'PREMIUM'  ? 'selected' : ''}>PREMIUM</option>
                    </select>
                </div>

                <div class="filter-field">
                    <label for="sort">Sắp xếp</label>
                    <select id="sort" name="sort">
                        <option value="newest"      ${filter.sortBy == 'newest'      ? 'selected' : ''}>Mới nhất</option>
                        <option value="title_asc"   ${filter.sortBy == 'title_asc'   ? 'selected' : ''}>Tên A → Z</option>
                        <option value="title_desc"  ${filter.sortBy == 'title_desc'  ? 'selected' : ''}>Tên Z → A</option>
                        <option value="rating_desc" ${filter.sortBy == 'rating_desc' ? 'selected' : ''}>Đánh giá cao</option>
                    </select>
                </div>

                <div class="filter-field">
                    <label>&nbsp;</label>
                    <button type="submit" class="btn btn-primary">Lọc dữ liệu</button>
                </div>
                <div class="filter-field">
                    <label>&nbsp;</label>
                    <a class="btn btn-ghost" href="${ctx}/movies">Xóa lọc</a>
                </div>
            </div>
        </form>

        <c:choose>
            <%-- ── Không có kết quả ── --%>
            <c:when test="${result==null}">
                <div class="empty">
                    <h3>Không tìm thấy phim phù hợp</h3>
                    <p>Hãy thử thay đổi từ khóa hoặc bộ lọc và tìm lại.</p>
                </div>
            </c:when>

            <%-- ── Có kết quả ── --%>
            <c:otherwise>
                <div class="result-meta">
                    Hiển thị ${result.fromIndex}–${result.toIndex}
                    trong tổng số ${result.totalItems} phim
                </div>

                <div class="movie-grid">
                    <c:forEach var="m" items="${result.items}">
                        <%-- Dùng lại component thẻ phim (include tĩnh, chia sẻ biến m) --%>
                        <%@ include file="/pages/common/movie-card.jsp" %>
                    </c:forEach>
                </div>

                <%-- ── Phân trang (cửa sổ trang lấy từ PageResult) ── --%>
                <c:if test="${result.totalPages > 1}">
                    <div class="pager">
                        <%-- Nút lùi --%>
                        <c:choose>
                            <c:when test="${result.hasPrev}">
                                <a href="${ctx}/movies?${queryString}page=${result.page - 1}">‹</a>
                            </c:when>
                            <c:otherwise><span class="disabled">‹</span></c:otherwise>
                        </c:choose>

                        <%-- Trang đầu + dấu … khi cửa sổ không bắt đầu từ 1 --%>
                        <c:if test="${result.startPage > 1}">
                            <a href="${ctx}/movies?${queryString}page=1">1</a>
                            <c:if test="${result.startPage > 2}"><span class="disabled">…</span></c:if>
                        </c:if>

                        <%-- Các trang trong cửa sổ --%>
                        <c:forEach var="p" begin="${result.startPage}" end="${result.endPage}">
                            <c:choose>
                                <c:when test="${p == result.page}">
                                    <span class="current">${p}</span>
                                </c:when>
                                <c:otherwise>
                                    <a href="${ctx}/movies?${queryString}page=${p}">${p}</a>
                                </c:otherwise>
                            </c:choose>
                        </c:forEach>

                        <%-- Dấu … + trang cuối khi cửa sổ chưa chạm trang cuối --%>
                        <c:if test="${result.endPage < result.totalPages}">
                            <c:if test="${result.endPage < result.totalPages - 1}"><span class="disabled">…</span></c:if>
                            <a href="${ctx}/movies?${queryString}page=${result.totalPages}">${result.totalPages}</a>
                        </c:if>

                        <%-- Nút tiến --%>
                        <c:choose>
                            <c:when test="${result.hasNext}">
                                <a href="${ctx}/movies?${queryString}page=${result.page + 1}">›</a>
                            </c:when>
                            <c:otherwise><span class="disabled">›</span></c:otherwise>
                        </c:choose>
                    </div>
                </c:if>
            </c:otherwise>
        </c:choose>
    </div>
    </div>

    <jsp:include page="/pages/common/footer.jsp" />
</body>
</html>
