/*
 * Hệ thống Quản lý Rạp chiếu phim RapViet
 * Module: Xem tình trạng ghế - dữ liệu trọn gói cho trang sơ đồ ghế
 */
package dto;

import java.util.ArrayList;
import java.util.List;
import model.Showtime;

/**
 * Gói toàn bộ dữ liệu cần để hiển thị sơ đồ ghế của một suất chiếu: thông tin
 * suất chiếu (phim, phòng, chi nhánh, ngày giờ — tái dùng {@link Showtime}),
 * các hàng ghế đã gom nhóm, và vài con số tổng hợp (tổng ghế, số ghế còn trống).
 *
 * @author Group6 - Huy (Module Duyệt phim)
 */
public class SeatMap {

    private Showtime showtime;
    private List<SeatRow> rows = new ArrayList<>();
    private int totalSeats;
    private int availableSeats;

    public SeatMap() {
    }

    public Showtime getShowtime() {
        return showtime;
    }

    public void setShowtime(Showtime showtime) {
        this.showtime = showtime;
    }

    public List<SeatRow> getRows() {
        return rows;
    }

    public void setRows(List<SeatRow> rows) {
        this.rows = rows;
    }

    public int getTotalSeats() {
        return totalSeats;
    }

    public void setTotalSeats(int totalSeats) {
        this.totalSeats = totalSeats;
    }

    public int getAvailableSeats() {
        return availableSeats;
    }

    public void setAvailableSeats(int availableSeats) {
        this.availableSeats = availableSeats;
    }
}
