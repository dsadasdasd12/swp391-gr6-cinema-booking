/*
 * Hệ thống Quản lý Rạp chiếu phim RapViet
 * Module: Reporting & Analytics —  (Long)
 */
package dao;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import util.DBContext;
import util.EncodingUtil;

/**
 * DAO thực hiện các truy vấn báo cáo & phân tích số liệu động. Kết quả trả về
 * dạng danh sách các hàng (Map<String, Object>) để ReportService đóng gói vào
 * ReportDTO.
 *
 * @author LONG
 */
public class ReportDAO {

    // ── 1. DOANH THU THEO NGÀY ────────────────────────────────
    /**
     * Báo cáo doanh thu theo ngày trong khoảng thời gian.
     */
    public List<Map<String, Object>> getRevenueByDate(String fromDate, String toDate) {
        String sql = "SELECT "
                + "    CAST(b.booked_at AS DATE) AS report_date, "
                + "    COUNT(b.id) AS booking_count, "
                + "    SUM((SELECT COUNT(*) FROM dbo.BOOKING_SEATS bs WHERE bs.booking_id = b.id)) AS ticket_count, "
                + "    SUM(b.total_price) AS revenue "
                + "FROM dbo.BOOKINGS b "
                + "WHERE b.status IN ('CONFIRMED', 'CHECKED_IN', 'USED', 'COMPLETED') "
                + "  AND b.booked_at >= ? AND b.booked_at <= ? "
                + "GROUP BY CAST(b.booked_at AS DATE) "
                + "ORDER BY report_date ASC";
        return executeQuery(sql, fromDate + " 00:00:00", toDate + " 23:59:59");
    }

    // ── 2. DOANH SỐ THEO CHI NHÁNH ─────────────────────────────
    /**
     * Báo cáo doanh số và số lượng vé bán ra theo từng chi nhánh.
     */
    public List<Map<String, Object>> getSalesByBranch(String fromDate, String toDate) {
        String sql = "SELECT "
                + "    br.id AS branch_id, "
                + "    br.name() AS branch_name, "
                + "    COUNT(b.id) AS booking_count, "
                + "    SUM((SELECT COUNT(*) FROM dbo.BOOKING_SEATS bs WHERE bs.booking_id = b.id)) AS ticket_count, "
                + "    SUM(b.total_price) AS revenue "
                + "FROM dbo.BRANCHES br "
                + "JOIN dbo.HALLS h ON br.id = h.branch_id "
                + "JOIN dbo.SHOWTIMES s ON h.id = s.hall_id "
                + "JOIN dbo.BOOKINGS b ON s.id = b.showtime_id "
                + "WHERE b.status IN ('CONFIRMED', 'CHECKED_IN', 'USED', 'COMPLETED') "
                + "  AND b.booked_at >= ? AND b.booked_at <= ? "
                + "GROUP BY br.id, br.name() "
                + "ORDER BY revenue DESC";
        return executeQuery(sql, fromDate + " 00:00:00", toDate + " 23:59:59");
    }

    // ── 3. TỈ LỆ LẤP ĐẦY PHÒNG CHIẾU ───────────────────────────
    /**
     * Thống kê tỉ lệ lấp đầy theo phòng chiếu (HALLS) dựa trên số ghế đã bán
     * trong SHOWTIMES. Sử dụng total_seats từ dbo.HALLS theo yêu cầu hệ thống.
     */
    public List<Map<String, Object>> getOccupancyRate() {
        String sql = "SELECT "
                + "    r.branch_name, "
                + "    r.hall_name, "
                + "    r.hall_seat_capacity, "
                + "    r.showtime_count, "
                + "    r.total_capacity, "
                + "    r.booked_seats, "
                + "    CASE WHEN r.total_capacity > 0 "
                + "         THEN CAST((r.booked_seats * 100.0) / r.total_capacity AS DECIMAL(5,2)) "
                + "         ELSE 0.0 "
                + "    END AS occupancy_rate "
                + "FROM ( "
                + "    SELECT "
                + "        br.name() AS branch_name, "
                + "        h.name() AS hall_name, "
                + "        h.total_seats AS hall_seat_capacity, "
                + "        COUNT(s.id) AS showtime_count, "
                + "        ISNULL(SUM(h.total_seats), 0) AS total_capacity, "
                + "        ISNULL(SUM(bs_count.cnt), 0) AS booked_seats "
                + "    FROM dbo.HALLS h "
                + "    JOIN dbo.BRANCHES br ON h.branch_id = br.id "
                + "    LEFT JOIN dbo.SHOWTIMES s ON h.id = s.hall_id AND s.status != 'CANCELLED' "
                + "    OUTER APPLY ( "
                + "        SELECT COUNT(bs.id) AS cnt "
                + "        FROM dbo.BOOKINGS b "
                + "        JOIN dbo.BOOKING_SEATS bs ON b.id = bs.booking_id "
                + "        WHERE b.showtime_id = s.id AND b.status IN ('CONFIRMED', 'CHECKED_IN', 'USED', 'COMPLETED') "
                + "    ) bs_count "
                + "    GROUP BY br.name(), h.id, h.name(), h.total_seats "
                + ") r "
                + "ORDER BY r.branch_name ASC, occupancy_rate DESC";
        return executeQuery(sql);
    }

    // ── 4. PHIM ĂN KHÁCH NHẤT ──────────────────────────────────
    /**
     * Báo cáo các phim ăn khách nhất theo số vé bán ra và doanh thu.
     */
    public List<Map<String, Object>> getPopularMovies(String fromDate, String toDate) {
        String sql = "SELECT "
                + "    m.id AS movie_id, "
                + "    m.title AS movie_title, "
                + "    m.poster_url AS poster_url, "
                + "    COUNT(b.id) AS booking_count, "
                + "    SUM((SELECT COUNT(*) FROM dbo.BOOKING_SEATS bs WHERE bs.booking_id = b.id)) AS ticket_count, "
                + "    SUM(b.total_price) AS revenue "
                + "FROM dbo.MOVIES m "
                + "JOIN dbo.SHOWTIMES s ON m.id = s.movie_id "
                + "JOIN dbo.BOOKINGS b ON s.id = b.showtime_id "
                + "WHERE b.status IN ('CONFIRMED', 'CHECKED_IN', 'USED', 'COMPLETED') "
                + "  AND b.booked_at >= ? AND b.booked_at <= ? "
                + "GROUP BY m.id, m.title, m.poster_url "
                + "ORDER BY ticket_count DESC, revenue DESC";
        return executeQuery(sql, fromDate + " 00:00:00", toDate + " 23:59:59");
    }

    // ── 5. HOẠT ĐỘNG KHÁCH HÀNG ────────────────────────────────
    /**
     * Báo cáo hoạt động khách hàng: số lần đặt, vé mua, tổng chi tiêu.
     */
    public List<Map<String, Object>> getCustomerActivity(String fromDate, String toDate) {
        String sql = "SELECT "
                + "    u.id AS customer_id, "
                + "    u.full_name AS customer_name, "
                + "    u.email AS customer_email, "
                + "    COUNT(DISTINCT b.id) AS booking_count, "
                + "    SUM((SELECT COUNT(*) FROM dbo.BOOKING_SEATS bs WHERE bs.booking_id = b.id)) AS ticket_count, "
                + "    SUM(b.total_price) AS total_spent, "
                + "    MAX(b.booked_at) AS last_booking_at "
                + "FROM dbo.[USER] u "
                + "JOIN dbo.BOOKINGS b ON b.user_id = u.id "
                + "WHERE u.role = 'CUSTOMER' "
                + "  AND b.status IN ('CONFIRMED', 'CHECKED_IN', 'USED') "
                + "  AND b.booked_at >= ? AND b.booked_at <= ? "
                + "GROUP BY u.id, u.full_name, u.email "
                + "ORDER BY total_spent DESC, ticket_count DESC";
        return executeQuery(sql, fromDate + " 00:00:00", toDate + " 23:59:59");
    }

    // ── 6. GIỜ CAO ĐIỂM (PEAK HOURS) ───────────────────────────
    /**
     * Báo cáo khung giờ đặt vé cao điểm trong ngày.
     */
    public List<Map<String, Object>> getPeakHours(String fromDate, String toDate) {
        String sql = "SELECT "
                + "    DATEPART(HOUR, b.booked_at) AS booking_hour, "
                + "    COUNT(b.id) AS booking_count, "
                + "    SUM((SELECT COUNT(*) FROM dbo.BOOKING_SEATS bs WHERE bs.booking_id = b.id)) AS ticket_count, "
                + "    SUM(b.total_price) AS revenue "
                + "FROM dbo.BOOKINGS b "
                + "WHERE b.status IN ('CONFIRMED', 'CHECKED_IN', 'USED', 'COMPLETED') "
                + "  AND b.booked_at >= ? AND b.booked_at <= ? "
                + "GROUP BY DATEPART(HOUR, b.booked_at) "
                + "ORDER BY booking_hour ASC";
        return executeQuery(sql, fromDate + " 00:00:00", toDate + " 23:59:59");
    }

    // ── Nền tảng thực thi JDBC động ────────────────────────────
    private List<Map<String, Object>> executeQuery(String sql, Object... params) {
        List<Map<String, Object>> list = new ArrayList<>();
        Connection conn = DBContext.getInstance().getConnection();
        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            for (int i = 0; i < params.length; i++) {
                ps.setObject(i + 1, params[i]);
            }
            try (ResultSet rs = ps.executeQuery()) {
                ResultSetMetaData meta = rs.getMetaData();
                int colCount = meta.getColumnCount();
                while (rs.next()) {
                    Map<String, Object> row = new HashMap<>();
                    for (int i = 1; i <= colCount; i++) {
                        String colName = meta.getColumnLabel(i);
                        Object val = rs.getObject(i);
                        if (val instanceof String s) {
                            val = EncodingUtil.fix(s);
                        }
                        row.put(colName, val);
                    }
                    list.add(row);
                }
            }
        } catch (SQLException e) {
            System.getLogger(ReportDAO.class.getName())
                    .log(System.Logger.Level.ERROR, "Lỗi truy vấn báo cáo SQL: " + sql, e);
        }
        return list;
    }
}
