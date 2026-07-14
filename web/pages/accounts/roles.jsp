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

    /* Style for read-only checkboxes */
    .matrix-chk {
        transform: scale(1.4);
        cursor: not-allowed;
        accent-color: var(--primary); /* Use primary color for checked */
        transition: opacity 0.2s;
    }
    .matrix-chk:not(:checked) {
        opacity: 0.15; /* Make unchecked checkboxes very faint */
    }
    .matrix-chk:checked {
        opacity: 1; /* Make checked checkboxes stand out completely */
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
        <!-- Empty or can add another read-only badge here if needed -->
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
        
        <div class="rv-card__body" style="padding: 0;">
            <div style="overflow-x: auto;">
                <table class="matrix-table" id="permissionsMatrix">
                    <thead>
                        <tr>
                            <th>Chức năng hệ thống (Module)</th>
                            <th>Xem (View)</th>
                            <th>Thêm mới (Create)</th>
                            <th>Sửa (Edit)</th>
                            <th>Xóa (Delete)</th>
                            <th>Xuất file (Export)</th>
                            <th>Tất cả (Manage)</th>
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
                                    <input type="checkbox" name="permissions" value="${mKey}:view" class="matrix-chk" ${perms.view ? 'checked' : ''} onclick="return false;">
                                </td>
                                <!-- Create Checkbox -->
                                <td>
                                    <input type="checkbox" name="permissions" value="${mKey}:create" class="matrix-chk" ${perms.create ? 'checked' : ''} onclick="return false;">
                                </td>
                                <!-- Edit Checkbox -->
                                <td>
                                    <input type="checkbox" name="permissions" value="${mKey}:edit" class="matrix-chk" ${perms.edit ? 'checked' : ''} onclick="return false;">
                                </td>
                                <!-- Delete Checkbox -->
                                <td>
                                    <input type="checkbox" name="permissions" value="${mKey}:delete" class="matrix-chk" ${perms.delete ? 'checked' : ''} onclick="return false;">
                                </td>
                                <!-- Export Checkbox -->
                                <td>
                                    <input type="checkbox" name="permissions" value="${mKey}:export" class="matrix-chk" ${perms.export ? 'checked' : ''} onclick="return false;">
                                </td>
                                <!-- Manage Checkbox -->
                                <td>
                                    <input type="checkbox" name="permissions" value="${mKey}:manage" class="matrix-chk" ${perms.manage ? 'checked' : ''} onclick="return false;">
                                </td>
                                </tr>
                            </c:forEach>
                        </tbody>
                    </table>
                </div>
        </div>
    </div>

    <!-- ── PHẢI: ROLE INFORMATION (280px) ── -->
    <div class="rv-card" style="display: flex; flex-direction: column; gap: var(--s-4);">
        <div class="rv-card__header" style="padding: var(--s-4);">
            <span class="rv-card__title">Thông tin vai trò</span>
        </div>
        <div class="rv-card__body" style="padding: var(--s-4);">
            <div style="display: flex; flex-direction: column; gap: var(--s-3);">
                <!-- Role Name -->
                <div class="rv-form-group">
                    <label class="rv-label" for="roleName">Tên nhóm quyền</label>
                    <input type="text" id="roleName" name="name" class="rv-input" value="<c:out value='${selectedRole.name}'/>" readonly>
                </div>

                <!-- Description -->
                <div class="rv-form-group">
                    <label class="rv-label" for="roleDesc">Mô tả tóm tắt</label>
                    <textarea id="roleDesc" name="description" class="rv-textarea" rows="3" readonly><c:out value="${selectedRole.description}"/></textarea>
                </div>

                <!-- Scope -->
                <div class="rv-form-group">
                    <label class="rv-label" for="roleScope">Phạm vi mặc định</label>
                    <select id="roleScope" name="scope" class="rv-select" disabled>
                        <option value="ALL_BRANCHES" ${selectedRole.scope == 'ALL_BRANCHES' ? 'selected' : ''}>Toàn hệ thống (All Branches)</option>
                        <option value="ASSIGNED_BRANCH" ${selectedRole.scope == 'ASSIGNED_BRANCH' ? 'selected' : ''}>Tại chi nhánh được gán</option>
                    </select>
                </div>

                <!-- Details Readonly Info -->
                <div style="font-size: 11px; color: var(--n-400); display: flex; flex-direction: column; gap: 4px; border-top: 1px solid var(--border); padding-top: var(--s-3); margin-top: var(--s-1);">
                    <div>Ngày khởi tạo: <strong>01/01/2026 08:00</strong></div>
                    <div>Trạng thái: <strong>Cố định (Chỉ xem)</strong></div>
                </div>

                <div style="font-size: 11px; color: var(--n-400); font-style: italic; text-align: center; margin-top: 10px;">
                    🔒 Nhóm vai trò và quyền hạn được cấu hình cứng từ đầu, không thể thay đổi.
                </div>
            </div>
        </div>
    </div>

</div>

<script>
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

</script>

</main>
</div>

</body>
</html>
