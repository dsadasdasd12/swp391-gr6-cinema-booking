<%@ page contentType="text/html; charset=UTF-8"
         pageEncoding="UTF-8" %>

<%@ taglib prefix="c"
           uri="http://java.sun.com/jsp/jstl/core" %>

<%@ taglib prefix="fmt"
           uri="http://java.sun.com/jsp/jstl/fmt" %>

<%@ taglib prefix="fn"
           uri="http://java.sun.com/jsp/jstl/functions" %>

<c:set var="ctx"
       value="${pageContext.request.contextPath}" />

<c:set var="pageTitle"
       value="Quản lý F&B - RapViet Cinema"
       scope="request" />

<%--
    Xác định danh mục hiện tại có phải Combo hay không.

    Category Combo trong database cần có tên là:
    Combo
--%>
<c:set var="isComboCategory"
       value="false" />

<c:set var="selectedCategoryName"
       value="" />

<c:forEach var="category"
           items="${categories}">

    <c:if test="${selectedCategoryId == category.id}">

        <c:set var="selectedCategoryName"
               value="${category.name}" />

        <c:if test="${fn:toLowerCase(fn:trim(category.name)) == 'combo'}">
            <c:set var="isComboCategory"
                   value="true" />
        </c:if>

    </c:if>

</c:forEach>

<!DOCTYPE html>

<html lang="vi">

    <head>

        <%@ include file="/pages/shared/header-admin.jsp" %>

        <link rel="stylesheet"
              href="${ctx}/assets/css/admin/fnbmanagement.css?v=2">

    </head>

    <body>

        <%@ include file="/pages/shared/sidebar-admin.jsp" %>

        <main>

            <section class="fnb-admin-page">

                <!-- =====================================================
                     PAGE HEADER
                     ===================================================== -->

                <div class="fnb-page-header">

                    <div>

                        <h1>Quản lý F&amp;B</h1>

                        <p>
                            Quản lý danh mục, sản phẩm, combo,
                            giá bán và quyền được phép bán.
                        </p>

                    </div>

                </div>

                <!-- =====================================================
                     FLASH MESSAGE
                     ===================================================== -->

                <c:if test="${not empty sessionScope.flashMessage}">

                    <div class="fnb-alert
                         ${sessionScope.flashType == 'success'
                           ? 'fnb-alert-success'
                           : 'fnb-alert-error'}">

                        <c:out value="${sessionScope.flashMessage}" />

                    </div>

                    <c:remove var="flashMessage"
                              scope="session" />

                    <c:remove var="flashType"
                              scope="session" />

                </c:if>

                <!-- =====================================================
                     CATEGORY SECTION
                     GIỮ NGUYÊN LAYOUT NỬA TRÊN
                     ===================================================== -->

                <div class="fnb-section">

                    <div class="fnb-section-header">

                        <div>

                            <h2>Danh mục F&amp;B</h2>

                            <p>
                                Chọn một danh mục để xem sản phẩm
                                hoặc combo.
                            </p>

                        </div>

                        <button type="button"
                                class="fnb-primary-button"
                                onclick="openCategoryModal()">

                            + Thêm danh mục

                        </button>

                    </div>

                    <div class="fnb-category-grid">

                        <c:forEach var="category"
                                   items="${categories}">

                            <a href="${ctx}/admin/fnb-dashboard?categoryId=${category.id}"
                               class="fnb-category-card
                               ${selectedCategoryId == category.id
                                 ? 'active'
                                 : ''}">

                                <div class="fnb-category-icon">

                                    <i class="bi bi-grid-fill"></i>

                                </div>

                                <h3>

                                    <c:out value="${category.name}" />

                                </h3>

                                <p>

                                    <c:out value="${category.description}" />

                                </p>

                                <div class="fnb-category-actions">

                                    <button type="button"
                                            class="fnb-card-button"
                                            data-id="${category.id}"
                                            data-name="<c:out value='${category.name}' />"
                                            data-description="<c:out value='${category.description}' />"
                                            onclick="event.preventDefault();
                                                    event.stopPropagation();
                                                    openEditCategoryFromButton(this);">

                                        Sửa

                                    </button>

                                    <form method="post"
                                          action="${ctx}/admin/fnb-dashboard/category/status"
                                          onclick="event.stopPropagation();">

                                        <input type="hidden"
                                               name="id"
                                               value="${category.id}">

                                        <input type="hidden"
                                               name="status"
                                               value="${category.status == 'ACTIVE'
                                                        ? 'INACTIVE'
                                                        : 'ACTIVE'}">

                                        <button type="submit"
                                                class="fnb-card-button
                                                ${category.status == 'ACTIVE'
                                                  ? 'danger'
                                                  : 'success'}">

                                            ${category.status == 'ACTIVE'
                                              ? 'Deactive'
                                              : 'Active'}

                                        </button>

                                    </form>

                                </div>

                                <span class="fnb-category-count">

                                    <c:choose>

                                        <c:when test="${fn:toLowerCase(fn:trim(category.name)) == 'combo'}">

                                            ${not empty combos
                                              ? fn:length(combos)
                                              : 0}
                                            combo

                                        </c:when>

                                        <c:otherwise>

                                            ${category.productCount}
                                            sản phẩm

                                        </c:otherwise>

                                    </c:choose>

                                </span>

                            </a>

                        </c:forEach>

                    </div>

                </div>

                <!-- =====================================================
                     PRODUCT / COMBO SECTION
                     ===================================================== -->

                <div class="fnb-section">

                    <div class="fnb-section-header">

                        <div>

                            <c:choose>

                                <c:when test="${isComboCategory}">

                                    <h2>Danh sách combo</h2>

                                    <p>
                                        Quản lý thành phần, giá bán,
                                        trạng thái và quyền bán combo.
                                    </p>

                                </c:when>

                                <c:otherwise>

                                    <h2>
                                        Sản phẩm trong danh mục

                                        <c:if test="${not empty selectedCategoryName}">
                                            -
                                            <c:out value="${selectedCategoryName}" />
                                        </c:if>
                                    </h2>

                                    <p>
                                        Quản lý các sản phẩm thuộc
                                        danh mục đã chọn.
                                    </p>

                                </c:otherwise>

                            </c:choose>

                        </div>

                        <c:if test="${selectedCategoryId > 0}">

                            <c:choose>

                                <c:when test="${isComboCategory}">

                                    <button type="button"
                                            class="fnb-primary-button"
                                            onclick="openComboModal()">

                                        + Thêm combo mới

                                    </button>

                                </c:when>

                                <c:otherwise>

                                    <button type="button"
                                            class="fnb-primary-button"
                                            onclick="openProductModal()">

                                        + Thêm sản phẩm

                                    </button>

                                </c:otherwise>

                            </c:choose>

                        </c:if>

                    </div>

                    <!-- Chưa chọn Category -->

                    <c:if test="${selectedCategoryId <= 0}">

                        <div class="fnb-empty">

                            Vui lòng chọn một danh mục để tiếp tục.

                        </div>

                    </c:if>

                    <!-- =================================================
                         COMBO TAB
                         ================================================= -->

                    <c:if test="${selectedCategoryId > 0
                                  && isComboCategory}">

                          <c:choose>

                              <c:when test="${empty combos}">

                                  <div class="fnb-empty">

                                      Chưa có combo nào.

                                  </div>

                              </c:when>

                              <c:otherwise>

                                  <div class="fnb-product-grid">

                                      <c:forEach var="combo"
                                                 items="${combos}">

                                          <article class="fnb-product-card">

                                              <div class="fnb-product-image">

                                                  <c:choose>

                                                      <c:when test="${empty combo.imageUrl}">
                                                          <img src="${ctx}/assets/img/fnb/popcorn_large.png"
                                                               alt="<c:out value='${combo.name}'/>">
                                                      </c:when>

                                                      <c:when test="${combo.imageUrl.startsWith('http://')
                                                                      or combo.imageUrl.startsWith('https://')}">
                                                              <img src="<c:out value='${combo.imageUrl}'/>"
                                                                   alt="<c:out value='${combo.name}'/>"
                                                                   onerror="this.onerror=null;this.src='${ctx}/assets/img/fnb/popcorn_large.png';">
                                                      </c:when>

                                                      <c:otherwise>
                                                          <img src="${ctx}${combo.imageUrl.startsWith('/') ? '' : '/'}<c:out value='${combo.imageUrl}'/>"
                                                               alt="<c:out value='${combo.name}'/>"
                                                               onerror="this.onerror=null;this.src='${ctx}/assets/img/fnb/popcorn_large.png';">
                                                      </c:otherwise>

                                                  </c:choose>

                                              </div>

                                              <div class="fnb-product-body">

                                                  <div class="fnb-product-type">

                                                      COMBO

                                                  </div>

                                                  <div class="fnb-product-title-row">

                                                      <h3>

                                                          <c:out value="${combo.name}" />

                                                      </h3>

                                                      <span class="fnb-status
                                                            ${combo.status == 'ACTIVE'
                                                              ? 'active'
                                                              : 'inactive'}">

                                                          ${combo.status == 'ACTIVE'
                                                            ? 'Hoạt động'
                                                            : 'Ngừng hoạt động'}

                                                      </span>

                                                  </div>

                                                  <p class="fnb-product-description">

                                                      <c:out value="${combo.description}" />

                                                  </p>

                                                  <div class="fnb-combo-components">

                                                      <strong>
                                                          Thành phần:
                                                      </strong>

                                                      <c:choose>

                                                          <c:when test="${empty combo.items}">

                                                              <span>
                                                                  Chưa có sản phẩm.
                                                              </span>

                                                          </c:when>

                                                          <c:otherwise>

                                                              <ul>

                                                                  <c:forEach var="item"
                                                                             items="${combo.items}">

                                                                      <li>

                                                                          <c:out value="${item.productName}" />

                                                                          ×

                                                                          <c:out value="${item.quantity}" />

                                                                      </li>

                                                                  </c:forEach>

                                                              </ul>

                                                          </c:otherwise>

                                                      </c:choose>

                                                  </div>

                                                  <c:if test="${not empty combo.originalPrice}">

                                                      <div class="fnb-combo-original-price">

                                                          <span>
                                                              Tổng giá sản phẩm:
                                                          </span>

                                                          <del>

                                                              <fmt:formatNumber
                                                                  value="${combo.originalPrice}"
                                                                  type="number"
                                                                  maxFractionDigits="0" />

                                                              ₫

                                                          </del>

                                                      </div>

                                                  </c:if>

                                                  <div class="fnb-product-price">

                                                      <fmt:formatNumber
                                                          value="${combo.sellingPrice}"
                                                          type="number"
                                                          maxFractionDigits="0" />

                                                      ₫

                                                  </div>

                                                  <!-- Cho phép bán Combo -->

                                                  <form method="post"
                                                        action="${ctx}/admin/fnb-dashboard/combo/toggle-sale"
                                                        class="fnb-sale-form">

                                                      <input type="hidden"
                                                             name="comboId"
                                                             value="${combo.id}">

                                                      <input type="hidden"
                                                             name="categoryId"
                                                             value="${selectedCategoryId}">

                                                      <input type="hidden"
                                                             name="allowed"
                                                             value="${combo.allowedToSell
                                                                      ? 'false'
                                                                      : 'true'}">

                                                      <label class="fnb-switch-row">

                                                          <span>
                                                              Cho phép bán
                                                          </span>

                                                          <button type="submit"
                                                                  class="fnb-switch
                                                                  ${combo.allowedToSell
                                                                    ? 'enabled'
                                                                    : ''}"
                                                                    ${combo.status != 'ACTIVE'
                                                                      ? 'disabled'
                                                                      : ''}
                                                                    aria-label="Bật hoặc tắt bán combo">

                                                                      <span class="fnb-switch-circle"></span>

                                                                  </button>

                                                          </label>

                                                      </form>

                                                  </div>

                                                  <div class="fnb-product-actions">

                                                      <!-- Sửa Combo -->

                                                      <button type="button"
                                                              class="fnb-card-button"
                                                              data-id="${combo.id}"
                                                              data-name="<c:out value='${combo.name}' />"
                                                              data-description="<c:out value='${combo.description}' />"
                                                              data-selling-price="${combo.sellingPrice}"
                                                              data-image-url="<c:out value='${combo.imageUrl}' />"
                                                              data-allowed-to-sell="${combo.allowedToSell}"
                                                              onclick="openEditComboFromButton(this)">

                                                          Sửa

                                                      </button>

                                                      <!-- Active / Deactive Combo -->

                                                      <form method="post"
                                                            action="${ctx}/admin/fnb-dashboard/combo/status">

                                                          <input type="hidden"
                                                                 name="id"
                                                                 value="${combo.id}">

                                                          <input type="hidden"
                                                                 name="categoryId"
                                                                 value="${selectedCategoryId}">

                                                          <input type="hidden"
                                                                 name="status"
                                                                 value="${combo.status == 'ACTIVE'
                                                                          ? 'INACTIVE'
                                                                          : 'ACTIVE'}">

                                                          <button type="submit"
                                                                  class="fnb-card-button
                                                                  ${combo.status == 'ACTIVE'
                                                                    ? 'danger'
                                                                    : 'success'}">

                                                              ${combo.status == 'ACTIVE'
                                                                ? 'Deactive'
                                                                : 'Active'}

                                                          </button>

                                                      </form>

                                                  </div>

                                                  <!--
                                                      Dữ liệu thành phần Combo dùng
                                                      khi mở form chỉnh sửa.
                                                  -->

                                                  <div id="combo-data-${combo.id}"
                                                       class="combo-edit-data"
                                                       hidden>

                                                      <c:forEach var="item"
                                                                 items="${combo.items}">

                                                          <span class="combo-edit-item"
                                                                data-product-id="${item.productId}"
                                                                data-product-name="<c:out value='${item.productName}' />"
                                                                data-quantity="${item.quantity}"
                                                                data-unit-price="${item.unitPrice}">
                                                          </span>

                                                      </c:forEach>

                                                  </div>

                                              </article>

                                          </c:forEach>

                                      </div>

                                  </c:otherwise>

                              </c:choose>

                          </c:if>

                          <!-- =================================================
                               PRODUCT TAB
                               ================================================= -->

                          <c:if test="${selectedCategoryId > 0
                                        && !isComboCategory}">

                                <c:choose>

                                    <c:when test="${empty products}">

                                        <div class="fnb-empty">

                                            Danh mục này chưa có sản phẩm.

                                        </div>

                                    </c:when>

                                    <c:otherwise>

                                        <div class="fnb-product-grid">

                                            <c:forEach var="product"
                                                       items="${products}">

                                                <article class="fnb-product-card">

                                                    <div class="fnb-product-image">

                                                        <c:choose>

                                                            <c:when test="${empty product.imageUrl}">
                                                                <img src="${ctx}/assets/img/fnb/pepsi.png"
                                                                     alt="<c:out value='${product.name}'/>">
                                                            </c:when>

                                                            <c:when test="${product.imageUrl.startsWith('http://')
                                                                            or product.imageUrl.startsWith('https://')}">
                                                                    <img src="<c:out value='${product.imageUrl}'/>"
                                                                         alt="<c:out value='${product.name}'/>"
                                                                         onerror="this.onerror=null;this.src='${ctx}/assets/img/fnb/pepsi.png';">
                                                            </c:when>

                                                            <c:otherwise>
                                                                <img src="${ctx}${product.imageUrl.startsWith('/') ? '' : '/'}<c:out value='${product.imageUrl}'/>"
                                                                     alt="<c:out value='${product.name}'/>"
                                                                     onerror="this.onerror=null;this.src='${ctx}/assets/img/fnb/pepsi.png';">
                                                            </c:otherwise>

                                                        </c:choose>

                                                    </div>

                                                    <div class="fnb-product-body">

                                                        <div class="fnb-product-type">

                                                            ITEM

                                                        </div>

                                                        <h3>

                                                            <c:out value="${product.name}" />

                                                        </h3>

                                                        <p class="fnb-product-description">

                                                            <c:out value="${product.description}" />

                                                        </p>

                                                        <div class="fnb-product-price">

                                                            <fmt:formatNumber
                                                                value="${product.sellingPrice}"
                                                                type="number"
                                                                maxFractionDigits="0" />

                                                            ₫

                                                        </div>

                                                        <!-- Cho phép bán Product -->

                                                        <form method="post"
                                                              action="${ctx}/admin/fnb-dashboard/toggle-sale"
                                                              class="fnb-sale-form">

                                                            <input type="hidden"
                                                                   name="productId"
                                                                   value="${product.id}">

                                                            <input type="hidden"
                                                                   name="categoryId"
                                                                   value="${selectedCategoryId}">

                                                            <input type="hidden"
                                                                   name="allowed"
                                                                   value="${product.allowedToSell
                                                                            ? 'false'
                                                                            : 'true'}">

                                                            <label class="fnb-switch-row">

                                                                <span>
                                                                    Cho phép bán
                                                                </span>

                                                                <button type="submit"
                                                                        class="fnb-switch
                                                                        ${product.allowedToSell
                                                                          ? 'enabled'
                                                                          : ''}"
                                                                          ${product.status != 'ACTIVE'
                                                                            ? 'disabled'
                                                                            : ''}
                                                                          aria-label="Bật hoặc tắt bán sản phẩm">

                                                                            <span class="fnb-switch-circle"></span>

                                                                        </button>

                                                                </label>

                                                            </form>

                                                        </div>

                                                        <div class="fnb-product-actions">

                                                            <button type="button"
                                                                    class="fnb-card-button"
                                                                    data-id="${product.id}"
                                                                    data-category-id="${product.categoryId}"
                                                                    data-name="<c:out value='${product.name}' />"
                                                                    data-description="<c:out value='${product.description}' />"
                                                                    data-product-type="<c:out value='${product.productType}' />"
                                                                    data-selling-price="${product.sellingPrice}"
                                                                    data-image-url="<c:out value='${product.imageUrl}' />"
                                                                    data-allowed-to-sell="${product.allowedToSell}"
                                                                    onclick="openEditProductFromButton(this)">

                                                                Sửa

                                                            </button>

                                                            <form method="post"
                                                                  action="${ctx}/admin/fnb-dashboard/product/status">

                                                                <input type="hidden"
                                                                       name="id"
                                                                       value="${product.id}">

                                                                <input type="hidden"
                                                                       name="categoryId"
                                                                       value="${selectedCategoryId}">

                                                                <input type="hidden"
                                                                       name="status"
                                                                       value="${product.status == 'ACTIVE'
                                                                                ? 'INACTIVE'
                                                                                : 'ACTIVE'}">

                                                                <button type="submit"
                                                                        class="fnb-card-button
                                                                        ${product.status == 'ACTIVE'
                                                                          ? 'danger'
                                                                          : 'success'}">

                                                                    ${product.status == 'ACTIVE'
                                                                      ? 'Deactive'
                                                                      : 'Active'}

                                                                </button>

                                                            </form>

                                                        </div>

                                                    </article>

                                                </c:forEach>

                                            </div>

                                        </c:otherwise>

                                    </c:choose>

                                </c:if>

                          </div>

                    </section>

                </main>

                <!-- =========================================================
                     CATEGORY MODAL
                     ========================================================= -->

                <div id="categoryModal"
                     class="fnb-modal-overlay">

                    <div class="fnb-modal">

                        <div class="fnb-modal-header">

                            <h2 id="categoryModalTitle">
                                Thêm danh mục
                            </h2>

                            <button type="button"
                                    onclick="closeCategoryModal()">

                                ×

                            </button>

                        </div>

                        <form method="post"
                              action="${ctx}/admin/fnb-dashboard/category/save">

                            <input type="hidden"
                                   id="categoryId"
                                   name="id">

                            <div class="fnb-form-group">

                                <label for="categoryName">

                                    Tên danh mục

                                </label>

                                <input type="text"
                                       id="categoryName"
                                       name="name"
                                       maxlength="100"
                                       required>

                            </div>

                            <div class="fnb-form-group">

                                <label for="categoryDescription">

                                    Mô tả

                                </label>

                                <textarea id="categoryDescription"
                                          name="description"
                                          rows="4"></textarea>

                            </div>

                            <div class="fnb-modal-actions">

                                <button type="button"
                                        class="fnb-secondary-button"
                                        onclick="closeCategoryModal()">

                                    Hủy

                                </button>

                                <button type="submit"
                                        class="fnb-primary-button">

                                    Lưu danh mục

                                </button>

                            </div>

                        </form>

                    </div>

                </div>

                <!-- =========================================================
                     PRODUCT MODAL
                     FNB_PRODUCTS CHỈ LƯU ITEM
                     ========================================================= -->

                <div id="productModal"
                     class="fnb-modal-overlay">

                    <div class="fnb-modal">

                        <div class="fnb-modal-header">

                            <h2 id="productModalTitle">
                                Thêm sản phẩm
                            </h2>

                            <button type="button"
                                    onclick="closeProductModal()">

                                ×

                            </button>

                        </div>

                        <form method="post"
                              action="${ctx}/admin/fnb-dashboard/product/save">

                            <input type="hidden"
                                   id="productId"
                                   name="id">

                            <div class="fnb-form-group">

                                <label for="productCategoryId">

                                    Danh mục

                                </label>

                                <select id="productCategoryId"
                                        name="categoryId"
                                        required>

                                    <c:forEach var="category"
                                               items="${categories}">

                                        <c:if test="${fn:toLowerCase(fn:trim(category.name)) != 'combo'}">

                                            <option value="${category.id}">

                                                <c:out value="${category.name}" />

                                            </option>

                                        </c:if>

                                    </c:forEach>

                                </select>

                            </div>

                            <div class="fnb-form-group">

                                <label for="productName">

                                    Tên sản phẩm

                                </label>

                                <input type="text"
                                       id="productName"
                                       name="name"
                                       maxlength="150"
                                       required>

                            </div>

                            <div class="fnb-form-group">

                                <label for="productDescription">

                                    Mô tả

                                </label>

                                <textarea id="productDescription"
                                          name="description"
                                          rows="3"></textarea>

                            </div>

                            <div class="fnb-form-grid">

                                <div class="fnb-form-group">

                                    <label for="productType">

                                        Loại sản phẩm

                                    </label>

                                    <select id="productType"
                                            name="productType"
                                            required>

                                        <option value="ITEM">
                                            Sản phẩm bán lẻ
                                        </option>

                                    </select>

                                </div>

                                <div class="fnb-form-group">

                                    <label for="productSellingPrice">

                                        Giá bán

                                    </label>

                                    <input type="number"
                                           id="productSellingPrice"
                                           name="sellingPrice"
                                           min="0"
                                           step="1000"
                                           required>

                                </div>

                            </div>

                            <div class="fnb-form-group">

                                <label for="productImageUrl">

                                    Đường dẫn ảnh

                                </label>

                                <input type="text"
                                       id="productImageUrl"
                                       name="imageUrl">

                            </div>

                            <label class="fnb-checkbox-row">

                                <input type="checkbox"
                                       id="productAllowedToSell"
                                       name="allowedToSell"
                                       value="true">

                                Cho phép bán sản phẩm

                            </label>

                            <div class="fnb-modal-actions">

                                <button type="button"
                                        class="fnb-secondary-button"
                                        onclick="closeProductModal()">

                                    Hủy

                                </button>

                                <button type="submit"
                                        class="fnb-primary-button">

                                    Lưu sản phẩm

                                </button>

                            </div>

                        </form>

                    </div>

                </div>

                <!-- =========================================================
                     COMBO MODAL
                     ========================================================= -->

                <div id="comboModal"
                     class="fnb-modal-overlay">

                    <div class="fnb-modal fnb-combo-modal">

                        <div class="fnb-modal-header">

                            <div>
                                <h2 id="comboModalTitle">
                                    Thêm combo mới
                                </h2>

                                <p>
                                    Chọn sản phẩm và thiết lập giá bán cho combo.
                                </p>
                            </div>

                            <button type="button"
                                    class="fnb-modal-close"
                                    onclick="closeComboModal()">
                                ×
                            </button>

                        </div>

                        <form method="post"
                              id="comboForm"
                              action="${ctx}/admin/fnb-dashboard/combo/save"
                              onsubmit="return validateComboForm();">

                            <input type="hidden"
                                   id="comboId"
                                   name="id">

                            <input type="hidden"
                                   name="categoryId"
                                   value="${selectedCategoryId}">

                            <div class="combo-builder">

                                <!-- ========================================
                                     CỘT 1: THÔNG TIN COMBO
                                     ======================================== -->

                                <section class="combo-panel combo-info-panel">

                                    <div class="combo-panel-header">
                                        <span class="combo-step-number">1</span>

                                        <div>
                                            <h3>Thông tin combo</h3>
                                            <p>Nhập thông tin cơ bản</p>
                                        </div>
                                    </div>

                                    <div class="fnb-form-group">

                                        <label for="comboName">
                                            Tên combo
                                            <span class="required">*</span>
                                        </label>

                                        <input type="text"
                                               id="comboName"
                                               name="name"
                                               maxlength="150"
                                               placeholder="Ví dụ: Combo Cặp Đôi"
                                               required>

                                    </div>

                                    <div class="fnb-form-group">

                                        <label for="comboDescription">
                                            Mô tả
                                        </label>

                                        <textarea id="comboDescription"
                                                  name="description"
                                                  rows="4"
                                                  placeholder="Mô tả ngắn về combo"></textarea>

                                    </div>

                                    <div class="fnb-form-group">

                                        <label for="comboImageUrl">
                                            Ảnh combo
                                        </label>

                                        <input type="text"
                                               id="comboImageUrl"
                                               name="imageUrl"
                                               placeholder="assets/img/combo/example.jpg"
                                               oninput="previewComboImage()">

                                    </div>

                                    <div class="combo-image-preview">

                                        <img id="comboImagePreview"
                                             src="${ctx}/assets/img/fnb/popcorn_large.png"
                                             alt="Ảnh combo"
                                             onerror="this.onerror=null;this.src='${ctx}/assets/img/fnb/popcorn_large.png';">

                                    </div>

                                    <label class="combo-checkbox-card">

                                        <input type="checkbox"
                                               id="comboAllowedToSell"
                                               name="allowedToSell"
                                               value="true"
                                               checked>

                                        <span class="combo-checkbox-content">

                                            <strong>Cho phép bán combo</strong>

                                            <small>
                                                Combo có thể được bán sau khi tạo.
                                            </small>

                                        </span>

                                    </label>

                                </section>

                                <!-- ========================================
                                     CỘT 2: CHỌN SẢN PHẨM
                                     ======================================== -->

                                <section class="combo-panel combo-product-panel">

                                    <div class="combo-panel-header">

                                        <span class="combo-step-number">2</span>

                                        <div>
                                            <h3>Chọn sản phẩm</h3>
                                            <p>Thêm món vào combo</p>
                                        </div>

                                    </div>

                                    <div class="combo-search-box">

                                        <i class="bi bi-search"></i>

                                        <input type="text"
                                               id="comboProductSearch"
                                               placeholder="Tìm theo tên hoặc danh mục..."
                                               oninput="filterComboProducts()">

                                    </div>

                                    <div class="combo-product-list"
                                         id="comboProductList">

                                        <c:choose>

                                            <c:when test="${empty comboProducts}">

                                                <div class="combo-empty-state">

                                                    <i class="bi bi-box-seam"></i>

                                                    <strong>
                                                        Chưa có sản phẩm
                                                    </strong>

                                                    <span>
                                                        Hãy tạo sản phẩm ITEM trước.
                                                    </span>

                                                </div>

                                            </c:when>

                                            <c:otherwise>

                                                <c:forEach var="item"
                                                           items="${comboProducts}">

                                                    <article class="combo-product-item"
                                                             data-search-text="${fn:toLowerCase(item.name)} ${fn:toLowerCase(item.categoryName)}"
                                                             data-product-id="${item.id}"
                                                             data-product-name="<c:out value='${item.name}' />"
                                                             data-product-price="${item.sellingPrice}">

                                                        <div class="combo-product-image">

                                                            <c:choose>

                                                                <c:when test="${empty item.imageUrl}">
                                                                    <img src="${ctx}/assets/img/fnb/pepsi.png"
                                                                         alt="<c:out value='${item.name}'/>">
                                                                </c:when>

                                                                <c:when test="${item.imageUrl.startsWith('http://')
                                                                                or item.imageUrl.startsWith('https://')}">
                                                                        <img src="<c:out value='${item.imageUrl}'/>"
                                                                             alt="<c:out value='${item.name}'/>"
                                                                             onerror="this.onerror=null;this.src='${ctx}/assets/img/fnb/pepsi.png';">
                                                                </c:when>

                                                                <c:otherwise>
                                                                    <img src="${ctx}${item.imageUrl.startsWith('/') ? '' : '/'}<c:out value='${item.imageUrl}'/>"
                                                                         alt="<c:out value='${item.name}'/>"
                                                                         onerror="this.onerror=null;this.src='${ctx}/assets/img/fnb/pepsi.png';">
                                                                </c:otherwise>

                                                            </c:choose>

                                                        </div>

                                                        <div class="combo-product-details">

                                                            <div class="combo-product-title-row">

                                                                <h4>
                                                                    <c:out value="${item.name}" />
                                                                </h4>

                                                                <span class="combo-product-category">
                                                                    <c:out value="${item.categoryName}" />
                                                                </span>

                                                            </div>

                                                            <div class="combo-product-price">

                                                                <fmt:formatNumber
                                                                    value="${item.sellingPrice}"
                                                                    type="number"
                                                                    maxFractionDigits="0" />

                                                                ₫

                                                            </div>

                                                        </div>

                                                        <div class="combo-product-controls">

                                                            <div class="combo-quantity-control">

                                                                <button type="button"
                                                                        onclick="decreasePickerQuantity(this)">
                                                                    −
                                                                </button>

                                                                <input type="number"
                                                                       class="combo-item-quantity"
                                                                       value="1"
                                                                       min="1"
                                                                       max="99">

                                                                <button type="button"
                                                                        onclick="increasePickerQuantity(this)">
                                                                    +
                                                                </button>

                                                            </div>

                                                            <button type="button"
                                                                    class="combo-add-button"
                                                                    onclick="addComboItem(this)">

                                                                <i class="bi bi-plus-lg"></i>
                                                                Thêm

                                                            </button>

                                                        </div>

                                                    </article>

                                                </c:forEach>

                                            </c:otherwise>

                                        </c:choose>

                                    </div>

                                </section>

                                <!-- ========================================
                                     CỘT 3: COMBO ĐÃ CHỌN
                                     ======================================== -->

                                <section class="combo-panel combo-summary-panel">

                                    <div class="combo-panel-header">

                                        <span class="combo-step-number">3</span>

                                        <div>
                                            <h3>Combo đã chọn</h3>

                                            <p>
                                                <span id="comboItemCount">0</span>
                                                sản phẩm
                                            </p>
                                        </div>

                                    </div>

                                    <div id="comboSelectedItems"
                                         class="combo-selected-list">

                                        <div class="combo-empty-state">

                                            <i class="bi bi-basket"></i>

                                            <strong>
                                                Chưa có sản phẩm
                                            </strong>

                                            <span>
                                                Chọn sản phẩm ở danh sách bên trái.
                                            </span>

                                        </div>

                                    </div>

                                    <div id="comboHiddenInputs"></div>

                                    <div class="combo-price-summary">

                                        <div class="combo-summary-row">

                                            <span>Tổng giá sản phẩm</span>

                                            <strong id="comboTemporaryTotal">
                                                0 ₫
                                            </strong>

                                        </div>

                                        <div class="fnb-form-group combo-selling-price-group">

                                            <label for="comboSellingPrice">
                                                Giá bán combo
                                                <span class="required">*</span>
                                            </label>

                                            <div class="combo-price-input">

                                                <input type="number"
                                                       id="comboSellingPrice"
                                                       name="sellingPrice"
                                                       min="0"
                                                       step="1000"
                                                       placeholder="Nhập giá bán"
                                                       required
                                                       oninput="updateComboPriceSummary()">

                                                <span>₫</span>

                                            </div>

                                        </div>

                                        <div class="combo-saving-box">

                                            <span>
                                                Khách hàng tiết kiệm
                                            </span>

                                            <strong id="comboSavingAmount">
                                                0 ₫
                                            </strong>

                                        </div>

                                    </div>

                                </section>

                            </div>

                            <div class="combo-modal-footer">

                                <button type="button"
                                        class="fnb-secondary-button"
                                        onclick="closeComboModal()">

                                    Hủy

                                </button>

                                <button type="submit"
                                        class="fnb-primary-button">

                                    <i class="bi bi-check-lg"></i>
                                    Lưu combo

                                </button>

                            </div>

                        </form>

                    </div>

                </div>

                <!-- =========================================================
                     JAVASCRIPT
                     ========================================================= -->

                <script>
                    const comboItems = new Map();

                    // =========================================================
                    // CATEGORY MODAL
                    // =========================================================

                    function openCategoryModal() {

                        document.getElementById(
                                "categoryModalTitle"
                                ).textContent = "Thêm danh mục";

                        document.getElementById(
                                "categoryId"
                                ).value = "";

                        document.getElementById(
                                "categoryName"
                                ).value = "";

                        document.getElementById(
                                "categoryDescription"
                                ).value = "";

                        document.getElementById(
                                "categoryModal"
                                ).classList.add("show");
                    }

                    function openEditCategoryFromButton(button) {

                        openEditCategoryModal(
                                button.dataset.id,
                                button.dataset.name,
                                button.dataset.description
                                );
                    }

                    function openEditCategoryModal(
                            id,
                            name,
                            description) {

                        document.getElementById(
                                "categoryModalTitle"
                                ).textContent = "Cập nhật danh mục";

                        document.getElementById(
                                "categoryId"
                                ).value = id;

                        document.getElementById(
                                "categoryName"
                                ).value = name || "";

                        document.getElementById(
                                "categoryDescription"
                                ).value = description || "";

                        document.getElementById(
                                "categoryModal"
                                ).classList.add("show");
                    }

                    function closeCategoryModal() {

                        document.getElementById(
                                "categoryModal"
                                ).classList.remove("show");
                    }

                    // =========================================================
                    // PRODUCT MODAL
                    // =========================================================

                    function openProductModal() {

                        document.getElementById(
                                "productModalTitle"
                                ).textContent = "Thêm sản phẩm";

                        document.getElementById(
                                "productId"
                                ).value = "";

                        document.getElementById(
                                "productCategoryId"
                                ).value = "${selectedCategoryId}";

                        document.getElementById(
                                "productName"
                                ).value = "";

                        document.getElementById(
                                "productDescription"
                                ).value = "";

                        document.getElementById(
                                "productType"
                                ).value = "ITEM";

                        document.getElementById(
                                "productSellingPrice"
                                ).value = "";

                        document.getElementById(
                                "productImageUrl"
                                ).value = "";

                        document.getElementById(
                                "productAllowedToSell"
                                ).checked = true;

                        document.getElementById(
                                "productModal"
                                ).classList.add("show");
                    }

                    function openEditProductFromButton(button) {

                        openEditProductModal(
                                button.dataset.id,
                                button.dataset.categoryId,
                                button.dataset.name,
                                button.dataset.description,
                                button.dataset.productType,
                                button.dataset.sellingPrice,
                                button.dataset.imageUrl,
                                button.dataset.allowedToSell
                                );
                    }

                    function openEditProductModal(
                            id,
                            categoryId,
                            name,
                            description,
                            productType,
                            sellingPrice,
                            imageUrl,
                            allowedToSell) {

                        document.getElementById(
                                "productModalTitle"
                                ).textContent = "Cập nhật sản phẩm";

                        document.getElementById(
                                "productId"
                                ).value = id;

                        document.getElementById(
                                "productCategoryId"
                                ).value = categoryId;

                        document.getElementById(
                                "productName"
                                ).value = name || "";

                        document.getElementById(
                                "productDescription"
                                ).value = description || "";

                        document.getElementById(
                                "productType"
                                ).value = productType || "ITEM";

                        document.getElementById(
                                "productSellingPrice"
                                ).value = sellingPrice || "";

                        document.getElementById(
                                "productImageUrl"
                                ).value = imageUrl || "";

                        document.getElementById(
                                "productAllowedToSell"
                                ).checked = allowedToSell === "true";

                        document.getElementById(
                                "productModal"
                                ).classList.add("show");
                    }

                    function closeProductModal() {

                        document.getElementById(
                                "productModal"
                                ).classList.remove("show");
                    }

                    // =========================================================
                    // COMBO MODAL
                    // =========================================================



                    function openComboModal() {

                        comboItems.clear();

                        document.getElementById("comboForm").reset();
                        document.getElementById("comboModalTitle").textContent =
                                "Thêm combo mới";

                        document.getElementById("comboId").value = "";
                        document.getElementById("comboName").value = "";
                        document.getElementById("comboDescription").value = "";
                        document.getElementById("comboSellingPrice").value = "";
                        document.getElementById("comboImageUrl").value = "";
                        document.getElementById("comboAllowedToSell").checked = true;

                        document.getElementById("comboProductSearch").value = "";

                        document.querySelectorAll(".combo-product-item")
                                .forEach(function (item) {
                                    item.style.display = "";
                                });

                        resetProductPickerQuantities();
                        previewComboImage();
                        renderComboItems();

                        document.getElementById("comboModal")
                                .classList.add("show");

                        document.body.classList.add("modal-open");
                    }

                    function openEditComboFromButton(button) {

                        const comboId = button.dataset.id;

                        comboItems.clear();

                        document.getElementById("comboModalTitle").textContent =
                                "Cập nhật combo";

                        document.getElementById("comboId").value =
                                comboId;

                        document.getElementById("comboName").value =
                                button.dataset.name || "";

                        document.getElementById("comboDescription").value =
                                button.dataset.description || "";

                        document.getElementById("comboSellingPrice").value =
                                button.dataset.sellingPrice || "";

                        document.getElementById("comboImageUrl").value =
                                button.dataset.imageUrl || "";

                        document.getElementById("comboAllowedToSell").checked =
                                button.dataset.allowedToSell === "true";

                        document.getElementById("comboProductSearch").value = "";

                        resetProductPickerQuantities();
                        loadComboItemsForEdit(comboId);
                        previewComboImage();
                        renderComboItems();

                        document.getElementById("comboModal")
                                .classList.add("show");

                        document.body.classList.add("modal-open");
                    }

                    function closeComboModal() {

                        document.getElementById("comboModal")
                                .classList.remove("show");

                        document.body.classList.remove("modal-open");
                    }

                    function previewComboImage() {

                        const imageInput =
                                document.getElementById("comboImageUrl");

                        const preview =
                                document.getElementById("comboImagePreview");

                        const imagePath =
                                imageInput.value.trim();

                        if (!imagePath) {

                            preview.src =
                                    "${ctx}/assets/img/fnb/popcorn_large.png";

                            return;
                        }

                        if (imagePath.startsWith("http://")
                                || imagePath.startsWith("https://")) {

                            preview.src = imagePath;

                        } else {

                            const normalizedPath =
                                    imagePath.startsWith("/")
                                    ? imagePath.substring(1)
                                    : imagePath;

                            preview.src =
                                    "${ctx}/" + normalizedPath;
                        }
                    }

                    function filterComboProducts() {

                        const keyword =
                                document.getElementById(
                                        "comboProductSearch"
                                        ).value
                                .trim()
                                .toLowerCase();

                        document.querySelectorAll(
                                ".combo-product-item"
                                ).forEach(function (card) {

                            const searchText =
                                    card.dataset.searchText || "";

                            card.style.display =
                                    searchText.includes(keyword)
                                    ? ""
                                    : "none";
                        });
                    }

                    function increasePickerQuantity(button) {

                        const input =
                                button.parentElement.querySelector(
                                        ".combo-item-quantity"
                                        );

                        const currentValue =
                                Number(input.value) || 1;

                        input.value =
                                Math.min(currentValue + 1, 99);
                    }

                    function decreasePickerQuantity(button) {

                        const input =
                                button.parentElement.querySelector(
                                        ".combo-item-quantity"
                                        );

                        const currentValue =
                                Number(input.value) || 1;

                        input.value =
                                Math.max(currentValue - 1, 1);
                    }

                    function resetProductPickerQuantities() {

                        document.querySelectorAll(
                                ".combo-item-quantity"
                                ).forEach(function (input) {

                            input.value = 1;
                        });
                    }

                    function addComboItem(button) {

                        const card =
                                button.closest(".combo-product-item");

                        const quantityInput =
                                card.querySelector(
                                        ".combo-item-quantity"
                                        );

                        const productId =
                                Number(card.dataset.productId);

                        const productName =
                                card.dataset.productName;

                        const unitPrice =
                                parseFloat(card.dataset.productPrice);

                        if (isNaN(unitPrice)) {
                            alert("Giá sản phẩm không hợp lệ");
                            return;
                        }

                        const quantity =
                                Number(quantityInput.value);

                        if (!Number.isInteger(quantity)
                                || quantity < 1
                                || quantity > 99) {

                            alert("Số lượng phải nằm trong khoảng 1 đến 99.");
                            return;
                        }

                        if (comboItems.has(productId)) {

                            const currentItem =
                                    comboItems.get(productId);

                            const newQuantity =
                                    currentItem.quantity + quantity;

                            if (newQuantity > 99) {

                                alert(
                                        "Số lượng tối đa của một sản phẩm là 99."
                                        );

                                return;
                            }

                            currentItem.quantity =
                                    newQuantity;

                        } else {

                            comboItems.set(productId, {
                                productId: productId,
                                productName: productName,
                                unitPrice: unitPrice,
                                quantity: quantity
                            });
                        }

                        quantityInput.value = 1;

                        card.classList.add("selected");

                        setTimeout(function () {
                            card.classList.remove("selected");
                        }, 500);

                        renderComboItems();
                    }

                    function loadComboItemsForEdit(comboId) {

                        const dataContainer =
                                document.getElementById(
                                        "combo-data-" + comboId
                                        );

                        if (!dataContainer) {
                            return;
                        }

                        dataContainer.querySelectorAll(
                                ".combo-edit-item"
                                ).forEach(function (element) {

                            const productId =
                                    Number(element.dataset.productId);

                            comboItems.set(productId, {
                                productId: productId,
                                productName:
                                        element.dataset.productName,
                                unitPrice:
                                        Number(element.dataset.unitPrice),
                                quantity:
                                        Number(element.dataset.quantity)
                            });
                        });
                    }

                    function increaseSelectedQuantity(productId) {

                        const item =
                                comboItems.get(productId);

                        if (!item) {
                            return;
                        }

                        if (item.quantity >= 99) {

                            alert(
                                    "Số lượng tối đa của một sản phẩm là 99."
                                    );

                            return;
                        }

                        item.quantity++;

                        renderComboItems();
                    }

                    function decreaseSelectedQuantity(productId) {

                        const item =
                                comboItems.get(productId);

                        if (!item) {
                            return;
                        }

                        if (item.quantity <= 1) {

                            removeComboItem(productId);
                            return;
                        }

                        item.quantity--;

                        renderComboItems();
                    }

                    function changeComboQuantity(
                            productId,
                            value) {

                        const quantity =
                                Number(value);

                        if (!Number.isInteger(quantity)
                                || quantity < 1) {

                            removeComboItem(productId);
                            return;
                        }

                        if (quantity > 99) {

                            alert(
                                    "Số lượng tối đa của một sản phẩm là 99."
                                    );

                            renderComboItems();
                            return;
                        }

                        const item =
                                comboItems.get(productId);

                        if (item) {
                            item.quantity = quantity;
                        }

                        renderComboItems();
                    }

                    function removeComboItem(productId) {

                        comboItems.delete(productId);

                        renderComboItems();
                    }

                    function renderComboItems() {

                        const container =
                                document.getElementById(
                                        "comboSelectedItems"
                                        );

                        const hiddenInputs =
                                document.getElementById(
                                        "comboHiddenInputs"
                                        );

                        const itemCount =
                                document.getElementById(
                                        "comboItemCount"
                                        );

                        container.innerHTML = "";
                        hiddenInputs.innerHTML = "";

                        itemCount.textContent =
                                comboItems.size;

                        if (comboItems.size === 0) {

                            container.innerHTML =
                                    '<div class="combo-empty-state">'
                                    + '<i class="bi bi-basket"></i>'
                                    + '<strong>Chưa có sản phẩm</strong>'
                                    + '<span>Chọn sản phẩm ở danh sách bên trái.</span>'
                                    + '</div>';

                            updateComboPriceSummary();
                            return;
                        }

                        comboItems.forEach(function (item) {

                            const lineTotal =
                                    item.unitPrice * item.quantity;

                            const row =
                                    document.createElement("article");

                            row.className =
                                    "combo-selected-item";

                            row.innerHTML =
                                    '<div class="combo-selected-main">'
                                    + '<div class="combo-selected-icon">'
                                    + '<i class="bi bi-cup-straw"></i>'
                                    + '</div>'
                                    + '<div class="combo-selected-info">'
                                    + '<strong>'
                                    + escapeHtml(item.productName)
                                    + '</strong>'
                                    + '<span>'
                                    + formatMoney(item.unitPrice)
                                    + ' / sản phẩm'
                                    + '</span>'
                                    + '</div>'
                                    + '</div>'

                                    + '<div class="combo-selected-bottom">'

                                    + '<div class="combo-quantity-control selected-control">'
                                    + '<button type="button" '
                                    + 'onclick="decreaseSelectedQuantity('
                                    + item.productId
                                    + ')">−</button>'

                                    + '<input type="number" '
                                    + 'min="1" '
                                    + 'max="99" '
                                    + 'value="'
                                    + item.quantity
                                    + '" '
                                    + 'onchange="changeComboQuantity('
                                    + item.productId
                                    + ', this.value)">'

                                    + '<button type="button" '
                                    + 'onclick="increaseSelectedQuantity('
                                    + item.productId
                                    + ')">+</button>'
                                    + '</div>'

                                    + '<strong class="combo-line-total">'
                                    + formatMoney(lineTotal)
                                    + '</strong>'

                                    + '<button type="button" '
                                    + 'class="combo-remove-button" '
                                    + 'onclick="removeComboItem('
                                    + item.productId
                                    + ')" '
                                    + 'title="Xóa khỏi combo">'
                                    + '<i class="bi bi-trash"></i>'
                                    + '</button>'

                                    + '</div>';

                            container.appendChild(row);

                            const productInput =
                                    document.createElement("input");

                            productInput.type = "hidden";
                            productInput.name = "itemProductId";
                            productInput.value = item.productId;

                            hiddenInputs.appendChild(productInput);

                            const quantityInput =
                                    document.createElement("input");

                            quantityInput.type = "hidden";
                            quantityInput.name = "itemQuantity";
                            quantityInput.value = item.quantity;

                            hiddenInputs.appendChild(quantityInput);
                        });

                        updateComboPriceSummary();
                    }

                    function getComboTemporaryTotal() {

                        let total = 0;

                        comboItems.forEach(function (item) {

                            total +=
                                    item.unitPrice
                                    * item.quantity;
                        });

                        return total;
                    }

                    function updateComboPriceSummary() {

                        const temporaryTotal =
                                getComboTemporaryTotal();

                        const sellingPrice =
                                Number(
                                        document.getElementById(
                                                "comboSellingPrice"
                                                ).value
                                        ) || 0;

                        const saving =
                                Math.max(
                                        temporaryTotal - sellingPrice,
                                        0
                                        );

                        document.getElementById(
                                "comboTemporaryTotal"
                                ).textContent =
                                formatMoney(temporaryTotal);

                        document.getElementById(
                                "comboSavingAmount"
                                ).textContent =
                                formatMoney(saving);
                    }

                    function validateComboForm() {

                        if (comboItems.size === 0) {

                            alert(
                                    "Vui lòng thêm ít nhất một sản phẩm vào combo."
                                    );

                            return false;
                        }

                        const sellingPrice =
                                Number(
                                        document.getElementById(
                                                "comboSellingPrice"
                                                ).value
                                        );

                        if (!Number.isFinite(sellingPrice)
                                || sellingPrice < 0) {

                            alert("Giá bán combo không hợp lệ.");
                            return false;
                        }

                        const totalPrice =
                                getComboTemporaryTotal();

                        if (sellingPrice > totalPrice) {

                            alert(
                                    "Giá bán combo không được lớn hơn "
                                    + "tổng giá sản phẩm."
                                    );

                            return false;
                        }

                        return true;
                    }



                    function formatMoney(value) {

                        const numberValue =
                                Number(value) || 0;

                        return new Intl.NumberFormat(
                                "vi-VN"
                                ).format(numberValue) + " ₫";
                    }

                    function escapeHtml(value) {

                        const element =
                                document.createElement("div");

                        element.textContent =
                                value == null
                                ? ""
                                : String(value);

                        return element.innerHTML;
                    }


                    window.addEventListener(
                            "click",
                            function (event) {

                                if (event.target.id === "categoryModal") {
                                    closeCategoryModal();
                                }

                                if (event.target.id === "productModal") {
                                    closeProductModal();
                                }

                                if (event.target.id === "comboModal") {
                                    closeComboModal();
                                }
                            }
                    );

                    window.addEventListener(
                            "keydown",
                            function (event) {

                                if (event.key === "Escape") {
                                    closeCategoryModal();
                                    closeProductModal();
                                    closeComboModal();
                                }
                            }
                    );
                </script>

            </body>

        </html>
