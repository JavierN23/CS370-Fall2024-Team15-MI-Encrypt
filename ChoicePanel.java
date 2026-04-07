import java.awt.*;
import javax.swing.*;

public class ChoicePanel extends JPanel {
    private final AppFrame app;
    private final JLabel title = UI.h1("");

    public ChoicePanel(AppFrame app) {
        this.app = app;

        JPanel page = UI.page();
        JPanel card = UI.card();

        card.add(title);
        UI.space(card, 8);
        card.add(UI.subtle("Choose where you want to manage entries:"));
        UI.space(card, 18);

        // Action buttons
        JButton personal = new JButton("Personal Vault");
        JButton business = new JButton("Business Vault");
        JButton logout = new JButton("Logout");

        // Center align buttons
        personal.setAlignmentX(Component.CENTER_ALIGNMENT);
        business.setAlignmentX(Component.CENTER_ALIGNMENT);
        logout.setAlignmentX(Component.CENTER_ALIGNMENT);

        card.add(personal);
        UI.space(card, 10);
        card.add(business);
        UI.space(card, 18);
        card.add(logout);

        page.add(Box.createVerticalGlue());
        page.add(card);
        page.add(Box.createVerticalGlue());

        setLayout(new BorderLayout());
        add(page, BorderLayout.CENTER);

        // When I click the buttons, I want to go to the Specific account or back to login
personal.addActionListener(e -> {
    SessionManager.updateActivity();
    app.showVault("Personal");
});

business.addActionListener(e -> {
    SessionManager.updateActivity();
    app.showVault("Business");
});

logout.addActionListener(e -> {
    SessionManager.endSession();
    app.showLogin();
});

    }

    public void setUser(String username) {
        title.setText("Hi, " + username + "!");
    }
}
