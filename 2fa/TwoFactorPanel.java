import java.awt.*;
import javax.swing.*;

public class TwoFactorPanel extends JPanel {

    private final AppFrame app;
    private final Credentials creds;

    private String pendingUsername; 

    private final JTextField codeField = new JTextField(8);
    private final JLabel statusLabel = new JLabel(" ", SwingConstants.CENTER);

    public TwoFactorPanel(AppFrame app, Credentials creds) {
        this.app = app;
        this.creds = creds;

        UI.styleInput(codeField);
        codeField.setHorizontalAlignment(JTextField.CENTER);

        setLayout(new BorderLayout());

        JPanel page = UI.page();
        JPanel card = UI.card();
        card.setLayout(new BoxLayout(card, BoxLayout.Y_AXIS));

        card.add(Box.createVerticalGlue());

        // Title
        JLabel title = UI.h1("Two-Factor Authentication");
        card.add(title);

        UI.space(card, 6);

        JLabel subtitle = UI.subtle(
                "Open your authenticator app and enter the 6-digit code for MI Encrypt.");
        subtitle.setAlignmentX(Component.CENTER_ALIGNMENT);
        card.add(subtitle);

        UI.space(card, 28);

        // Code field
        card.add(UI.row(UI.label("Authenticator Code"), codeField));

        UI.space(card, 8);

        statusLabel.setAlignmentX(Component.CENTER_ALIGNMENT);
        statusLabel.setForeground(new Color(0xE05050));
        card.add(statusLabel);

        UI.space(card, 20);

        // Buttons
        JPanel buttons = new JPanel(new FlowLayout(FlowLayout.CENTER, 10, 0));
        buttons.setOpaque(false);

        JButton verify = UI.accentButton("Verify");
        JButton back = UI.secondaryButton("Back to Login");

        buttons.add(verify);
        buttons.add(back);
        card.add(buttons);

        card.add(Box.createVerticalGlue());

        page.add(Box.createVerticalGlue());
        page.add(card);
        page.add(Box.createVerticalGlue());

        add(page, BorderLayout.CENTER);

        // Actions
        verify.addActionListener(e -> doVerify());
        back.addActionListener(e -> app.showLogin());
        codeField.addActionListener(e -> doVerify());
    }

    // Called by AppFrame.showTwoFactorVerify() before the card is shown

    public void setup(String username) {
        this.pendingUsername = username;
        codeField.setText("");
        statusLabel.setText(" ");
    }

    // Verify the TOTP code and proceed

    private void doVerify() {
        String code = codeField.getText().trim();

        if (code.length() != 6 || !code.matches("\\d{6}")) {
            statusLabel.setText("Please enter the 6-digit code from your authenticator app.");
            return;
        }

        UserAccount account = creds.getAccount(pendingUsername);
        if (account == null || account.getTotpSecret() == null) {
            // Shouldn't happen, but fall back gracefully
            app.setCurrentUser(pendingUsername);
            JOptionPane.showMessageDialog(this, "Login successful!");
            app.showChoice();
            return;
        }

        if (TOTPUtil.verify(account.getTotpSecret(), code)) {
            app.setCurrentUser(pendingUsername);
            JOptionPane.showMessageDialog(this, "Login successful!");
            app.showChoice();
        } else {
            statusLabel.setText("Incorrect code. Wait for the next code and try again.");
            codeField.selectAll();
        }
    }
}
