package com.pingpal.deviceping;

import static java.awt.EventQueue.invokeLater;
import java.io.IOException;
import java.net.InetAddress;
import java.util.ArrayList;
import javax.swing.JTable;
import javax.swing.table.DefaultTableModel;

/**
 * The {@code DevicePing} class performs a series of ping operations against a
 * given IP address. It collects metrics such as round-trip times, whether each
 * ping was successful, and calculates the packet loss percentage.
 * <p>
 * The results are displayed in three different {@code JTable} components:
 * <ul>
 * <li>One for individual ping results (IP, round-trip time, success, packet
 * loss)</li>
 * <li>One summarizing response results (min, max, average round-trip
 * times)</li>
 * <li>One summarizing packet loss results (total pings, successful pings,
 * failed pings, packet loss percentage)</li>
 * </ul>
 * <p>
 * The class supports both a fixed number of pings or continuous pinging based
 * on the provided parameters.
 * </p>
 */
public class DevicePing {

    // List of results from each ping operation.
    private ArrayList<DevicePingResult> devicePingResults;

    // The IP address to ping.
    private final String IP_ADDRESS;

    // The interval (in milliseconds) between successive pings.
    private final int PING_INTERVAL;

    // The total number of pings to attempt if not in continuous mode.
    private final int NUM_OF_PINGS;

    // If true, the ping operations continue indefinitely.
    private final boolean CONTINUOUS_PINGING;

    // Counter for the total number of pings attempted.
    private int pingCount;

    // Counter for the number of successful pings.
    private int successfulPings;

    // Table model for displaying individual ping results.
    private DefaultTableModel devicePingTableModel;

    // Table model for displaying ping response summaries (min, max, avg).
    private DefaultTableModel devicePingResponseResultsTableModel;

    // Table model for displaying packet loss summary results.
    private DefaultTableModel devicePingPacketResultsTableModel;

    // Flag indicating if a stop has been requested.
    private boolean stopRequested = false;

    /**
     * Constructs a new {@code DevicePing} instance with the specified IP
     * address, ping interval, number of pings, continuous pinging flag, and
     * tables for results.
     *
     * @param ipAddress the target IP address to ping
     * @param pingInterval the interval in milliseconds between pings
     * @param numOfPings the number of pings to perform (if not continuous)
     * @param continuousPinging {@code true} for continuous pinging;
     * {@code false} for a fixed number of pings
     * @param tblDevicePing the JTable for displaying individual ping results
     * @param tblDevicePingResponseResults the JTable for displaying response
     * summary (min, max, average round-trip times)
     * @param tblDevicePingPacketResults the JTable for displaying packet loss
     * summary (total, successful, failed, packet loss)
     */
    public DevicePing(String ipAddress, int pingInterval, int numOfPings, boolean continuousPinging, JTable tblDevicePing, JTable tblDevicePingResponseResults, JTable tblDevicePingPacketResults) {
        devicePingResults = new ArrayList<>();
        this.IP_ADDRESS = ipAddress;
        this.PING_INTERVAL = pingInterval;
        this.NUM_OF_PINGS = numOfPings;
        this.CONTINUOUS_PINGING = continuousPinging;

        // Initialize counters
        this.pingCount = 0;
        this.successfulPings = 0;

        // Retrieve and store table models from the passed JTables.
        this.devicePingTableModel = (DefaultTableModel) tblDevicePing.getModel();
        this.devicePingResponseResultsTableModel = (DefaultTableModel) tblDevicePingResponseResults.getModel();
        this.devicePingPacketResultsTableModel = (DefaultTableModel) tblDevicePingPacketResults.getModel();
    }

    /**
     * Starts the pinging process. This method clears the UI tables, then
     * repeatedly pings the target IP address according to the ping interval,
     * until the specified number of pings has been reached (or indefinitely if
     * continuous). After pinging is complete, it populates summary result
     * tables.
     * <p>
     * Note: This method runs on the calling thread. It is expected that you run
     * it in a background thread to avoid blocking the UI.
     * </p>
     */
    public void start() {
        // Clear current data in the result tables on the EDT
        invokeLater(() -> {
            devicePingTableModel.setRowCount(0);
            devicePingResponseResultsTableModel.setRowCount(0);
            devicePingPacketResultsTableModel.setRowCount(0);
        });

        // Calculate the time (in nanoseconds) when the next ping should be performed.
        double nextPingTime = System.nanoTime() + (PING_INTERVAL * 1_000_000);

        // Loop until the program reaches the specified number of pings or continuous mode, and no stop has been requested.
        while ((pingCount < NUM_OF_PINGS || CONTINUOUS_PINGING) && !stopRequested) {
            // Call the ping IP method
            pingIP();

            try {
                // Calculate remaining time (in milliseconds) before the next ping.
                double remainingTime = (nextPingTime - System.nanoTime()) / 1_000_000;

                // If pinging the IP took longer than the ping interval, reset the remaining time to 0 to ensure no negative time.
                if (remainingTime < 0) {
                    remainingTime = 0;
                }

                // Sleep until it's time for the next ping.
                Thread.sleep((long) remainingTime);

                // Update the time when the next ping should be performed.
                nextPingTime += (PING_INTERVAL * 1_000_000);

            } catch (InterruptedException e) {
                // If the thread is interrupted, reset the interrupt flag and exit the loop.
                Thread.currentThread().interrupt();
            }
        }

        // After pinging, update the UI with the summary results.
        populateResultsTables();
    }

    /**
     * Performs a single ping to the target IP address.
     * <p>
     * This method attempts to ping the specified IP address using
     * {@code InetAddress.isReachable()}. If the IP is reachable, it records the
     * round-trip time and considers the ping successful. If not, it records a
     * failed ping with a round-trip time equal to the ping interval. The result
     * is stored in the {@code devicePingResults} list, and the UI table is
     * updated.
     * </p>
     */
    private void pingIP() {
        try {
            // Resolve the IP address.
            InetAddress inAddress = InetAddress.getByName(IP_ADDRESS);

            // Record the time before pinging.
            long timeBeforePing = System.nanoTime();

            // Check if the IP is reachable within the ping interval.  
            if (inAddress.isReachable(PING_INTERVAL)) {
                // Calculate round-trip time in milliseconds.
                int roundTripTime = (int) (System.nanoTime() - timeBeforePing) / 1_000_000;

                // Clamp roundTripTime to the pingInterval if necessary.
                roundTripTime = Math.min(roundTripTime, PING_INTERVAL);

                // Update counter variables
                pingCount++;
                successfulPings++;

                // Add a new successful ping result.
                devicePingResults.add(new DevicePingResult(roundTripTime, true, getPacketLoss()));

                // Update the UI table with the successful ping result.
                invokeLater(() -> {
                    devicePingTableModel.addRow(new Object[]{
                        IP_ADDRESS,
                        devicePingResults.getLast().getRoundTripTime(),
                        true,
                        devicePingResults.getLast().getPacketLoss()
                    });
                });

                // Ping failed.
            } else {
                // Update counter variables.
                pingCount++;

                // Add a new unsuccessful ping result.
                devicePingResults.add(new DevicePingResult(PING_INTERVAL, false, getPacketLoss()));

                // Update the UI table with the failed ping result.
                invokeLater(() -> {
                    devicePingTableModel.addRow(new Object[]{
                        IP_ADDRESS,
                        PING_INTERVAL,
                        false,
                        devicePingResults.getLast().getPacketLoss()
                    });
                });

            }
        } catch (IOException e) {
            // In case of an I/O error, the exception is ignored as it does not occurr given the IP address is guaranteed to be in correct format.
        }
    }

    /**
     * Populates the summary result tables with the calculated ping statistics.
     * <p>
     * This method updates two tables:
     * </p>
     * <ul>
     * <li>The response results table with minimum, maximum, and average
     * round-trip times.</li>
     * <li>The packet results table with the total number of pings, successful
     * pings, failed pings, and the packet loss percentage.</li>
     * </ul>
     * <p>
     * The updates are performed on the EDT using {@code invokeLater()}.
     * </p>
     */
    public void populateResultsTables() {
        invokeLater(() -> {
            // Populate the response results table.
            devicePingResponseResultsTableModel.addRow(new Object[]{
                getMinimumRoundTripTime(),
                getMaximumRoundTripTime(),
                getAverageRoundTripTime()

            });

            // Populate the packet results table.
            devicePingPacketResultsTableModel.addRow(new Object[]{
                pingCount,
                successfulPings,
                pingCount - successfulPings,
                getPacketLoss()
            });
        });
    }

    /**
     * Calculates the packet loss percentage.
     *
     * @return the packet loss percentage as a double, rounded to two decimal
     * places
     */
    private double getPacketLoss() {
        double packetLoss = (1.0 - ((double) successfulPings / (double) pingCount)) * 10_000.0;
        packetLoss = Math.round(packetLoss);
        packetLoss /= 100.0;
        return packetLoss;
    }

    /**
     * Computes the minimum round-trip time among all successful ping results.
     *
     * @return the minimum round-trip time in milliseconds
     */
    private int getMinimumRoundTripTime() {
        // Assume the first result is the minimum initially.
        int minimum = devicePingResults.getFirst().getRoundTripTime();

        for (DevicePingResult result : devicePingResults) {
            // For each result, check if it is smaller than the current minimum.
            if (result.getRoundTripTime() < minimum) {
                // Update the minimum if it is the case.
                minimum = result.getRoundTripTime();
            }
        }

        return minimum;
    }

    /**
     * Computes the maximum round-trip time among all ping results.
     *
     * @return the maximum round-trip time in milliseconds
     */
    private int getMaximumRoundTripTime() {
        // Assume the first result is the maximum initially.
        int maximum = devicePingResults.getFirst().getRoundTripTime();

        for (DevicePingResult result : devicePingResults) {
            // For each result, check if it is larger than the current maximum
            if (result.getRoundTripTime() > maximum) {
                // Update the maximum if it is the case
                maximum = result.getRoundTripTime();
            }
        }

        return maximum;
    }

    /**
     * Computes the average round-trip time among all successful ping results.
     *
     * @return the average round-trip time in milliseconds, rounded to two
     * decimal places
     */
    private double getAverageRoundTripTime() {
        // Create a temporary holder variable to store the total round trip time of all successful pings.
        double totalRoundTripTime = 0;

        for (DevicePingResult result : devicePingResults) {
            // For each result, check if it is a successful result.
            if (result.isSuccessfulPing()) {
                // Add the round-trip time if it is the case
                totalRoundTripTime += result.getRoundTripTime();
            }
        }

        // Calculate the average given the total round trip time of all successful pings.
        double avgRoundTripTime = totalRoundTripTime / successfulPings * 100.0;
        avgRoundTripTime = Math.round(avgRoundTripTime);
        avgRoundTripTime /= 100.0;

        return avgRoundTripTime;
    }

    /**
     * Requests that the pinging process stop.
     * <p>
     * This sets the {@code stopRequested} flag to true so that the ping loop in
     * {@code start()} will terminate early.
     * </p>
     */
    public void requestStop() {
        stopRequested = true;
    }

    /**
     * Checks whether a stop request has been made.
     *
     * @return {@code true} if a stop has been requested; {@code false}
     * otherwise
     */
    public boolean isStopRequested() {
        return stopRequested;
    }

    /**
     * Returns the list of individual ping results.
     *
     * @return an {@code ArrayList} of {@code DevicePingResult} objects
     */
    public ArrayList<DevicePingResult> getDevicePingResults() {
        return devicePingResults;
    }

    /**
     * Sets the list of ping results.
     * <p>
     * Used when importing results, i.e. when no device ping was performed to
     * have added the successful pings to the device ping results list.
     * </p>
     *
     * @param devicePingResults an {@code ArrayList} of {@code DevicePingResult}
     * objects
     */
    public void setDevicePingResults(ArrayList<DevicePingResult> devicePingResults) {
        this.devicePingResults = devicePingResults;
    }

    /**
     * Returns the target IP address being pinged.
     *
     * @return the IP address as a {@code String}
     */
    public String getIpAddress() {
        return IP_ADDRESS;
    }

    /**
     * Returns the ping interval in milliseconds.
     *
     * @return the ping interval as an {@code int}
     */
    public int getPingInterval() {
        return PING_INTERVAL;
    }

    /**
     * Returns the total number of pings configured.
     *
     * @return the number of pings as an {@code int}
     */
    public int getNumOfPings() {
        return NUM_OF_PINGS;
    }

    /**
     * Indicates whether the ping process is running continuously.
     *
     * @return {@code true} if continuous pinging is enables; {@code false}
     * otherwise
     */
    public boolean isContinuousPinging() {
        return CONTINUOUS_PINGING;
    }

    /**
     * Returns the number of successful pings.
     *
     * @return the number of successful pings as an {@code int}
     */
    public int getSuccessfulPings() {
        return successfulPings;
    }

    /**
     * Recalculates the number of successful pings based on the current results.
     * <p>
     * This iterates over all results and increments the successful ping counter
     * for every result that indicates a successful ping.
     * </p>
     * <p>
     * Used when importing results, i.e. when no device ping was performed to
     * count the number of successful pings.
     * </p>
     */
    public void setSuccessfulPings() {
        for (DevicePingResult devicePingResult : devicePingResults) {
            successfulPings = devicePingResult.isSuccessfulPing() ? successfulPings + 1 : successfulPings;
        }
    }

    /**
     * Updates the ping counter to reflect the total number of pings performed.
     * <p>
     * This simply sets the pingCount to the size of the
     * {@code devicePingResults} list.
     * </p>
     * <p>
     * Used when importing results, i.e. when no device ping was performed to
     * count the number of pings.
     * </p>
     */
    public void setPingCount() {
        pingCount = devicePingResults.size();
    }
}
