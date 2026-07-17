<%@page contentType="text/html" pageEncoding="UTF-8"%>
<%@taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<!DOCTYPE html>
<html lang="vi">
    <head>
        <meta charset="UTF-8">
        <meta name="viewport" content="width=device-width, initial-scale=1.0">
        <meta name="description" content="Quầy bán vé POS chuyên nghiệp cho RapViet Cineplex">
        <title>RVS - Quầy Vé Trực Tiếp POS</title>

        <!-- Modern Google Font -->
        <link href="https://fonts.googleapis.com/css2?family=Inter:wght@300;400;600;700&display=swap" rel="stylesheet">

        <style>
            :root {
                --bg-color: hsl(222, 47%, 6%);
                --glass-bg: hsla(222, 47%, 12%, 0.75);
                --border-color: hsla(217, 30%, 20%, 0.6);
                --primary: hsl(224, 89%, 60%);
                --emerald: hsl(150, 84%, 37%);
                --pink: hsl(330, 80%, 50%);
                --royal-blue: hsl(220, 80%, 50%);
                --text-color: hsl(0, 0%, 100%);
                --muted-text: hsl(215, 20%, 65%);
            }

            body {
                background-color: var(--bg-color);
                color: var(--text-color);
                font-family: 'Inter', sans-serif;
                margin: 0;
                padding: 0;
                background-image: radial-gradient(circle at 10% 10%, hsla(242, 60%, 12%, 0.4) 0%, transparent 80%),
                    radial-gradient(circle at 90% 90%, hsla(224, 89%, 15%, 0.3) 0%, transparent 80%);
                min-height: 100vh;
                overflow-x: hidden;
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

            .pos-grid {
                display: grid;
                grid-template-columns: 320px 1fr 350px;
                height: calc(100vh - 81px);
                overflow: hidden;
            }

            /* Generic Panels */
            .pos-panel {
                border-right: 1px solid var(--border-color);
                padding: 24px;
                overflow-y: auto;
                box-sizing: border-box;
                background: rgba(3, 7, 18, 0.2);
            }

            .pos-panel:last-child {
                border-right: none;
                background: hsla(222, 47%, 8%, 0.9);
            }

            .panel-title {
                font-size: 18px;
                font-weight: 700;
                margin: 0 0 20px 0;
                background: linear-gradient(135deg, #fff 0%, #a5b4fc 100%);
                -webkit-background-clip: text;
                -webkit-text-fill-color: transparent;
                display: flex;
                justify-content: space-between;
                align-items: center;
            }

            /* Showtime List Card */
            .showtime-item {
                background: var(--glass-bg);
                border: 1px solid var(--border-color);
                border-radius: 8px;
                padding: 15px;
                margin-bottom: 12px;
                cursor: pointer;
                transition: all 0.3s;
            }

            .showtime-item:hover, .showtime-item.active {
                border-color: var(--primary);
                background: rgba(99, 102, 241, 0.15);
                box-shadow: 0 4px 12px rgba(99, 102, 241, 0.2);
            }

            .showtime-item.active::before {
                content: '';
                position: absolute;
                left: 24px;
                width: 4px;
                height: 40px;
                background: #ff3366;
                border-radius: 2px;
            }

            .st-title {
                font-size: 14px;
                font-weight: 700;
                color: #fff;
                margin: 0 0 6px 0;
            }

            .st-meta {
                display: flex;
                justify-content: space-between;
                font-size: 12px;
                color: var(--muted-text);
            }

            /* Center Panel - Seat Selection */
            .seat-map-container {
                display: flex;
                flex-direction: column;
                align-items: center;
                justify-content: center;
                min-height: 80%;
            }

            .screen {
                width: 80%;
                height: 12px;
                background: linear-gradient(180deg, #374151 0%, #1f2937 100%);
                box-shadow: 0 10px 20px rgba(99, 102, 241, 0.3);
                border-radius: 0 0 20px 20px;
                margin-bottom: 50px;
                text-align: center;
                font-size: 10px;
                color: var(--muted-text);
                line-height: 12px;
            }

            .seat-grid {
                display: grid;
                grid-template-columns: repeat(8, 48px);
                gap: 12px;
                justify-content: center;
            }

            .seat {
                width: 48px;
                height: 48px;
                border: 1px solid rgba(255, 255, 255, 0.05);
                border-radius: 6px;
                color: #fff;
                font-size: 12px;
                font-weight: 700;
                display: flex;
                align-items: center;
                justify-content: center;
                cursor: pointer;
                transition: all 0.2s;
                outline: none;
            }

            .seat.STANDARD {
                background-color: var(--emerald);
            }
            .seat.VIP {
                background-color: var(--royal-blue);
            }
            .seat.COUPLE {
                background-color: var(--pink);
            }
            <c:forEach items="${allSeatTypes}" var="st">
                .seat.${st.code}:not(.SELECTED):not(.OCCUPIED):not(.MAINTENANCE) {
                    background-color: ${st.color} !important;
                }
            </c:forEach>
            .seat.MAINTENANCE {
                background-color: #4b5563 !important;
                border-color: rgba(255, 255, 255, 0.05);
                color: #9ca3af;
                cursor: not-allowed;
            }

            .seat.SELECTED {
                background-color: #f59e0b !important;
                border-color: #fff;
                transform: scale(1.05);
                box-shadow: 0 0 12px #f59e0b;
                animation: pulse 1.5s infinite;
            }

            @keyframes pulse {
                0% {
                    transform: scale(1.05);
                }
                50% {
                    transform: scale(0.98);
                }
                100% {
                    transform: scale(1.05);
                }
            }

            .seat.OCCUPIED {
                background-color: #374151 !important;
                border-color: rgba(255, 255, 255, 0.05);
                color: #6b7280;
                cursor: not-allowed;
                position: relative;
            }

            .seat.OCCUPIED::after {
                content: 'x';
                font-size: 14px;
                position: absolute;
                color: #ef4444;
            }

            /* Right Panel - Cart Summary */
            .cart-item-row {
                display: flex;
                justify-content: space-between;
                align-items: center;
                padding: 10px 0;
                border-bottom: 1px solid rgba(255, 255, 255, 0.05);
            }

            .form-input {
                width: 100%;
                padding: 12px;
                background: rgba(255, 255, 255, 0.04);
                border: 1px solid var(--border-color);
                border-radius: 8px;
                color: white;
                box-sizing: border-box;
                outline: none;
                font-size: 14px;
                margin-bottom: 15px;
                transition: border-color 0.3s;
            }

            .form-input:focus {
                border-color: var(--primary);
            }

            .total-box {
                background: rgba(255, 255, 255, 0.03);
                border: 1px solid var(--border-color);
                border-radius: 8px;
                padding: 20px;
                margin-top: 30px;
            }

            .total-row {
                display: flex;
                justify-content: space-between;
                align-items: center;
                margin-bottom: 10px;
            }

            .total-row:last-child {
                margin-bottom: 0;
                padding-top: 10px;
                border-top: 1px solid rgba(255, 255, 255, 0.08);
            }

            .btn-confirm {
                width: 100%;
                background: linear-gradient(135deg, #10b981 0%, #059669 100%);
                border: none;
                color: white;
                padding: 16px;
                border-radius: 8px;
                font-size: 16px;
                font-weight: 700;
                cursor: pointer;
                box-shadow: 0 4px 15px rgba(16, 185, 129, 0.3);
                transition: opacity 0.3s;
                margin-top: 20px;
            }

            .btn-confirm:hover {
                opacity: 0.9;
            }

            /* Seat legend style */
            .legend {
                display: flex;
                gap: 20px;
                margin-top: 30px;
            }

            .legend-item {
                display: flex;
                align-items: center;
                gap: 8px;
                font-size: 12px;
                color: var(--muted-text);
            }

            .legend-color {
                width: 16px;
                height: 16px;
                border-radius: 4px;
            }

            /* Payment & Success Modals */
            .modal {
                display: none;
                position: fixed;
                z-index: 1000;
                top: 0;
                left: 0;
                width: 100%;
                height: 100%;
                background: rgba(3, 7, 18, 0.85);
                backdrop-filter: blur(8px);
                align-items: center;
                justify-content: center;
            }

            .modal-content {
                background: hsla(222, 47%, 10%, 0.95);
                border: 1px solid var(--border-color);
                border-radius: 16px;
                width: 90%;
                max-width: 450px;
                padding: 30px;
                box-shadow: 0 20px 40px rgba(0, 0, 0, 0.5);
                text-align: center;
                box-sizing: border-box;
                animation: modalFadeIn 0.3s ease-out;
            }

            @keyframes modalFadeIn {
                from {
                    transform: scale(0.95);
                    opacity: 0;
                }
                to {
                    transform: scale(1);
                    opacity: 1;
                }
            }

            .modal-title {
                font-size: 20px;
                font-weight: 700;
                margin-top: 0;
                margin-bottom: 15px;
                background: linear-gradient(135deg, #fff 0%, #a5b4fc 100%);
                -webkit-background-clip: text;
                -webkit-text-fill-color: transparent;
            }

            .modal-body {
                margin-bottom: 25px;
                color: var(--muted-text);
                font-size: 14px;
                line-height: 1.5;
            }

            .modal-footer {
                display: flex;
                gap: 15px;
                justify-content: center;
            }

            .btn-modal-primary {
                flex: 1;
                background: linear-gradient(135deg, #10b981 0%, #059669 100%);
                border: none;
                color: white;
                padding: 12px;
                border-radius: 8px;
                font-size: 14px;
                font-weight: 700;
                cursor: pointer;
                box-shadow: 0 4px 12px rgba(16, 185, 129, 0.3);
                text-align: center;
                transition: opacity 0.2s;
            }

            .btn-modal-secondary {
                flex: 1;
                background: #374151;
                border: 1px solid var(--border-color);
                color: white;
                padding: 12px;
                border-radius: 8px;
                font-size: 14px;
                font-weight: 600;
                cursor: pointer;
                text-align: center;
                transition: background 0.2s;
            }

            .btn-modal-secondary:hover {
                background: #4b5563;
            }

            .btn-modal-primary:hover {
                opacity: 0.9;
            }

            .qr-image {
                width: 220px;
                height: 220px;
                margin: 15px auto;
                border-radius: 8px;
                border: 4px solid #fff;
                display: block;
            }
            @keyframes spin {
                to {
                    transform: rotate(360deg);
                }
            }

            /* ===== F&B STAFF - CSS BEGIN ===== */
            /* Drawer, tab món lẻ/combo, card sản phẩm và phần F&B trong hóa đơn */
            /* ================= F&B DRAWER ================= */
            .fnb-drawer-toggle {
                position: fixed;
                top: 50%;
                right: 350px;
                z-index: 920;
                transform: translateY(-50%);
                width: 46px;
                min-height: 108px;
                padding: 12px 8px;
                border: 1px solid rgba(255, 51, 102, 0.65);
                border-right: 0;
                border-radius: 12px 0 0 12px;
                background: linear-gradient(180deg, #ff3366, #d91f52);
                color: #fff;
                font: inherit;
                font-size: 12px;
                font-weight: 800;
                cursor: pointer;
                writing-mode: vertical-rl;
                box-shadow: 0 10px 30px rgba(255, 51, 102, 0.3);
                transition: right .28s ease, opacity .2s ease;
            }

            .fnb-drawer-toggle:hover { opacity: .9; }
            body.fnb-drawer-open .fnb-drawer-toggle { right: 770px; }

            .fnb-drawer {
                position: fixed;
                z-index: 910;
                top: 81px;
                right: 350px;
                width: 420px;
                height: calc(100vh - 81px);
                box-sizing: border-box;
                display: flex;
                flex-direction: column;
                border-left: 1px solid var(--border-color);
                border-right: 1px solid var(--border-color);
                background: rgba(7, 12, 27, .98);
                box-shadow: -20px 0 45px rgba(0, 0, 0, .4);
                transform: translateX(100%);
                visibility: hidden;
                transition: transform .28s ease, visibility .28s ease;
            }

            body.fnb-drawer-open .fnb-drawer {
                transform: translateX(0);
                visibility: visible;
            }

            .fnb-drawer-header {
                display: flex;
                align-items: center;
                justify-content: space-between;
                gap: 12px;
                padding: 18px;
                border-bottom: 1px solid var(--border-color);
                background: rgba(255,255,255,.025);
            }

            .fnb-drawer-header h3 { margin: 0; font-size: 17px; }
            .fnb-drawer-header p { margin: 4px 0 0; color: var(--muted-text); font-size: 11px; }

            .fnb-close-button {
                width: 36px;
                height: 36px;
                border: 1px solid var(--border-color);
                border-radius: 9px;
                background: rgba(255,255,255,.04);
                color: #fff;
                cursor: pointer;
                font-size: 20px;
            }

            .fnb-tabs {
                display: grid;
                grid-template-columns: 1fr 1fr;
                gap: 8px;
                padding: 14px 18px 10px;
            }

            .fnb-tab-button {
                padding: 10px;
                border: 1px solid var(--border-color);
                border-radius: 8px;
                background: rgba(255,255,255,.035);
                color: var(--muted-text);
                font: inherit;
                font-weight: 700;
                cursor: pointer;
            }

            .fnb-tab-button.active {
                border-color: #ff3366;
                background: rgba(255,51,102,.14);
                color: #fff;
            }

            .fnb-drawer-body {
                min-height: 0;
                flex: 1;
                overflow-y: auto;
                padding: 8px 18px 18px;
            }

            .fnb-tab-panel { display: none; }
            .fnb-tab-panel.active { display: grid; gap: 10px; }

            .fnb-pos-card {
                display: grid;
                grid-template-columns: 68px minmax(0, 1fr) auto;
                align-items: center;
                gap: 11px;
                padding: 10px;
                border: 1px solid var(--border-color);
                border-radius: 11px;
                background: rgba(255,255,255,.035);
            }

            .fnb-pos-card img {
                width: 68px;
                height: 68px;
                object-fit: cover;
                border-radius: 9px;
                background: #111827;
            }

            .fnb-pos-info { min-width: 0; }
            .fnb-pos-info strong {
                display: block;
                overflow: hidden;
                color: #fff;
                font-size: 13px;
                white-space: nowrap;
                text-overflow: ellipsis;
            }
            .fnb-pos-info small {
                display: block;
                margin-top: 4px;
                color: var(--muted-text);
                font-size: 10px;
                line-height: 1.35;
            }
            .fnb-pos-price { margin-top: 7px; color: #fbbf24; font-size: 12px; font-weight: 800; }

            .fnb-qty-control {
                display: grid;
                grid-template-columns: 30px 36px 30px;
                height: 32px;
                overflow: hidden;
                border: 1px solid var(--border-color);
                border-radius: 8px;
            }
            .fnb-qty-control button {
                border: 0;
                background: rgba(255,255,255,.07);
                color: #fff;
                cursor: pointer;
                font-size: 17px;
            }
            .fnb-qty-value {
                display: grid;
                place-items: center;
                background: rgba(0,0,0,.25);
                color: #fff;
                font-size: 12px;
                font-weight: 800;
            }

            .fnb-drawer-footer {
                flex-shrink: 0;
                padding: 15px 18px 18px;
                border-top: 1px solid var(--border-color);
                background: rgba(255,255,255,.025);
            }
            .fnb-drawer-total {
                display: flex;
                justify-content: space-between;
                margin-bottom: 11px;
                color: var(--muted-text);
                font-size: 13px;
            }
            .fnb-drawer-total strong { color: #10b981; font-size: 16px; }
            .fnb-done-button {
                width: 100%;
                padding: 12px;
                border: 0;
                border-radius: 8px;
                background: #ff3366;
                color: #fff;
                font: inherit;
                font-weight: 800;
                cursor: pointer;
            }

            .fnb-cart-section { margin-top: 20px; }
            .fnb-cart-empty { color: var(--muted-text); font-size: 12px; }
            .fnb-cart-name { color: #fff; font-size: 12px; font-weight: 700; }
            .fnb-cart-meta { color: var(--muted-text); font-size: 10px; margin-top: 3px; }

            @media (max-width: 1250px) {
                .fnb-drawer { right: 0; width: min(420px, 92vw); }
                .fnb-drawer-toggle { right: 0; }
                body.fnb-drawer-open .fnb-drawer-toggle { right: min(420px, 92vw); }
            }

            /* ===== F&B STAFF - CSS END ===== */
        </style>
    </head>
    <body>

        <!-- Header Navigation -->
        <header class="navbar">
            <h1>RAPVIET CONSOLE <span style="font-size: 13px; color: var(--muted-text); margin-left: 10px; font-weight: normal; background: rgba(255,255,255,0.08); padding: 4px 10px; border-radius: 12px; border: 1px solid var(--border-color); vertical-align: middle;">CN: ${staffBranchName}</span></h1>
            <nav class="nav-links">
                <c:if test="${sessionScope.user.role == 'MANAGER' || sessionScope.user.role == 'ADMIN'}">
                    <a href="ShowtimeManager">Suất Chiếu & Giá Vé</a>
                </c:if>
                <a href="CounterBooking" class="active">Quầy Bán Vé (POS)</a>
                <a href="TicketValidation">Soát Vé Cổng</a>

                <a href="logout" style="margin-left: 20px; color: #ff3366; font-weight: bold;">Đăng Xuất</a>
            </nav>
        </header>



        <!-- ===== F&B STAFF - DRAWER UI BEGIN ===== -->
        <button type="button" class="fnb-drawer-toggle" id="fnbDrawerToggle" onclick="toggleFnbDrawer()">
            F&amp;B
        </button>

        <aside class="fnb-drawer" id="fnbDrawer" aria-hidden="true">
            <div class="fnb-drawer-header">
                <div>
                    <h3>Đồ ăn &amp; thức uống</h3>
                    <p>Chọn thêm sản phẩm cho khách mua vé tại quầy</p>
                </div>
                <button type="button" class="fnb-close-button" onclick="closeFnbDrawer()" aria-label="Đóng">×</button>
            </div>

            <div class="fnb-tabs">
                <button type="button" class="fnb-tab-button active" data-fnb-tab="items" onclick="switchFnbTab('items')">Món lẻ</button>
                <button type="button" class="fnb-tab-button" data-fnb-tab="combos" onclick="switchFnbTab('combos')">Combo</button>
            </div>

            <div class="fnb-drawer-body">
                <div class="fnb-tab-panel active" id="fnbTab-items">
                    <c:choose>
                        <c:when test="${not empty staffFnbItems}">
                            <c:forEach items="${staffFnbItems}" var="item">
                                <article class="fnb-pos-card">
                                    <c:choose>
                                        <c:when test="${empty item.imageUrl}">
                                            <img src="${pageContext.request.contextPath}/assets/images/fnb-placeholder.png"
                                                 alt="${item.productName}">
                                        </c:when>
                                        <c:when test="${item.imageUrl.startsWith('http')}">
                                            <img src="${item.imageUrl}" alt="${item.productName}">
                                        </c:when>
                                        <c:otherwise>
                                            <img src="${pageContext.request.contextPath}${item.imageUrl}"
                                                 alt="${item.productName}">
                                        </c:otherwise>
                                    </c:choose>
                                    <div class="fnb-pos-info">
                                        <strong>${item.productName}</strong>
                                        <small>${item.categoryName} · Còn ${item.stockQuantity}</small>
                                        <div class="fnb-pos-price">${item.sellingPrice} đ</div>
                                    </div>
                                    <div class="fnb-qty-control">
                                        <button type="button" onclick="changeFnbQuantity('PRODUCT', ${item.productId}, -1)">−</button>
                                        <span class="fnb-qty-value" id="fnbQty-PRODUCT-${item.productId}">0</span>
                                        <button type="button"
                                                onclick="changeFnbQuantity('PRODUCT', ${item.productId}, 1)"
                                                data-fnb-add
                                                data-type="PRODUCT"
                                                data-id="${item.productId}"
                                                data-name="${item.productName}"
                                                data-price="${item.sellingPrice}"
                                                data-max="${item.stockQuantity}">+</button>
                                    </div>
                                </article>
                            </c:forEach>
                        </c:when>
                        <c:otherwise><div class="fnb-cart-empty">Chi nhánh chưa có món lẻ đang được phép bán.</div></c:otherwise>
                    </c:choose>
                </div>

                <div class="fnb-tab-panel" id="fnbTab-combos">
                    <c:choose>
                        <c:when test="${not empty staffFnbCombos}">
                            <c:forEach items="${staffFnbCombos}" var="combo">
                                <article class="fnb-pos-card">
                                    <c:choose>
                                        <c:when test="${empty combo.imageUrl}">
                                            <img src="${pageContext.request.contextPath}/assets/images/fnb-placeholder.png"
                                                 alt="${combo.comboName}">
                                        </c:when>
                                        <c:when test="${combo.imageUrl.startsWith('http')}">
                                            <img src="${combo.imageUrl}" alt="${combo.comboName}">
                                        </c:when>
                                        <c:otherwise>
                                            <img src="${pageContext.request.contextPath}${combo.imageUrl}"
                                                 alt="${combo.comboName}">
                                        </c:otherwise>
                                    </c:choose>
                                    <div class="fnb-pos-info">
                                        <strong>${combo.comboName}</strong>
                                        <small>${combo.itemSummary} · Có thể bán ${combo.availableQuantity}</small>
                                        <div class="fnb-pos-price">${combo.sellingPrice} đ</div>
                                    </div>
                                    <div class="fnb-qty-control">
                                        <button type="button" onclick="changeFnbQuantity('COMBO', ${combo.comboId}, -1)">−</button>
                                        <span class="fnb-qty-value" id="fnbQty-COMBO-${combo.comboId}">0</span>
                                        <button type="button"
                                                onclick="changeFnbQuantity('COMBO', ${combo.comboId}, 1)"
                                                data-fnb-add
                                                data-type="COMBO"
                                                data-id="${combo.comboId}"
                                                data-name="${combo.comboName}"
                                                data-price="${combo.sellingPrice}"
                                                data-max="${combo.availableQuantity}">+</button>
                                    </div>
                                </article>
                            </c:forEach>
                        </c:when>
                        <c:otherwise><div class="fnb-cart-empty">Chi nhánh chưa có combo đang được phép bán.</div></c:otherwise>
                    </c:choose>
                </div>
            </div>

            <div class="fnb-drawer-footer">
                <div class="fnb-drawer-total">
                    <span>Tạm tính F&amp;B</span>
                    <strong id="fnbDrawerTotal">0 đ</strong>
                </div>
                <button type="button" class="fnb-done-button" onclick="closeFnbDrawer()">Hoàn tất chọn F&amp;B</button>
            </div>
        </aside>

        <main class="pos-grid">

            <!-- CỘT 1: Danh sách suất chiếu -->
            <section class="pos-panel">
                <h2 class="panel-title">Suất Chiếu Chi Nhánh</h2>

                <!-- Bộ lọc POS thông minh -->
                <div style="background: rgba(255,255,255,0.02); border: 1px solid var(--border-color); padding: 12px; border-radius: 8px; margin-bottom: 15px; display: flex; flex-direction: column; gap: 8px;">
                    <select id="filterMoviePOS" onchange="applyPOSFilters()" style="width: 100%; padding: 8px 10px; background: rgba(0,0,0,0.3); color: white; border: 1px solid var(--border-color); border-radius: 6px; outline: none; cursor: pointer; font-size: 13px; transition: border-color 0.2s;">
                        <option value="ALL">-- Tất cả phim --</option>
                    </select>
                    <select id="filterDatePOS" onchange="applyPOSFilters()" style="width: 100%; padding: 8px 10px; background: rgba(0,0,0,0.3); color: white; border: 1px solid var(--border-color); border-radius: 6px; outline: none; cursor: pointer; font-size: 13px; transition: border-color 0.2s;">
                        <option value="ALL">-- Tất cả ngày --</option>
                    </select>
                </div>

                <div style="margin-top: 15px;">
                    <c:forEach items="${showtimeList}" var="st">
                        <div class="showtime-item ${st.id == selectedShowtime.id ? 'active' : ''}" 
                             data-movie="${st.movieTitle}"
                             data-date="${st.getFormattedStartTime().substring(0, 10)}"
                             onclick="window.location.href = 'CounterBooking?showtimeId=${st.id}'"
                             id="showtimeItem-${st.id}">
                            <h3 class="st-title">${st.movieTitle}</h3>
                            <div class="st-meta">
                                <span style="font-weight: bold; color: #a5b4fc;">${st.getFormattedStartTime()}</span>
                                <span>${st.hallName}</span>
                            </div>
                        </div>
                    </c:forEach>
                </div>
            </section>

            <!-- CỘT 2: Sơ đồ phòng chiếu tương tác -->
            <section class="pos-panel" style="background: rgba(3, 7, 18, 0.05);">
                <h2 class="panel-title">Sơ Đồ Phòng Chiếu</h2>

                <c:choose>
                    <c:when test="${not empty selectedShowtime}">
                        <div class="seat-map-container">
                            <div class="screen">MÀN HÌNH CHIẾU CHÍNH (SCREEN)</div>

                            <div style="width: 100%; overflow-x: auto; padding-bottom: 10px;">
                                <div class="seat-grid" style="grid-template-columns: repeat(${maxSeatNumber}, 48px); margin: 0 auto; width: fit-content; justify-content: start;">
                                    <c:forEach items="${seatList}" var="s">
                                        <c:set var="isBooked" value="false" />
                                        <c:forEach items="${bookedSeatIds}" var="bookedId">
                                            <c:if test="${bookedId == s.id}">
                                                <c:set var="isBooked" value="true" />
                                            </c:if>
                                        </c:forEach>
                                        <c:set var="seatPrice" value="${not empty seatPricesMap[s.seatType] ? seatPricesMap[s.seatType] : selectedShowtime.basePrice}" />
                                        <button class="seat ${isBooked ? 'OCCUPIED' : (s.maintenance ? 'MAINTENANCE' : s.seatType)}"
                                                style="grid-column: ${s.seatNumber}; grid-row: ${s.getRowIndex()};"
                                                ${isBooked || s.maintenance ? 'disabled' : ''}
                                                id="seatBtn-${s.id}"
                                                data-id="${s.id}"
                                                data-code="${s.getSeatCode()}"
                                                data-price="${seatPrice}"
                                                onclick="toggleSeatSelection(this)">
                                            ${s.getSeatCode()}
                                        </button>
                                    </c:forEach>
                                </div>
                            </div>

                            <!-- Chú thích legend -->
                            <div class="legend" style="flex-wrap: wrap; gap: 10px 20px;">
                                <c:forEach items="${allSeatTypes}" var="st">
                                    <c:if test="${st.status == 'ACTIVE'}">
                                        <div class="legend-item">
                                            <div class="legend-color" style="background: ${st.color};"></div> ${st.name}
                                        </div>
                                    </c:if>
                                </c:forEach>
                                <div class="legend-item">
                                    <div class="legend-color" style="background: #374151;"></div> Đã bán
                                </div>
                                <div class="legend-item">
                                    <div class="legend-color" style="background: #4b5563;"></div> Bảo trì
                                </div>
                            </div>
                        </div>
                    </c:when>
                    <c:otherwise>
                        <div style="display: flex; flex-direction: column; align-items: center; justify-content: center; height: 100%; color: var(--muted-text);">
                            <svg width="64" height="64" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="1.5" style="margin-bottom: 15px;">
                            <rect x="3" y="3" width="18" height="18" rx="2" stroke-linecap="round" stroke-linejoin="round"></rect>
                            <path d="M9 3v18M15 3v18M3 9h18M3 15h18" stroke-linecap="round" stroke-linejoin="round"></path>
                            </svg>
                            <b>VUI LÒNG CHỌN MỘT SUẤT CHIẾU TRÊN CỘT TRÁI</b>
                        </div>
                    </c:otherwise>
                </c:choose>
            </section>

            <!-- CỘT 3: Hóa đơn giỏ hàng & Thanh toán -->
            <section class="pos-panel">
                <h2 class="panel-title">Hóa Đơn & Đặt Vé</h2>

                <!-- Alerts -->
                <c:if test="${not empty sessionScope.msgSuccess}">
                    <div style="background: rgba(16, 185, 129, 0.15); border: 1px solid rgba(16, 185, 129, 0.3); color: #34d399; padding: 12px; border-radius: 8px; margin-bottom: 15px; font-size: 13px;" id="successAlert">
                        ${sessionScope.msgSuccess}
                        <% session.removeAttribute("msgSuccess"); %>
                    </div>
                </c:if>
                <c:if test="${not empty sessionScope.msgError}">
                    <div style="background: rgba(239, 68, 68, 0.15); border: 1px solid rgba(239, 68, 68, 0.3); color: #f87171; padding: 12px; border-radius: 8px; margin-bottom: 15px; font-size: 13px;" id="errorAlert">
                        ${sessionScope.msgError}
                        <% session.removeAttribute("msgError"); %>
                    </div>
                </c:if>

                <c:choose>
                    <c:when test="${not empty selectedShowtime}">
                        <form action="CounterBooking" method="POST" id="bookingForm" onsubmit="return validateCheckout()">
                            <input type="hidden" name="action" value="book">
                            <input type="hidden" name="showtimeId" value="${selectedShowtime.id}">
                            <input type="hidden" id="selectedSeatsInput" name="selectedSeats" value="">
                            <!-- ===== F&B STAFF - FORM DATA BEGIN ===== -->
                            <!-- Dữ liệu gửi lên server theo dạng PRODUCT:id:qty hoặc COMBO:id:qty -->
                            <input type="hidden" id="selectedFnbInput" name="selectedFnb" value="">
                            <!-- ===== F&B STAFF - FORM DATA END ===== -->

                            <!-- Thông tin vé đang đặt -->
                            <div style="padding-bottom: 20px; border-bottom: 1px solid rgba(255, 255, 255, 0.08);">
                                <div style="font-weight: 700; font-size: 15px; color: #fff;">${selectedShowtime.movieTitle}</div>
                                <div style="font-size: 12px; color: var(--muted-text); margin-top: 5px;">
                                    Suất chiếu: <b>${selectedShowtime.getFormattedStartTime()}</b> | ${selectedShowtime.hallName}
                                </div>
                            </div>

                            <!-- Danh sách ghế đang chọn -->
                            <div style="margin-top: 20px;">
                                <div style="font-size: 13px; font-weight: 600; color: var(--muted-text); margin-bottom: 10px;">GHẾ ĐÃ CHỌN</div>
                                <div id="cartItemsList" style="min-height: 80px;">
                                    <div id="emptyCartMessage" style="color: var(--muted-text); font-size: 13px;">Chưa có ghế nào được chọn.</div>
                                </div>
                            </div>


                            <!-- ===== F&B STAFF - CART INVOICE BEGIN ===== -->
                            <div class="fnb-cart-section">
                                <div style="font-size: 13px; font-weight: 600; color: var(--muted-text); margin-bottom: 10px;">F&amp;B ĐÃ CHỌN</div>
                                <div id="fnbCartItemsList">
                                    <div class="fnb-cart-empty" id="emptyFnbMessage">Chưa chọn món lẻ hoặc combo.</div>
                                </div>
                            </div>

                            <!-- ===== F&B STAFF - CART INVOICE END ===== -->

                            <!-- Áp dụng mã giảm giá -->
                            <div style="margin-top: 30px;">
                                <label for="discountCodeInput" style="font-size: 13px; font-weight: 600; color: var(--muted-text); display: block; margin-bottom: 8px;">ÁP DỤNG MÃ GIẢM GIÁ</label>
                                <div style="display: flex; gap: 8px; align-items: center;">
                                    <input type="text" id="discountCodeInput" name="discountCode" class="form-input" placeholder="Nhập mã voucher (ví dụ: GIAM20K)..." style="text-transform: uppercase; flex: 1; margin: 0;">
                                    <button type="button" class="btn-pricing" onclick="applyVoucher()" style="padding: 10px 15px; border-radius: 8px; margin: 0; font-size: 12px; font-weight: 700; white-space: nowrap; height: 42px; border: 1px solid var(--primary); background: rgba(99, 102, 241, 0.15); color: #a5b4fc; cursor: pointer; transition: all 0.3s;">Áp Dụng</button>
                                </div>
                                <div id="voucherFeedback" style="font-size: 11px; margin-top: 6px; font-weight: 600; min-height: 16px;"></div>

                                <!-- Giá trị giảm thực tế & lý do được truyền ngầm lên server -->
                                <input type="hidden" id="discountAmount" name="discountAmount" value="0">
                                <input type="hidden" id="discountReason" name="discountReason" value="">
                            </div>

                            <!-- Phương thức thanh toán -->
                            <div style="margin-top: 20px;">
                                <label for="paymentMethod" style="font-size: 13px; font-weight: 600; color: var(--muted-text); display: block; margin-bottom: 8px;">PHƯƠNG THỨC THANH TOÁN</label>
                                <select id="paymentMethod" name="paymentMethod" class="form-input">
                                    <option value="CASH">TIỀN MẶT (Cash)</option>
                                    <option value="BANKING">CHUYỂN KHOẢN (Banking)</option>
                                </select>
                            </div>

                            <!-- Tổng cộng -->
                            <div class="total-box">
                                <div class="total-row">
                                    <span style="font-size: 13px; color: var(--muted-text);">Tạm tính (Ghế):</span>
                                    <span style="font-size: 14px; font-weight: 600;" id="subtotalDisplay">0 đ</span>
                                </div>
                                <!-- ===== F&B STAFF - TOTAL BEGIN ===== -->
                                <div class="total-row">
                                    <span style="font-size: 13px; color: var(--muted-text);">Tạm tính (F&amp;B):</span>
                                    <span style="font-size: 14px; font-weight: 600; color: #fbbf24;" id="fnbSubtotalDisplay">0 đ</span>
                                </div>
                                <!-- ===== F&B STAFF - TOTAL END ===== -->
                                <div class="total-row">
                                    <span style="font-size: 13px; color: var(--muted-text);">Giảm giá:</span>
                                    <span style="font-size: 14px; font-weight: 600; color: #f87171;" id="discountDisplay">0 đ</span>
                                </div>
                                <div class="total-row">
                                    <span style="font-size: 14px; font-weight: 700; color: #fff;">TỔNG THANH TOÁN:</span>
                                    <span style="font-size: 20px; font-weight: 700; color: #10b981;" id="totalDisplay">0 đ</span>
                                </div>
                            </div>

                            <button type="button" class="btn-confirm" id="btnBookSubmit" onclick="showPaymentDetails()">
                                Thanh Toán Vé
                            </button>
                        </form>
                    </c:when>
                    <c:otherwise>
                        <div style="color: var(--muted-text); font-size: 13px; text-align: center; margin-top: 50px;">
                            Hóa đơn rỗng. Vui lòng cấu hình chọn suất chiếu và ghế ngồi.
                        </div>
                    </c:otherwise>
                </c:choose>
            </section>
        </main>

        <!-- ===== F&B STAFF - DRAWER UI END ===== -->

        <!-- Modal Hộp thoại thanh toán (CASH / BANKING) -->
        <div id="paymentModal" class="modal">
            <div class="modal-content">
                <h3 class="modal-title" id="paymentModalTitle">Xác Nhận Thanh Toán</h3>
                <div class="modal-body" id="paymentModalBody">
                    <!-- Điền động bằng Javascript -->
                </div>
                <div class="modal-footer">
                    <button type="button" class="btn-modal-secondary" onclick="closePaymentModal()">Hủy</button>
                    <button type="button" class="btn-modal-primary" id="btnConfirmPayment" onclick="submitBookingForm()">Xác Nhận Giao Dịch</button>
                </div>
            </div>
        </div>

        <!-- Modal Hộp thoại thành công (Success Modal) -->
        <div id="successModal" class="modal">
            <div class="modal-content">
                <div style="width: 60px; height: 60px; background: rgba(16, 185, 129, 0.1); color: #10b981; border-radius: 50%; display: flex; align-items: center; justify-content: center; margin: 0 auto 20px auto;">
                    <svg width="32" height="32" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2.5" stroke-linecap="round" stroke-linejoin="round">
                    <polyline points="20 6 9 17 4 12"></polyline>
                    </svg>
                </div>
                <h3 class="modal-title" style="color: #10b981; -webkit-text-fill-color: initial;">Đặt Vé Thành Công!</h3>
                <div class="modal-body">
                    Giao dịch đặt vé đã được thanh toán và ghi nhận thành công.<br>
                    Bạn muốn thực hiện thao tác nào tiếp theo?
                </div>
                <div class="modal-footer" style="flex-direction: column; gap: 10px;">
                    <a href="CounterBooking?action=printTicket&bookingId=${bookingSuccessId}" target="_blank" class="btn-modal-primary" style="text-decoration: none; display: block; line-height: 20px; width: 100%; box-sizing: border-box;" onclick="closeSuccessModal()">
                        In Hóa Đơn Vé (Print Receipt)
                    </a>
                    <button type="button" class="btn-modal-secondary" style="width: 100%; margin: 0;" onclick="closeSuccessModal()">
                        Tiếp Tục Đặt Vé Mới
                    </button>
                </div>
            </div>
        </div>

        <c:if test="${not empty successBooking}">
            <script>
                window.addEventListener('DOMContentLoaded', () => {
                <c:choose>
                    <c:when test="${successBooking.status == 'PENDING'}">
                    openPendingPaymentModal(${successBooking.id}, ${successBooking.totalPrice});
                    </c:when>
                    <c:otherwise>
                    openSuccessModal();
                    </c:otherwise>
                </c:choose>
                });
            </script>
        </c:if>

        <script>
            let selectedSeats = [];
            /* ===== F&B STAFF - JAVASCRIPT BEGIN ===== */
            // Giỏ F&B chạy phía client và đồng bộ vào selectedFnbInput.
            let selectedFnb = [];
            let serverQuote = {subtotal: 0, discountAmount: 0, total: 0};
            let currentPendingBookingId = null;


            function toggleFnbDrawer() {
                const opened = document.body.classList.toggle('fnb-drawer-open');
                document.getElementById('fnbDrawer').setAttribute('aria-hidden', opened ? 'false' : 'true');
            }

            function closeFnbDrawer() {
                document.body.classList.remove('fnb-drawer-open');
                document.getElementById('fnbDrawer').setAttribute('aria-hidden', 'true');
            }

            function switchFnbTab(tab) {
                document.querySelectorAll('.fnb-tab-button').forEach(button => {
                    button.classList.toggle('active', button.dataset.fnbTab === tab);
                });
                document.querySelectorAll('.fnb-tab-panel').forEach(panel => panel.classList.remove('active'));
                document.getElementById('fnbTab-' + tab).classList.add('active');
            }

            function changeFnbQuantity(type, id, delta) {
                const addButton = document.querySelector('[data-fnb-add][data-type="' + type + '"][data-id="' + id + '"]');
                if (!addButton) return;

                const name = addButton.dataset.name;
                const price = Number(addButton.dataset.price || 0);
                const max = Number(addButton.dataset.max || 0);
                let line = selectedFnb.find(item => item.type === type && item.id === id);
                const current = line ? line.quantity : 0;
                const next = Math.max(0, Math.min(max, current + delta));

                if (next === current) return;
                if (!line && next > 0) {
                    line = {type, id, name, price, max, quantity: next};
                    selectedFnb.push(line);
                } else if (line) {
                    line.quantity = next;
                    if (next === 0) selectedFnb = selectedFnb.filter(item => item !== line);
                }

                const qtyElement = document.getElementById('fnbQty-' + type + '-' + id);
                if (qtyElement) qtyElement.textContent = next;
                updateFnbCartUI();
            }

            function getFnbSubtotal() {
                return selectedFnb.reduce((sum, item) => sum + item.price * item.quantity, 0);
            }

            function updateFnbCartUI() {
                const list = document.getElementById('fnbCartItemsList');
                const hidden = document.getElementById('selectedFnbInput');
                if (!list || !hidden) return;

                list.innerHTML = '';
                if (selectedFnb.length === 0) {
                    list.innerHTML = '<div class="fnb-cart-empty" id="emptyFnbMessage">Chưa chọn món lẻ hoặc combo.</div>';
                } else {
                    selectedFnb.forEach(item => {
                        const row = document.createElement('div');
                        row.className = 'cart-item-row';
                        row.innerHTML = '<div><div class="fnb-cart-name">' + item.quantity + ' × ' + escapeHtml(item.name) + '</div>' +
                                '<div class="fnb-cart-meta">' + (item.type === 'COMBO' ? 'Combo' : 'Món lẻ') + '</div></div>' +
                                '<span style="color:#fbbf24;font-size:12px;font-weight:700;">' +
                                (item.price * item.quantity).toLocaleString('vi-VN') + ' đ</span>';
                        list.appendChild(row);
                    });
                }

                hidden.value = selectedFnb.map(item => item.type + ':' + item.id + ':' + item.quantity).join(',');
                const subtotal = getFnbSubtotal();
                document.getElementById('fnbDrawerTotal').innerText = subtotal.toLocaleString('vi-VN') + ' đ';
                document.getElementById('fnbSubtotalDisplay').innerText = subtotal.toLocaleString('vi-VN') + ' đ';
                updateTotalSum();
            }

            function escapeHtml(value) {
                return String(value || '').replace(/[&<>'"]/g, character => ({
                    '&': '&amp;', '<': '&lt;', '>': '&gt;', "'": '&#39;', '"': '&quot;'
                })[character]);
            }

            function toggleSeatSelection(button) {
                const id = parseInt(button.getAttribute('data-id'));
                const code = button.getAttribute('data-code');
                const price = parseFloat(button.getAttribute('data-price'));

                const index = selectedSeats.findIndex(item => item.id === id);
                if (index > -1) {
                    // Remove seat
                    selectedSeats.splice(index, 1);
                    button.classList.remove('SELECTED');
                } else {
                    // Add seat
                    selectedSeats.push({id: id, code: code, price: price});
                    button.classList.add('SELECTED');
                }

                updateCartUI();
            }

            function updateCartUI() {
                const list = document.getElementById('cartItemsList');
                const emptyMsg = document.getElementById('emptyCartMessage');
                const subtotalDisp = document.getElementById('subtotalDisplay');

                // Xóa danh sách cũ
                list.innerHTML = "";

                if (selectedSeats.length === 0) {
                    list.appendChild(emptyMsg);
                    subtotalDisp.innerText = "0 đ";
                    resetAppliedVoucher();
                    return;
                }

                let subtotal = 0;
                let ids = [];

                selectedSeats.forEach(seat => {
                    subtotal += seat.price;
                    ids.push(seat.id);

                    const row = document.createElement('div');
                    row.className = "cart-item-row";
                    row.innerHTML = `
                        <span style="font-weight: 700; color: #fff;">Ghế ${seat.code}</span>
                        <span style="color: #fbbf24;">${seat.price.toLocaleString('vi-VN')} đ</span>
                    `;
                    list.appendChild(row);
                });

                // Update subtotal display
                subtotalDisp.innerText = subtotal.toLocaleString('vi-VN') + " đ";
                document.getElementById('selectedSeatsInput').value = ids.join(',');

                // Tự động cập nhật lại giảm giá âm thầm khi thay đổi ghế ngồi
                applyVoucherSilent();
            }

            // --- HÀM XỬ LÝ ÁP DỤNG VOUCHER ---
            function applyVoucher() {
                const codeInput = document.getElementById('discountCodeInput');
                const feedback = document.getElementById('voucherFeedback');
                const code = codeInput.value.trim().toUpperCase();

                if (!code) {
                    feedback.style.color = '#f87171';
                    feedback.innerText = 'Vui lòng nhập mã giảm giá!';
                    resetAppliedVoucher(true);
                    return;
                }

                let subtotal = 0;
                selectedSeats.forEach(s => subtotal += s.price);
                if (subtotal === 0) {
                    feedback.style.color = '#f87171';
                    feedback.innerText = 'Vui lòng chọn ghế trước khi áp dụng mã!';
                    resetAppliedVoucher(true);
                    return;
                }

                feedback.style.color = '#a5b4fc';
                feedback.innerText = 'Đang xác thực mã...';

                const seatIds = selectedSeats.map(s => s.id).join(',');
                const showtimeId = document.querySelector('input[name="showtimeId"]').value;
                fetch('CounterBooking?action=checkVoucher&code=' + encodeURIComponent(code)
                        + '&showtimeId=' + encodeURIComponent(showtimeId) + '&selectedSeats=' + encodeURIComponent(seatIds))
                        .then(response => response.json())
                        .then(data => {
                            if (data.success) {
                                feedback.style.color = '#34d399';
                                feedback.innerText = '✓ Thành công! Giảm: ' + data.discountAmount.toLocaleString('vi-VN') + ' đ';

                                serverQuote = data;
                                document.getElementById('discountAmount').value = data.discountAmount;
                                document.getElementById('discountReason').value = 'Mã giảm giá: ' + code;

                                updateTotalSum();
                            } else {
                                feedback.style.color = '#f87171';
                                feedback.innerText = '✗ ' + data.message;
                                serverQuote = data;
                                document.getElementById('discountAmount').value = 0;
                                document.getElementById('discountReason').value = '';
                                updateTotalSum();
                            }
                        })
                        .catch(err => {
                            feedback.style.color = '#f87171';
                            feedback.innerText = 'Lỗi kết nối kiểm tra mã!';
                            resetAppliedVoucher(true);
                        });
            }

            function applyVoucherSilent() {
                const codeInput = document.getElementById('discountCodeInput');
                const feedback = document.getElementById('voucherFeedback');
                const code = codeInput.value.trim().toUpperCase();

                let subtotal = 0;
                selectedSeats.forEach(s => subtotal += s.price);
                if (subtotal === 0) {
                    resetAppliedVoucher();
                    return;
                }

                const seatIds = selectedSeats.map(s => s.id).join(',');
                const showtimeId = document.querySelector('input[name="showtimeId"]').value;
                fetch('CounterBooking?action=checkVoucher&code=' + encodeURIComponent(code)
                        + '&showtimeId=' + encodeURIComponent(showtimeId) + '&selectedSeats=' + encodeURIComponent(seatIds))
                        .then(response => response.json())
                        .then(data => {
                            if (data.success) {
                                feedback.style.color = '#34d399';
                                feedback.innerText = '✓ Thành công! Giảm: ' + data.discountAmount.toLocaleString('vi-VN') + ' đ';
                                serverQuote = data;
                                document.getElementById('discountAmount').value = data.discountAmount;
                                document.getElementById('discountReason').value = 'Mã giảm giá: ' + code;
                            } else {
                                feedback.style.color = '#f87171';
                                feedback.innerText = '✗ ' + data.message;
                                serverQuote = data;
                                document.getElementById('discountAmount').value = 0;
                                document.getElementById('discountReason').value = '';
                            }
                            updateTotalSum();
                        })
                        .catch(err => {
                            resetAppliedVoucher();
                        });
            }

            function resetAppliedVoucher(keepFeedback) {
                serverQuote = {subtotal: 0, discountAmount: 0, total: 0};
                document.getElementById('discountAmount').value = 0;
                document.getElementById('discountReason').value = '';
                const feedback = document.getElementById('voucherFeedback');
                if (feedback && !keepFeedback)
                    feedback.innerText = '';
                updateTotalSum();
            }

            function updateTotalSum() {
                const localSeatSubtotal = selectedSeats.reduce((sum, seat) => sum + seat.price, 0);
                const seatSubtotal = Number(serverQuote.subtotal || localSeatSubtotal);
                const discount = Number(serverQuote.discountAmount || 0);
                const fnbSubtotal = getFnbSubtotal();
                const total = Math.max(0, seatSubtotal - discount + fnbSubtotal);

                document.getElementById('subtotalDisplay').innerText = seatSubtotal.toLocaleString('vi-VN') + " đ";
                document.getElementById('fnbSubtotalDisplay').innerText = fnbSubtotal.toLocaleString('vi-VN') + " đ";
                document.getElementById('discountDisplay').innerText = (discount > 0 ? "-" : "") + discount.toLocaleString('vi-VN') + " đ";
                document.getElementById('totalDisplay').innerText = total.toLocaleString('vi-VN') + " đ";
            }

            function validateCheckout() {
                if (selectedSeats.length === 0) {
                    alert("Vui lòng chọn ghế ngồi trên sơ đồ trước khi thanh toán!");
                    return false;
                }

                const discountInput = document.getElementById('discountAmount');
                const reasonInput = document.getElementById('discountReason');
                if (discountInput && parseFloat(discountInput.value) > 0) {
                    if (!reasonInput.value || reasonInput.value.trim().length === 0) {
                        alert("Vui lòng nhập lý do áp dụng giảm giá!");
                        return false;
                    }
                }
                return true;
            }

            // Tự động ẩn thông báo sau 5 giây
            setTimeout(() => {
                const sAlert = document.getElementById('successAlert');
                const eAlert = document.getElementById('errorAlert');
                if (sAlert)
                    sAlert.style.display = 'none';
                if (eAlert)
                    eAlert.style.display = 'none';
            }, 5000);

            let checkPaymentInterval = null;

            function showPaymentDetails() {
                if (!validateCheckout()) {
                    return;
                }

                let subtotal = 0;
                selectedSeats.forEach(s => subtotal += s.price);
                let discount = 0;
                const discountInput = document.getElementById('discountAmount');
                if (discountInput && discountInput.value) {
                    discount = parseFloat(discountInput.value);
                }
                const fnbSubtotal = getFnbSubtotal();
                let total = subtotal - discount + fnbSubtotal;
                if (total < 0)
                    total = 0;

                const paymentMethod = document.getElementById('paymentMethod').value;
                const modal = document.getElementById('paymentModal');
                const title = document.getElementById('paymentModalTitle');
                const body = document.getElementById('paymentModalBody');

                let seatCodes = selectedSeats.map(s => s.code).join(', ');

                // Đảm bảo hiển thị lại nút Xác nhận của Cash mặc định
                document.getElementById('btnConfirmPayment').style.display = 'inline-block';

                if (paymentMethod === 'CASH') {
                    title.innerText = "Xác Nhận Thu Tiền Mặt";
                    body.innerHTML =
                            '<div style="text-align: left; background: rgba(255,255,255,0.02); padding: 15px; border-radius: 8px; border: 1px solid var(--border-color);">' +
                            '<p style="margin: 5px 0;">Ghế chọn: <strong style="color: #fff;">' + seatCodes + '</strong></p>' +
                            '<p style="margin: 5px 0;">F&amp;B: <strong style="color: #fbbf24;">' + fnbSubtotal.toLocaleString('vi-VN') + ' đ</strong></p>' +
                            '<p style="margin: 5px 0;">Tổng tiền: <strong style="color: #10b981; font-size: 18px;">' + total.toLocaleString('vi-VN') + ' đ</strong></p>' +
                            '<hr style="border-color: rgba(255,255,255,0.08); margin: 15px 0;">' +
                            '<p style="color: #fbbf24; font-weight: 600; margin: 0; line-height: 1.4;">👉 Vui lòng nhận đủ số tiền mặt trên từ khách hàng trước khi xác nhận.</p>' +
                            '</div>';
                    modal.style.display = 'flex';
                } else if (paymentMethod === 'BANKING') {
                    // Đối với chuyển khoản, gửi form ngay lập tức để tạo đơn hàng trạng thái PENDING khóa ghế trước!
                    submitBookingForm();
                }
            }

            function openPendingPaymentModal(bookingId, amount) {
                currentPendingBookingId = bookingId;
                const modal = document.getElementById('paymentModal');
                const title = document.getElementById('paymentModalTitle');
                const body = document.getElementById('paymentModalBody');

                title.innerText = "Chuyển Khoản Banking QR (Đơn #" + bookingId + ")";

                const bankId = "MB";
                const accountNo = "0972282208";
                const accountName = "Hoang Ngoc Tu";
                // Nội dung chuyển khoản định danh: RVS[bookingId]
                const content = "RVS" + bookingId;
                const qrUrl = 'https://img.vietqr.io/image/' + bankId + '-' + accountNo + '-print.png?amount=' + amount + '&addInfo=' + encodeURIComponent(content) + '&accountName=' + encodeURIComponent(accountName);

                body.innerHTML =
                        '<div style="text-align: center;">' +
                        '<p style="margin: 5px 0;">Mã đơn vé: <strong style="color: #fff;">#' + bookingId + '</strong></p>' +
                        '<p style="margin: 5px 0;">Số tiền cần chuyển: <strong style="color: #10b981; font-size: 18px;">' + amount.toLocaleString('vi-VN') + ' đ</strong></p>' +
                        '<p style="margin: 5px 0;">Nội dung chuyển khoản bắt buộc: <strong style="color: #fbbf24; font-size: 16px; background: rgba(251,191,36,0.1); padding: 2px 8px; border-radius: 4px; border: 1px dashed #fbbf24;">' + content + '</strong></p>' +
                        '<img src="' + qrUrl + '" alt="VietQR Banking Code" class="qr-image" style="max-width: 200px; display: block; margin: 15px auto;" />' +
                        '<div id="bankingStatus" style="margin-top: 15px; font-weight: 600; color: #a5b4fc; font-size: 13px;">' +
                        '<span class="spinner-demo" style="display: inline-block; width: 12px; height: 12px; border: 2px solid #a5b4fc; border-top-color: transparent; border-radius: 50%; animation: spin 1s linear infinite; margin-right: 8px; vertical-align: middle;"></span>' +
                        'Đang quét đối soát ngân hàng tự động (SePay)...' +
                        '</div>' +
                        '</div>';

                // Ẩn nút xác nhận thủ công ở modal footer vì đang đợi webhook đối soát hoặc giả lập
                document.getElementById('btnConfirmPayment').style.display = 'none';
                modal.style.display = 'flex';

                // Bắt đầu vòng lặp polling kiểm tra trạng thái thanh toán mỗi 2 giây
                if (checkPaymentInterval)
                    clearInterval(checkPaymentInterval);
                checkPaymentInterval = setInterval(() => {
                    checkBookingStatusFromServer(bookingId);
                }, 2000);
            }

            function checkBookingStatusFromServer(bookingId) {
                fetch('CounterBooking?action=checkPaymentStatus&bookingId=' + bookingId)
                        .then(response => response.json())
                        .then(data => {
                            if (data.status === 'CONFIRMED') {
                                // Thanh toán thành công!
                                if (checkPaymentInterval) {
                                    clearInterval(checkPaymentInterval);
                                    checkPaymentInterval = null;
                                }

                                // Cập nhật giao diện trạng thái thành công
                                const statusDiv = document.getElementById('bankingStatus');
                                if (statusDiv) {
                                    statusDiv.innerHTML = '<span style="color: #10b981; font-weight: 700; font-size: 14px;">✓ Đã nhận đủ tiền! Đang chuyển tiếp...</span>';
                                }

                                setTimeout(() => {
                                    // RẤT QUAN TRỌNG: Đặt lại ID bằng null trước khi đóng modal
                                    // để tránh hàm closePaymentModal hiểu nhầm là người dùng chủ động bấm Hủy đơn!
                                    currentPendingBookingId = null;
                                    closePaymentModal();
                                    // Chuyển sang màn hình successModal
                                    // Cập nhật URL hiện tại để khi reload hoặc in vé có đúng bookingId
                                    const url = new URL(window.location.href);
                                    url.searchParams.set('bookingSuccessId', bookingId);
                                    window.history.replaceState({}, document.title, url.toString());

                                    // Cập nhật đường link in vé động trên successModal
                                    const printBtn = document.querySelector("#successModal .btn-modal-primary");
                                    if (printBtn) {
                                        printBtn.setAttribute("href", "CounterBooking?action=printTicket&bookingId=" + bookingId);
                                    }

                                    openSuccessModal();
                                }, 1200);
                            }
                        })
                        .catch(err => console.error("Lỗi kiểm tra trạng thái thanh toán:", err));
            }



            function closePaymentModal() {
                if (checkPaymentInterval) {
                    clearInterval(checkPaymentInterval);
                    checkPaymentInterval = null;
                }

                if (currentPendingBookingId !== null) {
                    const bookingId = currentPendingBookingId;
                    currentPendingBookingId = null; // Tránh loop hoặc bấm nhiều lần

                    const statusDiv = document.getElementById('bankingStatus');
                    if (statusDiv) {
                        statusDiv.innerHTML = '<span style="color: #f87171; font-weight: 600;">⌛ Đang giải phóng ghế ngồi...</span>';
                    }

                    // Disable nút huỷ để tránh bấm nhiều lần
                    const cancelBtn = document.querySelector("#paymentModal .btn-modal-secondary");
                    if (cancelBtn) {
                        cancelBtn.disabled = true;
                        cancelBtn.style.opacity = '0.5';
                    }

                    fetch('CounterBooking?action=cancelBooking&bookingId=' + bookingId, {method: 'POST'})
                            .then(res => res.json())
                            .then(data => {
                                // Reload trang về dạng không có bookingSuccessId để giải phóng ghế trên giao diện
                                const url = new URL(window.location.href);
                                url.searchParams.delete('bookingSuccessId');
                                window.location.href = url.toString();
                            })
                            .catch(err => {
                                console.error("Lỗi khi hủy đơn:", err);
                                const url = new URL(window.location.href);
                                url.searchParams.delete('bookingSuccessId');
                                window.location.href = url.toString();
                            });
                } else {
                    document.getElementById('paymentModal').style.display = 'none';
                }
            }

            function submitBookingForm() {
                document.getElementById('bookingForm').submit();
            }

            function openSuccessModal() {
                document.getElementById('successModal').style.display = 'flex';
            }

            function closeSuccessModal() {
                document.getElementById('successModal').style.display = 'none';
                // General clean-up of URL parameter
                const url = new URL(window.location.href);
                url.searchParams.delete('bookingSuccessId');
                window.history.replaceState({}, document.title, url.toString());
            }

            /* ===== F&B STAFF - JAVASCRIPT END ===== */

            // INITIALIZE AND APPLY DYNAMIC CLIENT-SIDE FILTERS FOR POS LEFT PANEL
            let posItems = [];
            window.addEventListener('DOMContentLoaded', () => {
                posItems = Array.from(document.querySelectorAll('.showtime-item'));

                const movieSelect = document.getElementById('filterMoviePOS');
                const dateSelect = document.getElementById('filterDatePOS');

                const movies = new Set();
                const dates = new Set();

                posItems.forEach(item => {
                    if (item.dataset.movie)
                        movies.add(item.dataset.movie);
                    if (item.dataset.date)
                        dates.add(item.dataset.date);
                });

                Array.from(movies).sort().forEach(m => {
                    const opt = document.createElement('option');
                    opt.value = m;
                    opt.textContent = m;
                    movieSelect.appendChild(opt);
                });

                Array.from(dates).sort((a, b) => {
                    const partsA = a.split('/');
                    const partsB = b.split('/');
                    return new Date(partsA[2], partsA[1] - 1, partsA[0]) - new Date(partsB[2], partsB[1] - 1, partsB[0]);
                }).forEach(d => {
                    const opt = document.createElement('option');
                    opt.value = d;
                    opt.textContent = d;
                    dateSelect.appendChild(opt);
                });

                // Check if there is a selected showtime, pre-select the filter option so the selected card is visible
                const activeItem = document.querySelector('.showtime-item.active');
                if (activeItem) {
                    movieSelect.value = activeItem.dataset.movie || 'ALL';
                    dateSelect.value = activeItem.dataset.date || 'ALL';
                }

                applyPOSFilters();
            });

            function applyPOSFilters() {
                const movieVal = document.getElementById('filterMoviePOS').value;
                const dateVal = document.getElementById('filterDatePOS').value;

                posItems.forEach(item => {
                    const mMatch = (movieVal === 'ALL' || item.dataset.movie === movieVal);
                    const dMatch = (dateVal === 'ALL' || item.dataset.date === dateVal);

                    if (mMatch && dMatch) {
                        item.style.display = 'block';
                    } else {
                        item.style.display = 'none';
                    }
                });
            }
        </script>
    </body>
</html>
