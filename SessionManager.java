import java.util.UUID;

public class SessionManager {

    private static String currentUser;
    private static String sessionId;
    private static long lastActivityTime;

    
    private static final long TIMEOUT = 90 * 1000; 

    public static void startSession(String username) {
        currentUser = username;
        sessionId = UUID.randomUUID().toString();
        lastActivityTime = System.currentTimeMillis();

        System.out.println("Session started for " + username);
        System.out.println("Session ID: " + sessionId);
    }

    public static void endSession() {
        System.out.println("Session ended for " + currentUser);
        currentUser = null;
        sessionId = null;
        lastActivityTime = 0;
    }

    public static void updateActivity() {
        if (currentUser != null) {
            lastActivityTime = System.currentTimeMillis();
        }
    }

    public static boolean isExpired() {
        if (currentUser == null) {
            return true;
        }
        
        return System.currentTimeMillis() - lastActivityTime > TIMEOUT;
    }

    public static boolean isLoggedIn() {
        return currentUser != null;
    }

    public static String getUser() {
        return currentUser;
    }

    public static boolean validateSession(AppFrame app) {
        if (!isLoggedIn()) {
            app.showLogin();
            return false;
        }

        if (!currentUser.equals(app.getCurrentUser())) {
            endSession();
            app.showLogin();
            return false;
        }

        if (isExpired()) {
            endSession();
            JOptionPane.showMessageDialog(null,
                    "Session expired due to inactivity. Please log in again.");
            app.showLogin();
            return false;
        }

        updateActivity();
        return true;
    }
}
