import java.io.*;
import java.util.*;

public class PasswordManager implements Serializable {
    private static final long serialVersionUID = 1L;

    // Stores password entries by vault
    private Map<String, List<PasswordEntry>> entriesByVault = new HashMap<>();

    // Builds a vault key
    private String key(String username, String accountType) {
        if (username == null || accountType == null) {
            return "";
        }
        return username.trim() + "|" + accountType.trim();
    }

    private String getBusinessVaultOwner(UserAccount user) {
        if (user == null || !user.isBusinessAuthorized()) {
            return null;
        }

        if (user.isBusinessAdmin()) {
            return user.getUsername();
        }

        String owner = user.getAssignedVaultOwner();
        if (owner == null || owner.trim().isEmpty()) {
            return null;
        }

        return owner.trim();
    }

    private String getBusinessVaultOwnerForModification(UserAccount user) {
        if (user == null || !user.isBusinessAuthorized() || !user.isBusinessAdmin()) {
            return null;
        }

        return user.getUsername();
    }

    // Gets the list for a vault, or creates it if needed
    private List<PasswordEntry> getVaultEntries(String username, String accountType) {
        return entriesByVault.computeIfAbsent(key(username, accountType), k -> new ArrayList<>());
    }

    // Checks if user can open the business vault
    public boolean canAccessBusinessVault(UserAccount user) {
        return getBusinessVaultOwner(user) != null;
    }

    // Checks if user can add, edit, or delete business entries
    public boolean canModifyBusinessVault(UserAccount user) {
        return getBusinessVaultOwnerForModification(user) != null;
    }

    // Checks if user can view one business entry
    public boolean canViewBusinessEntry(UserAccount user, PasswordEntry entry) {
        if (user == null || entry == null) {
            return false;
        }

        if (!canAccessBusinessVault(user)) {
            return false;
        }

        // Admin can view everything in their own business vault
        if (user.isBusinessAdmin()) {
            return true;
        }

        // Employees can only view entries from their group
        String groupText = entry.getBusinessGroup();
        if (groupText == null || groupText.trim().isEmpty())
            return false;

        String[] groups = groupText.split("\\s*(,|/|&|\\band\\b)\\s*");

        for (String group : groups) {
            if (user.hasBusinessGroup(group.trim())) {
                return true;
            }
        }
        return false;
    }

    // Returns the entries the user is allowed to see
    public List<PasswordEntry> getVisibleEntries(UserAccount user, String accountType) {
        if (user == null || accountType == null) {
            return new ArrayList<>();
        }

        // Personal vault: user sees their own entries
        if (!"Business".equalsIgnoreCase(accountType)) {
            return new ArrayList<>(getVaultEntries(user.getUsername(), accountType));
        }

        // Business Vault
        String owner = getBusinessVaultOwner(user);
        if (owner == null) {
            return new ArrayList<>();
        }

        List<PasswordEntry> entries = getVaultEntries(owner, "Business");

        // Admin sees everything
        if (user.isBusinessAdmin()) {
            return new ArrayList<>(entries);
        }

        // Employee sees only entries matching their groups
        List<PasswordEntry> filtered = new ArrayList<>();
        for (PasswordEntry entry : entries) {
            if (entry != null && canViewBusinessEntry(user, entry)) {
                filtered.add(entry);
            }
        }

        return filtered;
    }

    // Returns the decrypted password if the user has access
    public String getDecryptedPassword(UserAccount user, String accountType, PasswordEntry entry) {
        if (user == null || accountType == null || entry == null) {
            return null;
        }

        if ("Business".equalsIgnoreCase(accountType) && !canViewBusinessEntry(user, entry)) {
            return null;
        }

        return EncryptionService.decryptCTR(entry.getEncryptedPassword());
    }

    // Adds a new password entry
    public boolean addEntry(UserAccount user, String accountType, PasswordEntry entry) {
        if (user == null || accountType == null || entry == null) {
            return false;
        }

        if ("Business".equalsIgnoreCase(accountType)) {
            String owner = getBusinessVaultOwnerForModification(user);

            // Only Admin can add to business vault
            if (owner == null) {
                return false;
            }

            // Business entries must have a group
            if (entry.getBusinessGroup() == null || entry.getBusinessGroup().trim().isEmpty()) {
                return false;
            }

            getVaultEntries(owner, "Business").add(entry);
            saveToFile();
            return true;
        }
        
        getVaultEntries(user.getUsername(), accountType).add(entry);
        saveToFile();
        return true;
    }

    // Updates an existing password entry
    public boolean updateEntry(UserAccount user, String accountType, PasswordEntry originalEntry, PasswordEntry updatedEntry) {
        if (user == null || accountType == null || originalEntry == null || updatedEntry == null) {
            return false;
        }

        if ("Business".equalsIgnoreCase(accountType)) {
            // Only admin can edit business entries
            String owner = getBusinessVaultOwnerForModification(user);

            if (owner == null) {
                return false;
            }

            // Business entries must have a group
            if (updatedEntry.getBusinessGroup() == null || updatedEntry.getBusinessGroup().trim().isEmpty()) {
                return false;
            }
            List<PasswordEntry> list = getVaultEntries(owner, "Business");
            int index = list.indexOf(originalEntry);
            if (index == -1) {
                return false;
            }

            list.set(index, updatedEntry);
            saveToFile();
            return true;
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

    // Removes a password entry
    public boolean removeEntry(UserAccount user, String accountType, PasswordEntry entryToRemove) {
        if (user == null || accountType == null || entryToRemove == null) {
            return false;
        }

        // Only admin can delete from business vault
        if ("Business".equalsIgnoreCase(accountType)) {
            String owner = getBusinessVaultOwnerForModification(user);

            if (owner == null) {
                return false;
            }

            List<PasswordEntry> list = getVaultEntries(owner, "Business");
            boolean removed = list.remove(entryToRemove);

            if (removed) {
                saveToFile();
            }
            return removed;
        }

        List<PasswordEntry> list = getVaultEntries(user.getUsername(), accountType);
        boolean removed = list.remove(entryToRemove);
        if (removed) {
            saveToFile();
        }
        return removed;
    }

    // Returns all entries in a vault
    public List<PasswordEntry> getRawEntriesForVault(String username, String accountType) {
        return new ArrayList<>(getVaultEntries(username, accountType));
    }

    // Deletes all vault data for one user
    public void deleteUser(String username) {
        if (username == null) {
            return;
        }

        String prefix = username.trim() + "|";
        entriesByVault.keySet().removeIf(k -> k.startsWith(prefix));
        saveToFile();
    }

    // Saves password data to file
    public void saveToFile() {
        try (ObjectOutputStream oos = new ObjectOutputStream(new FileOutputStream(UI.passwordsFile()))) {
            oos.writeObject(this);
        } catch (IOException e) {
            System.out.println("Error saving password data: " + e.getMessage());
        }
    }

    // Loads password data from file
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
        if (user == null || accountType == null || newPassword == null) {
            return false;
        }

        List<PasswordEntry> entries;

        if ("Business".equalsIgnoreCase(accountType)) {
            String owner = getBusinessVaultOwner(user);
            if (owner == null) {
                return false;
            }
            entries = getVaultEntries(owner, "Business");
        } else {
            entries = getVaultEntries(user.getUsername(), accountType);
        }

        for (PasswordEntry entry : entries) {
            String existing = EncryptionService.decryptCTR(entry.getEncryptedPassword());
            if (existing.equals(newPassword)) {
                return true;
            }
        }

        return false;
    }
}