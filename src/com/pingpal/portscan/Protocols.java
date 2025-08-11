package com.pingpal.portscan;

import java.io.File;
import java.io.FileNotFoundException;
import java.util.HashMap;
import java.util.Map;
import java.util.Scanner;
import javax.swing.JOptionPane;

/**
 * The {@code Protocols} class loads and stores a mapping between TCP/UDP port
 * numbers and their corresponding protocol names from a CSV file.
 * <p>
 * The CSV file is expected to be located at:
 * <code>./src/com/pingpal/resources/databases/port_list.csv</code> and must
 * contain a header line followed by lines where the first field is a port
 * number and the second field is the protocol name.
 * </p>
 * <p>
 * If the file is not found, the class displays an error message using a
 * JOptionPane.
 * </p>
 */
public class Protocols {

    /**
     * A mapping from port numbers (as {@code Integer}) to protocol names (as
     * {@code String}).
     */
    private Map<Integer, String> portProtocolMap = new HashMap<>();

    /**
     * Constructs a new {@code Protocols} instance.
     * <p>
     * This constructor reads the port list from a CSV file and populates the
     * {@code portProtocolMap}. It skips the first line assuming it is a header.
     * </p>
     * <p>
     * If the CSV file is not found, an error dialog is shown.
     * </p>
     */
    public Protocols() {
        // Open scanner for reading.
        try (Scanner scFile = new Scanner(new File(".\\src\\com\\pingpal\\resources\\databases\\port_list.csv"))) {
            // Skip the header line.
            scFile.nextLine();
            // Read each subsequent line in the CSV file.
            while (scFile.hasNextLine()) {
                // Split the line on commas
                String[] line = scFile.nextLine().split(",");
                // Parse the port number (first column).
                int port = Integer.parseInt(line[0].trim());
                // Get the protocol name (second column).
                String protocol = line[1].trim();
                // Add the port-to-protocol mapping.
                portProtocolMap.put(port, protocol);
            }
        } catch (FileNotFoundException ex) {
            // Show an error message if the file cannot be found.
            JOptionPane.showMessageDialog(null, "Port list not found.", "File Not Found Error", JOptionPane.ERROR_MESSAGE);
        }
    }

    /**
     * Retrieves the protocol name associated with the specified port number.
     * <p>
     * If no protocol is found for the given port, a default message is
     * returned.
     * </p>
     *
     * @param portNumber the port number for which to retrieve the protocol
     * @return the protocol name corresponding to the port number, or "No
     * specific protocol associated with this port." if the port is not mapped
     */
    public String getProtocolForPort(int portNumber) {
        return portProtocolMap.getOrDefault(portNumber, "No specific protocol associated with this port.");
    }
}
    