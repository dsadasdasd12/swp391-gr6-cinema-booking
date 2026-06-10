package dao;
import java.sql.*;
import model.User;
import util.DBContext;
import util.EncodingUtil;
import util.PasswordUtil;

public class UserDAO {

    public User login(String email, String password) {
        if (email == null || password == null) return null;
        email = email.trim().toLowerCase();

        String sql = "SELECT id, full_name, email, role, active, password_hash "
                   + "FROM dbo.[USER] WHERE LOWER(email)=? AND active=1";
        Connection conn = DBContext.getInstance().getConnection();
        if (conn == null) return null;
        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, email);
            try (ResultSet rs = ps.executeQuery()) {
                if (!rs.next()) return null;
                String storedHash = rs.getString("password_hash");
                if (!PasswordUtil.matches(password, storedHash)) return null;

                User user = new User();
                user.setId(rs.getInt("id"));
                user.setFullName(EncodingUtil.getString(rs, "full_name"));
                user.setEmail(rs.getString("email"));
                user.setRole(rs.getString("role"));
                user.setActive(rs.getBoolean("active"));
                return user;
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }

    public boolean emailExists(String email) {
        if (email == null) return false;
        String sql = "SELECT id FROM dbo.[USER] WHERE LOWER(email)=?";
        Connection conn = DBContext.getInstance().getConnection();
        if (conn == null) return false;
        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, email.trim().toLowerCase());
            return ps.executeQuery().next();
        } catch (Exception e) {
            e.printStackTrace();
        }
        return false;
    }

    public boolean register(User user) {
        String sql = "INSERT INTO dbo.[USER] "
                + "(full_name, email, password_hash, phone, "
                + " role, active, email_verified, created_at, last_update) "
                + "VALUES (?, ?, ?, ?, 'CUSTOMER', 1, 1, GETDATE(), GETDATE())";
        Connection conn = DBContext.getInstance().getConnection();
        if (conn == null) return false;
        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setNString(1, user.getFullName());
            ps.setString(2, user.getEmail() != null ? user.getEmail().trim().toLowerCase() : null);
            ps.setString(3, PasswordUtil.hashPassword(user.getPasswordHash()));
            ps.setString(4, user.getPhone());
            return ps.executeUpdate() > 0;
        } catch (Exception e) {
            e.printStackTrace();
        }
        return false;
    }
}
