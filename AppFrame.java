import java.awt.*;
import javax.swing.*;

public class AppFrame extends JFrame {
    private final Credentials creds;
    private final PasswordManager pm;

    private final CardLayout layout = new CardLayout();
    private final JPanel cards = new JPanel(layout);

    private final LoginPanel login;
    private final SignUpPanel signUp;
    private final ChoicePanel choice;
    private final VaultPanel vault;

    private String currentUser;

    public AppFrame(Credentials creds, PasswordManager pm) {
        this.creds = creds;
        this.pm = pm;

        // Project Title and frame settings
        setTitle("MI Encrypt");
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setSize(820, 520);
        setMinimumSize(new Dimension(820, 520));
        setLocationRelativeTo(null);

        setAppIcon();

        // Panels
        login = new LoginPanel(this, creds);
        signUp = new SignUpPanel(this, creds);
        choice = new ChoicePanel(this);
        vault = new VaultPanel(this, pm);

        // Panels with CardLayout
        cards.add(login, "LOGIN");
        cards.add(signUp, "SIGNUP");
        cards.add(choice, "CHOICE");
        cards.add(vault, "VAULT");

        // Default to login page
        setContentPane(cards);
        showLogin();
        setVisible(true);

        new javax.swing.Timer(5000, e -> {
    if (SessionManager.isLoggedIn() && SessionManager.isExpired()) {
        JOptionPane.showMessageDialog(this, "Session expired. Please log in again.");

        SessionManager.endSession();
        showLogin();
    }
}).start();
        
    }

    private void setAppIcon() {
        java.net.URL iconUrl = AppFrame.class.getResource("/MI_Encrypt.png");

        System.out.println("iconUrl = " + iconUrl);

        if (iconUrl != null) {
            ImageIcon icon = new ImageIcon(iconUrl);
            setIconImage(icon.getImage());
            System.out.println("App icon loaded from resources");
        } else {
            System.out.println("Not Found");
        }
    }
    public void setCurrentUser(String username) {
        this.currentUser = username;
    }

    public String getCurrentUser() {
        return currentUser;
    }
    
    // Navigation methods
    
    public void showLogin() {
        currentUser = null;
        login.clear();
        layout.show(cards, "LOGIN");
    }

    public void showSignUp() {
        layout.show(cards, "SIGNUP");
    }

    // Show choice page after login

    public void showChoice() {
        choice.setUser(currentUser);
        layout.show(cards, "CHOICE");
    }

    // Show vault page for selected type

    public void showVault(String type) {
        vault.load(currentUser, type);
        layout.show(cards, "VAULT");
    }
}
