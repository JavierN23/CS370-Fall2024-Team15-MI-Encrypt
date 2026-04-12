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
        this.businessRole = "employee";
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

    public String getBusinessRole() {
        return businessRole;
    }

    public List<String> getAllowedBusinessGroups() {
        return new ArrayList<>(allowedBusinessGroups);
    }

    public boolean isBusinessAdmin() {
        return "admin".equalsIgnoreCase(businessRole);
    }

    public boolean isBusinessEmployee() {
        return businessAuthorized && "employee".equalsIgnoreCase(businessRole);
    }

    public boolean hasBusinessGroup(String group) {
        if (group == null || allowedBusinessGroups == null) {
            return false;
        }
        return allowedBusinessGroups.contains(group);
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
    }

    public void setBusinessRole(String businessRole) {
        this.businessRole = businessRole;
    }

    public void setAllowedBusinessGroups(List<String> allowedBusinessGroups) {
        if (allowedBusinessGroups == null) {
            this.allowedBusinessGroups = new ArrayList<>();
        } else {
            this.allowedBusinessGroups = new ArrayList<>(allowedBusinessGroups);
        }
    }

    public void addBusinessGroup(String group) {
        if (group != null && !allowedBusinessGroups.contains(group)) {
            allowedBusinessGroups.add(group);
        }
    }

    public void removeBusinessGroup(String group) {
        if (group != null) {
            allowedBusinessGroups.remove(group);
        }
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
        this.businessRole = "employee";
        this.allowedBusinessGroups = new ArrayList<>();
    }
}
