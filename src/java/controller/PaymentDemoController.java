package controller;

import dto.PaymentView;
import dto.TicketSeatView;
import dto.TicketView;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.*;
import java.io.IOException;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;

@WebServlet( urlPatterns = {
           "/payment-demo",
            "/booking/success"
        })
public class PaymentDemoController extends HttpServlet {

   private static final String BANK_CODE = "BIDV";
    private static final String ACCOUNT_NO = "4860555705";
    private static final String ACCOUNT_NAME = "TRAN THE TRUONG";

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {

        String path = request.getServletPath();

        if ("/payment-demo".equals(path)) {
            showPaymentDemo(request, response);
            return;
        }

        if ("/booking/success".equals(path)) {
            showSuccessDemo(request, response);
            return;
        }

        response.sendRedirect(request.getContextPath() + "/home");
    }

    private void showPaymentDemo(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {

        int bookingId = 999;
        double amount = 1000;
        String transferContent = "RV" + bookingId;

        String qrUrl = "https://img.vietqr.io/image/"
                + BANK_CODE + "-"
                + ACCOUNT_NO
                + "-compact2.png"
                + "?amount=" + (long) amount
                + "&addInfo=" + encode(transferContent)
                + "&accountName=" + encode(ACCOUNT_NAME);

        PaymentView payment = new PaymentView();

        payment.setBookingId(bookingId);
        payment.setBookingStatus("PENDING");
        payment.setStatus("PENDING");

        payment.setMovieTitle("Demo Movie");
        payment.setShowDate("30/06/2026");
        payment.setShowTime("19:30");
        payment.setBranchName("RapViet Cinema Hà Nội");
        payment.setHallName("Hall 1");

        payment.setMethod("SEPAY");
        payment.setGateway("BIDV");
        payment.setTransactionId(transferContent);
        payment.setAmount(amount);

        payment.setTransferContent(transferContent);
        payment.setQrUrl(qrUrl);

        request.setAttribute("payment", payment);

        request.getRequestDispatcher("/pages/payment/payment.jsp")
                .forward(request, response);
    }

    private void showSuccessDemo(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {

        int bookingId = 999;

        String bookingIdParam = request.getParameter("bookingId");
        if (bookingIdParam != null && !bookingIdParam.trim().isEmpty()) {
            try {
                bookingId = Integer.parseInt(bookingIdParam.trim());
            } catch (NumberFormatException ignored) {
            }
        }

        TicketView ticket = new TicketView();

        ticket.setBookingId(bookingId);
        ticket.setBookingStatus("CONFIRMED");
        ticket.setPaymentStatus("PAID");

        ticket.setMovieTitle("Demo Movie");
        ticket.setMoviePoster("");
        ticket.setShowDate("30/06/2026");
        ticket.setShowTime("19:30");
        ticket.setBranchName("RapViet Cinema Hà Nội");
        ticket.setBranchAddress("Hà Nội");
        ticket.setHallName("Hall 1");

        ticket.setPaymentMethod("SEPAY");
        ticket.setPaymentGateway("BIDV");
        ticket.setTransactionId("RV" + bookingId);

        ticket.setTotalPrice(1000);
        ticket.setDiscountAmount(0);
        ticket.setFinalAmount(1000);
        ticket.setQrCode("RV" + bookingId);

        List<TicketSeatView> seats = new ArrayList<>();
        seats.add(new TicketSeatView(1, "A1", "STANDARD", 1000));
        ticket.setSeats(seats);

        request.setAttribute("ticket", ticket);

        request.getRequestDispatcher("/pages/booking/success.jsp")
                .forward(request, response);
    }

    private String encode(String value) {
        return URLEncoder.encode(value, StandardCharsets.UTF_8);
    }
}