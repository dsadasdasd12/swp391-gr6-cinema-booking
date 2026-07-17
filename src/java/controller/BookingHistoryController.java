package controller;

import java.io.IOException;
import java.time.LocalDate;
import java.time.format.DateTimeParseException;
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
 * <p>
 * Luồng UI:</p>
 * <ul>
 * <li>User mở GET {@code /my-bookings} từ menu lịch sử đặt vé.</li>
 * <li>Controller kiểm tra session; nếu chưa đăng nhập thì redirect
 * {@code /login}.</li>
 * <li>Gọi {@link BookingService#getHistory(int)} theo userId hiện tại.</li>
 * <li>Đẩy danh sách booking sang {@code /pages/booking/history.jsp}.</li>
 * <li>Từ mỗi dòng booking, user có thể mở chi tiết hoặc review nếu đủ điều
 * kiện.</li>
 * </ul>
 *
 * @author HuyPD
 */
@WebServlet(name = "BookingHistoryController", urlPatterns = {"/my-bookings"})
public class BookingHistoryController extends HttpServlet {

    /**
     * Mỗi trang chỉ hiển thị 6 đơn để bảng không quá dài, đồng thời vẫn có thể demo
     * rõ cơ chế phân trang. Giá trị này được dùng cùng lúc ở phép đếm và câu SQL lấy trang.
     */
    private static final int PAGE_SIZE = 6;

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
         * Đọc filter từ query string, ví dụ:
         * /my-bookings?status=CONFIRMED&fromDate=2026-07-01&toDate=2026-07-31&page=2.
         * Dùng GET thay vì session để người dùng refresh, bookmark hoặc chuyển trang
         * vẫn giữ đúng bộ lọc. parseDate/parsePositiveInt chặn giá trị sai trước khi gọi Service.
         */
        String status = request.getParameter("status");
        LocalDate fromDate = parseDate(request.getParameter("fromDate"));
        LocalDate toDate = parseDate(request.getParameter("toDate"));
        int page = Math.max(1, parsePositiveInt(request.getParameter("page"), 1));

        /*
         * Mọi lời gọi đều truyền user.getId() lấy từ session, không lấy userId từ URL.
         * Vì vậy khách A không thể đổi tham số trên browser để đọc lịch sử booking của khách B.
         *
         * countHistory(...) gọi BookingService -> BookingDAO -> SELECT COUNT(*) với cùng filter
         * như danh sách. totalBookings là tổng sau filter status; totalAllBookings bỏ filter status
         * để badge tab "Tất cả" luôn phản ánh đúng tổng đơn trong khoảng ngày đang chọn.
         */
        int totalBookings = bookingService.countHistory(user.getId(), status, fromDate, toDate);
        int totalAllBookings = bookingService.countHistory(user.getId(), null, fromDate, toDate);
        int totalPages = Math.max(1, (int) Math.ceil(totalBookings / (double) PAGE_SIZE));
        if (page > totalPages) {
            page = totalPages;
        }

        /*
         * getHistoryPage(...) trả List<BookingView> đã join phim/rap/phòng/ghế.
         * Các attribute này là dữ liệu đầu vào duy nhất của history.jsp:
         * - bookings: từng dòng của bảng;
         * - statusCounts: số đơn theo từng trạng thái để vẽ tab;
         * - selectedStatus/fromDate/toDate/page: giữ trạng thái filter và phân trang trên UI.
         */
        request.setAttribute("bookings", bookingService.getHistoryPage(
                user.getId(), status, fromDate, toDate, page, PAGE_SIZE));
        request.setAttribute("statusCounts", bookingService.getHistoryStatusCounts(user.getId(), fromDate, toDate));
        request.setAttribute("selectedStatus", bookingService.normalizeHistoryStatus(status));
        request.setAttribute("fromDate", fromDate == null ? "" : fromDate.toString());
        request.setAttribute("toDate", toDate == null ? "" : toDate.toString());
        request.setAttribute("currentPage", page);
        request.setAttribute("totalPages", totalPages);
        request.setAttribute("totalBookings", totalBookings);
        request.setAttribute("totalAllBookings", totalAllBookings);

        /*
         * forward giu nguyen request hien tai va dua attribute "bookings" sang JSP.
         * JSP se dung attribute nay de render bang lich su dat ve.
         */
        request.getRequestDispatcher("/pages/booking/history.jsp").forward(request, response);
    }

    /**
     * Lay nguoi dung dang dang nhap tu session.
     *
     * Tung buoc: 1. request.getSession(false): chi lay session neu da ton tai,
     * khong tao session moi. 2. Neu khong co session thi user chua dang nhap.
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

    /**
     * Chuyển chuỗi từ input type="date" (yyyy-MM-dd) thành LocalDate.
     * Nếu người dùng tự sửa URL thành ngày không hợp lệ thì trả null, nghĩa là không áp dụng filter ngày,
     * thay vì làm controller ném lỗi 500.
     */
    private static LocalDate parseDate(String raw) {
        if (raw == null || raw.isBlank()) {
            return null;
        }
        try {
            return LocalDate.parse(raw.trim());
        } catch (DateTimeParseException ignored) {
            return null;
        }
    }

    /**
     * Phân trang cũng không tin trực tiếp giá trị từ browser. Chỉ nhận số nguyên dương;
     * các giá trị như page=-1 hoặc page=abc sẽ quay về trang mặc định.
     */
    private static int parsePositiveInt(String raw, int defaultValue) {
        try {
            int value = Integer.parseInt(raw);
            return value > 0 ? value : defaultValue;
        } catch (NumberFormatException ignored) {
            return defaultValue;
        }
    }
}
