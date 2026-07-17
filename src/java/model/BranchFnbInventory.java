package model;

import java.time.LocalDateTime;

public class BranchFnbInventory {

    private int branchId;
    private int productId;
    private int stockQuantity;
    private boolean enabledAtBranch;
    private LocalDateTime lastUpdate;

    public BranchFnbInventory() {
    }

    public BranchFnbInventory(
            int branchId,
            int productId,
            int stockQuantity,
            boolean enabledAtBranch,
            LocalDateTime lastUpdate
    ) {
        this.branchId = branchId;
        this.productId = productId;
        this.stockQuantity = stockQuantity;
        this.enabledAtBranch = enabledAtBranch;
        this.lastUpdate = lastUpdate;
    }

    public int getBranchId() {
        return branchId;
    }

    public void setBranchId(int branchId) {
        this.branchId = branchId;
    }

    public int getProductId() {
        return productId;
    }

    public void setProductId(int productId) {
        this.productId = productId;
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

    public LocalDateTime getLastUpdate() {
        return lastUpdate;
    }

    public void setLastUpdate(LocalDateTime lastUpdate) {
        this.lastUpdate = lastUpdate;
    }

    @Override
    public String toString() {
        return "BranchFnbInventory{"
                + "branchId=" + branchId
                + ", productId=" + productId
                + ", stockQuantity=" + stockQuantity
                + ", enabledAtBranch=" + enabledAtBranch
                + ", lastUpdate=" + lastUpdate
                + '}';
    }
}
