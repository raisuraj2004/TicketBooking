
package main;

import javax.swing.SwingUtilities;

import model.Bookable;
import model.Booking;
import model.Venue;
import service.BookingManager;
import service.EventFactory;
import ui.MainFrame;
import util.FileHandler;

public class Main {
    public static void main(String[] args) {
        final BookingManager manager = BookingManager.getInstance();

        // Seed events via Factory Pattern
        manager.addEvent(EventFactory.createEvent("movie", "E100", "The Silent Ocean", "2026-05-02", "IMAX Arena", 350, 80));
        manager.addEvent(EventFactory.createEvent("concert", "E101", "Neon Nights Live", "2026-05-10", "City Dome", 500, 120));
        manager.addEvent(EventFactory.createEvent("sport", "E102", "Cricket Cup Final", "2026-05-18", "National Stadium", 450, 150));
        manager.addEvent(EventFactory.createEvent("concert", "E103", "Standup Fest", "2026-05-25", "Laugh Factory", 300, 60));
        manager.addEvent(EventFactory.createEvent("movie", "E104", "Project Hail Mary", "2026-06-01", "PVR Orion", 320, 90));
        manager.addEvent(EventFactory.createEvent("movie", "E105", "The Last Voyage", "2026-06-05", "Cinepolis", 280, 70));
        manager.addEvent(EventFactory.createEvent("concert", "E106", "A.R. Rahman Live", "2026-06-08", "Phoenix Arena", 850, 200));
        manager.addEvent(EventFactory.createEvent("concert", "E107", "Standup Night Live", "2026-06-12", "Indiranagar Social", 299, 50));
        manager.addEvent(EventFactory.createEvent("sport", "E108", "Bengaluru FC vs Goa", "2026-06-15", "Kanteerava Stadium", 420, 120));
        manager.addEvent(EventFactory.createEvent("sport", "E109", "Badminton Super Series", "2026-06-18", "KBA Arena", 380, 90));

        // Load bookings from file
        for (Booking b : FileHandler.loadBookings("bookings.txt")) {
            manager.restoreBooking(b);
            Bookable event = manager.findEventById(b.getEventId());
            if (event instanceof Venue && "CONFIRMED".equalsIgnoreCase(b.getStatus())) {
                ((Venue) event).decrementSeats(b.getQuantity());
            }
        }

        Runtime.getRuntime().addShutdownHook(new Thread(new Runnable() {
            @Override
            public void run() {
                FileHandler.saveBookings("bookings.txt", manager.getBookingsAsList());
            }
        }));

        SwingUtilities.invokeLater(new Runnable() {
            @Override
            public void run() {
                MainFrame frame = new MainFrame(manager);
                frame.setVisible(true);
            }
        });
    }
}
