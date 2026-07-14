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
 * DAO cho Vé xem phim.
 * Toàn bộ dữ liệu vé được lưu và truy vấn từ bảng dbo.BOOKINGS thay cho bảng TICKETS không tồn tại.
 *
 * @author LONG
 */
public class TicketDAO {

    // ── 5a. UPDATE BOOKING QR ──────────────────────────────────

    /**
     * Cập nhật chuỗi QR Base64 trực tiếp vào bảng BOOKINGS.
     */
    /** Xác nhận booking sau thanh toán (PENDING → CONFIRMED). */
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

    public boolean updateBookingQR(int bookingId, String qrCodeBase64) {
        String sql = "UPDATE dbo.BOOKINGS SET qr_code = ? WHERE id = ?";
        Connection conn = DBContext.getInstance().getConnection();
        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, qrCodeBase64);
            ps.setInt   (2, bookingId);
            return ps.executeUpdate() > 0;
        } catch (SQLException e) {
            System.getLogger(TicketDAO.class.getName())
                    .log(System.Logger.Level.ERROR, "updateBookingQR thất bại", e);
        }
        return false;
    }

    // ── 5a-2. FIND BOOKINGS WITHOUT QR ────────────────────────

    /**
     * Tìm tất cả booking đã xác nhận (CONFIRMED/COMPLETED) nhưng chưa có QR code.
     */
    public List<Integer> findBookingIdsWithoutQR() {
        String sql = "SELECT id FROM dbo.BOOKINGS "
                   + "WHERE (qr_code IS NULL OR qr_code LIKE 'DEMO%') "
                   + "AND status IN ('CONFIRMED','CHECKED_IN','COMPLETED','PENDING')";
        List<Integer> ids = new ArrayList<>();
        Connection conn = DBContext.getInstance().getConnection();
        try (PreparedStatement ps = conn.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {
            while (rs.next()) {
                ids.add(rs.getInt("id"));
            }
        } catch (SQLException e) {
            System.getLogger(TicketDAO.class.getName())
                    .log(System.Logger.Level.ERROR, "findBookingIdsWithoutQR thất bại", e);
        }
        return ids;
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
        try (PreparedStatement ps = conn.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {
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
     * Đếm tổng ticket theo keyword + status.
     * statusFilter: "", "ISSUED", "USED", "PENDING_MANUAL"
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
                case "USED" -> sql.append(" AND b.status IN ('USED','COMPLETED')");
                case "ISSUED" -> sql.append(" AND b.status IN ('CONFIRMED','CHECKED_IN') AND b.qr_code IS NOT NULL");
                case "PENDING_MANUAL" -> sql.append(" AND b.status IN ('CONFIRMED','CHECKED_IN') AND b.qr_code IS NULL");
                default -> {}
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
                case "USED" -> sql.append(" AND b.status IN ('USED','COMPLETED')");
                case "ISSUED" -> sql.append(" AND b.status IN ('CONFIRMED','CHECKED_IN') AND b.qr_code IS NOT NULL");
                case "PENDING_MANUAL" -> sql.append(" AND b.status IN ('CONFIRMED','CHECKED_IN') AND b.qr_code IS NULL");
                default -> {}
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

    public boolean updateQrCode(int bookingId, String qrBase64) {
        return updateBookingQR(bookingId, qrBase64);
    }

    // ── Helper Mapping ────────────────────────────────────────

    private Ticket mapRow(ResultSet rs) throws SQLException {
        Ticket t = new Ticket();
        int bookingId = rs.getInt("booking_id");
        t.setBookingId       (bookingId);
        t.setId              (bookingId);
        
        // Generate consistent deterministic UUID based on booking ID
        java.util.UUID uuid = java.util.UUID.nameUUIDFromBytes(String.valueOf(bookingId).getBytes());
        t.setTicketUuid      (uuid.toString());

        String bStatus = rs.getString("booking_status");
        t.setBookingStatus   (bStatus);
        
        String qrCode = rs.getString("qr_code");
        if ("COMPLETED".equals(bStatus) || "USED".equals(bStatus)) {
            t.setUsed(true);
            t.setTicketStatus("USED");
        } else {
            t.setUsed(false);
            t.setTicketStatus(qrCode == null || qrCode.startsWith("DEMO") ? "PENDING_MANUAL" : "ISSUED");
        }
        
        t.setQrCodeBase64    (rs.getString("qr_code"));
        t.setQrCode          (rs.getString("qr_code"));
        t.setCustomerName    (EncodingUtil.getString(rs, "customer_name"));
        t.setCustomerEmail   (rs.getString("customer_email"));
        t.setMovieTitle      (EncodingUtil.getString(rs, "movie_title"));
        t.setShowtimeStart   (rs.getString("showtime_start"));
        t.setCreatedAt       (rs.getObject("created_at", LocalDateTime.class));
        t.setLastUpdate      (rs.getObject("created_at", LocalDateTime.class));
        return t;
    }
}
