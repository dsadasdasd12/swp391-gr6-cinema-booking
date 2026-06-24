/*
 * Hệ thống Quản lý Rạp chiếu phim RapViet
 * Module: Duyệt phim (Browse / Search / Filter / Xem chi tiết) - UC06
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
import dto.MovieFilter;
import dto.PageResult;
import model.Category;
import model.Language;
import model.Movie;
import model.Showtime;

public class MovieService {

    private static final int MAX_PAGE_SIZE = 48;

    private static final int MIN_DURATION_MIN = 1;

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

    private final MovieManagementDAO movieManagementDAO
            = new MovieManagementDAO();

    private final StaffBranchDAO staffBranchDAO
            = new StaffBranchDAO();

    private final HallDAO hallDAO
            = new HallDAO();

    public PageResult<Movie> browseMovies(MovieFilter filter) {
        normalize(filter);
        return movieDAO.search(filter);
    }

    public Movie getMovieDetail(int movieId) {
        if (movieId <= 0) {
            return null;
        }
        return movieDAO.findById(movieId);
    }

    public List<Showtime> getShowtimes(int movieId) {
        return showtimeDAO.findUpcomingByMovie(movieId);
    }

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

    public List<Category> getCategories() {
        return categoryDAO.findAllActive();
    }

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
            int branchId) {

        validateManagerId(managerId);
        validateBranchPermission(managerId, branchId);

        return hallDAO.findByBranchId(branchId);
    }


    public List<MovieAssignmentItem> getItemsForBranch(
            int managerId,
            int branchId) {

        validateManagerId(managerId);
        validateBranchPermission(managerId, branchId);

        return movieManagementDAO.findItemsForBranch(branchId);
    }


    public List<MovieAssignmentItem> getItemsForHall(
            int managerId,
            int hallId) {

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
            List<Integer> selectedMovieIds) {

        validateManagerId(managerId);
        validateBranchPermission(managerId, branchId);

        List<Integer> validMovieIds
                = validateAndCleanMovieIds(selectedMovieIds);

        return movieManagementDAO.saveBranchAssignments(
                branchId,
                validMovieIds
        );
    }


    public boolean saveHallAssignments(
            int managerId,
            int hallId,
            List<Integer> selectedMovieIds) {

        validateManagerId(managerId);

        int branchId = getAuthorizedBranchIdByHallId(
                managerId,
                hallId
        );

        List<Integer> validMovieIds
                = validateAndCleanMovieIds(selectedMovieIds);

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

        return movieManagementDAO.saveHallAssignments(
                hallId,
                validMovieIds
        );
    }


    public List<Movie> getMoviesAssignedToHall(
            int managerId,
            int hallId) {

        validateManagerId(managerId);
        getAuthorizedBranchIdByHallId(managerId, hallId);

        return movieManagementDAO.findMoviesAssignedToHall(hallId);
    }


    public boolean isMovieAssignedToHall(
            int managerId,
            int hallId,
            int movieId) {

        validateManagerId(managerId);

        if (movieId <= 0) {
            return false;
        }

        getAuthorizedBranchIdByHallId(managerId, hallId);

        return movieManagementDAO.isMovieAssignedToHall(
                hallId,
                movieId
        );
    }

    public boolean isManagerAllowedBranch(
            int managerId,
            int branchId) {

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
            int hallId) {

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
            int hallId) {

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


    private void validateBranchPermission(
            int managerId,
            int branchId) {

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
            List<Integer> selectedMovieIds) {

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

    public List<Movie> getAllMovies() {
        return movieManagementDAO.findAllForDurationManagement();
    }

    /**
     * Tìm phim theo ID để quản lý thời lượng.
     */
    public Movie getMovieById(int movieId) {
        if (movieId <= 0) {
            throw new IllegalArgumentException("Phim không hợp lệ.");
        }

        Movie movie = movieManagementDAO.findMovieById(movieId);

        if (movie == null) {
            throw new IllegalArgumentException(
                    "Không tìm thấy phim cần cập nhật."
            );
        }

        return movie;
    }

    public boolean updateDuration(
            int movieId,
            int durationMin) {

        if (movieId <= 0) {
            throw new IllegalArgumentException("Phim không hợp lệ.");
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

        return movieManagementDAO.updateDuration(movieId, durationMin);
    }

    public int parseDuration(String durationValue) {
        if (durationValue == null || durationValue.trim().isEmpty()) {
            throw new IllegalArgumentException(
                    "Vui lòng nhập thời lượng phim."
            );
        }

        try {
            return Integer.parseInt(durationValue.trim());
        } catch (NumberFormatException e) {
            throw new IllegalArgumentException(
                    "Thời lượng phim phải là số nguyên."
            );
        }
    }

    private void normalize(MovieFilter f) {
        // keyword: cắt khoảng trắng, bỏ nếu rỗng
        if (f.getKeyword() != null) {
            String kw = f.getKeyword().trim();
            f.setKeyword(kw.isEmpty() ? null : kw);
        }
        if (f.getStatus() != null && !VALID_STATUS.contains(f.getStatus())) {
            f.setStatus(null);
        }
        if (f.getFormat() != null && !VALID_FORMAT.contains(f.getFormat())) {
            f.setFormat(null);
        }
        if (f.getSortBy() == null || !VALID_SORT.contains(f.getSortBy())) {
            f.setSortBy("newest");
        }
        if (f.getCategoryId() != null && f.getCategoryId() <= 0) {
            f.setCategoryId(null);
        }
        if (f.getLanguageId() != null && f.getLanguageId() <= 0) {
            f.setLanguageId(null);
        }
        if (f.getPage() < 1) {
            f.setPage(1);
        }
        if (f.getPageSize() < 1 || f.getPageSize() > MAX_PAGE_SIZE) {
            f.setPageSize(MovieFilter.DEFAULT_PAGE_SIZE);
        }
    }
}
