<%--
    MÀN HÌNH LỊCH SỬ ĐẶT VÉ – CUSTOMER

    Luồng dữ liệu đầy đủ:
    1) User mở GET /my-bookings, có thể kèm status, fromDate, toDate và page trên URL.
    2) BookingHistoryController kiểm tra session "user", đọc/validate các tham số filter.
    3) Controller gọi BookingService để lấy List<BookingView>, số lượng theo status và số trang.
    4) BookingService chuẩn hóa status rồi gọi BookingDAO. DAO query SQL theo userId hiện tại,
       join ra thông tin phim/rạp/phòng/ghế và trả DTO BookingView.
    5) Controller truyền các attribute bookings, statusCounts, selectedStatus, fromDate, toDate,
       currentPage, totalPages, totalBookings, totalAllBookings sang JSP bằng request.setAttribute(...).
    6) JSP chỉ render bằng JSTL/EL; không có Java/SQL nên không thể bỏ qua kiểm tra quyền sở hữu booking.
--%>
<%@ page contentType="text/html; charset=UTF-8" pageEncoding="UTF-8" %>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<c:set var="ctx" value="${pageContext.request.contextPath}" />

<!DOCTYPE html>
<html lang="vi">
    <head>
        <meta charset="UTF-8">
        <meta name="viewport" content="width=device-width, initial-scale=1">
        <title>Vé của tôi - RapViet Cinema</title>
        <link rel="stylesheet" href="${ctx}/assets/css/style.css">
        <link rel="stylesheet" href="${ctx}/assets/css/movie.css">
    </head>
    <body>
        <jsp:include page="/pages/common/header.jsp">
            <jsp:param name="active" value="bookings"/>
        </jsp:include>

        <main class="page-wrap">
            <div class="container">
                <section class="booking-history" aria-labelledby="booking-history-title">
                    <div class="booking-history__heading">
                        <div>
                            <p class="booking-history__eyebrow">TÀI KHOẢN CỦA TÔI</p>
                            <h1 class="page-title" id="booking-history-title">Lịch sử đặt vé</h1>
                            <p class="result-meta">Theo dõi đơn vé, thanh toán và trạng thái sử dụng vé của bạn.</p>
                        </div>
                        <%-- totalBookings do Controller đếm theo filter hiện tại; dùng để hiển thị tổng và tính phân trang. --%>
                        <div class="booking-history__total" aria-label="Tổng số đơn phù hợp">
                            <strong>${totalBookings}</strong>
                            <span>đơn phù hợp</span>
                        </div>
                    </div>

                    <%--
                        Form GET giữ filter trên URL. Khi Customer refresh/chuyển trang/quay lại browser,
                        Controller vẫn nhận được status/fromDate/toDate như cũ, không cần lưu state tạm trong session.
                    --%>
                    <form class="booking-history-filter" action="${ctx}/my-bookings" method="get">
                        <div class="booking-history-filter__field">
                            <label for="history-status">Trạng thái</label>
                            <select id="history-status" name="status">
                                <option value="" ${empty selectedStatus ? 'selected' : ''}>Tất cả trạng thái</option>
                                <option value="PENDING" ${selectedStatus == 'PENDING' ? 'selected' : ''}>Chờ thanh toán</option>
                                <option value="CONFIRMED" ${selectedStatus == 'CONFIRMED' ? 'selected' : ''}>Đã thanh toán</option>
                                <option value="CHECKED_IN" ${selectedStatus == 'CHECKED_IN' ? 'selected' : ''}>Đã check-in</option>
                                <option value="USED" ${selectedStatus == 'USED' ? 'selected' : ''}>Đã sử dụng</option>
                                <option value="CANCELLED" ${selectedStatus == 'CANCELLED' ? 'selected' : ''}>Đã hủy</option>
                            </select>
                        </div>
                        <div class="booking-history-filter__field">
                            <label for="history-from-date">Từ ngày đặt</label>
                            <input id="history-from-date" type="date" name="fromDate" value="${fromDate}">
                        </div>
                        <div class="booking-history-filter__field">
                            <label for="history-to-date">Đến ngày đặt</label>
                            <input id="history-to-date" type="date" name="toDate" value="${toDate}">
                        </div>
                        <div class="booking-history-filter__actions">
                            <button class="btn btn-primary" type="submit">Lọc</button>
                            <a class="btn btn-ghost" href="${ctx}/my-bookings">Đặt lại</a>
                        </div>
                    </form>

                    <%--
                        Các tab gọi lại đúng route /my-bookings với status khác nhau và luôn quay về page 1.
                        fromDate/toDate được giữ lại để con số badge và bảng đang hiển thị cùng một khoảng dữ liệu.
                    --%>
                    <nav class="booking-history-tabs" aria-label="Lọc nhanh theo trạng thái">
                        <a class="booking-history-tabs__item ${empty selectedStatus ? 'is-active' : ''}"
                           href="${ctx}/my-bookings?fromDate=${fromDate}&amp;toDate=${toDate}">
                            Tất cả <span>${totalAllBookings}</span>
                        </a>
                        <a class="booking-history-tabs__item ${selectedStatus == 'PENDING' ? 'is-active' : ''}"
                           href="${ctx}/my-bookings?status=PENDING&amp;fromDate=${fromDate}&amp;toDate=${toDate}">
                            Chờ thanh toán <span>${statusCounts['PENDING']}</span>
                        </a>
                        <a class="booking-history-tabs__item ${selectedStatus == 'CONFIRMED' ? 'is-active' : ''}"
                           href="${ctx}/my-bookings?status=CONFIRMED&amp;fromDate=${fromDate}&amp;toDate=${toDate}">
                            Đã thanh toán <span>${statusCounts['CONFIRMED']}</span>
                        </a>
                        <a class="booking-history-tabs__item ${selectedStatus == 'CHECKED_IN' ? 'is-active' : ''}"
                           href="${ctx}/my-bookings?status=CHECKED_IN&amp;fromDate=${fromDate}&amp;toDate=${toDate}">
                            Đã check-in <span>${statusCounts['CHECKED_IN']}</span>
                        </a>
                        <a class="booking-history-tabs__item ${selectedStatus == 'USED' ? 'is-active' : ''}"
                           href="${ctx}/my-bookings?status=USED&amp;fromDate=${fromDate}&amp;toDate=${toDate}">
                            Đã sử dụng <span>${statusCounts['USED']}</span>
                        </a>
                        <a class="booking-history-tabs__item ${selectedStatus == 'CANCELLED' ? 'is-active' : ''}"
                           href="${ctx}/my-bookings?status=CANCELLED&amp;fromDate=${fromDate}&amp;toDate=${toDate}">
                            Đã hủy <span>${statusCounts['CANCELLED']}</span>
                        </a>
                    </nav>

                    <c:choose>
                        <c:when test="${empty bookings}">
                            <div class="empty booking-history__empty">
                                <h3>Không có đơn vé phù hợp</h3>
                                <p>Hãy thay đổi bộ lọc hoặc chọn phim để bắt đầu đặt vé.</p>
                                <p><a class="btn btn-primary" href="${ctx}/booking/start">Đặt vé ngay</a></p>
                            </div>
                        </c:when>
                        <c:otherwise>
                            <div class="booking-history-table-wrap">
                                <table class="booking-history-table">
                                    <thead>
                                        <tr>
                                            <th scope="col">Mã đặt vé</th>
                                            <th scope="col">Phim</th>
                                            <th scope="col">Rạp / Phòng</th>
                                            <th scope="col">Ngày / giờ chiếu</th>
                                            <th scope="col">Ghế</th>
                                            <th scope="col">Tổng tiền</th>
                                            <th scope="col">Trạng thái</th>
                                            <th scope="col">Thao tác</th>
                                        </tr>
                                    </thead>
                                    <tbody>
                                        <c:forEach var="bk" items="${bookings}">
                                            <tr>
                                                <td data-label="Mã đặt vé">
                                                    <a class="booking-history-code" href="${ctx}/my-booking?id=${bk.booking.id}">
                                                        #${bk.booking.id}
                                                    </a>
                                                    <small>Đặt: ${bk.bookedAtLabel}</small>
                                                </td>
                                                <td data-label="Phim">
                                                    <a class="booking-history-movie" href="${ctx}/my-booking?id=${bk.booking.id}">
                                                        <c:out value="${bk.movieTitle}"/>
                                                    </a>
                                                </td>
                                                <td data-label="Rạp / Phòng">
                                                    <strong><c:out value="${bk.branchName}"/></strong>
                                                    <small><c:out value="${bk.hallName}"/></small>
                                                </td>
                                                <td data-label="Ngày / giờ chiếu">${bk.showTimeLabel}</td>
                                                <td data-label="Ghế">
                                                    <strong>${bk.seatCount} ghế</strong>
                                                    <small><c:out value="${bk.seatLabels}"/></small>
                                                </td>
                                                <td data-label="Tổng tiền" class="booking-history-price">${bk.totalPriceLabel}</td>
                                                <td data-label="Trạng thái">
                                                    <span class="badge booking-history-status ${bk.statusBadgeClass}">${bk.statusLabel}</span>
                                                </td>
                                                <td data-label="Thao tác">
                                                    <div class="booking-history-actions">
                                                        <a class="btn btn-ghost btn-sm" href="${ctx}/my-booking?id=${bk.booking.id}">Chi tiết</a>

                                                        <%-- bk.reviewable do BookingView tính từ status: chỉ CHECKED_IN/USED mới mở luồng ReviewController. --%>
                                                        <c:if test="${bk.reviewable}">
                                                            <a class="btn btn-primary btn-sm"
                                                               href="${ctx}/review?bookingId=${bk.booking.id}&amp;movieId=${bk.movieId}">Đánh giá</a>
                                                        </c:if>

                                                        <%--
                                                            UI chỉ hiển thị Hủy vé khi status PENDING (chưa thanh toán).
                                                            Form POST /my-booking truyền booking id sang BookingDetailController;
                                                            Controller/Service/DAO vẫn kiểm tra lại user sở hữu đơn và trạng thái,
                                                            không tin chỉ riêng điều kiện hiển thị ở JSP.
                                                        --%>
                                                        <c:if test="${bk.booking.status == 'PENDING'}">
                                                            <form action="${ctx}/my-booking" method="post" class="booking-history-cancel-form">
                                                                <input type="hidden" name="id" value="${bk.booking.id}">
                                                                <button class="btn btn-ghost btn-sm booking-history-cancel" type="submit"
                                                                        onclick="return confirm('Bạn có chắc muốn hủy đơn vé #${bk.booking.id}?');">Hủy vé</button>
                                                            </form>
                                                        </c:if>
                                                    </div>
                                                </td>
                                            </tr>
                                        </c:forEach>
                                    </tbody>
                                </table>
                            </div>

                            <%-- Controller đã chặn currentPage trong [1..totalPages], nên link phân trang sinh ra luôn hợp lệ. --%>
                            <c:if test="${totalPages > 1}">
                                <nav class="booking-history-pagination" aria-label="Phân trang lịch sử vé">
                                    <c:if test="${currentPage > 1}">
                                        <a href="${ctx}/my-bookings?status=${selectedStatus}&amp;fromDate=${fromDate}&amp;toDate=${toDate}&amp;page=${currentPage - 1}">‹</a>
                                    </c:if>
                                    <c:forEach begin="1" end="${totalPages}" var="pageNumber">
                                        <a class="${pageNumber == currentPage ? 'is-active' : ''}"
                                           href="${ctx}/my-bookings?status=${selectedStatus}&amp;fromDate=${fromDate}&amp;toDate=${toDate}&amp;page=${pageNumber}">${pageNumber}</a>
                                    </c:forEach>
                                    <c:if test="${currentPage < totalPages}">
                                        <a href="${ctx}/my-bookings?status=${selectedStatus}&amp;fromDate=${fromDate}&amp;toDate=${toDate}&amp;page=${currentPage + 1}">›</a>
                                    </c:if>
                                </nav>
                            </c:if>
                        </c:otherwise>
                    </c:choose>
                </section>
            </div>
        </main>

        <jsp:include page="/pages/common/footer.jsp" />
    </body>
</html>
