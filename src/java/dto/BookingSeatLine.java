package dto;

import java.io.Serializable;
import model.Seat;

public class BookingSeatLine implements Serializable {

    private Seat seat;
    private double price;

    public BookingSeatLine() {
    }

    public BookingSeatLine(Seat seat, double price) {
        this.seat = seat;
        this.price = price;
    }

    public Seat getSeat() {
        return seat;
    }

    public void setSeat(Seat seat) {
        this.seat = seat;
    }

    public double getPrice() {
        return price;
    }

    public void setPrice(double price) {
        this.price = price;
    }

    public String getSeatCode() {
        return seat == null ? "" : seat.getSeatCode();
    }

    public String getSeatType() {
        return seat == null ? "" : seat.getSeatType();
    }

    public String getPriceLabel() {
        return String.format("%,.0f", price).replace(',', '.') + " đ";
    }
}
