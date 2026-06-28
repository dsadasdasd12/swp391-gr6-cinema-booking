package dao;

import java.sql.*;
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
}