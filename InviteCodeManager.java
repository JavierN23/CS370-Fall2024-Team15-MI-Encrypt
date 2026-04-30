import java.io.*;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class InviteCodeManager implements Serializable {
    private static final long serialVersionUID = 1L;

    // Stores all invite codes
    private Map<String, InviteCode> codes = new HashMap<>();

    private void ensureCodesInitialized() {
        if (codes == null) {
            codes = new HashMap<>();
        }
    }

    // Code input
    private String normalizeCode(String code) {
        return code == null ? "" : code.trim().toUpperCase();
    }

    // Role input
    private String normalizeRole(String role) {
        return role == null ? "" : role.trim().toLowerCase();
    }

    private String normalizeUsername(String username) {
        return username == null ? "" : username.trim();
    }

    // Creates a new invite code
    public boolean createCode(String code, String role, List<String> groups, int maxUses, String vaultOwnerUsername) {
        ensureCodesInitialized();

        code = normalizeCode(code);
        role = normalizeRole(role);
        vaultOwnerUsername = normalizeUsername(vaultOwnerUsername);

        if (groups == null) {
            groups = new ArrayList<>();
        }

        // Basic checks
        if (code.isEmpty()) {
            return false;
        }

        if (vaultOwnerUsername.isEmpty()) {
            return false;
        }

        if (!role.equals("employee") && !role.equals("admin")) {
            return false;
        }

        if (maxUses <= 0) {
            return false;
        }

        // Code must be unique
        if (codes.containsKey(code)) {
            return false;
        }

        // Employee must have at least one group
        if (role.equals("employee") && groups.isEmpty()) {
            return false;
        }

        // Admin don't need groups
        if (role.equals("admin")) {
            groups = new ArrayList<>();
        }

        InviteCode inviteCode = new InviteCode(code, role, groups, maxUses, vaultOwnerUsername);
        codes.put(code, inviteCode);
        saveToFile();
        return true;
    }

    // Get code by value
    public InviteCode getCode(String code) {
        ensureCodesInitialized();

        code = normalizeCode(code);
        if (code.isEmpty()) {
            return null;
        }
        return codes.get(code);
    }

    // Checks if code exists
    public boolean codeExists(String code) {
        return getCode(code) != null;
    }

    // Turns a code off
    public boolean deactivateCode(String code) {
        InviteCode inviteCode = getCode(code);
        if (inviteCode == null) {
            return false;
        }

        inviteCode.deactivate();
        saveToFile();
        return true;
    }

    // Turns a code back on
    public boolean activateCode(String code) {
        InviteCode inviteCode = getCode(code);
        if (inviteCode == null) {
            return false;
        }

        inviteCode.activate();
        saveToFile();
        return true;
    }

    // Deletes a code
    public boolean deleteCode(String code) {
        ensureCodesInitialized();

        code = normalizeCode(code);
        if (code.isEmpty()) {
            return false;
        }

        if (codes.remove(code) != null) {
            saveToFile();
            return true;
        }
        return false;
    }

    // Returns all invite codes
    public List<InviteCode> getAllCodes() {
        ensureCodesInitialized();
        return new ArrayList<>(codes.values());
    }

    // Uses an invite code for an account
    public boolean redeemCode(String code, UserAccount account) {
        ensureCodesInitialized();

        InviteCode inviteCode = getCode(code);

        // Check if valid and usable
        if (inviteCode == null || account == null || !inviteCode.canBeUsed()) {
            return false;
        }

        String accountType = account.getAccountType();

        // Only business/both accounts can use codes
        if (!"business".equalsIgnoreCase(accountType) && !"both".equalsIgnoreCase(accountType)) {
            return false;
        }

        account.setAssignedVaultOwner(inviteCode.getVaultOwnerUsername());

        // Apply role from the invite code
        if ("admin".equalsIgnoreCase(inviteCode.getRole())) {
            account.grantBusinessAdminAccess();
        } else {
            account.grantBusinessEmployeeAccess(inviteCode.getGroups());
        }

        inviteCode.markUsed();
        saveToFile();
        return true;

    }

    // Saves all invite codes to file
    public void saveToFile() {
        ensureCodesInitialized();

        File file = UI.inviteCodesFile();
        try (ObjectOutputStream oos = new ObjectOutputStream(new FileOutputStream(file))) {
            oos.writeObject(this);
        } catch (IOException e) {
            System.err.println("Error saving invite codes: " + e.getMessage());
        }
    }

    // Loads invite codes from file
    public static InviteCodeManager loadFromFile() {
        File file = UI.inviteCodesFile();
        if (!file.exists() || file.length() == 0) {
            return new InviteCodeManager();
        }

        try (ObjectInputStream ois = new ObjectInputStream(new FileInputStream(file))) {
            Object obj = ois.readObject();
            if (obj instanceof InviteCodeManager) {
                InviteCodeManager manager = (InviteCodeManager) obj;
                manager.ensureCodesInitialized();
                return manager;
            }
        } catch (IOException | ClassNotFoundException e) {
            System.err.println("Error loading invite codes: " + e.getMessage());
        }
        return new InviteCodeManager();
    }

}
