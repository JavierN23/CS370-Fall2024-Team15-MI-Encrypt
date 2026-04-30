import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

public class UserAccount implements Serializable {
    private static final long serialVersionUID = 1L;
    
    // Basic account info
    private String username;
    private String password;
    private String email;
    private String accountType;
    private String securityQuestion;
    private String securityAnswer;

    // 2FA info
    private boolean twoFactorEnabled;
    private String totpSecret; // Store the Base32-encoded TOTP secret for 2FA

    // Login lock info
    private int failedAttempts;
    private boolean locked;

    // Business Acount Access Controls
    private boolean businessAuthorized;
    private String businessRole;
    private List<String> allowedBusinessGroups;

    private String assignedVaultOwner;

    // Creates new user
    public UserAccount(String username, String password, String email, String accountType, String securityQuestion, String securityAnswer, boolean twoFactorEnabled) {
        this.username = username;
        this.password = password;
        this.email = email;
        this.accountType = accountType;
        this.securityQuestion = securityQuestion;
        this.securityAnswer = securityAnswer;
        this.twoFactorEnabled = twoFactorEnabled;

        this.totpSecret = null; // Will be set during 2FA setup if enabled
        this.failedAttempts = 0;
        this.locked = false;

        // Business access starts off diabled
        this.businessAuthorized = false;
        this.businessRole = "none";
        this.allowedBusinessGroups = new ArrayList<>();

        this.assignedVaultOwner = null;
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
    
    public String getSecurityQuestion() {
        return securityQuestion;
    }

    public String getSecurityAnswer() {
        return securityAnswer;
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

    // Returns a copy of the business groups
    public List<String> getAllowedBusinessGroups() {
        return new ArrayList<>(allowedBusinessGroups);
    }

    // Checks if user is a business admin
    public boolean isBusinessAdmin() {
        return businessAuthorized && "admin".equalsIgnoreCase(businessRole);
    }

    // Checks if user is a business employee
    public boolean isBusinessEmployee() {
        return businessAuthorized && "employee".equalsIgnoreCase(businessRole);
    }

    // Checks if user belongs to a business group
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

    public String getAssignedVaultOwner() {
        return assignedVaultOwner;
    }

    public void setAssignedVaultOwner(String assignedVaultOwner) {
        if (assignedVaultOwner == null || assignedVaultOwner.trim().isEmpty()) {
            this.assignedVaultOwner = null;
        } else {
            this.assignedVaultOwner = assignedVaultOwner.trim();
        }
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
    public void setSecurityQuestion(String securityQuestion) {
        this.securityQuestion = securityQuestion;
        // This method can be implemented to store the security question if needed
    }

    public void setSecurityAnswer(String securityAnswer) {
        this.securityAnswer = securityAnswer;
        // This method can be implemented to store the security answer if needed
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

    // Gives or removes business access
    public void setBusinessAuthorized(boolean businessAuthorized) {
        this.businessAuthorized = businessAuthorized;

        // Clear role and groups if access is removed
        if (!businessAuthorized) {
            this.businessRole = "none";
            this.allowedBusinessGroups = new ArrayList<>();
            this.assignedVaultOwner = null;
        }
    }

    // Sets the business role
    public void setBusinessRole(String businessRole) {
        if (businessRole == null) {
            this.businessRole = "none";
            return;
        }

        String normalized = businessRole.trim().toLowerCase();

        // Only allow valid roles
        if (!normalized.equals("admin") && !normalized.equals("employee") && !normalized.equals("none")) {
            normalized = "none"; // Default to "none" if an invalid role is provided
        }
        this.businessRole = normalized;
    }

    // Replaces the business groups list
    public void setAllowedBusinessGroups(List<String> allowedBusinessGroups) {
        this.allowedBusinessGroups = new ArrayList<>();

        if (allowedBusinessGroups == null) {
            return;
        } 
        for (String group : allowedBusinessGroups) {
            addBusinessGroup(group);
        }
    }

    // Adds a business group if it's not already there
    public void addBusinessGroup(String group) {
        if (group == null || group.trim().isEmpty()) {
            return; // Ignore null or empty group names
        }
        
        String normalized = group.trim();
        if (!hasBusinessGroup(normalized)) {
            allowedBusinessGroups.add(normalized);
        }
    }

    // Removes a business group
    public void removeBusinessGroup(String group) {
        if (group == null) {
            return; // Ignore null or empty group names
        }

        allowedBusinessGroups.removeIf(g -> g != null && g.equalsIgnoreCase(group));
    }

    // Gives employee-level business access
    public void grantBusinessEmployeeAccess(List<String> groups) {
        this.businessAuthorized = true;
        this.businessRole = "employee";
        setAllowedBusinessGroups(groups);
    }

    // Gives admin-level business access
    public void grantBusinessAdminAccess() {
        this.businessAuthorized = true;
        this.businessRole = "admin";
        this.allowedBusinessGroups = new ArrayList<>();
    }

    // Removes all business access
    public void revokeBusinessAccess() {
        this.businessAuthorized = false;
        this.businessRole = "none";
        this.allowedBusinessGroups = new ArrayList<>();
        this.assignedVaultOwner = null;
    }
}