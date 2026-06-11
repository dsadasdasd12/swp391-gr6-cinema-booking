/*
 * Hệ thống Quản lý Rạp chiếu phim RapViet
 * Module: Xem tình trạng ghế - gom các ghế cùng một hàng
 */
package dto;

import java.util.ArrayList;
import java.util.List;
import model.Seat;

/**
 * Một hàng ghế (ví dụ hàng "A") gồm nhãn hàng và danh sách ghế thuộc hàng đó,
 * đã sắp theo số ghế. Việc gom nhóm thực hiện ở tầng service để JSP chỉ việc
 * lặp: vòng ngoài theo hàng, vòng trong theo ghế — dựng sơ đồ ghế dạng lưới.
 *
 * @author Group6 - Huy (Module Duyệt phim)
 */
public class SeatRow {

    private String rowLabel;
    private List<Seat> seats = new ArrayList<>();

    public SeatRow() {
    }

    public SeatRow(String rowLabel) {
        this.rowLabel = rowLabel;
    }

    public String getRowLabel() {
        return rowLabel;
    }

    public void setRowLabel(String rowLabel) {
        this.rowLabel = rowLabel;
    }

    public List<Seat> getSeats() {
        return seats;
    }

    public void setSeats(List<Seat> seats) {
        this.seats = seats;
    }
}
