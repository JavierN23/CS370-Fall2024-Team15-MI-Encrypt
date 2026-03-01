import javax.swing.SwingUtilities;

public class Main {
    public static void main(String[] args) {

        Credentials creds = Credentials.loadFromFile(); // Create an instance of the Credentials class to manage user credentials
        PasswordManager pm = PasswordManager.loadFromFile(); // Create an instance of the PasswordManager class to manage passwords

        SwingUtilities.invokeLater(() -> new LoginPage(creds, pm)); // Create and display the login page.
        
    }
}
