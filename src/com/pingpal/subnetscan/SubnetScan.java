package com.pingpal.subnetscan;

import static java.awt.EventQueue.invokeLater;
import java.io.IOException;
import java.net.InetAddress;
import java.net.UnknownHostException;
import java.util.ArrayList;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;
import javax.swing.JProgressBar;
import javax.swing.JTable;
import javax.swing.table.DefaultTableModel;

/**
 * The {@code SubnetScan} class performs a scan on a given subnet range and
 * identifies reachable IP addresses by attempting to ping them.
 * <p>
 * The class uses a thread pool to concurrently scan multiple IPs. It also
 * updates a {@code JTable} with the scan results and a {@code JProgressBar} to
 * indicate progress.
 * </p>
 * <p>
 * The network range is expected to be in the form "x.x.x.x/n", where n is the
 * number of network bits. The total number of IP addresses to scan is derived
 * from this value.
 * </p>
 *
 * @author Igor Karbowy
 */
public class SubnetScan {

    // List of subnet scan results. Each result represents a reachable IP.
    private ArrayList<SubnetScanResult> subnetScanResults = new ArrayList<>();

    // The network range to scan (e.g., "192.168.0.0/24").
    private final String NETWORK_RANGE;

    // Timeout in milliseconds for checking if an IP is reachable.
    private final int TIMEOUT;

    // Total number of IPs to scan (calculated from the network range).
    private int numOfIPs;

    // Table model that will be updated with the reachable IP addresses.
    private DefaultTableModel model;

    // Progress bar to display the scanning progress.
    private JProgressBar prgSubnetScan;

    // Number of threads used for scanning.
    // It is set to the number of available processors of the machine multiplied by 32.
    private final int THREAD_COUNT = Runtime.getRuntime().availableProcessors() * 32;

    // Executor service for running scan tasks concurrently.
    private ExecutorService executorService = Executors.newFixedThreadPool(THREAD_COUNT);

    // Flag to indicate if a stop has been requested for the scan.
    private boolean stopRequested = false;

    /**
     * Constructs a new {@code SubnetScan} instance with the specified network
     * range, timeout, table for results, and progress bar.
     *
     * @param networkRange the network range to scan (format "x.x.x.x/n")
     * @param timeout the timeout (in milliseconds) to use for reachability
     * checks
     * @param tbl the {@code JTable} whose model will be updated with scan
     * results
     * @param prgSubnetScan the {@code JProgressBar} used to show scan progress
     */
    public SubnetScan(String networkRange, int timeout, JTable tbl, JProgressBar prgSubnetScan) {
        this.NETWORK_RANGE = networkRange;
        this.TIMEOUT = timeout;
        // Retrieve the model from the passed JTable.
        this.model = (DefaultTableModel) tbl.getModel();
        this.prgSubnetScan = prgSubnetScan;

        // Calculate the number of IP addresses based on the network range.
        setNumOfIPs();
    }

    /**
     * Starts the subnet scan.
     * <p>
     * Clears the table and progress bar, then concurrently scans all IPs in the
     * subnet. Uses an ExecutorService to run scan tasks concurrently and
     * updates the progress bar.
     * </p>
     * <p>
     * The method executes until either all tasks complete, the timeout is
     * reached, or a stop is requested.
     * </p>
     */
    public void start() {
        // Clear current data in the table on the EDT.
        invokeLater(() -> model.setRowCount(0));
        // Reset the progress bar on the EDT.
        invokeLater(() -> prgSubnetScan.setValue(0));

        // Counter variable for IPs to scan.
        AtomicInteger ips = new AtomicInteger(0);
        // Counter variable for already scanned IPs.
        AtomicInteger scannedIps = new AtomicInteger(0);

        // Loop through each IP index in the range.
        while (ips.get() <= numOfIPs) {
            // Generate the IP to scan.
            String ip = generateIP(ips.getAndIncrement());

            // Submit a task to the executor to scan the IP.
            executorService.submit(() -> {
                InetAddress inAddress;
                try {
                    // Resolve the IP address.
                    inAddress = InetAddress.getByName(ip);
                    // Scan the IP.
                    scanIP(inAddress);

                    // Update the progress bar after scanning each IP.
                    updateProgressBar(scannedIps.getAndIncrement());
                } catch (UnknownHostException e) {
                    // If IP is unknown, ignore and continue.
                }
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
     * Calculates and sets the number of IP addresses to scan ({@code numOfIps}
     * variable) based on the network bits provided in the network range.
     * <p>
     * For example, for a /24 network, this computes 2^(32-24) - 1.
     * </p>
     */
    private void setNumOfIPs() {
        // Extract the network bits from the network range string (e.g., "24" from "192.168.0.0/24")
        int networkBits = Integer.parseInt(NETWORK_RANGE.substring(NETWORK_RANGE.indexOf("/") + 1));

        // Calculate the number of IPs: 2^(32 - networkBits) - 1 (subtracting network address)
        numOfIPs = (int) Math.pow(2, (32 - networkBits)) - 1;
    }

    /**
     * Generates an IP address from the subnet starting IP based on the given
     * index.
     *
     * @param ipNum the sequential number of the IP address to generate
     * @return the generated IP address as a {@code String} (e.g.,
     * "192.168.0.15")
     */
    private String generateIP(int ipNum) {
        // Split the network address portion (before the '/') into its segments.
        String[] segments = NETWORK_RANGE.substring(0, NETWORK_RANGE.indexOf("/")).split("\\.");

        // Convert the IP segments to a single long value.
        long ipValue = 0;
        for (String segment : segments) {
            ipValue = (ipValue << 8) | Integer.parseInt(segment);
        }

        // Add the sequential IP number to the base IP
        long newIpValue = ipValue + ipNum;
        long mod = 1L << 32; // Total number of IPv4 addresses
        newIpValue = newIpValue % mod;
        if (newIpValue < 0) {
            newIpValue += mod;
        }

        // Extract each segment of the new IP address.
        int newSeg1 = (int) ((newIpValue >> 24) & 0xFF);
        int newSeg2 = (int) ((newIpValue >> 16) & 0xFF);
        int newSeg3 = (int) ((newIpValue >> 8) & 0xFF);
        int newSeg4 = (int) (newIpValue & 0xFF);

        // Combine the segments and return the generated IP address as a String.
        return newSeg1 + "." + newSeg2 + "." + newSeg3 + "." + newSeg4;
    }

    /**
     * Attempts to ping the single given InetAddress. If reachable, adds the IP
     * address to the results list and updates the table model.
     *
     * @param inAddress the InetAddress representing the IP to scan
     */
    private void scanIP(InetAddress inAddress) {
        try {
            // If the IP is reachable within the given timeout, consider it active.
            if (inAddress.isReachable(TIMEOUT)) {
                // Add result to the subnet scan results list.
                subnetScanResults.add(new SubnetScanResult(inAddress.getHostAddress()));
                // Update the table model on the EDT using invokeLater.
                invokeLater(() -> model.addRow(new Object[]{inAddress.getHostAddress()}));
            }
        } catch (IOException e) {
            // If an exception occurs (e.g., timeout), consider it inactive.
        }
    }

    /**
     * Updates the progress bar based on the number of IPs scanned.
     *
     * @param ipNum the number of IPs scanned so far
     */
    private void updateProgressBar(int ipNum) {
        // Calculate the percentage of scanned IPs, and update the progress bar on the EDT.
        invokeLater(() -> prgSubnetScan.setValue((int) Math.round(((double) ipNum / numOfIPs) * 100)));
    }

    /**
     * Retrieves the list of subnet scan results.
     *
     * @return an {@code ArrayList} of {@code SubnetScanResult} objects
     */
    public ArrayList<SubnetScanResult> getSubnetScanResults() {
        return subnetScanResults;
    }

    /**
     * Sets the subnet scan results.
     * <p>
     * Used when importing results, i.e. when no subnet scan was performed to
     * have added the found devices to the subnet scan results list.
     * </p>
     *
     * @param subnetScanResults an {@code ArrayList} of {@code SubnetScanResult}
     * objects
     */
    public void setSubnetScanResults(ArrayList<SubnetScanResult> subnetScanResults) {
        this.subnetScanResults = subnetScanResults;
    }

    /**
     * Returns the network range that was scanned.
     *
     * @return the network range in string format (e.g., "192.168.0.0/24") as a
     * {@code String}
     */
    public String getNetworkRange() {
        return NETWORK_RANGE;
    }

    /**
     * Returns the timeout used for each IP scan.
     *
     * @return the timeout in milliseconds as an {@code int}
     */
    public int getTimeout() {
        return TIMEOUT;
    }

    /**
     * Forcefully shuts down the executor service, stopping any running tasks.
     */
    public void shutDownExecutorService() {
        executorService.shutdownNow();
    }

    /**
     * Requests the subnet scan to stop.
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
     * otherwise.320
     */
    public boolean isStopRequested() {
        return stopRequested;
    }
}
