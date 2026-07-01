package controller;

import dto.BookingView;
import java.io.IOException;
import java.util.List;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;
import model.User;
import service.BookingService;

@WebServlet(name = "StaffBookingController", urlPatterns = {
    "/staff/bookings",
    "/staff/booking",
    "/staff/booking/cancel",
    "/staff/booking/check-in",
    "/staff/booking/use"
})
public class StaffBookingController extends HttpServlet {

    private final BookingService bookingService = new BookingService();

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {

        User staff = currentStaff(request, response);
        if (staff == null) {
            return;
        }

        String path = request.getServletPath();
        if ("/staff/booking".equals(path)) {
            showDetail(staff, request, response);
            return;
        }

        showList(staff, request, response);
    }

    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {

        request.setCharacterEncoding("UTF-8");

        User staff = currentStaff(request, response);
        if (staff == null) {
            return;
        }

        int bookingId = parsePositiveInt(request.getParameter("id"));
        String path = request.getServletPath();
        boolean ok;
        String msg;

        switch (path) {
            case "/staff/booking/cancel":
                ok = bookingService.cancelByStaff(bookingId, staff.getId());
                msg = ok ? "cancelled" : "cancel_failed";
                break;
            case "/staff/booking/check-in":
                ok = bookingService.checkInByStaff(bookingId, staff.getId());
                msg = ok ? "checked_in" : "checkin_failed";
                break;
            case "/staff/booking/use":
                ok = bookingService.markUsedByStaff(bookingId, staff.getId());
                msg = ok ? "used" : "use_failed";
                break;
            default:
                response.sendRedirect(request.getContextPath() + "/staff/bookings");
                return;
        }

        response.sendRedirect(request.getContextPath()
                + "/staff/booking?id=" + bookingId + "&msg=" + msg);
    }

    private void showList(User staff, HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        String keyword = request.getParameter("q");
        String status = request.getParameter("status");
        List<BookingView> bookings = bookingService.getStaffBookings(staff.getId(), keyword, status);

        request.setAttribute("bookings", bookings);
        request.setAttribute("keyword", keyword);
        request.setAttribute("selectedStatus", status);
        request.getRequestDispatcher("/pages/staff/booking-list.jsp").forward(request, response);
    }

    private void showDetail(User staff, HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        int bookingId = parsePositiveInt(request.getParameter("id"));
        BookingView booking = bookingService.getStaffDetail(bookingId, staff.getId());

        if (booking == null) {
            response.setStatus(HttpServletResponse.SC_NOT_FOUND);
            request.setAttribute("notFound", Boolean.TRUE);
        } else {
            request.setAttribute("bk", booking);
        }

        request.getRequestDispatcher("/pages/staff/booking-detail.jsp").forward(request, response);
    }

    private User currentStaff(HttpServletRequest request, HttpServletResponse response)
            throws IOException {
        HttpSession session = request.getSession(false);
        User user = session == null ? null : (User) session.getAttribute("user");
        if (user == null) {
            response.sendRedirect(request.getContextPath() + "/login");
            return null;
        }

        String role = user.getRole();
        if (!"STAFF".equalsIgnoreCase(role)
                && !"MANAGER".equalsIgnoreCase(role)
                && !"ADMIN".equalsIgnoreCase(role)) {
            response.sendRedirect(request.getContextPath() + "/home");
            return null;
        }

        return user;
    }

    private int parsePositiveInt(String value) {
        if (value == null || value.trim().isEmpty()) {
            return 0;
        }
        try {
            int parsed = Integer.parseInt(value.trim());
            return parsed > 0 ? parsed : 0;
        } catch (NumberFormatException e) {
            return 0;
        }
    }
}
