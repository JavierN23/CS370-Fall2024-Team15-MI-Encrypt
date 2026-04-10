import java.io.*;
import java.util.HashMap;

public class Credentials implements Serializable {
    private static final long serialVersionUID = 1L;

    private HashMap<String, UserAccount> accounts = new HashMap<>();
    private static final String FILE_NAME = "users.dat";

    /**
      Creates a new account
      When twoFactorEnabled is true a TOTP secret is automatically generated
      and stored on the UserAccount so the setup panel can retrieve it using
      getAccount(username).getTotpSecret()
     
      @return true if the account was created, false if the username exists
    */
    public boolean signUp(String username, String password, String email, boolean twoFactorEnabled) {
        username = username.trim();
        password = password.trim();
        email = email.trim();

        if (accounts.containsKey(username)) {
            return false; // Username already exists
        }

        UserAccount account = new UserAccount(username, password, email, twoFactorEnabled);

        // generate secret at sign up
        if (twoFactorEnabled) {
            String secret = TOTPUtil.generateSecret();
            account.setTotpSecret(secret);
        }

        accounts.put(username, account);
        saveToFile();
        return true; // Account created
    }

    /**
      Validates username + password only
      The LoginPanel is responsible for checking isTwoFactorEnabled()
      and routing to TwoFactorPanel when needed.
     */
    public boolean login(String username, String password) {
        username = username.trim();
        password = password.trim();

        UserAccount account = accounts.get(username);
        return account != null && account.getPassword().equals(password);
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
        return accounts.get(username.trim());
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
