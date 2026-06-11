/*
 * Hệ thống Quản lý Rạp chiếu phim RapViet
 * Module: Xem tình trạng ghế - truy xuất bảng dbo.SEATS
 */
package dao;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import model.Seat;
import util.DBContext;

/**
 * DAO cho ghế ngồi. Trả về toàn bộ ghế của phòng chiếu gắn với một suất chiếu,
 * kèm cờ "đã đặt" tính riêng cho suất đó: một ghế bị coi là không còn trống khi
 * đã nằm trong một vé chưa hủy (BOOKING_SEATS) hoặc đang bị giữ chỗ trong giỏ
 * còn hiệu lực (CART_ITEMS.locked_until > hiện tại).
 *
 * Lưu ý: DBContext dùng chung một Connection (singleton) nên ở đây chỉ đóng
 * PreparedStatement và ResultSet, KHÔNG đóng Connection.
 *
 * @author Group6 - Huy (Module Duyệt phim)
 */
public class SeatDAO {

    /**
     * Tất cả ghế của phòng chiếu thuộc suất {@code showtimeId}, kèm trạng thái
     * đã đặt/đang giữ, sắp theo hàng rồi số ghế để view dựng sơ đồ dạng lưới.
     */
    public List<Seat> findByShowtime(int showtimeId) {
        // JOIN SHOWTIMES để biết phòng chiếu (hall_id) của suất; LEFT JOIN tới
        // tập ghế "không còn trống" (đã đặt hoặc đang giữ) để gắn cờ booked.
        String sql = "SELECT s.id, s.hall_id, s.seat_row, s.seat_number, s.seat_type, s.maintenance, "
                + "       CASE WHEN taken.seat_id IS NULL THEN 0 ELSE 1 END AS booked "
                + "FROM dbo.SEATS s "
                + "JOIN dbo.SHOWTIMES st ON st.id = ? "
                + "LEFT JOIN ( "
                + "      SELECT bs.seat_id "
                + "      FROM dbo.BOOKING_SEATS bs "
                + "      JOIN dbo.BOOKINGS bk ON bk.id = bs.booking_id "
                + "      WHERE bk.showtime_id = ? AND bk.status <> 'CANCELLED' "
                + "      UNION "
                + "      SELECT ci.seat_id "
                + "      FROM dbo.CART_ITEMS ci "
                + "      JOIN dbo.CART c ON c.id = ci.cart_id "
                + "      WHERE c.showtime_id = ? AND ci.locked_until > ? "
                + ") taken ON taken.seat_id = s.id "
                + "WHERE s.hall_id = st.hall_id "
                + "ORDER BY s.seat_row, s.seat_number";
        List<Seat> list = new ArrayList<>();
        Connection conn = DBContext.getInstance().getConnection();
        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, showtimeId);                 // phòng chiếu của suất
            ps.setInt(2, showtimeId);                 // vé đã đặt của suất
            ps.setInt(3, showtimeId);                 // giỏ hàng của suất
            ps.setObject(4, LocalDateTime.now());     // mốc kiểm tra giữ chỗ còn hiệu lực
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    list.add(map(rs));
                }
            }
        } catch (SQLException e) {
            System.getLogger(SeatDAO.class.getName())
                    .log(System.Logger.Level.ERROR, "findByShowtime thất bại", e);
        }
        return list;
    }

    /** Ánh xạ một dòng ResultSet sang đối tượng Seat. */
    private Seat map(ResultSet rs) throws SQLException {
        Seat s = new Seat();
        s.setId(rs.getInt("id"));
        s.setHallId(rs.getInt("hall_id"));
        s.setSeatRow(rs.getString("seat_row"));
        s.setSeatNumber(rs.getInt("seat_number"));
        s.setSeatType(rs.getString("seat_type"));
        s.setMaintenance(rs.getBoolean("maintenance"));
        s.setBooked(rs.getInt("booked") == 1);
        return s;
    }
}
