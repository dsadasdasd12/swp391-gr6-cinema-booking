package model;

import java.io.Serializable;
import java.sql.Timestamp;

public class Booking implements Serializable {

    private int id;
    private int userId;
    private int showtimeId;
    private String source;
    private String status;
    private double totalPrice;
    private String qrCode;
    private Timestamp bookedAt;

    public Booking() {
    }

    public Booking(int id, int userId, int showtimeId, String source, String status, double totalPrice, String qrCode, Timestamp bookedAt) {
        this.id = id;
        this.userId = userId;
        this.showtimeId = showtimeId;
        this.source = source;
        this.status = status;
        this.totalPrice = totalPrice;
        this.qrCode = qrCode;
        this.bookedAt = bookedAt;
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public int getUserId() {
        return userId;
    }

    public void setUserId(int userId) {
        this.userId = userId;
    }

    public int getShowtimeId() {
        return showtimeId;
    }

    public void setShowtimeId(int showtimeId) {
        this.showtimeId = showtimeId;
    }

    public String getSource() {
        return source;
    }

    public void setSource(String source) {
        this.source = source;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public double getTotalPrice() {
        return totalPrice;
    }

    public void setTotalPrice(double totalPrice) {
        this.totalPrice = totalPrice;
    }

    public String getQrCode() {
        return qrCode;
    }

    public void setQrCode(String qrCode) {
        this.qrCode = qrCode;
    }

    public Timestamp getBookedAt() {
        return bookedAt;
    }

    public void setBookedAt(Timestamp bookedAt) {
        this.bookedAt = bookedAt;
    }
}
