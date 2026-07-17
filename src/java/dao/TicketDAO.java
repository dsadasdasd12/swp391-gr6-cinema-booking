/*
 * Hệ thống Quản lý Rạp chiếu phim RapViet
 * Module: E-Ticket & QR Code —  (Long)
 */
package dao;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import model.Ticket;
import util.DBContext;
import util.EncodingUtil;

/**
 * DAO cho Vé xem phim. Toàn bộ dữ liệu vé được lưu và truy vấn từ bảng
 * dbo.BOOKINGS thay cho bảng TICKETS không tồn tại.
 *
 * @author LONG
 */
public class TicketDAO {

    // ── 5a. UPDATE BOOKING QR ──────────────────────────────────
    /**
     * Persists only the canonical scan token. QR images are generated at read
     * time and must never overwrite BOOKINGS.qr_code.
     */
    /**
     * Xác nhận booking sau thanh toán (PENDING → CONFIRMED).
     */
    public boolean confirmBooking(int bookingId) {
        String sql = "UPDATE dbo.BOOKINGS SET status = 'CONFIRMED', last_update = GETDATE() "
                + "WHERE id = ? AND status = 'PENDING'";
        Connection conn = DBContext.getInstance().getConnection();
        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, bookingId);
            return ps.executeUpdate() > 0;
        } catch (SQLException e) {
            System.getLogger(TicketDAO.class.getName())
                    .log(System.Logger.Level.ERROR, "confirmBooking thất bại", e);
        }
        return false;
    }

    public boolean updateBookingQR(int bookingId, String qrCodeToken) {
        if (qrCodeToken == null || !qrCodeToken.matches("RV-(ONLINE|WALK)-" + bookingId)) {
            return false;
        }
        String sql = "UPDATE dbo.BOOKINGS SET qr_code = ? WHERE id = ?";
        Connection conn = DBContext.getInstance().getConnection();
        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, qrCodeToken);
            ps.setInt(2, bookingId);
            return ps.executeUpdate() > 0;
        } catch (SQLException e) {
            System.getLogger(TicketDAO.class.getName())
                    .log(System.Logger.Level.ERROR, "updateBookingQR thất bại", e);
        }
        return false;
    }

    public boolean deleteTicket(int bookingId) {
        Connection conn = DBContext.getInstance().getConnection();
        boolean success = false;
        try {
            conn.setAutoCommit(false);
            
            String[] sqls = {
                "DELETE FROM dbo.ATTENDANCE WHERE booking_id = ?",
                "DELETE FROM dbo.REVIEWS WHERE booking_id = ?",
                "DELETE FROM dbo.BOOKING_FNB WHERE booking_id = ?",
                "DELETE FROM dbo.COUNTER_DISCOUNTS WHERE booking_id = ?",
                "DELETE FROM dbo.PAYMENTS WHERE booking_id = ?",
                "DELETE FROM dbo.BOOKING_SEATS WHERE booking_id = ?",
                "DELETE FROM dbo.BOOKINGS WHERE id = ?"
            };
            
            for (int i = 0; i < sqls.length; i++) {
                String sql = sqls[i];
                try (PreparedStatement ps = conn.prepareStatement(sql)) {
                    ps.setInt(1, bookingId);
                    ps.executeUpdate();
                } catch (Exception e) {
                    System.getLogger(TicketDAO.class.getName())
                        .log(System.Logger.Level.ERROR, "Error executing: " + sql, e);
                    if (i == sqls.length - 1) { // If deleting BOOKINGS fails, throw it to rollback!
                        throw e;
                    }
                }
            }
            
            conn.commit();
            success = true;
        } catch (SQLException e) {
            try {
                if (conn != null) conn.rollback();
            } catch (SQLException ex) { }
            System.getLogger(TicketDAO.class.getName())
                    .log(System.Logger.Level.ERROR, "deleteTicket error", e);
        } finally {
            try {
                if (conn != null) conn.setAutoCommit(true);
            } catch (SQLException ex) { }
        }
        return success;
    }

    // ── 5b. SELECT BY BOOKING ID ───────────────────────────────
    public double getBookingTotalPrice(int bookingId) {
        String sql = "SELECT total_price FROM dbo.BOOKINGS WHERE id = ?";
        Connection conn = DBContext.getInstance().getConnection();
        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, bookingId);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return rs.getDouble("total_price");
                }
            }
        } catch (SQLException e) {
            System.getLogger(TicketDAO.class.getName())
                    .log(System.Logger.Level.ERROR, "getBookingTotalPrice thất bại", e);
        }
        return 0;
    }

    public Ticket findByBookingId(int bookingId) {
        String sql = "SELECT b.id AS booking_id, b.user_id, b.status AS booking_status, "
                + "       b.qr_code, b.total_price, b.booked_at AS created_at, "
                + "       u.full_name AS customer_name, u.email AS customer_email, "
                + "       m.title AS movie_title, "
                + "       FORMAT(s.start_time, 'HH:mm dd/MM/yyyy') AS showtime_start "
                + "FROM dbo.BOOKINGS b "
                + "JOIN dbo.[USER]    u ON u.id = b.user_id "
                + "JOIN dbo.SHOWTIMES s ON s.id = b.showtime_id "
                + "JOIN dbo.MOVIES    m ON m.id = s.movie_id "
                + "WHERE b.id = ?";
        Connection conn = DBContext.getInstance().getConnection();
        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, bookingId);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return mapRow(rs);
                }
            }
        } catch (SQLException e) {
            System.getLogger(TicketDAO.class.getName())
                    .log(System.Logger.Level.ERROR, "findByBookingId thất bại", e);
        }
        return null;
    }

    // ── 5c. SELECT BY ID (FOR COMPATIBILITY) ────────────────────
    public Ticket findById(int bookingId) {
        return findByBookingId(bookingId);
    }

    // ── 5d. SELECT ALL (WITH QR CODES) ─────────────────────────
    public List<Ticket> findAll() {
        String sql = "SELECT b.id AS booking_id, b.user_id, b.status AS booking_status, "
                + "       b.qr_code, b.total_price, b.booked_at AS created_at, "
                + "       u.full_name AS customer_name, u.email AS customer_email, "
                + "       m.title AS movie_title, "
                + "       FORMAT(s.start_time, 'HH:mm dd/MM/yyyy') AS showtime_start "
                + "FROM dbo.BOOKINGS b "
                + "JOIN dbo.[USER]    u ON u.id = b.user_id "
                + "JOIN dbo.SHOWTIMES s ON s.id = b.showtime_id "
                + "JOIN dbo.MOVIES    m ON m.id = s.movie_id "
                + "WHERE b.status IN ('CONFIRMED','CHECKED_IN','USED','COMPLETED') "
                + "ORDER BY b.booked_at DESC";
        List<Ticket> list = new ArrayList<>();
        Connection conn = DBContext.getInstance().getConnection();
        try (PreparedStatement ps = conn.prepareStatement(sql); ResultSet rs = ps.executeQuery()) {
            while (rs.next()) {
                list.add(mapRow(rs));
            }
        } catch (SQLException e) {
            System.getLogger(TicketDAO.class.getName())
                    .log(System.Logger.Level.ERROR, "findAll tickets thất bại", e);
        }
        return list;
    }

    /**
     * Đếm tổng ticket theo keyword + status. statusFilter: "", "ISSUED",
     * "USED", "PENDING_MANUAL"
     */
    public long countTickets(String keyword, String statusFilter) {
        String kw = keyword == null ? "" : keyword.trim();
        String like = "%" + kw + "%";

        StringBuilder sql = new StringBuilder();
        sql.append("SELECT COUNT(*) ")
                .append("FROM dbo.BOOKINGS b ")
                .append("JOIN dbo.[USER]    u ON u.id = b.user_id ")
                .append("JOIN dbo.SHOWTIMES s ON s.id = b.showtime_id ")
                .append("JOIN dbo.MOVIES    m ON m.id = s.movie_id ")
                .append("WHERE b.status IN ('CONFIRMED','CHECKED_IN','USED','COMPLETED')");

        if (statusFilter != null && !statusFilter.isBlank()) {
            switch (statusFilter) {
                case "USED" ->
                    sql.append(" AND b.status IN ('USED','COMPLETED')");
                case "ISSUED" ->
                    sql.append(" AND b.status IN ('CONFIRMED','CHECKED_IN') AND b.qr_code IS NOT NULL");
                case "PENDING_MANUAL" ->
                    sql.append(" AND b.status IN ('CONFIRMED','CHECKED_IN') AND b.qr_code IS NULL");
                default -> {
                }
            }
        }

        if (!kw.isEmpty()) {
            sql.append(" AND (")
                    .append("u.full_name LIKE ? OR ")
                    .append("u.email LIKE ? OR ")
                    .append("m.title LIKE ? OR ")
                    .append("CAST(b.id AS NVARCHAR(50)) LIKE ?")
                    .append(")");
        }

        Connection conn = DBContext.getInstance().getConnection();
        try (PreparedStatement ps = conn.prepareStatement(sql.toString())) {
            if (!kw.isEmpty()) {
                ps.setString(1, like);
                ps.setString(2, like);
                ps.setString(3, like);
                ps.setString(4, like);
            }
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return rs.getLong(1);
                }
            }
        } catch (SQLException e) {
            System.getLogger(TicketDAO.class.getName())
                    .log(System.Logger.Level.ERROR, "countTickets thất bại", e);
        }
        return 0;
    }

    /**
     * Lấy trang ticket theo keyword + status.
     */
    public List<Ticket> findPaged(String keyword, String statusFilter, int offset, int pageSize) {
        String kw = keyword == null ? "" : keyword.trim();
        String like = "%" + kw + "%";

        StringBuilder sql = new StringBuilder();
        sql.append("SELECT b.id AS booking_id, b.user_id, b.status AS booking_status, ")
                .append("       b.qr_code, b.total_price, b.booked_at AS created_at, ")
                .append("       u.full_name AS customer_name, u.email AS customer_email, ")
                .append("       m.title AS movie_title, ")
                .append("       FORMAT(s.start_time, 'HH:mm dd/MM/yyyy') AS showtime_start ")
                .append("FROM dbo.BOOKINGS b ")
                .append("JOIN dbo.[USER]    u ON u.id = b.user_id ")
                .append("JOIN dbo.SHOWTIMES s ON s.id = b.showtime_id ")
                .append("JOIN dbo.MOVIES    m ON m.id = s.movie_id ")
                .append("WHERE b.status IN ('CONFIRMED','CHECKED_IN','USED','COMPLETED')");

        if (statusFilter != null && !statusFilter.isBlank()) {
            switch (statusFilter) {
                case "USED" ->
                    sql.append(" AND b.status IN ('USED','COMPLETED')");
                case "ISSUED" ->
                    sql.append(" AND b.status IN ('CONFIRMED','CHECKED_IN') AND b.qr_code IS NOT NULL");
                case "PENDING_MANUAL" ->
                    sql.append(" AND b.status IN ('CONFIRMED','CHECKED_IN') AND b.qr_code IS NULL");
                default -> {
                }
            }
        }

        if (!kw.isEmpty()) {
            sql.append(" AND (")
                    .append("u.full_name LIKE ? OR ")
                    .append("u.email LIKE ? OR ")
                    .append("m.title LIKE ? OR ")
                    .append("CAST(b.id AS NVARCHAR(50)) LIKE ?")
                    .append(")");
        }

        sql.append(" ORDER BY b.booked_at DESC ")
                .append(" OFFSET ? ROWS FETCH NEXT ? ROWS ONLY");

        List<Ticket> list = new ArrayList<>();
        Connection conn = DBContext.getInstance().getConnection();
        try (PreparedStatement ps = conn.prepareStatement(sql.toString())) {
            int idx = 1;
            if (!kw.isEmpty()) {
                ps.setString(idx++, like);
                ps.setString(idx++, like);
                ps.setString(idx++, like);
                ps.setString(idx++, like);
            }
            ps.setInt(idx++, Math.max(0, offset));
            ps.setInt(idx, pageSize);

            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    list.add(mapRow(rs));
                }
            }
        } catch (SQLException e) {
            System.getLogger(TicketDAO.class.getName())
                    .log(System.Logger.Level.ERROR, "findPaged thất bại", e);
        }
        return list;
    }

    // ── 5e. MARK TICKET AS USED ────────────────────────────────
    /**
     * Chuyển trạng thái booking thành COMPLETED khi vé được quét.
     */
    public boolean markUsed(int bookingId) {
        String sql = "UPDATE dbo.BOOKINGS SET status='COMPLETED' WHERE id=?";
        Connection conn = DBContext.getInstance().getConnection();
        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, bookingId);
            return ps.executeUpdate() > 0;
        } catch (SQLException e) {
            System.getLogger(TicketDAO.class.getName())
                    .log(System.Logger.Level.ERROR, "markUsed thất bại", e);
        }
        return false;
    }

    // ── 5f. UPDATE QR CODE ─────────────────────────────────────
    public boolean updateQrCode(int bookingId, String qrCodeToken) {
        return updateBookingQR(bookingId, qrCodeToken);
    }

    // ── Helper Mapping ────────────────────────────────────────
    private Ticket mapRow(ResultSet rs) throws SQLException {
        Ticket t = new Ticket();
        int bookingId = rs.getInt("booking_id");
        t.setBookingId(bookingId);
        t.setId(bookingId);

        // Generate consistent deterministic UUID based on booking ID
        java.util.UUID uuid = java.util.UUID.nameUUIDFromBytes(String.valueOf(bookingId).getBytes());
        t.setTicketUuid(uuid.toString());

        String bStatus = rs.getString("booking_status");
        t.setBookingStatus(bStatus);

        // Trạng thái vé map tương ứng
        if ("COMPLETED".equals(bStatus) || "USED".equals(bStatus)) {
            t.setUsed(true);
            t.setTicketStatus("USED");
        } else {
            t.setUsed(false);
            t.setTicketStatus(rs.getString("qr_code") == null ? "PENDING_MANUAL" : "ISSUED");
        }

        t.setQrCode(rs.getString("qr_code"));
        t.setQrCodeBase64(null);
        t.setCustomerName(EncodingUtil.getString(rs, "customer_name"));
        t.setCustomerEmail(rs.getString("customer_email"));
        t.setMovieTitle(EncodingUtil.getString(rs, "movie_title"));
        t.setShowtimeStart(rs.getString("showtime_start"));
        t.setCreatedAt(rs.getObject("created_at", LocalDateTime.class));
        t.setLastUpdate(rs.getObject("created_at", LocalDateTime.class));
        return t;
    }
}
