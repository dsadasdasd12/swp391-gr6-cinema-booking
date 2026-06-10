package util;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;

/**
 * Hash mật khẩu SHA-256 (hex) — dùng chung cho login, đăng ký, seed DB.
 */
public final class PasswordUtil {

    private PasswordUtil() {}

    public static String hashPassword(String password) {
        if (password == null) return "";
        try {
            MessageDigest digest = MessageDigest.getInstance("SHA-256");
            byte[] hash = digest.digest(password.getBytes(StandardCharsets.UTF_8));
            StringBuilder hex = new StringBuilder();
            for (byte b : hash) {
                String h = Integer.toHexString(0xff & b);
                if (h.length() == 1) hex.append('0');
                hex.append(h);
            }
            return hex.toString();
        } catch (Exception e) {
            return password;
        }
    }

    /** So khớp mật khẩu nhập với giá trị trong DB (plain hoặc SHA-256). */
    public static boolean matches(String plainPassword, String storedHash) {
        if (plainPassword == null || storedHash == null) return false;
        String plain = plainPassword.trim();
        String stored = storedHash.trim();
        if (plain.isEmpty() || stored.isEmpty()) return false;
        if (plain.equals(stored)) return true;
        return hashPassword(plain).equalsIgnoreCase(stored);
    }
}
