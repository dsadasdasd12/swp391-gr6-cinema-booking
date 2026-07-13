package model;
import java.io.Serializable;
import java.time.LocalDateTime;

public class User implements Serializable {

    private static final long serialVersionUID = 1L;

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

    public User() {
    }

    public User(int id, String fullName, String email,
                String passwordHash, String googleId,
                String phone, String role,
                boolean active, boolean emailVerified,
                LocalDateTime createdAt, LocalDateTime lastUpdate) {

        this.id = id;
        this.fullName = fullName;
        this.email = email;
        this.passwordHash = passwordHash;
        this.googleId = googleId;
        this.phone = phone;
        this.role = role;
        this.active = active;
        this.emailVerified = emailVerified;
        this.createdAt = createdAt;
        this.lastUpdate = lastUpdate;
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getFullName() {
        return fullName;
    }

    public void setFullName(String fullName) {
        this.fullName = fullName;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getPasswordHash() {
        return passwordHash;
    }

    public void setPasswordHash(String passwordHash) {
        this.passwordHash = passwordHash;
    }

    public String getGoogleId() {
        return googleId;
    }

    public void setGoogleId(String googleId) {
        this.googleId = googleId;
    }

    public String getPhone() {
        return phone;
    }

    public void setPhone(String phone) {
        this.phone = phone;
    }

    public String getRole() {
        return role;
    }

    public void setRole(String role) {
        this.role = role;
    }

    public boolean isActive() {
        return active;
    }

    public void setActive(boolean active) {
        this.active = active;
    }

    public boolean isEmailVerified() {
        return emailVerified;
    }

    public void setEmailVerified(boolean emailVerified) {
        this.emailVerified = emailVerified;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }

    public LocalDateTime getLastUpdate() {
        return lastUpdate;
    }

    public void setLastUpdate(LocalDateTime lastUpdate) {
        this.lastUpdate = lastUpdate;
    }
    
    public boolean isAdmin()      { return "ADMIN".equals(role); }
    public boolean isManager()    { return "MANAGER".equals(role); }
    public boolean isStaff()      { return "STAFF".equals(role); }
    public boolean isCustomer()   { return "CUSTOMER".equals(role); }
}
