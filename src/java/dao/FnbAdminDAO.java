package dao;

import dto.FnbCategoryDTO;
import dto.FnbComboDTO;
import dto.FnbComboFormDTO;
import dto.FnbComboItemDTO;
import dto.FnbProductDTO;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.sql.Types;
import java.util.ArrayList;
import java.util.List;
import model.FnbCategory;
import model.FnbProduct;
import util.DBContext;

public class FnbAdminDAO {

    // =========================================================
    // CATEGORY
    // =========================================================
    public List<FnbCategoryDTO> findAllCategories() {
        List<FnbCategoryDTO> categories = new ArrayList<>();

        String sql = """
            SELECT
                c.id,
                c.name,
                c.description,
                c.status,
                COUNT(p.id) AS product_count
            FROM dbo.FNB_CATEGORIES c
            LEFT JOIN dbo.FNB_PRODUCTS p
                ON p.category_id = c.id
            GROUP BY
                c.id,
                c.name,
                c.description,
                c.status
            ORDER BY c.id
            """;

        try (Connection conn = getConnection(); PreparedStatement ps = conn.prepareStatement(sql); ResultSet rs = ps.executeQuery()) {

            while (rs.next()) {
                FnbCategoryDTO dto = new FnbCategoryDTO();
                dto.setId(rs.getInt("id"));
                dto.setName(rs.getString("name"));
                dto.setDescription(rs.getString("description"));
                dto.setStatus(rs.getString("status"));
                dto.setProductCount(rs.getInt("product_count"));
                categories.add(dto);
            }

            return categories;

        } catch (SQLException e) {
            throw new RuntimeException("Không thể lấy danh mục F&B.", e);
        }
    }

    public boolean insertCategory(FnbCategory category) {
        String sql = """
            INSERT INTO dbo.FNB_CATEGORIES
                (name, description, status, last_update)
            VALUES
                (?, ?, 'ACTIVE', GETDATE())
            """;

        try (Connection conn = getConnection(); PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setString(1, category.getName());
            ps.setString(2, category.getDescription());
            return ps.executeUpdate() > 0;

        } catch (SQLException e) {
            throw new RuntimeException("Không thể thêm danh mục.", e);
        }
    }

    public boolean updateCategory(FnbCategory category) {
        String sql = """
            UPDATE dbo.FNB_CATEGORIES
            SET name = ?,
                description = ?,
                last_update = GETDATE()
            WHERE id = ?
            """;

        try (Connection conn = getConnection(); PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setString(1, category.getName());
            ps.setString(2, category.getDescription());
            ps.setInt(3, category.getId());
            return ps.executeUpdate() > 0;

        } catch (SQLException e) {
            throw new RuntimeException("Không thể cập nhật danh mục.", e);
        }
    }

    public boolean existsCategoryNameExceptId(String name, Integer categoryId) {
        String sql = """
            SELECT 1
            FROM dbo.FNB_CATEGORIES
            WHERE LOWER(name) = LOWER(?)
              AND (? IS NULL OR id <> ?)
            """;

        try (Connection conn = getConnection(); PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setString(1, name);
            setNullableInteger(ps, 2, categoryId);
            setNullableInteger(ps, 3, categoryId);

            try (ResultSet rs = ps.executeQuery()) {
                return rs.next();
            }

        } catch (SQLException e) {
            throw new RuntimeException("Không thể kiểm tra danh mục.", e);
        }
    }

    public boolean updateCategoryStatus(Connection conn, int categoryId, String status)
            throws SQLException {

        String sql = """
            UPDATE dbo.FNB_CATEGORIES
            SET status = ?,
                last_update = GETDATE()
            WHERE id = ?
            """;

        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, status);
            ps.setInt(2, categoryId);
            return ps.executeUpdate() > 0;
        }
    }

    public List<Integer> findProductIdsByCategory(Connection conn, int categoryId)
            throws SQLException {

        List<Integer> productIds = new ArrayList<>();

        String sql = """
            SELECT id
            FROM dbo.FNB_PRODUCTS
            WHERE category_id = ?
            """;

        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, categoryId);

            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    productIds.add(rs.getInt("id"));
                }
            }
        }

        return productIds;
    }

    // =========================================================
    // PRODUCT
    // =========================================================
    public List<FnbProductDTO> findProductsByCategory(int categoryId) {
        List<FnbProductDTO> products = new ArrayList<>();

        String sql = """
            SELECT
                p.id,
                p.category_id,
                c.name AS category_name,
                p.name,
                p.description,
                p.image_url,
                p.product_type,
                p.selling_price,
                p.allowed_to_sell,
                p.status
            FROM dbo.FNB_PRODUCTS p
            INNER JOIN dbo.FNB_CATEGORIES c
                ON c.id = p.category_id
            WHERE p.category_id = ?
            ORDER BY p.id DESC
            """;

        try (Connection conn = getConnection(); PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setInt(1, categoryId);

            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    products.add(mapProductDTO(rs));
                }
            }

            return products;

        } catch (SQLException e) {
            throw new RuntimeException("Không thể lấy sản phẩm F&B.", e);
        }
    }

    public List<FnbProductDTO> findActiveItemsForCombo() {
        List<FnbProductDTO> products = new ArrayList<>();

        String sql = """
            SELECT
                p.id,
                p.category_id,
                c.name AS category_name,
                p.name,
                p.description,
                p.image_url,
                p.product_type,
                p.selling_price,
                p.allowed_to_sell,
                p.status
            FROM dbo.FNB_PRODUCTS p
            INNER JOIN dbo.FNB_CATEGORIES c
                ON c.id = p.category_id
            WHERE p.product_type = 'ITEM'
              AND p.status = 'ACTIVE'
            ORDER BY c.name, p.name
            """;

        try (Connection conn = getConnection(); PreparedStatement ps = conn.prepareStatement(sql); ResultSet rs = ps.executeQuery()) {

            while (rs.next()) {
                products.add(mapProductDTO(rs));
            }

            return products;

        } catch (SQLException e) {
            throw new RuntimeException("Không thể lấy sản phẩm cho combo.", e);
        }
    }

    public boolean insertProduct(FnbProduct product) {
        String sql = """
            INSERT INTO dbo.FNB_PRODUCTS
            (
                category_id,
                name,
                description,
                product_type,
                selling_price,
                image_url,
                allowed_to_sell,
                status,
                last_update
            )
            VALUES (?, ?, ?, ?, ?, ?, ?, 'ACTIVE', GETDATE())
            """;

        try (Connection conn = getConnection(); PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setInt(1, product.getCategoryId());
            ps.setString(2, product.getName());
            ps.setString(3, product.getDescription());
            ps.setString(4, product.getProductType());
            ps.setBigDecimal(5, product.getSellingPrice());
            ps.setString(6, product.getImageUrl());
            ps.setBoolean(7, product.isAllowedToSell());
            return ps.executeUpdate() > 0;

        } catch (SQLException e) {
            throw new RuntimeException("Không thể thêm sản phẩm.", e);
        }
    }

    public boolean updateProduct(FnbProduct product) {
        String sql = """
            UPDATE dbo.FNB_PRODUCTS
            SET category_id = ?,
                name = ?,
                description = ?,
                product_type = ?,
                selling_price = ?,
                image_url = ?,
                allowed_to_sell = ?,
                last_update = GETDATE()
            WHERE id = ?
            """;

        try (Connection conn = getConnection(); PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setInt(1, product.getCategoryId());
            ps.setString(2, product.getName());
            ps.setString(3, product.getDescription());
            ps.setString(4, product.getProductType());
            ps.setBigDecimal(5, product.getSellingPrice());
            ps.setString(6, product.getImageUrl());
            ps.setBoolean(7, product.isAllowedToSell());
            ps.setInt(8, product.getId());
            return ps.executeUpdate() > 0;

        } catch (SQLException e) {
            throw new RuntimeException("Không thể cập nhật sản phẩm.", e);
        }
    }

    public FnbProduct findProductById(Connection conn, int productId)
            throws SQLException {

        String sql = """
            SELECT
                id,
                category_id,
                name,
                description,
                product_type,
                selling_price,
                image_url,
                allowed_to_sell,
                status
            FROM dbo.FNB_PRODUCTS
            WHERE id = ?
            """;

        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, productId);

            try (ResultSet rs = ps.executeQuery()) {
                return rs.next() ? mapProduct(rs) : null;
            }
        }
    }

    public boolean updateAllowedToSell(Connection conn, int productId, boolean allowed)
            throws SQLException {

        String sql = """
            UPDATE dbo.FNB_PRODUCTS
            SET allowed_to_sell = ?,
                last_update = GETDATE()
            WHERE id = ?
            """;

        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setBoolean(1, allowed);
            ps.setInt(2, productId);
            return ps.executeUpdate() > 0;
        }
    }

    public boolean updateProductStatus(Connection conn, int productId, String status)
            throws SQLException {

        String sql = """
            UPDATE dbo.FNB_PRODUCTS
            SET status = ?,
                allowed_to_sell =
                    CASE
                        WHEN ? = 'INACTIVE' THEN 0
                        ELSE allowed_to_sell
                    END,
                last_update = GETDATE()
            WHERE id = ?
            """;

        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, status);
            ps.setString(2, status);
            ps.setInt(3, productId);
            return ps.executeUpdate() > 0;
        }
    }

    // =========================================================
    // PRODUCT - COMBO RELATION
    // Giữ nguyên chữ ký để không ảnh hưởng Service hiện tại.
    // =========================================================
    public List<Integer> findComboIdsContainingProduct(Connection conn, int productId)
            throws SQLException {

        List<Integer> comboIds = new ArrayList<>();

        String sql = """
            SELECT DISTINCT combo_id
            FROM dbo.FNB_COMBO_ITEMS
            WHERE product_id = ?
            """;

        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, productId);

            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    comboIds.add(rs.getInt("combo_id"));
                }
            }
        }

        return comboIds;
    }

    public void deactivateCombos(Connection conn, List<Integer> comboIds)
            throws SQLException {

        updateComboList(
                conn,
                comboIds,
                """
                UPDATE dbo.FNB_COMBOS
                SET status = 'INACTIVE',
                    allowed_to_sell = 0,
                    last_update = GETDATE()
                WHERE id = ?
                """
        );
    }

    public void disableComboSales(Connection conn, List<Integer> comboIds)
            throws SQLException {

        updateComboList(
                conn,
                comboIds,
                """
                UPDATE dbo.FNB_COMBOS
                SET allowed_to_sell = 0,
                    last_update = GETDATE()
                WHERE id = ?
                """
        );
    }

    // =========================================================
    // COMBO
    // =========================================================
    public List<FnbComboDTO> findAllCombos() {
        List<FnbComboDTO> combos = new ArrayList<>();

        String sql = """
            SELECT
                c.id,
                c.name,
                c.description,
                c.selling_price,
                c.image_url,
                c.allowed_to_sell,
                c.status,
                COALESCE(SUM(p.selling_price * ci.quantity), 0) AS original_price
            FROM dbo.FNB_COMBOS c
            LEFT JOIN dbo.FNB_COMBO_ITEMS ci
                ON ci.combo_id = c.id
            LEFT JOIN dbo.FNB_PRODUCTS p
                ON p.id = ci.product_id
            GROUP BY
                c.id,
                c.name,
                c.description,
                c.selling_price,
                c.image_url,
                c.allowed_to_sell,
                c.status
            ORDER BY c.id DESC
            """;

        try (Connection conn = getConnection(); PreparedStatement ps = conn.prepareStatement(sql); ResultSet rs = ps.executeQuery()) {

            while (rs.next()) {
                FnbComboDTO combo = mapComboDTO(rs);
                combo.setOriginalPrice(rs.getBigDecimal("original_price"));
                combo.setItems(findComboItems(conn, combo.getId()));
                combos.add(combo);
            }

            return combos;

        } catch (SQLException e) {
            throw new RuntimeException("Không thể lấy danh sách combo.", e);
        }
    }

    public FnbComboDTO findComboById(Connection conn, int comboId)
            throws SQLException {

        String sql = """
            SELECT
                id,
                name,
                description,
                selling_price,
                image_url,
                allowed_to_sell,
                status
            FROM dbo.FNB_COMBOS
            WHERE id = ?
            """;

        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, comboId);

            try (ResultSet rs = ps.executeQuery()) {
                return rs.next() ? mapComboDTO(rs) : null;
            }
        }
    }

    public List<FnbComboItemDTO> findComboItems(Connection conn, int comboId)
            throws SQLException {

        List<FnbComboItemDTO> items = new ArrayList<>();

        String sql = """
            SELECT
                ci.product_id,
                p.name AS product_name,
                ci.quantity,
                p.selling_price AS unit_price
            FROM dbo.FNB_COMBO_ITEMS ci
            INNER JOIN dbo.FNB_PRODUCTS p
                ON p.id = ci.product_id
            WHERE ci.combo_id = ?
            ORDER BY p.name
            """;

        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, comboId);

            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    FnbComboItemDTO item = new FnbComboItemDTO();
                    item.setProductId(rs.getInt("product_id"));
                    item.setProductName(rs.getString("product_name"));
                    item.setQuantity(rs.getInt("quantity"));
                    item.setUnitPrice(rs.getBigDecimal("unit_price"));
                    items.add(item);
                }
            }
        }

        return items;
    }

    public boolean existsComboNameExceptId(
            Connection conn,
            String name,
            Integer comboId) throws SQLException {

        String sql = """
            SELECT 1
            FROM dbo.FNB_COMBOS
            WHERE LOWER(name) = LOWER(?)
              AND (? IS NULL OR id <> ?)
            """;

        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, name);
            setNullableInteger(ps, 2, comboId);
            setNullableInteger(ps, 3, comboId);

            try (ResultSet rs = ps.executeQuery()) {
                return rs.next();
            }
        }
    }

    public int insertCombo(Connection conn, FnbComboFormDTO dto)
            throws SQLException {

        String sql = """
            INSERT INTO dbo.FNB_COMBOS
            (
                name,
                description,
                selling_price,
                image_url,
                allowed_to_sell,
                status,
                last_update
            )
            VALUES (?, ?, ?, ?, ?, 'ACTIVE', GETDATE())
            """;

        try (PreparedStatement ps = conn.prepareStatement(
                sql,
                Statement.RETURN_GENERATED_KEYS
        )) {

            ps.setString(1, dto.getName());
            ps.setString(2, dto.getDescription());
            ps.setBigDecimal(3, dto.getSellingPrice());
            ps.setString(4, dto.getImageUrl());
            ps.setBoolean(5, dto.isAllowedToSell());

            if (ps.executeUpdate() == 0) {
                return 0;
            }

            try (ResultSet keys = ps.getGeneratedKeys()) {
                return keys.next() ? keys.getInt(1) : 0;
            }
        }
    }

    public boolean updateCombo(Connection conn, FnbComboFormDTO dto)
            throws SQLException {

        String sql = """
            UPDATE dbo.FNB_COMBOS
            SET name = ?,
                description = ?,
                selling_price = ?,
                image_url = ?,
                allowed_to_sell = ?,
                last_update = GETDATE()
            WHERE id = ?
            """;

        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, dto.getName());
            ps.setString(2, dto.getDescription());
            ps.setBigDecimal(3, dto.getSellingPrice());
            ps.setString(4, dto.getImageUrl());
            ps.setBoolean(5, dto.isAllowedToSell());
            ps.setInt(6, dto.getId());
            return ps.executeUpdate() > 0;
        }
    }

    public void deleteComboItems(Connection conn, int comboId)
            throws SQLException {

        String sql = """
            DELETE FROM dbo.FNB_COMBO_ITEMS
            WHERE combo_id = ?
            """;

        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, comboId);
            ps.executeUpdate();
        }
    }

    public void insertComboItems(
            Connection conn,
            int comboId,
            List<FnbComboItemDTO> items) throws SQLException {

        String sql = """
            INSERT INTO dbo.FNB_COMBO_ITEMS
                (combo_id, product_id, quantity)
            VALUES (?, ?, ?)
            """;

        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            for (FnbComboItemDTO item : items) {
                ps.setInt(1, comboId);
                ps.setInt(2, item.getProductId());
                ps.setInt(3, item.getQuantity());
                ps.addBatch();
            }

            ps.executeBatch();
        }
    }

    public boolean updateComboStatus(int comboId, String status) {
        String sql = """
            UPDATE dbo.FNB_COMBOS
            SET status = ?,
                allowed_to_sell =
                    CASE
                        WHEN ? = 'INACTIVE' THEN 0
                        ELSE allowed_to_sell
                    END,
                last_update = GETDATE()
            WHERE id = ?
            """;

        try (Connection conn = getConnection(); PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setString(1, status);
            ps.setString(2, status);
            ps.setInt(3, comboId);
            return ps.executeUpdate() > 0;

        } catch (SQLException e) {
            throw new RuntimeException("Không thể cập nhật trạng thái combo.", e);
        }
    }

    public boolean updateComboAllowedToSell(
            Connection conn,
            int comboId,
            boolean allowed) throws SQLException {

        String sql = """
            UPDATE dbo.FNB_COMBOS
            SET allowed_to_sell = ?,
                last_update = GETDATE()
            WHERE id = ?
            """;

        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setBoolean(1, allowed);
            ps.setInt(2, comboId);
            return ps.executeUpdate() > 0;
        }
    }

    public List<String> findUnavailableComboItemNames(
            Connection conn,
            int comboId) throws SQLException {

        List<String> names = new ArrayList<>();

        String sql = """
            SELECT p.name
            FROM dbo.FNB_COMBO_ITEMS ci
            INNER JOIN dbo.FNB_PRODUCTS p
                ON p.id = ci.product_id
            INNER JOIN dbo.FNB_CATEGORIES c
                ON c.id = p.category_id
            WHERE ci.combo_id = ?
              AND (
                    p.status <> 'ACTIVE'
                    OR p.allowed_to_sell = 0
                    OR c.status <> 'ACTIVE'
              )
            ORDER BY p.name
            """;

        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, comboId);

            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    names.add(rs.getString("name"));
                }
            }
        }

        return names;
    }

    // =========================================================
    // PRIVATE HELPERS
    // =========================================================
    private Connection getConnection() throws SQLException {
        return DBContext.getInstance().getConnection();
    }

    private void setNullableInteger(
            PreparedStatement ps,
            int index,
            Integer value) throws SQLException {

        if (value == null) {
            ps.setNull(index, Types.INTEGER);
        } else {
            ps.setInt(index, value);
        }
    }

    private void updateComboList(
            Connection conn,
            List<Integer> comboIds,
            String sql) throws SQLException {

        if (comboIds == null || comboIds.isEmpty()) {
            return;
        }

        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            for (Integer comboId : comboIds) {
                if (comboId == null || comboId <= 0) {
                    continue;
                }

                ps.setInt(1, comboId);
                ps.addBatch();
            }

            ps.executeBatch();
        }
    }

    private FnbProductDTO mapProductDTO(ResultSet rs)
            throws SQLException {

        FnbProductDTO dto = new FnbProductDTO();
        dto.setId(rs.getInt("id"));
        dto.setCategoryId(rs.getInt("category_id"));
        dto.setCategoryName(rs.getString("category_name"));
        dto.setName(rs.getString("name"));
        dto.setDescription(rs.getString("description"));
        dto.setImageUrl(rs.getString("image_url"));
        dto.setProductType(rs.getString("product_type"));
        dto.setSellingPrice(rs.getBigDecimal("selling_price"));
        dto.setAllowedToSell(rs.getBoolean("allowed_to_sell"));
        dto.setStatus(rs.getString("status"));
        return dto;
    }

    private FnbProduct mapProduct(ResultSet rs)
            throws SQLException {

        FnbProduct product = new FnbProduct();
        product.setId(rs.getInt("id"));
        product.setCategoryId(rs.getInt("category_id"));
        product.setName(rs.getString("name"));
        product.setDescription(rs.getString("description"));
        product.setProductType(rs.getString("product_type"));
        product.setSellingPrice(rs.getBigDecimal("selling_price"));
        product.setImageUrl(rs.getString("image_url"));
        product.setAllowedToSell(rs.getBoolean("allowed_to_sell"));
        product.setStatus(rs.getString("status"));
        return product;
    }

    private FnbComboDTO mapComboDTO(ResultSet rs)
            throws SQLException {

        FnbComboDTO dto = new FnbComboDTO();
        dto.setId(rs.getInt("id"));
        dto.setName(rs.getString("name"));
        dto.setDescription(rs.getString("description"));
        dto.setSellingPrice(rs.getBigDecimal("selling_price"));
        dto.setImageUrl(rs.getString("image_url"));
        dto.setAllowedToSell(rs.getBoolean("allowed_to_sell"));
        dto.setStatus(rs.getString("status"));
        return dto;
    }
}
