<%@page contentType="text/html; charset=UTF-8" pageEncoding="UTF-8"%>
<%@taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<%@taglib prefix="fmt" uri="http://java.sun.com/jsp/jstl/fmt" %>
<c:set var="ctx" value="${pageContext.request.contextPath}" />

    <%-- ========================================================
         1. ADMIN ROLE VIEW (USING ADMIN CMS LAYOUT)
         ======================================================== --%>
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
            .custom-discount-modal {
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
            .custom-discount-modal-content {
                background: #1e293b !important;
                border: 1px solid rgba(255, 255, 255, 0.08) !important;
                border-radius: 16px !important;
                width: 95% !important;
                max-width: 500px !important;
                padding: 30px !important;
                box-sizing: border-box !important;
                box-shadow: 0 25px 50px rgba(0,0,0,0.5) !important;
                position: relative !important;
            }
            .custom-discount-modal-title {
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
                background: #0f172a !important;
                border: 1px solid rgba(255, 255, 255, 0.08) !important;
                border-radius: 8px !important;
                padding: 10px 14px !important;
                height: 42px !important;
                font-family: inherit !important;
                font-size: 14px !important;
                color: white !important;
                outline: none !important;
                width: 100% !important;
                box-sizing: border-box !important;
            }
            select {
                appearance: none;
                -webkit-appearance: none;
                -moz-appearance: none;
                background-image: url("data:image/svg+xml;utf8,<svg fill='white' height='24' viewBox='0 0 24 24' width='24' xmlns='http://www.w3.org/2000/svg'><path d='M7 10l5 5 5-5z'/><path d='M0 0h24v24H0z' fill='none'/></svg>");
                background-repeat: no-repeat;
                background-position: right 10px center;
                padding-right: 30px !important;
            }
            input:focus, select:focus {
                border-color: #3b82f6;
                box-shadow: 0 0 8px rgba(59, 130, 246, 0.2);
            }
            .custom-discount-modal-footer {
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

        <!-- SEARCH BAR CONTAINER -->
        <div class="search-bar-container" style="display: flex; gap: 15px; margin-bottom: 20px; align-items: center;">
            <div style="position: relative; flex: 1;">
                <i class="bi bi-search" style="position: absolute; left: 15px; top: 50%; transform: translateY(-50%); color: #9ca3af;"></i>
                <input type="text" id="voucherSearchInput" placeholder="Tìm kiếm theo mã voucher..." 
                       style="padding: 10px 15px 10px 42px; background: #1e293b; border: 1px solid rgba(255, 255, 255, 0.08); border-radius: 8px; color: white; width: 100%; outline: none;"
                       onkeyup="filterVouchers()">
            </div>
            <select id="voucherStatusFilter" onchange="filterVouchers()" 
                    style="width: auto; min-width: 180px; padding: 10px 15px; background: #1e293b; border: 1px solid rgba(255, 255, 255, 0.08); border-radius: 8px; color: white; outline: none; cursor: pointer;">
                <option value="ALL">Tất cả trạng thái</option>
                <option value="ACTIVE">Hoạt động</option>
                <option value="PAUSED">Tạm dừng</option>
                <option value="EXPIRED">Hết hạn</option>
            </select>
        </div>

        <div class="rv-card">
            <div class="rv-card__body p-0">
                <jsp:include page="/pages/manager/discountTableContent.jsp" />
                
                <!-- PAGINATION CONTAINER -->
                <div class="pagination-container" style="display: flex; justify-content: space-between; align-items: center; padding: 20px; border-top: 1px solid rgba(255,255,255,0.08); background: rgba(255,255,255,0.01);">
                    <div class="pagination-info" style="color: #9ca3af; font-size: 13px;">
                        Hiển thị <span id="paginated-start" style="font-weight: 600; color: white;">0</span> - <span id="paginated-end" style="font-weight: 600; color: white;">0</span> trong số <span id="paginated-total" style="font-weight: 600; color: white;">0</span> mã giảm giá
                    </div>
                    <div class="pagination-buttons" style="display: flex; gap: 8px;">
                        <button onclick="prevPage()" id="btn-prev-page" class="btn-action" style="padding: 6px 12px; font-weight: bold; border-radius: 6px;">&laquo; Trước</button>
                        <div id="page-numbers-container" style="display: flex; gap: 5px;"></div>
                        <button onclick="nextPage()" id="btn-next-page" class="btn-action" style="padding: 6px 12px; font-weight: bold; border-radius: 6px;">Sau &raquo;</button>
                    </div>
                </div>
            </div>
        </div>
        
        
        </main>
        </div>
        <jsp:include page="/pages/manager/discountModalContent.jsp" />
</body>
        </html>

<script>
    let currentPage = 1;
    const rowsPerPage = 5;
    let filteredRows = [];

    function initPagination() {
        const tableBody = document.querySelector(".vouchers-card table tbody");
        if (!tableBody) return;
        
        const rows = Array.from(tableBody.querySelectorAll("tr"));
        filteredRows = rows;
        
        showPage(1);
    }

    function showPage(page) {
        currentPage = page;
        const totalRows = filteredRows.length;
        const totalPages = Math.ceil(totalRows / rowsPerPage) || 1;
        
        if (currentPage < 1) currentPage = 1;
        if (currentPage > totalPages) currentPage = totalPages;
        
        const startIndex = (currentPage - 1) * rowsPerPage;
        const endIndex = Math.min(startIndex + rowsPerPage, totalRows);
        
        const tableBody = document.querySelector(".vouchers-card table tbody");
        if (tableBody) {
            tableBody.querySelectorAll("tr").forEach(row => {
                row.style.display = "none";
            });
        }
        
        for (let i = startIndex; i < endIndex; i++) {
            if (filteredRows[i]) {
                filteredRows[i].style.display = "";
            }
        }
        
        const infoStart = totalRows === 0 ? 0 : startIndex + 1;
        const startEl = document.getElementById("paginated-start");
        const endEl = document.getElementById("paginated-end");
        const totalEl = document.getElementById("paginated-total");
        
        if (startEl) startEl.innerText = infoStart;
        if (endEl) endEl.innerText = endIndex;
        if (totalEl) totalEl.innerText = totalRows;
        
        const btnPrev = document.getElementById("btn-prev-page");
        const btnNext = document.getElementById("btn-next-page");
        
        if (btnPrev) {
            btnPrev.disabled = currentPage === 1;
            btnPrev.style.opacity = currentPage === 1 ? "0.5" : "1";
            btnPrev.style.cursor = currentPage === 1 ? "not-allowed" : "pointer";
        }
        if (btnNext) {
            btnNext.disabled = currentPage === totalPages;
            btnNext.style.opacity = currentPage === totalPages ? "0.5" : "1";
            btnNext.style.cursor = currentPage === totalPages ? "not-allowed" : "pointer";
        }
        
        const pageContainer = document.getElementById("page-numbers-container");
        if (pageContainer) {
            pageContainer.innerHTML = "";
            for (let i = 1; i <= totalPages; i++) {
                const btn = document.createElement("button");
                btn.innerText = i;
                btn.className = "btn-action";
                btn.style.padding = "6px 12px";
                btn.style.borderRadius = "6px";
                btn.style.cursor = "pointer";
                if (i === currentPage) {
                    btn.style.background = "#3b82f6";
                    btn.style.color = "white";
                    btn.style.borderColor = "#3b82f6";
                }
                btn.onclick = () => showPage(i);
                pageContainer.appendChild(btn);
            }
        }
    }

    function prevPage() {
        if (currentPage > 1) showPage(currentPage - 1);
    }

    function nextPage() {
        const totalPages = Math.ceil(filteredRows.length / rowsPerPage) || 1;
        if (currentPage < totalPages) showPage(currentPage + 1);
    }

    function filterVouchers() {
        const searchInput = document.getElementById("voucherSearchInput");
        const statusFilter = document.getElementById("voucherStatusFilter");
        if (!searchInput || !statusFilter) return;
        
        const searchVal = searchInput.value.trim().toLowerCase();
        const statusVal = statusFilter.value;
        
        const tableBody = document.querySelector(".vouchers-card table tbody");
        if (!tableBody) return;
        
        const allRows = Array.from(tableBody.querySelectorAll("tr"));
        
        filteredRows = allRows.filter(row => {
            const codeEl = row.querySelector(".code-text");
            const codeText = codeEl ? codeEl.innerText.trim().toLowerCase() : "";
            
            const badge = row.querySelector(".badge");
            let status = "ACTIVE";
            if (badge) {
                if (badge.classList.contains("badge-paused")) {
                    status = "PAUSED";
                } else if (badge.classList.contains("badge-expired")) {
                    status = "EXPIRED";
                }
            }
            
            const matchSearch = codeText.includes(searchVal);
            const matchStatus = statusVal === "ALL" || status === statusVal;
            
            return matchSearch && matchStatus;
        });
        
        showPage(1);
    }

    window.addEventListener("DOMContentLoaded", initPagination);
</script>
