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
        currentUser = null;
        sessionId = null;
        lastActivityTime = 0;
    }

    public static void updateActivity() {
        lastActivityTime = System.currentTimeMillis();
    }

    public static boolean isExpired() {
        return System.currentTimeMillis() - lastActivityTime > TIMEOUT;
    }

    public static boolean isLoggedIn() {
        return currentUser != null;
    }

    public static String getUser() {
        return currentUser;
    }
}
