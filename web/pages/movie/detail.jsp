<%--
    RapViet Cinema - Chi tiết phim (Xem chi tiết + Suất chiếu)
    Module: Duyệt phim - UC06 / UC12   (Group6 - DuyThai)
    Được phục vụ bởi controller.MovieDetailController  ->  URL /movie?id=N
    View chỉ dùng JSTL + EL, KHÔNG nhúng code Java.
--%>
<%@ page contentType="text/html; charset=UTF-8" pageEncoding="UTF-8" %>
<%@ taglib prefix="c" uri="jakarta.tags.core" %>

<c:set var="ctx" value="${pageContext.request.contextPath}" />
<c:set var="ph" value="data:image/svg+xml;charset=UTF-8,%3Csvg%20xmlns='http://www.w3.org/2000/svg'%20width='300'%20height='450'%3E%3Crect%20width='100%25'%20height='100%25'%20fill='%2323262d'/%3E%3Ctext%20x='50%25'%20y='50%25'%20fill='%237d828c'%20font-family='Arial'%20font-size='20'%20text-anchor='middle'%20dominant-baseline='middle'%3ENo%20Image%3C/text%3E%3C/svg%3E" />

<!DOCTYPE html>
<html lang="vi">
<head>
    <meta charset="UTF-8">
    <meta name="viewport" content="width=device-width, initial-scale=1">
    <title><c:out value="${empty movie ? 'Không tìm thấy phim' : movie.title}"/> - RapViet Cinema</title>
    <link rel="stylesheet" href="${ctx}/assets/css/movie.css">
</head>
<body>
    <header class="site-header">
        <a href="${ctx}/" class="brand">RapViet</a>
        <nav><a href="${ctx}/movies">Phim</a></nav>
    </header>

    <div class="container">
    <c:choose>
        <%-- ── Không tìm thấy phim ── --%>
        <c:when test="${notFound or empty movie}">
            <div class="empty">
                <h3>Không tìm thấy phim</h3>
                <p>Phim bạn tìm không tồn tại hoặc đã bị gỡ.</p>
                <p><a class="btn btn-primary" href="${ctx}/movies">← Về danh sách phim</a></p>
            </div>
        </c:when>

        <%-- ── Chi tiết phim ── --%>
        <c:otherwise>
            <p style="margin:0 0 16px;">
                <a href="${ctx}/movies" style="color:#9aa0aa;">← Danh sách phim</a>
            </p>

            <div class="detail-hero">
                <div class="detail-poster">
                    <img src="${empty movie.posterUrl ? ph : movie.posterUrl}"
                         alt="<c:out value='${movie.title}'/>"
                         onerror="this.onerror=null;this.src='${ph}'">
                </div>

                <div class="detail-info">
                    <h1><c:out value="${movie.title}"/></h1>

                    <div class="chips">
                        <span class="chip"><c:out value="${movie.statusLabel}"/></span>
                        <span class="chip">${movie.durationLabel}</span>
                        <c:forEach var="c" items="${movie.categories}">
                            <span class="chip"><c:out value="${c.name}"/></span>
                        </c:forEach>
                    </div>

                    <%-- Điểm đánh giá: vẽ 5 sao, tô đặc theo số sao làm tròn --%>
                    <c:choose>
                        <c:when test="${movie.reviewCount > 0}">
                            <div>
                                <span class="rating-stars">
                                    <c:forEach var="s" begin="1" end="5">${s <= movie.roundedStars ? '★' : '☆'}</c:forEach>
                                </span>
                                <strong>${movie.ratingRounded}/5</strong>
                                <span style="color:#9aa0aa;">(${movie.reviewCount} đánh giá)</span>
                            </div>
                        </c:when>
                        <c:otherwise>
                            <div style="color:#9aa0aa;">Chưa có đánh giá</div>
                        </c:otherwise>
                    </c:choose>

                    <div class="detail-table">
                        <div><span class="k">Đạo diễn</span>
                            <c:out value="${empty movie.director ? 'Đang cập nhật' : movie.director}"/></div>
                        <div><span class="k">Diễn viên</span>
                            <c:out value="${empty movie.actor ? 'Đang cập nhật' : movie.actor}"/></div>
                        <div><span class="k">Khởi chiếu</span>
                            <c:out value="${empty movie.releaseDateLabel ? 'Đang cập nhật' : movie.releaseDateLabel}"/></div>
                        <div><span class="k">Ngôn ngữ</span>
                            <c:out value="${empty movie.languageNames ? 'Đang cập nhật' : movie.languageNames}"/></div>
                    </div>
                </div>
            </div>

            <%-- ── Nội dung phim ── --%>
            <c:if test="${not empty movie.description}">
                <h2 class="section-title">Nội dung phim</h2>
                <div class="synopsis"><c:out value="${movie.description}"/></div>
            </c:if>

            <%-- ── Trailer (chỉ hiện khi là link YouTube hợp lệ) ── --%>
            <c:if test="${not empty movie.embedUrl}">
                <h2 class="section-title">Trailer</h2>
                <div class="trailer-wrap">
                    <iframe src="${movie.embedUrl}" allowfullscreen
                            allow="accelerometer; autoplay; clipboard-write; encrypted-media; gyroscope; picture-in-picture"></iframe>
                </div>
            </c:if>

            <%-- ── Lịch chiếu (đã gom nhóm theo chi nhánh ở tầng service) ── --%>
            <h2 class="section-title">Lịch chiếu</h2>
            <c:choose>
                <c:when test="${empty branchShowtimes}">
                    <div class="empty">Hiện chưa có lịch chiếu cho phim này.</div>
                </c:when>
                <c:otherwise>
                    <c:forEach var="b" items="${branchShowtimes}">
                        <div class="branch-block">
                            <h4><c:out value="${b.branchName}"/></h4>
                            <div class="addr"><c:out value="${b.branchAddress}"/></div>
                            <div class="showtime-chips">
                                <c:forEach var="st" items="${b.showtimes}">
                                    <a class="showtime-chip" href="${ctx}/booking?showtimeId=${st.id}">
                                        <div class="t">${st.startHour}</div>
                                        <div class="sub">${st.showDate} &middot; <c:out value="${st.hallType}"/></div>
                                    </a>
                                </c:forEach>
                            </div>
                        </div>
                    </c:forEach>
                </c:otherwise>
            </c:choose>
        </c:otherwise>
    </c:choose>
    </div>
</body>
</html>
