/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */

package controller;

import java.io.IOException;
import java.util.List;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;
import model.Movie;
import model.User;
import service.MovieDurationService;

@WebServlet(
        name = "MovieDurationController",
        urlPatterns = {
            "/manager/movie-durations",
            "/manager/movie-durations/update"
        }
)
public class MovieDurationController extends HttpServlet {

    private static final String MOVIE_DURATION_PAGE
            = "/pages/manager/movie-duration-list.jsp";

    private final MovieDurationService movieDurationService
            = new MovieDurationService();

    /**
     * Hiển thị danh sách phim và thời lượng hiện tại.
     */
    @Override
    protected void doGet(
            HttpServletRequest request,
            HttpServletResponse response)
            throws ServletException, IOException {

        User manager = getCurrentManager(request, response);

        if (manager == null) {
            return;
        }

        String path = request.getServletPath();

        if (!"/manager/movie-durations".equals(path)) {
            response.sendRedirect(
                    request.getContextPath()
                    + "/manager/movie-durations"
            );
            return;
        }

        showDurationList(request, response);
    }

    /**
     * Xử lý cập nhật thời lượng phim.
     */
    @Override
    protected void doPost(
            HttpServletRequest request,
            HttpServletResponse response)
            throws ServletException, IOException {

        request.setCharacterEncoding("UTF-8");

        User manager = getCurrentManager(request, response);

        if (manager == null) {
            return;
        }

        String path = request.getServletPath();

        if ("/manager/movie-durations/update".equals(path)) {
            updateDuration(request, response);
            return;
        }

        response.sendRedirect(
                request.getContextPath()
                + "/manager/movie-durations"
        );
    }

    /**
     * Lấy danh sách phim từ Service rồi chuyển sang JSP.
     */
    private void showDurationList(
            HttpServletRequest request,
            HttpServletResponse response)
            throws ServletException, IOException {

        List<Movie> movies
                = movieDurationService.getAllMovies();

        request.setAttribute("movies", movies);

        request.getRequestDispatcher(MOVIE_DURATION_PAGE)
                .forward(request, response);
    }

    /**
     * Cập nhật thời lượng của một phim.
     */
    private void updateDuration(
            HttpServletRequest request,
            HttpServletResponse response)
            throws IOException {

        int movieId = parseInt(
                request.getParameter("movieId")
        );

        String durationValue
                = request.getParameter("durationMin");

        try {
            /*
             * Chuyển giá trị người dùng nhập thành số nguyên.
             */
            int durationMin
                    = movieDurationService.parseDuration(
                            durationValue
                    );

            /*
             * Service kiểm tra:
             * - Phim có tồn tại không.
             * - Thời lượng từ 1 đến 600 phút.
             * - Sau đó cập nhật MOVIES.duration_min.
             */
            boolean success
                    = movieDurationService.updateDuration(
                            movieId,
                            durationMin
                    );

            if (success) {
                setFlash(
                        request,
                        "success",
                        "Cập nhật thời lượng phim thành công."
                );
            } else {
                setFlash(
                        request,
                        "error",
                        "Không thể cập nhật thời lượng phim."
                );
            }

        } catch (IllegalArgumentException e) {
            setFlash(
                    request,
                    "error",
                    e.getMessage()
            );
        }

        /*
         * Dùng redirect để tránh gửi lại form
         * khi người dùng tải lại trang.
         */
        response.sendRedirect(
                request.getContextPath()
                + "/manager/movie-durations"
        );
    }

    /**
     * Lấy Manager đang đăng nhập trong session.
     */
    private User getCurrentManager(
            HttpServletRequest request,
            HttpServletResponse response)
            throws IOException {

        HttpSession session
                = request.getSession(false);

        if (session == null
                || session.getAttribute("user") == null) {

            response.sendRedirect(
                    request.getContextPath()
                    + "/login"
            );

            return null;
        }

        User user
                = (User) session.getAttribute("user");

        if (!"MANAGER".equalsIgnoreCase(
                user.getRole())) {

            response.sendRedirect(
                    request.getContextPath()
                    + "/home"
            );

            return null;
        }

        return user;
    }

    /**
     * Chuyển chuỗi ID thành số nguyên.
     */
    private int parseInt(String value) {

        if (value == null
                || value.trim().isEmpty()) {

            return 0;
        }

        try {
            return Integer.parseInt(
                    value.trim()
            );

        } catch (NumberFormatException e) {
            return 0;
        }
    }

    /**
     * Lưu thông báo tạm thời trong session.
     */
    private void setFlash(
            HttpServletRequest request,
            String type,
            String message) {

        HttpSession session
                = request.getSession();

        session.setAttribute(
                "flashType",
                type
        );

        session.setAttribute(
                "flashMessage",
                message
        );
    }
}