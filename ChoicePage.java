import javax.swing.*;
import java.awt.event.*;

public class ChoicePage implements ActionListener {

    private JFrame frame;
    private JButton businessButton, personalButton;

    private Credentials creds;
    private String username;

    public ChoicePage(Credentials creds, String username) {
        this.creds = creds;
        this.username = username;

        frame = new JFrame ("Choose Account Type");
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.setSize(400, 300);
        frame.setLayout(null);

        JLabel label = new JLabel("Welcome, " + username + "! Please choose your account type:");
        label.setBounds(49, 20, 380, 25);
        frame.add(label);

        businessButton = new JButton("Business Account");
        businessButton.setBounds(130, 80, 180, 30);
        businessButton.addActionListener(this);
        frame.add(businessButton);

        personalButton = new JButton("Personal Account");
        personalButton.setBounds(130, 130, 180, 30);
        personalButton.addActionListener(this);
        frame.add(personalButton);

        frame.setLocationRelativeTo(null);
        frame.setVisible(true);
    }
    @Override
    public void actionPerformed(ActionEvent e) {
        if  (e.getSource() == businessButton) {
            frame.dispose();
            SwingUtilities.invokeLater(() -> new BusinessAccountPage(creds, username));
        } 
        if (e.getSource() == personalButton) {
            frame.dispose();
            SwingUtilities.invokeLater(() -> new PersonalAccountPage(creds, username));
        }
    }
}