<%--
    Rạp Việt CMS — Cài đặt hệ thống
    URL: /admin/settings
    Servlet: SystemSettingsServlet
--%>
<%@ page contentType="text/html; charset=UTF-8" pageEncoding="UTF-8" %>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<c:set var="ctx" value="${pageContext.request.contextPath}" />
<c:set var="pageTitle" value="Cài đặt hệ thống — Rạp Việt CMS" scope="request" />

<%@ include file="/pages/shared/header-admin.jsp" %>
<%@ include file="/pages/shared/sidebar-admin.jsp" %>

<div class="rv-page-header">
    <div class="rv-page-header__left">
        <div class="rv-breadcrumb">
            <a href="${ctx}/admin/dashboard">Dashboard</a>
            <i class="bi bi-chevron-right rv-breadcrumb__sep"></i>
            <span class="rv-breadcrumb__current">Cài đặt hệ thống</span>
        </div>
        <h1 class="rv-page-title">Cài đặt hệ thống</h1>
        <p class="rv-page-subtitle">
            Cấu hình thông tin rạp chiếu, máy chủ SMTP gửi email và chế độ bảo trì hệ thống.
        </p>
    </div>
</div>

<ul class="nav nav-tabs mb-4" id="settingsTabs" role="tablist">
    <li class="nav-item" role="presentation">
        <button class="nav-link active" id="tab-cinema" data-bs-toggle="tab" data-bs-target="#panel-cinema"
                type="button" role="tab">
            <i class="bi bi-building me-1"></i> Thông tin rạp
        </button>
    </li>
    <li class="nav-item" role="presentation">
        <button class="nav-link" id="tab-smtp" data-bs-toggle="tab" data-bs-target="#panel-smtp"
                type="button" role="tab">
            <i class="bi bi-envelope-at me-1"></i> SMTP / Email
        </button>
    </li>
    <li class="nav-item" role="presentation">
        <button class="nav-link" id="tab-maint" data-bs-toggle="tab" data-bs-target="#panel-maint"
                type="button" role="tab">
            <i class="bi bi-tools me-1"></i> Bảo trì
        </button>
    </li>
</ul>

<div class="tab-content" id="settingsTabContent">

    <c:if test="${not empty sessionScope.flashSuccess}">
        <div class="alert alert-success alert-dismissible fade show mb-3" role="alert">
            <i class="bi bi-check-circle me-2"></i><c:out value="${sessionScope.flashSuccess}"/>
            <button type="button" class="btn-close" data-bs-dismiss="alert" aria-label="Đóng"></button>
        </div>
        <c:remove var="flashSuccess" scope="session"/>
    </c:if>
    <c:if test="${not empty sessionScope.flashError}">
        <div class="alert alert-danger alert-dismissible fade show mb-3" role="alert">
            <i class="bi bi-exclamation-circle me-2"></i><c:out value="${sessionScope.flashError}"/>
            <button type="button" class="btn-close" data-bs-dismiss="alert" aria-label="Đóng"></button>
        </div>
        <c:remove var="flashError" scope="session"/>
    </c:if>

    <!-- ── Cinema ── -->
    <div class="tab-pane fade show active" id="panel-cinema" role="tabpanel">
        <div class="card shadow-sm border-0">
            <div class="card-header bg-white py-3">
                <h2 class="h6 mb-0 fw-semibold"><i class="bi bi-building text-primary me-2"></i>Thông tin chuỗi rạp</h2>
            </div>
            <div class="card-body">
                <c:choose>
                    <c:when test="${empty cinema}">
                        <div class="alert alert-warning mb-0">
                            <i class="bi bi-exclamation-triangle me-2"></i>
                            Chưa có bản ghi trong bảng <code>CINEMA</code>. Vui lòng chạy script seed dữ liệu.
                        </div>
                    </c:when>
                    <c:otherwise>
                        <form method="post" action="${ctx}/admin/settings" class="row g-3">
                            <input type="hidden" name="section" value="cinema">
                            <input type="hidden" name="cinemaId" value="${cinema.id}">
                            <div class="col-md-6">
                                <label class="form-label fw-medium" for="cinemaName">Tên rạp / thương hiệu</label>
                                <input type="text" class="form-control" id="cinemaName" name="name" required
                                       maxlength="150" value="<c:out value='${cinema.name}'/>">
                            </div>
                            <div class="col-md-6">
                                <label class="form-label fw-medium" for="cinemaPhone">Số điện thoại</label>
                                <input type="text" class="form-control" id="cinemaPhone" name="phone"
                                       maxlength="20" value="<c:out value='${cinema.phone}'/>">
                            </div>
                            <div class="col-12">
                                <label class="form-label fw-medium" for="cinemaAddress">Địa chỉ trụ sở</label>
                                <textarea class="form-control" id="cinemaAddress" name="address" rows="2"
                                          maxlength="300"><c:out value="${cinema.address}"/></textarea>
                            </div>
                            <div class="col-md-4">
                                <label class="form-label fw-medium" for="cinemaStatus">Trạng thái</label>
                                <select class="form-select" id="cinemaStatus" name="status">
                                    <option value="ACTIVE" ${cinema.status == 'ACTIVE' ? 'selected' : ''}>Hoạt động</option>
                                    <option value="INACTIVE" ${cinema.status == 'INACTIVE' ? 'selected' : ''}>Tạm ngưng</option>
                                </select>
                            </div>
                            <div class="col-12 d-flex justify-content-end pt-2">
                                <button type="submit" class="rv-btn rv-btn--primary">
                                    <i class="bi bi-check-lg"></i> Lưu thông tin rạp
                                </button>
                            </div>
                        </form>
                    </c:otherwise>
                </c:choose>
            </div>
        </div>
    </div>

    <!-- ── SMTP ── -->
    <div class="tab-pane fade" id="panel-smtp" role="tabpanel">
        <div class="card shadow-sm border-0">
            <div class="card-header bg-white py-3">
                <h2 class="h6 mb-0 fw-semibold"><i class="bi bi-envelope-at text-primary me-2"></i>Cấu hình SMTP</h2>
            </div>
            <div class="card-body">
                <div class="alert alert-info small">
                    <i class="bi bi-info-circle me-1"></i>
                    Thay đổi tại đây có hiệu lực ngay trên máy chủ đang chạy. Để lưu vĩnh viễn sau khi khởi động lại,
                    cập nhật <code>web.xml</code> (context-param <code>mail.smtp.*</code>).
                </div>
                <form class="row g-3">
                    <div class="col-md-8">
                        <label class="form-label fw-medium" for="smtpHost">SMTP Host</label>
                        <input type="text" class="form-control" id="smtpHost" name="smtpHost" readonly
                               placeholder="smtp.gmail.com" value="<c:out value='${smtpHost}'/>">
                    </div>
                    <div class="col-md-4">
                        <label class="form-label fw-medium" for="smtpPort">Cổng</label>
                        <c:set var="smtpPortDisplay" value="${empty smtpPort ? '587' : smtpPort}" />
                        <input type="number" class="form-control" id="smtpPort" name="smtpPort" readonly
                               min="1" max="65535" value="<c:out value='${smtpPortDisplay}'/>">
                    </div>
                    <div class="col-md-6">
                        <label class="form-label fw-medium" for="smtpUser">Email gửi (From)</label>
                        <input type="email" class="form-control" id="smtpUser" name="smtpUser" readonly
                               value="<c:out value='${smtpUser}'/>">
                    </div>
                    <div class="col-md-3">
                        <label class="form-label fw-medium" for="smtpAuth">Xác thực</label>
                        <select class="form-select" id="smtpAuth" name="smtpAuth" disabled>
                            <option value="true" ${smtpAuth == 'true' ? 'selected' : ''}>Bật (true)</option>
                            <option value="false" ${smtpAuth == 'false' ? 'selected' : ''}>Tắt (false)</option>
                        </select>
                    </div>
                    <div class="col-md-6">
                        <label class="form-label fw-medium" for="smtpPassword">Mật khẩu / App Password</label>
                        <input type="password" class="form-control" id="smtpPassword" name="smtpPassword"
                               autocomplete="new-password" readonly
                               placeholder="${smtpPasswordSet ? '•••••••• (đã cấu hình)' : 'Chưa cấu hình mật khẩu'}">
                    </div>
                </form>
            </div>
        </div>
    </div>

    <!-- ── Maintenance ── -->
    <div class="tab-pane fade" id="panel-maint" role="tabpanel">
        <div class="card shadow-sm border-0">
            <div class="card-header bg-white py-3">
                <h2 class="h6 mb-0 fw-semibold"><i class="bi bi-tools text-primary me-2"></i>Bảo trì hệ thống</h2>
            </div>
            <div class="card-body">
                <form method="post" action="${ctx}/admin/settings">
                    <input type="hidden" name="section" value="maintenance">
                    <div class="form-check form-switch mb-3">
                        <input class="form-check-input" type="checkbox" role="switch" id="maintenanceSwitch"
                               name="maintenance" value="on" ${maintenanceMode ? 'checked' : ''}>
                        <label class="form-check-label fw-medium" for="maintenanceSwitch">
                            Bật chế độ bảo trì
                        </label>
                    </div>
                    <p class="text-muted small mb-4">
                        Khi bật, cờ <code>system.maintenance</code> được lưu trên máy chủ ứng dụng.
                    </p>
                    <c:if test="${maintenanceMode}">
                        <div class="alert alert-warning d-flex align-items-center">
                            <i class="bi bi-exclamation-triangle-fill me-2 fs-5"></i>
                            <span>Hệ thống đang ở <strong>chế độ bảo trì</strong>.</span>
                        </div>
                    </c:if>
                    <button type="submit" class="rv-btn rv-btn--secondary">
                        <i class="bi bi-save"></i> Áp dụng
                    </button>
                </form>
            </div>
        </div>
    </div>
</div>

</main>
</div>

<script src="https://cdn.jsdelivr.net/npm/bootstrap@5.3.3/dist/js/bootstrap.bundle.min.js"
        integrity="sha384-YvpcrYf0tY3lHB60NNkmXc5s9fDVZLESaAA55NDzOxhy9GkcIdslK1eN7N6jIeHz"
crossorigin="anonymous"></script>
<script>
    (function () {
        var hash = window.location.hash;
        if (hash && hash.startsWith('#panel-')) {
            var btn = document.querySelector('[data-bs-target="' + hash + '"]');
            if (btn && typeof bootstrap !== 'undefined') {
                bootstrap.Tab.getOrCreateInstance(btn).show();
            }
        }
    })();
</script>
</body>
</html>
