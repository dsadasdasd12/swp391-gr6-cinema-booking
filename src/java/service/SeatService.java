package service;

import dao.SeatDAO;
import dto.SeatView;
import java.util.List;
import model.Seat;
import model.Hall;

public class SeatService {
    private final SeatDAO seatDAO = new SeatDAO();

    public List<Seat> getSeatsByHall(int hallId) {
        return seatDAO.getSeatsByHall(hallId);
    }

    public boolean updateSeatConfig(int hallId, String seatRow, int seatNumber, String seatType, boolean maintenance) {
        return seatDAO.updateSeatConfig(hallId, seatRow, seatNumber, seatType, maintenance);
    }

    public boolean deleteSeat(int hallId, String seatRow, int seatNumber) {
        return seatDAO.deleteSeat(hallId, seatRow, seatNumber);
    }

    public boolean insertSeat(int hallId, String seatRow, int seatNumber, String seatType, boolean maintenance) {
        return seatDAO.insertSeat(hallId, seatRow, seatNumber, seatType, maintenance);
    }

    public List<Hall> getAllHalls() {
        return seatDAO.getAllHalls();
    }

    public List<SeatView> getSeatViewsByShowtimeAndIds(int showtimeId, List<Integer> seatIds) {
        return seatDAO.findByShowtimeAndIds(showtimeId, seatIds);
    }
}
