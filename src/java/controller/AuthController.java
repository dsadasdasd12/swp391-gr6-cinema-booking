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
import util.EmailUtil;

/**
 *
 * @author tttru
 */
@WebServlet(name = "AuthController", urlPatterns = {
    "/auth",
    "/login",
    "/register",
    "/logout",
    "/verify-email",
    "/forgot-password",
    "/confirm-reset-otp",
    "/reset-password"
})
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
            case "/verify-email":
                request.getRequestDispatcher("/pages/verify-email.jsp")
                        .forward(request, response);
                break;
            case "/logout":
                request.getSession().invalidate();
                response.sendRedirect("home");
                break;
            case "/forgot-password":
                request.getRequestDispatcher("/pages/forgot-password.jsp")
                        .forward(request, response);
                break;

            case "/confirm-reset-otp":
                request.getRequestDispatcher("/pages/confirm-reset-otp.jsp")
                        .forward(request, response);
                break;

            case "/reset-password":
                request.getRequestDispatcher("/pages/reset-password.jsp")
                        .forward(request, response);
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
            case "/verify-email":
                verifyEmail(request, response);
                break;
            case "/forgot-password":
                forgotPassword(request, response);
                break;

            case "/confirm-reset-otp":
                confirmResetOtp(request, response);
                break;

            case "/reset-password":
                resetPassword(request, response);
                break;
            default:
                response.sendRedirect(request.getContextPath() + "/home");
        }
    }

    private void login(HttpServletRequest request,
            HttpServletResponse response)
            throws ServletException, IOException {

        String email = request.getParameter("email");
        String password = request.getParameter("password");
        System.out.println(email);
        System.out.println(password);
        User user = userDAO.login(email, password);

        if (user == null) {
            System.out.println(user.getEmail());
            request.setAttribute(
                    "error",
                    "Email hoặc mật khẩu không đúng"
            );
            request.setAttribute("email", email);
            request.getRequestDispatcher("/pages/login.jsp")
                    .forward(request, response);

            return;
        }
        if (!user.isEmailVerified()) {

            HttpSession session = request.getSession();

            session.setAttribute(
                    "verifyUser",
                    user
            );

            response.sendRedirect(
                    request.getContextPath()
                    + "/verify-email"
            );

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
                        + "/pages/manager/brach-list.jsp");
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
        String confirmPassword
                = request.getParameter("confirmPassword");

        String phoneRegex = "^0\\d{9}$";

        if (phone != null && !phone.matches(phoneRegex)) {

            request.setAttribute(
                    "error",
                    "Số điện thoại phải gồm 10 chữ số và bắt đầu bằng 0"
            );

            request.setAttribute("fullname", fullName);
            request.setAttribute("email", email);
            request.setAttribute("phone", phone);

            request.getRequestDispatcher("/pages/register.jsp")
                    .forward(request, response);

            return;
        }

        if (password == null || password.length() < 8) {

            request.setAttribute(
                    "error",
                    "Mật khẩu phải có ít nhất 8 ký tự"
            );

            request.setAttribute("fullname", fullName);
            request.setAttribute("email", email);
            request.setAttribute("phone", phone);

            request.getRequestDispatcher("/pages/register.jsp")
                    .forward(request, response);

            return;
        }

        if (!password.equals(confirmPassword)) {

            request.setAttribute(
                    "error",
                    "Mật khẩu xác nhận không khớp"
            );

            request.setAttribute("fullname", fullName);
            request.setAttribute("email", email);
            request.setAttribute("phone", phone);

            request.getRequestDispatcher("/pages/register.jsp")
                    .forward(request, response);
            return;
        }
        String emailRegex
                = "^[A-Za-z0-9+_.-]+@[A-Za-z0-9.-]+$";

        if (!email.matches(emailRegex)) {

            request.setAttribute(
                    "error",
                    "Email không đúng định dạng"
            );
            request.setAttribute("fullname", fullName);
            request.setAttribute("email", email);
            request.setAttribute("phone", phone);
            request.getRequestDispatcher(
                    "/pages/register.jsp")
                    .forward(request, response);

            return;
        }
        if (userDAO.emailExists(email)) {

            request.setAttribute(
                    "error",
                    "Email đã tồn tại"
            );
            request.setAttribute("fullname", fullName);
            request.setAttribute("email", email);
            request.setAttribute("phone", phone);
            request.getRequestDispatcher("/pages/register.jsp")
                    .forward(request, response);
            return;
        }

        User user = new User();

        user.setFullName(fullName);
        user.setEmail(email);
        user.setPhone(phone);
        user.setGoogleId("local_" + email);
        user.setPasswordHash(password);
        System.out.println(user.getGoogleId());
        boolean success = userDAO.register(user);

        if (!success) {

            request.setAttribute(
                    "error",
                    "Đăng ký thất bại"
            );
            request.setAttribute("fullname", fullName);
            request.setAttribute("email", email);
            request.setAttribute("phone", phone);
            request.getRequestDispatcher("/pages/register.jsp")
                    .forward(request, response);
            return;
        }

        User createdUser = userDAO.login(email, password);

        String otp = String.valueOf((int) (Math.random() * 900000) + 100000);

        HttpSession session = request.getSession();
        session.setAttribute("verifyUser", createdUser);
        session.setAttribute("emailOtp", otp);
        session.setAttribute("otpExpiredAt", System.currentTimeMillis() + 5 * 60 * 1000);

        EmailUtil.sendOtp(email, otp);

        response.sendRedirect(request.getContextPath() + "/verify-email");
    }

    private void verifyEmail(HttpServletRequest request,
            HttpServletResponse response)
            throws ServletException, IOException {

        String code = request.getParameter("otp");

        HttpSession session = request.getSession(false);

        if (session == null || session.getAttribute("verifyUser") == null) {
            response.sendRedirect(request.getContextPath() + "/login");
            return;
        }

        User verifyUser = (User) session.getAttribute("verifyUser");

        // Demo code cố định
        // Sau này có thể đổi thành OTP gửi email
        /*if (!"123456".equals(code)) {
            request.setAttribute("error", "Mã xác thực không đúng");
            request.getRequestDispatcher("/pages/verify-email.jsp")
                    .forward(request, response);
            return;
        }*/
        String emailOtp = (String) session.getAttribute("emailOtp");
        Long otpExpiredAt = (Long) session.getAttribute("otpExpiredAt");

        if (emailOtp == null || otpExpiredAt == null) {
            response.sendRedirect(request.getContextPath() + "/login");
            return;
        }

        if (System.currentTimeMillis() > otpExpiredAt) {
            request.setAttribute("error", "Mã OTP đã hết hạn");
            request.getRequestDispatcher("/pages/verify-email.jsp")
                    .forward(request, response);
            return;
        }

        if (!emailOtp.equals(code)) {
            request.setAttribute("error", "Mã OTP không đúng");
            request.getRequestDispatcher("/pages/verify-email.jsp")
                    .forward(request, response);
            return;
        }

        boolean success = userDAO.activateEmail(verifyUser.getId());

        if (!success) {
            request.setAttribute("error", "Xác thực email thất bại");
            request.getRequestDispatcher("/pages/verify-email.jsp")
                    .forward(request, response);
            return;
        }

        verifyUser.setEmailVerified(true);
        session.removeAttribute("emailOtp");
        session.removeAttribute("otpExpiredAt");
        session.removeAttribute("verifyUser");
        session.setAttribute("user", verifyUser);

        redirectByRole(request, response, verifyUser);
    }

    private void redirectByRole(HttpServletRequest request,
            HttpServletResponse response,
            User user)
            throws IOException {

        String ctx = request.getContextPath();

        switch (user.getRole()) {
            case "ADMIN":
                response.sendRedirect(ctx + "/admin/dashboard");
                break;

            case "MANAGER":
                response.sendRedirect(ctx + "/manager/dashboard");
                break;

            case "STAFF":
                response.sendRedirect(ctx + "/staff/dashboard");
                break;

            case "CUSTOMER":
            default:
                response.sendRedirect(ctx + "/home");
                break;
        }
    }

    private void forgotPassword(HttpServletRequest request,
            HttpServletResponse response)
            throws ServletException, IOException {

        String email = request.getParameter("email");

        User user = userDAO.getUserByEmail(email);

        if (user == null) {
            request.setAttribute("error", "Email không tồn tại");
            request.setAttribute("email", email);
            request.getRequestDispatcher("/pages/forgot-password.jsp")
                    .forward(request, response);
            return;
        }

        String otp = String.valueOf((int) (Math.random() * 900000) + 100000);

        HttpSession session = request.getSession();
        session.setAttribute("resetUser", user);
        session.setAttribute("resetEmail", email);
        session.setAttribute("resetOtp", otp);
        session.setAttribute("resetOtpExpiredAt",
                System.currentTimeMillis() + 5 * 60 * 1000);
        
         EmailUtil.sendOtp(email, otp);
        response.sendRedirect(request.getContextPath() + "/confirm-reset-otp");
    }

    private void confirmResetOtp(HttpServletRequest request,
            HttpServletResponse response)
            throws ServletException, IOException {

        String otp = request.getParameter("otp");

        HttpSession session = request.getSession(false);

        if (session == null || session.getAttribute("resetUser") == null) {
            response.sendRedirect(request.getContextPath() + "/forgot-password");
            return;
        }

        String resetOtp = (String) session.getAttribute("resetOtp");
        Long expiredAt = (Long) session.getAttribute("resetOtpExpiredAt");

        if (expiredAt == null || System.currentTimeMillis() > expiredAt) {
            request.setAttribute("error", "Mã OTP đã hết hạn");
            request.getRequestDispatcher("/pages/confirm-reset-otp.jsp")
                    .forward(request, response);
            return;
        }

        if (!resetOtp.equals(otp)) {
            request.setAttribute("error", "Mã OTP không đúng");
            request.getRequestDispatcher("/pages/confirm-reset-otp.jsp")
                    .forward(request, response);
            return;
        }

        session.setAttribute("resetOtpConfirmed", true);

        response.sendRedirect(request.getContextPath() + "/reset-password");
    }

    private void resetPassword(HttpServletRequest request,
            HttpServletResponse response)
            throws ServletException, IOException {

        String newPassword = request.getParameter("newPassword");
        String confirmPassword = request.getParameter("confirmPassword");

        HttpSession session = request.getSession(false);

        if (session == null
                || session.getAttribute("resetUser") == null
                || session.getAttribute("resetOtpConfirmed") == null) {

            response.sendRedirect(request.getContextPath() + "/forgot-password");
            return;
        }

        User resetUser = (User) session.getAttribute("resetUser");

        if (newPassword == null || newPassword.length() < 8) {
            request.setAttribute("error", "Mật khẩu mới phải có ít nhất 8 ký tự");
            request.getRequestDispatcher("/pages/reset-password.jsp")
                    .forward(request, response);
            return;
        }

        if (!newPassword.equals(confirmPassword)) {
            request.setAttribute("error", "Mật khẩu xác nhận không khớp");
            request.getRequestDispatcher("/pages/reset-password.jsp")
                    .forward(request, response);
            return;
        }

        boolean success = userDAO.updatePassword(resetUser.getId(), newPassword);

        if (success) {
            session.removeAttribute("resetUser");
            session.removeAttribute("resetEmail");
            session.removeAttribute("resetOtp");
            session.removeAttribute("resetOtpExpiredAt");
            session.removeAttribute("resetOtpConfirmed");
            session.setAttribute("successMessage", "Đổi mật khẩu thành công!");

            response.sendRedirect(request.getContextPath() + "/login");
        } else {
            request.setAttribute("error", "Đổi mật khẩu thất bại");
            request.getRequestDispatcher("/pages/reset-password.jsp")
                    .forward(request, response);
        }
    }
}
