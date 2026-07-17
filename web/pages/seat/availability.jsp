<%--
    RapViet Cinema - Sơ đồ ghế của một suất chiếu (Xem tình trạng ghế)
    Module: Duyệt phim - View seat availability   (Group6 - Huy)
    Được phục vụ bởi controller.SeatAvailabilityController  ->  URL /seats?showtimeId=N
    View chỉ dùng JSTL + EL + component, KHÔNG nhúng code Java.
--%>
<%@ page contentType="text/html; charset=UTF-8" pageEncoding="UTF-8" %>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<c:set var="ctx" value="${pageContext.request.contextPath}" />
<c:set var="st" value="${seatMap.showtime}" />

<!DOCTYPE html>
<html lang="vi">
    <head>
        <meta charset="UTF-8">
        <style>
            <c:forEach items="${allSeatTypes}" var="st">
                .seat.available.${st.code}:not(.selected) {
                    background-color: ${st.color} !important;
                }
                .seat-choice input:checked + .seat.available.${st.code} {
                    background-color: #10b981 !important;
                    border-color: #10b981 !important;
                }
            </c:forEach>
        </style>
        <meta name="viewport" content="width=device-width, initial-scale=1">
        <title><c:out value="${empty st ? 'Không tìm thấy suất chiếu' : st.movieTitle}"/> - Sơ đồ ghế - RapViet Cinema</title>
        <link rel="stylesheet" href="${ctx}/assets/css/style.css">
        <link rel="stylesheet" href="${ctx}/assets/css/movie.css?v=2">
    </head>
    <body>
        <jsp:include page="/pages/common/header.jsp">
            <jsp:param name="active" value="showtimes"/>
        </jsp:include>

        <div class="page-wrap">
            <div class="container">
                <c:choose>
                    <%-- ── Không tìm thấy suất chiếu ── --%>
                    <c:when test="${notFound or empty seatMap}">
                        <div class="empty">
                            <h3>Không tìm thấy suất chiếu</h3>
                            <p>Suất chiếu bạn chọn không tồn tại hoặc đã bị gỡ.</p>
                            <p><a class="btn btn-primary" href="${ctx}/showtimes">← Về lịch chiếu</a></p>
                        </div>
                    </c:when>

                    <%-- ── Sơ đồ ghế ── --%>
                    <c:when test="${empty bookingMode}">
                        <%-- Ngữ cảnh suất chiếu: phim / rạp / phòng / ngày giờ --%>
                        <div class="seat-context">
                            <h1><c:out value="${st.movieTitle}"/></h1>
                            <ul class="seat-meta">
                                <li>🏢 <c:out value="${st.branchName}"/></li>
                                <li>🎬 Phòng <c:out value="${st.hallName}"/> (<c:out value="${st.hallType}"/>)</li>
                                <li>📅 ${st.showDate}</li>
                                <li>🕒 ${st.startHour} - ${st.endHour}</li>
                            </ul>
                            <div class="result-meta">
                                Còn trống <strong>${seatMap.availableSeats}</strong> / ${seatMap.totalSeats} ghế
                            </div>
                        </div>

                        <%-- Chú thích màu trạng thái --%>
                        <div class="seat-legend" style="flex-wrap: wrap; gap: 10px 20px;">
                            <span><i class="seat available"></i> Còn trống</span>
                            <span><i class="seat booked"></i> Đã đạt</span>
                            <span><i class="seat maintenance"></i> Bảo trì</span>
                            <c:forEach items="${allSeatTypes}" var="st">
                                <c:if test="${st.status == 'ACTIVE' && st.code != 'STANDARD'}">
                                    <span><i class="seat available ${st.code}"></i> ${st.name}</span>
                                </c:if>
                            </c:forEach>
                        </div>

                        <%-- Biểu tượng màn hình --%>
                        <div class="screen">MÀN HÌNH</div>

                        <%-- Lưới ghế: vòng ngoài theo hàng, vòng trong theo ghế --%>
                        <div class="seat-map">
                            <c:forEach var="row" items="${seatMap.rows}">
                                <div class="seat-row">
                                    <span class="row-label"><c:out value="${row.rowLabel}"/></span>
                                    <%-- sv là dto.SeatView: sv.seat = entity Seat (cột DB),
                                         sv.statusClass/statusLabel = trạng thái suy theo suất chiếu --%>
                                    <c:forEach var="sv" items="${row.seats}">
                                        <span class="seat ${sv.statusClass} ${sv.seat.seatType}"
                                              title="${sv.seat.seatCode} - ${sv.statusLabel}">
                                            ${sv.seat.seatNumber}
                                        </span>
                                    </c:forEach>
                                </div>
                            </c:forEach>
                        </div>
                    </c:when>

                    <%-- ── Sơ đồ ghế ── --%>
                    <c:otherwise>
                        <%-- Ngữ cảnh suất chiếu: phim / rạp / phòng / ngày giờ --%>
                        <div class="seat-context">
                            <h1><c:out value="${st.movieTitle}"/></h1>
                            <ul class="seat-meta">
                                <li>🏢 <c:out value="${st.branchName}"/></li>
                                <li>🎬 Phòng <c:out value="${st.hallName}"/> (<c:out value="${st.hallType}"/>)</li>
                                <li>📅 ${st.showDate}</li>
                                <li>🕒 ${st.startHour} - ${st.endHour}</li>
                            </ul>
                            <div class="result-meta">
                                Còn trống <strong>${seatMap.availableSeats}</strong> / ${seatMap.totalSeats} ghế
                            </div>
                        </div>

                        <%-- Chú thích màu trạng thái --%>
                        <div class="seat-legend" style="flex-wrap: wrap; gap: 10px 20px;">
                            <span><i class="seat available"></i> Còn trống</span>
                            <span><i class="seat booked"></i> Đã đạt</span>
                            <span><i class="seat maintenance"></i> Bảo trì</span>
                            <c:forEach items="${allSeatTypes}" var="st">
                                <c:if test="${st.status == 'ACTIVE' && st.code != 'STANDARD'}">
                                    <span><i class="seat available ${st.code}"></i> ${st.name}</span>
                                </c:if>
                            </c:forEach>
                        </div>

                        <%-- Biểu tượng màn hình --%>
                        <div class="screen">MÀN HÌNH</div>

                        <%-- Lưới ghế: vòng ngoài theo hàng, vòng trong theo ghế --%> 
                        <form action="${ctx}/booking/payment" method="post" onsubmit="return validateSeats();">
                            <input type="hidden" name="showtimeId" value="${st.id}">
                            <div class="seat-map">
                                <c:forEach var="row" items="${seatMap.rows}">
                                    <div class="seat-row">
                                        <span class="row-label">${row.rowLabel}</span>

                                        <c:forEach var="sv" items="${row.seats}">
                                            <c:choose>
                                                <c:when test="${sv.selectable}">
                                                    <label class="seat ${sv.statusClass} ${sv.seat.seatType}"
                                                           title="${sv.seat.seatRow}${sv.seat.seatNumber} - ${sv.statusLabel}">
                                                        <!-- thay vao input data-price="${sv.price}" -->
                                                        <input type="checkbox"
                                                               name="seatIds"
                                                               value="${sv.seat.id}"
                                                               data-seat-code="${sv.seat.seatRow}${sv.seat.seatNumber}"
                                                               data-seat-type="${sv.seat.seatType}"
                                                               data-price="${sv.price}"
                                                               hidden
                                                               onchange="toggleSeat(this)">
                                                        ${sv.seat.seatNumber}
                                                    </label>
                                                </c:when>

                                                <c:otherwise>
                                                    <span class="seat ${sv.statusClass} ${sv.seat.seatType}"
                                                          title="${sv.seat.seatRow}${sv.seat.seatNumber} - ${sv.statusLabel}">
                                                        ${sv.seat.seatNumber}
                                                    </span>
                                                </c:otherwise>
                                            </c:choose>
                                        </c:forEach>
                                    </div>
                                </c:forEach>
                            </div>
                            <div style="margin-top:24px;
                                 display:flex;
                                 justify-content:space-between;
                                 align-items:center;">

                                <div>
                                    <strong>Tổng tiền:</strong>
                                    <span id="totalPrice">0 đ</span>
                                </div>

                                <button type="submit" class="btn btn-primary">
                                    Tiếp tục thanh toán
                                </button>

                            </div>
                        </form>

                        <p style="margin-top:24px;">
                            <a href="${ctx}/showtimes?branchId=${st.branchId}" style="color:#9aa0aa;">← Về lịch chiếu</a>
                        </p>
                    </c:otherwise>
                </c:choose>
            </div>
        </div>

        <jsp:include page="/pages/common/footer.jsp" />
    </body>
    <script>
        function formatMoney(value) {
            return Number(value).toLocaleString("vi-VN") + " đ";
        }

        function toggleSeat(input) {

            const seat = input.closest(".seat");

            if (input.checked) {
                seat.classList.add("selected");
            } else {
                seat.classList.remove("selected");
            }

            updateTotal();
        }

        function updateTotal() {

            let total = 0;

            document.querySelectorAll("input[name='seatIds']:checked")
                    .forEach(function (item) {
                        total += Number(item.dataset.price);
                    });

            document.getElementById("totalPrice").innerHTML = formatMoney(total);
        }

        function validateSeats() {

            if (document.querySelectorAll("input[name='seatIds']:checked").length == 0) {
                alert("Vui lòng chọn ít nhất một ghế.");
                return false;
            }

            return true;
        }
    </script>
</html>

