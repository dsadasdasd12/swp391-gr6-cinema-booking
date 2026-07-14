package controller;

import dao.RoleDAO;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import model.Module;
import model.Role;

import java.io.IOException;
import java.util.*;

@WebServlet("/admin/accounts/roles")
public class RolePermissionServlet extends HttpServlet {

    private final RoleDAO roleDAO = new RoleDAO();

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp)
            throws ServletException, IOException {
        String roleIdStr = req.getParameter("roleId");
        int selectedRoleId = 0;
        if (roleIdStr != null && !roleIdStr.trim().isEmpty()) {
            try {
                selectedRoleId = Integer.parseInt(roleIdStr);
            } catch (NumberFormatException ignored) {}
        }

        List<Role> roles = roleDAO.findAllRoles();
        if (roles.isEmpty()) {
            req.getRequestDispatcher("/pages/accounts/roles.jsp").forward(req, resp);
            return;
        }

        Role selectedRole = null;
        if (selectedRoleId > 0) {
            for (Role r : roles) {
                if (r.getId() == selectedRoleId) {
                    selectedRole = r;
                    break;
                }
            }
        }
        if (selectedRole == null) {
            selectedRole = roles.get(0);
            selectedRoleId = selectedRole.getId();
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

        List<Role> rolesPage = roles.subList(start, end);

        List<Module> modules = roleDAO.findAllModules();
        Map<String, boolean[]> selectedRolePermissions = roleDAO.getPermissionMatrix(selectedRoleId);

        // Map boolean array to PermissionSet for JSP compatibility
        Map<String, PermissionSet> permsMap = new HashMap<>();
        for (Module m : modules) {
            boolean[] perms = selectedRolePermissions.getOrDefault(m.getModuleKey(), new boolean[6]);
            permsMap.put(m.getModuleKey(), new PermissionSet(perms[0], perms[1], perms[2], perms[3], perms[4], perms[5]));
        }

        req.setAttribute("rolesPage", rolesPage);
        req.setAttribute("currentPage", page);
        req.setAttribute("totalPages", totalPages);
        req.setAttribute("totalItems", totalItems);
        req.setAttribute("pageSize", pageSize);

        req.setAttribute("modules", modules);
        req.setAttribute("selectedRole", selectedRole);
        req.setAttribute("selectedRolePermissions", permsMap);

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

            int newId = roleDAO.insertRole(name, description, scope);
            if (newId > 0) {
                req.getSession().setAttribute("flashSuccess", "Tạo nhóm vai trò '" + name + "' thành công!");
                resp.sendRedirect(req.getContextPath() + "/admin/accounts/roles?roleId=" + newId);
            } else {
                req.getSession().setAttribute("flashError", "Lỗi: Không thể tạo vai trò. Có thể tên đã tồn tại.");
                resp.sendRedirect(req.getContextPath() + "/admin/accounts/roles");
            }
            return;

        } else if ("update-role-info".equalsIgnoreCase(action)) {
            String name = req.getParameter("name");
            String description = req.getParameter("description");
            String scope = req.getParameter("scope");

            boolean ok = roleDAO.updateRoleInfo(roleId, name, description, scope);
            if (ok) {
                req.getSession().setAttribute("flashSuccess", "Cập nhật thông tin vai trò thành công!");
            } else {
                req.getSession().setAttribute("flashError", "Lỗi: Không thể cập nhật (có thể là vai trò hệ thống).");
            }

        } else if ("update-permissions".equalsIgnoreCase(action)) {
            String[] permissionParams = req.getParameterValues("permissions");
            Set<String> checkedPerms = new HashSet<>();
            if (permissionParams != null) {
                checkedPerms.addAll(Arrays.asList(permissionParams));
            }

            boolean ok = roleDAO.savePermissionMatrix(roleId, checkedPerms);
            if (ok) {
                req.getSession().setAttribute("flashSuccess", "Cập nhật ma trận phân quyền thành công!");
            } else {
                req.getSession().setAttribute("flashError", "Lỗi: Cập nhật quyền thất bại.");
            }

        } else if ("delete-role".equalsIgnoreCase(action)) {
            boolean ok = roleDAO.deleteRole(roleId);
            if (ok) {
                req.getSession().setAttribute("flashSuccess", "Đã xóa nhóm vai trò thành công!");
            } else {
                req.getSession().setAttribute("flashError", "Không thể xóa vai trò mặc định của hệ thống.");
            }
            resp.sendRedirect(req.getContextPath() + "/admin/accounts/roles");
            return;
        }

        resp.sendRedirect(req.getContextPath() + "/admin/accounts/roles?roleId=" + roleId);
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
