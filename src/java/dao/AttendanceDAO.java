package dao;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.Date;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import dto.AttendanceHistoryView;
import util.DBContext;

public class AttendanceDAO {

    /** Lịch sử chỉ lấy lượt do chính staff thực hiện. */
    public List<AttendanceHistoryView> getHistoryByStaff(int staffId, LocalDate checkedDate,
            int offset, int pageSize) {
        List<AttendanceHistoryView> history = new ArrayList<>();
        String sql = "SELECT a.booking_id, b.qr_code, m.title AS movie_title, "
                + "h.name AS hall_name, st.start_time, a.checked_at "
                + "FROM dbo.ATTENDANCE a "
                + "JOIN dbo.BOOKINGS b ON b.id = a.booking_id "
                + "JOIN dbo.SHOWTIMES st ON st.id = b.showtime_id "
                + "JOIN dbo.HALLS h ON h.id = st.hall_id "
                + "JOIN dbo.MOVIES m ON m.id = st.movie_id "
                + "WHERE a.checked_by = ? "
                + "AND (? IS NULL OR CAST(a.checked_at AS DATE) = ?) "
                + "ORDER BY a.checked_at DESC OFFSET ? ROWS FETCH NEXT ? ROWS ONLY";

        try (Connection conn = new DBContext().getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, staffId);
            if (checkedDate == null) {
                ps.setNull(2, java.sql.Types.DATE);
                ps.setNull(3, java.sql.Types.DATE);
            } else {
                Date sqlDate = Date.valueOf(checkedDate);
                ps.setDate(2, sqlDate);
                ps.setDate(3, sqlDate);
            }
            ps.setInt(4, Math.max(0, offset));
            ps.setInt(5, Math.max(1, pageSize));
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    history.add(new AttendanceHistoryView(rs.getInt("booking_id"),
                            rs.getString("qr_code"), rs.getString("movie_title"),
                            rs.getString("hall_name"), rs.getTimestamp("start_time"),
                            rs.getTimestamp("checked_at")));
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return history;
    }

    /** Đếm để tạo phân trang, áp dụng đúng cùng bộ lọc lịch sử. */
    public int countHistoryByStaff(int staffId, LocalDate checkedDate) {
        String sql = "SELECT COUNT(*) FROM dbo.ATTENDANCE a WHERE a.checked_by = ? "
                + "AND (? IS NULL OR CAST(a.checked_at AS DATE) = ?)";
        try (Connection conn = new DBContext().getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, staffId);
            if (checkedDate == null) {
                ps.setNull(2, java.sql.Types.DATE);
                ps.setNull(3, java.sql.Types.DATE);
            } else {
                Date sqlDate = Date.valueOf(checkedDate);
                ps.setDate(2, sqlDate);
                ps.setDate(3, sqlDate);
            }
            try (ResultSet rs = ps.executeQuery()) {
                return rs.next() ? rs.getInt(1) : 0;
            }
        } catch (Exception e) {
            e.printStackTrace();
            return 0;
        }
    }

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
        String bookingCheckSql = "SELECT b.status, h.branch_id, s.start_time, s.end_time "
                + "FROM dbo.BOOKINGS b JOIN dbo.SHOWTIMES s ON b.showtime_id = s.id "
                + "JOIN dbo.HALLS h ON s.hall_id = h.id WHERE b.id = ?";
        String staffCheckSql = "SELECT u.role, sb.branch_id FROM dbo.[USER] u LEFT JOIN dbo.STAFF_BRANCH sb ON u.id = sb.user_id WHERE u.id = ?";
        String insertAttendanceSql = "INSERT INTO dbo.ATTENDANCE (booking_id, checked_by, checked_at) VALUES (?, ?, GETDATE())";
        String updateBookingStatusSql = "UPDATE dbo.BOOKINGS SET status = 'USED', last_update = GETDATE() "
                + "WHERE id = ? AND status = 'CONFIRMED'";
        
        Connection conn = null;
        try {
            conn = new DBContext().getConnection();
            conn.setAutoCommit(false); // Bắt đầu Transaction

            // Bước A: Kiểm tra trạng thái đơn vé và chi nhánh của vé
        String status = null;
        int bookingBranchId = -1;
        java.sql.Timestamp startTime = null;
        java.sql.Timestamp endTime = null;
            try (PreparedStatement ps = conn.prepareStatement(bookingCheckSql)) {
                ps.setInt(1, bookingId);
                try (ResultSet rs = ps.executeQuery()) {
                    if (rs.next()) {
                        status = rs.getString("status");
                        bookingBranchId = rs.getInt("branch_id");
                        startTime = rs.getTimestamp("start_time");
                        endTime = rs.getTimestamp("end_time");
                    } else {
                        conn.rollback();
                        return "VÉ KHÔNG TỒN TẠI: Không tìm thấy hóa đơn mã số #" + bookingId;
                    }
                }
            }

            // Kiểm tra phân quyền và chi nhánh của nhân viên soát vé
            String staffRole = null;
            Integer staffBranchId = null;
            try (PreparedStatement ps = conn.prepareStatement(staffCheckSql)) {
                ps.setInt(1, staffId);
                try (ResultSet rs = ps.executeQuery()) {
                    if (rs.next()) {
                        staffRole = rs.getString("role");
                        if (rs.getObject("branch_id") != null) {
                            staffBranchId = rs.getInt("branch_id");
                        }
                    }
                }
            }

            if (!"STAFF".equalsIgnoreCase(staffRole)) {
                conn.rollback();
                return "LỖI PHÂN QUYỀN: Chỉ nhân viên được phép soát vé.";
            }

            if (staffBranchId == null) {
                conn.rollback();
                return "LỖI PHÂN QUYỀN: Nhân viên soát vé chưa được gán vào chi nhánh nào.";
            }

            if (!staffBranchId.equals(bookingBranchId)) {
                conn.rollback();
                return "SAI CHI NHÁNH: Vé này thuộc chi nhánh khác, không thể soát tại đây!";
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
            long now = System.currentTimeMillis();
            long early = startTime == null ? Long.MAX_VALUE : startTime.getTime() - 30L * 60L * 1000L;
            long late = endTime == null ? Long.MIN_VALUE : endTime.getTime() + 30L * 60L * 1000L;
            if (now < early || now > late) {
                conn.rollback();
                return "CHƯA ĐẾN GIỜ SOÁT VÉ: Chỉ được soát gần thời gian suất chiếu.";
            }

            // Claim the booking before inserting attendance so concurrent scanners cannot both succeed.
            try (PreparedStatement ps = conn.prepareStatement(updateBookingStatusSql)) {
                ps.setInt(1, bookingId);
                if (ps.executeUpdate() != 1) {
                    conn.rollback();
                    return "CẢNH BÁO TRÙNG LẶP: Vé này đã được sử dụng hoặc không còn hợp lệ.";
                }
            }

            try (PreparedStatement ps = conn.prepareStatement(insertAttendanceSql)) {
                ps.setInt(1, bookingId);
                ps.setInt(2, staffId);
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
