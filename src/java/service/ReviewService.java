/*
 * Hệ thống Quản lý Rạp chiếu phim RapViet
 * Module: Đánh giá phim - nghiệp vụ chấm điểm / viết / sửa / xóa đánh giá
 */
package service;

import java.util.ArrayList;
import java.util.List;
import dao.ReviewDAO;
import dto.ReviewView;
import model.Review;

/**
 * Tầng nghiệp vụ cho đánh giá phim. Áp các quy tắc: điểm trong khoảng 1..5;
 * khách chỉ đánh giá phim mình đã thực sự xem (qua đơn đặt vé hợp lệ); mỗi đơn
 * chỉ một đánh giá; chỉ sửa/xóa đánh giá của chính mình. Controller chỉ gọi lớp này.
 *
 * @author Group6 - Huy (Module Đánh giá)
 */
public class ReviewService {

    private final ReviewDAO reviewDAO = new ReviewDAO();

    /** Danh sách đánh giá hiển thị ở trang chi tiết phim. */
    public List<ReviewView> getMovieReviews(int movieId) {
        if (movieId <= 0) {
            return new ArrayList<>();
        }
        return reviewDAO.findActiveByMovieId(movieId);
    }

    /** Đánh giá hiện có gắn với một đơn (null nếu chưa đánh giá) — để chọn tạo mới hay sửa. */
    public Review getReviewForBooking(int bookingId) {
        if (bookingId <= 0) {
            return null;
        }
        return reviewDAO.findByBookingId(bookingId);
    }

    /** Lấy đánh giá của chính khách theo id (dùng cho form sửa). */
    public Review getOwnReview(int reviewId, int userId) {
        if (reviewId <= 0 || userId <= 0) {
            return null;
        }
        return reviewDAO.findByIdAndUser(reviewId, userId);
    }

    /** Khách có đủ điều kiện đánh giá phim qua đơn này không. */
    public boolean canReview(int userId, int movieId, int bookingId) {
        return userId > 0 && movieId > 0 && bookingId > 0
                && reviewDAO.canReview(userId, movieId, bookingId);
    }

    /**
     * Tạo đánh giá mới (Rate + Write). Trả về false nếu điểm sai, không đủ điều
     * kiện đánh giá, hoặc đơn này đã có đánh giá rồi.
     */
    public boolean createReview(int userId, int movieId, int bookingId, int rating, String comment) {
        if (!isValidRating(rating) || !canReview(userId, movieId, bookingId)) {
            return false;
        }
        if (reviewDAO.findByBookingId(bookingId) != null) {
            return false;   // mỗi đơn chỉ đánh giá một lần (booking_id UNIQUE)
        }
        Review r = new Review();
        r.setUserId(userId);
        r.setMovieId(movieId);
        r.setBookingId(bookingId);
        r.setRating(rating);
        r.setComment(trimToNull(comment));
        return reviewDAO.insert(r);
    }

    /** Sửa đánh giá của chính khách (Edit). */
    public boolean updateReview(int reviewId, int userId, int rating, String comment) {
        if (reviewId <= 0 || userId <= 0 || !isValidRating(rating)) {
            return false;
        }
        return reviewDAO.update(reviewId, userId, rating, trimToNull(comment));
    }

    /** Xóa đánh giá của chính khách (Delete). */
    public boolean deleteReview(int reviewId, int userId) {
        if (reviewId <= 0 || userId <= 0) {
            return false;
        }
        return reviewDAO.delete(reviewId, userId);
    }

    private static boolean isValidRating(int rating) {
        return rating >= 1 && rating <= 5;
    }

    private static String trimToNull(String s) {
        if (s == null) {
            return null;
        }
        String t = s.trim();
        return t.isEmpty() ? null : t;
    }
}
