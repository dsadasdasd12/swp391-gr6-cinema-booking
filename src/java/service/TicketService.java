package service;

import dao.AttendanceDAO;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import util.DBContext;
import dao.TicketDAO;
import jakarta.servlet.ServletContext;
import dto.PageResult;
import model.Ticket;

import java.util.List;


public class TicketService {
    private final AttendanceDAO attendanceDAO = new AttendanceDAO();

    public String checkInTicket(int bookingId, int staffId) {
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
        if (bookingIdStr == null || bookingIdStr.trim().isEmpty()) {
            throw new NumberFormatException("Empty input");
        }
        String cleanIdStr = bookingIdStr.trim();
        if (cleanIdStr.contains("bookingId=")) {
            int index = cleanIdStr.indexOf("bookingId=");
            cleanIdStr = cleanIdStr.substring(index + 10);
            int ampersandIndex = cleanIdStr.indexOf("&");
            if (ampersandIndex != -1) {
                cleanIdStr = cleanIdStr.substring(0, ampersandIndex);
            }
        } else if (cleanIdStr.toUpperCase().startsWith("RV-WALK-")) {
            cleanIdStr = cleanIdStr.substring(8);
        } else if (cleanIdStr.toUpperCase().startsWith("TICKET-")) {
            cleanIdStr = cleanIdStr.substring(7);
        }
        return Integer.parseInt(cleanIdStr.trim());
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
 *   2. Gọi QRCodeService.generateQRBase64("RAPVIET-BOOKING-" + bookingId) tối đa 3 lần
 *   3a. Thành công → lưu ticket trực tiếp vào BOOKINGS.qr_code → trigger email
 *   3b. Tất cả thất bại → lưu null và xử lý thủ công sau
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
        // Kiểm tra xem booking này đã có ticket chưa (tránh tạo trùng)
        Ticket existing = ticketDAO.findByBookingId(bookingId);
        if (existing != null && existing.getQrCodeBase64() != null) {
            return existing;
        }

        String qrContent = "RAPVIET-BOOKING-" + bookingId;
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

        // ── Lưu vào DB ────────────────────────────────────────
        boolean saved = ticketDAO.updateBookingQR(bookingId, qrBase64);
        if (!saved) {
            System.getLogger(TicketService.class.getName()).log(
                    System.Logger.Level.ERROR,
                    "Không thể lưu ticket cho booking " + bookingId);
            if (!qrSuccess) {
                ticketDAO.updateQrCode(bookingId, null);
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

        return ticketDAO.findByBookingId(bookingId);
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
        return ticketDAO.findAll();
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

        return new PageResult<>(items, total, safePage, safeSize);
    }

    /**
     * Lấy chi tiết ticket theo id (bookingId).
     */
    public Ticket getTicketById(int id) {
        return ticketDAO.findById(id);
    }

    /**
     * Lấy ticket theo booking id.
     */
    public Ticket getTicketByBookingId(int bookingId) {
        return ticketDAO.findByBookingId(bookingId);
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
        String qrContent = "RAPVIET-BOOKING-" + bookingId;
        for (int attempt = 1; attempt <= MAX_QR_RETRIES; attempt++) {
            try {
                String qrBase64 = qrCodeService.generateQRBase64(qrContent);
                return ticketDAO.updateQrCode(bookingId, qrBase64);
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
}
