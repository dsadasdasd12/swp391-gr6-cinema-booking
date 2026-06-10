package dao;

import model.Cinema;
import util.DBContext;
import util.EncodingUtil;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

public class CinemaDAO {

    public Cinema findPrimary() {
        String sql = "SELECT TOP 1 id, name, address, phone, logo_url, status FROM dbo.CINEMA ORDER BY id";
        Connection conn = DBContext.getInstance().getConnection();
        if (conn == null) return null;
        try (PreparedStatement ps = conn.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {
            if (rs.next()) {
                return mapRow(rs);
            }
        } catch (SQLException e) {
            System.getLogger(CinemaDAO.class.getName())
                  .log(System.Logger.Level.ERROR, "findPrimary cinema thất bại", e);
        }
        return null;
    }

    public boolean update(Cinema cinema) {
        if (cinema == null || cinema.getId() <= 0) return false;
        String sql = "UPDATE dbo.CINEMA SET name = ?, address = ?, phone = ?, status = ?, last_update = GETDATE() WHERE id = ?";
        Connection conn = DBContext.getInstance().getConnection();
        if (conn == null) return false;
        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setNString(1, cinema.getName());
            ps.setNString(2, cinema.getAddress());
            ps.setString(3, cinema.getPhone());
            ps.setString(4, cinema.getStatus() != null ? cinema.getStatus() : "ACTIVE");
            ps.setInt(5, cinema.getId());
            return ps.executeUpdate() > 0;
        } catch (SQLException e) {
            System.getLogger(CinemaDAO.class.getName())
                  .log(System.Logger.Level.ERROR, "update cinema thất bại", e);
            return false;
        }
    }

    private Cinema mapRow(ResultSet rs) throws SQLException {
        Cinema c = new Cinema();
        c.setId(rs.getInt("id"));
        c.setName(EncodingUtil.getString(rs, "name"));
        c.setAddress(EncodingUtil.getString(rs, "address"));
        c.setPhone(rs.getString("phone"));
        c.setLogoUrl(rs.getString("logo_url"));
        c.setStatus(rs.getString("status"));
        return c;
    }
}
