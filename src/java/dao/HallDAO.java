/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package dao;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import model.Hall;
import util.DBContext;

public class HallDAO {

    public List<Hall> findByBranchId(int branchId) {
        String sql = "SELECT h.id, h.branch_id, b.name AS branch_name, "
                + "h.name, h.seat_rows, h.seats_per_row, h.total_seats, "
                + "h.hall_type, h.status, h.last_update "
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
                + "h.name, h.seat_rows, h.seats_per_row, h.total_seats, "
                + "h.hall_type, h.status, h.last_update "
                + "FROM dbo.HALLS h "
                + "JOIN dbo.BRANCHES b ON h.branch_id = b.id "
                + "WHERE h.id = ? AND h.branch_id = ?";

        Connection conn = DBContext.getInstance().getConnection();

        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, id);
            ps.setInt(2, branchId);
            try (ResultSet rs = ps.executeQuery()) {
                return rs.next() ? mapRow(rs) : null;
            }
        } catch (SQLException e) {
            e.printStackTrace();
            return null;
        }
    }

    public boolean insert(Hall hall) {
        return inTransaction(conn -> {
            int hallId = insertHall(conn, hall);
            if (hallId <= 0) {
                return false;
            }

            insertDefaultSeats(conn, hallId, hall.getSeatRows(), hall.getSeatsPerRow());
            hall.setId(hallId);
            return true;
        });
    }

    /* Nếu phòng chưa từng có suất chiếu,xóa sơ đồ cũ và tạo lại sơ đồ mới*/
    public boolean update(Hall hall) {
        return inTransaction(conn -> {
            int[] oldLayout = findLayout(conn, hall.getId(), hall.getBranchId());
            if (oldLayout == null || !updateHallRow(conn, hall)) {
                return false;
            }

            boolean layoutChanged = oldLayout[0] != hall.getSeatRows()
                    || oldLayout[1] != hall.getSeatsPerRow();

            if (layoutChanged) {
                deleteSeats(conn, hall.getId());
                insertDefaultSeats(conn, hall.getId(), hall.getSeatRows(), hall.getSeatsPerRow());
            }

            return true;
        });
    }

    public boolean delete(int id, int branchId) {
        return inTransaction(conn -> {
            if (!exists(conn, id, branchId) || hasAnyShowtime(conn, id)) {
                return false;
            }

            try (PreparedStatement ps = conn.prepareStatement(
                    "DELETE FROM dbo.HALL_MOVIES WHERE hall_id = ?")) {
                ps.setInt(1, id);
                ps.executeUpdate();
            }

            deleteSeats(conn, id);

            try (PreparedStatement ps = conn.prepareStatement(
                    "DELETE FROM dbo.HALLS WHERE id = ? AND branch_id = ?")) {
                ps.setInt(1, id);
                ps.setInt(2, branchId);
                return ps.executeUpdate() > 0;
            }
        });
    }

    public boolean updateStatus(int id, int branchId, String status) {
        String sql = "UPDATE dbo.HALLS SET status = ?, last_update = GETDATE() "
                + "WHERE id = ? AND branch_id = ?";
        Connection conn = DBContext.getInstance().getConnection();

        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, status);
            ps.setInt(2, id);
            ps.setInt(3, branchId);
            return ps.executeUpdate() > 0;
        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }

    public boolean existsByNameAndBranchId(String name, int branchId) {
        String sql = "SELECT COUNT(*) FROM dbo.HALLS WHERE branch_id = ? "
                + "AND LOWER(LTRIM(RTRIM(name))) = LOWER(LTRIM(RTRIM(?)))";
        Connection conn = DBContext.getInstance().getConnection();

        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, branchId);
            ps.setString(2, name);
            try (ResultSet rs = ps.executeQuery()) {
                return rs.next() && rs.getInt(1) > 0;
            }
        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }

    public boolean existsByNameAndBranchIdExceptId(String name, int branchId, int id) {
        String sql = "SELECT COUNT(*) FROM dbo.HALLS WHERE branch_id = ? "
                + "AND LOWER(LTRIM(RTRIM(name))) = LOWER(LTRIM(RTRIM(?))) "
                + "AND id <> ?";
        Connection conn = DBContext.getInstance().getConnection();

        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, branchId);
            ps.setString(2, name);
            ps.setInt(3, id);
            try (ResultSet rs = ps.executeQuery()) {
                return rs.next() && rs.getInt(1) > 0;
            }
        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }

    public boolean hasAnyShowtime(int hallId) {
        Connection conn = DBContext.getInstance().getConnection();
        try {
            return hasAnyShowtime(conn, hallId);
        } catch (SQLException e) {
            e.printStackTrace();
            return true;
        }
    }

    public boolean hasUnfinishedShowtimes(int hallId) {
        String sql = "SELECT TOP 1 1 FROM dbo.SHOWTIMES "
                + "WHERE hall_id = ? "
                + "AND status IN ('SCHEDULED', 'ON_SALE') "
                + "AND end_time > GETDATE()";

        Connection conn = DBContext.getInstance().getConnection();

        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, hallId);

            try (ResultSet rs = ps.executeQuery()) {
                return rs.next();
            }

        } catch (SQLException e) {
            e.printStackTrace();
            return true;
        }
    }

    private int insertHall(Connection conn, Hall hall) throws SQLException {
        String sql = "INSERT INTO dbo.HALLS "
                + "(branch_id, name, seat_rows, seats_per_row, total_seats, hall_type, status) "
                + "VALUES (?, ?, ?, ?, ?, ?, ?)";

        try (PreparedStatement ps = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            ps.setInt(1, hall.getBranchId());
            ps.setString(2, hall.getName());
            ps.setInt(3, hall.getSeatRows());
            ps.setInt(4, hall.getSeatsPerRow());
            ps.setInt(5, hall.getTotalSeats());
            ps.setString(6, hall.getHallType());
            ps.setString(7, hall.getStatus());

            if (ps.executeUpdate() == 0) {
                return 0;
            }

            try (ResultSet keys = ps.getGeneratedKeys()) {
                return keys.next() ? keys.getInt(1) : 0;
            }
        }
    }

    private int[] findLayout(Connection conn, int id, int branchId) throws SQLException {
        String sql = "SELECT seat_rows, seats_per_row FROM dbo.HALLS "
                + "WHERE id = ? AND branch_id = ?";

        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, id);
            ps.setInt(2, branchId);
            try (ResultSet rs = ps.executeQuery()) {
                return rs.next()
                        ? new int[]{rs.getInt("seat_rows"), rs.getInt("seats_per_row")}
                        : null;
            }
        }
    }

    private boolean updateHallRow(Connection conn, Hall hall) throws SQLException {
        String sql = "UPDATE dbo.HALLS SET name = ?, seat_rows = ?, seats_per_row = ?, "
                + "total_seats = ?, hall_type = ?, status = ?, last_update = GETDATE() "
                + "WHERE id = ? AND branch_id = ?";

        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, hall.getName());
            ps.setInt(2, hall.getSeatRows());
            ps.setInt(3, hall.getSeatsPerRow());
            ps.setInt(4, hall.getTotalSeats());
            ps.setString(5, hall.getHallType());
            ps.setString(6, hall.getStatus());
            ps.setInt(7, hall.getId());
            ps.setInt(8, hall.getBranchId());
            return ps.executeUpdate() > 0;
        }
    }

    private void insertDefaultSeats(Connection conn, int hallId, int rows, int columns)
            throws SQLException {
        String sql = "INSERT INTO dbo.SEATS "
                + "(hall_id, seat_row, seat_number, seat_type, maintenance) "
                + "VALUES (?, ?, ?, 'STANDARD', 0)";

        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            for (int row = 0; row < rows; row++) {
                for (int number = 1; number <= columns; number++) {
                    ps.setInt(1, hallId);
                    ps.setString(2, String.valueOf((char) ('A' + row)));
                    ps.setInt(3, number);
                    ps.addBatch();
                }
            }
            ps.executeBatch();
        }
    }

    private void deleteSeats(Connection conn, int hallId) throws SQLException {
        try (PreparedStatement ps = conn.prepareStatement(
                "DELETE FROM dbo.SEATS WHERE hall_id = ?")) {
            ps.setInt(1, hallId);
            ps.executeUpdate();
        }
    }

    private boolean exists(Connection conn, int id, int branchId) throws SQLException {
        try (PreparedStatement ps = conn.prepareStatement(
                "SELECT COUNT(*) FROM dbo.HALLS WHERE id = ? AND branch_id = ?")) {
            ps.setInt(1, id);
            ps.setInt(2, branchId);
            try (ResultSet rs = ps.executeQuery()) {
                return rs.next() && rs.getInt(1) > 0;
            }
        }
    }

    private boolean hasAnyShowtime(Connection conn, int hallId) throws SQLException {
        try (PreparedStatement ps = conn.prepareStatement(
                "SELECT COUNT(*) FROM dbo.SHOWTIMES WHERE hall_id = ?")) {
            ps.setInt(1, hallId);
            try (ResultSet rs = ps.executeQuery()) {
                return rs.next() && rs.getInt(1) > 0;
            }
        }
    }

    private boolean inTransaction(SqlWork work) {
        Connection conn = DBContext.getInstance().getConnection();
        boolean oldAutoCommit = true;

        try {
            oldAutoCommit = conn.getAutoCommit();
            conn.setAutoCommit(false);
            boolean success = work.execute(conn);
            if (success) {
                conn.commit();
            } else {
                conn.rollback();
            }
            return success;
        } catch (SQLException e) {
            try {
                conn.rollback();
            } catch (SQLException ignored) {
            }
            e.printStackTrace();
            return false;
        } finally {
            try {
                conn.setAutoCommit(oldAutoCommit);
            } catch (SQLException ignored) {
            }
        }
    }

    @FunctionalInterface
    private interface SqlWork {
        boolean execute(Connection conn) throws SQLException;
    }

    private Hall mapRow(ResultSet rs) throws SQLException {
        Hall hall = new Hall();
        hall.setId(rs.getInt("id"));
        hall.setBranchId(rs.getInt("branch_id"));
        hall.setBranchName(rs.getString("branch_name"));
        hall.setName(rs.getString("name"));
        hall.setSeatRows(rs.getInt("seat_rows"));
        hall.setSeatsPerRow(rs.getInt("seats_per_row"));
        hall.setTotalSeats(rs.getInt("total_seats"));
        hall.setHallType(rs.getString("hall_type"));
        hall.setStatus(rs.getString("status"));
        hall.setLastUpdate(rs.getObject("last_update", LocalDateTime.class));
        return hall;
    }
}
