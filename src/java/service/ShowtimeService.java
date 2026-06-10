/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package service;

import dao.MovieDAO;
import dao.ShowtimeDAO;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.List;
import model.Movie;
import model.Movie;
import model.Showtime;
import model.Showtime;

public class ShowtimeService {

    private static final List<String> VALID_STATUS = Arrays.asList(
            "SCHEDULED", "ON_SALE"
    );

    private final ShowtimeDAO showtimeDAO = new ShowtimeDAO();
    private final MovieDAO movieDAO = new MovieDAO();

    public List<Showtime> getShowtimesByManagerId(int managerId) {
        if (managerId <= 0) {
            throw new IllegalArgumentException("Tài khoản Manager không hợp lệ.");
        }

        return showtimeDAO.findByManagerId(managerId);
    }

    public Showtime getShowtimeByIdAndManagerId(int id, int managerId) {
        if (id <= 0 || managerId <= 0) {
            return null;
        }

        return showtimeDAO.findByIdAndManagerId(id, managerId);
    }

    public boolean createShowtime(Showtime showtime) {
        validateAndPrepare(showtime, false);

        boolean conflict = showtimeDAO.hasScheduleConflict(
                showtime.getHallId(),
                showtime.getStartTime(),
                showtime.getEndTime(),
                0
        );

        if (conflict) {
            throw new IllegalArgumentException("Phòng chiếu này đã có suất chiếu trong khoảng thời gian đã chọn.");
        }

        return showtimeDAO.insert(showtime);
    }

    public boolean updateShowtime(Showtime showtime) {
        validateAndPrepare(showtime, true);

        boolean conflict = showtimeDAO.hasScheduleConflict(
                showtime.getHallId(),
                showtime.getStartTime(),
                showtime.getEndTime(),
                showtime.getId()
        );

        if (conflict) {
            throw new IllegalArgumentException("Phòng chiếu này đã có suất chiếu trong khoảng thời gian đã chọn.");
        }

        return showtimeDAO.update(showtime);
    }

    public boolean cancelShowtime(int id, int managerId) {
        if (id <= 0 || managerId <= 0) {
            return false;
        }

        Showtime current = showtimeDAO.findByIdAndManagerId(id, managerId);

        if (current == null) {
            return false;
        }

        if ("CANCELLED".equalsIgnoreCase(current.getStatus())) {
            return false;
        }

        return showtimeDAO.cancel(id);
    }

    private void validateAndPrepare(Showtime showtime, boolean requireId) {
        if (showtime == null) {
            throw new IllegalArgumentException("Dữ liệu suất chiếu không hợp lệ.");
        }

        if (requireId && showtime.getId() <= 0) {
            throw new IllegalArgumentException("Không xác định được suất chiếu cần cập nhật.");
        }

        if (showtime.getMovieId() <= 0) {
            throw new IllegalArgumentException("Vui lòng chọn phim.");
        }

        if (showtime.getHallId() <= 0) {
            throw new IllegalArgumentException("Vui lòng chọn phòng chiếu.");
        }

        if (showtime.getStartTime() == null) {
            throw new IllegalArgumentException("Vui lòng chọn thời gian bắt đầu.");
        }

        if (showtime.getBasePrice() == null) {
            throw new IllegalArgumentException("Vui lòng nhập giá vé cơ bản.");
        }

        if (showtime.getBasePrice().compareTo(BigDecimal.ZERO) < 0) {
            throw new IllegalArgumentException("Giá vé cơ bản không được nhỏ hơn 0.");
        }

        Movie movie = movieDAO.findById(showtime.getMovieId());

        if (movie == null) {
            throw new IllegalArgumentException("Phim không tồn tại.");
        }

        if (movie.getDurationMin() <= 0) {
            throw new IllegalArgumentException("Phim chưa có thời lượng hợp lệ.");
        }

        String status = normalize(showtime.getStatus());

        if (status == null) {
            status = "SCHEDULED";
        }

        if (!VALID_STATUS.contains(status)) {
            throw new IllegalArgumentException("Trạng thái suất chiếu không hợp lệ.");
        }

        showtime.setStatus(status);

        LocalDateTime endTime = showtime.getStartTime().plusMinutes(movie.getDurationMin());
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