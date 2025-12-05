package rrosa10Client;

import java.util.ArrayList;
import java.util.List;

import javafx.application.Platform;
import javafx.beans.property.IntegerProperty;
import javafx.beans.property.SimpleIntegerProperty;
import javafx.fxml.FXML;
import javafx.scene.control.Label;
import javafx.scene.control.MenuItem;
import javafx.scene.control.TextArea;
import javafx.scene.control.TextField;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.VBox;
import javafx.scene.text.Text;
import model.Card;
import model.PokerInfo;

/**
 * Controller for the Poker game screen.
 *
 * Responsibilities:
 * - Manage the poker UI (card images, betting buttons, wallet display).
 * - Maintain a Client reference for sending user actions to the server.
 * - Receive PokerInfo objects from the server (via handlePokerInfo) and update the UI accordingly.
 */
public class PokerScreenController {

    // Root container for the scene
    @FXML private BorderPane root;
    @FXML private VBox titleBox;
    @FXML private Text gradientText;
    @FXML private Label instruction;
    @FXML private TextField portText;
    @FXML private TextField ipText;

    // Text elements bound to game state
    @FXML private Text walletText;
    @FXML private Text anteText;
    @FXML private Text pairPlusText;
    @FXML private TextArea logBox;

    // Player card image views
    @FXML private ImageView playerCard1;
    @FXML private ImageView playerCard2;
    @FXML private ImageView playerCard3;
    // Dealer card image views
    @FXML private ImageView dealerCard1;
    @FXML private ImageView dealerCard2;
    @FXML private ImageView dealerCard3;

    // Control images (used as buttons)
    @FXML private ImageView boardImage;

    @FXML private ImageView pairPlusImage;
    @FXML private ImageView startButton;
    @FXML private ImageView anteImage;
    @FXML private ImageView foldImage;
    @FXML private ImageView dealImage;
    @FXML private ImageView playImageLeft;
    @FXML private ImageView fiveDollarImage;
    @FXML private ImageView tenDollarImage;
    @FXML private ImageView twentyDollarImage;
    @FXML private ImageView twentyFiveDollarImage;
    @FXML private ImageView menuImage;
    @FXML private MenuItem exitItem;
    @FXML private MenuItem freshStart;
    @FXML private MenuItem newLook;


    // Network client and reference to main application (for scene switching)
    private Client client;
    private GuiServer mainApp;

    // Local model of hands
    private List<Card> dealerHand;
    private List<Card> playerHand;

    // Observable properties for UI binding
    private final IntegerProperty wallet = new SimpleIntegerProperty(0);
    private final IntegerProperty anteAmount = new SimpleIntegerProperty(0);
    private final IntegerProperty pairPlusAmount = new SimpleIntegerProperty(0);

    // UI state helpers
    private int highlightedValue = 0;
    private PokerInfo pokerInfo;
    
    private final List<String> boardImages = List.of(
            "/images/Board.png",
            "/images/Board2.png"
        );
    
    private int boardIndex = 0;


    /**
     * Initialize FXML controls and set default UI state.
     * - Set initial card backs for both player and dealer.
     * - Bind wallet/ante/pairPlus properties to text fields.
     * - Initialize wallet and disable action buttons until a new game starts.
     */
    @FXML
    private void initialize() {
        setPlayerCard(1, "/images/cards/back2-256.png");
        setPlayerCard(2, "/images/cards/back2-256.png");
        setPlayerCard(3, "/images/cards/back2-256.png");
        setDealerCard(1, "/images/cards/back2-256.png");
        setDealerCard(2, "/images/cards/back2-256.png");
        setDealerCard(3, "/images/cards/back2-256.png");
        
        if (newLook != null) {
            newLook.setOnAction(e -> cycleBoardImage());
        }
        
        if (boardImage != null && boardImages.size() > 0) {
            setBoardImage(boardImages.get(boardIndex));
        }
        
        Image startHover = new Image(getClass().getResourceAsStream("/images/StartHover.png"));
        Image originalHover = startButton.getImage();
        startButton.setOnMouseEntered(e -> startButton.setImage(startHover));
        startButton.setOnMouseExited(e -> startButton.setImage(originalHover));
        
        Image anteHover = new Image(getClass().getResourceAsStream("/images/AnteHover.png"));
        Image originalAnte = anteImage.getImage();
        anteImage.setOnMouseEntered(e -> anteImage.setImage(anteHover));
        anteImage.setOnMouseExited(e -> anteImage.setImage(originalAnte));
        
        Image pairPlusHover = new Image(getClass().getResourceAsStream("/images/PairPlusHover.png"));
        Image originalPairPlus = pairPlusImage.getImage();
        pairPlusImage.setOnMouseEntered(e -> pairPlusImage.setImage( pairPlusHover));
        pairPlusImage.setOnMouseExited(e -> pairPlusImage.setImage(originalPairPlus));
        
        Image tenDollarsHover = new Image(getClass().getResourceAsStream("/images/TenDollarsHover.png"));
        Image originalTenDollars = tenDollarImage.getImage();
        tenDollarImage.setOnMouseEntered(e -> tenDollarImage.setImage(tenDollarsHover));
        tenDollarImage.setOnMouseExited(e -> tenDollarImage.setImage(originalTenDollars));
        
        Image twentyDollarsHover = new Image(getClass().getResourceAsStream("/images/TwentyDollarsHover.png"));
        Image originalTwentyDollars = twentyDollarImage.getImage();
        twentyDollarImage.setOnMouseEntered(e -> twentyDollarImage.setImage(twentyDollarsHover));
        twentyDollarImage.setOnMouseExited(e -> twentyDollarImage.setImage(originalTwentyDollars));
        
        Image twentyFiveDollarsHover = new Image(getClass().getResourceAsStream("/images/TwentyFiveDollarsHover.png"));
        Image originalTwentyFiveDollars = twentyFiveDollarImage.getImage();
        twentyFiveDollarImage.setOnMouseEntered(e -> twentyFiveDollarImage.setImage(twentyFiveDollarsHover));
        twentyFiveDollarImage.setOnMouseExited(e -> twentyFiveDollarImage.setImage(originalTwentyFiveDollars));
        
        Image fiveDollarsHover = new Image(getClass().getResourceAsStream("/images/FiveDollarsHover.png"));
        Image originalFiveDollars = fiveDollarImage.getImage();
        fiveDollarImage.setOnMouseEntered(e -> fiveDollarImage.setImage(fiveDollarsHover));
        fiveDollarImage.setOnMouseExited(e -> fiveDollarImage.setImage(originalFiveDollars));
        
        wallet.addListener((obs, oldVal, newVal) -> walletText.setText("Wallet: $" + newVal));
        anteAmount.addListener((obs, oldVal, newVal) -> anteText.setText("Ante: $" + newVal));
        pairPlusAmount.addListener((obs, oldVal, newVal) -> pairPlusText.setText("Pair Plus: $" + newVal));
        
        exitItem.setOnAction(e -> {
        	if(mainApp != null) {
        		mainApp.shutdown();
        	} else {
        		Platform.exit();
        	}
        	
        	});
        freshStart.setOnAction(e -> {
        	startNewGame();
        	disableButtonsForNewGame();
            anteAmount.set(0);
            pairPlusAmount.set(0);
            wallet.set(500);
        	});
        
        wallet.set(500);
        setAnteText("Ante: $0");
        setPairPlusText("Pair Plus: $0");
        highlightedValue = 0;

        disableButtonsForNewGame();
    }

    /**
     * Called by GuiServer after FXML is loaded and before the scene is shown.
     * Stores the client instance used to send messages and an initial PokerInfo.
     *
     * param client    network client for sending user actions
     * param firstInfo initial PokerInfo payload (may be WELCOME)
     */
    public void setClient(Client client, PokerInfo firstInfo) {
        this.client = client;
        this.pokerInfo = firstInfo;
        // WELCOME typically has no hand; nothing else needed here yet
    }

    // --- UI Handlers ---
    // Small click handlers that change betting state or send actions to the server.
    // Each handler prints a debug line and performs UI updates; network sends are done where appropriate.

    @FXML
    private void onPairPlusClicked(MouseEvent event) {
        System.out.println("Pair Plus clicked");
        addToPairPlus(highlightedValue);
    }

    @FXML
    private void onStartClicked(MouseEvent event) {
        System.out.println("Play clicked");
        startButton.setDisable(true);
        enableButtonsForStartGame();
    }

    @FXML
    private void onAnteClicked(MouseEvent event) {
        System.out.println("Ante clicked");
        addToAnte(highlightedValue);
        dealImage.setDisable(false);
    }

    @FXML
    private void on5Clicked(MouseEvent event) {
        System.out.println("$1 clicked");
        highlightedValue = 5;
    }

    @FXML
    private void on10Clicked(MouseEvent event) {
        System.out.println("$5 clicked");
        highlightedValue = 10;
    }

    @FXML
    private void on20Clicked(MouseEvent event) {
        System.out.println("$10 clicked");
        highlightedValue = 20;
    }

    @FXML
    private void on25Clicked(MouseEvent event) {
        System.out.println("$20 clicked");
        highlightedValue = 25;
    }
    
    @FXML
    private void onMenuClicked() {
    	System.out.println("Menu clicked");
    }
    
    /**
     * User clicked Deal:
     * - Disable betting controls and enable play/fold.
     * - Construct a START PokerInfo package with current ante/pairPlus values and send it to server.
     */
    @FXML
    private void onDealClicked(MouseEvent event) {
        System.out.println("Deal clicked");
        pairPlusImage.setDisable(true);
        anteImage.setDisable(true);
        dealImage.setDisable(true);
        if (twentyFiveDollarImage != null) twentyFiveDollarImage.setDisable(true);
        if (fiveDollarImage != null) fiveDollarImage.setDisable(true);
        if (tenDollarImage != null) tenDollarImage.setDisable(true);
        if (twentyDollarImage != null) twentyDollarImage.setDisable(true);
        playImageLeft.setDisable(false);
        foldImage.setDisable(false);
        pokerInfo = new PokerInfo(PokerInfo.Type.START, null, null, anteAmount.getValue(), pairPlusAmount.getValue());
        client.send(pokerInfo);
    }

    /**
     * User clicked Side Play (left play):
     * - Disable further input for betting and deal.
     * - Flip dealer cards for display and send PLAY request to server with current hands and bets.
     */
    @FXML
    private void onLeftPlayClicked(MouseEvent event) {
        System.out.println("Side Play clicked");
        dealImage.setDisable(true);
        foldImage.setDisable(true);
        if (twentyFiveDollarImage != null) twentyFiveDollarImage.setDisable(true);
        if (fiveDollarImage != null) fiveDollarImage.setDisable(true);
        if (tenDollarImage != null) tenDollarImage.setDisable(true);
        if (twentyDollarImage != null) twentyDollarImage.setDisable(true);
        flipDealerHand();
        PokerInfo packageToSend = new PokerInfo(
                PokerInfo.Type.PLAY, 
                playerHand, dealerHand, 
                anteAmount.get(), 
                pairPlusAmount.get());
        if(client != null) {
            client.send(packageToSend);
        }
    }

    /**
     * User clicked Fold:
     * - Reset bets and send FOLD message to server with current state.
     */
    @FXML
    private void onFoldClicked(MouseEvent event) {
        System.out.println("Fold clicked");
        resetAnteAmount();
        resetPairPlusAmount();
        PokerInfo packageToSend = new PokerInfo(
                PokerInfo.Type.FOLD,
                playerHand,
                dealerHand,
                anteAmount.get(),
                pairPlusAmount.get());
        if (client != null) {
            client.send(packageToSend);
        }
    }

    // --- Display updates from server ---

    /**
     * Handle incoming PokerInfo objects from the server and update UI state.
     * - GAME_DEAL: update card images and reveal player hand.
     * - GAME_RESULT: update cards, flip dealer hand, update wallet with results, reset bets, and show result screen.
     *
     * @param pokerInfo incoming server state payload
     */
    public void handlePokerInfo(PokerInfo pokerInfo) {
        System.out.println("handlePokerInfo in controller " + this + ", type=" + pokerInfo.getType());
        PokerInfo.Type type = pokerInfo.getType();
        switch (type) {
            case GAME_DEAL:
                System.out.println("GAME_DEAL: playerHand=" + pokerInfo.getPlayerHand());
                updateCards(pokerInfo.getPlayerHand(), pokerInfo.getDealerHand());
                flipPlayerHand();
                break;
            case LOG:
                if (pokerInfo.getLog() != null) {
                    for (String entry : pokerInfo.getLog()) {
                        System.out.println("[CONTROLLER] got log line: " + entry); // PRINT HERE
                        Platform.runLater(() -> logBox.appendText(entry + "\n"));
                    }
                }
                break;

            case GAME_RESULT:
                System.out.println("GAME_RESULT: Ante = " + pokerInfo.getAnte());
                System.out.println("GAME_RESULT: Pair Plus = " + pokerInfo.getPairPlus());
                updateCards(pokerInfo.getPlayerHand(), pokerInfo.getDealerHand());
                flipDealerHand();

                int anteResult = pokerInfo.getAnte();
                int pairPlusResult = pokerInfo.getPairPlus();
                int netThisGame = anteResult + pairPlusResult;

                addToWallet(anteResult);
                addToWallet(pairPlusResult);
                resetPairPlusAmount();
                resetAnteAmount();

                // Show result screen using the main application reference
                if (mainApp != null) {
                    mainApp.showResultScreen(netThisGame);
                }
                break;

            case CHAT:
            default:
                // CHAT and unknown types are ignored here
                break;
        }
    }

    /**
     * Update internal hand lists and set the player card images.
     * Only updates player images if playerHand has at least 3 cards.
     *
     * @param playerHand list of player cards from server
     * @param dealerHand list of dealer cards from server
     */
    private void updateCards(List<Card> playerHand, List<Card> dealerHand) {
        System.out.println("updateCards called. incoming playerHand=" + playerHand);
        if (playerHand != null && playerHand.size() >= 3) {
            this.playerHand = new ArrayList<>(playerHand);
            setPlayerCard(1, playerHand.get(0).getImagePath());
            setPlayerCard(2, playerHand.get(1).getImagePath());
            setPlayerCard(3, playerHand.get(2).getImagePath());
        } else {
            System.out.println("Not setting this.playerHand; list is null or size < 3");
        }

        if (dealerHand != null && dealerHand.size() >= 3) {
            this.dealerHand = new ArrayList<>(dealerHand);
        }
    }

    /**
     * Reveal the player's cards by setting the three player card image views to the current playerHand values.
     * If playerHand is not ready, the method logs a message and returns.
     */
    public void flipPlayerHand() {
        if (playerHand == null || playerHand.size() < 3) {
            System.out.println("flipPlayerHand called but playerHand is not ready. playerHand=" + playerHand);
            return;
        }

        setPlayerCard(1, playerHand.get(0).getImagePath());
        setPlayerCard(2, playerHand.get(1).getImagePath());
        setPlayerCard(3, playerHand.get(2).getImagePath());
    }

    /**
     * Reveal the dealer's cards by setting the three dealer card image views to the current dealerHand values.
     * If dealerHand is not ready, the method logs a message and returns.
     */
    public void flipDealerHand() {
        if (dealerHand == null || dealerHand.size() < 3) {
            System.out.println("flipDealerHand called but dealerHand is not ready. dealerHand=" + dealerHand);
            return;
        }
        setDealerCard(1, dealerHand.get(0).getImagePath());
        setDealerCard(2, dealerHand.get(1).getImagePath());
        setDealerCard(3, dealerHand.get(2).getImagePath());
    }

    /**
     * Set an individual player card ImageView by loading an Image from resources.
     *
     * @param position      1-based card slot (1..3)
     * @param cardImagePath resource path to the card image
     */
    private void setPlayerCard(int position, String cardImagePath) {
        Image img = new Image(getClass().getResourceAsStream(cardImagePath));
        switch (position) {
            case 1:
                playerCard1.setImage(img);
                break;
            case 2:
                playerCard2.setImage(img);
                break;
            case 3:
                playerCard3.setImage(img);
                break;
            default:
                break;
        }
    }

    /**
     * Set an individual dealer card ImageView by loading an Image from resources.
     *
     * @param position      1-based card slot (1..3)
     * @param cardImagePath resource path to the card image
     */
    private void setDealerCard(int position, String cardImagePath) {
        Image img = new Image(getClass().getResourceAsStream(cardImagePath));
        switch (position) {
            case 1:
                dealerCard1.setImage(img);
                break;
            case 2:
                dealerCard2.setImage(img);
                break;
            case 3:
                dealerCard3.setImage(img);
                break;
            default:
                break;
        }
    }

    // --- Betting and wallet helpers --

    /**
     * Deduct amount from wallet and add it to the anteAmount.
     *
     * @param amount amount to move from wallet into the ante bet
     */
    public void addToAnte(int amount) {
        wallet.set(wallet.get() - amount);
        anteAmount.set(anteAmount.get() + amount);
    }

    /**
     * Deduct amount from wallet and add it to the pair-plus bet.
     *
     * @param amount amount to move from wallet into pair-plus
     */
    public void addToPairPlus(int amount) {
        wallet.set(wallet.get() - amount);
        pairPlusAmount.set(pairPlusAmount.get() + amount);
    }

    /**
     * Add amount to the wallet (used for applying results).
     *
     * @param amount net amount to add (can be negative for losses)
     */
    public void addToWallet(int amount) {
        wallet.set(wallet.get() + amount);
    }

    /** Reset ante amount to zero. */
    public void resetAnteAmount() {
        anteAmount.set(0);
    }

    /** Reset pair-plus amount to zero. */
    public void resetPairPlusAmount() {
        pairPlusAmount.set(0);
    }

    /**
     * Disable game control buttons for the initial/new-game state.
     * This prevents user interaction until a round is started.
     */
    public void disableButtonsForNewGame() {
        if (pairPlusImage != null) pairPlusImage.setDisable(true);
        if (anteImage != null) anteImage.setDisable(true);
        if (playImageLeft != null) playImageLeft.setDisable(true);
        if (foldImage != null) foldImage.setDisable(true);
        if (dealImage != null) dealImage.setDisable(true);
        if (dealImage != null) dealImage.setDisable(true);
        if (twentyFiveDollarImage != null) twentyFiveDollarImage.setDisable(true);
        if (fiveDollarImage != null) fiveDollarImage.setDisable(true);
        if (tenDollarImage != null) tenDollarImage.setDisable(true);
        if (twentyDollarImage != null) twentyDollarImage.setDisable(true);
    }
    
    /**
     * Enable the main betting controls so the player can choose ante/pair-plus and denominations.
     * Called when the player clicks the Start/Play control.
     */
    public void enableButtonsForStartGame() {
        if (pairPlusImage != null) pairPlusImage.setDisable(false);
        if (anteImage != null) anteImage.setDisable(false);
        if (twentyFiveDollarImage != null) twentyFiveDollarImage.setDisable(false);
        if (fiveDollarImage != null) fiveDollarImage.setDisable(false);
        if (tenDollarImage != null) tenDollarImage.setDisable(false);
        if (twentyDollarImage != null) twentyDollarImage.setDisable(false);
    }

    // text setters

    /** Set displayed pair-plus text */
    private void setPairPlusText(String newPairPlus) {
        pairPlusText.setText(newPairPlus);
    }

    /** Set displayed ante text */
    private void setAnteText(String newAnte) {
        anteText.setText(newAnte);
    }
    
    /** Provide reference to main application so controller can trigger scene changes. */
    public void setMainApp(GuiServer mainApp) {
        this.mainApp = mainApp;
    }
    
    /**
     * Start a new game from the client side:
     * - reset UI controls and card backs
     * - send a GAME_DEAL request (with zero bets) to the server to request a new deal
     */
    public void startNewGame() {
        setPlayerCard(1, "/images/cards/back2-256.png");
        setPlayerCard(2, "/images/cards/back2-256.png");
        setPlayerCard(3, "/images/cards/back2-256.png");
        setDealerCard(1, "/images/cards/back2-256.png");
        setDealerCard(2, "/images/cards/back2-256.png");
        setDealerCard(3, "/images/cards/back2-256.png");
        PokerInfo packageToSend = new PokerInfo(PokerInfo.Type.GAME_DEAL, 
                null, 
                null, 
                0, 
                0);
        if (client != null) {
            client.send(packageToSend);
        }
    }
    
    public void pushInitialLogs(List<String> logs) {
        if (logs == null || logs.isEmpty()) return;
        Platform.runLater(() -> {
            for (String entry : logs) {
                logBox.appendText(entry + "\n");
            }
        });
    }
    
    private void cycleBoardImage() {
        boardIndex = (boardIndex + 1) % boardImages.size();
        setBoardImage(boardImages.get(boardIndex));
    }

    private void setBoardImage(String resourcePath) {
        try {
            Image img = new Image(getClass().getResourceAsStream(resourcePath));
            boardImage.setImage(img);
        } catch (Exception ex) {
            System.err.println("Failed to load board image: " + resourcePath + " -> " + ex.getMessage());
        }
    }
}