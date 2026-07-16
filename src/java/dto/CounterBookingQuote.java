package dto;

/** Authoritative price quote returned to a counter terminal. */
public class CounterBookingQuote {
    private final boolean valid;
    private final String message;
    private final double subtotal;
    private final double discountAmount;
    private final double total;

    private CounterBookingQuote(boolean valid, String message, double subtotal, double discountAmount, double total) {
        this.valid = valid;
        this.message = message;
        this.subtotal = subtotal;
        this.discountAmount = discountAmount;
        this.total = total;
    }

    public static CounterBookingQuote valid(double subtotal, double discountAmount) {
        return new CounterBookingQuote(true, "", subtotal, discountAmount, Math.max(0, subtotal - discountAmount));
    }
    public static CounterBookingQuote invalid(String message) { return new CounterBookingQuote(false, message, 0, 0, 0); }
    public boolean isValid() { return valid; }
    public String getMessage() { return message; }
    public double getSubtotal() { return subtotal; }
    public double getDiscountAmount() { return discountAmount; }
    public double getTotal() { return total; }
}
