<%@page contentType="text/html" pageEncoding="UTF-8"%>
<%@taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>

<!-- Admin voucher creation modal -->
<div id="addDiscountModal" class="custom-discount-modal">
    <div class="custom-discount-modal-content">
        <h3 class="custom-discount-modal-title">Tạo Mã Giảm Giá Mới</h3>
        
        <form action="DiscountManager" method="POST" onsubmit="return validateForm()">
            <input type="hidden" name="action" value="create">
            
            <div class="form-grid">
                <div class="form-group full-width">
                    <label for="code">Tên Mã Giảm Giá</label>
                    <input type="text" id="code" name="code" placeholder="Ví dụ: DONGGIA70K, CHAOSONG20" required autocomplete="off">
                </div>
                
                <div class="form-group">
                    <label for="discountType">Loại giảm</label>
                    <select id="discountType" name="discountType" onchange="toggleMaxDiscountField(this.value)">
                        <option value="FLAT">Số tiền (đ)</option>
                        <option value="PERCENT">Phần trăm (%)</option>
                    </select>
                </div>
                
                <div class="form-group">
                    <label for="discountValue" id="valueLabel">Mức Giảm (đ)</label>
                    <input type="number" id="discountValue" name="discountValue" min="1" max="10000000" placeholder="Ví dụ: 20000" required>
                </div>

                <div class="form-group" id="maxDiscountGroup" style="display: none;">
                    <label for="maxDiscountAmount">Mức Giảm Tối Đa (đ)</label>
                    <input type="number" id="maxDiscountAmount" name="maxDiscountAmount" max="10000000" placeholder="Bỏ trống nếu không hạn chế">
                </div>
                
                <div class="form-group" id="minOrderGroup">
                    <label for="minOrderValue">Đơn Tối Thiểu (đ)</label>
                    <input type="number" id="minOrderValue" name="minOrderValue" min="0" max="10000000" value="0" required>
                </div>

                <div class="form-group">
                    <label for="maxUses">Số lượt sử dụng tối đa</label>
                    <input type="number" id="maxUses" name="maxUses" min="1" value="100" required>
                </div>

                <div class="form-group">
                    <label for="startDate">Ngày hiệu lực</label>
                    <input type="datetime-local" id="startDate" name="startDate" required>
                </div>

                <div class="form-group">
                    <label for="endDate">Ngày hết hạn</label>
                    <input type="datetime-local" id="endDate" name="endDate" required>
                </div>
            </div>

            <div class="custom-discount-modal-footer">
                <button type="button" class="btn-modal-cancel" onclick="closeModal()">Hủy Bỏ</button>
                <button type="submit" class="btn-modal-submit">Xác Nhận Tạo</button>
            </div>
        </form>
    </div>
</div>

<!-- JavaScript Interactions -->
<script>
    // Tự động tắt thông báo alert sau 4 giây
    window.addEventListener('DOMContentLoaded', () => {
        const alerts = ['successAlert', 'errorAlert'];
        alerts.forEach(id => {
            const el = document.getElementById(id);
            if (el) {
                setTimeout(() => {
                    el.style.opacity = '0';
                    el.style.transition = 'opacity 0.4s ease';
                    setTimeout(() => el.remove(), 400);
                }, 4000);
            }
        });

        // Set mặc định thời gian cho form thêm mới (bắt đầu từ hôm nay, kết thúc sau 30 ngày)
        const startInput = document.getElementById('startDate');
        const endInput = document.getElementById('endDate');
        if (startInput && endInput) {
            const now = new Date();
            const pad = (num) => String(num).padStart(2, '0');
            
            const formatDateTimeLocal = (d) => {
                return d.getFullYear() + '-' + pad(d.getMonth() + 1) + '-' + pad(d.getDate()) + 'T' + pad(d.getHours()) + ':' + pad(d.getMinutes());
            };
            
            startInput.value = formatDateTimeLocal(now);
            
            const nextMonth = new Date();
            nextMonth.setDate(now.getDate() + 30);
            endInput.value = formatDateTimeLocal(nextMonth);
        }
    });

    // Đóng/Mở Modal Popup
    function openModal() {
        document.getElementById('addDiscountModal').style.display = 'flex';
    }

    function closeModal() {
        document.getElementById('addDiscountModal').style.display = 'none';
    }

    // Đóng modal khi bấm ra ngoài vùng nội dung
    window.onclick = function(event) {
        const modal = document.getElementById('addDiscountModal');
        if (event.target === modal) {
            closeModal();
        }
    }

    // Hiện/Ẩn trường Giảm tối đa tùy theo loại Voucher
    function toggleMaxDiscountField(type) {
        const maxGroup = document.getElementById('maxDiscountGroup');
        const valueLabel = document.getElementById('valueLabel');
        const discountValue = document.getElementById('discountValue');
        
        if (type === 'PERCENT') {
            maxGroup.style.display = 'flex';
            valueLabel.innerText = "Mức Giảm (%)";
            discountValue.placeholder = "Ví dụ: 10 (tức 10%)";
            discountValue.max = "100";
        } else {
            maxGroup.style.display = 'none';
            document.getElementById('maxDiscountAmount').value = '';
            valueLabel.innerText = "Mức Giảm (đ)";
            discountValue.placeholder = "Ví dụ: 20000";
            discountValue.max = "10000000";
        }
    }

    // Validate ngày bắt đầu và ngày kết thúc, cùng các giới hạn số tiền tối đa 10.000.000đ
    function validateForm() {
        const start = new Date(document.getElementById('startDate').value);
        const end = new Date(document.getElementById('endDate').value);
        
        if (end <= start) {
            alert("Ngày hết hạn phải xảy ra sau ngày hiệu lực!");
            return false;
        }
        
        const type = document.getElementById('discountType').value;
        const value = parseFloat(document.getElementById('discountValue').value);
        
        if (type === 'FLAT' && value > 10000000) {
            alert("Mức giảm giá tối đa là 10.000.000đ!");
            return false;
        }
        if (type === 'PERCENT' && value > 100) {
            alert("Mức phần trăm giảm giá tối đa là 100%!");
            return false;
        }
        
        const minOrder = parseFloat(document.getElementById('minOrderValue').value);
        if (minOrder > 10000000) {
            alert("Giá trị đơn tối thiểu tối đa là 10.000.000đ!");
            return false;
        }
        
        const maxDiscountInput = document.getElementById('maxDiscountAmount');
        if (maxDiscountInput && maxDiscountInput.value) {
            const maxDiscount = parseFloat(maxDiscountInput.value);
            if (maxDiscount > 10000000) {
                alert("Mức giảm giá tối đa tối đa là 10.000.000đ!");
                return false;
            }
        }
        
        return true;
    }
</script>
