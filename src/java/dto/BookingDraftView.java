package dto;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;
import model.Showtime;

public class BookingDraftView implements Serializable {

    private Showtime showtime;
    private List<BookingSeatLine> seats = new ArrayList<>();

    /*
     * Tổng giá gốc của toàn bộ ghế trước khuyến mãi.
     */
    private double seatSubtotal;

    /*
     * Số tiền được giảm bởi chương trình mua 5 tặng 1.
     */
    private double buyFiveDiscount;

    /*
     * Voucher online hiện chưa được tích hợp nên mặc định bằng 0.
     * Field được giữ để đúng thứ tự tính giá và dễ mở rộng sau này.
     */
    private double voucherDiscount;

    /*
     * Số tiền cuối khách phải thanh toán.
     */
    private double totalPrice;
    private List<BookingFnbLine> fnbLines = new ArrayList<>();

    public Showtime getShowtime() {
        return showtime;
    }

    public void setShowtime(Showtime showtime) {
        this.showtime = showtime;
    }

    public List<BookingSeatLine> getSeats() {
        return seats;
    }

    public void setSeats(List<BookingSeatLine> seats) {
        this.seats = seats == null ? new ArrayList<>() : seats;
    }

    public double getSeatSubtotal() {
        return seatSubtotal;
    }

    public void setSeatSubtotal(double seatSubtotal) {
        this.seatSubtotal = Math.max(0, seatSubtotal);
    }

    public double getBuyFiveDiscount() {
        return buyFiveDiscount;
    }

    public void setBuyFiveDiscount(double buyFiveDiscount) {
        this.buyFiveDiscount = Math.max(0, buyFiveDiscount);
    }

    public double getVoucherDiscount() {
        return voucherDiscount;
    }

    public void setVoucherDiscount(double voucherDiscount) {
        this.voucherDiscount = Math.max(0, voucherDiscount);
    }

    public double getTotalPrice() {
        return totalPrice;
    }

    public void setTotalPrice(double totalPrice) {
        this.totalPrice = Math.max(0, totalPrice);
    }

    public List<BookingFnbLine> getFnbLines() {
        return fnbLines;
    }

    public void setFnbLines(List<BookingFnbLine> fnbLines) {
        this.fnbLines = fnbLines == null ? new ArrayList<>() : fnbLines;
    }

    public double getFnbSubtotal() {
        return fnbLines.stream().mapToDouble(BookingFnbLine::getLineTotal).sum();
    }

    public String getFnbSubtotalLabel() {
        return formatMoney(getFnbSubtotal());
    }

    public String getGrandTotalLabel() {
        return formatMoney(totalPrice + getFnbSubtotal());
    }

    public int getSeatCount() {
        return seats == null ? 0 : seats.size();
    }

    public int getFreeTicketCount() {
        return getSeatCount() / 5;
    }

    public String getSeatLabels() {
        if (seats == null || seats.isEmpty()) {
            return "";
        }

        return seats.stream()
                .map(BookingSeatLine::getSeatCode)
                .collect(Collectors.joining(", "));
    }

    public String getSeatSubtotalLabel() {
        return formatMoney(seatSubtotal);
    }

    public String getBuyFiveDiscountLabel() {
        return formatMoney(buyFiveDiscount);
    }

    public String getVoucherDiscountLabel() {
        return formatMoney(voucherDiscount);
    }

    public String getTotalPriceLabel() {
        return formatMoney(totalPrice);
    }

    private String formatMoney(double value) {
        return String.format("%,.0f", value).replace(',', '.') + " đ";
    }
}
