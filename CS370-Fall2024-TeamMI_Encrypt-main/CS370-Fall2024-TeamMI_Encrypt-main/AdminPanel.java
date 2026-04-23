import java.awt.*;
import java.util.ArrayList;
import java.util.List;
import javax.swing.*;

public class AdminPanel extends JPanel {
    private final AppFrame app;
    private final Credentials creds;
    private final InviteCodeManager inviteCodeManager;
    private UserAccount currentUser;

    // User list components
    private final DefaultListModel<String> userModel = new DefaultListModel<>();
    private final JList<String> userList = new JList<>(userModel);

    // User details components
    private final JLabel selectedUserLabel = new JLabel("No user selected");
    private final JCheckBox authorizedBox = new JCheckBox("Business Authorized");
    private final JComboBox<String> roleBox = new JComboBox<>(new String[]{"employee", "admin"});
    private final JCheckBox salesBox = new JCheckBox("Sales");
    private final JCheckBox hrBox = new JCheckBox("HR");

    // Action buttons
    private final JButton saveBtn = new JButton("Save Changes");
    private final JButton backBtn = new JButton("Back");
    private final JButton refreshBtn = new JButton("Refresh");

    private final JTextField inviteCodeField = new JTextField(12);
    private final JComboBox<String> inviteRoleBox = new JComboBox<>(new String[]{"employee", "admin"});
    private final JCheckBox inviteSalesBox = new JCheckBox("Sales");
    private final JCheckBox inviteHRBox = new JCheckBox("HR");
    private final JSpinner maxUsesSpinner = new JSpinner(new SpinnerNumberModel(1, 1, 100, 1));
    private final JButton generateCodeBtn = new JButton("Generate Invite Code");
    private final JButton createInviteBtn = new JButton("Create Invite Code");

    private final DefaultListModel<String> inviteModel = new DefaultListModel<>();
    private final JList<String> inviteList = new JList<>(inviteModel);
    private final JButton deactivateInviteBtn = new JButton("Deactivate Selected Code");
    private final JButton deleteInviteBtn = new JButton("Delete Selected Code");
    private final JButton refreshInvitesBtn = new JButton("Refresh Invite Codes");

    public AdminPanel(AppFrame app, Credentials creds, UserAccount currentUser) {
        this.app = app;
        this.creds = creds;
        this.currentUser = currentUser;
        this.inviteCodeManager = InviteCodeManager.loadFromFile();

        // Set up layout and UI components
        setLayout(new BorderLayout(12, 12));
        setBorder(BorderFactory.createEmptyBorder(20, 20, 20, 20));

        // Page title
        JLabel title = new JLabel("Business Admin Panel");
        title.setFont(title.getFont().deriveFont(Font.BOLD, 24f));

        // Top Bar with title and refresh/back buttons
        JPanel top = new JPanel(new BorderLayout());
        top.add(title, BorderLayout.WEST);

        JPanel topButtons = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        topButtons.add(refreshBtn);
        topButtons.add(backBtn);
        top.add(topButtons, BorderLayout.EAST);

        add(top, BorderLayout.NORTH);

        JTabbedPane tabbedPane = new JTabbedPane();
        tabbedPane.addTab("Manage Users", createUsersTab());
        tabbedPane.addTab("Invite Codes", createInviteTab());
        add(tabbedPane, BorderLayout.CENTER);

        refreshBtn.addActionListener(e -> {
            loadBusinessUsers();
            loadInviteCodes();
        });

        backBtn.addActionListener(e -> app.showChoice());

        userList.addListSelectionListener(e -> {
            if (!e.getValueIsAdjusting()) {
                loadSelectedUser();
            }
        });

        saveBtn.addActionListener(e -> saveSelectedUser());

        authorizedBox.addActionListener(e -> updateUserControls());
        roleBox.addActionListener(e -> updateUserControls());

        inviteRoleBox.addActionListener(e -> updateInviteControls());

        generateCodeBtn.addActionListener(e -> generateInviteCode());
        createInviteBtn.addActionListener(e -> createInviteCode());
        refreshInvitesBtn.addActionListener(e -> loadInviteCodes());
        deactivateInviteBtn.addActionListener(e -> deactivateSelectedInvite());
        deleteInviteBtn.addActionListener(e -> deleteSelectedInvite());

        updateUserControls();
        updateInviteControls();
    }

    private JPanel createUsersTab() {
        JPanel panel = new JPanel(new BorderLayout(12, 12));

        userList.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        JScrollPane leftScroll = new JScrollPane(userList);
        leftScroll.setPreferredSize(new Dimension(220, 400));

        JPanel right = new JPanel();
        right.setLayout(new BoxLayout(right, BoxLayout.Y_AXIS));
        right.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));

        selectedUserLabel.setAlignmentX(Component.LEFT_ALIGNMENT);
        authorizedBox.setAlignmentX(Component.LEFT_ALIGNMENT);

        // Panel for user roles 
        JPanel rolePanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
        rolePanel.setAlignmentX(Component.LEFT_ALIGNMENT);
        rolePanel.add(new JLabel("Role:"));
        rolePanel.add(roleBox);

        // Panels for groups
        JPanel groupsPanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
        groupsPanel.setAlignmentX(Component.LEFT_ALIGNMENT);
        groupsPanel.add(new JLabel("Groups:"));
        groupsPanel.add(salesBox);
        groupsPanel.add(hrBox);

        saveBtn.setAlignmentX(Component.LEFT_ALIGNMENT);

        // Right Side Layout
        right.add(selectedUserLabel);
        right.add(Box.createVerticalStrut(12));
        right.add(authorizedBox);
        right.add(Box.createVerticalStrut(12));
        right.add(rolePanel);
        right.add(Box.createVerticalStrut(12));
        right.add(groupsPanel);
        right.add(Box.createVerticalStrut(20));
        right.add(saveBtn);

        JSplitPane split = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT, leftScroll, right);
        split.setDividerLocation(260);
        add(split, BorderLayout.CENTER);

        return panel;
    }
    private JPanel createInviteTab() {
        JPanel panel = new JPanel(new BorderLayout(12, 12));

        JPanel formPanel = new JPanel();
        formPanel.setLayout(new BoxLayout(formPanel, BoxLayout.Y_AXIS));
        formPanel.setBorder(BorderFactory.createTitledBorder("Create New Invite Code"));
        JPanel codePanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
        codePanel.add(new JLabel("Code:"));
        codePanel.add(inviteCodeField);
        codePanel.add(generateCodeBtn);

        JPanel rolePanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
        rolePanel.add(new JLabel("Role:"));
        rolePanel.add(inviteRoleBox);

        JPanel groupsPanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
        groupsPanel.add(new JLabel("Groups:"));
        groupsPanel.add(inviteSalesBox);
        groupsPanel.add(inviteHRBox);

        JPanel usesPanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
        usesPanel.add(new JLabel("Max Uses:"));
        usesPanel.add(maxUsesSpinner);

        JPanel createPanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
        createPanel.add(createInviteBtn);

        formPanel.add(codePanel);
        formPanel.add(rolePanel);
        formPanel.add(groupsPanel);
        formPanel.add(usesPanel);
        formPanel.add(createPanel);

        inviteList.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        JScrollPane inviteScroll = new JScrollPane(inviteList);
        inviteScroll.setBorder(BorderFactory.createTitledBorder("Existing Invite Codes"));

        JPanel inviteButtonsPanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
        inviteButtonsPanel.add(refreshInvitesBtn);
        inviteButtonsPanel.add(deactivateInviteBtn);
        inviteButtonsPanel.add(deleteInviteBtn);

        JPanel bottomPanel = new JPanel(new BorderLayout());
        bottomPanel.add(inviteScroll, BorderLayout.CENTER);
        bottomPanel.add(inviteButtonsPanel, BorderLayout.SOUTH);

        panel.add(formPanel, BorderLayout.NORTH);
        panel.add(bottomPanel, BorderLayout.CENTER);

        return panel;
    }

    public void load(String currentUsername) {
        this.currentUser = creds.getAccount(currentUsername);

        // Ensures only business admins can access this panel.
        if (this.currentUser == null
                || !this.currentUser.isBusinessAuthorized()
                || !this.currentUser.isBusinessAdmin()) {
            JOptionPane.showMessageDialog(this, "Access denied.");
            SwingUtilities.invokeLater(() -> app.showChoice());
            return;
        }

        clearUserSelection();
        loadBusinessUsers();
        loadInviteCodes();

        if (!userModel.isEmpty()) {
            userList.setSelectedIndex(0);
        }
    }

    private void clearUserSelection() {
        selectedUserLabel.setText("No user selected");
        authorizedBox.setSelected(false);
        roleBox.setSelectedItem("employee");
        salesBox.setSelected(false);
        hrBox.setSelected(false);
        updateUserControls();
    }

    private void loadBusinessUsers() {
        userModel.clear();

        List<UserAccount> accounts = creds.getAllAccounts();
        for (UserAccount account : accounts) {
            String type = account.getAccountType();
            // Only show accounts that have business access (business or both)
            if ("business".equalsIgnoreCase(type) || "both".equalsIgnoreCase(type)) {
                userModel.addElement(account.getUsername());
            }
        }
    }

    private void loadSelectedUser() {
        String username = userList.getSelectedValue();
        if (username == null) {
            clearUserSelection();
            return;
        }

        UserAccount account = creds.getAccount(username);
        if (account == null) {
            clearUserSelection();
            return;
        }

        // Update UI fields based on selected user's current settings
        selectedUserLabel.setText("Selected User: " + account.getUsername());

        boolean authorized = account.isBusinessAuthorized();
        authorizedBox.setSelected(authorized);

        // Load Role
        String role = account.getBusinessRole();
        if (role == null || role.isBlank() || "none".equalsIgnoreCase(role)) {
            role = "employee";
        }
        roleBox.setSelectedItem(role.toLowerCase());

        // Load group checkboxes based on user's allowed business groups
        List<String> groups = account.getAllowedBusinessGroups();
        salesBox.setSelected(groups != null && groups.contains("Sales"));
        hrBox.setSelected(groups != null && groups.contains("HR"));

        updateUserControls();
    }

    private void saveSelectedUser() {
        String username = userList.getSelectedValue();
        if (username == null) {
            JOptionPane.showMessageDialog(this, "Select a user first.");
            return;
        }

        boolean authorized = authorizedBox.isSelected();


        if (!authorized) {
            creds.setBusinessAccess(username, false);
            JOptionPane.showMessageDialog(this, "User " + username + " is no longer authorized for business access.");
            loadSelectedUser();
            return;
        }

        String role = (String) roleBox.getSelectedItem();
        if (role == null) {
            JOptionPane.showMessageDialog(this, "Select a valid role.");
            return;
        }

        if ("employee".equalsIgnoreCase(role) && !salesBox.isSelected() && !hrBox.isSelected()) {
            JOptionPane.showMessageDialog(this, "Employee must belong to at least one group.");
            return;
        }

        creds.setBusinessAccess(username, true);
        creds.setBusinessRole(username, role);
        creds.clearBusinessGroups(username);

        if ("employee".equalsIgnoreCase(role)) {
            if (salesBox.isSelected()) {
                creds.addBusinessGroup(username, "Sales");
            }
            if (hrBox.isSelected()) {
                creds.addBusinessGroup(username, "HR");
            }
        }

        JOptionPane.showMessageDialog(this, "User " + username + " updated successfully.");
        loadSelectedUser();
    }

    private void updateUserControls() {
        boolean authorized = authorizedBox.isSelected();
        String role = (String) roleBox.getSelectedItem();

        roleBox.setEnabled(authorized);

        boolean employeeMode = authorized && "employee".equalsIgnoreCase(role);
        salesBox.setEnabled(employeeMode);
        hrBox.setEnabled(employeeMode);

        if (!employeeMode) {
            salesBox.setSelected(false);
            hrBox.setSelected(false);
        }
    }
    private void updateInviteControls() {
        String role = (String) inviteRoleBox.getSelectedItem();
        boolean employeeMode = "employee".equalsIgnoreCase(role);

        inviteSalesBox.setEnabled(employeeMode);
        inviteHRBox.setEnabled(employeeMode);

        if (!employeeMode) {
            inviteSalesBox.setSelected(false);
            inviteHRBox.setSelected(false);
        }
    }

    private void generateInviteCode() {
        inviteCodeField.setText(generateReadableInviteCode());
    }

    private String generateReadableInviteCode() {
        return generateInvitePart(4) + "-" + generateInvitePart(4);
    }

    private String generateInvitePart(int length) {
        String chars = "ABCDEFGHJKMNPQRSTUVWXYZ23456789";
        StringBuilder sb = new StringBuilder();

        for (int i = 0; i < length; i++) {
            int index = (int) (Math.random() * chars.length());
            sb.append(chars.charAt(index));
        }

        return sb.toString();
    }

    private void createInviteCode() {
        String code = inviteCodeField.getText().trim();
        String role = (String) inviteRoleBox.getSelectedItem();
        int maxUses = (Integer) maxUsesSpinner.getValue();

        if (code.isEmpty()) {
            JOptionPane.showMessageDialog(this, "Enter or generate a code first.");
            return;
        }

        List<String> groups = new ArrayList<>();
        if ("employee".equalsIgnoreCase(role)) {
            if (inviteSalesBox.isSelected()) {
                groups.add("Sales");
            }
            if (inviteHRBox.isSelected()) {
                groups.add("HR");
            }

            if (groups.isEmpty()) {
                JOptionPane.showMessageDialog(this, "Employee invite codes need at least one group.");
                return;
            }
        }

        boolean success = inviteCodeManager.createCode(code, role, groups, maxUses);

        if (success) {
            JOptionPane.showMessageDialog(this, "Invite code created: " + code);
            inviteCodeField.setText("");
            inviteRoleBox.setSelectedItem("employee");
            inviteSalesBox.setSelected(false);
            inviteHRBox.setSelected(false);
            maxUsesSpinner.setValue(1);
            loadInviteCodes();
        } else {
            JOptionPane.showMessageDialog(this, "Failed to create invite code. It may already exist.");
        }
    }

    private void loadInviteCodes() {
        inviteModel.clear();

        List<InviteCode> codes = inviteCodeManager.getAllCodes();
        for (InviteCode code : codes) {
            if (code != null) {
                inviteModel.addElement(formatInviteCodeDisplay(code));
            }
        }
    }

    private String formatInviteCodeDisplay(InviteCode code) {
        String status = code.isActive() ? "ACTIVE" : "INACTIVE";
        String groups = code.getGroups().isEmpty() ? "All" : String.join(",", code.getGroups());

        return code.getCode()
                + " | " + code.getRole()
                + " | Groups: " + groups
                + " | Uses: " + code.getUsedCount() + "/" + code.getMaxUses()
                + " | " + status;
    }

    private String getSelectedInviteCodeValue() {
        String selected = inviteList.getSelectedValue();
        if (selected == null || selected.isBlank()) {
            return null;
        }

        int separator = selected.indexOf(" | ");
        if (separator == -1) {
            return null;
        }

        return selected.substring(0, separator).trim();
    }

    private void deactivateSelectedInvite() {
        String code = getSelectedInviteCodeValue();
        if (code == null) {
            JOptionPane.showMessageDialog(this, "Select an invite code first.");
            return;
        }

        if (inviteCodeManager.deactivateCode(code)) {
            JOptionPane.showMessageDialog(this, "Invite code deactivated.");
            loadInviteCodes();
        } else {
            JOptionPane.showMessageDialog(this, "Unable to deactivate invite code.");
        }
    }

    private void deleteSelectedInvite() {
        String code = getSelectedInviteCodeValue();
        if (code == null) {
            JOptionPane.showMessageDialog(this, "Select an invite code first.");
            return;
        }

        int choice = JOptionPane.showConfirmDialog(
                this,
                "Delete invite code " + code + "?",
                "Confirm Delete",
                JOptionPane.YES_NO_OPTION
        );

        if (choice != JOptionPane.YES_OPTION) {
            return;
        }

        if (inviteCodeManager.deleteCode(code)) {
            JOptionPane.showMessageDialog(this, "Invite code deleted.");
            loadInviteCodes();
        } else {
            JOptionPane.showMessageDialog(this, "Unable to delete invite code.");
        }
    }

    private boolean containsIgnoreCase(List<String> list, String value) {
        if (list == null || value == null) {
            return false;
        }

        for (String item : list) {
            if (item != null && item.equalsIgnoreCase(value)) {
                return true;
            }
        }

        return false;
    }
}