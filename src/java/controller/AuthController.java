package controller;
import dao.UserDAO;
import model.User;
import java.io.IOException;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.*;

@WebServlet(urlPatterns = {"/auth","/login","/register","/logout"})
public class AuthController extends HttpServlet {

    private final UserDAO userDAO = new UserDAO();

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp)
            throws ServletException, IOException {
        switch (req.getServletPath()) {
            case "/login":
                req.getRequestDispatcher("/pages/login.jsp")
                   .forward(req, resp); break;
            case "/register":
                req.getRequestDispatcher("/pages/register.jsp")
                   .forward(req, resp); break;
            case "/logout":
                req.getSession().invalidate();
                resp.sendRedirect(req.getContextPath() + "/home"); break;
        }
    }

    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse resp)
            throws ServletException, IOException {
        req.setCharacterEncoding("UTF-8");
        switch (req.getServletPath()) {
            case "/login":    login(req, resp);    break;
            case "/register": register(req, resp); break;
        }
    }

    private void login(HttpServletRequest req, HttpServletResponse resp)
            throws ServletException, IOException {
        String email    = req.getParameter("email");
        String password = req.getParameter("password");
        User user = userDAO.login(email, password);
        if (user == null) {
            req.setAttribute("error", "Email hoặc mật khẩu không đúng");
            req.getRequestDispatcher("/pages/login.jsp").forward(req, resp);
            return;
        }
        req.getSession().setAttribute("user", user);
        String ctx = req.getContextPath();
        switch (user.getRole()) {
            case "ADMIN":
                resp.sendRedirect(ctx + "/admin/movies?action=list"); break;
            case "MANAGER":
                resp.sendRedirect(ctx + "/pages/manager/dashboard.jsp"); break;
            case "STAFF":
                resp.sendRedirect(ctx + "/pages/staff/dashboard.jsp"); break;
            default:
                resp.sendRedirect(ctx + "/home"); break;
        }
    }

    private void register(HttpServletRequest req, HttpServletResponse resp)
            throws ServletException, IOException {
        String fullName         = req.getParameter("fullname");
        String email            = req.getParameter("email");
        String phone            = req.getParameter("phone");
        String password         = req.getParameter("password");
        String confirmPassword  = req.getParameter("confirmPassword");

        if (!password.equals(confirmPassword)) {
            req.setAttribute("error", "Mật khẩu xác nhận không khớp");
            req.getRequestDispatcher("/pages/register.jsp").forward(req, resp);
            return;
        }
        if (userDAO.emailExists(email)) {
            req.setAttribute("error", "Email đã tồn tại");
            req.getRequestDispatcher("/pages/register.jsp").forward(req, resp);
            return;
        }
        User user = new User();
        user.setFullName(fullName);
        user.setEmail(email);
        user.setPhone(phone);
        user.setPasswordHash(password);

        if (userDAO.register(user)) {
            resp.sendRedirect(req.getContextPath() + "/login");
        } else {
            req.setAttribute("error", "Đăng ký thất bại, vui lòng thử lại");
            req.getRequestDispatcher("/pages/register.jsp").forward(req, resp);
        }
    }
}
