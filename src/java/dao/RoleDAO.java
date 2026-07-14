package dao;

import java.sql.*;
import java.util.*;
import model.Module;
import model.Role;
import util.DBContext;

/**
 * DAO cho ROLES, MODULES, ROLE_PERMISSIONS.
 * Thay thế mock data trong RolePermissionServlet.
 *
 * @author LONG
 */
public class RoleDAO {

    // ═══════════════════════════════════════════════════════
    //  ROLES
    // ═══════════════════════════════════════════════════════

    public List<Role> findAllRoles() {
        String sql = "SELECT id, role_name, description, scope, is_system, created_at "
                   + "FROM dbo.ROLES ORDER BY id";
        List<Role> list = new ArrayList<>();
        Connection conn = DBContext.getInstance().getConnection();
        try (PreparedStatement ps = conn.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {
            while (rs.next()) {
                list.add(mapRole(rs));
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return list;
    }

    public Role findRoleById(int id) {
        String sql = "SELECT id, role_name, description, scope, is_system, created_at "
                   + "FROM dbo.ROLES WHERE id = ?";
        Connection conn = DBContext.getInstance().getConnection();
        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, id);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) return mapRole(rs);
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return null;
    }

    public int insertRole(String roleName, String description, String scope) {
        String sql = "INSERT INTO dbo.ROLES (role_name, description, scope, is_system) "
                   + "VALUES (?, ?, ?, 0)";
        Connection conn = DBContext.getInstance().getConnection();
        try (PreparedStatement ps = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            ps.setNString(1, roleName);
            ps.setNString(2, description);
            ps.setNString(3, scope != null ? scope : "Toàn hệ thống (All Branches)");
            if (ps.executeUpdate() > 0) {
                try (ResultSet keys = ps.getGeneratedKeys()) {
                    if (keys.next()) {
                        int newId = keys.getInt(1);
                        initPermissionsForRole(newId);
                        return newId;
                    }
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return 0;
    }

    public boolean updateRoleInfo(int id, String roleName, String description, String scope) {
        String sql = "UPDATE dbo.ROLES SET role_name = ?, description = ?, scope = ? WHERE id = ? AND is_system = 0";
        Connection conn = DBContext.getInstance().getConnection();
        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setNString(1, roleName);
            ps.setNString(2, description);
            ps.setNString(3, scope);
            ps.setInt(4, id);
            return ps.executeUpdate() > 0;
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return false;
    }

    public boolean deleteRole(int id) {
        // ON DELETE CASCADE sẽ tự xóa ROLE_PERMISSIONS
        String sql = "DELETE FROM dbo.ROLES WHERE id = ? AND is_system = 0";
        Connection conn = DBContext.getInstance().getConnection();
        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, id);
            return ps.executeUpdate() > 0;
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return false;
    }

    // ═══════════════════════════════════════════════════════
    //  MODULES
    // ═══════════════════════════════════════════════════════

    public List<Module> findAllModules() {
        String sql = "SELECT id, module_key, module_name, description, sort_order "
                   + "FROM dbo.MODULES ORDER BY sort_order";
        List<Module> list = new ArrayList<>();
        Connection conn = DBContext.getInstance().getConnection();
        try (PreparedStatement ps = conn.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {
            while (rs.next()) {
                list.add(mapModule(rs));
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return list;
    }

    // ═══════════════════════════════════════════════════════
    //  PERMISSIONS MATRIX
    // ═══════════════════════════════════════════════════════

    /**
     * Lấy ma trận quyền cho 1 role.
     * Key: moduleKey, Value: boolean[6] = {view, create, edit, delete, export, manage}
     */
    public Map<String, boolean[]> getPermissionMatrix(int roleId) {
        String sql = "SELECT m.module_key, rp.can_view, rp.can_create, rp.can_edit, "
                   + "       rp.can_delete, rp.can_export, rp.can_manage "
                   + "FROM dbo.ROLE_PERMISSIONS rp "
                   + "JOIN dbo.MODULES m ON m.id = rp.module_id "
                   + "WHERE rp.role_id = ? "
                   + "ORDER BY m.sort_order";
        Map<String, boolean[]> matrix = new LinkedHashMap<>();
        Connection conn = DBContext.getInstance().getConnection();
        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, roleId);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    boolean[] perms = {
                        rs.getBoolean("can_view"),
                        rs.getBoolean("can_create"),
                        rs.getBoolean("can_edit"),
                        rs.getBoolean("can_delete"),
                        rs.getBoolean("can_export"),
                        rs.getBoolean("can_manage")
                    };
                    matrix.put(rs.getString("module_key"), perms);
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return matrix;
    }

    /**
     * Lưu ma trận quyền cho 1 role.
     * @param permsSet Set chứa các chuỗi dạng "moduleKey:action" (vd: "movies:view", "tickets:edit")
     */
    public boolean savePermissionMatrix(int roleId, Set<String> permsSet) {
        String sql = "UPDATE dbo.ROLE_PERMISSIONS SET "
                   + "can_view = ?, can_create = ?, can_edit = ?, "
                   + "can_delete = ?, can_export = ?, can_manage = ? "
                   + "WHERE role_id = ? AND module_id = (SELECT id FROM dbo.MODULES WHERE module_key = ?)";
        Connection conn = DBContext.getInstance().getConnection();
        List<Module> modules = findAllModules();
        try {
            for (Module m : modules) {
                String key = m.getModuleKey();
                try (PreparedStatement ps = conn.prepareStatement(sql)) {
                    ps.setBoolean(1, permsSet.contains(key + ":view"));
                    ps.setBoolean(2, permsSet.contains(key + ":create"));
                    ps.setBoolean(3, permsSet.contains(key + ":edit"));
                    ps.setBoolean(4, permsSet.contains(key + ":delete"));
                    ps.setBoolean(5, permsSet.contains(key + ":export"));
                    ps.setBoolean(6, permsSet.contains(key + ":manage"));
                    ps.setInt(7, roleId);
                    ps.setString(8, key);
                    ps.executeUpdate();
                }
            }
            return true;
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return false;
    }

    /**
     * Kiểm tra 1 role có quyền cụ thể trên 1 module hay không.
     * Dùng cho RolePermissionFilter.
     */
    public boolean hasPermission(String roleName, String moduleKey, String action) {
        String col = switch (action) {
            case "view"   -> "can_view";
            case "create" -> "can_create";
            case "edit"   -> "can_edit";
            case "delete" -> "can_delete";
            case "export" -> "can_export";
            case "manage" -> "can_manage";
            default       -> null;
        };
        if (col == null) return false;

        String sql = "SELECT " + col + " FROM dbo.ROLE_PERMISSIONS rp "
                   + "JOIN dbo.ROLES r ON r.id = rp.role_id "
                   + "JOIN dbo.MODULES m ON m.id = rp.module_id "
                   + "WHERE r.role_name = ? AND m.module_key = ?";
        Connection conn = DBContext.getInstance().getConnection();
        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, roleName);
            ps.setString(2, moduleKey);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) return rs.getBoolean(1);
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return false;
    }

    // ═══════════════════════════════════════════════════════
    //  PRIVATE HELPERS
    // ═══════════════════════════════════════════════════════

    /** Tạo 1 bộ ROLE_PERMISSIONS trống cho role mới (1 row mỗi module). */
    private void initPermissionsForRole(int roleId) {
        String sql = "INSERT INTO dbo.ROLE_PERMISSIONS (role_id, module_id) "
                   + "SELECT ?, id FROM dbo.MODULES "
                   + "WHERE id NOT IN (SELECT module_id FROM dbo.ROLE_PERMISSIONS WHERE role_id = ?)";
        Connection conn = DBContext.getInstance().getConnection();
        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, roleId);
            ps.setInt(2, roleId);
            ps.executeUpdate();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    private Role mapRole(ResultSet rs) throws SQLException {
        Role r = new Role();
        r.setId(rs.getInt("id"));
        r.setRoleName(util.EncodingUtil.getString(rs, "role_name"));
        r.setDescription(util.EncodingUtil.getString(rs, "description"));
        r.setScope(util.EncodingUtil.getString(rs, "scope"));
        r.setSystem(rs.getBoolean("is_system"));
        Timestamp ts = rs.getTimestamp("created_at");
        if (ts != null) r.setCreatedAt(ts.toLocalDateTime());
        return r;
    }

    private Module mapModule(ResultSet rs) throws SQLException {
        Module m = new Module();
        m.setId(rs.getInt("id"));
        m.setModuleKey(rs.getString("module_key"));
        m.setModuleName(util.EncodingUtil.getString(rs, "module_name"));
        m.setDescription(util.EncodingUtil.getString(rs, "description"));
        m.setSortOrder(rs.getInt("sort_order"));
        return m;
    }
}
