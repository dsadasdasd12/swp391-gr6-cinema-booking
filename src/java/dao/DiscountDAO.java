package dao;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.List;
import model.DiscountCode;
import util.DBContext;

public class DiscountDAO {

    public DiscountCode findByCode(String code) {
        if (code == null || code.trim().isEmpty()) {
            return null;
        }
        String sql = "SELECT * FROM dbo.DISCOUNT_CODES WHERE code = ?";
        try (Connection conn = new DBContext().getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, code.trim().toUpperCase());
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    Double maxDiscount = rs.getObject("max_discount_amount") != null
                            ? rs.getDouble("max_discount_amount") : null;
                    return new DiscountCode(rs.getInt("id"), rs.getString("code"),
                            rs.getString("discount_type"), rs.getDouble("discount_value"),
                            maxDiscount, rs.getDouble("min_order_value"), rs.getInt("max_uses"),
                            rs.getInt("used_count"), rs.getTimestamp("start_date"),
                            rs.getTimestamp("end_date"), rs.getString("status"));
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }

    // 1. READ: Lấy toàn bộ mã giảm giá
    public List<DiscountCode> getAllDiscountCodes() {
        List<DiscountCode> list = new ArrayList<>();
        String sql = "SELECT * FROM dbo.DISCOUNT_CODES ORDER BY id DESC";
        try (Connection conn = new DBContext().getConnection();
             PreparedStatement ps = conn.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {
            while (rs.next()) {
                Double maxDiscount = rs.getObject("max_discount_amount") != null ? rs.getDouble("max_discount_amount") : null;
                list.add(new DiscountCode(
                    rs.getInt("id"),
                    rs.getString("code"),
                    rs.getString("discount_type"),
                    rs.getDouble("discount_value"),
                    maxDiscount,
                    rs.getDouble("min_order_value"),
                    rs.getInt("max_uses"),
                    rs.getInt("used_count"),
                    rs.getTimestamp("start_date"),
                    rs.getTimestamp("end_date"),
                    rs.getString("status")
                ));
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return list;
    }

    // 2. WRITE: Tạo mã giảm giá mới
    public boolean createDiscountCode(DiscountCode code) {
        String sql = "INSERT INTO dbo.DISCOUNT_CODES (code, discount_type, discount_value, max_discount_amount, "
                   + "                               min_order_value, max_uses, used_count, start_date, end_date, status) "
                   + "VALUES (?, ?, ?, ?, ?, ?, 0, ?, ?, ?)";
        try (Connection conn = new DBContext().getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, code.getCode().toUpperCase().trim());
            ps.setString(2, code.getDiscountType());
            ps.setDouble(3, code.getDiscountValue());
            if (code.getMaxDiscountAmount() != null) {
                ps.setDouble(4, code.getMaxDiscountAmount());
            } else {
                ps.setNull(4, java.sql.Types.DECIMAL);
            }
            ps.setDouble(5, code.getMinOrderValue());
            ps.setInt(6, code.getMaxUses());
            ps.setTimestamp(7, code.getStartDate());
            ps.setTimestamp(8, code.getEndDate());
            ps.setString(9, code.getStatus());
            return ps.executeUpdate() > 0;
        } catch (Exception e) {
            e.printStackTrace();
        }
        return false;
    }

    // 3. WRITE: Cập nhật trạng thái hoạt động
    public boolean updateDiscountCodeStatus(int id, String status) {
        String sql = "UPDATE dbo.DISCOUNT_CODES SET status = ?, last_update = GETDATE() WHERE id = ?";
        try (Connection conn = new DBContext().getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, status);
            ps.setInt(2, id);
            return ps.executeUpdate() > 0;
        } catch (Exception e) {
            e.printStackTrace();
        }
        return false;
    }

    // 4. WRITE: Xóa mã giảm giá
    public boolean deleteDiscountCode(int id) {
        String sql = "UPDATE dbo.DISCOUNT_CODES SET status = 'PAUSED', last_update = GETDATE() WHERE id = ?";
        try (Connection conn = new DBContext().getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, id);
            return ps.executeUpdate() > 0;
        } catch (Exception e) {
            e.printStackTrace();
        }
        return false;
    }

    // 5. WRITE: Tăng số lần sử dụng của mã giảm giá
    public boolean incrementUsedCount(String code) {
        String sql = "UPDATE dbo.DISCOUNT_CODES SET used_count = used_count + 1, last_update = GETDATE() WHERE code = ?";
        try (Connection conn = new DBContext().getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, code.toUpperCase().trim());
            return ps.executeUpdate() > 0;
        } catch (Exception e) {
            e.printStackTrace();
        }
        return false;
    }
}
