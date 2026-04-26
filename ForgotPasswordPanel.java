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

        JPanel page = UI.page();
        page.setBackground(UI.BG);
        JPanel card = UI.card();
        card.setLayout(new BoxLayout(card, BoxLayout.Y_AXIS));
        //card.add(Box.createVerticalGlue()); // Push content to the center vertically

        //title
        JLabel title = UI.h1("Reset Password");
        card.add(title);
        UI.space(card, 20);
        //subtitle
        JLabel subtitle = UI.subtle("Enter your username and email with your security question answer to reset your password.");
        subtitle.setAlignmentX(Component.CENTER_ALIGNMENT);
        card.add(subtitle);
        UI.space(card, 24);


        //input fields
        card.add(UI.row("Username", usernameField, 12, 150)); // Adjust gap and label width for better alignment
        UI.space(card, 14);

        card.add(UI.row("Email", emailField, 12, 150)); // Adjust gap and label width for better alignment
        UI.space(card, 14);

        securityQuestion.setAlignmentX(Component.CENTER_ALIGNMENT);//set text to center
        securityQuestion.setForeground(new Color(0x7CFF00));// Set text color to red for errors
        card.add(securityQuestion);
        UI.space(card, 14);

        card.add(UI.row("Security Answer", codeField, 12, 150)); 
        UI.space(card, 14);

            card.add(UI.row("New Password", newPasswordField, 12, 150)); // Add extra space after the field
        UI.space(card, 14);
        
        card.add(UI.row("Confirm Password", confirmPasswordField, 12, 150));
        UI.space(card, 14);

        //add show password checkbox
                JCheckBox showPass = new JCheckBox("Show Password");
        showPass.setOpaque(false);
        showPass.setForeground(UI.MUTED);
        showPass.setAlignmentX(Component.CENTER_ALIGNMENT);

        char defaultEcho = newPasswordField.getEchoChar();
        showPass.addActionListener(e ->
                newPasswordField.setEchoChar(showPass.isSelected() ? (char) 0 : defaultEcho)
        );

        char defaultEchoConfirm = confirmPasswordField.getEchoChar();
        showPass.addActionListener(e ->
                confirmPasswordField.setEchoChar(showPass.isSelected() ? (char) 0 : defaultEchoConfirm)
        );

        card.add(showPass);
        UI.space(card, 16);
        

        //status label
        statusLabel.setAlignmentX(Component.CENTER_ALIGNMENT);//set text to center
        statusLabel.setForeground(new Color(0xE05050));// Set text color to red for errors
        card.add(statusLabel); //delete later
        UI.space(card, 14);


        //buttons
        JPanel buttons = new JPanel(new FlowLayout(FlowLayout.CENTER, 10, 0));
        buttons.setOpaque(false);

        JButton back = UI.secondaryButton("Back to Login");
        JButton reset = UI.accentButton("Reset Password");
        buttons.add(back);
        buttons.add(reset);
        card.add(buttons);

        card.add(Box.createVerticalGlue()); //i think this and the one above are causing spacing 
        page.add(card); // Add card to page
        add(page, BorderLayout.CENTER); // Add page to panel

        // Action listeners
        back.addActionListener(e -> app.showLogin());
        reset.addActionListener(e -> resetP());
        usernameField.addActionListener(e -> resetP()); //for convenience, allow pressing Enter to trigger reset
        emailField.addActionListener(e -> resetP());
        codeField.addActionListener(e -> resetP());
        newPasswordField.addActionListener(e -> resetP());
        confirmPasswordField.addActionListener(e -> resetP());


    }

    public void clear() {
        usernameField.setText("");
        emailField.setText("");
        codeField.setText("");
        newPasswordField.setText("");
        confirmPasswordField.setText("");
        statusLabel.setText(" ");
    }

    private void resetP() {
        String username = usernameField.getText().trim();
        String email = emailField.getText().trim();
        String code = codeField.getText().trim();
        String newPassword = new String(newPasswordField.getPassword());
        String confirmPassword = new String(confirmPasswordField.getPassword());

        if(codeField.isEnabled() == false) { //if the security answer field is still disabled, that means the user has not verified their account yet, so we will check if the username and email are valid and if so, display the security question associated with the account and enable the other fields
            check();
            return;
        }

        // Basic validation
        if (username.isEmpty() || email.isEmpty() || code.isEmpty() || newPassword.isEmpty() || confirmPassword.isEmpty()) {
            statusLabel.setText("Please fill in all fields.");
            return;
        }

        if (!newPassword.equals(confirmPassword)) {
            statusLabel.setText("Passwords do not match.");
            return;
        }

        UserAccount user = creds.getAccount(username);
        if (user == null) {
            statusLabel.setText("No account found with that username.");
            return;
        }  

        if (!user.getEmail().equalsIgnoreCase(email)) {
            statusLabel.setText("Email does not match our records.");
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
            statusLabel.setText("Security question answer is correct.");
            statusLabel.setForeground(new Color(0x7CFF00)); // Change text color to green for success 
        JOptionPane.showMessageDialog(this, "Password reset successful! You can now log in with your new password.", "Success", JOptionPane.INFORMATION_MESSAGE);// Show success message 
        app.showLogin();

    }
    private void check() { //checks if the users and email are valid and if so, displays the security question associated with the account, and allow you to type on other boxes, user would press check acount.
        String username = usernameField.getText().trim();
        String email = emailField.getText().trim();

        if (username.isEmpty() || email.isEmpty()) {
            statusLabel.setText("Please enter both username and email to check account.");
            return;
        }

        UserAccount user = creds.getAccount(username);
        if (user == null) {
            statusLabel.setText("Account or email not found.");
            return;
        }

        if (!user.getEmail().equalsIgnoreCase(email)) {
            statusLabel.setText("Account or email not found.");
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

