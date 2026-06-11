/*
 * Hệ thống Quản lý Rạp chiếu phim RapViet
 * Module: Xem chi nhánh rạp - truy xuất bảng dbo.BRANCHES
 */
package dao;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.List;
import model.Branch;
import util.DBContext;

/**
 * DAO cho chi nhánh rạp. Mỗi chi nhánh được join với rạp (CINEMA) để lấy tên
 * thương hiệu và đếm sẵn số phòng chiếu đang hoạt động, phục vụ trang danh sách
 * chi nhánh và làm dữ liệu cho ô chọn chi nhánh ở trang suất chiếu.
 *
 * Lưu ý: DBContext dùng chung một Connection (singleton) nên ở đây chỉ đóng
 * PreparedStatement và ResultSet, KHÔNG đóng Connection.
 *
 * @author Group6 - Huy (Module Duyệt phim)
 */
public class BranchDAO {

    /**
     * Tất cả chi nhánh đang hoạt động, kèm tên rạp và số phòng chiếu đang hoạt
     * động, sắp theo tên chi nhánh. Dùng subquery COUNT để tránh phải nhóm
     * (GROUP BY) toàn bộ các cột của BRANCHES.
     */
    public List<Branch> findAllActive() {
        String sql = "SELECT b.id, b.cinema_id, b.name, b.address, b.phone, "
                + "       b.open_time, b.close_time, b.status, "
                + "       c.name AS cinema_name, "
                + "       (SELECT COUNT(*) FROM dbo.HALLS h "
                + "          WHERE h.branch_id = b.id AND h.status = 'ACTIVE') AS hall_count "
                + "FROM dbo.BRANCHES b "
                + "JOIN dbo.CINEMA c ON c.id = b.cinema_id "
                + "WHERE b.status = 'ACTIVE' "
                + "ORDER BY b.name";
        List<Branch> list = new ArrayList<>();
        Connection conn = DBContext.getInstance().getConnection();
        try (PreparedStatement ps = conn.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {
            while (rs.next()) {
                list.add(map(rs));
            }
        } catch (SQLException e) {
            System.getLogger(BranchDAO.class.getName())
                    .log(System.Logger.Level.ERROR, "findAllActive thất bại", e);
        }
        return list;
    }

    /** Một chi nhánh theo id (kèm tên rạp + số phòng), hoặc null nếu không có. */
    public Branch findById(int branchId) {
        String sql = "SELECT b.id, b.cinema_id, b.name, b.address, b.phone, "
                + "       b.open_time, b.close_time, b.status, "
                + "       c.name AS cinema_name, "
                + "       (SELECT COUNT(*) FROM dbo.HALLS h "
                + "          WHERE h.branch_id = b.id AND h.status = 'ACTIVE') AS hall_count "
                + "FROM dbo.BRANCHES b "
                + "JOIN dbo.CINEMA c ON c.id = b.cinema_id "
                + "WHERE b.id = ?";
        Connection conn = DBContext.getInstance().getConnection();
        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, branchId);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return map(rs);
                }
            }
        } catch (SQLException e) {
            System.getLogger(BranchDAO.class.getName())
                    .log(System.Logger.Level.ERROR, "findById thất bại", e);
        }
        return null;
    }

    /** Ánh xạ một dòng ResultSet sang đối tượng Branch. */
    private Branch map(ResultSet rs) throws SQLException {
        Branch b = new Branch();
        b.setId(rs.getInt("id"));
        b.setCinemaId(rs.getInt("cinema_id"));
        b.setName(rs.getString("name"));
        b.setAddress(rs.getString("address"));
        b.setPhone(rs.getString("phone"));
        b.setOpenTime(rs.getObject("open_time", LocalTime.class));
        b.setCloseTime(rs.getObject("close_time", LocalTime.class));
        b.setStatus(rs.getString("status"));
        b.setCinemaName(rs.getString("cinema_name"));
        b.setHallCount(rs.getInt("hall_count"));
        return b;
    }
}
