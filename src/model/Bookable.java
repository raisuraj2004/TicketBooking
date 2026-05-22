package model;

public interface Bookable {
    String getId();
    String getName();
    String getDate();
    String getVenueName();
    double getPrice();
    int getAvailableSeats();
}
