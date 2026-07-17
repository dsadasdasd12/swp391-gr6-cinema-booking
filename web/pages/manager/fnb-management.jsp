<%@ page contentType="text/html; charset=UTF-8" pageEncoding="UTF-8" %>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<%@ taglib prefix="fmt" uri="http://java.sun.com/jsp/jstl/fmt" %>

<c:set var="ctx" value="${pageContext.request.contextPath}" />
<c:set var="pageTitle" value="Quản lý kho F&B - Rạp Việt CMS" scope="request" />
<c:set var="extraCss" scope="request">
    <link rel="stylesheet" href="${ctx}/assets/css/admin/fnbmanagement.css?v=manager-card-v2">
</c:set>

<jsp:include page="/pages/shared/header-admin.jsp" />
<jsp:include page="/pages/shared/sidebar-admin.jsp" />

<c:set var="currentTab"
       value="${not empty requestScope.activeTab
                ? requestScope.activeTab
                : (not empty param.tab ? param.tab : 'items')}" />

<section class="manager-fnb-page">

    <div class="manager-fnb-hero">
        <div>
            <div class="rv-breadcrumb manager-fnb-breadcrumb">
                <a href="${ctx}/manager/dashboard" class="rv-breadcrumb__link">
                    Quản lý chi nhánh
                </a>
                <i class="bi bi-chevron-right"></i>
                <span class="rv-breadcrumb__current">Kho F&amp;B</span>
            </div>

            <h1 class="manager-fnb-title">Quản lý kho F&amp;B</h1>

            <p class="manager-fnb-subtitle">
                Cập nhật tồn kho và trạng thái bán tại
                <strong>
                    <c:out value="${empty branchName
                                    ? 'chi nhánh đang quản lý'
                                    : branchName}" />
                </strong>.
            </p>
        </div>

        <span class="manager-fnb-role">
            <i class="bi bi-person-workspace"></i>
            Branch Manager
        </span>
    </div>

    <div class="manager-fnb-summary">
        <article class="manager-fnb-stat">
            <span class="manager-fnb-stat__icon">
                <i class="bi bi-cup-straw"></i>
            </span>
            <div>
                <strong><c:out value="${empty totalItems ? 0 : totalItems}" /></strong>
                <span>Tổng số món</span>
            </div>
        </article>

        <article class="manager-fnb-stat">
            <span class="manager-fnb-stat__icon manager-fnb-stat__icon--success">
                <i class="bi bi-toggle-on"></i>
            </span>
            <div>
                <strong><c:out value="${empty enabledItems ? 0 : enabledItems}" /></strong>
                <span>Món đang bật bán</span>
            </div>
        </article>

        <article class="manager-fnb-stat">
            <span class="manager-fnb-stat__icon manager-fnb-stat__icon--info">
                <i class="bi bi-box2-heart-fill"></i>
            </span>
            <div>
                <strong><c:out value="${empty totalCombos ? 0 : totalCombos}" /></strong>
                <span>Tổng số combo</span>
            </div>
        </article>

        <article class="manager-fnb-stat">
            <span class="manager-fnb-stat__icon manager-fnb-stat__icon--warning">
                <i class="bi bi-exclamation-triangle-fill"></i>
            </span>
            <div>
                <strong><c:out value="${empty lowStockItems ? 0 : lowStockItems}" /></strong>
                <span>Món sắp hết</span>
            </div>
        </article>
    </div>

    <section class="manager-fnb-panel">
        <div class="manager-fnb-panel__top">
            <div>
                <h2>
                    <i class="bi bi-box-seam-fill"></i>
                    Danh sách kho F&amp;B
                </h2>
                <p>Hiển thị dạng thẻ, tối đa 5 món trên mỗi hàng.</p>
            </div>

            <div class="manager-fnb-tabs">
                <a href="${ctx}/manager/fnb?tab=items"
                   class="manager-fnb-tab ${currentTab eq 'items' ? 'active' : ''}">
                    <i class="bi bi-cup-straw"></i>
                    Món lẻ
                </a>

                <a href="${ctx}/manager/fnb?tab=combos"
                   class="manager-fnb-tab ${currentTab eq 'combos' ? 'active' : ''}">
                    <i class="bi bi-box2-heart-fill"></i>
                    Combo
                </a>
            </div>
        </div>

        <div class="manager-fnb-toolbar">
            <label class="manager-fnb-search">
                <i class="bi bi-search"></i>
                <input id="fnbSearchInput"
                       type="search"
                       placeholder="Tìm theo tên món hoặc mã..."
                       autocomplete="off">
            </label>

            <c:if test="${currentTab eq 'items'}">
                <label class="manager-fnb-filter">
                    <i class="bi bi-grid"></i>
                    <select id="fnbCategoryFilter">
                        <option value="">Tất cả danh mục</option>
                    </select>
                </label>
            </c:if>

            <label class="manager-fnb-filter">
                <i class="bi bi-funnel"></i>
                <select id="fnbStatusFilter">
                    <option value="">Tất cả trạng thái</option>
                    <option value="enabled">Đang bán</option>
                    <option value="disabled">Không bán</option>
                    <option value="unavailable">Không khả dụng</option>
                </select>
            </label>

            <span id="fnbResultCount" class="manager-fnb-result-count"></span>
        </div>

        <c:choose>
            <c:when test="${currentTab eq 'items'}">
                <c:choose>
                    <c:when test="${empty items}">
                        <div class="manager-fnb-empty">
                            <i class="bi bi-inbox"></i>
                            <h3>Chưa có món F&amp;B</h3>
                            <p>Admin chưa tạo món nào để quản lý tại chi nhánh này.</p>
                        </div>
                    </c:when>

                    <c:otherwise>
                        <div id="fnbCardGrid" class="manager-fnb-grid">
                            <c:forEach var="item" items="${items}">
                                <c:set var="systemAvailable"
                                       value="${item.status eq 'ACTIVE' and item.allowedToSell}" />

                                <article class="manager-fnb-card"
                                         data-name="<c:out value='${item.productName}' />"
                                         data-code="${item.productId}"
                                         data-category="<c:out value='${item.categoryName}' />"
                                         data-status="${not systemAvailable
                                                        ? 'unavailable'
                                                        : (item.enabledAtBranch ? 'enabled' : 'disabled')}">

                                    <div class="manager-fnb-card__image-wrap">
                                        <img src="${empty item.imageUrl
                                                    ? ctx.concat('/assets/images/no-image.png')
                                                    : item.imageUrl}"
                                             alt="<c:out value='${item.productName}' />"
                                             class="manager-fnb-card__image"
                                             onerror="this.onerror=null;this.src='${ctx}/assets/images/no-image.png';">

                                        <span class="manager-fnb-card__code">
                                            #<c:out value="${item.productId}" />
                                        </span>

                                        <c:choose>
                                            <c:when test="${systemAvailable}">
                                                <span class="manager-fnb-card__availability manager-fnb-card__availability--ok">
                                                    <i class="bi bi-check-circle-fill"></i>
                                                    Có thể bán
                                                </span>
                                            </c:when>
                                            <c:otherwise>
                                                <span class="manager-fnb-card__availability manager-fnb-card__availability--off">
                                                    <i class="bi bi-x-circle-fill"></i>
                                                    Admin ngừng bán
                                                </span>
                                            </c:otherwise>
                                        </c:choose>
                                    </div>

                                    <div class="manager-fnb-card__body">
                                        <div class="manager-fnb-card__heading">
                                            <div>
                                                <span class="manager-fnb-card__category">
                                                    <c:out value="${item.categoryName}" />
                                                </span>
                                                <h3><c:out value="${item.productName}" /></h3>
                                            </div>

                                            <strong class="manager-fnb-card__price">
                                                <fmt:formatNumber value="${item.sellingPrice}"
                                                                  type="number"
                                                                  groupingUsed="true" />₫
                                            </strong>
                                        </div>

                                        <div class="manager-fnb-card__divider"></div>

                                        <form method="post"
                                              action="${ctx}/manager/fnb/update-stock"
                                              class="manager-fnb-stock-form">

                                            <input type="hidden"
                                                   name="productId"
                                                   value="${item.productId}">

                                            <input type="hidden"
                                                   name="tab"
                                                   value="items">

                                            <div class="manager-fnb-stock-label">
                                                <span>Quản lý tồn kho</span>

                                                <c:if test="${item.stockQuantity <= 5}">
                                                    <span class="manager-fnb-low-stock">
                                                        <i class="bi bi-exclamation-triangle-fill"></i>
                                                        Sắp hết
                                                    </span>
                                                </c:if>
                                            </div>

                                            <div class="manager-fnb-stock-grid">

                                                <div class="manager-fnb-current-stock">
                                                    <span class="manager-fnb-current-stock__label">
                                                        Tổng còn trong kho
                                                    </span>

                                                    <strong class="manager-fnb-current-stock__value">
                                                        <c:out value="${item.stockQuantity}" />
                                                    </strong>

                                                    <span class="manager-fnb-current-stock__unit">
                                                        sản phẩm
                                                    </span>
                                                </div>

                                                <div class="manager-fnb-stock-update">
                                                    <label for="stock-change-${item.productId}">
                                                        Số lượng thay đổi
                                                    </label>

                                                    <div class="manager-fnb-stock-update__control">
                                                        <input id="stock-change-${item.productId}"
                                                               type="number"
                                                               name="stockChange"
                                                               min="${-item.stockQuantity}"
                                                               max="999999"
                                                               placeholder="+20 hoặc -5"
                                                               required>

                                                        <button type="submit"
                                                                class="manager-fnb-save">
                                                            <i class="bi bi-floppy-fill"></i>
                                                            Cập nhật
                                                        </button>
                                                    </div>

                                                    <span class="manager-fnb-stock-hint">
                                                        Số dương để nhập thêm, số âm để xuất kho.
                                                    </span>
                                                </div>

                                            </div>
                                        </form>

                                        <div class="manager-fnb-card__footer">
                                            <div>
                                                <span class="manager-fnb-card__footer-label">Bán tại chi nhánh</span>
                                                <strong>
                                                    <c:out value="${item.enabledAtBranch
                                                                    ? 'Đang bán'
                                                                    : 'Không bán'}" />
                                                </strong>
                                            </div>

                                            <c:choose>
                                                <c:when test="${systemAvailable}">
                                                    <form method="post"
                                                          action="${ctx}/manager/fnb/toggle-item"
                                                          class="manager-fnb-toggle-form">
                                                        <input type="hidden" name="productId" value="${item.productId}">
                                                        <input type="hidden" name="tab" value="items">
                                                        <input type="hidden"
                                                               name="enabled"
                                                               value="${not item.enabledAtBranch}">

                                                        <label class="manager-fnb-switch">
                                                            <input type="checkbox"
                                                                   ${item.enabledAtBranch ? 'checked' : ''}
                                                                   onchange="this.form.submit()">
                                                            <span></span>
                                                        </label>
                                                    </form>
                                                </c:when>

                                                <c:otherwise>
                                                    <label class="manager-fnb-switch is-disabled">
                                                        <input type="checkbox" disabled>
                                                        <span></span>
                                                    </label>
                                                </c:otherwise>
                                            </c:choose>
                                        </div>
                                    </div>
                                </article>
                            </c:forEach>
                        </div>
                    </c:otherwise>
                </c:choose>
            </c:when>

            <c:otherwise>
                <c:choose>
                    <c:when test="${empty combos}">
                        <div class="manager-fnb-empty">
                            <i class="bi bi-inbox"></i>
                            <h3>Chưa có combo F&amp;B</h3>
                            <p>Admin chưa tạo combo nào để quản lý tại chi nhánh này.</p>
                        </div>
                    </c:when>

                    <c:otherwise>
                        <div id="fnbCardGrid" class="manager-fnb-grid">
                            <c:forEach var="combo" items="${combos}">
                                <c:set var="comboSystemAvailable"
                                       value="${combo.status eq 'ACTIVE' and combo.allowedToSell}" />
                                <c:set var="comboSellable"
                                       value="${comboSystemAvailable and combo.availableQuantity > 0}" />

                                <article class="manager-fnb-card"
                                         data-name="<c:out value='${combo.comboName}' />"
                                         data-code="${combo.comboId}"
                                         data-category="Combo"
                                         data-status="${not comboSellable
                                                        ? 'unavailable'
                                                        : (combo.enabledAtBranch ? 'enabled' : 'disabled')}">

                                    <div class="manager-fnb-card__image-wrap">
                                        <img src="${empty combo.imageUrl
                                                    ? ctx.concat('/assets/images/no-image.png')
                                                    : combo.imageUrl}"
                                             alt="<c:out value='${combo.comboName}' />"
                                             class="manager-fnb-card__image"
                                             onerror="this.onerror=null;this.src='${ctx}/assets/images/no-image.png';">

                                        <span class="manager-fnb-card__code">
                                            #<c:out value="${combo.comboId}" />
                                        </span>

                                        <c:choose>
                                            <c:when test="${comboSellable}">
                                                <span class="manager-fnb-card__availability manager-fnb-card__availability--ok">
                                                    <i class="bi bi-check-circle-fill"></i>
                                                    Có thể bán
                                                </span>
                                            </c:when>
                                            <c:when test="${not comboSystemAvailable}">
                                                <span class="manager-fnb-card__availability manager-fnb-card__availability--off">
                                                    <i class="bi bi-x-circle-fill"></i>
                                                    Admin ngừng bán
                                                </span>
                                            </c:when>
                                            <c:otherwise>
                                                <span class="manager-fnb-card__availability manager-fnb-card__availability--warning">
                                                    <i class="bi bi-exclamation-triangle-fill"></i>
                                                    Thiếu nguyên liệu
                                                </span>
                                            </c:otherwise>
                                        </c:choose>
                                    </div>

                                    <div class="manager-fnb-card__body">
                                        <div class="manager-fnb-card__heading">
                                            <div>
                                                <span class="manager-fnb-card__category">Combo</span>
                                                <h3><c:out value="${combo.comboName}" /></h3>
                                            </div>

                                            <strong class="manager-fnb-card__price">
                                                <fmt:formatNumber value="${combo.sellingPrice}"
                                                                  type="number"
                                                                  groupingUsed="true" />₫
                                            </strong>
                                        </div>

                                        <div class="manager-fnb-combo-items">
                                            <i class="bi bi-list-check"></i>
                                            <span>
                                                <c:out value="${empty combo.itemSummary
                                                                ? 'Chưa có thành phần'
                                                                : combo.itemSummary}" />
                                            </span>
                                        </div>

                                        <div class="manager-fnb-card__divider"></div>

                                        <div class="manager-fnb-combo-stock">
                                            <span>Số lượng có thể bán</span>
                                            <strong class="${combo.availableQuantity <= 5
                                                             ? 'is-low'
                                                             : ''}">
                                                    <c:out value="${combo.availableQuantity}" />
                                                    combo
                                            </strong>
                                        </div>

                                        <div class="manager-fnb-card__footer">
                                            <div>
                                                <span class="manager-fnb-card__footer-label">Bán tại chi nhánh</span>
                                                <strong>
                                                    <c:out value="${combo.enabledAtBranch
                                                                    ? 'Đang bán'
                                                                    : 'Không bán'}" />
                                                </strong>
                                            </div>

                                            <c:choose>
                                                <c:when test="${comboSellable}">
                                                    <form method="post"
                                                          action="${ctx}/manager/fnb/toggle-combo"
                                                          class="manager-fnb-toggle-form">
                                                        <input type="hidden" name="comboId" value="${combo.comboId}">
                                                        <input type="hidden" name="tab" value="combos">
                                                        <input type="hidden"
                                                               name="enabled"
                                                               value="${not combo.enabledAtBranch}">

                                                        <label class="manager-fnb-switch">
                                                            <input type="checkbox"
                                                                   ${combo.enabledAtBranch ? 'checked' : ''}
                                                                   onchange="this.form.submit()">
                                                            <span></span>
                                                        </label>
                                                    </form>
                                                </c:when>

                                                <c:otherwise>
                                                    <label class="manager-fnb-switch is-disabled">
                                                        <input type="checkbox" disabled>
                                                        <span></span>
                                                    </label>
                                                </c:otherwise>
                                            </c:choose>
                                        </div>
                                    </div>
                                </article>
                            </c:forEach>
                        </div>
                    </c:otherwise>
                </c:choose>
            </c:otherwise>
        </c:choose>

        <div id="fnbNoResult" class="manager-fnb-empty manager-fnb-empty--filtered" hidden>
            <i class="bi bi-search"></i>
            <h3>Không tìm thấy kết quả</h3>
            <p>Hãy thử thay đổi từ khóa hoặc bộ lọc.</p>
        </div>
    </section>
</section>

<script>
    document.addEventListener("DOMContentLoaded", function () {
        const grid = document.getElementById("fnbCardGrid");
        if (!grid)
            return;

        const cards = Array.from(grid.querySelectorAll(".manager-fnb-card"));
        const searchInput = document.getElementById("fnbSearchInput");
        const categoryFilter = document.getElementById("fnbCategoryFilter");
        const statusFilter = document.getElementById("fnbStatusFilter");
        const resultCount = document.getElementById("fnbResultCount");
        const noResult = document.getElementById("fnbNoResult");

        if (categoryFilter) {
            const categories = [...new Set(
                        cards.map(card => card.dataset.category).filter(Boolean)
                        )].sort((a, b) => a.localeCompare(b, "vi"));

            categories.forEach(category => {
                const option = document.createElement("option");
                option.value = category.toLowerCase();
                option.textContent = category;
                categoryFilter.appendChild(option);
            });
        }

        const normalize = value => (value || "")
                    .toString()
                    .normalize("NFD")
                    .replace(/[\u0300-\u036f]/g, "")
                    .toLowerCase()
                    .trim();

        function applyFilters() {
            const keyword = normalize(searchInput ? searchInput.value : "");
            const category = normalize(categoryFilter ? categoryFilter.value : "");
            const status = statusFilter ? statusFilter.value : "";

            let visible = 0;

            cards.forEach(card => {
                const searchable = normalize(
                        card.dataset.name + " " +
                        card.dataset.code + " " +
                        card.dataset.category
                        );

                const matchesKeyword = !keyword || searchable.includes(keyword);
                const matchesCategory = !category ||
                        normalize(card.dataset.category) === category;
                const matchesStatus = !status || card.dataset.status === status;

                const show = matchesKeyword && matchesCategory && matchesStatus;
                card.hidden = !show;

                if (show)
                    visible++;
            });

            if (resultCount) {
                resultCount.textContent = visible + " / " + cards.length + " kết quả";
            }

            if (noResult) {
                noResult.hidden = visible !== 0;
            }
        }

        [searchInput, categoryFilter, statusFilter]
                .filter(Boolean)
                .forEach(control => {
                    control.addEventListener("input", applyFilters);
                    control.addEventListener("change", applyFilters);
                });


        applyFilters();
    });
</script>

</main>
</div>
</body>
</html>