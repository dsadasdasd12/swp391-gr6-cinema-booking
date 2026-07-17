package service;

import dao.ManagerFnbDAO;
import dto.ManagerFnbComboDTO;
import dto.ManagerFnbItemDTO;

import java.sql.SQLException;
import java.util.List;

public class ManagerFnbService {

    private final ManagerFnbDAO managerFnbDAO;

    public ManagerFnbService() {
        this.managerFnbDAO = new ManagerFnbDAO();
    }

    public ManagerFnbService(ManagerFnbDAO managerFnbDAO) {
        this.managerFnbDAO = managerFnbDAO;
    }

    /**
     * Lấy chi nhánh mà Manager đang được phân công quản lý.
     */
    public int getManagerBranchId(int managerId) {
        if (managerId <= 0) {
            throw new IllegalArgumentException(
                    "Manager không hợp lệ."
            );
        }

        try {
            Integer branchId
                    = managerFnbDAO.findBranchIdByManagerId(managerId);

            if (branchId == null) {
                throw new IllegalStateException(
                        "Tài khoản Manager chưa được phân công chi nhánh."
                );
            }

            return branchId;

        } catch (SQLException exception) {
            throw new IllegalStateException(
                    "Không thể xác định chi nhánh của Manager.",
                    exception
            );
        }
    }

    /**
     * Lấy tên chi nhánh để hiển thị trên giao diện.
     */
    public String getBranchName(int branchId) {
        validateBranchId(branchId);

        try {
            String branchName
                    = managerFnbDAO.findBranchNameById(branchId);

            if (branchName == null || branchName.isBlank()) {
                return "Chi nhánh #" + branchId;
            }

            return branchName;

        } catch (SQLException exception) {
            throw new IllegalStateException(
                    "Không thể đọc thông tin chi nhánh.",
                    exception
            );
        }
    }

    /**
     * Lấy toàn bộ món lẻ tại chi nhánh.
     *
     * Dữ liệu gồm: - thông tin món do Admin quản lý - tồn kho theo chi nhánh -
     * trạng thái bật/tắt bán theo chi nhánh
     */
    public List<ManagerFnbItemDTO> getItemsByBranch(int branchId) {
        validateBranchId(branchId);

        try {
            return managerFnbDAO.findItemsByBranch(branchId);

        } catch (SQLException exception) {
            throw new IllegalStateException(
                    "Không thể tải danh sách món F&B.",
                    exception
            );
        }
    }

    /**
     * Lấy toàn bộ combo tại chi nhánh.
     *
     * Số lượng combo có thể bán được tính từ tồn kho của các món thành phần.
     */
    public List<ManagerFnbComboDTO> getCombosByBranch(int branchId) {
        validateBranchId(branchId);

        try {
            return managerFnbDAO.findCombosByBranch(branchId);

        } catch (SQLException exception) {
            throw new IllegalStateException(
                    "Không thể tải danh sách combo F&B.",
                    exception
            );
        }
    }

    /**
     * Manager cập nhật số lượng tồn kho của món lẻ.
     *
     * Manager không được cập nhật tồn kho trực tiếp cho combo. Số lượng combo
     * được tính tự động từ tồn kho món thành phần.
     */
    public void updateStock(
            int branchId,
            int productId,
            int stockQuantity
    ) {
        validateBranchId(branchId);
        validateProductId(productId);

        if (stockQuantity < 0) {
            throw new IllegalArgumentException(
                    "Số lượng tồn kho không được nhỏ hơn 0."
            );
        }

        try {
            if (!managerFnbDAO.isItemProduct(productId)) {
                throw new IllegalArgumentException(
                        "Sản phẩm được chọn không phải món lẻ."
                );
            }

            managerFnbDAO.upsertItemInventory(
                    branchId,
                    productId,
                    stockQuantity
            );

        } catch (SQLException exception) {
            throw new IllegalStateException(
                    "Không thể cập nhật tồn kho món F&B.",
                    exception
            );
        }
    }

    /**
     * Manager bật hoặc tắt bán món tại chi nhánh.
     *
     * Quy tắc: - Manager chỉ được thay đổi trạng thái tại chi nhánh. - Manager
     * không thay đổi trạng thái hệ thống của sản phẩm. - Nếu Admin đã khóa sản
     * phẩm hoặc danh mục, Manager không được bật bán. - Khi Manager tắt một
     * món, các combo chứa món đó tại cùng chi nhánh cũng bị tắt. - Khi bật lại
     * món, combo không tự động bật lại.
     */
    public void setItemEnabledAtBranch(
            int branchId,
            int productId,
            boolean enabled
    ) {
        validateBranchId(branchId);
        validateProductId(productId);

        try {
            if (!managerFnbDAO.isItemProduct(productId)) {
                throw new IllegalArgumentException(
                        "Sản phẩm được chọn không phải món lẻ."
                );
            }

            if (enabled) {
                boolean canSell
                        = managerFnbDAO.canItemBeSoldBySystem(productId);

                if (!canSell) {
                    throw new IllegalStateException(
                            "Không thể bật bán món này vì món hoặc danh mục đang bị Admin khóa."
                    );
                }
            }

            managerFnbDAO.setItemEnabledAtBranch(
                    branchId,
                    productId,
                    enabled
            );

            /*
             * Nếu Manager tắt món tại chi nhánh,
             * tắt tất cả combo chứa món đó tại cùng chi nhánh.
             */
            if (!enabled) {
                managerFnbDAO.disableCombosContainingItem(
                        branchId,
                        productId
                );
            }

        } catch (SQLException exception) {
            throw new IllegalStateException(
                    "Không thể cập nhật trạng thái bán món.",
                    exception
            );
        }
    }

    /**
     * Manager bật hoặc tắt bán combo tại chi nhánh.
     *
     * Khi bật combo cần kiểm tra: - Combo tồn tại. - Combo ACTIVE. - Combo được
     * Admin cho phép bán. - Combo có món thành phần. - Tất cả món thành phần
     * ACTIVE. - Tất cả món được Admin cho phép bán. - Danh mục của món ACTIVE.
     * - Tất cả món được bật bán tại chi nhánh. - Tồn kho đủ tạo ít nhất một
     * combo.
     */
    public void setComboEnabledAtBranch(
            int branchId,
            int comboId,
            boolean enabled
    ) {
        validateBranchId(branchId);
        validateComboId(comboId);

        try {
            if (!managerFnbDAO.comboExists(comboId)) {
                throw new IllegalArgumentException(
                        "Combo không tồn tại."
                );
            }

            if (enabled) {
                String validationError
                        = managerFnbDAO.findComboEnableValidationError(
                                branchId,
                                comboId
                        );

                if (validationError != null) {
                    throw new IllegalStateException(
                            validationError
                    );
                }
            }

            managerFnbDAO.setComboEnabledAtBranch(
                    branchId,
                    comboId,
                    enabled
            );

        } catch (SQLException exception) {
            throw new IllegalStateException(
                    "Không thể cập nhật trạng thái bán combo.",
                    exception
            );
        }
    }

    /**
     * Kiểm tra món có đang được bật bán tại chi nhánh hay không.
     */
    public boolean isItemEnabledAtBranch(
            int branchId,
            int productId
    ) {
        validateBranchId(branchId);
        validateProductId(productId);

        try {
            return managerFnbDAO.isItemEnabledAtBranch(
                    branchId,
                    productId
            );

        } catch (SQLException exception) {
            throw new IllegalStateException(
                    "Không thể kiểm tra trạng thái bán món.",
                    exception
            );
        }
    }

    /**
     * Kiểm tra combo có đang được bật bán tại chi nhánh hay không.
     */
    public boolean isComboEnabledAtBranch(
            int branchId,
            int comboId
    ) {
        validateBranchId(branchId);
        validateComboId(comboId);

        try {
            return managerFnbDAO.isComboEnabledAtBranch(
                    branchId,
                    comboId
            );

        } catch (SQLException exception) {
            throw new IllegalStateException(
                    "Không thể kiểm tra trạng thái bán combo.",
                    exception
            );
        }
    }

    /**
     * Lấy số lượng tồn kho hiện tại của món tại chi nhánh.
     */
    public int getItemStock(
            int branchId,
            int productId
    ) {
        validateBranchId(branchId);
        validateProductId(productId);

        try {
            return managerFnbDAO.getItemStock(
                    branchId,
                    productId
            );

        } catch (SQLException exception) {
            throw new IllegalStateException(
                    "Không thể đọc số lượng tồn kho món.",
                    exception
            );
        }
    }

    /**
     * Lấy số lượng combo có thể tạo được từ tồn kho.
     */
    public int getAvailableComboQuantity(
            int branchId,
            int comboId
    ) {
        validateBranchId(branchId);
        validateComboId(comboId);

        try {
            return managerFnbDAO.calculateAvailableComboQuantity(
                    branchId,
                    comboId
            );

        } catch (SQLException exception) {
            throw new IllegalStateException(
                    "Không thể tính số lượng combo có thể bán.",
                    exception
            );
        }
    }

    private void validateBranchId(int branchId) {
        if (branchId <= 0) {
            throw new IllegalArgumentException(
                    "Chi nhánh không hợp lệ."
            );
        }
    }

    private void validateProductId(int productId) {
        if (productId <= 0) {
            throw new IllegalArgumentException(
                    "Món F&B không hợp lệ."
            );
        }
    }

    private void validateComboId(int comboId) {
        if (comboId <= 0) {
            throw new IllegalArgumentException(
                    "Combo F&B không hợp lệ."
            );
        }
    }

    public void changeStock(
            int branchId,
            int productId,
            int stockChange
    ) {
        validateBranchId(branchId);
        validateProductId(productId);

        try {

            if (!managerFnbDAO.isItemProduct(productId)) {
                throw new IllegalArgumentException(
                        "Sản phẩm được chọn không phải món lẻ."
                );
            }

            int currentStock
                    = managerFnbDAO.getItemStock(
                            branchId,
                            productId
                    );

            int newStock
                    = currentStock + stockChange;

            if (newStock < 0) {
                throw new IllegalArgumentException(
                        "Tồn kho không đủ."
                );
            }

            managerFnbDAO.upsertItemInventory(
                    branchId,
                    productId,
                    newStock
            );

        } catch (SQLException e) {

            throw new IllegalStateException(
                    "Không thể cập nhật tồn kho.",
                    e
            );

        }
    }
}
