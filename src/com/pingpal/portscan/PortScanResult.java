package com.pingpal.portscan;

/**
 * Represents a single result from a port scan.
 * <p>
 * This class encapsulates the details of a port scan result, including the port
 * number that was found to be open and the associated protocol determined for
 * that port.
 * </p>
 */
public class PortScanResult {

    // The port number that was found to be open.
    private int portNumber;

    // The protocol associated with the port number. For example, "http", "https".
    private String protocol;

    /**
     * Constructs a new {@code PortScanResult} with the specified port number
     * and protocol.
     *
     * @param portNumber the port number that was found to be open
     * @param protocol the protocol associated with the port (e.g., "http",
     * "ftp")
     */
    public PortScanResult(int portNumber, String protocol) {
        this.portNumber = portNumber;
        this.protocol = protocol;
    }

    /**
     * Returns the port number associated with this scan result.
     *
     * @return the open port number as an {@code int}
     */
    public int getPortNumber() {
        return portNumber;
    }

    /**
     * Returns the protocol associated with this port scan result.
     *
     * @return the protocol name as a {@code String}
     */
    public String getProtocol() {
        return protocol;
    }
}
