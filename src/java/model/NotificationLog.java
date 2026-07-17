/*
 * Hệ thống Quản lý Rạp chiếu phim RapViet
 * Module: Notification Management —  (Long)
 */
package model;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

/**
 * Entity ánh xạ bảng dbo.NOTIFICATIONS. Lưu từng lần gửi email (booking confirm
 * / payment confirm / promotion).
 *
 * @author LONG
 */
public class NotificationLog {

    private int notificationId;   // maps to notification_id PK
    private Integer userId;           // maps to user_id (nullable)
    private Integer bookingId;        // maps to booking_id (nullable)
    private String type;             // maps to type column (BOOKING_CONFIRM | PAYMENT_CONFIRM | PROMOTION | SYSTEM)
    private String recipientEmail;
    private String subject;          // maps to title
    private String errorMessage;     // maps to message (khi FAILED)
    private String status;           // SENT | FAILED | PENDING
    private int retryCount;
    private LocalDateTime sentAt;
    private LocalDateTime createdAt;

    // ── Constructors ─────────────────────────────────────────
    public NotificationLog() {
    }

    // ── Getters & Setters ────────────────────────────────────
    public int getNotificationId() {
        return notificationId;
    }

    public void setNotificationId(int id) {
        this.notificationId = id;
    }

    public Integer getUserId() {
        return userId;
    }

    public void setUserId(Integer userId) {
        this.userId = userId;
    }

    public Integer getBookingId() {
        return bookingId;
    }

    public void setBookingId(Integer bookingId) {
        this.bookingId = bookingId;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public String getRecipientEmail() {
        return recipientEmail;
    }

    public void setRecipientEmail(String s) {
        this.recipientEmail = s;
    }

    public String getSubject() {
        return subject;
    }

    public void setSubject(String subject) {
        this.subject = subject;
    }

    public String getErrorMessage() {
        return errorMessage;
    }

    public void setErrorMessage(String errorMessage) {
        this.errorMessage = errorMessage;
    }

    /**
     * Alias cho JSP (notification-list.jsp).
     */
    public int getId() {
        return notificationId;
    }

    public String getNotificationType() {
        return type;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String s) {
        this.status = s;
    }

    public int getRetryCount() {
        return retryCount;
    }

    public void setRetryCount(int c) {
        this.retryCount = c;
    }

    public LocalDateTime getSentAt() {
        return sentAt;
    }

    public void setSentAt(LocalDateTime t) {
        this.sentAt = t;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(LocalDateTime t) {
        this.createdAt = t;
    }

    // ── Display helpers ───────────────────────────────────────
    /**
     * Nhãn loại thông báo tiếng Việt.
     */
    public String getTypeLabel() {
        if (type == null) {
            return "";
        }
        switch (type) {
            case "BOOKING_CONFIRM":
                return "Xác nhận đặt vé";
            case "PAYMENT_CONFIRM":
                return "Xác nhận thanh toán";
            case "PROMOTION":
                return "Khuyến mãi";
            case "SYSTEM":
                return "Hệ thống";
            default:
                return type;
        }
    }

    /**
     * CSS class cho badge trạng thái (JSP).
     */
    public String getStatusBadgeClass() {
        return getStatusBadgeClassInternal();
    }

    private String getStatusBadgeClassInternal() {
        if ("SENT".equals(status)) {
            return "success";
        }
        if ("FAILED".equals(status)) {
            return "danger";
        }
        return "warning";  // PENDING
    }

    /**
     * Nhãn trạng thái tiếng Việt.
     */
    public String getStatusLabel() {
        if ("SENT".equals(status)) {
            return "Đã gửi";
        }
        if ("FAILED".equals(status)) {
            return "Thất bại";
        }
        return "Đang chờ";
    }

    /**
     * Ngày gửi dạng dd/MM/yyyy HH:mm.
     */
    public String getSentAtLabel() {
        return sentAt == null ? ""
                : sentAt.format(DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm"));
    }
}
