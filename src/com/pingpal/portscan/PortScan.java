package com.pingpal.portscan;

import static java.awt.EventQueue.invokeLater;
import java.io.IOException;
import java.net.InetSocketAddress;
import java.net.Socket;
import java.util.ArrayList;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;
import javax.swing.JProgressBar;
import javax.swing.JTable;
import javax.swing.table.DefaultTableModel;

/**
 * The {@code PortScan} class performs a TCP port scan on a specified IP
 * address.
 * <p>
 * This class scans a range of ports (from {@code bottomRangePort} to
 * {@code topRangePort}) on the target IP address. For each port, it attempts to
 * connect using a specified timeout. If the connection is successful, the port
 * is assumed to be open, and the protocol for the port is determined using the
 * {@code Protocols} class. The results are stored in an {@code ArrayList} and
 * displayed in a {@code JTable} while the progress is updated via a
 * {@code JProgressBar}.
 * </p>
 */
public class PortScan {

    /**
     * A {@code Protocols} object used to determine the protocol associated with
     * a port.
     */
    private Protocols protocols = new Protocols();

    // A list of port scan results.
    private ArrayList<PortScanResult> portScanResults = new ArrayList<>();

    // The target IP address to scan.
    private final String IP_ADDRESS;

    // The starting port number of the scan range.
    private final int BOTTOM_RANGE_PORT;

    // The ending port number of the scan range.
    private final int TOP_RANGE_PORT;

    // The timeout (in milliseconds) for attempting to connect to each port.
    private final int TIMEOUT;

    // The table model used to update the UI with the scan results.
    private DefaultTableModel model;

    // The progress bar used to display scan progress.
    private JProgressBar prgPortScan;

    // Number of threads used for scanning.
    // It is set to the number of available processors of the machine multiplied by 32.
    private final int THREAD_COUNT = Runtime.getRuntime().availableProcessors() * 32;

    // Executor service for running scan tasks concurrently.
    private ExecutorService executorService = Executors.newFixedThreadPool(THREAD_COUNT);

    // A flag indicating whether a stop has been requested.
    private boolean stopRequested = false;

    /**
     * Constructs a new {@code PortScan} instance with the specified IP address,
     * bottom range port, top range port, timeout, table for results, and
     * progress bar.
     *
     * @param ipAddress the target IP address to scan
     * @param bottomRangePort the starting port number of the scan range
     * @param topRangePort the ending port number of the scan range
     * @param timeout the connection timeout (in milliseconds) for each port
     * @param tbl the {@code JTable} whose model will be updated with scan
     * results
     * @param prgPortScan the {@code JProgressBar} that displays the scanning
     * progress
     */
    public PortScan(String ipAddress, int bottomRangePort, int topRangePort, int timeout, JTable tbl, JProgressBar prgPortScan) {
        this.IP_ADDRESS = ipAddress;
        this.BOTTOM_RANGE_PORT = bottomRangePort;
        this.TOP_RANGE_PORT = topRangePort;
        this.TIMEOUT = timeout;
        this.model = (DefaultTableModel) tbl.getModel();
        this.prgPortScan = prgPortScan;
    }

    /**
     * Attempts to scan a single port on the target IP address.
     * <p>
     * The method creates a new socket, attempts to connect to the given port
     * with the specified timeout, and then closes the socket. If the connection
     * is successful, it retrieves the associated protocol and updates the scan
     * results list and the UI table.
     * </p>
     *
     * @param port the port number to scan
     */
    public void scanPort(int port) {
        try {
            // Create new socket object.
            Socket socket = new Socket();
            // Attempt to connect within a given timeout.
            socket.connect(new InetSocketAddress(IP_ADDRESS, port), TIMEOUT);
            socket.close();

            // Retrieve protocol for the given port.
            String protocol = protocols.getProtocolForPort(port);

            // Record the scan result in the port scan results list.
            portScanResults.add(new PortScanResult(port, protocol));

            // Update UI results table on the EDT
            invokeLater(() -> model.addRow(new Object[]{port, protocol}));

        } catch (IOException e) {
            // Exception is ignored if the port is not reachable, and the port is considered closed.
        }
    }

    /**
     * Starts the port scan.
     * <p>
     * This method clears any existing data from the UI components, then
     * iterates over the port range, submitting tasks to scan each port
     * concurrently. The progress bar is updated as ports are scanned.
     * </p>
     * <p>
     * The method executes until either all tasks complete, the timeout is
     * reached, or a stop is requested.
     * </p>
     */
    public void start() {
        // Clear current data being displayed in the UI table via EDT.
        invokeLater(() -> model.setRowCount(0));

        // Reset progress bar via EDT.
        invokeLater(() -> prgPortScan.setValue(0));

        // Atomic counter to generate port numbers from bottomRangePort to topRangePort.
        AtomicInteger ports = new AtomicInteger(BOTTOM_RANGE_PORT);
        // Counter to track how many ports have been scanned so far.
        AtomicInteger scannedPorts = new AtomicInteger(0);

        // Loop through each port in the range.
        while (ports.get() <= TOP_RANGE_PORT) {
            // Generate the IP to scan.
            int port = ports.getAndIncrement();

            // Submit a task to the executor to scan the port.
            executorService.submit(() -> {
                // Scan the port
                scanPort(port);

                // Update the progress bar after scanning each port.
                updateProgressBar(scannedPorts.getAndIncrement());
            });
        }

        // Initiate shutdown and wait for tasks to finish, with a maximum wait time.
        executorService.shutdown();
        try {
            // Wait until all tasks have finished, or timeout after 10 minutes.
            if (!executorService.awaitTermination(10, TimeUnit.MINUTES)) {
                // If tasks are not finished in 10 minutes, force shutdown.
                executorService.shutdownNow();
            }
        } catch (InterruptedException e) {
            // If the thread is interrupted, reset the interrupt flag and exit the loop.
            executorService.shutdownNow();
            Thread.currentThread().interrupt();
        }
    }

    /**
     * Updates the progress bar based on the number of ports scanned.
     * <p>
     * The progress is calculated as a percentage of the total number of ports
     * scanned.
     * </p>
     *
     * @param portNum the number of ports scanned so far
     */
    public void updateProgressBar(int portNum) {
        invokeLater(() -> prgPortScan.setValue((int) Math.round(((double) portNum / (TOP_RANGE_PORT - BOTTOM_RANGE_PORT + 1)) * 100)));
    }

    /**
     * Forcefully shuts down the executor service, stopping any running tasks.
     */
    public void shutDownExecutorService() {
        executorService.shutdownNow();
    }

    /**
     * Requests the port scan to stop.
     * <p>
     * The {@code stopRequested} flag is set to true, so that ongoing tasks in
     * {@code start} may check this flag and terminate early.
     * </p>
     */
    public void requestStop() {
        stopRequested = true;
    }

    /**
     * Checks whether a stop has been requested for the scan.
     *
     * @return {@code true} if a stop has been requested; {@code false}
     * otherwise.
     */
    public boolean isStopRequested() {
        return stopRequested;
    }

    /**
     * Retrieves the list of port scan results.
     *
     * @return an {@code ArrayList} of {@code PortScanResult} objects
     */
    public ArrayList<PortScanResult> getPortScanResults() {
        return portScanResults;
    }

    /**
     * Sets the port scan results.
     * <p>
     * Used when importing results, i.e. when no port scan was performed to have
     * added the open ports to the port scan results list.
     * </p>
     *
     * @param portScanResults an {@code ArrayList} of {@code PortScanResult}
     * objects
     */
    public void setPortScanResults(ArrayList<PortScanResult> portScanResults) {
        this.portScanResults = portScanResults;
    }

    /**
     * Returns the target IP address.
     *
     * @return the IP address as a {@code String}
     */
    public String getIpAddress() {
        return IP_ADDRESS;
    }

    /**
     * Returns the starting port number of the scan range.
     *
     * @return the bottom range port as an {@code int}
     */
    public int getBottomRangePort() {
        return BOTTOM_RANGE_PORT;
    }

    /**
     * Returns the ending port number of the scan range.
     *
     * @return the top range port as an {@code int}
     */
    public int getTopRangePort() {
        return TOP_RANGE_PORT;
    }

    /**
     * Returns the timeout used for each port connection attempt.
     *
     * @return the timeout in milliseconds as an {@code int}
     */
    public int getTimeout() {
        return TIMEOUT;
    }
}
