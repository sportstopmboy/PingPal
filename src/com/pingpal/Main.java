package com.pingpal;

import com.pingpal.ui.HomePage;
import static java.awt.EventQueue.invokeLater;
import java.io.IOException;
import javax.swing.JOptionPane;
import javax.swing.JPanel;

/**
 * The {@code Main} class is the fundamental class which is responsible for
 * loading and executing the entire program.
 */
public class Main {

    public static void main(String[] args) throws IOException, InterruptedException {

        // Create a Home Page object instance.
        HomePage homePage = new HomePage();

        try {
            // Display the form.
            invokeLater(() -> {
                homePage.setVisible(true);
            });
        } catch (Exception e) {
            // Catch any error that may occur and display a pane to inform the user of the error.
            JOptionPane.showMessageDialog(new JPanel(), "Encountered error when launching PingPal.", "Runtime Error", JOptionPane.ERROR_MESSAGE);
        }
    }
}