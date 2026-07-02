<%--
    Module: Manage Genres
    @author HuyPD
--%>
<%@ page contentType="text/html; charset=UTF-8" pageEncoding="UTF-8" %>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<c:set var="ctx" value="${pageContext.request.contextPath}" />
<c:set var="pageTitle" value="Quản lý Thể loại - Admin" scope="request" />

<%@ include file="/pages/shared/header-admin.jsp" %>
<%@ include file="/pages/shared/sidebar-admin.jsp" %>


<div class="rv-page-header">
    <div class="rv-page-header__left">
        <div class="rv-breadcrumb">
            <a href="${ctx}/admin/dashboard">Bảng điều khiển</a>
            <i class="bi bi-chevron-right rv-breadcrumb__sep"></i>
            <span class="rv-breadcrumb__current">Quản lý Genre</span>
        </div>
        <h1 class="rv-page-title">Thể loại (Genre)</h1>
        <p class="rv-page-subtitle">Quản lý danh sách các Genre phim.</p>
    </div>
    <div class="rv-page-header__right">
        <button class="rv-btn rv-btn--primary" data-bs-toggle="modal" data-bs-target="#addGenreModal">
            <i class="bi bi-plus-lg"></i>Thêm Genre
        </button>
    </div>
</div>

<div class="rv-toolbar" style="padding: 20px;">
    <!-- Messages -->
    <c:if test="${not empty sessionScope.msgSuccess}">
        <div class="alert alert-success alert-dismissible fade show">
            ${sessionScope.msgSuccess}
            <button type="button" class="btn-close" data-bs-dismiss="alert"></button>
            <c:remove var="msgSuccess" scope="session" />
        </div>
    </c:if>

    <c:if test="${not empty sessionScope.msgError}">
        <div class="alert alert-danger alert-dismissible fade show">
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
                        <th>Tên Genre</th>
                        <th>Mô tả</th>
                        <th style="text-align: center;">Trạng thái</th>
                        <th style="width: 140px; text-align: center;">Thao tác</th>
                    </tr>
                </thead>
                <tbody>
                    <c:forEach var="c" items="${genres}">
                        <tr>
                            <td style="text-align: center; color: var(--muted-text);">#${c.id}</td>
                            <td style="color: #fff; font-weight: 600;">${c.name}</td>
                            <td style="color: var(--muted-text);">${c.description}</td>
                            <td style="text-align: center;">
                                <c:choose>
                                    <c:when test="${c.status == 'ACTIVE'}">
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
                                            onclick="openEditModal(${c.id}, '${c.name}', '${c.description}', '${c.status}')">
                                        <i class="bi bi-pencil-square"></i>
                                    </button>
                                    <form action="${ctx}/admin/genres" method="get" class="d-inline"
                                          onsubmit="return confirm('Bạn có chắc chắn muốn xóa Genre này?');">
                                        <input type="hidden" name="action" value="delete">
                                        <input type="hidden" name="id" value="${c.id}">
                                        <button type="submit" class="rv-btn-icon rv-btn-icon--delete" title="Xóa">
                                            <i class="bi bi-trash3"></i>
                                        </button>
                                    </form>
                                </div>
                            </td>
                        </tr>
                    </c:forEach>
                    <c:if test="${empty genres}">
                        <tr>
                            <td colspan="5">
                                <div class="rv-empty" style="padding: 40px 0;">
                                    <div class="rv-empty__title">Chưa có dữ liệu</div>
                                </div>
                            </td>
                        </tr>
                    </c:if>
                </tbody>
            </table>
        </div>
    </div>

<!-- Modal Thêm -->
<div class="modal fade" id="addGenreModal" tabindex="-1">
    <div class="modal-dialog">
        <form action="${ctx}/admin/genres" method="post" class="modal-content" style="background: var(--surface-light); border: 1px solid var(--border-color);">
            <input type="hidden" name="action" value="add">
            <div class="modal-header" style="border-bottom: 1px solid var(--border-color);">
                <h5 class="modal-title" style="color: #fff;">Thêm Genre</h5>
                <button type="button" class="btn-close btn-close-white" data-bs-dismiss="modal"></button>
            </div>
            <div class="modal-body">
                <div class="mb-3">
                    <label class="form-label rv-label">Tên Genre</label>
                    <input type="text" name="name" class="rv-input" required>
                </div>
                <div class="mb-3">
                    <label class="form-label rv-label">Mô tả</label>
                    <textarea name="description" class="rv-input" rows="3"></textarea>
                </div>
                <div class="mb-3">
                    <label class="form-label rv-label">Trạng thái</label>
                    <select name="status" class="rv-input">
                        <option value="ACTIVE">Hoạt động</option>
                        <option value="INACTIVE">Khóa</option>
                    </select>
                </div>
            </div>
            <div class="modal-footer" style="border-top: 1px solid var(--border-color);">
                <button type="button" class="rv-btn rv-btn--secondary" data-bs-dismiss="modal">Đóng</button>
                <button type="submit" class="rv-btn rv-btn--primary">Thêm mới</button>
            </div>
        </form>
    </div>
</div>

<!-- Modal Sửa -->
<div class="modal fade" id="editGenreModal" tabindex="-1">
    <div class="modal-dialog">
        <form action="${ctx}/admin/genres" method="post" class="modal-content" style="background: var(--surface-light); border: 1px solid var(--border-color);">
            <input type="hidden" name="action" value="update">
            <input type="hidden" name="id" id="editId">
            <div class="modal-header" style="border-bottom: 1px solid var(--border-color);">
                <h5 class="modal-title" style="color: #fff;">Sửa Genre</h5>
                <button type="button" class="btn-close btn-close-white" data-bs-dismiss="modal"></button>
            </div>
            <div class="modal-body">
                <div class="mb-3">
                    <label class="form-label rv-label">Tên Genre</label>
                    <input type="text" name="name" id="editName" class="rv-input" required>
                </div>
                <div class="mb-3">
                    <label class="form-label rv-label">Mô tả</label>
                    <textarea name="description" id="editDesc" class="rv-input" rows="3"></textarea>
                </div>
                <div class="mb-3">
                    <label class="form-label rv-label">Trạng thái</label>
                    <select name="status" id="editStatus" class="rv-input">
                        <option value="ACTIVE">Hoạt động</option>
                        <option value="INACTIVE">Khóa</option>
                    </select>
                </div>
            </div>
            <div class="modal-footer" style="border-top: 1px solid var(--border-color);">
                <button type="button" class="rv-btn rv-btn--secondary" data-bs-dismiss="modal">Đóng</button>
                <button type="submit" class="rv-btn rv-btn--primary">Lưu thay đổi</button>
            </div>
        </form>
    </div>
</div>

<script>
    function openEditModal(id, name, desc, status) {
        document.getElementById('editId').value = id;
        document.getElementById('editName').value = name;
        document.getElementById('editDesc').value = desc;
        document.getElementById('editStatus').value = status;
        new bootstrap.Modal(document.getElementById('editGenreModal')).show();
    }
</script>

</main>
</div>
</body>
</html>
