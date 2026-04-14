import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

public class UserAccount implements Serializable {
    private static final long serialVersionUID = 1L;
    
    private String username;
    private String password;
    private String email;
    private String accountType;
    private boolean twoFactorEnabled;
    private String totpSecret; // Store the Base32-encoded TOTP secret for 2FA
    private int failedAttempts;
    private boolean locked;

    // Business Acount Access Controls
    private boolean businessAuthorized;
    private String businessRole;
    private List<String> allowedBusinessGroups;

    public UserAccount(String username, String password, String email, String accountType, boolean twoFactorEnabled) {
        this.username = username;
        this.password = password;
        this.email = email;
        this.accountType = accountType;
        this.twoFactorEnabled = twoFactorEnabled;
        this.totpSecret = null; // Will be set during 2FA setup if enabled
        this.failedAttempts = 0;
        this.locked = false;

        this.businessAuthorized = false;
        this.businessRole = "none";
        this.allowedBusinessGroups = new ArrayList<>();
    }

    public String getUsername() {
        return username;
    }

    public String getPassword() {
        return password;
    }

    public String getEmail() {
        return email;
    }

    public String getAccountType() {
        return accountType;
    }

    public boolean isTwoFactorEnabled() {
        return twoFactorEnabled;
    }

    public String getTotpSecret() {
        return totpSecret;
    }

    public int getFailedAttempts() {
        return failedAttempts;
    }

    public boolean isLocked() {
        return locked;
    }

    public boolean isBusinessAuthorized() {
        return businessAuthorized;
    }

    public boolean hasBusinessAccess() {
        return businessAuthorized;
    }

    public String getBusinessRole() {
        return businessRole;
    }

    public List<String> getAllowedBusinessGroups() {
        return new ArrayList<>(allowedBusinessGroups);
    }

    public boolean isBusinessAdmin() {
        return businessAuthorized && "admin".equalsIgnoreCase(businessRole);
    }

    public boolean isBusinessEmployee() {
        return businessAuthorized && "employee".equalsIgnoreCase(businessRole);
    }

    public boolean hasBusinessGroup(String group) {
        if (group == null || allowedBusinessGroups == null) {
            return false;
        }

        for (String g : allowedBusinessGroups) {
            if (g != null && g.equalsIgnoreCase(group)) {
                return true;
            }
        }
        return false;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public void setAccountType(String accountType) {
        this.accountType = accountType;
    }

    public void setTwoFactorEnabled(boolean twoFactorEnabled) {
        this.twoFactorEnabled = twoFactorEnabled;
    }

    public void setFailedAttempts(int failedAttempts) {
        this.failedAttempts = failedAttempts;
    }

    public void setTotpSecret(String totpSecret) {
        this.totpSecret = totpSecret;
    }

    public void setLocked(boolean locked) {
        this.locked = locked;
    }

    public void setBusinessAuthorized(boolean businessAuthorized) {
        this.businessAuthorized = businessAuthorized;

        if (!businessAuthorized) {
            this.businessRole = "none";
            this.allowedBusinessGroups = new ArrayList<>();
        }
    }

    public void setBusinessRole(String businessRole) {
        if (businessRole == null) {
            this.businessRole = "none";
            return;
        }

        String normalized = businessRole.trim().toLowerCase();
        if (!normalized.equals("admin") && !normalized.equals("employee") && !normalized.equals("none")) {
            normalized = "none"; // Default to "none" if an invalid role is provided
        }
        this.businessRole = normalized;
    }

    public void setAllowedBusinessGroups(List<String> allowedBusinessGroups) {
        this.allowedBusinessGroups = new ArrayList<>();

        if (allowedBusinessGroups == null) {
            return;
        } 
        for (String group : allowedBusinessGroups) {
            addBusinessGroup(group);
        }
    }

    public void addBusinessGroup(String group) {
        if (group == null || group.trim().isEmpty()) {
            return; // Ignore null or empty group names
        }
        
        String normalized = group.trim();
        if (!hasBusinessGroup(normalized)) {
            allowedBusinessGroups.add(normalized);
        }
    }

    public void removeBusinessGroup(String group) {
        if (group == null) {
            return; // Ignore null or empty group names
        }

        allowedBusinessGroups.removeIf(g -> g != null && g.equalsIgnoreCase(group));
    }

    public void grantBusinessEmployeeAccess(List<String> groups) {
        this.businessAuthorized = true;
        this.businessRole = "employee";
        setAllowedBusinessGroups(groups);
    }

    public void grantBusinessAdminAccess() {
        this.businessAuthorized = true;
        this.businessRole = "admin";
        this.allowedBusinessGroups = new ArrayList<>();
    }

    public void revokeBusinessAccess() {
        this.businessAuthorized = false;
        this.businessRole = "none";
        this.allowedBusinessGroups = new ArrayList<>();
    }
}
