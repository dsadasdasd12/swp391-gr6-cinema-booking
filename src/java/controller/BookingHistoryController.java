/*
 * Hệ thống Quản lý Rạp chiếu phim RapViet
 * Module: Đặt vé - Xem lịch sử đặt vé (Booking history) - KHÁCH
 */
package controller;

import java.io.IOException;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;
import model.User;
import service.BookingService;

/**
 * Hiển thị lịch sử đặt vé của khách đang đăng nhập (mỗi đơn gồm phim, suất
 * chiếu, ghế, tổng tiền và trạng thái). Bắt buộc đăng nhập: chưa đăng nhập thì
 * chuyển về trang /login.
 * <p>
 * URL: {@code /my-bookings} (GET).
 *
 * @author Group6 - Huy (Module Đặt vé)
 */
@WebServlet(name = "BookingHistoryController", urlPatterns = {"/my-bookings"})
public class BookingHistoryController extends HttpServlet {

    private final BookingService bookingService = new BookingService();

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {

        // Bắt buộc đăng nhập
        User user = currentUser(request);
        if (user == null) {
            response.sendRedirect(request.getContextPath() + "/login");
            return;
        }

        request.setAttribute("bookings", bookingService.getHistory(user.getId()));
        request.getRequestDispatcher("/pages/booking/history.jsp").forward(request, response);
    }

    /** Lấy người dùng đang đăng nhập từ session (do module auth đặt vào). */
    static User currentUser(HttpServletRequest request) {
        HttpSession session = request.getSession(false);
        if (session == null) {
            return null;
        }
        Object u = session.getAttribute("user");
        return (u instanceof User) ? (User) u : null;
    }
}
