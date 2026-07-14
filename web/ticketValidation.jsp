<%@page contentType="text/html" pageEncoding="UTF-8"%>
<%@taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<!DOCTYPE html>
<html lang="vi">
<head>
    <meta charset="UTF-8">
    <meta name="viewport" content="width=device-width, initial-scale=1.0">
    <meta name="description" content="Hệ thống soát vé cổng rạp thông minh RVS">
    <title>RVS - Kiểm Soát Vé Vào Cổng</title>
    
    <!-- Modern Google Font -->
    <link href="https://fonts.googleapis.com/css2?family=Inter:wght@300;400;600;700&display=swap" rel="stylesheet">
    
    <style>
        :root {
            --bg-color: hsl(222, 47%, 6%);
            --glass-bg: hsla(222, 47%, 12%, 0.7);
            --border-color: hsla(217, 30%, 20%, 0.5);
            --primary: hsl(224, 89%, 60%);
            --emerald: hsl(150, 84%, 37%);
            --crimson: hsl(350, 80%, 50%);
            --text-color: hsl(0, 0%, 100%);
            --muted-text: hsl(215, 20%, 65%);
        }

        body {
            background-color: var(--bg-color);
            color: var(--text-color);
            font-family: 'Inter', sans-serif;
            margin: 0;
            padding: 0;
            background-image: radial-gradient(circle at 10% 20%, hsla(242, 60%, 15%, 0.3) 0%, transparent 90%),
                              radial-gradient(circle at 90% 80%, hsla(350, 60%, 12%, 0.15) 0%, transparent 90%);
            min-height: 100vh;
        }

        .navbar {
            display: flex;
            justify-content: space-between;
            align-items: center;
            padding: 20px 40px;
            background: var(--glass-bg);
            backdrop-filter: blur(12px);
            border-bottom: 1px solid var(--border-color);
        }

        .navbar h1 {
            font-size: 20px;
            font-weight: 700;
            color: #ff3366;
            margin: 0;
        }

        .nav-links a {
            color: var(--muted-text);
            text-decoration: none;
            margin-left: 20px;
            font-weight: 600;
            font-size: 14px;
            transition: color 0.3s;
        }

        .nav-links a:hover, .nav-links a.active {
            color: #ff3366;
        }

        .container {
            max-width: 800px;
            margin: 60px auto;
            padding: 0 20px;
            box-sizing: border-box;
        }

        .scanner-card {
            background: var(--glass-bg);
            border: 1px solid var(--border-color);
            border-radius: 16px;
            padding: 40px;
            backdrop-filter: blur(16px);
            box-shadow: 0 20px 40px rgba(0, 0, 0, 0.4);
            text-align: center;
            margin-bottom: 30px;
        }

        .scan-title {
            font-size: 24px;
            font-weight: 700;
            margin-top: 0;
            background: linear-gradient(135deg, #fff 0%, #a5b4fc 100%);
            -webkit-background-clip: text;
            -webkit-text-fill-color: transparent;
        }

        .input-group {
            display: flex;
            gap: 15px;
            margin-top: 35px;
        }

        .scan-input {
            flex: 1;
            padding: 16px 20px;
            background: rgba(255, 255, 255, 0.05);
            border: 1px solid var(--border-color);
            border-radius: 8px;
            color: white;
            font-size: 16px;
            outline: none;
            box-sizing: border-box;
            transition: all 0.3s;
        }

        .scan-input:focus {
            border-color: var(--primary);
            box-shadow: 0 0 10px rgba(99, 102, 241, 0.3);
        }

        .btn-scan {
            background: linear-gradient(135deg, #ff3366 0%, #e11d48 100%);
            border: none;
            color: white;
            padding: 16px 30px;
            border-radius: 8px;
            font-size: 15px;
            font-weight: 700;
            cursor: pointer;
            box-shadow: 0 4px 15px rgba(255, 51, 102, 0.4);
            transition: opacity 0.3s;
            display: flex;
            align-items: center;
            gap: 8px;
        }

        .btn-scan:hover {
            opacity: 0.9;
        }

        /* glowing feedback card styles */
        .result-card {
            border-radius: 12px;
            padding: 30px;
            margin-top: 30px;
            animation: popIn 0.4s cubic-bezier(0.175, 0.885, 0.32, 1.275);
            text-align: left;
            position: relative;
        }

        @keyframes popIn {
            from { transform: scale(0.95); opacity: 0; }
            to { transform: scale(1); opacity: 1; }
        }

        .result-card.success {
            background: rgba(16, 185, 129, 0.08);
            border: 1px solid rgba(16, 185, 129, 0.4);
            box-shadow: 0 0 25px rgba(16, 185, 129, 0.25);
        }

        .result-card.error {
            background: rgba(239, 68, 68, 0.08);
            border: 1px solid rgba(239, 68, 68, 0.4);
            box-shadow: 0 0 25px rgba(239, 68, 68, 0.25);
        }

        .status-badge {
            display: inline-flex;
            align-items: center;
            gap: 8px;
            font-size: 15px;
            font-weight: 700;
            text-transform: uppercase;
            padding: 6px 16px;
            border-radius: 20px;
            margin-bottom: 20px;
        }

        .success .status-badge {
            background: rgba(16, 185, 129, 0.2);
            color: #34d399;
        }

        .error .status-badge {
            background: rgba(239, 68, 68, 0.2);
            color: #f87171;
        }

        .result-row {
            display: flex;
            justify-content: space-between;
            margin-bottom: 12px;
            font-size: 14px;
            border-bottom: 1px solid rgba(255, 255, 255, 0.04);
            padding-bottom: 8px;
        }

        .result-row span:first-child {
            color: var(--muted-text);
        }

        .result-row strong {
            color: #fff;
        }


    </style>
</head>
<body>

    <!-- Header Navigation -->
    <header class="navbar">
        <h1>RAPVIET CONSOLE</h1>
        <nav class="nav-links">
            <c:if test="${sessionScope.user.role == 'MANAGER' || sessionScope.user.role == 'ADMIN'}">
                <a href="ShowtimeManager">Suất Chiếu & Giá Vé</a>
            </c:if>
            <a href="CounterBooking">Quầy Bán Vé (POS)</a>
            <a href="TicketValidation" class="active">Soát Vé Cổng</a>
            <c:if test="${sessionScope.user.role == 'MANAGER' || sessionScope.user.role == 'ADMIN'}">
                <a href="DiscountManager" style="margin-left: 20px;">Mã Giảm Giá</a>
            </c:if>
            <a href="logout" style="margin-left: 20px; color: #ff3366; font-weight: bold;">Đăng Xuất</a>
        </nav>
    </header>

    <main class="container">
        
        <div class="scanner-card">
            <h2 class="scan-title">CỔNG SOÁT VÉ TỰ ĐỘNG</h2>
            <p style="color: var(--muted-text); margin: 5px 0 0 0;">Quét mã vạch e-ticket hoặc nhập Booking ID từ khách hàng</p>

            <form action="TicketValidation" method="POST">
                <input type="hidden" name="action" value="validate">
                
                <div class="input-group">
                    <input type="text" id="scanInputId" name="bookingId" class="scan-input" 
                           placeholder="Nhập Mã vé (Ví dụ: RV-WALK-1) hoặc quét QR..." required autofocus autocomplete="off">
                    
                    <button type="submit" class="btn-scan" id="btnSubmitScan">
                        <svg width="18" height="18" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round">
                            <line x1="4" y1="9" x2="20" y2="9"></line>
                            <line x1="4" y1="15" x2="20" y2="15"></line>
                            <line x1="10" y1="3" x2="10" y2="21"></line>
                            <line x1="14" y1="3" x2="14" y2="21"></line>
                        </svg>
                        Kiểm Tra Vé
                    </button>
                </div>
            </form>


        </div>

        <!-- HIỂN THỊ KẾT QUẢ QUÉT VÉ -->
        <c:if test="${not empty validationSuccess}">
            <div class="result-card success" id="resultCard">
                <div class="status-badge">
                    <svg width="18" height="18" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2.5">
                        <polyline points="20 6 9 17 4 12"></polyline>
                    </svg>
                    Vé Hợp Lệ - Cho Phép Vào Rạp
                </div>

                <div class="result-row">
                    <span>Mã vé:</span>
                    <strong>${booking.source == 'ONLINE' ? 'RV-ONLINE-' : 'RV-WALK-'}${booking.id}</strong>
                </div>
                <div class="result-row">
                    <span>Tên phim:</span>
                    <strong style="color: #fbbf24; font-size: 15px;">${showtime.movieTitle}</strong>
                </div>
                <div class="result-row">
                    <span>Phòng chiếu / Ghế:</span>
                    <strong>${showtime.hallName} / Ghế ${seatCodes}</strong>
                </div>
                <div class="result-row">
                    <span>Suất chiếu:</span>
                    <strong>${showtime.getFormattedStartTime()}</strong>
                </div>
                <div class="result-row" style="border-bottom: none; margin-bottom: 0; padding-bottom: 0;">
                    <span>Thời gian quét soát:</span>
                    <strong style="color: #34d399;" id="scanTimeDisplay">Mới quét xong lúc này</strong>
                </div>

                <!-- Nút in hóa đơn vé trực tiếp từ cổng soát vé -->
                <div style="margin-top: 20px; display: flex; justify-content: flex-end; border-top: 1px solid rgba(255, 255, 255, 0.08); padding-top: 15px;">
                    <a href="CounterBooking?action=printTicket&bookingId=${booking.id}" target="_blank" style="text-decoration: none; display: inline-flex; align-items: center; gap: 8px; background: linear-gradient(135deg, #10b981 0%, #059669 100%); color: white; padding: 10px 20px; border-radius: 8px; font-size: 13px; font-weight: 700; border: none; cursor: pointer; box-shadow: 0 4px 12px rgba(16, 185, 129, 0.3); transition: all 0.2s;">
                        <svg width="16" height="16" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2.5" stroke-linecap="round" stroke-linejoin="round">
                            <polyline points="6 9 6 2 18 2 18 9"></polyline>
                            <path d="M6 18H4a2 2 0 0 1-2-2v-5a2 2 0 0 1 2-2h16a2 2 0 0 1 2 2v5a2 2 0 0 1-2 2h-2"></path>
                            <rect x="6" y="14" width="12" height="8"></rect>
                        </svg>
                        In Hóa Đơn Vé (Print)
                    </a>
                </div>
            </div>
        </c:if>

        <c:if test="${not empty validationError}">
            <div class="result-card error" id="resultCard">
                <div class="status-badge">
                    <svg width="18" height="18" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2.5">
                        <line x1="18" y1="6" x2="6" y2="18"></line>
                        <line x1="6" y1="6" x2="18" y2="18"></line>
                    </svg>
                    Cảnh Báo - Từ Chối Vào Cổng!
                </div>
                <div style="font-size: 15px; font-weight: 600; color: #f87171; line-height: 1.5;">
                    ${validationError}
                </div>
            </div>
        </c:if>

    </main>

    <script>

        window.onload = function() {
            const timeDisp = document.getElementById('scanTimeDisplay');
            if (timeDisp) {
                const now = new Date();
                timeDisp.innerText = now.toLocaleTimeString('vi-VN') + " ngày " + now.toLocaleDateString('vi-VN');
            }

            // Tự động nhận diện thiết bị quét (scanner) hoặc dán mã vào để tự động submit xác thực vé
            const scanInput = document.getElementById('scanInputId');
            if (scanInput) {
                let scanTimeout;
                scanInput.addEventListener('input', function() {
                    clearTimeout(scanTimeout);
                    const val = this.value.trim();
                    
                    // 1. Nếu khớp định dạng vé tiêu chuẩn (RV-WALK-XXX, RV-ONLINE-XXX, RAPVIET-BOOKING-XXX hoặc TICKET-XXX)
                    if (/^(RV-WALK-|RV-ONLINE-|RAPVIET-BOOKING-|TICKET-)\d+$/i.test(val)) {
                        scanTimeout = setTimeout(() => {
                            document.getElementById('btnSubmitScan').click();
                        }, 100);
                    }
                    // 2. Nếu quét ra mã số trần (Ví dụ: chỉ có ID số "10024")
                    // Tự động submit sau 400ms ngừng nhập (để tránh gửi dở dang khi đang gõ tay)
                    else if (/^\d+$/.test(val) && val.length > 0) {
                        scanTimeout = setTimeout(() => {
                            document.getElementById('btnSubmitScan').click();
                        }, 450);
                    }
                });
            }
        };
    </script>
</body>
</html>
