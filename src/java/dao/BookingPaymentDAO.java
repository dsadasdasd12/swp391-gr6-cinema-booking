package dao;

import java.sql.SQLException;
import dto.TicketSeatView;
import dto.TicketView;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;
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
    public TicketView getTicketViewByBookingId(int bookingId) throws SQLException {

    String sql = """
        SELECT
            b.id AS booking_id,
            b.status AS booking_status,
            b.qr_code,

            p.status AS payment_status,
            p.gateway,
            p.method,
            p.transaction_id,
            p.amount,

            m.title,
            m.poster_url,

            br.name AS branch_name,
            h.name AS hall_name,

            CAST(st.start_time AS DATE) AS show_date,
            CAST(st.start_time AS TIME) AS show_time
        FROM BOOKINGS b
        JOIN PAYMENTS p
                ON p.booking_id = b.id
        JOIN SHOWTIMES st
                ON st.id = b.showtime_id
        JOIN MOVIES m
                ON m.id = st.movie_id
        JOIN HALLS h
                ON h.id = st.hall_id
        JOIN BRANCHES br
                ON br.id = h.branch_id
        WHERE b.id = ?
        """;

    try (Connection conn = DBContext.getInstance().getConnection();
         PreparedStatement ps = conn.prepareStatement(sql)) {

        ps.setInt(1, bookingId);

        try (ResultSet rs = ps.executeQuery()) {

            if (!rs.next()) {
                return null;
            }

            TicketView ticket = new TicketView();

            ticket.setBookingId(rs.getInt("booking_id"));
            ticket.setBookingStatus(rs.getString("booking_status"));

            ticket.setPaymentStatus(rs.getString("payment_status"));
            ticket.setPaymentGateway(rs.getString("gateway"));
            ticket.setPaymentMethod(rs.getString("method"));
            ticket.setTransactionId(rs.getString("transaction_id"));

            ticket.setFinalAmount(rs.getDouble("amount"));

            ticket.setMovieTitle(rs.getString("title"));
            ticket.setMoviePoster(rs.getString("poster_url"));

            ticket.setBranchName(rs.getString("branch_name"));
            ticket.setHallName(rs.getString("hall_name"));

            ticket.setShowDate(rs.getString("show_date"));
            ticket.setShowTime(rs.getString("show_time"));

            ticket.setQrCode(rs.getString("qr_code"));

            ticket.setSeats(getTicketSeats(conn, bookingId));

            return ticket;
        }
    }
}
    private List<TicketSeatView> getTicketSeats(Connection conn, int bookingId)
        throws SQLException {

    String sql = """
        SELECT
            se.id AS seat_id,
            CONCAT(se.seat_row, se.seat_number) AS seat_name,
            se.seat_type,
            bs.price
        FROM dbo.BOOKING_SEATS bs
        JOIN dbo.SEATS se
            ON se.id = bs.seat_id
        WHERE bs.booking_id = ?
        ORDER BY se.seat_row, se.seat_number
        """;

    List<TicketSeatView> seats = new ArrayList<>();

    try (PreparedStatement ps = conn.prepareStatement(sql)) {
        ps.setInt(1, bookingId);

        try (ResultSet rs = ps.executeQuery()) {
            while (rs.next()) {
                TicketSeatView seat = new TicketSeatView();

                seat.setSeatId(rs.getInt("seat_id"));
                seat.setSeatName(rs.getString("seat_name"));
                seat.setSeatType(rs.getString("seat_type"));
                seat.setPrice(rs.getDouble("price"));

                seats.add(seat);
            }
        }
    }

    return seats;
}
}