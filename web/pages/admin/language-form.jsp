<%@ page contentType="text/html; charset=UTF-8" pageEncoding="UTF-8" %>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<c:set var="ctx" value="${pageContext.request.contextPath}" />
<c:set var="pageTitle" value="Cap nhat ngon ngu" scope="request" />
<%@ include file="/pages/shared/header-admin.jsp" %>
<%@ include file="/pages/shared/sidebar-admin.jsp" %>
<div class="rv-page-header"><div class="rv-page-header__left"><div class="rv-breadcrumb"><a href="${ctx}/admin/dashboard">Dashboard</a><i class="bi bi-chevron-right rv-breadcrumb__sep"></i><a href="${ctx}/admin/languages">Ngon ngu</a></div><h1 class="rv-page-title">${formAction == 'add' ? 'Them ngon ngu' : 'Sua ngon ngu'}</h1></div></div>
<div class="rv-card"><c:if test="${not empty error}"><div class="alert alert-danger"><c:out value="${error}"/></div></c:if><form method="post" action="${ctx}/admin/languages?action=${formAction}" class="rv-form"><input type="hidden" name="id" value="${language.id}"><div class="rv-form__group"><label>Ten ngon ngu</label><input type="text" name="name" value="<c:out value='${language.name}'/>" required></div><div class="rv-form__group"><label>Ma ngon ngu</label><input type="text" name="code" value="<c:out value='${language.code}'/>" maxlength="10" required></div><div class="rv-form__group"><label>Trang thai</label><select name="status"><option value="ACTIVE" ${language.status != 'INACTIVE' ? 'selected' : ''}>ACTIVE</option><option value="INACTIVE" ${language.status == 'INACTIVE' ? 'selected' : ''}>INACTIVE</option></select></div><div class="d-flex gap-2"><button class="rv-btn rv-btn--primary" type="submit">Luu</button><a class="rv-btn rv-btn--secondary" href="${ctx}/admin/languages">Huy</a></div></form></div>
</main></div></body></html>
