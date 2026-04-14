import java.io.*;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class InviteCodeManager implements Serializable {
    private static final long serialVersionUID = 1L;
    private Map<String, InviteCode> codes = new HashMap<>();

    private String normalizeCode(String code) {
        return code == null ? "" : code.trim().toUpperCase();
    }

    private String normalizeRole(String role) {
        return role == null ? "" : role.trim().toLowerCase();
    }

    public boolean createCode(String code, String role, List<String> groups, int maxUses) {
        code = normalizeCode(code);
        role = normalizeRole(role);

        if (code.isEmpty()) {
            return false;
        }

        if (!role.equals("employee") && !role.equals("admin")) {
            return false;
        }

        if (codes.containsKey(code)) {
            return false;
        }

        if (role.equals("employee") && (groups == null || groups.isEmpty())) {
            return false;
        }

        if (role.equals("admin")) {
            groups = new ArrayList<>();
        }

        InviteCode inviteCode = new InviteCode(code, role, groups, maxUses);
        codes.put(code, inviteCode);
        saveToFile();
        return true;
    }

    public InviteCode getCode(String code) {
        code = normalizeCode(code);
        if (code.isEmpty()) {
            return null;
        }
        return codes.get(code);
    }

    public boolean codeExists(String code) {
        return getCode(code) != null;
    }

    public boolean deactivateCode(String code) {
        InviteCode inviteCode = getCode(code);
        if (inviteCode == null) {
            return false;
        }

        inviteCode.deactivate();
        saveToFile();
        return true;
    }

    public boolean activateCode(String code) {
        InviteCode inviteCode = getCode(code);
        if (inviteCode == null) {
            return false;
        }

        inviteCode.activate();
        saveToFile();
        return true;
    }

    public boolean deleteCode(String code) {
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

    public List<InviteCode> getAllCodes() {
        return new ArrayList<>(codes.values());
    }

    public boolean redeemCode(String code, UserAccount account) {
        InviteCode inviteCode = getCode(code);
        if (inviteCode == null || account == null || !inviteCode.canBeUsed()) {
            return false;
        }

        String accountType = account.getAccountType();
        if (!"business".equalsIgnoreCase(accountType) && !"both".equalsIgnoreCase(accountType)) {
            return false;
        }

        if ("admin".equalsIgnoreCase(inviteCode.getRole())) {
            account.grantBusinessAdminAccess();
        } else {
            account.grantBusinessEmployeeAccess(inviteCode.getGroups());
        }

        inviteCode.markUsed();
        saveToFile();
        return true;

    }

    public void saveToFile() {
        File file = UI.inviteCodesFile();
        try (ObjectOutputStream oos = new ObjectOutputStream(new FileOutputStream(file))) {
            oos.writeObject(this);
        } catch (IOException e) {
            System.err.println("Error saving invite codes: " + e.getMessage());
        }
    }

    public static InviteCodeManager loadFromFile() {
        File file = UI.inviteCodesFile();
        if (!file.exists() || file.length() == 0) {
            return new InviteCodeManager();
        }

        try (ObjectInputStream ois = new ObjectInputStream(new FileInputStream(file))) {
            Object obj = ois.readObject();
            if (obj instanceof InviteCodeManager) {
                return (InviteCodeManager) obj;
            }
        } catch (IOException | ClassNotFoundException e) {
            System.err.println("Error loading invite codes: " + e.getMessage());
        }
        return new InviteCodeManager();
    }

}
