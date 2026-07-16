package controller;

import dto.BookingView;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;
import model.User;
import service.BookingService;
import dao.UserDAO;

import java.io.IOException;
import java.util.List;

@WebServlet("/admin/bookings")
public class AdminCustomerBookingServlet extends HttpServlet {

    private final BookingService bookingService = new BookingService();
    private final UserDAO userDAO = new UserDAO();

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp)
            throws ServletException, IOException {

        HttpSession session = req.getSession(false);
        if (session == null || (session.getAttribute("adminUser") == null && 
            (session.getAttribute("user") == null || !"ADMIN".equals(((User)session.getAttribute("user")).getRole())))) {
            resp.sendRedirect(req.getContextPath() + "/login");
            return;
        }

        String customerIdStr = req.getParameter("customerId");
        if (customerIdStr == null || customerIdStr.trim().isEmpty()) {
            resp.sendRedirect(req.getContextPath() + "/admin/accounts/customers");
            return;
        }

        int customerId;
        try {
            customerId = Integer.parseInt(customerIdStr);
        } catch (NumberFormatException e) {
            resp.sendRedirect(req.getContextPath() + "/admin/accounts/customers");
            return;
        }

        User customer = userDAO.getUserById(customerId);
        if (customer == null) {
            resp.sendError(HttpServletResponse.SC_NOT_FOUND, "Không tìm thấy khách hàng");
            return;
        }

        String status = req.getParameter("status");

        List<BookingView> bookings = bookingService.getHistory(customerId, status);
        
        req.setAttribute("customer", customer);
        req.setAttribute("bookings", bookings);
        req.setAttribute("statusFilter", status);

        req.getRequestDispatcher("/pages/admin/customer-bookings.jsp").forward(req, resp);
    }
}
