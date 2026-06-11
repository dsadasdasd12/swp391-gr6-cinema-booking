package service;

import dao.UserDAO;
import jakarta.servlet.http.HttpSession;
import model.User;
import util.EmailUtil;
import util.PasswordUtil;

public class AuthService {

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

    public boolean updatePassword(int userId, String newPassword) {
        return userDAO.updatePassword(userId, newPassword);
    }

    public String generateOtp() {
        return String.valueOf((int) (Math.random() * 900000) + 100000);
    }

    public void sendOtp(String email, String otp) {
        EmailUtil.sendOtp(email, otp);
    }

    public void createVerifyOtpSession(HttpSession session, User user, String otp) {
        session.setAttribute("verifyUser", user);
        session.setAttribute("emailOtp", otp);
        session.setAttribute(
                "otpExpiredAt",
                System.currentTimeMillis() + 5 * 60 * 1000
        );
        session.setAttribute("otpPurpose", "VERIFY_EMAIL");
    }

    public void createResetOtpSession(HttpSession session, User user, String email, String otp) {
        session.setAttribute("resetUser", user);
        session.setAttribute("resetEmail", email);
        session.setAttribute("resetOtp", otp);
        session.setAttribute(
                "resetOtpExpiredAt",
                System.currentTimeMillis() + 5 * 60 * 1000
        );
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
}