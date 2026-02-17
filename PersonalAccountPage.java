import javax.swing.*;

public class PersonalAccountPage {
    public PersonalAccountPage(Credentials creds, String username) {
        JFrame frame = new JFrame("Personal Account Page");
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.setSize(400, 300);
        frame.setLayout(null);

        JLabel label = new JLabel("Welcome to your Personal Account, " + username + "!");
        label.setBounds(100, 30, 300, 25);
        frame.add(label);

        frame.setLocationRelativeTo(null);
        frame.setVisible(true);
    }
}