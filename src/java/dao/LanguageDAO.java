/*
 * Hệ thống Quản lý Rạp chiếu phim RapViet
 * Module: Duyệt phim - truy xuất dữ liệu bảng dbo.LANGUAGES
 */
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

/**
 * DAO cho ngôn ngữ phim. Dùng để đổ dữ liệu cho ô lọc ngôn ngữ và để nạp các
 * ngôn ngữ / phụ đề của một phim cụ thể.
 *
 * @author LONG
 */
public class LanguageDAO {

    /** Tất cả ngôn ngữ đang hoạt động — cho ô lọc. */
    public List<Language> findAllActive() {
        String sql = "SELECT id, name, code, status "
                + "FROM dbo.LANGUAGES WHERE status = 'ACTIVE' ORDER BY name";
        List<Language> list = new ArrayList<>();
        Connection conn = DBContext.getInstance().getConnection();
        try (PreparedStatement ps = conn.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {
            while (rs.next()) {
                Language l = new Language();
                l.setId(rs.getInt("id"));
                l.setName(EncodingUtil.getString(rs, "name"));
                l.setCode(rs.getString("code"));
                l.setStatus(rs.getString("status"));
                list.add(l);
            }
        } catch (SQLException e) {
            System.getLogger(LanguageDAO.class.getName())
                    .log(System.Logger.Level.ERROR, "findAllActive thất bại", e);
        }
        return list;
    }

    /** Các ngôn ngữ của một phim, kèm cờ phụ đề (subtitle). */
    public List<Language> findByMovieId(int movieId) {
        String sql = "SELECT l.id, l.name(), l.code, l.status, ml.subtitle "
                + "FROM dbo.LANGUAGES l "
                + "JOIN dbo.MOVIE_LANGUAGES ml ON ml.language_id = l.id "
                + "WHERE ml.movie_id = ? ORDER BY l.name()";
        List<Language> list = new ArrayList<>();
        Connection conn = DBContext.getInstance().getConnection();
        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, movieId);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    Language l = new Language();
                    l.setId(rs.getInt("id"));
                    l.setName(EncodingUtil.getString(rs, "name"));
                    l.setCode(rs.getString("code"));
                    l.setStatus(rs.getString("status"));
                    l.setSubtitle(rs.getBoolean("subtitle"));
                    list.add(l);
                }
            }
        } catch (SQLException e) {
            System.getLogger(LanguageDAO.class.getName())
                    .log(System.Logger.Level.ERROR, "findByMovieId thất bại", e);
        }
        return list;
    }
}
