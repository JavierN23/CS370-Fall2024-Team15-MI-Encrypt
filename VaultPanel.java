import javax.swing.*;
import java.awt.*;

public class VaultPanel extends JPanel {
    private final AppFrame app;
    private final PasswordManager pm;

    private String user;
    private String type;

    private final JLabel title = UI.h1("");

    private final DefaultListModel<PasswordEntry> model = new DefaultListModel<>();
    private final JList<PasswordEntry> list = new JList<>(model);

    public VaultPanel(AppFrame app, PasswordManager pm) {
        this.app = app;
        this.pm = pm;

        JPanel page = UI.page();

        // Top bar: title + Back/Logout
        JPanel top = new JPanel(new BorderLayout());
        top.setOpaque(false);

        JButton back = new JButton("Back");
        JButton logout = new JButton("Logout");

        JPanel right = new JPanel(new FlowLayout(FlowLayout.RIGHT, 10, 0));
        right.setOpaque(false);
        right.add(back);
        right.add(logout);

        top.add(title, BorderLayout.WEST);
        top.add(right, BorderLayout.EAST);

        // List area (card)
        JPanel card = UI.card();
        card.setLayout(new BorderLayout(10, 10));
        card.add(new JScrollPane(list), BorderLayout.CENTER);

        // Bottom buttons
        JPanel buttons = new JPanel(new FlowLayout(FlowLayout.LEFT, 10, 0));
        buttons.setOpaque(false);

        JButton add = new JButton("Add");
        JButton view = new JButton("View");
        JButton del = new JButton("Delete");

        buttons.add(add);
        buttons.add(view);
        buttons.add(del);

        page.add(top);
        UI.space(page, 12);
        page.add(card);
        UI.space(page, 12);
        page.add(buttons);

        setLayout(new BorderLayout());
        add(page, BorderLayout.CENTER);

        // Actions
        back.addActionListener(e -> app.showChoice());
        logout.addActionListener(e -> app.showLogin());
        add.addActionListener(e -> addEntry());
        view.addActionListener(e -> viewEntry());
        del.addActionListener(e -> deleteEntry());
    }

    public void load(String username, String accountType) {
        this.user = username;
        this.type = accountType;
        title.setText(accountType + " Vault");
        refresh();
    }

    private void refresh() {
        model.clear();
        for (PasswordEntry e : pm.getEntriesForUser(user, type)) {
            model.addElement(e);
        }
    }

    private void addEntry() {
        JTextField site = new JTextField();
        JTextField u = new JTextField();
        JPasswordField p = new JPasswordField();

        Object[] msg = {"Site:", site, "Username:", u, "Password:", p};
        int ok = JOptionPane.showConfirmDialog(this, msg, "Add Entry", JOptionPane.OK_CANCEL_OPTION);
        if (ok != JOptionPane.OK_OPTION) return;

        String s = site.getText().trim();
        String un = u.getText().trim();
        String pw = new String(p.getPassword()).trim();

        if (s.isEmpty() || un.isEmpty() || pw.isEmpty()) {
            JOptionPane.showMessageDialog(this, "All fields required.");
            return;
        }

        pm.addEntry(user, type, new PasswordEntry(s, un, pw));
        refresh();
    }

    private void viewEntry() {
        PasswordEntry sel = list.getSelectedValue();
        if (sel == null) {
            JOptionPane.showMessageDialog(this, "Select an entry.");
            return;
        }
        JOptionPane.showMessageDialog(this,
                "Site: " + sel.getSite() +
                "\nUsername: " + sel.getUsername() +
                "\nPassword: " + sel.getPassword());
    }

    private void deleteEntry() {
        int idx = list.getSelectedIndex();
        if (idx == -1) {
            JOptionPane.showMessageDialog(this, "Select an entry.");
            return;
        }
        pm.removeEntry(user, type, idx);
        refresh();
    }
}