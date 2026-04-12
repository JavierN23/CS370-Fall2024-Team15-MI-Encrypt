import java.awt.*;
import javax.swing.*;

public class ChoicePanel extends JPanel {
    private final AppFrame app;
    private final Credentials creds;

    private final JLabel title = UI.h1("");
    private final JLabel businessStatus = UI.subtle("");

    // Action buttons
    private final JButton personal = UI.accentButton("Personal Vault");
    private final JButton business = UI.accentButton("Business Vault");
    private final JButton adminPanel = UI.secondaryButton("Admin Panel");
    private final JButton account = UI.secondaryButton("Account Settings");
    private final JButton logout = UI.secondaryButton("Logout");

    public ChoicePanel(AppFrame app, Credentials creds) {
        this.app = app;
        this.creds = creds;

        setLayout(new BorderLayout());

        JPanel page = new JPanel(new GridBagLayout());
        page.setBackground(UI.BG);

        JPanel card = UI.card();
        card.setLayout(new BorderLayout());

        JPanel content = new JPanel();
        content.setOpaque(false);
        content.setLayout(new BoxLayout(content, BoxLayout.Y_AXIS));

        title.setAlignmentX(Component.CENTER_ALIGNMENT);
        content.add(title);

        UI.space(content, 10);

        JLabel subtitle = UI.subtle("Select a vault to manage your passwords:");
        subtitle.setAlignmentX(Component.CENTER_ALIGNMENT);
        content.add(subtitle);

        UI.space(content, 24);

        // Center align buttons
        personal.setAlignmentX(Component.CENTER_ALIGNMENT);
        business.setAlignmentX(Component.CENTER_ALIGNMENT);
        adminPanel.setAlignmentX(Component.CENTER_ALIGNMENT);
        businessStatus.setAlignmentX(Component.CENTER_ALIGNMENT);
        account.setAlignmentX(Component.CENTER_ALIGNMENT);
        logout.setAlignmentX(Component.CENTER_ALIGNMENT);

        content.add(personal);
        UI.space(content, 12);

        content.add(business);
        UI.space(content, 12);

        content.add(adminPanel);
        UI.space(content, 12);

        content.add(businessStatus);
        UI.space(content, 12);

        content.add(account);
        UI.space(content, 12);

        content.add(logout);

        JPanel centerWrap = new JPanel(new GridBagLayout());
        centerWrap.setOpaque(false);
        centerWrap.add(content);

        card.add(centerWrap, BorderLayout.CENTER);
        page.add(card);

        add(page, BorderLayout.CENTER);

        // When I click the buttons, I want to go to the Specific account or back to login
        personal.addActionListener(e -> app.showVault("Personal"));
        business.addActionListener(e -> app.showVault("Business"));
        adminPanel.addActionListener(e -> app.showAdmin());
        account.addActionListener(e -> app.showAccount());
        logout.addActionListener(e -> app.showLogin());
    }

    public void setUser(String username) {
        title.setText("Hi, " + username + "!");

        UserAccount accountInfo = creds.getAccount(username);
        if (accountInfo == null) {
            personal.setVisible(false);
            personal.setEnabled(false);
            
            business.setVisible(false);
            business.setEnabled(false);

            adminPanel.setVisible(false);
            adminPanel.setEnabled(false);

            businessStatus.setVisible(false);
            return;
        }

        String type = accountInfo.getAccountType();

        personal.setVisible(false);
        personal.setEnabled(false);

        business.setVisible(false);
        business.setEnabled(false);
        
        adminPanel.setVisible(false);
        adminPanel.setEnabled(false);

        businessStatus.setVisible(false);
        businessStatus.setText("");

    if ("personal".equalsIgnoreCase(type)) {
        personal.setVisible(true);
        personal.setEnabled(true);

    } else if ("business".equalsIgnoreCase(type)) {
        business.setVisible(true);
        business.setEnabled(true);

        if (!accountInfo.isBusinessAuthorized()) {
            businessStatus.setText("Business Vault access is limited until authorization is granted.");
            businessStatus.setVisible(true);
        }

    } else if ("both".equalsIgnoreCase(type)) {
        personal.setVisible(true);
        personal.setEnabled(true);

        business.setVisible(true);
        business.setEnabled(true);

        if (!accountInfo.isBusinessAuthorized()) {
            businessStatus.setText("Business Vault access is limited until authorization is granted.");
            businessStatus.setVisible(true);
            }
        }
        if (("business".equalsIgnoreCase(type) || "both".equalsIgnoreCase(type))
                && accountInfo.isBusinessAuthorized()
                && accountInfo.isBusinessAdmin()) {
            adminPanel.setVisible(true);
            adminPanel.setEnabled(true);
        }
    }
}