import java.awt.*;
import javax.swing.*;

public class ForgotPasswordPanel extends JPanel {

    private final AppFrame app;
    private final Credentials creds;

    private final JTextField usernameField = new JTextField(16);
    private final JTextField emailField = new JTextField(16);

    private final JLabel securityQuestion = new JLabel("Your security question will be here"); // Placeholder, should be replaced with actual security question from user data
    
    private final JTextField codeField = new JTextField(16);
    private final JPasswordField newPasswordField = new JPasswordField(16);
    private final JPasswordField confirmPasswordField = new JPasswordField(16);
    private final JLabel statusLabel = new JLabel(" "); // For displaying status messages

    private final JLabel strengthLabel = UI.subtle("Strength: ");

    private final JButton back = UI.secondaryButton("Back to Login");
    private final JButton reset = UI.accentButton("Verify Account");

    public ForgotPasswordPanel(AppFrame app, Credentials creds) {
        this.app = app;
        this.creds = creds;
        
        //intialize and style input fields
        UI.styleInput(usernameField);
        UI.styleInput(emailField);
        UI.styleInput(codeField);
        UI.styleInput(newPasswordField);
        UI.styleInput(confirmPasswordField);

        codeField.setEnabled(false); // Disable security answer field until account is verified
        newPasswordField.setEnabled(false); // Disable new password field until account is verified
        confirmPasswordField.setEnabled(false); // Disable confirm password field until account is verified

        setLayout(new BorderLayout());

        // Center align text in input fields
        // usernameField.setHorizontalAlignment(JTextField.CENTER);
        // emailField.setHorizontalAlignment(JTextField.CENTER);
        // codeField.setHorizontalAlignment(JTextField.CENTER);
        // newPasswordField.setHorizontalAlignment(JTextField.CENTER);
        // confirmPasswordField.setHorizontalAlignment(JTextField.CENTER);

        JPanel page = new JPanel(new GridBagLayout());
        page.setBackground(UI.BG);

        JPanel card = UI.card();
        card.setLayout(new BorderLayout());

        JPanel form = new JPanel();
        form.setOpaque(false);
        form.setLayout(new BoxLayout(form, BoxLayout.Y_AXIS));

        //title
        JLabel title = UI.h1("Reset Password");
        title.setAlignmentX(Component.CENTER_ALIGNMENT);
        form.add(title);
        UI.space(form, 20);
        //subtitle
        JLabel subtitle = UI.subtle("Enter your username and email with your security question answer to reset your password.");
        subtitle.setAlignmentX(Component.CENTER_ALIGNMENT);
        form.add(subtitle);
        UI.space(form, 24);


        //input fields
        form.add(UI.row("Username", usernameField, 12, 150)); // Adjust gap and label width for better alignment
        UI.space(form, 14);

        form.add(UI.row("Email", emailField, 12, 150)); // Adjust gap and label width for better alignment
        UI.space(form, 14);

        securityQuestion.setAlignmentX(Component.CENTER_ALIGNMENT);//set text to center
        securityQuestion.setForeground(UI.MUTED); // Set text color to red for errors
        form.add(securityQuestion);
        UI.space(form, 14);

        form.add(UI.row("Security Answer", codeField, 12, 150)); 
        UI.space(form, 14);

        form.add(UI.row("New Password", newPasswordField, 12, 150)); // Add extra space after the field
        UI.space(form, 14);
        
        form.add(UI.row("Confirm Password", confirmPasswordField, 12, 150));
        UI.space(form, 14);

        //add show password checkbox
        JCheckBox showPass = new JCheckBox("Show Password");
        showPass.setOpaque(false);
        showPass.setForeground(UI.MUTED);
        showPass.setAlignmentX(Component.CENTER_ALIGNMENT);

        char defaultEcho = newPasswordField.getEchoChar();
        char defaultEchoConfirm = confirmPasswordField.getEchoChar();

        showPass.addActionListener(e -> {
            boolean show = showPass.isSelected();
            newPasswordField.setEchoChar(show ? (char) 0 : defaultEcho);
            confirmPasswordField.setEchoChar(show ? (char) 0 : defaultEchoConfirm);
        });

        form.add(showPass);
        UI.space(form, 16);
        

        //status label
        statusLabel.setAlignmentX(Component.CENTER_ALIGNMENT);//set text to center
        statusLabel.setForeground(new Color(0xE05050));// Set text color to red for errors
        form.add(statusLabel);
        UI.space(form, 14);


        //buttons
        JPanel buttons = new JPanel(new FlowLayout(FlowLayout.CENTER, 10, 0));
        buttons.setOpaque(false);

        buttons.add(back);
        buttons.add(reset);
        form.add(buttons);

        JPanel wrapper = new JPanel(new GridBagLayout());
        wrapper.setOpaque(false);
        wrapper.add(form);

        card.add(wrapper, BorderLayout.CENTER);
        page.add(card); // Add card to page
        add(page, BorderLayout.CENTER); // Add page to panel

        // Action listeners
        back.addActionListener(e -> {
            clear();
            app.showLogin();
        });

        reset.addActionListener(e -> resetP());

        usernameField.addActionListener(e -> resetP()); //for convenience, allow pressing Enter to trigger reset
        emailField.addActionListener(e -> resetP());
        codeField.addActionListener(e -> resetP());
        newPasswordField.addActionListener(e -> resetP());
        confirmPasswordField.addActionListener(e -> resetP());

        newPasswordField.getDocument().addDocumentListener(new javax.swing.event.DocumentListener() {     
            public void insertUpdate(javax.swing.event.DocumentEvent e) {
                updateStrength();
            }
            
            public void removeUpdate(javax.swing.event.DocumentEvent e) {
                updateStrength();
            }

            public void changedUpdate(javax.swing.event.DocumentEvent e) {
                updateStrength();
            }
        });


    }

    public void clear() {
        usernameField.setText("");
        emailField.setText("");
        codeField.setText("");
        newPasswordField.setText("");
        confirmPasswordField.setText("");
        statusLabel.setText(" ");

        usernameField.setEnabled(true);
        emailField.setEnabled(true);
        codeField.setEnabled(false);
        newPasswordField.setEnabled(false);
        confirmPasswordField.setEnabled(false);

        securityQuestion.setText("Enter username and email to load your security question");
        securityQuestion.setForeground(UI.MUTED);

        strengthLabel.setText("Strength: ");
        strengthLabel.setForeground(UI.MUTED);

        statusLabel.setText(" ");
        statusLabel.setForeground(new Color(0xE05050));
    }

    private void updateStrength() {
        String p = new String(newPasswordField.getPassword());

        if (p.isEmpty()) {
            strengthLabel.setText("Strength: ");
            strengthLabel.setForeground(UI.MUTED);
            return;
        }

        String label = PasswordStrengthChecker.getStrengthLabel(p);
        strengthLabel.setText("Strength: " + label);

        switch (label) {
            case "Weak":
                strengthLabel.setForeground(Color.RED);
                break;
            case "Medium":
                strengthLabel.setForeground(Color.ORANGE);
                break;
            default:
                strengthLabel.setForeground(new Color(0, 150, 0));
                break;
        }
    }

    private void resetP() {
        String username = usernameField.getText().trim();
        String email = emailField.getText().trim();
        String code = codeField.getText().trim();
        String newPassword = new String(newPasswordField.getPassword());
        String confirmPassword = new String(confirmPasswordField.getPassword());

        if(!codeField.isEnabled()) { //if the security answer field is still disabled, that means the user has not verified their account yet, so we will check if the username and email are valid and if so, display the security question associated with the account and enable the other fields
            check();
            return;
        }

        // Basic validation
        if (username.isEmpty() || email.isEmpty() || code.isEmpty() || newPassword.isEmpty() || confirmPassword.isEmpty()) {
            statusLabel.setForeground(new Color(0xE05050));
            statusLabel.setText("Please fill in all fields.");
            return;
        }

        if (!newPassword.equals(confirmPassword)) {
            statusLabel.setForeground(new Color(0xE05050));
            statusLabel.setText("Passwords do not match.");
            return;
        }

        if (PasswordStrengthChecker.isWeak(newPassword)) {
            statusLabel.setForeground(new Color(0xE05050));
            statusLabel.setText("Password is too weak.");
            return;
        }

        UserAccount user = creds.getAccount(username);
        if (user == null) {
            statusLabel.setForeground(new Color(0xE05050));
            statusLabel.setText("Account information could not be verified.");
            return;
        }  

        if (!user.getEmail().equalsIgnoreCase(email)) {
            statusLabel.setForeground(new Color(0xE05050));
            statusLabel.setText("Account information could not be verified.");
            return;
        }
        
        if (!user.getSecurityAnswer().equalsIgnoreCase(code)) {
            statusLabel.setText("Security question answer is incorrect.");
            statusLabel.setForeground(new Color(0xE05050)); 
            return;
        }

        //need vertify the security question answer here, but since we dont have security questions implemented yet, for now we will just check if the code matches a hardcoded value, but in the future this should be replaced with a proper security question system where the user can set their own questions and answers during sign up and then verify them here during password reset
        // if (!creds.verifyForgotPassword(username, email, code)) {
        //     statusLabel.setText("Invalid username, email, or security question code.");
        //     return;
        // }

        // Update password
        user.setPassword(newPassword);
        creds.unlockAccount(username); // Unlock account if it was locked due to failed login attempts
        creds.saveToFile(); // Save updated credentials to file, im not sure if this is necessary since the password is hashed and the user object should update the file when setPassword is called, but just to be safe
        
        statusLabel.setText("Password reset successful.");
        statusLabel.setForeground(new Color(0x7CFF00)); // Change text color to green for success 
        
        JOptionPane.showMessageDialog(this, "Password reset successful! You can now log in with your new password.", "Success", JOptionPane.INFORMATION_MESSAGE);// Show success message 
        clear();
        app.showLogin();

    }
    private void check() { //checks if the users and email are valid and if so, displays the security question associated with the account, and allow you to type on other boxes, user would press check acount.
        String username = usernameField.getText().trim();
        String email = emailField.getText().trim();

        if (username.isEmpty() || email.isEmpty()) {
            statusLabel.setForeground(new Color(0xE05050)); 
            statusLabel.setText("Please enter both username and email to check account.");
            return;
        }

        UserAccount user = creds.getAccount(username);
        if (user == null) {
            statusLabel.setForeground(new Color(0xE05050)); 
            statusLabel.setText("Account information could not be verified.");
            return;
        }

        if (!user.getEmail().equalsIgnoreCase(email)) {
            statusLabel.setForeground(new Color(0xE05050)); 
            statusLabel.setText("Account information could not be verified.");
            return;
        }

        // If we reach here, the account and email are valid
        securityQuestion.setText(user.getSecurityQuestion()); // Display the security question associated with the account
        
        
        codeField.setEnabled(true); // Enable security answer field
        newPasswordField.setEnabled(true); // Enable new password field
        confirmPasswordField.setEnabled(true); // Enable confirm password field
        
        usernameField.setEnabled(false); // Disable username field to prevent changes after verification
        emailField.setEnabled(false); // Disable email field to prevent changes after verification

        statusLabel.setForeground(new Color(0x7CFF00)); // Change text color to green for success
        statusLabel.setText("Account verified. Please answer the security question and enter your new password."); // Update status message
        // This method can be used to implement real-time validation as the user types, if desired. For now, it's just a placeholder.
    }
}
