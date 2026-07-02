<%--
    RapViet Cinema - Trang chủ (Landing page)
    Được phục vụ bởi controller.HomeController  ->  URL /home
    Dùng component header/footer/movie-card. View chỉ JSTL + EL.
    (Group6 - DuyThai)
--%>
<%@ page contentType="text/html; charset=UTF-8" pageEncoding="UTF-8" %>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<c:set var="ctx" value="${pageContext.request.contextPath}" />
<!DOCTYPE html>
<html lang="vi">
<head>
    <meta charset="UTF-8">
    <meta name="viewport" content="width=device-width, initial-scale=1">
    <title>RapViet Cinema - Đặt vé xem phim trực tuyến</title>
    <link rel="stylesheet" href="${ctx}/assets/css/style.css">
</head>
<body>
    <jsp:include page="/pages/common/header.jsp">
        <jsp:param name="active" value="home"/>
    </jsp:include>

    <div class="page-wrap">

        <!-- ── Banner / Hero ── -->
        <section class="hero">
            <div class="hero-inner">
                <h1>Đặt vé xem phim tại <span>RapViet</span><br>nhanh chóng &amp; dễ dàng</h1>
                <p>Khám phá phim đang chiếu, chọn suất, chọn ghế và thanh toán online —
                   nhận vé QR ngay trên điện thoại.</p>
                <div class="hero-actions">
                    <a href="${ctx}/booking/start" class="btn btn-primary btn-lg">Đặt vé ngay</a>
                    <a href="${ctx}/movies" class="btn btn-ghost btn-lg">Xem tất cả phim</a>
                </div>
            </div>
        </section>

        <!-- ── Phim đang chiếu ── -->
        <section class="home-section">
            <div class="section-head">
                <h2>Phim đang chiếu</h2>
                <a href="${ctx}/movies?status=NOW_SHOWING">Xem tất cả →</a>
            </div>
            <c:choose>
                <c:when test="${empty nowShowing}">
                    <div class="empty">Hiện chưa có phim đang chiếu.</div>
                </c:when>
                <c:otherwise>
                    <div class="movie-grid">
                        <c:forEach var="m" items="${nowShowing}">
                            <%@ include file="/pages/common/movie-card.jsp" %>
                        </c:forEach>
                    </div>
                </c:otherwise>
            </c:choose>
        </section>

        <!-- ── Phim sắp chiếu ── -->
        <section class="home-section">
            <div class="section-head">
                <h2>Sắp chiếu</h2>
                <a href="${ctx}/movies?status=COMING_SOON">Xem tất cả →</a>
            </div>
            <c:choose>
                <c:when test="${empty comingSoon}">
                    <div class="empty">Chưa có phim sắp chiếu.</div>
                </c:when>
                <c:otherwise>
                    <div class="movie-grid">
                        <c:forEach var="m" items="${comingSoon}">
                            <%@ include file="/pages/common/movie-card.jsp" %>
                        </c:forEach>
                    </div>
                </c:otherwise>
            </c:choose>
        </section>

        <!-- ── Vì sao chọn RapViet ── -->
        <section class="home-section">
            <div class="section-head"><h2>Vì sao chọn RapViet?</h2></div>
            <div class="features">
                <div class="feature-card">
                    <div class="ic">🎟️</div>
                    <h3>Đặt vé online</h3>
                    <p>Chọn phim, suất chiếu và ghế ngồi mọi lúc mọi nơi, không cần xếp hàng.</p>
                </div>
                <div class="feature-card">
                    <div class="ic">💺</div>
                    <h3>Chọn ghế trực quan</h3>
                    <p>Sơ đồ ghế thời gian thực, thấy ngay ghế còn trống để lựa chọn.</p>
                </div>
                <div class="feature-card">
                    <div class="ic">📱</div>
                    <h3>Vé QR tiện lợi</h3>
                    <p>Nhận vé điện tử kèm mã QR, quét vào cổng nhanh gọn.</p>
                </div>
                <div class="feature-card">
                    <div class="ic">🏢</div>
                    <h3>Nhiều chi nhánh</h3>
                    <p>Hệ thống rạp đa chi nhánh, chọn rạp gần bạn nhất.</p>
                </div>
            </div>
        </section>

    </div>

    <jsp:include page="/pages/common/footer.jsp" />
</body>
</html>
