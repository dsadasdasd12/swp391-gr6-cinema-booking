/*
 * Hệ thống Quản lý Rạp chiếu phim RapViet
 * Module: Duyệt phim - suất chiếu liệt kê ở trang chi tiết phim
 */
package dao;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import dto.MovieShowtimes;
import model.Showtime;
import util.DBContext;

/**
 * DAO xử lý suất chiếu.
 * 
 * Gồm 2 phần:
 * 1. findUpcomingByMovie(): phục vụ trang chi tiết phim của khách.
 * 2. Các hàm Manager dùng cho Showtime Management.
 */
public class ShowtimeDAO {

    /**
     * Các suất chiếu sắp tới, còn bán vé của một phim.
     * Giữ lại hàm cũ để không ảnh hưởng module Browse Movie.
     */
    public List<Showtime> findUpcomingByMovie(int movieId) {
        String sql = "SELECT s.id, s.movie_id, s.hall_id, s.start_time, s.end_time, "
                + "s.base_price, s.status, "
                + "m.title AS movie_title, m.duration_min AS movie_duration_min, "
                + "h.name AS hall_name, h.hall_type, "
                + "b.id AS branch_id, b.name AS branch_name, b.address AS branch_address "
                + "FROM dbo.SHOWTIMES s "
                + "JOIN dbo.MOVIES m ON m.id = s.movie_id "
                + "JOIN dbo.HALLS h ON h.id = s.hall_id "
                + "JOIN dbo.BRANCHES b ON b.id = h.branch_id "
                + "WHERE s.movie_id = ? "
                + "AND s.status IN ('SCHEDULED','ON_SALE') "
                + "AND s.start_time >= ? "
                + "ORDER BY s.start_time, b.name";

        List<Showtime> list = new ArrayList<>();
        Connection conn = DBContext.getInstance().getConnection();

        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, movieId);
            ps.setObject(2, LocalDateTime.now());

            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    list.add(mapRow(rs));
                }
            }

        } catch (SQLException e) {
            System.getLogger(ShowtimeDAO.class.getName())
                    .log(System.Logger.Level.ERROR, "findUpcomingByMovie thất bại", e);
        }

        return list;
    }

    /**
     * (Phần KHÁCH xem) Suất chiếu của một chi nhánh trong một ngày, chỉ lấy suất
     * còn bán vé (SCHEDULED/ON_SALE), đã gom nhóm theo phim. Poster phim
     * (MOVIES.poster_url) được mang theo bởi DTO MovieShowtimes nên entity
     * Showtime không cần thêm cột ngoài bảng SHOWTIMES.
     */
    public List<MovieShowtimes> findByBranchAndDate(int branchId, LocalDate date) {
        String sql = "SELECT s.id, s.movie_id, s.hall_id, s.start_time, s.end_time, "
                + "s.base_price, s.status, "
                + "m.title AS movie_title, m.duration_min AS movie_duration_min, m.poster_url AS poster_url, "
                + "h.name AS hall_name, h.hall_type, "
                + "b.id AS branch_id, b.name AS branch_name, b.address AS branch_address "
                + "FROM dbo.SHOWTIMES s "
                + "JOIN dbo.MOVIES m ON m.id = s.movie_id "
                + "JOIN dbo.HALLS h ON h.id = s.hall_id "
                + "JOIN dbo.BRANCHES b ON b.id = h.branch_id "
                + "WHERE b.id = ? "
                + "AND CAST(s.start_time AS DATE) = ? "
                + "AND s.status IN ('SCHEDULED','ON_SALE') "
                + "ORDER BY m.title, s.start_time";

        // Gom theo phim ngay khi đọc; LinkedHashMap giữ thứ tự tên phim (đã ORDER BY)
        Map<Integer, MovieShowtimes> grouped = new LinkedHashMap<>();
        Connection conn = DBContext.getInstance().getConnection();

        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, branchId);
            ps.setObject(2, date);

            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    int movieId = rs.getInt("movie_id");
                    MovieShowtimes ms = grouped.get(movieId);
                    if (ms == null) {
                        ms = new MovieShowtimes(movieId,
                                rs.getString("movie_title"), rs.getString("poster_url"));
                        grouped.put(movieId, ms);
                    }
                    ms.getShowtimes().add(mapRow(rs));
                }
            }

        } catch (SQLException e) {
            System.getLogger(ShowtimeDAO.class.getName())
                    .log(System.Logger.Level.ERROR, "findByBranchAndDate thất bại", e);
        }

        return new ArrayList<>(grouped.values());
    }

    /**
     * (Phần KHÁCH xem) Một suất chiếu theo id (join phim/phòng/chi nhánh) để lấy
     * ngữ cảnh cho trang sơ đồ ghế; trả null nếu không tồn tại.
     */
    public Showtime findById(int showtimeId) {
        String sql = "SELECT s.id, s.movie_id, s.hall_id, s.start_time, s.end_time, "
                + "s.base_price, s.status, "
                + "m.title AS movie_title, m.duration_min AS movie_duration_min, "
                + "h.name AS hall_name, h.hall_type, "
                + "b.id AS branch_id, b.name AS branch_name, b.address AS branch_address "
                + "FROM dbo.SHOWTIMES s "
                + "JOIN dbo.MOVIES m ON m.id = s.movie_id "
                + "JOIN dbo.HALLS h ON h.id = s.hall_id "
                + "JOIN dbo.BRANCHES b ON b.id = h.branch_id "
                + "WHERE s.id = ?";

        Connection conn = DBContext.getInstance().getConnection();

        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, showtimeId);

            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return mapRow(rs);
                }
            }

        } catch (SQLException e) {
            System.getLogger(ShowtimeDAO.class.getName())
                    .log(System.Logger.Level.ERROR, "findById thất bại", e);
        }

        return null;
    }

    /**
     * Manager xem các suất chiếu thuộc các branch được phân công.
     */
    public List<Showtime> findByManagerId(int managerId) {
        String sql = "SELECT s.id, s.movie_id, s.hall_id, s.start_time, s.end_time, "
                + "s.base_price, s.status, "
                + "m.title AS movie_title, m.duration_min AS movie_duration_min, "
                + "h.name AS hall_name, h.hall_type, "
                + "b.id AS branch_id, b.name AS branch_name, b.address AS branch_address "
                + "FROM dbo.SHOWTIMES s "
                + "JOIN dbo.MOVIES m ON m.id = s.movie_id "
                + "JOIN dbo.HALLS h ON h.id = s.hall_id "
                + "JOIN dbo.BRANCHES b ON b.id = h.branch_id "
                + "JOIN dbo.STAFF_BRANCH sb ON sb.branch_id = b.id "
                + "WHERE sb.user_id = ? "
                + "ORDER BY s.start_time DESC, s.id DESC";

        List<Showtime> list = new ArrayList<>();
        Connection conn = DBContext.getInstance().getConnection();

        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, managerId);

            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    list.add(mapRow(rs));
                }
            }

        } catch (SQLException e) {
            System.getLogger(ShowtimeDAO.class.getName())
                    .log(System.Logger.Level.ERROR, "findByManagerId thất bại", e);
        }

        return list;
    }

    /**
     * Lấy 1 suất chiếu theo id, đồng thời check suất đó có thuộc branch của manager không.
     */
    public Showtime findByIdAndManagerId(int id, int managerId) {
        String sql = "SELECT s.id, s.movie_id, s.hall_id, s.start_time, s.end_time, "
                + "s.base_price, s.status, "
                + "m.title AS movie_title, m.duration_min AS movie_duration_min, "
                + "h.name AS hall_name, h.hall_type, "
                + "b.id AS branch_id, b.name AS branch_name, b.address AS branch_address "
                + "FROM dbo.SHOWTIMES s "
                + "JOIN dbo.MOVIES m ON m.id = s.movie_id "
                + "JOIN dbo.HALLS h ON h.id = s.hall_id "
                + "JOIN dbo.BRANCHES b ON b.id = h.branch_id "
                + "JOIN dbo.STAFF_BRANCH sb ON sb.branch_id = b.id "
                + "WHERE s.id = ? AND sb.user_id = ?";

        Connection conn = DBContext.getInstance().getConnection();

        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, id);
            ps.setInt(2, managerId);

            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return mapRow(rs);
                }
            }

        } catch (SQLException e) {
            System.getLogger(ShowtimeDAO.class.getName())
                    .log(System.Logger.Level.ERROR, "findByIdAndManagerId thất bại", e);
        }

        return null;
    }

    /**
     * Thêm suất chiếu mới.
     */
   public boolean insert(Showtime showtime) {
    String sql = "INSERT INTO dbo.SHOWTIMES "
            + "(hall_id, movie_id, start_time, end_time, base_price, status) "
            + "VALUES (?, ?, ?, ?, ?, ?)";

    Connection conn = DBContext.getInstance().getConnection();

    try (PreparedStatement ps = conn.prepareStatement(sql)) {
        ps.setInt(1, showtime.getHallId());
        ps.setInt(2, showtime.getMovieId());
        ps.setObject(3, showtime.getStartTime());
        ps.setObject(4, showtime.getEndTime());
        ps.setBigDecimal(5, showtime.getBasePrice());
        ps.setString(6, showtime.getStatus());

        return ps.executeUpdate() > 0;

    } catch (SQLException e) {
        System.getLogger(ShowtimeDAO.class.getName())
                .log(System.Logger.Level.ERROR, "insert showtime thất bại", e);
    }

    return false;
}

    /**
     * Cập nhật suất chiếu.
     */
    public boolean update(Showtime showtime) {
        String sql = "UPDATE dbo.SHOWTIMES "
                + "SET hall_id = ?, movie_id = ?, start_time = ?, end_time = ?, "
                + "base_price = ?, status = ?, last_update = GETDATE() "
                + "WHERE id = ?";

        Connection conn = DBContext.getInstance().getConnection();

        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, showtime.getHallId());
            ps.setInt(2, showtime.getMovieId());
            ps.setObject(3, showtime.getStartTime());
            ps.setObject(4, showtime.getEndTime());
            ps.setBigDecimal(5, showtime.getBasePrice());
            ps.setString(6, showtime.getStatus());
            ps.setInt(7, showtime.getId());

            return ps.executeUpdate() > 0;

        } catch (SQLException e) {
            System.getLogger(ShowtimeDAO.class.getName())
                    .log(System.Logger.Level.ERROR, "update showtime thất bại", e);
        }

        return false;
    }

    /**
     * Hủy suất chiếu, không xóa cứng.
     */
    public boolean cancel(int id) {
        String sql = "UPDATE dbo.SHOWTIMES "
                + "SET status = 'CANCELLED', last_update = GETDATE() "
                + "WHERE id = ?";

        Connection conn = DBContext.getInstance().getConnection();

        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, id);
            return ps.executeUpdate() > 0;

        } catch (SQLException e) {
            System.getLogger(ShowtimeDAO.class.getName())
                    .log(System.Logger.Level.ERROR, "cancel showtime thất bại", e);
        }

        return false;
    }

    /**
     * Check trùng lịch trong cùng một Hall.
     * 
     * Điều kiện trùng:
     * newStart < oldEnd AND newEnd > oldStart
     */
    public boolean hasScheduleConflict(int hallId,
            LocalDateTime startTime,
            LocalDateTime endTime,
            int exceptShowtimeId) {

        String sql = "SELECT COUNT(*) "
                + "FROM dbo.SHOWTIMES "
                + "WHERE hall_id = ? "
                + "AND status <> 'CANCELLED' "
                + "AND start_time < ? "
                + "AND end_time > ? "
                + "AND (? = 0 OR id <> ?)";

        Connection conn = DBContext.getInstance().getConnection();

        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, hallId);
            ps.setObject(2, endTime);
            ps.setObject(3, startTime);
            ps.setInt(4, exceptShowtimeId);
            ps.setInt(5, exceptShowtimeId);

            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return rs.getInt(1) > 0;
                }
            }

        } catch (SQLException e) {
            System.getLogger(ShowtimeDAO.class.getName())
                    .log(System.Logger.Level.ERROR, "hasScheduleConflict thất bại", e);
        }

        return false;
    }

    private Showtime mapRow(ResultSet rs) throws SQLException {
        Showtime st = new Showtime();

        st.setId(rs.getInt("id"));
        st.setMovieId(rs.getInt("movie_id"));
        st.setHallId(rs.getInt("hall_id"));
        st.setStartTime(rs.getObject("start_time", LocalDateTime.class));
        st.setEndTime(rs.getObject("end_time", LocalDateTime.class));
        st.setBasePrice(rs.getBigDecimal("base_price"));
        st.setStatus(rs.getString("status"));

        st.setMovieTitle(rs.getString("movie_title"));
        st.setMovieDurationMin(rs.getInt("movie_duration_min"));

        st.setHallName(rs.getString("hall_name"));
        st.setHallType(rs.getString("hall_type"));
        st.setBranchId(rs.getInt("branch_id"));
        st.setBranchName(rs.getString("branch_name"));
        st.setBranchAddress(rs.getString("branch_address"));

        return st;
    }
}