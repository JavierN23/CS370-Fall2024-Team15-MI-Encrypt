import javax.swing.*;

public class BusinessAccountPage {
    public BusinessAccountPage(Credentials creds, String username) {
        JFrame frame = new JFrame("Business Account Page");
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.setSize(400, 300);

        JLabel label = new JLabel("Welcome to your Business Account, " + username + "!");
        frame.add(label);

        frame.setLocationRelativeTo(null);
        frame.setVisible(true);
    }
}