/*
 * Hệ thống Quản lý Rạp chiếu phim RapViet
 * Module: Duyệt phim + Quản lý phim Admin (Long)
 */
package service;

import dao.HallDAO;
import dao.MovieManagementDAO;
import dao.StaffBranchDAO;
import dto.MovieAssignmentItem;
import java.util.LinkedHashSet;
import java.util.Set;
import model.Branch;
import model.Hall;

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
import java.time.LocalDate;
import model.Category;
import model.Language;
import model.Movie;
import model.Showtime;

public class MovieService {

    /** Giới hạn trên cho số phim mỗi trang (chặn lạm dụng tham số). */
    private static final int MAX_PAGE_SIZE = 48;

    /** Minimum valid duration in minutes. */
    private static final int MIN_DURATION_MIN = 1;

    /** Maximum valid duration in minutes. */
    private static final int MAX_DURATION_MIN = 600;

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

    /* DAO for Movie-Branch/Hall assignments. */
    private final MovieManagementDAO movieManagementDAO
            = new MovieManagementDAO();

    /* DAOs used to validate Manager permissions and hall ownership. */
    private final StaffBranchDAO staffBranchDAO
            = new StaffBranchDAO();

    private final HallDAO hallDAO
            = new HallDAO();

    /**
     * Duyệt / tìm kiếm / lọc phim. Bộ lọc được chuẩn hóa tại chỗ để view có thể
     * hiển thị lại đúng các giá trị đã được áp dụng.
     */
    public PageResult<Movie> browseMovies(MovieFilter filter) {
        normalize(filter);
        return movieDAO.search(filter);
    }

    public int refreshMovieStatusesByDate() {
        return movieDAO.refreshStatusesByDate(LocalDate.now());
    }

    /** Chi tiết đầy đủ của một phim, hoặc {@code null} nếu phim không tồn tại. */
    public Movie getMovieDetail(int movieId) {
        if (movieId <= 0) {
            return null;
        }

        return movieDAO.findById(movieId);
    }

    public List<Movie> getBookableMoviesByBranch(int branchId) {
        if (branchId <= 0) {
            return new ArrayList<>();
        }

        return movieDAO.findBookableByBranch(branchId);
    }

    /** Các suất chiếu sắp tới cho trang chi tiết. */
    public List<Showtime> getShowtimes(int movieId) {
        return showtimeDAO.findUpcomingByMovie(movieId);
    }


    public List<BranchShowtimes> getShowtimesByBranch(int movieId) {
        List<Showtime> all = showtimeDAO.findUpcomingByMovie(movieId);
        Map<Integer, BranchShowtimes> grouped = new LinkedHashMap<>();

        for (Showtime st : all) {
            BranchShowtimes b = grouped.computeIfAbsent(
                    st.getBranchId(),
                    k -> new BranchShowtimes(
                            st.getBranchId(),
                            st.getBranchName(),
                            st.getBranchAddress()
                    )
            );

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

    public Branch getAssignedBranch(int managerId) {
        validateManagerId(managerId);

        return staffBranchDAO.findBranchByManagerId(managerId);
    }

    @Deprecated
    public List<Branch> getBranchesByManagerId(int managerId) {
        Branch assignedBranch = getAssignedBranch(managerId);

        List<Branch> branches = new ArrayList<>();

        if (assignedBranch != null) {
            branches.add(assignedBranch);
        }

        return branches;
    }

    public List<Hall> getHallsByBranchId(
            int managerId,
            int branchId
    ) {
        validateManagerId(managerId);
        validateBranchPermission(managerId, branchId);

        List<Hall> activeHalls = new ArrayList<>();

        for (Hall hall : hallDAO.findByBranchId(branchId)) {
            if (isHallActive(hall)) {
                activeHalls.add(hall);
            }
        }

        return activeHalls;
    }

    public List<MovieAssignmentItem> getItemsForBranch(
            int managerId,
            int branchId
    ) {
        validateManagerId(managerId);
        validateBranchPermission(managerId, branchId);

        return movieManagementDAO.findItemsForBranch(branchId);
    }

    public List<MovieAssignmentItem> getItemsForHall(
            int managerId,
            int hallId
    ) {
        validateManagerId(managerId);

        int branchId = getAuthorizedBranchIdByHallId(
                managerId,
                hallId
        );

        if (branchId <= 0) {
            throw new IllegalArgumentException(
                    "Phòng chiếu không hợp lệ."
            );
        }

        return movieManagementDAO.findItemsForHall(hallId);
    }

    public boolean saveBranchAssignments(
            int managerId,
            int branchId,
            List<Integer> selectedMovieIds
    ) {
        validateManagerId(managerId);
        validateBranchPermission(managerId, branchId);

        List<Integer> validMovieIds
                = validateAndCleanMovieIds(selectedMovieIds);

        try {
            String conflictMovie
                    = movieManagementDAO.findBranchRemovalConflict(
                            branchId,
                            validMovieIds
                    );

            if (conflictMovie != null) {
                throw new IllegalArgumentException(
                        "Không thể gỡ phim \""
                        + conflictMovie
                        + "\" khỏi chi nhánh vì vẫn còn suất chiếu "
                        + "chưa kết thúc."
                );
            }
        } catch (IllegalStateException e) {
            throw new IllegalArgumentException(
                    "Không thể kiểm tra lịch chiếu trước khi lưu phân bổ phim.",
                    e
            );
        }

        return movieManagementDAO.saveBranchAssignments(
                branchId,
                validMovieIds
        );
    }

    public boolean saveHallAssignments(
            int managerId,
            int hallId,
            List<Integer> selectedMovieIds
    ) {
        validateManagerId(managerId);

        int branchId = getAuthorizedBranchIdByHallId(
                managerId,
                hallId
        );

        ensureHallIsActive(hallId, branchId);

        List<Integer> validMovieIds
                = validateAndCleanMovieIds(selectedMovieIds);

        /*
         * Kiểm tra phim đã được gán cho chi nhánh trước chưa.
         */
        for (int movieId : validMovieIds) {
            boolean assignedToBranch
                    = movieManagementDAO.isMovieAssignedToBranch(
                            branchId,
                            movieId
                    );

            if (!assignedToBranch) {
                Movie movie = movieDAO.findById(movieId);

                String movieTitle = movie == null
                        ? "Phim ID " + movieId
                        : movie.getTitle();

                throw new IllegalArgumentException(
                        movieTitle
                        + " chưa được phân bổ cho chi nhánh."
                );
            }
        }

        try {
            String conflictMovie
                    = movieManagementDAO.findHallRemovalConflict(
                            hallId,
                            validMovieIds
                    );

            if (conflictMovie != null) {
                throw new IllegalArgumentException(
                        "Không thể gỡ phim \""
                        + conflictMovie
                        + "\" khỏi phòng vì vẫn còn suất chiếu "
                        + "chưa kết thúc."
                );
            }
        } catch (IllegalStateException e) {
            throw new IllegalArgumentException(
                    "Không thể kiểm tra lịch chiếu trước khi lưu phân bổ phim.",
                    e
            );
        }

        return movieManagementDAO.saveHallAssignments(
                hallId,
                validMovieIds
        );
    }

    public List<Movie> getMoviesAssignedToHall(
            int managerId,
            int hallId
    ) {
        validateManagerId(managerId);

        getAuthorizedBranchIdByHallId(
                managerId,
                hallId
        );

        return movieManagementDAO.findMoviesAssignedToHall(hallId);
    }

    public boolean isMovieAssignedToHall(
            int managerId,
            int hallId,
            int movieId
    ) {
        validateManagerId(managerId);

        if (movieId <= 0) {
            return false;
        }

        getAuthorizedBranchIdByHallId(
                managerId,
                hallId
        );

        return movieManagementDAO.isMovieAssignedToHall(
                hallId,
                movieId
        );
    }

    /**
     * Kiểm tra Manager có được quản lý chi nhánh không.
     */
    public boolean isManagerAllowedBranch(
            int managerId,
            int branchId
    ) {
        if (managerId <= 0 || branchId <= 0) {
            return false;
        }

        return staffBranchDAO.isManagerAssignedToBranch(
                managerId,
                branchId
        );
    }

    public boolean isManagerAllowedHall(
            int managerId,
            int hallId
    ) {
        if (managerId <= 0 || hallId <= 0) {
            return false;
        }

        int branchId
                = movieManagementDAO.findBranchIdByHallId(hallId);

        if (branchId <= 0) {
            return false;
        }

        return staffBranchDAO.isManagerAssignedToBranch(
                managerId,
                branchId
        );
    }

    private int getAuthorizedBranchIdByHallId(
            int managerId,
            int hallId
    ) {
        if (hallId <= 0) {
            throw new IllegalArgumentException(
                    "Phòng chiếu không hợp lệ."
            );
        }

        int branchId
                = movieManagementDAO.findBranchIdByHallId(hallId);

        if (branchId <= 0) {
            throw new IllegalArgumentException(
                    "Không tìm thấy phòng chiếu."
            );
        }

        validateBranchPermission(managerId, branchId);

        return branchId;
    }

    private void ensureHallIsActive(
            int hallId,
            int branchId
    ) {
        Hall hall = hallDAO.findByIdAndBranchId(
                hallId,
                branchId
        );

        if (hall == null) {
            throw new IllegalArgumentException(
                    "Không tìm thấy phòng chiếu."
            );
        }

        if (!isHallActive(hall)) {
            throw new IllegalArgumentException(
                    "Chỉ có thể phân bổ phim cho phòng chiếu đang hoạt động."
            );
        }
    }

    private boolean isHallActive(Hall hall) {
        return hall != null
                && "ACTIVE".equalsIgnoreCase(
                        hall.getStatus()
                );
    }

    private void validateBranchPermission(
            int managerId,
            int branchId
    ) {
        if (branchId <= 0) {
            throw new IllegalArgumentException(
                    "Chi nhánh không hợp lệ."
            );
        }

        boolean allowed
                = staffBranchDAO.isManagerAssignedToBranch(
                        managerId,
                        branchId
                );

        if (!allowed) {
            throw new IllegalArgumentException(
                    "Bạn không có quyền quản lý chi nhánh này."
            );
        }
    }

    private void validateManagerId(int managerId) {
        if (managerId <= 0) {
            throw new IllegalArgumentException(
                    "Tài khoản Manager không hợp lệ."
            );
        }
    }

    private List<Integer> validateAndCleanMovieIds(
            List<Integer> selectedMovieIds
    ) {
        if (selectedMovieIds == null) {
            return new ArrayList<>();
        }

        Set<Integer> uniqueIds = new LinkedHashSet<>();

        for (Integer movieId : selectedMovieIds) {
            if (movieId != null && movieId > 0) {
                uniqueIds.add(movieId);
            }
        }

        List<Integer> validIds = new ArrayList<>();

        for (Integer movieId : uniqueIds) {
            Movie movie = movieDAO.findById(movieId);

            if (movie == null) {
                throw new IllegalArgumentException(
                        "Không tìm thấy phim có ID " + movieId + "."
                );
            }

            validIds.add(movieId);
        }

        return validIds;
    }
    /**
     * Lấy toàn bộ phim để hiển thị trên màn hình quản lý thời lượng.
     */
    public List<Movie> getAllMovies() {
        return movieManagementDAO.findAllForDurationManagement();
    }

    /**
     * Tìm phim theo ID để quản lý thời lượng.
     */
    public Movie getMovieById(int movieId) {
        if (movieId <= 0) {
            throw new IllegalArgumentException(
                    "Phim không hợp lệ."
            );
        }

        Movie movie = movieDAO.findById(movieId);

        if (movie == null) {
            throw new IllegalArgumentException(
                    "Không tìm thấy phim cần cập nhật."
            );
        }

        return movie;
    }

    /**
     * Cập nhật thời lượng phim.
     */
    public boolean updateDuration(
            int movieId,
            int durationMin
    ) {
        if (movieId <= 0) {
            throw new IllegalArgumentException(
                    "Phim không hợp lệ."
            );
        }

        if (durationMin < MIN_DURATION_MIN) {
            throw new IllegalArgumentException(
                    "Thời lượng phim phải lớn hơn 0 phút."
            );
        }

        if (durationMin > MAX_DURATION_MIN) {
            throw new IllegalArgumentException(
                    "Thời lượng phim không được vượt quá "
                    + MAX_DURATION_MIN
                    + " phút."
            );
        }

        Movie currentMovie = movieManagementDAO.findMovieById(movieId);

        if (currentMovie == null) {
            throw new IllegalArgumentException(
                    "Không tìm thấy phim cần cập nhật."
            );
        }

        if (currentMovie.getDurationMin() == durationMin) {
            return true;
        }

        if (movieManagementDAO.hasUnfinishedShowtimes(movieId)) {
            throw new IllegalArgumentException(
                    "Không thể thay đổi thời lượng vì phim đang có "
                    + "suất chiếu chưa kết thúc."
            );
        }

        return movieManagementDAO.updateDuration(
                movieId,
                durationMin
        );
    }

    /**
     * Chuyển chuỗi thời lượng người dùng nhập thành số nguyên.
     */
    public int parseDuration(String durationValue) {
        if (durationValue == null
                || durationValue.trim().isEmpty()) {

            throw new IllegalArgumentException(
                    "Vui lòng nhập thời lượng phim."
            );
        }

        try {
            return Integer.parseInt(
                    durationValue.trim()
            );

        } catch (NumberFormatException e) {
            throw new IllegalArgumentException(
                    "Thời lượng phim phải là số nguyên."
            );
        }
    }
   // ════════════════════════════════════════════════════════
    // ADMIN METHODS — Long
    // ════════════════════════════════════════════════════════

     public List<MovieDTO> getAllMoviesForAdmin() {
        return getAllMoviesForAdminPaged(null, null, null, null, 0, Integer.MAX_VALUE);
    }

    public List<MovieDTO> getAllMoviesForAdmin(String keyword, String status) {
        return getAllMoviesForAdminPaged(keyword, status, null, null, 0, Integer.MAX_VALUE);
    }

    public List<MovieDTO> getAllMoviesForAdminPaged(String keyword, String status, String sortField, String sortOrder, int offset, int limit) {
        List<Movie> movies = movieDAO.findAllPaged(keyword, status, sortField, sortOrder, offset, limit);
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

    public int countAllAdmin(String keyword, String status) {
        return movieDAO.countAllAdmin(keyword, status);
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
        if (m.getEndDate() == null) {
            errors.add("Vui lòng chọn ngày kết thúc chiếu.");
        }
        if (m.getReleaseDate() != null
                && m.getEndDate() != null
                && m.getEndDate().isBefore(m.getReleaseDate())) {

            errors.add("Ngày kết thúc chiếu không được trước ngày khởi chiếu.");
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
        return errors;
    }

    /**
     * Thêm mới phim.
     * @return id mới, hoặc -1 nếu validate thất bại hoặc lỗi DB
     */
    public int addMovie(Movie m, List<Integer> catIds, List<Integer> langIds) {
        m.setStatus(resolveStatusByDate(m.getReleaseDate(), m.getEndDate()));
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
        m.setStatus(resolveStatusByDate(m.getReleaseDate(), m.getEndDate()));
        boolean ok = movieDAO.update(m, catIds, langIds);
        if (!ok) errors.add("Cập nhật thất bại, vui lòng thử lại.");
        return errors;
    }

    public String resolveStatusByDate(LocalDate releaseDate, LocalDate endDate) {
        LocalDate today = LocalDate.now();
        if (releaseDate != null && releaseDate.isAfter(today)) {
            return "COMING_SOON";
        }
        if (endDate != null && endDate.isBefore(today)) {
            return "ENDED";
        }
        return "NOW_SHOWING";
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

    /** Kiểm tra hợp lệ và đưa mọi trường của bộ lọc về giá trị an toàn. */
    private void normalize(MovieFilter f) {
        if (f.getKeyword() != null) {
            String kw = f.getKeyword().trim();
            f.setKeyword(
                    kw.isEmpty() ? null : kw
            );
        }

        if (f.getStatus() != null
                && !VALID_STATUS.contains(f.getStatus())) {

            f.setStatus(null);
        }

        if (f.getFormat() != null
                && !VALID_FORMAT.contains(f.getFormat())) {

            f.setFormat(null);
        }

        if (f.getSortBy() == null
                || !VALID_SORT.contains(f.getSortBy())) {

            f.setSortBy("newest");
        }

        if (f.getCategoryId() != null
                && f.getCategoryId() <= 0) {

            f.setCategoryId(null);
        }

        if (f.getLanguageId() != null
                && f.getLanguageId() <= 0) {

            f.setLanguageId(null);
        }

        if (f.getPage() < 1) {
            f.setPage(1);
        }

        if (f.getPageSize() < 1
                || f.getPageSize() > MAX_PAGE_SIZE) {

            f.setPageSize(MovieFilter.DEFAULT_PAGE_SIZE);
        }
    }
}
