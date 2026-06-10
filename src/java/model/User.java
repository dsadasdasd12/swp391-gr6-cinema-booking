package model;
import java.time.LocalDateTime;

public class User {
    private int id;
    private String fullName;
    private String email;
    private String passwordHash;
    private String googleId;
    private String phone;
    private String role;
    private boolean active;
    private boolean emailVerified;
    private LocalDateTime createdAt;
    private LocalDateTime lastUpdate;

    public User() {}

    public User(int id, String fullName, String email,
                String passwordHash, String googleId,
                String phone, String role,
                boolean active, boolean emailVerified,
                LocalDateTime createdAt, LocalDateTime lastUpdate) {
        this.id = id; this.fullName = fullName; this.email = email;
        this.passwordHash = passwordHash; this.googleId = googleId;
        this.phone = phone; this.role = role; this.active = active;
        this.emailVerified = emailVerified; this.createdAt = createdAt;
        this.lastUpdate = lastUpdate;
    }

    public int getId()                          { return id; }
    public void setId(int id)                   { this.id = id; }
    public String getFullName()                 { return fullName; }
    public void setFullName(String v)           { this.fullName = v; }
    public String getEmail()                    { return email; }
    public void setEmail(String v)              { this.email = v; }
    public String getPasswordHash()             { return passwordHash; }
    public void setPasswordHash(String v)       { this.passwordHash = v; }
    public String getGoogleId()                 { return googleId; }
    public void setGoogleId(String v)           { this.googleId = v; }
    public String getPhone()                    { return phone; }
    public void setPhone(String v)              { this.phone = v; }
    public String getRole()                     { return role; }
    public void setRole(String v)               { this.role = v; }
    public boolean isActive()                   { return active; }
    public void setActive(boolean v)            { this.active = v; }
    public boolean isEmailVerified()            { return emailVerified; }
    public void setEmailVerified(boolean v)     { this.emailVerified = v; }
    public LocalDateTime getCreatedAt()         { return createdAt; }
    public void setCreatedAt(LocalDateTime v)   { this.createdAt = v; }
    public LocalDateTime getLastUpdate()        { return lastUpdate; }
    public void setLastUpdate(LocalDateTime v)  { this.lastUpdate = v; }

    public boolean isAdmin()      { return "ADMIN".equals(role); }
    public boolean isManager()    { return "MANAGER".equals(role); }
    public boolean isStaff()      { return "STAFF".equals(role); }
    public boolean isCustomer()   { return "CUSTOMER".equals(role); }
}
