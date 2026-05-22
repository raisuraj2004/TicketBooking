/*
 * Concepts: File I/O, Exception Handling
 */
package util;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.util.ArrayList;
import java.util.List;

import model.Booking;

public class FileHandler {
    public static List<Booking> loadBookings(String path) {
        List<Booking> list = new ArrayList<Booking>();
        File file = new File(path);
        if (!file.exists()) return list;

        try (BufferedReader br = new BufferedReader(new FileReader(file))) {
            String line;
            while ((line = br.readLine()) != null) {
                String[] parts = line.split("\\|");
                if (parts.length < 9) continue;
                Booking b = new Booking();
                b.setBookingId(parts[0]);
                b.setUserName(parts[1]);
                b.setEmail(parts[2]);
                b.setEventName(parts[3]);
                b.setEventId(parts[4]);
                b.setSeatCategory(parts[5]);
                b.setQuantity(Integer.parseInt(parts[6]));
                b.setAmount(Double.parseDouble(parts[7]));
                b.setStatus(parts[8]);
                list.add(b);
            }
        } catch (Exception e) {
            // swallow to keep startup robust
        }
        return list;
    }

    public static void saveBookings(String path, List<Booking> bookings) {
        try (BufferedWriter bw = new BufferedWriter(new FileWriter(path))) {
            for (Booking b : bookings) {
                String line = b.getBookingId() + "|" + b.getUserName() + "|" + b.getEmail() + "|" +
                        b.getEventName() + "|" + b.getEventId() + "|" + b.getSeatCategory() + "|" +
                        b.getQuantity() + "|" + b.getAmount() + "|" + b.getStatus();
                bw.write(line);
                bw.newLine();
            }
        } catch (Exception e) {
            // ignore file errors for exit path
        }
    }
}
