package dto;

import java.time.format.DateTimeFormatter;
import model.Review;

/**
 * Dữ liệu review hiển thị ở trang chi tiết phim, kèm tên người đánh giá.
 */
public class ReviewView {

    private Review review;
    private String userFullName;

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

    /**
     * Số sao đầy cần render ở trang chi tiết phim.
     *
     * Không dùng ký tự Unicode "◐" cho nửa sao vì font của từng máy có thể render nó
     * thành hình tròn nửa màu. JSP sẽ dùng số này để lặp ký tự ★, còn nửa sao được vẽ
     * bằng CSS gradient trên chính ký tự ★ nên hình dáng luôn đồng nhất.
     */
    public int getFullStarCount() {
        if (review == null) {
            return 0;
        }
        return Math.min(5, (int) Math.floor(review.getRating()));
    }

    /**
     * Rating của dự án chỉ tăng theo 0.5. Giá trị true báo cho JSP chèn một sao ★
     * có class review-star--half; CSS sẽ tô vàng đúng nửa bên trái của ngôi sao đó.
     */
    public boolean isHalfStar() {
        if (review == null) {
            return false;
        }
        return review.getRating() - getFullStarCount() >= 0.5;
    }

    /**
     * Phần sao rỗng còn lại để tổng số biểu tượng luôn là 5.
     * Ví dụ rating 4.5: full=4, half=true, empty=0.
     */
    public int getEmptyStarCount() {
        return Math.max(0, 5 - getFullStarCount() - (isHalfStar() ? 1 : 0));
    }

    public String getCreatedAtLabel() {
        if (review == null || review.getCreatedAt() == null) {
            return "";
        }
        return review.getCreatedAt().format(DateTimeFormatter.ofPattern("dd/MM/yyyy"));
    }
}
