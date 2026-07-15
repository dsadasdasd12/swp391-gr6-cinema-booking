/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package dao;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Time;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.List;
import model.Branch;
import util.DBContext;

public class StaffBranchDAO {

    public List<Branch> findBranchesByUserId(int userId) {
        String sql = "SELECT b.id, b.cinema_id, b.name, b.address, b.phone, "
                + "b.open_time, b.close_time, b.status, b.last_update "
                + "FROM dbo.STAFF_BRANCH sb "
                + "JOIN dbo.BRANCHES b ON sb.branch_id = b.id "
                + "WHERE sb.user_id = ? "
                + "ORDER BY b.name ASC";

        List<Branch> branches = new ArrayList<>();
        Connection conn = DBContext.getInstance().getConnection();

        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, userId);

            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    branches.add(mapBranch(rs));
                }
            }

        } catch (SQLException e) {
            e.printStackTrace();
        }

        return branches;
    }

    /* Lay tu staffBranch*/
    public Branch findBranchByManagerId(int managerId) {
        String sql = "SELECT TOP 1 "
                + "b.id, b.cinema_id, b.name, b.address, b.phone, "
                + "b.open_time, b.close_time, b.status, b.last_update "
                + "FROM dbo.STAFF_BRANCH sb "
                + "JOIN dbo.[USER] u ON u.id = sb.user_id "
                + "JOIN dbo.BRANCHES b ON b.id = sb.branch_id "
                + "WHERE sb.user_id = ? "
                + "AND u.role = 'MANAGER' "
                + "ORDER BY sb.assigned_at ASC, b.id ASC";

        Connection conn = DBContext.getInstance().getConnection();

        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, managerId);

            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return mapBranch(rs);
                }
            }

        } catch (SQLException e) {
            e.printStackTrace();
        }

        return null;
    }

    /*Kiem tra BranchID từ client co phan cong cho Manager*/
    public boolean isManagerAssignedToBranch(int userId, int branchId) {
        String sql = "SELECT COUNT(*) "
                + "FROM dbo.STAFF_BRANCH "
                + "WHERE user_id = ? AND branch_id = ?";

        Connection conn = DBContext.getInstance().getConnection();

        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, userId);
            ps.setInt(2, branchId);

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

    private Branch mapBranch(ResultSet rs) throws SQLException {
        Branch branch = new Branch();

        branch.setId(rs.getInt("id"));
        branch.setCinemaId(rs.getInt("cinema_id"));
        branch.setName(rs.getString("name"));
        branch.setAddress(rs.getString("address"));
        branch.setPhone(rs.getString("phone"));
        branch.setOpenTime(toLocalTime(rs.getTime("open_time")));
        branch.setCloseTime(toLocalTime(rs.getTime("close_time")));
        branch.setStatus(rs.getString("status"));
        branch.setLastUpdate(rs.getObject("last_update", LocalDateTime.class));

        return branch;
    }

    private LocalTime toLocalTime(Time time) {
        if (time == null) {
            return null;
        }

        return time.toLocalTime();
    }
}