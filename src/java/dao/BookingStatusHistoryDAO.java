package dao;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.List;
import model.BookingStatusHistory;
import util.DBContext;

/**
 * Read-only access for the booking-status audit timeline.
 */
public class BookingStatusHistoryDAO {

    public List<BookingStatusHistory> findByBookingId(int bookingId) {
        String sql = "SELECT id, booking_id, previous_status, new_status, changed_at, note "
                + "FROM dbo.BOOKING_STATUS_HISTORY WHERE booking_id = ? ORDER BY changed_at ASC, id ASC";
        List<BookingStatusHistory> history = new ArrayList<>();
        Connection conn = DBContext.getInstance().getConnection();
        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, bookingId);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    BookingStatusHistory item = new BookingStatusHistory();
                    item.setId(rs.getInt("id"));
                    item.setBookingId(rs.getInt("booking_id"));
                    item.setPreviousStatus(rs.getString("previous_status"));
                    item.setNewStatus(rs.getString("new_status"));
                    Timestamp changedAt = rs.getTimestamp("changed_at");
                    item.setChangedAt(changedAt == null ? null : changedAt.toLocalDateTime());
                    item.setNote(rs.getString("note"));
                    history.add(item);
                }
            }
        } catch (SQLException e) {
            System.getLogger(BookingStatusHistoryDAO.class.getName())
                    .log(System.Logger.Level.ERROR, "Unable to load booking status history", e);
        }
        return history;
    }
}
