package dao;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import model.Language;
import util.DBContext;
import util.EncodingUtil;

public class LanguageDAO {

    public List<Language> findAll() {
        String sql = "SELECT id, name, code, status FROM dbo.LANGUAGES ORDER BY name";
        List<Language> list = new ArrayList<>();
        Connection conn = DBContext.getInstance().getConnection();
        try (PreparedStatement ps = conn.prepareStatement(sql); ResultSet rs = ps.executeQuery()) {
            while (rs.next()) {
                list.add(map(rs));
            }
        } catch (SQLException e) {
            System.getLogger(LanguageDAO.class.getName()).log(System.Logger.Level.ERROR, "findAll failed", e);
        }
        return list;
    }

    public Language findById(int id) {
        String sql = "SELECT id, name, code, status FROM dbo.LANGUAGES WHERE id = ?";
        Connection conn = DBContext.getInstance().getConnection();
        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, id);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return map(rs);
                }
            }
        } catch (SQLException e) {
            System.getLogger(LanguageDAO.class.getName()).log(System.Logger.Level.ERROR, "findById failed", e);
        }
        return null;
    }

    public List<Language> findAllActive() {
        String sql = "SELECT id, name, code, status FROM dbo.LANGUAGES WHERE status = 'ACTIVE' ORDER BY name";
        List<Language> list = new ArrayList<>();
        Connection conn = DBContext.getInstance().getConnection();
        try (PreparedStatement ps = conn.prepareStatement(sql); ResultSet rs = ps.executeQuery()) {
            while (rs.next()) {
                list.add(map(rs));
            }
        } catch (SQLException e) {
            System.getLogger(LanguageDAO.class.getName()).log(System.Logger.Level.ERROR, "findAllActive failed", e);
        }
        return list;
    }

    public List<Language> findByMovieId(int movieId) {
        List<Language> list = new ArrayList<>();
        String sql = "SELECT l.id, l.name, l.code, l.status, ml.subtitle "
                + "FROM dbo.MOVIE_LANGUAGES ml "
                + "JOIN dbo.LANGUAGES l ON l.id = ml.language_id "
                + "WHERE ml.movie_id = ? ORDER BY l.name";
        Connection conn = DBContext.getInstance().getConnection();
        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, movieId);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    Language language = map(rs);
                    language.setSubtitle(rs.getBoolean("subtitle"));
                    list.add(language);
                }
            }
        } catch (SQLException e) {
            System.getLogger(LanguageDAO.class.getName()).log(System.Logger.Level.ERROR, "findByMovieId failed", e);
        }
        return list;
    }

    public boolean insert(Language language) {
        String sql = "INSERT INTO dbo.LANGUAGES (name, code, status) VALUES (?, ?, ?)";
        Connection conn = DBContext.getInstance().getConnection();
        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, language.getName());
            ps.setString(2, normalizeCode(language.getCode()));
            ps.setString(3, normalizeStatus(language.getStatus()));
            return ps.executeUpdate() > 0;
        } catch (SQLException e) {
            System.getLogger(LanguageDAO.class.getName()).log(System.Logger.Level.ERROR, "insert failed", e);
        }
        return false;
    }

    public boolean update(Language language) {
        String sql = "UPDATE dbo.LANGUAGES SET name = ?, code = ?, status = ?, last_update = GETDATE() WHERE id = ?";
        Connection conn = DBContext.getInstance().getConnection();
        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, language.getName());
            ps.setString(2, normalizeCode(language.getCode()));
            ps.setString(3, normalizeStatus(language.getStatus()));
            ps.setInt(4, language.getId());
            return ps.executeUpdate() > 0;
        } catch (SQLException e) {
            System.getLogger(LanguageDAO.class.getName()).log(System.Logger.Level.ERROR, "update failed", e);
        }
        return false;
    }

    public boolean delete(int id) {
        String sql = "UPDATE dbo.LANGUAGES SET status = 'INACTIVE', last_update = GETDATE() WHERE id = ?";
        Connection conn = DBContext.getInstance().getConnection();
        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, id);
            return ps.executeUpdate() > 0;
        } catch (SQLException e) {
            System.getLogger(LanguageDAO.class.getName()).log(System.Logger.Level.ERROR, "delete failed", e);
        }
        return false;
    }

    private Language map(ResultSet rs) throws SQLException {
        Language l = new Language();
        l.setId(rs.getInt("id"));
        l.setName(EncodingUtil.getString(rs, "name"));
        l.setCode(rs.getString("code"));
        l.setStatus(rs.getString("status"));
        return l;
    }

    private String normalizeCode(String code) {
        return code == null ? null : code.trim().toUpperCase();
    }

    private String normalizeStatus(String status) {
        if (status == null || status.trim().isEmpty()) {
            return "ACTIVE";
        }
        String normalized = status.trim().toUpperCase();
        return "INACTIVE".equals(normalized) ? "INACTIVE" : "ACTIVE";
    }
}
