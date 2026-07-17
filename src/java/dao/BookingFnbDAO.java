package dao;

import dto.BookingFnbLine;
import dto.StaffFnbComboDTO;
import dto.StaffFnbProductDTO;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import util.DBContext;

public class BookingFnbDAO {

    // ===== F&B STAFF POS - BEGIN =====
    /**
     * Lấy toàn bộ món lẻ và combo đang được phép bán tại đúng chi nhánh của Staff.
     *
     * Điều kiện món lẻ:
     * - Category ACTIVE
     * - Product ACTIVE
     * - Admin cho phép bán
     * - Manager bật bán tại chi nhánh
     * - Tồn kho > 0
     *
     * Điều kiện combo:
     * - Combo ACTIVE
     * - Admin cho phép bán
     * - Manager bật combo tại chi nhánh
     * - Tất cả sản phẩm thành phần vẫn ACTIVE, được phép bán và được bật tại chi nhánh
     * - Còn đủ nguyên liệu để tạo ít nhất 1 combo
     */
    public List<BookingFnbLine> findSellableByBranch(int branchId) {
        List<BookingFnbLine> result = new ArrayList<>();

        String products
                = "SELECT 'PRODUCT' AS item_type, "
                + "       p.id, p.name, p.description, p.image_url, p.selling_price, "
                + "       i.stock_quantity AS available_qty "
                + "FROM FNB_PRODUCTS p "
                + "JOIN FNB_CATEGORIES c ON c.id = p.category_id "
                + "JOIN BRANCH_FNB_INVENTORY i "
                + "  ON i.product_id = p.id "
                + " AND i.branch_id = ? "
                + "WHERE p.status = 'ACTIVE' "
                + "  AND p.allowed_to_sell = 1 "
                + "  AND c.status = 'ACTIVE' "
                + "  AND i.enabled_at_branch = 1 "
                + "  AND i.stock_quantity > 0 "
                + "ORDER BY p.name";

        String combos
                = "SELECT 'COMBO' AS item_type, "
                + "       cb.id, cb.name, cb.description, cb.image_url, cb.selling_price, "
                + "       ISNULL(MIN(inv.stock_quantity / NULLIF(ci.quantity, 0)), 0) AS available_qty "
                + "FROM FNB_COMBOS cb "
                + "JOIN BRANCH_FNB_COMBOS bc "
                + "  ON bc.combo_id = cb.id "
                + " AND bc.branch_id = ? "
                + " AND bc.enabled_at_branch = 1 "
                + "JOIN FNB_COMBO_ITEMS ci ON ci.combo_id = cb.id "
                + "JOIN FNB_PRODUCTS p ON p.id = ci.product_id "
                + "JOIN FNB_CATEGORIES cat ON cat.id = p.category_id "
                + "JOIN BRANCH_FNB_INVENTORY inv "
                + "  ON inv.product_id = ci.product_id "
                + " AND inv.branch_id = ? "
                + " AND inv.enabled_at_branch = 1 "
                + "WHERE cb.status = 'ACTIVE' "
                + "  AND cb.allowed_to_sell = 1 "
                + "  AND p.status = 'ACTIVE' "
                + "  AND p.allowed_to_sell = 1 "
                + "  AND cat.status = 'ACTIVE' "
                + "GROUP BY cb.id, cb.name, cb.description, cb.image_url, cb.selling_price "
                + "HAVING ISNULL(MIN(inv.stock_quantity / NULLIF(ci.quantity, 0)), 0) > 0 "
                + "ORDER BY cb.name";

        try (Connection connection = DBContext.getInstance().getConnection()) {
            load(connection, products, result, branchId);
            load(connection, combos, result, branchId, branchId);
        } catch (Exception e) {
            throw new RuntimeException(
                    "Không thể tải danh sách F&B tại chi nhánh " + branchId, e
            );
        }

        return result;
    }

    /**
     * Lấy danh sách sản phẩm lẻ dành riêng cho giao diện Staff POS.
     */
    public List<StaffFnbProductDTO> findSellableProductsByBranch(int branchId) {
        List<StaffFnbProductDTO> result = new ArrayList<>();

        String sql
                = "SELECT p.id AS product_id, "
                + "       p.name AS product_name, "
                + "       c.name AS category_name, "
                + "       p.description, "
                + "       p.image_url, "
                + "       p.selling_price, "
                + "       i.stock_quantity "
                + "FROM dbo.FNB_PRODUCTS p "
                + "JOIN dbo.FNB_CATEGORIES c ON c.id = p.category_id "
                + "JOIN dbo.BRANCH_FNB_INVENTORY i "
                + "  ON i.product_id = p.id "
                + " AND i.branch_id = ? "
                + "WHERE p.status = 'ACTIVE' "
                + "  AND p.allowed_to_sell = 1 "
                + "  AND c.status = 'ACTIVE' "
                + "  AND i.enabled_at_branch = 1 "
                + "  AND i.stock_quantity > 0 "
                + "ORDER BY p.name";

        try (Connection connection = DBContext.getInstance().getConnection();
                PreparedStatement ps = connection.prepareStatement(sql)) {

            ps.setInt(1, branchId);

            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    StaffFnbProductDTO item = new StaffFnbProductDTO();
                    item.setProductId(rs.getInt("product_id"));
                    item.setProductName(rs.getString("product_name"));
                    item.setCategoryName(rs.getString("category_name"));
                    item.setDescription(rs.getString("description"));
                    item.setImageUrl(rs.getString("image_url"));
                    item.setSellingPrice(rs.getDouble("selling_price"));
                    item.setStockQuantity(rs.getInt("stock_quantity"));
                    result.add(item);
                }
            }
        } catch (Exception e) {
            throw new RuntimeException(
                    "Không thể tải sản phẩm F&B tại chi nhánh " + branchId,
                    e
            );
        }

        return result;
    }

    /**
     * Lấy danh sách combo dành riêng cho giao diện Staff POS.
     */
    public List<StaffFnbComboDTO> findSellableCombosByBranch(int branchId) {
        List<StaffFnbComboDTO> result = new ArrayList<>();

        String sql
                = "SELECT cb.id AS combo_id, "
                + "       cb.name AS combo_name, "
                + "       cb.description, "
                + "       cb.image_url, "
                + "       cb.selling_price, "
                + "       ISNULL(MIN(inv.stock_quantity / NULLIF(ci.quantity, 0)), 0) "
                + "           AS available_quantity, "
                + "       STRING_AGG(CONCAT(ci.quantity, ' x ', p.name), ', ') "
                + "           WITHIN GROUP (ORDER BY p.name) AS item_summary "
                + "FROM dbo.FNB_COMBOS cb "
                + "JOIN dbo.BRANCH_FNB_COMBOS bc "
                + "  ON bc.combo_id = cb.id "
                + " AND bc.branch_id = ? "
                + " AND bc.enabled_at_branch = 1 "
                + "JOIN dbo.FNB_COMBO_ITEMS ci ON ci.combo_id = cb.id "
                + "JOIN dbo.FNB_PRODUCTS p ON p.id = ci.product_id "
                + "JOIN dbo.FNB_CATEGORIES cat ON cat.id = p.category_id "
                + "JOIN dbo.BRANCH_FNB_INVENTORY inv "
                + "  ON inv.product_id = ci.product_id "
                + " AND inv.branch_id = ? "
                + " AND inv.enabled_at_branch = 1 "
                + "WHERE cb.status = 'ACTIVE' "
                + "  AND cb.allowed_to_sell = 1 "
                + "  AND p.status = 'ACTIVE' "
                + "  AND p.allowed_to_sell = 1 "
                + "  AND cat.status = 'ACTIVE' "
                + "GROUP BY cb.id, cb.name, cb.description, "
                + "         cb.image_url, cb.selling_price "
                + "HAVING ISNULL(MIN(inv.stock_quantity / NULLIF(ci.quantity, 0)), 0) > 0 "
                + "ORDER BY cb.name";

        try (Connection connection = DBContext.getInstance().getConnection();
                PreparedStatement ps = connection.prepareStatement(sql)) {

            ps.setInt(1, branchId);
            ps.setInt(2, branchId);

            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    StaffFnbComboDTO combo = new StaffFnbComboDTO();
                    combo.setComboId(rs.getInt("combo_id"));
                    combo.setComboName(rs.getString("combo_name"));
                    combo.setDescription(rs.getString("description"));
                    combo.setImageUrl(rs.getString("image_url"));
                    combo.setSellingPrice(rs.getDouble("selling_price"));
                    combo.setAvailableQuantity(
                            rs.getInt("available_quantity")
                    );
                    combo.setItemSummary(rs.getString("item_summary"));
                    result.add(combo);
                }
            }
        } catch (Exception e) {
            throw new RuntimeException(
                    "Không thể tải combo F&B tại chi nhánh " + branchId,
                    e
            );
        }

        return result;
    }

    /**
     * Kiểm tra lại dữ liệu F&B gửi từ trình duyệt bằng dữ liệu thật trong DB.
     * Không tin giá, tên hoặc số lượng khả dụng do client gửi lên.
     */
    public List<BookingFnbLine> resolveSelection(
            int branchId,
            Map<String, Integer> quantities
    ) {
        List<BookingFnbLine> selected = new ArrayList<>();

        if (quantities == null || quantities.isEmpty()) {
            return selected;
        }

        Map<String, BookingFnbLine> sellable = new LinkedHashMap<>();

        for (BookingFnbLine line : findSellableByBranch(branchId)) {
            String key = line.getItemType().toUpperCase()
                    + ":" + line.getItemId();
            sellable.put(key, line);
        }

        for (Map.Entry<String, Integer> entry : quantities.entrySet()) {
            String key = entry.getKey() == null
                    ? ""
                    : entry.getKey().trim().toUpperCase();

            int quantity = entry.getValue() == null
                    ? 0
                    : entry.getValue();

            BookingFnbLine line = sellable.get(key);

            if (line == null) {
                throw new IllegalArgumentException(
                        "Món F&B không tồn tại hoặc hiện không được phép bán tại chi nhánh."
                );
            }

            if (quantity < 0) {
                throw new IllegalArgumentException(
                        "Số lượng F&B không được nhỏ hơn 0."
                );
            }

            if (quantity > line.getAvailableQuantity()) {
                throw new IllegalArgumentException(
                        "Món F&B '" + line.getName()
                        + "' chỉ còn tối đa "
                        + line.getAvailableQuantity() + "."
                );
            }

            if (quantity > 0) {
                line.setQuantity(quantity);
                selected.add(line);
            }
        }

        return selected;
    }

    /**
     * Đọc các dòng F&B đã lưu của một booking.
     *
     * Lưu ý: method này yêu cầu bảng BOOKING_FNB tồn tại.
     */
    public List<BookingFnbLine> findByBookingId(int bookingId) {
        List<BookingFnbLine> result = new ArrayList<>();

        String sql
                = "SELECT item_type, "
                + "       COALESCE(product_id, combo_id) AS id, "
                + "       item_name AS name, "
                + "       unit_price AS selling_price, "
                + "       quantity "
                + "FROM BOOKING_FNB "
                + "WHERE booking_id = ? "
                + "ORDER BY id";

        try (Connection connection = DBContext.getInstance().getConnection();
                PreparedStatement ps = connection.prepareStatement(sql)) {

            ps.setInt(1, bookingId);

            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    BookingFnbLine line = new BookingFnbLine();
                    line.setItemType(rs.getString("item_type"));
                    line.setItemId(rs.getInt("id"));
                    line.setName(rs.getString("name"));
                    line.setUnitPrice(rs.getDouble("selling_price"));
                    line.setQuantity(rs.getInt("quantity"));
                    result.add(line);
                }
            }
        } catch (Exception e) {
            throw new RuntimeException(
                    "Không thể tải F&B của booking " + bookingId, e
            );
        }

        return result;
    }

    private void load(
            Connection connection,
            String sql,
            List<BookingFnbLine> output,
            int... params
    ) throws Exception {

        try (PreparedStatement ps = connection.prepareStatement(sql)) {
            for (int index = 0; index < params.length; index++) {
                ps.setInt(index + 1, params[index]);
            }

            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    BookingFnbLine line = new BookingFnbLine();
                    line.setItemType(rs.getString("item_type"));
                    line.setItemId(rs.getInt("id"));
                    line.setName(rs.getString("name"));
                    line.setDescription(rs.getString("description"));
                    line.setImageUrl(rs.getString("image_url"));
                    line.setUnitPrice(rs.getDouble("selling_price"));
                    line.setAvailableQuantity(rs.getInt("available_qty"));
                    output.add(line);
                }
            }
        }
    }
    // ===== F&B STAFF POS - END =====
}
