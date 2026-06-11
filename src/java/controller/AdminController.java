package controller;

import java.io.IOException;
import java.io.PrintWriter;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.*;
import java.util.List;
import model.User;
import service.UserService;

@WebServlet({
    "/admin/dashboard",
    "/admin/user/save",
    "/admin/user/toggle-active"
})
public class AdminController extends HttpServlet {

    private final UserService userService = new UserService();

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
    protected void doGet(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {

        if (!isAdmin(request)) {
            response.sendRedirect(request.getContextPath() + "/login");
            return;
        }

        String path = request.getServletPath();

        switch (path) {

            case "/admin/dashboard":
                showDashboard(request, response);
                break;

            case "/admin/user/toggle-active":
                toggleActive(request, response);
                break;

            default:
                response.sendRedirect(request.getContextPath() + "/admin/dashboard");
                break;
        }
    }

    private void showDashboard(HttpServletRequest request,
            HttpServletResponse response)
            throws ServletException, IOException {

        List<User> users = userService.getAllUsersExceptAdmin();

        request.setAttribute("users", users);

        request.getRequestDispatcher("/pages/admin/dashboard.jsp")
                .forward(request, response);
    }

    private void toggleActive(HttpServletRequest request,
            HttpServletResponse response)
            throws IOException {

        String idRaw = request.getParameter("id");

        if (idRaw != null && !idRaw.isBlank()) {
            int id = Integer.parseInt(idRaw);
            userService.toggleActive(id);
        }

        response.sendRedirect(request.getContextPath() + "/admin/dashboard");
    }

    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {

        if (!isAdmin(request)) {
            response.sendRedirect(request.getContextPath() + "/login");
            return;
        }

        if ("/admin/user/save".equals(request.getServletPath())) {
            saveUser(request, response);
            return;
        }

        response.sendRedirect(request.getContextPath() + "/admin/dashboard");
    }

    private void saveUser(HttpServletRequest request,
            HttpServletResponse response)
            throws IOException {

        request.setCharacterEncoding("UTF-8");

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

        response.sendRedirect(request.getContextPath() + "/admin/dashboard");
    }
}