/*
 * Hệ thống Quản lý Rạp chiếu phim RapViet
 * Module: Đánh giá phim - dữ liệu hiển thị một đánh giá
 */
package dto;

import java.time.format.DateTimeFormatter;
import model.Review;

/**
 * DTO hiển thị một đánh giá phim: bọc entity {@link Review} (cột bảng
 * dbo.REVIEWS) kèm tên người đánh giá (join từ dbo.[USER]) để view khỏi truy
 * vấn thêm. Entity Review nhờ vậy không phải mang field ngoài cột DB.
 *
 * @author Group6 - Huy (Module Đánh giá)
 */
public class ReviewView {

    private Review review;
    private String userFullName;    // dbo.[USER].full_name

    public ReviewView() {
    }

    public ReviewView(Review review, String userFullName) {
        this.review = review;
        this.userFullName = userFullName;
    }

    public Review getReview() {
        return review;
    }

    public void setReview(Review review) {
        this.review = review;
    }

    public String getUserFullName() {
        return userFullName;
    }

    public void setUserFullName(String userFullName) {
        this.userFullName = userFullName;
    }

    // ── Helper hiển thị ─────────────────────────────────────

    /** Chuỗi 5 sao: tô đặc theo số điểm, ví dụ rating=4 -> "★★★★☆". */
    public String getStars() {
        if (review == null) {
            return "";
        }
        int r = review.getRating();
        StringBuilder sb = new StringBuilder();
        for (int i = 1; i <= 5; i++) {
            sb.append(i <= r ? '★' : '☆');
        }
        return sb.toString();
    }

    /** Ngày đánh giá dạng "dd/MM/yyyy". */
    public String getCreatedAtLabel() {
        if (review == null || review.getCreatedAt() == null) {
            return "";
        }
        return review.getCreatedAt().format(DateTimeFormatter.ofPattern("dd/MM/yyyy"));
    }
}
