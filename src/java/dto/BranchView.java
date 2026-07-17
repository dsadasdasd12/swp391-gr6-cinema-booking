/*
 * Hệ thống Quản lý Rạp chiếu phim RapViet
 * Module: Xem chi nhánh rạp (View cinema branches)
 */
package dto;

import java.time.format.DateTimeFormatter;
import model.Branch;

/**
 * DTO hiển thị cho trang "Hệ thống rạp": bọc một {@link Branch} (giữ nguyên các
 * cột của bảng dbo.BRANCHES) kèm dữ liệu ghép từ bảng khác để hiển thị — tên
 * rạp (dbo.CINEMA.name) và số phòng chiếu đang hoạt động (đếm dbo.HALLS). Nhờ
 * vậy entity Branch không phải mang field ngoài cột DB.
 *
 * @author Group6 - Huy (Module Duyệt phim)
 */
public class BranchView {

    private Branch branch;
    private String cinemaName;      // dbo.CINEMA.name
    private int hallCount;          // số phòng chiếu ACTIVE của chi nhánh

    public BranchView() {
    }

    public BranchView(Branch branch, String cinemaName, int hallCount) {
        this.branch = branch;
        this.cinemaName = cinemaName;
        this.hallCount = hallCount;
    }

    public Branch getBranch() {
        return branch;
    }

    public void setBranch(Branch branch) {
        this.branch = branch;
    }

    public String getCinemaName() {
        return cinemaName;
    }

    public void setCinemaName(String cinemaName) {
        this.cinemaName = cinemaName;
    }

    public int getHallCount() {
        return hallCount;
    }

    public void setHallCount(int hallCount) {
        this.hallCount = hallCount;
    }

    /**
     * Khung giờ mở cửa dạng "08:00 - 23:00"; rỗng nếu chưa có dữ liệu.
     */
    public String getOpenHoursLabel() {
        if (branch == null || branch.getOpenTime() == null || branch.getCloseTime() == null) {
            return "";
        }
        DateTimeFormatter f = DateTimeFormatter.ofPattern("HH:mm");
        return branch.getOpenTime().format(f) + " - " + branch.getCloseTime().format(f);
    }
}
