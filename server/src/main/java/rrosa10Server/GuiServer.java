package rrosa10Server;

import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.text.Font;
import javafx.stage.Stage;

import java.io.Serializable;
import java.net.URL;
import java.util.function.Consumer;

/**
 * GuiServer - JavaFX application that manages the intro and status scenes
 * and starts/stops the Server backend.
 *
 * Responsibilities:
 * - Bootstraps the JavaFX UI (loads fonts, FXML scenes, and stylesheets).
 * - Keeps references to the scene controllers so UI updates can be performed from server callbacks.
 * - Starts and stops the Server instance and routes simple textual/Serializable messages
 *   into the UI via the ServerScreenController.logEvent() method.
 */
public class GuiServer extends Application {

    private Stage primaryStage;

    private Scene introScene;
    private Scene statusScene;

    private ServerScreenController serverScreenController;
    private ServerIntroController serverIntroController;
    private Server server;

    public static void main(String[] args) {
        launch(args);
    }

    /**
     * Responsible for:
     * - Loading optional fonts.
     * - Loading the intro and status FXML files.
     * - Wiring controller references back to this application.
     * - Creating Scenes and applying a common stylesheet.
     */
    @Override
    public void start(Stage stage) throws Exception {
        this.primaryStage = stage;

        try {
            // Attempt to load a custom font (optional). Failure is ignored.
            URL fontUrl = getClass().getResource("/CITYLIGL.TFF/fonts/.otf");
            if (fontUrl != null) {
                Font.loadFont(fontUrl.toExternalForm(), 12);
            }
        } catch (Exception ignore) {
        }

        // Intro scene: load FXML, grab its controller, and build a Scene with stylesheet.
        URL introFxml = getClass().getResource("/FXML/ServerIntro.fxml");
        if (introFxml == null) {
            throw new IllegalStateException("Cannot find /FXML/ServerIntro.fxml on the classpath");
        }
        FXMLLoader introLoader = new FXMLLoader(introFxml);
        Parent introRoot = introLoader.load();
        serverIntroController = introLoader.getController();
        // Give the intro controller a reference back to this application for callbacks (start server).
        serverIntroController.setMainApp(this);
        introScene = new Scene(introRoot, 900, 600);
        introScene.getStylesheets().add("/styles/server.css");

        // Status scene: load FXML, grab its controller, and build a Scene with stylesheet.
        URL statusFxml = getClass().getResource("/FXML/ServerScreen.fxml");
        if (statusFxml == null) {
            throw new IllegalStateException("Cannot find /FXML/ServerScreen.fxml on the classpath");
        }
        FXMLLoader statusLoader = new FXMLLoader(statusFxml);
        Parent statusRoot = statusLoader.load();
        serverScreenController = statusLoader.getController();
        // Give the status controller a reference back to this application.
        serverScreenController.setMainApp(this);
        statusScene = new Scene(statusRoot, 810, 810);
        statusScene.getStylesheets().add("/styles/server.css");

        // Configure and show the primary stage, starting on the intro scene.
        primaryStage.setTitle("3 Card Poker Server");
        primaryStage.setScene(introScene);
        primaryStage.setResizable(false);
        primaryStage.show();
    }

    /**
     * Start the server on the provided port and switch to the status scene.
     *
     * Behavior:
     * - Creates a Server instance with a simple callback that forwards messages to the UI controller.
     * - Notifies the ServerScreenController of the running state so it can update labels/logs.
     * - Switches the primary stage to the status scene.
     */
    public boolean startServerOnPort(int port) {
        try {
            Consumer<Serializable> callback = msg -> {
                if (serverScreenController != null && msg != null) {
                    serverScreenController.logEvent(msg.toString());
                }
            };

            server = new Server(port, callback);

            if (serverScreenController != null) {
                serverScreenController.setServerRunning(port);
                serverScreenController.logEvent("Server started on port " + port);
                serverScreenController.updateClientCount(0);
            }

            // Show status scene once server starts
            primaryStage.setScene(statusScene);
            primaryStage.show();
            return true;
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
    }

    /**
     * Stop the running server (if any), notify the UI, and return to the intro scene.
     *
     * Safety:
     * - Attempts to shutdown the Server instance and clears the reference.
     * - Updates the ServerScreenController status labels/logs to indicate stopped state.
     */
    public void stopServer() {
        try {
            if (server != null) {
                server.shutdown();
                server = null;
            }
            if (serverScreenController != null) {
                serverScreenController.setServerStopped();
                serverScreenController.logEvent("Server stopped.");
                serverScreenController.updateClientCount(0);
            }
            // Return the UI to the intro scene so the user can start it again.
            primaryStage.setScene(introScene);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}