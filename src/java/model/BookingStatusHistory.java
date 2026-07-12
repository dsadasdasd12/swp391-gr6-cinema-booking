package model;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

/** One auditable booking status transition for the customer-facing timeline. */
public class BookingStatusHistory {

    private int id;
    private int bookingId;
    private String previousStatus;
    private String newStatus;
    private LocalDateTime changedAt;
    private String note;

    public int getId() { return id; }
    public void setId(int id) { this.id = id; }
    public int getBookingId() { return bookingId; }
    public void setBookingId(int bookingId) { this.bookingId = bookingId; }
    public String getPreviousStatus() { return previousStatus; }
    public void setPreviousStatus(String previousStatus) { this.previousStatus = previousStatus; }
    public String getNewStatus() { return newStatus; }
    public void setNewStatus(String newStatus) { this.newStatus = newStatus; }
    public LocalDateTime getChangedAt() { return changedAt; }
    public void setChangedAt(LocalDateTime changedAt) { this.changedAt = changedAt; }
    public String getNote() { return note; }
    public void setNote(String note) { this.note = note; }

    public String getStatusLabel() {
        if (newStatus == null) return "Đang cập nhật";
        return switch (newStatus) {
            case "PENDING" -> "Chờ thanh toán";
            case "CONFIRMED" -> "Đã xác nhận";
            case "CHECKED_IN" -> "Đã check-in";
            case "USED", "COMPLETED" -> "Đã sử dụng";
            case "CANCELLED" -> "Đã hủy";
            default -> newStatus;
        };
    }

    public String getStatusClass() {
        if ("CANCELLED".equals(newStatus)) return "cancelled";
        if ("PENDING".equals(newStatus)) return "pending";
        return "complete";
    }

    public String getChangedAtLabel() {
        return changedAt == null ? "" : changedAt.format(DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm"));
    }
}
