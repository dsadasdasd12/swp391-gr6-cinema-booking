package service;

import dao.FnbAdminDAO;
import dto.FnbCategoryDTO;
import dto.FnbCategoryFormDTO;
import dto.FnbProductDTO;
import dto.FnbProductFormDTO;
import model.FnbCategory;
import model.FnbProduct;
import util.DBContext;

import java.sql.Connection;
import java.sql.SQLException;
import java.util.List;

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

    FnbProduct product =
            fnbAdminDAO.findProductById(productId);

    if (product == null) {
        throw new IllegalArgumentException(
                "Không tìm thấy sản phẩm."
        );
    }

    if (allowed
            && !"ACTIVE".equalsIgnoreCase(product.getStatus())) {

        throw new IllegalArgumentException(
                "Không thể cho phép bán sản phẩm đang ngừng hoạt động."
        );
    }

    return fnbAdminDAO.updateAllowedToSell(
            productId,
            allowed
    );
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
        String description =
                normalizeNullable(dto.getDescription());
        String imageUrl =
                normalizeNullable(dto.getImageUrl());
        String productType =
                normalizeProductType(dto.getProductType());

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

        String normalizedStatus =
                normalizeStatus(status);

        try (Connection conn =
                     DBContext.getInstance().getConnection()) {

            conn.setAutoCommit(false);

            try {
                boolean updated =
                        fnbAdminDAO.updateProductStatus(
                                conn,
                                productId,
                                normalizedStatus
                        );

                if (!updated) {
                    conn.rollback();
                    return false;
                }

                if ("INACTIVE".equals(normalizedStatus)) {
                    List<Integer> comboIds =
                            fnbAdminDAO
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

        String normalizedStatus =
                normalizeStatus(status);

        try (Connection conn =
                     DBContext.getInstance().getConnection()) {

            conn.setAutoCommit(false);

            try {
                boolean updated =
                        fnbAdminDAO.updateCategoryStatus(
                                conn,
                                categoryId,
                                normalizedStatus
                        );

                if (!updated) {
                    conn.rollback();
                    return false;
                }

                if ("INACTIVE".equals(normalizedStatus)) {
                    List<Integer> productIds =
                            fnbAdminDAO.findProductIdsByCategory(
                                    conn,
                                    categoryId
                            );

                    for (Integer productId : productIds) {
                        fnbAdminDAO.updateProductStatus(
                                conn,
                                productId,
                                "INACTIVE"
                        );

                        List<Integer> comboIds =
                                fnbAdminDAO
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

        String normalized =
                value.trim().toUpperCase();

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

        String normalized =
                value.trim().toUpperCase();

        if (!"ITEM".equals(normalized)
                && !"COMBO".equals(normalized)) {

            throw new IllegalArgumentException(
                    "Loại sản phẩm không hợp lệ."
            );
        }

        return normalized;
    }
}