package rrosa10Client;

import java.util.ArrayList;
import java.util.List;

import javafx.animation.PauseTransition;
import javafx.application.Application;
import javafx.application.Platform;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.text.Font;
import javafx.stage.Stage;
import javafx.util.Duration;
import model.PokerInfo;

/**
 * GuiServer (client-side) application launcher for the 3 Card Poker client UI.
 *
 * Responsibilities:
 * - Bootstraps the JavaFX application and loads the three main scenes:
 *     1) Start screen (connection / port entry)
 *     2) Poker game screen (main gameplay)
 *     3) Result screen (showing results after a round)
 * - Provides accessors for controllers so other parts of the app can interact with the UI.
 * - Manages simple scene switching and a small delayed transition to the result scene.
 *
 */
public class GuiServer extends Application {

    private Stage primaryStage;

    private Scene startScene;
    private Scene clientPokerGame;
    private Scene resultScene;
    
    private Client client;

    private PokerScreenController pokerScreenController;
    private ResultScreenController resultScreenController;
    private final List<String> bufferedLogs = new ArrayList<>();

    
    // 2-second PauseTransition used to delay showing the result screen so the UI can display animations/messages.
    PauseTransition pause = new PauseTransition(Duration.seconds(4));

    public static void main(String[] args) {
        launch(args);
    }

    /**
     * JavaFX entry point - initialize and show the primary stage.
     * Loads FXML files, extracts controllers, and prepares Scenes with stylesheets.
     */
    @Override
    public void start(Stage stage) throws Exception {
        this.primaryStage = stage;

        // Load custom fonts (optional). Fonts must exist under resources/fonts/.
        Font.loadFont(getClass().getResourceAsStream("/fonts/OUTRUNFUTURE.otf"), 12);
        Font.loadFont(getClass().getResourceAsStream("/fonts/PostNoBillsColombo-SemiBold.ttf"), 12);

        // -- Start screen --
        FXMLLoader loader = new FXMLLoader(getClass().getResource("/FXML/StartScreenFXML.fxml"));
        Parent root = loader.load();
        StartScreenController startController = loader.getController();
        startController.setMainApp(this);

        startScene = new Scene(root, 1238, 861);
        startScene.getStylesheets().add("/styles/style1.css");

        // -- Poker screen --
        FXMLLoader loader2 = new FXMLLoader(getClass().getResource("/FXML/PokerScreenFXML.fxml"));
        Parent root2 = loader2.load();
        pokerScreenController = loader2.getController();
        // give controller a back-reference so it can call application-level methods if needed
        pokerScreenController.setMainApp(this);
        clientPokerGame = new Scene(root2, 1238, 861);
        clientPokerGame.getStylesheets().add("/styles/pokerScreenStyle.css");

        // -- Result screen --
        FXMLLoader loader3 = new FXMLLoader(getClass().getResource("/FXML/ResultScreenFXML.fxml"));
        Parent root3 = loader3.load();
        resultScreenController = loader3.getController();
        resultScreenController.setMainApp(this);
        resultScene = new Scene(root3, 600, 400);

        // Finalize and display the stage
        primaryStage.setTitle("3 Card Poker");
        primaryStage.setScene(startScene);
        primaryStage.setResizable(false);
        primaryStage.show();
    }

    /**
     * Accessor for the poker screen controller used by other classes.
     */
    public PokerScreenController getPokerScreenController() {
        return pokerScreenController;
    }

    /**
     * Accessor for the result screen controller used by other classes (for showing results).
     */
    public ResultScreenController getResultScreenController() {
        return resultScreenController;
    }

    /**
     * Switch to the client poker scene and give it the client instance and initial PokerInfo payload.
     *
     * param client        the Client instance responsible for network communication
     * param firstInfo     the initial PokerInfo object (welcome or initial state) to populate the UI
     */
    public void switchToClient(Client client, PokerInfo firstInfo) {
        // store the active client so shutdown() can close it
        this.client = client;

        if (pokerScreenController != null) {
            pokerScreenController.setClient(client, firstInfo);
        }
        primaryStage.setScene(clientPokerGame);
        primaryStage.show();

        // flush buffered logs now that poker screen is active
        flushBufferedLogsToPokerScreen();
    }

    /**
     * Show the poker (gameplay) screen and start a new game on the poker screen controller.
     * This will also ensure the stage is visible.
     */
    public void showPokerScreen() {
        primaryStage.setScene(clientPokerGame);
        pokerScreenController.startNewGame();
        primaryStage.show();
    }

    /**
     * Show the result screen after a short delay. The delay allows on-screen animations or transitions
     * to complete before the result screen replaces the poker screen.
     *
     * param netAmount the net amount (win/loss) to display on the result screen
     */
    public void showResultScreen(int netAmount) {
        // Configure the pause to run only when this method is called.
        pause.setOnFinished(event -> {
            if (resultScreenController != null) {
                resultScreenController.setResult(netAmount);
            }
            primaryStage.setScene(resultScene);
            primaryStage.show();
            System.out.println("Delayed action performed!");
        });
        pause.play();
    }
    
    public synchronized void bufferLogEntries(List<String> entries) {
        if (entries == null) return;
        bufferedLogs.addAll(entries);
    }

    public synchronized List<String> drainBufferedLogs() {
        List<String> copy = new ArrayList<>(bufferedLogs);
        bufferedLogs.clear();
        return copy;
    }

    /**
     * If the PokerScreenController exists, push any buffered logs into it on the FX thread.
     */
    public void flushBufferedLogsToPokerScreen() {
        PokerScreenController psc = getPokerScreenController();
        if (psc == null) return;
        List<String> logs = drainBufferedLogs();
        if (logs.isEmpty()) return;
        Platform.runLater(() -> psc.pushInitialLogs(logs));
    }
    
    public void shutdown() {
        System.out.println("[GUI] shutdown() called");
        if (client != null) {
            System.out.println("[GUI] closing client...");
            client.close();
            try { client.join(500); } catch (InterruptedException e) { Thread.currentThread().interrupt(); }
            System.out.println("[GUI] client join completed");
            client = null;
        } else {
            System.out.println("[GUI] no client to close");
        }
        System.out.println("[GUI] calling Platform.exit()");
        Platform.exit();
    }
    
    
}