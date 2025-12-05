package rrosa10Server;

import javafx.animation.KeyFrame;
import javafx.animation.Timeline;
import javafx.application.Platform;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.ListView;
import javafx.scene.layout.Region;
import javafx.util.Duration;

import javax.sound.sampled.AudioInputStream;
import javax.sound.sampled.AudioSystem;
import javax.sound.sampled.Clip;
import javax.sound.sampled.FloatControl;
import java.net.URL;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * Controller for the server status screen.
 *
 * Responsibilities:
 * - Manage the "status" UI that shows server state, connected client rows, and a chronological log.
 * - Animate client-row and log-list text with per-character reveals.
 * - Play short audio feedback (separate clips for logs and client updates) when animations start.
 */
public class ServerScreenController {

    @FXML private Region greenScreen;
    @FXML private Label serverStatusLabel;
    @FXML private Label portLabel;
    @FXML private Label clientCountLabel;
    @FXML private Button stopServerButton;
    @FXML private ListView<String> logListView;

    @FXML private ListView<String> clientListView;
    private final ObservableList<String> clientItems = FXCollections.observableArrayList();
    private final Map<Integer, String> clientStatusMap = new HashMap<>();

    // Track per-client reveal animations so they can be cancelled/replaced
    private final Map<Integer, Timeline> clientAnimations = new HashMap<>();
    // Track active log animations (so we can stop them on shutdown)
    private final List<Timeline> activeLogAnimations = new ArrayList<>();

    // Two clips: one for logs, one for client updates
    private Clip logClip;
    private Clip clientClip;

    // enable/volume controls for each
    private boolean logSoundEnabled = true;
    private boolean clientSoundEnabled = true;
    private double logVolume = 0.35;
    private double clientVolume = 0.35;

    private GuiServer mainApp;

    public void setMainApp(GuiServer mainApp) {
        this.mainApp = mainApp;
    }

    /**
     * FXML initialize:
     * - Initialize labels and lists to a safe default state.
     * - Ensure the client list view uses the observable clientItems.
     * - Attempt to load optional audio clips from resources.
     */
    @FXML
    private void initialize() {
        serverStatusLabel.setText("Server stopped");
        clientCountLabel.setText("Connected clients: 0");
        clientListView.setItems(clientItems);
        if (logListView.getItems() == null) {
            logListView.setItems(FXCollections.observableArrayList());
        }

        // Load both clips from resources. Place your files at:
        // src/main/resources/sounds/tick.wav
        // src/main/resources/sounds/tick2.wav
        loadClips();
    }

    @FXML
    private void onStopServerClicked() {
        if (mainApp != null) {
            mainApp.stopServer();
        }
    }

    /**
     * Set UI to indicate the server is running and display the bound port.
     * This method posts to the JavaFX thread via Platform.runLater to be safe when called externally.
     */
    public void setServerRunning(int port) {
        Platform.runLater(() -> {
            serverStatusLabel.setText("Server running");
            portLabel.setText("Port: " + port);
        });
    }

    /** Indicate stopped status in the UI (thread-safe). */
    public void setServerStopped() {
        Platform.runLater(() -> serverStatusLabel.setText("Server stopped"));
    }

    /** Update the connected client counter label (thread-safe). */
    public void updateClientCount(int count) {
        Platform.runLater(() -> clientCountLabel.setText("Connected clients: " + count));
    }

    /**
     * Top-level log entry router.
     *
     * Contract:
     * - Messages that start with "CLIENT:" follow the convention "CLIENT:<id>|<payload>"
     *   and are treated as per-client status updates that occupy a single row in the client ListView.
     * - All other messages are appended to the chronological log ListView and animated.
     */
    public void logEvent(String message) {
        if (message == null) return;

        Platform.runLater(() -> {
            if (message.startsWith("CLIENT:")) {
                int pipe = message.indexOf('|');
                String idPart = (pipe > 0) ? message.substring(7, pipe) : message.substring(7);
                String payload = (pipe > 0) ? message.substring(pipe + 1) : "";
                try {
                    int clientId = Integer.parseInt(idPart.trim());
                    updateClientStatus(clientId, payload);
                } catch (NumberFormatException e) {
                    animateLogMessage("Malformed CLIENT message: " + message);
                }
            } else {
                animateLogMessage(message);
            }
        });
    }

    /**
     * Update the in-memory map of client id -> status payload and reconcile the clientItems list.
     *
     * Behavior:
     * - Builds a sorted list (by client id) of formatted rows.
     * - Reuses existing list entries when possible, and animates only rows that change.
     * - Adds or removes list entries if clients connect/disconnect.
     * - Performs UI updates and should be called on the JavaFX thread.
     */
    private void updateClientStatus(int clientId, String payload) {
        final String safePayload = payload == null ? "" : payload.trim();
        clientStatusMap.put(clientId, safePayload);

        List<String> newSorted = clientStatusMap.entrySet()
                .stream()
                .sorted(Comparator.comparingInt(Map.Entry::getKey))
                .map(e -> formatClientRow(e.getKey(), e.getValue()))
                .collect(Collectors.toList());

        int existingSize = clientItems.size();
        int newSize = newSorted.size();
        int min = Math.min(existingSize, newSize);

        // update changed rows
        for (int i = 0; i < min; i++) {
            String newVal = newSorted.get(i);
            String curVal = clientItems.get(i);
            if (!newVal.equals(curVal)) {
                animateClientTextAtIndex(i, newVal, 30);
            }
        }

        // add new rows
        if (newSize > existingSize) {
            for (int i = existingSize; i < newSize; i++) {
                clientItems.add(""); // placeholder
                animateClientTextAtIndex(i, newSorted.get(i), 30);
            }
        }

        // remove rows for disconnected clients
        if (newSize < existingSize) {
            for (int i = existingSize - 1; i >= newSize; i--) {
                cancelClientAnimation(i);
                clientItems.remove(i);
            }
        }

        clientCountLabel.setText("Connected clients: " + clientStatusMap.size());
    }

    /** Format a single client row for display in the client ListView. */
    private String formatClientRow(int clientId, String payload) {
        return "Client #" + clientId + ": " + payload;
    }

    /**
     * Animate a specific client list index to reveal fullText char-by-char.
     *
     * Details:
     * - Cancels any in-flight animation for that index.
     * - Ensures there's an entry in clientItems for the index.
     * - Plays the configured client clip once at the start (if enabled).
     * - Schedules KeyFrames to progressively replace the text content in the list cell.
     */
    private void animateClientTextAtIndex(int index, String fullText, int delayMs) {
        cancelClientAnimation(index);
        if (index < 0) return;
        while (clientItems.size() <= index) clientItems.add("");

        // Play client clip once at start (if enabled)
        if (clientSoundEnabled && clientClip != null) {
            playClipOnce(clientClip);
        }

        Timeline tl = new Timeline();
        for (int i = 1; i <= fullText.length(); i++) {
            final int len = i;
            KeyFrame kf = new KeyFrame(Duration.millis(delayMs * i), ev -> {
                if (index >= 0 && index < clientItems.size()) {
                    clientItems.set(index, fullText.substring(0, len));
                }
            });
            tl.getKeyFrames().add(kf);
        }
        KeyFrame finalKf = new KeyFrame(Duration.millis(delayMs * Math.max(fullText.length(), 1)), ev -> {
            if (index >= 0 && index < clientItems.size()) {
                clientItems.set(index, fullText);
            }
            clientAnimations.remove(index);
        });
        tl.getKeyFrames().add(finalKf);

        clientAnimations.put(index, tl);
        tl.play();
    }

    /** Cancel and remove any running animation for a given client list index. */
    private void cancelClientAnimation(int index) {
        Timeline prev = clientAnimations.remove(index);
        if (prev != null) prev.stop();
    }

    /**
     * Animate adding a new log message to the bottom of logListView with per-character reveal.
     *
     * Behavior:
     * - Adds a placeholder empty string to the log list and progressively fills it.
     * - Plays the configured log clip once at the start (if enabled).
     * - Keeps a reference to the Timeline so it can be stopped if needed.
     */
    private void animateLogMessage(String message) {
        ObservableList<String> logItems = logListView.getItems();
        int index = logItems.size();
        logItems.add("");

        // Play log clip once at start (if enabled)
        if (logSoundEnabled && logClip != null) {
            playClipOnce(logClip);
        }

        Timeline tl = new Timeline();
        final int delayMs = 18;
        for (int i = 1; i <= message.length(); i++) {
            final int len = i;
            KeyFrame kf = new KeyFrame(Duration.millis(delayMs * i), ev -> {
                if (index >= 0 && index < logItems.size()) {
                    logItems.set(index, message.substring(0, len));
                }
            });
            tl.getKeyFrames().add(kf);
        }
        KeyFrame finalKf = new KeyFrame(Duration.millis(delayMs * Math.max(message.length(), 1)), ev -> {
            if (index >= 0 && index < logItems.size()) {
                logItems.set(index, message);
            }
            activeLogAnimations.remove(tl);
        });
        tl.getKeyFrames().add(finalKf);

        activeLogAnimations.add(tl);
        tl.play();
    }

    // -------------------------
    // Audio loading / playing helpers using javax.sound.sampled
    // -------------------------
    private void loadClips() {
        logClip = loadClip("/sounds/tick.wav", logVolume);
        clientClip = loadClip("/sounds/tick2.wav", clientVolume);
    }

    /**
     * Load a Clip from a resource path and attempt to set its volume using MASTER_GAIN.
     *
     * Return: Clip if loaded successfully, otherwise null.
     */
    private Clip loadClip(String resourcePath, double volumeLinear) {
        try {
            URL soundUrl = getClass().getResource(resourcePath);
            if (soundUrl == null) {
                System.out.println("Sound not found at " + resourcePath + " (OK to ignore).");
                return null;
            }

            AudioInputStream ais = AudioSystem.getAudioInputStream(soundUrl);
            Clip clip = AudioSystem.getClip();
            clip.open(ais);

            // set initial volume if supported
            try {
                FloatControl gain = (FloatControl) clip.getControl(FloatControl.Type.MASTER_GAIN);
                float dB = (float) (20.0 * Math.log10(Math.max(1e-3, volumeLinear)));
                gain.setValue(dB);
            } catch (Exception ignored) {
                // not all mixers support MASTER_GAIN
            }
            return clip;
        } catch (Exception e) {
            System.err.println("Failed to load clip " + resourcePath + ": " + e.getMessage());
            e.printStackTrace();
            return null;
        }
    }

    /**
     * Play a Clip once. This will stop and restart the clip if it's already running.
     *
     * Note: Clips are reused. If you need overlapping playback of the same sound,
     * you'd need to open separate Clip instances per play (or use a pool).
     */
    private void playClipOnce(Clip clip) {
        if (clip == null) return;
        try {
            if (clip.isRunning()) clip.stop();
            clip.setFramePosition(0);
            clip.start(); // non-blocking
        } catch (Exception e) {
            System.err.println("Audio play error: " + e.getMessage());
        }
    }

    // -------------------------
    // Public sound controls
    // -------------------------
    public void setLogSoundEnabled(boolean enabled) {
        this.logSoundEnabled = enabled;
    }

    public void setClientSoundEnabled(boolean enabled) {
        this.clientSoundEnabled = enabled;
    }

    public boolean isLogSoundEnabled() {
        return logSoundEnabled;
    }

    public boolean isClientSoundEnabled() {
        return clientSoundEnabled;
    }

    /**
     * Set the linear volume for the log clip (0.0..1.0) and attempt to apply immediately.
     * Uses a dB conversion; very small values are clamped to avoid -Inf.
     */
    public void setLogVolume(double volumeLinear) {
        this.logVolume = Math.max(0, Math.min(1.0, volumeLinear));
        if (logClip != null) try {
            FloatControl gain = (FloatControl) logClip.getControl(FloatControl.Type.MASTER_GAIN);
            float dB = (float) (20.0 * Math.log10(Math.max(1e-3, this.logVolume)));
            gain.setValue(dB);
        } catch (Exception ignored) {}
    }

    /**
     * Set the linear volume for the client update clip (0.0..1.0) and attempt to apply immediately.
     */
    public void setClientVolume(double volumeLinear) {
        this.clientVolume = Math.max(0, Math.min(1.0, volumeLinear));
        if (clientClip != null) try {
            FloatControl gain = (FloatControl) clientClip.getControl(FloatControl.Type.MASTER_GAIN);
            float dB = (float) (20.0 * Math.log10(Math.max(1e-3, this.clientVolume)));
            gain.setValue(dB);
        } catch (Exception ignored) {}
    }

    /**
     * Stop and clear any running animations and stop audio playback.
     * Intended to be called during server shutdown to ensure no background Timelines or Clips remain running.
     */
    public void stopAllAnimations() {
        for (Timeline t : clientAnimations.values()) if (t != null) t.stop();
        clientAnimations.clear();
        for (Timeline t : new ArrayList<>(activeLogAnimations)) {
            if (t != null) t.stop();
            activeLogAnimations.remove(t);
        }
        // stop audio playback if playing
        try {
            if (logClip != null && logClip.isRunning()) logClip.stop();
            if (clientClip != null && clientClip.isRunning()) clientClip.stop();
        } catch (Exception ignored) {}
    }
}