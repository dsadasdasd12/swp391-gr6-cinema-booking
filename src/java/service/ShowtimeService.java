/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */

package service;

import dao.MovieManagementDAO;
import dao.MovieDAO;
import dao.ShowtimeDAO;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.List;
import model.Movie;
import model.Showtime;
public class ShowtimeService {

    private static final List<String> VALID_STATUS = Arrays.asList(
            "SCHEDULED",
            "ON_SALE"
    );

    private final ShowtimeDAO showtimeDAO = new ShowtimeDAO();
    private final MovieDAO movieDAO = new MovieDAO();

    /*
     * Dùng để kiểm tra phim đã được phân bổ
     * cho phòng chiếu hay chưa.
     */
    private final MovieManagementDAO movieManagementDAO
        = new MovieManagementDAO();

    /**
     * Lấy các suất chiếu thuộc những chi nhánh
     * mà Manager đang quản lý.
     */
    public List<Showtime> getShowtimesByManagerId(int managerId) {

        if (managerId <= 0) {
            throw new IllegalArgumentException(
                    "Tài khoản Manager không hợp lệ."
            );
        }

        return showtimeDAO.findByManagerId(managerId);
    }

    /**
     * Lấy một suất chiếu thuộc quyền quản lý của Manager.
     */
    public Showtime getShowtimeByIdAndManagerId(
            int id,
            int managerId) {

        if (id <= 0 || managerId <= 0) {
            return null;
        }

        return showtimeDAO.findByIdAndManagerId(
                id,
                managerId
        );
    }

    /**
     * Tạo suất chiếu mới.
     */
    public boolean createShowtime(Showtime showtime) {

        /*
         * Kiểm tra dữ liệu, kiểm tra phân bổ phim
         * và tự động tính giờ kết thúc.
         */
        validateAndPrepare(showtime, false);

        /*
         * Kiểm tra phòng có bị trùng lịch không.
         */
        boolean conflict
                = showtimeDAO.hasScheduleConflict(
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

    /**
     * Cập nhật suất chiếu.
     */
    public boolean updateShowtime(Showtime showtime) {

        /*
         * requireId = true vì cập nhật phải có ID.
         */
        validateAndPrepare(showtime, true);

        /*
         * Khi kiểm tra trùng lịch, bỏ qua chính suất chiếu
         * đang được chỉnh sửa.
         */
        boolean conflict
                = showtimeDAO.hasScheduleConflict(
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

    /**
     * Hủy suất chiếu.
     */
    public boolean cancelShowtime(
            int id,
            int managerId) {

        if (id <= 0 || managerId <= 0) {
            return false;
        }

        Showtime current
                = showtimeDAO.findByIdAndManagerId(
                        id,
                        managerId
                );

        if (current == null) {
            return false;
        }

        if ("CANCELLED".equalsIgnoreCase(
                current.getStatus())) {

            return false;
        }

        return showtimeDAO.cancel(id);
    }

    /**
     * Kiểm tra dữ liệu và chuẩn bị giờ kết thúc.
     */
    private void validateAndPrepare(
            Showtime showtime,
            boolean requireId) {

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

        /*
         * Kiểm tra phim có tồn tại hay không.
         */
        Movie movie
                = movieDAO.findById(
                        showtime.getMovieId()
                );

        if (movie == null) {
            throw new IllegalArgumentException(
                    "Phim không tồn tại."
            );
        }

        /*
         * Kiểm tra phim đã được phân bổ cho phòng chiếu.
         *
         * Đây là phần mới được thêm.
         */
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

        /*
         * Thời lượng phim phải hợp lệ để hệ thống
         * tự tính giờ kết thúc.
         */
        if (movie.getDurationMin() <= 0) {
            throw new IllegalArgumentException(
                    "Phim chưa có thời lượng hợp lệ."
            );
        }

        /*
         * Kiểm tra trạng thái.
         */
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

        /*
         * Tự động tính giờ kết thúc:
         *
         * endTime = startTime + durationMin
         */
        LocalDateTime endTime
                = showtime.getStartTime()
                        .plusMinutes(
                                movie.getDurationMin()
                        );

        showtime.setEndTime(endTime);
    }

    /**
     * Chuẩn hóa chuỗi trạng thái.
     */
    private String normalize(String value) {

        if (value == null) {
            return null;
        }

        String result
                = value.trim().toUpperCase();

        if (result.isEmpty()) {
            return null;
        }

        return result;
    }

    public List<Showtime> getActiveShowtimesByBranch(int branchId) {
        return showtimeDAO.getActiveShowtimesByBranch(branchId);
    }

    public Showtime getShowtimeById(int id) {
        return showtimeDAO.getShowtimeById(id);
    }

    public boolean setSeatPricing(int showtimeId, String seatType, double price) {
        return showtimeDAO.setSeatPricing(showtimeId, seatType, price);
    }

    public double getSeatPrice(int showtimeId, String seatType, double basePrice) {
        return showtimeDAO.getSeatPrice(showtimeId, seatType, basePrice);
    }

    public int getBookedSeatsCount(int showtimeId) {
        return showtimeDAO.getBookedSeatsCount(showtimeId);
    }

    public int getTotalSeatsInHall(int showtimeId) {
        return showtimeDAO.getTotalSeatsInHall(showtimeId);
    }

    public double getOccupancyRate(int showtimeId) {
        return showtimeDAO.getOccupancyRate(showtimeId);
    }

    public List<Showtime> getShowtimesByBranchAndDate(int branchId, String dateStr) {
        return showtimeDAO.getShowtimesByBranchAndDate(branchId, dateStr);
    }
}
