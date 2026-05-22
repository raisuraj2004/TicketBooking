/*
 * Concepts: Inheritance, Polymorphism
 */
package model;

public class Concert extends Venue {
    public Concert(String id, String name, String date, String venueName, double price, int totalSeats) {
        super(id, name, date, venueName, price, totalSeats);
    }

    @Override
    public int getCapacity() {
        return getTotalSeats();
    }
}
