package dto;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;
import model.Showtime;

public class BookingDraftView implements Serializable {

    private Showtime showtime;
    private List<BookingSeatLine> seats = new ArrayList<>();
    private double totalPrice;

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

    public double getTotalPrice() {
        return totalPrice;
    }

    public void setTotalPrice(double totalPrice) {
        this.totalPrice = totalPrice;
    }

    public int getSeatCount() {
        return seats == null ? 0 : seats.size();
    }

    public String getSeatLabels() {
        if (seats == null || seats.isEmpty()) {
            return "";
        }
        return seats.stream()
                .map(BookingSeatLine::getSeatCode)
                .collect(Collectors.joining(", "));
    }

    public String getTotalPriceLabel() {
        return String.format("%,.0f", totalPrice).replace(',', '.') + " đ";
    }
}
