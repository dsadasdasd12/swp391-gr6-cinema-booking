package model;

/**
 * Model cho module chức năng hệ thống.
 * Map với bảng dbo.MODULES.
 *
 * @author LONG
 */
public class Module {

    private int id;
    private String moduleKey;
    private String moduleName;
    private String description;
    private int sortOrder;

    public Module() {}

    public Module(int id, String moduleKey, String moduleName, String description, int sortOrder) {
        this.id = id;
        this.moduleKey = moduleKey;
        this.moduleName = moduleName;
        this.description = description;
        this.sortOrder = sortOrder;
    }

    // ── Getters / Setters ────────────────────────────────────

    public int getId() { return id; }
    public void setId(int id) { this.id = id; }

    public String getModuleKey() { return moduleKey; }
    public void setModuleKey(String moduleKey) { this.moduleKey = moduleKey; }

    public String getKey() { return moduleKey; }
    public void setKey(String key) { this.moduleKey = key; }

    public String getModuleName() { return moduleName; }
    public void setModuleName(String moduleName) { this.moduleName = moduleName; }

    public String getName() { return moduleName; }
    public void setName(String name) { this.moduleName = name; }

    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }

    public int getSortOrder() { return sortOrder; }
    public void setSortOrder(int sortOrder) { this.sortOrder = sortOrder; }
}
