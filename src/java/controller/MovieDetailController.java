/*
 * Hệ thống Quản lý Rạp chiếu phim RapViet
 * Module: Duyệt phim (Xem chi tiết phim + Suất chiếu) - UC06 / UC12
 */
package controller;

import java.io.IOException;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import model.Movie;
import service.MovieService;
import service.ReviewService;

/**
 * Hiển thị chi tiết đầy đủ của một phim (nội dung, diễn viên, thể loại, ngôn
 * ngữ, đánh giá, trailer) cùng các suất chiếu sắp tới.
 * <p>
 * URL: {@code /movie?id=<movieId>} (GET).
 *
 * @author Group6 - DuyThai (Module Duyệt phim)
 */
@WebServlet(name = "MovieDetailController", urlPatterns = {"/movie"})
public class MovieDetailController extends HttpServlet {

    private final MovieService movieService = new MovieService();
    private final ReviewService reviewService = new ReviewService();

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {

        int id = parseId(request.getParameter("id"));
        Movie movie = movieService.getMovieDetail(id);

        if (movie == null) {
            // id không hợp lệ / không tồn tại -> hiển thị thông báo 404 thân thiện
            response.setStatus(HttpServletResponse.SC_NOT_FOUND);
            request.setAttribute("notFound", Boolean.TRUE);
            request.getRequestDispatcher("/pages/movie/detail.jsp").forward(request, response);
            return;
        }

        request.setAttribute("movie", movie);
        request.setAttribute("branchShowtimes", movieService.getShowtimesByBranch(id));
        request.setAttribute("reviews", reviewService.getMovieReviews(id));
        request.getRequestDispatcher("/pages/movie/detail.jsp").forward(request, response);
    }

    /** Đọc id từ tham số request, trả về -1 nếu thiếu hoặc sai định dạng. */
    private static int parseId(String s) {
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
