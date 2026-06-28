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
    List<Language> list = new ArrayList<>();

    String sql =
        "SELECT l.id, l.name, l.code, l.status, l.last_update, ml.subtitle " +
        "FROM MOVIE_LANGUAGES ml " +
        "JOIN LANGUAGES l ON l.id = ml.language_id " +
        "WHERE ml.movie_id = ? " +
        "ORDER BY l.name";

    try (Connection conn = DBContext.getInstance().getConnection();
         PreparedStatement ps = conn.prepareStatement(sql)) {

        ps.setInt(1, movieId);

        try (ResultSet rs = ps.executeQuery()) {
            while (rs.next()) {
                Language lang = new Language();
                lang.setId(rs.getInt("id"));
                lang.setName(rs.getString("name"));
                lang.setCode(rs.getString("code"));
                lang.setStatus(rs.getString("status"));

                list.add(lang);
            }
        }

    } catch (Exception e) {
        e.printStackTrace();
    }

    return list;
}
}
