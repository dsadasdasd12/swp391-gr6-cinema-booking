<%@page contentType="text/html; charset=UTF-8" pageEncoding="UTF-8"%>
<%@taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<%@taglib prefix="fmt" uri="http://java.sun.com/jsp/jstl/fmt" %>
<c:set var="ctx" value="${pageContext.request.contextPath}" />
<c:set var="topUser" value="${sessionScope.user}" />
<!DOCTYPE html>
<html lang="vi">
<head>
    <meta charset="UTF-8">
    <meta name="viewport" content="width=device-width, initial-scale=1">
    <title>Cấu hình ghế - Rạp Việt CMS</title>

    <link href="https://cdn.jsdelivr.net/npm/bootstrap@5.3.3/dist/css/bootstrap.min.css"
          rel="stylesheet"
          integrity="sha384-QWTKZyjpPEjISv5WaRU9OFeRpok6YctnYmDr5pNlyT2bRjXh0JMhjY6hW+ALEwIH"
          crossorigin="anonymous">

    <link href="https://cdn.jsdelivr.net/npm/bootstrap-icons@1.11.3/font/bootstrap-icons.css"
          rel="stylesheet">

    <link href="https://fonts.googleapis.com/css2?family=Inter:wght@400;500;600;700&family=Outfit:wght@600;700;800&display=swap"
          rel="stylesheet">

    <link rel="stylesheet" href="${ctx}/assets/css/admin/variables.css?v=redblack">
    <link rel="stylesheet" href="${ctx}/assets/css/admin/base.css?v=redblack">
    <link rel="stylesheet" href="${ctx}/assets/css/admin/layout.css?v=redblack">
    <link rel="stylesheet" href="${ctx}/assets/css/admin/components.css?v=redblack">
    <link rel="stylesheet" href="${ctx}/assets/css/admin/tables.css?v=redblack">
    <link rel="stylesheet" href="${ctx}/assets/css/admin/forms.css?v=redblack">
    <link rel="stylesheet" href="${ctx}/assets/css/manager/movie-management.css?v=1">

    <script src="${ctx}/assets/js/main.js" charset="UTF-8" defer></script>
    <script src="https://cdn.jsdelivr.net/npm/bootstrap@5.3.3/dist/js/bootstrap.bundle.min.js"
            integrity="sha384-YvpcrYf0tY3lHB60NNkmXc5s9fDVZLESaAA55NDzOxhy9GkcIdslK1eN7N6jIeHz"
            crossorigin="anonymous" defer></script>

    <style>
        /* === Seat diagram styles (unchanged from original) === */
        .screen-container { display: flex; flex-direction: column; align-items: center; background: #0b0f19; padding: 20px; border-radius: 8px; }
        .grid-layout { display: grid; gap: 10px; margin-top: 20px; }
        .seat-btn { width: 45px; height: 45px; border: none; border-radius: 4px; color: white; font-weight: bold; cursor: pointer; text-align: center; line-height: 45px; transition: transform 0.2s; background-color: #8b5cf6; }
        .seat-btn:hover { transform: scale(1.1); }
        .MAINTENANCE { background-color: #4b5563 !important; }
        /* Dynamic seat type colors */
        <c:forEach items="${allSeatTypes}" var="st">
        .${st.code} { background-color: ${st.color} !important; }
        </c:forEach>
        .seat-config-row { display: flex; gap: 24px; margin-top: 0; }
        .config-col-side { width: 25%; min-width: 200px; }
        .config-col-main { flex: 1; }
        @media (max-width: 1024px) {
            .seat-config-row { flex-direction: column; }
            .config-col-side, .config-col-main { width: 100%; }
        }
        /* === Seat page card overrides === */
        .seat-panel {
            background: var(--rv-card-bg, #1a1f2e);
            border: 1px solid var(--rv-border, rgba(255,255,255,0.07));
            border-radius: 12px;
            overflow: hidden;
            height: 100%;
        }
        .seat-panel__header {
            padding: 14px 20px;
            background: rgba(255,255,255,0.03);
            border-bottom: 1px solid var(--rv-border, rgba(255,255,255,0.07));
            font-size: 12px;
            font-weight: 700;
            text-transform: uppercase;
            letter-spacing: 1px;
            color: #94a3b8;
        }
        .seat-panel__body { padding: 16px; }
        /* seat type list */
        .seat-type-badge {
            padding: 12px 16px;
            border-radius: 8px;
            font-weight: 700;
            color: white;
            display: flex;
            justify-content: space-between;
            align-items: center;
            border: 1px solid rgba(255,255,255,0.15);
            margin-bottom: 8px;
        }
        /* hall selector bar */
        .seat-toolbar {
            background: var(--rv-card-bg, #1a1f2e);
            border: 1px solid var(--rv-border, rgba(255,255,255,0.07));
            border-radius: 12px;
            padding: 16px 20px;
            display: flex;
            align-items: center;
            justify-content: space-between;
            gap: 16px;
            flex-wrap: wrap;
            margin-bottom: 20px;
        }
        .seat-toolbar label {
            font-weight: 600;
            font-size: 13px;
            color: #94a3b8;
            margin: 0;
            text-transform: uppercase;
            letter-spacing: 0.5px;
        }
        .seat-toolbar select {
            padding: 8px 14px;
            background: rgba(255,255,255,0.05);
            color: white;
            border: 1px solid rgba(255,255,255,0.12);
            border-radius: 8px;
            font-size: 14px;
            font-weight: 600;
            cursor: pointer;
            outline: none;
        }
        .seat-toolbar select:focus { border-color: var(--rv-accent, #e63946); }
        /* form inside side panel */
        .seat-form-label {
            display: block;
            font-weight: 600;
            font-size: 12px;
            color: #94a3b8;
            text-transform: uppercase;
            letter-spacing: 0.5px;
            margin-bottom: 6px;
        }
        .seat-form-input, .seat-form-select {
            width: 100%;
            padding: 10px 14px;
            background: rgba(255,255,255,0.05);
            color: white;
            border: 1px solid rgba(255,255,255,0.1);
            border-radius: 8px;
            box-sizing: border-box;
            font-size: 14px;
            font-family: inherit;
        }
        .seat-form-input:focus, .seat-form-select:focus {
            outline: none;
            border-color: var(--rv-accent, #e63946);
            box-shadow: 0 0 0 3px rgba(230,57,70,0.15);
        }
        /* Custom Bulk Add Modal */
        .custom-modal {
            display: none;
            position: fixed;
            z-index: 9999;
            top: 0; left: 0;
            width: 100%; height: 100%;
            background: rgba(3, 7, 18, 0.85);
            backdrop-filter: blur(8px);
            align-items: center;
            justify-content: center;
        }
        .custom-modal-content {
            background: #111827;
            border: 1px solid #374151;
            border-radius: 12px;
            width: 90%; max-width: 500px;
            box-shadow: 0 25px 50px -12px rgba(0,0,0,0.5);
            overflow: hidden;
            display: flex;
            flex-direction: column;
            animation: modalFadeIn 0.25s ease-out;
        }
        @keyframes modalFadeIn {
            from { opacity: 0; transform: translateY(-20px); }
            to   { opacity: 1; transform: translateY(0); }
        }
        .custom-modal-header { padding: 20px 24px; border-bottom: 1px solid #1f2937; display: flex; justify-content: space-between; align-items: center; }
        .custom-modal-title  { font-size: 1.25rem; font-weight: bold; color: #fff; margin: 0; display: flex; align-items: center; gap: 8px; }
        .custom-modal-close  { background: none; border: none; color: #9ca3af; font-size: 1.5rem; cursor: pointer; line-height: 1; padding: 0; }
        .custom-modal-close:hover { color: #fff; }
        .custom-modal-body   { padding: 24px; max-height: 70vh; overflow-y: auto; }
        .custom-modal-footer { padding: 16px 24px; border-top: 1px solid #1f2937; display: flex; justify-content: flex-end; gap: 12px; background: #0f172a; }
        .custom-form-group   { margin-bottom: 18px; }
        .custom-label        { display: block; font-weight: bold; margin-bottom: 6px; color: #a5b4fc; font-size: 0.9rem; }
        .custom-input        { width: 100%; padding: 10px; background: #1f2937; color: white; border: 1px solid #4b5563; border-radius: 6px; box-sizing: border-box; font-size: 0.95rem; }
        .custom-input:focus  { outline: none; border-color: #10b981; box-shadow: 0 0 0 3px rgba(16,185,129,0.2); }

        /* === Sidebar info card === */
        .manager-sidebar-info {
            display: flex;
            flex-direction: column;
            gap: 3px;
            margin: 0 16px 16px;
            padding: 14px;
            border: 1px solid rgba(255, 255, 255, 0.08);
            border-radius: 10px;
            background: linear-gradient(
                145deg,
                rgba(229, 9, 20, 0.18),
                rgba(255, 255, 255, 0.02)
            );
            color: rgba(255, 255, 255, 0.76);
            font-size: 12px;
            line-height: 1.5;
        }
        .manager-sidebar-info__label {
            color: rgba(255, 255, 255, 0.42);
            font-size: 10px;
            font-weight: 700;
            letter-spacing: 0.08em;
        }
        .manager-sidebar-info strong {
            color: #ffffff;
            font-size: 14px;
        }
        /* === Topbar context === */
        .manager-topbar-context {
            display: inline-flex;
            align-items: center;
            gap: 6px;
            padding-right: 16px;
            border-right: 1px solid rgba(255,255,255,0.1);
            color: #94a3b8;
            font-size: 13px;
        }
        .manager-topbar-context i { color: #e63946; }
    </style>
</head>
<body class="manager-seat-config-page">

<header class="rv-topbar">
    <button type="button" class="rv-topbar__toggle" title="Mở menu" aria-label="Mở menu">
        <i class="bi bi-list"></i>
    </button>

    <a class="rv-topbar__brand" href="${ctx}/manager/dashboard">
        <div class="rv-topbar__brand-icon"><i class="bi bi-film"></i></div>
        <span class="rv-topbar__brand-text">RẠP VIỆT <span>CMS</span></span>
    </a>

    <div class="rv-topbar__actions">
        <div class="manager-topbar-context">
            <i class="bi bi-building"></i>
            <span>Phân hệ Quản lý chi nhánh</span>
        </div>

        <div class="rv-topbar__avatar">
            <c:choose>
                <c:when test="${not empty topUser and not empty topUser.fullName}">
                    <c:out value="${topUser.fullName.substring(0, 1).toUpperCase()}" />
                </c:when>
                <c:otherwise>M</c:otherwise>
            </c:choose>
        </div>

        <div class="rv-topbar__user-info">
            <span class="rv-topbar__user-name">
                <c:out value="${not empty topUser ? topUser.fullName : 'Branch Manager'}" />
            </span>
            <span class="rv-topbar__user-role">Quản lý chi nhánh</span>
        </div>

        <i class="bi bi-chevron-down rv-topbar__user-arrow"></i>

        <div class="rv-topbar__dropdown">
            <div class="rv-topbar__dropdown-header">
                <div class="manager-dropdown-name">
                    <c:out value="${not empty topUser ? topUser.fullName : 'Branch Manager'}" />
                </div>
                <div class="email">
                    <c:out value="${not empty topUser ? topUser.email : ''}" />
                </div>
            </div>
            <a href="${ctx}/manager/dashboard" class="rv-topbar__dropdown-item">
                <i class="bi bi-grid-1x2-fill"></i> Bảng điều khiển
            </a>
            <div class="rv-topbar__dropdown-divider"></div>
            <a href="${ctx}/logout" class="rv-topbar__dropdown-item danger">
                <i class="bi bi-box-arrow-right"></i> Đăng xuất
            </a>
        </div>
    </div>
</header>

<div class="rv-wrapper">

    <aside class="rv-sidebar">
        <div class="manager-sidebar-info">
            <span class="manager-sidebar-info__label">PHÂN HỆ</span>
            <strong>Branch Manager</strong>
            <span>Quản lý vận hành chi nhánh được phân công</span>
        </div>

        <div class="rv-nav__group">
            <a href="${ctx}/manager/dashboard" class="rv-nav__item">
                <i class="bi bi-grid-1x2-fill"></i> Bảng điều khiển
            </a>
        </div>

        <div class="rv-nav__label">Vận hành chi nhánh</div>

        <div class="rv-nav__group">
            <a href="${ctx}/manager/halls" class="rv-nav__item">
                <i class="bi bi-door-open-fill"></i> Quản lý phòng chiếu
            </a>
        </div>

        <div class="rv-nav__group">
            <a href="${ctx}/manager/seat-config" class="rv-nav__item active">
                <i class="bi bi-grid-3x3-gap-fill"></i> Cấu hình ghế
            </a>
        </div>

        <div class="rv-nav__group open">
            <div class="rv-nav__item" role="button" tabindex="0">
                <i class="bi bi-film"></i> Phân bổ phim
                <i class="bi bi-chevron-right rv-nav__arrow"></i>
            </div>
            <div class="rv-nav__sub">
                <a href="${ctx}/manager/movie-assignments/branches" class="rv-nav__sub-item">Phim tại chi nhánh</a>
                <a href="${ctx}/manager/movie-assignments/halls" class="rv-nav__sub-item">Phim tại phòng chiếu</a>
                <a href="${ctx}/manager/movie-durations" class="rv-nav__sub-item">Thời lượng phim</a>
            </div>
        </div>

        <div class="rv-nav__group">
            <a href="${ctx}/manager/showtimesmanagement" class="rv-nav__item">
                <i class="bi bi-calendar-week-fill"></i> Quản lý lịch chiếu
            </a>
        </div>

        <div class="rv-nav__group">
            <a href="${ctx}/logout" class="rv-nav__item logout">
                <i class="bi bi-box-arrow-right"></i> Đăng xuất
            </a>
        </div>
    </aside>

    <main class="rv-main">

        <div class="rv-page-header">
            <div class="rv-page-header__left">
                <div class="rv-breadcrumb">
                    <a href="${ctx}/manager/dashboard">Quản lý chi nhánh</a>
                    <i class="bi bi-chevron-right rv-breadcrumb__sep"></i>
                    <span class="rv-breadcrumb__current">Cấu hình ghế</span>
                </div>
                <h1 class="rv-page-title">Cấu hình sơ đồ ghế</h1>
                <p class="rv-page-subtitle">Thiết lập vị trí, loại ghế và trạng thái vận hành của phòng chiếu</p>
            </div>
            <div class="rv-page-header__right">
                <a class="rv-btn rv-btn--ghost" href="${ctx}/manager/dashboard">
                    <i class="bi bi-arrow-left"></i> Quay lại Dashboard
                </a>
            </div>
        </div>

        <%-- Alert Messages --%>
        <c:if test="${not empty sessionScope.msgSuccess}">
            <div style="background: rgba(16,185,129,0.15); color: #10b981; border: 1px solid rgba(16,185,129,0.3); padding: 15px; border-radius: 8px; margin-bottom: 20px; display:flex; align-items:center; gap:10px;">
                <i class="bi bi-check-circle-fill"></i>
                <span>${sessionScope.msgSuccess}</span>
                <c:remove var="msgSuccess" scope="session" />
            </div>
        </c:if>
        <c:if test="${not empty sessionScope.msgError}">
            <div style="background: rgba(239,68,68,0.15); color: #ef4444; border: 1px solid rgba(239,68,68,0.3); padding: 15px; border-radius: 8px; margin-bottom: 20px; display:flex; align-items:center; gap:10px;">
                <i class="bi bi-exclamation-circle-fill"></i>
                <span>${sessionScope.msgError}</span>
                <c:remove var="msgError" scope="session" />
            </div>
        </c:if>

        <c:if test="${isLocked}">
            <div style="background: rgba(245,158,11,0.15); color: #f59e0b; border: 1px solid rgba(245,158,11,0.3); padding: 15px; border-radius: 8px; margin-bottom: 20px; font-weight: 500; display: flex; align-items: center; gap: 10px;">
                <i class="bi bi-lock-fill"></i>
                <span><strong>Lưu ý:</strong> Sơ đồ phòng chiếu này đã được khóa chỉnh sửa do đang có suất chiếu chưa diễn ra. Bạn chỉ được xem cấu hình hiện tại.</span>
            </div>
        </c:if>

        <div class="seat-toolbar">
            <div style="display:flex; align-items:center; gap:14px;">
                <label>Chọn Phòng Chiếu:</label>
                <select onchange="location.href='seat-config?hallId=' + this.value">
                    <c:forEach items="${hallList}" var="h">
                        <option value="${h.id}" ${h.id == currentHallId ? 'selected' : ''}>
                            ${h.name} (Tổng ${h.totalSeats} ghế)
                        </option>
                    </c:forEach>
                </select>
            </div>
            <c:if test="${!isLocked && currentHallId > 0}">
                <div style="display:flex; gap:10px; align-items:center;">
                    <button type="button" onclick="openBulkAddModal()" class="rv-btn rv-btn--success" style="display:flex;align-items:center;gap:8px;">
                        <i class="bi bi-magic"></i> Thêm nhanh hàng loạt
                    </button>
                    <form action="seat-config" method="POST"
                          onsubmit="return confirm('CẢNH BÁO: Hành động này sẽ xóa TOÀN BỘ ghế hiện có của phòng chiếu này! Bạn chắc chắn chứ?');"
                          style="margin:0;">
                        <input type="hidden" name="hallId" value="${currentHallId}">
                        <input type="hidden" name="action" value="clearAll">
                        <button type="submit" class="rv-btn rv-btn--danger" style="display:flex;align-items:center;gap:8px;">
                            <i class="bi bi-trash3-fill"></i> Xóa toàn bộ sơ đồ ghế
                        </button>
                    </form>
                </div>
            </c:if>
        </div>

        <div class="seat-config-row">

            <%-- CỘT TRÁI: LOẠI GHẾ --%>
            <div class="config-col-side">
                <div class="seat-panel">
                    <div class="seat-panel__header">1. Loại ghế hiện có</div>
                    <div class="seat-panel__body">
                        <c:forEach items="${allSeatTypes}" var="st">
                            <c:if test="${st.status == 'ACTIVE'}">
                                <div class="seat-type-badge" style="background-color: ${st.color};">
                                    <span>${st.name}</span>
                                    <span style="font-size:0.82em; opacity:0.9;">
                                        <fmt:formatNumber value="${st.defaultPrice}" pattern="#,##0"/>đ
                                    </span>
                                </div>
                            </c:if>
                        </c:forEach>
                        <div class="seat-type-badge" style="background:#4b5563;">
                            <span>Maintenance (Bảo Trì)</span>
                        </div>
                    </div>
                </div>
            </div>

            <%-- CỘT GIỮA: SƠ ĐỒ PHÒNG CHIẾU --%>
            <div class="config-col-main">
                <div class="seat-panel" style="height:100%;">
                    <div class="seat-panel__header">Sơ đồ phòng chiếu</div>
                    <div class="seat-panel__body">
                        <div class="screen-container">
                            <div style="background:#374151; width:80%; text-align:center; padding:6px 0; border-radius:0 0 15px 15px; font-weight:bold; font-size:13px; color:#d1d5db; text-transform:uppercase; letter-spacing:2px; margin-bottom:20px;">MÀN HÌNH CHIẾU (SCREEN)</div>
                            <div class="grid-wrapper" style="width:100%; overflow-x:auto; padding-bottom:10px;">
                                <div class="grid-layout" style="grid-template-columns: repeat(${maxSeatNumber}, 45px); margin: 20px auto 0 auto; width:fit-content;">
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

            <%-- CỘT PHẢI: FORM SỬA GHẾ --%>
            <div class="config-col-side">
                <div class="seat-panel">
                    <div class="seat-panel__header">2. Sửa thông tin ghế</div>
                    <div class="seat-panel__body">
                        <c:choose>
                            <c:when test="${isLocked}">
                                <div style="color:#9ca3af; text-align:center; padding:20px 0; font-style:italic;">
                                    Đã khóa chỉnh sửa sơ đồ phòng chiếu
                                </div>
                            </c:when>
                            <c:otherwise>
                                <form action="seat-config" method="POST">
                                    <input type="hidden" name="hallId" value="${currentHallId}">

                                    <div style="margin-bottom:14px;">
                                        <label class="seat-form-label">Mã vị trí ghế (VD: B7, C1)</label>
                                        <input type="text" id="formSeatCode" name="seatCode"
                                               placeholder="Click ghế hoặc tự gõ mã..."
                                               class="seat-form-input">
                                    </div>

                                    <div style="margin-bottom:14px;">
                                        <label class="seat-form-label">Loại ghế</label>
                                        <select id="formSeatType" name="seatType" class="seat-form-select">
                                            <c:forEach items="${activeSeatTypes}" var="st">
                                                <option value="${st.code}">${st.name}</option>
                                            </c:forEach>
                                        </select>
                                    </div>

                                    <div style="margin-bottom:20px;">
                                        <label class="seat-form-label">Trạng thái vận hành</label>
                                        <select id="formStatus" name="status" class="seat-form-select">
                                            <option value="AVAILABLE">AVAILABLE (Hoạt động)</option>
                                            <option value="MAINTENANCE">MAINTENANCE (Bảo trì)</option>
                                        </select>
                                    </div>

                                    <div style="display:flex; gap:10px; margin-bottom:10px;">
                                        <button type="submit" name="action" value="update"
                                                class="rv-btn rv-btn--primary" style="flex:1;">
                                            <i class="bi bi-pencil-fill"></i> Cập nhật
                                        </button>
                                        <button type="submit" name="action" value="add"
                                                class="rv-btn rv-btn--success" style="flex:1;">
                                            <i class="bi bi-plus-lg"></i> Thêm ghế
                                        </button>
                                    </div>
                                    <button type="submit" name="action" value="delete"
                                            class="rv-btn rv-btn--danger" style="width:100%;"
                                            onclick="return confirm('Bạn chắc chắn muốn xóa ghế này?')">
                                        <i class="bi bi-trash3-fill"></i> Xóa khỏi sơ đồ
                                    </button>
                                </form>
                            </c:otherwise>
                        </c:choose>
                    </div>
                </div>
            </div>

        </div><%-- seat-config-row --%>

    </main>

</div><%-- rv-wrapper --%>


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
