<%@ page contentType="text/html; charset=UTF-8" pageEncoding="UTF-8" %>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<c:set var="ctx" value="${pageContext.request.contextPath}" />
<c:set var="pageTitle" value="Quan ly ngon ngu" scope="request" />

<%@ include file="/pages/shared/header-admin.jsp" %>
<%@ include file="/pages/shared/sidebar-admin.jsp" %>

<div class="rv-page-header">
    <div class="rv-page-header__left">
        <div class="rv-breadcrumb">
            <a href="${ctx}/admin/dashboard">Dashboard</a>
            <i class="bi bi-chevron-right rv-breadcrumb__sep"></i>
            <span class="rv-breadcrumb__current">Ngon ngu</span>
        </div>
        <h1 class="rv-page-title">Quan ly ngon ngu</h1>
        <p class="rv-page-subtitle">Them, sua va an ngon ngu phim trong he thong.</p>
    </div>
    <div class="rv-page-header__right">
        <a href="${ctx}/admin/languages?action=new" class="rv-btn rv-btn--primary">
            <i class="bi bi-plus-lg"></i>Them ngon ngu
        </a>
    </div>
</div>

<c:if test="${not empty sessionScope.flashSuccess}">
    <div class="alert alert-success alert-dismissible fade show">
        <c:out value="${sessionScope.flashSuccess}" />
        <button type="button" class="btn-close" data-bs-dismiss="alert"></button>
        <c:remove var="flashSuccess" scope="session" />
    </div>
</c:if>

<c:if test="${not empty sessionScope.flashError}">
    <div class="alert alert-danger alert-dismissible fade show">
        <c:out value="${sessionScope.flashError}" />
        <button type="button" class="btn-close" data-bs-dismiss="alert"></button>
        <c:remove var="flashError" scope="session" />
    </div>
</c:if>

<div class="rv-card">
    <div class="rv-table-responsive">
        <table class="rv-table">
            <thead>
                <tr>
                    <th style="width:70px;">ID</th>
                    <th>Ten</th>
                    <th style="width:120px;">Ma</th>
                    <th style="width:140px;">Trang thai</th>
                    <th style="width:140px;text-align:center;">Thao tac</th>
                </tr>
            </thead>
            <tbody>
                <c:forEach var="l" items="${languages}">
                    <tr>
                        <td>${l.id}</td>
                        <td><strong><c:out value="${l.name}" /></strong></td>
                        <td><c:out value="${l.code}" /></td>
                        <td>
                            <span class="rv-badge ${l.status == 'ACTIVE' ? 'rv-badge--nowshowing' : 'rv-badge--ended'}">
                                <c:out value="${l.status}" />
                            </span>
                        </td>
                        <td>
                            <div class="d-flex align-items-center justify-content-center gap-2">
                                <a href="${ctx}/admin/languages?action=edit&id=${l.id}"
                                   class="rv-btn rv-btn--ghost rv-btn--icon"
                                   style="border:none">
                                    <i class="bi bi-pencil-square"></i>
                                </a>
                                <a href="${ctx}/admin/languages?action=delete&id=${l.id}"
                                   class="rv-btn rv-btn--ghost rv-btn--icon"
                                   style="color:var(--danger);border:none"
                                   data-confirm
                                   data-confirm-title="An ngon ngu?"
                                   data-confirm-message="Ngon ngu se duoc chuyen sang INACTIVE."
                                   data-confirm-type="warning"
                                   data-confirm-text="An">
                                    <i class="bi bi-trash3-fill"></i>
                                </a>
                            </div>
                        </td>
                    </tr>
                </c:forEach>
                <c:if test="${empty languages}">
                    <tr>
                        <td colspan="5">
                            <div class="rv-empty" style="padding: 40px 0;">
                                <div class="rv-empty__title">Chua co du lieu</div>
                            </div>
                        </td>
                    </tr>
                </c:if>
            </tbody>
        </table>
    </div>
</div>

</main>
</div>
</body>
</html>
