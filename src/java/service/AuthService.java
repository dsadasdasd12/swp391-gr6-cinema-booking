package service;

import dto.RegisterDTO;
import dao.UserDAO;
import jakarta.servlet.ServletContext;
import jakarta.servlet.http.HttpSession;
import java.util.concurrent.CompletableFuture;
import model.User;
import util.EmailUtil;
import util.PasswordUtil;

public class AuthService {

    private static final long OTP_DURATION = 5 * 60 * 1000;

    private final UserDAO userDAO = new UserDAO();

    public User login(String email, String password) {
        return userDAO.login(email, password);
    }

    public boolean emailExists(String email) {
        return userDAO.emailExists(email);
    }

    public boolean register(User user) {
        return userDAO.register(user);
    }

    public User getUserByEmail(String email) {
        return userDAO.getUserByEmail(email);
    }

    public boolean activateEmail(int userId) {
        return userDAO.activateEmail(userId);
    }

    public boolean updatePassword(int userId, String newPasswordHash) {
        return userDAO.updatePassword(userId, newPasswordHash);
    }

    public String generateOtp() {
        return String.valueOf((int) (Math.random() * 900000) + 100000);
    }

    public void sendOtp(ServletContext ctx, String email, String otp) {
        EmailUtil.sendOtp(ctx, email, otp);
    }

    // ===== OTP ASYNC - BEGIN =====
    /**
     * Gửi OTP ở background.
     *
     * Method này trả về ngay sau khi tạo tác vụ gửi email, vì vậy Controller
     * có thể redirect người dùng tới màn xác thực mà không phải đợi SMTP.
     */
    public void sendOtpAsync(ServletContext ctx, String email, String otp) {
        CompletableFuture.runAsync(() -> {
            try {
                sendOtp(ctx, email, otp);
            } catch (Exception ex) {
                System.err.println(
                        "Không thể gửi OTP tới email " + email + ": " + ex.getMessage()
                );
                ex.printStackTrace();
            }
        });
    }
    // ===== OTP ASYNC - END =====

    public void sendVerifyOtp(ServletContext ctx, HttpSession session, User user) {
        String otp = generateOtp();

        // Lưu OTP xác thực email trước để màn verify có thể sử dụng ngay.
        createVerifyOtpSession(session, user, otp);

        // ===== OTP ASYNC - BEGIN =====
        sendOtpAsync(ctx, user.getEmail(), otp);
        // ===== OTP ASYNC - END =====
    }

    public void sendResetOtp(ServletContext ctx, HttpSession session, User user, String email) {
        String otp = generateOtp();

        // Lưu OTP reset password trước để màn confirm-reset-otp có thể sử dụng ngay.
        createResetOtpSession(session, user, email, otp);

        // ===== OTP ASYNC - BEGIN =====
        sendOtpAsync(ctx, email, otp);
        // ===== OTP ASYNC - END =====
    }

    public void createVerifyOtpSession(HttpSession session, User user, String otp) {
        session.setAttribute("verifyUser", user);
        session.setAttribute("emailOtp", otp);
        session.setAttribute("otpExpiredAt", System.currentTimeMillis() + OTP_DURATION);
        session.setAttribute("otpPurpose", "VERIFY_EMAIL");
    }

    public void createResetOtpSession(HttpSession session, User user, String email, String otp) {
        session.setAttribute("resetUser", user);
        session.setAttribute("resetEmail", email);
        session.setAttribute("resetOtp", otp);
        session.setAttribute("resetOtpExpiredAt", System.currentTimeMillis() + OTP_DURATION);
        session.setAttribute("otpPurpose", "RESET_PASSWORD");
    }

    public boolean isOtpExpired(Long expiredAt) {
        return expiredAt == null || System.currentTimeMillis() > expiredAt;
    }

    public long getRemainingSeconds(Long expiredAt) {
        if (expiredAt == null) {
            return 0;
        }

        return Math.max(0, (expiredAt - System.currentTimeMillis()) / 1000);
    }

    public void clearVerifyOtpSession(HttpSession session) {
        session.removeAttribute("emailOtp");
        session.removeAttribute("otpExpiredAt");
        session.removeAttribute("otpPurpose");
        session.removeAttribute("verifyUser");
    }

    public void clearResetOtpSession(HttpSession session) {
        session.removeAttribute("resetUser");
        session.removeAttribute("resetEmail");
        session.removeAttribute("resetOtp");
        session.removeAttribute("resetOtpExpiredAt");
        session.removeAttribute("resetOtpConfirmed");
        session.removeAttribute("otpPurpose");
    }

    public boolean changePassword(User user, String oldPassword, String newPassword) {
        if (user == null) {
            return false;
        }

        if (!PasswordUtil.verifyPassword(oldPassword, user.getPasswordHash())) {
            return false;
        }

        String hashedNewPassword = PasswordUtil.hashPassword(newPassword);

        return userDAO.updatePassword(user.getId(), hashedNewPassword);
    }

    public String validateRegister(RegisterDTO dto) {

        if (dto.getFullName() == null
                || dto.getFullName().trim().isEmpty()) {
            return "Họ tên không được để trống";
        }

        if (dto.getFullName().length() < 2
                || dto.getFullName().length() > 50) {
            return "Họ tên phải từ 2 đến 50 ký tự";
        }

        if (!dto.getFullName()
                .matches("^[\\p{L} ]+$")) {
            return "Họ tên chỉ được chứa chữ cái";
        }

        if (dto.getEmail() == null
                || dto.getEmail().trim().isEmpty()) {
            return "Email không được để trống";
        }

        if (emailExists(dto.getEmail())) {
            return "Email đã tồn tại";
        }

        if (dto.getPhone() != null
                && !dto.getPhone().isBlank()
                && !dto.getPhone().matches("0\\d{9,10}")) {

            return "Số điện thoại không hợp lệ";
        }

        if (dto.getPassword() == null
                || dto.getPassword().length() < 8) {

            return "Mật khẩu phải có ít nhất 8 ký tự";
        }

        if (!dto.getPassword()
                .equals(dto.getConfirmPassword())) {

            return "Mật khẩu xác nhận không khớp";
        }

        return null;
    }

    public User buildUser(RegisterDTO dto) {

        User user = new User();

        user.setFullName(dto.getFullName().trim());
        user.setEmail(dto.getEmail().trim());
        user.setPhone(dto.getPhone());

        user.setGoogleId("local_" + dto.getEmail().trim());

        user.setPasswordHash(
                PasswordUtil.hashPassword(dto.getPassword())
        );

        user.setRole("CUSTOMER");
        user.setActive(true);
        user.setEmailVerified(false);

        return user;
    }
}
