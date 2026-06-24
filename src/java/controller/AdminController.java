package controller;

import java.io.IOException;
import java.time.LocalTime;
import java.util.List;

import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.*;
import java.io.PrintWriter;

import model.Branch;
import model.User;
import service.BranchService;
import service.UserService;

@WebServlet({
    
    "/admin/accounts",
    "/admin/user/save",
    "/admin/user/toggle-active"
})
public class AdminController extends HttpServlet {

    private final UserService userService = new UserService();
    private final BranchService branchService = new BranchService();

    protected void processRequest(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        response.setContentType("text/html;charset=UTF-8");
        try (PrintWriter out = response.getWriter()) {
            out.println("<h1>Servlet AdminController at " + request.getContextPath() + "</h1>");
        }
    }

    private boolean isAdmin(HttpServletRequest request) {
        HttpSession session = request.getSession(false);

        if (session == null) {
            return false;
        }

        User currentUser = (User) session.getAttribute("user");

        return currentUser != null
                && "ADMIN".equalsIgnoreCase(currentUser.getRole());
    }

    @Override
    protected void doGet(HttpServletRequest request,
                         HttpServletResponse response)
            throws ServletException, IOException {

        if (!isAdmin(request)) {
            response.sendRedirect(request.getContextPath() + "/login");
            return;
        }

        String path = request.getServletPath();

        switch (path) {

            case "/admin/accounts":
                showAccountManagement(request, response);
                break;

            case "/admin/branches":
                showBranchList(request, response);
                break;

            case "/admin/branches/create":
                showBranchForm(request, response, null, false);
                break;

            case "/admin/branches/edit":
                showEditBranchForm(request, response);
                break;

            case "/admin/user/toggle-active":
                toggleActive(request, response);
                break;

            default:
                response.sendRedirect(request.getContextPath() + "/admin/dashboard");
                break;
        }
    }

    @Override
    protected void doPost(HttpServletRequest request,
                          HttpServletResponse response)
            throws ServletException, IOException {

        request.setCharacterEncoding("UTF-8");

        if (!isAdmin(request)) {
            response.sendRedirect(request.getContextPath() + "/login");
            return;
        }

        String path = request.getServletPath();

        switch (path) {

            case "/admin/user/save":
                saveUser(request, response);
                break;

            case "/admin/branches/create":
                createBranch(request, response);
                break;

            case "/admin/branches/edit":
                updateBranch(request, response);
                break;

            case "/admin/branches/status":
                changeBranchStatus(request, response);
                break;

            case "/admin/branches/delete":
                deleteBranch(request, response);
                break;

            default:
                response.sendRedirect(request.getContextPath() + "/admin/dashboard");
                break;
        }
    }

    private void showAccountManagement(HttpServletRequest request,
                                       HttpServletResponse response)
            throws ServletException, IOException {

        List<User> users = userService.getAllUsersExceptAdmin();
        request.setAttribute("users", users);

        request.getRequestDispatcher("/pages/admin/accountmanagement.jsp")
                .forward(request, response);
    }

    private void saveUser(HttpServletRequest request,
                          HttpServletResponse response)
            throws IOException {

        String idRaw = request.getParameter("id");

        User user = new User();
        user.setFullName(request.getParameter("fullName"));
        user.setEmail(request.getParameter("email"));
        user.setPhone(request.getParameter("phone"));
        user.setRole(request.getParameter("role"));

        if (idRaw == null || idRaw.isBlank()) {
            userService.createUserByAdmin(user);
        } else {
            user.setId(Integer.parseInt(idRaw));
            userService.updateUserByAdmin(user);
        }

        response.sendRedirect(request.getContextPath() + "/admin/accounts");
    }

    private void toggleActive(HttpServletRequest request,
                              HttpServletResponse response)
            throws IOException {

        String idRaw = request.getParameter("id");

        if (idRaw != null && !idRaw.isBlank()) {
            int id = Integer.parseInt(idRaw);
            userService.toggleActive(id);
        }

        response.sendRedirect(request.getContextPath() + "/admin/accounts");
    }

    private void showBranchList(HttpServletRequest request,
                                HttpServletResponse response)
            throws ServletException, IOException {

        List<Branch> branches = branchService.getAllBranches();
        request.setAttribute("branches", branches);

        request.getRequestDispatcher("/pages/admin/branch-list.jsp")
                .forward(request, response);
    }

    private void showBranchForm(HttpServletRequest request,
                                HttpServletResponse response,
                                Branch branch,
                                boolean isEdit)
            throws ServletException, IOException {

        request.setAttribute("branch", branch == null ? new Branch() : branch);
        request.setAttribute("formMode", isEdit ? "edit" : "create");

        request.getRequestDispatcher("/pages/admin/branch-form.jsp")
                .forward(request, response);
    }

    private void showEditBranchForm(HttpServletRequest request,
                                    HttpServletResponse response)
            throws ServletException, IOException {

        String idRaw = request.getParameter("id");

        if (idRaw == null || idRaw.isBlank()) {
            response.sendRedirect(request.getContextPath() + "/admin/branches");
            return;
        }

        int id = Integer.parseInt(idRaw);
        Branch branch = branchService.getBranchById(id);

        if (branch == null) {
            request.getSession().setAttribute("flashMessage", "Không tìm thấy chi nhánh.");
            request.getSession().setAttribute("flashType", "error");
            response.sendRedirect(request.getContextPath() + "/admin/branches");
            return;
        }

        showBranchForm(request, response, branch, true);
    }

    private void createBranch(HttpServletRequest request,
                              HttpServletResponse response)
            throws IOException, ServletException {

        try {
            Branch branch = buildBranchFromRequest(request, false);
            branchService.createBranch(branch);

            request.getSession().setAttribute("flashMessage", "Thêm chi nhánh thành công.");
            request.getSession().setAttribute("flashType", "success");

            response.sendRedirect(request.getContextPath() + "/admin/branches");
        } catch (IllegalArgumentException e) {
            request.setAttribute("error", e.getMessage());
            showBranchForm(request, response, buildBranchFromRequestSafe(request, false), false);
        }
    }

    private void updateBranch(HttpServletRequest request,
                              HttpServletResponse response)
            throws IOException, ServletException {

        try {
            Branch branch = buildBranchFromRequest(request, true);
            branchService.updateBranch(branch);

            request.getSession().setAttribute("flashMessage", "Cập nhật chi nhánh thành công.");
            request.getSession().setAttribute("flashType", "success");

            response.sendRedirect(request.getContextPath() + "/admin/branches");
        } catch (IllegalArgumentException e) {
            request.setAttribute("error", e.getMessage());
            showBranchForm(request, response, buildBranchFromRequestSafe(request, true), true);
        }
    }

    private void deleteBranch(HttpServletRequest request,
                              HttpServletResponse response)
            throws IOException {

        int id = Integer.parseInt(request.getParameter("id"));
        boolean ok = branchService.deleteBranch(id);

        request.getSession().setAttribute(
                "flashMessage",
                ok ? "Xóa chi nhánh thành công." : "Không thể xóa chi nhánh."
        );

        request.getSession().setAttribute(
                "flashType",
                ok ? "success" : "error"
        );

        response.sendRedirect(request.getContextPath() + "/admin/branches");
    }

    private void changeBranchStatus(HttpServletRequest request,
                                    HttpServletResponse response)
            throws IOException {

        int id = Integer.parseInt(request.getParameter("id"));
        String status = request.getParameter("status");

        boolean ok = branchService.changeBranchStatus(id, status);

        request.getSession().setAttribute(
                "flashMessage",
                ok ? "Cập nhật trạng thái chi nhánh thành công." : "Không thể cập nhật trạng thái."
        );

        request.getSession().setAttribute(
                "flashType",
                ok ? "success" : "error"
        );

        response.sendRedirect(request.getContextPath() + "/admin/branches");
    }

    private Branch buildBranchFromRequest(HttpServletRequest request,
                                          boolean requireId) {

        Branch branch = new Branch();

        if (requireId) {
            branch.setId(Integer.parseInt(request.getParameter("id")));
        }

        String cinemaIdRaw = request.getParameter("cinemaId");

        if (cinemaIdRaw == null || cinemaIdRaw.isBlank()) {
            branch.setCinemaId(1);
        } else {
            branch.setCinemaId(Integer.parseInt(cinemaIdRaw));
        }

        branch.setName(request.getParameter("name"));
        branch.setAddress(request.getParameter("address"));
        branch.setPhone(request.getParameter("phone"));
        branch.setStatus(request.getParameter("status"));

        String openTime = request.getParameter("openTime");
        String closeTime = request.getParameter("closeTime");

        if (openTime != null && !openTime.isBlank()) {
            branch.setOpenTime(LocalTime.parse(openTime));
        }

        if (closeTime != null && !closeTime.isBlank()) {
            branch.setCloseTime(LocalTime.parse(closeTime));
        }

        return branch;
    }

    private Branch buildBranchFromRequestSafe(HttpServletRequest request,
                                              boolean requireId) {

        try {
            return buildBranchFromRequest(request, requireId);
        } catch (Exception e) {
            Branch branch = new Branch();

            if (requireId) {
                try {
                    branch.setId(Integer.parseInt(request.getParameter("id")));
                } catch (Exception ignored) {
                }
            }

            branch.setCinemaId(1);
            branch.setName(request.getParameter("name"));
            branch.setAddress(request.getParameter("address"));
            branch.setPhone(request.getParameter("phone"));
            branch.setStatus(request.getParameter("status"));

            return branch;
        }
    }
}