package dto;

public class TicketSeatView {

    private int seatId;

    // Ví dụ: A1, B5, C10
    private String seatName;

    // STANDARD, VIP, COUPLE...
    private String seatType;

    // Giá của ghế
    private double price;

    public TicketSeatView() {
    }

    public TicketSeatView(int seatId, String seatName, String seatType, double price) {
        this.seatId = seatId;
        this.seatName = seatName;
        this.seatType = seatType;
        this.price = price;
    }

    public int getSeatId() {
        return seatId;
    }

    public void setSeatId(int seatId) {
        this.seatId = seatId;
    }

    public String getSeatName() {
        return seatName;
    }

    public void setSeatName(String seatName) {
        this.seatName = seatName;
    }

    public String getSeatType() {
        return seatType;
    }

    public void setSeatType(String seatType) {
        this.seatType = seatType;
    }

    public double getPrice() {
        return price;
    }

    public void setPrice(double price) {
        this.price = price;
    }
}
