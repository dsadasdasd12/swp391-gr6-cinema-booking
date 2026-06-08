<%@page contentType="text/html" pageEncoding="UTF-8"%>
<%@taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<%@taglib prefix="fmt" uri="http://java.sun.com/jsp/jstl/fmt" %>
<%@page import="java.net.InetAddress"%>
<%
    String serverIp = request.getServerName();
    if ("localhost".equals(serverIp) || "127.0.0.1".equals(serverIp)) {
        try {
            serverIp = InetAddress.getLocalHost().getHostAddress();
        } catch (Exception e) {
            // Keep localhost on failure
        }
    }
    int port = request.getServerPort();
    String localBaseUrl = request.getScheme() + "://" + serverIp + ":" + port + request.getContextPath();
%>
<!DOCTYPE html>
<html lang="vi">
<head>
    <meta charset="UTF-8">
    <meta name="viewport" content="width=device-width, initial-scale=1.0">
    <title>Hóa Đơn Vé Xem Phim RVS - #${booking.id}</title>
    
    <link href="https://fonts.googleapis.com/css2?family=Courier+Prime:wght@400;700&display=swap" rel="stylesheet">
    
    <style>
        body {
            background-color: #111827;
            color: #f3f4f6;
            font-family: 'Courier Prime', monospace;
            margin: 0;
            padding: 40px;
            display: flex;
            flex-direction: column;
            align-items: center;
            justify-content: center;
            min-height: 100vh;
        }

        .ticket {
            background: #ffffff;
            color: #000000;
            width: 320px;
            padding: 30px 20px;
            border-radius: 4px;
            box-shadow: 0 10px 25px rgba(0, 0, 0, 0.5);
            box-sizing: border-box;
            border: 1px solid #e5e7eb;
            position: relative;
        }

        /* Thermal cut style dots */
        .ticket::before, .ticket::after {
            content: '';
            position: absolute;
            left: 0;
            right: 0;
            height: 8px;
            background-size: 16px 8px;
            background-repeat: repeat-x;
        }
        
        .ticket::before {
            top: -4px;
            background-image: radial-gradient(circle, transparent, transparent 50%, #ffffff 50%, #ffffff 100%);
        }
        
        .ticket::after {
            bottom: -4px;
            background-image: radial-gradient(circle, #ffffff, #ffffff 50%, transparent 50%, transparent 100%);
        }

        .cinema-header {
            text-align: center;
            border-bottom: 2px dashed #000000;
            padding-bottom: 15px;
            margin-bottom: 15px;
        }

        .cinema-title {
            font-size: 18px;
            font-weight: 700;
            margin: 0;
            letter-spacing: 1px;
        }

        .cinema-address {
            font-size: 11px;
            margin: 5px 0 0 0;
            color: #374151;
        }

        .details-row {
            display: flex;
            justify-content: space-between;
            margin-bottom: 10px;
            font-size: 13px;
        }

        .movie-title {
            font-size: 16px;
            font-weight: 700;
            text-align: center;
            margin: 15px 0;
            text-transform: uppercase;
        }

        .barcode-container {
            display: flex;
            flex-direction: column;
            align-items: center;
            margin-top: 30px;
            padding-top: 15px;
            border-top: 2px dashed #000000;
        }

        /* Barcode mock using CSS */
        .barcode {
            width: 200px;
            height: 50px;
            background: repeating-linear-gradient(
                90deg,
                #000,
                #000 2px,
                #fff 2px,
                #fff 4px,
                #000 4px,
                #000 8px,
                #fff 8px,
                #fff 10px
            );
            margin-bottom: 5px;
        }

        .barcode-text {
            font-size: 12px;
            font-weight: 700;
            letter-spacing: 2px;
        }

        .btn-print {
            background: #10b981;
            border: none;
            color: white;
            padding: 12px 30px;
            font-size: 15px;
            font-weight: 700;
            border-radius: 6px;
            cursor: pointer;
            margin-bottom: 25px;
            box-shadow: 0 4px 15px rgba(16, 185, 129, 0.4);
            display: flex;
            align-items: center;
            gap: 8px;
            transition: opacity 0.3s;
        }

        .btn-print:hover {
            opacity: 0.9;
        }

        /* Printer Media Rules */
        @media print {
            .no-print {
                display: none !important;
            }
            body {
                background: white;
                color: black;
                padding: 0;
                margin: 0;
            }
            .ticket {
                box-shadow: none;
                border: none;
            }
        }
    </style>
</head>
<body>

    <!-- Nút In vé ẩn khi xuất file PDF/giấy in -->
    <div class="no-print" style="display: flex; gap: 15px;">
        <button class="btn-print" onclick="window.print()" id="btnPrintTicket">
            <svg width="18" height="18" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round">
                <polyline points="6 9 6 2 18 2 18 9"></polyline>
                <path d="M6 18H4a2 2 0 0 1-2-2v-5a2 2 0 0 1 2-2h16a2 2 0 0 1 2 2v5a2 2 0 0 1-2 2h-2"></path>
                <rect x="6" y="14" width="12" height="8"></rect>
            </svg>
            In Vé Ngay (Print)
        </button>
        <button class="btn-print" style="background: #374151; box-shadow: none;" onclick="window.location.href='CounterBooking'" id="btnBackToPOS">
            Quay lại POS
        </button>
    </div>

    <!-- Phiếu Vé Nhiệt -->
    <article class="ticket" id="thermalTicket">
        
        <header class="cinema-header">
            <h1 class="cinema-title">RAPVIET CINEPLEX</h1>
            <p class="cinema-address">Hanoi, May 2026 - RVS Station</p>
        </header>

        <section>
            <div class="details-row">
                <span>Vé Số (ID):</span>
                <strong>#${booking.id}</strong>
            </div>
            <div class="details-row">
                <span>Ngày mua:</span>
                <span>${booking.bookedAt}</span>
            </div>
            <div class="details-row">
                <span>Nhân viên:</span>
                <span>Staff #10</span>
            </div>
        </section>

        <main>
            <h2 class="movie-title">${showtime.movieTitle}</h2>

            <div class="details-row">
                <span>Phòng chiếu:</span>
                <strong>${showtime.hallName}</strong>
            </div>
            <div class="details-row">
                <span>Suất chiếu:</span>
                <strong>${showtime.getFormattedStartTime()}</strong>
            </div>
            <div class="details-row">
                <span>Vị trí ghế:</span>
                <strong style="font-size: 15px; letter-spacing: 1px;">${seatCodes}</strong>
            </div>
        </main>

        <footer style="margin-top: 20px; padding-top: 15px; border-top: 1px dashed #000000;">
            <div class="details-row" style="font-size: 15px; font-weight: 700;">
                <span>Tổng tiền:</span>
                <span><fmt:formatNumber value="${booking.totalPrice}" pattern="#,##0"/> đ</span>
            </div>
            <div class="details-row" style="font-size: 12px; color: #4b5563;">
                <span>Thanh toán:</span>
                <span>Tiền mặt (CASH)</span>
            </div>
        </footer>
        <!-- QR Code quét soát vé cổng và tự động xác thực -->
        <div class="barcode-container" style="border-top: 2px dashed #000000; padding-top: 15px; margin-top: 25px; display: flex; flex-direction: column; align-items: center;">
            <img id="ticketQrImage" src="" alt="Ticket QR Code" style="width: 130px; height: 130px; margin-bottom: 8px;" />
            <div class="barcode-text" style="font-size: 13px; font-weight: 700; letter-spacing: 1px; color: #000;">${booking.qrCode}</div>
            <div style="font-size: 10px; color: #6b7280; margin-top: 4px; text-align: center; max-width: 250px;">
                Quét mã QR bằng điện thoại để tự động soát vé vào cổng
            </div>
        </div>

    </article>

    <script>
        window.addEventListener('DOMContentLoaded', (event) => {
            // Sử dụng địa chỉ IP nội bộ đã được nhận diện từ server
            const serverUrl = '<%= localBaseUrl %>/TicketValidation?action=validate&bookingId=${booking.id}';
            
            // Sử dụng API tạo QR Code miễn phí
            const qrApiUrl = 'https://api.qrserver.com/v1/create-qr-code/?size=200x200&data=' + encodeURIComponent(serverUrl);
            
            const qrImg = document.getElementById('ticketQrImage');
            if (qrImg) {
                qrImg.src = qrApiUrl;
            }
        });
    </script>
</body>
</html>
