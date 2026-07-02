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
import model.Genre;
import util.DBContext;
import util.EncodingUtil;

/**
 * DAO cho thể loại phim. Dùng để đổ dữ liệu cho ô lọc thể loại và để nạp danh
 * sách thể loại của một phim cụ thể.
 *
 * Lưu ý: DBContext dùng chung một Connection (singleton) nên ở đây chỉ đóng
 * PreparedStatement và ResultSet, KHÔNG đóng Connection.
 *
 * @author HuyPD
 */
public class GenreDAO {

    /** Tất cả thể loại đang hoạt động, sắp theo tên — cho ô lọc. */
    public List<Genre> findAllActive() {
        String sql = "SELECT id, name, description, status "
                + "FROM dbo.CATEGORY WHERE status = 'ACTIVE' ORDER BY name";
        List<Genre> list = new ArrayList<>();
        Connection conn = DBContext.getInstance().getConnection();
        try (PreparedStatement ps = conn.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {
            while (rs.next()) {
                list.add(map(rs));
            }
        } catch (SQLException e) {
            System.getLogger(GenreDAO.class.getName())
                    .log(System.Logger.Level.ERROR, "findAllActive thất bại", e);
        }
        return list;
    }

    /** Các thể loại gắn với một phim (qua bảng dbo.MOVIES_CATEGORY). */
    public List<Genre> findByMovieId(int movieId) {
        String sql = "SELECT c.id, c.name, c.description, c.status "
                + "FROM dbo.CATEGORY c "
                + "JOIN dbo.MOVIES_CATEGORY mc ON mc.category_id = c.id "
                + "WHERE mc.movie_id = ? ORDER BY c.name";
        List<Genre> list = new ArrayList<>();
        Connection conn = DBContext.getInstance().getConnection();
        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, movieId);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    list.add(map(rs));
                }
            }
        } catch (SQLException e) {
            System.getLogger(GenreDAO.class.getName())
                    .log(System.Logger.Level.ERROR, "findByMovieId thất bại", e);
        }
        return list;
    }

    public List<Genre> findAll() {
        String sql = "SELECT id, name, description, status FROM dbo.CATEGORY ORDER BY name";
        List<Genre> list = new ArrayList<>();
        Connection conn = DBContext.getInstance().getConnection();
        try (PreparedStatement ps = conn.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {
            while (rs.next()) {
                list.add(map(rs));
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return list;
    }

    public boolean insert(Genre c) {
        String sql = "INSERT INTO dbo.CATEGORY (name, description, status) VALUES (?, ?, ?)";
        Connection conn = DBContext.getInstance().getConnection();
        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setNString(1, c.getName());
            ps.setNString(2, c.getDescription());
            ps.setString(3, c.getStatus());
            return ps.executeUpdate() > 0;
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return false;
    }

    public boolean update(Genre c) {
        String sql = "UPDATE dbo.CATEGORY SET name = ?, description = ?, status = ? WHERE id = ?";
        Connection conn = DBContext.getInstance().getConnection();
        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setNString(1, c.getName());
            ps.setNString(2, c.getDescription());
            ps.setString(3, c.getStatus());
            ps.setInt(4, c.getId());
            return ps.executeUpdate() > 0;
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return false;
    }

    public boolean delete(int id) {
        String sql = "DELETE FROM dbo.CATEGORY WHERE id = ?";
        Connection conn = DBContext.getInstance().getConnection();
        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, id);
            return ps.executeUpdate() > 0;
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return false;
    }

    /** Ánh xạ một dòng ResultSet sang đối tượng Genre. */
    private Genre map(ResultSet rs) throws SQLException {
        Genre c = new Genre();
        c.setId(rs.getInt("id"));
        c.setName(EncodingUtil.getString(rs, "name"));
        c.setDescription(EncodingUtil.getString(rs, "description"));
        c.setStatus(rs.getString("status"));
        return c;
    }
}
