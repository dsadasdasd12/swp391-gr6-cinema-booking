package service;

import dao.AttendanceDAO;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import util.DBContext;

public class TicketService {
    private final AttendanceDAO attendanceDAO = new AttendanceDAO();

    public String checkInTicket(int bookingId, int staffId) {
        return attendanceDAO.checkInTicket(bookingId, staffId);
    }

    public String getSeatCodesByBookingId(int bookingId) {
        StringBuilder seatCodes = new StringBuilder();
        String sql = "SELECT s.seat_row, s.seat_number FROM dbo.BOOKING_SEATS bs "
                   + "JOIN dbo.SEATS s ON bs.seat_id = s.id WHERE bs.booking_id = ?";
        try (Connection conn = new DBContext().getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, bookingId);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    seatCodes.append(rs.getString("seat_row")).append(rs.getInt("seat_number")).append(" ");
                }
            }
        } catch (Exception ex) {
            ex.printStackTrace();
        }
        return seatCodes.toString().trim();
    }

    public int parseBookingId(String bookingIdStr) throws NumberFormatException {
        if (bookingIdStr == null || bookingIdStr.trim().isEmpty()) {
            throw new NumberFormatException("Empty input");
        }
        String cleanIdStr = bookingIdStr.trim();
        if (cleanIdStr.contains("bookingId=")) {
            int index = cleanIdStr.indexOf("bookingId=");
            cleanIdStr = cleanIdStr.substring(index + 10);
            int ampersandIndex = cleanIdStr.indexOf("&");
            if (ampersandIndex != -1) {
                cleanIdStr = cleanIdStr.substring(0, ampersandIndex);
            }
        } else if (cleanIdStr.toUpperCase().startsWith("RV-WALK-")) {
            cleanIdStr = cleanIdStr.substring(8);
        } else if (cleanIdStr.toUpperCase().startsWith("TICKET-")) {
            cleanIdStr = cleanIdStr.substring(7);
        }
        return Integer.parseInt(cleanIdStr.trim());
    }
}
