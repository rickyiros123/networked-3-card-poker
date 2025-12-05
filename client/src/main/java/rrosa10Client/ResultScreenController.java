package rrosa10Client;

import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.Label;

/**
 * Controller for the Result screen shown after a game round completes.
 *
 * Responsibilities:
 * - Display the outcome message ("You Won!", "You Lost", or "Push") and the net amount.
 * - Provide actions for the user to play again or exit the application.
 *
 */
public class ResultScreenController {

    @FXML private Label resultMessage;
    @FXML private Label amountLabel;
    @FXML private Button playAgainButton;
    @FXML private Button exitButton;

    private GuiServer mainApp;

    /**
     * Provide a reference to the main application so this controller can request scene changes.
     *
     * param mainApp the GuiServer application instance
     */
    public void setMainApp(GuiServer mainApp) {
        this.mainApp = mainApp;
    }

    /**
     * Configure the result screen with the net amount from the last round.
     *
     * - Positive netAmount => player won.
     * - Negative netAmount => player lost.
     * - Zero netAmount => push (broke even).
     *
     * This method updates the visible labels to show an appropriate message and the absolute amount.
     *
     * param netAmount net winnings (positive) or losses (negative) for the player this round
     */
    public void setResult(int netAmount) {

        if (netAmount > 0) {
            resultMessage.setText("You Won!");
            amountLabel.setText("You won $" + netAmount + " this game.");
        } else if (netAmount < 0) {
            resultMessage.setText("You Lost");
            amountLabel.setText("You lost $" + Math.abs(netAmount) + " this game.");
        } else {
            resultMessage.setText("Push");
            amountLabel.setText("You broke even this game.");
        }
    }

    /**
     * Handler for the "Play Again" button.
     * Requests the main application to switch back to the poker screen so the player can play another hand.
     */
    @FXML
    private void onPlayAgainClicked() {
        // Return to poker screen for another hand
        if (mainApp != null) {
            mainApp.showPokerScreen();
            mainApp.getPokerScreenController().enableButtonsForStartGame();
        }
    }

    /**
     * Handler for the "Exit" button.
     * Closes the window (and thereby exits the application since this is the primary stage).
     */
    @FXML
    private void onExitClicked() {
        System.out.println("[UI] Exit clicked");
        if (mainApp != null) {
            System.out.println("[UI] calling mainApp.shutdown()");
            mainApp.shutdown();
        } else {
            System.out.println("[UI] mainApp is null; calling Platform.exit()");
            javafx.application.Platform.exit();
        }
    }
}