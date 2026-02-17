import java.util.HashMap;

public class Credentials {
    
    private HashMap<String, String> loginPageInfo = new HashMap<String, String>();

    // For storing the username and password in a HashMap. 
    public boolean signUp(String username, String password) {
        if (loginPageInfo.containsKey(username)) {
            return false;
        } 
        loginPageInfo.put(username, password);
        return true;
    }

    public boolean login(String username, String password) {
        if (!loginPageInfo.containsKey(username)) {
            return false;
        } 
        return loginPageInfo.get(username).equals(password);
    }

    public HashMap<String, String> getLoginInfo() {
        return loginPageInfo;
    }    
}