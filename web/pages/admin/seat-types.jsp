<%--
    Module: Manage Seat Types & Pricing
    Author: Antigravity
--%>
<%@ page contentType="text/html; charset=UTF-8" pageEncoding="UTF-8" %>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<%@ taglib prefix="fmt" uri="http://java.sun.com/jsp/jstl/fmt" %>
<c:set var="ctx" value="${pageContext.request.contextPath}" />
<c:set var="pageTitle" value="Quản lý loại ghế & giá - Admin" scope="request" />

<%@ include file="/pages/shared/header-admin.jsp" %>
<%@ include file="/pages/shared/sidebar-admin.jsp" %>

<style>
    /* CSS Overrides for Premium Modal appearance */
    .custom-modal-content {
        background: #111827 !important;
        border: 1px solid #374151 !important;
        border-radius: 12px !important;
        color: #ffffff !important;
        box-shadow: 0 20px 25px -5px rgba(0, 0, 0, 0.3), 0 10px 10px -5px rgba(0, 0, 0, 0.04) !important;
    }
    .custom-modal-header {
        border-bottom: 1px solid #1f2937 !important;
        padding: 20px 24px !important;
        display: flex;
        align-items: center;
        justify-content: space-between;
    }
    .custom-modal-footer {
        border-top: 1px solid #1f2937 !important;
        padding: 16px 24px !important;
        display: flex;
        justify-content: flex-end;
        gap: 12px;
    }
    .custom-modal-title {
        font-size: 1.25rem !important;
        font-weight: 600 !important;
        color: #ffffff !important;
        margin: 0 !important;
    }
    .custom-modal-body {
        padding: 24px !important;
    }
    .custom-form-group {
        margin-bottom: 20px !important;
    }
    .custom-label {
        display: block !important;
        font-size: 0.875rem !important;
        font-weight: 600 !important;
        color: #9ca3af !important;
        margin-bottom: 8px !important;
        text-transform: none !important;
    }
    .custom-input {
        width: 100% !important;
        padding: 10px 14px !important;
        background: #1f2937 !important;
        border: 1px solid #4b5563 !important;
        border-radius: 6px !important;
        color: #ffffff !important;
        font-size: 0.95rem !important;
        box-sizing: border-box !important;
        transition: border-color 0.15s ease, box-shadow 0.15s ease !important;
    }
    .custom-input:focus {
        border-color: #ef4444 !important;
        box-shadow: 0 0 0 3px rgba(239, 68, 68, 0.25) !important;
        outline: none !important;
    }
    .custom-input::placeholder {
        color: #6b7280 !important;
    }
    .custom-color-container {
        display: flex;
        align-items: center;
        gap: 12px;
        background: #1f2937;
        padding: 10px 14px;
        border: 1px solid #4b5563;
        border-radius: 6px;
    }
    .custom-color-picker {
        width: 50px !important;
        height: 36px !important;
        padding: 0 !important;
        border: 1px solid #374151 !important;
        border-radius: 4px !important;
        cursor: pointer !important;
        background: none !important;
    }
</style>

<div class="rv-page-header">
    <div class="rv-page-header__left">
        <div class="rv-breadcrumb">
            <a href="${ctx}/admin/dashboard">Bảng điều khiển</a>
            <i class="bi bi-chevron-right rv-breadcrumb__sep"></i>
            <span class="rv-breadcrumb__current">Quản lý loại ghế &amp; giá</span>
        </div>
        <h1 class="rv-page-title">Loại ghế &amp; Giá mặc định</h1>
        <p class="rv-page-subtitle">Quản lý danh mục loại ghế, giá mặc định và màu sắc trên quầy vé &amp; sơ đồ.</p>
    </div>
    <div class="rv-page-header__right">
        <button class="rv-btn rv-btn--primary" data-bs-toggle="modal" data-bs-target="#addSeatTypeModal">
            <i class="bi bi-plus-lg"></i>Thêm loại ghế
        </button>
    </div>
</div>

<div class="rv-toolbar" style="padding: 20px;">
    <!-- Messages -->
    <c:if test="${not empty sessionScope.msgSuccess}">
        <div class="alert alert-success alert-dismissible fade show" style="margin-bottom: 20px;">
            ${sessionScope.msgSuccess}
            <button type="button" class="btn-close" data-bs-dismiss="alert"></button>
            <c:remove var="msgSuccess" scope="session" />
        </div>
    </c:if>

    <c:if test="${not empty sessionScope.msgError}">
        <div class="alert alert-danger alert-dismissible fade show" style="margin-bottom: 20px;">
            ${sessionScope.msgError}
            <button type="button" class="btn-close" data-bs-dismiss="alert"></button>
            <c:remove var="msgError" scope="session" />
        </div>
    </c:if>

    <!-- DATA TABLE -->
    <div class="rv-card">
        <div class="rv-table-responsive">
            <table class="rv-table">
                <thead>
                    <tr>
                        <th style="width: 80px; text-align: center;">ID</th>
                        <th>Mã loại (Code)</th>
                        <th>Tên hiển thị</th>
                        <th style="text-align: right;">Hệ số nhân</th>
                        <th style="text-align: center;">Màu hiển thị</th>
                        <th style="text-align: center;">Trạng thái</th>
                        <th style="width: 140px; text-align: center;">Thao tác</th>
                    </tr>
                </thead>
                <tbody>
                    <c:forEach var="st" items="${allSeatTypes}">
                        <tr>
                            <td style="text-align: center; color: var(--muted-text);">#${st.id}</td>
                            <td style="font-family: monospace; font-weight: bold; color: #fbbf24;">${st.code}</td>
                            <td style="color: #fff; font-weight: 600;">${st.name}</td>
                            <td style="text-align: right; color: #34d399; font-weight: 600;">
                                x<fmt:formatNumber value="${st.defaultPrice}" pattern="#,##0.0#"/>
                            </td>
                            <td style="text-align: center;">
                                <span style="display: inline-block; width: 24px; height: 24px; border-radius: 4px; background-color: ${st.color}; border: 1px solid #4b5563; vertical-align: middle;"></span>
                            </td>
                            <td style="text-align: center;">
                                <c:choose>
                                    <c:when test="${st.status == 'ACTIVE'}">
                                        <span class="rv-status rv-status--active">Hoạt động</span>
                                    </c:when>
                                    <c:otherwise>
                                        <span class="rv-status rv-status--inactive">Khóa</span>
                                    </c:otherwise>
                                </c:choose>
                            </td>
                            <td style="text-align: center;">
                                <div class="rv-actions">
                                    <button type="button" class="rv-btn-icon rv-btn-icon--edit" title="Chỉnh sửa"
                                            onclick="openEditModal(${st.id}, '${st.code}', '${st.name}', ${st.defaultPrice}, '${st.color}', '${st.status}')">
                                        <i class="bi bi-pencil-square"></i>
                                    </button>
                                    <c:if test="${st.code != 'STANDARD' && st.code != 'VIP' && st.code != 'COUPLE'}">
                                        <form action="${ctx}/admin/seat-types" method="post" class="d-inline"
                                              onsubmit="return confirm('Bạn có chắc chắn muốn khóa loại ghế này?');">
                                            <input type="hidden" name="action" value="delete">
                                            <input type="hidden" name="id" value="${st.id}">
                                            <button type="submit" class="rv-btn-icon rv-btn-icon--delete" title="Khóa">
                                                <i class="bi bi-trash3"></i>
                                            </button>
                                        </form>
                                    </c:if>
                                </div>
                            </td>
                        </tr>
                    </c:forEach>
                    <c:if test="${empty allSeatTypes}">
                        <tr>
                            <td colspan="7">
                                <div class="rv-empty" style="padding: 40px 0;">
                                    <div class="rv-empty__title">Chưa có dữ liệu loại ghế</div>
                                </div>
                            </td>
                        </tr>
                    </c:if>
                </tbody>
            </table>
        </div>
    </div>
</div>

<!-- Modal Thêm -->
<div class="modal fade" id="addSeatTypeModal" tabindex="-1">
    <div class="modal-dialog">
        <form action="${ctx}/admin/seat-types" method="post" class="modal-content custom-modal-content">
            <input type="hidden" name="action" value="add">
            <div class="custom-modal-header">
                <h5 class="custom-modal-title">Thêm Loại Ghế Mới</h5>
                <button type="button" class="btn-close btn-close-white" data-bs-dismiss="modal"></button>
            </div>
            <div class="custom-modal-body">
                <div class="custom-form-group">
                    <label class="custom-label">Mã loại ghế (VD: DELUXE)</label>
                    <input type="text" name="code" class="custom-input" placeholder="Nhập mã loại ghế..." style="text-transform: uppercase;" required>
                </div>
                <div class="custom-form-group">
                    <label class="custom-label">Tên hiển thị</label>
                    <input type="text" name="name" class="custom-input" placeholder="VD: Ghế Deluxe" required>
                </div>
                <div class="custom-form-group">
                    <label class="custom-label">Hệ số nhân giá (ví dụ: 1.0, 1.5, 2.0)</label>
                    <input type="number" step="0.1" name="defaultPrice" class="custom-input" min="0" value="1.0" required>
                </div>
                <div class="custom-form-group">
                    <label class="custom-label">Màu sắc hiển thị</label>
                    <div class="custom-color-container">
                        <input type="color" name="color" class="custom-color-picker" value="#10b981" required>
                        <span style="color: #9ca3af; font-size: 0.85rem;">Bấm chọn màu cho sơ đồ ghế</span>
                    </div>
                </div>
                <div class="custom-form-group">
                    <label class="custom-label">Trạng thái</label>
                    <select name="status" class="custom-input" style="cursor: pointer;">
                        <option value="ACTIVE">Hoạt động</option>
                        <option value="INACTIVE">Khóa</option>
                    </select>
                </div>
            </div>
            <div class="custom-modal-footer">
                <button type="button" class="rv-btn rv-btn--secondary" data-bs-dismiss="modal">Đóng</button>
                <button type="submit" class="rv-btn rv-btn--primary">Thêm mới</button>
            </div>
        </form>
    </div>
</div>

<!-- Modal Sửa -->
<div class="modal fade" id="editSeatTypeModal" tabindex="-1">
    <div class="modal-dialog">
        <form action="${ctx}/admin/seat-types" method="post" class="modal-content custom-modal-content">
            <input type="hidden" name="action" value="update">
            <input type="hidden" name="id" id="editId">
            <div class="custom-modal-header">
                <h5 class="custom-modal-title">Sửa Loại Ghế &amp; Giá</h5>
                <button type="button" class="btn-close btn-close-white" data-bs-dismiss="modal"></button>
            </div>
            <div class="custom-modal-body">
                <div class="custom-form-group">
                    <label class="custom-label">Mã loại ghế (Không được sửa)</label>
                    <input type="text" id="editCode" class="custom-input" readonly style="background: #111318 !important; color: #9ca3af !important; border-color: #374151 !important; cursor: not-allowed;">
                    <input type="hidden" name="code" id="editCodeHidden">
                </div>
                <div class="custom-form-group">
                    <label class="custom-label">Tên hiển thị</label>
                    <input type="text" name="name" id="editName" class="custom-input" required>
                </div>
                <div class="custom-form-group">
                    <label class="custom-label">Hệ số nhân giá (ví dụ: 1.0, 1.5, 2.0)</label>
                    <input type="number" step="0.1" name="defaultPrice" id="editDefaultPrice" class="custom-input" min="0" required>
                </div>
                <div class="custom-form-group">
                    <label class="custom-label">Màu sắc hiển thị</label>
                    <div class="custom-color-container">
                        <input type="color" name="color" id="editColor" class="custom-color-picker" required>
                        <span style="color: #9ca3af; font-size: 0.85rem;">Bấm chọn màu cho sơ đồ ghế</span>
                    </div>
                </div>
                <div class="custom-form-group">
                    <label class="custom-label">Trạng thái</label>
                    <select name="status" id="editStatus" class="custom-input" style="cursor: pointer;">
                        <option value="ACTIVE">Hoạt động</option>
                        <option value="INACTIVE">Khóa</option>
                    </select>
                </div>
            </div>
            <div class="custom-modal-footer">
                <button type="button" class="rv-btn rv-btn--secondary" data-bs-dismiss="modal">Đóng</button>
                <button type="submit" class="rv-btn rv-btn--primary">Lưu thay đổi</button>
            </div>
        </form>
    </div>
</div>

<script>
    function openEditModal(id, code, name, defaultPrice, color, status) {
        document.getElementById('editId').value = id;
        document.getElementById('editCode').value = code;
        document.getElementById('editCodeHidden').value = code;
        document.getElementById('editName').value = name;
        document.getElementById('editDefaultPrice').value = defaultPrice;
        document.getElementById('editColor').value = color || '#10b981';
        
        var statusSelect = document.getElementById('editStatus');
        statusSelect.value = status;
        
        if (code === 'STANDARD' || code === 'VIP' || code === 'COUPLE') {
            statusSelect.value = 'ACTIVE';
            statusSelect.disabled = true;
        } else {
            statusSelect.disabled = false;
        }
        
        new bootstrap.Modal(document.getElementById('editSeatTypeModal')).show();
    }
</script>

</main>
</div>
</body>
</html>
