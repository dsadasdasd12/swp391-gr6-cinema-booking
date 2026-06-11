/*
 * Hệ thống Quản lý Rạp chiếu phim RapViet
 * Module: Xem tình trạng ghế (View seat availability)
 */
package model;

/**
 * Một ghế ngồi - ánh xạ đúng các cột của bảng dbo.SEATS, không kèm trạng thái
 * suy diễn. Trạng thái "đã đặt hay chưa" phụ thuộc từng suất chiếu nên được
 * tính ở tầng DAO và mang theo bởi DTO {@code dto.SeatView}, không đặt ở đây.
 *
 * @author Group6 - Huy (Module Duyệt phim)
 */
public class Seat {

    private int id;
    private int hallId;
    private String seatRow;         // ví dụ "A", "B"
    private int seatNumber;         // ví dụ 1, 2, 3
    private String seatType;        // STANDARD | VIP | COUPLE
    private boolean maintenance;    // dbo.SEATS.maintenance: 1 = khóa để bảo trì

    public Seat() {
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public int getHallId() {
        return hallId;
    }

    public void setHallId(int hallId) {
        this.hallId = hallId;
    }

    public String getSeatRow() {
        return seatRow;
    }

    public void setSeatRow(String seatRow) {
        this.seatRow = seatRow;
    }

    public int getSeatNumber() {
        return seatNumber;
    }

    public void setSeatNumber(int seatNumber) {
        this.seatNumber = seatNumber;
    }

    public String getSeatType() {
        return seatType;
    }

    public void setSeatType(String seatType) {
        this.seatType = seatType;
    }

    public boolean isMaintenance() {
        return maintenance;
    }

    public void setMaintenance(boolean maintenance) {
        this.maintenance = maintenance;
    }

    /** Nhãn ghế dạng "A1", "B12" (ghép từ cột seat_row + seat_number). */
    public String getSeatLabel() {
        return (seatRow == null ? "" : seatRow) + seatNumber;
    }
}
