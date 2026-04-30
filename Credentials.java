import java.io.*;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

public class Credentials implements Serializable {
    private static final long serialVersionUID = 1L;

    private HashMap<String, UserAccount> accounts = new HashMap<>();
    private static final int MAX_LOGIN_ATTEMPTS = 3;

    // Make sure the accounts map exists
    private void ensureAccountsInitialized() {
        if (accounts == null) {
            accounts = new HashMap<>();
        }
    }

    // Checks if the account type is valid
    private boolean isValidAccountType(String type) {
        return type.equals("personal") || type.equals("business") || type.equals("both");
    }

    // Check if this is a business or both account
    private boolean isBusinessType(UserAccount account) {
        return account != null
                && (account.getAccountType().equalsIgnoreCase("business") 
                || account.getAccountType().equalsIgnoreCase("both"));
    }

    // Creates a new account
    public boolean signUp(String username, String password, String email, String accountType, String businessRole, String securityQuestion, String securityAnswer, boolean twoFactorEnabled, String inviteCode, InviteCodeManager inviteCodeManager) {

        ensureAccountsInitialized();

        username = username == null ? "" : username.trim();
        password = password == null ? "" : password.trim();
        email = email == null ? "" : email.trim();
        accountType = accountType == null ? "" : accountType.trim().toLowerCase();
        businessRole = businessRole == null ? "" : businessRole.trim().toLowerCase();
        securityQuestion = securityQuestion == null ? "" : securityQuestion.trim();
        securityAnswer = securityAnswer == null ? "" : securityAnswer.trim();
        inviteCode = inviteCode == null ? "" : inviteCode.trim();


        // Make sure required fields are filled in
        if (username.isEmpty() || password.isEmpty() || email.isEmpty() || accountType.isEmpty()) {
            return false; 
        }
        
        // Reject invalid account types
        if (!isValidAccountType(accountType)) {
            return false; 
        }

        // Security Question and answer are required
        if (securityQuestion.isEmpty()) {
            return false;
        }

        if (securityAnswer.isEmpty()) {
            return false;
        }

        // Username must be unique
        if (accounts.containsKey(username)) {
            return false; // Username already exists
        }
        
        boolean firstUser = accounts.isEmpty();

        UserAccount account = new UserAccount(username, password, email, accountType, securityQuestion, securityAnswer, twoFactorEnabled);

        // Make a 2FA secret if 2FA is turned on
        if (twoFactorEnabled) {
            account.setTotpSecret(TOTPUtil.generateSecret());
        }

        boolean isBusiness = accountType.equals("business") || accountType.equals("both");

        if (firstUser) {
            // First user becomes admin
            if (!isBusiness) {
                account.setAccountType("both");
            }
            account.grantBusinessAdminAccess();
        } else {

            account.revokeBusinessAccess();

            if (isBusiness) {

                if ("admin".equals(businessRole)) {
                    account.grantBusinessAdminAccess();

                } else if ("employee".equals(businessRole)) {

                    if (inviteCode.isEmpty() ||
                        inviteCodeManager == null ||
                        !inviteCodeManager.redeemCode(inviteCode, account)) {
                        return false;
                    }

                } else {
                    return false; // invalid role
                }
            }
        }

        accounts.put(username, account);
        saveToFile();
        return true;
    }

    // Logs a user in
    public boolean login(String username, String password) {
        ensureAccountsInitialized();
        
        username = username == null ? "" : username.trim();
        password = password == null ? "" : password.trim();

        UserAccount account = accounts.get(username);

        if (account == null) {
            return false;
        }

        // Block login if the account is locked
        if (account.isLocked()) {
            return false;
        }

        // Reset failed attempts on correct password
        if (account.getPassword().equals(password)) {
            account.setFailedAttempts(0);
            saveToFile();
            return true;
        } else {
            int attempts = account.getFailedAttempts() + 1;
            account.setFailedAttempts(attempts);

            // Lock account after too many failed tries
            if (attempts >= MAX_LOGIN_ATTEMPTS) {
                account.setLocked(true);
            }

            saveToFile();
            return false;
        }
    }

    // Returns how many login tries are left
    public int getRemainingAttempts(String username) {
        ensureAccountsInitialized();
        
        username = username == null ? "" : username.trim();
        UserAccount account = accounts.get(username);
        if (account == null) {
            return 0;
        }

        return Math.max(0, MAX_LOGIN_ATTEMPTS - account.getFailedAttempts());
    }

    // Checks if an account is locked
    public boolean isLocked(String username) {
        ensureAccountsInitialized();

        username = username == null ? "" : username.trim();
        UserAccount account = accounts.get(username);
        return account != null && account.isLocked();
    }

    // Unlocks an account and resets failed attempts
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

    // Changes a user's password
    public boolean changePassword(String username, String oldPassword, String newPassword) {
        ensureAccountsInitialized();
        
        username = username == null ? "" : username.trim();
        oldPassword = oldPassword == null ? "" : oldPassword.trim();
        newPassword = newPassword == null ? "" : newPassword.trim();

        UserAccount account = accounts.get(username);
        if (account == null) {
            return false;
        }

        // Old password must match
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

    // Updates account info
    public boolean updateAccountInfo(String username, String newEmail, String newAccountType, String securityQuestion, String securityAnswer, boolean twoFactorEnabled) {
        ensureAccountsInitialized();

        username = username == null ? "" : username.trim();
        newEmail = newEmail == null ? "" : newEmail.trim();
        newAccountType = newAccountType == null ? "" : newAccountType.trim().toLowerCase();
        securityQuestion = securityQuestion == null ? "" : securityQuestion.trim();
        securityAnswer = securityAnswer == null ? "" : securityAnswer.trim();

        UserAccount account = accounts.get(username);
        if (account == null) {
            return false;
        }

        // Makes sure the new type is valid
        if (!isValidAccountType(newAccountType)){
            return false;
        }

        account.setEmail(newEmail);
        account.setAccountType(newAccountType);
        account.setSecurityQuestion(securityQuestion);
        account.setSecurityAnswer(securityAnswer);
        account.setTwoFactorEnabled(twoFactorEnabled);

        // Handle 2FA secret
        if (!twoFactorEnabled) {
            account.setTotpSecret(null);
        } else if (account.getTotpSecret() == null || account.getTotpSecret().isBlank()) {
            account.setTotpSecret(TOTPUtil.generateSecret());
        }

        // Personal accounts should not keep business access
        if ("personal".equals(newAccountType)) {
            account.revokeBusinessAccess();
        }

        saveToFile();
        return true;
    }

    // Deletes an account
    public boolean deleteAccount(String username) {
        ensureAccountsInitialized();

        username = username == null ? "" : username.trim();

        if (accounts.remove(username) != null) {
            saveToFile();
            return true; // Deleted
        }
        return false; // Not found
    }

    // Gets one account by username
    public UserAccount getAccount(String username) {
        ensureAccountsInitialized();

        username = username == null ? "" : username.trim();
        if (username.isEmpty()) {
            return null;
        }
        return accounts.get(username);
    }

    // Gives or removes business access
    public boolean setBusinessAccess(String username, boolean authorized) {
        ensureAccountsInitialized();

        username = username == null ? "" : username.trim();
        ensureAccountsInitialized();
        UserAccount account = accounts.get(username);
        if (account == null){
            return false;
        }

        // Only business users can have business access
        if (!isBusinessType(account)) {
            return false; // Not a business account
        }

        if (!authorized) {
            account.revokeBusinessAccess();
        } else {
            account.setBusinessAuthorized(true);

            // Default role becomes employee
            if ("none".equalsIgnoreCase(account.getBusinessRole())) {
                account.setBusinessRole("employee");
            }
        }

        saveToFile();
        return true;
    }

    // Sets the business role
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

        // Only allow valid roles
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

    // Promotes a user to business admin
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

    // Adds a business group to a user
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

        // User must already have business access
        if (!account.isBusinessAuthorized()) {
            return false; // User doesn't have business access
        }

        account.addBusinessGroup(group);
        saveToFile();
        return true;
    }

    // Removes a business group from a user
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

    // Gets all business groups for a user
    public List<String> getBusinessGroups(String username) {
        ensureAccountsInitialized();

        username = username == null ? "" : username.trim();
        UserAccount account = accounts.get(username);
        
        if (account == null){
            return new ArrayList<>();
        }

        return account.getAllowedBusinessGroups();
    }

    // Clears all business groups
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

    // Checks if the user has business access
    public boolean isBusinessAuthorized(String username) {
        ensureAccountsInitialized();
        
        username = username == null ? "" : username.trim();
        UserAccount account = accounts.get(username);
        return account != null && account.isBusinessAuthorized();
    }

    // Checks if the user is a business admin
    public boolean isBusinessAdmin(String username) {
        ensureAccountsInitialized();
        username = username == null ? "" : username.trim();
        UserAccount account = accounts.get(username);
        return account != null && account.isBusinessAdmin();
    }

    // Returns all accounts
    public List<UserAccount> getAllAccounts() {
        ensureAccountsInitialized();
        return new ArrayList<>(accounts.values());
    }

    // Checks if there is already a business admin
    public boolean hasAnyBusinessAdmin() {
        ensureAccountsInitialized();
        for (UserAccount account : accounts.values()) {
            if (account != null && isBusinessType(account) && account.isBusinessAdmin()) {
                return true;
            }
        }
        return false;
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