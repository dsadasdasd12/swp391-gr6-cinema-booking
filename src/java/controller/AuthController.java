/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/JSP_Servlet/Servlet.java to edit this template
 */
package controller;
import dao.UserDAO;
import model.User;

import java.io.IOException;
import java.io.PrintWriter;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.*;

import java.io.IOException;

/**
 *
 * @author tttru
 */
@WebServlet(name = "AuthCotntroller", urlPatterns = {"/auth","/login",
    "/register",
    "/logout"})
public class AuthController extends HttpServlet {
     private final UserDAO userDAO = new UserDAO();
    /**
     * Processes requests for both HTTP <code>GET</code> and <code>POST</code>
     * methods.
     *
     * @param request servlet request
     * @param response servlet response
     * @throws ServletException if a servlet-specific error occurs
     * @throws IOException if an I/O error occurs
     */
    protected void processRequest(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        response.setContentType("text/html;charset=UTF-8");
        try (PrintWriter out = response.getWriter()) {
            /* TODO output your page here. You may use following sample code. */
            out.println("<!DOCTYPE html>");
            out.println("<html>");
            out.println("<head>");
            out.println("<title>Servlet AuthCotntroller</title>");
            out.println("</head>");
            out.println("<body>");
            out.println("<h1>Servlet AuthCotntroller at " + request.getContextPath() + "</h1>");
            out.println("</body>");
            out.println("</html>");
        }
    }

    // <editor-fold defaultstate="collapsed" desc="HttpServlet methods. Click on the + sign on the left to edit the code.">
    /**
     * Handles the HTTP <code>GET</code> method.
     *
     * @param request servlet request
     * @param response servlet response
     * @throws ServletException if a servlet-specific error occurs
     * @throws IOException if an I/O error occurs
     */
    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        String path = request.getServletPath();

    switch (path) {

        case "/login":
            request.getRequestDispatcher("/pages/login.jsp")
                    .forward(request, response);
            break;

        case "/register":
            request.getRequestDispatcher("/pages/register.jsp")
                    .forward(request, response);
            break;

        case "/logout":
            request.getSession().invalidate();
            response.sendRedirect("home");
            break;
    }
    }

    /**
     * Handles the HTTP <code>POST</code> method.
     *
     * @param request servlet request
     * @param response servlet response
     * @throws ServletException if a servlet-specific error occurs
     * @throws IOException if an I/O error occurs
     */
    @Override
    protected void doPost(HttpServletRequest request,
            HttpServletResponse response)
            throws ServletException, IOException {

         String path = request.getServletPath();

        switch (path) {

            case "/login":
                login(request, response);
                break;

            case "/register":
                register(request, response);
                break;
        }
    }

   private void login(HttpServletRequest request,
                   HttpServletResponse response)
        throws ServletException, IOException {

    String email = request.getParameter("email");
    String password = request.getParameter("password");

    User user = userDAO.login(email, password);

    if (user == null) {

        request.setAttribute(
                "error",
                "Email hoặc mật khẩu không đúng"
        );

        request.getRequestDispatcher("/pages/login.jsp")
                .forward(request, response);

        return;
    }

    HttpSession session = request.getSession();
    session.setAttribute("user", user);

    switch (user.getRole()) {

        case "ADMIN":
            response.sendRedirect(
                    request.getContextPath()
                    + "/pages/admin/dashboard.jsp");
            break;

        case "MANAGER":
            response.sendRedirect(
                    request.getContextPath()
                    + "/pages/manager/dashboard.jsp");
            break;

        case "STAFF":
            response.sendRedirect(
                    request.getContextPath()
                    + "/pages/staff/dashboard.jsp");
            break;

        default:
            response.sendRedirect(
                    request.getContextPath()
                    + "/home");
            break;
    }
}
    
    

    private void register(HttpServletRequest request,
                          HttpServletResponse response)
            throws ServletException, IOException {

        String fullName = request.getParameter("fullname");
        String email = request.getParameter("email");
        String phone = request.getParameter("phone");
        String password = request.getParameter("password");
        String confirmPassword =
                request.getParameter("confirmPassword");

        if (!password.equals(confirmPassword)) {

            request.setAttribute(
                    "error",
                    "Mật khẩu xác nhận không khớp"
            );

            request.getRequestDispatcher("/pages/register.jsp")
                    .forward(request, response);
            return;
        }

        if (userDAO.emailExists(email)) {

            request.setAttribute(
                    "error",
                    "Email đã tồn tại"
            );

            request.getRequestDispatcher("/pages/register.jsp")
                    .forward(request, response);
            return;
        }

        User user = new User();

        user.setFullName(fullName);
        user.setEmail(email);
        user.setPhone(phone);
        user.setPasswordHash(password);

        boolean success = userDAO.register(user);

        if (success) {

            response.sendRedirect(
                    request.getContextPath() + "/login"
            );

        } else {

            request.setAttribute(
                    "error",
                    "Đăng ký thất bại"
            );

            request.getRequestDispatcher("/pages/register.jsp")
                    .forward(request, response);
        }
    }
}


