/*
 * Hệ thống Quản lý Rạp chiếu phim RapViet
 * Module: Xem chi nhánh rạp (View cinema branches)
 */
package model;

import java.time.LocalTime;
import java.time.format.DateTimeFormatter;

/**
 * Một chi nhánh rạp (dbo.BRANCHES) đã được join sẵn với rạp (dbo.CINEMA) và
 * kèm số phòng chiếu đang hoạt động để trang danh sách chi nhánh hiển thị đủ
 * thông tin mà không phải truy vấn thêm.
 *
 * @author Group6 - Huy (Module Duyệt phim)
 */
public class Branch {

    private int id;
    private int cinemaId;
    private String name;
    private String address;
    private String phone;
    private LocalTime openTime;
    private LocalTime closeTime;
    private String status;          // ACTIVE | INACTIVE

    // ── Trường join từ bảng CINEMA + thống kê ───────────────
    private String cinemaName;
    private int hallCount;          // số phòng chiếu đang hoạt động của chi nhánh

    public Branch() {
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public int getCinemaId() {
        return cinemaId;
    }

    public void setCinemaId(int cinemaId) {
        this.cinemaId = cinemaId;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getAddress() {
        return address;
    }

    public void setAddress(String address) {
        this.address = address;
    }

    public String getPhone() {
        return phone;
    }

    public void setPhone(String phone) {
        this.phone = phone;
    }

    public LocalTime getOpenTime() {
        return openTime;
    }

    public void setOpenTime(LocalTime openTime) {
        this.openTime = openTime;
    }

    public LocalTime getCloseTime() {
        return closeTime;
    }

    public void setCloseTime(LocalTime closeTime) {
        this.closeTime = closeTime;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
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

    // ── Getter hỗ trợ hiển thị ──────────────────────────────

    /** Khung giờ mở cửa dạng "08:00 - 23:00"; trả về chuỗi rỗng nếu chưa có dữ liệu. */
    public String getOpenHoursLabel() {
        if (openTime == null || closeTime == null) {
            return "";
        }
        DateTimeFormatter f = DateTimeFormatter.ofPattern("HH:mm");
        return openTime.format(f) + " - " + closeTime.format(f);
    }
}
