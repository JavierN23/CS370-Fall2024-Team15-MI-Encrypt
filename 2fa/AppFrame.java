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
    private final TwoFactorSetupPanel tfSetup;   // new
    private final TwoFactorPanel tfVerify;  // new

    private String currentUser;

    public AppFrame(Credentials creds, PasswordManager pm) {
        this.creds = creds;
        this.pm = pm;

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
        tfSetup = new TwoFactorSetupPanel(this, creds); // new
        tfVerify = new TwoFactorPanel(this, creds);     // new

        // Register with CardLayout
        cards.add(login, "LOGIN");
        cards.add(signUp, "SIGNUP");
        cards.add(choice, "CHOICE");
        cards.add(vault, "VAULT");
        cards.add(tfSetup, "TFSETUP");   // new
        cards.add(tfVerify, "TFVERIFY");  // new
        
        // Default to login page
        setContentPane(cards);
        showLogin();
        setVisible(true);
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

    /**
      Show the one time 2FA setup screen (called after successful sign up when the user opted in for 2FA)
     
      @param secret the TOTP secret stored on their UserAccount
    */
    public void showTwoFactorSetup(String username, String secret) {
        tfSetup.setup(username, secret);
        layout.show(cards, "TFSETUP");
    }

    // Show the 2FA verification screen during login (called after the password has been validated and the account has 2FA enabled)
    
    public void showTwoFactorVerify(String username) {
        tfVerify.setup(username);
        layout.show(cards, "TFVERIFY");
    }
}
