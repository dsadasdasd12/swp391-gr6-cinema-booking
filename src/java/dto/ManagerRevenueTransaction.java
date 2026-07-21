package dto;

import java.math.BigDecimal;
import java.sql.Timestamp;

/** Một giao dịch hợp lệ hiển thị trong chi tiết doanh thu của manager. */
public class ManagerRevenueTransaction {
    private final int bookingId;
    private final Timestamp paidAt;
    private final String movieTitle, source, paymentMethod;
    private final BigDecimal netAmount, ticketAmount, fnbAmount, discountAmount;
    public ManagerRevenueTransaction(int bookingId, Timestamp paidAt, String movieTitle, String source,
            String paymentMethod, BigDecimal netAmount, BigDecimal ticketAmount, BigDecimal fnbAmount, BigDecimal discountAmount) {
        this.bookingId=bookingId; this.paidAt=paidAt; this.movieTitle=movieTitle; this.source=source; this.paymentMethod=paymentMethod;
        this.netAmount=zero(netAmount); this.ticketAmount=zero(ticketAmount); this.fnbAmount=zero(fnbAmount); this.discountAmount=zero(discountAmount);
    }
    private BigDecimal zero(BigDecimal value) { return value == null ? BigDecimal.ZERO : value; }
    public int getBookingId(){return bookingId;} public Timestamp getPaidAt(){return paidAt;} public String getMovieTitle(){return movieTitle;}
    public String getSource(){return source;} public String getPaymentMethod(){return paymentMethod;} public BigDecimal getNetAmount(){return netAmount;}
    public BigDecimal getTicketAmount(){return ticketAmount;} public BigDecimal getFnbAmount(){return fnbAmount;} public BigDecimal getDiscountAmount(){return discountAmount;}
}
