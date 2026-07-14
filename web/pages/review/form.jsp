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
    <jsp:include page="/pages/common/header.jsp"><jsp:param name="active" value="bookings"/></jsp:include>
    <div class="page-wrap"><div class="container" style="max-width:680px;">
        <c:choose>
            <c:when test="${notEligible and empty review}">
                <div class="empty">
                    <h3>Chưa thể đánh giá phim này</h3>
                    <p>Bạn chỉ có thể đánh giá sau khi vé đã check-in hoặc đã sử dụng.</p>
                    <p><a class="btn btn-primary" href="${ctx}/my-bookings">← Về vé của tôi</a></p>
                </div>
            </c:when>
            <c:when test="${not empty review and not canEdit}">
                <div class="empty">
                    <h3>Đã hết thời gian chỉnh sửa đánh giá</h3>
                    <p>Bạn chỉ có thể chỉnh sửa trong vòng 30 phút kể từ khi gửi.</p>
                    <p><a class="btn btn-primary" href="${ctx}/movie?id=${movieId}">← Về trang phim</a></p>
                </div>
            </c:when>
            <c:otherwise>
                <h1 class="page-title">${empty review ? 'Viết đánh giá' : 'Chỉnh sửa đánh giá'}</h1>
                <p style="color:#9aa0aa;margin:0 0 18px;">Phim: <strong><c:out value="${movie.title}"/></strong></p>
                <form class="review-form" method="post" action="${ctx}/review">
                    <input type="hidden" name="action" value="save">
                    <input type="hidden" name="movieId" value="${movieId}">
                    <input type="hidden" name="bookingId" value="${bookingId}">
                    <c:if test="${not empty review}"><input type="hidden" name="reviewId" value="${review.id}"></c:if>
                    <div class="filter-field">
                        <label>Điểm đánh giá</label>
                        <input type="hidden" id="rating" name="rating" value="${empty review ? 5 : review.rating}">
                        <%-- Rê nửa trái/phải mỗi sao để preview và chọn điểm .5/.0. --%>
                        <div id="starPicker" class="star-picker" role="radiogroup" aria-label="Điểm đánh giá">
                            <c:forEach var="star" begin="1" end="5">
                                <button type="button" class="rating-star" data-star="${star}"
                                        aria-label="${star} sao">★</button>
                            </c:forEach>
                        </div>
                        <small id="ratingHint" class="rating-hint">Chọn từ 0,5 đến 5 sao.</small>
                    </div>
                    <div class="filter-field">
                        <label for="comment">Nhận xét (không bắt buộc)</label>
                        <textarea id="comment" name="comment" rows="5" placeholder="Chia sẻ cảm nhận của bạn về bộ phim..."><c:out value="${review.comment}"/></textarea>
                    </div>
                    <div class="bd-actions">
                        <button type="submit" class="btn btn-primary">Lưu đánh giá</button>
                        <a class="btn btn-ghost" href="${ctx}/movie?id=${movieId}">Hủy</a>
                    </div>
                </form>
                <c:if test="${not empty review}">
                    <form method="post" action="${ctx}/review" style="margin-top:14px;" onsubmit="return confirm('Xóa đánh giá này?');">
                        <input type="hidden" name="action" value="delete"><input type="hidden" name="movieId" value="${movieId}"><input type="hidden" name="reviewId" value="${review.id}">
                        <button type="submit" class="btn btn-ghost">Xóa đánh giá</button>
                    </form>
                </c:if>
            </c:otherwise>
        </c:choose>
    </div></div>
    <jsp:include page="/pages/common/footer.jsp" />
    <script>
        (function () {
            const picker = document.getElementById('starPicker');
            const input = document.getElementById('rating');
            const hint = document.getElementById('ratingHint');
            if (!picker || !input || !hint) return;

            const stars = Array.from(picker.querySelectorAll('.rating-star'));
            let selected = Number(input.value) || 5;

            function paint(score) {
                stars.forEach(function (star) {
                    const index = Number(star.dataset.star);
                    star.classList.toggle('is-full', score >= index);
                    star.classList.toggle('is-half', score === index - 0.5);
                });
                hint.textContent = score.toFixed(1).replace('.', ',') + ' sao';
            }

            function scoreAt(event, star) {
                const rect = star.getBoundingClientRect();
                const index = Number(star.dataset.star);
                return index - (event.clientX - rect.left < rect.width / 2 ? 0.5 : 0);
            }

            stars.forEach(function (star) {
                star.addEventListener('mousemove', function (event) { paint(scoreAt(event, star)); });
                star.addEventListener('click', function (event) {
                    selected = scoreAt(event, star);
                    input.value = selected;
                    paint(selected);
                });
            });
            picker.addEventListener('mouseleave', function () { paint(selected); });
            paint(selected);
        }());
    </script>
</body>
</html>
