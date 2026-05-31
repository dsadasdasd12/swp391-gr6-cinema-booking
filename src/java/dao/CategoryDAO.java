/*
 * Hệ thống Quản lý Rạp chiếu phim RapViet
 * Module: Duyệt phim - truy xuất dữ liệu bảng dbo.CATEGORY
 */
package dao;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import model.Category;
import util.DBContext;

/**
 * DAO cho thể loại phim. Dùng để đổ dữ liệu cho ô lọc thể loại và để nạp danh
 * sách thể loại của một phim cụ thể.
 *
 * Lưu ý: DBContext dùng chung một Connection (singleton) nên ở đây chỉ đóng
 * PreparedStatement và ResultSet, KHÔNG đóng Connection.
 *
 * @author Group6 - DuyThai (Module Duyệt phim)
 */
public class CategoryDAO {

    /** Tất cả thể loại đang hoạt động, sắp theo tên — cho ô lọc. */
    public List<Category> findAllActive() {
        String sql = "SELECT id, name, description, status "
                + "FROM dbo.CATEGORY WHERE status = 'ACTIVE' ORDER BY name";
        List<Category> list = new ArrayList<>();
        Connection conn = DBContext.getInstance().getConnection();
        try (PreparedStatement ps = conn.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {
            while (rs.next()) {
                list.add(map(rs));
            }
        } catch (SQLException e) {
            System.getLogger(CategoryDAO.class.getName())
                    .log(System.Logger.Level.ERROR, "findAllActive thất bại", e);
        }
        return list;
    }

    /** Các thể loại gắn với một phim (qua bảng dbo.MOVIES_CATEGORY). */
    public List<Category> findByMovieId(int movieId) {
        String sql = "SELECT c.id, c.name, c.description, c.status "
                + "FROM dbo.CATEGORY c "
                + "JOIN dbo.MOVIES_CATEGORY mc ON mc.category_id = c.id "
                + "WHERE mc.movie_id = ? ORDER BY c.name";
        List<Category> list = new ArrayList<>();
        Connection conn = DBContext.getInstance().getConnection();
        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, movieId);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    list.add(map(rs));
                }
            }
        } catch (SQLException e) {
            System.getLogger(CategoryDAO.class.getName())
                    .log(System.Logger.Level.ERROR, "findByMovieId thất bại", e);
        }
        return list;
    }

    /** Ánh xạ một dòng ResultSet sang đối tượng Category. */
    private Category map(ResultSet rs) throws SQLException {
        Category c = new Category();
        c.setId(rs.getInt("id"));
        c.setName(rs.getString("name"));
        c.setDescription(rs.getString("description"));
        c.setStatus(rs.getString("status"));
        return c;
    }
}
