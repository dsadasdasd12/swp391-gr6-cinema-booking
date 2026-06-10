/*
 * Hệ thống Quản lý Rạp chiếu phim RapViet
 * Module: Notification Management —  (Long)
 */
package service;

import javax.mail.Message;
import javax.mail.MessagingException;
import javax.mail.Session;
import javax.mail.Transport;
import javax.mail.internet.InternetAddress;
import javax.mail.internet.MimeMessage;

/**
 * Dịch vụ gửi email qua JavaMail 1.6.2.
 * Chỉ chịu trách nhiệm gửi — không xử lý retry hay logging.
 * Caller (NotificationService) sẽ bọc retry + ghi log.
 *
 * @author LONG
 */
public class EmailService {

    /**
     * Gửi một email HTML.
     *
     * @param session  JavaMail Session đã cấu hình (từ EmailUtil)
     * @param from     email người gửi
     * @param to       email người nhận
     * @param subject  tiêu đề
     * @param htmlBody nội dung HTML
     * @throws MessagingException nếu gửi thất bại (caller xử lý retry)
     */
    public void send(Session session, String from, String to,
                     String subject, String htmlBody) throws MessagingException {
        MimeMessage msg = new MimeMessage(session);
        msg.setFrom(new InternetAddress(from));
        msg.setRecipient(Message.RecipientType.TO, new InternetAddress(to));
        msg.setSubject(subject, "UTF-8");
        msg.setContent(htmlBody, "text/html; charset=UTF-8");
        Transport.send(msg);
    }
}
