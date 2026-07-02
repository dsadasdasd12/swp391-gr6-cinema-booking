// @author HuyPD
package dao;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import dto.SeatView;
import model.Seat;
import model.Hall;
import util.DBContext;

public class SeatDAO {

    public List<SeatView> findByShowtime(int showtimeId) {
        String sql = "SELECT s.id, s.hall_id, s.seat_row, s.seat_number, s.seat_type, s.maintenance, "
                + "       CASE WHEN taken.seat_id IS NULL THEN 0 ELSE 1 END AS booked "
                + "FROM dbo.SEATS s "
                + "JOIN dbo.SHOWTIMES st ON st.id = ? "
                + "LEFT JOIN ( "
                + "      SELECT bs.seat_id "
                + "      FROM dbo.BOOKING_SEATS bs "
                + "      JOIN dbo.BOOKINGS bk ON bk.id = bs.booking_id "
                + "      WHERE bk.showtime_id = ? AND bk.status <> 'CANCELLED' "
                + "      UNION "
                + "      SELECT ci.seat_id "
                + "      FROM dbo.CART_ITEMS ci "
                + "      JOIN dbo.CART c ON c.id = ci.cart_id "
                + "      WHERE c.showtime_id = ? AND ci.locked_until > ? "
                + ") taken ON taken.seat_id = s.id "
                + "WHERE s.hall_id = st.hall_id "
                + "ORDER BY s.seat_row, s.seat_number";
        List<SeatView> list = new ArrayList<>();
        try {
            Connection conn = DBContext.getInstance().getConnection();
            if (conn == null) return list;
            PreparedStatement ps = conn.prepareStatement(sql);
            ps.setInt(1, showtimeId);
            ps.setInt(2, showtimeId);
            ps.setInt(3, showtimeId);
            ps.setObject(4, LocalDateTime.now());
            ResultSet rs = ps.executeQuery();
            while (rs.next()) {
                Seat s = new Seat();
                s.setId(rs.getInt("id"));
                s.setHallId(rs.getInt("hall_id"));
                s.setSeatRow(rs.getString("seat_row"));
                s.setSeatNumber(rs.getInt("seat_number"));
                s.setSeatType(rs.getString("seat_type"));
                s.setMaintenance(rs.getBoolean("maintenance"));
                boolean booked = rs.getInt("booked") == 1;
                list.add(new SeatView(s, booked));
            }
            rs.close();
            ps.close();
            conn.close();
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return list;
    }

    public List<Seat> getSeatsByHall(int hallId) {
        List<Seat> list = new ArrayList<>();
        String sql = "SELECT * FROM dbo.SEATS WHERE hall_id = ? ORDER BY seat_row, seat_number";
        try (Connection conn = DBContext.getInstance().getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, hallId);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    list.add(new Seat(
                        rs.getInt("id"),
                        rs.getInt("hall_id"),
                        rs.getString("seat_row"),
                        rs.getInt("seat_number"),
                        rs.getString("seat_type"),
                        rs.getBoolean("maintenance")
                    ));
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return list;
    }

    public boolean updateSeatConfig(int hallId, String seatRow, int seatNumber, String seatType, boolean maintenance) {
        String sql = "UPDATE dbo.SEATS SET seat_type = ?, maintenance = ?, last_update = GETDATE() "
                   + "WHERE hall_id = ? AND seat_row = ? AND seat_number = ?";
        try (Connection conn = DBContext.getInstance().getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, seatType);
            ps.setBoolean(2, maintenance);
            ps.setInt(3, hallId);
            ps.setString(4, seatRow);
            ps.setInt(5, seatNumber);
            return ps.executeUpdate() > 0;
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
    }

    public boolean deleteSeat(int hallId, String seatRow, int seatNumber) {
        String sql = "DELETE FROM dbo.SEATS WHERE hall_id = ? AND seat_row = ? AND seat_number = ?";
        try (Connection conn = DBContext.getInstance().getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, hallId);
            ps.setString(2, seatRow);
            ps.setInt(3, seatNumber);
            boolean success = ps.executeUpdate() > 0;
            if (success) {
                recalculateTotalSeats(hallId);
            }
            return success;
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
    }

    public boolean insertSeat(int hallId, String seatRow, int seatNumber, String seatType, boolean maintenance) {
        String sql = "INSERT INTO dbo.SEATS (hall_id, seat_row, seat_number, seat_type, maintenance) VALUES (?, ?, ?, ?, ?)";
        try (Connection conn = DBContext.getInstance().getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, hallId);
            ps.setString(2, seatRow);
            ps.setInt(3, seatNumber);
            ps.setString(4, seatType);
            ps.setBoolean(5, maintenance);
            boolean success = ps.executeUpdate() > 0;
            if (success) {
                recalculateTotalSeats(hallId);
            }
            return success;
        } catch (Exception e) {
            e.printStackTrace();
        }
        return false;
    }

    private void recalculateTotalSeats(int hallId) {
        String sql = "UPDATE dbo.HALLS SET total_seats = (SELECT COUNT(*) FROM dbo.SEATS WHERE hall_id = ?) WHERE id = ?";
        try (Connection conn = DBContext.getInstance().getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, hallId);
            ps.setInt(2, hallId);
            ps.executeUpdate();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public List<Hall> getAllHalls() {
        List<Hall> list = new ArrayList<>();
        String sql = "SELECT * FROM dbo.HALLS ORDER BY id";
        try (Connection conn = DBContext.getInstance().getConnection();
             PreparedStatement ps = conn.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {
            while (rs.next()) {
                list.add(new Hall(
                    rs.getInt("id"),
                    rs.getInt("branch_id"),
                    rs.getString("name"),
                    rs.getInt("total_seats")
                ));
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return list;
    }

    public List<SeatView> findByShowtimeAndIds(int showtimeId, List<Integer> seatIds) {
        List<SeatView> list = new ArrayList<>();
        if (showtimeId <= 0 || seatIds == null || seatIds.isEmpty()) {
            return list;
        }
        StringBuilder placeholders = new StringBuilder();
        for (int i = 0; i < seatIds.size(); i++) {
            if (i > 0) placeholders.append(",");
            placeholders.append("?");
        }
        String sql =
            "SELECT s.*, " +
            "CASE WHEN EXISTS(" +
            "SELECT 1 FROM BOOKING_SEATS bs JOIN BOOKINGS b ON b.id=bs.booking_id " +
            "WHERE bs.seat_id=s.id AND b.showtime_id=? AND b.status IN('PENDING','CONFIRMED','CHECKED_IN','USED')" +
            ") OR EXISTS(" +
            "SELECT 1 FROM CART_ITEMS ci JOIN CART c ON c.id=ci.cart_id " +
            "WHERE ci.seat_id=s.id AND c.showtime_id=? AND ci.locked_until>GETDATE()" +
            ") THEN 1 ELSE 0 END booked " +
            "FROM SEATS s " +
            "JOIN SHOWTIMES st ON st.hall_id=s.hall_id " +
            "WHERE st.id=? AND s.id IN (" + placeholders + ") " +
            "ORDER BY s.seat_row,s.seat_number";

        try (Connection conn = DBContext.getInstance().getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setInt(1, showtimeId);
            ps.setInt(2, showtimeId);
            ps.setInt(3, showtimeId);
            int index = 4;
            for (Integer seatId : seatIds) {
                ps.setInt(index++, seatId);
            }

            ResultSet rs = ps.executeQuery();
            while (rs.next()) {
                Seat seat = new Seat(
                    rs.getInt("id"),
                    rs.getInt("hall_id"),
                    rs.getString("seat_row"),
                    rs.getInt("seat_number"),
                    rs.getString("seat_type"),
                    rs.getBoolean("maintenance")
                );
                SeatView view = new SeatView();
                view.setSeat(seat);
                view.setBooked(rs.getBoolean("booked"));
                list.add(view);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return list;
    }
}
