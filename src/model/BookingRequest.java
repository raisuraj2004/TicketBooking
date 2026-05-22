/*
 * Concepts: Encapsulation, BlockingQueue usage in BookingQueue
 */
package model;

public class BookingRequest {
    private String userName;
    private String email;
    private Bookable event;
    private String seatCategory;
    private int quantity;
    private String paymentMethod;

    public BookingRequest(String userName, String email, Bookable event, String seatCategory, int quantity, String paymentMethod) {
        this.userName = userName;
        this.email = email;
        this.event = event;
        this.seatCategory = seatCategory;
        this.quantity = quantity;
        this.paymentMethod = paymentMethod;
    }

    public String getUserName() { return userName; }
    public void setUserName(String userName) { this.userName = userName; }
    public String getEmail() { return email; }
    public void setEmail(String email) { this.email = email; }
    public Bookable getEvent() { return event; }
    public void setEvent(Bookable event) { this.event = event; }
    public String getSeatCategory() { return seatCategory; }
    public void setSeatCategory(String seatCategory) { this.seatCategory = seatCategory; }
    public int getQuantity() { return quantity; }
    public void setQuantity(int quantity) { this.quantity = quantity; }
    public String getPaymentMethod() { return paymentMethod; }
    public void setPaymentMethod(String paymentMethod) { this.paymentMethod = paymentMethod; }
}
