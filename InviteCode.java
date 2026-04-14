import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

public class InviteCode implements Serializable {
    private static final long serialVersionUID = 1L;

    private String code;
    private String role;
    private List<String> groups;
    private boolean active;
    private int maxUses;
    private int usedCount;

    public InviteCode(String code, String role, List<String> groups, int maxUses) {
        this.code = code == null ? "" : code.trim().toUpperCase();

        role = role == null ? "" : role.trim().toLowerCase();
        if (!role.equals("employee") && !role.equals("admin")) {
            role = "employee"; // Default to employee if invalid role provided
        }
        this.role = role;

        this.groups = new ArrayList<>();
        if (groups != null) {
            for (String group : groups) {
                addGroup(group);
            }
        }

        this.active = true;
        this.maxUses = Math.max(1, maxUses);
        this.usedCount = 0;
    }

    public String getCode() {
        return code;
    }

    public String getRole() {
        return role;
    }

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

    public boolean canBeUsed() {
        return active && usedCount < maxUses;
    }

    public void markUsed() {
        if (!canBeUsed()) {
            return;
        }
        usedCount++;

        if (usedCount >= maxUses) {
            active = false;
        }
    }

    public void deactivate() {
        active = false;
    }

    public void activate() {
        if (usedCount < maxUses) {
            active = true;
        }
    }

    public void addGroup(String group) {
        if (group == null || group.trim().isEmpty()) {
            return; // Ignore null or empty group names
        }
        
        String normalized = group.trim();

        for (String existing : groups) {
            if (existing.equalsIgnoreCase(normalized)) {
                return; // Group already exists, ignore
            }
        }

        groups.add(normalized);
    }

    public void removeGroup(String group) {
        if (group == null || group.trim().isEmpty()) {
            return; // Ignore null or empty group names
        }

        String normalized = group.trim();
        groups.removeIf(g -> g != null && g.equalsIgnoreCase(normalized));
    }

    @Override
    public String toString() {
        return code + " | " + role + " | uses: " + usedCount + "/" + maxUses;

    }   
}
