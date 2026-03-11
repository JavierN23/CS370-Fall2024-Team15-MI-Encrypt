import java.io.Serializable;

public class PasswordEntry implements Serializable {
    private static final long serialVersionUID = 1L;

    
    private String site;
    private String username;
    private String encryptedPassword;

    public PasswordEntry(String site, String Username, String plainPassword) {
        this.site = site;
        this.username = Username;
        this.encryptedPassword = EncryptionService.encryptCTR(plainPassword);

    }

    public String getSite(){
        return site;
    }
    public String getUsername() {
        return username;
    }    
    public String getPassword() {
        return EncryptionService.decryptCTR(encryptedPassword);
    }

    public String getEncyptedPassword() {
        return encryptedPassword;
    }

    public void setSite(String site) {
        this.site = site;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public void setPassword(String plainPassword) {
        this.encryptedPassword = EncryptionService.encryptCTR(plainPassword);
    }

    @Override
    public String toString() {
        return site + " | " + username;
    }

}
