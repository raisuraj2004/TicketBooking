/*
 * Concepts: File I/O, Encapsulation
 */
package util;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;

import model.Booking;

public class ReceiptGenerator {
    public static File generateReceipt(Booking booking) {
        File dir = new File("receipts");
        if (!dir.exists()) {
            dir.mkdirs();
        }
        File receipt = new File(dir, booking.getBookingId() + "_receipt.txt");
        try (BufferedWriter bw = new BufferedWriter(new FileWriter(receipt))) {
            bw.write("FunSeats - Receipt\n");
            bw.write("--------------------------\n");
            bw.write(booking.toPrintableString());
        } catch (Exception e) {
            // ignore
        }
        return receipt;
    }
}
