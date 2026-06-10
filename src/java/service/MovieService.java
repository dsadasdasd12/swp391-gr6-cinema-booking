/*
 * Hệ thống Quản lý Rạp chiếu phim RapViet
 * Module: Duyệt phim + Quản lý phim Admin (Long)
 */
package service;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import dao.CategoryDAO;
import dao.LanguageDAO;
import dao.MovieDAO;
import dao.ShowtimeDAO;
import dto.BranchShowtimes;
import dto.MovieDTO;
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
 * @author LONG
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

    // ════════════════════════════════════════════════════════
    // ADMIN METHODS — Long
    // ════════════════════════════════════════════════════════

    /**
     * Trả về toàn bộ phim dưới dạng DTO cho trang quản lý admin.
     * Kiểm tra có suất chiếu đang hoạt động để JSP thiết lập trạng thái nút xóa.
     */
    public List<MovieDTO> getAllMoviesForAdmin() {
        return getAllMoviesForAdmin(null, null);
    }

    /**
     * Trả về danh sách phim DTO cho admin, lọc theo keyword và status.
     * @param keyword từ khóa tìm theo tên, đạo diễn, diễn viên (null = tất cả)
     * @param status  COMING_SOON / NOW_SHOWING / ENDED (null = tất cả)
     */
    public List<MovieDTO> getAllMoviesForAdmin(String keyword, String status) {
        List<Movie> movies = movieDAO.findAll(keyword, status);
        List<MovieDTO> dtos = new ArrayList<>();
        for (Movie m : movies) {
            boolean active = movieDAO.hasActiveShowtimes(m.getId());
            MovieDTO dto = new MovieDTO(
                    m.getId(), m.getTitle(), m.getStatus(), m.getStatusLabel(),
                    m.getCategoryNames(), m.getDurationLabel(), m.getPosterUrl(),
                    m.getDirector(), active);
            dtos.add(dto);
        }
        return dtos;
    }

    /**
     * Kiểm tra dữ liệu đầu vào của một phim theo business rules.
     * BR-01: Tiêu đề bắt buộc, tối đa 255 ký tự.
     * BR-02: Thời lượng phải lớn hơn 0.
     * @return danh sách lỗi (rỗng = hợp lệ)
     */
    public List<String> validateMovie(Movie m) {
        return validateMovie(m, null, null, false);
    }

    /**
     * Kiểm tra dữ liệu phim + thể loại/ngôn ngữ và quy tắc ngày khởi chiếu theo trạng thái.
     */
    public List<String> validateMovie(Movie m, List<Integer> categoryIds,
                                     List<Integer> languageIds, boolean requirePoster) {
        List<String> errors = new ArrayList<>();
        if (m.getTitle() == null || m.getTitle().isBlank()) {
            errors.add("Tiêu đề phim không được để trống.");
        } else if (m.getTitle().length() > 255) {
            errors.add("Tiêu đề phim tối đa 255 ký tự.");
        }
        if (m.getDescription() == null || m.getDescription().isBlank()) {
            errors.add("Mô tả phim không được để trống.");
        } else if (m.getDescription().length() > 2000) {
            errors.add("Mô tả phim tối đa 2000 ký tự.");
        }
        if (m.getDurationMin() <= 0) {
            errors.add("Thời lượng phải lớn hơn 0 phút.");
        } else if (m.getDurationMin() > 999) {
            errors.add("Thời lượng tối đa 999 phút.");
        }
        if (m.getReleaseDate() == null) {
            errors.add("Vui lòng chọn ngày khởi chiếu.");
        }
        if (m.getStatus() == null
                || (!m.getStatus().equals("COMING_SOON")
                &&  !m.getStatus().equals("NOW_SHOWING")
                &&  !m.getStatus().equals("ENDED"))) {
            errors.add("Vui lòng chọn trạng thái phát hành.");
        }
        if (categoryIds == null || categoryIds.isEmpty()) {
            errors.add("Vui lòng chọn ít nhất một thể loại.");
        }
        if (languageIds == null || languageIds.isEmpty()) {
            errors.add("Vui lòng chọn ngôn ngữ chính.");
        }
        if (requirePoster && (m.getPosterUrl() == null || m.getPosterUrl().isBlank())) {
            errors.add("Vui lòng tải lên poster phim.");
        }
        if (m.getReleaseDate() != null && m.getStatus() != null) {
            LocalDate today = LocalDate.now();
            switch (m.getStatus()) {
                case "COMING_SOON" -> {
                    if (m.getReleaseDate().isBefore(today)) {
                        errors.add("Phim sắp chiếu: ngày khởi chiếu không được trước hôm nay.");
                    }
                }
                case "NOW_SHOWING" -> {
                    if (m.getReleaseDate().isAfter(today)) {
                        errors.add("Phim đang chiếu: ngày khởi chiếu không được sau hôm nay.");
                    }
                }
                case "ENDED" -> {
                    if (!m.getReleaseDate().isBefore(today)) {
                        errors.add("Phim đã kết thúc: ngày khởi chiếu phải trước hôm nay.");
                    }
                }
                default -> {}
            }
        }
        return errors;
    }

    /**
     * Thêm mới phim.
     * @return id mới, hoặc -1 nếu validate thất bại hoặc lỗi DB
     */
    public int addMovie(Movie m, List<Integer> catIds, List<Integer> langIds) {
        return movieDAO.insert(m, catIds, langIds);
    }

    /**
     * Cập nhật thông tin phim.
     * @return danh sách lỗi (rỗng = thành công)
     */
    public List<String> editMovie(Movie m, List<Integer> catIds, List<Integer> langIds) {
        return editMovie(m, catIds, langIds, false);
    }

    public List<String> editMovie(Movie m, List<Integer> catIds, List<Integer> langIds,
                                  boolean requirePoster) {
        List<String> errors = validateMovie(m, catIds, langIds, requirePoster);
        if (!errors.isEmpty()) return errors;
        boolean ok = movieDAO.update(m, catIds, langIds);
        if (!ok) errors.add("Cập nhật thất bại, vui lòng thử lại.");
        return errors;
    }

    /**
     * Xóa phim. BL: chặn nếu có suất chiếu đang hoạt động.
     * @return null = thành công; String = mô tả lý do thất bại
     */
    public String deleteMovie(int id) {
        if (movieDAO.hasActiveShowtimes(id)) {
            return "Không thể xóa: phim này có suất chiếu đang hoạt động.";
        }
        boolean ok = movieDAO.delete(id);
        return ok ? null : "Xóa thất bại, vui lòng thử lại.";
    }

    /**
     * Thay đổi trạng thái phim.
     * Nếu chuyển sang ENDED mà có suất chiếu đang hoạt động, trả về cảnh báo.
     * @return null = OK; String = cảnh báo
     */
    public String changeStatus(int id, String newStatus) {
        List<String> validStatuses = List.of("COMING_SOON", "NOW_SHOWING", "ENDED");
        if (!validStatuses.contains(newStatus)) return "Trạng thái không hợp lệ.";
        String warning = null;
        if ("ENDED".equals(newStatus) && movieDAO.hasActiveShowtimes(id)) {
            warning = "Cảnh báo: phim có suất chiếu đang hoạt động. Trạng thái đã được cập nhật.";
        }
        movieDAO.updateStatus(id, newStatus);
        return warning;
    }

    /** Cập nhật chỉ đường dẫn poster (gọi sau upload). */
    public boolean updatePoster(int movieId, String url) {
        return movieDAO.updatePoster(movieId, normalizePosterPath(url));
    }

    /** Chuẩn hóa đường dẫn poster lưu DB (không có dấu / đầu). */
    public static String normalizePosterPath(String url) {
        return Movie.normalizePosterPath(url);
    }

    /** Cập nhật chỉ đường dẫn trailer (gọi sau upload). */
    public boolean updateTrailer(int movieId, String url) {
        return movieDAO.updateTrailer(movieId, url);
    }

    /** Lấy chi tiết một phim theo id (alias cho admin form). */
    public Movie getMovieById(int id) {
        return movieDAO.findById(id);
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
