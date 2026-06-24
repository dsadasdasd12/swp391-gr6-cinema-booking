package controller;

import service.TicketService;
import service.BookingService;
import service.ShowtimeService;
import java.io.IOException;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import model.Showtime;

@WebServlet(name = "TicketValidationController", urlPatterns = {"/TicketValidation"})
public class TicketValidationController extends HttpServlet {

    private final TicketService ticketService = new TicketService();
    private final BookingService bookingService = new BookingService();
    private final ShowtimeService showtimeService = new ShowtimeService();

    protected void processRequest(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        response.setContentType("text/html;charset=UTF-8");

        String action = request.getParameter("action");
        if ("validate".equalsIgnoreCase(action)) {
            String bookingIdStr = request.getParameter("bookingId");
            
            if (bookingIdStr == null || bookingIdStr.trim().isEmpty()) {
                request.setAttribute("validationError", "MÃ VÉ KHÔNG HỢP LỆ: Vui lòng nhập mã vé hợp lệ!");
                request.getRequestDispatcher("ticketValidation.jsp").forward(request, response);
                return;
            }

            try {
                // Hỗ trợ trích xuất và chuẩn hóa bookingId thông qua TicketService
                int bookingId = ticketService.parseBookingId(bookingIdStr);
                
                // Sử dụng Staff ID = 10 (theo seed_data.sql) để kiểm soát check-in
                int staffId = 10;
                
                String result = ticketService.checkInTicket(bookingId, staffId);
                
                if ("SUCCESS".equalsIgnoreCase(result)) {
                    model.Booking booking = bookingService.getBookingById(bookingId);
                    Showtime st = showtimeService.getShowtimeById(booking.getShowtimeId());
                    
                    request.setAttribute("validationSuccess", true);
                    request.setAttribute("booking", booking);
                    request.setAttribute("showtime", st);
                    
                    // Lấy mã ghế thông qua TicketService
                    String seatCodes = ticketService.getSeatCodesByBookingId(bookingId);
                    request.setAttribute("seatCodes", seatCodes);
                    
                } else {
                    request.setAttribute("validationError", result);
                }
            } catch (NumberFormatException e) {
                request.setAttribute("validationError", "MÃ VÉ KHÔNG HỢP LỆ: Mã vé '" + bookingIdStr + "' không đúng định dạng số.");
            } catch (Exception e) {
                request.setAttribute("validationError", "LỖI HỆ THỐNG: " + e.getMessage());
            }
        }

        request.getRequestDispatcher("ticketValidation.jsp").forward(request, response);
    }

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        processRequest(request, response);
    }

    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        processRequest(request, response);
    }
}
