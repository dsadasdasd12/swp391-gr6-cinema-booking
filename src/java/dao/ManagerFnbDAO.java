package dao;

import dto.ManagerFnbComboDTO;
import dto.ManagerFnbItemDTO;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import util.DBContext;

public class ManagerFnbDAO extends DBContext {

    /**
     * Tìm chi nhánh được phân công cho Manager.
     */
    public Integer findBranchIdByManagerId(int managerId)
            throws SQLException {

        String sql
                = "SELECT TOP 1 branch_id "
                + "FROM STAFF_BRANCH "
                + "WHERE user_id = ?";

        try (
                Connection connection = getConnection(); PreparedStatement statement
                = connection.prepareStatement(sql)) {
            statement.setInt(1, managerId);

            try (ResultSet resultSet = statement.executeQuery()) {
                if (resultSet.next()) {
                    return resultSet.getInt("branch_id");
                }
            }
        }

        return null;
    }

    /**
     * Lấy tên chi nhánh.
     */
    public String findBranchNameById(int branchId)
            throws SQLException {

        String sql
                = "SELECT name "
                + "FROM BRANCHES "
                + "WHERE id = ?";

        try (
                Connection connection = getConnection(); PreparedStatement statement
                = connection.prepareStatement(sql)) {
            statement.setInt(1, branchId);

            try (ResultSet resultSet = statement.executeQuery()) {
                if (resultSet.next()) {
                    return resultSet.getString("name");
                }
            }
        }

        return null;
    }

    /**
     * Lấy danh sách món lẻ và tồn kho tại chi nhánh.
     */
    public List<ManagerFnbItemDTO> findItemsByBranch(int branchId)
            throws SQLException {

        String sql
                = "SELECT "
                + "    p.id AS product_id, "
                + "    p.name AS product_name, "
                + "    p.image_url, "
                + "    p.category_id, "
                + "    c.name AS category_name, "
                + "    p.selling_price, "
                + "    p.status, "
                + "    p.allowed_to_sell, "
                + "    ISNULL(i.stock_quantity, 0) AS stock_quantity, "
                + "    ISNULL(i.enabled_at_branch, 0) AS enabled_at_branch "
                + "FROM FNB_PRODUCTS p "
                + "INNER JOIN FNB_CATEGORIES c "
                + "    ON c.id = p.category_id "
                + "LEFT JOIN BRANCH_FNB_INVENTORY i "
                + "    ON i.product_id = p.id "
                + "    AND i.branch_id = ? "
                + "WHERE p.product_type = 'ITEM' "
                + "ORDER BY c.name, p.name";

        List<ManagerFnbItemDTO> items = new ArrayList<>();

        try (
                Connection connection = getConnection(); PreparedStatement statement
                = connection.prepareStatement(sql)) {
            statement.setInt(1, branchId);

            try (ResultSet resultSet = statement.executeQuery()) {
                while (resultSet.next()) {
                    ManagerFnbItemDTO item
                            = new ManagerFnbItemDTO();

                    item.setProductId(
                            resultSet.getInt("product_id")
                    );

                    item.setProductName(
                            resultSet.getString("product_name")
                    );

                    item.setImageUrl(
                            resultSet.getString("image_url")
                    );

                    item.setCategoryId(
                            resultSet.getInt("category_id")
                    );

                    item.setCategoryName(
                            resultSet.getString("category_name")
                    );

                    item.setSellingPrice(
                            resultSet.getBigDecimal("selling_price")
                    );

                    item.setStatus(
                            resultSet.getString("status")
                    );

                    item.setAllowedToSell(
                            resultSet.getBoolean("allowed_to_sell")
                    );

                    item.setStockQuantity(
                            resultSet.getInt("stock_quantity")
                    );

                    item.setEnabledAtBranch(
                            resultSet.getBoolean("enabled_at_branch")
                    );

                    items.add(item);
                }
            }
        }

        return items;
    }

    /**
     * Lấy danh sách combo tại chi nhánh.
     *
     * availableQuantity được tính bằng:
     *
     * MIN( tồn kho món / số lượng món cần cho combo )
     */
    public List<ManagerFnbComboDTO> findCombosByBranch(int branchId)
            throws SQLException {

        String sql
                = "SELECT "
                + "    cb.id AS combo_id, "
                + "    cb.name AS combo_name, "
                + "    cb.image_url, "
                + "    cb.selling_price, "
                + "    cb.status, "
                + "    cb.allowed_to_sell, "
                + "    ISNULL(bc.enabled_at_branch, 0) "
                + "        AS enabled_at_branch, "
                + "    STRING_AGG( "
                + "        CONCAT(p.name, ' x', ci.quantity), "
                + "        ', ' "
                + "    ) AS item_summary, "
                + "    CASE "
                + "        WHEN COUNT(ci.product_id) = 0 THEN 0 "
                + "        ELSE ISNULL( "
                + "            MIN( "
                + "                ISNULL(inv.stock_quantity, 0) "
                + "                / NULLIF(ci.quantity, 0) "
                + "            ), "
                + "            0 "
                + "        ) "
                + "    END AS available_quantity "
                + "FROM FNB_COMBOS cb "
                + "LEFT JOIN BRANCH_FNB_COMBOS bc "
                + "    ON bc.combo_id = cb.id "
                + "    AND bc.branch_id = ? "
                + "LEFT JOIN FNB_COMBO_ITEMS ci "
                + "    ON ci.combo_id = cb.id "
                + "LEFT JOIN FNB_PRODUCTS p "
                + "    ON p.id = ci.product_id "
                + "LEFT JOIN BRANCH_FNB_INVENTORY inv "
                + "    ON inv.product_id = ci.product_id "
                + "    AND inv.branch_id = ? "
                + "GROUP BY "
                + "    cb.id, "
                + "    cb.name, "
                + "    cb.image_url, "
                + "    cb.selling_price, "
                + "    cb.status, "
                + "    cb.allowed_to_sell, "
                + "    bc.enabled_at_branch "
                + "ORDER BY cb.name";

        List<ManagerFnbComboDTO> combos
                = new ArrayList<>();

        try (
                Connection connection = getConnection(); PreparedStatement statement
                = connection.prepareStatement(sql)) {
            statement.setInt(1, branchId);
            statement.setInt(2, branchId);

            try (ResultSet resultSet = statement.executeQuery()) {
                while (resultSet.next()) {
                    ManagerFnbComboDTO combo
                            = new ManagerFnbComboDTO();

                    combo.setComboId(
                            resultSet.getInt("combo_id")
                    );

                    combo.setComboName(
                            resultSet.getString("combo_name")
                    );

                    combo.setImageUrl(
                            resultSet.getString("image_url")
                    );

                    combo.setSellingPrice(
                            resultSet.getBigDecimal("selling_price")
                    );

                    combo.setStatus(
                            resultSet.getString("status")
                    );

                    combo.setAllowedToSell(
                            resultSet.getBoolean("allowed_to_sell")
                    );

                    combo.setItemSummary(
                            resultSet.getString("item_summary")
                    );

                    combo.setAvailableQuantity(
                            resultSet.getInt("available_quantity")
                    );

                    combo.setEnabledAtBranch(
                            resultSet.getBoolean("enabled_at_branch")
                    );

                    combos.add(combo);
                }
            }
        }

        return combos;
    }

    /**
     * Kiểm tra sản phẩm có phải món lẻ không.
     */
    public boolean isItemProduct(int productId)
            throws SQLException {

        String sql
                = "SELECT 1 "
                + "FROM FNB_PRODUCTS "
                + "WHERE id = ? "
                + "AND product_type = 'ITEM'";

        try (
                Connection connection = getConnection(); PreparedStatement statement
                = connection.prepareStatement(sql)) {
            statement.setInt(1, productId);

            try (ResultSet resultSet = statement.executeQuery()) {
                return resultSet.next();
            }
        }
    }

    /**
     * Kiểm tra món có được phép bán bởi hệ thống hay không.
     *
     * Điều kiện: - món ACTIVE - món allowed_to_sell = 1 - danh mục ACTIVE
     */
    public boolean canItemBeSoldBySystem(int productId)
            throws SQLException {

        String sql
                = "SELECT 1 "
                + "FROM FNB_PRODUCTS p "
                + "INNER JOIN FNB_CATEGORIES c "
                + "    ON c.id = p.category_id "
                + "WHERE p.id = ? "
                + "AND p.product_type = 'ITEM' "
                + "AND p.status = 'ACTIVE' "
                + "AND p.allowed_to_sell = 1 "
                + "AND c.status = 'ACTIVE'";

        try (
                Connection connection = getConnection(); PreparedStatement statement
                = connection.prepareStatement(sql)) {
            statement.setInt(1, productId);

            try (ResultSet resultSet = statement.executeQuery()) {
                return resultSet.next();
            }
        }
    }

    /**
     * Thêm mới hoặc cập nhật tồn kho món tại chi nhánh.
     */
    public void upsertItemInventory(
            int branchId,
            int productId,
            int stockQuantity
    ) throws SQLException {

        String sql
                = "IF EXISTS ( "
                + "    SELECT 1 "
                + "    FROM BRANCH_FNB_INVENTORY "
                + "    WHERE branch_id = ? "
                + "    AND product_id = ? "
                + ") "
                + "BEGIN "
                + "    UPDATE BRANCH_FNB_INVENTORY "
                + "    SET stock_quantity = ?, "
                + "        last_update = GETDATE() "
                + "    WHERE branch_id = ? "
                + "    AND product_id = ? "
                + "END "
                + "ELSE "
                + "BEGIN "
                + "    INSERT INTO BRANCH_FNB_INVENTORY ( "
                + "        branch_id, "
                + "        product_id, "
                + "        stock_quantity, "
                + "        enabled_at_branch, "
                + "        last_update "
                + "    ) "
                + "    VALUES (?, ?, ?, 0, GETDATE()) "
                + "END";

        try (
                Connection connection = getConnection(); PreparedStatement statement
                = connection.prepareStatement(sql)) {
            statement.setInt(1, branchId);
            statement.setInt(2, productId);

            statement.setInt(3, stockQuantity);
            statement.setInt(4, branchId);
            statement.setInt(5, productId);

            statement.setInt(6, branchId);
            statement.setInt(7, productId);
            statement.setInt(8, stockQuantity);

            statement.executeUpdate();
        }
    }

    /**
     * Bật hoặc tắt món tại chi nhánh.
     */
    public void setItemEnabledAtBranch(
            int branchId,
            int productId,
            boolean enabled
    ) throws SQLException {

        String sql
                = "IF EXISTS ( "
                + "    SELECT 1 "
                + "    FROM BRANCH_FNB_INVENTORY "
                + "    WHERE branch_id = ? "
                + "    AND product_id = ? "
                + ") "
                + "BEGIN "
                + "    UPDATE BRANCH_FNB_INVENTORY "
                + "    SET enabled_at_branch = ?, "
                + "        last_update = GETDATE() "
                + "    WHERE branch_id = ? "
                + "    AND product_id = ? "
                + "END "
                + "ELSE "
                + "BEGIN "
                + "    INSERT INTO BRANCH_FNB_INVENTORY ( "
                + "        branch_id, "
                + "        product_id, "
                + "        stock_quantity, "
                + "        enabled_at_branch, "
                + "        last_update "
                + "    ) "
                + "    VALUES (?, ?, 0, ?, GETDATE()) "
                + "END";

        try (
                Connection connection = getConnection(); PreparedStatement statement
                = connection.prepareStatement(sql)) {
            statement.setInt(1, branchId);
            statement.setInt(2, productId);

            statement.setBoolean(3, enabled);
            statement.setInt(4, branchId);
            statement.setInt(5, productId);

            statement.setInt(6, branchId);
            statement.setInt(7, productId);
            statement.setBoolean(8, enabled);

            statement.executeUpdate();
        }
    }

    /**
     * Tắt tất cả combo chứa món bị Manager tắt.
     */
    public void disableCombosContainingItem(
            int branchId,
            int productId
    ) throws SQLException {

        String sql
                = "UPDATE bc "
                + "SET bc.enabled_at_branch = 0, "
                + "    bc.last_update = GETDATE() "
                + "FROM BRANCH_FNB_COMBOS bc "
                + "INNER JOIN FNB_COMBO_ITEMS ci "
                + "    ON ci.combo_id = bc.combo_id "
                + "WHERE bc.branch_id = ? "
                + "AND ci.product_id = ?";

        try (
                Connection connection = getConnection(); PreparedStatement statement
                = connection.prepareStatement(sql)) {
            statement.setInt(1, branchId);
            statement.setInt(2, productId);

            statement.executeUpdate();
        }
    }

    /**
     * Kiểm tra combo có tồn tại không.
     */
    public boolean comboExists(int comboId)
            throws SQLException {

        String sql
                = "SELECT 1 "
                + "FROM FNB_COMBOS "
                + "WHERE id = ?";

        try (
                Connection connection = getConnection(); PreparedStatement statement
                = connection.prepareStatement(sql)) {
            statement.setInt(1, comboId);

            try (ResultSet resultSet = statement.executeQuery()) {
                return resultSet.next();
            }
        }
    }

    /**
     * Kiểm tra điều kiện trước khi Manager bật combo.
     *
     * Trả về null nếu combo hợp lệ. Trả về thông báo lỗi nếu combo không hợp
     * lệ.
     */
    public String findComboEnableValidationError(
            int branchId,
            int comboId
    ) throws SQLException {

        String sql
                = "SELECT "
                + "    cb.status AS combo_status, "
                + "    cb.allowed_to_sell AS combo_allowed, "
                + "    COUNT(ci.product_id) AS item_count, "
                + "    SUM( "
                + "        CASE "
                + "            WHEN p.status <> 'ACTIVE' "
                + "                OR p.allowed_to_sell = 0 "
                + "                OR c.status <> 'ACTIVE' "
                + "            THEN 1 "
                + "            ELSE 0 "
                + "        END "
                + "    ) AS invalid_system_items, "
                + "    SUM( "
                + "        CASE "
                + "            WHEN ISNULL(inv.enabled_at_branch, 0) = 0 "
                + "            THEN 1 "
                + "            ELSE 0 "
                + "        END "
                + "    ) AS disabled_branch_items, "
                + "    ISNULL( "
                + "        MIN( "
                + "            ISNULL(inv.stock_quantity, 0) "
                + "            / NULLIF(ci.quantity, 0) "
                + "        ), "
                + "        0 "
                + "    ) AS available_quantity "
                + "FROM FNB_COMBOS cb "
                + "LEFT JOIN FNB_COMBO_ITEMS ci "
                + "    ON ci.combo_id = cb.id "
                + "LEFT JOIN FNB_PRODUCTS p "
                + "    ON p.id = ci.product_id "
                + "LEFT JOIN FNB_CATEGORIES c "
                + "    ON c.id = p.category_id "
                + "LEFT JOIN BRANCH_FNB_INVENTORY inv "
                + "    ON inv.product_id = ci.product_id "
                + "    AND inv.branch_id = ? "
                + "WHERE cb.id = ? "
                + "GROUP BY "
                + "    cb.status, "
                + "    cb.allowed_to_sell";

        try (
                Connection connection = getConnection(); PreparedStatement statement
                = connection.prepareStatement(sql)) {
            statement.setInt(1, branchId);
            statement.setInt(2, comboId);

            try (ResultSet resultSet = statement.executeQuery()) {

                if (!resultSet.next()) {
                    return "Combo không tồn tại.";
                }

                String comboStatus
                        = resultSet.getString("combo_status");

                boolean comboAllowed
                        = resultSet.getBoolean("combo_allowed");

                int itemCount
                        = resultSet.getInt("item_count");

                int invalidSystemItems
                        = resultSet.getInt("invalid_system_items");

                int disabledBranchItems
                        = resultSet.getInt("disabled_branch_items");

                int availableQuantity
                        = resultSet.getInt("available_quantity");

                if (!"ACTIVE".equalsIgnoreCase(comboStatus)) {
                    return "Combo đang bị Admin vô hiệu hóa.";
                }

                if (!comboAllowed) {
                    return "Combo đang bị Admin tắt quyền bán.";
                }

                if (itemCount <= 0) {
                    return "Combo chưa có món thành phần.";
                }

                if (invalidSystemItems > 0) {
                    return "Combo chứa món hoặc danh mục đang bị Admin khóa.";
                }

                if (disabledBranchItems > 0) {
                    return "Combo chứa món chưa được bật bán tại chi nhánh.";
                }

                if (availableQuantity <= 0) {
                    return "Tồn kho không đủ để bán combo.";
                }

                return null;
            }
        }
    }

    /**
     * Bật hoặc tắt combo tại chi nhánh.
     */
    public void setComboEnabledAtBranch(
            int branchId,
            int comboId,
            boolean enabled
    ) throws SQLException {

        String sql
                = "IF EXISTS ( "
                + "    SELECT 1 "
                + "    FROM BRANCH_FNB_COMBOS "
                + "    WHERE branch_id = ? "
                + "    AND combo_id = ? "
                + ") "
                + "BEGIN "
                + "    UPDATE BRANCH_FNB_COMBOS "
                + "    SET enabled_at_branch = ?, "
                + "        last_update = GETDATE() "
                + "    WHERE branch_id = ? "
                + "    AND combo_id = ? "
                + "END "
                + "ELSE "
                + "BEGIN "
                + "    INSERT INTO BRANCH_FNB_COMBOS ( "
                + "        branch_id, "
                + "        combo_id, "
                + "        enabled_at_branch, "
                + "        last_update "
                + "    ) "
                + "    VALUES (?, ?, ?, GETDATE()) "
                + "END";

        try (
                Connection connection = getConnection(); PreparedStatement statement
                = connection.prepareStatement(sql)) {
            statement.setInt(1, branchId);
            statement.setInt(2, comboId);

            statement.setBoolean(3, enabled);
            statement.setInt(4, branchId);
            statement.setInt(5, comboId);

            statement.setInt(6, branchId);
            statement.setInt(7, comboId);
            statement.setBoolean(8, enabled);

            statement.executeUpdate();
        }
    }

    /**
     * Kiểm tra món đang được bật bán tại chi nhánh.
     */
    public boolean isItemEnabledAtBranch(
            int branchId,
            int productId
    ) throws SQLException {

        String sql
                = "SELECT enabled_at_branch "
                + "FROM BRANCH_FNB_INVENTORY "
                + "WHERE branch_id = ? "
                + "AND product_id = ?";

        try (
                Connection connection = getConnection(); PreparedStatement statement
                = connection.prepareStatement(sql)) {
            statement.setInt(1, branchId);
            statement.setInt(2, productId);

            try (ResultSet resultSet = statement.executeQuery()) {
                return resultSet.next()
                        && resultSet.getBoolean("enabled_at_branch");
            }
        }
    }

    /**
     * Kiểm tra combo đang được bật bán tại chi nhánh.
     */
    public boolean isComboEnabledAtBranch(
            int branchId,
            int comboId
    ) throws SQLException {

        String sql
                = "SELECT enabled_at_branch "
                + "FROM BRANCH_FNB_COMBOS "
                + "WHERE branch_id = ? "
                + "AND combo_id = ?";

        try (
                Connection connection = getConnection(); PreparedStatement statement
                = connection.prepareStatement(sql)) {
            statement.setInt(1, branchId);
            statement.setInt(2, comboId);

            try (ResultSet resultSet = statement.executeQuery()) {
                return resultSet.next()
                        && resultSet.getBoolean("enabled_at_branch");
            }
        }
    }

    /**
     * Lấy tồn kho hiện tại của món.
     */
    public int getItemStock(
            int branchId,
            int productId
    ) throws SQLException {

        String sql
                = "SELECT stock_quantity "
                + "FROM BRANCH_FNB_INVENTORY "
                + "WHERE branch_id = ? "
                + "AND product_id = ?";

        try (
                Connection connection = getConnection(); PreparedStatement statement
                = connection.prepareStatement(sql)) {
            statement.setInt(1, branchId);
            statement.setInt(2, productId);

            try (ResultSet resultSet = statement.executeQuery()) {
                if (resultSet.next()) {
                    return resultSet.getInt("stock_quantity");
                }
            }
        }

        return 0;
    }

    /**
     * Tính số lượng combo có thể bán dựa trên tồn kho món.
     */
    public int calculateAvailableComboQuantity(
            int branchId,
            int comboId
    ) throws SQLException {

        String sql
                = "SELECT "
                + "    CASE "
                + "        WHEN COUNT(ci.product_id) = 0 THEN 0 "
                + "        ELSE ISNULL( "
                + "            MIN( "
                + "                ISNULL(inv.stock_quantity, 0) "
                + "                / NULLIF(ci.quantity, 0) "
                + "            ), "
                + "            0 "
                + "        ) "
                + "    END AS available_quantity "
                + "FROM FNB_COMBO_ITEMS ci "
                + "LEFT JOIN BRANCH_FNB_INVENTORY inv "
                + "    ON inv.product_id = ci.product_id "
                + "    AND inv.branch_id = ? "
                + "WHERE ci.combo_id = ?";

        try (
                Connection connection = getConnection(); PreparedStatement statement
                = connection.prepareStatement(sql)) {
            statement.setInt(1, branchId);
            statement.setInt(2, comboId);

            try (ResultSet resultSet = statement.executeQuery()) {
                if (resultSet.next()) {
                    return resultSet.getInt(
                            "available_quantity"
                    );
                }
            }
        }

        return 0;
    }
}
