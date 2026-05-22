/*
 * Concepts: Swing UI, Multithreading, ExecutorService Simulation
 */
package ui;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;

import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;
import javax.swing.SwingUtilities;

import service.BookingManager;
import service.SimulationListener;
import service.ThreadLogger;

public class ThreadMonitorPanel extends JPanel implements ThreadLogger {
    private final BookingManager manager;
    private final JTextArea logArea = new JTextArea();

    public ThreadMonitorPanel(BookingManager manager) {
        this.manager = manager;
        setLayout(new BorderLayout());
        setBackground(UITheme.BG);
        setBorder(BorderFactory.createEmptyBorder(16, 16, 16, 16));

        logArea.setEditable(false);
        logArea.setFont(UITheme.MONO);
        logArea.setBackground(Color.WHITE);
        logArea.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));

        JScrollPane scroll = new JScrollPane(logArea);
        scroll.setBorder(BorderFactory.createLineBorder(UITheme.BORDER));
        scroll.setPreferredSize(new Dimension(800, 500));

        JButton simulate = new JButton("Simulate 10 users");
        simulate.setBackground(UITheme.INDIGO);
        simulate.setForeground(Color.WHITE);
        simulate.setFocusPainted(false);
        simulate.setBorder(BorderFactory.createEmptyBorder(8, 12, 8, 12));
        simulate.addActionListener(e -> runSimulation());

        add(simulate, BorderLayout.NORTH);
        add(scroll, BorderLayout.CENTER);
    }

    private void runSimulation() {
        log("Starting simulation with 10 users...");
        manager.simulateConcurrentBookings(10, new SimulationListener() {
            @Override
            public void onComplete(int successCount, int seatUnavailableCount) {
                log("Simulation result: success=" + successCount + ", seatUnavailable=" + seatUnavailableCount);
            }
        });
    }

    @Override
    public void log(final String message) {
        SwingUtilities.invokeLater(new Runnable() {
            @Override
            public void run() {
                logArea.append(message + "\n");
                logArea.setCaretPosition(logArea.getDocument().getLength());
            }
        });
    }
}
