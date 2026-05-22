/*
 * Concepts: Custom Exception
 */
package exception;

public class SeatUnavailableException extends Exception {
    public SeatUnavailableException(String message) { super(message); }
    public SeatUnavailableException(String message, Throwable cause) { super(message, cause); }
}
