import java.io.*;
import java.util.*;

public class CreditCardManager implements Serializable {
    private static final long serialVersionUID = 1L;

    private Map<String, List<CreditCardEntry>> cardsByUser = new HashMap<>();

    private String key(String username) {
        return username.trim().toLowerCase();
    }

    private List<CreditCardEntry> getUserCards(String username) {
        return cardsByUser.computeIfAbsent(key(username), k -> new ArrayList<>());
    }

    public List<CreditCardEntry> getCards(String username) {
        return new ArrayList<>(getUserCards(username));
    }

    public boolean addCard(String username, CreditCardEntry card) {
        if (username == null || card == null) return false;
        getUserCards(username).add(card);
        saveToFile();
        return true;
    }

    public boolean removeCard(String username, CreditCardEntry card) {
        if (username == null || card == null) return false;
        boolean removed = getUserCards(username).remove(card);
        if (removed) saveToFile();
        return removed;
    }

    public boolean updateCard(String username, CreditCardEntry original, CreditCardEntry updated) {
        if (username == null || original == null || updated == null) return false;
        List<CreditCardEntry> list = getUserCards(username);
        int index = list.indexOf(original);
        if (index == -1) return false;
        list.set(index, updated);
        saveToFile();
        return true;
    }

    public void saveToFile() {
        try (ObjectOutputStream oos = new ObjectOutputStream(
                new FileOutputStream(UI.creditCardsFile()))) {
            oos.writeObject(this);
        } catch (IOException e) {
            System.out.println("Error saving credit-card data: " + e.getMessage());
        }
    }

    public static CreditCardManager loadFromFile() {
        File file = UI.creditCardsFile();
        if (!file.exists()) return new CreditCardManager();
        try (ObjectInputStream ois = new ObjectInputStream(new FileInputStream(file))) {
            Object obj = ois.readObject();
            if (obj instanceof CreditCardManager) return (CreditCardManager) obj;
        } catch (IOException | ClassNotFoundException e) {
            System.out.println("Error loading credit-card data: " + e.getMessage());
        }
        return new CreditCardManager();
    }
}
