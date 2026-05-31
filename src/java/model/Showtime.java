/*
 * Hệ thống Quản lý Rạp chiếu phim RapViet
 * Module: Duyệt phim - suất chiếu hiển thị ở trang chi tiết phim
 */
package model;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

/**
 * Một suất chiếu (dbo.SHOWTIMES) đã được join sẵn với phòng chiếu và chi nhánh
 * để trang chi tiết phim hiển thị "ở đâu & khi nào" mà không cần truy vấn thêm.
 *
 * @author Group6 - DuyThai (Module Duyệt phim)
 */
public class Showtime {

    private int id;
    private int movieId;
    private int hallId;
    private LocalDateTime startTime;
    private LocalDateTime endTime;
    private BigDecimal basePrice;
    private String status;          // SCHEDULED | ON_SALE | CANCELLED | COMPLETED

    // ── Trường join từ bảng HALLS / BRANCHES ────────────────
    private String hallName;
    private String hallType;        // STANDARD | VIP | IMAX | 4DX | PREMIUM (= "định dạng")
    private int branchId;
    private String branchName;
    private String branchAddress;

    public Showtime() {
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public int getMovieId() {
        return movieId;
    }

    public void setMovieId(int movieId) {
        this.movieId = movieId;
    }

    public int getHallId() {
        return hallId;
    }

    public void setHallId(int hallId) {
        this.hallId = hallId;
    }

    public LocalDateTime getStartTime() {
        return startTime;
    }

    public void setStartTime(LocalDateTime startTime) {
        this.startTime = startTime;
    }

    public LocalDateTime getEndTime() {
        return endTime;
    }

    public void setEndTime(LocalDateTime endTime) {
        this.endTime = endTime;
    }

    public BigDecimal getBasePrice() {
        return basePrice;
    }

    public void setBasePrice(BigDecimal basePrice) {
        this.basePrice = basePrice;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public String getHallName() {
        return hallName;
    }

    public void setHallName(String hallName) {
        this.hallName = hallName;
    }

    public String getHallType() {
        return hallType;
    }

    public void setHallType(String hallType) {
        this.hallType = hallType;
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

    // ── Getter hỗ trợ hiển thị ──────────────────────────────

    /** Ngày chiếu dạng dd/MM/yyyy. */
    public String getShowDate() {
        return startTime == null ? "" : startTime.format(DateTimeFormatter.ofPattern("dd/MM/yyyy"));
    }

    /** Giờ bắt đầu dạng HH:mm. */
    public String getStartHour() {
        return startTime == null ? "" : startTime.format(DateTimeFormatter.ofPattern("HH:mm"));
    }

    /** Giờ kết thúc dạng HH:mm. */
    public String getEndHour() {
        return endTime == null ? "" : endTime.format(DateTimeFormatter.ofPattern("HH:mm"));
    }
}
