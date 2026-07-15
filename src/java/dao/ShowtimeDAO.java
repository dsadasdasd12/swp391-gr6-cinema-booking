package dao;

import dto.MovieShowtimes;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.Timestamp;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import model.Showtime;
import util.DBContext;
import util.EncodingUtil;
import java.sql.SQLException;
import java.time.LocalDate;

/**
 * DAO xử lý suất chiếu.
 *
 * Gồm 2 phần: 1. findUpcomingByMovie(): phục vụ trang chi tiết phim của khách.
 * 2. Các hàm Manager dùng cho Showtime Management.
 */
public class ShowtimeDAO {

    private static final int SHOWTIME_GAP_MINUTES = 15;

    // ==========================================
    // 1. Movie Browsing Module (Original Git Method)
    // ==========================================
    /**
     * Các suất chiếu sắp tới, còn bán vé của một phim. Giữ lại hàm cũ để không
     * ảnh hưởng module Browse Movie.
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
        try (Connection conn = new DBContext().getConnection(); PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, movieId);
            ps.setTimestamp(2, Timestamp.valueOf(LocalDateTime.now()));   // mốc "hiện tại" để loại suất đã qua
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    Showtime st = new Showtime();
                    st.setId(rs.getInt("id"));
                    st.setMovieId(rs.getInt("movie_id"));
                    st.setHallId(rs.getInt("hall_id"));
                    st.setStartTime(rs.getTimestamp("start_time"));
                    st.setEndTime(rs.getTimestamp("end_time"));
                    st.setBasePrice(rs.getBigDecimal("base_price"));
                    st.setStatus(rs.getString("status"));
                    st.setHallName(EncodingUtil.getString(rs, "hall_name"));
                    st.setHallType(rs.getString("hall_type"));
                    st.setBranchId(rs.getInt("branch_id"));
                    st.setBranchName(EncodingUtil.getString(rs, "branch_name"));
                    st.setBranchAddress(EncodingUtil.getString(rs, "branch_address"));
                    list.add(st);
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return list;
    }

    public List<Showtime> findByBranchId(int branchId) {
        String sql = "SELECT s.id, s.movie_id, s.hall_id, s.start_time, s.end_time, "
                + "s.base_price, s.status, "
                + "m.title AS movie_title, m.duration_min AS movie_duration_min, "
                + "h.name AS hall_name, h.hall_type, "
                + "b.id AS branch_id, b.name AS branch_name, b.address AS branch_address "
                + "FROM dbo.SHOWTIMES s "
                + "JOIN dbo.MOVIES m ON m.id = s.movie_id "
                + "JOIN dbo.HALLS h ON h.id = s.hall_id "
                + "JOIN dbo.BRANCHES b ON b.id = h.branch_id "
                + "WHERE h.branch_id = ? "
                + "AND s.status <> 'CANCELLED' "
                + "ORDER BY s.start_time DESC, s.id DESC";

        List<Showtime> list = new ArrayList<>();
        Connection conn = DBContext.getInstance().getConnection();

        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, branchId);

            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    list.add(mapRow(rs));
                }
            }

        } catch (SQLException e) {
            System.getLogger(ShowtimeDAO.class.getName())
                    .log(System.Logger.Level.ERROR,
                            "findByBranchId thất bại", e);
        }

        return list;
    }

    public List<Showtime> getActiveShowtimesByBranch(int branchId) {
        List<Showtime> list = new ArrayList<>();
        String sql = "SELECT st.id, st.hall_id, st.movie_id, st.start_time, st.end_time, st.base_price, st.status, "
                + "       m.title AS movie_title, m.poster_url AS movie_poster, h.name AS hall_name, h.branch_id AS branch_id "
                + "FROM dbo.SHOWTIMES st "
                + "JOIN dbo.MOVIES m ON st.movie_id = m.id "
                + "JOIN dbo.HALLS h ON st.hall_id = h.id "
                + "WHERE h.branch_id = ? AND st.status IN ('ON_SALE', 'SCHEDULED') AND st.start_time >= GETDATE() "
                + "ORDER BY st.start_time ASC";
        try (Connection conn = new DBContext().getConnection(); PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, branchId);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    Showtime st = new Showtime(
                            rs.getInt("id"),
                            rs.getInt("hall_id"),
                            rs.getInt("movie_id"),
                            rs.getTimestamp("start_time"),
                            rs.getTimestamp("end_time"),
                            rs.getDouble("base_price"),
                            rs.getString("status")
                    );
                    st.setMovieTitle(rs.getString("movie_title"));
                    st.setMoviePoster(rs.getString("movie_poster"));
                    st.setHallName(rs.getString("hall_name"));
                    st.setBranchId(rs.getInt("branch_id"));
                    list.add(st);
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return list;
    }

    /*manage chi sua id showtime thuoc branch hien tai*/
    public Showtime findByIdAndBranchId(int id, int branchId) {
        String sql = "SELECT s.id, s.movie_id, s.hall_id, s.start_time, s.end_time, "
                + "s.base_price, s.status, "
                + "m.title AS movie_title, m.duration_min AS movie_duration_min, "
                + "h.name AS hall_name, h.hall_type, "
                + "b.id AS branch_id, b.name AS branch_name, b.address AS branch_address "
                + "FROM dbo.SHOWTIMES s "
                + "JOIN dbo.MOVIES m ON m.id = s.movie_id "
                + "JOIN dbo.HALLS h ON h.id = s.hall_id "
                + "JOIN dbo.BRANCHES b ON b.id = h.branch_id "
                + "WHERE s.id = ? "
                + "AND h.branch_id = ?";

        Connection conn = DBContext.getInstance().getConnection();

        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, id);
            ps.setInt(2, branchId);

            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return mapRow(rs);
                }
            }

        } catch (SQLException e) {
            System.getLogger(ShowtimeDAO.class.getName())
                    .log(System.Logger.Level.ERROR,
                            "findByIdAndBranchId thất bại", e);
        }

        return null;
    }

    // READ SINGLE: Lấy thông tin một suất chiếu cụ thể
    public Showtime getShowtimeById(int id) {
        String sql = "SELECT st.id, st.hall_id, st.movie_id, st.start_time, st.end_time, st.base_price, st.status, "
                + "       m.title AS movie_title, m.poster_url AS movie_poster, h.name AS hall_name, h.branch_id AS branch_id "
                + "FROM dbo.SHOWTIMES st "
                + "JOIN dbo.MOVIES m ON st.movie_id = m.id "
                + "JOIN dbo.HALLS h ON st.hall_id = h.id "
                + "WHERE st.id = ?";
        try (Connection conn = new DBContext().getConnection(); PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, id);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    Showtime st = new Showtime(
                            rs.getInt("id"),
                            rs.getInt("hall_id"),
                            rs.getInt("movie_id"),
                            rs.getTimestamp("start_time"),
                            rs.getTimestamp("end_time"),
                            rs.getDouble("base_price"),
                            rs.getString("status")
                    );
                    st.setMovieTitle(rs.getString("movie_title"));
                    st.setMoviePoster(rs.getString("movie_poster"));
                    st.setHallName(rs.getString("hall_name"));
                    st.setBranchId(rs.getInt("branch_id"));
                    return st;
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }

    // CALCULATE: Lấy giá vé thực tế của ghế bằng cách nhân giá gốc (basePrice) với hệ số nhân (multiplier) của loại ghế đó trong SEAT_TYPES
    public double getSeatPrice(int showtimeId, String seatType, double basePrice) {
        String sql = "SELECT default_price FROM dbo.SEAT_TYPES WHERE code = ?";
        try (Connection conn = DBContext.getInstance().getConnection(); PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, seatType);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    double multiplier = rs.getDouble("default_price");
                    // Tránh trường hợp hệ số nhân bị cấu hình sai <= 0
                    if (multiplier > 0) {
                        return basePrice * multiplier;
                    }
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return basePrice;
    }

    // CALCULATE: Đếm số ghế đã đặt của suất chiếu
    public int getBookedSeatsCount(int showtimeId) {
        String sql = "SELECT COUNT(bs.seat_id) AS booked_count "
                + "FROM dbo.BOOKING_SEATS bs "
                + "JOIN dbo.BOOKINGS b ON bs.booking_id = b.id "
                + "WHERE b.showtime_id = ? AND b.status IN ('CONFIRMED', 'CHECKED_IN', 'USED')";
        try (Connection conn = new DBContext().getConnection(); PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, showtimeId);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return rs.getInt("booked_count");
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return 0;
    }

    // CALCULATE: Lấy tổng số ghế thiết kế của phòng chiếu cho suất chiếu
    public int getTotalSeatsInHall(int showtimeId) {
        String sql = "SELECT h.total_seats "
                + "FROM dbo.SHOWTIMES st "
                + "JOIN dbo.HALLS h ON st.hall_id = h.id "
                + "WHERE st.id = ?";
        try (Connection conn = new DBContext().getConnection(); PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, showtimeId);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return rs.getInt("total_seats");
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return 0;
    }

    // CALCULATE: Tính tỷ lệ lấp đầy trực tiếp
    public double getOccupancyRate(int showtimeId) {
        int total = getTotalSeatsInHall(showtimeId);
        if (total == 0) {
            return 0;
        }
        int booked = getBookedSeatsCount(showtimeId);
        return Math.round(((double) booked / total * 100) * 10.0) / 10.0;
    }

    // READ: Lấy toàn bộ danh sách suất chiếu của chi nhánh theo ngày cụ thể (cho giám sát suất chiếu)
    public List<Showtime> getShowtimesByBranchAndDate(int branchId, String dateStr) {
        List<Showtime> list = new ArrayList<>();
        String sql = "SELECT st.id, st.hall_id, st.movie_id, st.start_time, st.end_time, st.base_price, st.status, "
                + "       m.title AS movie_title, m.poster_url AS movie_poster, h.name AS hall_name "
                + "FROM dbo.SHOWTIMES st "
                + "JOIN dbo.MOVIES m ON st.movie_id = m.id "
                + "JOIN dbo.HALLS h ON st.hall_id = h.id "
                + "WHERE h.branch_id = ? AND CONVERT(date, st.start_time) = ? "
                + "ORDER BY st.start_time ASC";
        try (Connection conn = new DBContext().getConnection(); PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, branchId);
            ps.setString(2, dateStr);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    Showtime st = new Showtime(
                            rs.getInt("id"),
                            rs.getInt("hall_id"),
                            rs.getInt("movie_id"),
                            rs.getTimestamp("start_time"),
                            rs.getTimestamp("end_time"),
                            rs.getDouble("base_price"),
                            rs.getString("status")
                    );
                    st.setMovieTitle(rs.getString("movie_title"));
                    st.setMoviePoster(rs.getString("movie_poster"));
                    st.setHallName(rs.getString("hall_name"));
                    list.add(st);
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }

        return list;
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
     * Lấy 1 suất chiếu theo id, đồng thời check suất đó có thuộc branch của
     * manager không.
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
            ps.setDouble(5, showtime.getBasePrice());
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
            ps.setDouble(5, showtime.getBasePrice());
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
     * Công thức mới mở rộng khoảng cần kiểm tra thêm 15 phút ở cả 2 đầu:
     * oldStart < newEnd + 15 phút
     * AND oldEnd > newStart - 15 phút
     *
     * exceptShowtimeId dùng cho update để loại trừ chính suất chiếu đang sửa.
     */
    public boolean hasScheduleConflict(int hallId,
            LocalDateTime startTime,
            LocalDateTime endTime,
            int exceptShowtimeId) {

        LocalDateTime conflictStartTime = startTime.minusMinutes(
                SHOWTIME_GAP_MINUTES
        );

        LocalDateTime conflictEndTime = endTime.plusMinutes(
                SHOWTIME_GAP_MINUTES
        );

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
            ps.setObject(2, conflictEndTime);
            ps.setObject(3, conflictStartTime);
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

    public Showtime findById(int id) {
        String sql
                = "SELECT s.id, s.movie_id, s.hall_id, s.start_time, s.end_time, "
                + "       s.base_price, s.status, "
                + "       m.title AS movie_title, m.duration_min AS movie_duration_min, "
                + "       h.name AS hall_name, h.hall_type, "
                + "       b.id AS branch_id, b.name AS branch_name, b.address AS branch_address "
                + "FROM dbo.SHOWTIMES s "
                + "JOIN dbo.MOVIES m ON m.id = s.movie_id "
                + "JOIN dbo.HALLS h ON h.id = s.hall_id "
                + "JOIN dbo.BRANCHES b ON b.id = h.branch_id "
                + "WHERE s.id = ?";

        try (Connection conn = DBContext.getInstance().getConnection(); PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setInt(1, id);

            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return mapRow(rs);
                }
            }

        } catch (Exception e) {
            e.printStackTrace();
        }

        return null;
    }

    public Showtime findBookableById(int id) {
        String sql
                = "SELECT s.id, s.movie_id, s.hall_id, s.start_time, s.end_time, "
                + "       s.base_price, s.status, "
                + "       m.title AS movie_title, m.duration_min AS movie_duration_min, "
                + "       h.name AS hall_name, h.hall_type, "
                + "       b.id AS branch_id, b.name AS branch_name, b.address AS branch_address "
                + "FROM dbo.SHOWTIMES s "
                + "JOIN dbo.MOVIES m ON m.id = s.movie_id "
                + "JOIN dbo.HALLS h ON h.id = s.hall_id "
                + "JOIN dbo.BRANCHES b ON b.id = h.branch_id "
                + "WHERE s.id = ? "
                + "AND s.status IN ('SCHEDULED','ON_SALE') "
                + "AND s.start_time > GETDATE()";

        try (Connection conn = DBContext.getInstance().getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setInt(1, id);

            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return mapRow(rs);
                }
            }

        } catch (Exception e) {
            e.printStackTrace();
        }

        return null;
    }

    public List<Showtime> findBookableByBranchMovieAndDate(
            int branchId,
            int movieId,
            LocalDate date) {

        List<Showtime> list = new ArrayList<>();

        String sql
                = "SELECT s.id, s.movie_id, s.hall_id, s.start_time, s.end_time, "
                + "       s.base_price, s.status, "
                + "       m.title AS movie_title, m.duration_min AS movie_duration_min, "
                + "       h.name AS hall_name, h.hall_type, "
                + "       b.id AS branch_id, b.name AS branch_name, b.address AS branch_address "
                + "FROM dbo.SHOWTIMES s "
                + "JOIN dbo.MOVIES m ON m.id = s.movie_id "
                + "JOIN dbo.HALLS h ON h.id = s.hall_id "
                + "JOIN dbo.BRANCHES b ON b.id = h.branch_id "
                + "WHERE h.branch_id = ? "
                + "AND s.movie_id = ? "
                + "AND CONVERT(date, s.start_time) = ? "
                + "AND s.status IN ('SCHEDULED','ON_SALE') "
                + "AND s.start_time > GETDATE() "
                + "ORDER BY s.start_time, h.name";

        try (Connection conn = DBContext.getInstance().getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setInt(1, branchId);
            ps.setInt(2, movieId);
            ps.setDate(3, java.sql.Date.valueOf(date));

            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    list.add(mapRow(rs));
                }
            }

        } catch (Exception e) {
            e.printStackTrace();
        }

        return list;
    }

    /**
     * Returns all bookable showtimes of one movie in one calendar week.
     * weekStart is Monday inclusive; the following Monday is exclusive.
     */
    public List<Showtime> findBookableByBranchMovieAndWeek(
            int branchId, int movieId, LocalDate weekStart) {
        List<Showtime> list = new ArrayList<>();
        LocalDate weekEndExclusive = weekStart.plusDays(7);
        String sql
                = "SELECT s.id, s.movie_id, s.hall_id, s.start_time, s.end_time, "
                + "       s.base_price, s.status, "
                + "       m.title AS movie_title, m.duration_min AS movie_duration_min, "
                + "       h.name AS hall_name, h.hall_type, "
                + "       b.id AS branch_id, b.name AS branch_name, b.address AS branch_address "
                + "FROM dbo.SHOWTIMES s "
                + "JOIN dbo.MOVIES m ON m.id = s.movie_id "
                + "JOIN dbo.HALLS h ON h.id = s.hall_id "
                + "JOIN dbo.BRANCHES b ON b.id = h.branch_id "
                + "WHERE h.branch_id = ? "
                + "AND s.movie_id = ? "
                + "AND s.start_time >= ? "
                + "AND s.start_time < ? "
                + "AND s.status IN ('SCHEDULED','ON_SALE') "
                + "AND s.start_time > GETDATE() "
                + "ORDER BY s.start_time, h.name";
        try (Connection conn = DBContext.getInstance().getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, branchId);
            ps.setInt(2, movieId);
            ps.setTimestamp(3, Timestamp.valueOf(weekStart.atStartOfDay()));
            ps.setTimestamp(4, Timestamp.valueOf(weekEndExclusive.atStartOfDay()));
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    list.add(mapRow(rs));
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return list;
    }

    public List<MovieShowtimes> findByBranchAndDate(
            int branchId,
            LocalDate date) {

        List<MovieShowtimes> result = new ArrayList<>();

        String sql
                = "SELECT s.id,s.movie_id,s.hall_id,s.start_time,s.end_time,"
                + "s.base_price,s.status,"
                + "m.title AS movie_title,"
                + "m.poster_url,"
                + "m.duration_min AS movie_duration_min,"
                + "h.name AS hall_name,"
                + "h.hall_type,"
                + "b.id AS branch_id,"
                + "b.name AS branch_name,"
                + "b.address AS branch_address "
                + "FROM SHOWTIMES s "
                + "JOIN MOVIES m ON m.id=s.movie_id "
                + "JOIN HALLS h ON h.id=s.hall_id "
                + "JOIN BRANCHES b ON b.id=h.branch_id "
                + "WHERE h.branch_id=? "
                + "AND CONVERT(date,s.start_time)=? "
                + "AND s.status IN('SCHEDULED','ON_SALE') "
                + "ORDER BY m.title,s.start_time";

        try (Connection conn = DBContext.getInstance().getConnection(); PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setInt(1, branchId);
            ps.setDate(2, java.sql.Date.valueOf(date));

            ResultSet rs = ps.executeQuery();

            MovieShowtimes current = null;
            int lastMovie = -1;

            while (rs.next()) {

                Showtime st = mapRow(rs);
                st.setMoviePoster(rs.getString("poster_url"));

                if (lastMovie != st.getMovieId()) {

                    current = new MovieShowtimes(
                            st.getMovieId(),
                            st.getMovieTitle(),
                            rs.getString("poster_url"));

                    result.add(current);
                    lastMovie = st.getMovieId();
                }

                current.getShowtimes().add(st);
            }

        } catch (Exception e) {
            e.printStackTrace();
        }

        return result;
    }

    public boolean hasFutureShowtimes(int hallId) {
        String sql = "SELECT COUNT(*) FROM dbo.SHOWTIMES WHERE hall_id = ? AND start_time > GETDATE() AND status != 'CANCELLED'";
        try (Connection conn = DBContext.getInstance().getConnection(); PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, hallId);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return rs.getInt(1) > 0;
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return false;
    }

    public boolean hasBookings(int showtimeId) {
        String sql = "SELECT COUNT(*) FROM dbo.BOOKINGS WHERE showtime_id = ? AND status != 'CANCELLED'";
        try (Connection conn = DBContext.getInstance().getConnection(); PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, showtimeId);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return rs.getInt(1) > 0;
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return false;
    }
}
