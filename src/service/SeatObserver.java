/*
 * Concepts: Observer Pattern (Interface)
 */
package service;

import model.Bookable;

public interface SeatObserver {
    void onSeatAvailable(Bookable event);
}
