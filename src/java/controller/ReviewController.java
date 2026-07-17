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
 * Controller cho luồng rate movie, viết review, sửa review và xóa review.
 *
 * <p>
 * Luồng GET {@code /review?bookingId=...&movieId=...}:</p>
 * <ul>
 * <li>Kiểm tra user đăng nhập.</li>
 * <li>Lấy review đã tồn tại theo booking để quyết định chế độ tạo mới hay chỉnh
 * sửa.</li>
 * <li>Nếu chưa có review, gọi {@link ReviewService#canReview(int, int, int)} để
 * đảm bảo user chỉ review phim từ booking hợp lệ của mình.</li>
 * <li>Forward sang {@code /pages/review/form.jsp}.</li>
 * </ul>
 *
 * <p>
 * Luồng POST {@code /review}:</p>
 * <ul>
 * <li>{@code action=delete}: gọi service xóa review theo reviewId và
 * userId.</li>
 * <li>Không có action delete: nếu có reviewId thì update, nếu không thì create
 * review mới.</li>
 * <li>Redirect về trang chi tiết phim với thông báo kết quả.</li>
 * </ul>
 *
 * @author HuyPD
 */
@WebServlet(name = "ReviewController", urlPatterns = {"/review"})
public class ReviewController extends HttpServlet {

    // ReviewService xu ly dieu kien review, tao/sua/xoa review.
    private final ReviewService reviewService = new ReviewService();

    // MovieService lay thong tin phim de hien tren form review.
    private final MovieService movieService = new MovieService();

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {

        // Lay user dang dang nhap; khong login thi khong duoc review.
        User user = BookingHistoryController.currentUser(request);
        if (user == null) {
            response.sendRedirect(request.getContextPath() + "/login");
            return;
        }

        // bookingId dung de biet review nay gan voi lan dat ve nao.
        int bookingId = parseInt(request.getParameter("bookingId"));

        // movieId dung de biet user dang review phim nao.
        int movieId = parseInt(request.getParameter("movieId"));

        // Đánh giá đã có cho đơn này? -> chế độ sửa; chưa có -> kiểm tra điều kiện tạo mới
        Review existing = reviewService.getReviewForBooking(bookingId);

        // Chi cho edit review neu review hien co thuoc user dang dang nhap.
        boolean ownsExisting = existing != null && existing.getUserId() == user.getId();
        boolean canEdit = ownsExisting && reviewService.canEditReview(existing.getId(), user.getId());

        if (existing == null && !reviewService.canReview(user.getId(), movieId, bookingId)) {
            // Không đủ điều kiện đánh giá (đơn không hợp lệ / chưa xem)
            request.setAttribute("notEligible", Boolean.TRUE);
        }

        // Lay thong tin phim de form hien title/poster/thong tin co ban.
        Movie movie = movieService.getMovieDetail(movieId);

        // Cac attribute ben duoi duoc JSP review/form.jsp dung de render form.
        request.setAttribute("movie", movie);
        request.setAttribute("bookingId", bookingId);
        request.setAttribute("movieId", movieId);
        // Neu user so huu review cu thi dua review vao form de sua, nguoc lai de null.
        request.setAttribute("review", ownsExisting ? existing : null);
        request.setAttribute("canEdit", canEdit);
        request.getRequestDispatcher("/pages/review/form.jsp").forward(request, response);
    }

    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {

        // POST review cung bat buoc user da dang nhap.
        User user = BookingHistoryController.currentUser(request);
        if (user == null) {
            response.sendRedirect(request.getContextPath() + "/login");
            return;
        }

        // action=delete nghia la user bam xoa review; action null/rong nghia la save.
        String action = request.getParameter("action");

        // movieId can de redirect ve trang chi tiet phim sau khi xu ly.
        int movieId = parseInt(request.getParameter("movieId"));

        // reviewId > 0 nghia la sua/xoa review cu; reviewId <= 0 nghia la tao moi.
        int reviewId = parseInt(request.getParameter("reviewId"));

        boolean ok;
        if ("delete".equals(action)) {
            // Xoa review, service kiem tra review co thuoc user nay khong.
            ok = reviewService.deleteReview(reviewId, user.getId());
        } else {
            // rating/comment la du lieu user nhap trong form.
            double rating = parseRating(request.getParameter("rating"));
            String comment = request.getParameter("comment");
            if (reviewId > 0) {
                // Sửa đánh giá đã có
                ok = reviewService.updateReview(reviewId, user.getId(), rating, comment);
            } else {
                // Tạo đánh giá mới gắn với đơn
                // bookingId bat buoc khi tao review moi de chung minh user da dat/xem phim.
                int bookingId = parseInt(request.getParameter("bookingId"));
                ok = reviewService.createReview(user.getId(), movieId, bookingId, rating, comment);
            }
        }

        // Xem lại đánh giá ở trang chi tiết phim
        String msg = ok ? "review_ok" : "review_failed";
        response.sendRedirect(request.getContextPath() + "/movie?id=" + movieId + "&msg=" + msg);
    }

    /**
     * Đọc số nguyên từ tham số, trả -1 nếu thiếu/sai định dạng.
     */
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

    /**
     * Đọc điểm sao nửa bước; trả -1 nếu thiếu hoặc sai định dạng.
     */
    private static double parseRating(String s) {
        if (s == null || s.isBlank()) {
            return -1;
        }
        try {
            return Double.parseDouble(s.trim());
        } catch (NumberFormatException e) {
            return -1;
        }
    }
}
