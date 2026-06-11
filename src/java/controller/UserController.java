package controller;

import java.io.IOException;
import java.io.PrintWriter;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.*;
import model.User;
import service.UserService;

@WebServlet({
    "/profile",
    "/profile/edit",
    "/profile/update",
    "/favorite-films",
    "/transaction-history"
})
public class UserController extends HttpServlet {

    private final UserService userService = new UserService();

    protected void processRequest(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {

        response.setContentType("text/html;charset=UTF-8");

        try (PrintWriter out = response.getWriter()) {
            out.println("<h1>Servlet UserController at " + request.getContextPath() + "</h1>");
        }
    }

    @Override
    protected void doGet(HttpServletRequest request,
            HttpServletResponse response)
            throws ServletException, IOException {

        HttpSession session = request.getSession(false);

        if (session == null || session.getAttribute("user") == null) {
            response.sendRedirect(request.getContextPath() + "/login");
            return;
        }

        User user = (User) session.getAttribute("user");

        String path = request.getServletPath();

        switch (path) {

            case "/profile":

                if ("CUSTOMER".equalsIgnoreCase(user.getRole())) {
                    request.getRequestDispatcher("/pages/customer/customerprofile.jsp")
                            .forward(request, response);
                } else {
                    request.getRequestDispatcher("/pages/profile.jsp")
                            .forward(request, response);
                }
                return;

            case "/profile/edit":
                if ("ADMIN".equalsIgnoreCase(user.getRole())) {
                    response.sendRedirect(request.getContextPath() + "/profile");
                    return;
                }
                if ("CUSTOMER".equalsIgnoreCase(user.getRole())) {
                    request.getRequestDispatcher("/pages/customer/editprofile.jsp")
                            .forward(request, response);
                } else {
                    request.getRequestDispatcher("/pages/edit-profile.jsp")
                            .forward(request, response);
                }
                return;

            case "/favorite-films":

                if (!"CUSTOMER".equalsIgnoreCase(user.getRole())) {
                    response.sendRedirect(request.getContextPath() + "/profile");
                    return;
                }

                request.getRequestDispatcher("/pages/customer/favoritefilms.jsp")
                        .forward(request, response);
                return;

            case "/transaction-history":

                if (!"CUSTOMER".equalsIgnoreCase(user.getRole())) {
                    response.sendRedirect(request.getContextPath() + "/profile");
                    return;
                }

                request.getRequestDispatcher("/pages/customer/transactionhistory.jsp")
                        .forward(request, response);
                return;

            default:
                response.sendRedirect(request.getContextPath() + "/profile");
        }
    }

    @Override
    protected void doPost(HttpServletRequest request,
            HttpServletResponse response)
            throws ServletException, IOException {

        request.setCharacterEncoding("UTF-8");

        HttpSession session = request.getSession(false);

        if (session == null || session.getAttribute("user") == null) {
            response.sendRedirect(request.getContextPath() + "/login");
            return;
        }

        String path = request.getServletPath();

        if ("/profile/update".equals(path)) {
            updateProfile(request, response);
            return;
        }

        response.sendRedirect(request.getContextPath() + "/profile");
    }

    private void updateProfile(HttpServletRequest request,
            HttpServletResponse response)
            throws ServletException, IOException {

        HttpSession session = request.getSession(false);

        User user = (User) session.getAttribute("user");
        if ("ADMIN".equalsIgnoreCase(user.getRole())) {
            response.sendRedirect(request.getContextPath() + "/profile");
            return;
        }
        String fullName = request.getParameter("fullName");
        String phone = request.getParameter("phone");

        fullName = fullName == null ? "" : fullName.trim();
        phone = phone == null ? "" : phone.trim();

        String nameRegex = "^[\\p{L}]+(?:\\s+[\\p{L}]+)*$";
        String phoneRegex = "^0\\d{9}$";

        if (fullName.length() < 2
                || fullName.length() > 50
                || !fullName.matches(nameRegex)) {

            request.setAttribute(
                    "error",
                    "Họ tên phải từ 2-50 ký tự và chỉ chứa chữ cái"
            );

            forwardEditPageByRole(request, response, user);
            return;
        }

        if (!phone.isEmpty() && !phone.matches(phoneRegex)) {

            request.setAttribute(
                    "error",
                    "Số điện thoại phải gồm 10 chữ số và bắt đầu bằng 0"
            );

            forwardEditPageByRole(request, response, user);
            return;
        }

        user.setFullName(fullName);
        user.setPhone(phone);

        boolean updated = userService.updateProfile(user);

        if (!updated) {
            request.setAttribute("error", "Cập nhật thông tin thất bại");
            forwardEditPageByRole(request, response, user);
            return;
        }

        session.setAttribute("user", user);
        session.setAttribute("profileSuccess", "Cập nhật thông tin thành công");

        response.sendRedirect(request.getContextPath() + "/profile");
    }

    private void forwardEditPageByRole(HttpServletRequest request,
            HttpServletResponse response,
            User user)
            throws ServletException, IOException {

        if ("CUSTOMER".equalsIgnoreCase(user.getRole())) {
            request.getRequestDispatcher("/pages/customer/editprofile.jsp")
                    .forward(request, response);
        } else {
            request.getRequestDispatcher("/pages/edit-profile.jsp")
                    .forward(request, response);
        }
    }

    @Override
    public String getServletInfo() {
        return "User Controller";
    }
}
