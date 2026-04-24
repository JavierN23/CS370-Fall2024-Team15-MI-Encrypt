import javax.swing.SwingUtilities;

public class Main {
    public static void main(String[] args) {

        UI.applyGlobalStyle();

        Credentials creds = Credentials.loadFromFile(); // Create an instance of the Credentials class to manage user credentials
        PasswordManager pm = PasswordManager.loadFromFile(); // Create an instance of the PasswordManager class to manage passwords
        CreditCardManager ccm = CreditCardManager.loadFromFile(); // Load saved credit cards

        SwingUtilities.invokeLater(() -> new AppFrame(creds, pm, ccm)); // Create and display the login page.
        
    }
}
