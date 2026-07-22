package controller;

import dao.BookingFnbDAO;
import dto.BookingFnbLine;
import dto.CounterBookingQuote;
import dto.StaffFnbComboDTO;
import dto.StaffFnbProductDTO;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;
import java.io.IOException;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import model.Seat;
import model.Showtime;
import model.User;
import service.BookingService;
import service.SeatService;
import service.ShowtimeService;
import service.TicketService;
import service.UserService;

@WebServlet(name = "CounterBookingController", urlPatterns = {"/CounterBooking"})
public class CounterBookingController extends HttpServlet {

    private final ShowtimeService showtimeService = new ShowtimeService();
    private final BookingService bookingService = new BookingService();
    private final SeatService seatService = new SeatService();
    private final TicketService ticketService = new TicketService();
    private final UserService userService = new UserService();
    private final BookingFnbDAO bookingFnbDAO = new BookingFnbDAO();

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        processRequest(request, response);
    }

    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        request.setCharacterEncoding("UTF-8");
        processRequest(request, response);
    }

    private void processRequest(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        // Quầy bán vé là màn hình nội bộ: bắt buộc đăng nhập và chỉ role STAFF được dùng.
        HttpSession session = request.getSession(false);
        User user = session == null ? null : (User) session.getAttribute("user");
        if (user == null) {
            response.sendRedirect(request.getContextPath() + "/login");
            return;
        }
        if (!"STAFF".equalsIgnoreCase(user.getRole())) {
            response.sendError(HttpServletResponse.SC_FORBIDDEN);
            return;
        }

        // Không có phân công chi nhánh thì không được bán/tra cứu vé ở bất kỳ chi nhánh nào.
        int staffId = user.getId();
        int branchId = userService.getBranchIdOfStaff(staffId);
        if (branchId <= 0) {
            response.sendError(HttpServletResponse.SC_FORBIDDEN, "Staff chưa được gán chi nhánh.");
            return;
        }
        model.Branch branch = new dao.BranchDAO().getBranchById(branchId);
        request.setAttribute("staffBranchName", branch == null ? "Không xác định" : branch.getName());

        // Các action làm thay đổi trạng thái (hủy/tạo vé) bắt buộc phải là POST.
        String action = request.getParameter("action");
        if ("checkPaymentStatus".equalsIgnoreCase(action)) {
            writeStatus(response, bookingService.getCounterBookingStatus(staffId, parseId(request.getParameter("bookingId"))));
            return;
        }
        if ("seatAvailability".equalsIgnoreCase(action)) {
            /*
             * POS không chỉ đọc sơ đồ ghế lúc mở trang. Khi customer vừa xác nhận
             * đơn online, BookingController tạo BOOKINGS=PENDING + BOOKING_SEATS.
             * Endpoint này cho phép trình duyệt của staff lấy lại danh sách ghế
             * đang bị giữ/đã bán để khóa nút ghế mà không cần reload toàn bộ POS.
             */
            writeSeatAvailability(response, staffId, parseId(request.getParameter("showtimeId")));
            return;
        }
        if ("cancelBooking".equalsIgnoreCase(action)) {
            if (!"POST".equalsIgnoreCase(request.getMethod())) {
                response.sendError(HttpServletResponse.SC_METHOD_NOT_ALLOWED);
                return;
            }
            writeJson(response, "{\"success\":" + bookingService.cancelPendingCounterBooking(staffId, parseId(request.getParameter("bookingId"))) + "}");
            return;
        }
        if ("checkVoucher".equalsIgnoreCase(action)) {
            quoteVoucher(request, response, staffId);
            return;
        }
        if ("changeSeats".equalsIgnoreCase(action)) {
            // Đổi ghế sau khi tạo vé đang khóa để tránh lệch giá và tranh chấp trạng thái ghế.
            session.setAttribute("msgError", "Đổi ghế tại quầy đang tạm khóa để bảo vệ giá vé và trạng thái ghế.");
            response.sendRedirect("CounterBooking");
            return;
        }
        if ("book".equalsIgnoreCase(action)) {
            if (!"POST".equalsIgnoreCase(request.getMethod())) {
                response.sendError(HttpServletResponse.SC_METHOD_NOT_ALLOWED);
                return;
            }
            createBooking(request, response, staffId, branchId);
            return;
        }
        if ("printTicket".equalsIgnoreCase(action)) {
            printTicket(request, response, staffId);
            return;
        }

        renderCounter(request, response, branchId);
    }

    private void quoteVoucher(HttpServletRequest request, HttpServletResponse response, int staffId) throws IOException {
        try {
            int branchId = userService.getBranchIdOfStaff(staffId);
            List<BookingFnbLine> fnbLines = bookingFnbDAO.resolveSelection(branchId,
                    parseFnbSelection(request.getParameter("selectedFnb")));
            // Không đọc discountAmount/total từ client: service tự đọc ghế, giá và voucher từ DB.
            CounterBookingQuote quote = bookingService.quoteCounterBooking(staffId,
                    parseId(request.getParameter("showtimeId")), parseSeatIds(request.getParameter("selectedSeats")),
                    request.getParameter("code"), fnbLines);
            if (!quote.isValid()) {
                // Voucher sai vẫn trả báo giá gốc để nhân viên thông báo đúng số tiền cho khách.
                CounterBookingQuote baseQuote = bookingService.quoteCounterBooking(staffId,
                        parseId(request.getParameter("showtimeId")), parseSeatIds(request.getParameter("selectedSeats")), null, fnbLines);
                writeJson(response, "{\"success\":false,\"message\":\"" + json(quote.getMessage())
                        + "\",\"subtotal\":" + baseQuote.getSubtotal() + ",\"discountAmount\":0,\"total\":" + baseQuote.getTotal() + "}");
                return;
            }
            writeJson(response, "{\"success\":true,\"subtotal\":" + quote.getSubtotal()
                    + ",\"discountAmount\":" + quote.getDiscountAmount()
                    + ",\"total\":" + quote.getTotal()
                    + ",\"fnbSubtotal\":" + fnbLines.stream().mapToDouble(BookingFnbLine::getLineTotal).sum() + "}");
        } catch (Exception e) {
            writeJson(response, "{\"success\":false,\"message\":\"Dữ liệu không hợp lệ.\"}");
        }
    }

    // ===== F&B STAFF - CREATE BOOKING BEGIN =====
    private void createBooking(
            HttpServletRequest request,
            HttpServletResponse response,
            int staffId,
            int branchId
    ) throws IOException {

        int showtimeId = parseId(request.getParameter("showtimeId"));

        try {
            /*
             * selectedFnb có định dạng:
             * PRODUCT:productId:quantity,COMBO:comboId:quantity
             *
             * Controller chỉ tách dữ liệu đầu vào.
             * BookingFnbDAO sẽ kiểm tra lại món/combo có được bán tại chi nhánh
             * và số lượng thực tế còn khả dụng hay không.
             */
            Map<String, Integer> fnbQuantities
                    = parseFnbSelection(request.getParameter("selectedFnb"));

            List<BookingFnbLine> selectedFnb
                    = bookingFnbDAO.resolveSelection(branchId, fnbQuantities);

            /*
             * Service phải tính lại toàn bộ giá vé và giá F&B từ DB,
             * sau đó lưu BOOKING, BOOKING_SEATS và BOOKING_FNB
             * trong cùng một transaction.
             */
            int bookingId = bookingService.createCounterBooking(
                    staffId,
                    showtimeId,
                    parseSeatIds(request.getParameter("selectedSeats")),
                    request.getParameter("discountCode"),
                    request.getParameter("paymentMethod"),
                    selectedFnb
            );

            if (bookingId <= 0) {
                throw new IllegalArgumentException(
                        "Không thể tạo vé; ghế, voucher hoặc F&B vừa thay đổi."
                );
            }

            response.sendRedirect(
                    "CounterBooking?showtimeId="
                    + showtimeId
                    + "&bookingSuccessId="
                    + bookingId
            );
        } catch (IllegalArgumentException e) {
            request.getSession().setAttribute("msgError", e.getMessage());
            response.sendRedirect(
                    "CounterBooking"
                    + (showtimeId > 0 ? "?showtimeId=" + showtimeId : "")
            );
        } catch (Exception e) {
            e.printStackTrace();
            request.getSession().setAttribute(
                    "msgError",
                    "Không thể tạo booking. Vui lòng kiểm tra tồn kho F&B và thử lại."
            );
            response.sendRedirect(
                    "CounterBooking"
                    + (showtimeId > 0 ? "?showtimeId=" + showtimeId : "")
            );
        }
    }
    // ===== F&B STAFF - CREATE BOOKING END =====

    private void printTicket(
            HttpServletRequest request,
            HttpServletResponse response,
            int staffId
    ) throws ServletException, IOException {

        int bookingId = parseId(
                request.getParameter("bookingId")
        );

        if (bookingId <= 0) {
            response.sendError(
                    HttpServletResponse.SC_BAD_REQUEST,
                    "Booking ID không hợp lệ."
            );
            return;
        }

        String status = bookingService.getCounterBookingStatus(
                staffId,
                bookingId
        );

        if (status == null) {
            response.sendError(
                    HttpServletResponse.SC_FORBIDDEN,
                    "Booking không thuộc chi nhánh của nhân viên."
            );
            return;
        }

        if (!"CONFIRMED".equalsIgnoreCase(status)) {
            response.sendError(
                    HttpServletResponse.SC_CONFLICT,
                    "Booking chưa được thanh toán thành công."
            );
            return;
        }

        model.Booking booking
                = bookingService.getBookingById(bookingId);

        if (booking == null) {
            response.sendError(
                    HttpServletResponse.SC_NOT_FOUND,
                    "Không tìm thấy booking."
            );
            return;
        }

        request.setAttribute("booking", booking);

        request.setAttribute(
                "showtime",
                showtimeService.getShowtimeById(
                        booking.getShowtimeId()
                )
        );

        request.setAttribute(
                "seatCodes",
                ticketService.getSeatCodesByBookingId(bookingId)
        );

        request.setAttribute(
                "bookingFnbLines",
                bookingFnbDAO.findByBookingId(bookingId)
        );

        request.getRequestDispatcher("/ticketPrint.jsp")
                .forward(request, response);
    }

    private void renderCounter(HttpServletRequest request, HttpServletResponse response, int branchId) throws ServletException, IOException {
        // Danh sách suất chiếu và ghế luôn giới hạn theo chi nhánh đã phân công.
        request.setAttribute("showtimeList", showtimeService.getActiveShowtimesByBranch(branchId));
        List<model.SeatType> types = seatService.getAllSeatTypes();

        // ===== F&B STAFF - BEGIN =====
        // Chỉ lấy F&B thỏa các điều kiện trong BookingFnbDAO:
        // - sản phẩm/combo ACTIVE
        // - Admin cho phép bán
        // - Manager bật bán tại đúng chi nhánh
        // - còn tồn kho / còn đủ nguyên liệu tạo combo
        List<StaffFnbProductDTO> staffFnbItems
                = bookingFnbDAO.findSellableProductsByBranch(branchId);

        List<StaffFnbComboDTO> staffFnbCombos
                = bookingFnbDAO.findSellableCombosByBranch(branchId);

        request.setAttribute("staffFnbItems", staffFnbItems);
        request.setAttribute("staffFnbCombos", staffFnbCombos);
        // ===== F&B STAFF - END =====

        request.setAttribute("allSeatTypes", types);
        int showtimeId = parseId(request.getParameter("showtimeId"));
        Showtime selected = showtimeId <= 0 ? null : showtimeService.getShowtimeById(showtimeId);
        if (selected != null && selected.getBranchId() == branchId) {
            List<Seat> seats = seatService.getSeatsByHall(selected.getHallId());
            int max = 8;
            java.util.Map<String, Double> prices = new java.util.HashMap<>();
            // Giá dùng để hiển thị; giá cuối cùng vẫn được service tính lại khi tạo vé.
            for (model.SeatType type : types) {
                prices.put(type.getCode(), selected.getBasePrice() * type.getDefaultPrice());
            }
            for (Seat seat : seats) {
                if (seat.getSeatNumber() > max) {
                    max = seat.getSeatNumber();
                }
            }
            request.setAttribute("selectedShowtime", selected);
            request.setAttribute("seatList", seats);
            request.setAttribute("bookedSeatIds", bookingService.getBookedSeatIds(showtimeId));
            request.setAttribute("maxSeatNumber", max);
            request.setAttribute("seatPricesMap", prices);
        }
        int successId = parseId(request.getParameter("bookingSuccessId"));
        if (successId > 0 && bookingService.getCounterBookingStatus(((User) request.getSession(false).getAttribute("user")).getId(), successId) != null) {
            request.setAttribute("bookingSuccessId", successId);
            request.setAttribute("successBooking", bookingService.getBookingById(successId));

            // ===== F&B STAFF - SUCCESS SUMMARY BEGIN =====
            request.setAttribute(
                    "successBookingFnbLines",
                    bookingFnbDAO.findByBookingId(successId)
            );
            // ===== F&B STAFF - SUCCESS SUMMARY END =====
        }
        String bankCode = getServletContext()
                .getInitParameter("bank.code");

        String bankAccountNo = getServletContext()
                .getInitParameter("bank.accountNo");

        String bankAccountName = getServletContext()
                .getInitParameter("bank.accountName");

        if (bankCode == null || bankCode.isBlank()
                || bankAccountNo == null || bankAccountNo.isBlank()
                || bankAccountName == null || bankAccountName.isBlank()) {

            throw new ServletException(
                    "Chưa cấu hình đầy đủ thông tin ngân hàng trong web.xml."
            );
        }

        request.setAttribute("bankCode", bankCode.trim());
        request.setAttribute("bankAccountNo", bankAccountNo.trim());
        request.setAttribute("bankAccountName", bankAccountName.trim());
        request.getRequestDispatcher("counterBooking.jsp").forward(request, response);
    }

    private List<Integer> parseSeatIds(String value) {
        // Chỉ tách dữ liệu đầu vào; tính hợp lệ của ghế tiếp tục do BookingService xác nhận với DB.
        List<Integer> result = new ArrayList<>();
        if (value == null || value.trim().isEmpty()) {
            return result;
        }
        for (String item : value.split(",")) {
            int id = parseId(item);
            if (id <= 0) {
                throw new IllegalArgumentException("Ghế không hợp lệ.");
            }
            result.add(id);
        }
        return result;
    }

    // ===== F&B STAFF - PARSE SELECTION BEGIN =====
    /**
     * Tách selectedFnb theo định dạng: PRODUCT:1:2,COMBO:3:1
     *
     * Key trả về có dạng PRODUCT:id hoặc COMBO:id.
     */
    private Map<String, Integer> parseFnbSelection(String value) {
        Map<String, Integer> result = new LinkedHashMap<>();

        if (value == null || value.trim().isEmpty()) {
            return result;
        }

        for (String rawItem : value.split(",")) {
            String item = rawItem == null ? "" : rawItem.trim();

            if (item.isEmpty()) {
                continue;
            }

            String[] parts = item.split(":");

            if (parts.length != 3) {
                throw new IllegalArgumentException(
                        "Dữ liệu F&B không đúng định dạng."
                );
            }

            String itemType = parts[0].trim().toUpperCase();
            int itemId = parseId(parts[1]);
            int quantity = parseId(parts[2]);

            if (!"PRODUCT".equals(itemType)
                    && !"COMBO".equals(itemType)) {
                throw new IllegalArgumentException(
                        "Loại F&B không hợp lệ."
                );
            }

            if (itemId <= 0) {
                throw new IllegalArgumentException(
                        "Mã món F&B không hợp lệ."
                );
            }

            if (quantity < 0) {
                throw new IllegalArgumentException(
                        "Số lượng F&B không hợp lệ."
                );
            }

            String key = itemType + ":" + itemId;

            /*
             * Nếu client gửi trùng một món nhiều lần thì cộng dồn,
             * sau đó DAO vẫn kiểm tra lại giới hạn tồn kho thực tế.
             */
            result.merge(key, quantity, Integer::sum);
        }

        return result;
    }
    // ===== F&B STAFF - PARSE SELECTION END =====

    private int parseId(String value) {
        try {
            return Integer.parseInt(value == null ? "" : value.trim());
        } catch (Exception e) {
            return -1;
        }
    }

    private void writeStatus(HttpServletResponse response, String status) throws IOException {
        writeJson(response, status == null ? "{\"status\":\"NOT_FOUND\"}" : "{\"status\":\"" + json(status) + "\"}");
    }

    /**
     * Trả về các ghế không còn được phép bán tại quầy của một suất chiếu.
     *
     * <p>Dữ liệu đi theo luồng: CounterBookingController -> BookingService
     * -> BookingDAO.getBookedSeatIds(). DAO gộp hai nguồn khóa ghế:</p>
     * <ul>
     *   <li>BOOKING_SEATS thuộc booking PENDING/CONFIRMED/CHECKED_IN/USED;</li>
     *   <li>CART_ITEMS còn locked_until, nếu một luồng khác đang giữ ghế tạm.</li>
     * </ul>
     *
     * <p>Kiểm tra branch ở đây rất quan trọng: staff chỉ được xem và đồng bộ
     * ghế của suất chiếu thuộc chi nhánh mình được phân công.</p>
     */
    private void writeSeatAvailability(HttpServletResponse response, int staffId, int showtimeId)
            throws IOException {
        int branchId = userService.getBranchIdOfStaff(staffId);
        Showtime showtime = showtimeId <= 0 ? null : showtimeService.getShowtimeById(showtimeId);

        if (branchId <= 0 || showtime == null || showtime.getBranchId() != branchId) {
            response.setStatus(HttpServletResponse.SC_FORBIDDEN);
            writeJson(response, "{\"success\":false,\"message\":\"Không có quyền xem trạng thái ghế của suất chiếu này.\"}");
            return;
        }

        List<Integer> bookedSeatIds = bookingService.getBookedSeatIds(showtimeId);
        StringBuilder jsonBody = new StringBuilder("{\"success\":true,\"bookedSeatIds\":[");
        for (int i = 0; i < bookedSeatIds.size(); i++) {
            if (i > 0) {
                jsonBody.append(',');
            }
            jsonBody.append(bookedSeatIds.get(i));
        }
        jsonBody.append("]}");
        writeJson(response, jsonBody.toString());
    }

    private void writeJson(HttpServletResponse response, String body) throws IOException {
        response.setContentType("application/json;charset=UTF-8");
        response.getWriter().write(body);
    }

    private String json(String value) {
        return value == null ? "" : value.replace("\\", "\\\\").replace("\"", "\\\"");
    }
}
