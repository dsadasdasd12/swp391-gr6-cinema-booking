package controller;

import dao.BranchDAO;
import dao.AdminUserDAO;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import model.Branch;
import model.ManagedUser;
import model.User;

import java.io.IOException;
import util.PasswordUtil;
import java.util.ArrayList;
import java.util.List;

@WebServlet("/admin/accounts/staff")
public class StaffAccountServlet extends HttpServlet {

    private final AdminUserDAO userDAO = new AdminUserDAO();
    private final BranchDAO branchDAO = new BranchDAO();

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp)
            throws ServletException, IOException {
        String keyword = req.getParameter("keyword");
        
        Integer roleId = null;
        String rStr = req.getParameter("roleId");
        if (rStr != null && !rStr.trim().isEmpty()) {
            try { roleId = Integer.parseInt(rStr); } catch (NumberFormatException ignored) {}
        }

        Integer branchId = null;
        String bStr = req.getParameter("branchId");
        if (bStr != null && !bStr.trim().isEmpty()) {
            try { branchId = Integer.parseInt(bStr); } catch (NumberFormatException ignored) {}
        }

        int currentPage = 1;
        String pageStr = req.getParameter("page");
        if (pageStr != null && !pageStr.trim().isEmpty()) {
            try { currentPage = Integer.parseInt(pageStr); } catch (NumberFormatException e) { currentPage = 1; }
        }

        int pageSize = 10;
        int offset = (currentPage - 1) * pageSize;

        List<ManagedUser> staffList = userDAO.findStaffPaged(keyword, roleId, branchId, offset, pageSize);
        int totalItems = userDAO.countStaff(keyword, roleId, branchId);
        int totalPages = (int) Math.ceil((double) totalItems / pageSize);
        if (totalPages == 0) totalPages = 1;

        List<Branch> branches = branchDAO.findAllActive();
        List<RoleMock> roles = getMockRoles();

        req.setAttribute("staffList", staffList);
        req.setAttribute("branches", branches);
        req.setAttribute("roles", roles);
        req.setAttribute("currentPage", currentPage);
        req.setAttribute("totalPages", totalPages);
        req.setAttribute("totalItems", totalItems);
        req.setAttribute("pageSize", pageSize);

        req.getRequestDispatcher("/pages/accounts/staff.jsp").forward(req, resp);
    }

    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse resp)
            throws ServletException, IOException {
        req.setCharacterEncoding("UTF-8");
        String action = req.getParameter("action");
        String idStr = req.getParameter("id");
        
        int userId = 0;
        if (idStr != null && !idStr.trim().isEmpty()) {
            try { userId = Integer.parseInt(idStr); } catch (NumberFormatException ignored) {}
        }

        if ("add".equalsIgnoreCase(action)) {
            String fullName = req.getParameter("fullName");
            String email = req.getParameter("email");
            int rId = Integer.parseInt(req.getParameter("roleId"));
            int bId = Integer.parseInt(req.getParameter("branchId"));

            User u = new User();
            u.setFullName(fullName);
            u.setEmail(email != null ? email.trim().toLowerCase() : null);
            u.setRole(mapRoleIdToName(rId));
            u.setPhone("");

            String defaultHash = PasswordUtil.hashPassword("123");
            int newId = userDAO.insertStaff(u, defaultHash, bId);
            if (newId > 0) {
                req.getSession().setAttribute("flashSuccess", "Thêm mới tài khoản nhân viên thành công!");
            } else {
                req.getSession().setAttribute("flashError", "Email đã tồn tại hoặc dữ liệu không hợp lệ.");
            }

        } else if ("update".equalsIgnoreCase(action)) {
            String fullName = req.getParameter("fullName");
            int rId = Integer.parseInt(req.getParameter("roleId"));
            int bId = Integer.parseInt(req.getParameter("branchId"));
            String status = req.getParameter("status");

            boolean updated = userDAO.updateStaffInfo(userId, fullName, mapRoleIdToName(rId), "", bId, status);
            if (updated) {
                req.getSession().setAttribute("flashSuccess", "Cập nhật thông tin nhân viên thành công!");
            } else {
                req.getSession().setAttribute("flashError", "Cập nhật thông tin thất bại.");
            }

        } else if ("reset-password".equalsIgnoreCase(action)) {
            String defaultHash = PasswordUtil.hashPassword("123");
            boolean updated = userDAO.updatePassword(userId, defaultHash);
            if (updated) {
                req.getSession().setAttribute("flashSuccess", "Mật khẩu của nhân viên đã được khôi phục về 123!");
            } else {
                req.getSession().setAttribute("flashError", "Khôi phục mật khẩu thất bại.");
            }

        } else if ("delete".equalsIgnoreCase(action)) {
            boolean deleted = userDAO.deleteUser(userId);
            if (deleted) {
                req.getSession().setAttribute("flashSuccess", "Xóa tài khoản nhân viên thành công!");
            } else {
                req.getSession().setAttribute("flashError", "Xóa tài khoản thất bại.");
            }
        }

        resp.sendRedirect(req.getContextPath() + "/admin/accounts/staff");
    }

    private String mapRoleIdToName(int roleId) {
        if (roleId == 1) return "ADMIN";
        if (roleId == 2) return "MANAGER";
        return "STAFF";
    }

    /** @deprecated dùng {@link PasswordUtil#hashPassword(String)} */
    public static String hashPassword(String password) {
        return PasswordUtil.hashPassword(password);
    }

    private List<RoleMock> getMockRoles() {
        List<RoleMock> list = new ArrayList<>();
        list.add(new RoleMock(1, "ADMIN"));
        list.add(new RoleMock(2, "QUẢN LÝ CHI NHÁNH"));
        list.add(new RoleMock(3, "NHÂN VIÊN RẠP"));
        return list;
    }

    public static class RoleMock {
        private int id;
        private String name;

        public RoleMock(int id, String name) {
            this.id = id;
            this.name = name;
        }

        public int getId() { return id; }
        public String getName() { return name; }
    }
}
