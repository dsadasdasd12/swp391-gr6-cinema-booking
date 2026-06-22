/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package service;

import dao.HallDAO;
import dao.MovieManagementDAO;
import dao.MovieDAO;
import dao.StaffBranchDAO;
import dto.MovieAssignmentItem;
import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;
import model.Branch;
import model.Hall;
import model.Movie;

public class MovieManagementService {

    private static final int MIN_DURATION_MIN = 1;
    private static final int MAX_DURATION_MIN = 600;

    private final MovieManagementDAO movieManagementDAO
            = new MovieManagementDAO();

    private final StaffBranchDAO staffBranchDAO
            = new StaffBranchDAO();

    private final HallDAO hallDAO
            = new HallDAO();

    private final MovieDAO movieDAO
            = new MovieDAO();

    /**
     * Lấy các chi nhánh mà Manager đang được phân công quản lý.
     */
    public List<Branch> getBranchesByManagerId(int managerId) {
        validateManagerId(managerId);

        return staffBranchDAO.findBranchesByUserId(managerId);
    }

    /**
     * Lấy các phòng thuộc một chi nhánh mà Manager quản lý.
     */
    public List<Hall> getHallsByBranchId(
            int managerId,
            int branchId) {

        validateManagerId(managerId);
        validateBranchPermission(managerId, branchId);

        return hallDAO.findByBranchId(branchId);
    }

    /**
     * Lấy danh sách phim và trạng thái phân bổ
     * của một chi nhánh.
     */
    public List<MovieAssignmentItem> getItemsForBranch(
            int managerId,
            int branchId) {

        validateManagerId(managerId);
        validateBranchPermission(managerId, branchId);

        return movieManagementDAO.findItemsForBranch(branchId);
    }

    /**
     * Lấy danh sách phim để phân bổ cho một phòng.
     *
     * Chỉ những phim đã được phân bổ cho chi nhánh
     * mới xuất hiện trong danh sách.
     */
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

    /**
     * Lưu danh sách phim được phân bổ cho chi nhánh.
     */
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

    /**
     * Lưu danh sách phim được phân bổ cho phòng.
     */
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

        return movieManagementDAO.saveHallAssignments(
                hallId,
                validMovieIds
        );
    }

    /**
     * Lấy các phim đã được phân bổ cho phòng.
     *
     * Phương thức này sẽ được dùng trong form tạo suất chiếu.
     */
    public List<Movie> getMoviesAssignedToHall(
            int managerId,
            int hallId) {

        validateManagerId(managerId);
        getAuthorizedBranchIdByHallId(managerId, hallId);

        return movieManagementDAO.findMoviesAssignedToHall(hallId);
    }

    /**
     * Kiểm tra phim đã được phân bổ cho phòng chưa.
     *
     * Sau này ShowtimeService sẽ gọi phương thức này
     * trước khi tạo hoặc cập nhật suất chiếu.
     */
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

    /**
     * Kiểm tra Manager có được quản lý chi nhánh không.
     */
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

    /**
     * Kiểm tra một phòng có thuộc quyền quản lý
     * của Manager hay không.
     */
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

    /**
     * Lấy branch_id của phòng và kiểm tra quyền Manager.
     */
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

    /**
     * Kiểm tra Manager có quyền với chi nhánh không.
     */
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

    /**
     * Kiểm tra tài khoản Manager.
     */
    private void validateManagerId(int managerId) {
        if (managerId <= 0) {
            throw new IllegalArgumentException(
                    "Tài khoản Manager không hợp lệ."
            );
        }
    }

    /**
     * Làm sạch danh sách ID phim:
     *
     * - Cho phép danh sách null khi Manager bỏ chọn toàn bộ.
     * - Loại bỏ ID trùng.
     * - Loại bỏ ID nhỏ hơn hoặc bằng 0.
     * - Kiểm tra phim có tồn tại trong database.
     */
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

    /**
     * Cập nhật thời lượng phim.
     */
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

    /**
     * Chuyển chuỗi thời lượng người dùng nhập thành số nguyên.
     */
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

}
