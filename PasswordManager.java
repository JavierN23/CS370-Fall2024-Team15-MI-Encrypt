import java.io.*;
import java.util.*;

public class PasswordManager implements Serializable {
    private static final long serialVersionUID = 1L;
    private Map<String, List<PasswordEntry>> entriesByVault = new HashMap<>();
    private static final String FILE_NAME = "passwords.dat";

    private String key(String username, String accountType) {
        return username.trim() + "|" + accountType.trim();
    }

    public List<PasswordEntry> getEntriesForUser(String username, String accountType) {
        return entriesByVault.computeIfAbsent(key(username, accountType), k -> new ArrayList<>());
    }

    // AddEntry
    public void addEntry(String username, String accountType, PasswordEntry entry) {
        getEntriesForUser(username, accountType).add(entry);
        saveToFile();
    }

    // RemoveEntry
    public void removeEntry(String username, String accountType, int index) {
        List<PasswordEntry> list = getEntriesForUser(username, accountType);
        if (index >= 0 && index < list.size()) {
            list.remove(index);
            saveToFile();
        }
    }

    // Delete all entries for a user (when account is deleted)
    public void deleteUser(String username) {
        String prefix = username.trim() + "|";
        entriesByVault.keySet().removeIf(k -> k.startsWith(prefix));
        saveToFile();
    }

    // Save to file
    public void saveToFile() {
        try (ObjectOutputStream oos = new ObjectOutputStream(new FileOutputStream(FILE_NAME))) {
            oos.writeObject(this);
        } catch (IOException e) {
            System.out.println("Error saving password data: " + e.getMessage());
        }
    }

    // Load from file
    public static PasswordManager loadFromFile() {
        File file = new File(FILE_NAME);
        if (!file.exists()) return new PasswordManager();
        try (ObjectInputStream ois = new ObjectInputStream(new FileInputStream(FILE_NAME))) {
            Object obj = ois.readObject();
            if (obj instanceof PasswordManager) return (PasswordManager) obj;
        } catch (IOException | ClassNotFoundException e) {
            System.out.println("Error loading password data: " + e.getMessage());
        }
        return new PasswordManager();
    }
}