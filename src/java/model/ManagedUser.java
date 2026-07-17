package model;

/**
 * User + trường hiển thị cho CMS (nhân viên / khách hàng).
 */
public class ManagedUser extends User {

    private int roleId;
    private int branchId;
    private String branchName;
    private String status;
    /**
     * Chuỗi hiển thị JSP (fmt:parseDate), không có cột last_login trong schema.
     */
    private String lastLogin;
    /**
     * Chuỗi ngày tạo cho trang khách hàng (tránh fmt:parseDate trên
     * LocalDateTime).
     */
    private String createdAtLabel;

    public int getRoleId() {
        return roleId;
    }

    public void setRoleId(int roleId) {
        this.roleId = roleId;
    }

    public int getBranchId() {
        return branchId;
    }

    public void setBranchId(int branchId) {
        this.branchId = branchId;
    }

    public String getBranchName() {
        return branchName;
    }

    public void setBranchName(String branchName) {
        this.branchName = branchName;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public String getLastLogin() {
        return lastLogin;
    }

    public void setLastLogin(String lastLogin) {
        this.lastLogin = lastLogin;
    }

    public String getCreatedAtLabel() {
        return createdAtLabel;
    }

    public void setCreatedAtLabel(String createdAtLabel) {
        this.createdAtLabel = createdAtLabel;
    }
}
