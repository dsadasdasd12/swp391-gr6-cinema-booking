/*
 * Hệ thống Quản lý Rạp chiếu phim RapViet
 * Module: Trang chủ (Landing page)
 */
package controller;

import java.io.IOException;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import dto.MovieFilter;
import service.MovieService;

/**
 * Trang chủ của website: lấy danh sách phim "Đang chiếu" và "Sắp chiếu" để
 * hiển thị trên landing page.
 * <p>
 * URL: {@code /home} (GET).
 *
 * @author LONG
 */
@WebServlet(name = "HomeController", urlPatterns = {"/home"})
public class HomeController extends HttpServlet {

    /** Số phim tối đa hiển thị trên mỗi dải (rail) ở trang chủ. */
    private static final int RAIL_SIZE = 10;

    private final MovieService movieService = new MovieService();

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {

        request.setAttribute("nowShowing", topMovies("NOW_SHOWING"));
        request.setAttribute("comingSoon", topMovies("COMING_SOON"));

        request.getRequestDispatcher("/pages/home.jsp").forward(request, response);
    }

    /** Lấy tối đa RAIL_SIZE phim mới nhất theo trạng thái cho trang chủ. */
    private java.util.List<model.Movie> topMovies(String status) {
        MovieFilter f = new MovieFilter();
        f.setStatus(status);
        f.setSortBy("newest");
        f.setPage(1);
        f.setPageSize(RAIL_SIZE);
        return movieService.browseMovies(f).getItems();
    }
}
