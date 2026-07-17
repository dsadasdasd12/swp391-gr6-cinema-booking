package dao;

import dto.FnbComboDTO;
import dto.FnbComboFormDTO;
import dto.FnbComboItemDTO;

import dto.FnbCategoryDTO;
import dto.FnbProductDTO;
import java.sql.*;
import java.util.ArrayList;
import java.util.List;
import model.FnbCategory;
import model.FnbProduct;
import util.DBContext;

public class FnbAdminDAO {

    public List<FnbCategoryDTO> findAllCategories() {
        List<FnbCategoryDTO> list = new ArrayList<>();

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

        try (Connection conn = DBContext.getInstance().getConnection(); PreparedStatement ps = conn.prepareStatement(sql); ResultSet rs = ps.executeQuery()) {

            while (rs.next()) {
                FnbCategoryDTO dto = new FnbCategoryDTO();

                dto.setId(rs.getInt("id"));
                dto.setName(rs.getString("name"));
                dto.setDescription(rs.getString("description"));
                dto.setStatus(rs.getString("status"));
                dto.setProductCount(rs.getInt("product_count"));

                list.add(dto);
            }

        } catch (SQLException e) {
            throw new RuntimeException("Không thể lấy danh mục F&B.", e);
        }

        return list;
    }

    public List<FnbProductDTO> findProductsByCategory(int categoryId) {
        List<FnbProductDTO> list = new ArrayList<>();

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
            JOIN dbo.FNB_CATEGORIES c
                ON c.id = p.category_id
            WHERE p.category_id = ?
            ORDER BY p.id DESC
            """;

        try (Connection conn = DBContext.getInstance().getConnection(); PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setInt(1, categoryId);

            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
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

                    list.add(dto);
                }
            }

        } catch (SQLException e) {
            throw new RuntimeException("Không thể lấy sản phẩm F&B.", e);
        }

        return list;
    }

    public boolean updateAllowedToSell(int productId, boolean allowed) {
        String sql = """
            UPDATE dbo.FNB_PRODUCTS
            SET allowed_to_sell = ?,
                last_update = GETDATE()
            WHERE id = ?
            """;

        try (Connection conn = DBContext.getInstance().getConnection(); PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setBoolean(1, allowed);
            ps.setInt(2, productId);

            return ps.executeUpdate() > 0;

        } catch (SQLException e) {
            throw new RuntimeException("Không thể cập nhật trạng thái bán.", e);
        }
    }

    public boolean insertCategory(FnbCategory category) {
        String sql = """
        INSERT INTO dbo.FNB_CATEGORIES
            (name, description, status, last_update)
        VALUES
            (?, ?, 'ACTIVE', GETDATE())
        """;

        try (Connection conn = DBContext.getInstance().getConnection(); PreparedStatement ps = conn.prepareStatement(sql)) {

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

        try (Connection conn = DBContext.getInstance().getConnection(); PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setString(1, category.getName());
            ps.setString(2, category.getDescription());
            ps.setInt(3, category.getId());

            return ps.executeUpdate() > 0;

        } catch (SQLException e) {
            throw new RuntimeException("Không thể cập nhật danh mục.", e);
        }
    }

    public boolean updateCategoryStatus(int id, String status) {
        String sql = """
        UPDATE dbo.FNB_CATEGORIES
        SET status = ?,
            last_update = GETDATE()
        WHERE id = ?
        """;

        try (Connection conn = DBContext.getInstance().getConnection(); PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setString(1, status);
            ps.setInt(2, id);

            return ps.executeUpdate() > 0;

        } catch (SQLException e) {
            throw new RuntimeException(
                    "Không thể cập nhật trạng thái danh mục.", e
            );
        }
    }

    public boolean existsCategoryNameExceptId(String name, Integer id) {
        String sql = """
        SELECT 1
        FROM dbo.FNB_CATEGORIES
        WHERE LOWER(name) = LOWER(?)
          AND (? IS NULL OR id <> ?)
        """;

        try (Connection conn = DBContext.getInstance().getConnection(); PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setString(1, name);

            if (id == null) {
                ps.setNull(2, Types.INTEGER);
                ps.setNull(3, Types.INTEGER);
            } else {
                ps.setInt(2, id);
                ps.setInt(3, id);
            }

            try (ResultSet rs = ps.executeQuery()) {
                return rs.next();
            }

        } catch (SQLException e) {
            throw new RuntimeException("Không thể kiểm tra danh mục.", e);
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

        try (Connection conn = DBContext.getInstance().getConnection(); PreparedStatement ps = conn.prepareStatement(sql)) {

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

        try (Connection conn = DBContext.getInstance().getConnection(); PreparedStatement ps = conn.prepareStatement(sql)) {

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

    public boolean updateProductStatus(int productId, String status) {

        String sql = """
        UPDATE dbo.FNB_PRODUCTS
        SET status = ?,
            allowed_to_sell =
                CASE
                    WHEN ? = 'INACTIVE'
                        THEN 0
                    ELSE allowed_to_sell
                END,
            last_update = GETDATE()
        WHERE id = ?
        """;

        try (Connection conn = DBContext.getInstance().getConnection(); PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setString(1, status);
            ps.setString(2, status);
            ps.setInt(3, productId);

            return ps.executeUpdate() > 0;

        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }

    public List<Integer> findComboIdsContainingProduct(
            Connection conn,
            int productId) throws SQLException {

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

    public boolean updateProductStatus(
            Connection conn,
            int productId,
            String status) throws SQLException {

        String sql = """
        UPDATE dbo.FNB_PRODUCTS
        SET status = ?,
            allowed_to_sell =
                CASE
                    WHEN ? = 'INACTIVE'
                        THEN 0
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

    public void deactivateCombos(
            Connection conn,
            List<Integer> comboIds) throws SQLException {

        if (comboIds == null || comboIds.isEmpty()) {
            return;
        }

        String sql = """
        UPDATE dbo.FNB_PRODUCTS
        SET status = 'INACTIVE',
            allowed_to_sell = 0,
            last_update = GETDATE()
        WHERE id = ?
          AND product_type = 'COMBO'
        """;

        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            for (Integer comboId : comboIds) {
                ps.setInt(1, comboId);
                ps.addBatch();
            }

            ps.executeBatch();
        }
    }

    public List<Integer> findProductIdsByCategory(
            Connection conn,
            int categoryId) throws SQLException {

        List<Integer> ids = new ArrayList<>();

        String sql = """
        SELECT id
        FROM dbo.FNB_PRODUCTS
        WHERE category_id = ?
        """;

        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, categoryId);

            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    ids.add(rs.getInt("id"));
                }
            }
        }

        return ids;
    }

    public boolean updateCategoryStatus(
            Connection conn,
            int categoryId,
            String status) throws SQLException {

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

    public FnbProduct findProductById(int productId) {
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
            status,
            last_update
        FROM dbo.FNB_PRODUCTS
        WHERE id = ?
        """;

        try (Connection conn
                = DBContext.getInstance().getConnection(); PreparedStatement ps
                = conn.prepareStatement(sql)) {

            ps.setInt(1, productId);

            try (ResultSet rs = ps.executeQuery()) {
                if (!rs.next()) {
                    return null;
                }

                FnbProduct product = new FnbProduct();

                product.setId(rs.getInt("id"));
                product.setCategoryId(
                        rs.getInt("category_id")
                );
                product.setName(rs.getString("name"));
                product.setDescription(
                        rs.getString("description")
                );
                product.setProductType(
                        rs.getString("product_type")
                );
                product.setSellingPrice(
                        rs.getBigDecimal("selling_price")
                );
                product.setImageUrl(
                        rs.getString("image_url")
                );
                product.setAllowedToSell(
                        rs.getBoolean("allowed_to_sell")
                );
                product.setStatus(
                        rs.getString("status")
                );

                return product;
            }

        } catch (SQLException e) {
            throw new RuntimeException(
                    "Không thể tìm sản phẩm F&B.",
                    e
            );
        }
    }

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

            COALESCE(
                SUM(p.selling_price * ci.quantity),
                0
            ) AS original_price

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

        try (Connection conn
                = DBContext.getInstance().getConnection(); PreparedStatement ps
                = conn.prepareStatement(sql); ResultSet rs = ps.executeQuery()) {

            while (rs.next()) {

                FnbComboDTO dto = new FnbComboDTO();

                dto.setId(rs.getInt("id"));
                dto.setName(rs.getString("name"));
                dto.setDescription(
                        rs.getString("description")
                );
                dto.setSellingPrice(
                        rs.getBigDecimal("selling_price")
                );
                dto.setOriginalPrice(
                        rs.getBigDecimal("original_price")
                );
                dto.setImageUrl(
                        rs.getString("image_url")
                );
                dto.setAllowedToSell(
                        rs.getBoolean("allowed_to_sell")
                );
                dto.setStatus(
                        rs.getString("status")
                );

                dto.setItems(
                        findComboItems(conn, dto.getId())
                );

                combos.add(dto);
            }

            return combos;

        } catch (SQLException e) {
            throw new RuntimeException(
                    "Không thể lấy danh sách combo.",
                    e
            );
        }
    }

    public List<FnbComboItemDTO> findComboItems(
            Connection conn,
            int comboId) throws SQLException {

        List<FnbComboItemDTO> items
                = new ArrayList<>();

        String sql = """
        SELECT
            ci.product_id,
            p.name AS product_name,
            ci.quantity,
            p.selling_price AS unit_price

        FROM dbo.FNB_COMBO_ITEMS ci

        JOIN dbo.FNB_PRODUCTS p
            ON p.id = ci.product_id

        WHERE ci.combo_id = ?

        ORDER BY p.name
        """;

        try (PreparedStatement ps
                = conn.prepareStatement(sql)) {

            ps.setInt(1, comboId);

            try (ResultSet rs = ps.executeQuery()) {

                while (rs.next()) {

                    FnbComboItemDTO item
                            = new FnbComboItemDTO();

                    item.setProductId(
                            rs.getInt("product_id")
                    );
                    item.setProductName(
                            rs.getString("product_name")
                    );
                    item.setQuantity(
                            rs.getInt("quantity")
                    );
                    item.setUnitPrice(
                            rs.getBigDecimal("unit_price")
                    );

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

        try (PreparedStatement ps
                = conn.prepareStatement(sql)) {

            ps.setString(1, name);

            if (comboId == null) {
                ps.setNull(2, Types.INTEGER);
                ps.setNull(3, Types.INTEGER);
            } else {
                ps.setInt(2, comboId);
                ps.setInt(3, comboId);
            }

            try (ResultSet rs = ps.executeQuery()) {
                return rs.next();
            }
        }
    }
    
    public int insertCombo(
        Connection conn,
        FnbComboFormDTO dto) throws SQLException {

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
        VALUES
        (
            ?, ?, ?, ?, ?, 'ACTIVE', GETDATE()
        )
        """;

    try (PreparedStatement ps =
                 conn.prepareStatement(
                         sql,
                         Statement.RETURN_GENERATED_KEYS
                 )) {

        ps.setString(1, dto.getName());
        ps.setString(2, dto.getDescription());
        ps.setBigDecimal(
                3,
                dto.getSellingPrice()
        );
        ps.setString(4, dto.getImageUrl());
        ps.setBoolean(
                5,
                dto.isAllowedToSell()
        );

        if (ps.executeUpdate() == 0) {
            return 0;
        }

        try (ResultSet keys =
                     ps.getGeneratedKeys()) {

            if (keys.next()) {
                return keys.getInt(1);
            }
        }

        return 0;
    }
}
    
    public boolean updateCombo(
        Connection conn,
        FnbComboFormDTO dto) throws SQLException {

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

    try (PreparedStatement ps =
                 conn.prepareStatement(sql)) {

        ps.setString(1, dto.getName());
        ps.setString(2, dto.getDescription());
        ps.setBigDecimal(
                3,
                dto.getSellingPrice()
        );
        ps.setString(4, dto.getImageUrl());
        ps.setBoolean(
                5,
                dto.isAllowedToSell()
        );
        ps.setInt(6, dto.getId());

        return ps.executeUpdate() > 0;
    }
}
    
    public void deleteComboItems(
        Connection conn,
        int comboId) throws SQLException {

    String sql = """
        DELETE FROM dbo.FNB_COMBO_ITEMS
        WHERE combo_id = ?
        """;

    try (PreparedStatement ps =
                 conn.prepareStatement(sql)) {

        ps.setInt(1, comboId);
        ps.executeUpdate();
    }
}
    
    public void insertComboItems(
        Connection conn,
        int comboId,
        List<FnbComboItemDTO> items)
        throws SQLException {

    String sql = """
        INSERT INTO dbo.FNB_COMBO_ITEMS
        (
            combo_id,
            product_id,
            quantity
        )
        VALUES (?, ?, ?)
        """;

    try (PreparedStatement ps =
                 conn.prepareStatement(sql)) {

        for (FnbComboItemDTO item : items) {

            ps.setInt(1, comboId);
            ps.setInt(
                    2,
                    item.getProductId()
            );
            ps.setInt(
                    3,
                    item.getQuantity()
            );

            ps.addBatch();
        }

        ps.executeBatch();
    }
}
    
    public boolean updateComboStatus(
        int comboId,
        String status) {

    String sql = """
        UPDATE dbo.FNB_COMBOS
        SET status = ?,

            allowed_to_sell =
                CASE
                    WHEN ? = 'INACTIVE'
                        THEN 0
                    ELSE allowed_to_sell
                END,

            last_update = GETDATE()

        WHERE id = ?
        """;

    try (Connection conn =
                 DBContext.getInstance().getConnection();
         PreparedStatement ps =
                 conn.prepareStatement(sql)) {

        ps.setString(1, status);
        ps.setString(2, status);
        ps.setInt(3, comboId);

        return ps.executeUpdate() > 0;

    } catch (SQLException e) {
        throw new RuntimeException(
                "Không thể cập nhật trạng thái combo.",
                e
        );
    }
}
    
    public boolean updateComboAllowedToSell(
        int comboId,
        boolean allowed) {

    String sql = """
        UPDATE dbo.FNB_COMBOS
        SET allowed_to_sell = ?,
            last_update = GETDATE()
        WHERE id = ?
        """;

    try (Connection conn =
                 DBContext.getInstance().getConnection();
         PreparedStatement ps =
                 conn.prepareStatement(sql)) {

        ps.setBoolean(1, allowed);
        ps.setInt(2, comboId);

        return ps.executeUpdate() > 0;

    } catch (SQLException e) {
        throw new RuntimeException(
                "Không thể cập nhật quyền bán combo.",
                e
        );
    }
}
    
    public FnbComboDTO findComboById(int comboId) {

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

    try (Connection conn =
                 DBContext.getInstance().getConnection();
         PreparedStatement ps =
                 conn.prepareStatement(sql)) {

        ps.setInt(1, comboId);

        try (ResultSet rs = ps.executeQuery()) {

            if (!rs.next()) {
                return null;
            }

            FnbComboDTO dto = new FnbComboDTO();

            dto.setId(rs.getInt("id"));
            dto.setName(rs.getString("name"));
            dto.setDescription(
                    rs.getString("description")
            );
            dto.setSellingPrice(
                    rs.getBigDecimal("selling_price")
            );
            dto.setImageUrl(
                    rs.getString("image_url")
            );
            dto.setAllowedToSell(
                    rs.getBoolean("allowed_to_sell")
            );
            dto.setStatus(
                    rs.getString("status")
            );

            dto.setItems(
                    findComboItems(conn, comboId)
            );

            return dto;
        }

    } catch (SQLException e) {
        throw new RuntimeException(
                "Không thể tìm combo.",
                e
        );
    }
}
    
    public List<FnbProductDTO> findActiveItemsForCombo() {

    List<FnbProductDTO> products =
            new ArrayList<>();

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

        JOIN dbo.FNB_CATEGORIES c
            ON c.id = p.category_id

        WHERE p.product_type = 'ITEM'
          AND p.status = 'ACTIVE'

        ORDER BY c.name, p.name
        """;

    try (Connection conn =
                 DBContext.getInstance().getConnection();
         PreparedStatement ps =
                 conn.prepareStatement(sql);
         ResultSet rs = ps.executeQuery()) {

        while (rs.next()) {

            FnbProductDTO dto =
                    new FnbProductDTO();

            dto.setId(rs.getInt("id"));
            dto.setCategoryId(
                    rs.getInt("category_id")
            );
            dto.setCategoryName(
                    rs.getString("category_name")
            );
            dto.setName(rs.getString("name"));
            dto.setDescription(
                    rs.getString("description")
            );
            dto.setImageUrl(
                    rs.getString("image_url")
            );
            dto.setProductType(
                    rs.getString("product_type")
            );
            dto.setSellingPrice(
                    rs.getBigDecimal("selling_price")
            );
            dto.setAllowedToSell(
                    rs.getBoolean("allowed_to_sell")
            );
            dto.setStatus(
                    rs.getString("status")
            );

            products.add(dto);
        }

        return products;

    } catch (SQLException e) {
        throw new RuntimeException(
                "Không thể lấy sản phẩm cho combo.",
                e
        );
    }
}
    public FnbProduct findProductById(
        Connection conn,
        int productId)
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
            status,
            last_update
        FROM dbo.FNB_PRODUCTS
        WHERE id = ?
        """;

    try (PreparedStatement ps = conn.prepareStatement(sql)) {

        ps.setInt(1, productId);

        try (ResultSet rs = ps.executeQuery()) {

            if (!rs.next()) {
                return null;
            }

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
    }
}
}
