import java.awt.*;
import java.awt.image.BufferedImage;
import javax.swing.*;

public class LoginPanel extends JPanel {
    private final AppFrame app;
    private final Credentials creds;

    private final JTextField user = new JTextField(18);
    private final JPasswordField pass = new JPasswordField(18);

    public LoginPanel(AppFrame app, Credentials creds) {
        this.app = app;
        this.creds = creds;

        UI.styleInput(user);
        UI.styleInput(pass);

        setLayout(new BorderLayout());

        JPanel page = UI.page();

        JPanel twoCol = new JPanel(new GridLayout(1, 2, 40, 0));
        twoCol.setOpaque(false);

        JPanel card = UI.card();
        card.setLayout(new BoxLayout(card, BoxLayout.Y_AXIS));

        card.add(Box.createVerticalGlue());

        JLabel title = UI.h1("Login");
        card.add(title);

        UI.space(card, 6);

        JLabel subtitle = UI.subtle("Log in or create an account to continue.");
        subtitle.setAlignmentX(Component.CENTER_ALIGNMENT);
        card.add(subtitle);

        UI.space(card, 24);

        card.add(UI.row(UI.label("Username"), user));
        UI.space(card, 15);

        card.add(UI.row(UI.label("Password"), pass));
        UI.space(card, 10);

        JCheckBox showPass = new JCheckBox("Show Password");
        showPass.setOpaque(false);
        showPass.setForeground(UI.MUTED);

        char defaultEcho = pass.getEchoChar();
        showPass.addActionListener(e ->
                pass.setEchoChar(showPass.isSelected() ? (char) 0 : defaultEcho)
        );

        card.add(showPass);
        UI.space(card, 16);

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

        JLabel logo = new JLabel(loadLogoResource(
                "/MI_Encrypt.png", 420
        ));
        logo.setHorizontalAlignment(SwingConstants.CENTER);
        logoPanel.add(logo, BorderLayout.CENTER);

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

        pass.addActionListener(e -> doLogin());

        SwingUtilities.invokeLater(() -> {
            JRootPane rp = SwingUtilities.getRootPane(LoginPanel.this);
            if (rp != null) rp.setDefaultButton(login);
        });
    }

    private Icon loadLogoResource(String resourcePath, int targetWidth) {
    java.net.URL url = getClass().getResource(resourcePath);

    if (url == null) {
        JLabel fallback = new JLabel("MI Encrypt");
        fallback.setForeground(UI.MUTED);
        fallback.setFont(fallback.getFont().deriveFont(Font.BOLD, 32f));
        return fallbackToIcon(fallback, targetWidth, 200);
    }

    ImageIcon icon = new ImageIcon(url);
    int w = icon.getIconWidth();
    int h = icon.getIconHeight();
    int targetHeight = (int) Math.round((double) targetWidth * h / w);

    Image scaled = icon.getImage().getScaledInstance(targetWidth, targetHeight, Image.SCALE_SMOOTH);
    return new ImageIcon(scaled);
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

    private Icon fallbackToIcon(JLabel label, int w, int h) {
        label.setSize(w, h);
        Image img = new BufferedImage(w, h, BufferedImage.TYPE_INT_ARGB);
        Graphics2D g2 = (Graphics2D) img.getGraphics();
        label.paint(g2);
        g2.dispose();
        return new ImageIcon(img);
    }

    public void clear() {
        user.setText("");
        pass.setText("");
    }

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