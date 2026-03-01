import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;

public class UI {
    public static JPanel page() {
        JPanel p = new JPanel();
        p.setLayout(new BoxLayout(p, BoxLayout.Y_AXIS));
        p.setBorder(new EmptyBorder(20, 20, 20, 20));
        p.setBackground(new Color(245, 246, 248));
        return p;
    }

    public static JPanel card() {
        JPanel c = new JPanel();
        c.setLayout(new BoxLayout(c, BoxLayout.Y_AXIS));
        c.setBorder(new EmptyBorder(18, 18, 18, 18));
        c.setBackground(Color.WHITE);
        c.setAlignmentX(Component.CENTER_ALIGNMENT);
        c.setMaximumSize(new Dimension(520, 1000));
        return c;
    }

    public static JLabel h1(String text) {
        JLabel l = new JLabel(text);
        l.setFont(l.getFont().deriveFont(Font.BOLD, 22f));
        l.setAlignmentX(Component.CENTER_ALIGNMENT);
        return l;
    }

    public static JLabel subtle(String text) {
        JLabel l = new JLabel(text);
        l.setForeground(new Color(90, 90, 90));
        l.setAlignmentX(Component.CENTER_ALIGNMENT);
        return l;
    }

    public static void space(JPanel p, int px) {
        p.add(Box.createVerticalStrut(px));
    }

    public static JPanel row(Component left, Component right) {
        JPanel r = new JPanel(new BorderLayout(10, 0));
        r.setOpaque(false);
        r.add(left, BorderLayout.WEST);
        r.add(right, BorderLayout.CENTER);
        return r;
    }
}