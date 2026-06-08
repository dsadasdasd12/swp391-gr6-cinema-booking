package dao;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.Timestamp;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import model.Showtime;
import util.DBContext;

public class ShowtimeDAO {

    // ==========================================
    // 1. Movie Browsing Module (Original Git Method)
    // ==========================================
    
    /**
     * Các suất chiếu sắp tới, còn bán vé của một phim, sắp theo thời gian. Chỉ
     * lấy suất ở tương lai và có trạng thái SCHEDULED hoặc ON_SALE.
     */
    public List<Showtime> findUpcomingByMovie(int movieId) {
        String sql = "SELECT s.id, s.movie_id, s.hall_id, s.start_time, s.end_time, "
                + "       s.base_price, s.status, "
                + "       h.name AS hall_name, h.hall_type, "
                + "       b.id AS branch_id, b.name AS branch_name, b.address AS branch_address "
                + "FROM dbo.SHOWTIMES s "
                + "JOIN dbo.HALLS h    ON h.id = s.hall_id "
                + "JOIN dbo.BRANCHES b ON b.id = h.branch_id "
                + "WHERE s.movie_id = ? "
                + "  AND s.status IN ('SCHEDULED','ON_SALE') "
                + "  AND s.start_time >= ? "
                + "ORDER BY s.start_time, b.name";
        List<Showtime> list = new ArrayList<>();
        try (Connection conn = new DBContext().getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
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
                    st.setHallName(rs.getString("hall_name"));
                    st.setHallType(rs.getString("hall_type"));
                    st.setBranchId(rs.getInt("branch_id"));
                    st.setBranchName(rs.getString("branch_name"));
                    st.setBranchAddress(rs.getString("branch_address"));
                    list.add(st);
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return list;
    }

    // ==========================================
    // 2. Showtime Manager & Booking Modules (Workspace Methods)
    // ==========================================

    // READ: Lấy danh sách suất chiếu của chi nhánh
    public List<Showtime> getActiveShowtimesByBranch(int branchId) {
        List<Showtime> list = new ArrayList<>();
        String sql = "SELECT st.id, st.hall_id, st.movie_id, st.start_time, st.end_time, st.base_price, st.status, "
                   + "       m.title AS movie_title, m.poster_url AS movie_poster, h.name AS hall_name "
                   + "FROM dbo.SHOWTIMES st "
                   + "JOIN dbo.MOVIES m ON st.movie_id = m.id "
                   + "JOIN dbo.HALLS h ON st.hall_id = h.id "
                   + "WHERE h.branch_id = ? AND st.status IN ('ON_SALE', 'SCHEDULED') AND st.start_time >= GETDATE() "
                   + "ORDER BY st.start_time ASC";
        try (Connection conn = new DBContext().getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
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
                    list.add(st);
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return list;
    }

    // READ SINGLE: Lấy thông tin một suất chiếu cụ thể
    public Showtime getShowtimeById(int id) {
        String sql = "SELECT st.id, st.hall_id, st.movie_id, st.start_time, st.end_time, st.base_price, st.status, "
                   + "       m.title AS movie_title, m.poster_url AS movie_poster, h.name AS hall_name "
                   + "FROM dbo.SHOWTIMES st "
                   + "JOIN dbo.MOVIES m ON st.movie_id = m.id "
                   + "JOIN dbo.HALLS h ON st.hall_id = h.id "
                   + "WHERE st.id = ?";
        try (Connection conn = new DBContext().getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
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
                    return st;
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }

    // WRITE: Thiết lập cấu hình giá vé riêng cho loại ghế
    public boolean setSeatPricing(int showtimeId, String seatType, double price) {
        String checkSql = "SELECT id FROM dbo.SEAT_PRICING WHERE showtime_id = ? AND seat_type = ?";
        String updateSql = "UPDATE dbo.SEAT_PRICING SET price = ?, last_update = GETDATE() WHERE showtime_id = ? AND seat_type = ?";
        String insertSql = "INSERT INTO dbo.SEAT_PRICING (showtime_id, seat_type, price) VALUES (?, ?, ?)";
        
        try (Connection conn = new DBContext().getConnection()) {
            boolean exists = false;
            try (PreparedStatement ps = conn.prepareStatement(checkSql)) {
                ps.setInt(1, showtimeId);
                ps.setString(2, seatType);
                try (ResultSet rs = ps.executeQuery()) {
                    if (rs.next()) exists = true;
                }
            }
            
            if (exists) {
                try (PreparedStatement ps = conn.prepareStatement(updateSql)) {
                    ps.setDouble(1, price);
                    ps.setInt(2, showtimeId);
                    ps.setString(3, seatType);
                    return ps.executeUpdate() > 0;
                }
            } else {
                try (PreparedStatement ps = conn.prepareStatement(insertSql)) {
                    ps.setInt(1, showtimeId);
                    ps.setString(2, seatType);
                    ps.setDouble(3, price);
                    return ps.executeUpdate() > 0;
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return false;
    }

    // READ: Lấy giá vé thực tế của ghế cho suất chiếu (Bảng SEAT_PRICING hoặc fallback về SHOWTIMES.base_price)
    public double getSeatPrice(int showtimeId, String seatType, double basePrice) {
        String sql = "SELECT price FROM dbo.SEAT_PRICING WHERE showtime_id = ? AND seat_type = ?";
        try (Connection conn = new DBContext().getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, showtimeId);
            ps.setString(2, seatType);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return rs.getDouble("price");
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
        try (Connection conn = new DBContext().getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
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
        try (Connection conn = new DBContext().getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
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
        if (total == 0) return 0;
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
        try (Connection conn = new DBContext().getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
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
}
