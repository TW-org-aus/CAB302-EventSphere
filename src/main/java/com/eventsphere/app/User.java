import java.time.LocalDateTime;

public class User {
    private int userId;
    private String email;
    private String passwordHash;
    private String username;
    private boolean notifyEnabled;
    private boolean active;
    private LocalDateTime createdAt;

    public User(int userId, String email, String passwordHash, String username,
                boolean notifyEnabled, boolean active, LocalDateTime createdAt) {
        this.userId = userId;
        this.email = email;
        this.passwordHash = passwordHash;
        this.username = username;
        this.notifyEnabled = notifyEnabled;
        this.active = active;
        this.createdAt = createdAt;
    }

    public int getUserId() { return userId; }
    public String getEmail() { return email; }
    public String getPasswordHash() { return passwordHash; }
    public String getUsername() { return username; }
    public boolean isNotifyEnabled() { return notifyEnabled; }
    public boolean isActive() { return active; }
    public LocalDateTime getCreatedAt() { return createdAt; }

    public void setEmail(String email) { this.email = email; }
    public void setPasswordHash(String passwordHash) { this.passwordHash = passwordHash; }
    public void setUsername(String username) { this.username = username; }
    public void setNotifyEnabled(boolean notifyEnabled) { this.notifyEnabled = notifyEnabled; }
    public void setActive(boolean active) { this.active = active; }
}