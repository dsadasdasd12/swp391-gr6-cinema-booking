package model;

import java.time.LocalDateTime;

/**
 * Model cho vai trò hệ thống.
 * Map với bảng dbo.ROLES.
 *
 * @author LONG
 */
public class Role {

    private int id;
    private String roleName;
    private String description;
    private String scope;
    private boolean system; // is_system — vai trò mặc định không xóa được
    private LocalDateTime createdAt;

    public Role() {}

    public Role(int id, String roleName, String description, String scope, boolean system) {
        this.id = id;
        this.roleName = roleName;
        this.description = description;
        this.scope = scope;
        this.system = system;
    }

    // ── Getters / Setters ────────────────────────────────────

    public int getId() { return id; }
    public void setId(int id) { this.id = id; }

    public String getRoleName() { return roleName; }
    public void setRoleName(String roleName) { this.roleName = roleName; }

    public String getName() { return roleName; }
    public void setName(String name) { this.roleName = name; }

    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }

    public String getScope() { return scope; }
    public void setScope(String scope) { this.scope = scope; }

    public boolean isSystem() { return system; }
    public void setSystem(boolean system) { this.system = system; }

    public LocalDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }
}
