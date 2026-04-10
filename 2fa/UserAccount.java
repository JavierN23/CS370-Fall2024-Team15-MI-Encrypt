import java.io.Serializable;

public class UserAccount implements Serializable {
    private static final long serialVersionUID = 1L;

    private String username;
    private String password;
    private String email;
    private boolean twoFactorEnabled;

    /**
      Stores the TOTP secret generated at sign up
      Null when twoFactorEnabled is false.
     */
    private String totpSecret;

    public UserAccount(String username, String password, String email, boolean twoFactorEnabled) {
        this.username = username;
        this.password = password;
        this.email = email;
        this.twoFactorEnabled = twoFactorEnabled;
    }

    // existing getters

    public String getUsername() { 
        return username; 
    }
    public String getPassword() {
        return password; 
    }
    public String getEmail() {             
        return email;
    }
    public boolean isTwoFactorEnabled() {
        return twoFactorEnabled; 
    }

    // new TOTP fields

    public String getTotpSecret() { 
        return totpSecret; 
    }
    public void setTotpSecret(String totpSecret)  {
        this.totpSecret = totpSecret; 
    }
}
