/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package dao;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import model.Hall;
import util.DBContext;

public class HallDAO {

    public List<Hall> findByBranchId(int branchId) {
        String sql = "SELECT h.id, h.branch_id, b.name AS branch_name, "
                + "h.name, h.total_seats, h.hall_type, h.status, h.last_update "
                + "FROM dbo.HALLS h "
                + "JOIN dbo.BRANCHES b ON h.branch_id = b.id "
                + "WHERE h.branch_id = ? "
                + "ORDER BY h.last_update DESC, h.id DESC";

        List<Hall> halls = new ArrayList<>();
        Connection conn = DBContext.getInstance().getConnection();

        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, branchId);

            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    halls.add(mapRow(rs));
                }
            }

        } catch (SQLException e) {
            e.printStackTrace();
        }

        return halls;
    }

    public Hall findByIdAndBranchId(int id, int branchId) {
        String sql = "SELECT h.id, h.branch_id, b.name AS branch_name, "
                + "h.name, h.total_seats, h.hall_type, h.status, h.last_update "
                + "FROM dbo.HALLS h "
                + "JOIN dbo.BRANCHES b ON h.branch_id = b.id "
                + "WHERE h.id = ? AND h.branch_id = ?";

        Connection conn = DBContext.getInstance().getConnection();

        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, id);
            ps.setInt(2, branchId);

            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return mapRow(rs);
                }
            }

        } catch (SQLException e) {
            e.printStackTrace();
        }

        return null;
    }

    public boolean insert(Hall hall) {
        String sql = "INSERT INTO dbo.HALLS "
                + "(branch_id, name, total_seats, hall_type, status) "
                + "VALUES (?, ?, ?, ?, ?)";

        Connection conn = DBContext.getInstance().getConnection();

        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, hall.getBranchId());
            ps.setString(2, hall.getName());
            ps.setInt(3, hall.getTotalSeats());
            ps.setString(4, hall.getHallType());
            ps.setString(5, hall.getStatus());

            return ps.executeUpdate() > 0;

        } catch (SQLException e) {
            e.printStackTrace();
        }

        return false;
    }

    public boolean update(Hall hall) {
        String sql = "UPDATE dbo.HALLS "
                + "SET name = ?, total_seats = ?, hall_type = ?, "
                + "status = ?, last_update = GETDATE() "
                + "WHERE id = ? AND branch_id = ?";

        Connection conn = DBContext.getInstance().getConnection();

        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, hall.getName());
            ps.setInt(2, hall.getTotalSeats());
            ps.setString(3, hall.getHallType());
            ps.setString(4, hall.getStatus());
            ps.setInt(5, hall.getId());
            ps.setInt(6, hall.getBranchId());

            return ps.executeUpdate() > 0;

        } catch (SQLException e) {
            e.printStackTrace();
        }

        return false;
    }

    public boolean delete(int id, int branchId) {
        String sql = "DELETE FROM dbo.HALLS "
                + "WHERE id = ? AND branch_id = ?";

        Connection conn = DBContext.getInstance().getConnection();

        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, id);
            ps.setInt(2, branchId);

            return ps.executeUpdate() > 0;

        } catch (SQLException e) {
            e.printStackTrace();
        }

        return false;
    }

    public boolean updateStatus(int id, int branchId, String status) {
        String sql = "UPDATE dbo.HALLS "
                + "SET status = ?, last_update = GETDATE() "
                + "WHERE id = ? AND branch_id = ?";

        Connection conn = DBContext.getInstance().getConnection();

        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, status);
            ps.setInt(2, id);
            ps.setInt(3, branchId);

            return ps.executeUpdate() > 0;

        } catch (SQLException e) {
            e.printStackTrace();
        }

        return false;
    }

    public boolean existsByNameAndBranchId(String name, int branchId) {
        String sql = "SELECT COUNT(*) "
                + "FROM dbo.HALLS "
                + "WHERE branch_id = ? "
                + "AND LOWER(LTRIM(RTRIM(name))) = LOWER(LTRIM(RTRIM(?)))";

        Connection conn = DBContext.getInstance().getConnection();

        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, branchId);
            ps.setString(2, name);

            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return rs.getInt(1) > 0;
                }
            }

        } catch (SQLException e) {
            e.printStackTrace();
        }

        return false;
    }

    public boolean existsByNameAndBranchIdExceptId(String name, int branchId, int id) {
        String sql = "SELECT COUNT(*) "
                + "FROM dbo.HALLS "
                + "WHERE branch_id = ? "
                + "AND LOWER(LTRIM(RTRIM(name))) = LOWER(LTRIM(RTRIM(?))) "
                + "AND id <> ?";

        Connection conn = DBContext.getInstance().getConnection();

        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, branchId);
            ps.setString(2, name);
            ps.setInt(3, id);

            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return rs.getInt(1) > 0;
                }
            }

        } catch (SQLException e) {
            e.printStackTrace();
        }

        return false;
    }

    private Hall mapRow(ResultSet rs) throws SQLException {
        Hall hall = new Hall();

        hall.setId(rs.getInt("id"));
        hall.setBranchId(rs.getInt("branch_id"));
        hall.setBranchName(rs.getString("branch_name"));
        hall.setName(rs.getString("name"));
        hall.setTotalSeats(rs.getInt("total_seats"));
        hall.setHallType(rs.getString("hall_type"));
        hall.setStatus(rs.getString("status"));
        hall.setLastUpdate(rs.getObject("last_update", LocalDateTime.class));

        return hall;
    }
}