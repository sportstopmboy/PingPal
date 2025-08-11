package com.pingpal.imports;

import com.pingpal.deviceping.DevicePingResult;
import com.pingpal.portscan.PortScanResult;
import com.pingpal.subnetscan.SubnetScanResult;
import java.util.ArrayList;

/**
 * The {@code ImportResultsListener} interface defines callback methods for
 * handling the imported scan results from various types of network scans.
 * <p>
 * Implementers of this interface will receive notifications when scan results
 * have been successfully imported from a JSON file. There are separate
 * callbacks for subnet scans, device pings, and port scans, providing all
 * necessary details for further processing or updating the user interface.
 * </p>
 */
public interface ImportResultsListener {

    /**
     * Called when subnet scan results have been successfully imported.
     *
     * @param networkRange the network range that was scanned (e.g.,
     * "192.168.0.0/24")
     * @param timeout the timeout (in milliseconds) used during the scan
     * @param subnetScanResults a list of {@code SubnetScanResult} objects
     * representing the reachable IP addresses found in the scan
     */
    void onSubnetScanResultsImported(String networkRange, int timeout, ArrayList<SubnetScanResult> subnetScanResults);

    /**
     * Called when device ping results have been successfully imported.
     *
     * @param ipAddress the IP address that was pinged
     * @param pingInterval the interval (in milliseconds) between successive
     * pings
     * @param numOfPings the total number of pings performed
     * @param continuousPinging a boolean indicating if the pinging was
     * continuous (true) or a fixed number of pings (false)
     * @param devicePingResults a list of {@code DevicePingResult} objects
     * containing the results of the ping operations (e.g., round-trip times,
     * packet loss)
     */
    void onDevicePingResultsImported(String ipAddress, int pingInterval, int numOfPings, boolean continuousPinging, ArrayList<DevicePingResult> devicePingResults);

    /**
     * Called when port scan results have been successfully imported.
     *
     * @param ipAddress the IP address that was scanned
     * @param bottomRangePort the starting port number of the scan range
     * @param topRangePort the ending port number of the scan range
     * @param timeout the timeout (in milliseconds) used during the scan
     * @param portScanResults a list of {@code PortScanResult} objects
     * representing the results of the port scan (i.e., open ports and
     * associated protocols)
     */
    void onPortScanResultsImported(String ipAddress, int bottomRangePort, int topRangePort, int timeout, ArrayList<PortScanResult> portScanResults);
}
