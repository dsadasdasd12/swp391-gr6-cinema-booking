<%--
    Component: Footer dùng chung cho toàn site.
    Cách dùng: <jsp:include page="/pages/common/footer.jsp" />
    (Group6 - DuyThai)
--%>
<%@ page contentType="text/html; charset=UTF-8" pageEncoding="UTF-8" %>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<c:set var="ctx" value="${pageContext.request.contextPath}" />
<footer class="site-footer">
    <div class="footer-grid">
        <div>
            <div class="brand">🎬 RapViet</div>
            <p>Hệ thống đặt vé xem phim trực tuyến của chuỗi rạp RapViet.
               Đặt vé nhanh, chọn ghế dễ dàng, nhận vé QR ngay trên điện thoại.</p>
        </div>
        <div>
            <h4>Khám phá</h4>
            <ul class="footer-links">
                <li><a href="${ctx}/home">Trang chủ</a></li>
                <li><a href="${ctx}/movieslist">Tất cả phim</a></li>
                <li><a href="${ctx}/movies?status=NOW_SHOWING">Đang chiếu</a></li>
                <li><a href="${ctx}/movies?status=COMING_SOON">Sắp chiếu</a></li>
            </ul>
        </div>
        <div>
            <h4>Tài khoản</h4>
            <ul class="footer-links">
                <li><a href="${ctx}/login">Đăng nhập</a></li>
                <li><a href="${ctx}/register">Đăng ký</a></li>
            </ul>
        </div>
        <div>
            <h4>Liên hệ</h4>
            <p>Hotline: 1900-6017</p>
            <p>Email: support@rapviet.vn</p>
            <p>Hà Nội, Việt Nam</p>
        </div>
    </div>
    <div class="footer-bottom">
        © 2026 RapViet Cinema — Đồ án SWP391, Nhóm 6. Chỉ dùng cho mục đích học tập.
    </div>
</footer>
