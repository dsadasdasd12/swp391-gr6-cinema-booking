<%@ page contentType="text/html; charset=UTF-8" pageEncoding="UTF-8" %>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<%@ taglib prefix="fmt" uri="http://java.sun.com/jsp/jstl/fmt" %>

<c:set var="ctx" value="${pageContext.request.contextPath}" />

<!DOCTYPE html>
<html lang="vi">
    <head>
        <meta charset="UTF-8">
        <title>Thanh toán SePay - RapViet Cinema</title>
        <link rel="stylesheet" href="${ctx}/assets/css/style.css">

        <style>
            body {
                background: #030712;
                color: #ffffff;
                font-family: Arial, sans-serif;
            }

            .payment-page {
                max-width: 1100px;
                margin: 40px auto;
                padding: 20px;
            }

            .payment-grid {
                display: grid;
                grid-template-columns: 1.1fr 0.9fr;
                gap: 24px;
            }

            .payment-card {
                background: #111827;
                border: 1px solid #374151;
                border-radius: 14px;
                padding: 24px;
            }

            .payment-title {
                color: #ef4444;
                margin-bottom: 16px;
            }

            .info-row {
                display: flex;
                justify-content: space-between;
                border-bottom: 1px solid #253041;
                padding: 12px 0;
                gap: 20px;
            }

            .info-row span:first-child {
                color: #9ca3af;
            }

            .amount {
                font-size: 30px;
                font-weight: 800;
                color: #22c55e;
            }

            .qr-box {
                text-align: center;
            }

            .qr-box img{
                width:280px;
                max-width:100%;
                background:#fff;
                padding:12px;
                border-radius:12px;

                display:block;
                margin-left:auto;
                margin-right:auto;
            }

            .transfer-code {
                font-size: 26px;
                font-weight: 800;
                color: #facc15;
                letter-spacing: 1px;
                margin-top: 14px;
            }

            .note {
                color: #cbd5e1;
                line-height: 1.6;
                margin-top: 18px;
            }

            .actions {
                display: flex;
                gap: 12px;
                margin-top: 24px;
            }

            .btn {
                padding: 12px 18px;
                border-radius: 8px;
                text-decoration: none;
                border: none;
                cursor: pointer;
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

            @media(max-width: 800px) {
                .payment-grid {
                    grid-template-columns: 1fr;
                }
            }
        </style>
    </head>

    <body>

        <div class="payment-page">

            <h1 class="payment-title">Thanh toán đặt vé</h1>

            <c:choose>
                <c:when test="${empty payment}">
                    <div class="payment-card">
                        Không tìm thấy thông tin thanh toán.
                    </div>
                </c:when>

                <c:otherwise>
                    <div class="payment-grid">

                        <div class="payment-card">
                            <h2>Thông tin đặt vé</h2>

                            <div class="info-row">
                                <span>Phim</span>
                                <strong>${payment.movieTitle}</strong>
                            </div>

                            <div class="info-row">
                                <span>Ngày chiếu</span>
                                <strong>${payment.showDate}</strong>
                            </div>

                            <div class="info-row">
                                <span>Giờ chiếu</span>
                                <strong>${payment.showTime}</strong>
                            </div>

                            <div class="info-row">
                                <span>Chi nhánh</span>
                                <strong>${payment.branchName}</strong>
                            </div>

                            <div class="info-row">
                                <span>Phòng</span>
                                <strong>${payment.hallName}</strong>
                            </div>

                            <div class="info-row">
                                <span>Mã booking</span>
                                <strong>#${payment.bookingId}</strong>
                            </div>

                            <div class="info-row">
                                <span>Trạng thái booking</span>
                                <strong>${payment.bookingStatus}</strong>
                            </div>

                            <div class="info-row">
                                <span>Trạng thái thanh toán</span>
                                <strong>${payment.status}</strong>
                            </div>

                            <div class="info-row">
                                <span>Tổng tiền</span>
                                <strong class="amount">
                                    <fmt:formatNumber value="${payment.amount}" pattern="#,##0"/> đ
                                </strong>
                            </div>

                            <div class="actions">
                                <a class="btn btn-secondary"
                                   href="${ctx}/home">
                                    Về trang chủ
                                </a>

                                <a class="btn btn-primary"
                                   href="${ctx}/booking/success?bookingId=${payment.bookingId}">
                                    Tôi đã thanh toán
                                </a>
                            </div>
                        </div>

                        <div class="payment-card qr-box">
                            <h2>Quét QR BIDV</h2>

                            <img src="${payment.qrUrl}" alt="QR thanh toán">

                            <div class="transfer-code">
                                ${payment.transferContent}
                            </div>

                            <p class="note">
                                Vui lòng chuyển khoản đúng số tiền và đúng nội dung.
                                Hệ thống sẽ tự động xác nhận khi SePay gửi webhook.
                            </p>

                            <div class="info-row">
                                <span>Gateway</span>
                                <strong>${payment.gateway}</strong>
                            </div>

                            <div class="info-row">
                                <span>Method</span>
                                <strong>${payment.method}</strong>
                            </div>

                            <div class="info-row">
                                <span>Mã giao dịch</span>
                                <strong>${payment.transactionId}</strong>
                            </div>
                        </div>

                    </div>
                </c:otherwise>
            </c:choose>

        </div>

    </body>
</html>