package service;

import dao.AttendanceDAO;
import dto.AttendanceHistoryView;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import util.DBContext;
import dao.TicketDAO;
import jakarta.servlet.ServletContext;
import dto.PageResult;
import model.Ticket;

import java.util.List;
import java.time.LocalDate;


public class TicketService {
    private final AttendanceDAO attendanceDAO = new AttendanceDAO();

    public String checkInTicket(int bookingId, int staffId) {
        // AttendanceDAO kiểm tra branch, thời gian và cập nhật trạng thái check-in nguyên tử.
        return attendanceDAO.checkInTicket(bookingId, staffId);
    }

    public String getSeatCodesByBookingId(int bookingId) {
        StringBuilder seatCodes = new StringBuilder();
        String sql = "SELECT s.seat_row, s.seat_number FROM dbo.BOOKING_SEATS bs "
                   + "JOIN dbo.SEATS s ON bs.seat_id = s.id WHERE bs.booking_id = ?";
        try (Connection conn = new DBContext().getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, bookingId);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    seatCodes.append(rs.getString("seat_row")).append(rs.getInt("seat_number")).append(" ");
                }
            }
        } catch (Exception ex) {
            ex.printStackTrace();
        }
        return seatCodes.toString().trim();
    }

    public int parseBookingId(String bookingIdStr) throws NumberFormatException {
        // Chỉ chấp nhận token QR có tiền tố được hệ thống phát hành, không nhận trực tiếp id booking.
        if (bookingIdStr == null || bookingIdStr.trim().isEmpty()) {
            throw new NumberFormatException("Empty input");
        }
        String cleanIdStr = bookingIdStr.trim();
        
        // Only signed ticket tokens are accepted; a URL/id alone must never check a ticket in.
        // Chuẩn hóa để mã quét không phân biệt hoa/thường, nhưng token DB vẫn được đối chiếu chính xác.
        String upper = cleanIdStr.toUpperCase();
        int resolvedId = -1;
        String expectedDbQrCode = null;
        
        // Mỗi định dạng hợp lệ đều quy về id booking và qr_code chuẩn đang lưu trong BOOKINGS.
        if (upper.startsWith("RAPVIET-BOOKING-")) {
            try {
                resolvedId = Integer.parseInt(cleanIdStr.substring(16).trim());
                expectedDbQrCode = "RV-ONLINE-" + resolvedId;
            } catch (NumberFormatException e) {}
        } else if (upper.startsWith("RV-ONLINE-")) {
            try {
                resolvedId = Integer.parseInt(cleanIdStr.substring(10).trim());
                expectedDbQrCode = "RV-ONLINE-" + resolvedId;
            } catch (NumberFormatException e) {}
        } else if (upper.startsWith("RV-WALK-")) {
            try {
                resolvedId = Integer.parseInt(cleanIdStr.substring(8).trim());
                expectedDbQrCode = "RV-WALK-" + resolvedId;
            } catch (NumberFormatException e) {}
        } else if (upper.startsWith("TICKET-")) {
            try {
                resolvedId = Integer.parseInt(cleanIdStr.substring(7).trim());
                expectedDbQrCode = "RV-ONLINE-" + resolvedId;
            } catch (NumberFormatException e) {}
        }
        
        // Nếu không khớp bất kỳ tiền tố hợp lệ nào
        if (resolvedId == -1 || expectedDbQrCode == null) {
            throw new NumberFormatException("Mã vé không đúng định dạng. Vui lòng quét mã QR hoặc nhập đầy đủ tiền tố (ví dụ: RV-ONLINE-5, RV-WALK-1).");
        }
        
        // 3. Truy vấn từ Database để xác nhận mã vé khớp hoàn toàn với cột qr_code
        // Đây là bước bắt buộc: id suy ra từ QR chỉ hợp lệ khi qr_code trong DB khớp hoàn toàn.
        String sql = "SELECT id FROM dbo.BOOKINGS WHERE id = ? AND qr_code = ?";
        try (Connection conn = new util.DBContext().getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, resolvedId);
            ps.setString(2, expectedDbQrCode);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return rs.getInt("id");
                }
            }
        } catch (Exception ex) {
            ex.printStackTrace();
        }
        
        throw new NumberFormatException("Mã vé không tồn tại hoặc không khớp với thông tin trong hệ thống.");
    }
/*
 * Hệ thống Quản lý Rạp chiếu phim RapViet
 * Module: E-Ticket & QR Code —  (Long)
 */



/**
 * Business logic cho việc tạo và quản lý e-ticket.
 *
 * Flow chính:
 *   1. PaymentController gọi generateTicket(bookingId, customerEmail, ctx)
 *   2. Tạo ảnh QR từ token đã lưu ở BOOKINGS.qr_code
 *   3. Gửi ảnh QR qua email; token trong database không bị thay đổi
 *
 * @author LONG
 */
    private static final int MAX_QR_RETRIES = 3;

    private final TicketDAO       ticketDAO       = new TicketDAO();
    private final QRCodeService   qrCodeService   = new QRCodeService();
    private       NotificationService notifService;

    public TicketService() {}

    // Constructor cho injection thủ công (hoặc test)
    public TicketService(NotificationService notifService) {
        this.notifService = notifService;
    }

    // ── Public API ────────────────────────────────────────────

    /**
     * Tạo e-ticket sau khi thanh toán xác nhận.
     *
     * @param bookingId     id booking đã được confirm
     * @param customerEmail email để gửi e-ticket
     * @param ctx           ServletContext (cần cho email config)
     * @return ticket vừa tạo
     */
    public Ticket generateTicket(int bookingId, String customerEmail, ServletContext ctx) {
        Ticket existing = ticketDAO.findByBookingId(bookingId);
        if (existing == null) return null;
        String qrContent = existing.getQrCode();
        if (qrContent == null || qrContent.isBlank()) qrContent = "RV-ONLINE-" + bookingId;
        String qrBase64   = null;
        boolean qrSuccess = false;

        // ── Retry QR generation tối đa MAX_QR_RETRIES lần ────
        for (int attempt = 1; attempt <= MAX_QR_RETRIES; attempt++) {
            try {
                qrBase64  = qrCodeService.generateQRBase64(qrContent);
                qrSuccess = true;
                break;
            } catch (Exception e) {
                System.getLogger(TicketService.class.getName()).log(
                        System.Logger.Level.WARNING,
                        "QR generation attempt " + attempt + "/" + MAX_QR_RETRIES
                        + " failed for booking " + bookingId + ": " + e.getMessage());
            }
        }

        // ── Gửi email thông báo (nếu QR thành công) ──────────
        if (qrSuccess && ctx != null) {
            try {
                getNotifService().sendBookingConfirmation(bookingId, ctx);
            } catch (Exception e) {
                System.getLogger(TicketService.class.getName()).log(
                        System.Logger.Level.WARNING,
                        "Gửi email xác nhận thất bại cho booking " + bookingId, e);
            }
        }

        existing.setQrCode(qrContent);
        existing.setQrCodeBase64(qrBase64);
        return existing;
    }

    /**
     * Hoàn tất luồng sau thanh toán: xác nhận booking, gửi email thanh toán, sinh e-ticket + email đặt vé.
     * Gọi từ quầy (walk-in) hoặc PaymentController khi tích hợp thanh toán online.
     */
    public Ticket issueTicketAfterPayment(int bookingId, ServletContext ctx) {
        ticketDAO.confirmBooking(bookingId);
        Ticket existing = ticketDAO.findByBookingId(bookingId);
        if (existing == null) {
            return null;
        }
        double total = ticketDAO.getBookingTotalPrice(bookingId);
        String amount = String.format("%,.0f", total);
        if (ctx != null) {
            try {
                getNotifService().sendPaymentConfirmation(bookingId, amount, ctx);
            } catch (Exception e) {
                System.getLogger(TicketService.class.getName()).log(
                        System.Logger.Level.WARNING,
                        "Gửi email thanh toán thất bại cho booking " + bookingId, e);
            }
        }
        String email = existing.getCustomerEmail();
        return generateTicket(bookingId, email, ctx);
    }

    /**
     * Lấy danh sách tất cả ticket (trang admin).
     */
    public List<Ticket> getAllTickets() {
        List<Ticket> tickets = ticketDAO.findAll();
        for (Ticket ticket : tickets) hydrateQrImage(ticket);
        return tickets;
    }

    /**
     * Lấy trang ticket theo keyword + status.
     */
    public PageResult<Ticket> getTicketsPaged(String keyword, String statusFilter, int page, int pageSize) {
        int safePage = page < 1 ? 1 : page;
        int safeSize = pageSize < 1 ? 10 : pageSize;
        int offset = (safePage - 1) * safeSize;

        long total = ticketDAO.countTickets(keyword, statusFilter);
        List<Ticket> items = ticketDAO.findPaged(keyword, statusFilter, offset, safeSize);
        for (Ticket ticket : items) hydrateQrImage(ticket);

        return new PageResult<>(items, total, safePage, safeSize);
    }

    /**
     * Lấy chi tiết ticket theo id (bookingId).
     */
    public Ticket getTicketById(int id) {
        Ticket ticket = ticketDAO.findById(id);
        hydrateQrImage(ticket);
        return ticket;
    }

    /**
     * Lấy ticket theo booking id.
     */
    public Ticket getTicketByBookingId(int bookingId) {
        Ticket ticket = ticketDAO.findByBookingId(bookingId);
        hydrateQrImage(ticket);
        return ticket;
    }

    /**
     * Quét QR → đánh dấu ticket đã sử dụng (booking COMPLETED).
     */
    public boolean useTicket(int bookingId) {
        Ticket t = ticketDAO.findByBookingId(bookingId);
        if (t == null || "COMPLETED".equals(t.getBookingStatus())) return false;
        return ticketDAO.markUsed(bookingId);
    }

    /**
     * Retry sinh QR cho ticket PENDING_MANUAL.
     */
    public boolean retryQrGeneration(int bookingId) {
        Ticket ticket = ticketDAO.findByBookingId(bookingId);
        if (ticket == null || ticket.getQrCode() == null || ticket.getQrCode().isBlank()) return false;
        String qrContent = ticket.getQrCode();
        for (int attempt = 1; attempt <= MAX_QR_RETRIES; attempt++) {
            try {
                qrCodeService.generateQRBase64(qrContent);
                return true;
            } catch (Exception e) {
                System.getLogger(TicketService.class.getName()).log(
                        System.Logger.Level.WARNING,
                        "Retry QR attempt " + attempt + " failed for booking " + bookingId, e);
            }
        }
        return false;
    }

    // ── Private helpers ───────────────────────────────────────

    private NotificationService getNotifService() {
        if (notifService == null) {
            notifService = new NotificationService();
        }
        return notifService;
    }

    /** Lấy lịch sử check-in của một staff trong ngày được chọn. */
    public List<AttendanceHistoryView> getCheckInHistory(int staffId, LocalDate checkedDate,
            int offset, int pageSize) {
        if (staffId <= 0) {
            return List.of();
        }
        return attendanceDAO.getHistoryByStaff(staffId, checkedDate, offset, pageSize);
    }

    public int countCheckInHistory(int staffId, LocalDate checkedDate) {
        return staffId <= 0 ? 0 : attendanceDAO.countHistoryByStaff(staffId, checkedDate);
    }

    private void hydrateQrImage(Ticket ticket) {
        if (ticket == null || ticket.getQrCode() == null || ticket.getQrCode().isBlank()) return;
        try {
            ticket.setQrCodeBase64(qrCodeService.generateQRBase64(ticket.getQrCode()));
        } catch (Exception e) {
            System.getLogger(TicketService.class.getName()).log(System.Logger.Level.WARNING,
                    "Không thể tạo ảnh QR cho booking " + ticket.getBookingId(), e);
        }
    }
}
