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
    private final CreditCardManager ccm;

    private String user;
    private String type;

    // Header
    private final JLabel title = UI.h1("");
    private final JLabel accessStatus = UI.subtle("");

    // Search Box
    private final JTextField searchField = new JTextField(18);

    // Password List
    private final DefaultListModel<PasswordEntry> model = new DefaultListModel<>();
    private final JList<PasswordEntry> list = new JList<>(model);

    // Buttons
    private final JButton add        = UI.accentButton("Add");
    private final JButton creditCardsBtn = UI.secondaryButton("Credit Cards");
    private final JButton view       = UI.secondaryButton("View");
    private final JButton edit       = UI.secondaryButton("Edit");
    private final JButton del        = UI.deleteButton("Delete");
    private final JButton copyUser   = UI.secondaryButton("Copy Username");
    private final JButton copyPass   = UI.secondaryButton("Copy Password");
    private final JButton generateBtn = UI.secondaryButton("Generate Password");

    // Resizing/Scaling
    private final Dimension baseSize = new Dimension(1000, 700);
    private float lastScale = 1.0f;

    private final EntryRenderer renderer = new EntryRenderer();

    public VaultPanel(AppFrame app, PasswordManager pm, Credentials creds,
                      CreditCardManager ccm) {
        this.app = app;
        this.pm = pm;
        this.creds = creds;
        this.ccm = ccm;

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

        // Search area
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

        // Center card with saved entries
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

        // Bottom actions buttons
        JPanel buttons = new JPanel(new FlowLayout(FlowLayout.CENTER, 10, 0));
        buttons.setOpaque(false);

        buttons.add(add);
        creditCardsBtn.setBackground(new Color(160, 120, 220));
        buttons.add(creditCardsBtn);
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

        // Button Actions
        back.addActionListener(e -> app.showChoice());
        logout.addActionListener(e -> app.showLogin());
        add.addActionListener(e -> {
            if (!SessionManager.validateSession(app)) return;
            addEntry();
        });

        view.addActionListener(e -> {
            if (!SessionManager.validateSession(app)) return;
            viewEntry();
        });

        edit.addActionListener(e -> {
            if (!SessionManager.validateSession(app)) return;
            editEntry();
        });

        del.addActionListener(e -> {
            if (!SessionManager.validateSession(app)) return;
            deleteEntry();
        });
        copyUser.addActionListener(e -> copySelectedUsername());
        copyPass.addActionListener(e -> copySelectedPassword());
        generateBtn.addActionListener(e -> showGeneratedPasswordDialog());
        creditCardsBtn.addActionListener(e -> showCreditCardsDialog());

        // Double click to view entry
        list.addMouseListener(new java.awt.event.MouseAdapter() {
            @Override
            public void mouseClicked(java.awt.event.MouseEvent evt) {
                if (evt.getClickCount() == 2) viewEntry();
            }
        });

        // Refresh search results while typing
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

        // Set starting scale
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

        // Show different status messages for business vault
        if ("Business".equalsIgnoreCase(accountType)) {
            if (!account.isBusinessAuthorized()) {
                title.setText("Business Vault — " + username);
                accessStatus.setText("Business Access Pending. Please contact your administrator.");
                accessStatus.setVisible(true);
            } else if (account.isBusinessAdmin()) {
                title.setText("Business Vault — " + username);
                accessStatus.setText("Full business vault access.");
                accessStatus.setVisible(true);
            } else {
                title.setText("Business Vault — employee — " + username);
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

    // Reloads the list based on search text
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

    // Turns button on/off based on access level
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
            creditCardsBtn.setEnabled(true);
            return;
        }
        
        view.setEnabled(canView);
        copyUser.setEnabled(canView);
        copyPass.setEnabled(canView);
        creditCardsBtn.setEnabled(canView);

        add.setEnabled(canModify);
        edit.setEnabled(canModify);
        del.setEnabled(canModify);
        generateBtn.setEnabled(canModify);
    }

    // Checks if user can see business vault entries
    private boolean canViewBusinessVault() {
        if (!"Business".equalsIgnoreCase(type)) {
            return true;
        }

        UserAccount account = creds.getAccount(user);
        return pm.canAccessBusinessVault(account);
    }

    // Checks if user can change business vault entries
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

    // Add a new password entry
    private void addEntry() {
        if (!SessionManager.validateSession(app)) return;
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
            if (!SessionManager.validateSession(app)) return;
            String s = site.getText().trim();
            String un = u.getText().trim();
            String pw = new String(p.getPassword()).trim();

            String group = groupField.getText().trim();

            if (s.isEmpty() || un.isEmpty() || pw.isEmpty() || group.isEmpty()) {
                JOptionPane.showMessageDialog(this, "All fields required.");
                return;
            }

            String strength = PasswordStrengthChecker.getStrengthLabel(pw);
            boolean isWeak = PasswordStrengthChecker.isWeak(pw);
            boolean reused = pm.isPasswordReused(account, type, pw);

            if (isWeak || reused) {
                String message = "";

                if (isWeak) {
                    message += "⚠ Weak password detected (" + strength + ").\n";
                }

                if (reused) {
                    message += "⚠ This password is already used in your vault.\n";
                }

                message += "\nDo you still want to continue?";

                int choice = JOptionPane.showConfirmDialog(
                        this,
                        message,
                        "Password Warning",
                        JOptionPane.YES_NO_OPTION
                );

                if (choice != JOptionPane.YES_OPTION) {
                    return;
                }
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

            String strength = PasswordStrengthChecker.getStrengthLabel(pw);
            boolean isWeak = PasswordStrengthChecker.isWeak(pw);
            boolean reused = pm.isPasswordReused(account, type, pw);

            if (isWeak || reused) {
                String message = "";

                if (isWeak) {
                    message += "⚠ Weak password detected (" + strength + ").\n";
                }

                if (reused) {
                    message += "⚠ This password is already used in your vault.\n";
                }

                message += "\nDo you still want to continue?";

                int choice = JOptionPane.showConfirmDialog(
                        this,
                        message,
                        "Password Warning",
                        JOptionPane.YES_NO_OPTION
                );

                if (choice != JOptionPane.YES_OPTION) {
                    return;
                }
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

        UserAccount account = creds.getAccount(user);
        if (account == null) {
            JOptionPane.showMessageDialog(this, "User account not found.");
            return;
        }

        String decryptedPassword = pm.getDecryptedPassword(account, type, sel);
        if (decryptedPassword == null) {
            JOptionPane.showMessageDialog(this, "You don't have permission to view this entry.");
            return;
        }

        JLabel site = new JLabel("Site: " + sel.getSite());
        site.setForeground(UI.TEXT);

        JLabel usr = new JLabel("Username: " + sel.getUsername());
        usr.setForeground(UI.TEXT);

        JLabel pwd = new JLabel("Password: " + mask(decryptedPassword));
        pwd.setForeground(UI.TEXT);

        JCheckBox show = new JCheckBox("Show password");
        show.setOpaque(false);
        show.setForeground(UI.MUTED);
        show.addActionListener(e ->
                pwd.setText("Password: " + (show.isSelected() ? decryptedPassword : mask(decryptedPassword)))
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

        // Show business group if this is a business entry
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

        String finalPassword;
        if (pw.isEmpty()) {
            finalPassword = pm.getDecryptedPassword(account, type, sel);
            if (finalPassword == null) {
                JOptionPane.showMessageDialog(this, "You don't have permission to view this entry.");
                return;
            }
        } else {
            finalPassword = pw;
        }

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

        boolean success = pm.updateEntry(account, type, sel, updated);
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

        boolean removed = pm.removeEntry(account, type, sel);
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
        PasswordEntry sel = list.getSelectedValue();
        if (sel == null) {
            JOptionPane.showMessageDialog(this, "Select an entry first.");
            return;
        }

        UserAccount account = creds.getAccount(user);
        if (account == null) {
            JOptionPane.showMessageDialog(this, "User account not found.");
            return;
        }

        String decryptedPassword = pm.getDecryptedPassword(account, type, sel);
        if (decryptedPassword == null) {
            JOptionPane.showMessageDialog(this, "You don't have permission to view this entry.");
            return;
        }

        copyToClipboard(decryptedPassword);
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

    // Credit Card Manager
    private void showCreditCardsDialog() {
        boolean isBusinessVault = "Business".equalsIgnoreCase(type);
        boolean canView = !isBusinessVault || canViewBusinessVault();
        boolean canModify = !isBusinessVault || canModifyBusinessVault();

        if (!canView) {
            JOptionPane.showMessageDialog(this, "You don't have permission to view business credit cards.");
            return;
        }

        DefaultListModel<CreditCardEntry> cardModel = new DefaultListModel<>();
        JList<CreditCardEntry> cardList = new JList<>(cardModel);

        for (CreditCardEntry c : ccm.getCards(user)) {
            cardModel.addElement(c);
        }

        cardList.setBackground(UI.CARD);
        cardList.setForeground(UI.TEXT);
        cardList.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        cardList.setFixedCellHeight(62);
        cardList.setBorder(BorderFactory.createEmptyBorder(6, 6, 6, 6));
        cardList.setCellRenderer(new ListCellRenderer<CreditCardEntry>() {
            @Override
            public Component getListCellRendererComponent(
                    JList<? extends CreditCardEntry> lst, CreditCardEntry val,
                    int idx, boolean sel, boolean focus) {
                JPanel p = new JPanel();
                p.setLayout(new BoxLayout(p, BoxLayout.Y_AXIS));
                p.setBorder(BorderFactory.createEmptyBorder(10, 14, 10, 14));
                p.setOpaque(true);

                JLabel nameL = new JLabel(val.getNickname() + "   [" + val.getCardType() + "]");
                nameL.setFont(nameL.getFont().deriveFont(Font.BOLD, 14f));
                nameL.setForeground(UI.TEXT);

                JLabel numL = new JLabel(val.getMaskedCardNumber() + "   Exp: " + val.getExpiryDate());
                numL.setFont(numL.getFont().deriveFont(Font.PLAIN, 12f));
                numL.setForeground(UI.MUTED);

                p.add(nameL);
                p.add(Box.createVerticalStrut(2));
                p.add(numL);
                p.setBackground(sel ? new Color(60, 90, 130) : UI.CARD);
                return p;
            }
        });

        JScrollPane scroll = new JScrollPane(cardList);
        scroll.setPreferredSize(new Dimension(520, 300));
        scroll.getViewport().setBackground(UI.CARD);
        scroll.setBorder(BorderFactory.createLineBorder(UI.BORDER, 1, true));
        scroll.getVerticalScrollBar().setUnitIncrement(16);

        JButton addCardBtn = UI.accentButton("Add Card");
        addCardBtn.setBackground(new Color(130, 90, 220));
        JButton viewCardBtn  = UI.secondaryButton("View Details");
        JButton copyNumBtn   = UI.secondaryButton("Copy Number");
        JButton delCardBtn   = UI.deleteButton("Delete");
        JButton closeBtn     = UI.secondaryButton("Close");

        addCardBtn.setEnabled(canModify);
        copyNumBtn.setEnabled(canModify);
        delCardBtn.setEnabled(canModify);        

        JPanel btnRow = new JPanel(new FlowLayout(FlowLayout.CENTER, 10, 0));
        btnRow.setOpaque(false);
        btnRow.add(addCardBtn);
        btnRow.add(viewCardBtn);
        btnRow.add(copyNumBtn);
        btnRow.add(delCardBtn);
        btnRow.add(closeBtn);

        JLabel header = new JLabel("Credit Cards");
        header.setFont(header.getFont().deriveFont(Font.BOLD, 18f));
        header.setForeground(UI.TEXT);

        JLabel sub = UI.subtle(ccm.getCards(user).size() + " card(s) stored");

        JPanel headerPanel = new JPanel();
        headerPanel.setOpaque(false);
        headerPanel.setLayout(new BoxLayout(headerPanel, BoxLayout.Y_AXIS));
        headerPanel.add(header);
        headerPanel.add(Box.createVerticalStrut(4));
        headerPanel.add(sub);
        headerPanel.add(Box.createVerticalStrut(10));

        JPanel content = new JPanel(new BorderLayout(10, 10));
        content.setBackground(UI.BG);
        content.setBorder(BorderFactory.createEmptyBorder(16, 16, 12, 16));
        content.add(headerPanel, BorderLayout.NORTH);
        content.add(scroll, BorderLayout.CENTER);
        content.add(btnRow, BorderLayout.SOUTH);

        JDialog dialog = new JDialog((Frame) SwingUtilities.getWindowAncestor(this), "Credit Cards", true);
        dialog.setContentPane(content);
        dialog.setSize(560, 440);
        dialog.setLocationRelativeTo(this);

        addCardBtn.addActionListener(e -> {
            addCreditCard(cardModel, sub, dialog);
        });

        viewCardBtn.addActionListener(e -> {
            CreditCardEntry sel = cardList.getSelectedValue();
            if (sel == null) { JOptionPane.showMessageDialog(dialog, "Select a card first."); return; }
            viewCreditCardDetails(sel, dialog);
        });

        copyNumBtn.addActionListener(e -> {
            if (!canModify) {
                JOptionPane.showMessageDialog(dialog, "Only admins can copy business card numbers.");
                return;
            }

            CreditCardEntry sel = cardList.getSelectedValue();
            if (sel == null) { JOptionPane.showMessageDialog(dialog, "Select a card first."); return; }
            copyToClipboard(sel.getCardNumber());
            JOptionPane.showMessageDialog(dialog, "Card number copied to clipboard.");
        });

        delCardBtn.addActionListener(e -> {
            if (!canModify) {
                JOptionPane.showMessageDialog(dialog, "Select a card first");
                return;
            }

            CreditCardEntry sel = cardList.getSelectedValue();
            if (sel == null) { JOptionPane.showMessageDialog(dialog, "Select a card first."); return; }
            int ok = JOptionPane.showConfirmDialog(dialog,
                    "Delete \"" + sel.getNickname() + "\"?", "Confirm Delete", JOptionPane.YES_NO_OPTION);
            if (ok == JOptionPane.YES_OPTION) {
                ccm.removeCard(user, sel);
                cardModel.removeElement(sel);
                sub.setText(cardModel.getSize() + " card(s) stored");
            }
        });

        closeBtn.addActionListener(e -> dialog.dispose());

        dialog.setVisible(true);
    }

    // Dialog to enter and save a new credit card
    private void addCreditCard(DefaultListModel<CreditCardEntry> model,
                               JLabel countLabel, JDialog parent) {
        if ("Business".equalsIgnoreCase(type) && !canModifyBusinessVault()) {
            JOptionPane.showMessageDialog(parent, "Only admins can add business credit cards. ");
            return;
        }

        JTextField nickname = new JTextField();
        JTextField holderName = new JTextField();
        JTextField cardNumber = new JTextField();
        JTextField expiryDate = new JTextField();
        JPasswordField cvv = new JPasswordField();
        String[] types = {"Visa", "Mastercard", "American Express", "Discover", "Other"};
        JComboBox<String> cardType = new JComboBox<>(types);

        UI.styleInput(nickname);
        UI.styleInput(holderName);
        UI.styleInput(cardNumber);
        UI.styleInput(expiryDate);
        UI.styleInput(cvv);
        UI.styleInput(cardType);

        expiryDate.setToolTipText("Format: MM/YY (e.g. 09/27)");
        cvv.setToolTipText("3 or 4 digit security code");

        Object[] fields = {
            "Nickname (e.g. Chase Debit):", nickname,
            "Cardholder Name:", holderName,
            "Card Number:", cardNumber,
            "Expiry Date (MM/YY):", expiryDate,
            "CVV:", cvv,
            "Card Type:", cardType
        };

        int ok = JOptionPane.showConfirmDialog(parent, fields,
                "Add Credit Card", JOptionPane.OK_CANCEL_OPTION);
        if (ok != JOptionPane.OK_OPTION) return;

        String nick = nickname.getText().trim();
        String holder = holderName.getText().trim();
        String num = cardNumber.getText().trim().replaceAll("[\\s-]", "");
        String exp = expiryDate.getText().trim();
        String cvvStr = new String(cvv.getPassword()).trim();
        String type = (String) cardType.getSelectedItem();

        if (nick.isEmpty() || holder.isEmpty() || num.isEmpty()
                || exp.isEmpty() || cvvStr.isEmpty()) {
            JOptionPane.showMessageDialog(parent, "All fields are required.");
            return;
        }
        if (!num.matches("\\d{13,19}")) {
            JOptionPane.showMessageDialog(parent, "Invalid card number. Enter 13-19 digits only.");
            return;
        }
        if (!exp.matches("(0[1-9]|1[0-2])/\\d{2}")) {
            JOptionPane.showMessageDialog(parent, "Invalid expiry. Use MM/YY format, e.g. 09/27.");
            return;
        }
        if (!cvvStr.matches("\\d{3,4}")) {
            JOptionPane.showMessageDialog(parent, "Invalid CVV. Must be 3 or 4 digits.");
            return;
        }

        CreditCardEntry entry = new CreditCardEntry(nick, holder, num, exp, cvvStr, type);
        ccm.addCard(user, entry);
        model.addElement(entry);
        countLabel.setText(model.getSize() + " card(s) stored");
    }

    // Dialog to view all details of a saved card (with reveal toggle)
    private void viewCreditCardDetails(CreditCardEntry card, JDialog parent) {
        JPanel panel = new JPanel();
        panel.setBackground(UI.BG);
        panel.setBorder(BorderFactory.createEmptyBorder(14, 14, 14, 14));
        panel.setLayout(new BoxLayout(panel, BoxLayout.Y_AXIS));

        JLabel nickL   = new JLabel(card.getNickname() + "  [" + card.getCardType() + "]");
        nickL.setFont(nickL.getFont().deriveFont(Font.BOLD, 16f));
        nickL.setForeground(new Color(160, 120, 255));

        JLabel holderL = new JLabel("Cardholder:  " + card.getCardholderName());
        holderL.setForeground(UI.TEXT);

        JLabel numL = new JLabel("Card Number:  " + card.getMaskedCardNumber());
        numL.setForeground(UI.TEXT);

        JLabel expL = new JLabel("Expires:  " + card.getExpiryDate());
        expL.setForeground(UI.TEXT);

        JLabel cvvL = new JLabel("CVV:  \u2022\u2022\u2022");
        cvvL.setForeground(UI.TEXT);

        JLabel dateL = new JLabel("Added:  " + card.getDateAdded());
        dateL.setForeground(UI.MUTED);

        boolean canReveal = !"Business".equalsIgnoreCase(type) || canModifyBusinessVault();

        JCheckBox revealBox = new JCheckBox("Reveal card number and CVV");
        revealBox.setOpaque(false);
        revealBox.setForeground(UI.MUTED);
        revealBox.setEnabled(canReveal);

        revealBox.addActionListener(e -> {
            if (!canReveal) {
                revealBox.setSelected(false);
                return;
            }

            if (revealBox.isSelected()) {
                numL.setText("Card Number:  " + card.getCardNumber());
                cvvL.setText("CVV:  " + card.getCVV());
            } else {
                numL.setText("Card Number:  " + card.getMaskedCardNumber());
                cvvL.setText("CVV:  \u2022\u2022\u2022");
            }
        });

        JButton copyNumBtn = UI.secondaryButton("Copy Number");
        copyNumBtn.setAlignmentX(Component.LEFT_ALIGNMENT);
        copyNumBtn.setEnabled(canReveal);

        copyNumBtn.addActionListener(e -> {
            if (!canReveal) {
                JOptionPane.showMessageDialog(parent, "Only admins can reveal or copy business card details.");
                return;
            }
            
            copyToClipboard(card.getCardNumber());
            JOptionPane.showMessageDialog(parent, "Card number copied to clipboard.");
        });

        panel.add(nickL);
        panel.add(Box.createVerticalStrut(12));
        panel.add(holderL);
        panel.add(Box.createVerticalStrut(6));
        panel.add(numL);
        panel.add(Box.createVerticalStrut(6));
        panel.add(expL);
        panel.add(Box.createVerticalStrut(6));
        panel.add(cvvL);
        panel.add(Box.createVerticalStrut(10));
        panel.add(dateL);
        panel.add(Box.createVerticalStrut(12));
        panel.add(revealBox);
        panel.add(Box.createVerticalStrut(10));
        panel.add(copyNumBtn);

        JOptionPane.showMessageDialog(parent, panel, "Card Details", JOptionPane.PLAIN_MESSAGE);
    }

    // Copy text to clipboard
    private void copyToClipboard(String text) {
        Toolkit.getDefaultToolkit()
                .getSystemClipboard()
                .setContents(new StringSelection(text), null);
    }

    // Mask a password
    private String mask(String s) {
        if (s == null){
            return "";
        }
        int n = Math.max(6, Math.min(12, s.length()));
        return "•".repeat(n);
    }

    // Look for password list items
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

        // Updates renderer size when UI scales
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