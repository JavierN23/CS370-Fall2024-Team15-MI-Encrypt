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
    private static final String OWNER_BOOTSTRAP_CODE = "MIENCRYPT";

    private void ensureAccountsInitialized() {
        if (accounts == null) {
            accounts = new HashMap<>();
        }
    }

    private boolean isValidAccountType(String type) {
        return type.equals("personal") || type.equals("business") || type.equals("both");
    }

    private boolean isBusinessType(UserAccount account) {
        return account != null
                && (account.getAccountType().equalsIgnoreCase("business") 
                || account.getAccountType().equalsIgnoreCase("both"));
    }

    public boolean signUp(String username, String password, String email, String accountType, boolean twoFactorEnabled, String inviteCode, InviteCodeManager inviteCodeManager) {

        ensureAccountsInitialized();

        username = username == null ? "" : username.trim();
        password = password == null ? "" : password.trim();
        email = email == null ? "" : email.trim();
        accountType = accountType == null ? "" : accountType.trim().toLowerCase();
        inviteCode = inviteCode == null ? "" : inviteCode.trim();


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

        account.revokeBusinessAccess();

        if ((accountType.equals("business") || accountType.equals("both")) 
                && !inviteCode.isEmpty()) {
            if (inviteCodeManager == null || !inviteCodeManager.redeemCode(inviteCode, account)) {
                return false; // Invalid invite code
            }
        }

        accounts.put(username, account);
        saveToFile();
        return true;
    }

    public boolean login(String username, String password) {
        ensureAccountsInitialized();
        
        username = username == null ? "" : username.trim();
        password = password == null ? "" : password.trim();

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
        ensureAccountsInitialized();
        
        username = username == null ? "" : username.trim();
        UserAccount account = accounts.get(username);
        if (account == null) {
            return 0;
        }

        return Math.max(0, MAX_LOGIN_ATTEMPTS - account.getFailedAttempts());
    }

    public boolean isLocked(String username) {
        ensureAccountsInitialized();

        username = username == null ? "" : username.trim();
        UserAccount account = accounts.get(username);
        return account != null && account.isLocked();
    }

    public boolean unlockAccount(String username) {
        ensureAccountsInitialized();

        username = username == null ? "" : username.trim();
        UserAccount account = accounts.get(username);
        if (account == null) {
            return false;
        }

        account.setLocked(false);
        account.setFailedAttempts(0);
        saveToFile();
        return true;
    }

    public boolean changePassword(String username, String oldPassword, String newPassword) {
        ensureAccountsInitialized();
        
        username = username == null ? "" : username.trim();
        oldPassword = oldPassword == null ? "" : oldPassword.trim();
        newPassword = newPassword == null ? "" : newPassword.trim();

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
        ensureAccountsInitialized();

        username = username == null ? "" : username.trim();
        newEmail = newEmail == null ? "" : newEmail.trim();
        newAccountType = newAccountType == null ? "" : newAccountType.trim().toLowerCase();

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

        if ("personal".equals(newAccountType)) {
            account.revokeBusinessAccess();
        }

        saveToFile();
        return true;
    }

    public boolean deleteAccount(String username) {
        ensureAccountsInitialized();

        username = username == null ? "" : username.trim();

        if (accounts.remove(username) != null) {
            saveToFile();
            return true; // Deleted
        }
        return false; // Not found
    }

    public UserAccount getAccount(String username) {
        ensureAccountsInitialized();

        username = username == null ? "" : username.trim();
        if (username.isEmpty()) {
            return null;
        }
        return accounts.get(username);
    }

    public boolean setBusinessAccess(String username, boolean authorized) {
        ensureAccountsInitialized();

        username = username == null ? "" : username.trim();
        ensureAccountsInitialized();
        UserAccount account = accounts.get(username);
        if (account == null){
            return false;
        }

        if (!isBusinessType(account)) {
            return false; // Not a business account
        }

        if (!authorized) {
            account.revokeBusinessAccess();
        } else {
            account.setBusinessAuthorized(true);

            if ("none".equalsIgnoreCase(account.getBusinessRole())) {
                account.setBusinessRole("employee");
            }
        }

        saveToFile();
        return true;
    }

    public boolean setBusinessRole(String username, String role) {
        ensureAccountsInitialized();

        username = username == null ? "" : username.trim();
        role = role == null ? "" : role.trim().toLowerCase();

        UserAccount account = accounts.get(username);
        if (account == null){
            return false;
        }

        if (!isBusinessType(account)) {
            return false; // Not a business account
        }

        if (!role.equals("employee") && !role.equals("admin") && !role.equals("none")) {
            return false; // Invalid role
        }

        if ("none".equals(role)) {
            account.revokeBusinessAccess();
        } else if ("admin".equals(role)) {
            account.grantBusinessAdminAccess();
        } else {
            account.setBusinessAuthorized(true);
            account.setBusinessRole("employee");
        }

        saveToFile();
        return true;
    }

    public boolean promoteToBusinessAdmin(String username) {
        ensureAccountsInitialized();

        username = username == null ? "" : username.trim();
        UserAccount account = accounts.get(username);
        if (account == null){
            return false;
        }
        
        if (!isBusinessType(account)) {
            return false; // Not a business account
    }

    account.grantBusinessAdminAccess();
    saveToFile();
    return true;
    }

    public boolean addBusinessGroup(String username, String group) {
        ensureAccountsInitialized();

        username = username == null ? "" : username.trim();
        group = group == null ? "" : group.trim();

        UserAccount account = accounts.get(username);
        if (account == null || group.isEmpty()){
            return false;
        }

        if (!isBusinessType(account)) {
            return false; // Not a business account
        }

        if (!account.isBusinessAuthorized()) {
            return false; // User doesn't have business access
        }

        account.addBusinessGroup(group);
        saveToFile();
        return true;
    }

    public boolean removeBusinessGroup(String username, String group) {
        ensureAccountsInitialized();

        username = username == null ? "" : username.trim();
        group = group == null ? "" : group.trim();

        UserAccount account = accounts.get(username);
        if (account == null || group.isEmpty()) {
            return false;
        }

        if (!isBusinessType(account)) {
            return false; // Not a business account
        }

        account.removeBusinessGroup(group);
        saveToFile();
        return true;
    }

    public List<String> getBusinessGroups(String username) {
        ensureAccountsInitialized();

        username = username == null ? "" : username.trim();
        UserAccount account = accounts.get(username);
        
        if (account == null){
            return new ArrayList<>();
        }

        return account.getAllowedBusinessGroups();
    }

    public boolean clearBusinessGroups(String username) {
        ensureAccountsInitialized();

        username = username == null ? "" : username.trim();
        UserAccount account = accounts.get(username);
        
        if (account == null){
            return false;
        }

        account.setAllowedBusinessGroups(new ArrayList<>());
        saveToFile();
        return true;
    }

    public boolean isBusinessAuthorized(String username) {
        ensureAccountsInitialized();
        
        username = username == null ? "" : username.trim();
        UserAccount account = accounts.get(username);
        return account != null && account.isBusinessAuthorized();
    }

    public boolean isBusinessAdmin(String username) {
        ensureAccountsInitialized();
        username = username == null ? "" : username.trim();
        UserAccount account = accounts.get(username);
        return account != null && account.isBusinessAdmin();
    }

    public List<UserAccount> getAllAccounts() {
        ensureAccountsInitialized();
        return new ArrayList<>(accounts.values());
    }

    public boolean hasAnyBusinessAdmin() {
        ensureAccountsInitialized();
        for (UserAccount account : accounts.values()) {
            if (account != null && isBusinessType(account) && account.isBusinessAdmin()) {
                return true;
            }
        }
        return false;
    }

    public boolean claimInitialBusinessAdmin(String username, String bootstrapCode) {
        ensureAccountsInitialized();
        username = username == null ? "" : username.trim();
        bootstrapCode = bootstrapCode == null ? "" : bootstrapCode.trim();

        UserAccount account = accounts.get(username);
        if (account == null) {
            return false;
        }

        if (!isBusinessType(account)) {
            return false; // Not a business account
        }

        if (hasAnyBusinessAdmin()) {
            return false; // Admin already exists
        }

        if (!OWNER_BOOTSTRAP_CODE.equals(bootstrapCode)) {
            return false; // Invalid master key
        }

        account.grantBusinessAdminAccess();
        saveToFile();
        return true;

    }

    // Save credentials to file
    public void saveToFile() {
        ensureAccountsInitialized();

        try (ObjectOutputStream oos = new ObjectOutputStream(new FileOutputStream(UI.usersFile()))) {
            oos.writeObject(this);
        } catch (IOException e) {
            System.out.println("Error saving user data: " + e.getMessage());
        }
    }

    // Load credentials from file
    public static Credentials loadFromFile() {
        File file = UI.usersFile();
        if (!file.exists() || file.length() == 0) {
            return new Credentials();
        }
            

        try (ObjectInputStream ois = new ObjectInputStream(new FileInputStream(file))) {
            Object obj = ois.readObject();
            if (obj instanceof Credentials) {
                Credentials creds = (Credentials) obj;
                creds.ensureAccountsInitialized();
                return creds;
            }
        } catch (IOException | ClassNotFoundException e) {
            System.out.println("Error loading user data: " + e.getMessage());
        }

        return new Credentials();
    }
}