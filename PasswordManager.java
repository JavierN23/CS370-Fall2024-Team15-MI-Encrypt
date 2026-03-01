import java.io.*;
import java.util.*;

public class PasswordManager implements Serializable {
    private static final long serialVersionUID = 1L;

    // username -> list of entries
    private Map<String, List<PasswordEntry>> userEntries;

    private static final String FILE_NAME = "passwords.dat";

    public PasswordManager() {
        userEntries = new HashMap<>();
    }

    public List<PasswordEntry> getEntriesForUser(String username) {
        return userEntries.computeIfAbsent(username, k -> new ArrayList<>());
    }

    public void addEntry(String username, PasswordEntry entry) {
        getEntriesForUser(username).add(entry);
        saveToFile();
    }

    public void removeEntry(String username, int index) {
        List<PasswordEntry> entries = userEntries.get(username);
        if (entries != null && index >= 0 && index < entries.size()) {
            entries.remove(index);
            saveToFile();
        }
    }

    public void saveToFile() {
        try (ObjectOutputStream oos =
                     new ObjectOutputStream(new FileOutputStream(FILE_NAME))) {

            oos.writeObject(this);

        } catch (IOException e) {
            System.out.println("Error saving password data: " + e.getMessage());
        }
    }

    public static PasswordManager loadFromFile() {
        File file = new File(FILE_NAME);

        if (!file.exists()) {
            return new PasswordManager();
        }

        try (ObjectInputStream ois =
                     new ObjectInputStream(new FileInputStream(FILE_NAME))) {

            Object obj = ois.readObject();

            if (obj instanceof PasswordManager) {
                return (PasswordManager) obj;
            }

        } catch (IOException | ClassNotFoundException e) {
            System.out.println("Error loading password data: " + e.getMessage());
        }

        return new PasswordManager();
    }
}