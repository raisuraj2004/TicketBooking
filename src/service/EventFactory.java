/*
 * Concepts: Factory Pattern
 */
package service;

import model.Bookable;
import model.Concert;
import model.Movie;
import model.SportEvent;

public class EventFactory {
    public static Bookable createEvent(String type, String id, String name, String date, String venue, double price, int seats) {
        String t = type == null ? "" : type.trim().toLowerCase();
        if ("concert".equals(t)) {
            return new Concert(id, name, date, venue, price, seats);
        }
        if ("sport".equals(t) || "sports".equals(t) || "sportevent".equals(t)) {
            return new SportEvent(id, name, date, venue, price, seats);
        }
        return new Movie(id, name, date, venue, price, seats);
    }
}
