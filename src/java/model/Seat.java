/*
 * Hệ thống Quản lý Rạp chiếu phim RapViet
 * Module: Xem tình trạng ghế (View seat availability)
 */
package model;

/**
 * Một ghế ngồi (dbo.SEATS) kèm trạng thái "đã đặt hay chưa" tính riêng cho một
 * suất chiếu cụ thể. Trường {@code booked} không có trong bảng SEATS mà được
 * suy ra ở tầng DAO (ghế đã nằm trong vé đã đặt hoặc đang bị giữ trong giỏ).
 *
 * Lưu ý: mọi xử lý hiển thị (nhãn ghế, lớp CSS theo trạng thái) đặt ở đây để
 * JSP chỉ cần gọi getter, không nhúng code Java.
 *
 * @author Group6 - Huy (Module Duyệt phim)
 */
public class Seat {

    // ── Các cột của bảng dbo.SEATS ──────────────────────────
    private int id;
    private int hallId;
    private String seatRow;         // ví dụ "A", "B"
    private int seatNumber;         // ví dụ 1, 2, 3
    private String seatType;        // STANDARD | VIP | COUPLE
    private boolean maintenance;    // true = ghế đang khóa để bảo trì

    // ── Trạng thái suy ra theo một suất chiếu ───────────────
    private boolean booked;         // true = đã có người đặt / đang giữ chỗ

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

    public boolean isBooked() {
        return booked;
    }

    public void setBooked(boolean booked) {
        this.booked = booked;
    }

    // ── Getter hỗ trợ hiển thị ──────────────────────────────

    /** Nhãn ghế dạng "A1", "B12". */
    public String getSeatLabel() {
        return (seatRow == null ? "" : seatRow) + seatNumber;
    }

    /**
     * Có cho chọn ghế này không: chỉ khi ghế còn trống (không bảo trì, chưa đặt).
     * Phục vụ bước đặt vé sau này và để view tô màu/cho phép click.
     */
    public boolean isSelectable() {
        return !maintenance && !booked;
    }

    /** Lớp CSS theo trạng thái: ưu tiên bảo trì > đã đặt > trống. */
    public String getStatusClass() {
        if (maintenance) {
            return "maintenance";
        }
        if (booked) {
            return "booked";
        }
        return "available";
    }

    /** Nhãn trạng thái tiếng Việt để hiện tooltip. */
    public String getStatusLabel() {
        if (maintenance) {
            return "Bảo trì";
        }
        if (booked) {
            return "Đã đặt";
        }
        return "Còn trống";
    }
}
