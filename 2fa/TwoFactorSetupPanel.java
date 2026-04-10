import java.awt.*;
import java.awt.image.BufferedImage;
import java.net.URL;
import javax.imageio.ImageIO;
import javax.swing.*;

/**
 Shown once, right after a user signs up with 2FA enabled.
 Steps:
    1. Display the QR code (fetched from qrserver.com) so the user can scan it
       with Google Authenticator, Authy, Microsoft Authenticator, etc.
    2. Show the plaintext secret as a fallback for manual entry.
    3. Ask the user to enter their first code to confirm the app is linked.
 
  After a successful verification the user is sent to the login screen.
 */
public class TwoFactorSetupPanel extends JPanel {

    private final AppFrame app;
    private final Credentials creds;

    // Dynamic fields populated in setup()
    private String currentUsername;
    private String currentSecret;

    private final JLabel qrLabel = new JLabel("Loading QR code…", SwingConstants.CENTER);
    private final JLabel secretLabel = new JLabel("", SwingConstants.CENTER);
    private final JTextField codeField = new JTextField(8);
    private final JLabel statusLabel = new JLabel(" ", SwingConstants.CENTER);

    public TwoFactorSetupPanel(AppFrame app, Credentials creds) {
        this.app = app;
        this.creds = creds;

        UI.styleInput(codeField);
        codeField.setHorizontalAlignment(JTextField.CENTER);

        setLayout(new BorderLayout());

        JPanel page = UI.page();
        JPanel card = UI.card();
        card.setLayout(new BoxLayout(card, BoxLayout.Y_AXIS));

        // Title 
        card.add(Box.createVerticalGlue());

        JLabel title = UI.h1("Set Up Authenticator");
        card.add(title);

        UI.space(card, 6);

        JLabel subtitle = UI.subtle(
                "Scan the QR code with your authenticator app, then enter the 6-digit code to confirm.");
        subtitle.setAlignmentX(Component.CENTER_ALIGNMENT);
        card.add(subtitle);

        UI.space(card, 20);

        // QR code image
        qrLabel.setAlignmentX(Component.CENTER_ALIGNMENT);
        qrLabel.setPreferredSize(new Dimension(200, 200));
        qrLabel.setMaximumSize(new Dimension(200, 200));
        qrLabel.setForeground(UI.MUTED);
        card.add(qrLabel);

        UI.space(card, 10);

        // Manual entry secret 
        JLabel manualHint = UI.subtle("Can't scan? Enter this key manually:");
        manualHint.setAlignmentX(Component.CENTER_ALIGNMENT);
        card.add(manualHint);

        UI.space(card, 4);

        secretLabel.setAlignmentX(Component.CENTER_ALIGNMENT);
        secretLabel.setForeground(UI.MUTED);
        secretLabel.setFont(new Font(Font.MONOSPACED, Font.BOLD, 13));
        card.add(secretLabel);

        UI.space(card, 18);

        // Verification field
        card.add(UI.row(UI.label("Enter Code"), codeField));

        UI.space(card, 6);

        statusLabel.setAlignmentX(Component.CENTER_ALIGNMENT);
        statusLabel.setForeground(new Color(0xE05050)); // soft red for errors
        card.add(statusLabel);

        UI.space(card, 14);

        // Buttons 
        JPanel buttons = new JPanel(new FlowLayout(FlowLayout.CENTER, 10, 0));
        buttons.setOpaque(false);

        JButton verify = UI.accentButton("Verify & Continue");
        JButton cancel = UI.secondaryButton("Cancel");

        buttons.add(verify);
        buttons.add(cancel);
        card.add(buttons);

        card.add(Box.createVerticalGlue());

        page.add(Box.createVerticalGlue());
        page.add(card);
        page.add(Box.createVerticalGlue());

        add(page, BorderLayout.CENTER);

        // Actions
        verify.addActionListener(e -> doVerify());
        cancel.addActionListener(e -> {
            // Delete the partially-created account and go back to login
            creds.deleteAccount(currentUsername);
            app.showLogin();
        });

        codeField.addActionListener(e -> doVerify());
    }

    // Called by AppFrame.showTwoFactorSetup() each time this panel is shown

    public void setup(String username, String secret) {
        this.currentUsername = username;
        this.currentSecret = secret;

        // Show the raw secret (formatted in groups of 4 for readability)
        secretLabel.setText(formatSecret(secret));

        // Reset state
        codeField.setText("");
        statusLabel.setText(" ");
        qrLabel.setIcon(null);
        qrLabel.setText("Loading QR code…");

        // Fetch the QR code image on a background thread so the UI stays responsive
        String otpauthUri = TOTPUtil.buildOtpauthUri("MI Encrypt", username, secret);
        String qrImageUrl = TOTPUtil.buildQrImageUrl(otpauthUri);

        SwingWorker<ImageIcon, Void> worker = new SwingWorker<>() {
            @Override
            protected ImageIcon doInBackground() {
                try {
                    URL url = new URL(qrImageUrl);
                    BufferedImage img = ImageIO.read(url);
                    if (img != null) return new ImageIcon(img);
                } catch (Exception ex) {
                    System.out.println("QR fetch failed: " + ex.getMessage());
                }
                return null;
            }

            @Override
            protected void done() {
                try {
                    ImageIcon icon = get();
                    if (icon != null) {
                        qrLabel.setIcon(icon);
                        qrLabel.setText("");
                    } else {
                        qrLabel.setText("<html><center>QR unavailable — use the key below</center></html>");
                    }
                } catch (Exception ignored) {
                    qrLabel.setText("<html><center>QR unavailable — use the key below</center></html>");
                }
            }
        };
        worker.execute();
    }

    // Verify the first TOTP code to confirm the app is correctly linked

    private void doVerify() {
        String code = codeField.getText().trim();

        if (code.length() != 6 || !code.matches("\\d{6}")) {
            statusLabel.setText("Enter the 6-digit code from your authenticator app.");
            return;
        }

        if (TOTPUtil.verify(currentSecret, code)) {
            JOptionPane.showMessageDialog(this, "Two-factor authentication is set up successfully!\n"
            + "You will be asked for a code every time you log in.",
            "2FA Enabled",
            JOptionPane.INFORMATION_MESSAGE);
            app.showLogin();
        } else {
            statusLabel.setText("Incorrect code. Check the time on your device and try again.");
            codeField.selectAll();
        }
    }

    // Insert a space every 4 characters so the secret is easier to read
    private static String formatSecret(String secret) {
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < secret.length(); i++) {
            if (i > 0 && i % 4 == 0) sb.append(' ');
            sb.append(secret.charAt(i));
        }
        return sb.toString();
    }
}
