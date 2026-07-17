package dto;

import java.math.BigDecimal;

public class ManagerFnbItemDTO {

    private int productId;
    private String productName;
    private String imageUrl;

    private int categoryId;
    private String categoryName;

    private BigDecimal sellingPrice;

    // Trạng thái hệ thống (Admin)
    private String status;
    private boolean allowedToSell;

    // Trạng thái tại chi nhánh
    private int stockQuantity;
    private boolean enabledAtBranch;

    public ManagerFnbItemDTO() {
    }

    public int getProductId() {
        return productId;
    }

    public void setProductId(int productId) {
        this.productId = productId;
    }

    public String getProductName() {
        return productName;
    }

    public void setProductName(String productName) {
        this.productName = productName;
    }

    public String getImageUrl() {
        return imageUrl;
    }

    public void setImageUrl(String imageUrl) {
        this.imageUrl = imageUrl;
    }

    public int getCategoryId() {
        return categoryId;
    }

    public void setCategoryId(int categoryId) {
        this.categoryId = categoryId;
    }

    public String getCategoryName() {
        return categoryName;
    }

    public void setCategoryName(String categoryName) {
        this.categoryName = categoryName;
    }

    public BigDecimal getSellingPrice() {
        return sellingPrice;
    }

    public void setSellingPrice(BigDecimal sellingPrice) {
        this.sellingPrice = sellingPrice;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public boolean isAllowedToSell() {
        return allowedToSell;
    }

    public void setAllowedToSell(boolean allowedToSell) {
        this.allowedToSell = allowedToSell;
    }

    public int getStockQuantity() {
        return stockQuantity;
    }

    public void setStockQuantity(int stockQuantity) {
        this.stockQuantity = stockQuantity;
    }

    public boolean isEnabledAtBranch() {
        return enabledAtBranch;
    }

    public void setEnabledAtBranch(boolean enabledAtBranch) {
        this.enabledAtBranch = enabledAtBranch;
    }
}
