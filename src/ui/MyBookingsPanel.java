/*
 * Concepts: Swing UI, JTable, Exception Handling
 */
package ui;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Component;
import java.awt.Dimension;

import javax.swing.AbstractCellEditor;
import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.table.AbstractTableModel;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.TableCellEditor;
import javax.swing.table.TableCellRenderer;

import exception.BookingNotFoundException;
import model.Booking;
import service.BookingManager;
import util.ReceiptGenerator;

public class MyBookingsPanel extends JPanel {
    public interface BookingActionListener {
        void onChange();
        void onLoginRequested();
    }

    private final BookingManager manager;
    private final BookingActionListener listener;
    private final BookingTableModel tableModel = new BookingTableModel();
    private final JTable table = new JTable(tableModel);
    private final JLabel statusLabel = new JLabel();
    private final JButton loginBtn = new JButton("Login to view");

    public MyBookingsPanel(BookingManager manager, BookingActionListener listener) {
        this.manager = manager;
        this.listener = listener;
        setLayout(new BorderLayout());
        setBackground(UITheme.BG);
        setBorder(BorderFactory.createEmptyBorder(16, 16, 16, 16));

        JPanel header = new JPanel(new BorderLayout());
        header.setBackground(UITheme.BG);
        statusLabel.setFont(UITheme.SUBTITLE);
        statusLabel.setForeground(UITheme.MUTED);

        loginBtn.setBackground(UITheme.INDIGO);
        loginBtn.setForeground(Color.WHITE);
        loginBtn.setFocusPainted(false);
        loginBtn.setBorder(BorderFactory.createEmptyBorder(6, 12, 6, 12));
        loginBtn.addActionListener(e -> listener.onLoginRequested());

        header.add(statusLabel, BorderLayout.WEST);
        header.add(loginBtn, BorderLayout.EAST);

        table.setRowHeight(36);
        table.setFillsViewportHeight(true);
        table.getTableHeader().setBackground(Color.WHITE);
        table.getTableHeader().setFont(UITheme.SUBTITLE);
        table.setDefaultRenderer(Object.class, new StripeRenderer());

        table.getColumnModel().getColumn(5).setCellRenderer(new ButtonRenderer("Cancel"));
        table.getColumnModel().getColumn(5).setCellEditor(new ButtonEditor("Cancel"));
        table.getColumnModel().getColumn(6).setCellRenderer(new ButtonRenderer("Download"));
        table.getColumnModel().getColumn(6).setCellEditor(new ButtonEditor("Download"));

        JScrollPane scroll = new JScrollPane(table);
        scroll.setPreferredSize(new Dimension(800, 500));
        scroll.setBorder(BorderFactory.createLineBorder(UITheme.BORDER));
        scroll.getViewport().setBackground(Color.WHITE);

        add(header, BorderLayout.NORTH);
        add(scroll, BorderLayout.CENTER);

        refreshTable();
    }

    public void refreshTable() {
        if (!manager.isLoggedIn()) {
            statusLabel.setText("Login to view your bookings");
            loginBtn.setText("Login to view");
            tableModel.setData(new java.util.ArrayList<Booking>());
            return;
        }

        String email = manager.getCurrentUserEmail();
        java.util.List<Booking> mine = new java.util.ArrayList<Booking>();
        for (Booking b : manager.getBookingsAsList()) {
            if (email != null && email.equalsIgnoreCase(b.getEmail())) {
                mine.add(b);
            }
        }
        statusLabel.setText("Bookings for " + manager.getCurrentUserName());
        loginBtn.setText("Change user");
        tableModel.setData(mine);
    }

    private class BookingTableModel extends AbstractTableModel {
        private java.util.List<Booking> data = new java.util.ArrayList<Booking>();
        private final String[] cols = {"Booking ID", "Event", "Seats", "Amount", "Status", "Cancel", "Receipt"};

        public void setData(java.util.List<Booking> list) {
            data = list;
            fireTableDataChanged();
        }

        public Booking getBookingAt(int row) {
            return data.get(row);
        }

        @Override
        public int getRowCount() { return data.size(); }
        @Override
        public int getColumnCount() { return cols.length; }
        @Override
        public String getColumnName(int col) { return cols[col]; }
        @Override
        public Object getValueAt(int rowIndex, int columnIndex) {
            Booking b = data.get(rowIndex);
            switch (columnIndex) {
                case 0: return b.getBookingId();
                case 1: return b.getEventName();
                case 2: return b.getQuantity();
                case 3: return b.getAmount();
                case 4: return b.getStatus();
                case 5: return "Cancel";
                case 6: return "Download";
                default: return "";
            }
        }

        @Override
        public boolean isCellEditable(int rowIndex, int columnIndex) {
            return columnIndex >= 5;
        }
    }

    private class StripeRenderer extends DefaultTableCellRenderer {
        @Override
        public Component getTableCellRendererComponent(JTable table, Object value, boolean isSelected,
                boolean hasFocus, int row, int column) {
            Component c = super.getTableCellRendererComponent(table, value, isSelected, hasFocus, row, column);
            if (!isSelected) {
                c.setBackground(row % 2 == 0 ? Color.WHITE : new Color(0xf9fafb));
            }
            return c;
        }
    }

    private class ButtonRenderer extends JButton implements TableCellRenderer {
        public ButtonRenderer(String text) {
            setText(text);
            setFocusPainted(false);
            setBackground(UITheme.INDIGO);
            setForeground(Color.WHITE);
        }

        @Override
        public Component getTableCellRendererComponent(JTable table, Object value, boolean isSelected,
                boolean hasFocus, int row, int column) {
            return this;
        }
    }

    private class ButtonEditor extends AbstractCellEditor implements TableCellEditor {
        private final JButton button = new JButton();
        private int row;

        public ButtonEditor(String text) {
            button.setText(text);
            button.setFocusPainted(false);
            button.setBackground(UITheme.INDIGO);
            button.setForeground(Color.WHITE);
            button.addActionListener(e -> handleAction());
        }

        private void handleAction() {
            Booking booking = tableModel.getBookingAt(row);
            if ("Cancel".equals(button.getText())) {
                try {
                    manager.cancel(booking.getBookingId());
                    JOptionPane.showMessageDialog(MyBookingsPanel.this, "Booking cancelled.");
                } catch (BookingNotFoundException ex) {
                    JOptionPane.showMessageDialog(MyBookingsPanel.this, ex.getMessage());
                } finally {
                    refreshTable();
                    listener.onChange();
                }
            } else {
                ReceiptGenerator.generateReceipt(booking);
                JOptionPane.showMessageDialog(MyBookingsPanel.this, "Receipt saved in receipts folder.");
            }
            fireEditingStopped();
        }

        @Override
        public Component getTableCellEditorComponent(JTable table, Object value, boolean isSelected, int row, int column) {
            this.row = row;
            return button;
        }

        @Override
        public Object getCellEditorValue() { return button.getText(); }
    }
}
