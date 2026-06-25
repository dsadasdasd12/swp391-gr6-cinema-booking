/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package util;

import jakarta.mail.Authenticator;
import jakarta.mail.Message;
import jakarta.mail.PasswordAuthentication;
import jakarta.mail.Session;
import jakarta.mail.Transport;
import jakarta.mail.internet.InternetAddress;
import jakarta.mail.internet.MimeMessage;

import java.util.Properties;

public class EmailUtil {

    private static final String FROM_EMAIL = "aare7220@gmail.com";
    private static final String APP_PASSWORD = "qsrx wbqy udid oqlj";

    public static void sendOtp(String toEmail, String otp) {

        try {
            Properties props = new Properties();

            props.put("mail.smtp.auth", "true");
            props.put("mail.smtp.starttls.enable", "true");
            props.put("mail.smtp.host", "smtp.gmail.com");
            props.put("mail.smtp.port", "587");

            Session session = Session.getInstance(
                    props,
                    new Authenticator() {
                        @Override
                        protected PasswordAuthentication getPasswordAuthentication() {
                            return new PasswordAuthentication(
                                    FROM_EMAIL,
                                    APP_PASSWORD
                            );
                        }
                    }
            );

            Message message = new MimeMessage(session);

            message.setFrom(new InternetAddress(FROM_EMAIL));
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
            e.printStackTrace();
        }
    }
}