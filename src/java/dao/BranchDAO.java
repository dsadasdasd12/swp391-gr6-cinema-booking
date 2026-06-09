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

public class BranchDAO {

    public List<Branch> findAll() {
        String sql = "SELECT id, cinema_id, name, address, phone, open_time, close_time, status, last_update "
                + "FROM dbo.BRANCHES "
                + "ORDER BY last_update DESC, id DESC";

        List<Branch> branches = new ArrayList<>();
        Connection conn = DBContext.getInstance().getConnection();

        try (PreparedStatement ps = conn.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {

            while (rs.next()) {
                branches.add(mapRow(rs));
            }

        } catch (SQLException e) {
            e.printStackTrace();
        }

        return branches;
    }

    public Branch findById(int id) {
        String sql = "SELECT id, cinema_id, name, address, phone, open_time, close_time, status, last_update "
                + "FROM dbo.BRANCHES "
                + "WHERE id = ?";

        Connection conn = DBContext.getInstance().getConnection();

        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, id);

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

    public boolean insert(Branch branch) {
        String sql = "INSERT INTO dbo.BRANCHES "
                + "(cinema_id, name, address, phone, open_time, close_time, status) "
                + "VALUES (?, ?, ?, ?, ?, ?, ?)";

        Connection conn = DBContext.getInstance().getConnection();

        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, branch.getCinemaId());
            ps.setString(2, branch.getName());
            ps.setString(3, branch.getAddress());
            ps.setString(4, branch.getPhone());
            ps.setTime(5, toSqlTime(branch.getOpenTime()));
            ps.setTime(6, toSqlTime(branch.getCloseTime()));
            ps.setString(7, branch.getStatus());

            return ps.executeUpdate() > 0;

        } catch (SQLException e) {
            e.printStackTrace();
        }

        return false;
    }

    public boolean update(Branch branch) {
        String sql = "UPDATE dbo.BRANCHES "
                + "SET cinema_id = ?, name = ?, address = ?, phone = ?, "
                + "open_time = ?, close_time = ?, status = ?, last_update = GETDATE() "
                + "WHERE id = ?";

        Connection conn = DBContext.getInstance().getConnection();

        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, branch.getCinemaId());
            ps.setString(2, branch.getName());
            ps.setString(3, branch.getAddress());
            ps.setString(4, branch.getPhone());
            ps.setTime(5, toSqlTime(branch.getOpenTime()));
            ps.setTime(6, toSqlTime(branch.getCloseTime()));
            ps.setString(7, branch.getStatus());
            ps.setInt(8, branch.getId());

            return ps.executeUpdate() > 0;

        } catch (SQLException e) {
            e.printStackTrace();
        }

        return false;
    }

    public boolean delete(int id) {
        String sql = "DELETE FROM dbo.BRANCHES WHERE id = ?";
        Connection conn = DBContext.getInstance().getConnection();

        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, id);
            return ps.executeUpdate() > 0;

        } catch (SQLException e) {
            e.printStackTrace();
        }

        return false;
    }

    public boolean updateStatus(int id, String status) {
        String sql = "UPDATE dbo.BRANCHES "
                + "SET status = ?, last_update = GETDATE() "
                + "WHERE id = ?";

        Connection conn = DBContext.getInstance().getConnection();

        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, status);
            ps.setInt(2, id);

            return ps.executeUpdate() > 0;

        } catch (SQLException e) {
            e.printStackTrace();
        }

        return false;
    }

    private Branch mapRow(ResultSet rs) throws SQLException {
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

    private Time toSqlTime(LocalTime time) {
        if (time == null) {
            return null;
        }

        return Time.valueOf(time);
    }
}