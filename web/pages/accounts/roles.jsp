<%--
    Rạp Việt CMS — Roles & Permissions Matrix
    URL: /admin/accounts/roles
    Servlet: RolePermissionServlet
--%>
<%@ page contentType="text/html; charset=UTF-8" pageEncoding="UTF-8" %>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<%@ taglib prefix="fn" uri="http://java.sun.com/jsp/jstl/functions" %>
<c:set var="ctx" value="${pageContext.request.contextPath}" />
<c:set var="pageTitle" value="Phân quyền & Vai trò — Rạp Việt CMS" scope="request" />

<%@ include file="/pages/shared/header-admin.jsp" %>
<%@ include file="/pages/shared/sidebar-admin.jsp" %>

<!-- Custom internal responsive styles for the three panels -->
<style>
    .rv-roles-container {
        display: grid;
        grid-template-columns: 280px 1fr 280px;
        gap: var(--s-5);
        align-items: start;
    }
    
    .rv-role-card {
        padding: var(--s-4);
        border: 1px solid var(--border);
        border-radius: var(--r-lg);
        background: var(--surface);
        cursor: pointer;
        transition: all var(--ease);
        display: flex;
        align-items: center;
        justify-content: space-between;
        margin-bottom: var(--s-3);
    }
    .rv-role-card:hover {
        border-color: var(--primary-light);
        background: var(--n-50);
    }
    .rv-role-card.active {
        border-color: var(--primary);
        background: rgba(37, 99, 168, 0.08);
        border-left: 4px solid var(--primary);
    }

    .matrix-table {
        width: 100%;
        border-collapse: collapse;
    }
    .matrix-table th {
        background: var(--n-50);
        padding: var(--s-3);
        border-bottom: 2px solid var(--border);
        font-size: var(--text-xs);
        text-transform: uppercase;
        font-weight: 600;
        text-align: center;
        color: var(--n-600);
    }
    .matrix-table th:first-child {
        text-align: left;
    }
    .matrix-table td {
        padding: var(--s-3);
        border-bottom: 1px solid var(--border);
        text-align: center;
        font-size: var(--text-base);
    }
    .matrix-table td:first-child {
        text-align: left;
        font-weight: 500;
        color: var(--n-800);
    }
    .matrix-table tr:hover td {
        background: var(--n-50);
    }

    @media (max-width: 1200px) {
        .rv-roles-container {
            grid-template-columns: 1fr;
        }
    }
</style>

<!-- ── PAGE HEADER ── -->
<div class="rv-page-header">
    <div class="rv-page-header__left">
        <!-- Breadcrumb -->
        <div class="rv-breadcrumb">
            <a href="${ctx}/admin/dashboard">Dashboard</a>
            <i class="bi bi-chevron-right rv-breadcrumb__sep"></i>
            <span class="rv-breadcrumb__current">Vai trò &amp; Quyền hạn</span>
        </div>
        <h1 class="rv-page-title">Ma trận vai trò &amp; Phân quyền</h1>
        <p class="rv-page-subtitle">Cấu hình các quyền hạn thao tác cụ thể cho từng nhóm người dùng trên hệ thống.</p>
    </div>
    <div class="rv-page-header__right">
        <button type="button" class="rv-btn rv-btn--primary" onclick="openAddRoleModal()">
            <i class="bi bi-plus-lg"></i>Thêm vai trò mới
        </button>
    </div>
</div>

<div class="rv-roles-container">
    
    <!-- ── TRÁI: ROLES LIST (280px) ── -->
    <div>
        <div style="font-weight: 600; font-size: 14px; color: var(--n-600); margin-bottom: var(--s-3); text-transform: uppercase; letter-spacing: 0.05em;">Danh sách vai trò</div>
        <div style="max-height: 70vh; overflow-y: auto;">
            <c:forEach var="r" items="${rolesPage}">
                <a href="${ctx}/admin/accounts/roles?roleId=${r.id}" style="text-decoration: none; color: inherit;">
                    <div class="rv-role-card ${selectedRole.id == r.id ? 'active' : ''}">
                        <div>
                            <div style="font-weight: 600; color: var(--n-900);"><c:out value="${r.name}"/></div>
                            <div style="font-size: 11px; color: var(--n-400); margin-top: 4px;"><c:out value="${r.description}"/></div>
                        </div>
                        <i class="bi bi-chevron-right" style="color: var(--n-400);"></i>
                    </div>
                </a>
            </c:forEach>
        </div>

        <div style="margin-top: var(--s-3);">
            <c:if test="${totalPages > 1}">
                <jsp:include page="/pages/shared/pagination.jsp">
                    <jsp:param name="currentPage" value="${currentPage}" />
                    <jsp:param name="totalPages" value="${totalPages}" />
                    <jsp:param name="totalItems" value="${totalItems}" />
                    <jsp:param name="pageSize" value="${pageSize}" />
                    <jsp:param name="baseUrl" value="${ctx}/admin/accounts/roles?roleId=${selectedRole.id}" />
                </jsp:include>
            </c:if>
        </div>
    </div>

    <!-- ── GIỮA: PERMISSIONS MATRIX (flex-1) ── -->
    <div class="rv-card">
        <div class="rv-card__header">
            <span class="rv-card__title">Ma trận quyền hạn: <span style="color: var(--primary); font-weight: 700;"><c:out value="${selectedRole.name}"/></span></span>
            <div class="d-flex align-items-center gap-2" style="width: 260px;">
                <div style="position: relative; flex: 1;">
                    <i class="bi bi-search" style="position: absolute; left: var(--s-2); top: 50%; transform: translateY(-50%); font-size: 12px; color: var(--n-400);"></i>
                    <input type="text" id="matrixSearch" placeholder="Tìm kiếm module..." style="width: 100%; height: 32px; border: 1px solid var(--border); border-radius: var(--r-md); padding: 0 var(--s-3) 0 28px; font-size: 12px;">
                </div>
                <button type="button" class="rv-btn rv-btn--secondary rv-btn--sm" onclick="filterMatrix()">Lọc dữ liệu</button>
                <button type="button" class="rv-btn rv-btn--ghost rv-btn--sm" onclick="clearMatrixFilter()">Xóa lọc</button>
            </div>
        </div>
        
        <form method="post" action="${ctx}/admin/accounts/roles" id="matrix-form">
            <input type="hidden" name="action" value="update-permissions">
            <input type="hidden" name="roleId" value="${selectedRole.id}">

            <div class="rv-card__body" style="padding: 0;">
                <div style="overflow-x: auto;">
                    <table class="matrix-table" id="permissionsMatrix">
                        <thead>
                            <tr>
                                <th>Chức năng hệ thống (Module)</th>
                                <th>Xem (View)<br><input type="checkbox" onclick="checkAllColumn(1, this.checked)" style="cursor: pointer;"></th>
                                <th>Thêm mới (Create)<br><input type="checkbox" onclick="checkAllColumn(2, this.checked)" style="cursor: pointer;"></th>
                                <th>Sửa (Edit)<br><input type="checkbox" onclick="checkAllColumn(3, this.checked)" style="cursor: pointer;"></th>
                                <th>Xóa (Delete)<br><input type="checkbox" onclick="checkAllColumn(4, this.checked)" style="cursor: pointer;"></th>
                                <th>Xuất file (Export)<br><input type="checkbox" onclick="checkAllColumn(5, this.checked)" style="cursor: pointer;"></th>
                                <th>Tất cả (Manage)<br><input type="checkbox" onclick="checkAllColumn(6, this.checked)" style="cursor: pointer;"></th>
                            </tr>
                        </thead>
                        <tbody>
                            <c:forEach var="m" items="${modules}">
                                <c:set var="mKey" value="${m.key}"/>
                                <c:set var="perms" value="${selectedRolePermissions[mKey]}"/>
                                
                                <tr class="matrix-row">
                                    <td>
                                        <div style="font-weight: 600;"><c:out value="${m.name}"/></div>
                                        <div style="font-size: 11px; color: var(--n-400); font-weight: normal; margin-top: 2px;"><c:out value="${m.description}"/></div>
                                    </td>
                                    
                                    <!-- View Checkbox -->
                                    <td>
                                        <input type="checkbox" name="permissions" value="${mKey}:view" class="col-1-chk" ${perms.view ? 'checked' : ''} style="transform: scale(1.1); cursor: pointer;">
                                    </td>
                                    <!-- Create Checkbox -->
                                    <td>
                                        <input type="checkbox" name="permissions" value="${mKey}:create" class="col-2-chk" ${perms.create ? 'checked' : ''} style="transform: scale(1.1); cursor: pointer;">
                                    </td>
                                    <!-- Edit Checkbox -->
                                    <td>
                                        <input type="checkbox" name="permissions" value="${mKey}:edit" class="col-3-chk" ${perms.edit ? 'checked' : ''} style="transform: scale(1.1); cursor: pointer;">
                                    </td>
                                    <!-- Delete Checkbox -->
                                    <td>
                                        <input type="checkbox" name="permissions" value="${mKey}:delete" class="col-4-chk" ${perms.delete ? 'checked' : ''} style="transform: scale(1.1); cursor: pointer;">
                                    </td>
                                    <!-- Export Checkbox -->
                                    <td>
                                        <input type="checkbox" name="permissions" value="${mKey}:export" class="col-5-chk" ${perms.export ? 'checked' : ''} style="transform: scale(1.1); cursor: pointer;">
                                    </td>
                                    <!-- Manage Checkbox -->
                                    <td>
                                        <input type="checkbox" name="permissions" value="${mKey}:manage" class="col-6-chk" ${perms.manage ? 'checked' : ''} style="transform: scale(1.1); cursor: pointer;">
                                    </td>
                                </tr>
                            </c:forEach>
                        </tbody>
                    </table>
                </div>
            </div>

            <!-- Matrix Footer Save Bar -->
            <div style="background: var(--n-50); border-top: 1px solid var(--border); padding: var(--s-4) var(--s-6); display: flex; align-items: center; justify-content: space-between; border-radius: 0 0 var(--r-lg) var(--r-lg);">
                <div style="font-size: 12px; color: var(--n-500); display: flex; align-items: center; gap: 6px;">
                    <i class="bi bi-info-circle-fill" style="color: var(--primary);"></i>
                    Thay đổi quyền sẽ áp dụng lập tức cho tất cả người dùng thuộc vai trò này.
                </div>
                <div style="display: flex; gap: var(--s-3);">
                    <a href="javascript:location.reload();" class="rv-btn rv-btn--ghost rv-btn--sm">Hủy bỏ</a>
                    <button type="submit" class="rv-btn rv-btn--primary rv-btn--sm">
                        <i class="bi bi-shield-check"></i>Lưu thay đổi ma trận
                    </button>
                </div>
            </div>
        </form>
    </div>

    <!-- ── PHẢI: ROLE INFORMATION (280px) ── -->
    <div class="rv-card" style="display: flex; flex-direction: column; gap: var(--s-4);">
        <div class="rv-card__header" style="padding: var(--s-4);">
            <span class="rv-card__title">Thông tin vai trò</span>
        </div>
        <div class="rv-card__body" style="padding: var(--s-4);">
            <form method="post" action="${ctx}/admin/accounts/roles" style="display: flex; flex-direction: column; gap: var(--s-3);">
                <input type="hidden" name="action" value="update-role-info">
                <input type="hidden" name="roleId" value="${selectedRole.id}">

                <!-- Role Name -->
                <div class="rv-form-group">
                    <label class="rv-label" for="roleName">Tên nhóm quyền</label>
                    <input type="text" id="roleName" name="name" class="rv-input" value="<c:out value='${selectedRole.name}'/>" required ${selectedRole.id <= 4 ? 'readonly' : ''}>
                </div>

                <!-- Description -->
                <div class="rv-form-group">
                    <label class="rv-label" for="roleDesc">Mô tả tóm tắt</label>
                    <textarea id="roleDesc" name="description" class="rv-textarea" rows="3" required ${selectedRole.id <= 4 ? 'readonly' : ''}><c:out value="${selectedRole.description}"/></textarea>
                </div>

                <!-- Scope -->
                <div class="rv-form-group">
                    <label class="rv-label" for="roleScope">Phạm vi mặc định</label>
                    <select id="roleScope" name="scope" class="rv-select" ${selectedRole.id <= 4 ? 'disabled' : ''}>
                        <option value="ALL_BRANCHES" ${selectedRole.scope == 'ALL_BRANCHES' ? 'selected' : ''}>Toàn hệ thống (All Branches)</option>
                        <option value="ASSIGNED_BRANCH" ${selectedRole.scope == 'ASSIGNED_BRANCH' ? 'selected' : ''}>Tại chi nhánh được gán</option>
                    </select>
                </div>

                <!-- Details Readonly Info -->
                <div style="font-size: 11px; color: var(--n-400); display: flex; flex-direction: column; gap: 4px; border-top: 1px solid var(--border); padding-top: var(--s-3); margin-top: var(--s-1);">
                    <div>Ngày khởi tạo: <strong>01/01/2026 08:00</strong></div>
                    <div>Cập nhật cuối: <strong>Vừa xong</strong></div>
                </div>

                <!-- Save Info Buttons (Only enable for editable custom roles) -->
                <c:choose>
                    <c:when test="${selectedRole.id > 3}">
                        <button type="submit" class="rv-btn rv-btn--secondary rv-btn--sm w-100" style="margin-top: var(--s-2);">
                            <i class="bi bi-save"></i>Cập nhật thông tin
                        </button>
                    </c:when>
                    <c:otherwise>
                        <div style="font-size: 11px; color: var(--n-400); font-style: italic; text-align: center; margin-top: 10px;">
                            🔒 Nhóm vai trò mặc định hệ thống. Không thể sửa thông tin hành chính.
                        </div>
                    </c:otherwise>
                </c:choose>
            </form>

            <!-- Delete Role Action (Only for non-default roles) -->
            <c:if test="${selectedRole.id > 3}">
                <div style="border-top: 1px solid var(--border); padding-top: var(--s-4); margin-top: var(--s-4);">
                    <form method="post" action="${ctx}/admin/accounts/roles" style="margin: 0;">
                        <input type="hidden" name="action" value="delete-role">
                        <input type="hidden" name="roleId" value="${selectedRole.id}">
                        <button type="submit" class="rv-btn rv-btn--danger rv-btn--sm w-100"
                                data-confirm
                                data-confirm-title="Xóa nhóm vai trò?"
                                data-confirm-message="Bạn chắc chắn muốn xóa vai trò '<strong>${selectedRole.name}</strong>'? Toàn bộ nhân viên được gán nhóm vai trò này sẽ mất quyền truy cập hệ thống và bạn phải gán lại nhóm vai trò khác cho họ."
                                data-confirm-type="danger"
                                data-confirm-text="Xóa vĩnh viễn">
                            <i class="bi bi-trash3-fill"></i>Xóa nhóm vai trò
                        </button>
                    </form>
                </div>
            </c:if>
        </div>
    </div>

</div>

<!-- ── ADD ROLE MODAL (POPUP) ── -->
<div id="add-role-modal-overlay" class="rv-modal-overlay">
    <div class="rv-modal">
        <form method="post" action="${ctx}/admin/accounts/roles" id="add-role-form">
            <input type="hidden" name="action" value="add-role">

            <div class="rv-modal__header">
                <div class="rv-modal__icon success">
                    <i class="bi bi-plus-circle"></i>
                </div>
                <h3 class="rv-modal__title">Tạo nhóm vai trò mới</h3>
            </div>
            
            <div class="rv-modal__body" style="display: flex; flex-direction: column; gap: var(--s-3);">
                <div class="rv-form-group">
                    <label class="rv-label" for="newRoleName">Tên nhóm quyền *</label>
                    <input type="text" id="newRoleName" name="name" class="rv-input" placeholder="Ví dụ: Giám Sát Chi Nhánh" required>
                </div>

                <div class="rv-form-group">
                    <label class="rv-label" for="newRoleDesc">Mô tả chức năng *</label>
                    <textarea id="newRoleDesc" name="description" class="rv-textarea" rows="3" placeholder="Mô tả tóm tắt quyền hạn công việc..." required></textarea>
                </div>

                <div class="rv-form-group">
                    <label class="rv-label" for="newRoleScope">Phạm vi hoạt động</label>
                    <select id="newRoleScope" name="scope" class="rv-select">
                        <option value="ASSIGNED_BRANCH">Chỉ tại chi nhánh được gán</option>
                        <option value="ALL_BRANCHES">Toàn bộ hệ thống</option>
                    </select>
                </div>
            </div>

            <div class="rv-modal__footer">
                <button type="button" class="rv-btn rv-btn--ghost rv-btn--sm" onclick="closeAddRoleModal()">Hủy bỏ</button>
                <button type="submit" class="rv-btn rv-btn--success rv-btn--sm">Tạo vai trò</button>
            </div>
        </form>
    </div>
</div>

<script>
    // Check all checkboxes in a column (View, Create, etc.)
    function checkAllColumn(colNum, isChecked) {
        const checkboxes = document.querySelectorAll('.col-' + colNum + '-chk');
        checkboxes.forEach(chk => {
            chk.checked = isChecked;
        });
    }

    // Filter matrix table modules via search box (on button click or Enter)
    function filterMatrix() {
        const query = document.getElementById('matrixSearch').value.toLowerCase().trim();
        const rows = document.querySelectorAll('.matrix-row');
        
        rows.forEach(row => {
            const title = row.querySelector('td:first-child').textContent.toLowerCase();
            if (!query || title.includes(query)) {
                row.style.display = '';
            } else {
                row.style.display = 'none';
            }
        });
    }

    function clearMatrixFilter() {
        document.getElementById('matrixSearch').value = '';
        document.querySelectorAll('.matrix-row').forEach(row => { row.style.display = ''; });
    }

    document.getElementById('matrixSearch').addEventListener('keydown', (e) => {
        if (e.key === 'Enter') {
            e.preventDefault();
            filterMatrix();
        }
    });

    // Modal toggles
    const addRoleModal = document.getElementById('add-role-modal-overlay');
    
    function openAddRoleModal() {
        addRoleModal.classList.add('show');
    }
    
    function closeAddRoleModal() {
        addRoleModal.classList.remove('show');
    }

    addRoleModal.addEventListener('click', (e) => {
        if (e.target === addRoleModal) {
            closeAddRoleModal();
        }
    });
</script>

</main>
</div>

</body>
</html>
