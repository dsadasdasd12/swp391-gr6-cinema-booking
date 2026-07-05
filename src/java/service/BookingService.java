package service;

import dao.BookingDAO;
import dto.BookingDraftView;
import dto.BookingSeatLine;
import dto.BookingView;
import dto.SeatView;
import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;
import model.Booking;
import model.Seat;
import model.Showtime;

public class BookingService {
    private final BookingDAO bookingDAO = new BookingDAO();
    private final ShowtimeService showtimeService = new ShowtimeService();
    private final SeatService seatService = new SeatService();

    public int createWalkinBooking(int userId, int showtimeId, List<Integer> seatIds, List<Double> seatPrices,
                                   double totalPrice, String paymentMethod, double discountAmount, 
                                   String discountReason, int staffId) {
        return bookingDAO.createWalkinBooking(userId, showtimeId, seatIds, seatPrices, totalPrice, paymentMethod, discountAmount, discountReason, staffId);
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

    public boolean cancelBooking(int bookingId) {
        return bookingDAO.cancelBooking(bookingId);
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

    public int createPendingBooking(int userId, BookingDraftView draftView) {
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

        return bookingDAO.createPendingBooking(
                userId,
                draftView.getShowtime().getId(),
                seatIds,
                prices,
                draftView.getTotalPrice()
        );
    }

    public List<BookingView> getHistory(int userId) {
        if (userId <= 0) {
            return new ArrayList<>();
        }
        return bookingDAO.findHistoryByUser(userId);
    }

    public BookingView getDetail(int bookingId, int userId) {
        if (bookingId <= 0 || userId <= 0) {
            return null;
        }
        return bookingDAO.findDetailByIdAndUser(bookingId, userId);
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
