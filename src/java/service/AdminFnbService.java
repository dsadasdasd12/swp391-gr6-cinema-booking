package service;

import dao.FnbAdminDAO;
import dto.FnbCategoryDTO;
import dto.FnbCategoryFormDTO;
import dto.FnbComboDTO;
import dto.FnbComboFormDTO;
import dto.FnbComboItemDTO;
import dto.FnbProductDTO;
import dto.FnbProductFormDTO;
import java.math.BigDecimal;
import model.FnbCategory;
import model.FnbProduct;
import util.DBContext;
import java.sql.Connection;
import java.sql.SQLException;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class AdminFnbService {

    private final FnbAdminDAO fnbAdminDAO = new FnbAdminDAO();

    public List<FnbCategoryDTO> getAllCategories() {
        return fnbAdminDAO.findAllCategories();
    }

    public List<FnbProductDTO> getProductsByCategory(int categoryId) {
        if (categoryId <= 0) {
            throw new IllegalArgumentException("Danh mục không hợp lệ.");
        }

        return fnbAdminDAO.findProductsByCategory(categoryId);
    }

    public boolean changeAllowedToSell(
            int productId,
            boolean allowed) {

        if (productId <= 0) {
            throw new IllegalArgumentException(
                    "Sản phẩm không hợp lệ."
            );
        }

        try (Connection conn
                = DBContext.getInstance().getConnection()) {

            conn.setAutoCommit(false);

            try {
                FnbProduct product
                        = fnbAdminDAO.findProductById(
                                conn,
                                productId
                        );

                if (product == null) {
                    throw new IllegalArgumentException(
                            "Không tìm thấy sản phẩm."
                    );
                }

                if (allowed
                        && !"ACTIVE".equalsIgnoreCase(
                                product.getStatus()
                        )) {

                    throw new IllegalArgumentException(
                            "Không thể cho phép bán sản phẩm "
                            + "đang ngừng hoạt động."
                    );
                }

                boolean updated
                        = fnbAdminDAO.updateAllowedToSell(
                                conn,
                                productId,
                                allowed
                        );

                if (!updated) {
                    conn.rollback();
                    return false;
                }

                /*
             * Khi ITEM bị tắt bán:
             * - Combo vẫn ACTIVE
             * - Nhưng combo không được phép bán
                 */
                if (!allowed) {

                    List<Integer> comboIds
                            = fnbAdminDAO
                                    .findComboIdsContainingProduct(
                                            conn,
                                            productId
                                    );

                    fnbAdminDAO.disableComboSales(
                            conn,
                            comboIds
                    );
                }

                /*
             * Khi ITEM được bật bán trở lại:
             * Không tự bật lại các combo.
             * Admin phải kiểm tra và bật combo thủ công.
                 */
                conn.commit();
                return true;

            } catch (Exception e) {

                conn.rollback();

                if (e instanceof IllegalArgumentException) {
                    throw (IllegalArgumentException) e;
                }

                throw new RuntimeException(
                        "Không thể cập nhật quyền bán sản phẩm.",
                        e
                );

            } finally {
                conn.setAutoCommit(true);
            }

        } catch (SQLException e) {
            throw new RuntimeException(
                    "Lỗi kết nối cơ sở dữ liệu.",
                    e
            );
        }
    }

    public boolean saveCategory(FnbCategoryFormDTO dto) {
        if (dto == null) {
            throw new IllegalArgumentException(
                    "Dữ liệu danh mục không hợp lệ."
            );
        }

        String name = normalize(dto.getName());
        String description = normalizeNullable(dto.getDescription());

        if (name == null) {
            throw new IllegalArgumentException(
                    "Tên danh mục không được để trống."
            );
        }

        if (name.length() > 100) {
            throw new IllegalArgumentException(
                    "Tên danh mục không được vượt quá 100 ký tự."
            );
        }

        if (fnbAdminDAO.existsCategoryNameExceptId(
                name,
                dto.getId()
        )) {
            throw new IllegalArgumentException(
                    "Tên danh mục đã tồn tại."
            );
        }

        FnbCategory category = new FnbCategory();
        category.setName(name);
        category.setDescription(description);

        if (dto.getId() == null || dto.getId() <= 0) {
            return fnbAdminDAO.insertCategory(category);
        }

        category.setId(dto.getId());

        return fnbAdminDAO.updateCategory(category);
    }

    public boolean saveProduct(FnbProductFormDTO dto) {
        if (dto == null) {
            throw new IllegalArgumentException(
                    "Dữ liệu sản phẩm không hợp lệ."
            );
        }

        if (dto.getCategoryId() <= 0) {
            throw new IllegalArgumentException(
                    "Vui lòng chọn danh mục."
            );
        }

        String name = normalize(dto.getName());
        String description
                = normalizeNullable(dto.getDescription());
        String imageUrl
                = normalizeNullable(dto.getImageUrl());
        String productType
                = normalizeProductType(dto.getProductType());

        if (name == null) {
            throw new IllegalArgumentException(
                    "Tên sản phẩm không được để trống."
            );
        }

        if (dto.getSellingPrice() == null
                || dto.getSellingPrice().signum() < 0) {

            throw new IllegalArgumentException(
                    "Giá bán phải lớn hơn hoặc bằng 0."
            );
        }

        FnbProduct product = new FnbProduct();

        product.setCategoryId(dto.getCategoryId());
        product.setName(name);
        product.setDescription(description);
        product.setProductType(productType);
        product.setSellingPrice(dto.getSellingPrice());
        product.setImageUrl(imageUrl);
        product.setAllowedToSell(dto.isAllowedToSell());

        if (dto.getId() == null || dto.getId() <= 0) {
            return fnbAdminDAO.insertProduct(product);
        }

        product.setId(dto.getId());

        return fnbAdminDAO.updateProduct(product);
    }

    public boolean changeProductStatus(
            int productId,
            String status) {

        if (productId <= 0) {
            throw new IllegalArgumentException(
                    "Sản phẩm không hợp lệ."
            );
        }

        String normalizedStatus
                = normalizeStatus(status);

        try (Connection conn
                = DBContext.getInstance().getConnection()) {

            conn.setAutoCommit(false);

            try {
                boolean updated
                        = fnbAdminDAO.updateProductStatus(
                                conn,
                                productId,
                                normalizedStatus
                        );

                if (!updated) {
                    conn.rollback();
                    return false;
                }

                if ("INACTIVE".equals(normalizedStatus)) {
                    List<Integer> comboIds
                            = fnbAdminDAO
                                    .findComboIdsContainingProduct(
                                            conn,
                                            productId
                                    );

                    fnbAdminDAO.deactivateCombos(
                            conn,
                            comboIds
                    );
                }

                conn.commit();
                return true;

            } catch (Exception e) {
                conn.rollback();

                throw new RuntimeException(
                        "Không thể cập nhật trạng thái sản phẩm.",
                        e
                );
            } finally {
                conn.setAutoCommit(true);
            }

        } catch (SQLException e) {
            throw new RuntimeException(
                    "Lỗi kết nối cơ sở dữ liệu.",
                    e
            );
        }
    }

    public boolean changeCategoryStatus(
            int categoryId,
            String status) {

        if (categoryId <= 0) {
            throw new IllegalArgumentException(
                    "Danh mục không hợp lệ."
            );
        }

        String normalizedStatus
                = normalizeStatus(status);

        try (Connection conn
                = DBContext.getInstance().getConnection()) {

            conn.setAutoCommit(false);

            try {
                boolean updated
                        = fnbAdminDAO.updateCategoryStatus(
                                conn,
                                categoryId,
                                normalizedStatus
                        );

                if (!updated) {
                    conn.rollback();
                    return false;
                }

                if ("INACTIVE".equals(normalizedStatus)) {
                    List<Integer> productIds
                            = fnbAdminDAO.findProductIdsByCategory(
                                    conn,
                                    categoryId
                            );

                    for (Integer productId : productIds) {
                        fnbAdminDAO.updateProductStatus(
                                conn,
                                productId,
                                "INACTIVE"
                        );

                        List<Integer> comboIds
                                = fnbAdminDAO
                                        .findComboIdsContainingProduct(
                                                conn,
                                                productId
                                        );

                        fnbAdminDAO.deactivateCombos(
                                conn,
                                comboIds
                        );
                    }
                }

                conn.commit();
                return true;

            } catch (Exception e) {
                conn.rollback();

                throw new RuntimeException(
                        "Không thể cập nhật trạng thái danh mục.",
                        e
                );
            } finally {
                conn.setAutoCommit(true);
            }

        } catch (SQLException e) {
            throw new RuntimeException(
                    "Lỗi kết nối cơ sở dữ liệu.",
                    e
            );
        }
    }

    private String normalize(String value) {
        if (value == null || value.isBlank()) {
            return null;
        }

        return value.trim();
    }

    private String normalizeNullable(String value) {
        return value == null || value.isBlank()
                ? null
                : value.trim();
    }

    private String normalizeStatus(String value) {
        if (value == null) {
            throw new IllegalArgumentException(
                    "Trạng thái không hợp lệ."
            );
        }

        String normalized
                = value.trim().toUpperCase();

        if (!"ACTIVE".equals(normalized)
                && !"INACTIVE".equals(normalized)) {

            throw new IllegalArgumentException(
                    "Trạng thái không hợp lệ."
            );
        }

        return normalized;
    }

    private String normalizeProductType(String value) {
        if (value == null) {
            throw new IllegalArgumentException(
                    "Loại sản phẩm không hợp lệ."
            );
        }

        String normalized
                = value.trim().toUpperCase();

        if (!"ITEM".equals(normalized)
                && !"COMBO".equals(normalized)) {

            throw new IllegalArgumentException(
                    "Loại sản phẩm không hợp lệ."
            );
        }

        return normalized;
    }

    public List<FnbComboDTO> getAllCombos() {
        return fnbAdminDAO.findAllCombos();
    }

    public List<FnbProductDTO> getActiveItemsForCombo() {
        return fnbAdminDAO.findActiveItemsForCombo();
    }

    public boolean saveCombo(FnbComboFormDTO dto) {

        validateCombo(dto);

        try (Connection conn
                = DBContext.getInstance().getConnection()) {

            conn.setAutoCommit(false);

            try {
                if (fnbAdminDAO.existsComboNameExceptId(
                        conn,
                        dto.getName(),
                        dto.getId()
                )) {
                    throw new IllegalArgumentException(
                            "Tên combo đã tồn tại."
                    );
                }

                BigDecimal originalPrice
                        = BigDecimal.ZERO;

                Set<Integer> productIds
                        = new HashSet<>();

                for (FnbComboItemDTO item : dto.getItems()) {

                    if (!productIds.add(
                            item.getProductId()
                    )) {
                        throw new IllegalArgumentException(
                                "Sản phẩm bị lặp trong combo."
                        );
                    }

                    FnbProduct product
                            = fnbAdminDAO.findProductById(
                                    conn,
                                    item.getProductId()
                            );

                    if (product == null) {
                        throw new IllegalArgumentException(
                                "Không tìm thấy sản phẩm."
                        );
                    }

                    if (!"ITEM".equalsIgnoreCase(
                            product.getProductType()
                    )) {
                        throw new IllegalArgumentException(
                                "Combo chỉ được chứa sản phẩm ITEM."
                        );
                    }

                    if (!"ACTIVE".equalsIgnoreCase(
                            product.getStatus()
                    )) {
                        throw new IllegalArgumentException(
                                product.getName()
                                + " đang ngừng hoạt động."
                        );
                    }

                    originalPrice = originalPrice.add(
                            product.getSellingPrice().multiply(
                                    BigDecimal.valueOf(
                                            item.getQuantity()
                                    )
                            )
                    );
                }

                if (dto.getSellingPrice()
                        .compareTo(originalPrice) > 0) {

                    throw new IllegalArgumentException(
                            "Giá combo không được lớn hơn "
                            + "tổng giá sản phẩm."
                    );
                }

                int comboId;

                if (dto.getId() == null
                        || dto.getId() <= 0) {

                    comboId
                            = fnbAdminDAO.insertCombo(
                                    conn,
                                    dto
                            );

                    if (comboId <= 0) {
                        throw new SQLException(
                                "Không thể tạo combo."
                        );
                    }

                } else {

                    comboId = dto.getId();

                    boolean updated
                            = fnbAdminDAO.updateCombo(
                                    conn,
                                    dto
                            );

                    if (!updated) {
                        throw new SQLException(
                                "Không tìm thấy combo."
                        );
                    }

                    fnbAdminDAO.deleteComboItems(
                            conn,
                            comboId
                    );
                }

                fnbAdminDAO.insertComboItems(
                        conn,
                        comboId,
                        dto.getItems()
                );

                conn.commit();
                return true;

            } catch (Exception e) {

                conn.rollback();

                if (e instanceof IllegalArgumentException) {
                    throw (IllegalArgumentException) e;
                }

                throw new RuntimeException(
                        "Không thể lưu combo.",
                        e
                );

            } finally {
                conn.setAutoCommit(true);
            }

        } catch (SQLException e) {
            throw new RuntimeException(
                    "Lỗi kết nối cơ sở dữ liệu.",
                    e
            );
        }
    }

    public boolean changeComboStatus(
            int comboId,
            String status) {

        if (comboId <= 0) {
            throw new IllegalArgumentException(
                    "Combo không hợp lệ."
            );
        }

        String normalizedStatus
                = normalizeStatus(status);

        return fnbAdminDAO.updateComboStatus(
                comboId,
                normalizedStatus
        );
    }

    public boolean changeComboAllowedToSell(
            int comboId,
            boolean allowed) {

        if (comboId <= 0) {
            throw new IllegalArgumentException(
                    "Combo không hợp lệ."
            );
        }

        try (Connection conn
                = DBContext.getInstance().getConnection()) {

            conn.setAutoCommit(false);

            try {
                FnbComboDTO combo
                        = fnbAdminDAO.findComboById(
                                conn,
                                comboId
                        );

                if (combo == null) {
                    throw new IllegalArgumentException(
                            "Không tìm thấy combo."
                    );
                }

                if (allowed
                        && !"ACTIVE".equalsIgnoreCase(
                                combo.getStatus()
                        )) {

                    throw new IllegalArgumentException(
                            "Không thể cho phép bán combo "
                            + "đang ngừng hoạt động."
                    );
                }

                if (allowed) {

                    List<String> unavailableItems
                            = fnbAdminDAO
                                    .findUnavailableComboItemNames(
                                            conn,
                                            comboId
                                    );

                    if (!unavailableItems.isEmpty()) {

                        throw new IllegalArgumentException(
                                "Không thể cho phép bán combo. "
                                + "Các sản phẩm sau đang ngừng hoạt động "
                                + "hoặc không được phép bán: "
                                + String.join(", ", unavailableItems)
                                + "."
                        );
                    }
                }

                boolean updated
                        = fnbAdminDAO.updateComboAllowedToSell(
                                conn,
                                comboId,
                                allowed
                        );

                conn.commit();
                return updated;

            } catch (Exception e) {

                conn.rollback();

                if (e instanceof IllegalArgumentException) {
                    throw (IllegalArgumentException) e;
                }

                throw new RuntimeException(
                        "Không thể cập nhật quyền bán combo.",
                        e
                );

            } finally {
                conn.setAutoCommit(true);
            }

        } catch (SQLException e) {
            throw new RuntimeException(
                    "Lỗi kết nối cơ sở dữ liệu.",
                    e
            );
        }
    }

    private void validateCombo(
            FnbComboFormDTO dto) {

        if (dto == null) {
            throw new IllegalArgumentException(
                    "Dữ liệu combo không hợp lệ."
            );
        }

        String name = normalize(dto.getName());

        if (name == null) {
            throw new IllegalArgumentException(
                    "Tên combo không được để trống."
            );
        }

        if (name.length() > 150) {
            throw new IllegalArgumentException(
                    "Tên combo không được vượt quá 150 ký tự."
            );
        }

        if (dto.getSellingPrice() == null
                || dto.getSellingPrice().signum() < 0) {

            throw new IllegalArgumentException(
                    "Giá combo không hợp lệ."
            );
        }

        if (dto.getItems() == null
                || dto.getItems().isEmpty()) {

            throw new IllegalArgumentException(
                    "Combo phải có ít nhất một sản phẩm."
            );
        }

        for (FnbComboItemDTO item : dto.getItems()) {

            if (item.getProductId() <= 0) {
                throw new IllegalArgumentException(
                        "Sản phẩm trong combo không hợp lệ."
                );
            }

            if (item.getQuantity() <= 0
                    || item.getQuantity() > 99) {

                throw new IllegalArgumentException(
                        "Số lượng sản phẩm phải từ 1 đến 99."
                );
            }
        }

        dto.setName(name);
        dto.setDescription(
                normalizeNullable(dto.getDescription())
        );
        dto.setImageUrl(
                normalizeNullable(dto.getImageUrl())
        );
    }

}
