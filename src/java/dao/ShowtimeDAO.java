/*
 * Hệ thống Quản lý Rạp chiếu phim RapViet
 * Module: Duyệt phim - suất chiếu liệt kê ở trang chi tiết phim
 */
package dao;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import model.Showtime;
import util.DBContext;
import util.EncodingUtil;

/**
 * Truy xuất bảng dbo.SHOWTIMES đã join với phòng chiếu và chi nhánh, giới hạn
 * theo một phim để trang chi tiết hiển thị các suất chiếu sắp tới.
 *
 * @author LONG
 */
public class ShowtimeDAO {

    /**
     * Các suất chiếu sắp tới, còn bán vé của một phim, sắp theo thời gian. Chỉ
     * lấy suất ở tương lai và có trạng thái SCHEDULED hoặc ON_SALE.
     */
    public List<Showtime> findUpcomingByMovie(int movieId) {
        String sql = "SELECT s.id, s.movie_id, s.hall_id, s.start_time, s.end_time, "
                + "       s.base_price, s.status, "
                + "       h.name() AS hall_name, h.hall_type, "
                + "       b.id AS branch_id, b.name() AS branch_name, b.address AS branch_address "
                + "FROM dbo.SHOWTIMES s "
                + "JOIN dbo.HALLS h    ON h.id = s.hall_id "
                + "JOIN dbo.BRANCHES b ON b.id = h.branch_id "
                + "WHERE s.movie_id = ? "
                + "  AND s.status IN ('SCHEDULED','ON_SALE') "
                + "  AND s.start_time >= ? "
                + "ORDER BY s.start_time, b.name()";
        List<Showtime> list = new ArrayList<>();
        Connection conn = DBContext.getInstance().getConnection();
        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, movieId);
            ps.setObject(2, LocalDateTime.now());   // mốc "hiện tại" để loại suất đã qua
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    Showtime st = new Showtime();
                    st.setId(rs.getInt("id"));
                    st.setMovieId(rs.getInt("movie_id"));
                    st.setHallId(rs.getInt("hall_id"));
                    st.setStartTime(rs.getObject("start_time", LocalDateTime.class));
                    st.setEndTime(rs.getObject("end_time", LocalDateTime.class));
                    st.setBasePrice(rs.getBigDecimal("base_price"));
                    st.setStatus(rs.getString("status"));
                    st.setHallName(EncodingUtil.getString(rs, "hall_name"));
                    st.setHallType(rs.getString("hall_type"));
                    st.setBranchId(rs.getInt("branch_id"));
                    st.setBranchName(EncodingUtil.getString(rs, "branch_name"));
                    st.setBranchAddress(EncodingUtil.getString(rs, "branch_address"));
                    list.add(st);
                }
            }
        } catch (SQLException e) {
            System.getLogger(ShowtimeDAO.class.getName())
                    .log(System.Logger.Level.ERROR, "findUpcomingByMovie thất bại", e);
        }
        return list;
    }
}
