/*
 * Hệ thống Quản lý Rạp chiếu phim RapViet
 * Module: Xem tình trạng ghế (View seat availability)
 */
package dto;

import model.Seat;

/**
 * DTO hiển thị một ghế trong sơ đồ của MỘT suất chiếu cụ thể: bọc {@link Seat}
 * (các cột bảng dbo.SEATS) kèm cờ {@code booked} được suy ra theo suất chiếu
 * (ghế đã nằm trong vé chưa hủy hoặc đang bị giữ trong giỏ). Nhờ vậy entity
 * Seat không phải mang trạng thái phụ thuộc suất chiếu.
 *
 * @author Group6 - Huy (Module Duyệt phim)
 */
public class SeatView {

    private Seat seat;
    private boolean booked;     // true = đã đặt / đang giữ chỗ cho suất chiếu này

    public SeatView() {
    }

    public SeatView(Seat seat, boolean booked) {
        this.seat = seat;
        this.booked = booked;
    }

    public Seat getSeat() {
        return seat;
    }

    public void setSeat(Seat seat) {
        this.seat = seat;
    }

    public boolean isBooked() {
        return booked;
    }

    public void setBooked(boolean booked) {
        this.booked = booked;
    }

    // ── Helper hiển thị (ưu tiên bảo trì > đã đặt > còn trống) ──────────

    /** Có cho chọn ghế này không: chỉ khi không bảo trì và chưa đặt. */
    public boolean isSelectable() {
        return seat != null && !seat.isMaintenance() && !booked;
    }

    /** Lớp CSS theo trạng thái: maintenance | booked | available. */
    public String getStatusClass() {
        if (seat != null && seat.isMaintenance()) {
            return "maintenance";
        }
        if (booked) {
            return "booked";
        }
        return "available";
    }

    /** Nhãn trạng thái tiếng Việt cho tooltip. */
    public String getStatusLabel() {
        if (seat != null && seat.isMaintenance()) {
            return "Bảo trì";
        }
        if (booked) {
            return "Đã đặt";
        }
        return "Còn trống";
    }
    
    private double price;

public double getPrice() {
    return price;
}

public void setPrice(double price) {
    this.price = price;
}
}
