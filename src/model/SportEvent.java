package model;

public class SportEvent extends Venue {
    public SportEvent(String id, String name, String date, String venueName, double price, int totalSeats) {
        super(id, name, date, venueName, price, totalSeats);
    }

    @Override
    public int getCapacity() {
        return getTotalSeats();
    }
}
