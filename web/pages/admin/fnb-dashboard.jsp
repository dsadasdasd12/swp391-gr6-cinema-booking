<%@ page contentType="text/html; charset=UTF-8"
         pageEncoding="UTF-8" %>

<%@ taglib prefix="c"
           uri="http://java.sun.com/jsp/jstl/core" %>

<%@ taglib prefix="fmt"
           uri="http://java.sun.com/jsp/jstl/fmt" %>

<c:set var="ctx"
       value="${pageContext.request.contextPath}" />

<c:set var="pageTitle"
       value="Quản lý F&B - RapViet Cinema"
       scope="request" />
<head>
    <%@ include file="/pages/shared/header-admin.jsp" %>

    <link rel="stylesheet"
          href="${ctx}/assets/css/admin/fnbmanagement.css?v=1">


</head>
<div id="categoryModal" class="fnb-modal-overlay">
    <div class="fnb-modal">

        <div class="fnb-modal-header">
            <h2 id="categoryModalTitle">Thêm danh mục</h2>

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
                <label for="categoryName">Tên danh mục</label>

                <input type="text"
                       id="categoryName"
                       name="name"
                       maxlength="100"
                       required>
            </div>

            <div class="fnb-form-group">
                <label for="categoryDescription">Mô tả</label>

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



<div id="productModal" class="fnb-modal-overlay">
    <div class="fnb-modal">

        <div class="fnb-modal-header">
            <h2 id="productModalTitle">Thêm sản phẩm</h2>

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
                <label>Danh mục</label>

                <select id="productCategoryId"
                        name="categoryId"
                        required>

                    <c:forEach var="category"
                               items="${categories}">
                        <option value="${category.id}">
                            <c:out value="${category.name}" />
                        </option>
                    </c:forEach>
                </select>
            </div>

            <div class="fnb-form-group">
                <label>Tên sản phẩm</label>

                <input type="text"
                       id="productName"
                       name="name"
                       required>
            </div>

            <div class="fnb-form-group">
                <label>Mô tả</label>

                <textarea id="productDescription"
                          name="description"
                          rows="3"></textarea>
            </div>

            <div class="fnb-form-grid">

                <div class="fnb-form-group">
                    <label>Loại sản phẩm</label>

                    <select id="productType"
                            name="productType"
                            required>
                        <option value="ITEM">Đồ ăn / nước uống</option>
                        <option value="COMBO">Combo</option>
                    </select>
                </div>

                <div class="fnb-form-group">
                    <label>Giá bán</label>

                    <input type="number"
                           id="productSellingPrice"
                           name="sellingPrice"
                           min="0"
                           step="1000"
                           required>
                </div>

            </div>

            <div class="fnb-form-group">
                <label>Đường dẫn ảnh</label>

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

<script>
    function openCategoryModal() {
        document.getElementById("categoryModalTitle").textContent =
                "Thêm danh mục";

        document.getElementById("categoryId").value = "";
        document.getElementById("categoryName").value = "";
        document.getElementById("categoryDescription").value = "";

        document.getElementById("categoryModal")
                .classList.add("show");
    }

    function openEditCategoryModal(id, name, description) {
        document.getElementById("categoryModalTitle").textContent =
                "Cập nhật danh mục";

        document.getElementById("categoryId").value = id;
        document.getElementById("categoryName").value = name || "";
        document.getElementById("categoryDescription").value =
                description || "";

        document.getElementById("categoryModal")
                .classList.add("show");
    }

    function closeCategoryModal() {
        document.getElementById("categoryModal")
                .classList.remove("show");
    }

    function openProductModal() {
        document.getElementById("productModalTitle").textContent =
                "Thêm sản phẩm";

        document.getElementById("productId").value = "";
        document.getElementById("productCategoryId").value =
                "${selectedCategoryId}";
        document.getElementById("productName").value = "";
        document.getElementById("productDescription").value = "";
        document.getElementById("productType").value = "ITEM";
        document.getElementById("productSellingPrice").value = "";
        document.getElementById("productImageUrl").value = "";
        document.getElementById("productAllowedToSell").checked = true;

        document.getElementById("productModal")
                .classList.add("show");
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

        document.getElementById("productModalTitle").textContent =
                "Cập nhật sản phẩm";

        document.getElementById("productId").value = id;
        document.getElementById("productCategoryId").value = categoryId;
        document.getElementById("productName").value = name || "";
        document.getElementById("productDescription").value =
                description || "";
        document.getElementById("productType").value = productType;
        document.getElementById("productSellingPrice").value =
                sellingPrice;
        document.getElementById("productImageUrl").value =
                imageUrl || "";
        document.getElementById("productAllowedToSell").checked =
                allowedToSell === "true";

        document.getElementById("productModal")
                .classList.add("show");
    }

    function closeProductModal() {
        document.getElementById("productModal")
                .classList.remove("show");
    }
</script>

<body>
    <%@ include file="/pages/shared/sidebar-admin.jsp" %>
    <section class="fnb-admin-page">

        <div class="fnb-page-header">
            <div>
                <h1>Quản lý F&amp;B</h1>

                <p>
                    Quản lý danh mục, sản phẩm, combo,
                    giá bán và quyền được phép bán.
                </p>
            </div>
        </div>

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

        <div class="fnb-section">
            <div class="fnb-section-header">
                <div class="fnb-section-header">
                    <div>
                        <h2>Danh mục F&amp;B</h2>
                        <p>Chọn một danh mục để xem sản phẩm.</p>
                    </div>

                    <button type="button"
                            class="fnb-primary-button"
                            onclick="openCategoryModal()">
                        + Thêm danh mục
                    </button>
                </div>
            </div>

            <div class="fnb-category-grid">

                <c:forEach var="category"
                           items="${categories}">

                    <a href="${ctx}/admin/fnb-dashboard?categoryId=${category.id}"
                       class="fnb-category-card
                       ${selectedCategoryId == category.id ? 'active' : ''}">

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
                                    onclick="event.preventDefault();
                                            event.stopPropagation();
                                            openEditCategoryModal(
                                                    '${category.id}',
                                                    '${category.name}',
                                                    '${category.description}'
                                                    )">
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
                            ${category.productCount} sản phẩm
                        </span>


                    </a>

                </c:forEach>

            </div>
        </div>

        <div class="fnb-section">

            <div class="fnb-section-header">
                <div>
                    <h2>Sản phẩm trong danh mục</h2>
                </div>

                <c:if test="${selectedCategoryId > 0}">
                    <button type="button"
                            class="fnb-primary-button"
                            onclick="openProductModal()">
                        + Thêm sản phẩm
                    </button>
                </c:if>
            </div>
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
                                    <img
                                        src="${ctx}/${not empty product.imageUrl
                                               ? product.imageUrl
                                               : 'assets/img/default-fnb.png'}"
                                        alt="${product.name}"
                                        onerror="this.src='${ctx}/assets/img/default-fnb.png'">
                                </div>

                                <div class="fnb-product-body">

                                    <div class="fnb-product-type">
                                        <c:out value="${product.productType}" />
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
                                               value="${product.allowedToSell ? 'false' : 'true'}">

                                        <label class="fnb-switch-row">
                                            <span>
                                                Cho phép bán
                                            </span>

                                            <button type="submit"
                                                    class="fnb-switch ${product.allowedToSell ? 'enabled' : ''}"
                                                    ${product.status != 'ACTIVE' ? 'disabled' : ''}
                                                    aria-label="Bật hoặc tắt bán sản phẩm">

                                                <span class="fnb-switch-circle"></span>
                                            </button>
                                        </label>
                                    </form>
                                </div>
                                <div class="fnb-product-actions">

                                    <button type="button"
                                            class="fnb-card-button"
                                            onclick="openEditProductModal(
                                                            '${product.id}',
                                                            '${product.categoryId}',
                                                            '${product.name}',
                                                            '${product.description}',
                                                            '${product.productType}',
                                                            '${product.sellingPrice}',
                                                            '${product.imageUrl}',
                                                            '${product.allowedToSell}'
                                                            )">
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
            </div>

        </section>

    </main>
</div>

</body>

</html>