/*
 * Concepts: Swing UI, Encapsulation
 */
package ui;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;

import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JTextField;
import javax.swing.JOptionPane;

import service.BookingManager;

public class LoginPanel extends JPanel {
    public interface LoginListener {
        void onLoginSuccess();
    }

    private final BookingManager manager;
    private final LoginListener listener;

    private final JTextField nameField = new JTextField(18);
    private final JTextField emailField = new JTextField(18);

    public LoginPanel(BookingManager manager, LoginListener listener) {
        this.manager = manager;
        this.listener = listener;
        setLayout(new BorderLayout());
        setBackground(UITheme.BG);
        setBorder(BorderFactory.createEmptyBorder(24, 24, 24, 24));

        JPanel card = new JPanel(new GridBagLayout());
        card.setBackground(Color.WHITE);
        card.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(UITheme.BORDER),
                BorderFactory.createEmptyBorder(20, 20, 20, 20)));

        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(8, 8, 8, 8);
        gbc.fill = GridBagConstraints.HORIZONTAL;

        JLabel title = new JLabel("Login to Continue");
        title.setFont(UITheme.TITLE);
        gbc.gridx = 0; gbc.gridy = 0; gbc.gridwidth = 2;
        card.add(title, gbc);
        gbc.gridwidth = 1;

        gbc.gridx = 0; gbc.gridy = 1; card.add(label("Name"), gbc);
        gbc.gridx = 1; gbc.gridy = 1; card.add(nameField, gbc);

        gbc.gridx = 0; gbc.gridy = 2; card.add(label("Email"), gbc);
        gbc.gridx = 1; gbc.gridy = 2; card.add(emailField, gbc);

        JButton login = new JButton("Login");
        login.setBackground(UITheme.INDIGO);
        login.setForeground(Color.WHITE);
        login.setFocusPainted(false);
        login.setBorder(BorderFactory.createEmptyBorder(8, 12, 8, 12));
        gbc.gridx = 1; gbc.gridy = 3; card.add(login, gbc);

        login.addActionListener(e -> doLogin());

        add(card, BorderLayout.NORTH);
    }

    private JLabel label(String text) {
        JLabel lbl = new JLabel(text);
        lbl.setFont(UITheme.BODY);
        lbl.setForeground(UITheme.MUTED);
        return lbl;
    }

    private void doLogin() {
        String name = nameField.getText().trim();
        String email = emailField.getText().trim();
        if (name.isEmpty() || email.isEmpty()) {
            JOptionPane.showMessageDialog(this, "Please enter name and email.");
            return;
        }
        manager.login(name, email);
        JOptionPane.showMessageDialog(this, "Welcome, " + name + "!");
        if (listener != null) {
            listener.onLoginSuccess();
        }
    }
}
