import javax.swing.SwingUtilities;

public class Main {
    public static void main(String[] args) {

        Credentials creds = new Credentials(); // Create an instance of the Credentials class to manage user credentials
        
       SwingUtilities.invokeLater(() -> new LoginPage(creds)); // Create and display the login page.
        
    }
}
