package rrosa10Client;


import javafx.fxml.FXML;

import javafx.application.Platform;
import javafx.geometry.Insets;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;
import javafx.scene.paint.CycleMethod;
import javafx.scene.paint.LinearGradient;
import javafx.scene.paint.Stop;
import javafx.scene.text.Font;
import javafx.scene.text.Text;
import model.PokerInfo;

/**
 * Controller for the start/connection screen of the 3 Card Poker client.
 *
 * Responsibilities:
 * - Initialize title styling.
 * - Validate host/port entered by the user.
 * - Create and start a Client instance to connect to the server.
 * - Route incoming PokerInfo objects to the appropriate screen/controller.
 *s
 */
public class StartScreenController {

    @FXML private BorderPane root;
    @FXML private VBox titleBox;
    @FXML private Text gradientText;
    @FXML private Label instruction;
    @FXML private TextField portText;
    @FXML private TextField ipText;
    @FXML private Button connectButton;

    private GuiServer guiServer;
    private Client client;

    /**
     * FXML initialize method.
     * - Configure the title font and gradient fill.
     * - Apply padding to the title box.
     *
     */
    @FXML
    private void initialize() {
        gradientText.setFont(Font.font("outrun future", 100));

        LinearGradient lg = new LinearGradient(
            0, 1, 0, 0,   // startX, startY, endX, endY
            true,         // proportional
            CycleMethod.NO_CYCLE,
            new Stop(0, Color.web("#C200F0")),
            new Stop(0.8, Color.web("#FF721F")),
            new Stop(1, Color.web("#FF5900"))
        );
        gradientText.setFill(lg);
        titleBox.setPadding(new Insets(40, 0, 0, 0));
    }

    /**
     * Called when the Connect button is clicked.
     *
     * Behavior:
     * - Validate the host and port fields.
     * - Disable inputs and show a connecting message.
     * - Create a Client and start it.
     * - The client's callback routes PokerInfo messages: WELCOME -> switch to the game screen
     */
    @FXML
    private void onConnectClicked() {
        String host = ipText.getText().trim();
        String portStr = portText.getText().trim();
        int port;

        if (host.isEmpty()) {
            instruction.setText("Enter a server IP or hostname");
            return;
        }

        try {
            port = Integer.parseInt(portStr);
        } catch (NumberFormatException e) {
            instruction.setText("Port must be a number");
            return;
        }

        if (port < 1 || port > 65535) {
            instruction.setText("Port must be between 1 and 65535");
            return;
        }

        instruction.setText("Connecting to " + host + ":" + port + "...");
        connectButton.setDisable(true);
        ipText.setDisable(true);
        portText.setDisable(true);

        // create and start Client directly
        client = new Client(host, port, incoming -> {
            // Ensure all UI updates run on JavaFX thread
            Platform.runLater(() -> {
                System.out.println("Client callback, incoming = " + incoming);

                if (incoming instanceof PokerInfo) {
                    PokerInfo pokerInfo = (PokerInfo) incoming;
                    System.out.println("Received PokerInfo type = " + pokerInfo.getType());
                    switch (pokerInfo.getType()) {
                        case WELCOME:
                            instruction.setText("Connected! Switching to game...");
                            guiServer.switchToClient(client, pokerInfo);
                            // after switching to the poker screen, flush any buffered logs
                            guiServer.flushBufferedLogsToPokerScreen();
                            break;

                        case GAME_DEAL:
                        case GAME_RESULT:
                        case CHAT:
                            PokerScreenController psc = guiServer.getPokerScreenController();
                            if (psc != null) {
                                psc.handlePokerInfo(pokerInfo);
                            } else {
                                System.out.println("PokerScreenController is null when handling " + pokerInfo.getType());
                            }
                            break;

                        case LOG:
                            // If poker screen is already present, deliver it immediately.
                            PokerScreenController pscLog = guiServer.getPokerScreenController();
                            if (pscLog != null) {
                                pscLog.handlePokerInfo(pokerInfo); // PokerScreenController already handles LOG
                            } else {
                                // otherwise buffer it in the main app (to be flushed when poker screen appears)
                                guiServer.bufferLogEntries(pokerInfo.getLog());
                            }
                            break;

                        default:
                            System.out.println("Unhandled PokerInfo type on start screen: " + pokerInfo.getType());
                            break;
                    }

                } else {
                    instruction.setText("Received non-PokerInfo: " + incoming);
                }
            });
        });

        client.start();

        instruction.setText("Connecting...");
    }

    /**
     * Cancel/reset the connection form.
     * Clears input fields and re-enables the connect controls.
     */
    @FXML
    public void onCancelClicked() {
        portText.clear();
        ipText.clear();
        instruction.setText("");
        connectButton.setDisable(false);
        ipText.setDisable(false);
        portText.setDisable(false);
    }

    /**
     * Called by GuiServer to provide the application reference.
     * The controller uses guiServer to switch scenes when the connection succeeds.
     *
     */
    public void setMainApp(GuiServer app) {
        this.guiServer = app;
    }
    
    
}