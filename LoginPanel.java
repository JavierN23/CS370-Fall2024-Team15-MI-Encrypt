import java.awt.*;
import java.awt.image.BufferedImage;
import javax.swing.*;

public class LoginPanel extends JPanel {
    private final AppFrame app;
    private final Credentials creds;

    // Input fields
    private final JTextField user = new JTextField(18);
    private final JPasswordField pass = new JPasswordField(18);

    public LoginPanel(AppFrame app, Credentials creds) {
        this.app = app;
        this.creds = creds;

        // Style inputs
        UI.styleInput(user);
        UI.styleInput(pass);

        setLayout(new BorderLayout());

        JPanel page = UI.page();

        JPanel twoCol = new JPanel(new GridLayout(1, 2, 40, 0));
        twoCol.setOpaque(false);

        JPanel card = UI.card();
        card.setLayout(new BoxLayout(card, BoxLayout.Y_AXIS));

        card.add(Box.createVerticalGlue());

        // Login form
        JLabel title = UI.h1("Login");
        card.add(title);

        UI.space(card, 6);

        JLabel subtitle = UI.subtle("Log in or create an account to continue.");
        subtitle.setAlignmentX(Component.CENTER_ALIGNMENT);
        card.add(subtitle);

        UI.space(card, 24);

        // Username Field
        card.add(UI.row(UI.label("Username"), user));
        UI.space(card, 14);

        // Password Field
        card.add(UI.row(UI.label("Password"), pass));
        UI.space(card, 10);

        // Show password checkbox
        JCheckBox showPass = new JCheckBox("Show Password");
        showPass.setOpaque(false);
        showPass.setForeground(UI.MUTED);

        char defaultEcho = pass.getEchoChar();
        showPass.addActionListener(e ->
                pass.setEchoChar(showPass.isSelected() ? (char) 0 : defaultEcho)
        );

        card.add(showPass);
        UI.space(card, 16);

        // Action buttons
        JPanel buttons = new JPanel(new FlowLayout(FlowLayout.CENTER, 10, 0));
        buttons.setOpaque(false);

        JButton login = UI.accentButton("Login");
        JButton signup = UI.secondaryButton("Sign Up");
        JButton reset = UI.secondaryButton("Reset");

        buttons.add(login);
        buttons.add(signup);
        buttons.add(reset);

        card.add(buttons);
        card.add(Box.createVerticalGlue());

        // Logo panel
        JPanel logoPanel = new JPanel(new BorderLayout());
        logoPanel.setOpaque(false);
        logoPanel.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createMatteBorder(0, 1, 0, 0, UI.BORDER),
                BorderFactory.createEmptyBorder(0, 24, 0, 24)
        ));

        JLabel logo = new JLabel(loadLogoFile(
                "/home/javi/VS Code/MI Encrypt/MI Encrypt/src/MI_Encrypt.png", 420
        ));
        logo.setHorizontalAlignment(SwingConstants.CENTER);
        logoPanel.add(logo, BorderLayout.CENTER);

        // Two column layout
        twoCol.add(card);
        twoCol.add(logoPanel);

        page.add(Box.createVerticalGlue());
        page.add(twoCol);
        page.add(Box.createVerticalGlue());

        add(page, BorderLayout.CENTER);

        // Actions
        login.addActionListener(e -> doLogin());
        signup.addActionListener(e -> doSignup());
        reset.addActionListener(e -> clear());

        // Pressing Enter in password field triggers login
        pass.addActionListener(e -> doLogin());

        // Set login button as default when this panel is shown
        SwingUtilities.invokeLater(() -> {
            JRootPane rp = SwingUtilities.getRootPane(LoginPanel.this);
            if (rp != null) rp.setDefaultButton(login);
        });
    }

    private Icon loadLogoFile(String path, int targetWidth) {
        ImageIcon icon = new ImageIcon(path);

        if (icon.getIconWidth() <= 0) {
            JLabel fallback = new JLabel("MI Encrypt");
            fallback.setForeground(UI.MUTED);
            fallback.setFont(fallback.getFont().deriveFont(Font.BOLD, 32f));
            return fallbackToIcon(fallback, targetWidth, 200);
        }

        int w = icon.getIconWidth();
        int h = icon.getIconHeight();
        int targetHeight = (int) Math.round((double) targetWidth * h / w);

        Image scaled = icon.getImage().getScaledInstance(targetWidth, targetHeight, Image.SCALE_SMOOTH);
        return new ImageIcon(scaled);
    }

    // If it can't load the image, it will load "MI Encrypt" text as a fallback logo
    private Icon fallbackToIcon(JLabel label, int w, int h) {
        label.setSize(w, h);
        Image img = new BufferedImage(w, h, BufferedImage.TYPE_INT_ARGB);
        Graphics2D g2 = (Graphics2D) img.getGraphics();
        label.paint(g2);
        g2.dispose();
        return new ImageIcon(img);
    }

    // Clears the input fields
    public void clear() {
        user.setText("");
        pass.setText("");
    }

    // Handles the login logic
    private void doLogin() {
        String u = user.getText().trim();
        String p = new String(pass.getPassword()).trim();

        if (u.isEmpty() || p.isEmpty()) {
            JOptionPane.showMessageDialog(this, "Enter both username and password.");
            return;
        }

        if (creds.login(u, p)) {
            app.setCurrentUser(u);
            JOptionPane.showMessageDialog(this, "Login successful!");
            app.showChoice();
        } else {
            JOptionPane.showMessageDialog(this, "Invalid username or password.");
        }
    }

    // Handles new account creation
    private void doSignup() {
        String u = user.getText().trim();
        String p = new String(pass.getPassword()).trim();

        if (u.isEmpty() || p.isEmpty()) {
            JOptionPane.showMessageDialog(this, "Enter both username and password.");
            return;
        }

        if (creds.signUp(u, p)) {
            JOptionPane.showMessageDialog(this, "Account created! You can now log in.");
        } else {
            JOptionPane.showMessageDialog(this, "Username already exists.");
        }
    }
}