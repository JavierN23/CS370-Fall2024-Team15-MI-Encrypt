import java.awt.*;
import javax.swing.*;

public class AppFrame extends JFrame {
    private final Credentials creds;
    private final PasswordManager pm;

    private final CardLayout layout = new CardLayout();
    private final JPanel cards = new JPanel(layout);

    private final LoginPanel login;
    private final ChoicePanel choice;
    private final VaultPanel vault;

    private String currentUser;

    public AppFrame(Credentials creds, PasswordManager pm) {
        this.creds = creds;
        this.pm = pm;

        setTitle("MI Encrypt");
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setSize(820, 520);
        setMinimumSize(new Dimension(820, 520));
        setLocationRelativeTo(null);

        login = new LoginPanel(this, creds);
        choice = new ChoicePanel(this);
        vault = new VaultPanel(this, pm);

        cards.add(login, "LOGIN");
        cards.add(choice, "CHOICE");
        cards.add(vault, "VAULT");

        setContentPane(cards);
        showLogin();
        setVisible(true);
        
    }

    public void setCurrentUser(String username) {
        this.currentUser = username;
    }

    public String getCurrentUser() {
        return currentUser;
    }
    
    public void showLogin() {
        currentUser = null;
        login.clear();
        layout.show(cards, "LOGIN");
    }

    public void showChoice() {
        choice.setUser(currentUser);
        layout.show(cards, "CHOICE");
    }

    public void showVault(String type) {
        vault.load(currentUser, type);
        layout.show(cards, "VAULT");
    }
}
