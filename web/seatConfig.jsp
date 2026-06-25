<%@page contentType="text/html" pageEncoding="UTF-8"%>
<%@taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<!DOCTYPE html>
<html lang="vi">
<head>
    <meta charset="UTF-8">
    <meta name="viewport" content="width=device-width, initial-scale=1.0">
    <meta name="description" content="Quản lý cấu hình phòng chiếu và sơ đồ ghế ngồi">
    <title>RVS - Cấu Hình Sơ Đồ Ghế</title>
    <!-- Modern Google Font -->
    <link href="https://fonts.googleapis.com/css2?family=Outfit:wght@300;400;500;600;700;800&display=swap" rel="stylesheet">
    <style>
        :root {
            --bg-color: hsl(222, 47%, 6%);
            --glass-bg: hsla(222, 47%, 12%, 0.7);
            --border-color: hsla(217, 30%, 20%, 0.5);
            --primary: hsl(342, 100%, 60%); /* RapViet Pink */
            --accent: hsl(224, 89%, 60%);
            --text-color: hsl(0, 0%, 100%);
            --muted-text: hsl(215, 20%, 65%);
            --emerald: hsl(150, 84%, 37%);
            --royal-blue: hsl(224, 89%, 60%);
            --pink: hsl(330, 80%, 50%);
            --maintenance: hsl(215, 15%, 35%);
        }

        body {
            background-color: var(--bg-color);
            color: var(--text-color);
            font-family: 'Outfit', sans-serif;
            margin: 0;
            padding: 0;
            background-image: radial-gradient(circle at 10% 20%, hsla(342, 60%, 12%, 0.3) 0%, transparent 80%),
                              radial-gradient(circle at 90% 80%, hsla(224, 89%, 15%, 0.25) 0%, transparent 80%);
            min-height: 100vh;
            display: flex;
            flex-direction: column;
            box-sizing: border-box;
        }

        /* Navbar Styling */
        .navbar {
            display: flex;
            justify-content: space-between;
            align-items: center;
            padding: 20px 40px;
            background: rgba(11, 14, 20, 0.8);
            border-bottom: 1px solid var(--border-color);
            backdrop-filter: blur(12px);
        }

        .navbar h1 {
            font-size: 20px;
            font-weight: 800;
            margin: 0;
            background: linear-gradient(135deg, var(--primary) 0%, #a5b4fc 100%);
            -webkit-background-clip: text;
            -webkit-text-fill-color: transparent;
            letter-spacing: 1px;
        }

        .nav-links a {
            text-decoration: none;
            color: var(--muted-text);
            font-size: 14px;
            font-weight: 600;
            transition: color 0.2s;
            margin-left: 20px;
        }

        .nav-links a:hover, .nav-links a.active {
            color: white;
        }

        .nav-links a.active {
            border-bottom: 2px solid var(--primary);
            padding-bottom: 4px;
        }

        /* Container & Grid Layouts */
        .container {
            max-width: 1400px;
            margin: 40px auto;
            padding: 0 30px;
            width: 100%;
            box-sizing: border-box;
            flex: 1;
        }

        .header-section {
            display: flex;
            justify-content: space-between;
            align-items: center;
            margin-bottom: 25px;
        }

        .header-section h2 {
            font-size: 28px;
            font-weight: 700;
            margin: 0;
            background: linear-gradient(135deg, #fff 0%, #a5b4fc 100%);
            -webkit-background-clip: text;
            -webkit-text-fill-color: transparent;
        }

        /* Glassmorphic Alerts */
        .alert {
            padding: 15px 20px;
            border-radius: 8px;
            margin-bottom: 25px;
            backdrop-filter: blur(8px);
            font-weight: 600;
            animation: slideDown 0.4s ease-out;
            display: flex;
            align-items: center;
            gap: 10px;
        }
        .alert-success {
            background: rgba(16, 185, 129, 0.15);
            border: 1px solid rgba(16, 185, 129, 0.3);
            color: #34d399;
        }
        .alert-error {
            background: rgba(239, 68, 68, 0.15);
            border: 1px solid rgba(239, 68, 68, 0.3);
            color: #f87171;
        }

        @keyframes slideDown {
            from { transform: translateY(-10px); opacity: 0; }
            to { transform: translateY(0); opacity: 1; }
        }

        /* Layout Grid */
        .main-layout {
            display: grid;
            grid-template-columns: 280px 1fr 340px;
            gap: 30px;
            align-items: start;
        }

        .sidebar-panel {
            background: var(--glass-bg);
            border: 1px solid var(--border-color);
            border-radius: 16px;
            padding: 24px;
            backdrop-filter: blur(16px);
            box-shadow: 0 10px 30px rgba(0, 0, 0, 0.3);
        }

        .sidebar-panel h3 {
            font-size: 16px;
            font-weight: 700;
            margin-top: 0;
            margin-bottom: 15px;
            text-transform: uppercase;
            letter-spacing: 0.5px;
            color: #a5b4fc;
        }

        .legend-list {
            display: flex;
            flex-direction: column;
            gap: 10px;
        }

        .legend-item {
            display: flex;
            align-items: center;
            gap: 12px;
            padding: 10px 14px;
            border-radius: 8px;
            background: rgba(255, 255, 255, 0.02);
            border: 1px solid rgba(255, 255, 255, 0.05);
            font-size: 14px;
            font-weight: 600;
        }

        .legend-color {
            width: 20px;
            height: 20px;
            border-radius: 4px;
        }

        /* Interactive Seat Screen Area */
        .screen-area {
            background: var(--glass-bg);
            border: 1px solid var(--border-color);
            border-radius: 16px;
            padding: 30px;
            backdrop-filter: blur(16px);
            box-shadow: 0 10px 30px rgba(0, 0, 0, 0.3);
            display: flex;
            flex-direction: column;
            align-items: center;
        }

        .screen-bar {
            width: 80%;
            height: 8px;
            background: linear-gradient(90deg, transparent, #a5b4fc, transparent);
            border-radius: 4px;
            box-shadow: 0 4px 20px rgba(165, 180, 252, 0.5);
            margin-bottom: 15px;
        }

        .screen-text {
            font-size: 11px;
            font-weight: 700;
            letter-spacing: 2px;
            color: var(--muted-text);
            margin-bottom: 45px;
        }

        /* Grid Layout */
        .grid-layout {
            display: grid;
            gap: 8px;
            justify-content: center;
            overflow-x: auto;
            width: 100%;
            padding: 10px;
            box-sizing: border-box;
        }

        .seat-btn {
            width: 44px;
            height: 44px;
            border: none;
            border-radius: 8px;
            color: white;
            font-weight: 700;
            font-size: 12px;
            cursor: pointer;
            text-align: center;
            line-height: 44px;
            transition: all 0.2s;
            box-shadow: 0 2px 5px rgba(0,0,0,0.2);
            position: relative;
        }

        .seat-btn::after {
            content: '';
            position: absolute;
            top: 0;
            left: 0;
            right: 0;
            bottom: 0;
            border-radius: 8px;
            border: 2px solid transparent;
            transition: all 0.2s;
        }

        .seat-btn:hover {
            transform: scale(1.1);
            z-index: 10;
        }

        .seat-btn.selected::after {
            border-color: #fff;
            box-shadow: 0 0 10px rgba(255, 255, 255, 0.8);
        }

        .STANDARD {
            background: linear-gradient(135deg, #10b981 0%, #059669 100%);
        }
        .VIP {
            background: linear-gradient(135deg, #3b82f6 0%, #1d4ed8 100%);
        }
        .COUPLE {
            background: linear-gradient(135deg, #ec4899 0%, #be185d 100%);
        }
        .MAINTENANCE {
            background: #374151;
            color: #9ca3af;
            border: 1px dashed #4b5563;
        }

        /* Edit Form Panel */
        .form-panel {
            background: var(--glass-bg);
            border: 1px solid var(--border-color);
            border-radius: 16px;
            padding: 24px;
            backdrop-filter: blur(16px);
            box-shadow: 0 10px 30px rgba(0, 0, 0, 0.3);
        }

        .form-panel h3 {
            font-size: 16px;
            font-weight: 700;
            margin-top: 0;
            margin-bottom: 20px;
            text-transform: uppercase;
            letter-spacing: 0.5px;
            color: #a5b4fc;
        }

        .form-group {
            margin-bottom: 18px;
        }

        .form-group label {
            display: block;
            font-size: 12px;
            font-weight: 700;
            color: var(--muted-text);
            text-transform: uppercase;
            letter-spacing: 0.5px;
            margin-bottom: 8px;
        }

        .form-group input, .form-group select {
            width: 100%;
            padding: 12px;
            background: rgba(15, 23, 42, 0.6);
            border: 1px solid var(--border-color);
            border-radius: 8px;
            color: white;
            font-family: inherit;
            font-size: 14px;
            outline: none;
            box-sizing: border-box;
            transition: all 0.3s;
        }

        .form-group input:focus, .form-group select:focus {
            border-color: var(--primary);
            box-shadow: 0 0 8px rgba(255, 51, 102, 0.25);
        }

        .btn-group {
            display: flex;
            flex-direction: column;
            gap: 12px;
            margin-top: 25px;
        }

        .btn-row {
            display: flex;
            gap: 10px;
        }

        .btn-action {
            padding: 12px;
            font-weight: 700;
            font-size: 14px;
            border-radius: 8px;
            border: none;
            cursor: pointer;
            transition: all 0.2s;
            display: flex;
            align-items: center;
            justify-content: center;
            gap: 6px;
            color: white;
        }

        .btn-action:hover {
            opacity: 0.9;
            transform: translateY(-1px);
        }

        .btn-save {
            background: linear-gradient(135deg, #3b82f6 0%, #1d4ed8 100%);
            box-shadow: 0 4px 12px rgba(59, 130, 246, 0.3);
            flex: 1;
        }

        .btn-add {
            background: linear-gradient(135deg, #10b981 0%, #059669 100%);
            box-shadow: 0 4px 12px rgba(16, 185, 129, 0.3);
            flex: 1;
        }

        .btn-delete {
            background: linear-gradient(135deg, #ef4444 0%, #dc2626 100%);
            box-shadow: 0 4px 12px rgba(239, 68, 68, 0.3);
            width: 100%;
        }

        /* Footer */
        .footer-note {
            margin-top: 60px;
            padding: 30px 0;
            font-size: 13px;
            color: var(--muted-text);
            text-align: center;
            border-top: 1px solid var(--border-color);
            background: rgba(11, 14, 20, 0.4);
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
            <c:if test="${sessionScope.user.role == 'STAFF' || sessionScope.user.role == 'ADMIN'}">
                <a href="CounterBooking">Quầy Bán Vé (POS)</a>
                <a href="TicketValidation">Soát Vé Cổng</a>
            </c:if>
            <c:if test="${sessionScope.user.role == 'MANAGER' || sessionScope.user.role == 'ADMIN'}">
                <a href="DiscountManager">Mã Giảm Giá</a>
                <a href="SeatConfigController" class="active">Cấu Hinh Phòng</a>
            </c:if>
            <a href="logout" style="margin-left: 20px; color: #ff3366; font-weight: bold;">Đăng Xuất</a>
        </nav>
    </header>

    <div class="container">
        
        <!-- Alerts -->
        <c:if test="${not empty requestScope.msgSuccess || not empty sessionScope.msgSuccess}">
            <div class="alert alert-success" id="successAlert">
                <svg width="20" height="20" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2.5">
                    <polyline points="20 6 9 17 4 12"></polyline>
                </svg>
                ${not empty requestScope.msgSuccess ? requestScope.msgSuccess : sessionScope.msgSuccess}
                <% session.removeAttribute("msgSuccess"); %>
            </div>
        </c:if>
        <c:if test="${not empty requestScope.msgError || not empty sessionScope.msgError}">
            <div class="alert alert-error" id="errorAlert">
                <svg width="20" height="20" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2.5">
                    <line x1="18" y1="6" x2="6" y2="18"></line>
                    <line x1="6" y1="6" x2="18" y2="18"></line>
                </svg>
                ${not empty requestScope.msgError ? requestScope.msgError : sessionScope.msgError}
                <% session.removeAttribute("msgError"); %>
            </div>
        </c:if>

        <section class="header-section">
            <div>
                <h2>Thiết Lập Sơ Đồ Phòng Chiếu</h2>
                <p style="color: var(--muted-text); margin: 5px 0 0 0;">Cấu hình trạng thái vận hành, sửa loại ghế và quản lý sơ đồ ghế</p>
            </div>
        </section>

        <!-- Room selector row -->
        <div style="background: var(--glass-bg); border: 1px solid var(--border-color); padding: 18px 24px; border-radius: 12px; margin-bottom: 30px; display: flex; gap: 20px; flex-wrap: wrap; align-items: center; justify-content: space-between; backdrop-filter: blur(12px);">
            <div style="display: flex; gap: 15px; align-items: center; flex: 1;">
                <div style="display: flex; flex-direction: column; gap: 6px; min-width: 250px;">
                    <label style="font-size: 11px; font-weight: 700; color: var(--muted-text); text-transform: uppercase; letter-spacing: 0.5px;">Phòng Chiếu Cần Cấu Hình</label>
                    <select onchange="location.href='SeatConfigController?hallId=' + this.value" style="padding: 10px 12px; background: rgba(0,0,0,0.3); color: white; border: 1px solid var(--border-color); border-radius: 6px; outline: none; cursor: pointer; font-size: 13px; transition: border-color 0.2s;">
                        <c:forEach items="${hallList}" var="h">
                            <option value="${h.id}" ${h.id == currentHallId ? 'selected' : ''}>
                                ${h.name} (${h.totalSeats} ghế hoạt động)
                            </option>
                        </c:forEach>
                    </select>
                </div>
            </div>
            <div style="font-size: 13px; color: var(--muted-text); font-weight: 600; background: rgba(255,255,255,0.03); padding: 8px 16px; border-radius: 20px; border: 1px solid var(--border-color);">
                Mã Phòng: <span style="color: #34d399; font-weight: 700;">HALL-${currentHallId}</span>
            </div>
        </div>

        <div class="main-layout">
            
            <!-- PANEL 1: Legend & Info -->
            <section class="sidebar-panel">
                <h3>Loại Ghế & Ký Hiệu</h3>
                <div class="legend-list">
                    <div class="legend-item">
                        <div class="legend-color STANDARD"></div>
                        <span>Standard (Thường)</span>
                    </div>
                    <div class="legend-item">
                        <div class="legend-color VIP"></div>
                        <span>VIP (Đẹp)</span>
                    </div>
                    <div class="legend-item">
                        <div class="legend-color COUPLE"></div>
                        <span>Couple (Đôi)</span>
                    </div>
                    <div class="legend-item" style="border-style: dashed;">
                        <div class="legend-color MAINTENANCE"></div>
                        <span>Bảo trì / Hỏng</span>
                    </div>
                </div>

                <div style="margin-top: 25px; font-size: 12px; color: var(--muted-text); line-height: 1.5; background: rgba(255,255,255,0.01); border: 1px solid rgba(255,255,255,0.05); padding: 12px; border-radius: 8px;">
                    <strong style="color: white; display: block; margin-bottom: 5px;">Lưu ý cấu hình:</strong>
                    - Click trực tiếp vào ghế trên sơ đồ để tải dữ liệu chỉnh sửa nhanh.<br>
                    - Để thêm ghế mới, nhập mã code ghế (ví dụ: F9) chọn loại ghế và nhấn Thêm Ghế.<br>
                    - Nhấn Xóa để loại bỏ ghế vĩnh viễn khỏi phòng chiếu này.
                </div>
            </section>

            <!-- PANEL 2: Interactive Screen & Grid Map -->
            <section class="screen-area">
                <div class="screen-bar"></div>
                <div class="screen-text">MÀN HÌNH CHÍNH (SCREEN)</div>
                
                <div class="grid-layout" style="grid-template-columns: repeat(${maxSeatNumber}, 44px);">
                    <c:forEach items="${seatList}" var="s">
                        <button class="seat-btn ${s.maintenance ? 'MAINTENANCE' : s.seatType}" 
                                style="grid-column: ${s.seatNumber}; grid-row: ${s.getRowIndex()};"
                                id="seat-${s.getSeatCode()}"
                                onclick="selectSeat('${s.getSeatCode()}', '${s.seatType}', '${s.maintenance ? 'MAINTENANCE' : 'AVAILABLE'}')">
                            ${s.getSeatCode()}
                        </button>
                    </c:forEach>
                </div>
            </section>

            <!-- PANEL 3: Form Controller -->
            <section class="form-panel">
                <h3>Chỉnh Sửa Ghế</h3>
                <form action="SeatConfigController" method="POST" id="seatForm">
                    <input type="hidden" name="hallId" value="${currentHallId}">
                    
                    <div class="form-group">
                        <label for="formSeatCode">Mã Vị Trí Ghế</label>
                        <input type="text" id="formSeatCode" name="seatCode" placeholder="Ví dụ: A5, B12..." required autocomplete="off">
                    </div>

                    <div class="form-group">
                        <label for="formSeatType">Loại Ghế</label>
                        <select id="formSeatType" name="seatType">
                            <option value="STANDARD">STANDARD</option>
                            <option value="VIP">VIP</option>
                            <option value="COUPLE">COUPLE</option>
                        </select>
                    </div>

                    <div class="form-group">
                        <label for="formStatus">Trạng Thái Vận Hành</label>
                        <select id="formStatus" name="status">
                            <option value="AVAILABLE">Sử dụng bình thường</option>
                            <option value="MAINTENANCE">Bảo trì (Khóa chọn)</option>
                        </select>
                    </div>

                    <div class="btn-group">
                        <div class="btn-row">
                            <button type="submit" name="action" value="update" class="btn-action btn-save">
                                <svg width="16" height="16" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2.5">
                                    <path d="M19 21H5a2 2 0 0 1-2-2V5a2 2 0 0 1 2-2h11l5 5v11a2 2 0 0 1-2 2z"></path>
                                    <polyline points="17 21 17 13 7 13 7 21"></polyline>
                                    <polyline points="7 3 7 8 15 8"></polyline>
                                </svg>
                                Cập Nhật
                            </button>
                            <button type="submit" name="action" value="add" class="btn-action btn-add">
                                <svg width="16" height="16" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2.5">
                                    <line x1="12" y1="5" x2="12" y2="19"></line>
                                    <line x1="5" y1="12" x2="19" y2="12"></line>
                                </svg>
                                Thêm Mới
                            </button>
                        </div>
                        
                        <button type="submit" name="action" value="delete" class="btn-action btn-delete" onclick="return confirm('Bạn có chắc chắn muốn xóa ghế này khỏi sơ đồ phòng chiếu vĩnh viễn không?')">
                            <svg width="16" height="16" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2.5">
                                <polyline points="3 6 5 6 21 6"></polyline>
                                <path d="M19 6v14a2 2 0 0 1-2 2H7a2 2 0 0 1-2-2V6m3 0V4a2 2 0 0 1 2-2h4a2 2 0 0 1 2 2v2"></path>
                            </svg>
                            Xóa Ghế Khỏi Sơ Đồ
                        </button>
                    </div>
                </form>
            </section>
        </div>
    </div>

    <!-- Footer -->
    <footer class="footer-note">
        <p>Hệ Thống Đặt Vé Phim Chi Nhánh RapViet Cineplex &copy; 2026 - Nhóm 6 Project SWP391</p>
    </footer>

    <script>
        function selectSeat(code, type, status) {
            // Remove previous selections
            document.querySelectorAll('.seat-btn').forEach(btn => btn.classList.remove('selected'));
            
            // Highlight selected seat
            const selectedBtn = document.getElementById('seat-' + code);
            if (selectedBtn) {
                selectedBtn.classList.add('selected');
            }

            // Fill form fields
            document.getElementById('formSeatCode').value = code;
            document.getElementById('formSeatType').value = type;
            document.getElementById('formStatus').value = status;
        }

        // Auto hide notifications after 5 seconds
        setTimeout(() => {
            const success = document.getElementById('successAlert');
            const error = document.getElementById('errorAlert');
            if (success) {
                success.style.opacity = '0';
                success.style.transition = 'opacity 0.6s ease';
                setTimeout(() => success.remove(), 600);
            }
            if (error) {
                error.style.opacity = '0';
                error.style.transition = 'opacity 0.6s ease';
                setTimeout(() => error.remove(), 600);
            }
        }, 5000);
    </script>
</body>
</html>