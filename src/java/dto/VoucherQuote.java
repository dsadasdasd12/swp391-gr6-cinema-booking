package dto;

import java.io.Serializable;

/**
 * Kết quả kiểm tra mã giảm giá cho một booking online.
 */
public class VoucherQuote implements Serializable {

    private final boolean valid;
    private final String message;
    private final String code;
    private final int discountCodeId;
    private final double discountAmount;

    private VoucherQuote(boolean valid, String message, String code,
            int discountCodeId, double discountAmount) {
        this.valid = valid;
        this.message = message;
        this.code = code;
        this.discountCodeId = discountCodeId;
        this.discountAmount = discountAmount;
    }

    public static VoucherQuote valid(String code, int discountCodeId, double discountAmount) {
        return new VoucherQuote(true, "Áp dụng mã giảm giá thành công.", code, discountCodeId, discountAmount);
    }

    public static VoucherQuote invalid(String message) {
        return new VoucherQuote(false, message, null, 0, 0);
    }

    public boolean isValid() {
        return valid;
    }

    public String getMessage() {
        return message;
    }

    public String getCode() {
        return code;
    }

    public int getDiscountCodeId() {
        return discountCodeId;
    }

    public double getDiscountAmount() {
        return discountAmount;
    }
}
