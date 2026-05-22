/*
 * Concepts: BlockingQueue, Multithreading
 */
package service;

import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;

import model.BookingRequest;

public class BookingQueue {
    private final BlockingQueue<BookingRequest> queue = new LinkedBlockingQueue<BookingRequest>();

    public void enqueue(BookingRequest request) throws InterruptedException {
        // BlockingQueue provides thread-safe producer/consumer behavior
        queue.put(request);
    }

    public BookingRequest dequeue() throws InterruptedException {
        // Blocking call to safely consume requests from multiple threads
        return queue.take();
    }

    public void remove(BookingRequest request) {
        queue.remove(request);
    }

    public int size() {
        return queue.size();
    }
}
