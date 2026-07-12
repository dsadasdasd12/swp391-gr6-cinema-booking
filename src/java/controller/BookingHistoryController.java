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
 * Controller cho luồng xem lịch sử đặt vé của khách hàng.
 *
 * <p>Luồng UI:</p>
 * <ul>
 *   <li>User mở GET {@code /my-bookings} từ menu lịch sử đặt vé.</li>
 *   <li>Controller kiểm tra session; nếu chưa đăng nhập thì redirect {@code /login}.</li>
 *   <li>Gọi {@link BookingService#getHistory(int)} theo userId hiện tại.</li>
 *   <li>Đẩy danh sách booking sang {@code /pages/booking/history.jsp}.</li>
 *   <li>Từ mỗi dòng booking, user có thể mở chi tiết hoặc review nếu đủ điều kiện.</li>
 * </ul>
 *
 * @author HuyPD
 */
@WebServlet(name = "BookingHistoryController", urlPatterns = {"/my-bookings"})
public class BookingHistoryController extends HttpServlet {

    /*
     * Service gom logic nghiep vu cua booking.
     * Controller chi dieu phoi request/response, khong tu xu ly SQL.
     */
    private final BookingService bookingService = new BookingService();

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {

        /*
         * Lay user dang dang nhap tu session.
         * Neu chua dang nhap thi khong duoc xem lich su booking.
         */
        User user = currentUser(request);
        if (user == null) {
            // sendRedirect tao request moi ve trang login tren browser.
            response.sendRedirect(request.getContextPath() + "/login");
            return;
        }

        /*
         * Lay lich su booking theo userId.
         * Viec truyen user.getId() giup tranh loi user A xem duoc booking cua user B.
         */
        request.setAttribute("bookings", bookingService.getHistory(user.getId()));

        /*
         * forward giu nguyen request hien tai va dua attribute "bookings" sang JSP.
         * JSP se dung attribute nay de render bang lich su dat ve.
         */
        request.getRequestDispatcher("/pages/booking/history.jsp").forward(request, response);
    }

    /**
     * Lay nguoi dung dang dang nhap tu session.
     *
     * Tung buoc:
     * 1. request.getSession(false): chi lay session neu da ton tai, khong tao session moi.
     * 2. Neu khong co session thi user chua dang nhap.
     * 3. Lay attribute "user" do AuthController set sau khi login thanh cong.
     * 4. Chi return khi object trong session that su la User.
     */
    static User currentUser(HttpServletRequest request) {
        HttpSession session = request.getSession(false);
        if (session == null) {
            return null;
        }
        Object u = session.getAttribute("user");
        return (u instanceof User) ? (User) u : null;
    }
}
