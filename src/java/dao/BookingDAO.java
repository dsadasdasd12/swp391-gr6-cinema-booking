package dao;

import dto.BookingView;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.Statement;
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.List;
import model.Booking;
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

    public int createPendingBooking(int userId, int showtimeId, List<Integer> seatIds,
                                    List<Double> seatPrices, double totalPrice,
                                    String voucherCode, double voucherDiscount) {
        if (userId <= 0 || showtimeId <= 0 || seatIds == null || seatIds.isEmpty()
                || seatPrices == null || seatPrices.size() != seatIds.size()) {
            return -1;
        }

        String checkShowtimeSql = "SELECT COUNT(*) FROM dbo.SHOWTIMES "
                + "WHERE id = ? AND status IN ('SCHEDULED','ON_SALE') AND start_time > GETDATE()";

        String seatPlaceholders = placeholders(seatIds.size());

        String checkSeatsSql = "SELECT COUNT(*) "
                + "FROM dbo.SEATS se "
                + "JOIN dbo.SHOWTIMES st ON st.hall_id = se.hall_id "
                + "WHERE st.id = ? AND se.maintenance = 0 AND se.id IN (" + seatPlaceholders + ")";

        String checkBookedSql = "SELECT COUNT(*) "
                + "FROM dbo.BOOKING_SEATS bs "
                + "JOIN dbo.BOOKINGS b ON b.id = bs.booking_id "
                + "WHERE b.showtime_id = ? "
                + "AND b.status IN ('PENDING','CONFIRMED','CHECKED_IN','USED') "
                + "AND bs.seat_id IN (" + seatPlaceholders + ")";

        String checkLockedSql = "SELECT COUNT(*) "
                + "FROM dbo.CART_ITEMS ci "
                + "JOIN dbo.CART c ON c.id = ci.cart_id "
                + "WHERE c.showtime_id = ? AND ci.locked_until > GETDATE() "
                + "AND ci.seat_id IN (" + seatPlaceholders + ")";

        String insertBookingSql = "INSERT INTO dbo.BOOKINGS "
                + "(user_id, showtime_id, source, status, total_price, qr_code, booked_at, last_update) "
                + "VALUES (?, ?, 'ONLINE', 'PENDING', ?, NULL, GETDATE(), GETDATE())";

        String updateQrSql = "UPDATE dbo.BOOKINGS SET qr_code = ? WHERE id = ?";

        String insertSeatSql = "INSERT INTO dbo.BOOKING_SEATS "
                + "(booking_id, seat_id, price, last_update) VALUES (?, ?, ?, GETDATE())";

        String insertPaymentSql = "INSERT INTO dbo.PAYMENTS "
                + "(booking_id, type, method, transaction_id, status, amount, paid_at, gateway, last_update) "
                + "VALUES (?, 'ONLINE', 'BANKING', ?, 'PENDING', ?, NULL, 'MANUAL', GETDATE())";

        String consumeVoucherSql = "UPDATE dbo.DISCOUNT_CODES SET used_count = used_count + 1, last_update = GETDATE() "
                + "OUTPUT INSERTED.id "
                + "WHERE code = ? AND status = 'ACTIVE' AND start_date <= GETDATE() AND end_date >= GETDATE() "
                + "AND used_count < max_uses";
        String insertVoucherHistorySql = "INSERT INTO dbo.VOUCHER_HISTORY "
                + "(booking_id, user_id, discount_code_id, discount_amount, used_at) VALUES (?, ?, ?, ?, GETDATE())";

        Connection conn = null;
        try {
            conn = new DBContext().getConnection();
            conn.setAutoCommit(false);

            int voucherCodeId = 0;
            if (voucherCode != null && !voucherCode.trim().isEmpty() && voucherDiscount > 0) {
                try (PreparedStatement ps = conn.prepareStatement(consumeVoucherSql)) {
                    ps.setString(1, voucherCode.trim().toUpperCase());
                    try (ResultSet rs = ps.executeQuery()) {
                        if (!rs.next()) {
                            conn.rollback();
                            return -1;
                        }
                        voucherCodeId = rs.getInt(1);
                    }
                }
            }

            try (PreparedStatement ps = conn.prepareStatement(checkShowtimeSql)) {
                ps.setInt(1, showtimeId);
                try (ResultSet rs = ps.executeQuery()) {
                    if (!rs.next() || rs.getInt(1) == 0) {
                        conn.rollback();
                        return -1;
                    }
                }
            }

            try (PreparedStatement ps = conn.prepareStatement(checkSeatsSql)) {
                ps.setInt(1, showtimeId);
                bindSeatIds(ps, seatIds, 2);
                try (ResultSet rs = ps.executeQuery()) {
                    if (!rs.next() || rs.getInt(1) != seatIds.size()) {
                        conn.rollback();
                        return -1;
                    }
                }
            }

            try (PreparedStatement ps = conn.prepareStatement(checkBookedSql)) {
                ps.setInt(1, showtimeId);
                bindSeatIds(ps, seatIds, 2);
                try (ResultSet rs = ps.executeQuery()) {
                    if (rs.next() && rs.getInt(1) > 0) {
                        conn.rollback();
                        return -1;
                    }
                }
            }

            try (PreparedStatement ps = conn.prepareStatement(checkLockedSql)) {
                ps.setInt(1, showtimeId);
                bindSeatIds(ps, seatIds, 2);
                try (ResultSet rs = ps.executeQuery()) {
                    if (rs.next() && rs.getInt(1) > 0) {
                        conn.rollback();
                        return -1;
                    }
                }
            }

            int bookingId = -1;
            try (PreparedStatement ps = conn.prepareStatement(insertBookingSql, Statement.RETURN_GENERATED_KEYS)) {
                ps.setInt(1, userId);
                ps.setInt(2, showtimeId);
                ps.setDouble(3, totalPrice);
                ps.executeUpdate();

                try (ResultSet rs = ps.getGeneratedKeys()) {
                    if (rs.next()) {
                        bookingId = rs.getInt(1);
                    }
                }
            }

            if (bookingId <= 0) {
                conn.rollback();
                return -1;
            }

            try (PreparedStatement ps = conn.prepareStatement(updateQrSql)) {
                ps.setString(1, "RV-ONLINE-" + bookingId);
                ps.setInt(2, bookingId);
                ps.executeUpdate();
            }

            try (PreparedStatement ps = conn.prepareStatement(insertSeatSql)) {
                for (int i = 0; i < seatIds.size(); i++) {
                    ps.setInt(1, bookingId);
                    ps.setInt(2, seatIds.get(i));
                    ps.setDouble(3, seatPrices.get(i));
                    ps.addBatch();
                }
                ps.executeBatch();
            }

            try (PreparedStatement ps = conn.prepareStatement(insertPaymentSql)) {
                ps.setInt(1, bookingId);
                ps.setString(2, "RVS" + bookingId);
                ps.setDouble(3, totalPrice);
                ps.executeUpdate();
            }

            if (voucherCodeId > 0) {
                try (PreparedStatement ps = conn.prepareStatement(insertVoucherHistorySql)) {
                    ps.setInt(1, bookingId);
                    ps.setInt(2, userId);
                    ps.setInt(3, voucherCodeId);
                    ps.setDouble(4, voucherDiscount);
                    ps.executeUpdate();
                }
            }

            conn.commit();
            return bookingId;
        } catch (Exception e) {
            e.printStackTrace();
            if (conn != null) {
                try {
                    conn.rollback();
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

    public List<BookingView> findHistoryByUser(int userId, String status) {
        List<BookingView> list = new ArrayList<>();
        String sql = bookingViewSelect()
                + "WHERE bk.user_id = ? ";
        if (status != null && !status.trim().isEmpty()) {
            sql += "AND bk.status = ? ";
        }
        sql += "GROUP BY bk.id, bk.user_id, bk.showtime_id, bk.source, bk.status, bk.total_price, "
                + "bk.qr_code, bk.booked_at, m.id, m.title, br.name, h.name, s.start_time, u.full_name, u.email "
                + "ORDER BY bk.booked_at DESC, bk.id DESC";

        try (Connection conn = new DBContext().getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, userId);
            if (status != null && !status.trim().isEmpty()) {
                ps.setString(2, status);
            }
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    list.add(mapBookingView(rs));
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return list;
    }

    public List<BookingView> findHistoryByUser(int userId) {
        return findHistoryByUser(userId, null);
    }

    public BookingView findDetailByIdAndUser(int bookingId, int userId) {
        String sql = bookingViewSelect()
                + "WHERE bk.id = ? AND bk.user_id = ? "
                + "GROUP BY bk.id, bk.user_id, bk.showtime_id, bk.source, bk.status, bk.total_price, "
                + "bk.qr_code, bk.booked_at, m.id, m.title, br.name, h.name, s.start_time, u.full_name, u.email";

        try (Connection conn = new DBContext().getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, bookingId);
            ps.setInt(2, userId);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return mapBookingView(rs);
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }

    public boolean cancelByUser(int bookingId, int userId) {
        String updateBookingSql = "UPDATE dbo.BOOKINGS "
                + "SET status = 'CANCELLED', last_update = GETDATE() "
                + "WHERE id = ? AND user_id = ? AND status IN ('PENDING','CONFIRMED')";
        String updatePaymentSql = "UPDATE dbo.PAYMENTS "
                + "SET status = 'FAILED', last_update = GETDATE() "
                + "WHERE booking_id = ?";

        Connection conn = null;
        try {
            conn = new DBContext().getConnection();
            conn.setAutoCommit(false);

            int affected;
            try (PreparedStatement ps = conn.prepareStatement(updateBookingSql)) {
                ps.setInt(1, bookingId);
                ps.setInt(2, userId);
                affected = ps.executeUpdate();
            }

            if (affected == 0) {
                conn.rollback();
                return false;
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
                try {
                    conn.rollback();
                } catch (Exception ex) {
                    ex.printStackTrace();
                }
            }
        } finally {
            if (conn != null) {
                try {
                    conn.close();
                } catch (Exception ex) {
                    ex.printStackTrace();
                }
            }
        }
        return false;
    }

    public List<BookingView> findByStaffBranch(int staffId, String keyword, String status) {
        List<BookingView> list = new ArrayList<>();
        String sql = bookingViewSelect()
                + "JOIN dbo.STAFF_BRANCH sb ON sb.branch_id = br.id "
                + "WHERE sb.user_id = ? "
                + "AND (? IS NULL OR bk.status = ?) "
                + "AND (? IS NULL OR m.title LIKE ? OR u.full_name LIKE ? OR CAST(bk.id AS varchar(20)) = ?) "
                + "GROUP BY bk.id, bk.user_id, bk.showtime_id, bk.source, bk.status, bk.total_price, "
                + "bk.qr_code, bk.booked_at, m.id, m.title, br.name, h.name, s.start_time, u.full_name, u.email "
                + "ORDER BY bk.booked_at DESC, bk.id DESC";

        String normalizedStatus = blankToNull(status);
        String normalizedKeyword = blankToNull(keyword);
        String like = normalizedKeyword == null ? null : "%" + normalizedKeyword + "%";

        try (Connection conn = new DBContext().getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, staffId);
            ps.setString(2, normalizedStatus);
            ps.setString(3, normalizedStatus);
            ps.setString(4, normalizedKeyword);
            ps.setString(5, like);
            ps.setString(6, like);
            ps.setString(7, normalizedKeyword);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    list.add(mapBookingView(rs));
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return list;
    }

    public BookingView findDetailByIdAndStaffBranch(int bookingId, int staffId) {
        String sql = bookingViewSelect()
                + "JOIN dbo.STAFF_BRANCH sb ON sb.branch_id = br.id "
                + "WHERE bk.id = ? AND sb.user_id = ? "
                + "GROUP BY bk.id, bk.user_id, bk.showtime_id, bk.source, bk.status, bk.total_price, "
                + "bk.qr_code, bk.booked_at, m.id, m.title, br.name, h.name, s.start_time, u.full_name, u.email";

        try (Connection conn = new DBContext().getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, bookingId);
            ps.setInt(2, staffId);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return mapBookingView(rs);
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }

    public boolean cancelByStaffBranch(int bookingId, int staffId) {
        String updateBookingSql = "UPDATE bk "
                + "SET bk.status = 'CANCELLED', bk.last_update = GETDATE() "
                + "FROM dbo.BOOKINGS bk "
                + "JOIN dbo.SHOWTIMES s ON s.id = bk.showtime_id "
                + "JOIN dbo.HALLS h ON h.id = s.hall_id "
                + "JOIN dbo.STAFF_BRANCH sb ON sb.branch_id = h.branch_id "
                + "WHERE bk.id = ? AND sb.user_id = ? AND bk.status IN ('PENDING','CONFIRMED')";
        String updatePaymentSql = "UPDATE dbo.PAYMENTS "
                + "SET status = 'FAILED', last_update = GETDATE() "
                + "WHERE booking_id = ?";

        Connection conn = null;
        try {
            conn = new DBContext().getConnection();
            conn.setAutoCommit(false);

            int affected;
            try (PreparedStatement ps = conn.prepareStatement(updateBookingSql)) {
                ps.setInt(1, bookingId);
                ps.setInt(2, staffId);
                affected = ps.executeUpdate();
            }

            if (affected == 0) {
                conn.rollback();
                return false;
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
                try {
                    conn.rollback();
                } catch (Exception ex) {
                    ex.printStackTrace();
                }
            }
        } finally {
            if (conn != null) {
                try {
                    conn.close();
                } catch (Exception ex) {
                    ex.printStackTrace();
                }
            }
        }
        return false;
    }

    public boolean checkInByStaffBranch(int bookingId, int staffId) {
        String sql = "UPDATE bk "
                + "SET bk.status = 'CHECKED_IN', bk.last_update = GETDATE() "
                + "FROM dbo.BOOKINGS bk "
                + "JOIN dbo.SHOWTIMES s ON s.id = bk.showtime_id "
                + "JOIN dbo.HALLS h ON h.id = s.hall_id "
                + "JOIN dbo.STAFF_BRANCH sb ON sb.branch_id = h.branch_id "
                + "WHERE bk.id = ? AND sb.user_id = ? AND bk.status = 'CONFIRMED'";
        return updateStaffBookingStatus(sql, bookingId, staffId);
    }

    public boolean markUsedByStaffBranch(int bookingId, int staffId) {
        String sql = "UPDATE bk "
                + "SET bk.status = 'USED', bk.last_update = GETDATE() "
                + "FROM dbo.BOOKINGS bk "
                + "JOIN dbo.SHOWTIMES s ON s.id = bk.showtime_id "
                + "JOIN dbo.HALLS h ON h.id = s.hall_id "
                + "JOIN dbo.STAFF_BRANCH sb ON sb.branch_id = h.branch_id "
                + "WHERE bk.id = ? AND sb.user_id = ? AND bk.status = 'CHECKED_IN'";
        return updateStaffBookingStatus(sql, bookingId, staffId);
    }

    private boolean updateStaffBookingStatus(String sql, int bookingId, int staffId) {
        try (Connection conn = new DBContext().getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, bookingId);
            ps.setInt(2, staffId);
            return ps.executeUpdate() > 0;
        } catch (Exception e) {
            e.printStackTrace();
        }
        return false;
    }

    private String bookingViewSelect() {
        return "SELECT bk.id, bk.user_id, bk.showtime_id, bk.source, bk.status, bk.total_price, "
                + "bk.qr_code, bk.booked_at, "
                + "m.id AS movie_id, m.title AS movie_title, "
                + "br.name AS branch_name, h.name AS hall_name, s.start_time AS show_start, "
                + "u.full_name AS customer_name, u.email AS customer_email, "
                + "STRING_AGG(CONCAT(se.seat_row, se.seat_number), ', ') "
                + "WITHIN GROUP (ORDER BY se.seat_row, se.seat_number) AS seat_labels, "
                + "COUNT(se.id) AS seat_count "
                + "FROM dbo.BOOKINGS bk "
                + "JOIN dbo.[USER] u ON u.id = bk.user_id "
                + "JOIN dbo.SHOWTIMES s ON s.id = bk.showtime_id "
                + "JOIN dbo.MOVIES m ON m.id = s.movie_id "
                + "JOIN dbo.HALLS h ON h.id = s.hall_id "
                + "JOIN dbo.BRANCHES br ON br.id = h.branch_id "
                + "LEFT JOIN dbo.BOOKING_SEATS bs ON bs.booking_id = bk.id "
                + "LEFT JOIN dbo.SEATS se ON se.id = bs.seat_id ";
    }

    private BookingView mapBookingView(ResultSet rs) throws Exception {
        BookingView view = new BookingView();
        Booking booking = new Booking(
                rs.getInt("id"),
                rs.getInt("user_id"),
                rs.getInt("showtime_id"),
                rs.getString("source"),
                rs.getString("status"),
                rs.getDouble("total_price"),
                rs.getString("qr_code"),
                rs.getTimestamp("booked_at")
        );

        Timestamp showStart = rs.getTimestamp("show_start");

        view.setBooking(booking);
        view.setMovieId(rs.getInt("movie_id"));
        view.setMovieTitle(rs.getString("movie_title"));
        view.setBranchName(rs.getString("branch_name"));
        view.setHallName(rs.getString("hall_name"));
        view.setShowStart(showStart == null ? null : showStart.toLocalDateTime());
        view.setSeatLabels(rs.getString("seat_labels") == null ? "" : rs.getString("seat_labels"));
        view.setSeatCount(rs.getInt("seat_count"));
        view.setCustomerName(rs.getString("customer_name"));
        view.setCustomerEmail(rs.getString("customer_email"));

        return view;
    }

    private String placeholders(int size) {
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < size; i++) {
            if (i > 0) {
                sb.append(",");
            }
            sb.append("?");
        }
        return sb.toString();
    }

    private void bindSeatIds(PreparedStatement ps, List<Integer> seatIds, int startIndex) throws Exception {
        int index = startIndex;
        for (Integer seatId : seatIds) {
            ps.setInt(index++, seatId);
        }
    }

    private String blankToNull(String value) {
        if (value == null || value.trim().isEmpty()) {
            return null;
        }
        return value.trim();
    }
    
    public List<BookingView> findHistoryByUserPaging(int userId, int page, int pageSize) {
    List<BookingView> list = new ArrayList<>();

    int offset = (page - 1) * pageSize;

    String sql = bookingViewSelect()
            + "WHERE bk.user_id = ? "
            + "GROUP BY bk.id, bk.user_id, bk.showtime_id, bk.source, bk.status, bk.total_price, "
            + "bk.qr_code, bk.booked_at, m.id, m.title, br.name, h.name, s.start_time, u.full_name, u.email "
            + "ORDER BY bk.booked_at DESC, bk.id DESC "
            + "OFFSET ? ROWS FETCH NEXT ? ROWS ONLY";

    try (Connection conn = new DBContext().getConnection();
         PreparedStatement ps = conn.prepareStatement(sql)) {

        ps.setInt(1, userId);
        ps.setInt(2, offset);
        ps.setInt(3, pageSize);

        try (ResultSet rs = ps.executeQuery()) {
            while (rs.next()) {
                list.add(mapBookingView(rs));
            }
        }

    } catch (Exception e) {
        e.printStackTrace();
    }

    return list;
}
    
    public int countHistoryByUser(int userId) {
    String sql = "SELECT COUNT(*) FROM dbo.BOOKINGS WHERE user_id = ?";

    try (Connection conn = new DBContext().getConnection();
         PreparedStatement ps = conn.prepareStatement(sql)) {

        ps.setInt(1, userId);

        try (ResultSet rs = ps.executeQuery()) {
            if (rs.next()) {
                return rs.getInt(1);
            }
        }

    } catch (Exception e) {
        e.printStackTrace();
    }

    return 0;
}
}
