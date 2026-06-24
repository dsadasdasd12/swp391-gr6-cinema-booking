package dao;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import util.DBContext;

public class AttendanceDAO {

    // 1. READ: Kiểm tra xem vé này đã được check-in vào rạp chưa
    public boolean isAlreadyCheckedIn(int bookingId) {
        String sql = "SELECT id FROM dbo.ATTENDANCE WHERE booking_id = ?";
        try (Connection conn = new DBContext().getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, bookingId);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return true;
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return false;
    }

    // 2. READ: Xem chi tiết check-in cũ (để báo lỗi cụ thể cho staff)
    public String getCheckInDetails(int bookingId) {
        String sql = "SELECT a.checked_at, u.full_name AS staff_name "
                   + "FROM dbo.ATTENDANCE a "
                   + "JOIN dbo.[USER] u ON a.checked_by = u.id "
                   + "WHERE a.booking_id = ?";
        try (Connection conn = new DBContext().getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, bookingId);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return "Được quét lúc " + rs.getTimestamp("checked_at") + " bởi nhân viên " + rs.getString("staff_name");
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }

    // 3. WRITE: Soát vé vào cổng (Check-in Transaction)
    public String checkInTicket(int bookingId, int staffId) {
        // Kiểm tra tính hợp lệ của đơn đặt vé trước
        String bookingCheckSql = "SELECT status, total_price FROM dbo.BOOKINGS WHERE id = ?";
        String insertAttendanceSql = "INSERT INTO dbo.ATTENDANCE (booking_id, checked_by, checked_at) VALUES (?, ?, GETDATE())";
        String updateBookingStatusSql = "UPDATE dbo.BOOKINGS SET status = 'USED', last_update = GETDATE() WHERE id = ?";
        
        Connection conn = null;
        try {
            conn = new DBContext().getConnection();
            conn.setAutoCommit(false); // Bắt đầu Transaction

            // Bước A: Kiểm tra trạng thái đơn vé
            String status = null;
            try (PreparedStatement ps = conn.prepareStatement(bookingCheckSql)) {
                ps.setInt(1, bookingId);
                try (ResultSet rs = ps.executeQuery()) {
                    if (rs.next()) {
                        status = rs.getString("status");
                    } else {
                        conn.rollback();
                        return "VÉ KHÔNG TỒN TẠI: Không tìm thấy hóa đơn mã số #" + bookingId;
                    }
                }
            }

            // Kiểm tra các trường hợp không hợp lệ
            if ("PENDING".equalsIgnoreCase(status)) {
                conn.rollback();
                return "LỖI THANH TOÁN: Vé này chưa được thanh toán (Trạng thái: PENDING)";
            }
            if ("CANCELLED".equalsIgnoreCase(status)) {
                conn.rollback();
                return "VÉ ĐÃ HỦY: Đơn đặt vé này đã bị hủy bỏ trước đó";
            }
            if ("USED".equalsIgnoreCase(status) || "CHECKED_IN".equalsIgnoreCase(status) || isAlreadyCheckedIn(bookingId)) {
                conn.rollback();
                String details = getCheckInDetails(bookingId);
                return "CẢNH BÁO TRÙNG LẶP: Vé này đã được sử dụng rồi! " + (details != null ? details : "");
            }
            if (!"CONFIRMED".equalsIgnoreCase(status)) {
                conn.rollback();
                return "TRẠNG THÁI KHÔNG HỢP LỆ: Trạng thái đơn vé là " + status;
            }

            // Bước B: Chèn bản ghi check-in cổng
            try (PreparedStatement ps = conn.prepareStatement(insertAttendanceSql)) {
                ps.setInt(1, bookingId);
                ps.setInt(2, staffId);
                ps.executeUpdate();
            }

            // Bước C: Cập nhật trạng thái BOOKINGS thành 'USED'
            try (PreparedStatement ps = conn.prepareStatement(updateBookingStatusSql)) {
                ps.setInt(1, bookingId);
                ps.executeUpdate();
            }

            conn.commit(); // Thành công, commit giao dịch
            return "SUCCESS";

        } catch (Exception e) {
            e.printStackTrace();
            if (conn != null) {
                try { conn.rollback(); } catch (Exception re) { re.printStackTrace(); }
            }
            return "LỖI HỆ THỐNG: Xảy ra lỗi trong quá trình ghi nhận check-in: " + e.getMessage();
        } finally {
            if (conn != null) {
                try { conn.close(); } catch (Exception ce) { ce.printStackTrace(); }
            }
        }
    }
}
