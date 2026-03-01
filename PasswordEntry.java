import java.io.Serializable;

public class PasswordEntry implements Serializable {
    private static final long serialVersionUID = 1L;

    
    private String site;
    private String username;
    private String password;

    public PasswordEntry(String site, String Username, String Password) {
        this.site = site;
        this.username = Username;
        this.password = Password;

    }

    public String getSite(){
        return site;
    }
    public String getUsername() {
        return username;
    }    
    public String getPassword() {
        return password;
    }

    @Override
    public String toString() {
        return site + " | " + username;
    }

}
