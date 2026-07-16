package controller;

import dto.RegisterDTO;
import java.io.IOException;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.*;
import model.User;
import service.AuthService;
import util.PasswordUtil;

@WebServlet(name = "AuthController", urlPatterns = {
    "/auth",
    "/login",
    "/register",
    "/logout",
    "/verify-email",
    "/resend-otp",
    "/forgot-password",
    "/confirm-reset-otp",
    "/reset-password",
    "/change-password"
})
public class AuthController extends HttpServlet {

    private static final int LOGIN_SESSION_TIMEOUT_SECONDS = 60 * 60;

    private final AuthService authService = new AuthService();

    @Override
    protected void doGet(HttpServletRequest request,
            HttpServletResponse response)
            throws ServletException, IOException {

        String path = request.getServletPath();

        switch (path) {
            case "/login":
                request.getRequestDispatcher("/pages/login.jsp").forward(request, response);
                break;

            case "/register":
                request.getRequestDispatcher("/pages/register.jsp").forward(request, response);
                break;

            case "/verify-email":
                request.getRequestDispatcher("/pages/verify-email.jsp").forward(request, response);
                break;

            case "/logout":
                request.getSession().invalidate();
                response.sendRedirect(request.getContextPath() + "/home");
                break;

            case "/forgot-password":
                request.getRequestDispatcher("/pages/forgot-password.jsp").forward(request, response);
                break;

            case "/confirm-reset-otp":
                request.getRequestDispatcher("/pages/confirm-reset-otp.jsp").forward(request, response);
                break;

            case "/reset-password":
                request.getRequestDispatcher("/pages/reset-password.jsp").forward(request, response);
                break;

            default:
                response.sendRedirect(request.getContextPath() + "/home");
        }
    }

    @Override
    protected void doPost(HttpServletRequest request,
            HttpServletResponse response)
            throws ServletException, IOException {

        request.setCharacterEncoding("UTF-8");

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

            case "/resend-otp":
                resendOtp(request, response);
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

            case "/change-password":
                changePassword(request, response);
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

        email = email == null ? "" : email.trim();
        
        if (email.isEmpty() || password == null || password.isEmpty()) {
            request.setAttribute("error", "Vui lòng nhập email và mật khẩu");
            request.setAttribute("email", email);
            request.getRequestDispatcher("/pages/login.jsp").forward(request, response);
            return;
        }

        User user = authService.login(email, password);

        if (user == null) {
            request.setAttribute("error", "Email hoặc mật khẩu không đúng");
            request.setAttribute("email", email);
            request.getRequestDispatcher("/pages/login.jsp").forward(request, response);
            return;
        }

        if (!user.isEmailVerified()) {

            HttpSession session = request.getSession();

            authService.sendVerifyOtp(getServletContext(), session, user);

            response.sendRedirect(request.getContextPath() + "/verify-email");
            return;
        }

        HttpSession session = request.getSession();
        session.setMaxInactiveInterval(LOGIN_SESSION_TIMEOUT_SECONDS);
        session.setAttribute("user", user);

        redirectByRole(request, response, user);
    }

    private void register(HttpServletRequest request,
            HttpServletResponse response)
            throws ServletException, IOException {

        RegisterDTO dto = new RegisterDTO(
                request.getParameter("fullname"),
                request.getParameter("email"),
                request.getParameter("phone"),
                request.getParameter("password"),
                request.getParameter("confirmPassword")
        );

        String error = authService.validateRegister(dto);

        if (error != null) {
            request.setAttribute("error", error);
            keepRegisterInput(
                    request,
                    dto.getFullName(),
                    dto.getEmail(),
                    dto.getPhone()
            );

            request.getRequestDispatcher("/pages/register.jsp")
                    .forward(request, response);
            return;
        }

        User user = authService.buildUser(dto);

        boolean success = authService.register(user);

        if (!success) {
            request.setAttribute("error", "Đăng ký thất bại");
            keepRegisterInput(
                    request,
                    dto.getFullName(),
                    dto.getEmail(),
                    dto.getPhone()
            );

            request.getRequestDispatcher("/pages/register.jsp")
                    .forward(request, response);
            return;
        }

        User createdUser = authService.login(
                dto.getEmail(),
                dto.getPassword()
        );

        HttpSession session = request.getSession();

        authService.sendVerifyOtp(
                getServletContext(),
                session,
                createdUser
        );

        response.sendRedirect(request.getContextPath() + "/verify-email");
    }

    private void verifyEmail(HttpServletRequest request,
            HttpServletResponse response)
            throws ServletException, IOException {

        String inputOtp = request.getParameter("otp");
        inputOtp = inputOtp == null ? "" : inputOtp.trim();

        HttpSession session = request.getSession(false);

        if (session == null || session.getAttribute("verifyUser") == null) {
            response.sendRedirect(request.getContextPath() + "/login");
            return;
        }

        User verifyUser = (User) session.getAttribute("verifyUser");

        String emailOtp = (String) session.getAttribute("emailOtp");
        Long otpExpiredAt = (Long) session.getAttribute("otpExpiredAt");

        if (emailOtp == null || otpExpiredAt == null) {
            request.setAttribute("error", "Không tìm thấy OTP. Vui lòng bấm gửi lại OTP.");
            request.getRequestDispatcher("/pages/verify-email.jsp")
                    .forward(request, response);
            return;
        }

        if (System.currentTimeMillis() > otpExpiredAt) {
            request.setAttribute("error", "Mã OTP đã hết hạn");
            request.getRequestDispatcher("/pages/verify-email.jsp")
                    .forward(request, response);
            return;
        }

        if (!emailOtp.equals(inputOtp)) {
            request.setAttribute("error", "Mã OTP không đúng");
            request.getRequestDispatcher("/pages/verify-email.jsp")
                    .forward(request, response);
            return;
        }

        boolean success = authService.activateEmail(verifyUser.getId());

        if (!success) {
            request.setAttribute("error", "Không thể cập nhật trạng thái xác thực email trong database.");
            request.getRequestDispatcher("/pages/verify-email.jsp")
                    .forward(request, response);
            return;
        }

        verifyUser.setEmailVerified(true);

        session.removeAttribute("emailOtp");
        session.removeAttribute("otpExpiredAt");
        session.removeAttribute("otpPurpose");
        session.removeAttribute("verifyUser");

        session.setMaxInactiveInterval(LOGIN_SESSION_TIMEOUT_SECONDS);
        session.setAttribute("user", verifyUser);

        redirectByRole(request, response, verifyUser);
    }

    private void resendOtp(HttpServletRequest request,
            HttpServletResponse response)
            throws ServletException, IOException {

        HttpSession session = request.getSession(false);

        if (session == null) {
            response.sendRedirect(request.getContextPath() + "/login");
            return;
        }

        String purpose = (String) session.getAttribute("otpPurpose");

        if (purpose == null) {
            response.sendRedirect(request.getContextPath() + "/login");
            return;
        }

        Long expiredAt;

        if ("RESET_PASSWORD".equals(purpose)) {
            expiredAt = (Long) session.getAttribute("resetOtpExpiredAt");
        } else {
            expiredAt = (Long) session.getAttribute("otpExpiredAt");
        }

        if (expiredAt != null && System.currentTimeMillis() < expiredAt) {
            long remainingSeconds = (expiredAt - System.currentTimeMillis()) / 1000;

            request.setAttribute(
                    "error",
                    "OTP cũ vẫn còn hiệu lực. Vui lòng thử lại sau "
                    + remainingSeconds + " giây."
            );

            if ("RESET_PASSWORD".equals(purpose)) {
                request.getRequestDispatcher("/pages/confirm-reset-otp.jsp")
                        .forward(request, response);
            } else {
                request.getRequestDispatcher("/pages/verify-email.jsp")
                        .forward(request, response);
            }

            return;
        }

        String newOtp = authService.generateOtp();

        if ("VERIFY_EMAIL".equals(purpose)) {
            User verifyUser = (User) session.getAttribute("verifyUser");

            if (verifyUser == null) {
                response.sendRedirect(request.getContextPath() + "/login");
                return;
            }

            session.setAttribute("emailOtp", newOtp);
            session.setAttribute("otpExpiredAt", System.currentTimeMillis() + 5 * 60 * 1000);

            authService.sendOtp(getServletContext(), verifyUser.getEmail(), newOtp);

            request.setAttribute("success", "OTP mới đã được gửi tới email của bạn");
            request.getRequestDispatcher("/pages/verify-email.jsp").forward(request, response);
            return;
        }

        if ("RESET_PASSWORD".equals(purpose)) {
            String resetEmail = (String) session.getAttribute("resetEmail");

            if (resetEmail == null) {
                response.sendRedirect(request.getContextPath() + "/forgot-password");
                return;
            }

            session.setAttribute("resetOtp", newOtp);
            session.setAttribute("resetOtpExpiredAt", System.currentTimeMillis() + 5 * 60 * 1000);

            authService.sendOtp(getServletContext(), resetEmail, newOtp);

            request.setAttribute("success", "OTP mới đã được gửi tới email của bạn");
            request.getRequestDispatcher("/pages/confirm-reset-otp.jsp").forward(request, response);
            return;
        }

        response.sendRedirect(request.getContextPath() + "/login");
    }

    private void forgotPassword(HttpServletRequest request,
            HttpServletResponse response)
            throws ServletException, IOException {

        String email = request.getParameter("email");
        email = email == null ? "" : email.trim();

        String emailRegex = "^[A-Za-z0-9+_.-]+@[A-Za-z0-9.-]+$";

        if (!email.matches(emailRegex)) {
            request.setAttribute("error", "Email không đúng định dạng");
            request.setAttribute("email", email);
            request.getRequestDispatcher("/pages/forgot-password.jsp").forward(request, response);
            return;
        }

        User user = authService.getUserByEmail(email);

        if (user == null) {
            request.setAttribute("error", "Email không tồn tại");
            request.setAttribute("email", email);
            request.getRequestDispatcher("/pages/forgot-password.jsp").forward(request, response);
            return;
        }

        HttpSession session = request.getSession();

        authService.sendResetOtp(getServletContext(), session, user, email);

        response.sendRedirect(request.getContextPath() + "/confirm-reset-otp");
    }

    private void confirmResetOtp(HttpServletRequest request,
            HttpServletResponse response)
            throws ServletException, IOException {

        String otp = request.getParameter("otp");

        if (otp == null || !otp.matches("^\\d{6}$")) {
            request.setAttribute("error", "OTP phải gồm 6 chữ số");
            request.getRequestDispatcher("/pages/confirm-reset-otp.jsp").forward(request, response);
            return;
        }

        HttpSession session = request.getSession(false);

        if (session == null || session.getAttribute("resetUser") == null) {
            response.sendRedirect(request.getContextPath() + "/forgot-password");
            return;
        }

        String resetOtp = (String) session.getAttribute("resetOtp");
        Long expiredAt = (Long) session.getAttribute("resetOtpExpiredAt");

        if (expiredAt == null || System.currentTimeMillis() > expiredAt) {
            request.setAttribute("error", "Mã OTP đã hết hạn");
            request.getRequestDispatcher("/pages/confirm-reset-otp.jsp").forward(request, response);
            return;
        }

        if (!resetOtp.equals(otp)) {
            request.setAttribute("error", "Mã OTP không đúng");
            request.getRequestDispatcher("/pages/confirm-reset-otp.jsp").forward(request, response);
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
            request.getRequestDispatcher("/pages/reset-password.jsp").forward(request, response);
            return;
        }

        if (!newPassword.equals(confirmPassword)) {
            request.setAttribute("error", "Mật khẩu xác nhận không khớp");
            request.getRequestDispatcher("/pages/reset-password.jsp").forward(request, response);
            return;
        }

        boolean success = authService.updatePassword(
                resetUser.getId(),
                PasswordUtil.hashPassword(newPassword)
        );

        if (success) {
            session.removeAttribute("resetUser");
            session.removeAttribute("resetEmail");
            session.removeAttribute("resetOtp");
            session.removeAttribute("resetOtpExpiredAt");
            session.removeAttribute("resetOtpConfirmed");
            session.removeAttribute("otpPurpose");

            session.setAttribute("successMessage", "Đổi mật khẩu thành công!");

            response.sendRedirect(request.getContextPath() + "/login");
        } else {
            request.setAttribute("error", "Đổi mật khẩu thất bại");
            request.getRequestDispatcher("/pages/reset-password.jsp").forward(request, response);
        }
    }

    private void changePassword(HttpServletRequest request,
            HttpServletResponse response)
            throws ServletException, IOException {

        HttpSession session = request.getSession(false);

        if (session == null || session.getAttribute("user") == null) {
            response.sendRedirect(request.getContextPath() + "/login");
            return;
        }

        User user = (User) session.getAttribute("user");

        String oldPassword = request.getParameter("oldPassword");
        String newPassword = request.getParameter("newPassword");
        String confirmPassword = request.getParameter("confirmPassword");

        if (oldPassword == null || oldPassword.isEmpty()) {
            session.setAttribute("profileError", "Vui lòng nhập mật khẩu hiện tại");
            response.sendRedirect(request.getContextPath() + "/profile");
            return;
        }

        if (newPassword == null || newPassword.length() < 8) {
            session.setAttribute("profileError", "Mật khẩu mới phải có ít nhất 8 ký tự");
            response.sendRedirect(request.getContextPath() + "/profile");
            return;
        }

        if (!newPassword.equals(confirmPassword)) {
            session.setAttribute("profileError", "Xác nhận mật khẩu không khớp");
            response.sendRedirect(request.getContextPath() + "/profile");
            return;
        }

        boolean success = authService.changePassword(user, oldPassword, newPassword);

        if (success) {
            user.setPasswordHash(PasswordUtil.hashPassword(newPassword));
            session.setAttribute("user", user);
            session.setAttribute("profileSuccess", "Đổi mật khẩu thành công");
        } else {
            session.setAttribute("profileError", "Mật khẩu hiện tại không đúng");
        }

        response.sendRedirect(request.getContextPath() + "/profile");
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
                response.sendRedirect(ctx + "/CounterBooking");
                break;

            case "CUSTOMER":
            default:
                response.sendRedirect(ctx + "/home");
                break;
        }
    }

    private void keepRegisterInput(HttpServletRequest request,
            String fullName,
            String email,
            String phone) {

        request.setAttribute("fullname", fullName);
        request.setAttribute("email", email);
        request.setAttribute("phone", phone);
    }
}
