import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

public class InviteCode implements Serializable {
    private static final long serialVersionUID = 1L;

    // Basic invite code info
    private String vaultOwnerUsername;
    private String code;
    private String role;
    private List<String> groups;

    // Usage tracking
    private boolean active;
    private int maxUses;
    private int usedCount;

    // Creates a new invite code
    public InviteCode(String code, String role, List<String> groups, int maxUses, String vaultOwnerUsername) {
        // Store code in uppercase
        this.code = code == null ? "" : code.trim().toUpperCase();

        this.vaultOwnerUsername = vaultOwnerUsername == null ? "" : vaultOwnerUsername.trim();

        // Only allow "employee" or "admin"
        role = role == null ? "" : role.trim().toLowerCase();
        if (!role.equals("employee") && !role.equals("admin")) {
            role = "employee"; // Default to employee if invalid role provided
        }
        this.role = role;

        // Copy groups into a new list
        this.groups = new ArrayList<>();
        if (groups != null) {
            for (String group : groups) {
                addGroup(group);
            }
        }

        if (this.role.equals("admin")) {
            this.groups = new ArrayList<>();
        }

        // Set default values
        this.active = true;
        this.maxUses = Math.max(1, maxUses);
        this.usedCount = 0;
    }

    public String getVaultOwnerUsername() {
        return vaultOwnerUsername;
    }

    public String getCode() {
        return code;
    }

    public String getRole() {
        return role;
    }

    // Returns a copy of the groups list
    public List<String> getGroups() {
        return new ArrayList<>(groups);
    }

    public boolean isActive() {
        return active;
    }

    public int getMaxUses() {
        return maxUses;
    }

    public int getUsedCount() {
        return usedCount;
    }

    // Checks if the code can still be used
    public boolean canBeUsed() {
        return active && usedCount < maxUses;
    }

    // Marks the code as used 
    public void markUsed() {
        if (!canBeUsed()) {
            return;
        }
        
        usedCount++;

        // Disables if max uses reached
        if (usedCount >= maxUses) {
            active = false;
        }
    }

    // Disables code
    public void deactivate() {
        active = false;
    }

    // Activates code (If still usable)
    public void activate() {
        if (usedCount < maxUses) {
            active = true;
        }
    }

    // Adds groups 
    public void addGroup(String group) {
        if (group == null || group.trim().isEmpty()) {
            return; // Ignore null or empty group names
        }
        
        String normalized = group.trim();

        // Checks for duplicates
        for (String existing : groups) {
            if (existing.equalsIgnoreCase(normalized)) {
                return; // Group already exists, ignore
            }
        }

        groups.add(normalized);
    }

    // Removes a group
    public void removeGroup(String group) {
        if (group == null || group.trim().isEmpty()) {
            return; // Ignore null or empty group names
        }

        String normalized = group.trim();
        groups.removeIf(g -> g != null && g.equalsIgnoreCase(normalized));
    }

    // Display for code
    @Override
    public String toString() {
        return code + " | " + role + " | uses: " + usedCount + "/" + maxUses;

    }   
}
