package com.pingpal.imports;

import com.pingpal.datavalidation.ValidationUtils;
import com.pingpal.deviceping.DevicePingResult;
import com.pingpal.exceptions.imports.InvalidNumOfPingsException;
import com.pingpal.exceptions.imports.InvalidPacketLossRangeException;
import com.pingpal.exceptions.imports.InvalidPingIntervalRangeException;
import com.pingpal.exceptions.imports.InvalidPortNumberRangeException;
import com.pingpal.exceptions.imports.InvalidPortProtocolRelationshipException;
import com.pingpal.exceptions.imports.InvalidRoundTripTimeException;
import com.pingpal.exceptions.imports.InvalidScanTypeException;
import com.pingpal.exceptions.imports.InvalidSuccessfulPingException;
import com.pingpal.exceptions.imports.InvalidTimeoutRangeException;
import com.pingpal.exceptions.imports.InvalidVariableInstanceException;
import com.pingpal.exceptions.imports.MissingRequiredKeysException;
import com.pingpal.exceptions.ui.BlankFieldException;
import com.pingpal.exceptions.ui.InvalidIPAddressException;
import com.pingpal.exceptions.ui.InvalidNetworkRangeException;
import com.pingpal.exceptions.ui.InvalidPortRangeException;
import com.pingpal.portscan.PortScanResult;
import com.pingpal.subnetscan.SubnetScanResult;
import java.awt.HeadlessException;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.util.ArrayList;
import javax.swing.JFileChooser;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.filechooser.FileNameExtensionFilter;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.json.JSONTokener;

/**
 * The {@code ImportResults} class handles the import of JSON-formatted scan
 * results from a file.
 * <p>
 * This class is responsible for selecting a file (via a JFileChooser), reading
 * its JSON contents, validating the data for different types of scan results
 * (subnet scan, device ping, and port scan), and then passing the parsed data
 * to a registered {@code ImportResultsListener}.
 * </p>
 * <p>
 * If any errors occur (e.g. missing fields, invalid data types, or JSON parsing
 * errors), the class displays a relevant error message using a JOptionPane.
 * </p>
 */
public class ImportResults {

    // The JPanel that is used as the parent for dialogues.
    private JPanel panel;

    // A JFileChooser configured to select only JSON files.
    private JFileChooser fchFileChooser;

    // The file containing the imported JSON scan results.
    private File importResultsFile;

    // The JSONObject parsed from the selected file.
    private JSONObject fileData;

    // A listener which will be notified when scan results have been successfully imported.
    private ImportResultsListener listener;

    /**
     * Constructs a new ImportResults instance with the specified panel and
     * listener.
     *
     * @param panel the JPanel used for displaying file chooser dialogues and
     * error messages
     * @param listener the listener to receive imported scan data
     */
    public ImportResults(JPanel panel, ImportResultsListener listener) {
        this.panel = panel;
        this.listener = listener;
    }

    /**
     * Opens a file chooser dialogue for the user to select a JSON file.
     * <p>
     * The file chooser is configured to only accept files with a ".json"
     * extension.
     * </p>
     */
    public void setImportResultsPath() {
        // Initialise the file chooser.
        fchFileChooser = new JFileChooser();

        // Disallow the selection of all files.
        fchFileChooser.setAcceptAllFileFilterUsed(false);
        // Restrict the accepted file type to only JSON files.
        fchFileChooser.setFileFilter(new FileNameExtensionFilter("JSON FILES", "json", "json"));

        // Set the title of the file chooser.
        fchFileChooser.setDialogTitle("Select a JSON file");

        // If a file is selected, initialise the results file variable.
        int returnVal = fchFileChooser.showOpenDialog(panel);
        if (returnVal == JFileChooser.APPROVE_OPTION) {
            importResultsFile = fchFileChooser.getSelectedFile();
        }
    }

    /**
     * Reads and parses JSON data from the selected file.
     *
     * @return a JSONObject representing the contents of the file
     * @throws FileNotFoundException if the file does not exist
     * @throws JSONException if an error occurs during JSON parsing
     */
    private JSONObject readFileData() throws FileNotFoundException, JSONException {
        return new JSONObject(new JSONTokener(new FileReader(importResultsFile)));
    }

    /**
     * Determines the type of scan results contained in the imported JSON file,
     * calls helper methods to validate the data, then calls the appropriate
     * import method.
     * <p>
     * The method checks whether the JSON file contains subnet scan, device
     * ping, or port scan results based on the presence of specific keys, and
     * then validates the data using dedicated validation methods. If a scan
     * type is unrecognized, an InvalidScanTypeException is thrown.
     * </p>
     *
     * @throws InvalidScanTypeException if the scan type in the JSON file is
     * unknown
     */
    public void determineScanType() throws InvalidScanTypeException {
        try {
            // Parse JSON data from the selected file.
            fileData = readFileData();

            // Determine if the data is the results of a subnet scan.
            if (fileData.has("subnetScanResults")) {
                // Validate the data in the file.
                if (!validateSubnetScanData()) {
                    return;
                }
                // If all checks are successful, import the results.
                importSubnetScanData();

                // Determine if the data is the result of a device ping.
            } else if (fileData.has("devicePingResults")) {
                // Validate the data in the file.
                if (!validateDevicePingData()) {
                    return;
                }
                // If all checks successful, import the results.
                importDevicePingData();

                // Determine if the data is the result of a port scan.
            } else if (fileData.has("portScanResults")) {
                // Validate the data in the file.
                if (!validatePortScanData()) {
                    return;
                }
                // If all checks successful, import the results.
                importPortScanData();

                // If the data is not the results of a PingPal scan, throw and InvaldScanTypeExceptioin.
            } else {
                throw new InvalidScanTypeException("Unkown scan type in file.");
            }

            // Catch any exceptions that may occur during the process of reading the data from the file.
            // Display appropropriate error messages.
        } catch (NullPointerException e) {
            JOptionPane.showMessageDialog(panel, "Results not imported as no directory was selected.", "File Path Error", JOptionPane.ERROR_MESSAGE);

        } catch (HeadlessException e) {
            JOptionPane.showMessageDialog(panel, "Error occurred during file reading process.", "File Reading Error", JOptionPane.ERROR_MESSAGE);

        } catch (FileNotFoundException e) {
            JOptionPane.showMessageDialog(panel, "This file does not exist.", "File Reading Error", JOptionPane.ERROR_MESSAGE);

        } catch (JSONException e) {
            JOptionPane.showMessageDialog(panel, "JSON parsing error occurred during file reading process.", "File Reading Error", JOptionPane.ERROR_MESSAGE);
        }
    }

    /**
     * Validates that the JSON data for a subnet scan contains the required
     * fields and that their types and values are correct.
     *
     * @return {@code true} if subnet scan data is valid; {@code false}
     * otherwise
     */
    private boolean validateSubnetScanData() {
        // Check for required top-level keys.
        try {
            ValidationUtils.validateRequiredKeys(new String[]{"networkRange", "timeout", "subnetScanResults"}, fileData);
        } catch (MissingRequiredKeysException e) {
            JOptionPane.showMessageDialog(panel, "Missing one or more required top-level fields.", "Missing Field Error", JOptionPane.ERROR_MESSAGE);
            return false;
        }

        // Validate networkRange is a string.
        try {
            ValidationUtils.validateInstanceString(fileData.get("networkRange"));
        } catch (InvalidVariableInstanceException e) {
            JOptionPane.showMessageDialog(panel, "Network range field is not a string.", "Data Field Error", JOptionPane.ERROR_MESSAGE);
            return false;
        }

        // Validate networkRange presence.
        try {
            ValidationUtils.validateFieldPresence(fileData.getString("networkRange"));
        } catch (BlankFieldException e) {
            JOptionPane.showMessageDialog(panel, "Network range field is blank.", "Data Field Error", JOptionPane.ERROR_MESSAGE);
            return false;
        }

        // Validate networkRange format.
        try {
            ValidationUtils.validateNetworkRange(fileData.getString("networkRange"));
        } catch (InvalidNetworkRangeException e) {
            JOptionPane.showMessageDialog(panel, "IP range field is not in correct format.", "Data Field Error", JOptionPane.ERROR_MESSAGE);
            return false;
        }

        // Validate timeout is an integer.
        try {
            ValidationUtils.validateInstanceInteger(fileData.get("timeout"));
        } catch (InvalidVariableInstanceException e) {
            JOptionPane.showMessageDialog(panel, "Timeout field is not an integer.", "Data Field Error", JOptionPane.ERROR_MESSAGE);
            return false;
        }

        // Validate timeout range.
        try {
            ValidationUtils.validateTimeoutRange(fileData.getInt("timeout"));
        } catch (InvalidTimeoutRangeException e) {
            JOptionPane.showMessageDialog(panel, "Timeout value falls out of acceptable range.", "Data Field Error", JOptionPane.ERROR_MESSAGE);
            return false;
        }

        // Validate subnetScanResults is an array.
        try {
            ValidationUtils.validateInstanceJSONArray(fileData.get("subnetScanResults"));
        } catch (InvalidVariableInstanceException e) {
            JOptionPane.showMessageDialog(panel, "Subnet scan results field is not an array.", "Data Field Error", JOptionPane.ERROR_MESSAGE);
            return false;
        }

        // Validate each result from subnetScanResults array.
        JSONArray jsonSubnetScanResults = fileData.getJSONArray("subnetScanResults");

        for (int i = 0; i < jsonSubnetScanResults.length(); i++) {
            JSONObject jsonSubnetScanResult = jsonSubnetScanResults.getJSONObject(i);

            // Check for required keys in each object.
            try {
                ValidationUtils.validateRequiredKeys(new String[]{"ipAddress"}, jsonSubnetScanResult);
            } catch (MissingRequiredKeysException e) {
                JOptionPane.showMessageDialog(panel, "Missing required field in subnet scan results array at index " + i + ".", "Missing Field Error", JOptionPane.ERROR_MESSAGE);
                return false;
            }

            // Validate IP address is a string.
            try {
                ValidationUtils.validateInstanceString(jsonSubnetScanResult.get("ipAddress"));
            } catch (InvalidVariableInstanceException e) {
                JOptionPane.showMessageDialog(panel, "IP address field is not a string at index " + i + ".", "Data Field Error", JOptionPane.ERROR_MESSAGE);
                return false;
            }

            // Validate IP address presence.
            try {
                ValidationUtils.validateFieldPresence(jsonSubnetScanResult.getString("ipAddress"));
            } catch (BlankFieldException e) {
                JOptionPane.showMessageDialog(panel, "IP is blank at index " + i + ".", "Data Field Error", JOptionPane.ERROR_MESSAGE);
                return false;
            }

            // Validate IP address format.
            try {
                ValidationUtils.validateIPAddress(jsonSubnetScanResult.getString("ipAddress"));
            } catch (InvalidIPAddressException e) {
                JOptionPane.showMessageDialog(panel, "IP address field is not in correct format at index " + i + ".", "Data Field Error", JOptionPane.ERROR_MESSAGE);
                return false;
            }
        }

        // All checks successful.
        return true;
    }

    /**
     * Validates that the JSON data for a device ping scan contains required
     * fields and that their values are valid.
     *
     * @return {@code true} if the device ping data is valid; {@code false}
     * otherwise
     */
    public boolean validateDevicePingData() {
        // Check for required top-level keys.
        try {
            ValidationUtils.validateRequiredKeys(new String[]{"ipAddress", "pingInterval", "numOfPings", "continuousPinging", "devicePingResults"}, fileData);
        } catch (MissingRequiredKeysException e) {
            JOptionPane.showMessageDialog(panel, "Missing one or more required top-level fields.", "Missing Field Error", JOptionPane.ERROR_MESSAGE);
            return false;
        }

        // Validate ipAddress is a string.
        try {
            ValidationUtils.validateInstanceString(fileData.get("ipAddress"));
        } catch (InvalidVariableInstanceException e) {
            JOptionPane.showMessageDialog(panel, "IP address field is not a string.", "Data Field Error", JOptionPane.ERROR_MESSAGE);
            return false;
        }

        // Validate ipAddress presence.
        try {
            ValidationUtils.validateFieldPresence(fileData.getString("ipAddress"));
        } catch (BlankFieldException e) {
            JOptionPane.showMessageDialog(panel, "IP address field is blank.", "Data Field Error", JOptionPane.ERROR_MESSAGE);
            return false;
        }

        // Validate ipAddress format.
        try {
            ValidationUtils.validateIPAddress(fileData.getString("ipAddress"));
        } catch (InvalidIPAddressException e) {
            JOptionPane.showMessageDialog(panel, "IP address field is not in correct format.", "Data Field Error", JOptionPane.ERROR_MESSAGE);
            return false;
        }

        // Validate pingInterval is an integer.
        try {
            ValidationUtils.validateInstanceInteger(fileData.get("pingInterval"));
        } catch (InvalidVariableInstanceException e) {
            JOptionPane.showMessageDialog(panel, "Ping interval field is not an integer.", "Data Field Error", JOptionPane.ERROR_MESSAGE);
            return false;
        }

        // Validate pingInterval range.
        try {
            ValidationUtils.validatePingInterval(fileData.getInt("pingInterval"));
        } catch (InvalidPingIntervalRangeException e) {
            JOptionPane.showMessageDialog(panel, "Ping interval value falls out of acceptable range.", "Data Field Error", JOptionPane.ERROR_MESSAGE);
            return false;
        }

        // Validate numOfPings is an integer.
        try {
            ValidationUtils.validateInstanceInteger(fileData.get("numOfPings"));
        } catch (InvalidVariableInstanceException e) {
            JOptionPane.showMessageDialog(panel, "Number of pings field is not an integer.", "Data Field Error", JOptionPane.ERROR_MESSAGE);
            return false;
        }

        // Validate numOfPings range.
        try {
            ValidationUtils.validateNumOfPingsRange(fileData.getInt("numOfPings"));
        } catch (InvalidNumOfPingsException e) {
            JOptionPane.showMessageDialog(panel, "Number of pings value falls out of acceptable range.", "Data Field Error", JOptionPane.ERROR_MESSAGE);
            return false;
        }

        // Validate continuousPinging is a boolean.
        try {
            ValidationUtils.validateInstanceBoolean(fileData.get("continuousPinging"));
        } catch (InvalidVariableInstanceException e) {
            JOptionPane.showMessageDialog(panel, "Continuous pinging field is not a boolean.", "Data Field Error", JOptionPane.ERROR_MESSAGE);
            return false;
        }

        // Validate devicePingResults is an array.
        try {
            ValidationUtils.validateInstanceJSONArray(fileData.get("devicePingResults"));
        } catch (InvalidVariableInstanceException e) {
            JOptionPane.showMessageDialog(panel, "Device ping results field is not an array.", "Data Field Error", JOptionPane.ERROR_MESSAGE);
            return false;
        }

        // Validate each result from devicePingResults array.
        JSONArray jsonDevicePingResults = fileData.getJSONArray("devicePingResults");

        for (int i = 0; i < jsonDevicePingResults.length(); i++) {
            JSONObject jsonDevicePingResult = jsonDevicePingResults.getJSONObject(i);

            // Check for required keys in each object.
            try {
                ValidationUtils.validateRequiredKeys(new String[]{"roundTripTime", "successfulPing", "packetLoss"}, jsonDevicePingResult);
            } catch (MissingRequiredKeysException e) {
                JOptionPane.showMessageDialog(panel, "Missing required field(s) in device ping results array at index " + i + ".", "Missing Field Error", JOptionPane.ERROR_MESSAGE);
                return false;
            }

            // Validate roundTripTime is an integer.
            try {
                ValidationUtils.validateInstanceInteger(jsonDevicePingResult.get("roundTripTime"));
            } catch (InvalidVariableInstanceException e) {
                JOptionPane.showMessageDialog(panel, "Round trip time field is not an integer at index " + i + ".", "Data Field Error", JOptionPane.ERROR_MESSAGE);
                return false;
            }

            // Validate roundTripTime range.
            try {
                ValidationUtils.validateRoundTripTime(jsonDevicePingResult.getInt("roundTripTime"), fileData.getInt("pingInterval"));
            } catch (InvalidRoundTripTimeException e) {
                JOptionPane.showMessageDialog(panel, "Round trip time value falls out of acceptable range at index " + i + ".", "Data Field Error", JOptionPane.ERROR_MESSAGE);
                return false;
            }

            // Validate successfulPing is a boolean.
            try {
                ValidationUtils.validateInstanceBoolean(jsonDevicePingResult.get("successfulPing"));
            } catch (InvalidVariableInstanceException e) {
                JOptionPane.showMessageDialog(panel, "Successful ping field is not boolean at index " + i + ".", "Data Field Error", JOptionPane.ERROR_MESSAGE);
                return false;
            }

            // Validate successfulPing logic.
            try {
                ValidationUtils.validateSuccessfulPingLogic(jsonDevicePingResult.getBoolean("successfulPing"), jsonDevicePingResult.getInt("roundTripTime"), fileData.getInt("pingInterval"));
            } catch (InvalidSuccessfulPingException e) {
                JOptionPane.showMessageDialog(panel, "Round trip time and successful ping results do not not match at index " + i + ".", "Data Field Error", JOptionPane.ERROR_MESSAGE);
                return false;
            }

            // Validate packetLoss is a double.
            try {
                ValidationUtils.validateInstanceNumber(jsonDevicePingResult.get("packetLoss"));
            } catch (InvalidVariableInstanceException e) {
                JOptionPane.showMessageDialog(panel, "Packet loss field is not a double at index " + i + ".", "Data Field Error", JOptionPane.ERROR_MESSAGE);
                return false;
            }

            // Validate packetLoss range.
            try {
                ValidationUtils.validatePacketLossRange(jsonDevicePingResult.getDouble("packetLoss"));
            } catch (InvalidPacketLossRangeException e) {
                JOptionPane.showMessageDialog(panel, "Packet loss value falls out of acceptable range at index " + i + ".", "Data Field Error", JOptionPane.ERROR_MESSAGE);
                return false;
            }
        }

        // All checks successful.
        return true;
    }

    /**
     * Validates that the JSON data for a port scan contains the required fields
     * and that each field is valid.
     *
     * @return {@code true} if the port scan data is valid; {@code false}
     * otherwise
     */
    public boolean validatePortScanData() {
        // Check for required top-level keys.
        try {
            ValidationUtils.validateRequiredKeys(new String[]{"ipAddress", "bottomRangePort", "topRangePort", "timeout", "portScanResults"}, fileData);
        } catch (MissingRequiredKeysException e) {
            JOptionPane.showMessageDialog(panel, "Missing one or more required top-level fields.", "Missing Field Error", JOptionPane.ERROR_MESSAGE);
            return false;
        }

        // Validate ipAddress is a string.
        try {
            ValidationUtils.validateInstanceString(fileData.get("ipAddress"));
        } catch (InvalidVariableInstanceException e) {
            JOptionPane.showMessageDialog(panel, "IP address field is not a string.", "Data Field Error", JOptionPane.ERROR_MESSAGE);
            return false;
        }

        // Validate ipAddress presence.
        try {
            ValidationUtils.validateFieldPresence(fileData.getString("ipAddress"));
        } catch (BlankFieldException e) {
            JOptionPane.showMessageDialog(panel, "IP address field is blank.", "Data Field Error", JOptionPane.ERROR_MESSAGE);
            return false;
        }

        // Validate ipAddress format.
        try {
            ValidationUtils.validateIPAddress(fileData.getString("ipAddress"));
        } catch (InvalidIPAddressException e) {
            JOptionPane.showMessageDialog(panel, "IP address field is not in correct format.", "Data Field Error", JOptionPane.ERROR_MESSAGE);
            return false;
        }

        // Validate bottomRangePort is an integer.
        try {
            ValidationUtils.validateInstanceInteger(fileData.get("bottomRangePort"));
        } catch (InvalidVariableInstanceException e) {
            JOptionPane.showMessageDialog(panel, "Bottom range port field is not an integer.", "Data Field Error", JOptionPane.ERROR_MESSAGE);
            return false;
        }

        // Validate bottomRangePort range.
        try {
            ValidationUtils.validatePortNumberRange(fileData.getInt("bottomRangePort"));
        } catch (InvalidPortNumberRangeException e) {
            JOptionPane.showMessageDialog(panel, "Bottom port value falls out of acceptable range.", "Data Field Error", JOptionPane.ERROR_MESSAGE);
            return false;
        }

        // Validate topRangePort is an integer.
        try {
            ValidationUtils.validateInstanceInteger(fileData.get("topRangePort"));
        } catch (InvalidVariableInstanceException e) {
            JOptionPane.showMessageDialog(panel, "Top range port field is not an integer.", "Data Field Error", JOptionPane.ERROR_MESSAGE);
            return false;
        }

        // Validate topRangePort range.
        try {
            ValidationUtils.validatePortNumberRange(fileData.getInt("topRangePort"));
        } catch (InvalidPortNumberRangeException e) {
            JOptionPane.showMessageDialog(panel, "Top port value falls out of acceptable range.", "Data Field Error", JOptionPane.ERROR_MESSAGE);
            return false;
        }

        // Validate port range logic.
        try {
            ValidationUtils.validatePortRange(fileData.getInt("bottomRangePort"), fileData.getInt("topRangePort"));
        } catch (InvalidPortRangeException e) {
            JOptionPane.showMessageDialog(panel, "Bottom port value is greater than top port value.", "Data Field Error", JOptionPane.ERROR_MESSAGE);
            return false;
        }

        // Validate timeout is an integer.
        try {
            ValidationUtils.validateInstanceInteger(fileData.get("timeout"));
        } catch (InvalidVariableInstanceException e) {
            JOptionPane.showMessageDialog(panel, "Timeout field is not an integer.", "Data Field Error", JOptionPane.ERROR_MESSAGE);
            return false;
        }

        // Validate timeout range.
        try {
            ValidationUtils.validateTimeoutRange(fileData.getInt("timeout"));
        } catch (InvalidTimeoutRangeException e) {
            JOptionPane.showMessageDialog(panel, "Timeout value falls out of acceptable range.", "Data Field Error", JOptionPane.ERROR_MESSAGE);
            return false;
        }

        // Validate portScanResults is an array.
        try {
            ValidationUtils.validateInstanceJSONArray(fileData.get("portScanResults"));
        } catch (InvalidVariableInstanceException e) {
            JOptionPane.showMessageDialog(panel, "Port scan results field is not an array.", "Data Field Error", JOptionPane.ERROR_MESSAGE);
            return false;
        }

        // Validate each result from portScanResults array.
        JSONArray jsonPortScanResults = fileData.getJSONArray("portScanResults");

        for (int i = 0; i < jsonPortScanResults.length(); i++) {
            JSONObject jsonPortScanResult = jsonPortScanResults.getJSONObject(i);

            // Check for required keys in each object.
            try {
                ValidationUtils.validateRequiredKeys(new String[]{"portNumber", "protocol"}, jsonPortScanResult);
            } catch (MissingRequiredKeysException e) {
                JOptionPane.showMessageDialog(panel, "Missing required field(s) in port scan results array at index " + i + ".", "Missing Field Error", JOptionPane.ERROR_MESSAGE);
                return false;
            }

            // Validate portNumber is an integer.
            try {
                ValidationUtils.validateInstanceInteger(jsonPortScanResult.get("portNumber"));
            } catch (InvalidVariableInstanceException e) {
                JOptionPane.showMessageDialog(panel, "Port number field is not an integer at index " + i + ".", "Data Field Error", JOptionPane.ERROR_MESSAGE);
                return false;
            }

            // Validate portNumber range.
            try {
                ValidationUtils.validatePortNumberRange(jsonPortScanResult.getInt("portNumber"));
            } catch (InvalidPortNumberRangeException e) {
                JOptionPane.showMessageDialog(panel, "Port number value falls out of acceptable range at index " + i + ".", "Data Field Error", JOptionPane.ERROR_MESSAGE);
                return false;
            }

            // Validate protocol is a string.
            try {
                ValidationUtils.validateInstanceString(jsonPortScanResult.get("protocol"));
            } catch (InvalidVariableInstanceException e) {
                JOptionPane.showMessageDialog(panel, "Protocol field is not a string at index " + i + ".", "Data Field Error", JOptionPane.ERROR_MESSAGE);
                return false;
            }

            // Validate protocol presence.
            try {
                ValidationUtils.validateFieldPresence(jsonPortScanResult.getString("protocol"));
            } catch (BlankFieldException e) {
                JOptionPane.showMessageDialog(panel, "Protocol field is blank at index " + i + ".", "Data Field Error", JOptionPane.ERROR_MESSAGE);
                return false;
            }

            // Validate protocol corresponds to correct port number.
            try {
                ValidationUtils.validatePortCorrespondsToProtocol(jsonPortScanResult.getInt("portNumber"), jsonPortScanResult.getString("protocol"));
            } catch (InvalidPortProtocolRelationshipException e) {
                JOptionPane.showMessageDialog(panel, "Protocol does not correspond to the port number at index " + i + ".", "Data Field Error", JOptionPane.ERROR_MESSAGE);
                return false;
            }
        }

        // All checks successful.
        return true;
    }

    /**
     * Parses subnet scan results from the JSON array and converts them into a
     * list of {@code SubnetScanResult} objects.
     *
     * @return an {@code ArrayList} of {@code SubnetScanResult} objects parsed
     * from the JSON data
     */
    private ArrayList<SubnetScanResult> parseSubnetScanResultsArray() {
        // Create the ArrayList of SubnetScanResult objects.
        ArrayList<SubnetScanResult> subnetScanResults = new ArrayList<>();

        // Parse the data from the file to a JSONArray.
        JSONArray jsonSubnetScanResults = fileData.getJSONArray("subnetScanResults");

        // Loop through each JSONObject in the JSONArray.
        for (int i = 0; i < jsonSubnetScanResults.length(); i++) {
            JSONObject jsonSubnetScanResult = jsonSubnetScanResults.getJSONObject(i);

            // Parse the individual data values from the JSONObject, and use them to create a new SubnetScanResult object.
            SubnetScanResult subnetScanResult = new SubnetScanResult(
                    jsonSubnetScanResult.getString("ipAddress")
            );
            // Append the SubnetScanResult to the list.
            subnetScanResults.add(subnetScanResult);
        }

        return subnetScanResults;
    }

    /**
     * Imports subnet scan results by extracting the network range, timeout, and
     * a list of subnet scan results from the JSON data, then passes the data to
     * the listener.
     */
    private void importSubnetScanData() {
        // Create the temporary holder variables.
        String networkRange = fileData.getString("networkRange");
        int timeout = fileData.getInt("timeout");
        ArrayList<SubnetScanResult> subnetScanResults = parseSubnetScanResultsArray();

        // Call the listener to indicate that a subnet scan has been imported.
        listener.onSubnetScanResultsImported(networkRange, timeout, subnetScanResults);
    }

    /**
     * Parses device ping results from the JSON array and converts them into a
     * list of {@code DevicePingResult} objects.
     *
     * @return an {@code ArrayList} of {@code DevicePingResult} objects parsed
     * from the JSON data
     */
    private ArrayList<DevicePingResult> parseDevicePingResults() {
        // Create the ArrayList of DevicePingResult objects.
        ArrayList<DevicePingResult> devicePingResults = new ArrayList<>();

        // Parse the data from the file to a JSONArray.
        JSONArray jsonDevicePingResults = fileData.getJSONArray("devicePingResults");

        // Loop through each JSONObject in the JSONArray.
        for (int i = 0; i < jsonDevicePingResults.length(); i++) {
            JSONObject jsonDevicePingResult = jsonDevicePingResults.getJSONObject(i);

            // Parse the individual data values from the JSONObject, and use them to create a new DevicePingResult object.
            DevicePingResult devicePingResult = new DevicePingResult(
                    jsonDevicePingResult.getInt("roundTripTime"),
                    jsonDevicePingResult.getBoolean("successfulPing"),
                    jsonDevicePingResult.getDouble("packetLoss")
            );
            // Append the SubnetScanResult to the list.
            devicePingResults.add(devicePingResult);
        }

        return devicePingResults;
    }

    /**
     * Imports device ping scan results by extracting the target IP address,
     * ping interval, number of pings, continuous pinging flag, and the list of
     * device ping results from the JSON data, then passes the data to the
     * listener.
     */
    private void importDevicePingData() {
        // Create the temporary holder variables.
        String ipAddress = fileData.getString("ipAddress");
        int pingInterval = fileData.getInt("pingInterval");
        int numOfPings = fileData.getInt("numOfPings");
        boolean continuousPinging = fileData.getBoolean("continuousPinging");
        ArrayList<DevicePingResult> devicePingResults = parseDevicePingResults();

        // Call the listener to indicate that a device ping has been imported.
        listener.onDevicePingResultsImported(ipAddress, pingInterval, numOfPings, continuousPinging, devicePingResults);
    }

    /**
     * Parses port scan results from the JSON array and converts them into a
     * list of {@code PortScanResult} objects.
     *
     * @return an {@code ArrayList} of {@code PortScanResult} objects parsed
     * from the JSON data
     */
    private ArrayList<PortScanResult> parsePortScanResults() {
        // Create the ArrayList of PortScanResult objects.
        ArrayList<PortScanResult> portScanResults = new ArrayList<>();

        // Parse the data from the file to a JSONArray.
        JSONArray jsonPortScanResults = fileData.getJSONArray("portScanResults");

        // Loop through each JSONObject in the JSONArray.
        for (int i = 0; i < jsonPortScanResults.length(); i++) {
            JSONObject jsonPortScanResult = jsonPortScanResults.getJSONObject(i);

            // Parse the individual data values from the JSONObject, and use them to create a new PortScanResult object.
            PortScanResult portScanResult = new PortScanResult(
                    jsonPortScanResult.getInt("portNumber"),
                    jsonPortScanResult.getString("protocol")
            );
            // Append the SubnetScanResult to the list.
            portScanResults.add(portScanResult);
        }

        return portScanResults;
    }

    /**
     * Imports port scan scan results by extracting the target IP address, port
     * range, timeout, and the list of port scan results from the JSON data,
     * then passes the data to the listener.
     */
    private void importPortScanData() {
        // Create the temporary holder variables.
        String ipAddress = fileData.getString("ipAddress");
        int bottomRangePort = fileData.getInt("bottomRangePort");
        int topRangePort = fileData.getInt("topRangePort");
        int timeout = fileData.getInt("timeout");
        ArrayList<PortScanResult> portScanResults = parsePortScanResults();

        // Call the listener to indicate that a port scan has been imported.
        listener.onPortScanResultsImported(ipAddress, bottomRangePort, topRangePort, timeout, portScanResults);
    }
}
