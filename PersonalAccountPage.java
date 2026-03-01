import javax.swing.*;

public class PersonalAccountPage {
    private PasswordManager pm;
    private String username;

    private DefaultListModel<PasswordEntry> listModel = new DefaultListModel<>(); // Holds Site and Username for display
    private JList<PasswordEntry> entryList = new JList<>(listModel); // Displays the list of entries

    
    public PersonalAccountPage(Credentials creds, PasswordManager pm, String username) {
        this.pm = pm;
        this.username = username;

        JFrame frame = new JFrame("Personal Account Page");
        frame.setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
        frame.setSize(400, 300);
        frame.setLayout(null);

        JLabel label = new JLabel("Welcome, " + username + " (Personal Account)");
        label.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));
        frame.add(label);

        JScrollPane scrollPane = new JScrollPane(entryList);
        scrollPane.setBounds(10, 50, 360, 150);
        frame.add(scrollPane);

        JButton addButton = new JButton("Add Entry");
        JButton deleteButton = new JButton("Delete Entry");
        JButton viewButton = new JButton("View Entry");

        addButton.setBounds(10, 210, 100, 30);
        deleteButton.setBounds(120, 210, 100, 30);
        viewButton.setBounds(230, 210, 100, 30);

        frame.add(addButton);
        frame.add(deleteButton);
        frame.add(viewButton);

        refreshList();

 addButton.addActionListener(e -> addEntryDialog(frame));

        // Delete the selected entry
        deleteButton.addActionListener(e -> {
            int idx = entryList.getSelectedIndex();
            if (idx == -1) {
                JOptionPane.showMessageDialog(frame, "Please select an entry to delete.");
                return;
            }
            pm.removeEntry(username, idx);
            refreshList();
        });

        // View the selected entry's details
        viewButton.addActionListener(e -> {
            PasswordEntry selected = entryList.getSelectedValue();
            if (selected == null) {
                JOptionPane.showMessageDialog(frame, "Please select an entry to view.");
                return;
            }

            JOptionPane.showMessageDialog(
                    frame,
                    "Site: " + selected.getSite()
                            + "\nUsername: " + selected.getUsername()
                            + "\nPassword: " + selected.getPassword()
            );
        });

        frame.setLocationRelativeTo(null);
        frame.setVisible(true);
    }

    // Refresh the list of entries displayed
    private void refreshList() {
        listModel.clear();
        for (PasswordEntry entry : pm.getEntriesForUser(username)) {
            listModel.addElement(entry);
        }
    }

    // Adds a new entry
    private void addEntryDialog(JFrame parent) {
        JTextField siteField = new JTextField();
        JTextField userField = new JTextField();
        JPasswordField passField = new JPasswordField();

        Object[] message = {
                "Site:", siteField,
                "Username:", userField,
                "Password:", passField
        };

        int option = JOptionPane.showConfirmDialog(parent, message, "Add Entry", JOptionPane.OK_CANCEL_OPTION);
        if (option == JOptionPane.OK_OPTION) {
            String site = siteField.getText().trim();
            String user = userField.getText().trim();
            String pass = new String(passField.getPassword());

            if (site.isEmpty() || user.isEmpty() || pass.isEmpty()) {
                JOptionPane.showMessageDialog(parent, "All fields are required.");
                return;
            }

            pm.addEntry(username, new PasswordEntry(site, user, pass));
            refreshList();
        }
    }
}