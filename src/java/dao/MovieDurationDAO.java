/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */

package dao;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import model.Movie;
import util.DBContext;

/**
 * Thực hiện các thao tác database cho chức năng
 * quản lý thời lượng phim của Branch Manager.
 */
public class MovieDurationDAO {

    /**
     * Lấy toàn bộ phim để hiển thị trên màn hình
     * quản lý thời lượng.
     */
    public List<Movie> findAllForDurationManagement() {
        String sql
                = "SELECT id, title, duration_min, status, "
                + "poster_url, last_update "
                + "FROM dbo.MOVIES "
                + "ORDER BY title ASC";

        List<Movie> movies = new ArrayList<>();

        Connection conn
                = DBContext.getInstance().getConnection();

        if (conn == null) {
            return movies;
        }

        try (PreparedStatement ps = conn.prepareStatement(sql);
                ResultSet rs = ps.executeQuery()) {

            while (rs.next()) {
                movies.add(mapMovie(rs));
            }

        } catch (SQLException e) {
            System.getLogger(MovieDurationDAO.class.getName())
                    .log(
                            System.Logger.Level.ERROR,
                            "Không thể lấy danh sách thời lượng phim.",
                            e
                    );
        }

        return movies;
    }

    /**
     * Tìm một phim theo ID.
     *
     * Dùng để kiểm tra phim tồn tại trước khi cập nhật.
     */
    public Movie findById(int movieId) {
        String sql
                = "SELECT id, title, duration_min, status, "
                + "poster_url, last_update "
                + "FROM dbo.MOVIES "
                + "WHERE id = ?";

        Connection conn
                = DBContext.getInstance().getConnection();

        if (conn == null) {
            return null;
        }

        try (PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setInt(1, movieId);

            try (ResultSet rs = ps.executeQuery()) {

                if (rs.next()) {
                    return mapMovie(rs);
                }
            }

        } catch (SQLException e) {
            System.getLogger(MovieDurationDAO.class.getName())
                    .log(
                            System.Logger.Level.ERROR,
                            "Không thể tìm phim theo ID.",
                            e
                    );
        }

        return null;
    }

    /**
     * Cập nhật thời lượng của phim.
     *
     * last_update được cập nhật cùng thời điểm sửa thời lượng.
     */
    public boolean updateDuration(
            int movieId,
            int durationMin) {

        String sql
                = "UPDATE dbo.MOVIES "
                + "SET duration_min = ?, "
                + "last_update = GETDATE() "
                + "WHERE id = ?";

        Connection conn
                = DBContext.getInstance().getConnection();

        if (conn == null) {
            return false;
        }

        try (PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setInt(1, durationMin);
            ps.setInt(2, movieId);

            return ps.executeUpdate() > 0;

        } catch (SQLException e) {
            System.getLogger(MovieDurationDAO.class.getName())
                    .log(
                            System.Logger.Level.ERROR,
                            "Không thể cập nhật thời lượng phim.",
                            e
                    );
        }

        return false;
    }

    /**
     * Chuyển dữ liệu ResultSet thành đối tượng Movie.
     *
     * Màn hình này chỉ cần các trường:
     * id, title, duration, status, poster và lastUpdate.
     */
    private Movie mapMovie(ResultSet rs)
            throws SQLException {

        Movie movie = new Movie();

        movie.setId(rs.getInt("id"));
        movie.setTitle(rs.getString("title"));
        movie.setDurationMin(
                rs.getInt("duration_min")
        );
        movie.setStatus(
                rs.getString("status")
        );
        movie.setPosterUrl(
                rs.getString("poster_url")
        );
        movie.setLastUpdate(
                rs.getObject(
                        "last_update",
                        LocalDateTime.class
                )
        );

        return movie;
    }
}

