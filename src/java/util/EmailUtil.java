package util;

import jakarta.mail.Authenticator;
import jakarta.mail.Message;
import jakarta.mail.PasswordAuthentication;
import jakarta.mail.Session;
import jakarta.mail.Transport;
import jakarta.mail.internet.InternetAddress;
import jakarta.mail.internet.MimeMessage;
import jakarta.servlet.ServletContext;

import java.util.Properties;

public class EmailUtil {

    public static void sendOtp(ServletContext ctx, String toEmail, String otp) {
        try {
            Session session = getMailSession(ctx);
            String fromEmail = getFromEmail(ctx);

            Message message = new MimeMessage(session);

            message.setFrom(new InternetAddress(fromEmail, "RapViet Cinema"));
            message.setRecipients(
                    Message.RecipientType.TO,
                    InternetAddress.parse(toEmail)
            );

            message.setSubject("RapViet Cinema - Email Verification");

            message.setText(
                    "Xin chào,\n\n"
                    + "Mã xác thực email của bạn là: " + otp + "\n\n"
                    + "Mã này có hiệu lực trong 5 phút.\n\n"
                    + "RapViet Cinema"
            );

            Transport.send(message);

        } catch (Exception e) {
            throw new RuntimeException("Không thể gửi email OTP.", e);
        }
    }

    public static Session getMailSession(ServletContext ctx) {
        String host = getParam(ctx, "mail.smtp.host", "smtp.gmail.com");
        String port = getParam(ctx, "mail.smtp.port", "587");
        String auth = getParam(ctx, "mail.smtp.auth", "true");
        String user = getParam(ctx, "mail.smtp.user", "");
        String password = getParam(ctx, "mail.smtp.password", "");

        Properties props = new Properties();

        props.put("mail.smtp.host", host);
        props.put("mail.smtp.port", port);
        props.put("mail.smtp.auth", auth);
        props.put("mail.smtp.starttls.enable", "true");
        props.put("mail.smtp.ssl.trust", host);
        props.put("mail.debug", "false");

        if ("465".equals(port)) {
            props.put("mail.smtp.socketFactory.port", "465");
            props.put("mail.smtp.socketFactory.class", "javax.net.ssl.SSLSocketFactory");
            props.put("mail.smtp.starttls.enable", "false");
        }

        if ("true".equalsIgnoreCase(auth)) {
            return Session.getInstance(props, new Authenticator() {
                @Override
                protected PasswordAuthentication getPasswordAuthentication() {
                    return new PasswordAuthentication(user, password);
                }
            });
        }

        return Session.getInstance(props);
    }

    public static String getFromEmail(ServletContext ctx) {
        return getParam(ctx, "mail.smtp.user", "noreply@rapviet.vn");
    }

    private static String getParam(ServletContext ctx, String name, String defaultValue) {
        String val = ctx.getInitParameter(name);
        return (val != null && !val.isBlank()) ? val.trim() : defaultValue;
    }
}
