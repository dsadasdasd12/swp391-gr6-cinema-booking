package dao;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;
import util.DBContext;

public class BookingDAO {

    // 1. WRITE: Tạo hóa đơn bán vé tại quầy (WALK-IN TRANSACTION)
    public int createWalkinBooking(int userId, int showtimeId, List<Integer> seatIds, List<Double> seatPrices,
                                   double totalPrice, String paymentMethod, double discountAmount, 
                                   String discountReason, int staffId) {
        
        String insertBookingSql = "INSERT INTO dbo.BOOKINGS (user_id, showtime_id, source, status, total_price, qr_code, booked_at, last_update) "
                                + "VALUES (?, ?, 'WALKIN', ?, ?, NULL, GETDATE(), GETDATE())";
        
        String updateQrSql = "UPDATE dbo.BOOKINGS SET qr_code = ? WHERE id = ?";
        
        String insertBookingSeatsSql = "INSERT INTO dbo.BOOKING_SEATS (booking_id, seat_id, price, last_update) "
                                     + "VALUES (?, ?, ?, GETDATE())";
        
        String insertPaymentSql = "INSERT INTO dbo.PAYMENTS (booking_id, type, method, transaction_id, status, amount, paid_at, gateway, last_update) "
                                + "VALUES (?, ?, ?, ?, ?, ?, ?, ?, GETDATE())";
        
        String insertDiscountSql = "INSERT INTO dbo.COUNTER_DISCOUNTS (booking_id, applied_by, reason, amount, applied_at) "
                                 + "VALUES (?, ?, ?, ?, GETDATE())";

        Connection conn = null;
        try {
            conn = new DBContext().getConnection();
            conn.setAutoCommit(false); // Bắt đầu Transaction

            // Kiểm tra trùng lặp ghế trước khi chèn để ngăn double booking tuyệt đối
            StringBuilder checkSeatSql = new StringBuilder("SELECT COUNT(*) FROM dbo.BOOKING_SEATS bs ")
                .append("JOIN dbo.BOOKINGS b ON bs.booking_id = b.id ")
                .append("WHERE b.showtime_id = ? AND b.status IN ('PENDING', 'CONFIRMED', 'CHECKED_IN', 'USED') ")
                .append("AND bs.seat_id IN (");
            for (int i = 0; i < seatIds.size(); i++) {
                checkSeatSql.append("?");
                if (i < seatIds.size() - 1) {
                    checkSeatSql.append(",");
                }
            }
            checkSeatSql.append(")");
            
            try (PreparedStatement ps = conn.prepareStatement(checkSeatSql.toString())) {
                ps.setInt(1, showtimeId);
                for (int i = 0; i < seatIds.size(); i++) {
                    ps.setInt(i + 2, seatIds.get(i));
                }
                try (ResultSet rs = ps.executeQuery()) {
                    if (rs.next() && rs.getInt(1) > 0) {
                        conn.rollback();
                        return -1; // Ghế đã có người đặt trước
                    }
                }
            }

            int bookingId = -1;
            // A. Chèn đơn đặt vé
            try (PreparedStatement ps = conn.prepareStatement(insertBookingSql, Statement.RETURN_GENERATED_KEYS)) {
                ps.setInt(1, userId);
                ps.setInt(2, showtimeId);
                String bStatus = "BANKING".equalsIgnoreCase(paymentMethod) ? "PENDING" : "CONFIRMED";
                ps.setString(3, bStatus);
                ps.setDouble(4, totalPrice);
                ps.executeUpdate();
                
                try (ResultSet rs = ps.getGeneratedKeys()) {
                    if (rs.next()) {
                        bookingId = rs.getInt(1);
                    }
                }
            }

            if (bookingId == -1) {
                conn.rollback();
                return -1;
            }

            // B. Cập nhật mã QR dạng mã đặt vé (Ví dụ: RV-WALK-10024)
            String qrCode = "RV-WALK-" + bookingId;
            try (PreparedStatement ps = conn.prepareStatement(updateQrSql)) {
                ps.setString(1, qrCode);
                ps.setInt(2, bookingId);
                ps.executeUpdate();
            }

            // C. Chèn từng ghế ngồi vào BOOKING_SEATS
            try (PreparedStatement ps = conn.prepareStatement(insertBookingSeatsSql)) {
                for (int i = 0; i < seatIds.size(); i++) {
                    int seatId = seatIds.get(i);
                    double seatPrice = seatPrices.get(i);
                    ps.setInt(1, bookingId);
                    ps.setInt(2, seatId);
                    ps.setDouble(3, seatPrice);
                    ps.addBatch();
                }
                ps.executeBatch();
            }

            // D. Chèn bản ghi thanh toán PAYMENTS
            String transId;
            String payStatus;
            java.sql.Timestamp paidAt;
            String payType = "BANKING".equalsIgnoreCase(paymentMethod) ? "ONLINE" : "CASH";
            String payMethod = "BANKING".equalsIgnoreCase(paymentMethod) ? "BANKING" : "CASH";

            if ("BANKING".equalsIgnoreCase(paymentMethod)) {
                transId = "PENDING-TX-" + System.currentTimeMillis() + "-" + bookingId;
                payStatus = "PENDING";
                paidAt = null;
            } else {
                transId = "CASH-TX-" + System.currentTimeMillis() + "-" + bookingId;
                payStatus = "SUCCESS";
                paidAt = new java.sql.Timestamp(System.currentTimeMillis());
            }

            try (PreparedStatement ps = conn.prepareStatement(insertPaymentSql)) {
                ps.setInt(1, bookingId);
                ps.setString(2, payType); // type: CASH hoặc ONLINE
                ps.setString(3, payMethod); // method: CASH hoặc BANKING
                ps.setString(4, transId);
                ps.setString(5, payStatus);
                ps.setDouble(6, totalPrice);
                ps.setTimestamp(7, paidAt);
                ps.setNull(8, java.sql.Types.VARCHAR);
                ps.executeUpdate();
            }

            // E. Nếu có giảm giá tại quầy, chèn vào COUNTER_DISCOUNTS
            if (discountAmount > 0) {
                try (PreparedStatement ps = conn.prepareStatement(insertDiscountSql)) {
                    ps.setInt(1, bookingId);
                    ps.setInt(2, staffId);
                    ps.setString(3, discountReason);
                    ps.setDouble(4, discountAmount);
                    ps.executeUpdate();
                }
            }

            conn.commit(); // Hoàn thành Transaction thành công
            return bookingId;

        } catch (Exception e) {
            e.printStackTrace();
            if (conn != null) {
                try {
                    conn.rollback(); // Rollback nếu xảy ra bất kỳ lỗi nào
                } catch (Exception rollbackEx) {
                    rollbackEx.printStackTrace();
                }
            }
        } finally {
            if (conn != null) {
                try {
                    conn.close();
                } catch (Exception closeEx) {
                    closeEx.printStackTrace();
                }
            }
        }
        return -1;
    }

    // 2. WRITE: Hỗ trợ đổi ghế ngồi (chỉ khi vé ở trạng thái PENDING hoặc trước khi check-in)
    public boolean changeBookingSeats(int bookingId, List<Integer> oldSeatIds, List<Integer> newSeatIds, List<Double> newPrices) {
        String deleteSql = "DELETE FROM dbo.BOOKING_SEATS WHERE booking_id = ? AND seat_id = ?";
        String insertSql = "INSERT INTO dbo.BOOKING_SEATS (booking_id, seat_id, price, last_update) VALUES (?, ?, ?, GETDATE())";
        
        Connection conn = null;
        try {
            conn = new DBContext().getConnection();
            conn.setAutoCommit(false);
            
            // Xóa ghế cũ
            try (PreparedStatement ps = conn.prepareStatement(deleteSql)) {
                for (int oldId : oldSeatIds) {
                    ps.setInt(1, bookingId);
                    ps.setInt(2, oldId);
                    ps.addBatch();
                }
                ps.executeBatch();
            }
            
            // Thêm ghế mới
            try (PreparedStatement ps = conn.prepareStatement(insertSql)) {
                for (int i = 0; i < newSeatIds.size(); i++) {
                    ps.setInt(1, bookingId);
                    ps.setInt(2, newSeatIds.get(i));
                    ps.setDouble(3, newPrices.get(i));
                    ps.addBatch();
                }
                ps.executeBatch();
            }
            
            conn.commit();
            return true;
        } catch (Exception e) {
            e.printStackTrace();
            if (conn != null) {
                try { conn.rollback(); } catch (Exception re) { re.printStackTrace(); }
            }
        } finally {
            if (conn != null) {
                try { conn.close(); } catch (Exception ce) { ce.printStackTrace(); }
            }
        }
        return false;
    }

    // 3. READ: Lấy danh sách ID ghế đã bán hoặc đang bị khóa tạm thời trong giỏ hàng
    public List<Integer> getBookedSeatIds(int showtimeId) {
        List<Integer> list = new ArrayList<>();
        
        // SQL lấy ghế đã bán chính thức (Trạng thái đặt vé không phải CANCELLED)
        String bookedSql = "SELECT bs.seat_id "
                         + "FROM dbo.BOOKING_SEATS bs "
                         + "JOIN dbo.BOOKINGS b ON bs.booking_id = b.id "
                         + "WHERE b.showtime_id = ? AND b.status IN ('PENDING', 'CONFIRMED', 'CHECKED_IN', 'USED')";
        
        // SQL lấy ghế đang bị khóa tạm thời trong giỏ hàng (Expires/Locked chưa hết hạn)
        String lockedSql = "SELECT ci.seat_id "
                         + "FROM dbo.CART_ITEMS ci "
                         + "JOIN dbo.CART c ON ci.cart_id = c.id "
                         + "WHERE c.showtime_id = ? AND ci.locked_until > GETDATE()";

        try (Connection conn = new DBContext().getConnection()) {
            // A. Lấy ghế đã đặt chính thức
            try (PreparedStatement ps = conn.prepareStatement(bookedSql)) {
                ps.setInt(1, showtimeId);
                try (ResultSet rs = ps.executeQuery()) {
                    while (rs.next()) {
                        list.add(rs.getInt("seat_id"));
                    }
                }
            }
            // B. Lấy ghế đang khóa trong giỏ hàng
            try (PreparedStatement ps = conn.prepareStatement(lockedSql)) {
                ps.setInt(1, showtimeId);
                try (ResultSet rs = ps.executeQuery()) {
                    while (rs.next()) {
                        int seatId = rs.getInt("seat_id");
                        if (!list.contains(seatId)) {
                            list.add(seatId);
                        }
                    }
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return list;
    }

    // 4. READ SINGLE: Lấy thông tin chi tiết một hóa đơn đặt vé
    public model.Booking getBookingById(int id) {
        String sql = "SELECT * FROM dbo.BOOKINGS WHERE id = ?";
        try (Connection conn = new DBContext().getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, id);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return new model.Booking(
                        rs.getInt("id"),
                        rs.getInt("user_id"),
                        rs.getInt("showtime_id"),
                        rs.getString("source"),
                        rs.getString("status"),
                        rs.getDouble("total_price"),
                        rs.getString("qr_code"),
                        rs.getTimestamp("booked_at")
                    );
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }

    // 5. READ RECENT: Lấy danh sách hóa đơn đặt vé gần đây cho trình giả lập
    public List<model.Booking> getRecentBookings(int limit) {
        List<model.Booking> list = new ArrayList<>();
        String sql = "SELECT TOP (?) * FROM dbo.BOOKINGS ORDER BY id DESC";
        try (Connection conn = new DBContext().getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, limit);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    list.add(new model.Booking(
                        rs.getInt("id"),
                        rs.getInt("user_id"),
                        rs.getInt("showtime_id"),
                        rs.getString("source"),
                        rs.getString("status"),
                        rs.getDouble("total_price"),
                        rs.getString("qr_code"),
                        rs.getTimestamp("booked_at")
                    ));
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return list;
    }

    // confirmPayment: Webhook cập nhật đơn hàng thành CONFIRMED và thanh toán thành SUCCESS
    public boolean confirmPayment(int bookingId, String transactionId, double amount, String gateway) {
        String updateBookingSql = "UPDATE dbo.BOOKINGS SET status = 'CONFIRMED', last_update = GETDATE() WHERE id = ?";
        String updatePaymentSql = "UPDATE dbo.PAYMENTS SET status = 'SUCCESS', transaction_id = ?, amount = ?, gateway = ?, paid_at = GETDATE(), last_update = GETDATE() WHERE booking_id = ?";
        
        Connection conn = null;
        try {
            conn = new DBContext().getConnection();
            conn.setAutoCommit(false);
            
            try (PreparedStatement ps = conn.prepareStatement(updateBookingSql)) {
                ps.setInt(1, bookingId);
                ps.executeUpdate();
            }
            
            try (PreparedStatement ps = conn.prepareStatement(updatePaymentSql)) {
                ps.setString(1, transactionId);
                ps.setDouble(2, amount);
                ps.setString(3, gateway);
                ps.setInt(4, bookingId);
                ps.executeUpdate();
            }
            
            conn.commit();
            return true;
        } catch (Exception e) {
            e.printStackTrace();
            if (conn != null) {
                try { conn.rollback(); } catch (Exception ex) { ex.printStackTrace(); }
            }
        } finally {
            if (conn != null) {
                try { conn.close(); } catch (Exception ex) { ex.printStackTrace(); }
            }
        }
        return false;
    }

    // getBookingStatus: Trả về trạng thái của booking
    public String getBookingStatus(int bookingId) {
        String sql = "SELECT status FROM dbo.BOOKINGS WHERE id = ?";
        try (Connection conn = new DBContext().getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, bookingId);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return rs.getString("status");
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return "NOT_FOUND";
    }

    // cancelBooking: Cập nhật booking thành CANCELLED và thanh toán thành FAILED để giải phóng ghế
    public boolean cancelBooking(int bookingId) {
        String updateBookingSql = "UPDATE dbo.BOOKINGS SET status = 'CANCELLED', last_update = GETDATE() WHERE id = ?";
        String updatePaymentSql = "UPDATE dbo.PAYMENTS SET status = 'FAILED', last_update = GETDATE() WHERE booking_id = ?";
        
        Connection conn = null;
        try {
            conn = new DBContext().getConnection();
            conn.setAutoCommit(false);
            
            try (PreparedStatement ps = conn.prepareStatement(updateBookingSql)) {
                ps.setInt(1, bookingId);
                ps.executeUpdate();
            }
            
            try (PreparedStatement ps = conn.prepareStatement(updatePaymentSql)) {
                ps.setInt(1, bookingId);
                ps.executeUpdate();
            }
            
            conn.commit();
            return true;
        } catch (Exception e) {
            e.printStackTrace();
            if (conn != null) {
                try { conn.rollback(); } catch (Exception ex) { ex.printStackTrace(); }
            }
        } finally {
            if (conn != null) {
                try { conn.close(); } catch (Exception ex) { ex.printStackTrace(); }
            }
        }
        return false;
    }
}
