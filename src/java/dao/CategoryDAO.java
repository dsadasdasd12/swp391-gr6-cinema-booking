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
import util.EncodingUtil;

/**
 * DAO cho thể loại phim. Dùng để đổ dữ liệu cho ô lọc thể loại và để nạp danh
 * sách thể loại của một phim cụ thể.
 *
 * Lưu ý: DBContext dùng chung một Connection (singleton) nên ở đây chỉ đóng
 * PreparedStatement và ResultSet, KHÔNG đóng Connection.
 *
 * @author LONG
 */
public class CategoryDAO {

    public List<Category> findAll() {
        String sql = "SELECT id, name, description, status FROM dbo.CATEGORY ORDER BY name";
        List<Category> list = new ArrayList<>();
        Connection conn = DBContext.getInstance().getConnection();
        try (PreparedStatement ps = conn.prepareStatement(sql); ResultSet rs = ps.executeQuery()) {
            while (rs.next()) {
                list.add(map(rs));
            }
        } catch (SQLException e) {
            System.getLogger(CategoryDAO.class.getName()).log(System.Logger.Level.ERROR, "findAll failed", e);
        }
        return list;
    }

    public Category findById(int id) {
        String sql = "SELECT id, name, description, status FROM dbo.CATEGORY WHERE id = ?";
        Connection conn = DBContext.getInstance().getConnection();
        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, id);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return map(rs);
                }
            }
        } catch (SQLException e) {
            System.getLogger(CategoryDAO.class.getName()).log(System.Logger.Level.ERROR, "findById failed", e);
        }
        return null;
    }

    /**
     * Tất cả thể loại đang hoạt động, sắp theo tên — cho ô lọc.
     */
    public List<Category> findAllActive() {
        String sql = "SELECT id, name, description, status "
                + "FROM dbo.CATEGORY WHERE status = 'ACTIVE' ORDER BY name";
        List<Category> list = new ArrayList<>();
        Connection conn = DBContext.getInstance().getConnection();
        try (PreparedStatement ps = conn.prepareStatement(sql); ResultSet rs = ps.executeQuery()) {
            while (rs.next()) {
                list.add(map(rs));
            }
        } catch (SQLException e) {
            System.getLogger(CategoryDAO.class.getName())
                    .log(System.Logger.Level.ERROR, "findAllActive thất bại", e);
        }
        return list;
    }

    /**
     * Các thể loại gắn với một phim (qua bảng dbo.MOVIES_CATEGORY).
     */
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

    public boolean insert(Category category) {
        String sql = "INSERT INTO dbo.CATEGORY (name, description, status) VALUES (?, ?, ?)";
        Connection conn = DBContext.getInstance().getConnection();
        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, category.getName());
            ps.setString(2, category.getDescription());
            ps.setString(3, normalizeStatus(category.getStatus()));
            return ps.executeUpdate() > 0;
        } catch (SQLException e) {
            System.getLogger(CategoryDAO.class.getName()).log(System.Logger.Level.ERROR, "insert failed", e);
        }
        return false;
    }

    public boolean update(Category category) {
        String sql = "UPDATE dbo.CATEGORY SET name = ?, description = ?, status = ?, last_update = GETDATE() WHERE id = ?";
        Connection conn = DBContext.getInstance().getConnection();
        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, category.getName());
            ps.setString(2, category.getDescription());
            ps.setString(3, normalizeStatus(category.getStatus()));
            ps.setInt(4, category.getId());
            return ps.executeUpdate() > 0;
        } catch (SQLException e) {
            System.getLogger(CategoryDAO.class.getName()).log(System.Logger.Level.ERROR, "update failed", e);
        }
        return false;
    }

    public boolean delete(int id) {
        String sql = "UPDATE dbo.CATEGORY SET status = 'INACTIVE', last_update = GETDATE() WHERE id = ?";
        Connection conn = DBContext.getInstance().getConnection();
        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, id);
            return ps.executeUpdate() > 0;
        } catch (SQLException e) {
            System.getLogger(CategoryDAO.class.getName()).log(System.Logger.Level.ERROR, "delete failed", e);
        }
        return false;
    }

    /**
     * Ánh xạ một dòng ResultSet sang đối tượng Category.
     */
    private Category map(ResultSet rs) throws SQLException {
        Category c = new Category();
        c.setId(rs.getInt("id"));
        c.setName(EncodingUtil.getString(rs, "name"));
        c.setDescription(EncodingUtil.getString(rs, "description"));
        c.setStatus(rs.getString("status"));
        return c;
    }

    private String normalizeStatus(String status) {
        if (status == null || status.trim().isEmpty()) {
            return "ACTIVE";
        }
        String normalized = status.trim().toUpperCase();
        return "INACTIVE".equals(normalized) ? "INACTIVE" : "ACTIVE";
    }
}
