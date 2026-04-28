import java.awt.*;
import javax.swing.*;

public class SignUpPanel extends JPanel {
    private final AppFrame app;
    private final Credentials creds;
    private final InviteCodeManager inviteCodeManager;

    // Sign up fields
    private final JTextField user = new JTextField(24);
    private final JPasswordField pass = new JPasswordField(24);
    private final JTextField email = new JTextField(24);
    private final JTextField securityAnswer = new JTextField(24);
    private final JCheckBox twoFactor = new JCheckBox("Enable Two-Factor Authentication");

    // Dropdowns for account type and security question
    private final JComboBox<String> accountType = new JComboBox<>(new String[] {"Personal", "Business", "Both"});
    private final JComboBox<String> securityQuestion = new JComboBox<>(new String[] {"What is your mother's maiden name?", "What was the name of your first pet?", "What was the make of your first car?", "What city were you born in?"});
    
    // Invite code field for business accounts
    private final JTextField inviteCode = new JTextField(24);
    private final JLabel inviteHelp = UI.subtle("For Business or Both accounts, enter your invite code to receive business access.");

    private final JLabel strengthLabel = UI.subtle("Strength: ");

    public SignUpPanel(AppFrame app, Credentials creds) {
        this.app = app;
        this.creds = creds;
        this.inviteCodeManager = InviteCodeManager.loadFromFile();

        UI.styleInput(user);
        UI.styleInput(pass);
        UI.styleInput(email);
        UI.styleInput(accountType);
        UI.styleInput(securityQuestion);
        UI.styleInput(securityAnswer);
        UI.styleInput(inviteCode);

        setLayout(new BorderLayout());

        JPanel page = new JPanel(new GridBagLayout());
        page.setBackground(UI.BG);

        JPanel card = UI.card();
        card.setLayout(new BoxLayout(card, BoxLayout.Y_AXIS));

        JLabel title = UI.h1("Create Account");
        card.add(title);

        UI.space(card, 8);

        JLabel subtitle = UI.subtle("Fill in the details below to create an account.");
        subtitle.setAlignmentX(Component.CENTER_ALIGNMENT);
        card.add(subtitle);

        UI.space(card, 24);

        // Main input fields
        card.add(UI.row("Username", user));
        UI.space(card, 14);

        card.add(UI.row("Password", pass));
        UI.space(card, 14);

        card.add(UI.row("Email", email));
        UI.space(card, 14);

        card.add(UI.row("Account Type", accountType));
        UI.space(card, 14);

        card.add(UI.row("Security Question", securityQuestion));
        UI.space(card, 14);

        card.add(UI.row("Security Answer", securityAnswer));
        UI.space(card, 14);

        card.add(UI.row("Invite Code", inviteCode));
        UI.space(card, 6);

        inviteHelp.setAlignmentX(Component.CENTER_ALIGNMENT);
        card.add(inviteHelp);
        UI.space(card, 14);

        // 2FA option
        twoFactor.setOpaque(false);
        twoFactor.setForeground(UI.MUTED);
        twoFactor.setAlignmentX(Component.CENTER_ALIGNMENT);
        card.add(twoFactor);

        UI.space(card, 14);

        // Action buttons
        JPanel buttons = new JPanel(new FlowLayout(FlowLayout.CENTER, 10, 0));
        buttons.setOpaque(false);

        JButton signUpBtn = UI.secondaryButton("Sign Up");
        JButton back = UI.secondaryButton("Back to Login");
        JButton reset = UI.secondaryButton("Reset");

        buttons.add(signUpBtn);
        buttons.add(back);
        buttons.add(reset);

        card.add(buttons);
        card.add(Box.createVerticalGlue());

        page.add(card);
        add(page, BorderLayout.CENTER);

        // Button actions
        signUpBtn.addActionListener(e -> doSignUp());
        back.addActionListener(e -> app.showLogin());
        reset.addActionListener(e -> clear());

        accountType.addActionListener(e -> updateInviteControls());
        updateInviteControls();

        pass.getDocument().addDocumentListener(new javax.swing.event.DocumentListener() {
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

    // Clears all form fields
    public void clear() {
        user.setText("");
        pass.setText("");
        email.setText("");
        inviteCode.setText("");
        accountType.setSelectedIndex(0);
        securityQuestion.setSelectedIndex(0);
        securityAnswer.setText("");
        twoFactor.setSelected(false);
        updateInviteControls();

        strengthLabel.setText("Strength: ");
        strengthLabel.setForeground(UI.MUTED);

        updateInviteControls();
    }

    private void updateStrength() {
        String p = new String(pass.getPassword());

        if (p.isEmpty()) {
            strengthLabel.setText("Strenght: ");
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

    // Only enables invite code for business/both accounts
    private void updateInviteControls() {
        String type = ((String) accountType.getSelectedItem()).toLowerCase();
        boolean businessType = "business".equalsIgnoreCase(type) || "both".equalsIgnoreCase(type);

        inviteCode.setEnabled(businessType);
        inviteHelp.setEnabled(businessType);

        if (!businessType) {
            inviteCode.setText("");
        }
    }

    // Sign up: validate input, create account, and show messages
    private void doSignUp() {
        // Variables for input values
        String u = user.getText().trim();
        String p = new String(pass.getPassword()).trim();
        String e = email.getText().trim();
        boolean tfa = twoFactor.isSelected();
        String type = ((String) accountType.getSelectedItem()).toLowerCase();
        String securityQ = ((String) securityQuestion.getSelectedItem()).trim();
        String securityA = securityAnswer.getText().trim();
        String code = inviteCode.getText().trim();

        // Makes sure required fields are filled in
        if (u.isEmpty() || p.isEmpty() || e.isEmpty() || securityQ.isEmpty() || securityA.isEmpty()) { //securityQ.isEmpty() should never be true since it's a dropdown, but we'll check just in case
            JOptionPane.showMessageDialog(this, "All fields are required.");
            return;
        }

        if (PasswordStrengthChecker.isWeak(p)) {
            JOptionPane.showMessageDialog(this, "Password is too weak. Use at least 8 characters with uppercase, lowercase, number, and symbol.");
            return;
        }
        boolean created;
        
        // Try creating the account
        if (("business".equalsIgnoreCase(type) || "both".equalsIgnoreCase(type)) && !code.isEmpty()) {
            created = creds.signUp(u, p, e, type, securityQ, securityA, tfa, code, inviteCodeManager);
        } else {
            created = creds.signUp(u, p, e, type, securityQ, securityA, tfa, "", inviteCodeManager);
        }

        // Show error if sign up failed
        if (!created) {
            if (("business".equalsIgnoreCase(type) || "both".equalsIgnoreCase(type)) && !code.isEmpty()) {
                JOptionPane.showMessageDialog(this, "Failed to create account. The invite code may be invalid or already used.");
            } else {
                JOptionPane.showMessageDialog(this, "Failed to create account. The username may already be taken.");
            }
            return;
        }

        UserAccount account = creds.getAccount(u);
        if (account == null) {
            JOptionPane.showMessageDialog(this, "Account was created, but could not be loaded.");
            app.showLogin();
            return;
        }

        // If 2FA is on, go to setup screen
        if (account.isTwoFactorEnabled()) {
            String secret = account.getTotpSecret();

            if (secret == null || secret.isBlank()) {
                JOptionPane.showMessageDialog(this, "2FA setup could not be started because no secret was generated.");
                return;
            }

            if (account.isBusinessAuthorized()) {
                JOptionPane.showMessageDialog(this, "Account created with Business access. Set up your authenticator app to finish enabling 2FA.");
            } else if ("business".equalsIgnoreCase(type) || "both".equalsIgnoreCase(type)) {
                JOptionPane.showMessageDialog(this, "Account created with Business access. Set up your authenticator app to finish enabling 2FA.");
            } else {
                JOptionPane.showMessageDialog(this, "Account created successfully! Set up your authenticator app to finish enabling 2FA.");
            }

            app.showTwoFactorSetup(u, secret);
        } else {
            // If no 2FA, finish sign up normally
            if (account.isBusinessAuthorized()) {
                JOptionPane.showMessageDialog(this, "Account created successfully and business access was assigned.");
            } else if ("business".equalsIgnoreCase(type) || "both".equalsIgnoreCase(type)) {
                JOptionPane.showMessageDialog(this, "Account created successfully. Business access will remain unavailable until an admin grants access or you use a valid invite code.");
            } else {
                JOptionPane.showMessageDialog(this, "Account created successfully! You can now log in.");
            }

            app.showLogin();
        }
    }
}