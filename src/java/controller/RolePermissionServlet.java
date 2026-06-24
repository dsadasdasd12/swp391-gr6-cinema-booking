package controller;

import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import java.io.IOException;
import java.util.*;

@WebServlet("/admin/accounts/roles")
public class RolePermissionServlet extends HttpServlet {

    private static final List<RoleMock> roles = new ArrayList<>();
    private static final List<ModuleMock> modules = new ArrayList<>();
    private static final Map<Integer, Map<String, PermissionSet>> permissionsMatrix = new HashMap<>();
    private static int nextRoleId = 4;

    static {
        // Initialize Default Roles
        roles.add(new RoleMock(1, "ADMIN", "Quản lý cấp cao, toàn quyền tất cả chi nhánh", "ALL_BRANCHES"));
        roles.add(new RoleMock(2, "QUẢN LÝ CHI NHÁNH", "Trưởng chi nhánh được phân công", "ASSIGNED_BRANCH"));
        roles.add(new RoleMock(3, "NHÂN VIÊN RẠP", "Vận hành kỹ thuật, soát vé tại chi nhánh", "ASSIGNED_BRANCH"));

        // Initialize System Modules
        modules.add(new ModuleMock("movies", "Quản lý phim", "Danh sách phim, thêm mới, cập nhật, xóa phim, upload poster và trailer"));
        modules.add(new ModuleMock("showtimes", "Quản lý suất chiếu", "Lên lịch chiếu phim, phân phòng chiếu, quản lý giá vé cơ bản"));
        modules.add(new ModuleMock("tickets", "Quản lý vé & Booking", "Thực hiện đặt vé tại quầy trực tiếp, xuất hóa đơn vé, in vé"));
        modules.add(new ModuleMock("reports", "Báo cáo thống kê", "Xem doanh thu ngày, doanh số rạp, tỷ lệ lấp đầy, giờ cao điểm"));
        modules.add(new ModuleMock("accounts", "Tài khoản nhân sự", "Quản trị danh sách nhân viên, trưởng rạp, phân vai trò"));
        modules.add(new ModuleMock("settings", "Cài đặt hệ thống", "Cấu hình rạp chiếu, cấu hình SMTP Server, bảo trì hệ thống"));

        // Initialize Permissions for ADMIN (Role 1) - All True
        Map<String, PermissionSet> adminPerms = new HashMap<>();
        for (ModuleMock m : modules) {
            adminPerms.put(m.getKey(), new PermissionSet(true, true, true, true, true, true));
        }
        permissionsMatrix.put(1, adminPerms);

        // Initialize Permissions for MANAGER (Role 2) - Limited
        Map<String, PermissionSet> managerPerms = new HashMap<>();
        managerPerms.put("movies", new PermissionSet(true, false, false, false, false, false));
        managerPerms.put("showtimes", new PermissionSet(true, true, true, false, false, false));
        managerPerms.put("tickets", new PermissionSet(true, true, true, false, false, false));
        managerPerms.put("reports", new PermissionSet(true, false, false, false, true, false));
        managerPerms.put("accounts", new PermissionSet(true, false, false, false, false, false));
        managerPerms.put("settings", new PermissionSet(false, false, false, false, false, false));
        permissionsMatrix.put(2, managerPerms);

        // Initialize Permissions for STAFF (Role 3)
        Map<String, PermissionSet> staffPerms = new HashMap<>();
        staffPerms.put("movies", new PermissionSet(true, false, false, false, false, false));
        staffPerms.put("showtimes", new PermissionSet(true, false, false, false, false, false));
        staffPerms.put("tickets", new PermissionSet(true, true, true, false, false, false));
        staffPerms.put("reports", new PermissionSet(false, false, false, false, false, false));
        staffPerms.put("accounts", new PermissionSet(false, false, false, false, false, false));
        staffPerms.put("settings", new PermissionSet(false, false, false, false, false, false));
        permissionsMatrix.put(3, staffPerms);
    }

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp)
            throws ServletException, IOException {
        String roleIdStr = req.getParameter("roleId");
        int selectedRoleId = 1;
        if (roleIdStr != null && !roleIdStr.trim().isEmpty()) {
            try {
                selectedRoleId = Integer.parseInt(roleIdStr);
            } catch (NumberFormatException ignored) {}
        }

        // Find Selected Role
        RoleMock selectedRole = null;
        for (RoleMock r : roles) {
            if (r.getId() == selectedRoleId) {
                selectedRole = r;
                break;
            }
        }
        if (selectedRole == null && !roles.isEmpty()) {
            selectedRole = roles.get(0);
            selectedRoleId = selectedRole.getId();
        }

        // Ensure permission mapping exists
        if (!permissionsMatrix.containsKey(selectedRoleId)) {
            Map<String, PermissionSet> emptyPerms = new HashMap<>();
            for (ModuleMock m : modules) {
                emptyPerms.put(m.getKey(), new PermissionSet(false, false, false, false, false, false));
            }
            permissionsMatrix.put(selectedRoleId, emptyPerms);
        }

        int page = parsePage(req.getParameter("page"));
        int pageSize = parsePageSize(req.getParameter("pageSize"));
        long totalItems = roles.size();
        int totalPages = (int) Math.ceil((double) totalItems / pageSize);
        if (totalPages < 1) totalPages = 1;

        int start = (page - 1) * pageSize;
        int end = Math.min(start + pageSize, roles.size());
        if (start < 0) start = 0;
        if (end < start) end = start;

        List<RoleMock> rolesPage = roles.subList(start, end);

        req.setAttribute("rolesPage", rolesPage);
        req.setAttribute("currentPage", page);
        req.setAttribute("totalPages", totalPages);
        req.setAttribute("totalItems", totalItems);
        req.setAttribute("pageSize", pageSize);

        req.setAttribute("modules", modules);
        req.setAttribute("selectedRole", selectedRole);
        req.setAttribute("selectedRolePermissions", permissionsMatrix.get(selectedRoleId));

        req.getRequestDispatcher("/pages/accounts/roles.jsp").forward(req, resp);
    }

    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse resp)
            throws ServletException, IOException {
        req.setCharacterEncoding("UTF-8");
        String action = req.getParameter("action");
        String roleIdStr = req.getParameter("roleId");
        int roleId = 0;
        if (roleIdStr != null && !roleIdStr.trim().isEmpty()) {
            try {
                roleId = Integer.parseInt(roleIdStr);
            } catch (NumberFormatException ignored) {}
        }

        if ("add-role".equalsIgnoreCase(action)) {
            String name = req.getParameter("name");
            String description = req.getParameter("description");
            String scope = req.getParameter("scope");

            RoleMock newRole = new RoleMock(nextRoleId++, name, description, scope);
            roles.add(newRole);

            // Initialize permissions
            Map<String, PermissionSet> emptyPerms = new HashMap<>();
            for (ModuleMock m : modules) {
                emptyPerms.put(m.getKey(), new PermissionSet(false, false, false, false, false, false));
            }
            permissionsMatrix.put(newRole.getId(), emptyPerms);

            req.getSession().setAttribute("flashSuccess", "Tạo nhóm vai trò '" + name + "' thành công!");
            resp.sendRedirect(req.getContextPath() + "/admin/accounts/roles?roleId=" + newRole.getId());
            return;

        } else if ("update-role-info".equalsIgnoreCase(action)) {
            String name = req.getParameter("name");
            String description = req.getParameter("description");
            String scope = req.getParameter("scope");

            for (RoleMock r : roles) {
                if (r.getId() == roleId) {
                    if (roleId > 3) { // only editable for custom roles
                        r.setName(name);
                        r.setDescription(description);
                        r.setScope(scope);
                    }
                    break;
                }
            }
            req.getSession().setAttribute("flashSuccess", "Cập nhật thông tin vai trò thành công!");

        } else if ("update-permissions".equalsIgnoreCase(action)) {
            String[] permissionParams = req.getParameterValues("permissions");
            Set<String> checkedPerms = new HashSet<>();
            if (permissionParams != null) {
                checkedPerms.addAll(Arrays.asList(permissionParams));
            }

            Map<String, PermissionSet> rolePerms = permissionsMatrix.get(roleId);
            if (rolePerms == null) {
                rolePerms = new HashMap<>();
                permissionsMatrix.put(roleId, rolePerms);
            }

            for (ModuleMock m : modules) {
                String key = m.getKey();
                PermissionSet ps = new PermissionSet(
                    checkedPerms.contains(key + ":view"),
                    checkedPerms.contains(key + ":create"),
                    checkedPerms.contains(key + ":edit"),
                    checkedPerms.contains(key + ":delete"),
                    checkedPerms.contains(key + ":export"),
                    checkedPerms.contains(key + ":manage")
                );
                rolePerms.put(key, ps);
            }

            req.getSession().setAttribute("flashSuccess", "Cập nhật ma trận phân quyền thành công!");

        } else if ("delete-role".equalsIgnoreCase(action)) {
            if (roleId > 3) {
                final int finalRoleId = roleId;
                roles.removeIf(r -> r.getId() == finalRoleId);
                permissionsMatrix.remove(roleId);
                req.getSession().setAttribute("flashSuccess", "Đã xóa nhóm vai trò thành công!");
            } else {
                req.getSession().setAttribute("flashError", "Không thể xóa vai trò mặc định của hệ thống.");
            }
            resp.sendRedirect(req.getContextPath() + "/admin/accounts/roles");
            return;
        }

        resp.sendRedirect(req.getContextPath() + "/admin/accounts/roles?roleId=" + roleId);
    }

    public static class RoleMock {
        private int id;
        private String name;
        private String description;
        private String scope;

        public RoleMock(int id, String name, String description, String scope) {
            this.id = id;
            this.name = name;
            this.description = description;
            this.scope = scope;
        }

        public int getId() { return id; }
        public void setId(int id) { this.id = id; }
        public String getName() { return name; }
        public void setName(String name) { this.name = name; }
        public String getDescription() { return description; }
        public void setDescription(String description) { this.description = description; }
        public String getScope() { return scope; }
        public void setScope(String scope) { this.scope = scope; }
    }

    public static class ModuleMock {
        private String key;
        private String name;
        private String description;

        public ModuleMock(String key, String name, String description) {
            this.key = key;
            this.name = name;
            this.description = description;
        }

        public String getKey() { return key; }
        public String getName() { return name; }
        public String getDescription() { return description; }
    }

    public static class PermissionSet {
        private boolean view;
        private boolean create;
        private boolean edit;
        private boolean delete;
        private boolean export;
        private boolean manage;

        public PermissionSet(boolean view, boolean create, boolean edit, boolean delete, boolean export, boolean manage) {
            this.view = view;
            this.create = create;
            this.edit = edit;
            this.delete = delete;
            this.export = export;
            this.manage = manage;
        }

        public boolean isView() { return view; }
        public boolean isCreate() { return create; }
        public boolean isEdit() { return edit; }
        public boolean isDelete() { return delete; }
        public boolean isExport() { return export; }
        public boolean isManage() { return manage; }
    }

    private static int parsePage(String s) {
        try {
            int v = s == null ? 1 : Integer.parseInt(s.trim());
            return v < 1 ? 1 : v;
        } catch (NumberFormatException e) {
            return 1;
        }
    }

    private static int parsePageSize(String s) {
        try {
            int v = s == null ? 10 : Integer.parseInt(s.trim());
            return v < 1 ? 10 : v;
        } catch (NumberFormatException e) {
            return 10;
        }
    }
}
