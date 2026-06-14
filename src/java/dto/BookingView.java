/*
 * Hệ thống Quản lý Rạp chiếu phim RapViet
 * Module: Đặt vé - dữ liệu hiển thị lịch sử / chi tiết đơn đặt vé (KHÁCH)
 */
package dto;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import model.Booking;

/**
 * DTO hiển thị một đơn đặt vé cho khách: bọc entity {@link Booking} (giữ nguyên
 * cột bảng dbo.BOOKINGS) kèm dữ liệu ghép từ các bảng khác để hiển thị — tên
 * phim, chi nhánh, phòng, thời gian suất chiếu và danh sách ghế. Nhờ vậy entity
 * Booking không phải mang field ngoài cột DB.
 *
 * @author Group6 - Huy (Module Đặt vé)
 */
public class BookingView {

    private Booking booking;
    private int movieId;            // để tạo link đánh giá phim
    private String movieTitle;
    private String branchName;
    private String hallName;
    private LocalDateTime showStart; // SHOWTIMES.start_time
    private String seatLabels;       // ví dụ "A1, A2, B5"
    private int seatCount;

    public BookingView() {
    }

    public Booking getBooking() {
        return booking;
    }

    public void setBooking(Booking booking) {
        this.booking = booking;
    }

    public int getMovieId() {
        return movieId;
    }

    public void setMovieId(int movieId) {
        this.movieId = movieId;
    }

    public String getMovieTitle() {
        return movieTitle;
    }

    public void setMovieTitle(String movieTitle) {
        this.movieTitle = movieTitle;
    }

    public String getBranchName() {
        return branchName;
    }

    public void setBranchName(String branchName) {
        this.branchName = branchName;
    }

    public String getHallName() {
        return hallName;
    }

    public void setHallName(String hallName) {
        this.hallName = hallName;
    }

    public LocalDateTime getShowStart() {
        return showStart;
    }

    public void setShowStart(LocalDateTime showStart) {
        this.showStart = showStart;
    }

    public String getSeatLabels() {
        return seatLabels;
    }

    public void setSeatLabels(String seatLabels) {
        this.seatLabels = seatLabels;
    }

    public int getSeatCount() {
        return seatCount;
    }

    public void setSeatCount(int seatCount) {
        this.seatCount = seatCount;
    }

    // ── Helper hiển thị ─────────────────────────────────────

    /** Thời gian suất chiếu dạng "dd/MM/yyyy HH:mm". */
    public String getShowTimeLabel() {
        return showStart == null ? ""
                : showStart.format(DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm"));
    }

    /** Thời điểm đặt vé dạng "dd/MM/yyyy HH:mm". */
    public String getBookedAtLabel() {
        if (booking == null || booking.getBookedAt() == null) {
            return "";
        }
        return booking.getBookedAt().toLocalDateTime()
                .format(DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm"));
    }

    /** Tổng tiền dạng "120.000 đ" (dấu chấm ngăn nghìn kiểu VN). */
    public String getTotalPriceLabel() {
        if (booking == null) {
            return "";
        }
        return String.format("%,.0f", booking.getTotalPrice()).replace(',', '.') + " đ";
    }

    /** Nhãn trạng thái đơn tiếng Việt (theo dõi trạng thái đơn hàng). */
    public String getStatusLabel() {
        if (booking == null) {
            return "";
        }
        switch (booking.getStatus()) {
            case "PENDING":    return "Chờ thanh toán";
            case "CONFIRMED":  return "Đã xác nhận";
            case "CHECKED_IN": return "Đã check-in";
            case "USED":       return "Đã sử dụng";
            case "CANCELLED":  return "Đã hủy";
            default:           return booking.getStatus();
        }
    }

    /** Lớp CSS badge theo trạng thái (tái dùng .badge.now/.soon/.ended). */
    public String getStatusBadgeClass() {
        if (booking == null) {
            return "ended";
        }
        switch (booking.getStatus()) {
            case "CONFIRMED":
            case "CHECKED_IN":
            case "USED":       return "now";
            case "PENDING":    return "soon";
            default:           return "ended";   // CANCELLED
        }
    }

    /** Khách được hủy đơn khi đơn còn ở trạng thái chờ/đã xác nhận. */
    public boolean isCancellable() {
        return booking != null
                && ("PENDING".equals(booking.getStatus()) || "CONFIRMED".equals(booking.getStatus()));
    }

    /** Khách được đánh giá phim khi đã thực sự xem (check-in / đã dùng vé). */
    public boolean isReviewable() {
        return booking != null
                && ("CHECKED_IN".equals(booking.getStatus()) || "USED".equals(booking.getStatus()));
    }
}
