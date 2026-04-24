import java.awt.*;
import javax.swing.*;

public class AppFrame extends JFrame {
    private final Credentials creds;
    private final PasswordManager pm;
    private final CreditCardManager ccm;

    private final CardLayout layout = new CardLayout();
    private final JPanel cards = new JPanel(layout);

    private final LoginPanel login;
    private final SignUpPanel signUp;
    private final ChoicePanel choice;
    private final VaultPanel vault;
    private final AccountPanel account;
    private final AdminPanel admin;
    private final TwoFactorPanel tfVerify;
    private final TwoFactorSetupPanel tfSetup;

    private String currentUser;

    public AppFrame(Credentials creds, PasswordManager pm,
                    CreditCardManager ccm) {
        this.creds = creds;
        this.pm = pm;
        this.ccm = ccm;

        // Project Title and frame settings
        setTitle("MI Encrypt");
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setSize(1200, 720);
        setMinimumSize(new Dimension(1200, 720));
        setLocationRelativeTo(null);

        setAppIcon();

        // Panels
        login = new LoginPanel(this, creds);
        signUp = new SignUpPanel(this, creds);
        choice = new ChoicePanel(this, creds);
        vault = new VaultPanel(this, pm, creds, ccm);
        account = new AccountPanel(this, creds);
        admin = new AdminPanel(this, creds, null);
        tfSetup = new TwoFactorSetupPanel(this, creds);
        tfVerify = new TwoFactorPanel(this, creds);

        // Panels with CardLayout
        cards.add(login, "LOGIN");
        cards.add(signUp, "SIGNUP");
        cards.add(choice, "CHOICE");
        cards.add(vault, "VAULT");
        cards.add(account, "ACCOUNT");
        cards.add(admin, "ADMIN");
        cards.add(tfSetup, "TFSETUP");
        cards.add(tfVerify, "TFVERIFY");

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
        SessionManager.endSession();
        currentUser = null;
        login.clear();
        layout.show(cards, "LOGIN");
    }

    public void showSignUp() {
        signUp.clear();
        layout.show(cards, "SIGNUP");
    }

    // Show choice page after login

    public void showChoice() {
        if (!SessionManager.validateSession(this)) return;
        choice.setUser(currentUser);
        layout.show(cards, "CHOICE");
    }

    // Show vault page for selected type

    public void showVault(String type) {
        if (!SessionManager.validateSession(this)) return;
        vault.load(currentUser, type);
        layout.show(cards, "VAULT");
    }

    public void showAccount() {
        if (!SessionManager.validateSession(this)) return;
        account.loadUser(currentUser);
        layout.show(cards, "ACCOUNT");
    }

    public void showAdmin() {
        if (!SessionManager.validateSession(this)) return;
        admin.load(currentUser);
        layout.show(cards, "ADMIN");
    }
    /**
      Show the one time 2FA setup screen (called after successful sign up when the user opted in for 2FA)
     
      @param secret the TOTP secret stored on their UserAccount
    */
    public void showTwoFactorSetup(String username, String secret) {
        tfSetup.setup(username, secret, true);
        layout.show(cards, "TFSETUP");
    }

    // Show the 2FA verification screen during login (called after the password has been validated and the account has 2FA enabled)
    
    public void showTwoFactorVerify(String username) {
        tfVerify.setup(username);
        layout.show(cards, "TFVERIFY");
    }
}