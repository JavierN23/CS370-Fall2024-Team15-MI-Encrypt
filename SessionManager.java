import java.util.UUID;

public class SessionManager {

    // Tracks current session info
    private static String currentUser;
    private static String sessionId;
    private static long lastActivityTime;

    //  Adjust depending on how strict you want the timeout to be.
    private static final long TIMEOUT = 90 * 1000; // 90 seconds

    // Starts a new session
    public static void startSession(String username) {
        currentUser = username;
        sessionId = UUID.randomUUID().toString();
        lastActivityTime = System.currentTimeMillis();

        System.out.println("Session started for " + username);
        System.out.println("Session ID: " + sessionId);
    }

    // Ends the current session
    public static void endSession() {
        System.out.println("Session ended for " + currentUser);
        currentUser = null;
        sessionId = null;
        lastActivityTime = 0;
    }

    // Updates last activity time
    public static void updateActivity() {
        if (currentUser != null) {
            lastActivityTime = System.currentTimeMillis();
        }
    }

    // Checks if session is timed out
    public static boolean isExpired() {
        if (currentUser == null) {
            return true;
        }
        
        return System.currentTimeMillis() - lastActivityTime > TIMEOUT;
    }

    // Checks if someone is logged in
    public static boolean isLoggedIn() {
        return currentUser != null;
    }

    // Gets current user
    public static String getUser() {
        return currentUser;
    }

    // Makes sure session is still valid
    public static boolean validateSession(AppFrame app) {
        if (!isLoggedIn()) {
            app.showLogin();
            return false;
        }

        // Session mismatch - Reset
        if (!currentUser.equals(app.getCurrentUser())) {
            endSession();
            app.showLogin();
            return false;
        }

        // Expired session - Logout
        if (isExpired()) {
            endSession();
            javax.swing.JOptionPane.showMessageDialog(null, "Session expired due to inactivity. Please log in again.");
            app.showLogin();
            return false;
        }

        // Update activity if still valid
        updateActivity();
        return true;
    }
}