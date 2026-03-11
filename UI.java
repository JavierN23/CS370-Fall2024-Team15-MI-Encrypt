import java.awt.*;
import javax.swing.*;
import javax.swing.border.CompoundBorder;
import javax.swing.border.EmptyBorder;
import javax.swing.border.LineBorder;

public class UI {


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

    public static JPanel page() {
        JPanel p = new JPanel();
        p.setLayout(new BoxLayout(p, BoxLayout.Y_AXIS));
        p.setBorder(new EmptyBorder(10, 10, 10, 10));
        p.setBackground(BG);
        return p;
    }

    public static JPanel card() {
        JPanel c = new JPanel();
        c.setLayout(new BoxLayout(c, BoxLayout.Y_AXIS));
        c.setBorder(new CompoundBorder(
            new LineBorder(BORDER, 1, true), 
            new EmptyBorder(14, 14, 14, 14)));

        c.setBackground(CARD);

        c.setPreferredSize(new Dimension(420, 320));
        return c;
    }

    public static JPanel pageBorder() {
        JPanel p = new JPanel(new BorderLayout(12, 12));
        p.setBorder(new EmptyBorder(10, 10, 10, 10));
        p.setBackground(BG);
        return p;
    }

    public static JLabel h1(String text) {
        JLabel l = new JLabel(text);
        l.setFont(l.getFont().deriveFont(Font.BOLD, 36f));
        l.setForeground(TEXT);
        l.setAlignmentX(Component.CENTER_ALIGNMENT);
        return l;
    }

    public static JLabel label(String text) {
        JLabel l = new JLabel(text);
        l.setForeground(TEXT);
        return l;
    }

    public static JLabel subtle(String text) {
        JLabel l = new JLabel(text);
        l.setForeground(new Color(MUTED.getRed(), MUTED.getGreen(), MUTED.getBlue(), 180));
        return l;
    }

    public static void space(JComponent p, int px) {
        p.add(Box.createVerticalStrut(px));
    }

    public static JPanel row(Component left, Component right) {
        JPanel r = new JPanel(new BorderLayout(10, 0));
        r.setOpaque(false);
        r.add(left, BorderLayout.WEST);
        r.add(right, BorderLayout.CENTER);
        return r;
    }

    public static void styleInput(JTextField f) {
        f.setBackground(new Color(34, 34, 40));
        f.setForeground(TEXT);
        f.setCaretColor(TEXT);

        f.setFont(f.getFont().deriveFont(14f));

        f.setBorder(new CompoundBorder(
            new LineBorder(BORDER, 1, true),
            new EmptyBorder(8, 12, 8, 12)));
    }

    public static void styleButton(JButton b, Color bg) {
        b.setFocusPainted(false);
        b.setBackground(bg);
        b.setForeground(TEXT);
        b.setBorder(new EmptyBorder(10, 14, 10,14));
        b.setOpaque(true);
        b.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
    }

    public static JButton accentButton(String text) {
        JButton b = new JButton(text);
        styleButton(b, ACCENT);
        return b;
    }

    public static JButton secondaryButton(String text) {
        JButton b = new JButton(text);
        styleButton(b, SECONDARYACCENT);
        return b;
    }

    public static JButton deleteButton(String text) {
        JButton b = new JButton(text);
        styleButton(b, DELETE);
        return b;
    }

    public static JLabel chip(String text, Color bg) {
        JLabel c = new JLabel(" " + text + "  ");
        c.setOpaque(true);
        c.setBackground(bg);
        c.setForeground(TEXT);
        c.setBorder(new EmptyBorder(4, 10, 4, 10));
        return c;
    }
}