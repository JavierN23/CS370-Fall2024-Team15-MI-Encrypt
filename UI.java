import java.awt.*;
import java.io.File;
import javax.swing.*;
import javax.swing.border.CompoundBorder;
import javax.swing.border.EmptyBorder;
import javax.swing.border.LineBorder;

public class UI {

    // App colors
    public static final Color BG = new Color(16, 16, 20);
    public static final Color CARD = new Color(26, 26, 32);
    public static final Color BORDER = new Color(55, 55, 65);
    public static final Color TEXT = new Color(235, 235, 240);
    public static final Color MUTED = new Color(160, 160, 170);

    public static final Color ACCENT = new Color(90, 160, 255);
    public static final Color DELETE = new Color(255, 90, 90);
    public static final Color SECONDARYACCENT = new Color(70, 70, 80);

    public static final Color PERSONAL = new Color(90, 160, 255);
    public static final Color BUSINESS = new Color(160, 120, 255);

    // Basic page layout
    public static JPanel page() {
        JPanel p = new JPanel();
        p.setLayout(new BoxLayout(p, BoxLayout.Y_AXIS));
        p.setBorder(new EmptyBorder(10, 10, 10, 10));
        p.setBackground(BG);
        return p;
    }

    // Card-style container
    public static JPanel card() {
        JPanel c = new JPanel();
        c.setLayout(new BoxLayout(c, BoxLayout.Y_AXIS));
        c.setBorder(new CompoundBorder(
            new LineBorder(BORDER, 1, true), 
            new EmptyBorder(20, 20, 20, 20)));

        c.setBackground(CARD);

        c.setMinimumSize(new Dimension(560, 500));
        c.setMaximumSize(new Dimension(760, Integer.MAX_VALUE));

        return c;
    }

    // Page with border layout
    public static JPanel pageBorder() {
        JPanel p = new JPanel(new BorderLayout(12, 12));
        p.setBorder(new EmptyBorder(10, 10, 10, 10));
        p.setBackground(BG);
        return p;
    }

    // Big title text
    public static JLabel h1(String text) {
        JLabel l = new JLabel(text);
        l.setFont(l.getFont().deriveFont(Font.BOLD, 36f));
        l.setForeground(TEXT);
        l.setAlignmentX(Component.CENTER_ALIGNMENT);
        return l;
    }

    // Normal label
    public static JLabel label(String text) {
        JLabel l = new JLabel(text);
        l.setForeground(TEXT);
        return l;
    }

    // Smaller faded label
    public static JLabel subtle(String text) {
        JLabel l = new JLabel(text);
        l.setForeground(new Color(MUTED.getRed(), MUTED.getGreen(), MUTED.getBlue(), 180));
        return l;
    }

    // Adds vertical space
    public static void space(JComponent p, int px) {
        p.add(Box.createVerticalStrut(px));
    }

    // Row with label + input
    public static JPanel row(String labelText, JComponent field) {
        return row(labelText, field, 12, 110); // gap and label width
    }

    // Row with custom spacing
    public static JPanel row(String labelText, JComponent field, int gap, int labelWidth) {
        JPanel r = new JPanel(new FlowLayout(FlowLayout.CENTER, gap, 0));
        r.setOpaque(false);
        r.setAlignmentX(Component.CENTER_ALIGNMENT);

        JLabel lbl = label(labelText);
        Dimension labelSize = new Dimension(labelWidth, 34);
        lbl.setPreferredSize(labelSize);
        lbl.setMaximumSize(labelSize);
        lbl.setMinimumSize(labelSize);

        Dimension fieldSize = new Dimension(440, 42);
        field.setPreferredSize(fieldSize);
        field.setMaximumSize(fieldSize);
        field.setMinimumSize(fieldSize);

        r.add(lbl);
        r.add(field);

        r.setMaximumSize(new Dimension(620, 50));
        return r;
    }

    // Style for text fields
    public static void styleInput(JTextField f) {
        f.setBackground(new Color(34, 34, 40));
        f.setForeground(TEXT);
        f.setCaretColor(TEXT);

        f.setFont(f.getFont().deriveFont(14f));

        f.setBorder(new CompoundBorder(
            new LineBorder(BORDER, 1, true),
            new EmptyBorder(10, 12, 10, 12)));
    }

    // Style for dropdowns
    public static void styleInput(JComboBox<?> box) {
        box.setBackground(new Color(34, 34, 40));
        box.setForeground(TEXT);
        box.setFont(box.getFont().deriveFont(14f));
        box.setBorder(new CompoundBorder(
            new LineBorder(BORDER, 1, true),
            new EmptyBorder(6, 10, 6, 10)));
    }

    // Base button style
    public static void styleButton(JButton b, Color bg) {
        b.setFocusPainted(false);
        b.setBackground(bg);
        b.setForeground(TEXT);
        b.setBorder(new EmptyBorder(10, 14, 10,14));
        b.setOpaque(true);
        b.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
    }

    // Blue button
    public static JButton accentButton(String text) {
        JButton b = new JButton(text);
        styleButton(b, ACCENT);
        return b;
    }

    // Gray Button
    public static JButton secondaryButton(String text) {
        JButton b = new JButton(text);
        styleButton(b, SECONDARYACCENT);
        return b;
    }

    // Red Delete Button
    public static JButton deleteButton(String text) {
        JButton b = new JButton(text);
        styleButton(b, DELETE);
        return b;
    }

    // Small Label badge
    public static JLabel chip(String text, Color bg) {
        JLabel c = new JLabel(" " + text + "  ");
        c.setOpaque(true);
        c.setBackground(bg);
        c.setForeground(TEXT);
        c.setBorder(new EmptyBorder(4, 10, 4, 10));
        return c;
    }

    // Applies global UI colors
    public static void applyGlobalStyle() {
        UIManager.put("Panel.background", BG);
        UIManager.put("OptionPane.background", BG);
        UIManager.put("OptionPane.messageForeground", TEXT);

        UIManager.put("Label.foreground", TEXT);

        UIManager.put("OptionPane.buttonBackground", SECONDARYACCENT);
        UIManager.put("OptionPane.ButtonForeground", TEXT);

        UIManager.put("Button.background", SECONDARYACCENT);
        UIManager.put("Button.foreground", TEXT);

        UIManager.put("TextField.background", new Color(34, 34, 40));
        UIManager.put("TextField.foreground", TEXT);
        UIManager.put("TextField.caretForeground", TEXT);

        // Tabsand containers
        UIManager.put("TabbedPane.background", CARD);
        UIManager.put("TabbedPane.foreground", TEXT);
        UIManager.put("TabbedPane.contentAreaColor", CARD);

        UIManager.put("ScrollPane.background", CARD);
        UIManager.put("Viewport.background", CARD);

        UIManager.put("SplitPane.background", BG);
        UIManager.put("SplitPaneDivider.background", BORDER);
    }

    // Folder where app data is stored
    public static File dataDir() {
        File dir = new File(System.getProperty("user.home"), ".MI-Encrypt");
        if (!dir.exists()) {
            dir.mkdirs();
        }
        return dir;
    }

    // File paths for saved data
    public static File usersFile() {
        return new File(dataDir(), "users.dat");
    }

    public static File passwordsFile() {
        return new File(dataDir(), "passwords.dat");
    }

    public static File inviteCodesFile() {
        return new File(dataDir(), "invitecodes.dat");
    }

    public static File creditCardsFile() {
        return new File(dataDir(), "creditcards.dat");
    }
}