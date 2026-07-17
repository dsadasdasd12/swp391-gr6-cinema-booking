/*
 * Hệ thống Quản lý Rạp chiếu phim RapViet
 * Module: Notification Management —  (Long)
 */
package dao;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Types;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import model.NotificationLog;
import util.DBContext;
import util.EncodingUtil;

/**
 * DAO cho bảng dbo.NOTIFICATIONS (schema RapViet_v3.sql).
 */
public class NotificationLogDAO {

    private static final int SYSTEM_SENDER_ID = 1;

    public boolean insert(NotificationLog log) {
        String sql = "INSERT INTO dbo.NOTIFICATIONS "
                + "(user_id, branch_id, sent_by, type, title, message, status) "
                + "VALUES (?, ?, ?, ?, ?, ?, ?)";
        Connection conn = DBContext.getInstance().getConnection();
        if (conn == null) {
            return false;
        }
        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            if (log.getUserId() != null) {
                ps.setInt(1, log.getUserId());
            } else {
                ps.setNull(1, Types.INTEGER);
            }
            ps.setNull(2, Types.INTEGER);
            ps.setInt(3, SYSTEM_SENDER_ID);
            ps.setString(4, log.getType());
            ps.setString(5, log.getSubject() != null ? log.getSubject() : log.getTypeLabel());
            ps.setString(6, buildMessageBody(log));
            ps.setString(7, mapStatusToDb(log.getStatus()));
            return ps.executeUpdate() > 0;
        } catch (SQLException e) {
            System.getLogger(NotificationLogDAO.class.getName())
                    .log(System.Logger.Level.ERROR, "insert notification thất bại", e);
        }
        return false;
    }

    public List<NotificationLog> findAll() {
        String sql = "SELECT n.id, n.user_id, n.type, n.title, n.message, n.status, n.sent_at, "
                + "       u.email AS recipient_email "
                + "FROM dbo.NOTIFICATIONS n "
                + "LEFT JOIN dbo.[USER] u ON u.id = n.user_id "
                + "ORDER BY n.sent_at DESC";
        return queryList(sql, null);
    }

    /**
     * Đếm tổng log (đã loại PROMOTION). type: BOOKING_CONFIRM | PAYMENT_CONFIRM
     * | SYSTEM | "" status: SENT | FAILED | PENDING | ""
     */
    public long countLogs(String keyword, String type, String status) {
        String kw = keyword == null ? "" : keyword.trim();
        String like = "%" + kw + "%";

        StringBuilder sql = new StringBuilder();
        sql.append("SELECT COUNT(*) ")
                .append("FROM dbo.NOTIFICATIONS n ")
                .append("LEFT JOIN dbo.[USER] u ON u.id = n.user_id ")
                .append("WHERE n.type <> 'PROMOTION'");

        if (type != null && !type.isBlank()) {
            sql.append(" AND n.type = ?");
        }
        if (status != null && !status.isBlank()) {
            switch (status) {
                case "SENT" ->
                    sql.append(" AND n.status = 'SENT'");
                case "FAILED" ->
                    sql.append(" AND n.status = 'FAILED'");
                case "PENDING" ->
                    sql.append(" AND (n.status IS NULL OR n.status = 'DRAFT')");
                default -> {
                }
            }
        }
        if (!kw.isEmpty()) {
            sql.append(" AND (u.email LIKE ? OR n.title LIKE ?)");
        }

        Connection conn = DBContext.getInstance().getConnection();
        try (PreparedStatement ps = conn.prepareStatement(sql.toString())) {
            int idx = 1;
            if (type != null && !type.isBlank()) {
                ps.setString(idx++, type.trim());
            }
            if (!kw.isEmpty()) {
                ps.setString(idx++, like);
                ps.setString(idx++, like);
            }
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return rs.getLong(1);
                }
            }
        } catch (SQLException e) {
            System.getLogger(NotificationLogDAO.class.getName())
                    .log(System.Logger.Level.ERROR, "countLogs thất bại", e);
        }
        return 0;
    }

    /**
     * Lấy trang log (đã loại PROMOTION).
     */
    public List<NotificationLog> findPaged(String keyword, String type, String status,
            int offset, int pageSize) {
        String kw = keyword == null ? "" : keyword.trim();
        String like = "%" + kw + "%";

        StringBuilder sql = new StringBuilder();
        sql.append("SELECT n.id, n.user_id, n.type, n.title, n.message, n.status, n.sent_at, ")
                .append("       u.email AS recipient_email ")
                .append("FROM dbo.NOTIFICATIONS n ")
                .append("LEFT JOIN dbo.[USER] u ON u.id = n.user_id ")
                .append("WHERE n.type <> 'PROMOTION'");

        if (type != null && !type.isBlank()) {
            sql.append(" AND n.type = ?");
        }
        if (status != null && !status.isBlank()) {
            switch (status) {
                case "SENT" ->
                    sql.append(" AND n.status = 'SENT'");
                case "FAILED" ->
                    sql.append(" AND n.status = 'FAILED'");
                case "PENDING" ->
                    sql.append(" AND (n.status IS NULL OR n.status = 'DRAFT')");
                default -> {
                }
            }
        }
        if (!kw.isEmpty()) {
            sql.append(" AND (u.email LIKE ? OR n.title LIKE ?)");
        }

        sql.append(" ORDER BY n.sent_at DESC ")
                .append(" OFFSET ? ROWS FETCH NEXT ? ROWS ONLY");

        List<NotificationLog> list = new ArrayList<>();
        Connection conn = DBContext.getInstance().getConnection();
        try (PreparedStatement ps = conn.prepareStatement(sql.toString())) {
            int idx = 1;
            if (type != null && !type.isBlank()) {
                ps.setString(idx++, type.trim());
            }
            if (!kw.isEmpty()) {
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
            System.getLogger(NotificationLogDAO.class.getName())
                    .log(System.Logger.Level.ERROR, "findPaged thất bại", e);
        }
        return list;
    }

    public List<NotificationLog> findByBookingId(int bookingId) {
        String sql = "SELECT n.id, n.user_id, n.type, n.title, n.message, n.status, n.sent_at, "
                + "       u.email AS recipient_email "
                + "FROM dbo.NOTIFICATIONS n "
                + "LEFT JOIN dbo.[USER] u ON u.id = n.user_id "
                + "WHERE n.title LIKE ? "
                + "ORDER BY n.sent_at DESC";
        List<NotificationLog> list = new ArrayList<>();
        Connection conn = DBContext.getInstance().getConnection();
        if (conn == null) {
            return list;
        }
        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, bookingTitlePrefix(bookingId) + "%");
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    list.add(mapRow(rs));
                }
            }
        } catch (SQLException e) {
            System.getLogger(NotificationLogDAO.class.getName())
                    .log(System.Logger.Level.ERROR, "findByBookingId notifications thất bại", e);
        }
        return list;
    }

    public boolean updateStatus(int id, String status, int retryCount) {
        String sql = "UPDATE dbo.NOTIFICATIONS "
                + "SET status = ?, message = COALESCE(message, '') + ?, sent_at = GETDATE() "
                + "WHERE id = ?";
        Connection conn = DBContext.getInstance().getConnection();
        if (conn == null) {
            return false;
        }
        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, mapStatusToDb(status));
            ps.setString(2, " [retry=" + retryCount + "]");
            ps.setInt(3, id);
            return ps.executeUpdate() > 0;
        } catch (SQLException e) {
            System.getLogger(NotificationLogDAO.class.getName())
                    .log(System.Logger.Level.ERROR, "updateStatus notification thất bại", e);
        }
        return false;
    }

    private List<NotificationLog> queryList(String sql, Integer bookingId) {
        List<NotificationLog> list = new ArrayList<>();
        Connection conn = DBContext.getInstance().getConnection();
        if (conn == null) {
            return list;
        }
        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            if (bookingId != null) {
                ps.setString(1, bookingTitlePrefix(bookingId) + "%");
            }
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    list.add(mapRow(rs));
                }
            }
        } catch (SQLException e) {
            System.getLogger(NotificationLogDAO.class.getName())
                    .log(System.Logger.Level.ERROR, "query notifications thất bại", e);
        }
        return list;
    }

    private NotificationLog mapRow(ResultSet rs) throws SQLException {
        NotificationLog log = new NotificationLog();
        log.setNotificationId(rs.getInt("id"));
        int uId = rs.getInt("user_id");
        log.setUserId(rs.wasNull() ? null : uId);
        log.setType(rs.getString("type"));
        log.setSubject(EncodingUtil.getString(rs, "title"));
        log.setRecipientEmail(rs.getString("recipient_email"));
        log.setStatus(mapStatusFromDb(rs.getString("status")));
        log.setSentAt(rs.getObject("sent_at", LocalDateTime.class));
        log.setCreatedAt(log.getSentAt());

        String message = EncodingUtil.getString(rs, "message");
        log.setErrorMessage(extractErrorMessage(message));
        log.setRetryCount(extractRetryCount(message));

        Integer bookingId = extractBookingIdFromTitle(log.getSubject());
        log.setBookingId(bookingId);
        return log;
    }

    private String buildMessageBody(NotificationLog log) {
        StringBuilder sb = new StringBuilder();
        if (log.getBookingId() != null) {
            sb.append("[booking_id=").append(log.getBookingId()).append("] ");
        }
        if (log.getErrorMessage() != null && !log.getErrorMessage().isBlank()) {
            sb.append(log.getErrorMessage());
        } else if (log.getRecipientEmail() != null) {
            sb.append("Gửi tới: ").append(log.getRecipientEmail());
        }
        if (log.getRetryCount() > 0) {
            sb.append(" [retry=").append(log.getRetryCount()).append("]");
        }
        return sb.toString().trim();
    }

    private String mapStatusToDb(String status) {
        if (status == null) {
            return "DRAFT";
        }
        return switch (status) {
            case "SENT" ->
                "SENT";
            case "FAILED" ->
                "FAILED";
            default ->
                "DRAFT";
        };
    }

    private String mapStatusFromDb(String status) {
        if (status == null) {
            return "PENDING";
        }
        return switch (status) {
            case "SENT" ->
                "SENT";
            case "FAILED" ->
                "FAILED";
            default ->
                "PENDING";
        };
    }

    public static String bookingTitlePrefix(int bookingId) {
        return "Booking #" + bookingId;
    }

    private Integer extractBookingIdFromTitle(String title) {
        if (title == null || !title.startsWith("Booking #")) {
            return null;
        }
        int end = title.indexOf(' ', 9);
        String num = end > 9 ? title.substring(9, end) : title.substring(9);
        try {
            return Integer.parseInt(num.trim());
        } catch (NumberFormatException e) {
            return null;
        }
    }

    private String extractErrorMessage(String message) {
        if (message == null) {
            return null;
        }
        if (message.startsWith("[booking_id=")) {
            int close = message.indexOf(']');
            if (close > 0 && close + 1 < message.length()) {
                String rest = message.substring(close + 1).trim();
                return rest.isEmpty() ? null : rest.replaceAll("\\s*\\[retry=\\d+\\]\\s*$", "").trim();
            }
        }
        return message.replaceAll("\\s*\\[retry=\\d+\\]\\s*$", "").trim();
    }

    private int extractRetryCount(String message) {
        if (message == null) {
            return 0;
        }
        int idx = message.lastIndexOf("[retry=");
        if (idx < 0) {
            return 0;
        }
        int end = message.indexOf(']', idx);
        if (end < 0) {
            return 0;
        }
        try {
            return Integer.parseInt(message.substring(idx + 7, end));
        } catch (NumberFormatException e) {
            return 0;
        }
    }

    public boolean delete(int id) {
        String sql = "DELETE FROM dbo.NOTIFICATIONS WHERE id = ?";
        Connection conn = DBContext.getInstance().getConnection();
        if (conn == null) return false;
        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, id);
            return ps.executeUpdate() > 0;
        } catch (SQLException e) {
            System.getLogger(NotificationLogDAO.class.getName())
                    .log(System.Logger.Level.ERROR, "delete notification thất bại", e);
        }
        return false;
    }
}
