/*
 * Hệ thống Quản lý Rạp chiếu phim RapViet
 * Module: Đặt vé - truy xuất đơn đặt vé cho KHÁCH (lịch sử / chi tiết / hủy)
 */
package dao;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import dto.BookingView;
import model.Booking;
import util.DBContext;

/**
 * Truy xuất đơn đặt vé phục vụ phần KHÁCH: xem lịch sử, xem chi tiết và tự hủy
 * đơn của chính mình. Mỗi đơn được join sẵn với phim/suất chiếu/chi nhánh/phòng
 * và gom danh sách ghế (STRING_AGG) để trả về DTO {@link BookingView} — entity
 * Booking giữ nguyên đúng cột bảng dbo.BOOKINGS.
 *
 * Lưu ý: DBContext của nhánh này dùng chung một Connection (singleton) nên chỉ
 * đóng PreparedStatement/ResultSet, KHÔNG đóng Connection.
 *
 * @author Group6 - Huy (Module Đặt vé)
 */
public class BookingDAO {

    /** SELECT + JOIN dùng chung cho truy vấn hiển thị đơn của khách. */
    private static final String SELECT_VIEW =
            "SELECT bk.id, bk.user_id, bk.showtime_id, bk.source, bk.status, "
            + "bk.total_price, bk.qr_code, bk.booked_at, "
            + "m.id AS movie_id, m.title AS movie_title, s.start_time, "
            + "b.name AS branch_name, h.name AS hall_name, "
            + "(SELECT COUNT(*) FROM dbo.BOOKING_SEATS bs WHERE bs.booking_id = bk.id) AS seat_count, "
            + "(SELECT STRING_AGG(CONCAT(se.seat_row, se.seat_number), ', ') "
            + "   WITHIN GROUP (ORDER BY se.seat_row, se.seat_number) "
            + "   FROM dbo.BOOKING_SEATS bs JOIN dbo.SEATS se ON se.id = bs.seat_id "
            + "   WHERE bs.booking_id = bk.id) AS seat_labels "
            + "FROM dbo.BOOKINGS bk "
            + "JOIN dbo.SHOWTIMES s ON s.id = bk.showtime_id "
            + "JOIN dbo.MOVIES m   ON m.id = s.movie_id "
            + "JOIN dbo.HALLS h    ON h.id = s.hall_id "
            + "JOIN dbo.BRANCHES b ON b.id = h.branch_id ";

    /** Lịch sử đặt vé của một khách, mới nhất lên đầu (booking history). */
    public List<BookingView> findByUserId(int userId) {
        String sql = SELECT_VIEW + "WHERE bk.user_id = ? ORDER BY bk.booked_at DESC";
        List<BookingView> list = new ArrayList<>();
        Connection conn = DBContext.getInstance().getConnection();
        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, userId);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    list.add(mapRow(rs));
                }
            }
        } catch (SQLException e) {
            System.getLogger(BookingDAO.class.getName())
                    .log(System.Logger.Level.ERROR, "findByUserId thất bại", e);
        }
        return list;
    }

    /**
     * Chi tiết một đơn của chính khách đó (kiểm tra quyền sở hữu qua user_id);
     * trả null nếu không tồn tại hoặc không phải đơn của khách.
     */
    public BookingView findDetailByIdAndUser(int bookingId, int userId) {
        String sql = SELECT_VIEW + "WHERE bk.id = ? AND bk.user_id = ?";
        Connection conn = DBContext.getInstance().getConnection();
        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, bookingId);
            ps.setInt(2, userId);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return mapRow(rs);
                }
            }
        } catch (SQLException e) {
            System.getLogger(BookingDAO.class.getName())
                    .log(System.Logger.Level.ERROR, "findDetailByIdAndUser thất bại", e);
        }
        return null;
    }

    /**
     * Khách tự hủy đơn của mình: chỉ hủy được khi đơn đang ở trạng thái
     * PENDING hoặc CONFIRMED và đúng là đơn của khách. Trả về true nếu hủy thành công.
     */
    public boolean cancelByUser(int bookingId, int userId) {
        String sql = "UPDATE dbo.BOOKINGS "
                + "SET status = 'CANCELLED', last_update = GETDATE() "
                + "WHERE id = ? AND user_id = ? AND status IN ('PENDING','CONFIRMED')";
        Connection conn = DBContext.getInstance().getConnection();
        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, bookingId);
            ps.setInt(2, userId);
            return ps.executeUpdate() > 0;
        } catch (SQLException e) {
            System.getLogger(BookingDAO.class.getName())
                    .log(System.Logger.Level.ERROR, "cancelByUser thất bại", e);
        }
        return false;
    }

    /** Ánh xạ một dòng ResultSet sang BookingView (Booking theo cột DB + dữ liệu ghép). */
    private BookingView mapRow(ResultSet rs) throws SQLException {
        Booking bk = new Booking();
        bk.setId(rs.getInt("id"));
        bk.setUserId(rs.getInt("user_id"));
        bk.setShowtimeId(rs.getInt("showtime_id"));
        bk.setSource(rs.getString("source"));
        bk.setStatus(rs.getString("status"));
        bk.setTotalPrice(rs.getDouble("total_price"));
        bk.setQrCode(rs.getString("qr_code"));
        bk.setBookedAt(rs.getTimestamp("booked_at"));

        BookingView v = new BookingView();
        v.setBooking(bk);
        v.setMovieId(rs.getInt("movie_id"));
        v.setMovieTitle(rs.getString("movie_title"));
        v.setBranchName(rs.getString("branch_name"));
        v.setHallName(rs.getString("hall_name"));
        v.setShowStart(rs.getObject("start_time", LocalDateTime.class));
        v.setSeatLabels(rs.getString("seat_labels"));
        v.setSeatCount(rs.getInt("seat_count"));
        return v;
    }
}
