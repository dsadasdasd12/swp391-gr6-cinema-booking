package dao;

import model.ManagedUser;
import model.User;
import util.DBContext;
import util.EncodingUtil;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.sql.Date;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;

/**
 * Truy vấn tài khoản CMS (khách hàng / nhân viên) — tách khỏi {@link UserDAO}
 * auth của Trường.
 */
public class AdminUserDAO {

    private static final DateTimeFormatter JSP_DATETIME
            = DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm");

    public ManagedUser findById(int id) {
        String sql = "SELECT u.id, u.full_name, u.email, u.phone, u.role, u.active, u.email_verified, u.created_at, "
                + "       sb.branch_id, b.name() AS branch_name "
                + "FROM dbo.[USER] u "
                + "LEFT JOIN dbo.STAFF_BRANCH sb ON u.id = sb.user_id "
                + "LEFT JOIN dbo.BRANCHES b ON sb.branch_id = b.id "
                + "WHERE u.id = ?";
        Connection conn = DBContext.getInstance().getConnection();
        if (conn == null) {
            return null;
        }
        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, id);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return mapFull(rs);
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return null;
    }

    public List<ManagedUser> findCustomersPaged(String keyword, String status, LocalDate createdDate, int offset, int limit) {
        List<ManagedUser> list = new ArrayList<>();
        StringBuilder sql = new StringBuilder(
                "SELECT id, full_name, email, phone, role, active, email_verified, created_at "
                + "FROM dbo.[USER] WHERE role = 'CUSTOMER' "
        );
        List<Object> params = new ArrayList<>();

        if (keyword != null && !keyword.trim().isEmpty()) {
            sql.append("AND (full_name LIKE ? OR email LIKE ? OR phone LIKE ?) ");
            String k = "%" + keyword.trim() + "%";
            params.add(k);
            params.add(k);
            params.add(k);
        }
        if (status != null && !status.trim().isEmpty()) {
            if ("ACTIVE".equalsIgnoreCase(status)) {
                sql.append("AND active = 1 AND email_verified = 1 ");
            } else if ("BLOCKED".equalsIgnoreCase(status)) {
                sql.append("AND active = 0 ");
            } else if ("PENDING".equalsIgnoreCase(status)) {
                sql.append("AND active = 1 AND email_verified = 0 ");
            }
        }
        if (createdDate != null) {
            // Half-open interval includes every time in the selected day and remains index-friendly.
            sql.append("AND created_at >= ? AND created_at < DATEADD(DAY, 1, ?) ");
            Date selectedDate = Date.valueOf(createdDate);
            params.add(selectedDate);
            params.add(selectedDate);
        }
        sql.append("ORDER BY created_at DESC OFFSET ? ROWS FETCH NEXT ? ROWS ONLY");
        params.add(offset);
        params.add(limit);

        Connection conn = DBContext.getInstance().getConnection();
        if (conn == null) {
            return list;
        }
        try (PreparedStatement ps = conn.prepareStatement(sql.toString())) {
            for (int i = 0; i < params.size(); i++) {
                ps.setObject(i + 1, params.get(i));
            }
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    list.add(mapCustomer(rs));
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return list;
    }

    public int countCustomers(String keyword, String status, LocalDate createdDate) {
        StringBuilder sql = new StringBuilder(
                "SELECT COUNT(*) FROM dbo.[USER] WHERE role = 'CUSTOMER' "
        );
        List<Object> params = new ArrayList<>();
        if (keyword != null && !keyword.trim().isEmpty()) {
            sql.append("AND (full_name LIKE ? OR email LIKE ? OR phone LIKE ?) ");
            String k = "%" + keyword.trim() + "%";
            params.add(k);
            params.add(k);
            params.add(k);
        }
        if (status != null && !status.trim().isEmpty()) {
            if ("ACTIVE".equalsIgnoreCase(status)) {
                sql.append("AND active = 1 AND email_verified = 1 ");
            } else if ("BLOCKED".equalsIgnoreCase(status)) {
                sql.append("AND active = 0 ");
            } else if ("PENDING".equalsIgnoreCase(status)) {
                sql.append("AND active = 1 AND email_verified = 0 ");
            }
        }
        if (createdDate != null) {
            sql.append("AND created_at >= ? AND created_at < DATEADD(DAY, 1, ?) ");
            Date selectedDate = Date.valueOf(createdDate);
            params.add(selectedDate);
            params.add(selectedDate);
        }
        Connection conn = DBContext.getInstance().getConnection();
        if (conn == null) {
            return 0;
        }
        try (PreparedStatement ps = conn.prepareStatement(sql.toString())) {
            for (int i = 0; i < params.size(); i++) {
                ps.setObject(i + 1, params.get(i));
            }
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return rs.getInt(1);
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return 0;
    }

    public List<ManagedUser> findStaffPaged(String keyword, Integer roleId, Integer branchId, int offset, int limit) {
        List<ManagedUser> list = new ArrayList<>();
        StringBuilder sql = new StringBuilder(
                "SELECT u.id, u.full_name, u.email, u.phone, u.role, u.active, u.created_at, "
                + "       sb.branch_id, b.name AS branch_name "
                + "FROM dbo.[USER] u "
                + "LEFT JOIN dbo.STAFF_BRANCH sb ON u.id = sb.user_id "
                + "LEFT JOIN dbo.BRANCHES b ON sb.branch_id = b.id "
                + "WHERE u.role IN ('ADMIN','MANAGER','STAFF') "
        );
        List<Object> params = new ArrayList<>();
        if (keyword != null && !keyword.trim().isEmpty()) {
            sql.append("AND (u.full_name LIKE ? OR u.email LIKE ?) ");
            String k = "%" + keyword.trim() + "%";
            params.add(k);
            params.add(k);
        }
        if (roleId != null && roleId > 0) {
            sql.append("AND u.role = ? ");
            params.add(mapRoleIdToName(roleId));
        }
        if (branchId != null && branchId > 0) {
            sql.append("AND sb.branch_id = ? ");
            params.add(branchId);
        }
        sql.append("ORDER BY u.created_at DESC OFFSET ? ROWS FETCH NEXT ? ROWS ONLY");
        params.add(offset);
        params.add(limit);

        Connection conn = DBContext.getInstance().getConnection();
        if (conn == null) {
            return list;
        }
        try (PreparedStatement ps = conn.prepareStatement(sql.toString())) {
            for (int i = 0; i < params.size(); i++) {
                ps.setObject(i + 1, params.get(i));
            }
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    list.add(mapFull(rs));
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return list;
    }

    public int countStaff(String keyword, Integer roleId, Integer branchId) {
        StringBuilder sql = new StringBuilder(
                "SELECT COUNT(*) FROM dbo.[USER] u "
                + "LEFT JOIN dbo.STAFF_BRANCH sb ON u.id = sb.user_id "
                + "WHERE u.role IN ('ADMIN','MANAGER','STAFF') "
        );
        List<Object> params = new ArrayList<>();
        if (keyword != null && !keyword.trim().isEmpty()) {
            sql.append("AND (u.full_name LIKE ? OR u.email LIKE ?) ");
            String k = "%" + keyword.trim() + "%";
            params.add(k);
            params.add(k);
        }
        if (roleId != null && roleId > 0) {
            sql.append("AND u.role = ? ");
            params.add(mapRoleIdToName(roleId));
        }
        if (branchId != null && branchId > 0) {
            sql.append("AND sb.branch_id = ? ");
            params.add(branchId);
        }
        Connection conn = DBContext.getInstance().getConnection();
        if (conn == null) {
            return 0;
        }
        try (PreparedStatement ps = conn.prepareStatement(sql.toString())) {
            for (int i = 0; i < params.size(); i++) {
                ps.setObject(i + 1, params.get(i));
            }
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return rs.getInt(1);
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return 0;
    }
    /**
     * Kiểm tra chi nhánh đã có MANAGER khác hay chưa.
     */
    public boolean hasBranchManager(int branchId, int excludeUserId) {
        if (branchId <= 0) {
            return false;
        }

        try (Connection conn = DBContext.getInstance().getConnection()) {
            return hasBranchManager(conn, branchId, excludeUserId, false);
        } catch (SQLException e) {
            e.printStackTrace();
            return true;
        }
    }

    /**
     * Khi lock=true, UPDLOCK + HOLDLOCK giúp chặn hai request đồng thời
     * cùng thêm MANAGER vào một chi nhánh.
     */
    private boolean hasBranchManager(
            Connection conn,
            int branchId,
            int excludeUserId,
            boolean lock
    ) throws SQLException {

        String lockHint = lock ? " WITH (UPDLOCK, HOLDLOCK) " : " ";

        String sql = "SELECT COUNT(*) "
                + "FROM dbo.STAFF_BRANCH sb" + lockHint
                + "JOIN dbo.[USER] u ON u.id = sb.user_id "
                + "WHERE sb.branch_id = ? "
                + "AND u.role = 'MANAGER' "
                + "AND u.id <> ?";

        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, branchId);
            ps.setInt(2, excludeUserId);

            try (ResultSet rs = ps.executeQuery()) {
                return rs.next() && rs.getInt(1) > 0;
            }
        }
    }

    /**
     * Tạo tài khoản và phân công chi nhánh trong cùng transaction.
     *
     * Return:
     *  > 0: user id mới
     *   -2: chi nhánh đã có MANAGER
     *    0: lỗi dữ liệu/SQL
     */
    public int insertStaff(User u, String passwordHash, int branchId) {
        String insertUserSql
                = "INSERT INTO dbo.[USER] "
                + "(full_name, email, password_hash, phone, role, google_id, "
                + "active, email_verified, created_at, last_update) "
                + "VALUES (?, ?, ?, ?, ?, ?, 1, 1, GETDATE(), GETDATE())";

        String insertBranchSql
                = "INSERT INTO dbo.STAFF_BRANCH "
                + "(user_id, branch_id, position, assigned_at) "
                + "VALUES (?, ?, ?, GETDATE())";

        Connection conn = null;

        try {
            conn = DBContext.getInstance().getConnection();

            if (conn == null) {
                return 0;
            }

            conn.setAutoCommit(false);
            conn.setTransactionIsolation(Connection.TRANSACTION_SERIALIZABLE);

            String role = u.getRole() == null
                    ? "STAFF"
                    : u.getRole().trim().toUpperCase();

            if ("MANAGER".equals(role)
                    && hasBranchManager(conn, branchId, 0, true)) {
                conn.rollback();
                return -2;
            }

            int userId;

            try (PreparedStatement ps = conn.prepareStatement(
                    insertUserSql,
                    PreparedStatement.RETURN_GENERATED_KEYS
            )) {
                ps.setNString(1, u.getFullName());
                ps.setString(2, u.getEmail() == null
                        ? null
                        : u.getEmail().trim().toLowerCase());
                ps.setString(3, passwordHash);
                ps.setString(4, u.getPhone() == null ? "" : u.getPhone());
                ps.setString(5, role);
                ps.setString(6, u.getGoogleId());

                if (ps.executeUpdate() != 1) {
                    conn.rollback();
                    return 0;
                }

                try (ResultSet rs = ps.getGeneratedKeys()) {
                    if (!rs.next()) {
                        conn.rollback();
                        return 0;
                    }

                    userId = rs.getInt(1);
                }
            }

            if (branchId > 0) {
                try (PreparedStatement ps = conn.prepareStatement(insertBranchSql)) {
                    ps.setInt(1, userId);
                    ps.setInt(2, branchId);
                    ps.setString(3, "MANAGER".equals(role)
                            ? "MANAGER"
                            : "STAFF");

                    if (ps.executeUpdate() != 1) {
                        conn.rollback();
                        return 0;
                    }
                }
            }

            conn.commit();
            return userId;
        } catch (SQLException e) {
            e.printStackTrace();

            if (conn != null) {
                try {
                    conn.rollback();
                } catch (SQLException ignored) {
                }
            }

            return 0;
        } finally {
            if (conn != null) {
                try {
                    conn.setAutoCommit(true);
                    conn.close();
                } catch (SQLException ignored) {
                }
            }
        }
    }

    /**
     * Cập nhật role và chi nhánh trong cùng transaction.
     * DAO kiểm tra lại manager để không thể bỏ qua validation ở Controller.
     */
    public boolean updateStaffInfo(
            int userId,
            String fullName,
            String role,
            String phone,
            int branchId,
            String status
    ) {
        boolean active = !"BLOCKED".equalsIgnoreCase(status);

        String updateUserSql
                = "UPDATE dbo.[USER] "
                + "SET full_name = ?, role = ?, phone = ?, active = ?, "
                + "last_update = GETDATE() "
                + "WHERE id = ?";

        String deleteBranchSql
                = "DELETE FROM dbo.STAFF_BRANCH WHERE user_id = ?";

        String insertBranchSql
                = "INSERT INTO dbo.STAFF_BRANCH "
                + "(user_id, branch_id, position, assigned_at) "
                + "VALUES (?, ?, ?, GETDATE())";

        Connection conn = null;

        try {
            conn = DBContext.getInstance().getConnection();

            if (conn == null) {
                return false;
            }

            conn.setAutoCommit(false);
            conn.setTransactionIsolation(Connection.TRANSACTION_SERIALIZABLE);

            String normalizedRole = role == null
                    ? "STAFF"
                    : role.trim().toUpperCase();

            if ("MANAGER".equals(normalizedRole)
                    && hasBranchManager(conn, branchId, userId, true)) {
                conn.rollback();
                return false;
            }

            try (PreparedStatement ps = conn.prepareStatement(updateUserSql)) {
                ps.setNString(1, fullName);
                ps.setString(2, normalizedRole);
                ps.setString(3, phone == null ? "" : phone);
                ps.setBoolean(4, active);
                ps.setInt(5, userId);

                if (ps.executeUpdate() != 1) {
                    conn.rollback();
                    return false;
                }
            }

            try (PreparedStatement ps = conn.prepareStatement(deleteBranchSql)) {
                ps.setInt(1, userId);
                ps.executeUpdate();
            }

            if (branchId > 0) {
                try (PreparedStatement ps = conn.prepareStatement(insertBranchSql)) {
                    ps.setInt(1, userId);
                    ps.setInt(2, branchId);
                    ps.setString(3, "MANAGER".equals(normalizedRole)
                            ? "MANAGER"
                            : "STAFF");

                    if (ps.executeUpdate() != 1) {
                        conn.rollback();
                        return false;
                    }
                }
            }

            conn.commit();
            return true;
        } catch (SQLException e) {
            e.printStackTrace();

            if (conn != null) {
                try {
                    conn.rollback();
                } catch (SQLException ignored) {
                }
            }

            return false;
        } finally {
            if (conn != null) {
                try {
                    conn.setAutoCommit(true);
                    conn.close();
                } catch (SQLException ignored) {
                }
            }
        }
    }

    /**
     * Giữ method cũ cho các nơi khác đang gọi.
     * Khi không biết role, mặc định position STAFF.
     */
    public void setStaffBranch(int userId, int branchId) {
        String deleteSql = "DELETE FROM dbo.STAFF_BRANCH WHERE user_id = ?";
        String insertSql
                = "INSERT INTO dbo.STAFF_BRANCH "
                + "(user_id, branch_id, position, assigned_at) "
                + "VALUES (?, ?, 'STAFF', GETDATE())";

        Connection conn = null;

        try {
            conn = DBContext.getInstance().getConnection();

            if (conn == null) {
                return;
            }

            conn.setAutoCommit(false);

            try (PreparedStatement ps = conn.prepareStatement(deleteSql)) {
                ps.setInt(1, userId);
                ps.executeUpdate();
            }

            if (branchId > 0) {
                try (PreparedStatement ps = conn.prepareStatement(insertSql)) {
                    ps.setInt(1, userId);
                    ps.setInt(2, branchId);
                    ps.executeUpdate();
                }
            }

            conn.commit();
        } catch (SQLException e) {
            e.printStackTrace();

            if (conn != null) {
                try {
                    conn.rollback();
                } catch (SQLException ignored) {
                }
            }
        } finally {
            if (conn != null) {
                try {
                    conn.setAutoCommit(true);
                    conn.close();
                } catch (SQLException ignored) {
                }
            }
        }
    }

    public boolean updateActiveStatus(int userId, boolean active) {
        String sql = "UPDATE dbo.[USER] SET active=?, last_update=GETDATE() WHERE id=?";
        Connection conn = DBContext.getInstance().getConnection();
        if (conn == null) {
            return false;
        }
        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setBoolean(1, active);
            ps.setInt(2, userId);
            return ps.executeUpdate() > 0;
        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }

    public boolean updatePassword(int userId, String passwordHash) {
        String sql = "UPDATE dbo.[USER] SET password_hash=?, last_update=GETDATE() WHERE id=?";
        Connection conn = DBContext.getInstance().getConnection();
        if (conn == null) {
            return false;
        }
        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, passwordHash);
            ps.setInt(2, userId);
            return ps.executeUpdate() > 0;
        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }

    public boolean deleteUser(int userId) {
        String delSB = "DELETE FROM dbo.STAFF_BRANCH WHERE user_id = ?";
        String delUser = "DELETE FROM dbo.[USER] WHERE id = ?";
        Connection conn = DBContext.getInstance().getConnection();
        if (conn == null) {
            return false;
        }
        try {
            try (PreparedStatement ps = conn.prepareStatement(delSB)) {
                ps.setInt(1, userId);
                ps.executeUpdate();
            }
            try (PreparedStatement ps = conn.prepareStatement(delUser)) {
                ps.setInt(1, userId);
                return ps.executeUpdate() > 0;
            }
        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }

    private ManagedUser mapCustomer(ResultSet rs) throws SQLException {
        ManagedUser u = new ManagedUser();
        u.setId(rs.getInt("id"));
        u.setFullName(EncodingUtil.getString(rs, "full_name"));
        u.setEmail(rs.getString("email"));
        u.setPhone(rs.getString("phone"));
        u.setRole(rs.getString("role"));
        boolean active = rs.getBoolean("active");
        boolean emailVerified = rs.getBoolean("email_verified");
        u.setActive(active);
        u.setEmailVerified(emailVerified);
        if (!active) {
            u.setStatus("BLOCKED");
        } else if (!emailVerified) {
            u.setStatus("PENDING");
        } else {
            u.setStatus("ACTIVE");
        }
        Timestamp ca = rs.getTimestamp("created_at");
        if (ca != null) {
            u.setCreatedAt(ca.toLocalDateTime());
            u.setCreatedAtLabel(ca.toLocalDateTime().format(JSP_DATETIME));
        }
        return u;
    }

    private ManagedUser mapFull(ResultSet rs) throws SQLException {
        ManagedUser u = new ManagedUser();
        u.setId(rs.getInt("id"));
        u.setFullName(EncodingUtil.getString(rs, "full_name"));
        u.setEmail(rs.getString("email"));
        u.setPhone(rs.getString("phone"));
        String dbRole = rs.getString("role");
        u.setRole(dbRole);
        u.setRoleId(mapNameToRoleId(dbRole));
        try {
            u.setBranchId(rs.getInt("branch_id"));
            u.setBranchName(EncodingUtil.getString(rs, "branch_name"));
        } catch (SQLException ignored) {
            u.setBranchId(0);
        }
        boolean active = rs.getBoolean("active");
        u.setActive(active);
        u.setStatus(active ? "ACTIVE" : "BLOCKED");
        Timestamp ca = rs.getTimestamp("created_at");
        if (ca != null) {
            u.setCreatedAt(ca.toLocalDateTime());
            u.setCreatedAtLabel(ca.toLocalDateTime().format(JSP_DATETIME));
        }
        u.setLastLogin(null);
        return u;
    }

    private int mapNameToRoleId(String role) {
        if ("ADMIN".equalsIgnoreCase(role)) {
            return 1;
        }
        if ("MANAGER".equalsIgnoreCase(role)) {
            return 2;
        }
        if ("STAFF".equalsIgnoreCase(role)) {
            return 3;
        }
        return 3;
    }

    private String mapRoleIdToName(int roleId) {
        if (roleId == 1) {
            return "ADMIN";
        }
        if (roleId == 2) {
            return "MANAGER";
        }
        return "STAFF";
    }
}
