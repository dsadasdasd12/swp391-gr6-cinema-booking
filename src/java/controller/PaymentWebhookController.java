package controller;

import service.BookingService;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

@WebServlet(name = "PaymentWebhookController", urlPatterns = {"/PaymentWebhook"})
public class PaymentWebhookController extends HttpServlet {

    private final BookingService bookingService = new BookingService();

    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        response.setContentType("application/json;charset=UTF-8");
        PrintWriter out = response.getWriter();

        // 1. Đọc body của request (JSON payload từ SePay)
        StringBuilder sb = new StringBuilder();
        String line;
        try (BufferedReader reader = request.getReader()) {
            while ((line = reader.readLine()) != null) {
                sb.append(line);
            }
        }
        String jsonPayload = sb.toString();

        System.out.println("=== SEPAY WEBHOOK RECEIVED ===");
        System.out.println("Payload: " + jsonPayload);

        try {
            // 2. Phân tích các trường JSON bằng Regex (không dùng thư viện ngoài)
            String transferType = extractJsonField(jsonPayload, "transferType");
            String gateway = extractJsonField(jsonPayload, "gateway");
            String transactionContent = extractJsonField(jsonPayload, "transactionContent");
            if (transactionContent == null) {
                transactionContent = extractJsonField(jsonPayload, "content"); // Fallback
            }
            
            String transferAmountStr = extractJsonField(jsonPayload, "transferAmount");
            if (transferAmountStr == null) {
                transferAmountStr = extractJsonField(jsonPayload, "amount"); // Fallback
            }
            
            String referenceCode = extractJsonField(jsonPayload, "referenceCode");
            if (referenceCode == null || "null".equals(referenceCode)) {
                referenceCode = extractJsonField(jsonPayload, "id"); // Fallback dùng ID giao dịch SePay
            }

            // Mặc định transferType là "in" nếu không khai báo để dễ test
            if (transferType != null && !transferType.trim().isEmpty() && !"in".equalsIgnoreCase(transferType)) {
                System.out.println("Ignored: transferType is not 'in' (outbound transfer).");
                out.write("{\"success\":false,\"message\":\"Only inbound transfers are processed\"}");
                return;
            }

            if (transactionContent == null || transactionContent.trim().isEmpty()) {
                System.out.println("Error: transactionContent is empty.");
                out.write("{\"success\":false,\"message\":\"Missing transaction content\"}");
                return;
            }

            // 3. Trích xuất bookingId từ nội dung giao dịch (dạng RVS123 hoặc RVS 123)
            int bookingId = extractBookingId(transactionContent);
            if (bookingId == -1) {
                System.out.println("Error: Cannot extract bookingId from content: " + transactionContent);
                out.write("{\"success\":false,\"message\":\"Invalid transaction code syntax\"}");
                return;
            }

            double amount = 0;
            if (transferAmountStr != null && !transferAmountStr.trim().isEmpty() && !"null".equals(transferAmountStr)) {
                amount = Double.parseDouble(transferAmountStr);
            }

            if (gateway == null || "null".equals(gateway)) {
                gateway = "SePay Gateway";
            }

            if (referenceCode == null || "null".equals(referenceCode)) {
                referenceCode = "SP-TX-" + System.currentTimeMillis();
            }

            System.out.println("Processing Payment Confirmation:");
            System.out.println("- Booking ID: " + bookingId);
            System.out.println("- Amount: " + amount);
            System.out.println("- Gateway: " + gateway);
            System.out.println("- Reference Code: " + referenceCode);

            // 4. Xác nhận thanh toán & cập nhật trạng thái cơ sở dữ liệu
            boolean updated = bookingService.confirmPayment(bookingId, referenceCode, amount, gateway);

            if (updated) {
                System.out.println("Payment CONFIRMED successfully for Booking ID: " + bookingId);
                out.write("{\"success\":true,\"message\":\"Payment confirmed successfully\"}");
            } else {
                System.out.println("Failed to confirm payment for Booking ID: " + bookingId + " (Booking may not exist or database error)");
                out.write("{\"success\":false,\"message\":\"Failed to update database status\"}");
            }

        } catch (Exception e) {
            e.printStackTrace();
            System.out.println("Error processing webhook: " + e.getMessage());
            out.write("{\"success\":false,\"message\":\"Internal error: " + e.getMessage() + "\"}");
        }
    }

    private String extractJsonField(String json, String fieldName) {
        // Regex tìm trường JSON dạng "key": "value" hoặc "key": value
        Pattern pattern = Pattern.compile(
            "\"" + fieldName + "\"[\\s]*:[\\s]*(?:\"([^\"]*)\"|([0-9.-]+|true|false|null))"
        );
        Matcher matcher = pattern.matcher(json);
        if (matcher.find()) {
            if (matcher.group(1) != null) {
                return matcher.group(1);
            }
            return matcher.group(2);
        }
        return null;
    }

    private int extractBookingId(String content) {
        if (content == null) return -1;
        // Tìm chữ RVS/RVS và các con số liền sau (có thể chứa khoảng trắng)
        Pattern pattern = Pattern.compile("RVS\\s*([0-9]+)", Pattern.CASE_INSENSITIVE);
        Matcher matcher = pattern.matcher(content);
        if (matcher.find()) {
            try {
                return Integer.parseInt(matcher.group(1));
            } catch (NumberFormatException e) {
                return -1;
            }
        }
        return -1;
    }
}
