package dto;

import java.math.BigDecimal;

public class ManagerFnbComboDTO {

    private int comboId;
    private String comboName;
    private String imageUrl;

    private BigDecimal sellingPrice;

    // Admin quản lý
    private String status;
    private boolean allowedToSell;

    // Hiển thị
    private String itemSummary;

    // Có đủ nguyên liệu không
    private int availableQuantity;

    // Manager bật/tắt bán
    private boolean enabledAtBranch;

    public ManagerFnbComboDTO() {
    }

    public int getComboId() {
        return comboId;
    }

    public void setComboId(int comboId) {
        this.comboId = comboId;
    }

    public String getComboName() {
        return comboName;
    }

    public void setComboName(String comboName) {
        this.comboName = comboName;
    }

    public String getImageUrl() {
        return imageUrl;
    }

    public void setImageUrl(String imageUrl) {
        this.imageUrl = imageUrl;
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

    public String getItemSummary() {
        return itemSummary;
    }

    public void setItemSummary(String itemSummary) {
        this.itemSummary = itemSummary;
    }

    public int getAvailableQuantity() {
        return availableQuantity;
    }

    public void setAvailableQuantity(int availableQuantity) {
        this.availableQuantity = availableQuantity;
    }

    public boolean isEnabledAtBranch() {
        return enabledAtBranch;
    }

    public void setEnabledAtBranch(boolean enabledAtBranch) {
        this.enabledAtBranch = enabledAtBranch;
    }
}
