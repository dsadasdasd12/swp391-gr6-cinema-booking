package dao;

import dto.BookingFnbLine;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import util.DBContext;

public class BookingFnbDAO {

    public List<BookingFnbLine> findSellableByBranch(int branchId) {
        List<BookingFnbLine> result = new ArrayList<>();
        String products = "SELECT 'PRODUCT' item_type,p.id,p.name,p.description,p.image_url,p.selling_price,"
                + " i.stock_quantity available_qty FROM FNB_PRODUCTS p JOIN FNB_CATEGORIES c ON c.id=p.category_id"
                + " JOIN BRANCH_FNB_INVENTORY i ON i.product_id=p.id AND i.branch_id=?"
                + " WHERE p.status='ACTIVE' AND p.allowed_to_sell=1 AND c.status='ACTIVE'"
                + " AND i.enabled_at_branch=1 AND i.stock_quantity>0";
        String combos = "SELECT 'COMBO' item_type,cb.id,cb.name,cb.description,cb.image_url,cb.selling_price,"
                + " ISNULL(MIN(inv.stock_quantity/NULLIF(ci.quantity,0)),0) available_qty"
                + " FROM FNB_COMBOS cb JOIN BRANCH_FNB_COMBOS bc ON bc.combo_id=cb.id AND bc.branch_id=? AND bc.enabled_at_branch=1"
                + " JOIN FNB_COMBO_ITEMS ci ON ci.combo_id=cb.id"
                + " JOIN BRANCH_FNB_INVENTORY inv ON inv.product_id=ci.product_id AND inv.branch_id=? AND inv.enabled_at_branch=1"
                + " WHERE cb.status='ACTIVE' AND cb.allowed_to_sell=1"
                + " GROUP BY cb.id,cb.name,cb.description,cb.image_url,cb.selling_price"
                + " HAVING ISNULL(MIN(inv.stock_quantity/NULLIF(ci.quantity,0)),0)>0";
        try (Connection c = DBContext.getInstance().getConnection()) {
            load(c, products, result, branchId);
            load(c, combos, result, branchId, branchId);
        } catch (Exception e) {
            e.printStackTrace();
        }
        return result;
    }

    public List<BookingFnbLine> resolveSelection(int branchId, Map<String, Integer> quantities) {
        if (quantities == null || quantities.isEmpty()) {
            return new ArrayList<>();
        }
        Map<String, BookingFnbLine> sellable = new LinkedHashMap<>();
        for (BookingFnbLine line : findSellableByBranch(branchId)) {
            sellable.put(line.getItemType() + ":" + line.getItemId(), line);
        }
        List<BookingFnbLine> selected = new ArrayList<>();
        for (Map.Entry<String, Integer> entry : quantities.entrySet()) {
            BookingFnbLine line = sellable.get(entry.getKey());
            int qty = entry.getValue() == null ? 0 : entry.getValue();
            if (line == null || qty < 0 || qty > line.getAvailableQuantity()) {
                throw new IllegalArgumentException("Món F&B không còn đủ số lượng tại chi nhánh.");
            }
            if (qty > 0) {
                line.setQuantity(qty);
                selected.add(line);
            }
        }
        return selected;
    }

    public List<BookingFnbLine> findByBookingId(int bookingId) {
        List<BookingFnbLine> result = new ArrayList<>();
        String sql = "SELECT item_type,COALESCE(product_id,combo_id) id,item_name name,NULL description,NULL image_url,"
                + " unit_price selling_price,quantity available_qty,quantity FROM BOOKING_FNB WHERE booking_id=? ORDER BY id";
        try (Connection c = DBContext.getInstance().getConnection(); PreparedStatement ps = c.prepareStatement(sql)) {
            ps.setInt(1, bookingId);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    BookingFnbLine x = new BookingFnbLine();
                    x.setItemType(rs.getString("item_type"));
                    x.setItemId(rs.getInt("id"));
                    x.setName(rs.getString("name"));
                    x.setUnitPrice(rs.getDouble("selling_price"));
                    x.setQuantity(rs.getInt("quantity"));
                    result.add(x);
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return result;
    }

    private void load(Connection c, String sql, List<BookingFnbLine> out, int... params) throws Exception {
        try (PreparedStatement ps = c.prepareStatement(sql)) {
            for (int i = 0; i < params.length; i++) {
                ps.setInt(i + 1, params[i]);
            }
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    BookingFnbLine x = new BookingFnbLine();
                    x.setItemType(rs.getString("item_type"));
                    x.setItemId(rs.getInt("id"));
                    x.setName(rs.getString("name"));
                    x.setDescription(rs.getString("description"));
                    x.setImageUrl(rs.getString("image_url"));
                    x.setUnitPrice(rs.getDouble("selling_price"));
                    x.setAvailableQuantity(rs.getInt("available_qty"));
                    out.add(x);
                }
            }
        }
    }
}
