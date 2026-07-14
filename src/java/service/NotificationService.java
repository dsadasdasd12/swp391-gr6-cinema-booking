/*
 * Hệ thống Quản lý Rạp chiếu phim RapViet
 * Module: Notification Management —  (Long)
 */
package service;

import dao.NotificationLogDAO;
import dao.TicketDAO;
import jakarta.servlet.ServletContext;
import dto.PageResult;
import model.NotificationLog;
import model.Ticket;
import util.EmailUtil;

import jakarta.mail.MessagingException;
import jakarta.mail.Session;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.List;
import util.DBContext;

/**
 * Dịch vụ gửi thông báo (email) cho hệ thống RapViet.
 *
 *  — Gửi xác nhận đặt vé / thanh toán:
 *   - Tự động gọi sau khi booking confirmed / payment success
 *   - Retry tối đa 3 lần, log mỗi lần thử
 *
 *  — Gửi thông báo khuyến mãi:
 *   - Admin tạo nội dung + chọn đối tượng
 *   - Gửi batch, log từng người nhận
 *
 * @author LONG
 */
public class NotificationService {

    private static final int MAX_EMAIL_RETRIES = 3;

    private final EmailService        emailService = new EmailService();
    private final NotificationLogDAO  logDAO       = new NotificationLogDAO();
    private final TicketDAO           ticketDAO    = new TicketDAO();

    // ═══════════════════════════════════════════════════════════
    //  — Booking / Payment Confirmation
    // ═══════════════════════════════════════════════════════════

    /**
     * Gửi email xác nhận đặt vé cho khách hàng.
     * Được gọi từ TicketService sau khi sinh e-ticket thành công.
     *
     * @param bookingId id booking đã confirmed
     * @param ctx       ServletContext (cần cho email config)
     */
    public void sendBookingConfirmation(int bookingId, ServletContext ctx) {
        // Lấy thông tin booking bằng một truy vấn join
        BookingInfo info = loadBookingInfo(bookingId);
        if (info == null) {
            System.getLogger(NotificationService.class.getName())
                    .log(System.Logger.Level.WARNING, "Không tìm thấy booking #" + bookingId);
            return;
        }

        // Load ticket (nếu có)
        Ticket ticket = ticketDAO.findByBookingId(bookingId);
        String ticketLink = (ticket != null)
                ? ctx.getContextPath() + "/admin/tickets?action=detail&bookingId=" + ticket.getBookingId()
                : "#";

        String subject = "RapViet — Xác nhận đặt vé: " + info.movieTitle;
        String body = buildBookingEmailHtml(info, ticket, ticketLink);

        // Tạo log entry trước → trạng thái PENDING
        NotificationLog log = new NotificationLog();
        log.setUserId(info.userId);
        log.setBookingId(bookingId);
        log.setType("BOOKING_CONFIRM");
        log.setSubject(NotificationLogDAO.bookingTitlePrefix(bookingId) + " — " + info.movieTitle);
        log.setRecipientEmail(info.email);
        log.setStatus("PENDING");
        log.setRetryCount(0);
        logDAO.insert(log);

        // Đọc lại ID của log để cập nhật trạng thái sau này
        // Do insert không trả về id tự sinh, chúng ta lấy log mới nhất của email này
        List<NotificationLog> logs = logDAO.findByBookingId(bookingId);
        if (!logs.isEmpty()) {
            log.setNotificationId(logs.get(0).getNotificationId());
        }

        // Gửi với retry
        sendWithRetry(ctx, info.email, subject, body, log);
    }

    /**
     * Gửi email xác nhận thanh toán.
     */
    public void sendPaymentConfirmation(int bookingId, String amount, ServletContext ctx) {
        BookingInfo info = loadBookingInfo(bookingId);
        if (info == null) return;

        String subject = "RapViet — Thanh toán thành công: " + amount + " VNĐ";
        String body = "<div style='font-family:Arial,sans-serif;max-width:600px;margin:0 auto;'>"
                + "<h2 style='color:#e50914;'>Thanh toán thành công!</h2>"
                + "<p>Xin chào <strong>" + esc(info.customerName) + "</strong>,</p>"
                + "<p>Thanh toán <strong>" + esc(amount) + " VNĐ</strong> cho booking #" + bookingId
                + " đã được xác nhận.</p>"
                + "<p>Phim: <strong>" + esc(info.movieTitle) + "</strong></p>"
                + "<p>Cảm ơn bạn đã sử dụng RapViet Cinema!</p>"
                + "</div>";

        NotificationLog log = new NotificationLog();
        log.setUserId(info.userId);
        log.setBookingId(bookingId);
        log.setType("PAYMENT_CONFIRM");
        log.setSubject(NotificationLogDAO.bookingTitlePrefix(bookingId) + " — Thanh toán " + amount + " VNĐ");
        log.setRecipientEmail(info.email);
        log.setStatus("PENDING");
        log.setRetryCount(0);
        logDAO.insert(log);

        List<NotificationLog> logs = logDAO.findByBookingId(bookingId);
        if (!logs.isEmpty()) {
            log.setNotificationId(logs.get(0).getNotificationId());
        }

        sendWithRetry(ctx, info.email, subject, body, log);
    }

    /**
     * Gửi email thông báo đăng ký tài khoản thành công.
     */
    public void sendRegistrationSuccess(int userId, String customerName, String email, ServletContext ctx) {
        String subject = "RapViet — Đăng ký tài khoản thành công";
        String body = "<div style='font-family:Arial,sans-serif;max-width:600px;margin:0 auto;'>"
                + "<h2 style='color:#e50914;'>Chào mừng đến với RapViet Cinema!</h2>"
                + "<p>Xin chào <strong>" + esc(customerName) + "</strong>,</p>"
                + "<p>Tài khoản của bạn đã được đăng ký và kích hoạt thành công.</p>"
                + "<p>Giờ đây bạn có thể đăng nhập để bắt đầu đặt vé xem phim.</p>"
                + "<p>Cảm ơn bạn đã tham gia RapViet Cinema!</p>"
                + "</div>";

        NotificationLog log = new NotificationLog();
        log.setUserId(userId);
        log.setBookingId(0);
        log.setType("REGISTRATION");
        log.setSubject(subject);
        log.setRecipientEmail(email);
        log.setStatus("PENDING");
        log.setRetryCount(0);
        logDAO.insert(log);

        List<NotificationLog> logs = logDAO.findAll();
        for (NotificationLog l : logs) {
            if (l.getUserId() == userId && "REGISTRATION".equals(l.getType()) && "PENDING".equals(l.getStatus())) {
                log.setNotificationId(l.getNotificationId());
                break;
            }
        }

        sendWithRetry(ctx, email, subject, body, log);
    }

    /**
     * Ghi log sự kiện hệ thống (ví dụ: xoá phim) mà không gửi email.
     */
    public void logSystemNotification(int userId, String eventDesc, String adminEmail) {
        NotificationLog log = new NotificationLog();
        log.setUserId(userId);
        log.setBookingId(0);
        log.setType("SYSTEM_EVENT");
        log.setSubject("HỆ THỐNG: " + eventDesc);
        log.setRecipientEmail(adminEmail != null ? adminEmail : "system@rapviet.vn");
        log.setStatus("SENT"); // Không gửi email thực, chỉ log
        log.setRetryCount(0);
        logDAO.insert(log);
    }

    // ═══════════════════════════════════════════════════════════
    //  — Promotion / Batch Emails
    // ═══════════════════════════════════════════════════════════

    /**
     * Gửi email khuyến mãi hàng loạt.
     * Mỗi email được log riêng (SENT / FAILED per recipient).
     *
     * @param emails    danh sách email người nhận
     * @param subject   tiêu đề
     * @param htmlBody  nội dung HTML
     * @param ctx       ServletContext
     * @return số email gửi thành công
     */
    public int sendPromotion(List<String> emails, String subject, String htmlBody, ServletContext ctx) {
        Session session = EmailUtil.getMailSession(ctx);
        String from     = EmailUtil.getFromEmail(ctx);
        int successCount = 0;

        for (String email : emails) {
            NotificationLog log = new NotificationLog();
            log.setType("PROMOTION");
            log.setSubject(subject);
            log.setRecipientEmail(email);
            log.setStatus("PENDING");
            log.setRetryCount(0);

            boolean sent = false;

            for (int attempt = 1; attempt <= MAX_EMAIL_RETRIES; attempt++) {
                try {
                    emailService.send(session, from, email, subject, htmlBody);
                    sent = true;
                    log.setRetryCount(attempt);
                    break;
                } catch (MessagingException e) {
                    log.setRetryCount(attempt);
                    System.getLogger(NotificationService.class.getName())
                            .log(System.Logger.Level.WARNING,
                                 "Promo email attempt " + attempt + " failed for " + email, e);
                }
            }

            if (sent) {
                log.setStatus("SENT");
                successCount++;
            } else {
                log.setStatus("FAILED");
            }
            // Chỉ ghi log một lần duy nhất sau khi kết thúc các lượt thử
            logDAO.insert(log);
        }

        return successCount;
    }

    /**
     * Lấy tất cả email khách hàng đang active.
     */
    public List<String> getAllCustomerEmails() {
        List<String> emails = new java.util.ArrayList<>();
        String sql = "SELECT email FROM dbo.[USER] WHERE role='CUSTOMER' AND active=1";
        Connection conn = DBContext.getInstance().getConnection();
        try (PreparedStatement ps = conn.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {
            while (rs.next()) {
                emails.add(rs.getString("email"));
            }
        } catch (SQLException e) {
            System.getLogger(NotificationService.class.getName())
                    .log(System.Logger.Level.ERROR, "getAllCustomerEmails thất bại", e);
        }
        return emails;
    }

    /**
     * Lấy toàn bộ log để hiển thị trang admin.
     */
    public List<NotificationLog> getAllLogs() {
        return logDAO.findAll();
    }

    /**
     * Lấy trang log để hiển thị trang admin.
     */
    public PageResult<NotificationLog> getLogsPaged(String keyword, String type, String status, String sortField, String sortOrder,
                                                     int page, int pageSize) {
        int safePage = page < 1 ? 1 : page;
        int safeSize = pageSize < 1 ? 10 : pageSize;
        int offset = (safePage - 1) * safeSize;

        long total = logDAO.countLogs(keyword, type, status);
        List<NotificationLog> items = logDAO.findPaged(keyword, type, status, sortField, sortOrder, offset, safeSize);
        return new PageResult<>(items, total, safePage, safeSize);
    }

    // ═══════════════════════════════════════════════════════════
    // Private helpers
    // ═══════════════════════════════════════════════════════════

    /** Retry gửi email tối đa MAX_EMAIL_RETRIES lần, cập nhật log sau mỗi lần. */
    private void sendWithRetry(ServletContext ctx, String to,
                               String subject, String body, NotificationLog log) {
        Session session = EmailUtil.getMailSession(ctx);
        String  from    = EmailUtil.getFromEmail(ctx);

        for (int attempt = 1; attempt <= MAX_EMAIL_RETRIES; attempt++) {
            try {
                emailService.send(session, from, to, subject, body);
                log.setStatus("SENT");
                log.setRetryCount(attempt);
                logDAO.updateStatus(log.getNotificationId(), "SENT", attempt);
                return; // thành công → thoát
            } catch (MessagingException e) {
                log.setRetryCount(attempt);
                logDAO.updateStatus(log.getNotificationId(), "FAILED", attempt);
                System.getLogger(NotificationService.class.getName())
                        .log(System.Logger.Level.WARNING,
                             "Email attempt " + attempt + "/" + MAX_EMAIL_RETRIES
                             + " to " + to + " failed: " + e.getMessage());
            }
        }
    }

    /**
     * Load thông tin booking tối thiểu bằng một truy vấn join.
     */
    private BookingInfo loadBookingInfo(int bookingId) {
        String sql = "SELECT b.id AS booking_id, b.user_id, u.full_name, u.email, m.title AS movie_title, "
                + "       FORMAT(s.start_time, 'HH:mm dd/MM/yyyy') AS showtime_str, "
                + "       b.total_price "
                + "FROM dbo.BOOKINGS b "
                + "JOIN dbo.[USER]    u ON u.id = b.user_id "
                + "JOIN dbo.SHOWTIMES s ON s.id = b.showtime_id "
                + "JOIN dbo.MOVIES    m ON m.id = s.movie_id "
                + "WHERE b.id = ?";
        Connection conn = DBContext.getInstance().getConnection();
        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, bookingId);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    BookingInfo info = new BookingInfo();
                    info.bookingId    = rs.getInt("booking_id");
                    info.userId       = rs.getInt("user_id");
                    info.customerName = rs.getString("full_name");
                    info.email        = rs.getString("email");
                    info.movieTitle   = rs.getString("movie_title");
                    info.showtimeStr  = rs.getString("showtime_str");
                    info.totalPrice   = rs.getBigDecimal("total_price").toPlainString();
                    return info;
                }
            }
        } catch (SQLException e) {
            System.getLogger(NotificationService.class.getName())
                    .log(System.Logger.Level.ERROR, "loadBookingInfo thất bại", e);
        }
        return null;
    }

    /** DTO nội bộ cho dữ liệu booking dùng khi soạn email. */
    private static class BookingInfo {
        String customerName;
        String email;
        String movieTitle;
        String showtimeStr;
        String totalPrice;
        int    bookingId;
        int    userId;
    }

    /** Soạn HTML email xác nhận booking. */
    private String buildBookingEmailHtml(BookingInfo info, Ticket ticket, String ticketLink) {
        StringBuilder sb = new StringBuilder();
        sb.append("<div style='font-family:Arial,sans-serif;max-width:600px;margin:0 auto;")
          .append("background:#1a1d27;color:#e8eaf0;border-radius:12px;overflow:hidden;'>");
        // Header
        sb.append("<div style='background:#e50914;padding:1.5rem 2rem;text-align:center;'>")
          .append("<h1 style='color:#fff;margin:0;font-size:1.5rem;'>🎬 RapViet Cinema</h1>")
          .append("<p style='color:rgba(255,255,255,.8);margin:.25rem 0 0;font-size:.9rem;'>Xác nhận đặt vé</p>")
          .append("</div>");
        // Body
        sb.append("<div style='padding:2rem;'>");
        sb.append("<p>Xin chào <strong>").append(esc(info.customerName)).append("</strong>,</p>");
        sb.append("<p>Bạn đã đặt vé thành công!</p>");
        sb.append("<table style='width:100%;border-collapse:collapse;margin:1rem 0;'>");
        sb.append(infoRow("Phim", info.movieTitle));
        sb.append(infoRow("Suất chiếu", info.showtimeStr));
        sb.append(infoRow("Tổng tiền", info.totalPrice + " VNĐ"));
        sb.append(infoRow("Mã booking", "#" + info.bookingId));
        sb.append("</table>");
        // QR Code inline (nếu có)
        if (ticket != null && ticket.getQrCodeBase64() != null) {
            sb.append("<div style='text-align:center;margin:1.5rem 0;'>");
            sb.append("<img src='data:image/png;base64,").append(ticket.getQrCodeBase64())
              .append("' alt='QR Code' style='width:200px;border-radius:8px;border:2px solid #333;'>");
            sb.append("<p style='font-size:.8rem;color:#8b8fa8;margin-top:.5rem;'>Xuất trình mã QR tại quầy soát vé</p>");
            sb.append("</div>");
        }
        sb.append("<p style='font-size:.85rem;color:#8b8fa8;margin-top:1.5rem;'>")
          .append("Cảm ơn bạn đã sử dụng RapViet Cinema!<br>")
          .append("Nếu cần hỗ trợ, vui lòng liên hệ hotline hoặc reply email này.</p>");
        sb.append("</div></div>");
        return sb.toString();
    }

    /** Một dòng thông tin trong email (label: value). */
    private String infoRow(String label, String value) {
        return "<tr><td style='padding:.5rem .75rem;color:#8b8fa8;font-size:.85rem;border-bottom:1px solid #333;'>"
                + esc(label)
                + "</td><td style='padding:.5rem .75rem;font-weight:600;font-size:.9rem;border-bottom:1px solid #333;'>"
                + esc(value)
                + "</td></tr>";
    }

    /** Escape HTML tối thiểu cho email. */
    private String esc(String s) {
        if (s == null) return "";
        return s.replace("&", "&amp;").replace("<", "&lt;").replace(">", "&gt;");
    }
}
