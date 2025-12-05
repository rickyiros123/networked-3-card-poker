package rrosa10Server;

import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;

/**
 * Controller for the server intro screen.
 *
 * Responsibilities:
 * - Read the port number entered by the user.
 * - Validate the port value (presence, numeric, range).
 * - Request the GuiServer to start the Server on the validated port.
 * - Update the statusLabel with validation errors or failure messages.
 */
public class ServerIntroController {

    @FXML private TextField portField;
    @FXML private Button startButton;
    @FXML private Label statusLabel;

    private GuiServer mainApp;

    /**
     * Provide a reference to the application coordinator so this controller can
     * request server start/stop actions or switch scenes.
     *
     * param mainApp the GuiServer application instance
     */
    public void setMainApp(GuiServer mainApp) {
        this.mainApp = mainApp;
    }

    /**
     * Handler invoked when the Start button is clicked.
     *
     * Behavior:
     * - Reads and trims the text from the portField.
     * - Validates that the port is present, is an integer, and falls within the
     *   valid TCP port range (1..65535).
     * - If validation passes, delegates to GuiServer.startServerOnPort(port).
     * - Updates statusLabel with any validation or startup failure messages so the user sees feedback.
     */
    @FXML
    private void onStartServerClicked() {
        String portText = portField.getText().trim();
        int port;

        if (portText.isEmpty()) {
            statusLabel.setText("Please enter a port number.");
            return;
        }

        try {
            port = Integer.parseInt(portText);
        } catch (NumberFormatException e) {
            statusLabel.setText("Port must be a number.");
            return;
        }

        if (port < 1 || port > 65535) {
            statusLabel.setText("Port must be between 1 and 65535.");
            return;
        }

        if (mainApp != null) {
            boolean started = mainApp.startServerOnPort(port);
            if (!started) {
                statusLabel.setText("Failed to start server on port " + port + ".");
            }
        }
    }
}