import java.io.*;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

public class Credentials implements Serializable {
    private static final long serialVersionUID = 1L;

    private HashMap<String, UserAccount> accounts = new HashMap<>();
    private static final String FILE_NAME = "users.dat";
    private static final int MAX_LOGIN_ATTEMPTS = 3;

    // For Testing the Master Key functionality.
    private static final String MASTER_KEY = "MIENCRYPT";

    private boolean isValidAccountType(String type) {
        return type.equals("personal") || type.equals("business") || type.equals("both");
    }

    public boolean signUp(String username, String password, String email, String accountType, boolean twoFactorEnabled) {
        username = username.trim();
        password = password.trim();
        email = email.trim();
        accountType = accountType.trim().toLowerCase();


        if (username.isEmpty() || password.isEmpty() || email.isEmpty() || accountType.isEmpty()) {
            return false; 
        }
        
        if (!isValidAccountType(accountType)) {
            return false; 
        }

        if (accounts.containsKey(username)) {
            return false; // Username already exists
        }
        
        UserAccount account = new UserAccount(username, password, email, accountType, twoFactorEnabled);
        if (twoFactorEnabled) {
            account.setTotpSecret(TOTPUtil.generateSecret());
        }
        account.setBusinessAuthorized(false);
        account.setBusinessRole("employee");
        account.setAllowedBusinessGroups(new ArrayList<>());

        accounts.put(username, account);
        saveToFile();
        return true;
    }

    public boolean login(String username, String password) {
        username = username.trim();
        password = password.trim();

        UserAccount account = accounts.get(username);

        if (account == null) {
            return false;
        }

        if (account.isLocked()) {
            return false;
        }

        if (account.getPassword().equals(password)) {
            account.setFailedAttempts(0);
            saveToFile();
            return true;
        } else {
            int attempts = account.getFailedAttempts() + 1;
            account.setFailedAttempts(attempts);

            if (attempts >= MAX_LOGIN_ATTEMPTS) {
                account.setLocked(true);
            }

            saveToFile();
            return false;
        }
    }

    public int getRemainingAttempts(String username) {
        UserAccount account = accounts.get(username.trim());
        if (account == null) {
            return 0;
        }

        return Math.max(0, MAX_LOGIN_ATTEMPTS - account.getFailedAttempts());
    }

    public boolean isLocked(String username) {
        UserAccount account = accounts.get(username.trim());
        return account != null && account.isLocked();
    }

    public boolean unlockAccount(String username) {
        UserAccount account = accounts.get(username.trim());
        if (account == null) {
            return false;
        }

        account.setLocked(false);
        account.setFailedAttempts(0);
        saveToFile();
        return true;
    }

    public boolean changePassword(String username, String oldPassword, String newPassword) {
        username = username.trim();
        oldPassword = oldPassword.trim();
        newPassword = newPassword.trim();

        UserAccount account = accounts.get(username);
        if (account == null) {
            return false;
        }

        if (!account.getPassword().equals(oldPassword)) {
            return false;
        }

        if (newPassword.isEmpty()) {
            return false;
        }

        account.setPassword(newPassword);
        saveToFile();
        return true;
    }

    public boolean updateAccountInfo(String username, String newEmail, String newAccountType, boolean twoFactorEnabled) {
        username = username.trim();
        newEmail = newEmail.trim();
        newAccountType = newAccountType.trim().toLowerCase();

        UserAccount account = accounts.get(username);
        if (account == null) {
            return false;
        }

        if (!isValidAccountType(newAccountType)){
            return false;
        }

        account.setEmail(newEmail);
        account.setAccountType(newAccountType);
        account.setTwoFactorEnabled(twoFactorEnabled);

        if (!twoFactorEnabled) {
            account.setTotpSecret(null);
        } else if (account.getTotpSecret() == null || account.getTotpSecret().isBlank()) {
            account.setTotpSecret(TOTPUtil.generateSecret());
        }

        if (newAccountType.equals("personal")) {
            account.setBusinessAuthorized(false);
            account.setBusinessRole("employee");
            account.setAllowedBusinessGroups(new ArrayList<>());
        }

        saveToFile();
        return true;
    }

    public boolean deleteAccount(String username) {
        username = username.trim();

        if (accounts.remove(username) != null) {
            saveToFile();
            return true; // Deleted
        }
        return false; // Not found
    }

    public UserAccount getAccount(String username) {
        if (username == null) {
            return null;
        }
        return accounts.get(username.trim());
    }

    public boolean setBusinessAccess(String username, boolean authorized) {
        UserAccount account = accounts.get(username.trim());
        if (account == null){
            return false;
        }

        if (!isBusinessType(account)) {
            return false; // Not a business account
        }

        account.setBusinessAuthorized(authorized);

        if (!authorized) {
            account.setBusinessRole("employee");
            account.setAllowedBusinessGroups(new ArrayList<>());
        }
        saveToFile();
        return true;
    }

    public boolean setBusinessRole(String username, String role) {
        UserAccount account = accounts.get(username.trim());
        if (account == null){
            return false;
        }

        if (!isBusinessType(account)) {
            return false; // Not a business account
        }

        if (role == null) {
            return false;
        }

        role = role.trim().toLowerCase();

        if (!role.equals("employee") && !role.equals("admin")) {
            return false; // Invalid role
        }

        account.setBusinessRole(role);

        if (role.equals("admin")) {
            account.setBusinessAuthorized(true);
            account.setAllowedBusinessGroups(new ArrayList<>());
        }

        saveToFile();
        return true;
    }

    public boolean promoteToBusinessAdmin(String username) {
        UserAccount account = accounts.get(username.trim());
        if (account == null){
            return false;
        }
        
        if (!isBusinessType(account)) {
            return false; // Not a business account
    }

    account.setBusinessRole("admin");
    account.setBusinessAuthorized(true);
    account.setAllowedBusinessGroups(new ArrayList<>());

    saveToFile();
    return true;
}

    public boolean addBusinessGroup(String username, String group) {
        UserAccount account = accounts.get(username.trim());
        if (account == null || group == null || group.trim().isEmpty()){
            return false;
        }

        if (!isBusinessType(account)) {
            return false; // Not a business account
        }

        account.addBusinessGroup(group.trim());
        saveToFile();
        return true;
    }

    public boolean removeBusinessGroup(String username, String group) {
        UserAccount account = accounts.get(username.trim());
        if (account == null || group == null || group.trim().isEmpty()){
            return false;
        }

        account.removeBusinessGroup(group.trim());
        saveToFile();
        return true;
    }

    public List<String> getBusinessGroups(String username) {
        UserAccount account = accounts.get(username.trim());
        if (account == null){
            return new ArrayList<>();
        }

        return account.getAllowedBusinessGroups();
    }

    public boolean clearBusinessGroups(String username) {
        UserAccount account = accounts.get(username.trim());
        if (account == null){
            return false;
        }

        account.setAllowedBusinessGroups(new ArrayList<>());
        saveToFile();
        return true;
    }

    public boolean isBusinessAuthorized(String username) {
        UserAccount account = accounts.get(username.trim());
        return account != null && account.isBusinessAuthorized();
    }

    public boolean isBusinessAdmin(String username) {
        UserAccount account = accounts.get(username.trim());
        return account != null && account.isBusinessAdmin();
    }

    private boolean isBusinessType(UserAccount account) {
        return account.getAccountType().equalsIgnoreCase("business") ||
               account.getAccountType().equalsIgnoreCase("both");
    }

    public List<UserAccount> getAllAccounts() {
        return new ArrayList<>(accounts.values());
    }

    public boolean hasAnyBusinessAdmin() {
        for (UserAccount account : accounts.values()) {
            if (account != null && isBusinessType(account) && account.isBusinessAdmin()) {
                return true;
            }
        }

        return false;
    }

    public boolean claimInitialBusinessAdmin(String username, String password) {
        UserAccount account = accounts.get(username.trim());
        if (account == null) {
            return false;
        }

        if (!isBusinessType(account)) {
            return false; // Not a business account
        }

        if (hasAnyBusinessAdmin()) {
            return false; // Admin already exists
        }

        if (password == null || !MASTER_KEY.equals(password.trim())) {
            return false; // Invalid master key
        }

        account.setBusinessAuthorized(true);
        account.setBusinessRole("admin");
        account.setAllowedBusinessGroups(new ArrayList<>());

        saveToFile();
        return true;

    }

    // Save credentials to file
    public void saveToFile() {
        try (ObjectOutputStream oos =
                     new ObjectOutputStream(new FileOutputStream(FILE_NAME))) {
            oos.writeObject(this);
        } catch (IOException e) {
            System.out.println("Error saving user data: " + e.getMessage());
        }
    }

    // Load credentials from file
    public static Credentials loadFromFile() {
        File file = new File(FILE_NAME);
        if (!file.exists() || file.length() == 0) {
            return new Credentials();
        }
            

        try (ObjectInputStream ois =
                     new ObjectInputStream(new FileInputStream(FILE_NAME))) {

            Object obj = ois.readObject();
            if (obj instanceof Credentials) {
                return (Credentials) obj;
            }

        } catch (IOException | ClassNotFoundException e) {
            System.out.println("Error loading user data: " + e.getMessage());
        }

        return new Credentials();
    }
}