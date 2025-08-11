package com.pingpal.tcpmessage.connect;

import java.awt.Color;
import static java.awt.EventQueue.invokeLater;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.ConnectException;
import java.net.InetAddress;
import java.net.Socket;
import java.net.SocketTimeoutException;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import javax.swing.JOptionPane;
import javax.swing.JTextPane;
import javax.swing.text.BadLocationException;
import javax.swing.text.Style;
import javax.swing.text.StyleConstants;
import javax.swing.text.StyledDocument;

/**
 * Establishes a TCP connection to a server, receives and sends messages, and
 * formats the communication in a styled text pane.
 * <p>
 * This class establishes a connection to a TCP server on a specified port, and
 * enables message exchange using a JTextPane.
 * </p>
 */
public class TCPMessageConnect {

    // Color constants for different message types.
    private final Color SUCCESS_COLOR = new Color(0, 204, 0);
    private final Color ERROR_COLOR = new Color(255, 51, 0);
    private final Color MESSAGE_COLOR = new Color(45, 45, 45);
    private final Color DATE_TIME_COLOR = new Color(26, 39, 107);
    private final Color HOSTNAME_COLOR = new Color(113, 89, 138);

    // Text styling for message formatting.
    private Style dateTimeStyle;
    private Style hostnameStyle;
    private Style messageStyle;
    private Style errorStyle;
    private Style successStyle;

    // UI components.
    private JTextPane txpTCPMessageConnect;
    private StyledDocument doc;

    // Flag to indicate if a stop has been requested for the scan.
    private boolean stopRequested = false;

    // Socket variables.
    private Socket socket;

    // Reader and writer to handle sending and receiving messages.
    private BufferedReader in;
    private PrintWriter out;

    // The server IP address the program will try to connect to.
    private String ipAddress;

    // The server port the socket will try to connect to.
    private int port;

    /**
     * Constructs a new {@code TCPMessageConnect} instance with the specified IP
     * address, port, and text pane for messages.
     *
     * @param ipAddress the IP address to which the socket will attempt to
     * connect to
     * @param port the port on which to listen for connections
     * @param txpTCPMessageConnect the JTextPane used to display the messages
     */
    public TCPMessageConnect(String ipAddress, int port, JTextPane txpTCPMessageConnect) {
        this.ipAddress = ipAddress;
        this.port = port;
        this.txpTCPMessageConnect = txpTCPMessageConnect;

        // Extract the styled document from the text pane. 
        doc = txpTCPMessageConnect.getStyledDocument();
        // Initialise the different message styles.
        setStyles();
    }

    /**
     * Starts the client socket, attempts to connect to a server socket, and
     * then continuously receives and displays messages.
     * <p>
     * If no client connects within 60 seconds, a timeout occurs, the socket is
     * closed, and an error message is displayed.
     * </p>
     */
    public void start() {
        try {
            // Clear any data that is currently displayed in the text pane.
            doc.remove(0, doc.getLength());
            
            // Write to the text pane to indicate the program is trying to establish a connection.
            updateTextPane("Trying to establish a connection to " + ipAddress + ":" + port + ".\n", messageStyle);

            // Attempt to connect to a device on the specified IP and port.
            socket = new Socket(ipAddress, port);
            // Set 60000ms (60s) accept timeout.
            socket.setSoTimeout(60_000);
            // Write to the text pane to indicate that connection to server is successful.
            updateTextPane("Connected to chat server at " + ipAddress + ":" + port + ".\n", successStyle);

            // Initialize the persistent reader and writer.
            in = new BufferedReader(new InputStreamReader(socket.getInputStream()));
            out = new PrintWriter(socket.getOutputStream(), true);

            // Enter receive loop.
            receiveMessages();

            // Throw an error if the server refuses connection.
        } catch (ConnectException e) {
            // Stop the program.
            requestStop();
            // Write to the pane that the server has refused the connection request.
            updateTextPane("Server refused connection. Closing socket.\n", errorStyle);
            
            // Throw an error if no clinet connected within 60s.
        } catch (SocketTimeoutException e) {
            // Stop the program.
            requestStop();
            // Write to the text pane to indicate no client connected within 60s.
            updateTextPane("No client connected within 60 seconds. Closing socket.\n", errorStyle);
            
            // General I/O error, but its most common use case is when the client disconnects.
        } catch (IOException e) {
            // Write to the text pane to indicate the connection closed.
            updateTextPane("Connection closed.\n", errorStyle);
            
            // Catch exceptions that may occur when trying to append a message to the text pane.
        } catch (BadLocationException ex) {
            // Display a JOptionPane to indicate such.
            JOptionPane.showMessageDialog(txpTCPMessageConnect.getParent(), "Error occurred during text pane update process.", "Text Pane Write Error", JOptionPane.ERROR_MESSAGE);
        }
    }

    /**
     * Sends a message to the connected client and echoes it locally with
     * timestamp and hostname formatting.
     *
     * @param message the text to send
     */
    public void sendMessage(String message) {
        try {
            // Get and format the current date and time.
            LocalDateTime now = LocalDateTime.now();
            DateTimeFormatter formatter = DateTimeFormatter.ofPattern("dd-MM-yy HH:mm:ss");
            String formattedDateTime = now.format(formatter);

            // Get the host name of the local device.
            String hostname = InetAddress.getLocalHost().getHostName();

            // Write the formatted message to the text pane.
            updateTextPane(formattedDateTime + " ", dateTimeStyle);
            updateTextPane("[" + hostname + "] ", hostnameStyle);
            updateTextPane("> " + message + "\n", messageStyle);

            // Print the message for the connected device.
            out.println(formattedDateTime + " [" + hostname + "] > " + message);
            
            // General I/O error, but its most common use case is when the server disconnects.
        } catch (IOException e) {
            // Write to the text pane to indicate the server disconnected.
            updateTextPane("Server disconnected.\n", errorStyle);
        }
    }

    /**
     * Continuously receives incoming messages from the client, formats them,
     * and appends them to the text pane.
     */
    private void receiveMessages() {
        try {
            // Loop until the server disconnects or the user chooses to stop listening for messages and disconnect themself.
            while (!stopRequested) {
                // Declare the variable to hold the message the server sends.
                String message;
                // Loop to keep checking whether the server has sent a message.
                while ((message = in.readLine()) != null) {
                    // Check whether the message came via a PingPal connection, by checking format.
                    if (message.contains("[") && message.contains("]") && message.contains(">")) {
                        // Write the formatted message to the text pane.
                        updateTextPane(message.substring(0, message.indexOf("[")), dateTimeStyle);
                        updateTextPane(message.substring(message.indexOf("["), message.indexOf(">")), hostnameStyle);
                        updateTextPane(message.substring(message.indexOf(">")) + "\n", messageStyle);
                    } else {
                        // Indicate that the message does not come from a PingPal connection, however, still display it.
                        updateTextPane("Not connected to a device via PingPal. However, the message reads:\n", errorStyle);
                        updateTextPane(message + "\n", messageStyle);
                    }
                }
            }
            
            // General I/O error, but it's most common use case is when the server disconnects.
        } catch (IOException e) {
            // Write to the text pane to indicate the server disconnected.
            updateTextPane("Server disconnected.\n", errorStyle);
        }
    }

    /**
     * Appends a styled message to the JTextPane safely on the Event Dispatch
     * Thread.
     *
     * @param message the message text to append
     * @param style the Style to apply to the message
     */
    private void updateTextPane(String message, Style style) {
        invokeLater(() -> {
            try {
                // Append the passed message in the passed style.
                doc.insertString(doc.getLength(), message, style);
                
                // Catch exceptions that may occur when trying to append a message to the text pane.
            } catch (BadLocationException e) {
                JOptionPane.showMessageDialog(txpTCPMessageConnect.getParent(), "Error occurred during text pane update process.", "Text Pane Write Error", JOptionPane.ERROR_MESSAGE);
            }
        });
    }

    /**
     * Defines and registers custom styles used for formatting different types
     * of messages in the document.
     */
    private void setStyles() {
        // Initialise and register the date & time style.
        dateTimeStyle = doc.addStyle("dateTimeStyle", null);
        StyleConstants.setForeground(dateTimeStyle, DATE_TIME_COLOR);

        // Initialise and register the hostname style.
        hostnameStyle = doc.addStyle("hostnameStyle", null);
        StyleConstants.setForeground(hostnameStyle, HOSTNAME_COLOR);

        // Initialise and register the message style.
        messageStyle = doc.addStyle("messageStyle", null);
        StyleConstants.setForeground(messageStyle, MESSAGE_COLOR);

        // Initialise and register the error style.
        errorStyle = doc.addStyle("errorStyle", null);
        StyleConstants.setForeground(errorStyle, ERROR_COLOR);

        // Initialise and register the success style.
        successStyle = doc.addStyle("successStyle", null);
        StyleConstants.setForeground(successStyle, SUCCESS_COLOR);

        // Set the styled document of the text pane.
        txpTCPMessageConnect.setStyledDocument(doc);
    }

    /**
     * Requests that the pinging process stop.
     * <p>
     * This sets the {@code stopRequested} flag to true, so that the ping loop
     * in {@code start()} will terminate early, updates the text pane to
     * indicate the connection is being closed, and close both the client socket
     * and server socket.
     * </p>
     */
    public void requestStop() {
        // Set the stopRequested flag to true.
        stopRequested = true;
        
        // Write to the text pane to indicate that the sockets are being closed.
        updateTextPane("Exiting TCP Message and closing socket.\n", errorStyle);
        try {
            // Close the socket.
            if (socket != null && !socket.isClosed()) {
                socket.close();
            }
            
            // General I/O error, but it's most common use case is when the socket is already closed.
        } catch (IOException e) {
            updateTextPane("Socket closed already.\n", errorStyle);
        }
    }

    /**
     * Checks whether a client is currently connected.
     *
     * @return {@code true} if the client socket is connected; otherwise
     * {@code false}
     */
    public boolean deviceConnected() {
        return socket != null && socket.isConnected();
    }

    /**
     * Retrieves the full contents of the text pane's document.
     *
     * @return a {@code String} containing the entire text pane output
     * @throws BadLocationException if the text cannot be accessed
     */
    public String getTextPaneContents() throws BadLocationException {
        return doc.getText(0, doc.getLength());
    }
}
