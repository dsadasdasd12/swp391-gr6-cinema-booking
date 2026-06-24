package service;

import dao.BookingDAO;
import java.util.List;
import model.Booking;

public class BookingService {
    private final BookingDAO bookingDAO = new BookingDAO();

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
}
