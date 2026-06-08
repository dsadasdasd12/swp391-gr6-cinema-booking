package service;

import dao.ShowtimeDAO;
import java.util.List;
import model.Showtime;

public class ShowtimeService {
    private final ShowtimeDAO showtimeDAO = new ShowtimeDAO();

    public List<Showtime> getActiveShowtimesByBranch(int branchId) {
        return showtimeDAO.getActiveShowtimesByBranch(branchId);
    }

    public Showtime getShowtimeById(int id) {
        return showtimeDAO.getShowtimeById(id);
    }

    public boolean setSeatPricing(int showtimeId, String seatType, double price) {
        return showtimeDAO.setSeatPricing(showtimeId, seatType, price);
    }

    public double getSeatPrice(int showtimeId, String seatType, double basePrice) {
        return showtimeDAO.getSeatPrice(showtimeId, seatType, basePrice);
    }

    public int getBookedSeatsCount(int showtimeId) {
        return showtimeDAO.getBookedSeatsCount(showtimeId);
    }

    public int getTotalSeatsInHall(int showtimeId) {
        return showtimeDAO.getTotalSeatsInHall(showtimeId);
    }

    public double getOccupancyRate(int showtimeId) {
        return showtimeDAO.getOccupancyRate(showtimeId);
    }

    public List<Showtime> getShowtimesByBranchAndDate(int branchId, String dateStr) {
        return showtimeDAO.getShowtimesByBranchAndDate(branchId, dateStr);
    }
}
