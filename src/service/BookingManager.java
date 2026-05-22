/*
 * Concepts: Singleton Pattern, ReentrantLock, AtomicInteger, BlockingQueue, Collections, Stream API, Observer Pattern
 */
package service;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.ThreadFactory;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.locks.ReentrantLock;
import java.util.stream.Collectors;

import exception.BookingNotFoundException;
import exception.PaymentFailedException;
import exception.SeatUnavailableException;
import model.Bookable;
import model.Booking;
import model.BookingRequest;
import model.BookingResult;
import model.Cancellable;
import model.Venue;

public class BookingManager implements Cancellable {
    private static BookingManager instance;

    private final Map<String, Booking> bookingRegistry = new HashMap<String, Booking>();
    private final List<Bookable> events = new ArrayList<Bookable>();
    private final LinkedList<BookingRequest> waitlist = new LinkedList<BookingRequest>();
    private final ReentrantLock seatLock = new ReentrantLock(true);
    private final AtomicInteger bookingCounter = new AtomicInteger(1000);
    private final BookingQueue bookingQueue = new BookingQueue();
    private final ExecutorService executor = Executors.newFixedThreadPool(6);
    private final List<SeatObserver> observers = new CopyOnWriteArrayList<SeatObserver>();

    private ThreadLogger threadLogger;
    private String currentUserName;
    private String currentUserEmail;
    private boolean adminLoggedIn;
    private final String adminPassword = "admin123";

    private BookingManager() {}

    public static BookingManager getInstance() {
        if (instance == null) {
            instance = new BookingManager();
        }
        return instance;
    }

    public void setThreadLogger(ThreadLogger logger) {
        this.threadLogger = logger;
    }

    public void login(String name, String email) {
        this.currentUserName = name;
        this.currentUserEmail = email;
    }

    public void logout() {
        this.currentUserName = null;
        this.currentUserEmail = null;
    }

    public boolean isLoggedIn() {
        return currentUserEmail != null && !currentUserEmail.trim().isEmpty();
    }

    public String getCurrentUserName() {
        return currentUserName;
    }

    public String getCurrentUserEmail() {
        return currentUserEmail;
    }

    public boolean isAdminLoggedIn() {
        return adminLoggedIn;
    }

    public boolean loginAdmin(String password) {
        if (password != null && password.equals(adminPassword)) {
            adminLoggedIn = true;
            return true;
        }
        return false;
    }

    public void logoutAdmin() {
        adminLoggedIn = false;
    }

    public void addObserver(SeatObserver observer) {
        observers.add(observer);
    }

    public void removeObserver(SeatObserver observer) {
        observers.remove(observer);
    }

    private void notifySeatAvailable(Bookable event) {
        for (SeatObserver observer : observers) {
            observer.onSeatAvailable(event);
        }
    }

    public void addEvent(Bookable event) {
        events.add(event);
    }

    public List<Bookable> getAllEvents() {
        return Collections.unmodifiableList(events);
    }

    public List<Bookable> getAvailableEvents() {
        // Stream API used for filtering available seats
        return events.stream().filter(e -> e.getAvailableSeats() > 0).collect(Collectors.toList());
    }

    public Map<String, Booking> getBookingRegistry() {
        return bookingRegistry;
    }

    public List<Booking> getBookingsAsList() {
        return new ArrayList<Booking>(bookingRegistry.values());
    }

    public BookingQueue getBookingQueue() {
        return bookingQueue;
    }

    public ExecutorService getExecutor() {
        return executor;
    }

    public BookingResult<Booking> book(BookingRequest request) {
        BookingResult<Booking> result;
        try {
            // BlockingQueue usage to accept booking requests safely
            bookingQueue.enqueue(request);

            // ReentrantLock for seat locking (not synchronized)
            seatLock.lock();
            log("Lock acquired by " + Thread.currentThread().getName());

            try {
                Bookable event = request.getEvent();
                int qty = request.getQuantity();

                if (event.getAvailableSeats() < qty) {
                    waitlist.add(request); // LinkedList waitlist
                    throw new SeatUnavailableException("Not enough seats for " + event.getName(),
                            new IllegalStateException("Inventory check failed"));
                }

                if ("Card".equalsIgnoreCase(request.getPaymentMethod()) && new Random().nextInt(20) == 0) {
                    throw new PaymentFailedException("Payment declined by gateway",
                            new RuntimeException("Gateway timeout"));
                }

                if (event instanceof Venue) {
                    // AtomicInteger update inside Venue
                    ((Venue) event).decrementSeats(qty);
                }

                String bookingId = "B" + bookingCounter.incrementAndGet();
                double amount = calculateAmount(event.getPrice(), request.getSeatCategory(), qty);
                Booking booking = new Booking(bookingId, request.getUserName(), request.getEmail(),
                        event.getName(), event.getId(), request.getSeatCategory(), qty, amount, "CONFIRMED");

                bookingRegistry.put(bookingId, booking); // HashMap registry
                result = new BookingResult<Booking>(true, "Booking confirmed", booking, null);
            } finally {
                seatLock.unlock();
                log("Lock released by " + Thread.currentThread().getName());
            }
        } catch (SeatUnavailableException e) {
            log("SeatUnavailableException: " + e.getMessage());
            result = new BookingResult<Booking>(false, e.getMessage(), null, e);
        } catch (PaymentFailedException e) {
            log("PaymentFailedException: " + e.getMessage());
            result = new BookingResult<Booking>(false, e.getMessage(), null, e);
        } catch (InterruptedException e) {
            log("Interrupted: " + e.getMessage());
            Thread.currentThread().interrupt();
            result = new BookingResult<Booking>(false, "Booking interrupted", null, e);
        } catch (Exception e) {
            log("Unexpected error: " + e.getMessage());
            result = new BookingResult<Booking>(false, "Unexpected error", null, e);
        } finally {
            bookingQueue.remove(request);
            log("Booking flow finished for " + Thread.currentThread().getName());
        }
        return result;
    }

    public void submitBookingAsync(final BookingRequest request, final java.util.function.Consumer<BookingResult<Booking>> callback) {
        // ExecutorService runs booking tasks concurrently
        executor.submit(new Runnable() {
            @Override
            public void run() {
                BookingResult<Booking> result = book(request);
                if (callback != null) {
                    callback.accept(result);
                }
            }
        });
    }

    @Override
    public void cancel(String bookingId) throws BookingNotFoundException {
        Booking booking = bookingRegistry.get(bookingId);
        if (booking == null) {
            throw new BookingNotFoundException("Booking not found: " + bookingId);
        }

        seatLock.lock();
        log("Lock acquired for cancel by " + Thread.currentThread().getName());
        try {
            booking.setStatus("CANCELLED");
            Bookable event = findEventById(booking.getEventId());
            if (event instanceof Venue) {
                ((Venue) event).incrementSeats(booking.getQuantity());
                notifySeatAvailable(event);
            }
        } finally {
            seatLock.unlock();
            log("Lock released for cancel by " + Thread.currentThread().getName());
        }
    }

    public Bookable findEventById(String id) {
        for (Bookable event : events) {
            if (event.getId().equals(id)) return event;
        }
        return null;
    }

    public double calculateAmount(double basePrice, String category, int qty) {
        double multiplier = 1.0;
        if ("Gold".equalsIgnoreCase(category)) multiplier = 1.5;
        if ("Silver".equalsIgnoreCase(category)) multiplier = 1.2;
        if ("Bronze".equalsIgnoreCase(category)) multiplier = 1.0;
        return basePrice * multiplier * qty;
    }

    public void simulateConcurrentBookings(final int users, final SimulationListener listener) {
        final AtomicInteger success = new AtomicInteger(0);
        final AtomicInteger seatUnavailable = new AtomicInteger(0);
        final CountDownLatch latch = new CountDownLatch(users);

        ExecutorService simExecutor = Executors.newFixedThreadPool(users, new ThreadFactory() {
            private final AtomicInteger idx = new AtomicInteger(0);
            @Override
            public Thread newThread(Runnable r) {
                return new Thread(r, "Thread-" + idx.incrementAndGet());
            }
        });

        for (int i = 0; i < users; i++) {
            simExecutor.submit(new Runnable() {
                @Override
                public void run() {
                    try {
                        // Staggering to simulate real user timing
                        Thread.sleep(100 + new Random().nextInt(401));
                        List<Bookable> pool = getAllEvents();
                        if (pool.isEmpty()) {
                            seatUnavailable.incrementAndGet();
                            return;
                        }
                        Bookable event = pool.get(new Random().nextInt(pool.size()));
                        BookingRequest req = new BookingRequest("User" + Thread.currentThread().getName(),
                                "user" + Thread.currentThread().getName() + "@mail.com", event, "Silver", 1, "Card");
                        BookingResult<Booking> res = book(req);
                        if (res.isSuccess()) {
                            success.incrementAndGet();
                        } else if (res.getException() instanceof SeatUnavailableException) {
                            seatUnavailable.incrementAndGet();
                        } else if (res.getException() != null) {
                            log("Other failure: " + res.getException().getMessage());
                        }
                    } catch (InterruptedException e) {
                        Thread.currentThread().interrupt();
                        log("Interrupted simulation thread");
                        seatUnavailable.incrementAndGet();
                    } finally {
                        log("Simulation done for " + Thread.currentThread().getName());
                        latch.countDown();
                    }
                }
            });
        }

        new Thread(new Runnable() {
            @Override
            public void run() {
                try {
                    latch.await();
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                } finally {
                    simExecutor.shutdown();
                    if (listener != null) {
                        listener.onComplete(success.get(), seatUnavailable.get());
                    }
                }
            }
        }, "SimResultThread").start();
    }

    public double getTotalRevenue() {
        double total = 0.0;
        for (Booking b : bookingRegistry.values()) {
            if ("CONFIRMED".equalsIgnoreCase(b.getStatus())) {
                total += b.getAmount();
            }
        }
        return total;
    }

    public Map<String, Integer> getSeatsSoldPerEvent() {
        Map<String, Integer> map = new HashMap<String, Integer>();
        for (Booking b : bookingRegistry.values()) {
            if ("CONFIRMED".equalsIgnoreCase(b.getStatus())) {
                Integer cur = map.get(b.getEventName());
                map.put(b.getEventName(), (cur == null ? 0 : cur) + b.getQuantity());
            }
        }
        return map;
    }

    public void restoreBooking(Booking booking) {
        bookingRegistry.put(booking.getBookingId(), booking);
        try {
            String id = booking.getBookingId().replaceAll("[^0-9]", "");
            if (!id.isEmpty()) {
                int val = Integer.parseInt(id);
                if (val > bookingCounter.get()) {
                    bookingCounter.set(val);
                }
            }
        } catch (Exception e) {
            // ignore parsing errors
        }
    }

    private void log(String msg) {
        if (threadLogger != null) {
            threadLogger.log(msg);
        }
    }
}
