package service;

import dao.BookingDAO;
import dao.BookingStatusHistoryDAO;
import dao.DiscountDAO;
import dto.BookingDraftView;
import dto.BookingFnbLine;
import dto.BookingSeatLine;
import dto.BookingView;
import dto.CounterBookingQuote;
import dto.SeatView;
import dto.VoucherQuote;
import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;
import java.util.Map;
import java.time.LocalDate;
import model.Booking;
import model.BookingStatusHistory;
import model.Seat;
import model.Showtime;
import java.util.Collections;

public class BookingService {

    private final BookingDAO bookingDAO = new BookingDAO();
    private final BookingStatusHistoryDAO bookingStatusHistoryDAO = new BookingStatusHistoryDAO();
    private final ShowtimeService showtimeService = new ShowtimeService();
    private final SeatService seatService = new SeatService();
    private final DiscountDAO discountDAO = new DiscountDAO();
    private final UserService userService = new UserService();

    /**
     * Tạo vé bán tại quầy. Giá tiền, ghế và mã giảm giá đều được tính lại ở
     * server; không sử dụng các trường tổng tiền/giảm giá do trình duyệt gửi
     * lên.
     */
    /**
     * Giữ overload cũ để các controller khác chưa truyền F&B vẫn compile.
     */
    public int createCounterBooking(int staffId, int showtimeId,
            List<Integer> requestedSeatIds,
            String voucherCode, String paymentMethod) {
        return createCounterBooking(
                staffId,
                showtimeId,
                requestedSeatIds,
                voucherCode,
                paymentMethod,
                Collections.emptyList()
        );
    }

    // ===== F&B STAFF - CREATE COUNTER BOOKING BEGIN =====
    public int createCounterBooking(int staffId, int showtimeId,
            List<Integer> requestedSeatIds,
            String voucherCode, String paymentMethod,
            List<BookingFnbLine> selectedFnb) {

        CounterCalculation calculation = calculateCounterBooking(
                staffId,
                showtimeId,
                requestedSeatIds,
                null
        );

        if (!"CASH".equalsIgnoreCase(paymentMethod)
                && !"BANKING".equalsIgnoreCase(paymentMethod)) {
            throw new IllegalArgumentException(
                    "Phương thức thanh toán không hợp lệ."
            );
        }

        List<BookingFnbLine> safeFnb = selectedFnb == null
                ? Collections.emptyList()
                : selectedFnb;

        double orderSubtotal = calculation.quote.getSubtotal()
                + safeFnb.stream().mapToDouble(BookingFnbLine::getLineTotal).sum();
        VoucherQuote orderVoucher = voucherCode == null || voucherCode.trim().isEmpty()
                ? VoucherQuote.invalid("") : quoteVoucher(voucherCode, orderSubtotal);
        if (voucherCode != null && !voucherCode.trim().isEmpty() && !orderVoucher.isValid()) {
            throw new IllegalArgumentException(orderVoucher.getMessage());
        }
        double discount = orderVoucher.isValid() ? orderVoucher.getDiscountAmount() : 0;

        return bookingDAO.createWalkinBooking(
                showtimeId,
                calculation.seatIds,
                calculation.prices,
                // DAO nhận tổng cuối của toàn đơn; voucher có thể giảm cả F&B.
                Math.max(0, orderSubtotal - discount),
                paymentMethod.toUpperCase(),
                discount,
                orderVoucher.isValid()
                        ? "Mã giảm giá: " + orderVoucher.getCode()
                        : "",
                orderVoucher.isValid()
                        ? orderVoucher.getCode()
                        : null,
                staffId,
                safeFnb
        );
    }
    // ===== F&B STAFF - CREATE COUNTER BOOKING END =====

    /**
     * Trả về báo giá để giao diện quầy hiển thị cho khách. Báo giá này dùng
     * cùng công thức với lúc lưu vé, nên nhân viên thấy đúng số tiền sẽ thu.
     */
    public CounterBookingQuote quoteCounterBooking(int staffId, int showtimeId, List<Integer> requestedSeatIds, String voucherCode) {
        return quoteCounterBooking(staffId, showtimeId, requestedSeatIds, voucherCode, Collections.emptyList());
    }

    public CounterBookingQuote quoteCounterBooking(int staffId, int showtimeId, List<Integer> requestedSeatIds,
            String voucherCode, List<BookingFnbLine> selectedFnb) {
        try {
            CounterCalculation calculation = calculateCounterBooking(staffId, showtimeId, requestedSeatIds, null);
            double subtotal = calculation.quote.getSubtotal()
                    + (selectedFnb == null ? 0 : selectedFnb.stream().mapToDouble(BookingFnbLine::getLineTotal).sum());
            VoucherQuote voucher = voucherCode == null || voucherCode.trim().isEmpty()
                    ? VoucherQuote.invalid("") : quoteVoucher(voucherCode, subtotal);
            if (voucherCode != null && !voucherCode.trim().isEmpty() && !voucher.isValid()) {
                return CounterBookingQuote.invalid(voucher.getMessage());
            }
            return CounterBookingQuote.valid(subtotal, voucher.isValid() ? voucher.getDiscountAmount() : 0);
        } catch (IllegalArgumentException e) {
            return CounterBookingQuote.invalid(e.getMessage());
        }
    }

    private CounterCalculation calculateCounterBooking(int staffId, int showtimeId, List<Integer> requestedSeatIds,
            String voucherCode) {
        // Staff chỉ được bán vé cho suất chiếu thuộc chi nhánh được phân công.
        int branchId = userService.getBranchIdOfStaff(staffId);
        Showtime showtime = showtimeService.getShowtimeById(showtimeId);
        if (branchId <= 0 || showtime == null || showtime.getBranchId() != branchId) {
            throw new IllegalArgumentException("Bạn không được bán vé cho suất chiếu này.");
        }
        if (!"SCHEDULED".equalsIgnoreCase(showtime.getStatus()) && !"ON_SALE".equalsIgnoreCase(showtime.getStatus())) {
            throw new IllegalArgumentException("Suất chiếu không mở bán.");
        }
        // Chính sách bán vé tại quầy: chỉ bán đến 30 phút sau giờ bắt đầu suất chiếu.
        if (showtime.getStartTime() == null
                || showtime.getStartTime().getTime() + 30L * 60L * 1000L <= System.currentTimeMillis()) {
            throw new IllegalArgumentException("Suất chiếu đã bắt đầu quá 30 phút, không thể tiếp tục bán vé.");
        }
        // Loại id rỗng/trùng lặp trước khi kiểm tra ghế trong cơ sở dữ liệu.
        List<Integer> seatIds = cleanSeatIds(requestedSeatIds);
        if (seatIds.isEmpty()) {
            throw new IllegalArgumentException("Vui lòng chọn ít nhất một ghế.");
        }
        List<SeatView> views = seatService.getSeatViewsByShowtimeAndIds(showtimeId, seatIds);
        if (views.size() != seatIds.size()) {
            throw new IllegalArgumentException("Ghế không thuộc suất chiếu này.");
        }
        List<Double> prices = new ArrayList<>();
        double subtotal = 0;
        for (SeatView view : views) {
            // Chỉ ghế còn chọn được mới được bán; trạng thái ghế lấy từ DB.
            if (view == null || !view.isSelectable()) {
                throw new IllegalArgumentException("Ghế đã được đặt hoặc đang bảo trì.");
            }
            // Giá từng ghế được lấy theo loại ghế và giá suất chiếu ở server.
            double price = showtimeService.getSeatPrice(showtimeId, view.getSeat().getSeatType(), showtime.getBasePrice());
            prices.add(price);
            subtotal += price;
        }
        // Mã giảm giá cũng được kiểm tra lại ở server theo tổng tiền vừa tính.
        VoucherQuote quote = voucherCode == null || voucherCode.trim().isEmpty()
                ? VoucherQuote.invalid("") : quoteVoucher(voucherCode, subtotal);
        if (voucherCode != null && !voucherCode.trim().isEmpty() && !quote.isValid()) {
            throw new IllegalArgumentException(quote.getMessage());
        }
        double discount = quote.isValid() ? quote.getDiscountAmount() : 0;
        return new CounterCalculation(seatIds, prices, quote, CounterBookingQuote.valid(subtotal, discount));
    }

    private static class CounterCalculation {

        // Dữ liệu nội bộ dùng chung cho bước báo giá và bước tạo vé tại quầy.
        final List<Integer> seatIds;
        final List<Double> prices;
        final VoucherQuote voucher;
        final CounterBookingQuote quote;

        CounterCalculation(List<Integer> seatIds, List<Double> prices, VoucherQuote voucher, CounterBookingQuote quote) {
            this.seatIds = seatIds;
            this.prices = prices;
            this.voucher = voucher;
            this.quote = quote;
        }
    }

    public boolean changeBookingSeats(int bookingId, List<Integer> oldSeatIds, List<Integer> newSeatIds, List<Double> newPrices) {
        return bookingDAO.changeBookingSeats(bookingId, oldSeatIds, newSeatIds, newPrices);
    }

    public List<Integer> getBookedSeatIds(int showtimeId) {
        return bookingDAO.getBookedSeatIds(showtimeId);
    }

    public Booking getBookingById(int id) {
        return bookingDAO.getBookingById(id);
    }

    public List<Booking> getRecentBookings(int limit) {
        return bookingDAO.getRecentBookings(limit);
    }

    public boolean confirmPayment(int bookingId, String transactionId, double amount, String gateway) {
        return bookingDAO.confirmPayment(bookingId, transactionId, amount, gateway);
    }

    public String getBookingStatus(int bookingId) {
        return bookingDAO.getBookingStatus(bookingId);
    }

    public String getCounterBookingStatus(int staffId, int bookingId) {
        int branchId = userService.getBranchIdOfStaff(staffId);
        // Không có chi nhánh thì không được tra cứu bất kỳ vé nào.
        return branchId <= 0 ? null : bookingDAO.getBookingStatusInBranch(bookingId, branchId);
    }

    public boolean cancelBooking(int bookingId) {
        return bookingDAO.cancelBooking(bookingId);
    }

    public boolean cancelPendingCounterBooking(int staffId, int bookingId) {
        int branchId = userService.getBranchIdOfStaff(staffId);
        // DAO chỉ hủy vé PENDING thuộc đúng chi nhánh của nhân viên.
        return branchId > 0 && bookingDAO.cancelPendingBookingInBranch(bookingId, branchId);
    }

//    public BookingDraftView buildDraftView(int showtimeId, List<Integer> seatIds) {
//        Showtime showtime = showtimeService.getBookableShowtime(showtimeId);
//        if (showtime == null) {
//            throw new IllegalArgumentException("Suất chiếu không còn mở đặt vé.");
//        }
//
//        List<Integer> cleanSeatIds = cleanSeatIds(seatIds);
//        if (cleanSeatIds.isEmpty()) {
//            throw new IllegalArgumentException("Vui lòng chọn ít nhất một ghế.");
//        }
//
//        List<SeatView> seatViews = seatService.getSeatViewsByShowtimeAndIds(showtimeId, cleanSeatIds);
//        if (seatViews.size() != cleanSeatIds.size()) {
//            throw new IllegalArgumentException("Danh sách ghế không hợp lệ.");
//        }
//
//        BookingDraftView draftView = new BookingDraftView();
//        draftView.setShowtime(showtime);
//
//        List<BookingSeatLine> lines = new ArrayList<>();
//        double total = 0;
//        for (SeatView seatView : seatViews) {
//            if (seatView == null || !seatView.isSelectable()) {
//                throw new IllegalArgumentException("Một hoặc nhiều ghế đã được đặt hoặc đang bảo trì.");
//            }
//
//            Seat seat = seatView.getSeat();
//            double price = showtimeService.getSeatPrice(
//                    showtimeId,
//                    seat.getSeatType(),
//                    showtime.getBasePrice()
//            );
//            total += price;
//            lines.add(new BookingSeatLine(seat, price));
//        }
//
//        draftView.setSeats(lines);
//        draftView.setTotalPrice(total);
//        return draftView;
//    }
    public BookingDraftView buildDraftView(int showtimeId, List<Integer> seatIds) {
        Showtime showtime = showtimeService.getBookableShowtime(showtimeId);
        if (showtime == null) {
            throw new IllegalArgumentException("Suất chiếu không còn mở đặt vé.");
        }

        List<Integer> cleanSeatIds = cleanSeatIds(seatIds);
        if (cleanSeatIds.isEmpty()) {
            throw new IllegalArgumentException("Vui lòng chọn ít nhất một ghế.");
        }

        List<SeatView> seatViews
                = seatService.getSeatViewsByShowtimeAndIds(showtimeId, cleanSeatIds);

        if (seatViews.size() != cleanSeatIds.size()) {
            throw new IllegalArgumentException("Danh sách ghế không hợp lệ.");
        }

        BookingDraftView draftView = new BookingDraftView();
        draftView.setShowtime(showtime);

        List<BookingSeatLine> lines = new ArrayList<>();
        List<Double> seatPrices = new ArrayList<>();

        double seatSubtotal = 0;

        for (SeatView seatView : seatViews) {
            if (seatView == null || !seatView.isSelectable()) {
                throw new IllegalArgumentException(
                        "Một hoặc nhiều ghế đã được đặt hoặc đang bảo trì."
                );
            }

            Seat seat = seatView.getSeat();
            if (seat == null) {
                throw new IllegalArgumentException("Danh sách ghế không hợp lệ.");
            }

            /*
         * Giá được lấy lại hoàn toàn ở server.
         * Không sử dụng giá từ request hoặc JavaScript.
             */
            double price = showtimeService.getSeatPrice(
                    showtimeId,
                    seat.getSeatType(),
                    showtime.getBasePrice()
            );

            if (price < 0) {
                throw new IllegalArgumentException(
                        "Không thể xác định giá của ghế " + seat.getSeatCode() + "."
                );
            }

            seatSubtotal += price;
            seatPrices.add(price);
            lines.add(new BookingSeatLine(seat, price));
        }

        /*
     * Cứ mỗi 5 ghế thì có 1 vé miễn phí.
     *
     * 4 ghế  -> 0 vé miễn phí
     * 5 ghế  -> 1 vé miễn phí
     * 6-9    -> 1 vé miễn phí
     * 10 ghế -> 2 vé miễn phí
         */
        int freeTicketCount = seatPrices.size() / 5;

        /*
     * Ghế miễn phí phải là các ghế có giá thấp nhất.
     * Sort bản sao của danh sách giá để không thay đổi thứ tự ghế hiển thị.
         */
        List<Double> sortedPrices = new ArrayList<>(seatPrices);
        Collections.sort(sortedPrices);

        double buyFiveDiscount = 0;
        for (int i = 0; i < freeTicketCount; i++) {
            buyFiveDiscount += sortedPrices.get(i);
        }

        /*
     * Online booking hiện chưa có voucher.
     * Khi tích hợp voucher, voucher phải được tính trên:
     *
     * seatSubtotal - buyFiveDiscount
         */
        double voucherDiscount = 0;

        double amountAfterBuyFive = Math.max(
                0,
                seatSubtotal - buyFiveDiscount
        );

        /*
     * Không cho voucher làm tổng tiền âm.
         */
        voucherDiscount = Math.min(
                Math.max(0, voucherDiscount),
                amountAfterBuyFive
        );

        double finalTotal = Math.max(
                0,
                amountAfterBuyFive - voucherDiscount
        );

        draftView.setSeats(lines);
        draftView.setSeatSubtotal(seatSubtotal);
        draftView.setBuyFiveDiscount(buyFiveDiscount);
        draftView.setVoucherDiscount(voucherDiscount);
        draftView.setTotalPrice(finalTotal);

        return draftView;
    }

    public VoucherQuote quoteVoucher(String voucherCode, double subtotal) {
        // Hàm này là nguồn kiểm tra voucher dùng chung cho web đặt vé và quầy vé.
        if (voucherCode == null || voucherCode.trim().isEmpty()) {
            return VoucherQuote.invalid("Vui lòng nhập mã giảm giá.");
        }
        model.DiscountCode voucher = discountDAO.findByCode(voucherCode);
        if (voucher == null) {
            return VoucherQuote.invalid("Mã giảm giá không tồn tại.");
        }
        long now = System.currentTimeMillis();
        // Voucher phải đang hoạt động, đúng thời gian, còn lượt và đạt đơn tối thiểu.
        if (!"ACTIVE".equalsIgnoreCase(voucher.getStatus())) {
            return VoucherQuote.invalid("Mã giảm giá hiện không hoạt động.");
        }
        if (voucher.getStartDate() == null || voucher.getStartDate().getTime() > now) {
            return VoucherQuote.invalid("Mã giảm giá chưa đến thời gian áp dụng.");
        }
        if (voucher.getEndDate() == null || voucher.getEndDate().getTime() < now) {
            return VoucherQuote.invalid("Mã giảm giá đã hết hạn.");
        }
        if (voucher.getUsedCount() >= voucher.getMaxUses()) {
            return VoucherQuote.invalid("Mã giảm giá đã hết lượt sử dụng.");
        }
        if (subtotal < voucher.getMinOrderValue()) {
            return VoucherQuote.invalid("Đơn hàng chưa đạt giá trị tối thiểu để dùng mã này.");
        }

        double discount = "PERCENT".equalsIgnoreCase(voucher.getDiscountType())
                ? subtotal * voucher.getDiscountValue() / 100.0 : voucher.getDiscountValue();
        // Nếu là giảm phần trăm có mức trần thì không được giảm vượt mức trần đó.
        if (voucher.getMaxDiscountAmount() != null) {
            discount = Math.min(discount, voucher.getMaxDiscountAmount());
        }
        return VoucherQuote.valid(voucher.getCode(), voucher.getId(), Math.min(discount, subtotal));
    }

    public int createPendingBooking(int userId, BookingDraftView draftView, VoucherQuote voucherQuote) {
        // Dữ liệu draft đã được build lại từ DB trước khi tới đây, không lấy giá từ client.
        if (userId <= 0 || draftView == null || draftView.getShowtime() == null
                || draftView.getSeats() == null || draftView.getSeats().isEmpty()) {
            return -1;
        }

        List<Integer> seatIds = new ArrayList<>();
        List<Double> prices = new ArrayList<>();
        for (BookingSeatLine line : draftView.getSeats()) {
            if (line.getSeat() == null) {
                return -1;
            }
            seatIds.add(line.getSeat().getId());
            prices.add(line.getPrice());
        }

        // Tính lại từ dữ liệu server để voucher áp dụng cho cả vé và F&B, không tin giá client/session.
        double orderSubtotal = draftView.getTotalPrice() + draftView.getFnbSubtotal();
        VoucherQuote verifiedVoucher = voucherQuote != null && voucherQuote.isValid()
                ? quoteVoucher(voucherQuote.getCode(), orderSubtotal) : VoucherQuote.invalid("");
        if (voucherQuote != null && voucherQuote.isValid() && !verifiedVoucher.isValid()) return -1;
        double discount = verifiedVoucher.isValid() ? verifiedVoucher.getDiscountAmount() : 0;
        String voucherCode = discount > 0 ? verifiedVoucher.getCode() : null;
        return bookingDAO.createPendingBooking(
                userId,
                draftView.getShowtime().getId(),
                seatIds,
                prices,
                Math.max(0, orderSubtotal - discount),
                voucherCode,
                discount,
                draftView.getFnbLines()
        );
    }

    public List<BookingView> getHistory(int userId) {
        return getHistory(userId, null);
    }

    public List<BookingView> getHistory(int userId, String status) {
        if (userId <= 0) {
            return new ArrayList<>();
        }
        return bookingDAO.findHistoryByUser(userId, status);
    }

    /**
     * Trả về đúng một trang lịch sử booking cho Customer.
     *
     * status từ Controller được chuẩn hóa trước bằng normalizeStatus; giá trị lạ sẽ thành null
     * (tương đương "tất cả trạng thái"). Sau đó Service gọi
     * {@link BookingDAO#findHistoryByUserPaging(int, String, LocalDate, LocalDate, int, int)}.
     * Việc lọc nằm ở DAO/SQL chứ không lọc trong JSP, để danh sách, tổng số và phân trang
     * luôn dựa trên cùng một tập dữ liệu.
     */
    public List<BookingView> getHistoryPage(int userId, String status,
            LocalDate fromDate, LocalDate toDate, int page, int pageSize) {
        if (userId <= 0) {
            return new ArrayList<>();
        }
        return bookingDAO.findHistoryByUserPaging(userId, normalizeStatus(status),
                fromDate, toDate, Math.max(1, page), Math.max(1, pageSize));
    }

    /**
     * Đếm số booking khớp filter hiện tại. Controller dùng kết quả này để tính totalPages
     * và hiển thị số "đơn phù hợp". Query đếm dùng cùng điều kiện với query lấy danh sách.
     */
    public int countHistory(int userId, String status, LocalDate fromDate, LocalDate toDate) {
        if (userId <= 0) {
            return 0;
        }
        return bookingDAO.countHistoryByUser(userId, normalizeStatus(status), fromDate, toDate);
    }

    /**
     * Lấy số lượng theo trạng thái để JSP hiển thị badge ở các tab PENDING, CONFIRMED,
     * CHECKED_IN, USED và CANCELLED. DAO vẫn bắt buộc điều kiện userId nên số liệu của
     * Customer khác không thể bị lộ ra màn hình hiện tại.
     */
    public Map<String, Integer> getHistoryStatusCounts(int userId, LocalDate fromDate, LocalDate toDate) {
        return userId <= 0 ? new java.util.LinkedHashMap<>()
                : bookingDAO.countHistoryByUserStatus(userId, fromDate, toDate);
    }

    /**
     * Trả status đã chuẩn hóa cho JSP đánh dấu tab đang được chọn. Status không hợp lệ
     * trả null để JSP active tab "Tất cả", đồng bộ với query thực tế.
     */
    public String normalizeHistoryStatus(String status) {
        return normalizeStatus(status);
    }

    public BookingView getDetail(int bookingId, int userId) {
        if (bookingId <= 0 || userId <= 0) {
            return null;
        }
        return bookingDAO.findDetailByIdAndUser(bookingId, userId);
    }

    public List<BookingStatusHistory> getStatusHistory(int bookingId) {
        if (bookingId <= 0) {
            return new ArrayList<>();
        }
        return bookingStatusHistoryDAO.findByBookingId(bookingId);
    }

    public boolean cancel(int bookingId, int userId) {
        if (bookingId <= 0 || userId <= 0) {
            return false;
        }
        return bookingDAO.cancelByUser(bookingId, userId);
    }

    public List<BookingView> getStaffBookings(int staffId, String keyword, String status) {
        if (staffId <= 0) {
            return new ArrayList<>();
        }
        return bookingDAO.findByStaffBranch(staffId, keyword, normalizeStatus(status));
    }

    public BookingView getStaffDetail(int bookingId, int staffId) {
        if (bookingId <= 0 || staffId <= 0) {
            return null;
        }
        return bookingDAO.findDetailByIdAndStaffBranch(bookingId, staffId);
    }

    public boolean cancelByStaff(int bookingId, int staffId) {
        if (bookingId <= 0 || staffId <= 0) {
            return false;
        }
        return bookingDAO.cancelByStaffBranch(bookingId, staffId);
    }

    public boolean checkInByStaff(int bookingId, int staffId) {
        if (bookingId <= 0 || staffId <= 0) {
            return false;
        }
        return bookingDAO.checkInByStaffBranch(bookingId, staffId);
    }

    public boolean markUsedByStaff(int bookingId, int staffId) {
        if (bookingId <= 0 || staffId <= 0) {
            return false;
        }
        return bookingDAO.markUsedByStaffBranch(bookingId, staffId);
    }

    private List<Integer> cleanSeatIds(List<Integer> seatIds) {
        // LinkedHashSet vừa bỏ trùng id ghế vừa giữ thứ tự khách đã chọn.
        Set<Integer> unique = new LinkedHashSet<>();
        if (seatIds != null) {
            for (Integer seatId : seatIds) {
                if (seatId != null && seatId > 0) {
                    unique.add(seatId);
                }
            }
        }
        return new ArrayList<>(unique);
    }

    private String normalizeStatus(String status) {
        if (status == null || status.trim().isEmpty()) {
            return null;
        }
        String normalized = status.trim().toUpperCase();
        switch (normalized) {
            case "PENDING":
            case "CONFIRMED":
            case "CHECKED_IN":
            case "USED":
            case "CANCELLED":
                return normalized;
            default:
                return null;
        }
    }

    public List<BookingView> getHistoryByUserPaging(int userId, int page, int pageSize) {
        return bookingDAO.findHistoryByUserPaging(userId, page, pageSize);
    }

    public int countHistoryByUser(int userId) {
        return bookingDAO.countHistoryByUser(userId);
    }
}
