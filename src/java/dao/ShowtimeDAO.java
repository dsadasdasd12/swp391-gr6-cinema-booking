/*
 * Hệ thống Quản lý Rạp chiếu phim RapViet
 * Module: Duyệt phim - suất chiếu liệt kê ở trang chi tiết phim
 */
package dao;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import model.Showtime;
import util.DBContext;

/**
 * Truy xuất bảng dbo.SHOWTIMES đã join với phòng chiếu và chi nhánh, giới hạn
 * theo một phim để trang chi tiết hiển thị các suất chiếu sắp tới.
 *
 * @author Group6 - DuyThai (Module Duyệt phim)
 */
public class ShowtimeDAO {

    /**
     * Các suất chiếu sắp tới, còn bán vé của một phim, sắp theo thời gian. Chỉ
     * lấy suất ở tương lai và có trạng thái SCHEDULED hoặc ON_SALE.
     */
    public List<Showtime> findUpcomingByMovie(int movieId) {
        String sql = SELECT_BASE
                + "WHERE s.movie_id = ? "
                + "  AND s.status IN ('SCHEDULED','ON_SALE') "
                + "  AND s.start_time >= ? "
                + "ORDER BY s.start_time, b.name";
        List<Showtime> list = new ArrayList<>();
        Connection conn = DBContext.getInstance().getConnection();
        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, movieId);
            ps.setObject(2, LocalDateTime.now());   // mốc "hiện tại" để loại suất đã qua
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    list.add(mapRow(rs));
                }
            }
        } catch (SQLException e) {
            System.getLogger(ShowtimeDAO.class.getName())
                    .log(System.Logger.Level.ERROR, "findUpcomingByMovie thất bại", e);
        }
        return list;
    }

    /**
     * Các suất chiếu của một chi nhánh trong một ngày cụ thể, chỉ lấy suất còn
     * bán vé (SCHEDULED / ON_SALE). Sắp theo tên phim rồi giờ chiếu để tầng
     * service gom nhóm theo phim. Lọc chi nhánh qua HALLS.branch_id và lọc ngày
     * bằng cách so phần DATE của start_time.
     */
    public List<Showtime> findByBranchAndDate(int branchId, LocalDate date) {
        String sql = SELECT_BASE
                + "WHERE b.id = ? "
                + "  AND CAST(s.start_time AS DATE) = ? "
                + "  AND s.status IN ('SCHEDULED','ON_SALE') "
                + "ORDER BY m.title, s.start_time";
        List<Showtime> list = new ArrayList<>();
        Connection conn = DBContext.getInstance().getConnection();
        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, branchId);
            ps.setObject(2, date);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    list.add(mapRow(rs));
                }
            }
        } catch (SQLException e) {
            System.getLogger(ShowtimeDAO.class.getName())
                    .log(System.Logger.Level.ERROR, "findByBranchAndDate thất bại", e);
        }
        return list;
    }

    /** Phần SELECT + JOIN dùng chung; mỗi method chỉ nối thêm WHERE/ORDER BY. */
    private static final String SELECT_BASE =
            "SELECT s.id, s.movie_id, s.hall_id, s.start_time, s.end_time, "
            + "       s.base_price, s.status, "
            + "       h.name AS hall_name, h.hall_type, "
            + "       b.id AS branch_id, b.name AS branch_name, b.address AS branch_address, "
            + "       m.title AS movie_title, m.poster_url AS poster_url "
            + "FROM dbo.SHOWTIMES s "
            + "JOIN dbo.HALLS h    ON h.id = s.hall_id "
            + "JOIN dbo.BRANCHES b ON b.id = h.branch_id "
            + "JOIN dbo.MOVIES m   ON m.id = s.movie_id ";

    /** Ánh xạ một dòng ResultSet (theo SELECT_BASE) sang đối tượng Showtime. */
    private Showtime mapRow(ResultSet rs) throws SQLException {
        Showtime st = new Showtime();
        st.setId(rs.getInt("id"));
        st.setMovieId(rs.getInt("movie_id"));
        st.setHallId(rs.getInt("hall_id"));
        st.setStartTime(rs.getObject("start_time", LocalDateTime.class));
        st.setEndTime(rs.getObject("end_time", LocalDateTime.class));
        st.setBasePrice(rs.getBigDecimal("base_price"));
        st.setStatus(rs.getString("status"));
        st.setHallName(rs.getString("hall_name"));
        st.setHallType(rs.getString("hall_type"));
        st.setBranchId(rs.getInt("branch_id"));
        st.setBranchName(rs.getString("branch_name"));
        st.setBranchAddress(rs.getString("branch_address"));
        st.setMovieTitle(rs.getString("movie_title"));
        st.setPosterUrl(rs.getString("poster_url"));
        return st;
    }
}
