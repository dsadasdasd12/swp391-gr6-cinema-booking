package dao;

import dto.PaymentView;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.Statement;
import util.DBContext;

public class PaymentDAO {

    public boolean createPendingPayment(int bookingId, double amount) {
        String sql =
                "INSERT INTO dbo.PAYMENTS " +
                "(booking_id, type, method, transaction_id, status, amount, paid_at, gateway, last_update) " +
                "VALUES (?, 'BOOKING', 'SEPAY', ?, 'PENDING', ?, NULL, 'SEPAY', GETDATE())";

        String transactionId = "RV" + bookingId;

        try (Connection conn = DBContext.getInstance().getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setInt(1, bookingId);
            ps.setString(2, transactionId);
            ps.setDouble(3, amount);

            return ps.executeUpdate() > 0;

        } catch (Exception e) {
            e.printStackTrace();
        }

        return false;
    }

    public PaymentView findByBookingId(int bookingId) {
        String sql =
                "SELECT TOP 1 " +
                "p.id, p.booking_id, p.type, p.method, p.transaction_id, " +
                "p.status, p.amount, p.paid_at, p.gateway, " +
                "b.status AS booking_status, " +
                "m.title AS movie_title, " +
                "CONVERT(varchar(10), st.start_time, 103) AS show_date, " +
                "CONVERT(varchar(5), st.start_time, 108) AS show_time, " +
                "br.name AS branch_name, h.name AS hall_name " +
                "FROM dbo.PAYMENTS p " +
                "JOIN dbo.BOOKINGS b ON b.id = p.booking_id " +
                "JOIN dbo.SHOWTIMES st ON st.id = b.showtime_id " +
                "JOIN dbo.MOVIES m ON m.id = st.movie_id " +
                "JOIN dbo.HALLS h ON h.id = st.hall_id " +
                "JOIN dbo.BRANCHES br ON br.id = h.branch_id " +
                "WHERE p.booking_id = ? " +
                "ORDER BY p.id DESC";

        try (Connection conn = DBContext.getInstance().getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setInt(1, bookingId);

            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    PaymentView v = new PaymentView();

                    v.setId(rs.getInt("id"));
                    v.setBookingId(rs.getInt("booking_id"));
                    v.setType(rs.getString("type"));
                    v.setMethod(rs.getString("method"));
                    v.setTransactionId(rs.getString("transaction_id"));
                    v.setStatus(rs.getString("status"));
                    v.setAmount(rs.getDouble("amount"));
                    v.setPaidAt(rs.getString("paid_at"));
                    v.setGateway(rs.getString("gateway"));

                    v.setBookingStatus(rs.getString("booking_status"));
                    v.setMovieTitle(rs.getString("movie_title"));
                    v.setShowDate(rs.getString("show_date"));
                    v.setShowTime(rs.getString("show_time"));
                    v.setBranchName(rs.getString("branch_name"));
                    v.setHallName(rs.getString("hall_name"));

                    return v;
                }
            }

        } catch (Exception e) {
            e.printStackTrace();
        }

        return null;
    }

    public boolean confirmPaymentByBookingId(
            int bookingId,
            String transactionId,
            double amount,
            String gateway) {

        String updatePayment =
                "UPDATE dbo.PAYMENTS " +
                "SET status = 'PAID', transaction_id = ?, amount = ?, " +
                "paid_at = GETDATE(), gateway = ?, last_update = GETDATE() " +
                "WHERE booking_id = ? AND status = 'PENDING'";

        String updateBooking =
                "UPDATE dbo.BOOKINGS " +
                "SET status = 'CONFIRMED', last_update = GETDATE() " +
                "WHERE id = ?";

        Connection conn = null;

        try {
            conn = DBContext.getInstance().getConnection();
            conn.setAutoCommit(false);

            try (PreparedStatement ps = conn.prepareStatement(updatePayment)) {
                ps.setString(1, transactionId);
                ps.setDouble(2, amount);
                ps.setString(3, gateway);
                ps.setInt(4, bookingId);

                int affected = ps.executeUpdate();

                if (affected == 0) {
                    conn.rollback();
                    return false;
                }
            }

            try (PreparedStatement ps = conn.prepareStatement(updateBooking)) {
                ps.setInt(1, bookingId);
                ps.executeUpdate();
            }

            conn.commit();
            return true;

        } catch (Exception e) {
            e.printStackTrace();

            try {
                if (conn != null) {
                    conn.rollback();
                }
            } catch (Exception ignored) {
            }

        } finally {
            try {
                if (conn != null) {
                    conn.setAutoCommit(true);
                }
            } catch (Exception ignored) {
            }
        }

        return false;
    }
}