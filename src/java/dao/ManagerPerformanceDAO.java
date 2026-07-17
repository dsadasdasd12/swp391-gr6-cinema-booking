package dao;

import dto.ManagerHallOccupancy;
import dto.ManagerPerformanceReport;
import dto.ManagerShowtimeProgress;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.List;
import util.DBContext;

/** Query riêng cho manager; không gọi DAO/servlet báo cáo của admin. */
public class ManagerPerformanceDAO {
    private static final String SOLD_BOOKING_STATUSES = "('CONFIRMED','CHECKED_IN','USED','COMPLETED')";

    public int getAssignedBranchId(int managerId) {
        String sql = "SELECT branch_id FROM dbo.STAFF_BRANCH WHERE user_id = ?";
        try (Connection conn = new DBContext().getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, managerId);
            try (ResultSet rs = ps.executeQuery()) {
                return rs.next() ? rs.getInt("branch_id") : 0;
            }
        } catch (Exception e) {
            e.printStackTrace();
            return 0;
        }
    }

    public ManagerPerformanceReport getReport(int branchId, Timestamp from, Timestamp toExclusive) {
        List<ManagerShowtimeProgress> showtimes = getShowtimeProgress(branchId, from, toExclusive);
        List<ManagerHallOccupancy> halls = getHallOccupancy(branchId, from, toExclusive);
        int totalCapacity = 0;
        int soldSeats = 0;
        for (ManagerShowtimeProgress showtime : showtimes) {
            totalCapacity += showtime.getCapacity();
            soldSeats += showtime.getSoldSeats();
        }
        return new ManagerPerformanceReport(showtimes.size(), totalCapacity, soldSeats, showtimes, halls);
    }

    private List<ManagerShowtimeProgress> getShowtimeProgress(int branchId, Timestamp from, Timestamp toExclusive) {
        List<ManagerShowtimeProgress> rows = new ArrayList<>();
        String sql = "SELECT st.id, m.title AS movie_title, h.name AS hall_name, st.start_time, h.total_seats, "
                + "COUNT(bs.id) AS sold_seats "
                + "FROM dbo.SHOWTIMES st "
                + "JOIN dbo.HALLS h ON h.id = st.hall_id "
                + "JOIN dbo.MOVIES m ON m.id = st.movie_id "
                + "LEFT JOIN dbo.BOOKINGS b ON b.showtime_id = st.id AND b.status IN " + SOLD_BOOKING_STATUSES + " "
                + "LEFT JOIN dbo.BOOKING_SEATS bs ON bs.booking_id = b.id "
                + "WHERE h.branch_id = ? AND st.start_time >= ? AND st.start_time < ? "
                + "GROUP BY st.id, m.title, h.name, st.start_time, h.total_seats "
                + "ORDER BY st.start_time ASC";
        try (Connection conn = new DBContext().getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            bindRange(ps, branchId, from, toExclusive);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    rows.add(new ManagerShowtimeProgress(rs.getString("movie_title"),
                            rs.getString("hall_name"), rs.getTimestamp("start_time"),
                            rs.getInt("total_seats"), rs.getInt("sold_seats")));
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return rows;
    }

    private List<ManagerHallOccupancy> getHallOccupancy(int branchId, Timestamp from, Timestamp toExclusive) {
        List<ManagerHallOccupancy> rows = new ArrayList<>();
        String sql = "SELECT h.name AS hall_name, COUNT(DISTINCT st.id) AS showtime_count, "
                + "MAX(h.total_seats) * COUNT(DISTINCT st.id) AS total_capacity, COUNT(bs.id) AS sold_seats "
                + "FROM dbo.SHOWTIMES st "
                + "JOIN dbo.HALLS h ON h.id = st.hall_id "
                + "LEFT JOIN dbo.BOOKINGS b ON b.showtime_id = st.id AND b.status IN " + SOLD_BOOKING_STATUSES + " "
                + "LEFT JOIN dbo.BOOKING_SEATS bs ON bs.booking_id = b.id "
                + "WHERE h.branch_id = ? AND st.start_time >= ? AND st.start_time < ? "
                + "GROUP BY h.id, h.name ORDER BY h.name";
        try (Connection conn = new DBContext().getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            bindRange(ps, branchId, from, toExclusive);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    rows.add(new ManagerHallOccupancy(rs.getString("hall_name"),
                            rs.getInt("showtime_count"), rs.getInt("total_capacity"),
                            rs.getInt("sold_seats")));
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return rows;
    }

    private void bindRange(PreparedStatement ps, int branchId, Timestamp from, Timestamp toExclusive) throws Exception {
        ps.setInt(1, branchId);
        ps.setTimestamp(2, from);
        ps.setTimestamp(3, toExclusive);
    }
}
