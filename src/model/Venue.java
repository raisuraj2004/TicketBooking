package model;

import java.util.concurrent.atomic.AtomicInteger;

public abstract class Venue implements Bookable {
    private String id;
    private String name;
    private String date;
    private String venueName;
    private double price;
    private int totalSeats;
    private AtomicInteger availableSeats;

    public Venue(String id, String name, String date, String venueName, double price, int totalSeats) {
        this.id = id;
        this.name = name;
        this.date = date;
        this.venueName = venueName;
        this.price = price;
        this.totalSeats = totalSeats;
        this.availableSeats = new AtomicInteger(totalSeats);
    }

    public abstract int getCapacity();

    @Override
    public int getAvailableSeats() {
        return availableSeats.get();
    }

    public int decrementSeats(int qty) {
        return availableSeats.addAndGet(-qty);
    }

    public int incrementSeats(int qty) {
        return availableSeats.addAndGet(qty);
    }

    public String getId() { return id; }
    public void setId(String id) { this.id = id; }
    public String getName() { return name; }
    public void setName(String name) { this.name = name; }
    public String getDate() { return date; }
    public void setDate(String date) { this.date = date; }
    public String getVenueName() { return venueName; }
    public void setVenueName(String venueName) { this.venueName = venueName; }
    public double getPrice() { return price; }
    public void setPrice(double price) { this.price = price; }
    public int getTotalSeats() { return totalSeats; }
    public void setTotalSeats(int totalSeats) { this.totalSeats = totalSeats; }
    public AtomicInteger getAvailableSeatsAtomic() { return availableSeats; }
    public void setAvailableSeatsAtomic(AtomicInteger availableSeats) { this.availableSeats = availableSeats; }

    @Override
    public String toString() {
        return name + " (" + date + ")";
    }
}
