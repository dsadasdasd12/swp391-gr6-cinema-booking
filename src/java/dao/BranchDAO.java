package dao;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import model.Branch;
import util.DBContext;
import util.EncodingUtil;

public class BranchDAO {

    public List<Branch> findAllActive() {
        List<Branch> list = new ArrayList<>();
        String sql = "SELECT id, name, address, phone, status FROM dbo.BRANCHES WHERE status = 'ACTIVE' ORDER BY name";
        Connection conn = DBContext.getInstance().getConnection();
        try (PreparedStatement ps = conn.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {
            while (rs.next()) {
                Branch b = new Branch();
                b.setId(rs.getInt("id"));
                b.setName(EncodingUtil.getString(rs, "name"));
                b.setAddress(EncodingUtil.getString(rs, "address"));
                b.setPhone(rs.getString("phone"));
                b.setStatus(rs.getString("status"));
                list.add(b);
            }
        } catch (SQLException e) {
            System.getLogger(BranchDAO.class.getName())
                  .log(System.Logger.Level.ERROR, "findAllActive branches thất bại", e);
        }
        return list;
    }
    
    public Branch findById(int id) {
        String sql = "SELECT id, name, address, phone, status FROM dbo.BRANCHES WHERE id = ?";
        Connection conn = DBContext.getInstance().getConnection();
        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, id);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    Branch b = new Branch();
                    b.setId(rs.getInt("id"));
                    b.setName(EncodingUtil.getString(rs, "name"));
                    b.setAddress(EncodingUtil.getString(rs, "address"));
                    b.setPhone(rs.getString("phone"));
                    b.setStatus(rs.getString("status"));
                    return b;
                }
            }
        } catch (SQLException e) {
            System.getLogger(BranchDAO.class.getName())
                  .log(System.Logger.Level.ERROR, "find branch by id thất bại", e);
        }
        return null;
    }
}
