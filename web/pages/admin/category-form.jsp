<%@ page contentType="text/html; charset=UTF-8" pageEncoding="UTF-8" %>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<c:set var="ctx" value="${pageContext.request.contextPath}" />
<c:set var="pageTitle" value="Cap nhat the loai" scope="request" />
<%@ include file="/pages/shared/header-admin.jsp" %>
<%@ include file="/pages/shared/sidebar-admin.jsp" %>
<div class="rv-page-header"><div class="rv-page-header__left"><div class="rv-breadcrumb"><a href="${ctx}/admin/dashboard">Dashboard</a><i class="bi bi-chevron-right rv-breadcrumb__sep"></i><a href="${ctx}/admin/categories">The loai</a></div><h1 class="rv-page-title">${formAction == 'add' ? 'Them the loai' : 'Sua the loai'}</h1></div></div>
<div class="rv-card"><c:if test="${not empty error}"><div class="alert alert-danger"><c:out value="${error}"/></div></c:if><form method="post" action="${ctx}/admin/categories?action=${formAction}" class="rv-form"><input type="hidden" name="id" value="${category.id}"><div class="rv-form__group"><label>Ten the loai</label><input type="text" name="name" value="<c:out value='${category.name}'/>" required></div><div class="rv-form__group"><label>Mo ta</label><textarea name="description" rows="4"><c:out value="${category.description}"/></textarea></div><div class="rv-form__group"><label>Trang thai</label><select name="status"><option value="ACTIVE" ${category.status != 'INACTIVE' ? 'selected' : ''}>ACTIVE</option><option value="INACTIVE" ${category.status == 'INACTIVE' ? 'selected' : ''}>INACTIVE</option></select></div><div class="d-flex gap-2"><button class="rv-btn rv-btn--primary" type="submit">Luu</button><a class="rv-btn rv-btn--secondary" href="${ctx}/admin/categories">Huy</a></div></form></div>
</main></div></body></html>
