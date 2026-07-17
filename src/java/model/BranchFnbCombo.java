package model;

import java.time.LocalDateTime;

public class BranchFnbCombo {

    private int branchId;
    private int comboId;
    private boolean enabledAtBranch;
    private LocalDateTime lastUpdate;

    public BranchFnbCombo() {
    }

    public BranchFnbCombo(
            int branchId,
            int comboId,
            boolean enabledAtBranch,
            LocalDateTime lastUpdate
    ) {
        this.branchId = branchId;
        this.comboId = comboId;
        this.enabledAtBranch = enabledAtBranch;
        this.lastUpdate = lastUpdate;
    }

    public int getBranchId() {
        return branchId;
    }

    public void setBranchId(int branchId) {
        this.branchId = branchId;
    }

    public int getComboId() {
        return comboId;
    }

    public void setComboId(int comboId) {
        this.comboId = comboId;
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
        return "BranchFnbCombo{"
                + "branchId=" + branchId
                + ", comboId=" + comboId
                + ", enabledAtBranch=" + enabledAtBranch
                + ", lastUpdate=" + lastUpdate
                + '}';
    }
}
