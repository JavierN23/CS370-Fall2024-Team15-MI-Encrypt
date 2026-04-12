import java.awt.*;
import java.awt.datatransfer.StringSelection;
import java.util.List;
import javax.swing.*;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;

public class VaultPanel extends JPanel {
    private final AppFrame app;
    private final PasswordManager pm;
    private final Credentials creds;

    private String user;
    private String type;

    // Header
    private final JLabel title = UI.h1("");
    private final JLabel accessStatus = UI.subtle("");

    // Search
    private final JTextField searchField = new JTextField(18);

    // List
    private final DefaultListModel<PasswordEntry> model = new DefaultListModel<>();
    private final JList<PasswordEntry> list = new JList<>(model);

    // Buttons
    private final JButton add = UI.accentButton("Add");
    private final JButton view = UI.secondaryButton("View");
    private final JButton edit = UI.secondaryButton("Edit");
    private final JButton del = UI.deleteButton("Delete");
    private final JButton copyUser = UI.secondaryButton("Copy Username");
    private final JButton copyPass = UI.secondaryButton("Copy Password");
    private final JButton generateBtn = UI.secondaryButton("Generate Password");

    // Scaling
    private final Dimension baseSize = new Dimension(1000, 700);
    private float lastScale = 1.0f;

    private final EntryRenderer renderer = new EntryRenderer();

    public VaultPanel(AppFrame app, PasswordManager pm, Credentials creds) {
        this.app = app;
        this.pm = pm;
        this.creds = creds;


        // Use BorderLayout page so CENTER grows when maximized
        JPanel page = UI.pageBorder();

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

        UI.space(left, 6);

        accessStatus.setAlignmentX(Component.LEFT_ALIGNMENT);
        accessStatus.setVisible(false);
        left.add(accessStatus);

        UI.space(left, 12);

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

        // Center card
        JPanel card = UI.card();
        card.setLayout(new BorderLayout(10, 10));

        list.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        list.setFixedCellHeight(56);
        list.setCellRenderer(renderer);
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
        JPanel buttons = new JPanel(new FlowLayout(FlowLayout.CENTER, 10, 0));
        buttons.setOpaque(false);

        buttons.add(add);
        buttons.add(view);
        buttons.add(edit);
        buttons.add(del);
        buttons.add(copyUser);
        buttons.add(copyPass);
        buttons.add(generateBtn);

        // Layout placement
        page.add(top, BorderLayout.NORTH);
        page.add(card, BorderLayout.CENTER);
        page.add(buttons, BorderLayout.SOUTH);

        setLayout(new BorderLayout());
        add(page, BorderLayout.CENTER);

        // Actions
        back.addActionListener(e -> app.showChoice());
        logout.addActionListener(e -> app.showLogin());

        add.addActionListener(e -> addEntry());
        view.addActionListener(e -> viewEntry());
        edit.addActionListener(e -> editEntry()); // editEntry saves
        del.addActionListener(e -> deleteEntry());

        copyUser.addActionListener(e -> copySelectedUsername());
        copyPass.addActionListener(e -> copySelectedPassword());
        generateBtn.addActionListener (e -> showGeneratedPasswordDialog());

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

        // Resize -> scale UI 
        addComponentListener(new java.awt.event.ComponentAdapter() {
            @Override
            public void componentResized(java.awt.event.ComponentEvent e) {
                int w = Math.max(getWidth(), 1);
                int h = Math.max(getHeight(), 1);

                float sx = w / (float) baseSize.width;
                float sy = h / (float) baseSize.height;

                float scale = Math.min(sx, sy);
                scale = Math.max(0.70f, Math.min(1.40f, scale));

                applyScale(scale);
            }
        });

        SwingUtilities.invokeLater(() -> {
            int w = getWidth() > 0 ? getWidth() : baseSize.width;
            int h = getHeight() > 0 ? getHeight() : baseSize.height;
            float s = Math.min(w / (float) baseSize.width, h / (float) baseSize.height);
            s = Math.max(0.70f, Math.min(1.40f, s));
            applyScale(s);
        });
    }

    // Load the specific account
    public void load(String username, String accountType) {
        this.user = username;
        this.type = accountType;

        UserAccount account = creds.getAccount(username);

        if (account == null) {
            JOptionPane.showMessageDialog(this, "User account not found.");
            app.showChoice();
            return;
        }

        if ("Business".equalsIgnoreCase(accountType)) {
            String role = account.getBusinessRole();
            if (role == null || role.trim().isEmpty()) {
                role = "employee";
            }
            title.setText("Business Vault — " + role + " — " + username);

            if (!account.isBusinessAuthorized()) {
                accessStatus.setText("Business Access Pending. Please contact your administrator.");
                accessStatus.setVisible(true);
            } else if (account.isBusinessAdmin()) {
                accessStatus.setText("Full business vault access.");
                accessStatus.setVisible(true);
            } else {
                accessStatus.setText("Read-only access to authorized business groups.");
                accessStatus.setVisible(true);
            }
        } else {
            title.setText("Personal Vault — " + username);
            accessStatus.setText("");
            accessStatus.setVisible(false);
        }

        searchField.setText("");
        updatePermissions();
        refresh();
    }

    private void refresh() {
        model.clear();

        String q = searchField.getText().trim().toLowerCase();
        UserAccount account = creds.getAccount(user);

        if (account == null) {
            return;
        }

        List<PasswordEntry> entries = pm.getVisibleEntries(account, type);

        for (PasswordEntry e : entries) {
            String hay = (e.getSite() + " " + e.getUsername()).toLowerCase();
            if (q.isEmpty() || hay.contains(q)) {
                model.addElement(e);
            }
        }
    }

    private void updatePermissions() {
        boolean canView = canViewBusinessVault();
        boolean canModify = canModifyBusinessVault();
        boolean isBusinessVault = "Business".equalsIgnoreCase(type);

        if (!isBusinessVault) {
            add.setEnabled(true);
            edit.setEnabled(true);
            del.setEnabled(true);
            view.setEnabled(true);
            copyUser.setEnabled(true);
            copyPass.setEnabled(true);
            generateBtn.setEnabled(true);
            return;
        }
        
        view.setEnabled(canView);
        copyUser.setEnabled(canView);
        copyPass.setEnabled(canView);

        add.setEnabled(canModify);
        edit.setEnabled(canModify);
        del.setEnabled(canModify);
        generateBtn.setEnabled(canModify);
    }

    private boolean canViewBusinessVault() {
        if (!"Business".equalsIgnoreCase(type)) {
            return true;
        }

        UserAccount account = creds.getAccount(user);
        return pm.canAccessBusinessVault(account);
    }

    private boolean canModifyBusinessVault() {
        if (!"Business".equalsIgnoreCase(type)) {
            return true;
        }

        UserAccount account = creds.getAccount(user);
        return pm.canModifyBusinessVault(account);
    }


    // Apply scale to key UI elements
    public void applyScale(float scale) {
        if (Math.abs(scale - lastScale) < 0.03f) return;
        lastScale = scale;

        title.setFont(title.getFont().deriveFont(Font.BOLD, 32f * scale));
        searchField.setPreferredSize(new Dimension((int)(260 * scale), (int)(34 * scale)));
        list.setFixedCellHeight((int)(56 * scale));

        renderer.setScale(scale);
        list.setCellRenderer(renderer);

        revalidate();
        repaint();
    }

    // Add new entry
    private void addEntry() {
        UserAccount account = creds.getAccount(user);
        if (account == null) {
            JOptionPane.showMessageDialog(this, "User account not found.");
            return;
        }

        if ("Business".equalsIgnoreCase(type) && !canModifyBusinessVault()) {
            JOptionPane.showMessageDialog(this, "You don't have permission to modify this vault.");
            return;
        }

        JTextField site = new JTextField();
        JTextField u = new JTextField();
        JPasswordField p = new JPasswordField();

        UI.styleInput(site);
        UI.styleInput(u);
        UI.styleInput(p);

        JButton generateInsideBtn = UI.secondaryButton("Generate Password");
        JButton copy = UI.secondaryButton("Copy Generated");

        JPanel passwordButtons = new JPanel(new FlowLayout(FlowLayout.LEFT, 10, 0));
        passwordButtons.setOpaque(false);
        passwordButtons.add(generateInsideBtn);
        passwordButtons.add(copy);

        JPanel passwordPanel = new JPanel();
        passwordPanel.setOpaque(false);
        passwordPanel.setLayout(new BoxLayout(passwordPanel, BoxLayout.Y_AXIS));
        passwordPanel.add(p);
        passwordPanel.add(Box.createVerticalStrut(6));
        passwordPanel.add(passwordButtons);

        generateInsideBtn.addActionListener(e -> p.setText(PasswordUtils.generatePassword()));
        copy.addActionListener(e -> {
            String pw = new String(p.getPassword());
            if (pw.isEmpty()) {
                JOptionPane.showMessageDialog(this, "No Password to copy.");
                return;
            }
            copyToClipboard(pw);
            JOptionPane.showMessageDialog(this, "Generated password copied.");
        });

        if ("Business".equalsIgnoreCase(type)) {
            JTextField groupField = new JTextField();
            UI.styleInput(groupField);

            Object[] msg = {
                "Site:", site,
                "Username:", u,
                "Password:", passwordPanel,
                "Business Group:", groupField
            };

            int ok = JOptionPane.showConfirmDialog(this, msg, "Add Entry", JOptionPane.OK_CANCEL_OPTION);
            if (ok != JOptionPane.OK_OPTION) return;

            String s = site.getText().trim();
            String un = u.getText().trim();
            String pw = new String(p.getPassword()).trim();
            String group = groupField.getText().trim();

            if (s.isEmpty() || un.isEmpty() || pw.isEmpty() || group.isEmpty()) {
                JOptionPane.showMessageDialog(this, "All fields required.");
                return;
            }

            boolean added = pm.addEntry(account, type, new PasswordEntry(s, un, pw, group));
            if (!added) {
                JOptionPane.showMessageDialog(this, "Could not add entry.");
                return;
            }
        } else {
            Object[] msg = {
                "Site:", site,
                "Username:", u,
                "Password:", passwordPanel
            };

            int ok = JOptionPane.showConfirmDialog(this, msg, "Add Entry", JOptionPane.OK_CANCEL_OPTION);
            if (ok != JOptionPane.OK_OPTION) return;

            String s = site.getText().trim();
            String un = u.getText().trim();
            String pw = new String(p.getPassword()).trim();

            if (s.isEmpty() || un.isEmpty() || pw.isEmpty()) {
                JOptionPane.showMessageDialog(this, "All fields required.");
                return;
            }

            boolean added = pm.addEntry(account, type, new PasswordEntry(s, un, pw));
            if (!added) {
                JOptionPane.showMessageDialog(this, "Could not add entry.");
                return;
            }
        }

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

        if ("Business".equalsIgnoreCase(type) && sel.getBusinessGroup() != null) {
            JLabel group = new JLabel("Business Group: " + sel.getBusinessGroup());
            group.setForeground(UI.TEXT);
            panel.add(group);
            panel.add(Box.createVerticalStrut(10));
        }
        
        panel.add(Box.createVerticalStrut(6));
        panel.add(show);

        JOptionPane.showMessageDialog(this, panel, "Entry", JOptionPane.PLAIN_MESSAGE);
    }

    // Edit selected entry
    private void editEntry() {
        UserAccount account = creds.getAccount(user);
        if (account == null) {
            JOptionPane.showMessageDialog(this, "User account not found.");
            return;
        }

        if ("Business".equalsIgnoreCase(type) && !canModifyBusinessVault()) {
            JOptionPane.showMessageDialog(this, "You don't have permission to edit business entries.");
            return;
        }

        PasswordEntry sel = list.getSelectedValue();
        if (sel == null) {
            JOptionPane.showMessageDialog(this, "Select an entry.");
            return;
        }

        JTextField site = new JTextField(sel.getSite());
        JTextField u = new JTextField(sel.getUsername());
        JPasswordField p = new JPasswordField(); // blank = keep current password

        UI.styleInput(site);
        UI.styleInput(u);
        UI.styleInput(p);

        JTextField groupField = null;

        Object[] msg;
        if ("Business".equalsIgnoreCase(type)) {
            groupField = new JTextField(sel.getBusinessGroup() == null ? "" : sel.getBusinessGroup());
            UI.styleInput(groupField);

            msg = new Object[] {
                "Site:", site,
                "Username:", u,
                "New Password (leave blank to keep current):", p,
                "Business Group:", groupField
            };
        } else {
            msg = new Object[] {
                "Site:", site,
                "Username:", u,
                "New Password (leave blank to keep current):", p
            };
        }

        int ok = JOptionPane.showConfirmDialog(this, msg, "Edit Entry", JOptionPane.OK_CANCEL_OPTION);
        if (ok != JOptionPane.OK_OPTION) return;

        String s = site.getText().trim();
        String un = u.getText().trim();
        String pw = new String(p.getPassword()).trim();

        if (s.isEmpty() || un.isEmpty()) {
            JOptionPane.showMessageDialog(this, "Site and Username are required.");
            return;
        }

        List<PasswordEntry> full = pm.getRawEntriesForVault(user, type);
        int realIndex = full.indexOf(sel);

        if (realIndex < 0) {
            JOptionPane.showMessageDialog(this, "Error updating entry.");
            return;
        }

        String finalPassword = pw.isEmpty() ? sel.getPassword() : pw;
        PasswordEntry updated;

        if ("Business".equalsIgnoreCase(type)) {
            String group = groupField.getText().trim();
            if (group.isEmpty()) {
                JOptionPane.showMessageDialog(this, "Business group is required.");
                return;
            }

            updated = new PasswordEntry(s, un, finalPassword, group);
        } else {
            updated = new PasswordEntry(s, un, finalPassword);
        }

        boolean success = pm.updateEntry(account, type, realIndex, updated);
        if (!success) {
            JOptionPane.showMessageDialog(this, "Error updating entry.");
            return;
        }

        refresh();
        JOptionPane.showMessageDialog(this, "Entry updated.");
    }

    // Delete selected entry
    private void deleteEntry() {
        UserAccount account = creds.getAccount(user);
        if (account == null) {
            JOptionPane.showMessageDialog(this, "User account not found.");
            return;
        }

        if ("Business".equalsIgnoreCase(type) && !canModifyBusinessVault()) {
            JOptionPane.showMessageDialog(this, "You don't have permission to delete business entries.");
            return;
        }

        PasswordEntry sel = list.getSelectedValue();
        if (sel == null) {
            JOptionPane.showMessageDialog(this, "Select an entry.");
            return;
        }

        int ok = JOptionPane.showConfirmDialog(this, "Delete this entry?", "Confirm", JOptionPane.YES_NO_OPTION);
        if (ok != JOptionPane.YES_OPTION) {
            return;
        }

        List<PasswordEntry> full = pm.getRawEntriesForVault(user, type);
        int realIndex = full.indexOf(sel);

        if (realIndex < 0) {
            JOptionPane.showMessageDialog(this, "Error deleting entry.");
            return;
        }

        boolean removed = pm.removeEntry(account, type, realIndex);
        if (!removed) {
            JOptionPane.showMessageDialog(this, "Error deleting entry.");
            return;
        }

        refresh();
    }

    // Copy username to clipboard
    private void copySelectedUsername() {
        if ("Business".equalsIgnoreCase(type) && !canViewBusinessVault()) {
            JOptionPane.showMessageDialog(this, "You don't have permission to view business entries.");
            return;
        }

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
        if ("Business".equalsIgnoreCase(type) && !canViewBusinessVault()) {
            JOptionPane.showMessageDialog(this, "You don't have permission to view business entries.");
            return;
        }

        PasswordEntry sel = list.getSelectedValue();
        if (sel == null) {
            JOptionPane.showMessageDialog(this, "Select an entry first.");
            return;
        }
        
        copyToClipboard(sel.getPassword());
        JOptionPane.showMessageDialog(this, "Password copied.");
    }

    // Generate password dialog
    private void showGeneratedPasswordDialog() {
        JTextField passwordField = new JTextField(PasswordUtils.generatePassword());
        passwordField.setEditable(false);
        UI.styleInput(passwordField);

        JButton regenerateBtn = UI.secondaryButton("Regenerate");
        JButton copyBtn = UI.secondaryButton("Copy");
        JButton closeBtn = UI.secondaryButton("Close");

        JPanel panel = new JPanel();
        panel.setBackground(UI.BG);
        panel.setBorder(BorderFactory.createEmptyBorder(12, 12, 12, 12));
        panel.setLayout(new BoxLayout(panel, BoxLayout.Y_AXIS));

        JLabel label = new JLabel("Generated Password:");
        label.setForeground(UI.TEXT);
        label.setAlignmentX(Component.LEFT_ALIGNMENT);

        passwordField.setAlignmentX(Component.LEFT_ALIGNMENT);

        JPanel buttons = new JPanel(new FlowLayout(FlowLayout.RIGHT, 10, 0));
        buttons.setOpaque(false);
        buttons.add(regenerateBtn);
        buttons.add(copyBtn);
        buttons.add(closeBtn);

        panel.add(label);
        panel.add(Box.createVerticalStrut(6));
        panel.add(passwordField);
        panel.add(Box.createVerticalStrut(12));
        panel.add(buttons);

        JDialog dialog = new JDialog((Frame) SwingUtilities.getWindowAncestor(this), "Generate Password", true);
        dialog.setContentPane(panel);
        dialog.pack();
        dialog.setLocationRelativeTo(this);

        regenerateBtn.addActionListener(e -> passwordField.setText(PasswordUtils.generatePassword()));

        copyBtn.addActionListener(e -> {
            copyToClipboard(passwordField.getText());
            JOptionPane.showMessageDialog(dialog, "Password copied.");
        });

        closeBtn.addActionListener(e -> dialog.dispose());

        dialog.setVisible(true);
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

        void setScale(float scale) {
            site.setFont(site.getFont().deriveFont(Font.BOLD, 14f * scale));
            user.setFont(user.getFont().deriveFont(Font.PLAIN, 12f * scale));
            int top = Math.max(4, (int)(10 * scale));
            int left = Math.max(6, (int)(12 * scale));
            setBorder(BorderFactory.createEmptyBorder(top, left, top, left));
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
            setBackground(isSelected ? new Color(60, 90, 130) : UI.CARD);
            return this;
        }
    }
}