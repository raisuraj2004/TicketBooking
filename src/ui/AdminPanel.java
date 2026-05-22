/*
 * Concepts: Swing UI, Java2D, Factory Pattern, Encapsulation
 */
package ui;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.RenderingHints;
import java.awt.geom.Rectangle2D;
import java.util.Map;

import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JTextField;
import javax.swing.SwingUtilities;
import javax.swing.JOptionPane;
import java.awt.GridBagLayout;
import java.awt.GridBagConstraints;
import java.awt.Insets;

import model.Bookable;
import service.BookingManager;
import service.EventFactory;

public class AdminPanel extends JPanel {
    public interface EventAddedListener {
        void onEventAdded();
    }

    private final BookingManager manager;
    private final EventAddedListener listener;

    private final JTextField nameField = new JTextField(16);
    private final JTextField dateField = new JTextField(10);
    private final JTextField venueField = new JTextField(16);
    private final JTextField seatsField = new JTextField(6);
    private final JTextField priceField = new JTextField(6);
    private final StatsChart chart = new StatsChart();

    public AdminPanel(BookingManager manager, EventAddedListener listener) {
        this.manager = manager;
        this.listener = listener;
        setLayout(new BorderLayout());
        setBackground(UITheme.BG);
        setBorder(BorderFactory.createEmptyBorder(16, 16, 16, 16));

        JPanel formCard = new JPanel(new GridBagLayout());
        formCard.setBackground(Color.WHITE);
        formCard.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(UITheme.BORDER),
                BorderFactory.createEmptyBorder(16, 16, 16, 16)));

        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(6, 6, 6, 6);
        gbc.fill = GridBagConstraints.HORIZONTAL;

        JLabel title = new JLabel("Add New Event");
        title.setFont(UITheme.SUBTITLE);
        gbc.gridx = 0; gbc.gridy = 0; gbc.gridwidth = 2; formCard.add(title, gbc);
        gbc.gridwidth = 1;

        gbc.gridx = 0; gbc.gridy = 1; formCard.add(label("Name"), gbc);
        gbc.gridx = 1; gbc.gridy = 1; formCard.add(nameField, gbc);

        gbc.gridx = 0; gbc.gridy = 2; formCard.add(label("Date"), gbc);
        gbc.gridx = 1; gbc.gridy = 2; formCard.add(dateField, gbc);

        gbc.gridx = 0; gbc.gridy = 3; formCard.add(label("Venue"), gbc);
        gbc.gridx = 1; gbc.gridy = 3; formCard.add(venueField, gbc);

        gbc.gridx = 0; gbc.gridy = 4; formCard.add(label("Total Seats"), gbc);
        gbc.gridx = 1; gbc.gridy = 4; formCard.add(seatsField, gbc);

        gbc.gridx = 0; gbc.gridy = 5; formCard.add(label("Price"), gbc);
        gbc.gridx = 1; gbc.gridy = 5; formCard.add(priceField, gbc);

        JButton add = new JButton("Add Event");
        add.setBackground(UITheme.INDIGO);
        add.setForeground(Color.WHITE);
        add.setFocusPainted(false);
        add.setBorder(BorderFactory.createEmptyBorder(8, 12, 8, 12));
        gbc.gridx = 1; gbc.gridy = 6; formCard.add(add, gbc);

        add.addActionListener(e -> addEvent());

        JLabel statsTitle = new JLabel("Booking Stats");
        statsTitle.setFont(UITheme.SUBTITLE);
        statsTitle.setBorder(BorderFactory.createEmptyBorder(0, 0, 8, 0));

        JPanel statsPanel = new JPanel(new BorderLayout());
        statsPanel.setBackground(Color.WHITE);
        statsPanel.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(UITheme.BORDER),
                BorderFactory.createEmptyBorder(16, 16, 16, 16)));
        statsPanel.add(statsTitle, BorderLayout.NORTH);
        statsPanel.add(chart, BorderLayout.CENTER);

        JPanel left = new JPanel(new BorderLayout());
        left.setBackground(UITheme.BG);
        left.add(formCard, BorderLayout.NORTH);

        add(left, BorderLayout.WEST);
        add(statsPanel, BorderLayout.CENTER);

        chart.setPreferredSize(new Dimension(600, 300));
    }

    private JLabel label(String text) {
        JLabel lbl = new JLabel(text);
        lbl.setFont(UITheme.BODY);
        lbl.setForeground(UITheme.MUTED);
        return lbl;
    }

    private void addEvent() {
        try {
            String name = nameField.getText();
            String date = dateField.getText();
            String venue = venueField.getText();
            int seats = Integer.parseInt(seatsField.getText());
            double price = Double.parseDouble(priceField.getText());
            String id = "E" + System.currentTimeMillis();
            Bookable event = EventFactory.createEvent("movie", id, name, date, venue, price, seats);
            manager.addEvent(event);
            listener.onEventAdded();
            refreshStats();
            JOptionPane.showMessageDialog(this, "Event added.");
        } catch (Exception ex) {
            JOptionPane.showMessageDialog(this, "Invalid input.");
        }
    }

    public void refreshStats() {
        SwingUtilities.invokeLater(new Runnable() {
            @Override
            public void run() {
                chart.setData(manager.getSeatsSoldPerEvent(), manager.getTotalRevenue());
                chart.repaint();
            }
        });
    }

    private static class StatsChart extends JPanel {
        private Map<String, Integer> data;
        private double totalRevenue;

        public void setData(Map<String, Integer> data, double totalRevenue) {
            this.data = data;
            this.totalRevenue = totalRevenue;
        }

        @Override
        protected void paintComponent(Graphics g) {
            super.paintComponent(g);
            Graphics2D g2 = (Graphics2D) g;
            g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

            int width = getWidth();
            int height = getHeight();
            g2.setColor(Color.WHITE);
            g2.fillRect(0, 0, width, height);

            if (data == null || data.isEmpty()) {
                g2.setColor(Color.GRAY);
                g2.drawString("No bookings yet", 20, 30);
                return;
            }

            int max = 1;
            for (int v : data.values()) {
                if (v > max) max = v;
            }

            int barWidth = Math.max(40, width / data.size());
            int x = 20;
            int base = height - 40;

            g2.setColor(UITheme.INDIGO);
            for (Map.Entry<String, Integer> e : data.entrySet()) {
                int barHeight = (int) ((double) e.getValue() / max * (height - 80));
                Rectangle2D bar = new Rectangle2D.Double(x, base - barHeight, barWidth - 12, barHeight);
                g2.fill(bar);
                g2.setColor(Color.DARK_GRAY);
                g2.drawString(e.getKey(), x, base + 14);
                g2.setColor(UITheme.INDIGO);
                x += barWidth;
            }

            g2.setColor(Color.DARK_GRAY);
            g2.drawString("Total Revenue: " + totalRevenue, 20, 20);
        }
    }
}
