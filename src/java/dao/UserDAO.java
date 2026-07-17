package dao;

import java.sql.*;
import util.PasswordUtil;

import java.util.ArrayList;
import java.util.List;
import model.User;
import util.DBContext;

public class UserDAO {

    public User login(String email, String password) {

        String sql = """
        SELECT *
        FROM [USER]
        WHERE email = ?
        AND active = 1
    """;

        Connection conn = DBContext.getInstance().getConnection();

        try (PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setString(1, email);

            ResultSet rs = ps.executeQuery();

            if (rs.next()) {
                String storedHash = rs.getString("password_hash");

                if (!PasswordUtil.verifyPassword(password, storedHash)) {
                    return null;
                }

                User user = new User();
                user.setId(rs.getInt("id"));
                user.setFullName(rs.getString("full_name"));
                user.setEmail(rs.getString("email"));
                user.setPasswordHash(storedHash);
                user.setGoogleId(rs.getString("google_id"));
                user.setPhone(rs.getString("phone"));
                user.setRole(rs.getString("role"));
                user.setActive(rs.getBoolean("active"));
                user.setEmailVerified(rs.getBoolean("email_verified"));

                return user;
            }

        } catch (Exception e) {
            e.printStackTrace();
        }

        return null;
    }

    public boolean emailExists(String email) {

        String sql = "SELECT id FROM [USER] WHERE email=?";
        Connection conn = DBContext.getInstance().getConnection();
        try (PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setString(1, email);

            return ps.executeQuery().next();

        } catch (Exception e) {
            e.printStackTrace();
        }

        return false;
    }

    public boolean register(User user) {

        String sql = """
    INSERT INTO [USER]
    (
        full_name,
        email,
        password_hash,
        google_id,
        phone,
        role,
        active,
        email_verified,
        created_at,
        last_update
    )
    VALUES
    (
        ?, ?, ?, ?, ?,
        'CUSTOMER',
        1,
        0,
        GETDATE(),
        GETDATE()
    )
""";

        Connection conn = DBContext.getInstance().getConnection();
        try (PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setString(1, user.getFullName());
            ps.setString(2, user.getEmail());
            ps.setString(3, user.getPasswordHash());
            ps.setString(4, user.getGoogleId());
            ps.setString(5, user.getPhone());

            return ps.executeUpdate() > 0;

        } catch (Exception e) {
            e.printStackTrace();
        }

        return false;
    }

    public boolean activateEmail(int userId) {

        String sql = """
        UPDATE [USER]
        SET email_verified = 1,
            last_update = GETDATE()
        WHERE id = ?
    """;

        Connection conn = DBContext.getInstance().getConnection();
        try (PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setInt(1, userId);

            return ps.executeUpdate() > 0;

        } catch (Exception e) {
            e.printStackTrace();
        }

        return false;
    }

    public User getUserByEmail(String email) {

        String sql = """
        SELECT *
        FROM [USER]
        WHERE email = ?
          AND active = 1
    """;

        Connection conn = DBContext.getInstance().getConnection();

        try (PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setString(1, email);

            ResultSet rs = ps.executeQuery();

            if (rs.next()) {
                User user = new User();

                user.setId(rs.getInt("id"));
                user.setFullName(rs.getString("full_name"));
                user.setEmail(rs.getString("email"));
                user.setPasswordHash(rs.getString("password_hash"));
                user.setGoogleId(rs.getString("google_id"));
                user.setPhone(rs.getString("phone"));
                user.setRole(rs.getString("role"));
                user.setActive(rs.getBoolean("active"));
                user.setEmailVerified(rs.getBoolean("email_verified"));

                return user;
            }

        } catch (Exception e) {
            e.printStackTrace();
        }

        return null;
    }

    public boolean updatePassword(int userId, String newPassword) {

        String sql = """
        UPDATE [USER]
        SET password_hash = ?,
            last_update = GETDATE()
        WHERE id = ?
    """;

        Connection conn = DBContext.getInstance().getConnection();

        try (PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setString(1, newPassword);
            ps.setInt(2, userId);

            return ps.executeUpdate() > 0;

        } catch (Exception e) {
            e.printStackTrace();
        }

        return false;
    }

    public boolean updateCustomerProfile(int userId, String fullName, String phone) {

        String sql = """
        UPDATE [USER]
        SET full_name = ?,
            phone = ?,
            last_update = GETDATE()
        WHERE id = ?
    """;

        Connection conn = DBContext.getInstance().getConnection();

        try (PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setString(1, fullName);
            ps.setString(2, phone);
            ps.setInt(3, userId);

            return ps.executeUpdate() > 0;

        } catch (Exception e) {
            e.printStackTrace();
        }

        return false;
    }

    public List<User> getAllUsersExceptAdmin() {

        List<User> list = new ArrayList<>();

        String sql = """
        SELECT *
        FROM [USER]
        WHERE role <> 'ADMIN'
        ORDER BY created_at DESC
    """;

        Connection conn = DBContext.getInstance().getConnection();

        try (PreparedStatement ps = conn.prepareStatement(sql)) {

            ResultSet rs = ps.executeQuery();

            while (rs.next()) {
                User user = new User();

                user.setId(rs.getInt("id"));
                user.setFullName(rs.getString("full_name"));
                user.setEmail(rs.getString("email"));
                user.setPhone(rs.getString("phone"));
                user.setRole(rs.getString("role"));
                user.setActive(rs.getBoolean("active"));
                user.setEmailVerified(rs.getBoolean("email_verified"));
                user.setCreatedAt(rs.getTimestamp("created_at").toLocalDateTime());
                user.setLastUpdate(rs.getTimestamp("last_update").toLocalDateTime());

                list.add(user);
            }

        } catch (Exception e) {
            e.printStackTrace();
        }

        return list;
    }

    public User getUserById(int id) {

        String sql = """
        SELECT *
        FROM [USER]
        WHERE id = ?
    """;

        Connection conn = DBContext.getInstance().getConnection();

        try (PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setInt(1, id);

            ResultSet rs = ps.executeQuery();

            if (rs.next()) {

                User user = new User();

                user.setId(rs.getInt("id"));
                user.setFullName(rs.getString("full_name"));
                user.setEmail(rs.getString("email"));
                user.setPhone(rs.getString("phone"));
                user.setRole(rs.getString("role"));
                user.setActive(rs.getBoolean("active"));
                user.setEmailVerified(rs.getBoolean("email_verified"));

                return user;
            }

        } catch (Exception e) {
            e.printStackTrace();
        }

        return null;
    }

    public boolean toggleActive(int id) {

        String sql = """
        UPDATE [USER]
        SET active =
            CASE
                WHEN active = 1 THEN 0
                ELSE 1
            END,
            last_update = GETDATE()
        WHERE id = ?
    """;

        Connection conn = DBContext.getInstance().getConnection();

        try (PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setInt(1, id);

            return ps.executeUpdate() > 0;

        } catch (Exception e) {
            e.printStackTrace();
        }

        return false;
    }

    public boolean createUserByAdmin(User user) {

        String sql = """
        INSERT INTO [USER]
        (
            full_name,
            email,
            password_hash,
            google_id,
            phone,
            role,
            active,
            email_verified,
            created_at,
            last_update
        )
        VALUES
        (
            ?, ?, ?,
            ?, ?, ?,
            1,
            1,
            GETDATE(),
            GETDATE()
        )
    """;

        Connection conn = DBContext.getInstance().getConnection();

        try (PreparedStatement ps = conn.prepareStatement(sql)) {

            String googleId = "local_" + user.getEmail();

            ps.setString(1, user.getFullName());
            ps.setString(2, user.getEmail());
            ps.setString(3, user.getPasswordHash()); // BCrypt từ UserService
            ps.setString(4, googleId);
            ps.setString(5, user.getPhone());
            ps.setString(6, user.getRole());

            return ps.executeUpdate() > 0;

        } catch (Exception e) {
            e.printStackTrace();
        }

        return false;
    }

    public boolean updateUserByAdmin(User user) {

        String sql = """
        UPDATE [USER]
        SET full_name = ?,
            email = ?,
            phone = ?,
            role = ?,
            last_update = GETDATE()
        WHERE id = ?
    """;

        Connection conn = DBContext.getInstance().getConnection();

        try (PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setString(1, user.getFullName());
            ps.setString(2, user.getEmail());
            ps.setString(3, user.getPhone());
            ps.setString(4, user.getRole());
            ps.setInt(5, user.getId());

            return ps.executeUpdate() > 0;

        } catch (Exception e) {
            e.printStackTrace();
        }

        return false;
    }

    public int getBranchIdOfStaff(int userId) {
        String sql = "SELECT branch_id FROM STAFF_BRANCH WHERE user_id = ?";
        Connection conn = DBContext.getInstance().getConnection();
        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, userId);
            ResultSet rs = ps.executeQuery();
            if (rs.next()) {
                return rs.getInt("branch_id");
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return 1; // Mặc định trả về chi nhánh 1 nếu chưa phân công
    }
}
