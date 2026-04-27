import java.io.Serializable;

public class PasswordEntry implements Serializable {
    private static final long serialVersionUID = 1L;

    // Basic info for saved password
    private String id;
    private String vaultId;
    private String site;
    private String username;
    private String encryptedPassword;
    private String businessGroup; // For business accounts, indicates which group this entry belongs to

    // Constructor
    public PasswordEntry(String site, String Username, String plainPassword) {
        this(site, Username, plainPassword, null);
    }

    // Constructor with optional business group
    public PasswordEntry(String site, String username, String plainPassword, String businessGroup) {
        this.site = site;
        this.username = username;

        // Encrypt password before storing
        this.encryptedPassword = EncryptionService.encryptCTR(plainPassword);
        this.businessGroup = businessGroup;
    }
    
    public String getId() {
        return id;
    }

    public String getVaultId() {
        return vaultId;
    }

    public String getSite(){
        return site;
    }
    public String getUsername() {
        return username;
    }
    
    // Returns decrypted password
    public String getPassword() {
        return EncryptionService.decryptCTR(encryptedPassword);
    }

    // Returns encrypted password
    public String getEncryptedPassword() {
        return encryptedPassword;
    }

    public String getBusinessGroup() {
        return businessGroup;
    }

    // Cheks if this is from the shared business vault
    public boolean isBusinessEntry() {
        return "BUSINESS_SHARED".equals(vaultId);
    }

    public void setVaultId(String vaultId) {
        this.vaultId = vaultId;
    }

    public void setSite(String site) {
        this.site = site;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    // Encrypts and updates the password
    public void setPassword(String plainPassword) {
        this.encryptedPassword = EncryptionService.encryptCTR(plainPassword);
    }

    // Sets or clears the business group
    public void setBusinessGroup(String businessGroup) {
        if (businessGroup == null || businessGroup.trim().isEmpty()) {
            this.businessGroup = null;
        } else {
            this.businessGroup = businessGroup.trim();
        }
    }

    // Display in lists
    @Override
    public String toString() {
        return site + " | " + username;
    }

}
