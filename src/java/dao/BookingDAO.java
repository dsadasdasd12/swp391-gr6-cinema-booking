package dao;

import dto.BookingView;
import dto.BookingFnbLine;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.Statement;
import java.sql.Timestamp;
import java.sql.Date;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import model.Booking;
import util.DBContext;

public class BookingDAO {

    private static final String WALKIN_CUSTOMER_EMAIL = "walkin@rapviet.local";

    // 1. WRITE: Tạo hóa đơn bán vé tại quầy (WALK-IN TRANSACTION)
    /**
     * Overload cũ để giữ tương thích với code chưa truyền F&B.
     */
    public int createWalkinBooking(int showtimeId, List<Integer> seatIds,
            List<Double> seatPrices, double totalPrice,
            String paymentMethod, double discountAmount,
            String discountReason, String voucherCode, int staffId) {
        return createWalkinBooking(
                showtimeId,
                seatIds,
                seatPrices,
                totalPrice,
                paymentMethod,
                discountAmount,
                discountReason,
                voucherCode,
                staffId,
                java.util.Collections.emptyList()
        );
    }

    // ===== F&B STAFF - WALK-IN TRANSACTION BEGIN =====
    public int createWalkinBooking(int showtimeId, List<Integer> seatIds,
            List<Double> seatPrices, double ticketTotal,
            String paymentMethod, double discountAmount,
            String discountReason, String voucherCode, int staffId,
            List<BookingFnbLine> selectedFnb) {

        String insertBookingSql = "INSERT INTO dbo.BOOKINGS (user_id, showtime_id, source, status, total_price, qr_code, booked_at, last_update) "
                + "VALUES (?, ?, 'WALKIN', ?, ?, NULL, GETDATE(), GETDATE())";

        String updateQrSql = "UPDATE dbo.BOOKINGS SET qr_code = ? WHERE id = ?";

        String insertBookingSeatsSql = "INSERT INTO dbo.BOOKING_SEATS (booking_id, seat_id, price, last_update) "
                + "VALUES (?, ?, ?, GETDATE())";

        String insertPaymentSql = "INSERT INTO dbo.PAYMENTS (booking_id, type, method, transaction_id, status, amount, paid_at, gateway, last_update) "
                + "VALUES (?, ?, ?, ?, ?, ?, ?, ?, GETDATE())";

        String insertDiscountSql = "INSERT INTO dbo.COUNTER_DISCOUNTS (booking_id, applied_by, reason, amount, applied_at) "
                + "VALUES (?, ?, ?, ?, GETDATE())";
        String consumeVoucherSql = "UPDATE dbo.DISCOUNT_CODES SET used_count = used_count + 1, last_update = GETDATE() "
                + "WHERE code = ? AND status = 'ACTIVE' AND start_date <= GETDATE() AND end_date >= GETDATE() "
                + "AND used_count < max_uses";

        String insertFnbSql = "INSERT INTO dbo.BOOKING_FNB "
                + "(booking_id,item_type,product_id,combo_id,item_name,quantity,unit_price,status,last_update) "
                + "VALUES (?,?,?,?,?,?,?,?,GETDATE())";

        String consumeProductSql = "UPDATE dbo.BRANCH_FNB_INVENTORY "
                + "SET stock_quantity = stock_quantity - ?, last_update = GETDATE() "
                + "WHERE branch_id = (SELECT h.branch_id FROM dbo.SHOWTIMES st "
                + "JOIN dbo.HALLS h ON h.id = st.hall_id WHERE st.id = ?) "
                + "AND product_id = ? AND enabled_at_branch = 1 "
                + "AND stock_quantity >= ?";

        String consumeComboSql = "UPDATE inv "
                + "SET inv.stock_quantity = inv.stock_quantity - (ci.quantity * ?), "
                + "inv.last_update = GETDATE() "
                + "FROM dbo.BRANCH_FNB_INVENTORY inv "
                + "JOIN dbo.FNB_COMBO_ITEMS ci ON ci.product_id = inv.product_id "
                + "JOIN dbo.HALLS h ON h.branch_id = inv.branch_id "
                + "JOIN dbo.SHOWTIMES st ON st.hall_id = h.id "
                + "WHERE st.id = ? AND ci.combo_id = ? "
                + "AND inv.enabled_at_branch = 1 "
                + "AND inv.stock_quantity >= ci.quantity * ?";

        if (showtimeId <= 0 || seatIds == null || seatIds.isEmpty()
                || seatPrices == null || seatPrices.size() != seatIds.size()) {
            return -1;
        }

        List<BookingFnbLine> safeFnb = selectedFnb == null
                ? java.util.Collections.emptyList()
                : selectedFnb;

        double fnbTotal = safeFnb.stream()
                .filter(line -> line != null && line.getQuantity() > 0)
                .mapToDouble(BookingFnbLine::getLineTotal)
                .sum();

        double finalTotal = Math.max(0, ticketTotal + fnbTotal);

        Connection conn = null;
        try {
            conn = new DBContext().getConnection();
            conn.setAutoCommit(false); // Bắt đầu Transaction

            int walkinCustomerId = getOrCreateWalkinCustomer(conn);
            if (walkinCustomerId <= 0) {
                conn.rollback();
                return -1;
            }

            if (voucherCode != null && !voucherCode.trim().isEmpty() && discountAmount > 0) {
                try (PreparedStatement ps = conn.prepareStatement(consumeVoucherSql)) {
                    ps.setString(1, voucherCode.trim().toUpperCase());
                    if (ps.executeUpdate() != 1) {
                        conn.rollback();
                        return -1;
                    }
                }
            }

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
                ps.setInt(1, walkinCustomerId);
                ps.setInt(2, showtimeId);
                String bStatus = "BANKING".equalsIgnoreCase(paymentMethod) ? "PENDING" : "CONFIRMED";
                ps.setString(3, bStatus);
                ps.setDouble(4, finalTotal);
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

            // ===== F&B STAFF - SAVE WALK-IN F&B BEGIN =====
            String bookingFnbStatus = "BANKING".equalsIgnoreCase(paymentMethod)
                    ? "PENDING"
                    : "PREPARING";

            if (!safeFnb.isEmpty()) {
                try (PreparedStatement insertFnb = conn.prepareStatement(insertFnbSql)) {
                    for (BookingFnbLine line : safeFnb) {
                        if (line == null || line.getQuantity() <= 0) {
                            continue;
                        }

                        String itemType = line.getItemType() == null
                                ? ""
                                : line.getItemType().trim().toUpperCase();

                        int affected;

                        if ("PRODUCT".equals(itemType)) {
                            try (PreparedStatement stock = conn.prepareStatement(consumeProductSql)) {
                                stock.setInt(1, line.getQuantity());
                                stock.setInt(2, showtimeId);
                                stock.setInt(3, line.getItemId());
                                stock.setInt(4, line.getQuantity());
                                affected = stock.executeUpdate();
                            }

                            if (affected != 1) {
                                conn.rollback();
                                return -1;
                            }
                        } else if ("COMBO".equals(itemType)) {
                            int requiredProductCount;

                            try (PreparedStatement count = conn.prepareStatement(
                                    "SELECT COUNT(*) FROM dbo.FNB_COMBO_ITEMS WHERE combo_id = ?")) {
                                count.setInt(1, line.getItemId());

                                try (ResultSet rs = count.executeQuery()) {
                                    requiredProductCount = rs.next() ? rs.getInt(1) : 0;
                                }
                            }

                            try (PreparedStatement stock = conn.prepareStatement(consumeComboSql)) {
                                stock.setInt(1, line.getQuantity());
                                stock.setInt(2, showtimeId);
                                stock.setInt(3, line.getItemId());
                                stock.setInt(4, line.getQuantity());
                                affected = stock.executeUpdate();
                            }

                            if (requiredProductCount <= 0
                                    || affected != requiredProductCount) {
                                conn.rollback();
                                return -1;
                            }
                        } else {
                            conn.rollback();
                            return -1;
                        }

                        insertFnb.setInt(1, bookingId);
                        insertFnb.setString(2, itemType);

                        if ("PRODUCT".equals(itemType)) {
                            insertFnb.setInt(3, line.getItemId());
                            insertFnb.setNull(4, java.sql.Types.INTEGER);
                        } else {
                            insertFnb.setNull(3, java.sql.Types.INTEGER);
                            insertFnb.setInt(4, line.getItemId());
                        }

                        insertFnb.setString(5, line.getName());
                        insertFnb.setInt(6, line.getQuantity());
                        insertFnb.setDouble(7, line.getUnitPrice());
                        insertFnb.setString(8, bookingFnbStatus);
                        insertFnb.addBatch();
                    }

                    insertFnb.executeBatch();
                }
            }
            // ===== F&B STAFF - SAVE WALK-IN F&B END =====

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
                ps.setDouble(6, finalTotal);
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
    // ===== F&B STAFF - WALK-IN TRANSACTION END =====

    /**
     * Counter sales must not be assigned to a staff account or a real customer.
     */
    private int getOrCreateWalkinCustomer(Connection conn) throws java.sql.SQLException {
        String findSql = "SELECT id FROM dbo.[USER] WHERE email = ?";
        try (PreparedStatement ps = conn.prepareStatement(findSql)) {
            ps.setString(1, WALKIN_CUSTOMER_EMAIL);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return rs.getInt(1);
                }
            }
        }

        String insertSql = "INSERT INTO dbo.[USER] (full_name, email, password_hash, google_id, phone, role, active, email_verified, created_at, last_update) "
                + "VALUES (?, ?, ?, ?, ?, 'CUSTOMER', 0, 1, GETDATE(), GETDATE())";
        try (PreparedStatement ps = conn.prepareStatement(insertSql, Statement.RETURN_GENERATED_KEYS)) {
            ps.setNString(1, "Khách vãng lai");
            ps.setString(2, WALKIN_CUSTOMER_EMAIL);
            ps.setString(3, "SYSTEM_WALKIN_ACCOUNT_DISABLED");
            ps.setString(4, "system_walkin_customer");
            ps.setString(5, "");
            ps.executeUpdate();
            try (ResultSet keys = ps.getGeneratedKeys()) {
                if (keys.next()) {
                    return keys.getInt(1);
                }
            }
        } catch (java.sql.SQLIntegrityConstraintViolationException duplicate) {
            try (PreparedStatement ps = conn.prepareStatement(findSql)) {
                ps.setString(1, WALKIN_CUSTOMER_EMAIL);
                try (ResultSet rs = ps.executeQuery()) {
                    return rs.next() ? rs.getInt(1) : -1;
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
                try {
                    conn.rollback();
                } catch (Exception re) {
                    re.printStackTrace();
                }
            }
        } finally {
            if (conn != null) {
                try {
                    conn.close();
                } catch (Exception ce) {
                    ce.printStackTrace();
                }
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
        try (Connection conn = new DBContext().getConnection(); PreparedStatement ps = conn.prepareStatement(sql)) {
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
        try (Connection conn = new DBContext().getConnection(); PreparedStatement ps = conn.prepareStatement(sql)) {
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
        String updateFnbSql = "UPDATE dbo.BOOKING_FNB "
                + "SET status = 'PREPARING', last_update = GETDATE() "
                + "WHERE booking_id = ? AND status = 'PENDING'";

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

            try (PreparedStatement ps = conn.prepareStatement(updateFnbSql)) {
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

    // getBookingStatus: Trả về trạng thái của booking
    public String getBookingStatus(int bookingId) {
        String sql = "SELECT status FROM dbo.BOOKINGS WHERE id = ?";
        try (Connection conn = new DBContext().getConnection(); PreparedStatement ps = conn.prepareStatement(sql)) {
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

    public String getBookingStatusInBranch(int bookingId, int branchId) {
        String sql = "SELECT b.status FROM dbo.BOOKINGS b "
                + "JOIN dbo.SHOWTIMES s ON s.id = b.showtime_id "
                + "JOIN dbo.HALLS h ON h.id = s.hall_id "
                + "WHERE b.id = ? AND h.branch_id = ?";
        try (Connection conn = new DBContext().getConnection(); PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, bookingId);
            ps.setInt(2, branchId);
            try (ResultSet rs = ps.executeQuery()) {
                return rs.next() ? rs.getString(1) : null;
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }

    // ===== F&B STAFF - CANCEL PENDING COUNTER BOOKING BEGIN =====
    public boolean cancelPendingBookingInBranch(int bookingId, int branchId) {
        String bookingSql = "UPDATE b SET status = 'CANCELLED', last_update = GETDATE() "
                + "FROM dbo.BOOKINGS b "
                + "JOIN dbo.SHOWTIMES s ON s.id = b.showtime_id "
                + "JOIN dbo.HALLS h ON h.id = s.hall_id "
                + "WHERE b.id = ? AND h.branch_id = ? AND b.status = 'PENDING'";

        String paymentSql = "UPDATE dbo.PAYMENTS "
                + "SET status = 'FAILED', last_update = GETDATE() "
                + "WHERE booking_id = ? AND status = 'PENDING'";

        String restoreProductsSql = "UPDATE inv "
                + "SET inv.stock_quantity = inv.stock_quantity + bf.quantity, "
                + "inv.last_update = GETDATE() "
                + "FROM dbo.BRANCH_FNB_INVENTORY inv "
                + "JOIN dbo.BOOKING_FNB bf ON bf.product_id = inv.product_id "
                + "JOIN dbo.BOOKINGS b ON b.id = bf.booking_id "
                + "JOIN dbo.SHOWTIMES st ON st.id = b.showtime_id "
                + "JOIN dbo.HALLS h ON h.id = st.hall_id "
                + "AND h.branch_id = inv.branch_id "
                + "WHERE bf.booking_id = ? "
                + "AND bf.item_type = 'PRODUCT' "
                + "AND bf.status <> 'CANCELLED'";

        String restoreCombosSql = "UPDATE inv "
                + "SET inv.stock_quantity = inv.stock_quantity "
                + "+ (ci.quantity * bf.quantity), "
                + "inv.last_update = GETDATE() "
                + "FROM dbo.BRANCH_FNB_INVENTORY inv "
                + "JOIN dbo.FNB_COMBO_ITEMS ci ON ci.product_id = inv.product_id "
                + "JOIN dbo.BOOKING_FNB bf ON bf.combo_id = ci.combo_id "
                + "JOIN dbo.BOOKINGS b ON b.id = bf.booking_id "
                + "JOIN dbo.SHOWTIMES st ON st.id = b.showtime_id "
                + "JOIN dbo.HALLS h ON h.id = st.hall_id "
                + "AND h.branch_id = inv.branch_id "
                + "WHERE bf.booking_id = ? "
                + "AND bf.item_type = 'COMBO' "
                + "AND bf.status <> 'CANCELLED'";

        String cancelFnbSql = "UPDATE dbo.BOOKING_FNB "
                + "SET status = 'CANCELLED', last_update = GETDATE() "
                + "WHERE booking_id = ? AND status <> 'CANCELLED'";

        Connection conn = null;

        try {
            conn = new DBContext().getConnection();
            conn.setAutoCommit(false);

            try (PreparedStatement ps = conn.prepareStatement(bookingSql)) {
                ps.setInt(1, bookingId);
                ps.setInt(2, branchId);

                if (ps.executeUpdate() != 1) {
                    conn.rollback();
                    return false;
                }
            }

            try (PreparedStatement ps = conn.prepareStatement(restoreProductsSql)) {
                ps.setInt(1, bookingId);
                ps.executeUpdate();
            }

            try (PreparedStatement ps = conn.prepareStatement(restoreCombosSql)) {
                ps.setInt(1, bookingId);
                ps.executeUpdate();
            }

            try (PreparedStatement ps = conn.prepareStatement(cancelFnbSql)) {
                ps.setInt(1, bookingId);
                ps.executeUpdate();
            }

            try (PreparedStatement ps = conn.prepareStatement(paymentSql)) {
                ps.setInt(1, bookingId);
                ps.executeUpdate();
            }

            conn.commit();
            return true;
        } catch (Exception e) {
            if (conn != null) {
                try {
                    conn.rollback();
                } catch (Exception ignored) {
                }
            }
            e.printStackTrace();
        } finally {
            if (conn != null) {
                try {
                    conn.close();
                } catch (Exception ignored) {
                }
            }
        }

        return false;
    }
    // ===== F&B STAFF - CANCEL PENDING COUNTER BOOKING END =====

    public int createPendingBooking(int userId, int showtimeId, List<Integer> seatIds,
            List<Double> seatPrices, double totalPrice,
            String voucherCode, double voucherDiscount, List<BookingFnbLine> fnbLines) {
        if (userId <= 0 || showtimeId <= 0 || seatIds == null || seatIds.isEmpty()
                || seatPrices == null || seatPrices.size() != seatIds.size()) {
            return -1;
        }

        String checkShowtimeSql = "SELECT COUNT(*) FROM dbo.SHOWTIMES "
                + "WHERE id = ? AND status IN ('SCHEDULED','ON_SALE') "
                + "AND DATEADD(MINUTE, 30, start_time) > GETDATE()";

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
        String insertFnbSql = "INSERT INTO dbo.BOOKING_FNB "
                + "(booking_id,item_type,product_id,combo_id,item_name,quantity,unit_price,status,last_update) "
                + "VALUES (?,?,?,?,?,?,?,'PENDING',GETDATE())";
        String consumeProductSql = "UPDATE dbo.BRANCH_FNB_INVENTORY SET stock_quantity=stock_quantity-?,last_update=GETDATE() "
                + "WHERE branch_id=(SELECT h.branch_id FROM SHOWTIMES st JOIN HALLS h ON h.id=st.hall_id WHERE st.id=?) "
                + "AND product_id=? AND enabled_at_branch=1 AND stock_quantity>=?";
        String consumeComboSql = "UPDATE inv SET inv.stock_quantity=inv.stock_quantity-(ci.quantity*?),inv.last_update=GETDATE() "
                + "FROM dbo.BRANCH_FNB_INVENTORY inv JOIN dbo.FNB_COMBO_ITEMS ci ON ci.product_id=inv.product_id "
                + "JOIN dbo.HALLS h ON h.branch_id=inv.branch_id JOIN dbo.SHOWTIMES st ON st.hall_id=h.id "
                + "WHERE st.id=? AND ci.combo_id=? AND inv.enabled_at_branch=1 "
                + "AND inv.stock_quantity>=ci.quantity*?";

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
                double fnbTotal = fnbLines == null ? 0 : fnbLines.stream().mapToDouble(BookingFnbLine::getLineTotal).sum();
                ps.setDouble(3, totalPrice + fnbTotal);
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

            if (fnbLines != null && !fnbLines.isEmpty()) {
                try (PreparedStatement insert = conn.prepareStatement(insertFnbSql)) {
                    for (BookingFnbLine line : fnbLines) {
                        int affected;
                        if ("PRODUCT".equals(line.getItemType())) {
                            try (PreparedStatement stock = conn.prepareStatement(consumeProductSql)) {
                                stock.setInt(1, line.getQuantity());
                                stock.setInt(2, showtimeId);
                                stock.setInt(3, line.getItemId());
                                stock.setInt(4, line.getQuantity());
                                affected = stock.executeUpdate();
                            }
                            if (affected != 1) {
                                conn.rollback();
                                return -1;
                            }
                        } else if ("COMBO".equals(line.getItemType())) {
                            int required;
                            try (PreparedStatement count = conn.prepareStatement("SELECT COUNT(*) FROM FNB_COMBO_ITEMS WHERE combo_id=?")) {
                                count.setInt(1, line.getItemId());
                                try (ResultSet rs = count.executeQuery()) {
                                    rs.next();
                                    required = rs.getInt(1);
                                }
                            }
                            try (PreparedStatement stock = conn.prepareStatement(consumeComboSql)) {
                                stock.setInt(1, line.getQuantity());
                                stock.setInt(2, showtimeId);
                                stock.setInt(3, line.getItemId());
                                stock.setInt(4, line.getQuantity());
                                affected = stock.executeUpdate();
                            }
                            if (required == 0 || affected != required) {
                                conn.rollback();
                                return -1;
                            }
                        } else {
                            conn.rollback();
                            return -1;
                        }

                        insert.setInt(1, bookingId);
                        insert.setString(2, line.getItemType());
                        if ("PRODUCT".equals(line.getItemType())) {
                            insert.setInt(3, line.getItemId());
                            insert.setNull(4, java.sql.Types.INTEGER);
                        } else {
                            insert.setNull(3, java.sql.Types.INTEGER);
                            insert.setInt(4, line.getItemId());
                        }
                        insert.setString(5, line.getName());
                        insert.setInt(6, line.getQuantity());
                        insert.setDouble(7, line.getUnitPrice());
                        insert.addBatch();
                    }
                    insert.executeBatch();
                }
            }

            try (PreparedStatement ps = conn.prepareStatement(insertPaymentSql)) {
                ps.setInt(1, bookingId);
                ps.setString(2, "RVS" + bookingId);
                double fnbTotal = fnbLines == null ? 0 : fnbLines.stream().mapToDouble(BookingFnbLine::getLineTotal).sum();
                ps.setDouble(3, totalPrice + fnbTotal);
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

        try (Connection conn = new DBContext().getConnection(); PreparedStatement ps = conn.prepareStatement(sql)) {
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

        try (Connection conn = new DBContext().getConnection(); PreparedStatement ps = conn.prepareStatement(sql)) {
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
        String restoreProductsSql = "UPDATE inv SET inv.stock_quantity=inv.stock_quantity+bf.quantity,inv.last_update=GETDATE() "
                + "FROM BRANCH_FNB_INVENTORY inv JOIN BOOKING_FNB bf ON bf.product_id=inv.product_id "
                + "JOIN BOOKINGS b ON b.id=bf.booking_id JOIN SHOWTIMES st ON st.id=b.showtime_id "
                + "JOIN HALLS h ON h.id=st.hall_id AND h.branch_id=inv.branch_id "
                + "WHERE bf.booking_id=? AND bf.item_type='PRODUCT' AND bf.status<>'CANCELLED'";
        String restoreCombosSql = "UPDATE inv SET inv.stock_quantity=inv.stock_quantity+(ci.quantity*bf.quantity),inv.last_update=GETDATE() "
                + "FROM BRANCH_FNB_INVENTORY inv JOIN FNB_COMBO_ITEMS ci ON ci.product_id=inv.product_id "
                + "JOIN BOOKING_FNB bf ON bf.combo_id=ci.combo_id JOIN BOOKINGS b ON b.id=bf.booking_id "
                + "JOIN SHOWTIMES st ON st.id=b.showtime_id JOIN HALLS h ON h.id=st.hall_id AND h.branch_id=inv.branch_id "
                + "WHERE bf.booking_id=? AND bf.item_type='COMBO' AND bf.status<>'CANCELLED'";
        String cancelFnbSql = "UPDATE BOOKING_FNB SET status='CANCELLED',last_update=GETDATE() WHERE booking_id=? AND status<>'CANCELLED'";

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

            try (PreparedStatement ps = conn.prepareStatement(restoreProductsSql)) {
                ps.setInt(1, bookingId);
                ps.executeUpdate();
            }
            try (PreparedStatement ps = conn.prepareStatement(restoreCombosSql)) {
                ps.setInt(1, bookingId);
                ps.executeUpdate();
            }
            try (PreparedStatement ps = conn.prepareStatement(cancelFnbSql)) {
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

        try (Connection conn = new DBContext().getConnection(); PreparedStatement ps = conn.prepareStatement(sql)) {
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

        try (Connection conn = new DBContext().getConnection(); PreparedStatement ps = conn.prepareStatement(sql)) {
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
        try (Connection conn = new DBContext().getConnection(); PreparedStatement ps = conn.prepareStatement(sql)) {
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
                //+ "COUNT(se.id) AS seat_count "
                + "COUNT(se.id) AS seat_count, "
                + "COALESCE(SUM(bs.price), 0) AS seat_subtotal "
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
        double seatSubtotal = rs.getDouble("seat_subtotal");
        double finalTotal = booking.getTotalPrice();

        /*
 * Online booking hiện chưa có voucher.
 * Vì vậy phần chênh lệch giữa tổng giá ghế và tổng cuối
 * chính là ưu đãi mua 5 tặng 1.
         */
        double buyFiveDiscount = Math.max(0, seatSubtotal - finalTotal);
        double voucherDiscount = 0;

        view.setSeatSubtotal(seatSubtotal);
        view.setBuyFiveDiscount(buyFiveDiscount);
        view.setVoucherDiscount(voucherDiscount);
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

        try (Connection conn = new DBContext().getConnection(); PreparedStatement ps = conn.prepareStatement(sql)) {

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

        try (Connection conn = new DBContext().getConnection(); PreparedStatement ps = conn.prepareStatement(sql)) {

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

    /**
     * Query một trang lịch sử booking cho màn hình "Vé của tôi".
     *
     * Dữ liệu đi từ BookingService xuống các tham số userId/status/fromDate/toDate/page/pageSize.
     * bookingViewSelect() join BOOKINGS với SHOWTIMES, MOVIES, BRANCHES, HALLS và BOOKING_SEATS
     * để mỗi BookingView đã có đủ thông tin cho một dòng JSP; JSP không phải gọi thêm DAO.
     * Tất cả filter đều bind qua PreparedStatement, không nối dữ liệu request vào SQL nên tránh SQL Injection.
     */
    public List<BookingView> findHistoryByUserPaging(int userId, String status,
            LocalDate fromDate, LocalDate toDate, int page, int pageSize) {
        List<BookingView> list = new ArrayList<>();
        StringBuilder sql = new StringBuilder(bookingViewSelect())
                .append("WHERE bk.user_id = ? ");
        appendHistoryFilters(sql, status, fromDate, toDate);
        sql.append("GROUP BY bk.id, bk.user_id, bk.showtime_id, bk.source, bk.status, bk.total_price, ")
                .append("bk.qr_code, bk.booked_at, m.id, m.title, br.name, h.name, s.start_time, u.full_name, u.email ")
                .append("ORDER BY bk.booked_at DESC, bk.id DESC ")
                .append("OFFSET ? ROWS FETCH NEXT ? ROWS ONLY");

        try (Connection conn = new DBContext().getConnection();
                PreparedStatement ps = conn.prepareStatement(sql.toString())) {
            int index = 1;
            ps.setInt(index++, userId);
            index = bindHistoryFilters(ps, index, status, fromDate, toDate);
            ps.setInt(index++, (page - 1) * pageSize);
            ps.setInt(index, pageSize);
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

    /**
     * Đếm booking bằng chính điều kiện filter của query lấy trang. Nhờ vậy Controller tính
     * totalPages chính xác, không xuất hiện tình huống bảng có 2 dòng nhưng lại báo 3 trang.
     */
    public int countHistoryByUser(int userId, String status, LocalDate fromDate, LocalDate toDate) {
        StringBuilder sql = new StringBuilder("SELECT COUNT(*) FROM dbo.BOOKINGS bk WHERE bk.user_id = ? ");
        appendHistoryFilters(sql, status, fromDate, toDate);
        try (Connection conn = new DBContext().getConnection();
                PreparedStatement ps = conn.prepareStatement(sql.toString())) {
            int index = 1;
            ps.setInt(index++, userId);
            bindHistoryFilters(ps, index, status, fromDate, toDate);
            try (ResultSet rs = ps.executeQuery()) {
                return rs.next() ? rs.getInt(1) : 0;
            }
        } catch (Exception e) {
            e.printStackTrace();
            return 0;
        }
    }

    /**
     * Đếm số đơn theo trạng thái chỉ bằng một câu GROUP BY cho các tab lọc nhanh của JSP.
     * Map được khởi tạo sẵn đủ 5 trạng thái với giá trị 0, vì vậy giao diện vẫn hiển thị tab
     * ổn định ngay cả khi Customer chưa có booking ở một trạng thái nào đó.
     */
    public Map<String, Integer> countHistoryByUserStatus(int userId, LocalDate fromDate, LocalDate toDate) {
        Map<String, Integer> counts = new LinkedHashMap<>();
        String[] statuses = {"PENDING", "CONFIRMED", "CHECKED_IN", "USED", "CANCELLED"};
        for (String status : statuses) {
            counts.put(status, 0);
        }

        StringBuilder sql = new StringBuilder("SELECT bk.status, COUNT(*) AS total ")
                .append("FROM dbo.BOOKINGS bk WHERE bk.user_id = ? ");
        appendHistoryFilters(sql, null, fromDate, toDate);
        sql.append("GROUP BY bk.status");
        try (Connection conn = new DBContext().getConnection();
                PreparedStatement ps = conn.prepareStatement(sql.toString())) {
            int index = 1;
            ps.setInt(index++, userId);
            bindHistoryFilters(ps, index, null, fromDate, toDate);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    counts.put(rs.getString("status"), rs.getInt("total"));
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return counts;
    }

    /**
     * Nối các điều kiện filter hợp lệ vào SQL. status đã được BookingService chuẩn hóa.
     * Ngày kết thúc dùng mốc nhỏ hơn ngày kế tiếp (DATEADD day, 1) để lấy đủ mọi giờ trong ngày đó,
     * ví dụ toDate=2026-07-17 vẫn gồm booking lúc 23:59 ngày 17/07.
     */
    private void appendHistoryFilters(StringBuilder sql, String status, LocalDate fromDate, LocalDate toDate) {
        if (status != null) {
            sql.append("AND bk.status = ? ");
        }
        if (fromDate != null) {
            sql.append("AND bk.booked_at >= ? ");
        }
        if (toDate != null) {
            // Dùng cận trên độc quyền của ngày kế tiếp để không mất booking có phần giờ/phút/giây.
            sql.append("AND bk.booked_at < DATEADD(day, 1, ?) ");
        }
    }

    /**
     * Bind giá trị PreparedStatement đúng thứ tự mà appendHistoryFilters đã thêm dấu ?:
     * status -> fromDate -> toDate. Nếu đổi thứ tự bind, câu SQL sẽ lọc sai dữ liệu hoặc ném SQLException.
     */
    private int bindHistoryFilters(PreparedStatement ps, int index, String status,
            LocalDate fromDate, LocalDate toDate) throws Exception {
        if (status != null) {
            ps.setString(index++, status);
        }
        if (fromDate != null) {
            ps.setDate(index++, Date.valueOf(fromDate));
        }
        if (toDate != null) {
            ps.setDate(index++, Date.valueOf(toDate));
        }
        return index;
    }
}
