/*
 * Hệ thống Quản lý Rạp chiếu phim RapViet
 * Module: Duyệt phim - nhóm các suất chiếu theo từng chi nhánh cho trang chi tiết
 */
package dto;

import java.util.ArrayList;
import java.util.List;
import model.Showtime;

/**
 * Gom các suất chiếu của cùng một chi nhánh lại với nhau để trang chi tiết phim
 * hiển thị theo từng rạp (tên rạp + danh sách giờ chiếu). Việc gom nhóm được
 * thực hiện ở tầng service, JSP chỉ việc lặp hai vòng for.
 *
 * @author LONG
 */
public class BranchShowtimes {

    private int branchId;
    private String branchName;
    private String branchAddress;
    private List<Showtime> showtimes = new ArrayList<>();

    public BranchShowtimes() {
    }

    public BranchShowtimes(int branchId, String branchName, String branchAddress) {
        this.branchId = branchId;
        this.branchName = branchName;
        this.branchAddress = branchAddress;
    }

    public int getBranchId() {
        return branchId;
    }

    public void setBranchId(int branchId) {
        this.branchId = branchId;
    }

    public String getBranchName() {
        return branchName;
    }

    public void setBranchName(String branchName) {
        this.branchName = branchName;
    }

    public String getBranchAddress() {
        return branchAddress;
    }

    public void setBranchAddress(String branchAddress) {
        this.branchAddress = branchAddress;
    }

    public List<Showtime> getShowtimes() {
        return showtimes;
    }

    public void setShowtimes(List<Showtime> showtimes) {
        this.showtimes = showtimes;
    }
}
