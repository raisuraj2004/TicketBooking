/*
 * Concepts: Interface
 */
package model;

public interface Cancellable {
    void cancel(String bookingId) throws exception.BookingNotFoundException;
}
