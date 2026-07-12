package controller;

import dao.BookingDAO;
import java.io.IOException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

@WebServlet("/payment/status")
public class PaymentStatusController extends HttpServlet {

    private final BookingDAO bookingDAO = new BookingDAO();

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response)
            throws IOException {

        response.setContentType("application/json;charset=UTF-8");

        int bookingId;
        try {
            bookingId = Integer.parseInt(request.getParameter("bookingId"));
        } catch (Exception e) {
            response.getWriter().write("{\"paid\":false}");
            return;
        }

        String status = bookingDAO.getBookingStatus(bookingId);

        boolean paid = "CONFIRMED".equalsIgnoreCase(status);

        response.getWriter().write("{\"paid\":" + paid + "}");
    }
}