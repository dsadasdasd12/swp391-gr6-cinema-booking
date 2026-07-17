package util;

import com.google.zxing.WriterException;
import dao.TicketDAO;
import service.QRCodeService;

import java.io.IOException;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

/**
 * Sinh mã QR thật (ZXing) cho các booking chưa có QR hoặc QR placeholder cũ.
 */
public final class BookingQrSeedService {

    private static final String PLACEHOLDER_PREFIX = "iVBORw0KGgoAAAANSUhEUgAAAIAAAACAAQMAAAD58NuI";

    private BookingQrSeedService() {
    }

    public static void generateMissingQrCodes(Connection conn) {
        if (conn == null) {
            return;
        }
        QRCodeService qrService = new QRCodeService();
        TicketDAO ticketDAO = new TicketDAO();
        int count = 0;

        String sql = "SELECT id, qr_code FROM dbo.BOOKINGS "
                + "WHERE status IN ('CONFIRMED','CHECKED_IN','USED','COMPLETED') "
                + "AND (qr_code IS NULL OR LEN(qr_code) < 200 OR qr_code LIKE ?)";

        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, PLACEHOLDER_PREFIX + "%");
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    int bookingId = rs.getInt("id");
                    String content = "RAPVIET-BOOKING-" + bookingId;
                    try {
                        String base64 = qrService.generateQRBase64(content);
                        if (base64 != null && base64.length() > 200) {
                            ticketDAO.updateQrCode(bookingId, base64);
                            count++;
                        }
                    } catch (WriterException | IOException e) {
                        System.err.println("==> QR booking #" + bookingId + ": " + e.getMessage());
                    }
                }
            }
        } catch (SQLException e) {
            System.err.println("==> Lỗi quét booking cho QR: " + e.getMessage());
            return;
        }
        if (count > 0) {
            System.out.println("==> Đã sinh QR cho " + count + " booking.");
        }
    }
}
