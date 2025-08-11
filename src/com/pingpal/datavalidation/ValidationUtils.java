package com.pingpal.datavalidation;

import com.pingpal.exceptions.imports.InvalidNumOfPingsException;
import com.pingpal.exceptions.imports.InvalidPacketLossRangeException;
import com.pingpal.exceptions.imports.InvalidPingIntervalRangeException;
import com.pingpal.exceptions.imports.InvalidPortNumberRangeException;
import com.pingpal.exceptions.imports.InvalidPortProtocolRelationshipException;
import com.pingpal.exceptions.imports.InvalidRoundTripTimeException;
import com.pingpal.exceptions.imports.InvalidSuccessfulPingException;
import com.pingpal.exceptions.imports.InvalidTimeoutRangeException;
import com.pingpal.exceptions.imports.InvalidVariableInstanceException;
import com.pingpal.exceptions.imports.MissingRequiredKeysException;
import com.pingpal.exceptions.ui.BlankFieldException;
import com.pingpal.exceptions.ui.InvalidIPAddressException;
import com.pingpal.exceptions.ui.InvalidNetworkRangeException;
import com.pingpal.exceptions.ui.InvalidPortRangeException;
import com.pingpal.portscan.Protocols;
import java.awt.Color;
import org.json.JSONArray;
import org.json.JSONObject;

/**
 * The {@code ValidationUtils} class provides a set of static methods to perform
 * data validation for network scanning operations. It includes methods for
 * checking field presence, format and type validation of network ranges, IP
 * addresses, port numbers, ping intervals, timeouts, and relationships between
 * ports and protocols.
 * <p>
 * In addition, this class holds several constants such as color codes and
 * regular expression patterns, as well as minimum/maximum acceptable values for
 * various parameters.
 * </p>
 */
public class ValidationUtils {

    /**
     * A {@code Protocols} instance used to verify that a port number
     * corresponds to the correct protocol.
     */
    private static Protocols protocols = new Protocols();

    // Color constants used for UI validation feedback.
    public final static Color ERROR_COLOR = new Color(250, 200, 200);
    public final static Color GRAYED_OUT_COLOR = new Color(184, 184, 184);
    public final static Color NORMAL_TEXT_COLOR = new Color(233, 247, 249);
    public final static Color SUCCESSFUL_SCAN_COLOR = new Color(0, 204, 0);
    public final static Color INTERRUPTED_SCAN_COLOR = new Color(255, 51, 0);

    // Regular expression patterns.
    public final static String NETWORK_RANGE_PATTERN = "^(?:(?:25[0-5]|2[0-4][0-9]|1[0-9][0-9]|[1-9][0-9]|[0-9])\\.){3}(?:25[0-5]|2[0-4][0-9]|1[0-9][0-9]|[1-9][0-9]|[0-9])\\/(?:[1-9]|[12]\\d|3[0-2])$";
    public final static String IP_ADDRESS_PATTERN = "^(?:(?:25[0-5]|2[0-4][0-9]|1[0-9][0-9]|[1-9][0-9]|[0-9])\\.){3}(?:25[0-5]|2[0-4][0-9]|1[0-9][0-9]|[1-9][0-9]|[0-9])$";

    // Range constants for validations.
    public final static int MIN_TIMEOUT = 100;
    public final static int MAX_TIMEOUT = 10_000;

    public final static int MIN_PING_INTERVAL = 100;
    public final static int MAX_PING_INTERVAL = 10_000;

    public final static int MIN_PINGS = 1;
    public final static int MAX_PINGS = 100;

    public final static int MIN_PORT = 1;
    public final static int MAX_PORT = 65535;

    // Private constructor to prevent instantiation.
    private ValidationUtils() {

    }

    /**
     * Validates that the provided object is present (i.e. not null), and if it
     * is a {@code String}, is not blank.
     *
     * @param obj the object to validate for presence
     * @throws BlankFieldException if the object is null, or if a
     * {@code String}, is blank
     */
    public static void validateFieldPresence(Object obj) throws BlankFieldException {
        if (obj == null) {
            throw new BlankFieldException("Field is blank.");
        }

        if (obj instanceof String && ((String) obj).isBlank()) {
            throw new BlankFieldException("Field is blank.");
        }
    }

    /**
     * Validates that the provided network range string matches the required
     * format.
     *
     * @param networkRange the network range string to validate (e.g.,
     * "192.168.0.0/24")
     * @throws InvalidNetworkRangeException if the network range does not match
     * the required pattern
     */
    public static void validateNetworkRange(String networkRange) throws InvalidNetworkRangeException {
        if (!networkRange.matches(NETWORK_RANGE_PATTERN)) {
            throw new InvalidNetworkRangeException("Invalid network range format.");

        }
    }

    /**
     * Validates that the provided IP address string matches the required
     * format.
     *
     * @param ipAddress the IP address string to validate (e.g., "192.168.0.1")
     * @throws InvalidIPAddressException if the IP address does not match the
     * required pattern
     */
    public static void validateIPAddress(String ipAddress) throws InvalidIPAddressException {
        if (!ipAddress.matches(IP_ADDRESS_PATTERN)) {
            throw new InvalidIPAddressException("Invalid IP address format.");

        }
    }

    /**
     * Validates that the bottom range port is not greater than the top range
     * port.
     *
     * @param bottomRange the starting port number of the range
     * @param topRange the ending port number of the range
     * @throws InvalidPortRangeException if the bottom range port is greater
     * than the top range port
     */
    public static void validatePortRange(int bottomRange, int topRange) throws InvalidPortRangeException {
        if (bottomRange > topRange) {
            throw new InvalidPortRangeException("Invalid port range.");

        }
    }

    /**
     * Validates that the provided JSON object contains all required top-level
     * keys.
     *
     * @param topLevelKeys an array of keys that must be present in the JSON
     * object
     * @param fileData the JSON object to validate
     * @throws MissingRequiredKeysException if any required key is missing from
     * the JSON object
     */
    public static void validateRequiredKeys(String[] topLevelKeys, JSONObject fileData) throws MissingRequiredKeysException {
        for (String topLevelKey : topLevelKeys) {
            if (!fileData.has(topLevelKey)) {
                throw new MissingRequiredKeysException("Missing one or more required keys");
            }
        }
    }

    /**
     * Validates that the provided object is an instance of {@code String}.
     *
     * @param obj the object to validate
     * @throws InvalidVariableInstanceException if the object is not a String
     */
    public static void validateInstanceString(Object obj) throws InvalidVariableInstanceException {
        if (!(obj instanceof String)) {
            throw new InvalidVariableInstanceException("Field is not a string.");
        }
    }

    /**
     * Validates that the provided object is an instance of {@code Integer}.
     *
     * @param obj the object to validate
     * @throws InvalidVariableInstanceException if the object is not an Integer
     */
    public static void validateInstanceInteger(Object obj) throws InvalidVariableInstanceException {
        if (!(obj instanceof Integer)) {
            throw new InvalidVariableInstanceException("Field is not an integer.");
        }
    }

    /**
     * Validates that the provided object is an instance of {@code Number}.
     *
     * @param obj the object to validate
     * @throws InvalidVariableInstanceException if the object is not a Number
     */
    public static void validateInstanceNumber(Object obj) throws InvalidVariableInstanceException {
        if (!(obj instanceof Number)) {
            throw new InvalidVariableInstanceException("Field is not a number.");
        }
    }

    /**
     * Validates that the provided object is an instance of {@code Boolean}.
     *
     * @param obj the object to validate
     * @throws InvalidVariableInstanceException if the object is not a Boolean
     */
    public static void validateInstanceBoolean(Object obj) throws InvalidVariableInstanceException {
        if (!(obj instanceof Boolean)) {
            throw new InvalidVariableInstanceException("Field is not a boolean.");
        }
    }

    /**
     * Validates that the provided object is an instance of {@code JSONArray}.
     *
     * @param obj the object to validate
     * @throws InvalidVariableInstanceException if the object is not a JSONArray
     */
    public static void validateInstanceJSONArray(Object obj) throws InvalidVariableInstanceException {
        if (!(obj instanceof JSONArray)) {
            throw new InvalidVariableInstanceException("Field is not a JSON array.");
        }
    }

    /**
     * Validates that the timeout value falls within the acceptable range.
     *
     * @param timeout the timeout value in milliseconds to validate
     * @throws InvalidTimeoutRangeException if the timeout is less than
     * {@code MIN_TIMEOUT} or greater than {@code MAX_TIMEOUT}
     */
    public static void validateTimeoutRange(int timeout) throws InvalidTimeoutRangeException {
        if (timeout < MIN_TIMEOUT || timeout > MAX_TIMEOUT) {
            throw new InvalidTimeoutRangeException("Invalid timeout range.");
        }
    }

    /**
     * Validates that the ping interval falls within the acceptable range.
     *
     * @param pingInterval the ping interval in milliseconds to validate
     * @throws InvalidPingIntervalRangeException if the ping interval is less
     * than {@code MIN_PING_INTERVAL} or greater than {@code MAX_PING_INTERVAL}
     */
    public static void validatePingInterval(int pingInterval) throws InvalidPingIntervalRangeException {
        if (pingInterval < MIN_PING_INTERVAL || pingInterval > MAX_PING_INTERVAL) {
            throw new InvalidPingIntervalRangeException("Invalid ping interval range.");
        }
    }

    /**
     * Validates that the number of pings falls within the acceptable range.
     *
     * @param numOfPings the number of pings to validate
     * @throws InvalidNumOfPingsException if the number of pings is less than
     * {@code MIN_PINGS} or greater than {@code MAX_PINGS}
     */
    public static void validateNumOfPingsRange(int numOfPings) throws InvalidNumOfPingsException {
        if (numOfPings < MIN_PINGS || numOfPings > MAX_PINGS) {
            throw new InvalidNumOfPingsException("Invalid number of pings.");
        }
    }

    /**
     * Validates that the round-trip time is within acceptable bounds.
     *
     * @param roundTripTime the measured round-trip time in milliseconds
     * @param pingInterval the ping interval used during measurement
     * @throws InvalidRoundTripTimeException if the round-trip time is negative
     * or exceeds the ping interval
     */
    public static void validateRoundTripTime(int roundTripTime, int pingInterval) throws InvalidRoundTripTimeException {
        if (roundTripTime < 0 || roundTripTime > pingInterval) {
            throw new InvalidRoundTripTimeException("Invalid round trip time.");
        }
    }

    /**
     * Validates the logic of a successful ping.
     * <p>
     * This method ensures that if the round-trip time is less than the ping
     * interval, then the ping must be marked as successful.
     * </p>
     *
     * @param successfulPing a boolean indicating if the ping was successful
     * @param roundTripTime the round-trip time measured in milliseconds
     * @param pingInterval the ping interval in milliseconds
     * @throws InvalidSuccessfulPingException if the logic is inconsistent
     */
    public static void validateSuccessfulPingLogic(boolean successfulPing, int roundTripTime, int pingInterval) throws InvalidSuccessfulPingException {
        if (roundTripTime < pingInterval && successfulPing == false) {
            throw new InvalidSuccessfulPingException("Invalid successful ping logic.");
        }
    }

    /**
     * Validates that the packet loss percentage is within the acceptable range.
     *
     * @param packetLoss the packet loss percentage to validate
     * @throws InvalidPacketLossRangeException if the packet loss is less than 0
     * or greater than 100
     */
    public static void validatePacketLossRange(double packetLoss) throws InvalidPacketLossRangeException {
        if (packetLoss < 0 || packetLoss > 100) {
            throw new InvalidPacketLossRangeException("Invalid packet loss range.");
        }
    }

    /**
     * Validates that the given port number falls within the acceptable range.
     *
     * @param portNumber the port number to validate
     * @throws InvalidPortNumberRangeException if the port number is less than
     * {@code MIN_PORT} or greater than {@code MAX_PORT}
     */
    public static void validatePortNumberRange(int portNumber) throws InvalidPortNumberRangeException {
        if (portNumber < MIN_PORT || portNumber > MAX_PORT) {
            throw new InvalidPortNumberRangeException("Invalid port number range.");
        }
    }

    /**
     * Validates that the specified protocol corresponds to the given port
     * number.
     * <p>
     * This method uses the {@code Protocols} instance to retrieve the expected
     * protocol for the port number. If the retrieved protocol does not match
     * the provided protocol, an exception is thrown.
     * </p>
     *
     * @param portNumber the port number for which to validate the protocol
     * @param protocol the protocol provided to validate
     * @throws InvalidPortProtocolRelationshipException if the protocol does not
     * match the expected value
     */
    public static void validatePortCorrespondsToProtocol(int portNumber, String protocol) throws InvalidPortProtocolRelationshipException {
        if (!protocols.getProtocolForPort(portNumber).equals(protocol)) {
            throw new InvalidPortProtocolRelationshipException("Port does not correspond to the protocol.");
        }
    }
}
