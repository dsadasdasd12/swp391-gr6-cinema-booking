/*
 * Hệ thống Quản lý Rạp chiếu phim RapViet
 * Module: Duyệt phim (Browse / Search / Filter / Xem chi tiết) - UC06
 */
package service;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import dao.CategoryDAO;
import dao.LanguageDAO;
import dao.MovieDAO;
import dao.ShowtimeDAO;
import dto.BranchShowtimes;
import dto.MovieFilter;
import dto.PageResult;
import model.Category;
import model.Language;
import model.Movie;
import model.Showtime;

/**
 * Tầng nghiệp vụ cho chức năng duyệt phim. Lớp này làm sạch / kiểm tra hợp lệ
 * {@link MovieFilter} đầu vào, áp giá trị mặc định và các quy tắc nghiệp vụ, rồi
 * giao việc truy xuất dữ liệu cho tầng DAO. Controller chỉ làm việc với lớp này.
 *
 * @author Group6 - DuyThai (Module Duyệt phim)
 */
public class MovieService {

    /** Giới hạn trên cho số phim mỗi trang (chặn lạm dụng tham số). */
    private static final int MAX_PAGE_SIZE = 48;

    private static final List<String> VALID_STATUS =
            List.of("COMING_SOON", "NOW_SHOWING", "ENDED");
    private static final List<String> VALID_FORMAT =
            List.of("STANDARD", "VIP", "IMAX", "4DX", "PREMIUM");
    private static final List<String> VALID_SORT =
            List.of("newest", "title_asc", "title_desc", "rating_desc");

    private final MovieDAO movieDAO = new MovieDAO();
    private final CategoryDAO categoryDAO = new CategoryDAO();
    private final LanguageDAO languageDAO = new LanguageDAO();
    private final ShowtimeDAO showtimeDAO = new ShowtimeDAO();

    /**
     * Duyệt / tìm kiếm / lọc phim. Bộ lọc được chuẩn hóa tại chỗ để view có thể
     * hiển thị lại đúng các giá trị đã được áp dụng.
     */
    public PageResult<Movie> browseMovies(MovieFilter filter) {
        normalize(filter);
        return movieDAO.search(filter);
    }

    /** Chi tiết đầy đủ của một phim, hoặc {@code null} nếu phim không tồn tại. */
    public Movie getMovieDetail(int movieId) {
        if (movieId <= 0) {
            return null;
        }
        return movieDAO.findById(movieId);
    }

    /** Các suất chiếu sắp tới cho trang chi tiết. */
    public List<Showtime> getShowtimes(int movieId) {
        return showtimeDAO.findUpcomingByMovie(movieId);
    }

    /**
     * Các suất chiếu sắp tới đã gom nhóm theo chi nhánh để view chỉ việc lặp.
     * Thứ tự chi nhánh theo suất chiếu sớm nhất; trong mỗi chi nhánh các suất
     * vẫn giữ thứ tự thời gian do DAO trả về.
     */
    public List<BranchShowtimes> getShowtimesByBranch(int movieId) {
        List<Showtime> all = showtimeDAO.findUpcomingByMovie(movieId);
        Map<Integer, BranchShowtimes> grouped = new LinkedHashMap<>();
        for (Showtime st : all) {
            BranchShowtimes b = grouped.computeIfAbsent(st.getBranchId(),
                    k -> new BranchShowtimes(st.getBranchId(), st.getBranchName(), st.getBranchAddress()));
            b.getShowtimes().add(st);
        }
        return new ArrayList<>(grouped.values());
    }

    /** Danh sách thể loại đang hoạt động cho ô lọc. */
    public List<Category> getCategories() {
        return categoryDAO.findAllActive();
    }

    /** Danh sách ngôn ngữ đang hoạt động cho ô lọc. */
    public List<Language> getLanguages() {
        return languageDAO.findAllActive();
    }

    /** Kiểm tra hợp lệ và đưa mọi trường của bộ lọc về giá trị an toàn. */
    private void normalize(MovieFilter f) {
        // keyword: cắt khoảng trắng, bỏ nếu rỗng
        if (f.getKeyword() != null) {
            String kw = f.getKeyword().trim();
            f.setKeyword(kw.isEmpty() ? null : kw);
        }
        // status / format / sort: chỉ chấp nhận giá trị trong danh sách trắng
        if (f.getStatus() != null && !VALID_STATUS.contains(f.getStatus())) {
            f.setStatus(null);
        }
        if (f.getFormat() != null && !VALID_FORMAT.contains(f.getFormat())) {
            f.setFormat(null);
        }
        if (f.getSortBy() == null || !VALID_SORT.contains(f.getSortBy())) {
            f.setSortBy("newest");
        }
        // id thể loại / ngôn ngữ phải dương
        if (f.getCategoryId() != null && f.getCategoryId() <= 0) {
            f.setCategoryId(null);
        }
        if (f.getLanguageId() != null && f.getLanguageId() <= 0) {
            f.setLanguageId(null);
        }
        // giới hạn phân trang
        if (f.getPage() < 1) {
            f.setPage(1);
        }
        if (f.getPageSize() < 1 || f.getPageSize() > MAX_PAGE_SIZE) {
            f.setPageSize(MovieFilter.DEFAULT_PAGE_SIZE);
        }
    }
}
