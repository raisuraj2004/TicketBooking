/*
 * Concepts: Encapsulation, Interface Implementation, Collections (used in BookingManager)
 */
package model;

public class Booking implements Printable {
    private String bookingId;
    private String userName;
    private String email;
    private String eventName;
    private String eventId;
    private String seatCategory;
    private int quantity;
    private double amount;
    private String status;

    public Booking() {}

    public Booking(String bookingId, String userName, String email, String eventName, String eventId,
                   String seatCategory, int quantity, double amount, String status) {
        this.bookingId = bookingId;
        this.userName = userName;
        this.email = email;
        this.eventName = eventName;
        this.eventId = eventId;
        this.seatCategory = seatCategory;
        this.quantity = quantity;
        this.amount = amount;
        this.status = status;
    }

    public String getBookingId() { return bookingId; }
    public void setBookingId(String bookingId) { this.bookingId = bookingId; }
    public String getUserName() { return userName; }
    public void setUserName(String userName) { this.userName = userName; }
    public String getEmail() { return email; }
    public void setEmail(String email) { this.email = email; }
    public String getEventName() { return eventName; }
    public void setEventName(String eventName) { this.eventName = eventName; }
    public String getEventId() { return eventId; }
    public void setEventId(String eventId) { this.eventId = eventId; }
    public String getSeatCategory() { return seatCategory; }
    public void setSeatCategory(String seatCategory) { this.seatCategory = seatCategory; }
    public int getQuantity() { return quantity; }
    public void setQuantity(int quantity) { this.quantity = quantity; }
    public double getAmount() { return amount; }
    public void setAmount(double amount) { this.amount = amount; }
    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }

    @Override
    public String toPrintableString() {
        StringBuilder sb = new StringBuilder();
        sb.append("Booking ID: ").append(bookingId).append("\n");
        sb.append("Name: ").append(userName).append("\n");
        sb.append("Email: ").append(email).append("\n");
        sb.append("Event: ").append(eventName).append("\n");
        sb.append("Category: ").append(seatCategory).append("\n");
        sb.append("Quantity: ").append(quantity).append("\n");
        sb.append("Amount: ").append(amount).append("\n");
        sb.append("Status: ").append(status).append("\n");
        return sb.toString();
    }
}
