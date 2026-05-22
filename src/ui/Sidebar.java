/*
 * Concepts: Swing UI, Encapsulation, Custom Painting
 */
package ui;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.GridLayout;
import java.util.HashMap;
import java.util.Map;

import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.SwingConstants;

public class Sidebar extends JPanel {
    public interface NavListener {
        void onNav(String key);
    }

    private final Map<String, JButton> buttons = new HashMap<String, JButton>();
    private String activeKey = "HOME";

    public Sidebar(NavListener listener) {
        setBackground(UITheme.NAVY);
        setPreferredSize(new Dimension(230, 0));
        setLayout(new BorderLayout());

        JPanel brand = new JPanel(new BorderLayout());
        brand.setOpaque(false);
        brand.setBorder(javax.swing.BorderFactory.createEmptyBorder(20, 12, 16, 12));

        JLabel name = new JLabel("FunSeats");
        name.setForeground(Color.WHITE);
        name.setFont(new Font("Segoe UI", Font.BOLD, 18));
        name.setHorizontalAlignment(SwingConstants.CENTER);

        brand.add(name, BorderLayout.CENTER);

        JPanel navPanel = new JPanel(new GridLayout(5, 1, 0, 6));
        navPanel.setBackground(UITheme.NAVY);

        navPanel.add(makeButton("HOME", "\u2302  Home", listener));
        navPanel.add(makeButton("BOOKING", "\u270E  Booking", listener));
        navPanel.add(makeButton("MY_BOOKINGS", "\u260E  My Bookings", listener));
        navPanel.add(makeButton("LOGIN", "\u26BF  Login", listener));
        navPanel.add(makeButton("ADMIN", "\u2605  Admin", listener));

        add(brand, BorderLayout.NORTH);
        add(navPanel, BorderLayout.CENTER);
    }

    private JButton makeButton(final String key, String label, final NavListener listener) {
        JButton btn = new JButton(label);
        btn.setForeground(Color.WHITE);
        btn.setBackground(UITheme.NAVY);
        btn.setFocusPainted(false);
        btn.setBorderPainted(false);
        btn.setFont(new Font("Segoe UI", Font.BOLD, 15));
        btn.setHorizontalAlignment(JButton.LEFT);
        btn.addActionListener(e -> listener.onNav(key));
        buttons.put(key, btn);
        return btn;
    }

    public void setActive(String key) {
        activeKey = key;
        for (Map.Entry<String, JButton> entry : buttons.entrySet()) {
            JButton btn = entry.getValue();
            if (entry.getKey().equals(key)) {
                btn.setBackground(UITheme.INDIGO);
            } else {
                btn.setBackground(UITheme.NAVY);
            }
        }
        repaint();
    }
}
