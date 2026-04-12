import java.awt.*;
import javax.swing.*;

public class SignUpPanel extends JPanel {
    private final AppFrame app;
    private final Credentials creds;

    private final JTextField user = new JTextField(24);
    private final JPasswordField pass = new JPasswordField(24);
    private final JTextField email = new JTextField(24);
    private final JCheckBox twoFactor = new JCheckBox("Enable Two-Factor Authentication");

    private final JComboBox<String> accountType = new JComboBox<>(new String[] {"Personal", "Business", "Both"});

    public SignUpPanel(AppFrame app, Credentials creds) {
        this.app = app;
        this.creds = creds;

        UI.styleInput(user);
        UI.styleInput(pass);
        UI.styleInput(email);
        UI.styleInput(accountType);
        
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

        JLabel usernameLabel = UI.label("Username");
        JLabel passwordLabel = UI.label("Password");
        JLabel emailLabel = UI.label("Email");

        Dimension labelSize = new Dimension(100, usernameLabel.getPreferredSize().height);
        usernameLabel.setPreferredSize(labelSize);
        passwordLabel.setPreferredSize(labelSize);
        emailLabel.setPreferredSize(labelSize);

        card.add(UI.row("Username", user));
        UI.space(card, 14);

        card.add(UI.row("Password", pass));
        UI.space(card, 14);

        card.add(UI.row("Email", email));
        UI.space(card, 14);

        card.add(UI.row("Account Type", accountType));
        UI.space(card, 14);

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
    }

    public void clear() {
        user.setText("");
        pass.setText("");
        email.setText("");
        accountType.setSelectedIndex(0);
        twoFactor.setSelected(false);
    }

    // Sign up: validate input, create account, and show messages
    private void doSignUp() {
        // Variables for input values
        String u = user.getText().trim();
        String p = new String(pass.getPassword()).trim();
        String e = email.getText().trim();
        boolean tfa = twoFactor.isSelected();
        String type = ((String) accountType.getSelectedItem()).toLowerCase();

        if (u.isEmpty() || p.isEmpty() || e.isEmpty()) {
            JOptionPane.showMessageDialog(this, "All fields are required.");
            return;
        }

        boolean created = creds.signUp(u, p, e, type, tfa);
        if (!created) {
            JOptionPane.showMessageDialog(this, "Username already exists or account type is invalid.");
            return;
        }

        UserAccount account = creds.getAccount(u);
        if (account == null) {
            JOptionPane.showMessageDialog(this, "Account was created, but could not be loaded.");
            app.showLogin();
            return;
        }

        if (account.isTwoFactorEnabled()) {
            String secret = account.getTotpSecret();

            if (secret == null || secret.isBlank()) {
                JOptionPane.showMessageDialog(this, "2FA setup could not be started because no secret was generated.");
                return;
            }

            JOptionPane.showMessageDialog(this, "Account created. Set up your authenticator app to finish enabling 2FA.");
            app.showTwoFactorSetup(u, secret);
        } else {
            JOptionPane.showMessageDialog(this, "Account created successfully! You can now log in.");
            app.showLogin();
        }
    }
}