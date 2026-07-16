package dao;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import model.SeatType;
import util.DBContext;
import util.EncodingUtil;

public class SeatTypeDAO {

    private String normalizeCode(String code) {
        if (code == null) return "";
        return code.trim().toUpperCase();
    }

    private String normalizeStatus(String status) {
        if (status == null) return "ACTIVE";
        return status.trim().toUpperCase();
    }

    public List<SeatType> findAll() {
        String sql = "SELECT id, code, name, default_price, color, status, last_update FROM dbo.SEAT_TYPES ORDER BY id";
        List<SeatType> list = new ArrayList<>();
        Connection conn = DBContext.getInstance().getConnection();
        try (PreparedStatement ps = conn.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {
            while (rs.next()) {
                list.add(map(rs));
            }
        } catch (SQLException e) {
            System.getLogger(SeatTypeDAO.class.getName()).log(System.Logger.Level.ERROR, "findAll failed", e);
        }
        return list;
    }

    public List<SeatType> findAllActive() {
        String sql = "SELECT id, code, name, default_price, color, status, last_update FROM dbo.SEAT_TYPES WHERE status = 'ACTIVE' ORDER BY id";
        List<SeatType> list = new ArrayList<>();
        Connection conn = DBContext.getInstance().getConnection();
        try (PreparedStatement ps = conn.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {
            while (rs.next()) {
                list.add(map(rs));
            }
        } catch (SQLException e) {
            System.getLogger(SeatTypeDAO.class.getName()).log(System.Logger.Level.ERROR, "findAllActive failed", e);
        }
        return list;
    }

    public SeatType findById(int id) {
        String sql = "SELECT id, code, name, default_price, color, status, last_update FROM dbo.SEAT_TYPES WHERE id = ?";
        Connection conn = DBContext.getInstance().getConnection();
        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, id);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return map(rs);
                }
            }
        } catch (SQLException e) {
            System.getLogger(SeatTypeDAO.class.getName()).log(System.Logger.Level.ERROR, "findById failed", e);
        }
        return null;
    }

    public SeatType findByCode(String code) {
        String sql = "SELECT id, code, name, default_price, color, status, last_update "
                + "FROM dbo.SEAT_TYPES WHERE code = ?";
        Connection conn = DBContext.getInstance().getConnection();
        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, normalizeCode(code));
            try (ResultSet rs = ps.executeQuery()) {
                return rs.next() ? map(rs) : null;
            }
        } catch (SQLException e) {
            System.getLogger(SeatTypeDAO.class.getName()).log(System.Logger.Level.ERROR, "findByCode failed", e);
        }
        return null;
    }

    public boolean insert(SeatType st) {
        String sql = "INSERT INTO dbo.SEAT_TYPES (code, name, default_price, color, status) VALUES (?, ?, ?, ?, ?)";
        Connection conn = DBContext.getInstance().getConnection();
        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, normalizeCode(st.getCode()));
            ps.setString(2, st.getName());
            ps.setDouble(3, st.getDefaultPrice());
            ps.setString(4, st.getColor() != null ? st.getColor() : "#10b981");
            ps.setString(5, normalizeStatus(st.getStatus()));
            return ps.executeUpdate() > 0;
        } catch (SQLException e) {
            System.getLogger(SeatTypeDAO.class.getName()).log(System.Logger.Level.ERROR, "insert failed", e);
        }
        return false;
    }

    public boolean update(SeatType st) {
        String sql = "UPDATE dbo.SEAT_TYPES SET code = ?, name = ?, default_price = ?, color = ?, status = ?, last_update = GETDATE() WHERE id = ?";
        Connection conn = DBContext.getInstance().getConnection();
        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, normalizeCode(st.getCode()));
            ps.setString(2, st.getName());
            ps.setDouble(3, st.getDefaultPrice());
            ps.setString(4, st.getColor() != null ? st.getColor() : "#10b981");
            ps.setString(5, normalizeStatus(st.getStatus()));
            ps.setInt(6, st.getId());
            return ps.executeUpdate() > 0;
        } catch (SQLException e) {
            System.getLogger(SeatTypeDAO.class.getName()).log(System.Logger.Level.ERROR, "update failed", e);
        }
        return false;
    }

    public boolean delete(int id) {
        // Thử hard-delete trước (xóa hoàn toàn khỏi database nếu chưa được sử dụng)
        String sql = "UPDATE dbo.SEAT_TYPES SET status = 'INACTIVE', last_update = GETDATE() WHERE id = ?";
        Connection conn = DBContext.getInstance().getConnection();
        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, id);
            return ps.executeUpdate() > 0;
        } catch (SQLException e) {
            // Nếu có lỗi khóa ngoại (do đang có ghế hoặc lịch sử tham chiếu loại ghế này), chuyển sang soft-delete (Khóa trạng thái)
            String fallbackSql = "UPDATE dbo.SEAT_TYPES SET status = 'INACTIVE', last_update = GETDATE() WHERE id = ?";
            try (PreparedStatement psFallback = conn.prepareStatement(fallbackSql)) {
                psFallback.setInt(1, id);
                return psFallback.executeUpdate() > 0;
            } catch (SQLException ex) {
                System.getLogger(SeatTypeDAO.class.getName()).log(System.Logger.Level.ERROR, "delete failed", ex);
            }
        }
        return false;
    }

    private SeatType map(ResultSet rs) throws SQLException {
        SeatType st = new SeatType();
        st.setId(rs.getInt("id"));
        st.setCode(rs.getString("code"));
        st.setName(EncodingUtil.getString(rs, "name"));
        st.setDefaultPrice(rs.getDouble("default_price"));
        st.setColor(rs.getString("color"));
        st.setStatus(rs.getString("status"));
        
        java.sql.Timestamp ts = rs.getTimestamp("last_update");
        if (ts != null) {
            st.setLastUpdate(ts.toLocalDateTime());
        }
        
        return st;
    }
}
