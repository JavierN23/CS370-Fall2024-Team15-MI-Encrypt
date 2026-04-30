import java.awt.*;
import java.awt.event.ActionEvent;
import java.util.ArrayList;
import java.util.List;
import javax.swing.*;
import javax.swing.border.CompoundBorder;
import javax.swing.border.EmptyBorder;
import javax.swing.border.LineBorder;
import javax.swing.border.TitledBorder;

public class AdminPanel extends JPanel {
    // Main application frame so this panel can switch screen
    private final AppFrame app;
    // Handles user account data and login/business permission updates
    private final Credentials creds;
    // Stores the currently logged-in user
    private UserAccount currentUser;

    // User list components

    // Model that stores usernames shown in the user list
    private final DefaultListModel<String> userModel = new DefaultListModel<>();
    // List displaying business users
    private final JList<String> userList = new JList<>(userModel);

    // User details components

    // Label showing which user is currently selected
    private final JLabel selectedUserLabel = new JLabel("No user selected");
    // Checkbox for enabling/disabling business authorization
    private final JCheckBox authorizedBox = new JCheckBox("Business Authorized");
    // Dropdown for user role
    private final JComboBox<String> roleBox = new JComboBox<>(new String[]{"employee", "admin"});
    // Checkboxes for assigning employee groups
    private final JCheckBox salesBox = new JCheckBox("Sales");
    private final JCheckBox hrBox = new JCheckBox("HR");

    // Action buttons

    // Save user changes
    private final JButton saveBtn = UI.accentButton("Save Changes");
    // Return to the previous screen
    private final JButton backBtn = UI.secondaryButton("Back");
    // Refreshes users and invite codes
    private final JButton refreshBtn = UI.secondaryButton("Refresh");

    // Invite Code Creation Components

    // Text field for entering a new invite code
    private final JTextField inviteCodeField = new JTextField(12);
    // Dropdown for selecting the role assigned by the invite code
    private final JComboBox<String> inviteRoleBox = new JComboBox<>(new String[]{"employee", "admin"});
    // Checkboxes for assigning groups to employee invite codes
    private final JCheckBox inviteSalesBox = new JCheckBox("Sales");
    private final JCheckBox inviteHRBox = new JCheckBox("HR");
    // Spinner for setting maximum number of uses for an invite code
    private final JSpinner maxUsesSpinner = new JSpinner(new SpinnerNumberModel(1, 1, 100, 1));
    // Button to auto-generate a readable invite code
    private final JButton generateCodeBtn = UI.secondaryButton("Generate Invite Code");
    // Button to create/save the invite code
    private final JButton createInviteBtn = UI.accentButton("Create Invite Code");

    // Invite Code List Components 

    // Model storing invite code display strings
    private final DefaultListModel<String> inviteModel = new DefaultListModel<>();
    // List displaying all existing invite codes
    private final JList<String> inviteList = new JList<>(inviteModel);
    // Button to activate or deactivate a selected invite code
    private final JButton deactivateInviteBtn = UI.secondaryButton("Deactivate Selected Code");
    // Button to permanently delete a selected invite code
    private final JButton deleteInviteBtn = UI.deleteButton("Delete Selected Code");
    // Button to reload invite code list
    private final JButton refreshInvitesBtn = UI.secondaryButton("Refresh Invite Codes");

    public AdminPanel(AppFrame app, Credentials creds, UserAccount currentUser) {
        this.app = app;
        this.creds = creds;
        this.currentUser = currentUser;

        // Apply consistent styles to inputs, lists, and buttons
        styleComponents();

        // Set custom renderers for the user list and invite code list
        userList.setCellRenderer(new UserListRenderer());
        inviteList.setCellRenderer(new InviteListRenderer());

        // Set up layout and UI components
        setLayout(new BorderLayout(12, 12));
        setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));
        setBackground(UI.BG);

        // Page title
        JLabel title = new JLabel("Business Admin Panel");
        title.setFont(title.getFont().deriveFont(Font.BOLD, 28f));
        title.setForeground(UI.TEXT);

        // Top Bar with title and refresh/back buttons
        JPanel top = new JPanel(new BorderLayout());
        top.setBackground(UI.BG);
        top.add(title, BorderLayout.WEST);

        JPanel topButtons = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        topButtons.setOpaque(false);
        topButtons.add(refreshBtn);
        topButtons.add(backBtn);
        top.add(topButtons, BorderLayout.EAST);

        add(top, BorderLayout.NORTH);

        // Tabbed interface for user management and invite code management
        JTabbedPane tabbedPane = new JTabbedPane();
        tabbedPane.setBackground(UI.CARD);
        tabbedPane.setForeground(UI.TEXT);
        tabbedPane.addTab("Manage Users", createUsersTab());
        tabbedPane.addTab("Invite Codes", createInviteTab());
        add(tabbedPane, BorderLayout.CENTER);

        // Refresh both users and invite codes
        refreshBtn.addActionListener(e -> {
            loadBusinessUsers();
            loadInviteCodes();
            updateActionStates();
        });

        // Return to previous screen
        backBtn.addActionListener(e -> app.showChoice());

        // When a user is selected, load that user's details into the form 
        userList.addListSelectionListener(e -> {
            if (!e.getValueIsAdjusting()) {
                loadSelectedUser();
                updateActionStates();
            }
        });

        inviteList.addListSelectionListener(e -> {
            if (!e.getValueIsAdjusting()) {
                updateActionStates();
            }
        });

        // Save changes for the selected user
        saveBtn.addActionListener(e -> saveSelectedUser());

        // Update controls when authorization state changes
        authorizedBox.addActionListener(e -> { 
            updateUserControls();
            updateActionStates();
        });

        // Update controls when role changes
        roleBox.addActionListener(e -> {
            updateUserControls();
            updateActionStates();
        });

        // Update invite controls when invite role changes
        inviteRoleBox.addActionListener(e -> {
            updateInviteControls();
            updateActionStates();
        });
        
        // Update button availability when invite code text changes
        inviteCodeField.getDocument().addDocumentListener(new SimpleDocumentListener(this::updateActionStates));

        // Geneerate a radom invite code
        generateCodeBtn.addActionListener(e -> {
            generateInviteCode();
            updateActionStates();
        });

        // Create a new invite code
        createInviteBtn.addActionListener(e -> {
            createInviteCode();
            updateActionStates();
        });

        // Reload the invite code list
        refreshInvitesBtn.addActionListener(e -> {
            loadInviteCodes();
            updateActionStates();
        });

        // Activate/deactivate selected invite code
        deactivateInviteBtn.addActionListener(e -> {
            toggleSelectedInviteActiveState();
            updateActionStates();
        });

        // Delete selected invite code
        deleteInviteBtn.addActionListener(e -> {
            deleteSelectedInvite();
            updateActionStates();
        });

        // Shortcuts
        installKeyboardShortcuts();

        // Initialize control states
        loadBusinessUsers();
        loadInviteCodes();
        updateUserControls();
        updateInviteControls();
        updateActionStates();
    }

    // Applies shared styling to text fields
    private void styleComponents() {
        UI.styleInput(inviteCodeField);
        UI.styleInput(roleBox);
        UI.styleInput(inviteRoleBox);

        styleList(userList);
        styleList(inviteList);

        styleCheckBox(authorizedBox);
        styleCheckBox(salesBox);
        styleCheckBox(hrBox);
        styleCheckBox(inviteSalesBox);
        styleCheckBox(inviteHRBox);

        selectedUserLabel.setForeground(UI.TEXT);
        selectedUserLabel.setFont(selectedUserLabel.getFont().deriveFont(Font.BOLD, 15f));

        maxUsesSpinner.setBackground(new Color(34, 34, 40));
        maxUsesSpinner.setForeground(UI.TEXT);

        // Style the spinner text field editor
        JComponent editor = maxUsesSpinner.getEditor();
        if (editor instanceof JSpinner.DefaultEditor) {
            JSpinner.DefaultEditor spinnerEditor = (JSpinner.DefaultEditor) editor;
            JTextField tf = spinnerEditor.getTextField();
            tf.setBackground(new Color(34, 34, 40));
            tf.setForeground(UI.TEXT);
            tf.setCaretColor(UI.TEXT);
            tf.setBorder(new CompoundBorder(
                    new LineBorder(UI.BORDER, 1, true), 
                    new EmptyBorder(6, 10, 6, 10)
                ));
        }

        // Standard button sizes
        Dimension mainBtn = new Dimension(170, 38);
        saveBtn.setPreferredSize(mainBtn);
        refreshBtn.setPreferredSize(mainBtn);
        backBtn.setPreferredSize(new Dimension(120, 38));

        Dimension inviteBtn = new Dimension(210, 38);
        generateCodeBtn.setPreferredSize(new Dimension(190, 38));
        createInviteBtn.setPreferredSize(new Dimension(180, 38));
        refreshInvitesBtn.setPreferredSize(inviteBtn);
        deactivateInviteBtn.setPreferredSize(inviteBtn);
        deleteInviteBtn.setPreferredSize(inviteBtn);

        syncButtonColors();
    }

    // Applies consistent styling to list components
    private void styleList(JList<String> list) {
        list.setBackground(UI.CARD);
        list.setForeground(UI.TEXT);
        list.setSelectionBackground(UI.ACCENT);
        list.setSelectionForeground(UI.TEXT);
        list.setFixedCellHeight(34);
        list.setBorder(new EmptyBorder(6, 6, 6, 6));
    }

    // Applies shared styling to checkboxes
    private void styleCheckBox(JCheckBox box) {
        box.setOpaque(false);
        box.setForeground(UI.TEXT);
        box.setFocusPainted(false);
    }

    // Creates a titled border used for sections of the admin panel
    private TitledBorder createSectionBorder(String title) {
        TitledBorder border = BorderFactory.createTitledBorder(
                BorderFactory.createLineBorder(UI.BORDER, 1, true),
                title   
        );
        border.setTitleColor(UI.TEXT);
        border.setTitleFont(new Font("SansSerif", Font.BOLD, 13));
        return border;
    }

    // Build the tab for manging business users
    private JPanel createUsersTab() {
        JPanel panel = new JPanel(new BorderLayout(12, 12));
        panel.setBackground(UI.BG);
        panel.setOpaque(true);

        userList.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);

        // Left side: user list
        JScrollPane leftScroll = new JScrollPane(userList);
        leftScroll.setPreferredSize(new Dimension(220, 400));
        leftScroll.setBackground(UI.CARD);
        leftScroll.getViewport().setBackground(UI.CARD);
        leftScroll.setViewportBorder(null);
        leftScroll.setBorder(createSectionBorder("Business Users"));
    
        // Right side: selected user details
        JPanel right = new JPanel();
        right.setLayout(new BoxLayout(right, BoxLayout.Y_AXIS));
        right.setBackground(UI.CARD);
        right.setOpaque(true);
        right.setBorder(BorderFactory.createCompoundBorder(
                createSectionBorder("User Details"), 
                BorderFactory.createEmptyBorder(12, 12, 12, 12)
            ));

        selectedUserLabel.setAlignmentX(Component.LEFT_ALIGNMENT);
        authorizedBox.setAlignmentX(Component.LEFT_ALIGNMENT);

        // Panel for user roles 
        JPanel rolePanel = new JPanel(new FlowLayout(FlowLayout.LEFT, 10, 0));
        rolePanel.setOpaque(false);
        rolePanel.setAlignmentX(Component.LEFT_ALIGNMENT);
        JLabel roleLabel = new JLabel("Role:");
        roleLabel.setForeground(UI.TEXT);
        roleLabel.setFont(roleLabel.getFont().deriveFont(Font.BOLD, 13f));
        rolePanel.add(roleLabel);
        rolePanel.add(roleBox);

        // Panels for groups
        JPanel groupsPanel = new JPanel(new FlowLayout(FlowLayout.LEFT, 10, 0));
        groupsPanel.setOpaque(false);
        groupsPanel.setAlignmentX(Component.LEFT_ALIGNMENT);
        JLabel groupsLabel = new JLabel("Groups:");
        groupsLabel.setForeground(UI.TEXT);
        groupsLabel.setFont(groupsLabel.getFont().deriveFont(Font.BOLD, 13f));
        groupsPanel.add(groupsLabel);
        groupsPanel.add(salesBox);
        groupsPanel.add(hrBox);

        saveBtn.setAlignmentX(Component.LEFT_ALIGNMENT);

        // Arranging right-side components
        right.add(selectedUserLabel);
        right.add(Box.createVerticalStrut(14));
        right.add(authorizedBox);
        right.add(Box.createVerticalStrut(16));
        right.add(rolePanel);
        right.add(Box.createVerticalStrut(16));
        right.add(groupsPanel);
        right.add(Box.createVerticalStrut(20));
        right.add(Box.createVerticalGlue());
        right.add(saveBtn);

        // Split pane separates the user list and detail editor
        JSplitPane split = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT, leftScroll, right);
        split.setDividerLocation(220);
        split.setBorder(null);
        split.setOpaque(true);
        split.setBackground(UI.BG);
        
        panel.add(split, BorderLayout.CENTER);
        return panel;
    }

    // Builds the tab for creating and managing invite codes
    private JPanel createInviteTab() {
        JPanel panel = new JPanel(new BorderLayout(12, 12));
        panel.setBackground(UI.BG);
        panel.setOpaque(true);

        // Top form for creating a new invite code
        JPanel formPanel = new JPanel();
        formPanel.setLayout(new BoxLayout(formPanel, BoxLayout.Y_AXIS));
        formPanel.setBackground(UI.CARD);
        formPanel.setOpaque(true);
        formPanel.setBorder(BorderFactory.createCompoundBorder(
                createSectionBorder("Create New Invite Code"),
                BorderFactory.createEmptyBorder(12, 12, 12, 12)
        ));

        JPanel codePanel = new JPanel(new FlowLayout(FlowLayout.LEFT, 10, 0));
        codePanel.setOpaque(false);
        JLabel codeLabel = new JLabel("Code:");
        codeLabel.setForeground(UI.TEXT);
        codeLabel.setFont(codeLabel.getFont().deriveFont(Font.BOLD, 13f));
        codePanel.add(codeLabel);
        codePanel.add(inviteCodeField);
        codePanel.add(generateCodeBtn);

        JPanel rolePanel = new JPanel(new FlowLayout(FlowLayout.LEFT, 10, 0));
        rolePanel.setOpaque(false);
        JLabel roleLabel = new JLabel("Role:");
        roleLabel.setForeground(UI.TEXT);
        roleLabel.setFont(roleLabel.getFont().deriveFont(Font.BOLD, 13f));
        rolePanel.add(roleLabel);
        rolePanel.add(inviteRoleBox);

        JPanel groupsPanel = new JPanel(new FlowLayout(FlowLayout.LEFT, 10, 0));
        groupsPanel.setOpaque(false);
        JLabel groupsLabel = new JLabel("Groups:");
        groupsLabel.setForeground(UI.TEXT);
        groupsLabel.setFont(groupsLabel.getFont().deriveFont(Font.BOLD, 13f));
        groupsPanel.add(groupsLabel);
        groupsPanel.add(inviteSalesBox);
        groupsPanel.add(inviteHRBox);

        JPanel usesPanel = new JPanel(new FlowLayout(FlowLayout.LEFT, 10, 0));
        usesPanel.setOpaque(false);
        JLabel usesLabel = new JLabel("Max Uses:");
        usesLabel.setForeground(UI.TEXT);
        usesLabel.setFont(usesLabel.getFont().deriveFont(Font.BOLD, 13f));
        usesPanel.add(usesLabel);
        usesPanel.add(maxUsesSpinner);

        JPanel createPanel = new JPanel(new FlowLayout(FlowLayout.LEFT, 10, 0));
        createPanel.setOpaque(false);
        createPanel.add(createInviteBtn);

        formPanel.add(codePanel);
        formPanel.add(Box.createVerticalStrut(10));
        formPanel.add(rolePanel);
        formPanel.add(Box.createVerticalStrut(10));
        formPanel.add(groupsPanel);
        formPanel.add(Box.createVerticalStrut(10));
        formPanel.add(usesPanel);
        formPanel.add(Box.createVerticalStrut(14));
        formPanel.add(createPanel);

        inviteList.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);

        // Bottom section showing existing invite codes
        JScrollPane inviteScroll = new JScrollPane(inviteList);
        inviteScroll.setBackground(UI.CARD);
        inviteScroll.getViewport().setBackground(UI.CARD);
        inviteScroll.setViewportBorder(null);
        inviteScroll.setBorder(createSectionBorder("Existing Invite Codes"));

        JPanel inviteButtonsPanel = new JPanel(new FlowLayout(FlowLayout.LEFT, 10, 8));
        inviteButtonsPanel.setBackground(UI.CARD);
        inviteButtonsPanel.setOpaque(true);
        inviteButtonsPanel.add(refreshInvitesBtn);
        inviteButtonsPanel.add(deactivateInviteBtn);
        inviteButtonsPanel.add(deleteInviteBtn);

        JPanel bottomPanel = new JPanel(new BorderLayout());
        bottomPanel.setBackground(UI.CARD);
        bottomPanel.setOpaque(true);
        bottomPanel.setBorder(new EmptyBorder(8, 0, 0, 0));
        bottomPanel.add(inviteScroll, BorderLayout.CENTER);
        bottomPanel.add(inviteButtonsPanel, BorderLayout.SOUTH);

        panel.add(formPanel, BorderLayout.NORTH);
        panel.add(bottomPanel, BorderLayout.CENTER);

        return panel;
    }

    // Loads the panel for a specific user and verifies admin access
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

        // Auto-select the first valid user if available
        if (!userModel.isEmpty () && !isPlaceholderValue(userModel.getElementAt(0))) {
            userList.setSelectedIndex(0);
        }

        updateActionStates();
    }

    // Clears the current user selection and resets user detail fields
    private void clearUserSelection() {
        userList.clearSelection();
        selectedUserLabel.setText("No user selected");
        authorizedBox.setSelected(false);
        roleBox.setSelectedItem("employee");
        salesBox.setSelected(false);
        hrBox.setSelected(false);
        updateUserControls();
    }

    // Loads all users who have business or both account access types
    private void loadBusinessUsers() {
        userModel.clear();

        if (currentUser == null) {
            userModel.addElement("No editable business users available");
            return;
        }

        String currentAdmin = currentUser.getUsername();

        List<UserAccount> accounts = creds.getAllAccounts();
        for (UserAccount account : accounts) {
            if (account == null) {
                continue;
            }

            String type = account.getAccountType();
            String assignedOwner = account.getAssignedVaultOwner();

            boolean businessType = "business".equalsIgnoreCase(type) || "both".equalsIgnoreCase(type);
            boolean linkedToCurrentAdmin = assignedOwner != null && assignedOwner.equalsIgnoreCase(currentAdmin);

            // Only show accounts that have business access (business or both)
            if (businessType && !account.isBusinessAdmin() && linkedToCurrentAdmin) {
                userModel.addElement(account.getUsername());
            }
        }

        // Show placeholder text if no user exist
        if (userModel.isEmpty()) {
            userModel.addElement("No editable business users available");
        }
    }

    // Loads the selected user's details into the from controls
    private void loadSelectedUser() {
        String username = userList.getSelectedValue();
        if (username == null || isPlaceholderValue(username)) {
            clearUserSelection();
            return;
        }

        UserAccount account = creds.getAccount(username);
        if (account == null) {
            clearUserSelection();
            return;
        }

        // Update label and checkboxes based on selected account
        selectedUserLabel.setText("Selected User: " + account.getUsername());

        boolean authorized = account.isBusinessAuthorized();
        authorizedBox.setSelected(authorized);

        // Default empty or invalid roles to employee
        String role = account.getBusinessRole();
        if (role == null || role.isBlank()) {
            role = "employee";
        }
        roleBox.setSelectedItem(role.toLowerCase());

        // Load the user's business groups
        List<String> groups = account.getAllowedBusinessGroups();
        salesBox.setSelected(containsIgnoreCase(groups, "Sales"));
        hrBox.setSelected(containsIgnoreCase(groups, "HR"));

        updateUserControls();
    }

    // Saves changes made to the selected user's business permissions and groups
    private void saveSelectedUser() {
        String username = userList.getSelectedValue();
        if (username == null || isPlaceholderValue(username)) {
            JOptionPane.showMessageDialog(this, "Select a user first.");
            return;
        }

        boolean authorized = authorizedBox.isSelected();

        // If business access is removed ,update and stop here
        if (!authorized) {
            creds.setBusinessAccess(username, false);
            JOptionPane.showMessageDialog(this, "User " + username + " is no longer authorized for business access.");
            loadSelectedUser();
            loadBusinessUsers();
            updateActionStates();
            return;
        }

        String role = (String) roleBox.getSelectedItem();
        if (role == null) {
            JOptionPane.showMessageDialog(this, "Select a valid role.");
            return;
        }

        // Employeee must belong to at least one group
        if ("employee".equalsIgnoreCase(role) && !salesBox.isSelected() && !hrBox.isSelected()) {
            JOptionPane.showMessageDialog(this, "Employee must belong to at least one group.");
            return;
        }

        // Update access, role, and groups
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
        loadBusinessUsers();
        updateActionStates();
    }

    // Enables or disables role/group controls based on authorization and selected role
    private void updateUserControls() {
        boolean authorized = authorizedBox.isSelected();
        String role = (String) roleBox.getSelectedItem();

        roleBox.setEnabled(authorized);

        boolean employeeMode = authorized && "employee".equalsIgnoreCase(role);
 
        salesBox.setEnabled(employeeMode);
        hrBox.setEnabled(employeeMode);

        // Clear group selection if user is not an authorized employee 
        if (!employeeMode) {
            salesBox.setSelected(false);
            hrBox.setSelected(false);
        }
    }

    // Enables or disables invite group checkboxes depending on selected inivte role
    private void updateInviteControls() {
        String role = (String) inviteRoleBox.getSelectedItem();
        boolean employeeMode = "employee".equalsIgnoreCase(role);

        inviteSalesBox.setEnabled(employeeMode);
        inviteHRBox.setEnabled(employeeMode);

        // Clear group selection if the invite is not for an employee
        if (!employeeMode) {
            inviteSalesBox.setSelected(false);
            inviteHRBox.setSelected(false);
        }
    }

    // Updates button enabled/disabled states based on current selections and form input
    private void updateActionStates() {
        String selectedUser = userList.getSelectedValue();
        boolean validUserSelected = selectedUser != null && !isPlaceholderValue(selectedUser);
        saveBtn.setEnabled(validUserSelected);

        String selectedInvite = inviteList.getSelectedValue();
        boolean validInviteSelected = selectedInvite != null && !isPlaceholderValue(selectedInvite);

        deactivateInviteBtn.setEnabled(validInviteSelected);
        deleteInviteBtn.setEnabled(validInviteSelected);

        // Change button text depending on whether the selected invite is active
        if  (validInviteSelected) {
            if (selectedInvite.endsWith("| ACTIVE")) {
                deactivateInviteBtn.setText("Deactivate Selected Code");
            } else if (selectedInvite.endsWith("| INACTIVE")) {
                deactivateInviteBtn.setText("Reactivate Selected Code");
            } else {
                deactivateInviteBtn.setText("Toggle Selected Code");
            }
        } else {
            deactivateInviteBtn.setText("Deactivate Selected Code");
        }

        // Only allow invite creation when required fields are valid
        String role = (String) inviteRoleBox.getSelectedItem();
        boolean employeeRole = "employee".equalsIgnoreCase(role);

        boolean createAllowed = !inviteCodeField.getText().trim().isEmpty();
        if (employeeRole) {
            createAllowed = createAllowed && (inviteSalesBox.isSelected() || inviteHRBox.isSelected());
        }
        createInviteBtn.setEnabled(createAllowed);

        syncButtonColors();
    }

    // Enabled/disabled colors to all buttons
    private void syncButtonColors() {
        applyButtonState(saveBtn, UI.ACCENT);
        applyButtonState(refreshBtn, UI.SECONDARYACCENT);
        applyButtonState(backBtn, UI.SECONDARYACCENT);
        applyButtonState(generateCodeBtn, UI.SECONDARYACCENT);
        applyButtonState(createInviteBtn, UI.ACCENT);
        applyButtonState(refreshInvitesBtn, UI.SECONDARYACCENT);
        applyButtonState(deactivateInviteBtn, UI.SECONDARYACCENT);
        applyButtonState(deleteInviteBtn, UI.DELETE);
    }

    // Applies the correct background/foreground colors depending on whether a button is enabled.
    private void applyButtonState(JButton button, Color enabledColor) {
        if (button.isEnabled()) {
            button.setBackground(enabledColor);
            button.setForeground(UI.TEXT);
        } else {
            button.setBackground(new Color(55, 55, 65));
            button.setForeground(new Color(220, 220, 230));
        }
    }

    // Adds Keyboard shortcuts: (Enter = save changes, ESCAPE = go back)    
    private void installKeyboardShortcuts() {
        InputMap inputMap = getInputMap(WHEN_ANCESTOR_OF_FOCUSED_COMPONENT);
        ActionMap actionMap = getActionMap();

        inputMap.put(KeyStroke.getKeyStroke("ENTER"), "saveChanges");
        actionMap.put("saveChanges", new AbstractAction() {
            @Override 
            public void actionPerformed(ActionEvent e) {
                if (saveBtn.isEnabled()) {
                    saveBtn.doClick();
                }
            }
        });

        inputMap.put(KeyStroke.getKeyStroke("ESCAPE"), "goBack");
        actionMap.put("goBack", new AbstractAction() {
            @Override
            public void actionPerformed(ActionEvent e) {
                backBtn.doClick();
            }
        });
    }

    // Generates and palce a random invite code into the invite code text field
    private void generateInviteCode() {
        inviteCodeField.setText(generateReadableInviteCode());
    }

    // Create invite in a specific format
    private String generateReadableInviteCode() {
        return generateInvitePart(4) + "-" + generateInvitePart(4);
    }

    // Generates one seciton of an invite code using readable letters and numbers
    private String generateInvitePart(int length) {
        String chars = "ABCDEFGHJKMNPQRSTUVWXYZ23456789";
        StringBuilder sb = new StringBuilder();

        for (int i = 0; i < length; i++) {
            int index = (int) (Math.random() * chars.length());
            sb.append(chars.charAt(index));
        }

        return sb.toString();
    }

    // Creates a new invite code using the data entered in the invite form
    private void createInviteCode() {
        if (currentUser == null || !currentUser.isBusinessAdmin()) {
            JOptionPane.showMessageDialog(this, "Only business admin can create invite codes.");
            return;
        }

        String code = inviteCodeField.getText().trim();
        String role = (String) inviteRoleBox.getSelectedItem();
        int maxUses = (Integer) maxUsesSpinner.getValue();

        if (code.isEmpty()) {
            JOptionPane.showMessageDialog(this, "Enter or generate a code first.");
            return;
        }

        // Collect selected groups
        List<String> groups = new ArrayList<>();
        if ("employee".equalsIgnoreCase(role)) {
            if (inviteSalesBox.isSelected()) {
                groups.add("Sales");
            }
            if (inviteHRBox.isSelected()) {
                groups.add("HR");
            }

            // Employees must have at least one group
            if (groups.isEmpty()) {
                JOptionPane.showMessageDialog(this, "Employee invite codes need at least one group.");
                return;
            }
        }

        // Create the code
        InviteCodeManager manager = InviteCodeManager.loadFromFile();
        boolean success = manager.createCode(code, role, groups, maxUses, currentUser.getUsername());

        if (success) { 
            // Reset form
            inviteCodeField.setText("");
            inviteRoleBox.setSelectedItem("employee");
            inviteSalesBox.setSelected(false);
            inviteHRBox.setSelected(false);
            maxUsesSpinner.setValue(1);

            loadInviteCodes();
            updateInviteControls();
            updateActionStates();

            JOptionPane.showMessageDialog(this, "Invite code created: " + code);
        } else {

            JOptionPane.showMessageDialog(this, "Failed to create invite code. It may already exist.");
        }
    }

    // Invite code into the invite list
    private void loadInviteCodes() {
        inviteModel.clear();

        if (currentUser == null) {
            inviteModel.addElement("No invite codes created yet");
            inviteList.clearSelection();
            updateActionStates();
            return;
        }

        InviteCodeManager manager = InviteCodeManager.loadFromFile();
        String currentAdmin = currentUser.getUsername();

        List<InviteCode> codes = manager.getAllCodes();
        for (InviteCode code : codes) {
            if (code == null) {
                continue;
            }

            String owner = code.getVaultOwnerUsername();
            if (owner != null && owner.equalsIgnoreCase(currentAdmin)) {
                inviteModel.addElement(formatInviteCodeDisplay(code));

            }
        }

        // Show placeholder if there are no invite codes
        if (inviteModel.isEmpty()) {
            inviteModel.addElement("No invite codes created yet");
        }

        inviteList.clearSelection();
        updateActionStates();
    }

    // Formats an invite code into a readable display string for the list
    private String formatInviteCodeDisplay(InviteCode code) {
        String status = code.isActive() ? "ACTIVE" : "INACTIVE";
        String groups = code.getGroups().isEmpty() ? "All" : String.join(",", code.getGroups());

        return code.getCode()
                + " | " + code.getRole()
                + " | Owner: " + code.getVaultOwnerUsername()
                + " | Groups: " + groups
                + " | Uses: " + code.getUsedCount() + "/" + code.getMaxUses()
                + " | " + status;
    }

    // Extracts just the invite code valut from the selcted display string
    private String getSelectedInviteCodeValue() {
        String selected = inviteList.getSelectedValue();
        if (selected == null || selected.isBlank() || isPlaceholderValue(selected)) {
            return null;
        }

        int separator = selected.indexOf(" | ");
        if (separator == -1) {
            return null;
        }

        return selected.substring(0, separator).trim();
    }

    // Toggles the selected invite code between active and inactive
    private void toggleSelectedInviteActiveState() {
        String codeValue = getSelectedInviteCodeValue();
        if (codeValue == null) {
            JOptionPane.showMessageDialog(this, "Select an invite code first.");
            return;
        }

        InviteCodeManager manager = InviteCodeManager.loadFromFile();
        InviteCode code = manager.getCode(codeValue);

        if (code == null) {
            JOptionPane.showMessageDialog(this, "Invite code could not be found.");
            loadInviteCodes();
            return;
        }

        boolean success;
        if (code.isActive()) {
            success = manager.deactivateCode(codeValue);
            if(success) {
                JOptionPane.showMessageDialog(this, "Invite code deactivated.");
            } else {
                JOptionPane.showMessageDialog(this, "Unable to deactivate invite code.");
            }
        } else {
            success = manager.activateCode(codeValue);
            if (success) {
                JOptionPane.showMessageDialog(this, "Invite code reactivated.");
            } else {
                JOptionPane.showMessageDialog(this, "Unable to reactivate invite code.");
            }
        }

        if (success) {
            loadInviteCodes();
            updateActionStates();
        }
    }
    
    // Deletes the selected invite code after confirmation
    private void deleteSelectedInvite() {
        String codeValue = getSelectedInviteCodeValue();
        if (codeValue == null) {
            JOptionPane.showMessageDialog(this, "Select an invite code first.");
            return;
        }

        int choice = JOptionPane.showConfirmDialog(
                this,
                "Delete invite code " + codeValue + "?",
                "Confirm Delete",
                JOptionPane.YES_NO_OPTION
        );

        if (choice != JOptionPane.YES_OPTION) {
            return;
        }

        InviteCodeManager manager = InviteCodeManager.loadFromFile();
        boolean success = manager.deleteCode(codeValue);

        if (success) {
            JOptionPane.showMessageDialog(this, "Invite code deleted.");
            loadInviteCodes();
            updateActionStates();
        } else {
            JOptionPane.showMessageDialog(this, "Unable to delete invite code.");
        }
    }

    // Check if a list item is just a placeholder message instead of real data
    private boolean isPlaceholderValue(String value) {
        return value != null
                && (value.equals("No business users available")
                || value.equals("No editable business users available")
                || value.equals("No invite codes created yet"));
    }

    // Return true if the given list contains the target value, ignoring letter case
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

    // Styles placeholder entries differently from normal usernames
    private class UserListRenderer extends DefaultListCellRenderer {
        @Override
        public Component getListCellRendererComponent(JList<?> list, Object value, int index, boolean isSelected, boolean cellHasFocus) {
            JLabel label = (JLabel) super.getListCellRendererComponent(list, value, index, isSelected, cellHasFocus);

            String text = value == null ? "" : value.toString();
            label.setBorder(new EmptyBorder(8, 12, 8, 12));

            if (isPlaceholderValue(text)) {
                label.setForeground(UI.MUTED);
                label.setFont(label.getFont().deriveFont(Font.ITALIC));
            } else {
                label.setForeground(UI.TEXT);
                label.setFont(label.getFont().deriveFont(Font.PLAIN));
            }

            label.setBackground(isSelected ? UI.ACCENT : UI.CARD);
            return label;
        }
    }

    // Active codes and inactive codes are shown in differnet colors
    private class InviteListRenderer extends DefaultListCellRenderer {
        @Override
        public Component getListCellRendererComponent(JList<?> list, Object value, int index, boolean isSelected, boolean cellHasFocus) {
            JLabel label = (JLabel) super.getListCellRendererComponent(list, value, index, isSelected, cellHasFocus);

            String text = value == null ? "" : value.toString();
            label.setBorder(new EmptyBorder(8, 12, 8, 12));
            label.setBackground(isSelected ? UI.ACCENT : UI.CARD);

            if (isPlaceholderValue(text)) {
                label.setForeground(UI.MUTED);
                label.setFont(label.getFont().deriveFont(Font.ITALIC));
                return label;
            }

            if (text.endsWith("| ACTIVE")) {
                label.setForeground(isSelected ? UI.TEXT : new Color(120, 220, 140));
            } else if (text.endsWith("| INACTIVE")) {
                label.setForeground(isSelected ? UI.TEXT : new Color(225, 130, 130));
            } else {
                label.setForeground(UI.TEXT);
            }

            label.setFont(label.getFont().deriveFont(Font.PLAIN));
            return label;
        }
    }

    //  Helper class for document change events
    private static class SimpleDocumentListener implements javax.swing.event.DocumentListener {
        private final Runnable callback;

        public SimpleDocumentListener(Runnable callback) {
            this.callback = callback;
        }

        @Override
        public void insertUpdate(javax.swing.event.DocumentEvent e) {
            callback.run();
        }

        @Override
        public void removeUpdate(javax.swing.event.DocumentEvent e) {
            callback.run();
        }

        @Override
        public void changedUpdate(javax.swing.event.DocumentEvent e) {
            callback.run();
        }
    }
}