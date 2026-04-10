import java.awt.*;
import javax.swing.*;

public class SignUpPanel extends JPanel {
    private final AppFrame app;
    private final Credentials creds;

    private final JTextField user = new JTextField(18);
    private final JPasswordField pass = new JPasswordField(18);
    private final JTextField email = new JTextField(18);
    private final JCheckBox twoFactor = new JCheckBox("Enable Two-Factor Authentication");

    public SignUpPanel(AppFrame app, Credentials creds) {
        this.app = app;
        this.creds = creds;

        UI.styleInput(user);
        UI.styleInput(pass);
        UI.styleInput(email);

        setLayout(new BorderLayout());

        JPanel page = UI.page();
        JPanel card = UI.card();
        card.setLayout(new BoxLayout(card, BoxLayout.Y_AXIS));

        card.add(Box.createVerticalGlue());

        JLabel title = UI.h1("Create Account");
        card.add(title);

        UI.space(card, 8);

        JLabel subtitle = UI.subtle("Fill in the details below to create an account.");
        subtitle.setAlignmentX(Component.CENTER_ALIGNMENT);
        card.add(subtitle);

        UI.space(card, 24);

        card.add(UI.row(UI.label("Username"), user));
        UI.space(card, 15);

        card.add(UI.row(UI.label("Password"), pass));
        UI.space(card, 15);

        card.add(UI.row(UI.label("Email"), email));
        UI.space(card, 15);

        twoFactor.setOpaque(false);
        twoFactor.setForeground(UI.MUTED);
        twoFactor.setAlignmentX(Component.CENTER_ALIGNMENT);
        card.add(twoFactor);

        UI.space(card, 20);

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

        page.add(Box.createVerticalGlue());
        page.add(card);
        page.add(Box.createVerticalGlue());

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
        twoFactor.setSelected(false);
    }

    // Sign up: validate input, create account, and show messages
    private void doSignUp() {
        String u = user.getText().trim();
        String p = new String(pass.getPassword());
        String e = email.getText().trim();
        boolean tfa = twoFactor.isSelected();

        if (u.isEmpty() || p.isEmpty() || e.isEmpty()) {
            JOptionPane.showMessageDialog(this, "All fields are required.");
            return;
        }

        if (creds.signUp(u, p, e, tfa)) {
            if (tfa) {
                // Retrieve the TOTP secret that Credentials generated
                String secret = creds.getAccount(u).getTotpSecret();
                // Show it  one time on 2FA setup screen
                app.showTwoFactorSetup(u, secret);
            } else {
                JOptionPane.showMessageDialog(this, "Account created successfully! You can now log in.");
                app.showLogin();
            }
        } else {
            JOptionPane.showMessageDialog(this, "Username already exists. Please choose another.");
        }
    }
}
