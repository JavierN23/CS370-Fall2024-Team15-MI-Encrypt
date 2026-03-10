import java.awt.*;
import java.awt.datatransfer.StringSelection;
import java.util.List;
import javax.swing.*;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;

public class VaultPanel extends JPanel {
    private final AppFrame app;
    private final PasswordManager pm;

    private String user;
    private String type;

    // Header
    private final JLabel title = UI.h1("");

    // Search
    private final JTextField searchField = new JTextField(18);

    // List
    private final DefaultListModel<PasswordEntry> model = new DefaultListModel<>();
    private final JList<PasswordEntry> list = new JList<>(model);

    public VaultPanel(AppFrame app, PasswordManager pm) {
        this.app = app;
        this.pm = pm;

        JPanel page = UI.page();

        // Top bar: title + search + nav buttons
        JPanel top = new JPanel(new BorderLayout(12, 0));
        top.setOpaque(false);

        JPanel left = new JPanel();
        left.setOpaque(false);
        left.setLayout(new BoxLayout(left, BoxLayout.Y_AXIS));

        // Title 

        title.setFont(title.getFont().deriveFont(Font.BOLD, 32f));
        title.setForeground(UI.TEXT);
        title.setAlignmentX(Component.LEFT_ALIGNMENT);
        left.add(title);

        UI.space(left, 10);

        // Search row
        JPanel searchRow = new JPanel(new FlowLayout(FlowLayout.LEFT, 10, 0));
        searchRow.setOpaque(false);

        JLabel searchLabel = new JLabel("Search:");
        searchLabel.setForeground(UI.TEXT);
        searchRow.add(searchLabel);

        UI.styleInput(searchField);
        searchField.setToolTipText("Search by site or username");
        searchField.setPreferredSize(new Dimension(260, 34));
        searchRow.add(searchField);

        left.add(searchRow);

        // Navigation buttons
        JButton back = UI.secondaryButton("Back");
        JButton logout = UI.secondaryButton("Logout");

        JPanel right = new JPanel(new FlowLayout(FlowLayout.RIGHT, 10, 0));
        right.setOpaque(false);
        right.add(back);
        right.add(logout);

        top.add(left, BorderLayout.CENTER);
        top.add(right, BorderLayout.EAST);

        // Center card: entry list
        JPanel card = UI.card();
        card.setLayout(new BorderLayout(10, 10));

        list.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        list.setFixedCellHeight(56);
        list.setCellRenderer(new EntryRenderer());
        list.setBackground(UI.CARD);
        list.setForeground(UI.TEXT);
        list.setBorder(BorderFactory.createEmptyBorder(8, 8, 8, 8));

        JScrollPane scroll = new JScrollPane(list);
        scroll.setOpaque(false);
        scroll.getViewport().setOpaque(true);
        scroll.getViewport().setBackground(UI.CARD);
        scroll.setBorder(BorderFactory.createLineBorder(UI.BORDER, 1, true));
        scroll.getVerticalScrollBar().setUnitIncrement(16);

        card.add(scroll, BorderLayout.CENTER);

        // Bottom actions
        JPanel buttons = new JPanel(new FlowLayout(FlowLayout.LEFT, 10, 0));
        buttons.setOpaque(false);

        JButton add = UI.accentButton("Add");
        JButton view = UI.secondaryButton("View");
        JButton del = UI.deleteButton("Delete");
        JButton copyUser = UI.secondaryButton("Copy Username");
        JButton copyPass = UI.secondaryButton("Copy Password");

        buttons.add(add);
        buttons.add(view);
        buttons.add(del);
        buttons.add(copyUser);
        buttons.add(copyPass);

        // Layout: top, card, bottom
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

        copyUser.addActionListener(e -> copySelectedUsername());
        copyPass.addActionListener(e -> copySelectedPassword());

        // Double click to view entry
        list.addMouseListener(new java.awt.event.MouseAdapter() {
            @Override
            public void mouseClicked(java.awt.event.MouseEvent evt) {
                if (evt.getClickCount() == 2) viewEntry();
            }
        });

        searchField.getDocument().addDocumentListener(new DocumentListener() {
            @Override public void insertUpdate(DocumentEvent e) { refresh(); }
            @Override public void removeUpdate(DocumentEvent e) { refresh(); }
            @Override public void changedUpdate(DocumentEvent e) { refresh(); }
        });
    }

    // Load the specific account 
    public void load(String username, String accountType) {
        this.user = username;
        this.type = accountType;

        title.setText(accountType + " Vault — " + username);
        searchField.setText("");
        refresh();
    }

    // Refresh the list
    private void refresh() {
        model.clear();

        String q = searchField.getText().trim().toLowerCase();
        List<PasswordEntry> entries = pm.getEntriesForUser(user, type);

        for (PasswordEntry e : entries) {
            String hay = (e.getSite() + " " + e.getUsername()).toLowerCase();
            if (q.isEmpty() || hay.contains(q)) {
                model.addElement(e);
            }
        }
    }

    // Add new entry 
    private void addEntry() {
        JTextField site = new JTextField();
        JTextField u = new JTextField();
        JPasswordField p = new JPasswordField();

        UI.styleInput(site);
        UI.styleInput(u);
        UI.styleInput(p);

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

    // View selected entry
    private void viewEntry() {
        PasswordEntry sel = list.getSelectedValue();
        if (sel == null) {
            JOptionPane.showMessageDialog(this, "Select an entry.");
            return;
        }

        JLabel site = new JLabel("Site: " + sel.getSite());
        site.setForeground(UI.TEXT);

        JLabel usr = new JLabel("Username: " + sel.getUsername());
        usr.setForeground(UI.TEXT);

        JLabel pwd = new JLabel("Password: " + mask(sel.getPassword()));
        pwd.setForeground(UI.TEXT);

        JCheckBox show = new JCheckBox("Show password");
        show.setOpaque(false);
        show.setForeground(UI.MUTED);
        show.addActionListener(e ->
                pwd.setText("Password: " + (show.isSelected() ? sel.getPassword() : mask(sel.getPassword())))
        );

        JPanel panel = new JPanel();
        panel.setBackground(UI.BG);
        panel.setBorder(BorderFactory.createEmptyBorder(12, 12, 12, 12));
        panel.setLayout(new BoxLayout(panel, BoxLayout.Y_AXIS));

        panel.add(site);
        panel.add(Box.createVerticalStrut(6));
        panel.add(usr);
        panel.add(Box.createVerticalStrut(6));
        panel.add(pwd);
        panel.add(Box.createVerticalStrut(10));
        panel.add(show);

        JOptionPane.showMessageDialog(this, panel, "Entry", JOptionPane.PLAIN_MESSAGE);
    }

    // Deletes the selected entry after confirmation
    private void deleteEntry() {
        PasswordEntry sel = list.getSelectedValue();
        if (sel == null) {
            JOptionPane.showMessageDialog(this, "Select an entry.");
            return;
        }

        int ok = JOptionPane.showConfirmDialog(this, "Delete this entry?", "Confirm", JOptionPane.YES_NO_OPTION);
        if (ok != JOptionPane.YES_OPTION) return;

        // If filtered, find it in the full list first
        List<PasswordEntry> full = pm.getEntriesForUser(user, type);
        int realIndex = full.indexOf(sel);

        if (realIndex >= 0) {
            pm.removeEntry(user, type, realIndex);
            refresh();
        }
    }

    // Copy username to clipboard
    private void copySelectedUsername() {
        PasswordEntry sel = list.getSelectedValue();
        if (sel == null) {
            JOptionPane.showMessageDialog(this, "Select an entry first.");
            return;
        }
        copyToClipboard(sel.getUsername());
        JOptionPane.showMessageDialog(this, "Username copied.");
    }

    // Copy password to clipboard
    private void copySelectedPassword() {
        PasswordEntry sel = list.getSelectedValue();
        if (sel == null) {
            JOptionPane.showMessageDialog(this, "Select an entry first.");
            return;
        }
        copyToClipboard(sel.getPassword());
        JOptionPane.showMessageDialog(this, "Password copied.");
    }

    // Copy text to clipboard
    private void copyToClipboard(String text) {
        Toolkit.getDefaultToolkit()
                .getSystemClipboard()
                .setContents(new StringSelection(text), null);
    }


    // Mask a password
    private String mask(String s) {
        if (s == null) return "";
        int n = Math.max(6, Math.min(12, s.length()));
        return "•".repeat(n);
    }

    // password entry renderer for JList
    private static class EntryRenderer extends JPanel implements ListCellRenderer<PasswordEntry> {
        private final JLabel site = new JLabel();
        private final JLabel user = new JLabel();

        EntryRenderer() {
            setLayout(new BoxLayout(this, BoxLayout.Y_AXIS));
            setBorder(BorderFactory.createEmptyBorder(10, 12, 10, 12));
            setOpaque(true);

            site.setForeground(UI.TEXT);
            site.setFont(site.getFont().deriveFont(Font.BOLD, 14f));

            user.setForeground(UI.MUTED);
            user.setFont(user.getFont().deriveFont(Font.PLAIN, 12f));

            add(site);
            add(Box.createVerticalStrut(2));
            add(user);
        }

        @Override
        public Component getListCellRendererComponent(
                JList<? extends PasswordEntry> list,
                PasswordEntry value,
                int index,
                boolean isSelected,
                boolean cellHasFocus
        ) {
            site.setText(value.getSite());
            user.setText(value.getUsername());

            // Selection highlight
            setBackground(isSelected ? new Color(60, 90, 130) : UI.CARD);
            return this;
        }
    }
}