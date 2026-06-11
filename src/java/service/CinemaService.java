/*
 * Hệ thống Quản lý Rạp chiếu phim RapViet
 * Module: Xem chi nhánh rạp / Suất chiếu / Sơ đồ ghế
 */
package service;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import dao.BranchDAO;
import dao.SeatDAO;
import dao.ShowtimeDAO;
import dto.MovieShowtimes;
import dto.SeatMap;
import dto.SeatRow;
import model.Branch;
import model.Seat;
import model.Showtime;

/**
 * Tầng nghiệp vụ cho nhóm chức năng liên quan tới rạp: xem chi nhánh, (sau này)
 * xem suất chiếu theo chi nhánh và sơ đồ ghế. Controller chỉ làm việc với lớp
 * này, không gọi trực tiếp DAO.
 *
 * @author Group6 - Huy (Module Duyệt phim)
 */
public class CinemaService {

    private final BranchDAO branchDAO = new BranchDAO();
    private final ShowtimeDAO showtimeDAO = new ShowtimeDAO();
    private final SeatDAO seatDAO = new SeatDAO();

    /** Danh sách chi nhánh đang hoạt động cho trang "Hệ thống rạp". */
    public List<Branch> getActiveBranches() {
        return branchDAO.findAllActive();
    }

    /** Chi tiết một chi nhánh, hoặc {@code null} nếu id không hợp lệ / không tồn tại. */
    public Branch getBranch(int branchId) {
        if (branchId <= 0) {
            return null;
        }
        return branchDAO.findById(branchId);
    }

    /**
     * Suất chiếu của một chi nhánh trong một ngày, đã gom nhóm theo phim để
     * view chỉ việc lặp. Trả về danh sách rỗng nếu tham số không hợp lệ. Thứ tự
     * phim theo tên (DAO đã sắp), trong mỗi phim các suất giữ thứ tự thời gian.
     */
    public List<MovieShowtimes> getShowtimesByMovie(int branchId, LocalDate date) {
        if (branchId <= 0 || date == null) {
            return new ArrayList<>();
        }
        List<Showtime> all = showtimeDAO.findByBranchAndDate(branchId, date);
        Map<Integer, MovieShowtimes> grouped = new LinkedHashMap<>();
        for (Showtime st : all) {
            MovieShowtimes ms = grouped.computeIfAbsent(st.getMovieId(),
                    k -> new MovieShowtimes(st.getMovieId(), st.getMovieTitle(), st.getPosterUrl()));
            ms.getShowtimes().add(st);
        }
        return new ArrayList<>(grouped.values());
    }

    /**
     * Sơ đồ ghế của một suất chiếu: ngữ cảnh suất + các hàng ghế đã gom nhóm +
     * số ghế còn trống. Trả về {@code null} nếu suất chiếu không tồn tại để
     * controller hiển thị trang 404 thân thiện.
     */
    public SeatMap getSeatMap(int showtimeId) {
        if (showtimeId <= 0) {
            return null;
        }
        Showtime showtime = showtimeDAO.findById(showtimeId);
        if (showtime == null) {
            return null;
        }

        // Gom ghế theo hàng (DAO đã sắp theo hàng rồi số ghế nên thứ tự đúng)
        List<Seat> seats = seatDAO.findByShowtime(showtimeId);
        Map<String, SeatRow> rows = new LinkedHashMap<>();
        int available = 0;
        for (Seat seat : seats) {
            SeatRow row = rows.computeIfAbsent(seat.getSeatRow(), SeatRow::new);
            row.getSeats().add(seat);
            if (seat.isSelectable()) {
                available++;
            }
        }

        SeatMap map = new SeatMap();
        map.setShowtime(showtime);
        map.setRows(new ArrayList<>(rows.values()));
        map.setTotalSeats(seats.size());
        map.setAvailableSeats(available);
        return map;
    }
}
