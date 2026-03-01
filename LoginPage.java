import java.awt.event.*;
import javax.swing.*;
import java.awt.even.*;
import javax.swing.*;
import java.awt.*;

public class LoginPage implements ActionListener {

    private JFrame frame;
    private JButton loginButton, signUpButton, resetButton;
    private JTextField userIDField;
    private JPasswordField userPasswordField;
    private JLabel userIDLabel, userPasswordLabel;
    private Credentials creds;
    private PasswordManager pm;

    public LoginPage(Credentials creds, PasswordManager pm) {
        this.creds = creds;
        this.pm = pm;
 
        frame = new JFrame("Login"); // For the Login Page
        frame.setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
        frame.setSize(400, 300);
        frame.setLayout(null);

        userIDLabel = new JLabel("Username:"); // Username Label
        userIDLabel.setBounds(50, 50, 80, 25);
        frame.add(userIDLabel);

        userIDField = new JTextField();
        userIDField.setBounds(150, 50, 165, 25);
        frame.add(userIDField);

        userPasswordLabel = new JLabel("Password:"); // Password Label
        userPasswordLabel.setBounds(50, 100, 80, 25);
        frame.add(userPasswordLabel);

        userPasswordField = new JPasswordField();
        userPasswordField.setBounds(150, 100, 165, 25);
        frame.add(userPasswordField);

        loginButton = new JButton("Login"); // Login Button
        loginButton.setBounds(20, 150, 80, 25);
        loginButton.addActionListener(this);
        frame.add(loginButton);

        signUpButton = new JButton("Sign Up"); // Sign Up Button to create new accounts
        signUpButton.setBounds(153, 150, 100, 25);
        signUpButton.addActionListener(this);
        frame.add(signUpButton);

        resetButton = new JButton("Reset"); // Reset Button to clear the fields
        resetButton.setBounds(300, 150, 80, 25);
        resetButton.addActionListener(this);
        frame.add(resetButton);

        frame.setLocationRelativeTo(null);
        frame.setVisible(true);
    }

    @Override
    public void actionPerformed(ActionEvent e) {
        String username = userIDField.getText().trim();
        String password = new String(userPasswordField.getPassword()).trim();

        if (e.getSource() == loginButton) {
            if (username.isEmpty() || password.isEmpty()) {
                JOptionPane.showMessageDialog(frame, "Please enter both username and password.");
                return;
            }

            if (creds.login(username, password)) {
                JOptionPane.showMessageDialog(frame, "Login successful!");
                frame.dispose();
                SwingUtilities.invokeLater(() -> new ChoicePage(creds, pm, username));
            } else {
                JOptionPane.showMessageDialog(frame, "Invalid username or password. Please try again.");
            }

        } else if (e.getSource() == signUpButton) {
            if (username.isEmpty() || password.isEmpty()) {
                JOptionPane.showMessageDialog(frame, "Please enter both username and password to sign up.");
                return;
            }

            if (creds.signUp(username, password)) {
                JOptionPane.showMessageDialog(frame, "Account created successfully! You can now log in.");
            } else {
                JOptionPane.showMessageDialog(frame, "Username already exists. Please choose a different username.");
            }

        } else if (e.getSource() == resetButton) {
            userIDField.setText("");
            userPasswordField.setText("");
        }
    }
}
