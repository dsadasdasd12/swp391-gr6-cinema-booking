package service;

import dao.BookingDAO;
import dao.BookingStatusHistoryDAO;
import dao.DiscountDAO;
import dto.BookingDraftView;
import dto.BookingSeatLine;
import dto.BookingView;
import dto.CounterBookingQuote;
import dto.SeatView;
import dto.VoucherQuote;
import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;
import model.Booking;
import model.BookingStatusHistory;
import model.Seat;
import model.Showtime;

public class BookingService {
    private final BookingDAO bookingDAO = new BookingDAO();
    private final BookingStatusHistoryDAO bookingStatusHistoryDAO = new BookingStatusHistoryDAO();
    private final ShowtimeService showtimeService = new ShowtimeService();
    private final SeatService seatService = new SeatService();
    private final DiscountDAO discountDAO = new DiscountDAO();
    private final UserService userService = new UserService();

    public int createCounterBooking(int staffId, int showtimeId, List<Integer> requestedSeatIds,
                                    String voucherCode, String paymentMethod) {
        CounterCalculation calculation = calculateCounterBooking(staffId, showtimeId, requestedSeatIds, voucherCode);
        if (!"CASH".equalsIgnoreCase(paymentMethod) && !"BANKING".equalsIgnoreCase(paymentMethod)) {
            throw new IllegalArgumentException("Phương thức thanh toán không hợp lệ.");
        }
        return bookingDAO.createWalkinBooking(showtimeId, calculation.seatIds, calculation.prices,
                calculation.quote.getTotal(), paymentMethod.toUpperCase(), calculation.quote.getDiscountAmount(),
                calculation.voucher.isValid() ? "Mã giảm giá: " + calculation.voucher.getCode() : "",
                calculation.voucher.isValid() ? calculation.voucher.getCode() : null, staffId);
    }

    /** Returns the same server-side calculation that will be persisted on booking. */
    public CounterBookingQuote quoteCounterBooking(int staffId, int showtimeId, List<Integer> requestedSeatIds, String voucherCode) {
        try {
            return calculateCounterBooking(staffId, showtimeId, requestedSeatIds, voucherCode).quote;
        } catch (IllegalArgumentException e) {
            return CounterBookingQuote.invalid(e.getMessage());
        }
    }

    private CounterCalculation calculateCounterBooking(int staffId, int showtimeId, List<Integer> requestedSeatIds,
                                                        String voucherCode) {
        int branchId = userService.getBranchIdOfStaff(staffId);
        Showtime showtime = showtimeService.getShowtimeById(showtimeId);
        if (branchId <= 0 || showtime == null || showtime.getBranchId() != branchId) {
            throw new IllegalArgumentException("Bạn không được bán vé cho suất chiếu này.");
        }
        if (!"SCHEDULED".equalsIgnoreCase(showtime.getStatus()) && !"ON_SALE".equalsIgnoreCase(showtime.getStatus())) {
            throw new IllegalArgumentException("Suất chiếu không mở bán.");
        }
        if (showtime.getStartTime() == null
                || showtime.getStartTime().getTime() + 30L * 60L * 1000L <= System.currentTimeMillis()) {
            throw new IllegalArgumentException("Suất chiếu đã bắt đầu quá 30 phút, không thể tiếp tục bán vé.");
        }
        List<Integer> seatIds = cleanSeatIds(requestedSeatIds);
        if (seatIds.isEmpty()) throw new IllegalArgumentException("Vui lòng chọn ít nhất một ghế.");
        List<SeatView> views = seatService.getSeatViewsByShowtimeAndIds(showtimeId, seatIds);
        if (views.size() != seatIds.size()) throw new IllegalArgumentException("Ghế không thuộc suất chiếu này.");
        List<Double> prices = new ArrayList<>();
        double subtotal = 0;
        for (SeatView view : views) {
            if (view == null || !view.isSelectable()) throw new IllegalArgumentException("Ghế đã được đặt hoặc đang bảo trì.");
            double price = showtimeService.getSeatPrice(showtimeId, view.getSeat().getSeatType(), showtime.getBasePrice());
            prices.add(price);
            subtotal += price;
        }
        VoucherQuote quote = voucherCode == null || voucherCode.trim().isEmpty()
                ? VoucherQuote.invalid("") : quoteVoucher(voucherCode, subtotal);
        if (voucherCode != null && !voucherCode.trim().isEmpty() && !quote.isValid()) {
            throw new IllegalArgumentException(quote.getMessage());
        }
        double discount = quote.isValid() ? quote.getDiscountAmount() : 0;
        return new CounterCalculation(seatIds, prices, quote, CounterBookingQuote.valid(subtotal, discount));
    }

    private static class CounterCalculation {
        final List<Integer> seatIds;
        final List<Double> prices;
        final VoucherQuote voucher;
        final CounterBookingQuote quote;
        CounterCalculation(List<Integer> seatIds, List<Double> prices, VoucherQuote voucher, CounterBookingQuote quote) {
            this.seatIds = seatIds; this.prices = prices; this.voucher = voucher; this.quote = quote;
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
        return branchId <= 0 ? null : bookingDAO.getBookingStatusInBranch(bookingId, branchId);
    }

    public boolean cancelBooking(int bookingId) {
        return bookingDAO.cancelBooking(bookingId);
    }

    public boolean cancelPendingCounterBooking(int staffId, int bookingId) {
        int branchId = userService.getBranchIdOfStaff(staffId);
        return branchId > 0 && bookingDAO.cancelPendingBookingInBranch(bookingId, branchId);
    }

    public BookingDraftView buildDraftView(int showtimeId, List<Integer> seatIds) {
        Showtime showtime = showtimeService.getBookableShowtime(showtimeId);
        if (showtime == null) {
            throw new IllegalArgumentException("Suất chiếu không còn mở đặt vé.");
        }

        List<Integer> cleanSeatIds = cleanSeatIds(seatIds);
        if (cleanSeatIds.isEmpty()) {
            throw new IllegalArgumentException("Vui lòng chọn ít nhất một ghế.");
        }

        List<SeatView> seatViews = seatService.getSeatViewsByShowtimeAndIds(showtimeId, cleanSeatIds);
        if (seatViews.size() != cleanSeatIds.size()) {
            throw new IllegalArgumentException("Danh sách ghế không hợp lệ.");
        }

        BookingDraftView draftView = new BookingDraftView();
        draftView.setShowtime(showtime);

        List<BookingSeatLine> lines = new ArrayList<>();
        double total = 0;
        for (SeatView seatView : seatViews) {
            if (seatView == null || !seatView.isSelectable()) {
                throw new IllegalArgumentException("Một hoặc nhiều ghế đã được đặt hoặc đang bảo trì.");
            }

            Seat seat = seatView.getSeat();
            double price = showtimeService.getSeatPrice(
                    showtimeId,
                    seat.getSeatType(),
                    showtime.getBasePrice()
            );
            total += price;
            lines.add(new BookingSeatLine(seat, price));
        }

        draftView.setSeats(lines);
        draftView.setTotalPrice(total);
        return draftView;
    }

    public VoucherQuote quoteVoucher(String voucherCode, double subtotal) {
        if (voucherCode == null || voucherCode.trim().isEmpty()) {
            return VoucherQuote.invalid("Vui lòng nhập mã giảm giá.");
        }
        model.DiscountCode voucher = discountDAO.findByCode(voucherCode);
        if (voucher == null) return VoucherQuote.invalid("Mã giảm giá không tồn tại.");
        long now = System.currentTimeMillis();
        if (!"ACTIVE".equalsIgnoreCase(voucher.getStatus())) return VoucherQuote.invalid("Mã giảm giá hiện không hoạt động.");
        if (voucher.getStartDate() == null || voucher.getStartDate().getTime() > now) return VoucherQuote.invalid("Mã giảm giá chưa đến thời gian áp dụng.");
        if (voucher.getEndDate() == null || voucher.getEndDate().getTime() < now) return VoucherQuote.invalid("Mã giảm giá đã hết hạn.");
        if (voucher.getUsedCount() >= voucher.getMaxUses()) return VoucherQuote.invalid("Mã giảm giá đã hết lượt sử dụng.");
        if (subtotal < voucher.getMinOrderValue()) return VoucherQuote.invalid("Đơn hàng chưa đạt giá trị tối thiểu để dùng mã này.");

        double discount = "PERCENT".equalsIgnoreCase(voucher.getDiscountType())
                ? subtotal * voucher.getDiscountValue() / 100.0 : voucher.getDiscountValue();
        if (voucher.getMaxDiscountAmount() != null) {
            discount = Math.min(discount, voucher.getMaxDiscountAmount());
        }
        return VoucherQuote.valid(voucher.getCode(), voucher.getId(), Math.min(discount, subtotal));
    }

    public int createPendingBooking(int userId, BookingDraftView draftView, VoucherQuote voucherQuote) {
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

        double discount = voucherQuote != null && voucherQuote.isValid() ? voucherQuote.getDiscountAmount() : 0;
        String voucherCode = discount > 0 ? voucherQuote.getCode() : null;
        return bookingDAO.createPendingBooking(
                userId,
                draftView.getShowtime().getId(),
                seatIds,
                prices,
                Math.max(0, draftView.getTotalPrice() - discount),
                voucherCode,
                discount
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
