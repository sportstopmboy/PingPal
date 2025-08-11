package com.pingpal.exports;

import com.pingpal.deviceping.DevicePing;
import com.pingpal.deviceping.DevicePingResult;
import com.pingpal.portscan.PortScan;
import com.pingpal.portscan.PortScanResult;
import com.pingpal.subnetscan.SubnetScan;
import com.pingpal.subnetscan.SubnetScanResult;
import com.pingpal.tcpmessage.connect.TCPMessageConnect;
import com.pingpal.tcpmessage.listen.TCPMessageListen;
import java.io.FileWriter;
import java.io.IOException;
import java.nio.file.Path;
import javax.swing.JFileChooser;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.text.BadLocationException;
import org.json.JSONArray;
import org.json.JSONObject;

/**
 * Handles exporting of various scan and chat results to JSON or text files.
 * <p>
 * Prompts the user for a directory and file name, then writes the results of
 * SubnetScan, DevicePing, PortScan, or TCPMessage sessions to disk in the
 * respective format.
 * </p>
 */
public class ExportResults {

    // Parent UI panel for dialogueues.
    private JPanel panel;

    // File chooser for directory selection.
    private JFileChooser fchDirectoryChooser;

    // Path of the directory selected for export.
    private Path exportResultsPath;

    // Base name for the output file (no extension).
    private String fileName;

    /**
     *
     * Constructs a new {@code ExportResults} instance with the specified panel,
     * immediately asking the user to choose a directory, then a file name.
     *
     * @param panel the Swing panel used as parent for dialogue
     */
    public ExportResults(JPanel panel) {
        this.panel = panel;

        // Configure chooser to pick directories only.
        fchDirectoryChooser = new JFileChooser();
        fchDirectoryChooser.setDialogTitle("Select a directory");
        fchDirectoryChooser.setFileSelectionMode(JFileChooser.DIRECTORIES_ONLY);

        // Prompt for directory.
        setExportResultsPath();

        // If successful, then prompt for file name.
        if (exportResultsPath != null) {
            setFileName();
        }
    }

    /**
     * Shows the directory chooser and stores the chosen path. If the user
     * cancels, exportResultsPath remains null.
     */
    private void setExportResultsPath() {
        // Displays the directory chooser.
        int returnVal = fchDirectoryChooser.showOpenDialog(panel);
        if (returnVal == JFileChooser.APPROVE_OPTION) {
            // Convert the selected File to Path.
            exportResultsPath = fchDirectoryChooser.getSelectedFile().toPath();
        }
    }

    /**
     * Repeatedly prompts the user for a valid file name (no blanks, periods, or
     * slashes) until one is entered.
     */
    private void setFileName() {
        // Loop until user provides acceptable name.
        while (fileName == null) {
            // Input dialogueue returns null if cancelled.
            String input = "" + JOptionPane.showInputDialog(panel, "Enter a file name:", "File Name Input", JOptionPane.QUESTION_MESSAGE);

            // Guard against cancel or blank input.
            if (input.isBlank() || input.equals("null")) {
                // Display corresponding error message in a message dialogueue.
                JOptionPane.showMessageDialog(panel, "File name cannot be blank.", "Blank File Name Error", JOptionPane.ERROR_MESSAGE);

                // Guard against a file name that contains a period.
            } else if (input.contains(".")) {
                // Display corresponding error message in a message dialogueue.
                JOptionPane.showMessageDialog(panel, "File name cannot contain a fullstop.", "Invalid Format Error", JOptionPane.ERROR_MESSAGE);

                // Guard against a file name that contains a forward slash and/or back slash.
            } else if (input.contains("\\") || input.contains("/")) {
                // Display corresponding error message in a message dialogueue.
                JOptionPane.showMessageDialog(panel, "File name cannot contain a slash.", "Invalid Format Error", JOptionPane.ERROR_MESSAGE);

                // If the name is valid, set the file name to the user input, and exit the loop.
            } else {
                fileName = input;
            }
        }
    }

    /**
     * Writes a {@link JSONObject} to a .json file in the chosen directory,
     * showing a success or error dialogue upon completion.
     *
     * @param output the JSON object to write
     */
    private void writeToJSONFile(JSONObject output) {
        // Construct full file path with .json extension.
        // Try-with-resources to ensure FileWriter is closed.
        try (FileWriter file = new FileWriter(exportResultsPath + "\\" + fileName + ".json")) {
            // Use toString(4) for pretty printing with 4 space indentation.
            file.write(output.toString(4));

            // If successful, display a success message in a message dialogue.
            JOptionPane.showMessageDialog(panel, "Successfully exported results to \"" + fileName + ".json\"", "Successful Export", JOptionPane.INFORMATION_MESSAGE);

            // General I/O error.
        } catch (IOException e) {
            // If unsuccessful, display an error message in a message dialogue.
            JOptionPane.showMessageDialog(panel, "Error occured during file writing process.", "File Writing Error", JOptionPane.ERROR_MESSAGE);
        }
    }

    /**
     * Writes plain text to a .txt file in the chosen directory, showing a
     * success or error dialogue upon completion.
     *
     * @param txt the text content to write
     */
    private void writeToTextFile(String txt) {
        // Construct full file path with .txt extension.
        // Try-with-resources to ensure FileWriter is closed.
        try (FileWriter file = new FileWriter(exportResultsPath + "\\" + fileName + ".txt")) {
            // Write to the file.
            file.write(txt);

            // If successful, display a success message in a message dialogue.
            JOptionPane.showMessageDialog(panel, "Successfully exported results to \"" + fileName + ".txt\"", "Successful Export", JOptionPane.INFORMATION_MESSAGE);

            // General I/O error.
        } catch (IOException e) {
            // If unsuccessful, display an error message in a message dialogue.
            JOptionPane.showMessageDialog(panel, "Error occured during file writing process.", "File Writing Error", JOptionPane.ERROR_MESSAGE);
        }
    }

    /**
     * Exports the results of a {@link SubnetScan} to JSON.
     * <p>
     * This overload uses {@link SubnetScan} as the scan type.
     * </p>
     *
     * @param subnetScan the scan whose results to export
     */
    public void exportResults(SubnetScan subnetScan) {
        // Guard against a blank subnet scan, i.e. if no scan has been performed.
        if (subnetScan == null) {
            // Display corresponding error message in a message dialogue.
            JOptionPane.showMessageDialog(panel, "Cannot export results as no subnet scan has been performed.", "Null Subnet Scan Error", JOptionPane.ERROR_MESSAGE);
            return;
        }

        // Guard against a blank file path, i.e. if the user has not selected a path.
        if (exportResultsPath == null) {
            // Display corresponding error message in a message dialogue.
            JOptionPane.showMessageDialog(panel, "Results not exported as no directory was selected.", "File Path Error", JOptionPane.ERROR_MESSAGE);
            return;
        }

        // Create a blank JSON array to hold all subnet scan results.
        JSONArray resultsArray = new JSONArray();

        // Loop through each subnet scan result.
        for (SubnetScanResult result : subnetScan.getSubnetScanResults()) {
            // Create a blank JSON object to hold the individual subnet scan results.
            JSONObject resultObj = new JSONObject();

            // Append the data from the subnet scan result to the JSON object.
            resultObj.put("ipAddress", result.getIPAddress());

            // Append the JSON object to the JSON array.
            resultsArray.put(resultObj);
        }

        // Create a top-level JSON object.
        JSONObject output = new JSONObject();

        // Wrap the metadata and array in the top-level JSON object.
        output.put("networkRange", subnetScan.getNetworkRange());
        output.put("timeout", subnetScan.getTimeout());
        output.put("subnetScanResults", resultsArray);

        // Write the data to the file.
        writeToJSONFile(output);
    }

    /**
     * Exports the results of a {@link DevicePing} to JSON.
     * <p>
     * This overload uses {@link DevicePing} as the scan type.
     * </p>
     *
     * @param devicePing the ping sessions whose results to export
     */
    public void exportResults(DevicePing devicePing) {
        // Guard against a blank device ping, i.e. if no ping has been performed.
        if (devicePing == null) {
            // Display corresponding error message in a message dialogue.
            JOptionPane.showMessageDialog(panel, "Cannot export results as no device ping has been performed.", "Null Device Ping Error", JOptionPane.ERROR_MESSAGE);
            return;
        }

        // Guard against a blank file path, i.e. if the user has not selected a path.
        if (exportResultsPath == null) {
            // Display corresponding error message in a message dialogue.
            JOptionPane.showMessageDialog(panel, "Results not exported as no directory was selected.", "File Path Error", JOptionPane.ERROR_MESSAGE);
            return;
        }

        // Create a blank JSON array to hold all device ping results.
        JSONArray resultsArray = new JSONArray();

        // Loop through each device ping result.
        for (DevicePingResult result : devicePing.getDevicePingResults()) {
            // Create a blank JSON object to hold the individual device ping results. 
            JSONObject resultObj = new JSONObject();

            // Append the data from the subnet scan result to the JSON object.
            resultObj.put("roundTripTime", result.getRoundTripTime());
            resultObj.put("successfulPing", result.isSuccessfulPing());
            resultObj.put("packetLoss", result.getPacketLoss());

            // Append the JSON object to the JSON array.
            resultsArray.put(resultObj);
        }

        // Create a top-level JSON object.
        JSONObject output = new JSONObject();

        // Wrap the metadata and array in the top-level JSON object.
        output.put("ipAddress", devicePing.getIpAddress());
        output.put("pingInterval", devicePing.getPingInterval());
        output.put("numOfPings", devicePing.getNumOfPings());
        output.put("continuousPinging", devicePing.isContinuousPinging());
        output.put("devicePingResults", resultsArray);

        // Write the data to the file.
        writeToJSONFile(output);
    }

    /**
     * Exports the results of a {@link PortScan} to JSON.
     * <p>
     * This overload uses {@link PortScan} as the scan type.
     * </p>
     *
     * @param portScan the port scan whose results to export
     */
    public void exportResults(PortScan portScan) {
        // Guard against a blank port scan, i.e. if no scan has been performed.
        if (portScan == null) {
            // Display corresponding error message in a message dialogue.
            JOptionPane.showMessageDialog(panel, "Cannot export results as no port scan has been performed.", "Null Port Scan Error", JOptionPane.ERROR_MESSAGE);
            return;
        }

        // Guard against a blank file path, i.e. if the user has not selected a path.
        if (exportResultsPath == null) {
            // Display corresponding error message in a message dialogue.
            JOptionPane.showMessageDialog(panel, "Results not exported as no directory was selected.", "File Path Error", JOptionPane.ERROR_MESSAGE);
            return;
        }

        // Create a JSON array to hold all port scan results.
        JSONArray resultsArray = new JSONArray();

        // Loop through each port scan result.
        for (PortScanResult result : portScan.getPortScanResults()) {
            // Create a blank JSON object to hold the individual port scan results.
            JSONObject resultObj = new JSONObject();

            // Append the data from the port scan result to the JSON object.
            resultObj.put("portNumber", result.getPortNumber());
            resultObj.put("protocol", result.getProtocol());

            // Append the JSON object to the JSON array.
            resultsArray.put(resultObj);
        }

        // Create a top-level JSON object.
        JSONObject output = new JSONObject();

        // Wrap the metadata and array in the top-level JSON object.
        output.put("ipAddress", portScan.getIpAddress());
        output.put("bottomRangePort", portScan.getBottomRangePort());
        output.put("topRangePort", portScan.getTopRangePort());
        output.put("timeout", portScan.getTimeout());
        output.put("portScanResults", resultsArray);

        // Write the data to the file.
        writeToJSONFile(output);

    }

    /**
     * Exports the results of a {@link TCPMessageListen} to a text file.
     * <p>
     * This overload uses {@link TCPMessageListen} as the scan type.
     * </p>
     *
     * @param tcpMessageListen the server side messages to export
     */
    public void exportResults(TCPMessageListen tcpMessageListen) {
        // Guard against a blank TCP message listen, i.e. if no messages have been exchanged.
        if (tcpMessageListen == null) {
            // Display corresponding error message in a message dialogue.
            JOptionPane.showMessageDialog(panel, "Cannot export results as no TCP message listen has been performed.", "Null TCP Message Listen Error", JOptionPane.ERROR_MESSAGE);
            return;

        }

        // Guard against a blank file path, i.e. if the user has not selected a path.
        if (exportResultsPath == null) {
            // Display corresponding error message in a message dialogue.
            JOptionPane.showMessageDialog(panel, "Results not exported as no directory was selected.", "File Path Error", JOptionPane.ERROR_MESSAGE);
            return;
        }

        try {
            // Write the data to the file.
            writeToTextFile(tcpMessageListen.getTextPaneContents());
        } catch (BadLocationException e) {
            // Display an error message in a message dialogue if an error occurs.
            JOptionPane.showMessageDialog(panel, "Error occured during file writing process.", "File Writing Error", JOptionPane.ERROR_MESSAGE);
        }

    }

    /**
     * Exports the results of a {@link TCPMessageConnect} to a text file.
     * <p>
     * This overload uses {@link TCPMessageConnect} as the scan type.
     * </p>
     *
     * @param tcpMessageConnect the server side messages to export
     */
    public void exportResults(TCPMessageConnect tcpMessageConnect) {
        // Guard against a blank TCP message connect, i.e. if no messages have been exchanged.
        if (tcpMessageConnect == null) {
            // Display corresponding error message in a message dialogue.
            JOptionPane.showMessageDialog(panel, "Cannot export results as no TCP message connect has been performed.", "Null TCP Message Connect Error", JOptionPane.ERROR_MESSAGE);
            return;
        }
        
        // Guard against a blank file path, i.e. if the user has not selected a path.
        if (exportResultsPath == null) {
            // Display corresponding error message in a message dialogue.
            JOptionPane.showMessageDialog(panel, "Results not exported as no directory was selected.", "File Path Error", JOptionPane.ERROR_MESSAGE);
            return;
        }
        
        try {
            // Write the data to the file.
            writeToTextFile(tcpMessageConnect.getTextPaneContents());
        } catch (BadLocationException e) {
            // Display an error message in a message dialogue if an error occurs.
            JOptionPane.showMessageDialog(panel, "Error occured during file writing process.", "File Writing Error", JOptionPane.ERROR_MESSAGE);
        }

    }
}
