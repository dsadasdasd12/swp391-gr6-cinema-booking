<%@page contentType="text/html" pageEncoding="UTF-8"%>
<%@taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<%@taglib prefix="fmt" uri="http://java.sun.com/jsp/jstl/fmt" %>

<section class="vouchers-card">
    <c:choose>
        <c:when test="${not empty discountList}">
            <table>
                <thead>
                    <tr>
                        <th>Mã Voucher</th>
                        <th>Trị giá giảm</th>
                        <th>Đơn tối thiểu</th>
                        <th>Tỉ lệ sử dụng</th>
                        <th>Thời hạn hiệu lực</th>
                        <th>Trạng thái</th>
                        <th style="text-align: center;">Thao tác</th>
                    </tr>
                </thead>
                <tbody>
                    <c:forEach items="${discountList}" var="d">
                        <tr>
                            <td>
                                <span class="code-text">${d.code}</span>
                            </td>
                            <td>
                                <span class="value-text">
                                    <c:choose>
                                        <c:when test="${d.discountType == 'PERCENT'}">
                                            Giảm ${d.discountValue}%
                                            <c:if test="${not empty d.maxDiscountAmount}">
                                                <span class="max-value">(Tối đa <fmt:formatNumber value="${d.maxDiscountAmount}" pattern="#,##0"/>đ)</span>
                                            </c:if>
                                        </c:when>
                                        <c:otherwise>
                                            Giảm <fmt:formatNumber value="${d.discountValue}" pattern="#,##0"/>đ
                                        </c:otherwise>
                                    </c:choose>
                                </span>
                            </td>
                            <td>
                                <span style="font-weight: 600;">
                                    <fmt:formatNumber value="${d.minOrderValue}" pattern="#,##0"/>đ
                                </span>
                            </td>
                            <td>
                                <div class="usage-container">
                                    <div class="usage-text">
                                        <span>${d.usedCount}/${d.maxUses} lượt</span>
                                        <span><fmt:formatNumber value="${(d.usedCount / d.maxUses) * 100}" pattern="#,##0"/>%</span>
                                    </div>
                                    <div class="progress-bar-bg">
                                        <div class="progress-bar-fill" style="width: ${(d.usedCount / d.maxUses) * 100}%"></div>
                                    </div>
                                </div>
                            </td>
                            <td style="font-size: 13px; color: var(--muted-text); line-height: 1.4;">
                                Từ: ${d.getFormattedStartDate()}<br>
                                Đến: ${d.getFormattedEndDate()}
                            </td>
                            <td>
                                <c:choose>
                                    <c:when test="${d.status == 'ACTIVE'}">
                                        <span class="badge badge-active">Hoạt động</span>
                                    </c:when>
                                    <c:when test="${d.status == 'PAUSED'}">
                                        <span class="badge badge-paused">Tạm dừng</span>
                                    </c:when>
                                    <c:otherwise>
                                        <span class="badge badge-expired">Hết hạn</span>
                                    </c:otherwise>
                                </c:choose>
                            </td>
                            <td style="text-align: center;">
                                <div class="actions-cell" style="justify-content: center;">
                                    <!-- Toggle Status Button -->
                                    <c:choose>
                                        <c:when test="${d.status == 'ACTIVE'}">
                                            <form action="DiscountManager" method="POST" style="display:inline;">
                                                <input type="hidden" name="action" value="updateStatus">
                                                <input type="hidden" name="id" value="${d.id}">
                                                <input type="hidden" name="status" value="PAUSED">
                                                <button type="submit" class="btn-action" title="Tạm dừng hoạt động">
                                                    <svg width="15" height="15" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2.5">
                                                        <rect x="6" y="4" width="4" height="16"></rect>
                                                        <rect x="14" y="4" width="4" height="16"></rect>
                                                    </svg>
                                                </button>
                                            </form>
                                        </c:when>
                                        <c:otherwise>
                                            <form action="DiscountManager" method="POST" style="display:inline;">
                                                <input type="hidden" name="action" value="updateStatus">
                                                <input type="hidden" name="id" value="${d.id}">
                                                <input type="hidden" name="status" value="ACTIVE">
                                                <button type="submit" class="btn-action" title="Kích hoạt hoạt động" style="color: #34d399;">
                                                    <svg width="15" height="15" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2.5">
                                                        <polygon points="5 3 19 12 5 21 5 3"></polygon>
                                                    </svg>
                                                </button>
                                            </form>
                                        </c:otherwise>
                                    </c:choose>

                                    <!-- Delete Button -->
                                    <form action="DiscountManager" method="POST" style="display:inline;" onsubmit="return confirm('Bạn có chắc chắn muốn xóa mã giảm giá này không?')">
                                        <input type="hidden" name="action" value="delete">
                                        <input type="hidden" name="id" value="${d.id}">
                                        <button type="submit" class="btn-action btn-delete" title="Xóa mã">
                                            <svg width="15" height="15" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2.5">
                                                <polyline points="3 6 5 6 21 6"></polyline>
                                                <path d="M19 6v14a2 2 0 0 1-2 2H7a2 2 0 0 1-2-2V6m3 0V4a2 2 0 0 1 2-2h4a2 2 0 0 1 2 2v2"></path>
                                            </svg>
                                        </button>
                                    </form>
                                </div>
                            </td>
                        </tr>
                    </c:forEach>
                </tbody>
            </table>
        </c:when>
        <c:otherwise>
            <div style="padding: 60px; text-align: center; color: var(--muted-text);">
                <svg width="64" height="64" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="1.5" style="margin-bottom: 15px; opacity: 0.5;">
                    <path d="M21 16V8a2 2 0 0 0-1-1.73l-7-4a2 2 0 0 0-2 0l-7 4A2 2 0 0 0 3 8v8a2 2 0 0 0 1 1.73l7 4a2 2 0 0 0 2 0l7-4A2 2 0 0 0 21 16z"></path>
                    <polyline points="3.27 6.96 12 12.01 20.73 6.96"></polyline>
                    <line x1="12" y1="22.08" x2="12" y2="12"></line>
                </svg>
                <p style="font-weight: 600; margin: 0; font-size: 16px;">Hệ thống chưa có mã giảm giá nào</p>
                <p style="font-size: 13px; margin: 5px 0 0 0;">Bấm nút "Thêm Mã Giảm Giá" để tạo chương trình khuyến mãi đầu tiên.</p>
            </div>
        </c:otherwise>
    </c:choose>
</section>
