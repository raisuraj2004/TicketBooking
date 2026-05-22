/*
 * Concepts: Swing UI, GridBagLayout, Multithreading via ExecutorService
 */
package ui;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Font;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;

import javax.swing.BorderFactory;
import javax.swing.ButtonGroup;
import javax.swing.JComboBox;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JSpinner;
import javax.swing.JTextField;
import javax.swing.SpinnerNumberModel;
import javax.swing.JButton;
import javax.swing.JRadioButton;
import javax.swing.SwingUtilities;

import model.Bookable;
import model.Booking;
import model.BookingRequest;
import model.BookingResult;
import service.BookingManager;

public class BookingPanel extends JPanel {
    public interface BookingListener {
        void onBookingCompleted();
        void onLoginRequired();
    }

    private final BookingManager manager;
    private final BookingListener listener;

    private final JTextField nameField = new JTextField(20);
    private final JTextField emailField = new JTextField(20);
    private final JComboBox<Bookable> eventBox = new JComboBox<Bookable>();
    private final JSpinner qtySpinner = new JSpinner(new SpinnerNumberModel(1, 1, 10, 1));
    private final JComboBox<String> paymentBox = new JComboBox<String>(new String[]{"Card", "UPI", "Cash"});

    private final JRadioButton goldBtn = new JRadioButton("Gold");
    private final JRadioButton silverBtn = new JRadioButton("Silver");
    private final JRadioButton bronzeBtn = new JRadioButton("Bronze");

    private final JLabel goldPrice = new JLabel();
    private final JLabel silverPrice = new JLabel();
    private final JLabel bronzePrice = new JLabel();
    private final JLabel totalLabel = new JLabel("Total: Rs 0");

    private String selectedCategory = "Silver";

    public BookingPanel(BookingManager manager, BookingListener listener) {
        this.manager = manager;
        this.listener = listener;
        setLayout(new BorderLayout());
        setBackground(UITheme.BG);
        setBorder(BorderFactory.createEmptyBorder(24, 24, 24, 24));

        JPanel formCard = new JPanel(new GridBagLayout());
        formCard.setBackground(Color.WHITE);
        formCard.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(UITheme.BORDER),
                BorderFactory.createEmptyBorder(20, 20, 20, 20)));

        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(8, 8, 8, 8);
        gbc.fill = GridBagConstraints.HORIZONTAL;

        JLabel title = new JLabel("Confirm Your Booking");
        title.setFont(UITheme.TITLE);
        gbc.gridx = 0; gbc.gridy = 0; gbc.gridwidth = 2;
        formCard.add(title, gbc);
        gbc.gridwidth = 1;

        gbc.gridx = 0; gbc.gridy = 1; formCard.add(label("User Name"), gbc);
        gbc.gridx = 1; gbc.gridy = 1; formCard.add(nameField, gbc);

        gbc.gridx = 0; gbc.gridy = 2; formCard.add(label("Email"), gbc);
        gbc.gridx = 1; gbc.gridy = 2; formCard.add(emailField, gbc);

        gbc.gridx = 0; gbc.gridy = 3; formCard.add(label("Event"), gbc);
        gbc.gridx = 1; gbc.gridy = 3; formCard.add(eventBox, gbc);

        gbc.gridx = 0; gbc.gridy = 4; formCard.add(label("Ticket Tier"), gbc);
        gbc.gridx = 1; gbc.gridy = 4; formCard.add(tierPanel(), gbc);

        gbc.gridx = 0; gbc.gridy = 5; formCard.add(label("Quantity"), gbc);
        gbc.gridx = 1; gbc.gridy = 5; formCard.add(qtySpinner, gbc);

        gbc.gridx = 0; gbc.gridy = 6; formCard.add(label("Payment Method"), gbc);
        gbc.gridx = 1; gbc.gridy = 6; formCard.add(paymentBox, gbc);

        totalLabel.setFont(new Font("SansSerif", Font.BOLD, 14));
        gbc.gridx = 1; gbc.gridy = 7; formCard.add(totalLabel, gbc);

        JButton confirm = new JButton("Confirm Booking");
        confirm.setBackground(UITheme.INDIGO);
        confirm.setForeground(Color.WHITE);
        confirm.setFocusPainted(false);
        confirm.setBorder(BorderFactory.createEmptyBorder(10, 12, 10, 12));
        gbc.gridx = 1; gbc.gridy = 8; formCard.add(confirm, gbc);

        confirm.addActionListener(e -> submitBooking());
        eventBox.addItemListener(e -> updateTierPrices());
        qtySpinner.addChangeListener(e -> updateTotal());

        add(formCard, BorderLayout.NORTH);

        refreshEvents();
        updateUserFields();
    }

    private JPanel tierPanel() {
        JPanel panel = new JPanel(new GridBagLayout());
        panel.setBackground(Color.WHITE);
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(4, 4, 4, 4);
        gbc.fill = GridBagConstraints.HORIZONTAL;

        ButtonGroup group = new ButtonGroup();
        group.add(goldBtn);
        group.add(silverBtn);
        group.add(bronzeBtn);
        silverBtn.setSelected(true);

        goldBtn.addActionListener(e -> { selectedCategory = "Gold"; updateTotal(); });
        silverBtn.addActionListener(e -> { selectedCategory = "Silver"; updateTotal(); });
        bronzeBtn.addActionListener(e -> { selectedCategory = "Bronze"; updateTotal(); });

        gbc.gridx = 0; gbc.gridy = 0; panel.add(goldBtn, gbc);
        gbc.gridx = 1; gbc.gridy = 0; panel.add(goldPrice, gbc);
        gbc.gridx = 0; gbc.gridy = 1; panel.add(silverBtn, gbc);
        gbc.gridx = 1; gbc.gridy = 1; panel.add(silverPrice, gbc);
        gbc.gridx = 0; gbc.gridy = 2; panel.add(bronzeBtn, gbc);
        gbc.gridx = 1; gbc.gridy = 2; panel.add(bronzePrice, gbc);

        return panel;
    }

    private JLabel label(String text) {
        JLabel lbl = new JLabel(text);
        lbl.setFont(UITheme.BODY);
        lbl.setForeground(UITheme.MUTED);
        return lbl;
    }

    public void refreshEvents() {
        eventBox.removeAllItems();
        for (Bookable event : manager.getAllEvents()) {
            eventBox.addItem(event);
        }
        updateTierPrices();
    }

    public void syncUser() {
        updateUserFields();
    }

    public void setSelectedEvent(Bookable event) {
        if (event != null) {
            eventBox.setSelectedItem(event);
            updateTierPrices();
        }
    }

    private void updateUserFields() {
        if (manager.isLoggedIn()) {
            nameField.setText(manager.getCurrentUserName());
            emailField.setText(manager.getCurrentUserEmail());
            nameField.setEditable(false);
            emailField.setEditable(false);
        } else {
            nameField.setText("");
            emailField.setText("");
            nameField.setEditable(true);
            emailField.setEditable(true);
        }
    }

    private void updateTierPrices() {
        Bookable event = (Bookable) eventBox.getSelectedItem();
        double base = event == null ? 0.0 : event.getPrice();
        goldPrice.setText("Rs " + (int) manager.calculateAmount(base, "Gold", 1));
        silverPrice.setText("Rs " + (int) manager.calculateAmount(base, "Silver", 1));
        bronzePrice.setText("Rs " + (int) manager.calculateAmount(base, "Bronze", 1));
        updateTotal();
    }

    private void updateTotal() {
        Bookable event = (Bookable) eventBox.getSelectedItem();
        if (event == null) {
            totalLabel.setText("Total: Rs 0");
            return;
        }
        int qty = (Integer) qtySpinner.getValue();
        double total = manager.calculateAmount(event.getPrice(), selectedCategory, qty);
        totalLabel.setText("Total: Rs " + (int) total);
    }

    private void submitBooking() {
        if (!manager.isLoggedIn()) {
            JOptionPane.showMessageDialog(this, "Please login to continue booking.");
            if (listener != null) {
                listener.onLoginRequired();
            }
            return;
        }

        Bookable event = (Bookable) eventBox.getSelectedItem();
        if (event == null) {
            JOptionPane.showMessageDialog(this, "Please select an event.");
            return;
        }

        BookingRequest req = new BookingRequest(
                nameField.getText(),
                emailField.getText(),
                event,
                selectedCategory,
                (Integer) qtySpinner.getValue(),
                (String) paymentBox.getSelectedItem()
        );

        manager.submitBookingAsync(req, new java.util.function.Consumer<BookingResult<Booking>>() {
            @Override
            public void accept(final BookingResult<Booking> result) {
                SwingUtilities.invokeLater(new Runnable() {
                    @Override
                    public void run() {
                        JOptionPane.showMessageDialog(BookingPanel.this, result.getMessage());
                        if (result.isSuccess()) {
                            listener.onBookingCompleted();
                        }
                    }
                });
            }
        });
    }
}
