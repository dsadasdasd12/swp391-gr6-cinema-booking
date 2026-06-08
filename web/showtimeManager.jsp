<%@page contentType="text/html" pageEncoding="UTF-8"%>
<%@taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<%@taglib prefix="fmt" uri="http://java.sun.com/jsp/jstl/fmt" %>
<!DOCTYPE html>
<html lang="vi">
<head>
    <meta charset="UTF-8">
    <meta name="viewport" content="width=device-width, initial-scale=1.0">
    <meta name="description" content="Hệ thống điều hành suất chiếu và doanh thu RVS">
    <title>RVS - Quản Lý Suất Chiếu & Giá Vé</title>
    
    <!-- Modern Google Font -->
    <link href="https://fonts.googleapis.com/css2?family=Inter:wght@300;400;600;700&display=swap" rel="stylesheet">
    
    <style>
        :root {
            --bg-color: hsl(222, 47%, 6%);
            --glass-bg: hsla(222, 47%, 12%, 0.7);
            --border-color: hsla(217, 30%, 20%, 0.5);
            --primary: hsl(224, 89%, 60%);
            --emerald: hsl(150, 84%, 37%);
            --text-color: hsl(0, 0%, 100%);
            --muted-text: hsl(215, 20%, 65%);
        }

        body {
            background-color: var(--bg-color);
            color: var(--text-color);
            font-family: 'Inter', sans-serif;
            margin: 0;
            padding: 0;
            background-image: radial-gradient(circle at 10% 20%, hsla(242, 60%, 15%, 0.4) 0%, transparent 90%),
                              radial-gradient(circle at 90% 80%, hsla(224, 89%, 20%, 0.3) 0%, transparent 90%);
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
            letter-spacing: 1px;
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
            max-width: 1200px;
            margin: 40px auto;
            padding: 0 20px;
        }

        .header-section {
            display: flex;
            justify-content: space-between;
            align-items: center;
            margin-bottom: 30px;
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
            margin-bottom: 20px;
            backdrop-filter: blur(8px);
            font-weight: 600;
            animation: slideDown 0.4s ease-out;
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

        /* Grid of Showtimes */
        .showtimes-grid {
            display: grid;
            grid-template-columns: repeat(auto-fill, minmax(360px, 1fr));
            gap: 25px;
        }

        .showtime-card {
            background: var(--glass-bg);
            border: 1px solid var(--border-color);
            border-radius: 12px;
            padding: 24px;
            backdrop-filter: blur(16px);
            transition: transform 0.3s, border-color 0.3s;
            display: flex;
            flex-direction: column;
            justify-content: space-between;
            position: relative;
            overflow: hidden;
        }

        .showtime-card::before {
            content: '';
            position: absolute;
            top: 0;
            left: 0;
            width: 100%;
            height: 4px;
            background: linear-gradient(90deg, #ff3366, var(--primary));
        }

        .showtime-card:hover {
            transform: translateY(-5px);
            border-color: rgba(99, 102, 241, 0.5);
            box-shadow: 0 10px 20px rgba(0, 0, 0, 0.3);
        }

        .movie-title {
            font-size: 18px;
            font-weight: 700;
            margin: 0 0 10px 0;
            color: #fff;
        }

        .showtime-info {
            display: flex;
            justify-content: space-between;
            align-items: center;
            margin-bottom: 20px;
        }

        .time-badge {
            background: rgba(255, 255, 255, 0.08);
            padding: 6px 12px;
            border-radius: 6px;
            font-size: 13px;
            font-weight: 600;
            color: #a5b4fc;
        }

        .hall-badge {
            background: var(--primary);
            color: white;
            padding: 4px 10px;
            border-radius: 4px;
            font-size: 12px;
            font-weight: bold;
        }

        /* Occupancy Ring Chart SVG */
        .occupancy-box {
            display: flex;
            align-items: center;
            gap: 15px;
            background: rgba(255, 255, 255, 0.03);
            padding: 12px;
            border-radius: 8px;
            border: 1px solid rgba(255, 255, 255, 0.05);
            margin-bottom: 20px;
        }

        .ring-container {
            position: relative;
            width: 60px;
            height: 60px;
        }

        .ring-svg {
            transform: rotate(-90deg);
        }

        .ring-bg {
            fill: none;
            stroke: rgba(255, 255, 255, 0.08);
            stroke-width: 6;
        }

        .ring-fill {
            fill: none;
            stroke: var(--emerald);
            stroke-width: 6;
            stroke-linecap: round;
            transition: stroke-dashoffset 0.6s ease;
        }

        .ring-text {
            position: absolute;
            top: 50%;
            left: 50%;
            transform: translate(-50%, -50%);
            font-size: 12px;
            font-weight: 700;
            color: #34d399;
        }

        .pricing-section {
            border-top: 1px solid var(--border-color);
            padding-top: 15px;
            display: flex;
            justify-content: space-between;
            align-items: center;
        }

        .btn-pricing {
            background: rgba(99, 102, 241, 0.15);
            border: 1px solid var(--primary);
            color: #a5b4fc;
            padding: 8px 16px;
            border-radius: 6px;
            font-size: 13px;
            font-weight: 600;
            cursor: pointer;
            transition: all 0.3s;
        }

        .btn-pricing:hover {
            background: var(--primary);
            color: #fff;
            box-shadow: 0 0 10px rgba(99, 102, 241, 0.4);
        }

        /* Modal Popup styles */
        .modal {
            display: none;
            position: fixed;
            z-index: 100;
            top: 0;
            left: 0;
            width: 100%;
            height: 100%;
            background: rgba(3, 7, 18, 0.85);
            backdrop-filter: blur(12px);
            align-items: center;
            justify-content: center;
        }

        .modal-content {
            background: hsla(222, 47%, 10%, 0.95);
            border: 1px solid var(--border-color);
            border-radius: 16px;
            width: 90%;
            max-width: 460px;
            padding: 30px;
            box-shadow: 0 20px 40px rgba(0, 0, 0, 0.5);
            animation: modalFadeIn 0.3s ease-out;
        }

        @keyframes modalFadeIn {
            from { transform: scale(0.9); opacity: 0; }
            to { transform: scale(1); opacity: 1; }
        }

        .modal-header {
            display: flex;
            justify-content: space-between;
            align-items: center;
            margin-bottom: 20px;
        }

        .modal-header h3 {
            margin: 0;
            font-size: 20px;
            font-weight: 700;
        }

        .close-btn {
            background: none;
            border: none;
            color: var(--muted-text);
            font-size: 24px;
            cursor: pointer;
            transition: color 0.3s;
        }

        .close-btn:hover {
            color: #fff;
        }

        .form-group {
            margin-bottom: 20px;
        }

        .form-group label {
            display: block;
            font-size: 13px;
            font-weight: 600;
            color: var(--muted-text);
            margin-bottom: 8px;
        }

        .form-group input, .form-group select {
            width: 100%;
            padding: 12px;
            background: rgba(255, 255, 255, 0.05);
            border: 1px solid var(--border-color);
            border-radius: 8px;
            color: #fff;
            font-size: 14px;
            box-sizing: border-box;
            outline: none;
            transition: border-color 0.3s;
        }

        .form-group input:focus, .form-group select:focus {
            border-color: var(--primary);
        }

        .btn-submit {
            width: 100%;
            background: linear-gradient(135deg, #ff3366 0%, #e11d48 100%);
            border: none;
            color: white;
            padding: 14px;
            border-radius: 8px;
            font-size: 15px;
            font-weight: bold;
            cursor: pointer;
            box-shadow: 0 4px 15px rgba(255, 51, 102, 0.4);
            transition: opacity 0.3s;
        }

        .btn-submit:hover {
            opacity: 0.9;
        }
    </style>
</head>
<body>

    <!-- Header Navigation -->
    <header class="navbar">
        <h1 id="logo">RAPVIET CONSOLE</h1>
        <nav class="nav-links">
            <a href="ShowtimeManager" class="active">Suất Chiếu & Giá Vé</a>
            <a href="CounterBooking">Quầy Bán Vé (POS)</a>
            <a href="TicketValidation">Soát Vé Cổng</a>
            <a href="DiscountManager" style="margin-left: 20px;">Mã Giảm Giá</a>
        </nav>
    </header>

    <main class="container">
        
        <!-- Alerts -->
        <c:if test="${not empty sessionScope.msgSuccess}">
            <div class="alert alert-success" id="successAlert">
                ${sessionScope.msgSuccess}
                <% session.removeAttribute("msgSuccess"); %>
            </div>
        </c:if>
        <c:if test="${not empty sessionScope.msgError}">
            <div class="alert alert-error" id="errorAlert">
                ${sessionScope.msgError}
                <% session.removeAttribute("msgError"); %>
            </div>
        </c:if>

        <section class="header-section">
            <div>
                <h2>Giám Sát Suất Chiếu Hoạt Động</h2>
                <p style="color: var(--muted-text); margin: 5px 0 0 0;">Thiết lập giá vé và theo dõi tiến độ lấp đầy rạp</p>
            </div>
        </section>

        <!-- Sleek Filter Bar -->
        <div style="background: var(--glass-bg); border: 1px solid var(--border-color); padding: 18px 24px; border-radius: 12px; margin-bottom: 30px; display: flex; gap: 20px; flex-wrap: wrap; align-items: center; justify-content: space-between; backdrop-filter: blur(12px);">
            <div style="display: flex; gap: 15px; flex-wrap: wrap; flex: 1;">
                <!-- Lọc theo Phim -->
                <div style="display: flex; flex-direction: column; gap: 6px; min-width: 220px;">
                    <label style="font-size: 11px; font-weight: 700; color: var(--muted-text); text-transform: uppercase; letter-spacing: 0.5px;">Phim</label>
                    <select id="filterMovie" onchange="applyFilters()" style="padding: 10px 12px; background: rgba(0,0,0,0.3); color: white; border: 1px solid var(--border-color); border-radius: 6px; outline: none; cursor: pointer; font-size: 13px; transition: border-color 0.2s;">
                        <option value="ALL">-- Tất cả phim --</option>
                    </select>
                </div>
                <!-- Chọn Ngày Chiếu (Server-side) -->
                <div style="display: flex; flex-direction: column; gap: 6px; min-width: 160px;">
                    <label style="font-size: 11px; font-weight: 700; color: var(--muted-text); text-transform: uppercase; letter-spacing: 0.5px;">Chọn Ngày Chiếu</label>
                    <input type="date" id="selectedDate" value="${selectedDate}" onchange="window.location.href='ShowtimeManager?branchId=${currentBranchId}&date=' + this.value" style="padding: 10px 12px; background: rgba(0,0,0,0.3); color: white; border: 1px solid var(--border-color); border-radius: 6px; outline: none; cursor: pointer; font-size: 13px; transition: border-color 0.2s; color-scheme: dark;">
                </div>
                <!-- Lọc theo Phòng Chiếu -->
                <div style="display: flex; flex-direction: column; gap: 6px; min-width: 160px;">
                    <label style="font-size: 11px; font-weight: 700; color: var(--muted-text); text-transform: uppercase; letter-spacing: 0.5px;">Phòng Chiếu</label>
                    <select id="filterHall" onchange="applyFilters()" style="padding: 10px 12px; background: rgba(0,0,0,0.3); color: white; border: 1px solid var(--border-color); border-radius: 6px; outline: none; cursor: pointer; font-size: 13px; transition: border-color 0.2s;">
                        <option value="ALL">-- Tất cả phòng --</option>
                    </select>
                </div>
            </div>
            <!-- Bộ đếm số lượng suất chiếu hiển thị -->
            <div style="font-size: 13px; color: var(--muted-text); font-weight: 600; background: rgba(255,255,255,0.03); padding: 8px 16px; border-radius: 20px; border: 1px solid var(--border-color);" id="filterCounter">
                Hiển thị: <span id="visibleCount" style="color: #34d399; font-weight: 700;">0</span>/<span id="totalCount" style="color: var(--muted-text);">0</span> suất chiếu
            </div>
        </div>

        <!-- Active Showtimes Grid -->
        <section class="showtimes-grid">
            <c:forEach items="${showtimeList}" var="st">
                <c:set var="occupancy" value="${showtimeService.getOccupancyRate(st.id)}" />
                <c:set var="booked" value="${showtimeService.getBookedSeatsCount(st.id)}" />
                <c:set var="total" value="${showtimeService.getTotalSeatsInHall(st.id)}" />
                
                <div class="showtime-card" data-movie="${st.movieTitle}" data-date="${st.getFormattedStartTime().substring(0, 10)}" data-hall="${st.hallName}">
                    <div>
                        <h3 class="movie-title">${st.movieTitle}</h3>
                        <div class="showtime-info">
                            <span class="time-badge">${st.getFormattedStartTime()} - ${st.getFormattedEndTime()}</span>
                            <span class="hall-badge">${st.hallName}</span>
                        </div>
                        
                        <!-- Occupancy SVG Circle Progress -->
                        <div class="occupancy-box">
                            <div class="ring-container">
                                <svg class="ring-svg" width="60" height="60">
                                    <circle class="ring-bg" cx="30" cy="30" r="25"></circle>
                                    <circle class="ring-fill" cx="30" cy="30" r="25" 
                                            stroke-dasharray="157" 
                                            stroke-dashoffset="${157 - (157 * occupancy / 100)}">
                                    </circle>
                                </svg>
                                <span class="ring-text">${occupancy}%</span>
                            </div>
                            <div>
                                <div style="font-size: 13px; font-weight: 700; color: #fff;">Lấp đầy phòng</div>
                                <div style="font-size: 12px; color: var(--muted-text); margin-top: 2px;">
                                    Đã mua: <b>${booked}</b> / ${total} ghế
                                </div>
                            </div>
                        </div>
                        <!-- Custom configured prices per seat type -->
                        <c:set var="stdPrice" value="${showtimeService.getSeatPrice(st.id, 'STANDARD', st.basePrice)}" />
                        <c:set var="vipPrice" value="${showtimeService.getSeatPrice(st.id, 'VIP', st.basePrice)}" />
                        <c:set var="cplPrice" value="${showtimeService.getSeatPrice(st.id, 'COUPLE', st.basePrice)}" />
                        
                        <div style="background: rgba(255,255,255,0.03); padding: 10px 14px; border-radius: 8px; border: 1px solid rgba(255,255,255,0.05); margin-top: 15px; font-size: 12px; display: flex; flex-direction: column; gap: 4px;">
                            <div style="font-weight: 600; color: var(--muted-text); margin-bottom: 2px;">Cấu hình giá theo ghế:</div>
                            <div style="display: flex; justify-content: space-between;">
                                <span>STANDARD (Thường):</span>
                                <span style="font-weight: 700; color: ${stdPrice != st.basePrice ? '#34d399' : '#fff'};">
                                    <fmt:formatNumber value="${stdPrice}" pattern="#,##0"/> đ
                                </span>
                            </div>
                            <div style="display: flex; justify-content: space-between;">
                                <span>VIP (Đặc biệt):</span>
                                <span style="font-weight: 700; color: ${vipPrice != st.basePrice ? '#34d399' : '#fff'};">
                                    <fmt:formatNumber value="${vipPrice}" pattern="#,##0"/> đ
                                </span>
                            </div>
                            <div style="display: flex; justify-content: space-between;">
                                <span>COUPLE (Ghế đôi):</span>
                                <span style="font-weight: 700; color: ${cplPrice != st.basePrice ? '#34d399' : '#fff'};">
                                    <fmt:formatNumber value="${cplPrice}" pattern="#,##0"/> đ
                                </span>
                            </div>
                        </div>
                    </div>
                    
                    <div class="pricing-section">
                        <div>
                            <span style="font-size: 11px; color: var(--muted-text); display: block;">Giá vé gốc</span>
                            <span style="font-size: 16px; font-weight: 700; color: #fbbf24;"><fmt:formatNumber value="${st.basePrice}" pattern="#,##0"/> đ</span>
                        </div>
                        <button class="btn-pricing" id="pricingBtn-${st.id}" 
                                data-price-standard="${stdPrice}"
                                data-price-vip="${vipPrice}"
                                data-price-couple="${cplPrice}"
                                onclick="openPricingModal(${st.id}, '${st.movieTitle} - ${st.hallName}', ${st.basePrice}, this)">
                            Cấu Hình Giá
                        </button>
                    </div>
                </div>
            </c:forEach>
        </section>
    </main>

    <!-- Modal Cấu Hình Giá Vé -->
    <div class="modal" id="pricingModal">
        <div class="modal-content">
            <div class="modal-header">
                <h3 id="modalTitle">Thiết Lập Giá Vé Phụ Phí</h3>
                <button class="close-btn" onclick="closePricingModal()">&times;</button>
            </div>
            
            <form action="ShowtimeManager" method="POST">
                <input type="hidden" id="formShowtimeId" name="showtimeId" value="">
                <input type="hidden" name="action" value="setPricing">
                <input type="hidden" name="branchId" value="${currentBranchId}">
                <input type="hidden" name="date" value="${selectedDate}">
                
                <div class="form-group">
                    <label for="formSeatType">Loại Ghế Áp Dụng</label>
                    <select id="formSeatType" name="seatType">
                        <option value="STANDARD">STANDARD (Thường)</option>
                        <option value="VIP">VIP (Đặc biệt)</option>
                        <option value="COUPLE">COUPLE (Ghế Đôi)</option>
                    </select>
                </div>
                
                <div class="form-group">
                    <label for="formPrice">Giá Vé Mới (VNĐ)</label>
                    <input type="number" id="formPrice" name="price" required min="0" placeholder="Ví dụ: 95000">
                    <span style="font-size: 11px; color: var(--muted-text); display: block; margin-top: 5px;" id="basePriceHint">
                        Giá vé gốc của suất chiếu: 0đ
                    </span>
                </div>
                
                <button type="submit" class="btn-submit">Lưu Cấu Hình</button>
            </form>
        </div>
    </div>

    <script>
        let currentPricingData = {};

        function openPricingModal(showtimeId, showtimeLabel, basePrice, btn) {
            document.getElementById('formShowtimeId').value = showtimeId;
            document.getElementById('modalTitle').innerText = "Cấu Hình: " + showtimeLabel;
            document.getElementById('basePriceHint').innerText = "Giá vé gốc của suất chiếu: " + basePrice.toLocaleString('vi-VN') + " đ";
            
            // Store configured prices from the button's data attributes
            currentPricingData = {
                STANDARD: parseFloat(btn.getAttribute('data-price-standard')) || basePrice,
                VIP: parseFloat(btn.getAttribute('data-price-vip')) || basePrice,
                COUPLE: parseFloat(btn.getAttribute('data-price-couple')) || basePrice
            };
            
            // Update input value based on currently selected seat type
            updateModalPriceInput();
            
            const modal = document.getElementById('pricingModal');
            modal.style.display = 'flex';
        }

        function updateModalPriceInput() {
            const seatType = document.getElementById('formSeatType').value;
            const priceInput = document.getElementById('formPrice');
            priceInput.value = Math.round(currentPricingData[seatType] || 0);
        }

        // Auto-update input when dropdown option is changed
        document.getElementById('formSeatType').addEventListener('change', updateModalPriceInput);

        function closePricingModal() {
            const modal = document.getElementById('pricingModal');
            modal.style.display = 'none';
        }

        // Hide alert message after 5 seconds
        setTimeout(() => {
            const sAlert = document.getElementById('successAlert');
            const eAlert = document.getElementById('errorAlert');
            if(sAlert) sAlert.style.display = 'none';
            if(eAlert) eAlert.style.display = 'none';
        }, 5000);

        // INITIALIZE AND APPLY DYNAMIC CLIENT-SIDE FILTERS
        let cards = [];
        window.addEventListener('DOMContentLoaded', () => {
            cards = Array.from(document.querySelectorAll('.showtime-card'));
            
            const movieSelect = document.getElementById('filterMovie');
            const hallSelect = document.getElementById('filterHall');
            
            const movies = new Set();
            const halls = new Set();
            
            cards.forEach(card => {
                if (card.dataset.movie) movies.add(card.dataset.movie);
                if (card.dataset.hall) halls.add(card.dataset.hall);
            });
            
            // Sắp xếp các danh sách
            Array.from(movies).sort().forEach(m => {
                const opt = document.createElement('option');
                opt.value = m;
                opt.textContent = m;
                movieSelect.appendChild(opt);
            });

            Array.from(halls).sort().forEach(h => {
                const opt = document.createElement('option');
                opt.value = h;
                opt.textContent = h;
                hallSelect.appendChild(opt);
            });
            
            document.getElementById('totalCount').textContent = cards.length;
            applyFilters();
        });

        function applyFilters() {
            const movieVal = document.getElementById('filterMovie').value;
            const hallVal = document.getElementById('filterHall').value;
            
            let visible = 0;
            
            cards.forEach(card => {
                const mMatch = (movieVal === 'ALL' || card.dataset.movie === movieVal);
                const hMatch = (hallVal === 'ALL' || card.dataset.hall === hallVal);
                
                if (mMatch && hMatch) {
                    card.style.display = 'flex';
                    visible++;
                } else {
                    card.style.display = 'none';
                }
            });
            
            document.getElementById('visibleCount').textContent = visible;
        }
    </script>
</body>
</html>
