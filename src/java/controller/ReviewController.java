/*
 * Hệ thống Quản lý Rạp chiếu phim RapViet
 * Module: Đánh giá phim (Rate / Write / Edit / Delete reviews) - KHÁCH
 */
package controller;

import java.io.IOException;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import model.Movie;
import model.Review;
import model.User;
import service.MovieService;
import service.ReviewService;

/**
 * Xử lý đánh giá phim của khách: hiển thị form chấm điểm/viết (GET) và lưu
 * (tạo/sửa) hoặc xóa đánh giá (POST). Đánh giá luôn gắn với một đơn đặt vé hợp
 * lệ của khách. Bắt buộc đăng nhập.
 * <p>
 * URL: {@code /review?bookingId=<b>&movieId=<m>} (GET) ;
 * POST với {@code action=save|delete}.
 *
 * @author Group6 - Huy (Module Đánh giá)
 */
@WebServlet(name = "ReviewController", urlPatterns = {"/review"})
public class ReviewController extends HttpServlet {

    private final ReviewService reviewService = new ReviewService();
    private final MovieService movieService = new MovieService();

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {

        User user = BookingHistoryController.currentUser(request);
        if (user == null) {
            response.sendRedirect(request.getContextPath() + "/login");
            return;
        }

        int bookingId = parseInt(request.getParameter("bookingId"));
        int movieId = parseInt(request.getParameter("movieId"));

        // Đánh giá đã có cho đơn này? -> chế độ sửa; chưa có -> kiểm tra điều kiện tạo mới
        Review existing = reviewService.getReviewForBooking(bookingId);
        boolean ownsExisting = existing != null && existing.getUserId() == user.getId();

        if (existing == null && !reviewService.canReview(user.getId(), movieId, bookingId)) {
            // Không đủ điều kiện đánh giá (đơn không hợp lệ / chưa xem)
            request.setAttribute("notEligible", Boolean.TRUE);
        }

        Movie movie = movieService.getMovieDetail(movieId);
        request.setAttribute("movie", movie);
        request.setAttribute("bookingId", bookingId);
        request.setAttribute("movieId", movieId);
        request.setAttribute("review", ownsExisting ? existing : null);
        request.getRequestDispatcher("/pages/review/form.jsp").forward(request, response);
    }

    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {

        User user = BookingHistoryController.currentUser(request);
        if (user == null) {
            response.sendRedirect(request.getContextPath() + "/login");
            return;
        }

        String action = request.getParameter("action");
        int movieId = parseInt(request.getParameter("movieId"));
        int reviewId = parseInt(request.getParameter("reviewId"));

        boolean ok;
        if ("delete".equals(action)) {
            ok = reviewService.deleteReview(reviewId, user.getId());
        } else {
            int rating = parseInt(request.getParameter("rating"));
            String comment = request.getParameter("comment");
            if (reviewId > 0) {
                // Sửa đánh giá đã có
                ok = reviewService.updateReview(reviewId, user.getId(), rating, comment);
            } else {
                // Tạo đánh giá mới gắn với đơn
                int bookingId = parseInt(request.getParameter("bookingId"));
                ok = reviewService.createReview(user.getId(), movieId, bookingId, rating, comment);
            }
        }

        // Xem lại đánh giá ở trang chi tiết phim
        String msg = ok ? "review_ok" : "review_failed";
        response.sendRedirect(request.getContextPath() + "/movie?id=" + movieId + "&msg=" + msg);
    }

    /** Đọc số nguyên từ tham số, trả -1 nếu thiếu/sai định dạng. */
    private static int parseInt(String s) {
        if (s == null || s.isBlank()) {
            return -1;
        }
        try {
            return Integer.parseInt(s.trim());
        } catch (NumberFormatException e) {
            return -1;
        }
    }
}
