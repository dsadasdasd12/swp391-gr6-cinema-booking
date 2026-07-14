<%@page contentType="text/html; charset=UTF-8" pageEncoding="UTF-8"%>
<%@taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<%@taglib prefix="fmt" uri="http://java.sun.com/jsp/jstl/fmt" %>
<c:set var="ctx" value="${pageContext.request.contextPath}" />

<c:choose>
    <%-- ========================================================
         1. ADMIN ROLE VIEW (USING ADMIN CMS LAYOUT)
         ======================================================== --%>
    <c:when test="${sessionScope.user.role == 'ADMIN'}">
        <c:set var="pageTitle" value="Quản lý mã giảm giá — Rạp Việt CMS" scope="request" />
        <%@ include file="/pages/shared/header-admin.jsp" %>
        <%@ include file="/pages/shared/sidebar-admin.jsp" %>
        
        <style>
            /* Scoped styling for Admin layout */
            :root {
                --glass-bg: #1e293b;
                --border-color: rgba(255, 255, 255, 0.08);
                --primary: #3b82f6;
                --muted-text: #9ca3af;
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
                color: #9ca3af;
                display: block;
                margin-top: 3px;
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
            .usage-container {
                width: 140px;
            }
            .usage-text {
                font-size: 12px;
                font-weight: 600;
                color: #fff;
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
                background: linear-gradient(90deg, #2563eb, #4f46e5);
            }
            .actions-cell {
                display: flex;
                gap: 8px;
            }
            .btn-action {
                background: rgba(255, 255, 255, 0.05);
                border: 1px solid rgba(255, 255, 255, 0.1);
                color: #9ca3af;
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
            
            /* Admin table overrides to clean up borders */
            .vouchers-card table {
                width: 100%;
                border-collapse: collapse;
            }
            .vouchers-card th {
                background: rgba(255, 255, 255, 0.02);
                padding: 16px 20px;
                font-size: 13px;
                font-weight: 700;
                color: #9ca3af;
                text-transform: uppercase;
                border-bottom: 1px solid rgba(255, 255, 255, 0.08);
            }
            .vouchers-card td {
                padding: 16px 20px;
                font-size: 14px;
                border-bottom: 1px solid rgba(255, 255, 255, 0.04);
                color: #e2e8f0;
                vertical-align: middle;
            }
            .vouchers-card {
                background: #1e293b;
                border: 1px solid rgba(255, 255, 255, 0.08);
                border-radius: 12px;
                overflow: hidden;
            }

            /* Custom Modal layout inside Admin page */
            .modal {
                display: none;
                position: fixed;
                z-index: 10000;
                top: 0;
                left: 0;
                width: 100%;
                height: 100%;
                background: rgba(15, 23, 42, 0.85);
                backdrop-filter: blur(12px);
                align-items: center;
                justify-content: center;
            }
            .modal-content {
                background: #1e293b;
                border: 1px solid rgba(255, 255, 255, 0.08);
                border-radius: 16px;
                width: 95%;
                max-width: 500px;
                padding: 30px;
                box-sizing: border-box;
                box-shadow: 0 25px 50px rgba(0,0,0,0.5);
                position: relative;
            }
            .modal-title {
                font-size: 20px;
                font-weight: 700;
                margin: 0 0 20px 0;
                color: #ffffff;
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
                color: #9ca3af;
                text-transform: uppercase;
                letter-spacing: 0.5px;
                margin-bottom: 6px;
            }
            input, select {
                background: #0f172a;
                border: 1px solid rgba(255, 255, 255, 0.08);
                border-radius: 8px;
                padding: 10px 14px;
                font-family: inherit;
                font-size: 14px;
                color: white;
                outline: none;
            }
            input:focus, select:focus {
                border-color: #3b82f6;
                box-shadow: 0 0 8px rgba(59, 130, 246, 0.2);
            }
            .modal-footer {
                display: flex;
                justify-content: flex-end;
                gap: 12px;
                margin-top: 15px;
            }
            .btn-modal-cancel {
                background: rgba(255,255,255,0.05);
                border: 1px solid rgba(255, 255, 255, 0.08);
                color: #9ca3af;
                padding: 10px 20px;
                border-radius: 8px;
                font-weight: 600;
                font-size: 14px;
                cursor: pointer;
            }
            .btn-modal-submit {
                background: #3b82f6;
                border: none;
                color: white;
                padding: 10px 24px;
                border-radius: 8px;
                font-weight: 700;
                font-size: 14px;
                cursor: pointer;
            }
        </style>
        
        <div class="rv-page-header">
            <div class="rv-page-header__left">
                <h1 class="rv-page-title">Hệ thống quản lý mã giảm giá</h1>
                <p class="rv-page-subtitle">Thiết lập, thêm mới và cập nhật trạng thái các chương trình khuyến mãi, voucher</p>
            </div>
            <div class="rv-page-header__right">
                <button class="rv-btn rv-btn--primary" onclick="openModal()" id="btnOpenAddModal">
                    <i class="bi bi-plus-lg"></i> Thêm Mã Giảm Giá
                </button>
            </div>
        </div>

        <div class="rv-card">
            <div class="rv-card__body p-0">
                <jsp:include page="/pages/manager/discountTableContent.jsp" />
            </div>
        </div>
        
        <jsp:include page="/pages/manager/discountModalContent.jsp" />
        
        </main>
        </div>
        </body>
        </html>
    </c:when>

    <%-- ========================================================
         2. MANAGER ROLE VIEW (USING MANAGER DASHBOARD LAYOUT)
         ======================================================== --%>
    <c:otherwise>
        <!DOCTYPE html>
        <html lang="vi">
        <head>
            <meta charset="UTF-8">
            <title>Quản lý mã giảm giá - RapViet Cinema</title>
            <link rel="stylesheet" href="${ctx}/assets/css/style.css">
            <link rel="stylesheet" href="${ctx}/assets/css/admin.css">
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
                    --indigo: #4f46e5;
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

                .modal {
                    display: none;
                    position: fixed;
                    z-index: 1000;
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
                    <a href="${ctx}/manager/seat-config">Cấu hình ghế</a>
                    <a href="${ctx}/manager/showtimesmanagement">Quản lý lịch chiếu</a>
                    <a href="${ctx}/manager/movie-assignments/branches">Phim tại chi nhánh</a>
                    <a href="${ctx}/manager/movie-assignments/halls">Phim tại phòng chiếu</a>
                    <a href="${ctx}/manager/movie-durations">Quản lý thời lượng phim</a>
                    <a class="active" href="${ctx}/DiscountManager">Quản lý mã giảm giá</a>
                    <a href="${ctx}/logout">Đăng xuất</a>
                </nav>
            </aside>
            
            <%-- MAIN CONTENT --%>
            <main class="admin-main">
                <div class="admin-topbar">
                    <div>
                        <strong>Quản lý mã giảm giá</strong>
                        <span>Thiết lập, thêm mới và cập nhật trạng thái các chương trình khuyến mãi, voucher</span>
                    </div>
                </div>
                
                <section class="admin-content">
                    <div style="display: flex; justify-content: space-between; align-items: center; margin-bottom: 24px;">
                        <h2 style="font-size: 24px; font-weight: 800; margin: 0; background: linear-gradient(135deg, #fff 0%, #a5b4fc 100%); -webkit-background-clip: text; -webkit-text-fill-color: transparent;">Hệ Thống Quản Lý Mã Giảm Giá</h2>
                        <button class="btn-modal-submit" onclick="openModal()" id="btnOpenAddModal" style="display: flex; align-items: center; gap: 8px; border: none;">
                            <svg width="18" height="18" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2.5" stroke-linecap="round" stroke-linejoin="round">
                                <line x1="12" y1="5" x2="12" y2="19"></line>
                                <line x1="5" y1="12" x2="19" y2="12"></line>
                            </svg>
                            Thêm Mã Giảm Giá
                        </button>
                    </div>

                    <!-- Alerts -->
                    <c:if test="${not empty sessionScope.msgSuccess}">
                        <div style="background: rgba(16,185,129,0.15); color: #10b981; border: 1px solid rgba(16,185,129,0.3); padding: 15px; border-radius: 6px; margin-bottom: 20px; font-weight: bold;">
                            ${sessionScope.msgSuccess}
                            <% session.removeAttribute("msgSuccess"); %>
                        </div>
                    </c:if>
                    <c:if test="${not empty sessionScope.msgError}">
                        <div style="background: rgba(239,68,68,0.15); color: #ef4444; border: 1px solid rgba(239,68,68,0.3); padding: 15px; border-radius: 6px; margin-bottom: 20px; font-weight: bold;">
                            ${sessionScope.msgError}
                            <% session.removeAttribute("msgError"); %>
                        </div>
                    </c:if>

                    <jsp:include page="/pages/manager/discountTableContent.jsp" />
                </section>
            </main>
        </div>

        <jsp:include page="/pages/manager/discountModalContent.jsp" />
        </body>
        </html>
    </c:otherwise>
</c:choose>
