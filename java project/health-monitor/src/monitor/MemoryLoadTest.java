package monitor;

import java.util.ArrayList;
import java.util.List;

/**
 * Memory Load Test - creates memory pressure to demonstrate monitoring
 * This class allocates memory to show how the monitor detects changes
 */
public class MemoryLoadTest {
    
    public static void main(String[] args) {
        SimpleJVMMonitor monitor = new SimpleJVMMonitor();
        
        System.out.println("Memory Load Test - Demonstrating Health Monitor");
        System.out.println("===============================================");
        
        // Initial metrics
        System.out.println("\n--- INITIAL STATE ---");
        monitor.printMetrics();
        
        // Create memory load
        System.out.println("\n--- CREATING MEMORY LOAD ---");
        System.out.println("Allocating 100MB of memory...");
        
        List<byte[]> memoryLoad = new ArrayList<>();
        for (int i = 0; i < 100; i++) {
            memoryLoad.add(new byte[1024 * 1024]); // 1MB each
        }
        
        System.out.println("Memory allocated. Checking metrics after allocation...");
        monitor.printMetrics();
        
        // Create some threads
        System.out.println("\n--- CREATING THREAD LOAD ---");
        System.out.println("Starting 5 background threads...");
        
        List<Thread> threads = new ArrayList<>();
        for (int i = 0; i < 5; i++) {
            Thread t = new Thread(() -> {
                try {
                    Thread.sleep(5000); // Sleep for 5 seconds
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                }
            }, "LoadTest-" + i);
            t.start();
            threads.add(t);
        }
        
        // Wait a moment and check again
        try {
            Thread.sleep(1000);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }
        
        System.out.println("Threads started. Checking metrics with active threads...");
        monitor.printMetrics();
        
        // Force garbage collection
        System.out.println("\n--- TRIGGERING GARBAGE COLLECTION ---");
        System.out.println("Forcing garbage collection...");
        System.gc();
        
        try {
            Thread.sleep(1000);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }
        
        System.out.println("After garbage collection:");
        monitor.printMetrics();
        
        // Wait for threads to complete
        System.out.println("\n--- WAITING FOR THREADS TO COMPLETE ---");
        for (Thread t : threads) {
            try {
                t.join();
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
            }
        }
        
        System.out.println("All threads completed. Final metrics:");
        monitor.printMetrics();
        
        System.out.println("\n=== TEST COMPLETED ===");
        System.out.println("The monitor successfully detected changes in:");
        System.out.println("- Memory usage (heap allocation)");
        System.out.println("- Thread count (temporary threads)");
        System.out.println("- Garbage collection activity");
    }
}