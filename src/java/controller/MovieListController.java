/*
 * Hệ thống Quản lý Rạp chiếu phim RapViet
 * Module: Duyệt phim (Browse / Search / Filter)
 */
package controller;

import java.io.IOException;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.List;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import dto.MovieFilter;
import dto.PageResult;
import model.Category;
import model.Language;
import model.Movie;
import service.MovieService;

/**
 * Xử lý trang danh sách phim công khai: duyệt, tìm kiếm theo từ khóa, lọc
 * (thể loại / ngôn ngữ / định dạng / trạng thái), sắp xếp và phân trang.
 * <p>
 * URL: {@code /movies} (GET). Mọi tiêu chí đi theo query-string để có thể lưu
 * và chia sẻ đường dẫn của một danh sách đã lọc.
 *
 * @author LONG
 */
@WebServlet(name = "MovieListController", urlPatterns = {"/movies"})
public class MovieListController extends HttpServlet {

    private final MovieService movieService = new MovieService();

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {

        // 1) Đổ tham số request vào bộ lọc
        MovieFilter filter = new MovieFilter();
        filter.setKeyword(request.getParameter("q"));
        filter.setStatus(emptyToNull(request.getParameter("status")));
        filter.setFormat(emptyToNull(request.getParameter("format")));
        filter.setSortBy(emptyToNull(request.getParameter("sort")));
        filter.setCategoryId(parseInt(request.getParameter("category")));
        filter.setLanguageId(parseInt(request.getParameter("language")));

        Integer page = parseInt(request.getParameter("page"));
        if (page != null) {
            filter.setPage(page);
        }

        // 2) Tầng nghiệp vụ kiểm tra/chuẩn hóa bộ lọc rồi truy vấn
        PageResult<Movie> result = movieService.browseMovies(filter);
        List<Category> categories = movieService.getCategories();
        List<Language> languages = movieService.getLanguages();

        // 3) Đẩy dữ liệu sang view
        request.setAttribute("result", result);
        request.setAttribute("filter", filter);     // giá trị đã chuẩn hóa, để hiển thị lại trong form
        request.setAttribute("categories", categories);
        request.setAttribute("languages", languages);
        // Chuỗi query giữ nguyên bộ lọc (không có "page") để dựng link phân trang.
        request.setAttribute("queryString", buildQueryString(filter));

        request.getRequestDispatcher("/pages/movie/list.jsp").forward(request, response);
    }

    /**
     * Dựng phần query-string giữ lại mọi bộ lọc (trừ tham số page), kết thúc
     * bằng dấu "&" để JSP chỉ việc nối thêm "page=N".
     */
    private String buildQueryString(MovieFilter f) {
        StringBuilder sb = new StringBuilder();
        if (f.getKeyword() != null) {
            sb.append("q=").append(enc(f.getKeyword())).append("&");
        }
        if (f.getCategoryId() != null) {
            sb.append("category=").append(f.getCategoryId()).append("&");
        }
        if (f.getLanguageId() != null) {
            sb.append("language=").append(f.getLanguageId()).append("&");
        }
        if (f.getStatus() != null) {
            sb.append("status=").append(enc(f.getStatus())).append("&");
        }
        if (f.getFormat() != null) {
            sb.append("format=").append(enc(f.getFormat())).append("&");
        }
        sb.append("sort=").append(enc(f.getSortBy())).append("&");
        return sb.toString();
    }

    private static String enc(String s) {
        return URLEncoder.encode(s == null ? "" : s, StandardCharsets.UTF_8);
    }

    private static String emptyToNull(String s) {
        return (s == null || s.isBlank()) ? null : s.trim();
    }

    private static Integer parseInt(String s) {
        if (s == null || s.isBlank()) {
            return null;
        }
        try {
            return Integer.valueOf(s.trim());
        } catch (NumberFormatException e) {
            return null;
        }
    }
}
