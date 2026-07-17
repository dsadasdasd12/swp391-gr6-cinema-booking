<%@ page contentType="text/html; charset=UTF-8" pageEncoding="UTF-8" %>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<%@ taglib prefix="fmt" uri="http://java.sun.com/jsp/jstl/fmt" %>

<c:set var="ctx" value="${pageContext.request.contextPath}" />

<!DOCTYPE html>
<html lang="vi">
    <head>
        <meta charset="UTF-8">
        <meta name="viewport" content="width=device-width, initial-scale=1">
        <title>Chọn F&amp;B - RapViet Cinema</title>
        <link rel="stylesheet" href="${ctx}/assets/css/style.css">
        <link rel="stylesheet" href="${ctx}/assets/css/movie.css">

        <style>
            .fnb-tabs {
                display: grid;
                grid-template-columns: repeat(2, minmax(0, 1fr));
                gap: 14px;
                max-width: 620px;
                margin: 0 auto 28px;
                padding: 6px;
                border: 1px solid #293548;
                border-radius: 16px;
                background: #111827;
            }

            .fnb-tab {
                min-height: 58px;
                padding: 12px 18px;
                border: 1px solid transparent;
                border-radius: 11px;
                background: transparent;
                color: #cbd5e1;
                font-size: 16px;
                font-weight: 800;
                cursor: pointer;
                transition: 0.2s ease;
            }

            .fnb-tab:hover {
                color: #fff;
                border-color: #475569;
            }

            .fnb-tab.active {
                color: #fff;
                border-color: #ef4444;
                background: linear-gradient(135deg, #dc2626, #991b1b);
                box-shadow: 0 8px 24px rgba(220, 38, 38, 0.25);
            }

            .fnb-grid {
                display: grid;
                grid-template-columns: repeat(4, minmax(0, 1fr));
                gap: 18px;
            }

            .fnb-card {
                min-width: 0;
                overflow: hidden;
                display: flex;
                flex-direction: column;
                border: 1px solid #374151;
                border-radius: 14px;
                background: #111827;
            }

            .fnb-card[hidden] {
                display: none;
            }

            .fnb-image,
            .fnb-image-placeholder {
                width: 100%;
                height: 170px;
                object-fit: cover;
                display: block;
                background: #1f2937;
            }

            .fnb-body {
                flex: 1;
                display: flex;
                flex-direction: column;
                gap: 10px;
                padding: 17px;
            }

            .fnb-body h3 {
                margin: 0;
                color: #fff;
                font-size: 17px;
            }

            .fnb-desc {
                min-height: 40px;
                color: #9ca3af;
                line-height: 1.45;
            }

            .fnb-bottom {
                display: flex;
                align-items: center;
                justify-content: space-between;
                gap: 12px;
                margin-top: auto;
            }

            .fnb-price {
                color: #fff;
                white-space: nowrap;
            }

            .fnb-qty {
                width: 76px;
                padding: 9px 10px;
                border: 1px solid #4b5563;
                border-radius: 8px;
                outline: none;
                background: #030712;
                color: #fff;
                font-weight: 700;
            }

            .fnb-qty:focus {
                border-color: #ef4444;
                box-shadow: 0 0 0 3px rgba(239, 68, 68, 0.16);
            }

            .fnb-empty-filter {
                display: none;
                padding: 34px;
                border: 1px dashed #374151;
                border-radius: 14px;
                color: #9ca3af;
                text-align: center;
            }

            .skip {
                margin: 8px 0 22px;
                color: #9ca3af;
            }

            @media (max-width: 1050px) {
                .fnb-grid {
                    grid-template-columns: repeat(3, minmax(0, 1fr));
                }
            }

            @media (max-width: 760px) {
                .fnb-grid {
                    grid-template-columns: repeat(2, minmax(0, 1fr));
                }
            }

            @media (max-width: 520px) {
                .fnb-tabs, .fnb-grid {
                    grid-template-columns: 1fr;
                }
            }
        </style>
    </head>
    <body>
        <jsp:include page="/pages/common/header.jsp">
            <jsp:param name="active" value="booking"/>
        </jsp:include>

        <div class="page-wrap">
            <div class="container">
                <div class="flow-head">
                    <div>
                        <div class="flow-step">Bước 5 / 6</div>
                        <h1 class="page-title">Chọn bắp, nước và combo</h1>
                        <p class="skip">Bạn có thể bỏ qua bước này nếu không muốn mua F&amp;B.</p>
                    </div>
                </div>

                <c:if test="${not empty error}">
                    <div class="notice err"><c:out value="${error}"/></div>
                </c:if>

                <form method="post" action="${ctx}/booking/fnb">
                    <div class="fnb-tabs" role="tablist" aria-label="Loại F&B">
                        <button type="button" class="fnb-tab active" data-filter="PRODUCT"
                                role="tab" aria-selected="true">Mua lẻ</button>
                        <button type="button" class="fnb-tab" data-filter="COMBO"
                                role="tab" aria-selected="false">Combo</button>
                    </div>

                    <div class="fnb-grid" id="fnbGrid">
                        <c:forEach var="item" items="${fnbOptions}">
                            <div class="fnb-card" data-type="${item.itemType}">
                                <c:choose>
                                    <c:when test="${not empty item.imageUrl}">
                                        <img class="fnb-image" src="${item.imageUrl}"
                                             alt="${item.name}">
                                    </c:when>
                                    <c:otherwise>
                                        <div class="fnb-image-placeholder"></div>
                                    </c:otherwise>
                                </c:choose>

                                <div class="fnb-body">
                                    <h3><c:out value="${item.name}"/></h3>
                                    <c:if test="${not empty item.description}">
                                        <div class="fnb-desc"><c:out value="${item.description}"/></div>
                                    </c:if>

                                    <div class="fnb-bottom">
                                        <strong class="fnb-price">
                                            <fmt:formatNumber value="${item.unitPrice}" pattern="#,##0"/> đ
                                        </strong>
                                        <input class="fnb-qty" type="number" min="0"
                                               max="${item.availableQuantity}"
                                               name="qty_${item.itemType}_${item.itemId}"
                                               aria-label="Số lượng ${item.name}"
                                               value="${empty selectedFnb[item.itemType.concat(':').concat(item.itemId)] ? 0 : selectedFnb[item.itemType.concat(':').concat(item.itemId)]}">
                                    </div>
                                </div>
                            </div>
                        </c:forEach>
                    </div>

                    <div class="fnb-empty-filter" id="fnbEmptyFilter">
                        Chi nhánh hiện chưa có sản phẩm thuộc nhóm này.
                    </div>

                    <c:if test="${empty fnbOptions}">
                        <div class="empty"><h3>Chi nhánh hiện chưa có F&amp;B khả dụng</h3></div>
                    </c:if>

                    <div class="bd-actions" style="margin-top:24px">
                        <a class="btn btn-ghost"
                           href="${ctx}/booking/seats?showtimeId=${draftView.showtime.id}">Quay lại chọn ghế</a>
                        <button class="btn btn-primary" type="submit">Tiếp tục xác nhận</button>
                    </div>
                </form>
            </div>
        </div>

        <jsp:include page="/pages/common/footer.jsp"/>

        <script>
            (function () {
                const tabs = document.querySelectorAll('.fnb-tab');
                const cards = document.querySelectorAll('.fnb-card');
                const emptyMessage = document.getElementById('fnbEmptyFilter');

                function showType(type) {
                    let visibleCount = 0;

                    cards.forEach(function (card) {
                        const visible = card.dataset.type === type;
                        card.hidden = !visible;
                        if (visible)
                            visibleCount++;
                    });

                    tabs.forEach(function (tab) {
                        const active = tab.dataset.filter === type;
                        tab.classList.toggle('active', active);
                        tab.setAttribute('aria-selected', active ? 'true' : 'false');
                    });

                    emptyMessage.style.display = visibleCount === 0 ? 'block' : 'none';
                }

                tabs.forEach(function (tab) {
                    tab.addEventListener('click', function () {
                        showType(tab.dataset.filter);
                    });
                });

                showType('PRODUCT');
            })();
        </script>
    </body>
</html>
