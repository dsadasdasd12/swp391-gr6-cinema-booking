/*
 * Hệ thống Quản lý Rạp chiếu phim RapViet
 * Module: Đặt vé - Xem chi tiết + Hủy đơn (Booking details / Cancel) - KHÁCH
 */
package controller;


import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;

import java.io.IOException;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import dto.BookingView;
import model.User;
import service.BookingService;

/**
 * Hiển thị chi tiết một đơn đặt vé của khách (kèm theo dõi trạng thái) và xử lý
 * yêu cầu khách tự hủy đơn.
 * <p>
 * URL: {@code /my-booking?id=<bookingId>} — GET xem chi tiết, POST (id) để hủy.
 * Bắt buộc đăng nhập và chỉ thao tác trên đơn của chính khách.
 *
 * @author Group6 - Huy (Module Đặt vé)
 */
@WebServlet(name = "BookingDetailController", urlPatterns = {"/my-booking"})
public class BookingDetailController extends HttpServlet {
        private static final String BANK_CODE = "BIDV";
private static final String ACCOUNT_NO = "4860555705";
private static final String ACCOUNT_NAME = "TRAN THE TRUONG";

    private final BookingService bookingService = new BookingService();

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {

        User user = currentUser(request);
        if (user == null) {
            response.sendRedirect(request.getContextPath() + "/login");
            return;
        }

        int id = parseId(request.getParameter("id"));
        BookingView booking = bookingService.getDetail(id, user.getId());

        if (booking == null) {
            // Không tồn tại hoặc không phải đơn của khách -> 404 thân thiện
            response.setStatus(HttpServletResponse.SC_NOT_FOUND);
            request.setAttribute("notFound", Boolean.TRUE);
        } else {
            request.setAttribute("bk", booking);
            
            String transferContent = "RV" + booking.getBooking().getId();

    String paymentQr =
            "https://img.vietqr.io/image/"
            + BANK_CODE + "-"
            + ACCOUNT_NO
            + "-compact2.png"
            + "?amount=" + (long) booking.getBooking().getTotalPrice()
            + "&addInfo=" + URLEncoder.encode(transferContent, StandardCharsets.UTF_8)
            + "&accountName=" + URLEncoder.encode(ACCOUNT_NAME, StandardCharsets.UTF_8);

    request.setAttribute("paymentQr", paymentQr);
    request.setAttribute("transferContent", transferContent);
        }
        
        request.getRequestDispatcher("/pages/booking/detail.jsp").forward(request, response);
    }

    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {

        User user = currentUser(request);
        if (user == null) {
            response.sendRedirect(request.getContextPath() + "/login");
            return;
        }

        // Chỉ hỗ trợ hành động hủy đơn
        int id = parseId(request.getParameter("id"));
        boolean ok = bookingService.cancel(id, user.getId());

        // Quay lại trang chi tiết kèm thông báo kết quả
        String msg = ok ? "cancelled" : "cancel_failed";
        response.sendRedirect(request.getContextPath() + "/my-booking?id=" + id + "&msg=" + msg);
    }

    /** Lấy người dùng đang đăng nhập (tái dùng helper ở BookingHistoryController). */
    private static User currentUser(HttpServletRequest request) {
        return BookingHistoryController.currentUser(request);
    }

    /** Đọc id từ tham số, trả -1 nếu thiếu/sai định dạng. */
    private static int parseId(String s) {
        if (s == null || s.isBlank()) {
            return -1;
        }
        try {
            return Integer.parseInt(s.trim());
        } catch (NumberFormatException e) {
            return -1;
        }
    }
}
