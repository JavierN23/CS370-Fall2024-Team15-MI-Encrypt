import java.io.Serializable;

public class UserAccount implements Serializable {
    private static final long serialVersionUID = 1L;
    private String username;
    private String password;
    private String email;
    private boolean twoFactorEnabled;

    public UserAccount(String username, String password, String email, boolean twoFactorEnabled) {
        this.username = username;
        this.password = password;
        this.email = email;
        this.twoFactorEnabled = twoFactorEnabled;
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

    public boolean isTwoFactorEnabled() {
        return twoFactorEnabled;
    }
    
}
