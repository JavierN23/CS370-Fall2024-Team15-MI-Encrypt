import java.awt.*;
import javax.swing.*;

public class AccountPanel extends JPanel {
    private final AppFrame app;
    private final Credentials creds;

    // Account info fields
    private final JLabel usernameValue = new JLabel();
    private final JTextField emailField = new JTextField(24);
    private final JComboBox<String> accountType = new JComboBox<>(new String[] {"Personal", "Business", "Both"});
    private final JComboBox<String> securityQuestion = new JComboBox<>(new String[] {"What is your mother's maiden name?", "What was the name of your first pet?", "What was the make of your first car?", "What city were you born in?"});
    
    private final JTextField securityAnswerField = new JTextField(24);
    private final JCheckBox twoFactor = new JCheckBox("Enable Two-Factor Authentication");
    private final JCheckBox showPasswords = new JCheckBox("Show Passwords");

    private final JPasswordField oldPasswordField = new JPasswordField(20);
    private final JPasswordField newPasswordField = new JPasswordField(20);

    private char oldEchoChar;
    private char newEchoChar;
    
    private String loadedUser;


    public AccountPanel(AppFrame app, Credentials creds) {
        this.app = app;
        this.creds = creds;

        // UI Style inputs
         UI.styleInput(emailField);
         UI.styleInput(accountType);
         UI.styleInput(securityQuestion);
         UI.styleInput(securityAnswerField);
         UI.styleInput(oldPasswordField);
         UI.styleInput(newPasswordField);

        oldEchoChar = oldPasswordField.getEchoChar();
        newEchoChar = newPasswordField.getEchoChar();

        // Main panel layout
        setLayout(new BorderLayout());

        JPanel page = new JPanel(new GridBagLayout());
        page.setBackground(UI.BG);

        JPanel card = UI.card();
        card.setLayout(new BoxLayout(card, BoxLayout.Y_AXIS));
        // Main page title
        JLabel title = UI.h1("Account Settings");

        card.add(title);
        UI.space(card, 8);

        JLabel subtitle = UI.subtle("Manage your account information and password settings.");
        subtitle.setAlignmentX(Component.CENTER_ALIGNMENT);

        card.add(subtitle);
        UI.space(card, 18);

        // Username display
        usernameValue.setForeground(UI.TEXT);
        usernameValue.setAlignmentX(Component.CENTER_ALIGNMENT);
        card.add(UI.row("Username", usernameValue));
        UI.space(card, 10);

        // Edit Email
        card.add(UI.row("Email", emailField));
        UI.space(card, 10);

        // Account Type Selector
        card.add(UI.row("Account Type", accountType));
        UI.space(card, 10);

        // Security Question and Answer
        card.add(UI.row("Security Question", securityQuestion));
        UI.space(card, 10);
        card.add(UI.row("Security Answer", securityAnswerField));
        UI.space(card, 10);

        // Two-factor authentication toggle
        twoFactor.setOpaque(false);
        twoFactor.setForeground(UI.MUTED);
        twoFactor.setAlignmentX(Component.CENTER_ALIGNMENT);
        card.add(twoFactor);
        UI.space(card, 16);

        // Password change section
        JLabel pwTitle = UI.label("Change Password");
        pwTitle.setAlignmentX(Component.CENTER_ALIGNMENT);
        card.add(pwTitle);
        UI.space(card, 10);

        // Old and new password fields
        card.add(UI.row("Old Password", oldPasswordField));
        UI.space(card, 10);

        card.add(UI.row("New Password", newPasswordField));
        UI.space(card, 8);
        // Hide/show password checkbox
        card.add(showPasswords);
        UI.space(card, 8);

        showPasswords.setOpaque(false);
        showPasswords.setForeground(UI.MUTED);
        showPasswords.setAlignmentX(Component.CENTER_ALIGNMENT);

        // Toggle password visibility
        char oldDefaultEcho = oldPasswordField.getEchoChar();
        char newDefaultEcho = newPasswordField.getEchoChar();
        // Ensures password is only visible when checkbox is clicked
        showPasswords.addActionListener(e -> {
            if (showPasswords.isSelected()) {
                oldPasswordField.setEchoChar((char) 0);
                newPasswordField.setEchoChar((char) 0);
            } else {
                oldPasswordField.setEchoChar(oldDefaultEcho);
                newPasswordField.setEchoChar(newDefaultEcho);
            }
        });
        // Button row for actions
        JPanel buttons = new JPanel(new FlowLayout(FlowLayout.CENTER, 10, 3));
        buttons.setOpaque(false);

        JButton saveInfoBtn = UI.accentButton("Save Info");
        JButton changePasswordBtn = UI.secondaryButton("Change Password");
        JButton deleteBtn = UI.deleteButton("Delete Account");
        JButton backBtn = UI.secondaryButton("Back");

        buttons.add(saveInfoBtn);
        buttons.add(changePasswordBtn);
        buttons.add(deleteBtn);
        buttons.add(backBtn);

        card.add(buttons);
        card.add(Box.createVerticalGlue());

        page.add(card);

        add(page, BorderLayout.CENTER);
        // Button actions
        saveInfoBtn.addActionListener(e -> saveInfo());
        changePasswordBtn.addActionListener(e -> changePassword());
        deleteBtn.addActionListener(e -> deleteAccount());
        backBtn.addActionListener(e -> app.showChoice());
    }

    public void loadUser(String username) {
        loadedUser = username;

        UserAccount account = creds.getAccount(username);
        if (account == null) {
            JOptionPane.showMessageDialog(this, "Unable to load account.");
            return;
        }

        usernameValue.setText(account.getUsername());
        emailField.setText(account.getEmail());
        securityQuestion.setSelectedItem(account.getSecurityQuestion());
        securityAnswerField.setText(account.getSecurityAnswer());

        // Match account type to dropdown selection
        String type = account.getAccountType();
        if ("business".equalsIgnoreCase(type)) {
            accountType.setSelectedItem("Business");
        } else if ("both".equalsIgnoreCase(type)) {
            accountType.setSelectedItem("Both");
        } else {
            accountType.setSelectedItem("Personal");
        }
        // Loading current 2FA status into checkbox
        twoFactor.setSelected(account.isTwoFactorEnabled());
        oldPasswordField.setText("");
        newPasswordField.setText("");
    }

    private void saveInfo() {
        if (loadedUser == null) {
            JOptionPane.showMessageDialog(this, "No user loaded.");
            return;
        }

        // Load current account state to compare changes and handle 2FA enabling
        UserAccount before = creds.getAccount(loadedUser);
        if (before == null) {
            JOptionPane.showMessageDialog(this, "Unable to load account.");
            return;
        }

        boolean wasTwoFactorEnabled = before.isTwoFactorEnabled();

        // Rear update vaules from UI fields
        String email = emailField.getText().trim();
        String securityQuestion = (String) this.securityQuestion.getSelectedItem();
        String securityAnswer = securityAnswerField.getText().trim();

        String type = ((String) accountType.getSelectedItem()).toLowerCase();
        boolean tfa = twoFactor.isSelected();

        // Basic validation
        if (email.isEmpty()) {
            JOptionPane.showMessageDialog(this, "Email cannot be empty.");
            return;
        }

        boolean updated = creds.updateAccountInfo(loadedUser, email, type, securityQuestion, securityAnswer, tfa);
        if (!updated) {
            JOptionPane.showMessageDialog(this, "Unable to update account information. Check that the email is valid and account type is one of Personal, Business, or Both.");
            return;
        }

        UserAccount after = creds.getAccount(loadedUser);
        if (after == null) {
            JOptionPane.showMessageDialog(this, "Account was updated, but could not be loaded.");
            return;
        }

        // Handle 2FA enabling: if 2FA was just enabled, prompt user to complete setup
        if (!wasTwoFactorEnabled && after.isTwoFactorEnabled()) {
            String secret = after.getTotpSecret();
            if (secret == null || secret.isBlank()) {
                JOptionPane.showMessageDialog(this, "2FA was enabled, but the authenticator secret is missing.");
                return;
            }

            JOptionPane.showMessageDialog(this, "Account information updated. Complete authenticator setup to finish enabling 2FA.");
            app.showTwoFactorSetup(loadedUser, secret);
            return;
        }

        // Handle 2FA disabling: if 2FA was just disabled, show confirmation
        if (wasTwoFactorEnabled && !after.isTwoFactorEnabled()) {
            JOptionPane.showMessageDialog(this, "Two-factor authentication has been disabled.");
            return;
        }

        JOptionPane.showMessageDialog(this, "Account information updated successfully.");
    }

    private void changePassword() {
        if (loadedUser == null) {
            JOptionPane.showMessageDialog(this, "No user loaded.");
            return;
        }

        // Get old and new password values from fields
        String oldPassword = new String(oldPasswordField.getPassword()).trim();
        String newPassword = new String(newPasswordField.getPassword()).trim();

        if (oldPassword.isEmpty() || newPassword.isEmpty()) {
            JOptionPane.showMessageDialog(this, "Enter both old and new passwords.");
            return;
        }

        if (creds.changePassword(loadedUser, oldPassword, newPassword)) {
            oldPasswordField.setText("");
            newPasswordField.setText("");

            showPasswords.setSelected(false);
            oldPasswordField.setEchoChar(oldEchoChar);
            newPasswordField.setEchoChar(newEchoChar);

            JOptionPane.showMessageDialog(this, "Password changed successfully.");
        } else {
            JOptionPane.showMessageDialog(this, "Old password is incorrect or new password is invalid.");
        }
    }

    private void deleteAccount() {
        if (loadedUser == null) {
            JOptionPane.showMessageDialog(this, "No user loaded.");
            return;
        }

        // Confirm delete action with user
        int confirm = JOptionPane.showConfirmDialog(
                this,
                "Are you sure you want to delete this account?",
                "Confirm Delete",
                JOptionPane.YES_NO_OPTION
        );

        // If user does not confirm, do nothing
        if (confirm != JOptionPane.YES_OPTION) {
            return;
        }

        // Attempt to delete account and show result
        if (creds.deleteAccount(loadedUser)) {
            JOptionPane.showMessageDialog(this, "Account deleted.");
            app.showLogin();
        } else {
            JOptionPane.showMessageDialog(this, "Unable to delete account.");
        }
    }
}