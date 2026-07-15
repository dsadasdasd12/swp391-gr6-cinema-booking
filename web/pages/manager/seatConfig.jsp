<%@page contentType="text/html; charset=UTF-8" pageEncoding="UTF-8"%>
<%@taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<%@taglib prefix="fmt" uri="http://java.sun.com/jsp/jstl/fmt" %>
<c:set var="ctx" value="${pageContext.request.contextPath}" />
<!DOCTYPE html>
<html lang="vi">
<head>
    <meta charset="UTF-8">
    <title>Cấu hình ghế - RapViet Cinema</title>
    <link rel="stylesheet" href="${ctx}/assets/css/style.css">
    <link rel="stylesheet" href="${ctx}/assets/css/admin.css">
    <style>
        .screen-container { display: flex; flex-direction: column; align-items: center; background: #0b0f19; padding: 20px; border-radius: 8px;}
        .grid-layout { display: grid; gap: 10px; margin-top: 20px; }
        .seat-btn { width: 45px; height: 45px; border: none; border-radius: 4px; color: white; font-weight: bold; cursor: pointer; text-align: center; line-height: 45px; transition: transform 0.2s; background-color: #8b5cf6;}
        .seat-btn:hover { transform: scale(1.1); }
        .MAINTENANCE { background-color: #4b5563 !important; } /* Xám */
        
        /* Dynamic seat type background colors */
        <c:forEach items="${allSeatTypes}" var="st">
        .${st.code} { background-color: ${st.color} !important; }
        </c:forEach>
        
        .seat-config-row {
            display: flex;
            gap: 24px;
            margin-top: 24px;
        }
        .config-col-side {
            width: 25%;
        }
        .config-col-main {
            width: 50%;
        }
        @media (max-width: 1024px) {
            .seat-config-row {
                flex-direction: column;
            }
            .config-col-side, .config-col-main {
                width: 100%;
            }
        }

        /* Custom Vanilla Modal Style for Bulk Add */
        .custom-modal {
            display: none;
            position: fixed;
            z-index: 9999;
            top: 0;
            left: 0;
            width: 100%;
            height: 100%;
            background: rgba(3, 7, 18, 0.85);
            backdrop-filter: blur(8px);
            align-items: center;
            justify-content: center;
        }
        .custom-modal-content {
            background: #111827;
            border: 1px solid #374151;
            border-radius: 12px;
            width: 90%;
            max-width: 500px;
            box-shadow: 0 25px 50px -12px rgba(0, 0, 0, 0.5);
            overflow: hidden;
            display: flex;
            flex-direction: column;
            animation: modalFadeIn 0.25s ease-out;
        }
        @keyframes modalFadeIn {
            from { opacity: 0; transform: translateY(-20px); }
            to { opacity: 1; transform: translateY(0); }
        }
        .custom-modal-header {
            padding: 20px 24px;
            border-bottom: 1px solid #1f2937;
            display: flex;
            justify-content: space-between;
            align-items: center;
        }
        .custom-modal-title {
            font-size: 1.25rem;
            font-weight: bold;
            color: #ffffff;
            margin: 0;
            display: flex;
            align-items: center;
            gap: 8px;
        }
        .custom-modal-close {
            background: none;
            border: none;
            color: #9ca3af;
            font-size: 1.5rem;
            cursor: pointer;
            line-height: 1;
            padding: 0;
        }
        .custom-modal-close:hover {
            color: #ffffff;
        }
        .custom-modal-body {
            padding: 24px;
            max-height: 70vh;
            overflow-y: auto;
        }
        .custom-modal-footer {
            padding: 16px 24px;
            border-top: 1px solid #1f2937;
            display: flex;
            justify-content: flex-end;
            gap: 12px;
            background: #0f172a;
        }
        .custom-form-group {
            margin-bottom: 18px;
        }
        .custom-label {
            display: block;
            font-weight: bold;
            margin-bottom: 6px;
            color: #a5b4fc;
            font-size: 0.9rem;
            text-transform: none;
        }
        .custom-input {
            width: 100%;
            padding: 10px;
            background: #1f2937;
            color: white;
            border: 1px solid #4b5563;
            border-radius: 6px;
            box-sizing: border-box;
            font-size: 0.95rem;
        }
        .custom-input:focus {
            outline: none;
            border-color: #10b981;
            box-shadow: 0 0 0 3px rgba(16, 185, 129, 0.2);
        }
    </style>
</head>
<body>
<div class="admin-shell">

    <%-- SIDEBAR --%>
    <aside class="admin-sidebar">
        <div class="admin-brand">
            RAPVIET SYSTEM
        </div>

        <div class="admin-role">
            <p>Phân hệ</p>
            <strong>Manager Dashboard</strong>
            <span>Quyền: Branch Manager</span>
        </div>

        <nav class="admin-menu">
            <a href="${ctx}/manager/dashboard">Dashboard</a>
            <a href="${ctx}/manager/halls">Quản lý phòng chiếu</a>
            <a class="active" href="${ctx}/manager/seat-config">Cấu hình ghế</a>
            <a href="${ctx}/manager/showtimesmanagement">Quản lý lịch chiếu</a>
            <a href="${ctx}/manager/movie-assignments/branches">Phim tại chi nhánh</a>
            <a href="${ctx}/manager/movie-assignments/halls">Phim tại phòng chiếu</a>
            <a href="${ctx}/manager/movie-durations">Quản lý thời lượng phim</a>

            <a href="${ctx}/logout">Đăng xuất</a>
        </nav>
    </aside>

    <%-- MAIN CONTENT --%>
    <main class="admin-main">

        <div class="admin-topbar">
            <div>
                <strong>Cấu hình sơ đồ ghế</strong>
                <span>Thiết lập vị trí, loại ghế và trạng thái vận hành của phòng chiếu</span>
            </div>
        </div>

        <section class="admin-content">
            
            <%-- Alert Messages --%>
            <c:if test="${not empty sessionScope.msgSuccess}">
                <div class="alert alert-success alert-dismissible fade show" style="background: rgba(16,185,129,0.15); color: #10b981; border: 1px solid rgba(16,185,129,0.3); padding: 15px; border-radius: 6px; margin-bottom: 20px; position: relative;">
                    ${sessionScope.msgSuccess}
                    <c:remove var="msgSuccess" scope="session" />
                </div>
            </c:if>
            <c:if test="${not empty sessionScope.msgError}">
                <div class="alert alert-danger alert-dismissible fade show" style="background: rgba(239,68,68,0.15); color: #ef4444; border: 1px solid rgba(239,68,68,0.3); padding: 15px; border-radius: 6px; margin-bottom: 20px; position: relative;">
                    ${sessionScope.msgError}
                    <c:remove var="msgError" scope="session" />
                </div>
            </c:if>

            <%-- Lock State Warning --%>
            <c:if test="${isLocked}">
                <div style="background: rgba(245,158,11,0.15); color: #f59e0b; border: 1px solid rgba(245,158,11,0.3); padding: 15px; border-radius: 6px; margin-bottom: 20px; font-weight: 500; display: flex; align-items: center; gap: 10px;">
                    <i class="bi bi-exclamation-triangle-fill"></i>
                    <span><strong>Lưu ý:</strong> Sơ đồ phòng chiếu này đã được khóa chỉnh sửa do đang có suất chiếu chưa diễn ra. Bạn chỉ được xem cấu hình hiện tại.</span>
                </div>
            </c:if>

            <div class="panel" style="margin-bottom: 24px;">
                <div class="panel-body" style="display: flex; align-items: center; gap: 15px; justify-content: space-between;">
                    <div style="display: flex; align-items: center; gap: 15px;">
                        <label style="font-weight: bold; font-size: 15px; color: #a5b4fc; margin: 0;">Chọn Phòng Chiếu:</label>
                        <select onchange="location.href='seat-config?hallId=' + this.value" style="padding: 8px 12px; background: #1f2937; color: white; border: 1px solid #4b5563; border-radius: 6px; font-weight: bold; font-size: 14px; cursor: pointer; outline: none;">
                            <c:forEach items="${hallList}" var="h">
                                <option value="${h.id}" ${h.id == currentHallId ? 'selected' : ''}>
                                    ${h.name} (Tổng ${h.totalSeats} ghế)
                                </option>
                            </c:forEach>
                        </select>
                    </div>
                    
                    <c:if test="${!isLocked && currentHallId > 0}">
                        <div style="display: flex; gap: 10px; align-items: center;">
                            <button type="button" onclick="openBulkAddModal()" style="background: #10b981; color: white; border: none; font-weight: bold; cursor: pointer; padding: 10px 20px; border-radius: 6px; display: flex; align-items: center; gap: 8px; transition: background-color 0.2s; font-size: 0.95em;">
                                <i class="bi bi-magic"></i> Thêm nhanh hàng loạt
                            </button>
                            
                            <form action="seat-config" method="POST" onsubmit="return confirm('CẢNH BÁO: Hành động này sẽ xóa TOÀN BỘ ghế hiện có của phòng chiếu này! Bạn chắc chắn chứ?');" style="margin: 0;">
                                <input type="hidden" name="hallId" value="${currentHallId}">
                                <input type="hidden" name="action" value="clearAll">
                                <button type="submit" style="background: #dc2626; color: white; border: none; font-weight: bold; cursor: pointer; padding: 10px 20px; border-radius: 6px; display: flex; align-items: center; gap: 8px; transition: background-color 0.2s; font-size: 0.95em;">
                                    <i class="bi bi-trash3-fill"></i> Xóa toàn bộ sơ đồ ghế
                                </button>
                            </form>
                        </div>
                    </c:if>
                </div>
            </div>

            <div class="seat-config-row">
                
                <%-- DANH MỤC LOẠI GHẾ (ĐỌC) --%>
                <div class="config-col-side">
                    <div class="panel" style="height: 100%;">
                        <div class="panel-header">1. LOẠI GHẾ HIỆN CÓ</div>
                        <div class="panel-body" style="display: flex; flex-direction: column; gap: 10px;">
                            <c:forEach items="${allSeatTypes}" var="st">
                                <c:if test="${st.status == 'ACTIVE'}">
                                    <div style="padding: 12px; background-color: ${st.color}; border-radius: 6px; font-weight: bold; text-align: center; color: white; display: flex; justify-content: space-between; align-items: center; border: 1px solid rgba(255,255,255,0.15);">
                                        <span>${st.name}</span>
                                        <span style="font-size: 0.85em; opacity: 0.9;"><fmt:formatNumber value="${st.defaultPrice}" pattern="#,##0"/>đ</span>
                                    </div>
                                </c:if>
                            </c:forEach>
                            <div style="padding: 12px; background: #4b5563; border-radius: 6px; font-weight: bold; text-align: center; color: white; border: 1px solid rgba(255,255,255,0.15);">
                                Maintenance (Bảo Trì)
                            </div>
                        </div>
                    </div>
                </div>

                <%-- SƠ ĐỒ MÀN HÌNH --%>
                <div class="config-col-main">
                    <div class="panel" style="height: 100%;">
                        <div class="panel-header">SƠ ĐỒ PHÒNG CHIẾU</div>
                        <div class="panel-body">
                            <div class="screen-container">
                                <div style="background: #374151; width: 80%; text-align: center; padding: 6px 0; border-radius: 0 0 15px 15px; font-weight: bold; font-size: 13px; color: #d1d5db; text-transform: uppercase; letter-spacing: 2px; margin-bottom: 20px;">MÀN HÌNH CHIẾU (SCREEN)</div>
                                
                                <div class="grid-wrapper" style="width: 100%; overflow-x: auto; padding-bottom: 10px;">
                                    <div class="grid-layout" style="grid-template-columns: repeat(${maxSeatNumber}, 45px); margin: 20px auto 0 auto; width: fit-content;">
                                        <c:forEach items="${seatList}" var="s">
                                            <button class="seat-btn ${s.maintenance ? 'MAINTENANCE' : s.seatType}" 
                                                    style="grid-column: ${s.seatNumber}; grid-row: ${s.getRowIndex()};"
                                                    onclick="selectSeat('${s.getSeatCode()}', '${s.seatType}', '${s.maintenance ? 'MAINTENANCE' : 'AVAILABLE'}')">
                                                ${s.getSeatCode()}
                                            </button>
                                        </c:forEach>
                                    </div>
                                </div>
                            </div>
                        </div>
                    </div>
                </div>

                <%-- THAO TÁC SƠ ĐỒ (SỬA & THÊM HÀNG LOẠT) --%>
                <div class="config-col-side">
                    <%-- FORM SỬA THÔNG TIN GHẾ --%>
                    <div class="panel">
                        <div class="panel-header">2. SỬA THÔNG TIN GHẾ</div>
                        <div class="panel-body">
                            <c:choose>
                                <c:when test="${isLocked}">
                                    <div style="color: #9ca3af; text-align: center; padding: 20px 0; font-style: italic;">
                                        Đã khóa chỉnh sửa sơ đồ phòng chiếu
                                    </div>
                                </c:when>
                                <c:otherwise>
                                    <form action="seat-config" method="POST">
                                        <input type="hidden" name="hallId" value="${currentHallId}">
                                        
                                        <div class="form-group" style="margin-bottom: 15px;">
                                            <label style="display: block; font-weight: bold; margin-bottom: 6px; color: #a5b4fc;">Mã Vị Trí Ghế (Ví dụ: B7, C1):</label>
                                            <input type="text" id="formSeatCode" name="seatCode" placeholder="Click ghế hoặc tự gõ mã..." style="width: 100%; padding: 10px; background: #1f2937; color: white; border: 1px solid #4b5563; border-radius: 6px; box-sizing: border-box;">
                                        </div>

                                        <div class="form-group" style="margin-bottom: 15px;">
                                            <label style="display: block; font-weight: bold; margin-bottom: 6px; color: #a5b4fc;">Thay Đổi Loại Ghế:</label>
                                            <select id="formSeatType" name="seatType" style="width: 100%; padding: 10px; background: #1f2937; color: white; border: 1px solid #4b5563; border-radius: 6px; box-sizing: border-box; cursor: pointer;">
                                                <c:forEach items="${activeSeatTypes}" var="st">
                                                    <option value="${st.code}">${st.name}</option>
                                                </c:forEach>
                                            </select>
                                        </div>

                                        <div class="form-group" style="margin-bottom: 15px;">
                                            <label style="display: block; font-weight: bold; margin-bottom: 6px; color: #a5b4fc;">Trạng Thái Vận Hành:</label>
                                            <select id="formStatus" name="status" style="width: 100%; padding: 10px; background: #1f2937; color: white; border: 1px solid #4b5563; border-radius: 6px; box-sizing: border-box; cursor: pointer;">
                                                <option value="AVAILABLE">AVAILABLE (Hoạt động)</option>
                                                <option value="MAINTENANCE">MAINTENANCE (Bảo trì)</option>
                                            </select>
                                        </div>

                                        <div style="margin-top: 20px; display: flex; flex-direction: column; gap: 10px;">
                                            <div style="display: flex; gap: 10px;">
                                                <button type="submit" name="action" value="update" class="btn" style="flex: 1; background: #2563eb; color: white; border: none; font-weight: bold; cursor: pointer; padding: 10px; border-radius: 4px;">
                                                    Cập Nhật
                                                </button>
                                                
                                                <button type="submit" name="action" value="add" class="btn" style="flex: 1; background: #10b981; color: white; border: none; font-weight: bold; cursor: pointer; padding: 10px; border-radius: 4px;">
                                                    Thêm Ghế
                                                </button>
                                            </div>
                                            
                                            <button type="submit" name="action" value="delete" class="btn" style="width: 100%; background: #dc2626; color: white; border: none; font-weight: bold; cursor: pointer; padding: 10px; border-radius: 4px;" onclick="return confirm('Bạn chắc chắn muốn xóa ghế này?')">
                                                Xóa Khỏi Sơ Đồ
                                            </button>
                                        </div>
                                    </form>
                                </c:otherwise>
                            </c:choose>
                        </div>
                    </div>
                </div>

            </div>

        </section>

    </main>

</div>

<script>
    function selectSeat(code, type, status) {
        var seatCodeInput = document.getElementById('formSeatCode');
        if (seatCodeInput) {
            seatCodeInput.value = code;
        }
        var seatTypeSelect = document.getElementById('formSeatType');
        if (seatTypeSelect) {
            seatTypeSelect.value = type;
        }
        var statusSelect = document.getElementById('formStatus');
        if (statusSelect) {
            statusSelect.value = status;
        }
    }

    function openBulkAddModal() {
        var modal = document.getElementById('bulkAddModal');
        if (modal) {
            modal.style.display = 'flex';
        }
    }

    function closeBulkAddModal() {
        var modal = document.getElementById('bulkAddModal');
        if (modal) {
            modal.style.display = 'none';
        }
    }

    // Close modal when clicking outside content
    window.onclick = function(event) {
        var modal = document.getElementById('bulkAddModal');
        if (event.target == modal) {
            modal.style.display = 'none';
        }
    }

    function toggleBulkFields(type) {
        var rowFields = document.querySelectorAll('.bulk-field-row');
        var colFields = document.querySelectorAll('.bulk-field-col');
        var rangeFields = document.querySelectorAll('.bulk-field-range');
        var countFields = document.querySelectorAll('.bulk-field-count');
        
        if (type === 'row') {
            rowFields.forEach(el => el.style.display = 'block');
            colFields.forEach(el => el.style.display = 'none');
            rangeFields.forEach(el => el.style.display = 'none');
            countFields.forEach(el => el.style.display = 'block');
        } 
        else if (type === 'col') {
            rowFields.forEach(el => el.style.display = 'none');
            colFields.forEach(el => el.style.display = 'block');
            rangeFields.forEach(el => el.style.display = 'block');
            countFields.forEach(el => el.style.display = 'none');
        }
        else if (type === 'grid') {
            rowFields.forEach(el => el.style.display = 'none');
            colFields.forEach(el => el.style.display = 'none');
            rangeFields.forEach(el => el.style.display = 'block');
            countFields.forEach(el => el.style.display = 'block');
        }
    }
</script>

<!-- Custom Modal: Thêm nhanh hàng loạt -->
<div id="bulkAddModal" class="custom-modal">
    <div class="custom-modal-content">
        <div class="custom-modal-header">
            <h5 class="custom-modal-title">
                <i class="bi bi-magic" style="color: #10b981;"></i> Thêm nhanh hàng loạt
            </h5>
            <button type="button" class="custom-modal-close" onclick="closeBulkAddModal()">&times;</button>
        </div>
        <form action="seat-config" method="POST">
            <input type="hidden" name="action" value="bulkAdd">
            <input type="hidden" name="hallId" value="${currentHallId}">
            
            <div class="custom-modal-body">
                <div class="custom-form-group">
                    <label class="custom-label">Cách Thêm (Bulk Type):</label>
                    <select name="bulkType" id="modalBulkType" onchange="toggleBulkFields(this.value)" class="custom-input" style="cursor: pointer;">
                        <option value="row">Thêm theo hàng (Row)</option>
                        <option value="col">Thêm theo cột (Column)</option>
                        <option value="grid">Thêm theo lưới (Grid)</option>
                    </select>
                </div>
                
                <!-- Field: Row Name (for row addition) -->
                <div class="custom-form-group bulk-field-row">
                    <label class="custom-label">Nhập chữ hàng (A-Z):</label>
                    <input type="text" name="bulkRow" placeholder="VD: E" maxLength="1" class="custom-input" style="text-transform: uppercase;">
                </div>
                
                <!-- Field: Column Number (for col addition) -->
                <div class="custom-form-group bulk-field-col" style="display: none;">
                    <label class="custom-label">Nhập số cột (Số nguyên > 0):</label>
                    <input type="number" name="bulkCol" placeholder="VD: 5" min="1" class="custom-input">
                </div>
                
                <!-- Field: Row Range (for col and grid addition) -->
                <div class="custom-form-group bulk-field-range" style="display: none;">
                    <label class="custom-label">Khoảng hàng (VD: A đến H):</label>
                    <div style="display: flex; gap: 10px; align-items: center;">
                        <input type="text" name="bulkRowStart" placeholder="Từ (A)" maxLength="1" class="custom-input" style="text-transform: uppercase; text-align: center;">
                        <span style="color: #9ca3af;">đến</span>
                        <input type="text" name="bulkRowEnd" placeholder="Đến (H)" maxLength="1" class="custom-input" style="text-transform: uppercase; text-align: center;">
                    </div>
                </div>
                
                <!-- Field: Seat Count (for row and grid addition) -->
                <div class="custom-form-group bulk-field-count">
                    <label class="custom-label">Số ghế mỗi hàng (VD: 10):</label>
                    <input type="number" name="bulkSeatCount" placeholder="VD: 10" min="1" value="10" class="custom-input">
                </div>
                
                <!-- Field: Seat Type -->
                <div class="custom-form-group">
                    <label class="custom-label">Loại Ghế:</label>
                    <select name="bulkSeatType" class="custom-input" style="cursor: pointer;">
                        <c:forEach items="${activeSeatTypes}" var="st">
                            <option value="${st.code}">${st.code} (${st.name})</option>
                        </c:forEach>
                    </select>
                </div>
                
                <!-- Field: Status -->
                <div class="custom-form-group">
                    <label class="custom-label">Trạng Thái Vận Hành:</label>
                    <select name="bulkStatus" class="custom-input" style="cursor: pointer;">
                        <option value="AVAILABLE">AVAILABLE (Hoạt động)</option>
                        <option value="MAINTENANCE">MAINTENANCE (Bảo trì)</option>
                    </select>
                </div>
            </div>
            
            <div class="custom-modal-footer">
                <button type="button" class="rv-btn rv-btn--secondary" onclick="closeBulkAddModal()" style="border: 1px solid #4b5563; background: transparent; color: #d1d5db;">Đóng</button>
                <button type="submit" class="rv-btn rv-btn--primary" style="background: #10b981;">Tạo hàng loạt</button>
            </div>
        </form>
    </div>
</div>
</body>
</html>
