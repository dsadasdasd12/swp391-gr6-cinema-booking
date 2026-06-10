<%--
    Rạp Việt CMS — Staff & Manager Accounts Management
    URL: /admin/accounts/staff
    Servlet: StaffAccountServlet
--%>
<%@ page contentType="text/html; charset=UTF-8" pageEncoding="UTF-8" %>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<%@ taglib prefix="fmt" uri="http://java.sun.com/jsp/jstl/fmt" %>
<%@ taglib prefix="fn" uri="http://java.sun.com/jsp/jstl/functions" %>
<c:set var="ctx" value="${pageContext.request.contextPath}" />
<c:set var="pageTitle" value="Quản lý nhân viên — Rạp Việt CMS" scope="request" />

<%@ include file="/pages/shared/header-admin.jsp" %>
<%@ include file="/pages/shared/sidebar-admin.jsp" %>

<!-- ── PAGE HEADER ── -->
<div class="rv-page-header">
    <div class="rv-page-header__left">
        <!-- Breadcrumb -->
        <div class="rv-breadcrumb">
            <a href="${ctx}/admin/dashboard">Dashboard</a>
            <i class="bi bi-chevron-right rv-breadcrumb__sep"></i>
            <span class="rv-breadcrumb__current">Tài khoản nhân viên</span>
        </div>
        <h1 class="rv-page-title">Quản lý nhân viên &amp; Quản lý</h1>
        <p class="rv-page-subtitle">Quản trị toàn bộ lực lượng nhân sự hệ thống rạp và chi nhánh.</p>
    </div>
    <div class="rv-page-header__right">
        <button type="button" class="rv-btn rv-btn--primary" id="btn-add-staff" onclick="openStaffModal()">
            <i class="bi bi-person-plus-fill"></i>Thêm tài khoản mới
        </button>
    </div>
</div>

<!-- ── TOOLBAR / FILTERS ── -->
<div class="rv-toolbar">
    <form method="get" action="${ctx}/admin/accounts/staff" class="d-flex align-items-center flex-wrap gap-3 w-100" style="margin: 0; padding: 0; border: none; background: none;">
        <!-- Search Input -->
        <div class="rv-toolbar__search">
            <i class="bi bi-search"></i>
            <input type="text" name="keyword" placeholder="Tìm kiếm tên, email nhân viên..." value="<c:out value='${param.keyword}'/>">
        </div>

        <!-- Filter Role -->
        <div class="rv-toolbar__filter">
            <select name="roleId">
                <option value="">Tất cả vai trò</option>
                <c:forEach var="r" items="${roles}">
                    <option value="${r.id}" ${param.roleId == r.id ? 'selected' : ''}><c:out value="${r.name}"/></option>
                </c:forEach>
            </select>
        </div>

        <!-- Filter Branch -->
        <div class="rv-toolbar__filter">
            <select name="branchId">
                <option value="">Tất cả chi nhánh</option>
                <c:forEach var="b" items="${branches}">
                    <option value="${b.id}" ${param.branchId == b.id ? 'selected' : ''}><c:out value="${b.name}"/></option>
                </c:forEach>
            </select>
        </div>

        <!-- Action buttons -->
        <div class="d-flex align-items-center gap-2 ms-auto">
            <button type="submit" class="rv-btn rv-btn--secondary rv-btn--sm">
                Lọc dữ liệu
            </button>
            <a href="${ctx}/admin/accounts/staff" class="rv-btn rv-btn--ghost rv-btn--sm">
                Xóa lọc
            </a>
            <a href="javascript:location.reload();" class="rv-btn rv-btn--refresh" title="Làm mới">
                <i class="bi bi-arrow-clockwise"></i>
            </a>
        </div>
    </form>
</div>

<!-- ── STAFF TABLE ── -->
<div class="rv-card">
    <c:choose>
        <c:when test="${empty staffList}">
            <div class="rv-empty">
                <i class="bi bi-shield-lock rv-empty__icon"></i>
                <div class="rv-empty__title">Không tìm thấy tài khoản</div>
                <div class="rv-empty__message">Hệ thống chưa ghi nhận tài khoản quản lý hay nhân viên nào khớp với điều kiện lọc.</div>
            </div>
        </c:when>
        <c:otherwise>
            <div class="rv-table-responsive">
                <table class="rv-table">
                    <thead>
                        <tr>
                            <th style="width: 60px; text-align: center;">No.</th>
                            <th>Họ và tên</th>
                            <th>Địa chỉ Email</th>
                            <th>Vai trò</th>
                            <th>Chi nhánh</th>
                            <th>Trạng thái</th>
                            <th>Đăng nhập cuối</th>
                            <th style="width: 180px; text-align: center;">Thao tác</th>
                        </tr>
                    </thead>
                    <tbody>
                        <c:forEach var="s" items="${staffList}" varStatus="status">
                            <tr>
                                <!-- No. -->
                                <td style="text-align: center; font-weight: 600; color: var(--n-500);">
                                    ${status.index + 1}
                                </td>

                                <!-- Full Name -->
                                <td>
                                    <div style="font-weight: 600; color: var(--n-900);">
                                        <c:out value="${s.fullName}"/>
                                    </div>
                                </td>

                                <!-- Email -->
                                <td>
                                    <c:out value="${s.email}"/>
                                </td>

                                <!-- Role Badge -->
                                <td>
                                    <c:choose>
                                        <c:when test="${s.roleId == 1}">
                                            <span class="rv-badge rv-badge--manager">ADMIN</span>
                                        </c:when>
                                        <c:when test="${s.roleId == 2}">
                                            <span class="rv-badge rv-badge--manager">QUẢN LÝ CHI NHÁNH</span>
                                        </c:when>
                                        <c:when test="${s.roleId == 3}">
                                            <span class="rv-badge rv-badge--staff">NHÂN VIÊN RẠP</span>
                                        </c:when>
                                        <c:otherwise>
                                            <span class="rv-badge rv-badge--staff">NHÂN VIÊN</span>
                                        </c:otherwise>
                                    </c:choose>
                                </td>

                                <!-- Branch -->
                                <td>
                                    <c:out value="${not empty s.branchName ? s.branchName : 'Hệ Thống (Toàn bộ)'}"/>
                                </td>

                                <!-- Status Badge -->
                                <td>
                                    <c:choose>
                                        <c:when test="${s.status == 'ACTIVE'}">
                                            <span class="rv-badge rv-badge--active">Hoạt động</span>
                                        </c:when>
                                        <c:otherwise>
                                            <span class="rv-badge rv-badge--blocked">Bị khóa</span>
                                        </c:otherwise>
                                    </c:choose>
                                </td>

                                <!-- Last Login -->
                                <td>
                                    <c:choose>
                                        <c:when test="${not empty s.lastLogin}">
                                            <fmt:parseDate value="${s.lastLogin}" pattern="yyyy-MM-dd'T'HH:mm" var="parsedLogin" type="both" />
                                            <fmt:formatDate value="${parsedLogin}" pattern="dd/MM/yyyy HH:mm" />
                                        </c:when>
                                        <c:otherwise>
                                            <span style="color: var(--n-400); font-style: italic;">Chưa từng</span>
                                        </c:otherwise>
                                    </c:choose>
                                </td>

                                <!-- Actions -->
                                <td>
                                    <div class="d-flex align-items-center justify-content-center gap-1">
                                        <!-- Edit trigger -->
                                        <button type="button" class="rv-btn rv-btn--ghost rv-btn--icon" data-tooltip="Chỉnh sửa tài khoản" style="border: none;"
                                                onclick="openStaffModal({
                                                    id: '${s.id}',
                                                    fullName: '${fn:escapeXml(s.fullName)}',
                                                    email: '${s.email}',
                                                    roleId: '${s.roleId}',
                                                    branchId: '${s.branchId}',
                                                    status: '${s.status}'
                                                })">
                                            <i class="bi bi-pencil-square" style="color: var(--primary-light);"></i>
                                        </button>

                                        <!-- Reset Password Post -->
                                        <form method="post" action="${ctx}/admin/accounts/staff" style="margin: 0; padding: 0;">
                                            <input type="hidden" name="action" value="reset-password">
                                            <input type="hidden" name="id" value="${s.id}">
                                            <button type="submit" class="rv-btn rv-btn--ghost rv-btn--icon" data-tooltip="Đặt lại mật khẩu" style="border: none;"
                                                    data-confirm
                                                    data-confirm-title="Đặt lại mật khẩu?"
                                                    data-confirm-message="Mật khẩu của nhân viên '<strong>${s.fullName}</strong>' sẽ được khôi phục mặc định là <strong>'123'</strong>."
                                                    data-confirm-type="warning"
                                                    data-confirm-text="Khôi phục">
                                                <i class="bi bi-shield-lock" style="color: var(--warning);"></i>
                                            </button>
                                        </form>

                                        <!-- Delete Account Post -->
                                        <form method="post" action="${ctx}/admin/accounts/staff" style="margin: 0; padding: 0;">
                                            <input type="hidden" name="action" value="delete">
                                            <input type="hidden" name="id" value="${s.id}">
                                            <button type="submit" class="rv-btn rv-btn--ghost rv-btn--icon" data-tooltip="Xóa tài khoản" style="border: none;"
                                                    data-confirm
                                                    data-confirm-title="Xóa tài khoản nhân sự?"
                                                    data-confirm-message="Bạn chắc chắn muốn xóa vĩnh viễn tài khoản của nhân viên '<strong>${s.fullName}</strong>' khỏi hệ thống?"
                                                    data-confirm-type="danger"
                                                    data-confirm-text="Xóa vĩnh viễn">
                                                <i class="bi bi-trash3" style="color: var(--danger);"></i>
                                            </button>
                                        </form>
                                    </div>
                                </td>
                            </tr>
                        </c:forEach>
                    </tbody>
                </table>
            </div>

            <!-- Pagination inclusion -->
            <jsp:include page="/pages/shared/pagination.jsp">
                <jsp:param name="currentPage" value="${not empty currentPage ? currentPage : 1}" />
                <jsp:param name="totalPages" value="${not empty totalPages ? totalPages : 1}" />
                <jsp:param name="totalItems" value="${not empty totalItems ? totalItems : fn:length(staffList)}" />
                <jsp:param name="pageSize" value="${not empty pageSize ? pageSize : 10}" />
                <jsp:param name="baseUrl" value="${ctx}/admin/accounts/staff?keyword=${param.keyword}&roleId=${param.roleId}&branchId=${param.branchId}" />
            </jsp:include>
        </c:otherwise>
    </c:choose>
</div>

<!-- ── FOOTER LEGENDS BAR ── -->
<div class="rv-legend">
    <div style="display: flex; flex-direction: column; gap: 4px;">
        <span class="rv-legend__group-title">Vai trò hệ thống</span>
        <div class="rv-legend__items">
            <span class="rv-legend__item"><span class="rv-badge rv-badge--manager" style="font-size:10px;">ADMIN</span> Quản lý cấp cao, toàn quyền hệ thống</span>
            <span class="rv-legend__item"><span class="rv-badge rv-badge--manager" style="font-size:10px;">QUẢN LÝ CHI NHÁNH</span> Trưởng chi nhánh được phân công</span>
            <span class="rv-legend__item"><span class="rv-badge rv-badge--staff" style="font-size:10px;">NHÂN VIÊN RẠP</span> Vận hành kỹ thuật, soát vé</span>
        </div>
    </div>
</div>

<!-- ── STAFF MODAL (ADD / EDIT FORM) ── -->
<div id="staff-modal-overlay" class="rv-modal-overlay">
    <div class="rv-modal" style="max-width: 580px;">
        <form method="post" action="${ctx}/admin/accounts/staff" id="staff-form">
            <input type="hidden" name="action" id="form-action" value="add">
            <input type="hidden" name="id" id="staff-id" value="">

            <div class="rv-modal__header">
                <div class="rv-modal__icon info">
                    <i class="bi bi-person-fill-gear"></i>
                </div>
                <h3 class="rv-modal__title" id="modal-title">Thêm tài khoản nhân viên</h3>
            </div>
            
            <div class="rv-modal__body" style="display: flex; flex-direction: column; gap: var(--s-4);">
                <!-- Full name -->
                <div class="rv-form-group">
                    <label class="rv-label" for="fullName">Họ và tên *</label>
                    <input type="text" id="fullName" name="fullName" class="rv-input" placeholder="Nhập đầy đủ họ tên nhân sự..." required>
                </div>

                <!-- Email -->
                <div class="rv-form-group">
                    <label class="rv-label" for="email">Địa chỉ Email *</label>
                    <input type="email" id="email" name="email" class="rv-input" placeholder="email@rapviet.vn" required>
                </div>

                <div style="display: grid; grid-template-columns: 1fr 1fr; gap: var(--s-4);">
                    <!-- Role select -->
                    <div class="rv-form-group">
                        <label class="rv-label" for="roleIdSelect">Vai trò hệ thống *</label>
                        <select id="roleIdSelect" name="roleId" class="rv-select" required>
                            <option value="">-- Chọn vai trò --</option>
                            <c:forEach var="r" items="${roles}">
                                <option value="${r.id}"><c:out value="${r.name}"/></option>
                            </c:forEach>
                        </select>
                    </div>

                    <!-- Branch select -->
                    <div class="rv-form-group">
                        <label class="rv-label" for="branchIdSelect">Chi nhánh quản lý *</label>
                        <select id="branchIdSelect" name="branchId" class="rv-select" required>
                            <option value="0">Hệ Thống (Toàn bộ)</option>
                            <c:forEach var="b" items="${branches}">
                                <option value="${b.id}"><c:out value="${b.name}"/></option>
                            </c:forEach>
                        </select>
                    </div>
                </div>

                <!-- Status select -->
                <div class="rv-form-group" id="status-group" style="display: none;">
                    <label class="rv-label" for="statusSelect">Trạng thái tài khoản *</label>
                    <select id="statusSelect" name="status" class="rv-select">
                        <option value="ACTIVE">Hoạt động (Active)</option>
                        <option value="BLOCKED">Bị khóa (Blocked)</option>
                    </select>
                </div>

                <div style="font-size: 11px; color: var(--n-400); margin-top: 4px;">
                    ℹ️ Tài khoản tạo mới sẽ nhận mật khẩu mặc định là <strong>'123'</strong>. Nhân sự được yêu cầu đổi mật khẩu trong lần đăng nhập đầu tiên.
                </div>
            </div>

            <div class="rv-modal__footer">
                <button type="button" class="rv-btn rv-btn--ghost rv-btn--sm" onclick="closeStaffModal()">Hủy bỏ</button>
                <button type="submit" class="rv-btn rv-btn--primary rv-btn--sm" id="btn-save-staff">Lưu tài khoản</button>
            </div>
        </form>
    </div>
</div>

</main>
</div>

<!-- Modal Script Controllers -->
<script>
    const modalOverlay = document.getElementById('staff-modal-overlay');
    const formAction = document.getElementById('form-action');
    const staffId = document.getElementById('staff-id');
    const modalTitle = document.getElementById('modal-title');
    const fullNameInput = document.getElementById('fullName');
    const emailInput = document.getElementById('email');
    const roleSelect = document.getElementById('roleIdSelect');
    const branchSelect = document.getElementById('branchIdSelect');
    const statusSelect = document.getElementById('statusSelect');
    const statusGroup = document.getElementById('status-group');

    function openStaffModal(data = null) {
        if (data) {
            // Edit mode
            formAction.value = 'update';
            staffId.value = data.id;
            modalTitle.textContent = 'Chỉnh sửa tài khoản nhân sự';
            fullNameInput.value = data.fullName;
            emailInput.value = data.email;
            emailInput.readOnly = true; // Avoid changing login email ID
            roleSelect.value = data.roleId;
            branchSelect.value = data.branchId || '0';
            statusSelect.value = data.status;
            statusGroup.style.display = 'block';
        } else {
            // Add mode
            formAction.value = 'add';
            staffId.value = '';
            modalTitle.textContent = 'Thêm tài khoản nhân viên';
            fullNameInput.value = '';
            emailInput.value = '';
            emailInput.readOnly = false;
            roleSelect.value = '';
            branchSelect.value = '0';
            statusGroup.style.display = 'none';
        }
        modalOverlay.classList.add('show');
    }

    function closeStaffModal() {
        modalOverlay.classList.remove('show');
    }

    modalOverlay.addEventListener('click', (e) => {
        if (e.target === modalOverlay) {
            closeStaffModal();
        }
    });
</script>

</body>
</html>
