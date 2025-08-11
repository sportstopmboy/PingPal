package com.pingpal.deviceping;

/**
 * The {@code DevicePingResult} class represents the result of a ping operation
 * performed on a device.
 * <p>
 * This class encapsulates three pieces of information obtained from a device
 * ping:
 * </p>
 * <ul>
 * <li>The round-trip time (RTT) measured in milliseconds.</li>
 * <li>A flag indicating whether the ping was successful.</li>
 * <li>The packet loss percentage observed during the ping operation.</li>
 * </ul>
 */
public class DevicePingResult {

    // The round-trip time in milliseconds.
    private int roundTripTime;

    // Flag which indicates whether the ping was successful.
    private boolean successfulPing;

    // The percentage of packet loss measured during the ping.
    private double packetLoss;

    /**
     * Constructs a new {@code DevicePingResult} instance with the specified
     * round-trip time, successful ping flag, and packet loss percentage.
     *
     * @param roundTripTime the round-trip time in milliseconds
     * @param successfulPing {@code true} if the ping was successful;
     * {@code false} otherwise
     * @param packetLoss the percentage of packet loss (as a double, e.g. 0.0
     * for no loss, 100.0 for complete loss)
     */
    public DevicePingResult(int roundTripTime, boolean successfulPing, double packetLoss) {
        this.roundTripTime = roundTripTime;
        this.successfulPing = successfulPing;
        this.packetLoss = packetLoss;
    }

    /**
     * Returns the round-trip time (RTT) for the ping operation.
     *
     * @return the round-trip time in milliseconds as an {@code int}
     */
    public int getRoundTripTime() {
        return roundTripTime;
    }

    /**
     * Returns whether the ping was successful.
     *
     * @return {@code true} if the ping was successful; {@code false} otherwise
     */
    public boolean isSuccessfulPing() {
        return successfulPing;
    }

    /**
     * Returns the packet loss percentage observed during the ping.
     *
     * @return the packet loss percentage as a {@code double}
     */
    public double getPacketLoss() {
        return packetLoss;
    }
}
