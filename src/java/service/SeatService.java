package service;

import dao.SeatDAO;
import dto.SeatView;
import java.util.List;
import model.Seat;
import model.Hall;
import model.SeatType;
import dao.SeatTypeDAO;

public class SeatService {
    private final SeatDAO seatDAO = new SeatDAO();
    private final SeatTypeDAO seatTypeDAO = new SeatTypeDAO();

    public List<Seat> getSeatsByHall(int hallId) {
        return seatDAO.getSeatsByHall(hallId);
    }

    public boolean updateSeatConfig(int hallId, String seatRow, int seatNumber, String seatType, boolean maintenance) {
        if (!isActiveSeatType(seatType)) return false;
        return seatDAO.updateSeatConfig(hallId, seatRow, seatNumber, seatType, maintenance);
    }

    public boolean deleteSeat(int hallId, String seatRow, int seatNumber) {
        return seatDAO.deleteSeat(hallId, seatRow, seatNumber);
    }

    public boolean deleteSeatsOfHall(int hallId) {
        return seatDAO.deleteSeatsOfHall(hallId);
    }

    public boolean insertSeat(int hallId, String seatRow, int seatNumber, String seatType, boolean maintenance) {
        if (!isActiveSeatType(seatType)) return false;
        return seatDAO.insertSeat(hallId, seatRow, seatNumber, seatType, maintenance);
    }

    public List<Hall> getAllHalls() {
        return seatDAO.getAllHalls();
    }

    public List<SeatView> getSeatViewsByShowtimeAndIds(int showtimeId, List<Integer> seatIds) {
        return seatDAO.findByShowtimeAndIds(showtimeId, seatIds);
    }

    public List<SeatType> getActiveSeatTypes() {
        return seatTypeDAO.findAllActive();
    }

    public List<SeatType> getAllSeatTypes() {
        return seatTypeDAO.findAll();
    }

    private boolean isActiveSeatType(String code) {
        SeatType seatType = seatTypeDAO.findByCode(code);
        return seatType != null && "ACTIVE".equalsIgnoreCase(seatType.getStatus());
    }
}
