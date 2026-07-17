package controller;

import dao.BookingPaymentDAO;
import dao.BookingFnbDAO;
import dao.SeatDAO;
import dao.ShowtimeDAO;
import dto.PaymentView;
import dto.SeatView;
import dto.TicketSeatView;
import dto.TicketView;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;
import java.io.IOException;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import model.Seat;
import model.Showtime;
import model.User;

@WebServlet(urlPatterns = {
    "/payment-demo",
    "/booking/payment",
    "/booking/success"
})
public class PaymentDemoController extends HttpServlet {

    private static final String BANK_CODE = "BIDV";
    private static final String ACCOUNT_NO = "4860555705";
    private static final String ACCOUNT_NAME = "TRAN THE TRUONG";

    private static final String PAYMENT_PAGE = "/pages/payment/payment.jsp";
    private static final String SUCCESS_PAGE = "/pages/booking/success.jsp";

    private final ShowtimeDAO showtimeDAO = new ShowtimeDAO();
    private final SeatDAO seatDAO = new SeatDAO();
    private final BookingPaymentDAO bookingPaymentDAO = new BookingPaymentDAO();
    private final BookingFnbDAO bookingFnbDAO = new BookingFnbDAO();

    @Override
    protected void doGet(HttpServletRequest request,
            HttpServletResponse response)
            throws ServletException, IOException {

        String path = request.getServletPath();

        if ("/payment-demo".equals(path)) {
            showPaymentDemo(request, response);
            return;
        }

        if ("/booking/success".equals(path)) {
            try {
                showSuccess(request, response);

            } catch (SQLException e) {
                throw new ServletException(e);
            }
            return;
        }

        response.sendRedirect(request.getContextPath() + "/home");
    }

    @Override
    protected void doPost(HttpServletRequest request,
            HttpServletResponse response)
            throws ServletException, IOException {

        request.setCharacterEncoding("UTF-8");

        String path = request.getServletPath();

        if ("/booking/payment".equals(path)) {
            processPayment(request, response);
            return;
        }

        response.sendRedirect(request.getContextPath() + "/home");
    }

    private void processPayment(HttpServletRequest request,
            HttpServletResponse response)
            throws ServletException, IOException {

        int showtimeId = Integer.parseInt(request.getParameter("showtimeId"));
        String[] seatIds = request.getParameterValues("seatIds");
        List<Integer> selectedSeatIds = parseSeatIds(
                request.getParameterValues("seatIds")
        );

        if (showtimeId <= 0) {
            request.setAttribute("error", "Suất chiếu không hợp lệ.");
            request.getRequestDispatcher(PAYMENT_PAGE).forward(request, response);
            return;
        }

        if (selectedSeatIds.isEmpty()) {
            response.sendRedirect(
                    request.getContextPath()
                    + "/seats?showtimeId=" + showtimeId
            );
            return;
        }

        Showtime showtime = showtimeDAO.findById(showtimeId);

        if (showtime == null) {
            request.setAttribute("error", "Không tìm thấy suất chiếu.");
            request.getRequestDispatcher(PAYMENT_PAGE).forward(request, response);
            return;
        }

        List<SeatView> allSeatViews = seatDAO.findByShowtime(showtimeId);
        List<TicketSeatView> selectedSeats = new ArrayList<>();

        double amount = 0;

        for (SeatView view : allSeatViews) {
            if (view == null || view.getSeat() == null) {
                continue;
            }

            Seat seat = view.getSeat();

            if (!selectedSeatIds.contains(seat.getId())) {
                continue;
            }

            if (view.isBooked() || seat.isMaintenance()) {
                request.setAttribute(
                        "error",
                        "Ghế " + seat.getSeatCode()
                        + " đã được đặt hoặc đang bảo trì."
                );
                request.getRequestDispatcher(PAYMENT_PAGE)
                        .forward(request, response);
                return;
            }

            double price = view.getPrice();

            if (price <= 0) {
                price = showtime.getBasePrice();
            }

            amount += price;

            selectedSeats.add(
                    new TicketSeatView(
                            seat.getId(),
                            seat.getSeatCode(),
                            seat.getSeatType(),
                            price
                    )
            );
        }

        if (selectedSeats.size() != selectedSeatIds.size()) {
            request.setAttribute("error", "Một số ghế không tồn tại trong suất chiếu này.");
            request.getRequestDispatcher(PAYMENT_PAGE).forward(request, response);
            return;
        }

        User user = (User) request.getSession().getAttribute("user");

        if (user == null) {
            response.sendRedirect(request.getContextPath() + "/login");
            return;
        }

        String tempQrCode = "PENDING";

        int bookingId = bookingPaymentDAO.createPendingBooking(
                user.getId(),
                showtimeId,
                amount,
                tempQrCode
        );

        if (bookingId <= 0) {
            request.setAttribute("error", "Không thể tạo booking.");
            request.getRequestDispatcher(PAYMENT_PAGE).forward(request, response);
            return;
        }

        String transferContent = "RVS" + bookingId;

        for (TicketSeatView seat : selectedSeats) {
            bookingPaymentDAO.addBookingSeat(
                    bookingId,
                    seat.getSeatId(),
                    seat.getPrice()
            );
        }
        bookingPaymentDAO.createPendingPayment(
                bookingId,
                "SEPAY",
                BANK_CODE,
                transferContent,
                amount
        );

        PaymentView payment = buildPaymentView(
                bookingId,
                showtime,
                amount,
                transferContent
        );

        TicketView ticket = buildTicketView(
                bookingId,
                showtime,
                amount,
                transferContent,
                selectedSeats
        );

        HttpSession session = request.getSession();
        session.setAttribute("lastPayment", payment);
        session.setAttribute("lastTicket", ticket);
        session.setAttribute("selectedSeats", selectedSeats);

        request.setAttribute("payment", payment);
        request.setAttribute("selectedSeats", selectedSeats);

        request.getRequestDispatcher(PAYMENT_PAGE)
                .forward(request, response);
    }

    private PaymentView buildPaymentView(int bookingId,
            Showtime showtime,
            double amount,
            String transferContent) {

        String qrUrl = buildQrUrl(amount, transferContent);

        PaymentView payment = new PaymentView();

        payment.setBookingId(bookingId);
        payment.setBookingStatus("PENDING");
        payment.setStatus("PENDING");

        payment.setMovieTitle(showtime.getMovieTitle());
        payment.setShowDate(showtime.getShowDate());
        payment.setShowTime(showtime.getStartHour() + " - " + showtime.getEndHour());
        payment.setBranchName(
                showtime.getBranchName()
                + " - "
                + showtime.getBranchAddress()
        );
        payment.setHallName(
                showtime.getHallName()
                + " ("
                + showtime.getHallType()
                + ")"
        );

        payment.setMethod("SEPAY");
        payment.setGateway(BANK_CODE);
        payment.setTransactionId(transferContent);
        payment.setAmount(amount);

        payment.setTransferContent(transferContent);
        payment.setQrUrl(qrUrl);

        return payment;
    }

    private TicketView buildTicketView(int bookingId,
            Showtime showtime,
            double amount,
            String transferContent,
            List<TicketSeatView> selectedSeats) {

        TicketView ticket = new TicketView();

        ticket.setBookingId(bookingId);
        ticket.setBookingStatus("CONFIRMED");
        ticket.setPaymentStatus("PAID");

        ticket.setMovieTitle(showtime.getMovieTitle());
        ticket.setMoviePoster(showtime.getMoviePoster());
        ticket.setShowDate(showtime.getShowDate());
        ticket.setShowTime(showtime.getStartHour() + " - " + showtime.getEndHour());
        ticket.setBranchName(showtime.getBranchName());
        ticket.setBranchAddress(showtime.getBranchAddress());
        ticket.setHallName(showtime.getHallName());

        ticket.setPaymentMethod("SEPAY");
        ticket.setPaymentGateway(BANK_CODE);
        ticket.setTransactionId(transferContent);

        ticket.setTotalPrice(amount);
        ticket.setDiscountAmount(0);
        ticket.setFinalAmount(amount);
        ticket.setQrCode(transferContent);

        ticket.setSeats(selectedSeats);

        return ticket;
    }

    private void showPaymentDemo(HttpServletRequest request,
            HttpServletResponse response)
            throws ServletException, IOException {

        int bookingId = 999;
        double amount = 1000;
        String transferContent = "RVS" + bookingId;

        PaymentView payment = new PaymentView();

        payment.setBookingId(bookingId);
        payment.setBookingStatus("PENDING");
        payment.setStatus("PENDING");

        payment.setMovieTitle("Demo Movie");
        payment.setShowDate("30/06/2026");
        payment.setShowTime("19:30 - 21:30");
        payment.setBranchName("RapViet Cinema Hà Nội - Hà Nội");
        payment.setHallName("Hall 1 (STANDARD)");

        payment.setMethod("SEPAY");
        payment.setGateway(BANK_CODE);
        payment.setTransactionId(transferContent);
        payment.setAmount(amount);

        payment.setTransferContent(transferContent);
        payment.setQrUrl(buildQrUrl(amount, transferContent));

        request.setAttribute("payment", payment);

        request.getRequestDispatcher(PAYMENT_PAGE)
                .forward(request, response);
    }

    private void showSuccess(HttpServletRequest request,
            HttpServletResponse response)
            throws ServletException, IOException, SQLException {

        int bookingId = parseInt(request.getParameter("bookingId"));

        if (bookingId <= 0) {
            response.sendRedirect(request.getContextPath() + "/home");
            return;
        }

        TicketView ticket = bookingPaymentDAO.getTicketViewByBookingId(bookingId);

        if (ticket == null) {
            request.setAttribute("ticket", null);
        } else {
            request.setAttribute("ticket", ticket);
            request.setAttribute("fnbLines", bookingFnbDAO.findByBookingId(bookingId));
        }

        request.getRequestDispatcher(SUCCESS_PAGE)
                .forward(request, response);
    }

    private String buildQrUrl(double amount, String transferContent) {
        return "https://img.vietqr.io/image/"
                + BANK_CODE + "-"
                + ACCOUNT_NO
                + "-compact2.png"
                + "?amount=" + (long) amount
                + "&addInfo=" + encode(transferContent)
                + "&accountName=" + encode(ACCOUNT_NAME);
    }

    private int generateDemoBookingId(int showtimeId, List<Integer> seatIds) {
        int hash = showtimeId;

        for (Integer seatId : seatIds) {
            if (seatId != null) {
                hash = hash * 31 + seatId;
            }
        }

        hash = Math.abs(hash);

        return 900000 + (hash % 99999);
    }

    private List<Integer> parseSeatIds(String[] values) {
        List<Integer> result = new ArrayList<>();
        Set<Integer> unique = new HashSet<>();

        if (values == null) {
            return result;
        }

        for (String value : values) {
            int id = parseInt(value);

            if (id > 0 && unique.add(id)) {
                result.add(id);
            }
        }

        return result;
    }

    private int parseInt(String value) {
        if (value == null || value.trim().isEmpty()) {
            return 0;
        }

        try {
            return Integer.parseInt(value.trim());
        } catch (NumberFormatException e) {
            return 0;
        }
    }

    private String encode(String value) {
        return URLEncoder.encode(
                value == null ? "" : value,
                StandardCharsets.UTF_8
        );
    }
}
