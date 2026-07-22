package dao;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.Statement;
import java.util.List;
import util.DBContext;

/**
 * DAO quan ly CART/CART_ITEMS cho luong dat ve online.
 *
 * <p>Cart khong phai booking. No chi la ban ghi giu ghe co thoi han de customer
 * co the chon F&B va kiem tra don ma staff/customer khac khong ban trung ghe.
 * Khi customer bam "Xác nhận đặt vé", BookingDAO se chuyen cart nay thanh
 * BOOKINGS + BOOKING_SEATS trong cung transaction.</p>
 */
public class CartDAO {

    private static final int HOLD_MINUTES = 15;

    /**
     * Tao mot lock ghe tam. SQL Server application lock cung resource voi
     * BookingDAO de thao tac check-then-insert cua online/POS khong chay song
     * song tren cung mot suat chieu.
     */
    public int createSeatHold(int userId, int showtimeId, List<Integer> seatIds,
            List<Double> prices) {
        if (userId <= 0 || showtimeId <= 0 || seatIds == null || seatIds.isEmpty()
                || prices == null || prices.size() != seatIds.size()) {
            return -1;
        }

        String placeholders = placeholders(seatIds.size());
        String checkShowtime = "SELECT COUNT(*) FROM dbo.SHOWTIMES st "
                + "JOIN dbo.MOVIES m ON m.id = st.movie_id "
                + "WHERE st.id=? AND st.status IN ('SCHEDULED','ON_SALE') "
                + "AND m.status='NOW_SHOWING' "
                + "AND DATEADD(MINUTE,30,st.start_time)>GETDATE()";
        String checkSeats = "SELECT COUNT(*) FROM dbo.SEATS se "
                + "JOIN dbo.SHOWTIMES st ON st.hall_id=se.hall_id "
                + "WHERE st.id=? AND se.maintenance=0 AND se.id IN (" + placeholders + ")";
        String checkBookings = "SELECT COUNT(*) FROM dbo.BOOKING_SEATS bs "
                + "JOIN dbo.BOOKINGS b ON b.id=bs.booking_id "
                + "WHERE b.showtime_id=? AND b.status IN ('PENDING','CONFIRMED','CHECKED_IN','USED') "
                + "AND bs.seat_id IN (" + placeholders + ")";
        String checkCarts = "SELECT COUNT(*) FROM dbo.CART_ITEMS ci "
                + "JOIN dbo.CART c ON c.id=ci.cart_id "
                + "WHERE c.showtime_id=? AND c.expires_at>GETDATE() "
                + "AND ci.locked_until>GETDATE() AND ci.seat_id IN (" + placeholders + ")";
        String insertCart = "INSERT INTO dbo.CART(user_id,showtime_id,created_at,expires_at,last_update) "
                + "VALUES(?,?,GETDATE(),DATEADD(MINUTE," + HOLD_MINUTES + ",GETDATE()),GETDATE())";
        String insertItem = "INSERT INTO dbo.CART_ITEMS(cart_id,seat_id,price,locked_until,last_update) "
                + "VALUES(?,?,?,DATEADD(MINUTE," + HOLD_MINUTES + ",GETDATE()),GETDATE())";

        Connection conn = null;
        try {
            conn = DBContext.getInstance().getConnection();
            conn.setAutoCommit(false);
            if (!acquireShowtimeSeatLock(conn, showtimeId)) {
                conn.rollback();
                return -1;
            }

            // Xoa lock da het han de truy van ben duoi chi can quan tam lock con hieu luc.
            try (PreparedStatement ps = conn.prepareStatement(
                    "DELETE ci FROM dbo.CART_ITEMS ci JOIN dbo.CART c ON c.id=ci.cart_id "
                    + "WHERE c.showtime_id=? AND (c.expires_at<=GETDATE() OR ci.locked_until<=GETDATE())")) {
                ps.setInt(1, showtimeId);
                ps.executeUpdate();
            }

            if (!countMatches(conn, checkShowtime, showtimeId, null, 1)
                    || !countMatches(conn, checkSeats, showtimeId, seatIds, seatIds.size())
                    || !countMatches(conn, checkBookings, showtimeId, seatIds, 0)
                    || !countMatches(conn, checkCarts, showtimeId, seatIds, 0)) {
                conn.rollback();
                return -1;
            }

            int cartId = -1;
            try (PreparedStatement ps = conn.prepareStatement(insertCart, Statement.RETURN_GENERATED_KEYS)) {
                ps.setInt(1, userId);
                ps.setInt(2, showtimeId);
                ps.executeUpdate();
                try (ResultSet rs = ps.getGeneratedKeys()) {
                    if (rs.next()) {
                        cartId = rs.getInt(1);
                    }
                }
            }
            if (cartId <= 0) {
                conn.rollback();
                return -1;
            }

            try (PreparedStatement ps = conn.prepareStatement(insertItem)) {
                for (int i = 0; i < seatIds.size(); i++) {
                    ps.setInt(1, cartId);
                    ps.setInt(2, seatIds.get(i));
                    ps.setDouble(3, prices.get(i));
                    ps.addBatch();
                }
                ps.executeBatch();
            }
            conn.commit();
            return cartId;
        } catch (Exception ex) {
            if (conn != null) {
                try { conn.rollback(); } catch (Exception ignored) { }
            }
            return -1;
        } finally {
            if (conn != null) {
                try { conn.close(); } catch (Exception ignored) { }
            }
        }
    }

    /** Gia han cart khi customer tiep tuc thao tac o F&B/confirm. */
    public boolean refreshSeatHold(int userId, int cartId) {
        String sql = "UPDATE c SET expires_at=DATEADD(MINUTE," + HOLD_MINUTES
                + ",GETDATE()),last_update=GETDATE() FROM dbo.CART c "
                + "WHERE c.id=? AND c.user_id=? AND c.expires_at>GETDATE() "
                + "AND EXISTS(SELECT 1 FROM dbo.CART_ITEMS ci WHERE ci.cart_id=c.id AND ci.locked_until>GETDATE())";
        String itemSql = "UPDATE dbo.CART_ITEMS SET locked_until=DATEADD(MINUTE," + HOLD_MINUTES
                + ",GETDATE()),last_update=GETDATE() WHERE cart_id=?";
        try (Connection conn = DBContext.getInstance().getConnection()) {
            conn.setAutoCommit(false);
            try (PreparedStatement ps = conn.prepareStatement(sql)) {
                ps.setInt(1, cartId);
                ps.setInt(2, userId);
                if (ps.executeUpdate() != 1) {
                    conn.rollback();
                    return false;
                }
            }
            try (PreparedStatement ps = conn.prepareStatement(itemSql)) {
                ps.setInt(1, cartId);
                ps.executeUpdate();
            }
            conn.commit();
            return true;
        } catch (Exception ex) {
            return false;
        }
    }

    /** Customer chon lai ghe: bo lock cu cua chinh minh. */
    public void releaseSeatHold(int userId, int cartId) {
        if (userId <= 0 || cartId <= 0) return;
        try (Connection conn = DBContext.getInstance().getConnection()) {
            conn.setAutoCommit(false);
            try (PreparedStatement items = conn.prepareStatement(
                    "DELETE ci FROM dbo.CART_ITEMS ci JOIN dbo.CART c ON c.id=ci.cart_id WHERE c.id=? AND c.user_id=?")) {
                items.setInt(1, cartId);
                items.setInt(2, userId);
                items.executeUpdate();
            }
            try (PreparedStatement cart = conn.prepareStatement("DELETE FROM dbo.CART WHERE id=? AND user_id=?")) {
                cart.setInt(1, cartId);
                cart.setInt(2, userId);
                cart.executeUpdate();
            }
            conn.commit();
        } catch (Exception ignored) { }
    }

    private boolean countMatches(Connection conn, String sql, int showtimeId,
            List<Integer> seatIds, int expected) throws Exception {
        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, showtimeId);
            if (seatIds != null) {
                int index = 2;
                for (Integer seatId : seatIds) ps.setInt(index++, seatId);
            }
            try (ResultSet rs = ps.executeQuery()) {
                return rs.next() && rs.getInt(1) == expected;
            }
        }
    }

    private boolean acquireShowtimeSeatLock(Connection conn, int showtimeId) throws Exception {
        String sql = "DECLARE @result INT; EXEC @result = sp_getapplock "
                + "@Resource=?,@LockMode='Exclusive',@LockOwner='Session',@LockTimeout=5000; "
                + "SELECT @result AS lock_result;";
        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, "RapViet:booking-seat-showtime:" + showtimeId);
            boolean resultSet = ps.execute();
            while (true) {
                if (resultSet) {
                    try (ResultSet rs = ps.getResultSet()) {
                        if (rs != null && rs.next()) return rs.getInt("lock_result") >= 0;
                    }
                } else if (ps.getUpdateCount() == -1) return false;
                resultSet = ps.getMoreResults();
            }
        }
    }

    private String placeholders(int size) {
        StringBuilder values = new StringBuilder();
        for (int i = 0; i < size; i++) {
            if (i > 0) values.append(',');
            values.append('?');
        }
        return values.toString();
    }
}
