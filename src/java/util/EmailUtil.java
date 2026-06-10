/*
 * Hệ thống Quản lý Rạp chiếu phim RapViet
 * Module: Notification Management —  (Long)
 */
package util;

import jakarta.servlet.ServletContext;
import java.util.Properties;
import javax.mail.Authenticator;
import javax.mail.PasswordAuthentication;
import javax.mail.Session;

/**
 * Utility đọc cấu hình SMTP từ web.xml context-param và tạo JavaMail Session.
 *
 * Các context-param cần thiết trong web.xml:
 *   mail.smtp.host     — SMTP server (ví dụ: smtp.gmail.com)
 *   mail.smtp.port     — cổng (587 cho TLS, 465 cho SSL)
 *   mail.smtp.auth     — "true" nếu cần đăng nhập
 *   mail.smtp.user     — email gửi
 *   mail.smtp.password — mật khẩu hoặc app-password
 *
 * @author LONG
 */
public class EmailUtil {

    /** Không cho khởi tạo — chỉ dùng static. */
    private EmailUtil() {}

    /**
     * Tạo javax.mail.Session từ context-param trong web.xml.
     * Mỗi lần gọi sẽ đọc lại config (chi phí nhỏ; đảm bảo hot-reload nếu thay đổi).
     *
     * @param ctx ServletContext chứa context-param
     * @return Session đã được cấu hình SMTP
     */
    public static Session getMailSession(ServletContext ctx) {
        String host     = getParam(ctx, "mail.smtp.host",     "smtp.gmail.com");
        String port     = getParam(ctx, "mail.smtp.port",     "587");
        String auth     = getParam(ctx, "mail.smtp.auth",     "true");
        String user     = getParam(ctx, "mail.smtp.user",     "");
        String password = getParam(ctx, "mail.smtp.password", "");

        Properties props = new Properties();
        props.put("mail.smtp.host",            host);
        props.put("mail.smtp.port",            port);
        props.put("mail.smtp.auth",            auth);
        props.put("mail.smtp.starttls.enable", "true");   // TLS cho port 587
        props.put("mail.smtp.ssl.trust",       host);     // tin SMTP server
        props.put("mail.debug",                "false");

        // Nếu port = 465 → dùng SSL thay cho STARTTLS
        if ("465".equals(port)) {
            props.put("mail.smtp.socketFactory.port",  "465");
            props.put("mail.smtp.socketFactory.class", "javax.net.ssl.SSLSocketFactory");
            props.put("mail.smtp.starttls.enable",     "false");
        }

        if ("true".equalsIgnoreCase(auth)) {
            final String u = user;
            final String p = password;
            return Session.getInstance(props, new Authenticator() {
                @Override
                protected PasswordAuthentication getPasswordAuthentication() {
                    return new PasswordAuthentication(u, p);
                }
            });
        } else {
            return Session.getInstance(props);
        }
    }

    /**
     * Đọc email "From" từ web.xml (mail.smtp.user).
     */
    public static String getFromEmail(ServletContext ctx) {
        return getParam(ctx, "mail.smtp.user", "noreply@rapviet.vn");
    }

    // ── Helpers ───────────────────────────────────────────────

    /**
     * Đọc context-param, trả defaultValue nếu null/blank.
     */
    private static String getParam(ServletContext ctx, String name, String defaultValue) {
        Object attr = ctx.getAttribute(name);
        if (attr instanceof String s && !s.isBlank()) {
            return s.trim();
        }
        String val = ctx.getInitParameter(name);
        return (val != null && !val.isBlank()) ? val.trim() : defaultValue;
    }
}
