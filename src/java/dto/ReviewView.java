package dto;

import java.time.format.DateTimeFormatter;
import model.Review;

/** Dữ liệu review hiển thị ở trang chi tiết phim, kèm tên người đánh giá. */
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

    /** Hiển thị 5 sao, hỗ trợ nửa sao: 3.5 = ★★★◐☆. */
    public String getStars() {
        if (review == null) {
            return "";
        }
        double rating = review.getRating();
        StringBuilder stars = new StringBuilder();
        for (int i = 1; i <= 5; i++) {
            if (rating >= i) {
                stars.append('★');
            } else if (rating >= i - 0.5) {
                stars.append('◐');
            } else {
                stars.append('☆');
            }
        }
        return stars.toString();
    }

    public String getCreatedAtLabel() {
        if (review == null || review.getCreatedAt() == null) {
            return "";
        }
        return review.getCreatedAt().format(DateTimeFormatter.ofPattern("dd/MM/yyyy"));
    }
}
