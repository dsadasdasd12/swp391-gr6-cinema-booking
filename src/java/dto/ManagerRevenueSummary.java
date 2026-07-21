package dto;

import java.math.BigDecimal;

/** Tổng hợp doanh thu đã thanh toán thành công của một chi nhánh manager. */
public class ManagerRevenueSummary {
    private final int paidBookings;
    private final BigDecimal netRevenue;
    private final BigDecimal ticketRevenue;
    private final BigDecimal fnbRevenue;
    private final BigDecimal discountAmount;

    public ManagerRevenueSummary(int paidBookings, BigDecimal netRevenue,
            BigDecimal ticketRevenue, BigDecimal fnbRevenue, BigDecimal discountAmount) {
        this.paidBookings = paidBookings;
        this.netRevenue = safe(netRevenue);
        this.ticketRevenue = safe(ticketRevenue);
        this.fnbRevenue = safe(fnbRevenue);
        this.discountAmount = safe(discountAmount);
    }

    private BigDecimal safe(BigDecimal value) {
        return value == null ? BigDecimal.ZERO : value;
    }

    public int getPaidBookings() { return paidBookings; }
    public BigDecimal getNetRevenue() { return netRevenue; }
    public BigDecimal getTicketRevenue() { return ticketRevenue; }
    public BigDecimal getFnbRevenue() { return fnbRevenue; }
    public BigDecimal getDiscountAmount() { return discountAmount; }
}
