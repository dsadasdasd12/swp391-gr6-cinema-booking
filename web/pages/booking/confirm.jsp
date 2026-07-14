<%@ page contentType="text/html; charset=UTF-8" pageEncoding="UTF-8" %>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<c:set var="ctx" value="${pageContext.request.contextPath}" />
<c:set var="st" value="${draftView.showtime}" />

<!DOCTYPE html>
<html lang="vi">
<head>
    <meta charset="UTF-8">
    <meta name="viewport" content="width=device-width, initial-scale=1">
    <title>Xác nhận đặt vé - RapViet Cinema</title>
    <link rel="stylesheet" href="${ctx}/assets/css/style.css">
    <link rel="stylesheet" href="${ctx}/assets/css/movie.css">
</head>
<body>
    <jsp:include page="/pages/common/header.jsp">
        <jsp:param name="active" value="booking"/>
    </jsp:include>

    <div class="page-wrap">
        <div class="container">
            <div class="flow-head">
                <div>
                    <div class="flow-step">Bước 5 / 5</div>
                    <h1 class="page-title">Xác nhận đặt vé</h1>
                </div>
            </div>

            <c:if test="${not empty error}">
                <div class="notice err"><c:out value="${error}"/></div>
            </c:if>

            <c:choose>
                <c:when test="${draftInvalid or empty draftView}">
                    <div class="empty">
                        <h3>Thông tin giữ chỗ không còn hợp lệ</h3>
                        <p><a class="btn btn-primary" href="${ctx}/booking/start">Chọn lại</a></p>
                    </div>
                </c:when>
                <c:otherwise>
                    <div class="booking-detail">
                        <div class="bd-head">
                            <h1><c:out value="${st.movieTitle}"/></h1>
                            <span class="badge soon">PENDING</span>
                        </div>

                        <div class="detail-table">
                            <div><span class="k">Chi nhánh</span> <c:out value="${st.branchName}"/></div>
                            <div><span class="k">Phòng</span> <c:out value="${st.hallName}"/> (<c:out value="${st.hallType}"/>)</div>
                            <div><span class="k">Suất chiếu</span> ${st.showDate} ${st.startHour} - ${st.endHour}</div>
                            <div><span class="k">Ghế</span> <c:out value="${draftView.seatLabels}"/> (${draftView.seatCount} ghế)</div>
                            <div><span class="k">Tổng tiền</span> ${draftView.totalPriceLabel}</div>
                        </div>

                        <c:if test="${not empty voucherQuote}">
                            <div class="notice ok">
                                Mã <strong><c:out value="${voucherQuote.code}"/></strong> đã được áp dụng:
                                giảm ${voucherQuote.discountAmount} đ. Tổng thanh toán:
                                ${draftView.totalPrice - voucherQuote.discountAmount} đ.
                            </div>
                        </c:if>

                        <div class="booking-seat-lines">
                            <c:forEach var="line" items="${draftView.seats}">
                                <div class="booking-seat-line">
                                    <span><c:out value="${line.seatCode}"/> · <c:out value="${line.seatType}"/></span>
                                    <strong>${line.priceLabel}</strong>
                                </div>
                            </c:forEach>
                        </div>

                        <form method="post" action="${ctx}/booking/confirm" style="margin: 14px 0;">
                            <label for="voucherCode" class="k" style="display:block; margin-bottom:6px;">Mã giảm giá</label>
                            <input id="voucherCode" name="voucherCode" type="text" value="${voucherQuote.code}" placeholder="VD: GIAM20K" style="text-transform:uppercase;">
                            <button type="submit" name="action" value="applyVoucher" class="btn btn-ghost">Áp dụng mã</button>
                        </form>

                        <div class="bd-actions">
                            <a class="btn btn-ghost" href="${ctx}/booking/seats?showtimeId=${st.id}">Chọn lại ghế</a>
                            <form method="post" action="${ctx}/booking/confirm">
                                <button type="submit" class="btn btn-primary">Xác nhận đặt vé</button>
                            </form>
                        </div>
                    </div>
                </c:otherwise>
            </c:choose>
        </div>
    </div>

    <jsp:include page="/pages/common/footer.jsp" />
</body>
</html>
