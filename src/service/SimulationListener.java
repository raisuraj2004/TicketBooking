/*
 * Concepts: Callback Interface for Concurrency Simulation
 */
package service;

public interface SimulationListener {
    void onComplete(int successCount, int seatUnavailableCount);
}
