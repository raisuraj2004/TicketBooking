
package model;

public class BookingResult<T> {
    private boolean success;
    private String message;
    private T data;
    private Exception exception;

    public BookingResult(boolean success, String message, T data, Exception exception) {
        this.success = success;
        this.message = message;
        this.data = data;
        this.exception = exception;
    }

    public boolean isSuccess() { return success; }
    public void setSuccess(boolean success) { this.success = success; }
    public String getMessage() { return message; }
    public void setMessage(String message) { this.message = message; }
    public T getData() { return data; }
    public void setData(T data) { this.data = data; }
    public Exception getException() { return exception; }
    public void setException(Exception exception) { this.exception = exception; }
}
