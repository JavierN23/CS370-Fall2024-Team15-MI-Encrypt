import java.awt.*;
import java.util.List;
import javax.swing.*;

public class AdminPanel extends JPanel {
    private final AppFrame app;
    private final Credentials creds;
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

    public AdminPanel(AppFrame app, Credentials creds, UserAccount currentUser) {
        this.app = app;
        this.creds = creds;
        this.currentUser = currentUser;

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
        split.setDividerLocation(250);
        add(split, BorderLayout.CENTER);

        // Action listeners
        refreshBtn.addActionListener(e -> loadBusinessUsers());
        backBtn.addActionListener(e -> app.showChoice());
        userList.addListSelectionListener(e -> {
            if (!e.getValueIsAdjusting()) {
                loadSelectedUser();
            }
        });

        // Save updats for selected user
        saveBtn.addActionListener(e -> saveSelectedUser());
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

        // Clear selection and reset fields
        selectedUserLabel.setText("No user selected");
        authorizedBox.setSelected(false);
        roleBox.setSelectedItem("employee");
        salesBox.setSelected(false);
        hrBox.setSelected(false);

        // Load business users into the list
        loadBusinessUsers();

        if (!userModel.isEmpty()) {
            userList.setSelectedIndex(0);
        }
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
            selectedUserLabel.setText("No user selected");
            return;
        }

        UserAccount account = creds.getAccount(username);
        if (account == null) {
            return;
        }

        // Update UI fields based on selected user's current settings
        selectedUserLabel.setText("Selected User: " + account.getUsername());
        authorizedBox.setSelected(account.isBusinessAuthorized());

        // Load Role
        String role = account.getBusinessRole();
        if (role == null || role.isBlank()) {
            role = "employee";
        }
        roleBox.setSelectedItem(role.toLowerCase());

        // Load group checkboxes based on user's allowed business groups
        List<String> groups = account.getAllowedBusinessGroups();
        salesBox.setSelected(groups != null && groups.contains("Sales"));
        hrBox.setSelected(groups != null && groups.contains("HR"));
    }

    private void saveSelectedUser() {
        String username = userList.getSelectedValue();
        if (username == null) {
            JOptionPane.showMessageDialog(this, "Select a user first.");
            return;
        }

        boolean authorized = authorizedBox.isSelected();
        String role = (String) roleBox.getSelectedItem();

        // Update user account with new values from UI
        creds.setBusinessAccess(username, authorized);
        creds.setBusinessRole(username, role);
        creds.clearBusinessGroups(username);

        if (salesBox.isSelected()) {
            creds.addBusinessGroup(username, "Sales");
        }

        if (hrBox.isSelected()) {
            creds.addBusinessGroup(username, "HR");
        }

        JOptionPane.showMessageDialog(this, "User updated.");
        // Re-load the user to refresh the UI with any changes
        loadSelectedUser();
    }
}