package dao;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.Statement;
import util.DBContext;

public class BookingPaymentDAO {

    public int createPendingBooking(
            int userId,
            int showtimeId,
            double totalPrice,
            String qrCode) {

        String sql = """
            INSERT INTO dbo.BOOKINGS
            (user_id, showtime_id, source, status, total_price, qr_code, booked_at, last_update)
            VALUES
            (?, ?, 'ONLINE', 'PENDING', ?, ?, GETDATE(), GETDATE())
        """;

        try (Connection conn = DBContext.getInstance().getConnection();
             PreparedStatement ps = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {

            ps.setInt(1, userId);
            ps.setInt(2, showtimeId);
            ps.setDouble(3, totalPrice);
            ps.setString(4, qrCode);

            ps.executeUpdate();

            try (ResultSet rs = ps.getGeneratedKeys()) {
                if (rs.next()) {
                    return rs.getInt(1);
                }
            }

        } catch (Exception e) {
            e.printStackTrace();
        }

        return 0;
    }

    public boolean addBookingSeat(int bookingId, int seatId, double price) {
        String sql = """
            INSERT INTO dbo.BOOKING_SEATS
            (booking_id, seat_id, price)
            VALUES
            (?, ?, ?)
        """;

        try (Connection conn = DBContext.getInstance().getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setInt(1, bookingId);
            ps.setInt(2, seatId);
            ps.setDouble(3, price);

            return ps.executeUpdate() > 0;

        } catch (Exception e) {
            e.printStackTrace();
        }

        return false;
    }

    public boolean createPendingPayment(
            int bookingId,
            String method,
            String gateway,
            String transactionId,
            double amount) {

        String sql = """
            INSERT INTO dbo.PAYMENTS
            (booking_id, method, gateway, transaction_id, status, amount, created_at)
            VALUES
            (?, ?, ?, ?, 'PENDING', ?, GETDATE())
        """;

        try (Connection conn = DBContext.getInstance().getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setInt(1, bookingId);
            ps.setString(2, method);
            ps.setString(3, gateway);
            ps.setString(4, transactionId);
            ps.setDouble(5, amount);

            return ps.executeUpdate() > 0;

        } catch (Exception e) {
            e.printStackTrace();
        }

        return false;
    }
}