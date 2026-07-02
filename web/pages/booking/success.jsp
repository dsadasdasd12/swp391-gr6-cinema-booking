<%@ page contentType="text/html; charset=UTF-8" pageEncoding="UTF-8" %>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<%@ taglib prefix="fmt" uri="http://java.sun.com/jsp/jstl/fmt" %>

<c:set var="ctx" value="${pageContext.request.contextPath}" />

<!DOCTYPE html>
<html lang="vi">
<head>
    <meta charset="UTF-8">
    <title>Đặt vé thành công - RapViet Cinema</title>
    <link rel="stylesheet" href="${ctx}/assets/css/style.css">

    <style>
        body {
            background: #030712;
            color: white;
            font-family: Arial, sans-serif;
        }

        .success-page {
            max-width: 1200px;
            margin: 40px auto;
            padding: 20px;
        }

        .success-header {
            background: #111827;
            border: 1px solid #374151;
            border-radius: 16px;
            padding: 24px;
            margin-bottom: 24px;
            text-align: center;
        }

        .success-icon {
            font-size: 48px;
            color: #22c55e;
            margin-bottom: 10px;
        }

        .success-title {
            color: #22c55e;
            margin: 0;
        }

        .booking-info {
            margin-top: 18px;
            color: #cbd5e1;
        }

        .payment-info {
            background: #111827;
            border: 1px solid #374151;
            border-radius: 16px;
            padding: 22px;
            margin-bottom: 26px;
        }

        .payment-grid {
            display: grid;
            grid-template-columns: repeat(4, 1fr);
            gap: 14px;
        }

        .payment-item {
            background: #0b1220;
            border: 1px solid #253041;
            border-radius: 12px;
            padding: 14px;
        }

        .payment-item span {
            display: block;
            color: #9ca3af;
            font-size: 13px;
            margin-bottom: 8px;
        }

        .payment-item strong {
            color: white;
            font-size: 15px;
        }

        .ticket-list {
            display: flex;
            flex-direction: column;
            gap: 18px;
        }

        .ticket-card {
            display: grid;
            grid-template-columns: 160px 1fr 180px;
            gap: 20px;
            align-items: center;
            background: #111827;
            border: 1px solid #374151;
            border-radius: 16px;
            padding: 20px;
            box-shadow: 0 0 24px rgba(239, 68, 68, 0.08);
        }

        .ticket-poster {
            width: 150px;
            height: 210px;
            border-radius: 12px;
            object-fit: cover;
            background: #1f2937;
        }

        .ticket-main h2 {
            margin: 0 0 12px;
            color: #ef4444;
        }

        .ticket-row {
            display: flex;
            justify-content: space-between;
            border-bottom: 1px solid #253041;
            padding: 9px 0;
            gap: 20px;
        }

        .ticket-row span {
            color: #9ca3af;
        }

        .ticket-row strong {
            color: white;
        }

        .seat-badge {
            display: inline-block;
            background: #dc2626;
            color: white;
            padding: 8px 18px;
            border-radius: 999px;
            font-weight: 800;
            font-size: 22px;
        }

        .ticket-side {
            text-align: center;
            border-left: 1px dashed #4b5563;
            padding-left: 20px;
        }

        .ticket-code {
            color: #facc15;
            font-size: 22px;
            font-weight: 800;
            margin-bottom: 16px;
        }

        .ticket-price {
            color: #22c55e;
            font-size: 24px;
            font-weight: 900;
        }

        .actions {
            margin-top: 28px;
            display: flex;
            justify-content: center;
            gap: 14px;
        }

        .btn {
            padding: 12px 20px;
            border-radius: 8px;
            text-decoration: none;
            font-weight: 700;
        }

        .btn-primary {
            background: #dc2626;
            color: white;
        }

        .btn-secondary {
            background: #374151;
            color: white;
        }

        @media(max-width: 900px) {
            .payment-grid {
                grid-template-columns: 1fr 1fr;
            }

            .ticket-card {
                grid-template-columns: 1fr;
            }

            .ticket-poster {
                display: none;
            }

            .ticket-side {
                border-left: none;
                border-top: 1px dashed #4b5563;
                padding-left: 0;
                padding-top: 18px;
            }
        }
    </style>
</head>

<body>

<div class="success-page">

    <c:choose>
        <c:when test="${empty ticket}">
            <div class="success-header">
                <h1>Không tìm thấy thông tin vé</h1>
                <a class="btn btn-primary" href="${ctx}/home">Về trang chủ</a>
            </div>
        </c:when>

        <c:otherwise>

            <div class="success-header">
                <div class="success-icon">✓</div>
                <h1 class="success-title">Đặt vé thành công</h1>
                <div class="booking-info">
                    Booking #${ticket.bookingId} • ${ticket.bookingStatus} • ${ticket.paymentStatus}
                </div>
            </div>

            <div class="payment-info">
                <h2>Thông tin thanh toán</h2>

                <div class="payment-grid">
                    <div class="payment-item">
                        <span>Gateway</span>
                        <strong>${ticket.paymentGateway}</strong>
                    </div>

                    <div class="payment-item">
                        <span>Phương thức</span>
                        <strong>${ticket.paymentMethod}</strong>
                    </div>

                    <div class="payment-item">
                        <span>Mã giao dịch</span>
                        <strong>${ticket.transactionId}</strong>
                    </div>

                    <div class="payment-item">
                        <span>Tổng thanh toán</span>
                        <strong>
                            <fmt:formatNumber value="${ticket.finalAmount}" pattern="#,##0"/> đ
                        </strong>
                    </div>
                </div>
            </div>

            <div class="ticket-list">

                <c:forEach var="seat" items="${ticket.seats}">
                    <div class="ticket-card">

                        <c:choose>
                            <c:when test="${not empty ticket.moviePoster}">
                                <img class="ticket-poster" src="${ticket.moviePoster}" alt="${ticket.movieTitle}">
                            </c:when>
                            <c:otherwise>
                                <div class="ticket-poster"></div>
                            </c:otherwise>
                        </c:choose>

                        <div class="ticket-main">
                            <h2>${ticket.movieTitle}</h2>

                            <div class="ticket-row">
                                <span>Ngày chiếu</span>
                                <strong>${ticket.showDate}</strong>
                            </div>

                            <div class="ticket-row">
                                <span>Giờ chiếu</span>
                                <strong>${ticket.showTime}</strong>
                            </div>

                            <div class="ticket-row">
                                <span>Địa điểm</span>
                                <strong>${ticket.branchName}</strong>
                            </div>

                            <div class="ticket-row">
                                <span>Phòng</span>
                                <strong>${ticket.hallName}</strong>
                            </div>

                            <div class="ticket-row">
                                <span>Loại ghế</span>
                                <strong>${seat.seatType}</strong>
                            </div>
                        </div>

                        <div class="ticket-side">
                            <div class="ticket-code">${ticket.qrCode}</div>

                            <div>Ghế</div>
                            <div class="seat-badge">${seat.seatName}</div>

                            <div style="margin-top:18px;">Giá vé</div>
                            <div class="ticket-price">
                                <fmt:formatNumber value="${seat.price}" pattern="#,##0"/> đ
                            </div>
                        </div>

                    </div>
                </c:forEach>

            </div>

            <div class="actions">
                <a class="btn btn-secondary" href="${ctx}/home">Về trang chủ</a>
                <a class="btn btn-primary" href="${ctx}/movies">Tiếp tục đặt vé</a>
            </div>

        </c:otherwise>
    </c:choose>

</div>

</body>
</html>