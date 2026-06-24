/*
 * Hệ thống Quản lý Rạp chiếu phim RapViet
 * Module: E-Ticket & QR Code —  (Long)
 */
package model;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

/**
 * View object for e-ticket display. QR data comes from dbo.BOOKINGS.qr_code.
 * No separate TICKETS table exists in schema.
 *
 * @author LONG
 */
public class Ticket {

    private int           id;
    private int           bookingId;
    private String        ticketUuid;       // UUID dạng chuỗi, e.g. "550e8400-e29b-..."
    private String        qrCodeBase64;     // Base64 của ảnh PNG QR 300×300
    private boolean       isUsed;           // đã quét tại cổng chưa
    private String        ticketStatus;     // ISSUED | USED | PENDING_MANUAL
    private LocalDateTime createdAt;
    private LocalDateTime lastUpdate;

    // ── Trường hiển thị bổ sung (join với BOOKINGS / MOVIES khi cần) ──
    private String customerName;
    private String customerEmail;
    private String movieTitle;
    private String showtimeStart;   // chuỗi hiển thị HH:mm dd/MM/yyyy
    
    // ── Thêm trường theo Fix 4 ──
    private String qrCode;           // maps to dbo.BOOKINGS.qr_code
    private String bookingStatus;    // maps to dbo.BOOKINGS.status

    // ── Constructors ─────────────────────────────────────────
    public Ticket() {}

    // ── Getters & Setters ────────────────────────────────────

    public int getId()                       { return id; }
    public void setId(int id)                { this.id = id; }

    public int getBookingId()                { return bookingId; }
    public void setBookingId(int bookingId)  { this.bookingId = bookingId; }

    public String getTicketUuid()            { return ticketUuid; }
    public void setTicketUuid(String u)      { this.ticketUuid = u; }

    public String getQrCodeBase64()          { return qrCodeBase64; }
    public void setQrCodeBase64(String q)    { this.qrCodeBase64 = q; }

    public boolean isUsed()                  { return isUsed; }
    public void setUsed(boolean used)        { this.isUsed = used; }

    public String getTicketStatus()          { return ticketStatus; }
    public void setTicketStatus(String s)    { this.ticketStatus = s; }

    public LocalDateTime getCreatedAt()      { return createdAt; }
    public void setCreatedAt(LocalDateTime t){ this.createdAt = t; }

    public LocalDateTime getLastUpdate()     { return lastUpdate; }
    public void setLastUpdate(LocalDateTime t){ this.lastUpdate = t; }

    // ── Display fields ────────────────────────────────────────
    public String getCustomerName()          { return customerName; }
    public void setCustomerName(String s)    { this.customerName = s; }

    public String getCustomerEmail()         { return customerEmail; }
    public void setCustomerEmail(String s)   { this.customerEmail = s; }

    public String getMovieTitle()            { return movieTitle; }
    public void setMovieTitle(String s)      { this.movieTitle = s; }

    public String getShowtimeStart()         { return showtimeStart; }
    public void setShowtimeStart(String s)   { this.showtimeStart = s; }

    public String getQrCode()                { return qrCode; }
    public void setQrCode(String qrCode)     { this.qrCode = qrCode; }

    public String getBookingStatus()         { return bookingStatus; }
    public void setBookingStatus(String s)   { this.bookingStatus = s; }

    // ── Display helpers ───────────────────────────────────────

    /** Nhãn trạng thái tiếng Việt. */
    public String getStatusLabel() {
        if (ticketStatus == null) return "";
        switch (ticketStatus) {
            case "ISSUED":         return "Đã phát hành";
            case "USED":           return "Đã sử dụng";
            case "PENDING_MANUAL": return "Chờ xử lý thủ công";
            default:               return ticketStatus;
        }
    }

    /** CSS class cho badge trạng thái. */
    public String getStatusBadgeClass() {
        if ("ISSUED".equals(ticketStatus))         return "success";
        if ("USED".equals(ticketStatus))           return "secondary";
        if ("PENDING_MANUAL".equals(ticketStatus)) return "danger";
        return "secondary";
    }

    /** Ngày tạo định dạng dd/MM/yyyy HH:mm. */
    public String getCreatedAtLabel() {
        return createdAt == null ? ""
                : createdAt.format(DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm"));
    }
}
