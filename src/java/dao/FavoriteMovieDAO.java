package dao;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;
import model.Movie;
import util.DBContext;

public class FavoriteMovieDAO {

    public boolean exists(int userId, int movieId) {
        String sql = "SELECT 1 FROM FAVORITE_MOVIES WHERE user_id = ? AND movie_id = ?";

        try (Connection conn = DBContext.getInstance().getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setInt(1, userId);
            ps.setInt(2, movieId);

            try (ResultSet rs = ps.executeQuery()) {
                return rs.next();
            }

        } catch (Exception e) {
            e.printStackTrace();
        }

        return false;
    }

    public boolean add(int userId, int movieId) {
        String sql = "INSERT INTO FAVORITE_MOVIES (user_id, movie_id, created_at) VALUES (?, ?, GETDATE())";

        
        Connection conn = DBContext.getInstance().getConnection();
        try (
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setInt(1, userId);
            ps.setInt(2, movieId);

            return ps.executeUpdate() > 0;

        } catch (Exception e) {
            e.printStackTrace();
        }

        return false;
    }

    public boolean remove(int userId, int movieId) {
        String sql = "DELETE FROM FAVORITE_MOVIES WHERE user_id = ? AND movie_id = ?";

        try (Connection conn = DBContext.getInstance().getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setInt(1, userId);
            ps.setInt(2, movieId);

            return ps.executeUpdate() > 0;

        } catch (Exception e) {
            e.printStackTrace();
        }

        return false;
    }

    public boolean toggle(int userId, int movieId) {
        System.out.println("[Toggle function] " + exists(userId, movieId));
        if (exists(userId, movieId)) {
            return remove(userId, movieId);
        }
        return add(userId, movieId);
    }
    
    public List<Movie> findByUserId(int userId) {

    List<Movie> movies = new ArrayList<>();

    String sql = """
        SELECT
            m.id,
            m.title,
            m.duration_min,
            m.description,
            m.release_date,
            m.status,
            m.poster_url,
            m.trailer_url,
            m.actor,
            m.director,
            m.last_update
        FROM FAVORITE_MOVIES f
        INNER JOIN MOVIES m
            ON f.movie_id = m.id
        WHERE f.user_id = ?
        ORDER BY m.title
        """;

    
    Connection conn = DBContext.getInstance().getConnection();
    try (
            
            PreparedStatement ps = conn.prepareStatement(sql)
    ) {

        ps.setInt(1, userId);

        ResultSet rs = ps.executeQuery();

        while (rs.next()) {

            Movie movie = new Movie();

            movie.setId(rs.getInt("id"));
            movie.setTitle(rs.getString("title"));
            movie.setDurationMin(rs.getInt("duration_min"));
            movie.setDescription(rs.getString("description"));

            Date release = rs.getDate("release_date");
            if (release != null) {
                movie.setReleaseDate(release.toLocalDate());
            }

            movie.setStatus(rs.getString("status"));
            movie.setPosterUrl(rs.getString("poster_url"));
            movie.setTrailerUrl(rs.getString("trailer_url"));
            movie.setActor(rs.getString("actor"));
            movie.setDirector(rs.getString("director"));

            Timestamp update = rs.getTimestamp("last_update");
            if (update != null) {
                movie.setLastUpdate(update.toLocalDateTime());
            }

            movies.add(movie);
        }

    } catch (Exception e) {
        e.printStackTrace();
    }

    return movies;
}
}