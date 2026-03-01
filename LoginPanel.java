import java.awt.*;
import javax.swing.*;

public class LoginPanel extends JPanel {
    private final AppFrame app;
    private final Credentials creds;

    private final JTextField user = new JTextField(18);
    private final JPasswordField pass = new JPasswordField(18);

    public LoginPanel(AppFrame app, Credentials creds) {
        this.app = app;
        this.creds = creds;

        JPanel page = UI.page();
        JPanel card = UI.card();

        card.add(UI.h1("Login"));
        UI.space(card, 8);
        card.add(UI.subtle("Log in or create an account to continue."));
        UI.space(card, 18);

        card.add(UI.row(new JLabel("Username"), user));
        UI.space(card, 10);
        card.add(UI.row(new JLabel("Password"), pass));
        UI.space(card, 16);

        JPanel buttons = new JPanel(new FlowLayout(FlowLayout.CENTER, 10, 0));
        buttons.setOpaque(false);

        JButton login = new JButton("Login");
        JButton signup = new JButton("Sign Up");
        JButton reset = new JButton("Reset");

        buttons.add(login);
        buttons.add(signup);
        buttons.add(reset);

        card.add(buttons);

        page.add(Box.createVerticalGlue());
        page.add(card);
        page.add(Box.createVerticalGlue());

        setLayout(new BorderLayout());
        add(page, BorderLayout.CENTER);

        login.addActionListener(e -> doLogin());
        signup.addActionListener(e -> doSignup());
        reset.addActionListener(e -> clear());
        pass.addActionListener(e -> doLogin());
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