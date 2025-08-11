package com.pingpal.subnetscan;

/**
 * The {@code SubnetScanResult} class represents the result of scanning a
 * subnet, encapsulating a reachable IP address.
 * <p>
 * This class is a simple data container used to store an IP address that has
 * been found to be reachable during a subnet scan.
 * </p>
 *
 * @author Igor Karbowy
 */
public class SubnetScanResult {

    // The IP address that was found to be reachable.
    private String ipAddress;

    /**
     * Constructs a new {@code SubnetScanResult} instance with the specified IP
     * address.
     *
     * @param ipAddress the reachable IP address in standard dot-decimal
     * notation (e.g., "192.168.0.1")
     */
    public SubnetScanResult(String ipAddress) {
        this.ipAddress = ipAddress;
    }

    /**
     * Returns the reachable IP address stored in this result.
     *
     * @return a {@code String} representing the IP address in dot-decimal
     * notation
     */
    public String getIPAddress() {
        return ipAddress;
    }
}
