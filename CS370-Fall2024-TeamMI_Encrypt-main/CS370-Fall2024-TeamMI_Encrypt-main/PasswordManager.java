import java.io.*;
import java.util.*;

public class PasswordManager implements Serializable {
    private static final long serialVersionUID = 1L;

    private Map<String, List<PasswordEntry>> entriesByVault = new HashMap<>();

    private String key(String username, String accountType) {
        if ("Business".equalsIgnoreCase(accountType)) {
            return "BUSINESS_SHARED";
        }
        return username.trim() + "|" + accountType.trim();
    }

    private List<PasswordEntry> getVaultEntries(String username, String accountType) {
        return entriesByVault.computeIfAbsent(key(username, accountType), k -> new ArrayList<>());
    }

    public boolean canAccessBusinessVault(UserAccount user) {
        return user != null && user.isBusinessAuthorized();
    }

    public boolean canModifyBusinessVault(UserAccount user) {
        return user != null
                && user.isBusinessAuthorized()
                && user.isBusinessAdmin();
    }

    public boolean canViewBusinessEntry(UserAccount user, PasswordEntry entry) {
        if (user == null || entry == null) {
            return false;
        }

        if (!user.isBusinessAuthorized()) {
            return false;
        }

        if (user.isBusinessAdmin()) {
            return true;
        }

        return user.hasBusinessGroup(entry.getBusinessGroup());
    }

    public List<PasswordEntry> getVisibleEntries(UserAccount user, String accountType) {
        if (user == null || accountType == null) {
            return new ArrayList<>();
        }

        List<PasswordEntry> entries = getVaultEntries(user.getUsername(), accountType);

        // Personal vault: user sees their own entries
        if (!"Business".equalsIgnoreCase(accountType)) {
            return new ArrayList<>(entries);
        }

        // Business vault: unauthorized users see nothing
        if (!user.isBusinessAuthorized()) {
            return new ArrayList<>();
        }

        // Admin sees everything
        if (user.isBusinessAdmin()) {
            return new ArrayList<>(entries);
        }

        // Employee sees only entries matching their groups
        List<PasswordEntry> filtered = new ArrayList<>();
        for (PasswordEntry entry : entries) {
            if (entry != null && user.hasBusinessGroup(entry.getBusinessGroup())) {
                filtered.add(entry);
            }
        }

        return filtered;
    }

    public String getDecryptedPassword(UserAccount user, String accountType, PasswordEntry entry) {
        if (user == null || accountType == null || entry == null) {
            return null;
        }

        if ("Business".equalsIgnoreCase(accountType) && !canViewBusinessEntry(user, entry)) {
            return null;
        }

        return EncryptionService.decryptCTR(entry.getEncryptedPassword());
    }

    public boolean addEntry(UserAccount user, String accountType, PasswordEntry entry) {
        if (user == null || accountType == null || entry == null) {
            return false;
        }

        if ("Business".equalsIgnoreCase(accountType)) {
            if (!canModifyBusinessVault(user)) {
                return false;
            }

            if (entry.getBusinessGroup() == null || entry.getBusinessGroup().trim().isEmpty()) {
                return false;
            }
        }

        getVaultEntries(user.getUsername(), accountType).add(entry);
        saveToFile();
        return true;
    }

    public boolean updateEntry(UserAccount user, String accountType, PasswordEntry originalEntry, PasswordEntry updatedEntry) {
        if (user == null || accountType == null || updatedEntry == null) {
            return false;
        }

        if ("Business".equalsIgnoreCase(accountType)) {
            if (!canModifyBusinessVault(user)) {
            return false;
            }
            if (updatedEntry.getBusinessGroup() == null || updatedEntry.getBusinessGroup().trim().isEmpty()) {
                return false;
            }
        }

        List<PasswordEntry> list = getVaultEntries(user.getUsername(), accountType);
        int index = list.indexOf(originalEntry);
        if (index == -1) {
            return false;
        }

        list.set(index, updatedEntry);
        saveToFile();
        return true;

    }

    public boolean removeEntry(UserAccount user, String accountType, PasswordEntry entryToRemove) {
        if (user == null || accountType == null || entryToRemove == null) {
            return false;
        }

        if ("Business".equalsIgnoreCase(accountType) && !canModifyBusinessVault(user)) {
            return false;
        }

        List<PasswordEntry> list = getVaultEntries(user.getUsername(), accountType);
        boolean removed = list.remove(entryToRemove);
        if (removed) {
            saveToFile();
        }
            return removed;
    }

    public List<PasswordEntry> getRawEntriesForVault(String username, String accountType) {
        return new ArrayList<>(getVaultEntries(username, accountType));
    }

    public void deleteUser(String username) {
        if (username == null) {
            return;
        }

        String prefix = username.trim() + "|";
        entriesByVault.keySet().removeIf(k -> k.startsWith(prefix));
        saveToFile();
    }

    public void saveToFile() {
        try (ObjectOutputStream oos = new ObjectOutputStream(new FileOutputStream(UI.passwordsFile()))) {
            oos.writeObject(this);
        } catch (IOException e) {
            System.out.println("Error saving password data: " + e.getMessage());
        }
    }

    public static PasswordManager loadFromFile() {
        File file = UI.passwordsFile();
        if (!file.exists()) {
            return new PasswordManager();
        }

        try (ObjectInputStream ois = new ObjectInputStream(new FileInputStream(file))) {
            Object obj = ois.readObject();
            if (obj instanceof PasswordManager) {
                return (PasswordManager) obj;
            }
        } catch (IOException | ClassNotFoundException e) {
            System.out.println("Error loading password data: " + e.getMessage());
        }

        return new PasswordManager();
    }

    public boolean isPasswordReused(UserAccount user, String accountType, String newPassword) {
        List<PasswordEntry> entries = getVaultEntries(user.getUsername(), accountType);

        for (PasswordEntry entry : entries) {
            String existing = EncryptionService.decryptCTR(entry.getEncryptedPassword());
            if (existing.equals(newPassword)) {
                return true;
            }
        }

        return false;
    }
}