<%@page contentType="text/html" pageEncoding="UTF-8"%>
<%@taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<%@taglib prefix="fmt" uri="http://java.sun.com/jsp/jstl/fmt" %>
<!DOCTYPE html>
<html lang="vi">
<head>
    <meta charset="UTF-8">
    <meta name="viewport" content="width=device-width, initial-scale=1.0">
    <meta name="description" content="Hệ thống điều hành mã giảm giá và khuyến mãi RVS">
    <title>RVS - Quản Lý Mã Giảm Giá (Vouchers)</title>
    
    <!-- Modern Google Font -->
    <link href="https://fonts.googleapis.com/css2?family=Outfit:wght@300;400;500;600;700;800&display=swap" rel="stylesheet">
    
    <style>
        :root {
            --bg-color: hsl(222, 47%, 6%);
            --glass-bg: hsla(222, 47%, 12%, 0.7);
            --border-color: hsla(217, 30%, 20%, 0.5);
            --primary: hsl(224, 89%, 60%);
            --emerald: hsl(150, 84%, 37%);
            --crimson: hsl(350, 80%, 50%);
            --amber: #d97706;
            --text-color: hsl(0, 0%, 100%);
            --muted-text: hsl(215, 20%, 65%);
        }

        body {
            background-color: var(--bg-color);
            color: var(--text-color);
            font-family: 'Outfit', sans-serif;
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
            box-sizing: border-box;
        }

        .header-section {
            display: flex;
            justify-content: space-between;
            align-items: center;
            margin-bottom: 30px;
        }

        .header-section h2 {
            font-size: 28px;
            font-weight: 800;
            margin: 0;
            background: linear-gradient(135deg, #fff 0%, #a5b4fc 100%);
            -webkit-background-clip: text;
            -webkit-text-fill-color: transparent;
        }

        .btn-create {
            background: linear-gradient(135deg, #ff3366 0%, #e11d48 100%);
            border: none;
            color: white;
            padding: 12px 24px;
            border-radius: 8px;
            font-size: 14px;
            font-weight: 700;
            cursor: pointer;
            box-shadow: 0 4px 15px rgba(255, 51, 102, 0.4);
            transition: all 0.3s;
            display: flex;
            align-items: center;
            gap: 8px;
        }

        .btn-create:hover {
            transform: translateY(-2px);
            opacity: 0.95;
        }

        /* Glassmorphic Alerts */
        .alert {
            padding: 15px 20px;
            border-radius: 8px;
            margin-bottom: 25px;
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

        /* Vouchers Table Styling */
        .vouchers-card {
            background: var(--glass-bg);
            border: 1px solid var(--border-color);
            border-radius: 16px;
            overflow: hidden;
            backdrop-filter: blur(16px);
            box-shadow: 0 20px 40px rgba(0, 0, 0, 0.4);
        }

        table {
            width: 100%;
            border-collapse: collapse;
            text-align: left;
        }

        th {
            background: rgba(255, 255, 255, 0.03);
            padding: 18px 20px;
            font-size: 13px;
            font-weight: 700;
            color: var(--muted-text);
            text-transform: uppercase;
            letter-spacing: 0.5px;
            border-bottom: 1px solid var(--border-color);
        }

        td {
            padding: 18px 20px;
            font-size: 14px;
            border-bottom: 1px solid rgba(255, 255, 255, 0.04);
            color: var(--text-color);
            vertical-align: middle;
        }

        tr:last-child td {
            border-bottom: none;
        }

        tr:hover td {
            background: rgba(255, 255, 255, 0.01);
        }

        .code-text {
            font-family: monospace;
            font-size: 15px;
            font-weight: 700;
            background: rgba(99, 102, 241, 0.15);
            color: #a5b4fc;
            padding: 4px 10px;
            border-radius: 6px;
            border: 1px dashed rgba(99, 102, 241, 0.3);
            display: inline-block;
        }

        .value-text {
            font-weight: 700;
            color: #fff;
        }

        .max-value {
            font-size: 12px;
            color: var(--muted-text);
            display: block;
            margin-top: 3px;
        }

        /* Status badges */
        .badge {
            display: inline-flex;
            align-items: center;
            padding: 4px 10px;
            border-radius: 12px;
            font-size: 11px;
            font-weight: 700;
            text-transform: uppercase;
            letter-spacing: 0.5px;
        }

        .badge-active {
            background: rgba(16, 185, 129, 0.15);
            color: #34d399;
            border: 1px solid rgba(16, 185, 129, 0.3);
        }

        .badge-paused {
            background: rgba(217, 119, 6, 0.15);
            color: #fbbf24;
            border: 1px solid rgba(217, 119, 6, 0.3);
        }

        .badge-expired {
            background: rgba(239, 68, 68, 0.15);
            color: #f87171;
            border: 1px solid rgba(239, 68, 68, 0.3);
        }

        /* Progress Bar for Voucher usages */
        .usage-container {
            width: 140px;
        }

        .usage-text {
            font-size: 12px;
            font-weight: 600;
            color: var(--text-color);
            margin-bottom: 5px;
            display: flex;
            justify-content: space-between;
        }

        .progress-bar-bg {
            background: rgba(255, 255, 255, 0.05);
            border-radius: 4px;
            height: 6px;
            overflow: hidden;
            width: 100%;
        }

        .progress-bar-fill {
            height: 100%;
            border-radius: 4px;
            background: linear-gradient(90deg, var(--primary), var(--indigo));
        }

        /* Actions styling */
        .actions-cell {
            display: flex;
            gap: 8px;
        }

        .btn-action {
            background: rgba(255, 255, 255, 0.05);
            border: 1px solid var(--border-color);
            color: var(--muted-text);
            padding: 8px;
            border-radius: 6px;
            cursor: pointer;
            transition: all 0.2s;
            display: inline-flex;
            align-items: center;
            justify-content: center;
        }

        .btn-action:hover {
            background: rgba(255, 255, 255, 0.1);
            color: #fff;
        }

        .btn-action.btn-delete:hover {
            background: rgba(239, 68, 68, 0.15);
            color: #f87171;
            border-color: rgba(239, 68, 68, 0.3);
        }

        /* Modal Dialog Styling */
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
            background: var(--glass-bg);
            border: 1px solid var(--border-color);
            border-radius: 16px;
            width: 95%;
            max-width: 500px;
            padding: 30px;
            box-sizing: border-box;
            box-shadow: 0 25px 50px rgba(0,0,0,0.5);
            position: relative;
            animation: modalFadeIn 0.3s cubic-bezier(0.16, 1, 0.3, 1);
        }

        @keyframes modalFadeIn {
            from { transform: scale(0.95); opacity: 0; }
            to { transform: scale(1); opacity: 1; }
        }

        .modal-title {
            font-size: 20px;
            font-weight: 700;
            margin: 0 0 20px 0;
            background: linear-gradient(135deg, #fff 0%, #a5b4fc 100%);
            -webkit-background-clip: text;
            -webkit-text-fill-color: transparent;
        }

        .form-grid {
            display: grid;
            grid-template-columns: 1fr 1fr;
            gap: 15px;
        }

        .form-group {
            margin-bottom: 18px;
            display: flex;
            flex-direction: column;
        }

        .form-group.full-width {
            grid-column: span 2;
        }

        label {
            font-size: 12px;
            font-weight: 700;
            color: var(--muted-text);
            text-transform: uppercase;
            letter-spacing: 0.5px;
            margin-bottom: 6px;
        }

        input, select {
            background: rgba(15, 23, 42, 0.6);
            border: 1px solid var(--border-color);
            border-radius: 8px;
            padding: 10px 14px;
            font-family: inherit;
            font-size: 14px;
            color: white;
            outline: none;
            box-sizing: border-box;
            transition: all 0.3s;
        }

        input:focus, select:focus {
            border-color: var(--primary);
            box-shadow: 0 0 8px rgba(255, 51, 102, 0.2);
        }

        .modal-footer {
            display: flex;
            justify-content: flex-end;
            gap: 12px;
            margin-top: 15px;
        }

        .btn-modal-cancel {
            background: rgba(255,255,255,0.05);
            border: 1px solid var(--border-color);
            color: var(--muted-text);
            padding: 10px 20px;
            border-radius: 8px;
            font-weight: 600;
            font-size: 14px;
            cursor: pointer;
            transition: all 0.3s;
        }

        .btn-modal-cancel:hover {
            background: rgba(255,255,255,0.1);
            color: white;
        }

        .btn-modal-submit {
            background: linear-gradient(135deg, #ff3366 0%, #e11d48 100%);
            border: none;
            color: white;
            padding: 10px 24px;
            border-radius: 8px;
            font-weight: 700;
            font-size: 14px;
            cursor: pointer;
            box-shadow: 0 4px 15px rgba(255, 51, 102, 0.4);
            transition: all 0.3s;
        }

        .btn-modal-submit:hover {
            opacity: 0.95;
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
                <a href="DiscountManager" class="active">Mã Giảm Giá</a>
                <a href="SeatConfigController" style="margin-left: 20px;">Cấu Hình Phòng</a>
            </c:if>
            <a href="logout" style="margin-left: 20px; color: #ff3366; font-weight: bold;">Đăng Xuất</a>
        </nav>
    </header>

    <main class="container">
        
        <!-- Header Title and Add Button -->
        <section class="header-section">
            <h2>Hệ Thống Quản Lý Mã Giảm Giá</h2>
            <button class="btn-create" onclick="openModal()" id="btnOpenAddModal">
                <svg width="18" height="18" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2.5" stroke-linecap="round" stroke-linejoin="round">
                    <line x1="12" y1="5" x2="12" y2="19"></line>
                    <line x1="5" y1="12" x2="19" y2="12"></line>
                </svg>
                Thêm Mã Giảm Giá
            </button>
        </section>

        <!-- Session Message Alerts -->
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

        <!-- Voucher List Card Table -->
        <section class="vouchers-card">
            <c:choose>
                <c:when test="${not empty discountList}">
                    <table>
                        <thead>
                            <tr>
                                <th>Mã Voucher</th>
                                <th>Trị giá giảm</th>
                                <th>Đơn tối thiểu</th>
                                <th>Tỉ lệ sử dụng</th>
                                <th>Thời hạn hiệu lực</th>
                                <th>Trạng thái</th>
                                <th style="text-align: center;">Thao tác</th>
                            </tr>
                        </thead>
                        <tbody>
                            <c:forEach items="${discountList}" var="d">
                                <tr>
                                    <td>
                                        <span class="code-text">${d.code}</span>
                                    </td>
                                    <td>
                                        <span class="value-text">
                                            <c:choose>
                                                <c:when test="${d.discountType == 'PERCENT'}">
                                                    Giảm ${d.discountValue}%
                                                    <c:if test="${not empty d.maxDiscountAmount}">
                                                        <span class="max-value">(Tối đa <fmt:formatNumber value="${d.maxDiscountAmount}" pattern="#,##0"/>đ)</span>
                                                    </c:if>
                                                </c:when>
                                                <c:otherwise>
                                                    Giảm <fmt:formatNumber value="${d.discountValue}" pattern="#,##0"/>đ
                                                </c:otherwise>
                                            </c:choose>
                                        </span>
                                    </td>
                                    <td>
                                        <span style="font-weight: 600;">
                                            <fmt:formatNumber value="${d.minOrderValue}" pattern="#,##0"/>đ
                                        </span>
                                    </td>
                                    <td>
                                        <div class="usage-container">
                                            <div class="usage-text">
                                                <span>${d.usedCount}/${d.maxUses} lượt</span>
                                                <span><fmt:formatNumber value="${(d.usedCount / d.maxUses) * 100}" pattern="#,##0"/>%</span>
                                            </div>
                                            <div class="progress-bar-bg">
                                                <div class="progress-bar-fill" style="width: ${(d.usedCount / d.maxUses) * 100}%"></div>
                                            </div>
                                        </div>
                                    </td>
                                    <td style="font-size: 13px; color: var(--muted-text); line-height: 1.4;">
                                        Từ: ${d.getFormattedStartDate()}<br>
                                        Đến: ${d.getFormattedEndDate()}
                                    </td>
                                    <td>
                                        <c:choose>
                                            <c:when test="${d.status == 'ACTIVE'}">
                                                <span class="badge badge-active">Hoạt động</span>
                                            </c:when>
                                            <c:when test="${d.status == 'PAUSED'}">
                                                <span class="badge badge-paused">Tạm dừng</span>
                                            </c:when>
                                            <c:otherwise>
                                                <span class="badge badge-expired">Hết hạn</span>
                                            </c:otherwise>
                                        </c:choose>
                                    </td>
                                    <td style="text-align: center;">
                                        <div class="actions-cell" style="justify-content: center;">
                                            <!-- Toggle Status Button -->
                                            <c:choose>
                                                <c:when test="${d.status == 'ACTIVE'}">
                                                    <form action="DiscountManager" method="POST" style="display:inline;">
                                                        <input type="hidden" name="action" value="updateStatus">
                                                        <input type="hidden" name="id" value="${d.id}">
                                                        <input type="hidden" name="status" value="PAUSED">
                                                        <button type="submit" class="btn-action" title="Tạm dừng hoạt động">
                                                            <svg width="15" height="15" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2.5">
                                                                <rect x="6" y="4" width="4" height="16"></rect>
                                                                <rect x="14" y="4" width="4" height="16"></rect>
                                                            </svg>
                                                        </button>
                                                    </form>
                                                </c:when>
                                                <c:otherwise>
                                                    <form action="DiscountManager" method="POST" style="display:inline;">
                                                        <input type="hidden" name="action" value="updateStatus">
                                                        <input type="hidden" name="id" value="${d.id}">
                                                        <input type="hidden" name="status" value="ACTIVE">
                                                        <button type="submit" class="btn-action" title="Kích hoạt hoạt động" style="color: #34d399;">
                                                            <svg width="15" height="15" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2.5">
                                                                <polygon points="5 3 19 12 5 21 5 3"></polygon>
                                                            </svg>
                                                        </button>
                                                    </form>
                                                </c:otherwise>
                                            </c:choose>

                                            <!-- Delete Button -->
                                            <form action="DiscountManager" method="POST" style="display:inline;" onsubmit="return confirm('Bạn có chắc chắn muốn xóa mã giảm giá này không?')">
                                                <input type="hidden" name="action" value="delete">
                                                <input type="hidden" name="id" value="${d.id}">
                                                <button type="submit" class="btn-action btn-delete" title="Xóa mã">
                                                    <svg width="15" height="15" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2.5">
                                                        <polyline points="3 6 5 6 21 6"></polyline>
                                                        <path d="M19 6v14a2 2 0 0 1-2 2H7a2 2 0 0 1-2-2V6m3 0V4a2 2 0 0 1 2-2h4a2 2 0 0 1 2 2v2"></path>
                                                    </svg>
                                                </button>
                                            </form>
                                        </div>
                                    </td>
                                </tr>
                            </c:forEach>
                        </tbody>
                    </table>
                </c:when>
                <c:otherwise>
                    <div style="padding: 60px; text-align: center; color: var(--muted-text);">
                        <svg width="64" height="64" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="1.5" style="margin-bottom: 15px; opacity: 0.5;">
                            <path d="M21 16V8a2 2 0 0 0-1-1.73l-7-4a2 2 0 0 0-2 0l-7 4A2 2 0 0 0 3 8v8a2 2 0 0 0 1 1.73l7 4a2 2 0 0 0 2 0l7-4A2 2 0 0 0 21 16z"></path>
                            <polyline points="3.27 6.96 12 12.01 20.73 6.96"></polyline>
                            <line x1="12" y1="22.08" x2="12" y2="12"></line>
                        </svg>
                        <p style="font-weight: 600; margin: 0; font-size: 16px;">Hệ thống chưa có mã giảm giá nào</p>
                        <p style="font-size: 13px; margin: 5px 0 0 0;">Bấm nút "Thêm Mã Giảm Giá" để tạo chương trình khuyến mãi đầu tiên.</p>
                    </div>
                </c:otherwise>
            </c:choose>
        </section>
    </main>

    <!-- Create Voucher Popup Modal Dialog -->
    <div id="addDiscountModal" class="modal">
        <div class="modal-content">
            <h3 class="modal-title">Tạo Mã Giảm Giá Mới</h3>
            
            <form action="DiscountManager" method="POST" onsubmit="return validateForm()">
                <input type="hidden" name="action" value="create">
                
                <div class="form-grid">
                    <div class="form-group full-width">
                        <label for="code">Tên Mã Giảm Giá</label>
                        <input type="text" id="code" name="code" placeholder="Ví dụ: DONGGIA70K, CHAOSONG20" required autocomplete="off">
                    </div>
                    
                    <div class="form-group">
                        <label for="discountType">Loại giảm</label>
                        <select id="discountType" name="discountType" onchange="toggleMaxDiscountField(this.value)">
                            <option value="FLAT">Số tiền (đ)</option>
                            <option value="PERCENT">Phần trăm (%)</option>
                        </select>
                    </div>
                    
                    <div class="form-group">
                        <label for="discountValue" id="valueLabel">Mức Giảm (đ)</label>
                        <input type="number" id="discountValue" name="discountValue" min="1" placeholder="Ví dụ: 20000" required>
                    </div>

                    <div class="form-group" id="maxDiscountGroup" style="display: none;">
                        <label for="maxDiscountAmount">Mức Giảm Tối Đa (đ)</label>
                        <input type="number" id="maxDiscountAmount" name="maxDiscountAmount" placeholder="Bỏ trống nếu không hạn chế">
                    </div>
                    
                    <div class="form-group" id="minOrderGroup">
                        <label for="minOrderValue">Đơn Tối Thiểu (đ)</label>
                        <input type="number" id="minOrderValue" name="minOrderValue" min="0" value="0" required>
                    </div>

                    <div class="form-group full-width">
                        <label for="maxUses">Số lượt sử dụng tối đa</label>
                        <input type="number" id="maxUses" name="maxUses" min="1" value="100" required>
                    </div>

                    <div class="form-group">
                        <label for="startDate">Ngày hiệu lực</label>
                        <input type="datetime-local" id="startDate" name="startDate" required>
                    </div>

                    <div class="form-group">
                        <label for="endDate">Ngày hết hạn</label>
                        <input type="datetime-local" id="endDate" name="endDate" required>
                    </div>
                </div>

                <div class="modal-footer">
                    <button type="button" class="btn-modal-cancel" onclick="closeModal()">Hủy Bỏ</button>
                    <button type="submit" class="btn-modal-submit">Xác Nhận Tạo</button>
                </div>
            </form>
        </div>
    </div>

    <!-- JavaScript Interactions -->
    <script>
        // Tự động tắt thông báo alert sau 4 giây
        window.addEventListener('DOMContentLoaded', () => {
            const alerts = ['successAlert', 'errorAlert'];
            alerts.forEach(id => {
                const el = document.getElementById(id);
                if (el) {
                    setTimeout(() => {
                        el.style.opacity = '0';
                        el.style.transition = 'opacity 0.4s ease';
                        setTimeout(() => el.remove(), 400);
                    }, 4000);
                }
            });

            // Set mặc định thời gian cho form thêm mới (bắt đầu từ hôm nay, kết thúc sau 30 ngày)
            const startInput = document.getElementById('startDate');
            const endInput = document.getElementById('endDate');
            if (startInput && endInput) {
                const now = new Date();
                const pad = (num) => String(num).padStart(2, '0');
                
                const formatDateTimeLocal = (d) => {
                    return d.getFullYear() + '-' + pad(d.getMonth() + 1) + '-' + pad(d.getDate()) + 'T' + pad(d.getHours()) + ':' + pad(d.getMinutes());
                };
                
                startInput.value = formatDateTimeLocal(now);
                
                const nextMonth = new Date();
                nextMonth.setDate(now.getDate() + 30);
                endInput.value = formatDateTimeLocal(nextMonth);
            }
        });

        // Đóng/Mở Modal Popup
        function openModal() {
            document.getElementById('addDiscountModal').style.display = 'flex';
        }

        function closeModal() {
            document.getElementById('addDiscountModal').style.display = 'none';
        }

        // Đóng modal khi bấm ra ngoài vùng nội dung
        window.onclick = function(event) {
            const modal = document.getElementById('addDiscountModal');
            if (event.target === modal) {
                closeModal();
            }
        }

        // Hiện/Ẩn trường Giảm tối đa tùy theo loại Voucher
        function toggleMaxDiscountField(type) {
            const maxGroup = document.getElementById('maxDiscountGroup');
            const minGroup = document.getElementById('minOrderGroup');
            const valueLabel = document.getElementById('valueLabel');
            const discountValue = document.getElementById('discountValue');
            
            if (type === 'PERCENT') {
                maxGroup.style.display = 'flex';
                valueLabel.innerText = "Mức Giảm (%)";
                discountValue.placeholder = "Ví dụ: 10 (tức 10%)";
                discountValue.max = "100";
            } else {
                maxGroup.style.display = 'none';
                document.getElementById('maxDiscountAmount').value = '';
                valueLabel.innerText = "Mức Giảm (đ)";
                discountValue.placeholder = "Ví dụ: 20000";
                discountValue.removeAttribute('max');
            }
        }

        // Validate ngày bắt đầu và ngày kết thúc
        function validateForm() {
            const start = new Date(document.getElementById('startDate').value);
            const end = new Date(document.getElementById('endDate').value);
            
            if (end <= start) {
                alert("Ngày hết hạn phải xảy ra sau ngày hiệu lực!");
                return false;
            }
            return true;
        }
    </script>
</body>
</html>
