/*
 * Hệ thống Quản lý Rạp chiếu phim RapViet
 * Module: E-Ticket & QR Code —  (Long)
 */
package service;

import com.google.zxing.BarcodeFormat;
import com.google.zxing.EncodeHintType;
import com.google.zxing.WriterException;
import com.google.zxing.client.j2se.MatrixToImageWriter;
import com.google.zxing.common.BitMatrix;
import com.google.zxing.qrcode.QRCodeWriter;
import com.google.zxing.qrcode.decoder.ErrorCorrectionLevel;

import java.awt.image.BufferedImage;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.Base64;
import java.util.EnumMap;
import java.util.Map;
import javax.imageio.ImageIO;

/**
 * Dịch vụ sinh mã QR sử dụng thư viện ZXing 3.5.2.
 *
 * Đặc tả:
 *   - Định dạng: QR_CODE
 *   - Kích thước: 300 × 300 px
 *   - Error correction: M (phục hồi tối đa 15% dữ liệu bị hỏng)
 *   - Output: BufferedImage → PNG bytes → Base64 string
 *
 * Cách dùng trong JSP:
 *   {@code <img src="data:image/png;base64,${ticket.qrCodeBase64}"> }
 *
 * @author LONG
 */
public class QRCodeService {

    private static final int QR_SIZE = 300;    // px

    /**
     * Sinh mã QR từ chuỗi nội dung và trả về dạng Base64 PNG.
     * Được gọi từ TicketService với retry wrapper (tối đa 3 lần).
     *
     * @param content nội dung cần mã hóa (thường là ticketUuid)
     * @return chuỗi Base64 của ảnh PNG QR, hoặc null nếu thất bại
     * @throws WriterException nếu ZXing không thể mã hóa
     * @throws IOException     nếu không thể ghi ảnh ra stream
     */
    public String generateQRBase64(String content) throws WriterException, IOException {
        // ── Cài đặt hint cho QR encoder ──────────────────────
        Map<EncodeHintType, Object> hints = new EnumMap<>(EncodeHintType.class);
        hints.put(EncodeHintType.ERROR_CORRECTION, ErrorCorrectionLevel.M);
        hints.put(EncodeHintType.CHARACTER_SET, "UTF-8");
        hints.put(EncodeHintType.MARGIN, 2);    // quiet zone (modules), mặc định = 4

        // ── Sinh BitMatrix ────────────────────────────────────
        QRCodeWriter writer    = new QRCodeWriter();
        BitMatrix    bitMatrix = writer.encode(content, BarcodeFormat.QR_CODE, QR_SIZE, QR_SIZE, hints);

        // ── Chuyển BitMatrix → BufferedImage ─────────────────
        BufferedImage qrImage = MatrixToImageWriter.toBufferedImage(bitMatrix);

        // ── Render ra byte array PNG ──────────────────────────
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        ImageIO.write(qrImage, "PNG", baos);

        // ── Encode Base64 ─────────────────────────────────────
        return Base64.getEncoder().encodeToString(baos.toByteArray());
    }
}
