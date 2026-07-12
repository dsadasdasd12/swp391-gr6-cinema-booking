<%--
    RapViet Cinema - Đánh giá phim (Rate / Write / Edit / Delete) - KHÁCH
    Module: Đánh giá   (Group6 - Huy)
    Được phục vụ bởi controller.ReviewController  ->  URL /review?bookingId=&movieId=
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
    <title>Đánh giá phim - RapViet Cinema</title>
    <link rel="stylesheet" href="${ctx}/assets/css/style.css">
    <link rel="stylesheet" href="${ctx}/assets/css/movie.css">
</head>
<body>
    <jsp:include page="/pages/common/header.jsp">
        <jsp:param name="active" value="bookings"/>
    </jsp:include>

    <div class="page-wrap">
    <div class="container" style="max-width:680px;">
        <c:choose>
            <%-- ── Không đủ điều kiện đánh giá ── --%>
            <c:when test="${notEligible and empty review}">
                <div class="empty">
                    <h3>Chưa thể đánh giá phim này</h3>
                    <p>Bạn chỉ có thể đánh giá phim sau khi đã xem (vé đã check-in / đã sử dụng).</p>
                    <p><a class="btn btn-primary" href="${ctx}/my-bookings">← Về vé của tôi</a></p>
                </div>
            </c:when>

            <%-- ── Form đánh giá (tạo mới hoặc sửa) ── --%>
            <c:when test="${not empty review and not canEdit}">
                <div class="empty">
                    <h3>Đã hết thời gian chỉnh sửa đánh giá</h3>
                    <p>Bạn chỉ có thể chỉnh sửa đánh giá trong vòng 30 phút kể từ khi gửi.</p>
                    <p><a class="btn btn-primary" href="${ctx}/movie?id=${movieId}">← Về trang phim</a></p>
                </div>
            </c:when>
            <c:otherwise>
                <h1 class="page-title">
                    ${empty review ? 'Viết đánh giá' : 'Chỉnh sửa đánh giá'}
                </h1>
                <p style="color:#9aa0aa;margin:0 0 18px;">
                    Phim: <strong><c:out value="${movie.title}"/></strong>
                </p>

                <form class="review-form" method="post" action="${ctx}/review">
                    <input type="hidden" name="action" value="save">
                    <input type="hidden" name="movieId" value="${movieId}">
                    <input type="hidden" name="bookingId" value="${bookingId}">
                    <%-- Có reviewId => chế độ sửa --%>
                    <c:if test="${not empty review}">
                        <input type="hidden" name="reviewId" value="${review.id}">
                    </c:if>

                    <div class="filter-field">
                        <label for="rating">Điểm đánh giá</label>
                        <select id="rating" name="rating" required>
                            <c:forEach var="i" begin="1" end="5">
                                <option value="${i}" ${(empty review ? 5 : review.rating) == i ? 'selected' : ''}>
                                    ${i} sao
                                </option>
                            </c:forEach>
                        </select>
                    </div>

                    <div class="filter-field">
                        <label for="comment">Nhận xét (không bắt buộc)</label>
                        <textarea id="comment" name="comment" rows="5"
                                  placeholder="Chia sẻ cảm nhận của bạn về bộ phim..."><c:out value="${review.comment}"/></textarea>
                    </div>

                    <div class="bd-actions">
                        <button type="submit" class="btn btn-primary">Lưu đánh giá</button>
                        <a class="btn btn-ghost" href="${ctx}/movie?id=${movieId}">Hủy</a>
                    </div>
                </form>

                <%-- Xóa đánh giá (chỉ khi đang sửa đánh giá đã có) --%>
                <c:if test="${not empty review}">
                    <form method="post" action="${ctx}/review" style="margin-top:14px;"
                          onsubmit="return confirm('Xóa đánh giá này?');">
                        <input type="hidden" name="action" value="delete">
                        <input type="hidden" name="movieId" value="${movieId}">
                        <input type="hidden" name="reviewId" value="${review.id}">
                        <button type="submit" class="btn btn-ghost">Xóa đánh giá</button>
                    </form>
                </c:if>
            </c:otherwise>
        </c:choose>
    </div>
    </div>

    <jsp:include page="/pages/common/footer.jsp" />
</body>
</html>
