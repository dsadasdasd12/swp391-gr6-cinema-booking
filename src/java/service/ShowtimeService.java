/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */

package service;

import dao.HallDAO;
import dao.MovieManagementDAO;
import dao.MovieDAO;
import dao.ShowtimeDAO;
import dao.StaffBranchDAO;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import model.Branch;
import model.Hall;
import model.Movie;
import model.Showtime;

public class ShowtimeService {

    private static final List<String> VALID_STATUS = Arrays.asList(
            "SCHEDULED",
            "ON_SALE"
    );

    private final ShowtimeDAO showtimeDAO = new ShowtimeDAO();
    private final MovieDAO movieDAO = new MovieDAO();
    private final MovieManagementDAO movieManagementDAO
            = new MovieManagementDAO();

    private final StaffBranchDAO staffBranchDAO
            = new StaffBranchDAO();

    private final HallDAO hallDAO = new HallDAO();

    public Branch getAssignedBranch(int managerId) {
        if (managerId <= 0) {
            return null;
        }

        return staffBranchDAO.findBranchByManagerId(managerId);
    }

    public List<Showtime> getShowtimesByManagerId(int managerId) {
        Branch branch = getAssignedBranch(managerId);

        if (branch == null) {
            return Collections.emptyList();
        }

        return showtimeDAO.findByBranchId(branch.getId());
    }

    public Showtime getShowtimeByIdAndManagerId(
            int id,
            int managerId
    ) {
        if (id <= 0 || managerId <= 0) {
            return null;
        }

        Branch branch = getAssignedBranch(managerId);

        if (branch == null) {
            return null;
        }

        return showtimeDAO.findByIdAndBranchId(
                id,
                branch.getId()
        );
    }

    @Deprecated
    public boolean createShowtime(Showtime showtime) {
        validateAndPrepare(showtime, false);

        boolean conflict = showtimeDAO.hasScheduleConflict(
                showtime.getHallId(),
                showtime.getStartTime(),
                showtime.getEndTime(),
                0
        );

        if (conflict) {
            throw new IllegalArgumentException(
                    "Phòng chiếu này đã có suất chiếu "
                    + "trong khoảng thời gian đã chọn."
            );
        }

        return showtimeDAO.insert(showtime);
    }

    public boolean createShowtime(
            int managerId,
            Showtime showtime
    ) {
        Branch branch = requireAssignedBranch(managerId);

        ensureHallBelongsToBranch(
                showtime == null ? 0 : showtime.getHallId(),
                branch.getId()
        );

        validateAndPrepare(showtime, false);

        boolean conflict = showtimeDAO.hasScheduleConflict(
                showtime.getHallId(),
                showtime.getStartTime(),
                showtime.getEndTime(),
                0
        );

        if (conflict) {
            throw new IllegalArgumentException(
                    "Phòng chiếu này đã có suất chiếu "
                    + "trong khoảng thời gian đã chọn."
            );
        }

        return showtimeDAO.insert(showtime);
    }

    @Deprecated
    public boolean updateShowtime(Showtime showtime) {
        validateAndPrepare(showtime, true);

        boolean conflict = showtimeDAO.hasScheduleConflict(
                showtime.getHallId(),
                showtime.getStartTime(),
                showtime.getEndTime(),
                showtime.getId()
        );

        if (conflict) {
            throw new IllegalArgumentException(
                    "Phòng chiếu này đã có suất chiếu "
                    + "trong khoảng thời gian đã chọn."
            );
        }

        return showtimeDAO.update(showtime);
    }

    public boolean updateShowtime(
            int managerId,
            Showtime showtime
    ) {
        Branch branch = requireAssignedBranch(managerId);

        if (showtime == null || showtime.getId() <= 0) {
            throw new IllegalArgumentException(
                    "Không xác định được suất chiếu cần cập nhật."
            );
        }

        Showtime current = showtimeDAO.findByIdAndBranchId(
                showtime.getId(),
                branch.getId()
        );

        if (current == null) {
            throw new IllegalArgumentException(
                    "Bạn không có quyền cập nhật suất chiếu này."
            );
        }

        ensureHallBelongsToBranch(
                showtime.getHallId(),
                branch.getId()
        );

        validateAndPrepare(showtime, true);

        boolean conflict = showtimeDAO.hasScheduleConflict(
                showtime.getHallId(),
                showtime.getStartTime(),
                showtime.getEndTime(),
                showtime.getId()
        );

        if (conflict) {
            throw new IllegalArgumentException(
                    "Phòng chiếu này đã có suất chiếu "
                    + "trong khoảng thời gian đã chọn."
            );
        }

        return showtimeDAO.update(showtime);
    }

    public boolean cancelShowtime(
            int id,
            int managerId
    ) {
        if (id <= 0 || managerId <= 0) {
            return false;
        }

        Branch branch = getAssignedBranch(managerId);

        if (branch == null) {
            return false;
        }

        Showtime current = showtimeDAO.findByIdAndBranchId(
                id,
                branch.getId()
        );

        if (current == null) {
            return false;
        }

        if ("CANCELLED".equalsIgnoreCase(
                current.getStatus()
        )) {
            return false;
        }

        return showtimeDAO.cancel(id);
    }

    private Branch requireAssignedBranch(int managerId) {
        if (managerId <= 0) {
            throw new IllegalArgumentException(
                    "Tài khoản Manager không hợp lệ."
            );
        }

        Branch branch = staffBranchDAO.findBranchByManagerId(managerId);

        if (branch == null) {
            throw new IllegalArgumentException(
                    "Tài khoản Manager chưa được Admin phân công chi nhánh."
            );
        }

        return branch;
    }

    private void ensureHallBelongsToBranch(
            int hallId,
            int branchId
    ) {
        if (hallId <= 0) {
            throw new IllegalArgumentException(
                    "Vui lòng chọn phòng chiếu."
            );
        }

        Hall hall = hallDAO.findByIdAndBranchId(
                hallId,
                branchId
        );

        if (hall == null) {
            throw new IllegalArgumentException(
                    "Bạn không có quyền sử dụng phòng chiếu này."
            );
        }
    }

    private void validateAndPrepare(
            Showtime showtime,
            boolean requireId
    ) {
        if (showtime == null) {
            throw new IllegalArgumentException(
                    "Dữ liệu suất chiếu không hợp lệ."
            );
        }

        if (requireId && showtime.getId() <= 0) {
            throw new IllegalArgumentException(
                    "Không xác định được suất chiếu cần cập nhật."
            );
        }

        if (showtime.getMovieId() <= 0) {
            throw new IllegalArgumentException(
                    "Vui lòng chọn phim."
            );
        }

        if (showtime.getHallId() <= 0) {
            throw new IllegalArgumentException(
                    "Vui lòng chọn phòng chiếu."
            );
        }

        if (showtime.getStartTime() == null) {
            throw new IllegalArgumentException(
                    "Vui lòng chọn thời gian bắt đầu."
            );
        }

        if (showtime.getBasePrice() == null) {
            throw new IllegalArgumentException(
                    "Vui lòng nhập giá vé cơ bản."
            );
        }

        if (showtime.getBasePrice()
                .compareTo(BigDecimal.ZERO) < 0) {

            throw new IllegalArgumentException(
                    "Giá vé cơ bản không được nhỏ hơn 0."
            );
        }

        Movie movie = movieDAO.findById(
                showtime.getMovieId()
        );

        if (movie == null) {
            throw new IllegalArgumentException(
                    "Phim không tồn tại."
            );
        }

        boolean assignedToHall
                = movieManagementDAO.isMovieAssignedToHall(
                        showtime.getHallId(),
                        showtime.getMovieId()
                );

        if (!assignedToHall) {
            throw new IllegalArgumentException(
                    "Phim này chưa được phân bổ cho phòng chiếu đã chọn."
            );
        }

        if (movie.getDurationMin() <= 0) {
            throw new IllegalArgumentException(
                    "Phim chưa có thời lượng hợp lệ."
            );
        }

        String status = normalize(
                showtime.getStatus()
        );

        if (status == null) {
            status = "SCHEDULED";
        }

        if (!VALID_STATUS.contains(status)) {
            throw new IllegalArgumentException(
                    "Trạng thái suất chiếu không hợp lệ."
            );
        }

        showtime.setStatus(status);

        LocalDateTime endTime = showtime.getStartTime()
                .plusMinutes(movie.getDurationMin());

        showtime.setEndTime(endTime);
    }

    private String normalize(String value) {
        if (value == null) {
            return null;
        }

        String result = value.trim().toUpperCase();

        if (result.isEmpty()) {
            return null;
        }

        return result;
    }
}