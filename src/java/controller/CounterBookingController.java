package controller;

import service.BookingService;
import service.SeatService;
import service.ShowtimeService;
import service.DiscountService;
import service.TicketService;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import model.Seat;
import model.Showtime;
import model.User;
import jakarta.servlet.http.HttpSession;
import service.UserService;

@WebServlet(name = "CounterBookingController", urlPatterns = {"/CounterBooking"})
public class CounterBookingController extends HttpServlet {

    private final ShowtimeService showtimeService = new ShowtimeService();
    private final BookingService bookingService = new BookingService();
    private final SeatService seatService = new SeatService();
    private final DiscountService discountService = new DiscountService();
    private final TicketService ticketService = new TicketService();
    private final UserService userService = new UserService();

    protected void processRequest(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        response.setContentType("text/html;charset=UTF-8");

        HttpSession session = request.getSession(false);
        User user = (session != null) ? (User) session.getAttribute("user") : null;
        if (user == null) {
            response.sendRedirect(request.getContextPath() + "/login");
            return;
        }
        String role = user.getRole();
        if (!"STAFF".equalsIgnoreCase(role) && !"MANAGER".equalsIgnoreCase(role) && !"ADMIN".equalsIgnoreCase(role)) {
            response.sendRedirect(request.getContextPath() + "/home");
            return;
        }

        int staffId = user.getId();
        int branchId = userService.getBranchIdOfStaff(staffId);
        String action = request.getParameter("action");

        // API CHECK PAYMENT STATUS: Truy vấn trạng thái đơn hàng (PENDING / CONFIRMED) phục vụ AJAX polling
        if ("checkPaymentStatus".equalsIgnoreCase(action)) {
            response.setContentType("application/json;charset=UTF-8");
            try {
                int bookingId = Integer.parseInt(request.getParameter("bookingId"));
                String status = bookingService.getBookingStatus(bookingId);
                response.getWriter().write("{\"status\":\"" + status + "\"}");
            } catch (Exception e) {
                response.getWriter().write("{\"status\":\"ERROR\",\"message\":\"" + e.getMessage() + "\"}");
            }
            return;
        }

        // API CANCEL BOOKING: Hủy đơn hàng và giải phóng ghế khi khách hàng không thanh toán nữa
        if ("cancelBooking".equalsIgnoreCase(action)) {
            response.setContentType("application/json;charset=UTF-8");
            try {
                int bookingId = Integer.parseInt(request.getParameter("bookingId"));
                boolean success = bookingService.cancelBooking(bookingId);
                response.getWriter().write("{\"success\":" + success + "}");
            } catch (Exception e) {
                response.getWriter().write("{\"success\":false,\"message\":\"" + e.getMessage() + "\"}");
            }
            return;
        }

        // CHỨC NĂNG BỔ TRỢ: Xác thực mã giảm giá qua AJAX ở quầy POS
        if ("checkVoucher".equalsIgnoreCase(action)) {
            response.setContentType("application/json;charset=UTF-8");
            try {
                String code = request.getParameter("code");
                double subtotal = Double.parseDouble(request.getParameter("total"));
                
                List<model.DiscountCode> list = discountService.getAllDiscountCodes();
                model.DiscountCode voucher = null;
                for (model.DiscountCode dc : list) {
                    if (dc.getCode().equalsIgnoreCase(code)) {
                        voucher = dc;
                        break;
                    }
                }
                
                if (voucher == null) {
                    response.getWriter().write("{\"success\":false,\"message\":\"Mã giảm giá không tồn tại!\"}");
                    return;
                }
                
                if (!"ACTIVE".equalsIgnoreCase(voucher.getStatus())) {
                    response.getWriter().write("{\"success\":false,\"message\":\"Mã giảm giá đã tạm dừng hoặc hết hiệu lực!\"}");
                    return;
                }
                
                java.sql.Timestamp now = new java.sql.Timestamp(System.currentTimeMillis());
                if (now.before(voucher.getStartDate())) {
                    response.getWriter().write("{\"success\":false,\"message\":\"Chương trình khuyến mãi chưa bắt đầu!\"}");
                    return;
                }
                if (now.after(voucher.getEndDate())) {
                    response.getWriter().write("{\"success\":false,\"message\":\"Mã giảm giá đã hết hạn sử dụng!\"}");
                    return;
                }
                
                if (voucher.getUsedCount() >= voucher.getMaxUses()) {
                    response.getWriter().write("{\"success\":false,\"message\":\"Mã giảm giá đã hết lượt sử dụng!\"}");
                    return;
                }
                
                if (subtotal < voucher.getMinOrderValue()) {
                    java.text.DecimalFormat df = new java.text.DecimalFormat("#,##0");
                    response.getWriter().write("{\"success\":false,\"message\":\"Chưa đạt giá trị đơn hàng tối thiểu (" + df.format(voucher.getMinOrderValue()) + "đ)!\"}");
                    return;
                }
                
                double discountAmount = 0;
                if ("PERCENT".equalsIgnoreCase(voucher.getDiscountType())) {
                    discountAmount = subtotal * (voucher.getDiscountValue() / 100.0);
                    if (voucher.getMaxDiscountAmount() != null && discountAmount > voucher.getMaxDiscountAmount()) {
                        discountAmount = voucher.getMaxDiscountAmount();
                    }
                } else {
                    discountAmount = voucher.getDiscountValue();
                }
                
                if (discountAmount > subtotal) {
                    discountAmount = subtotal;
                }
                
                response.getWriter().write("{\"success\":true,\"discountAmount\":" + discountAmount + "}");
            } catch (Exception e) {
                response.getWriter().write("{\"success\":false,\"message\":\"Lỗi hệ thống: " + e.getMessage() + "\"}");
            }
            return;
        }

        // CHỨC NĂNG 1: Tạo hóa đơn thanh toán trực tiếp tại quầy
        if ("book".equalsIgnoreCase(action)) {
            try {
                int showtimeId = Integer.parseInt(request.getParameter("showtimeId"));
                String seatsParam = request.getParameter("selectedSeats");
                
                if (seatsParam == null || seatsParam.trim().isEmpty()) {
                    request.getSession().setAttribute("msgError", "Vui lòng chọn ít nhất một ghế!");
                    response.sendRedirect("CounterBooking?showtimeId=" + showtimeId);
                    return;
                }

                // Chuyển mã ID ghế thành danh sách số nguyên
                String[] seatArr = seatsParam.split(",");
                List<Integer> seatIds = new ArrayList<>();
                List<Double> seatPrices = new ArrayList<>();
                double seatsTotal = 0;

                Showtime st = showtimeService.getShowtimeById(showtimeId);
                for (String seatIdStr : seatArr) {
                    int seatId = Integer.parseInt(seatIdStr.trim());
                    seatIds.add(seatId);
                    
                    // Tìm loại ghế của seatId này để lấy giá tương ứng
                    String seatType = "STANDARD";
                    // Tìm trong danh sách toàn bộ ghế của rạp để kiểm tra loại ghế
                    List<Seat> allSeats = seatService.getSeatsByHall(st.getHallId());
                    for (Seat s : allSeats) {
                        if (s.getId() == seatId) {
                            seatType = s.getSeatType();
                            break;
                        }
                    }
                    
                    double price = showtimeService.getSeatPrice(showtimeId, seatType, st.getBasePrice());
                    seatPrices.add(price);
                    seatsTotal += price;
                }

                // Xử lý giảm giá
                double discountAmount = 0;
                String discountReason = request.getParameter("discountReason");
                if (discountReason == null || discountReason.trim().isEmpty()) {
                    discountReason = "Chiết khấu trực tiếp tại quầy";
                }
                String discountStr = request.getParameter("discountAmount");
                if (discountStr != null && !discountStr.trim().isEmpty()) {
                    discountAmount = Double.parseDouble(discountStr.trim());
                }

                double finalPrice = seatsTotal - discountAmount;
                if (finalPrice < 0) finalPrice = 0;

                String paymentMethod = request.getParameter("paymentMethod"); // CASH hoặc BANKING
                if (paymentMethod == null) paymentMethod = "CASH";

                // Đặt vé tại quầy có thể gắn với tài khoản khách hoặc tài khoản mặc định
                int customerUserId = 10;

                int bookingId = bookingService.createWalkinBooking(customerUserId, showtimeId, seatIds, seatPrices, 
                                                                 finalPrice, paymentMethod, discountAmount, 
                                                                 discountReason, staffId);

                if (bookingId != -1) {
                    // Nếu sử dụng mã giảm giá, tự động tăng số lần sử dụng của mã
                    if (discountReason != null && discountReason.startsWith("Mã giảm giá: ")) {
                        String vCode = discountReason.substring(13).trim();
                        discountService.incrementUsedCount(vCode);
                    }
                    // Thành công: Chuyển hướng về trang POS kèm tham số thành công
                    response.sendRedirect("CounterBooking?showtimeId=" + showtimeId + "&bookingSuccessId=" + bookingId);
                    return;
                } else {
                    request.getSession().setAttribute("msgError", "Đặt vé thất bại! Ghế có thể đã có người khác nhanh tay chọn trước.");
                    response.sendRedirect("CounterBooking?showtimeId=" + showtimeId);
                    return;
                }

            } catch (Exception e) {
                request.getSession().setAttribute("msgError", "Lỗi dữ liệu: " + e.getMessage());
                response.sendRedirect("CounterBooking");
                return;
            }
        }

        // CHỨC NĂNG 2: Mở màn hình hóa đơn in vé
        if ("printTicket".equalsIgnoreCase(action)) {
            try {
                int bookingId = Integer.parseInt(request.getParameter("bookingId"));
                model.Booking booking = bookingService.getBookingById(bookingId);
                
                if (booking != null) {
                    Showtime st = showtimeService.getShowtimeById(booking.getShowtimeId());
                    request.setAttribute("booking", booking);
                    request.setAttribute("showtime", st);
                    
                    // Lấy danh sách tên ghế đã đặt
                    List<Seat> allSeats = seatService.getSeatsByHall(st.getHallId());
                    List<Integer> bookedIds = bookingService.getBookedSeatIds(st.getId()); // or get booking seat details
                    
                    // Query chính xác các ghế của booking này thông qua TicketService
                    String seatCodes = ticketService.getSeatCodesByBookingId(bookingId);
                    
                    request.setAttribute("seatCodes", seatCodes);
                    request.getRequestDispatcher("ticketPrint.jsp").forward(request, response);
                    return;
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
            response.sendRedirect("CounterBooking");
            return;
        }

        // CHỨC NĂNG 3: Đổi ghế cho hóa đơn (Chỉ khi chưa thanh toán / PENDING)
        if ("changeSeats".equalsIgnoreCase(action)) {
            try {
                int bookingId = Integer.parseInt(request.getParameter("bookingId"));
                String oldSeatsStr = request.getParameter("oldSeats");
                String newSeatsStr = request.getParameter("newSeats");
                int showtimeId = Integer.parseInt(request.getParameter("showtimeId"));

                String[] oldArr = oldSeatsStr.split(",");
                String[] newArr = newSeatsStr.split(",");

                List<Integer> oldSeatIds = new ArrayList<>();
                List<Integer> newSeatIds = new ArrayList<>();
                List<Double> newPrices = new ArrayList<>();

                Showtime st = showtimeService.getShowtimeById(showtimeId);
                List<Seat> allSeats = seatService.getSeatsByHall(st.getHallId());

                for (String s : oldArr) oldSeatIds.add(Integer.parseInt(s.trim()));
                for (String s : newArr) {
                    int nId = Integer.parseInt(s.trim());
                    newSeatIds.add(nId);
                    
                    // Resolve price
                    String seatType = "STANDARD";
                    for (Seat seat : allSeats) {
                        if (seat.getId() == nId) {
                            seatType = seat.getSeatType();
                            break;
                        }
                    }
                    newPrices.add(showtimeService.getSeatPrice(showtimeId, seatType, st.getBasePrice()));
                }

                boolean success = bookingService.changeBookingSeats(bookingId, oldSeatIds, newSeatIds, newPrices);
                if (success) {
                    request.getSession().setAttribute("msgSuccess", "Thay đổi vị trí ghế thành công!");
                } else {
                    request.getSession().setAttribute("msgError", "Thay đổi vị trí ghế thất bại. Ghế mới có thể đã bị khóa!");
                }
                response.sendRedirect("CounterBooking?showtimeId=" + showtimeId);
                return;
            } catch (Exception e) {
                request.getSession().setAttribute("msgError", "Lỗi xử lý đổi ghế: " + e.getMessage());
                response.sendRedirect("CounterBooking");
                return;
            }
        }

        // ĐỌC LUỒNG GET CHÍNH: Tìm kiếm suất chiếu & hiển thị sơ đồ ghế
        List<Showtime> showtimeList = showtimeService.getActiveShowtimesByBranch(branchId);
        request.setAttribute("showtimeList", showtimeList);

        String showtimeIdStr = request.getParameter("showtimeId");
        if (showtimeIdStr != null) {
            try {
                int showtimeId = Integer.parseInt(showtimeIdStr);
                Showtime selectedShowtime = showtimeService.getShowtimeById(showtimeId);
                
                if (selectedShowtime != null) {
                    // Lấy sơ đồ phòng chiếu
                    List<Seat> seatList = seatService.getSeatsByHall(selectedShowtime.getHallId());
                    // Lấy danh sách ID ghế đã bị đặt/khóa
                    List<Integer> bookedSeatIds = bookingService.getBookedSeatIds(showtimeId);

                    int maxSeatNumber = 8; // Default minimum columns
                    for (Seat s : seatList) {
                        if (s.getSeatNumber() > maxSeatNumber) {
                            maxSeatNumber = s.getSeatNumber();
                        }
                    }

                    request.setAttribute("selectedShowtime", selectedShowtime);
                    request.setAttribute("seatList", seatList);
                    request.setAttribute("bookedSeatIds", bookedSeatIds);
                    request.setAttribute("showtimeService", showtimeService);
                    request.setAttribute("maxSeatNumber", maxSeatNumber);
                }
            } catch (NumberFormatException e) {
                // Ignore
            }
        }

        String bookingSuccessId = request.getParameter("bookingSuccessId");
        if (bookingSuccessId != null) {
            request.setAttribute("bookingSuccessId", bookingSuccessId);
            try {
                int bId = Integer.parseInt(bookingSuccessId);
                model.Booking booking = bookingService.getBookingById(bId);
                if (booking != null) {
                    request.setAttribute("successBooking", booking);
                }
            } catch (Exception e) {
                // Ignore
            }
        }

        request.getRequestDispatcher("counterBooking.jsp").forward(request, response);
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
