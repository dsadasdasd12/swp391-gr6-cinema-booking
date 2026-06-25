/*
 * Hệ thống Quản lý Rạp chiếu phim RapViet
 * Module: Xem tình trạng ghế - gom các ghế cùng một hàng
 */
package dto;

import java.util.ArrayList;
import java.util.List;

/**
 * Một hàng ghế (ví dụ hàng "A") gồm nhãn hàng và danh sách ghế (kèm trạng thái
 * đặt theo suất chiếu) thuộc hàng đó, đã sắp theo số ghế. Việc gom nhóm thực
 * hiện ở tầng service để JSP chỉ việc lặp: vòng ngoài theo hàng, vòng trong
 * theo ghế — dựng sơ đồ ghế dạng lưới.
 *
 * @author Group6 - Huy (Module Duyệt phim)
 */
public class SeatRow {

    private String rowLabel;
    private List<SeatView> seats = new ArrayList<>();

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

    public List<SeatView> getSeats() {
        return seats;
    }

    public void setSeats(List<SeatView> seats) {
        this.seats = seats;
    }
}
