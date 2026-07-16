package service;

import dao.DiscountDAO;
import java.sql.Timestamp;
import java.util.List;
import model.DiscountCode;
import dto.VoucherQuote;

public class DiscountService {
    private final DiscountDAO discountDAO = new DiscountDAO();

    public List<DiscountCode> getAllDiscountCodes() {
        return discountDAO.getAllDiscountCodes();
    }

    public boolean createDiscountCode(DiscountCode code) {
        validateDiscount(code);
        return discountDAO.createDiscountCode(code);
    }

    /** Maps raw form values and owns every discount creation rule. */
    public boolean createDiscountCode(String code, String type, String value, String maxDiscount,
                                      String minimumOrder, String maxUses, String startDate,
                                      String endDate, String status) {
        try {
            Double cap = maxDiscount == null || maxDiscount.trim().isEmpty()
                    ? null : Double.valueOf(maxDiscount.trim());
            DiscountCode discount = new DiscountCode(0, code, type,
                    Double.parseDouble(value), cap, Double.parseDouble(minimumOrder),
                    Integer.parseInt(maxUses), 0, parseDate(startDate), parseDate(endDate), status);
            return createDiscountCode(discount);
        } catch (IllegalArgumentException e) {
            throw new IllegalArgumentException("Dữ liệu mã giảm giá không hợp lệ.");
        }
    }

    public boolean updateDiscountCodeStatus(int id, String status) {
        if (id <= 0) throw new IllegalArgumentException("Mã giảm giá không hợp lệ.");
        String normalized = status == null ? "" : status.trim().toUpperCase();
        if (!"ACTIVE".equals(normalized) && !"PAUSED".equals(normalized) && !"EXPIRED".equals(normalized)) {
            throw new IllegalArgumentException("Trạng thái mã giảm giá không hợp lệ.");
        }
        return discountDAO.updateDiscountCodeStatus(id, normalized);
    }

    public boolean deleteDiscountCode(int id) {
        return discountDAO.deleteDiscountCode(id);
    }

    public boolean incrementUsedCount(String code) {
        return discountDAO.incrementUsedCount(code);
    }

    public VoucherQuote quote(String code, double subtotal) {
        if (code == null || code.trim().isEmpty()) return VoucherQuote.invalid("Vui lòng nhập mã giảm giá.");
        if (subtotal < 0) return VoucherQuote.invalid("Tổng tiền không hợp lệ.");
        DiscountCode voucher = discountDAO.findByCode(code);
        if (voucher == null) return VoucherQuote.invalid("Mã giảm giá không tồn tại.");
        long now = System.currentTimeMillis();
        if (!"ACTIVE".equalsIgnoreCase(voucher.getStatus())) return VoucherQuote.invalid("Mã giảm giá hiện không hoạt động.");
        if (voucher.getStartDate() == null || voucher.getStartDate().getTime() > now) return VoucherQuote.invalid("Mã giảm giá chưa đến thời gian áp dụng.");
        if (voucher.getEndDate() == null || voucher.getEndDate().getTime() < now) return VoucherQuote.invalid("Mã giảm giá đã hết hạn.");
        if (voucher.getUsedCount() >= voucher.getMaxUses()) return VoucherQuote.invalid("Mã giảm giá đã hết lượt sử dụng.");
        if (subtotal < voucher.getMinOrderValue()) return VoucherQuote.invalid("Đơn hàng chưa đạt giá trị tối thiểu.");
        double amount = "PERCENT".equalsIgnoreCase(voucher.getDiscountType())
                ? subtotal * voucher.getDiscountValue() / 100.0 : voucher.getDiscountValue();
        if (voucher.getMaxDiscountAmount() != null) amount = Math.min(amount, voucher.getMaxDiscountAmount());
        return VoucherQuote.valid(voucher.getCode(), voucher.getId(), Math.min(amount, subtotal));
    }

    private void validateDiscount(DiscountCode code) {
        if (code == null) throw new IllegalArgumentException("Dữ liệu mã giảm giá không hợp lệ.");
        String value = code.getCode() == null ? "" : code.getCode().trim().toUpperCase();
        String type = code.getDiscountType() == null ? "" : code.getDiscountType().trim().toUpperCase();
        String status = code.getStatus() == null ? "ACTIVE" : code.getStatus().trim().toUpperCase();
        if (!value.matches("^[A-Z0-9_]{1,50}$")) throw new IllegalArgumentException("Mã giảm giá không hợp lệ.");
        if (!"PERCENT".equals(type) && !"FLAT".equals(type)) throw new IllegalArgumentException("Loại giảm giá không hợp lệ.");
        if (!"ACTIVE".equals(status) && !"PAUSED".equals(status) && !"EXPIRED".equals(status)) throw new IllegalArgumentException("Trạng thái mã giảm giá không hợp lệ.");
        if (code.getDiscountValue() <= 0 || code.getDiscountValue() > 10_000_000
                || ("PERCENT".equals(type) && code.getDiscountValue() > 100)) throw new IllegalArgumentException("Giá trị giảm giá không hợp lệ.");
        if (code.getMaxDiscountAmount() != null && (code.getMaxDiscountAmount() <= 0 || code.getMaxDiscountAmount() > 10_000_000)) throw new IllegalArgumentException("Mức giảm tối đa không hợp lệ.");
        if (code.getMinOrderValue() < 0 || code.getMinOrderValue() > 10_000_000 || code.getMaxUses() <= 0 || code.getStartDate() == null || code.getEndDate() == null || !code.getStartDate().before(code.getEndDate())) throw new IllegalArgumentException("Điều kiện áp dụng mã giảm giá không hợp lệ.");
        if ("FLAT".equals(type)) code.setMaxDiscountAmount(null);
        code.setCode(value);
        code.setDiscountType(type);
        code.setStatus(status);
    }

    private Timestamp parseDate(String value) {
        if (value == null || value.trim().isEmpty()) throw new IllegalArgumentException("Thời gian áp dụng không hợp lệ.");
        return Timestamp.valueOf(value.trim().replace("T", " ") + ":00");
    }
}
