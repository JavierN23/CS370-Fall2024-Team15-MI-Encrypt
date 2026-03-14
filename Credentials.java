import java.io.*;
import java.util.HashMap;

public class Credentials implements Serializable {
    private static final long serialVersionUID = 1L;

    private HashMap<String, String> loginPageInfo = new HashMap<>();
    private static final String FILE_NAME = "users.dat";

    public boolean signUp(String username, String password) {
        username = username.trim();
        password = password.trim();

        if (loginPageInfo.containsKey(username)) {
            return false; // Username already exists
        }
        loginPageInfo.put(username, password);
        saveToFile();
        return true; // Account created
    }

    public boolean login(String username, String password) {
        username = username.trim();
        password = password.trim();

        if (!loginPageInfo.containsKey(username)) {
            return false; // Username not found
        }
        return loginPageInfo.get(username).equals(password);
    }

    public boolean deleteAccount(String username) {
        username = username.trim();

        if (loginPageInfo.remove(username) != null) {
            saveToFile();
            return true; // Deleted
        }
        return false; // Not found
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