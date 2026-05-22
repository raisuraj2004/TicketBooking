/*
 * Concepts: Swing UI, BorderLayout, CardLayout
 */
package ui;

import java.awt.BorderLayout;
import java.awt.CardLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.FlowLayout;

import javax.swing.BorderFactory;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JTextField;
import javax.swing.JButton;
import javax.swing.SwingUtilities;

import service.BookingManager;

public class MainFrame extends JFrame {
    private final CardLayout cardLayout = new CardLayout();
    private final JPanel contentPanel = new JPanel(cardLayout);

    private final HomePanel homePanel;
    private final BookingPanel bookingPanel;
    private final MyBookingsPanel myBookingsPanel;
    private final AdminPanel adminPanel;
    private final LoginPanel loginPanel;

    private JLabel profileLabel;
    private JButton authButton;
    private JTextField searchField;
    private final String searchPlaceholder = "Search movies, concerts, sports...";

    public MainFrame(BookingManager manager) {
        super("FunSeats");
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setMinimumSize(new Dimension(1100, 700));
        setLayout(new BorderLayout());
        getContentPane().setBackground(UITheme.BG);

        final Sidebar[] sidebarRef = new Sidebar[1];
        Sidebar sidebar = new Sidebar(new Sidebar.NavListener() {
            @Override
            public void onNav(String key) {
                if ("ADMIN".equals(key) && !manager.isAdminLoggedIn()) {
                    javax.swing.JPasswordField pf = new javax.swing.JPasswordField();
                    int ok = javax.swing.JOptionPane.showConfirmDialog(
                            MainFrame.this,
                            pf,
                            "Admin Login",
                            javax.swing.JOptionPane.OK_CANCEL_OPTION,
                            javax.swing.JOptionPane.PLAIN_MESSAGE
                    );
                    if (ok == javax.swing.JOptionPane.OK_OPTION) {
                        String pass = new String(pf.getPassword());
                        if (!manager.loginAdmin(pass)) {
                            javax.swing.JOptionPane.showMessageDialog(MainFrame.this, "Invalid admin password");
                            return;
                        }
                    } else {
                        return;
                    }
                }
                cardLayout.show(contentPanel, key);
                if (sidebarRef[0] != null) {
                    sidebarRef[0].setActive(key);
                }
                if ("MY_BOOKINGS".equals(key)) {
                    myBookingsPanel.refreshTable();
                }
                if ("ADMIN".equals(key)) {
                    adminPanel.refreshStats();
                }
                if ("HOME".equals(key)) {
                    homePanel.refreshCards();
                }
            }
        });
        sidebarRef[0] = sidebar;

        homePanel = new HomePanel(manager, new HomePanel.BookNowListener() {
            @Override
            public void onBookNow(model.Bookable event) {
                bookingPanel.setSelectedEvent(event);
                cardLayout.show(contentPanel, "BOOKING");
                sidebar.setActive("BOOKING");
            }

            @Override
            public void onImageClick(model.Bookable event) {
                if (manager.isLoggedIn()) {
                    bookingPanel.setSelectedEvent(event);
                    cardLayout.show(contentPanel, "BOOKING");
                    sidebar.setActive("BOOKING");
                } else {
                    cardLayout.show(contentPanel, "LOGIN");
                    sidebar.setActive("LOGIN");
                }
            }
        });
        bookingPanel = new BookingPanel(manager, new BookingPanel.BookingListener() {
            @Override
            public void onBookingCompleted() {
                myBookingsPanel.refreshTable();
                adminPanel.refreshStats();
                homePanel.refreshCards();
            }

            @Override
            public void onLoginRequired() {
                cardLayout.show(contentPanel, "LOGIN");
                sidebar.setActive("LOGIN");
            }
        });
        myBookingsPanel = new MyBookingsPanel(manager, new MyBookingsPanel.BookingActionListener() {
            @Override
            public void onChange() {
                adminPanel.refreshStats();
                homePanel.refreshCards();
            }

            @Override
            public void onLoginRequested() {
                cardLayout.show(contentPanel, "LOGIN");
                sidebar.setActive("LOGIN");
            }
        });
        adminPanel = new AdminPanel(manager, new AdminPanel.EventAddedListener() {
            @Override
            public void onEventAdded() {
                homePanel.refreshCards();
                bookingPanel.refreshEvents();
            }
        });
        loginPanel = new LoginPanel(manager, new LoginPanel.LoginListener() {
            @Override
            public void onLoginSuccess() {
                updateProfile(manager);
                bookingPanel.syncUser();
                myBookingsPanel.refreshTable();
                cardLayout.show(contentPanel, "HOME");
                sidebar.setActive("HOME");
            }
        });

        manager.setThreadLogger(null);
        manager.addObserver(homePanel);

        contentPanel.setBackground(UITheme.BG);
        contentPanel.add(homePanel, "HOME");
        contentPanel.add(bookingPanel, "BOOKING");
        contentPanel.add(myBookingsPanel, "MY_BOOKINGS");
        contentPanel.add(adminPanel, "ADMIN");
        contentPanel.add(loginPanel, "LOGIN");

        JPanel mainArea = new JPanel(new BorderLayout());
        mainArea.setBackground(UITheme.BG);
        mainArea.add(buildTopBar(manager), BorderLayout.NORTH);
        mainArea.add(contentPanel, BorderLayout.CENTER);

        add(sidebar, BorderLayout.WEST);
        add(mainArea, BorderLayout.CENTER);

        SwingUtilities.invokeLater(new Runnable() {
            @Override
            public void run() {
                cardLayout.show(contentPanel, "HOME");
                sidebar.setActive("HOME");
                updateProfile(manager);
            }
        });
    }

    private JPanel buildTopBar(BookingManager manager) {
        JPanel bar = new JPanel(new BorderLayout());
        bar.setBackground(Color.WHITE);
        bar.setBorder(BorderFactory.createMatteBorder(0, 0, 1, 0, UITheme.BORDER));
        bar.setPreferredSize(new Dimension(0, 60));

        JLabel title = new JLabel("FunSeats");
        title.setFont(UITheme.SUBTITLE);
        title.setBorder(BorderFactory.createEmptyBorder(0, 16, 0, 16));

        searchField = new JTextField(searchPlaceholder);
        searchField.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(UITheme.BORDER),
                BorderFactory.createEmptyBorder(6, 10, 6, 10)));
        searchField.addActionListener(e -> doSearch());
        searchField.addFocusListener(new java.awt.event.FocusAdapter() {
            @Override
            public void focusGained(java.awt.event.FocusEvent e) {
                if (searchField.getText().equals(searchPlaceholder)) {
                    searchField.setText("");
                }
            }
            @Override
            public void focusLost(java.awt.event.FocusEvent e) {
                if (searchField.getText().trim().isEmpty()) {
                    searchField.setText(searchPlaceholder);
                }
            }
        });

        JPanel right = new JPanel(new FlowLayout(FlowLayout.RIGHT, 12, 12));
        right.setBackground(Color.WHITE);
        profileLabel = new JLabel(manager.isLoggedIn() ? manager.getCurrentUserName() : "Guest");
        profileLabel.setFont(UITheme.BODY);
        JButton searchBtn = new JButton("Search");
        searchBtn.setBackground(UITheme.INDIGO);
        searchBtn.setForeground(Color.WHITE);
        searchBtn.setFocusPainted(false);
        searchBtn.setBorder(BorderFactory.createEmptyBorder(6, 12, 6, 12));
        searchBtn.addActionListener(e -> doSearch());
        authButton = new JButton(manager.isLoggedIn() ? "Logout" : "Sign In");
        authButton.setBackground(UITheme.INDIGO);
        authButton.setForeground(Color.WHITE);
        authButton.setFocusPainted(false);
        authButton.setBorder(BorderFactory.createEmptyBorder(6, 12, 6, 12));
        authButton.addActionListener(e -> {
            if (manager.isLoggedIn()) {
                manager.logout();
                updateProfile(manager);
                bookingPanel.syncUser();
                myBookingsPanel.refreshTable();
            } else {
                cardLayout.show(contentPanel, "LOGIN");
            }
        });
        right.add(profileLabel);
        right.add(searchBtn);
        right.add(authButton);

        bar.add(title, BorderLayout.WEST);
        bar.add(searchField, BorderLayout.CENTER);
        bar.add(right, BorderLayout.EAST);
        return bar;
    }

    private void updateProfile(BookingManager manager) {
        if (profileLabel != null) {
            profileLabel.setText(manager.isLoggedIn() ? manager.getCurrentUserName() : "Guest");
        }
        if (authButton != null) {
            authButton.setText(manager.isLoggedIn() ? "Logout" : "Sign In");
        }
    }

    private void doSearch() {
        if (searchField == null) return;
        String q = searchField.getText() == null ? "" : searchField.getText().trim();
        if (q.equalsIgnoreCase(searchPlaceholder)) q = "";
        boolean found = homePanel.applySearch(q);
        if (!found) {
            javax.swing.JOptionPane.showMessageDialog(this, "Event not found");
        }
        cardLayout.show(contentPanel, "HOME");
    }
}
