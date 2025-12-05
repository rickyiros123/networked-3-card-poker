package rrosa10Server;

import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.Serializable;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.ArrayList;
import java.util.List;
import java.util.function.Consumer;

import model.Card;
import model.Player;
import model.PokerEngine;
import model.PokerInfo;

/**
 * Simplified Server - lightweight single-threaded-accept server that spawns a ClientThread
 * for each incoming connection and uses a PokerEngine to handle a single-player game instance.
 *
 * Key responsibilities:
 * - Listen for incoming TCP connections on a configured port.
 * - For each accepted connection, create a ClientThread that:
 *     * exchanges PokerInfo messages with the client (object streams),
 *     * uses a dedicated PokerEngine for game evaluation,
 *     * sends client-specific PokerInfo responses (GAME_DEAL, GAME_RESULT, etc).
 */
public class Server {

    private final int port;
    private final ArrayList<ClientThread> clients = new ArrayList<>();
    private int count = 1;
    private TheServer testPokerServer;
    private final Consumer<Serializable> callback;
    private volatile boolean running = true;
    private final List<String> gameLog = new ArrayList<>();

    /**
     * Construct a Server bound to the given port and a callback to receive status/log messages.
     *
     * param port TCP port to listen on
     * param call callback invoked with Serializable messages for UI logging
     */
    public Server(int port, Consumer<Serializable> call) {
        this.port = port;
        this.callback = call;
        testPokerServer = new TheServer();
        testPokerServer.start();
    }

    /**
     * Shutdown the server:
     * - mark running = false to stop the accept loop,
     * - interrupt the acceptor thread,
     * - close all client connections and clear the client list.
     */
    public void shutdown() {
        running = false;
        try {
            if (testPokerServer != null && !testPokerServer.isInterrupted()) {
                testPokerServer.interrupt();
            }
            synchronized (clients) {
                for (ClientThread ct : clients) {
                    ct.closeConnection();
                }
                clients.clear();
            }
        } catch (Exception e) {
            callback.accept("Server shutdown error: " + e.getMessage());
        }
    }

    /**
     * TheServer is the acceptor thread: it opens a ServerSocket and accepts incoming connections.
     * For each socket it:
     *  - assigns a client id,
     *  - creates and starts a ClientThread,
     *  - emits both a client specific status update and a general log entry.
     *
     * The accept loop checks the 'running' flag to determine when to stop.
     */
    public class TheServer extends Thread {
        @Override
        public void run() {
            try (ServerSocket serverSocket = new ServerSocket(port)) {
                callback.accept("Server listening on port " + port);
                while (running) {
                    try {
                        Socket clientSocket = serverSocket.accept();
                        if (!running) break;
                        int clientId = count++;
                        ClientThread clientThread = new ClientThread(clientSocket, clientId);
                        synchronized (clients) {
                            clients.add(clientThread);
                        }
                        clientThread.start();

                        // Send a client-specific status update for GUI to show on single row
                        callback.accept("CLIENT:" + clientId + "|connected");
                        Server.this.logAction("CLIENT:" + clientId + "|connected");
                        // Also emit a general log entry
                        callback.accept("Client connected: #" + clientId + " (total: " + clients.size() + ")");
                        Server.this.logAction("Client connected: #" + clientId + " (total: " + clients.size() + ")");

                    } catch (java.io.IOException e) {
                        if (!running) break;
                        callback.accept("Accept failed: " + e.getMessage());
                    }
                }
            } catch (Exception e) {
                callback.accept("Server socket did not launch: " + e.getMessage());
                e.printStackTrace();
            }
        }
    }
    
    public void logAction(String action) {
        gameLog.add(action);
        System.out.println("[SERVER] logAction fired: " + action);
        synchronized (clients) {
            for (ClientThread client : clients) {
                if (client.out != null) {
                    System.out.println("[SERVER] Sending to client #" + client.clientId + " LOG: " + action); // ADDED
                    PokerInfo info = new PokerInfo(
                        PokerInfo.Type.LOG, null, null, 0, 0, List.of(action));
                    client.sendPokerInfo(info);
                }
            }
        }
    }

    /**
     * Per-connection worker thread.
     *
     * Responsibilities:
     * - Setup ObjectOutputStream/ObjectInputStream for object-based communication.
     * - Send an initial WELCOME PokerInfo to the client.
     * - Read PokerInfo messages from the client and dispatch to handler methods:
     *     START -> handleDealRequest
     *     PLAY  -> handlePlay
     *     FOLD  -> handleFold
     * - On disconnect, close streams/socket and notify UI via callback with:
     *   plus a general log line.
     *
     * Note: each ClientThread contains a dedicated PokerEngine and Player instance.
     */
    class ClientThread extends Thread {
        private final Socket connection;
        private final int clientId;
        private ObjectInputStream in = null;
        private ObjectOutputStream out = null;
        private final Player player;
        private final PokerEngine pokerEngine;

        ClientThread(Socket s, int clientId) {
            this.connection = s;
            this.clientId = clientId;
            this.player = new Player(clientId, "Player-" + clientId);
            this.pokerEngine = new PokerEngine(player, 500);
            
        }

        /**
         * Close socket and streams for this client. Safe to call multiple times.
         */
        public void closeConnection() {
            try {
                if (in != null) in.close();
                if (out != null) out.close();
                if (connection != null && !connection.isClosed()) connection.close();
            } catch (Exception ignored) {}
        }

        /**
         * Send a PokerInfo object to the connected client.
         * Writes are synchronized on the 'out' stream to avoid concurrent write corruption.
         *
         * param pkg PokerInfo message to send
         */
        public void sendPokerInfo(PokerInfo pkg) {
            try {
                synchronized (out) {
                    out.writeObject(pkg);
                    out.flush();
                }
            } catch (Exception e) {
                System.out.println("Could not send PokerInfo to client #" + clientId + ": " + e.getMessage());
                e.printStackTrace();
            }
        }

        /**
         * Main loop for the client: setup streams, send WELCOME, then read incoming PokerInfo messages
         * and dispatch them. On exit, remove this client from the server list and notify the UI.
         */
        @Override
        public void run() {
            try {
            	try {
                    out = new ObjectOutputStream(connection.getOutputStream());
                    out.flush();
                    in = new ObjectInputStream(connection.getInputStream());
                    connection.setTcpNoDelay(true);
                } catch (Exception e) {
                    e.printStackTrace();
                }

                PokerInfo welcome = new PokerInfo(PokerInfo.Type.WELCOME, null, null, 0, 0);
                sendPokerInfo(welcome);

                // Inform GUI that streams are open (client-specific)
                callback.accept("CLIENT:" + clientId + "streams opened");

                while (!connection.isClosed() && running) {
                    try {
                        Object obj = in.readObject();
                        if (obj == null) break;
                        if (!(obj instanceof PokerInfo)) continue;

                        PokerInfo received = (PokerInfo) obj;

                        switch (received.getType()) {
                            case START:
                                handleDealRequest(received);
                                break;
                            case PLAY:
                                handlePlay(received);
                                break;
                            case FOLD:
                                handleFold(received);
                                break;
                            case CHAT:
                                callback.accept("Client #" + clientId + " chat.");
                                break;
                            default:
                                break;
                        }

                    } catch (java.io.EOFException eof) {
                        break;
                    } catch (Exception e) {
                        e.printStackTrace();
                        break;
                    }
                }

            } catch (Exception e) {
                e.printStackTrace();
            } finally {
                try {
                    synchronized (clients) {
                        clients.remove(this);
                    }
                    closeConnection();
                    callback.accept("CLIENT:" + clientId + "|disconnected");
                    Server.this.logAction("CLIENT:" + clientId + "|disconnected");
                    callback.accept("Client #" + clientId + " disconnected. (total: " + clients.size() + ")");
                    Server.this.logAction("Client #" + clientId + " disconnected. (total: " + clients.size() + ")");
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        }

        /**
         * Handle a START request from client: perform a deal via PokerEngine and
         * respond with a GAME_DEAL PokerInfo containing player and dealer hands.
         * Also emits a client-specific status update showing the bet amounts.
         *
         * param received PokerInfo containing ante and pairPlus from client
         */
        private void handleDealRequest(PokerInfo received) {
            int ante = received.getAnte();
            int pairPlus = received.getPairPlus();

            pokerEngine.startSewHand();

            List<Card> newPlayerHand = pokerEngine.getClient().getHand();
            List<Card> newDealerHand = pokerEngine.getDealer().getHand();

            PokerInfo response = new PokerInfo(
                    PokerInfo.Type.GAME_DEAL,
                    newPlayerHand,
                    newDealerHand,
                    ante,
                    pairPlus
            );

            // client-specific status: show latest bet
            callback.accept("CLIENT:" + clientId + "|bet ante $" + ante + ", pairplus $" + pairPlus);
            Server.this.logAction("CLIENT:" + clientId + "|bet ante $" + ante + ", pairplus $" + pairPlus);
            sendPokerInfo(response);
        }

        /**
         * Handle a PLAY request: evaluate hands in PokerEngine, compute result amounts,
         * emit a client-specific result status, and send a GAME_RESULT PokerInfo back.
         *
         * param received PokerInfo carrying ante/pairPlus
         */
        private void handlePlay(PokerInfo received) {
            int ante = received.getAnte();
            int pairPlus = received.getPairPlus();

            pokerEngine.evaluateHands(pairPlus, ante);

            int resultPairPlus = pokerEngine.getPairPlus();
            int resultAnte = pokerEngine.getAnte();

            int net = resultAnte + resultPairPlus;

            callback.accept("CLIENT:" + clientId + "|result: " + (net >= 0 ? "+" : "") + net);
            Server.this.logAction("CLIENT:" + clientId + "|result: " + (net >= 0 ? "+" : "") + net);

            List<Card> finalPlayerHand = pokerEngine.getClient().getHand();
            List<Card> finalDealerHand = pokerEngine.getDealer().getHand();

            PokerInfo response = new PokerInfo(
                    PokerInfo.Type.GAME_RESULT,
                    finalPlayerHand,
                    finalDealerHand,
                    resultAnte,
                    resultPairPlus
            );

            sendPokerInfo(response);
        }

        /**
         * Handle a FOLD request: compute negative payouts (loss of bets), notify UI,
         * and send a GAME_RESULT containing the negative ante/pairPlus as the result.
         *
         * param received PokerInfo carrying ante/pairPlus
         */
        private void handleFold(PokerInfo received) {
            int ante = received.getAnte();
            int pairPlus = received.getPairPlus();

            int resultAnte = -ante;
            int resultPairPlus = -pairPlus;
            int net = resultAnte + resultPairPlus;

            callback.accept("CLIENT:" + clientId + "|folded and lost " + (-net) + " total.");
            Server.this.logAction("CLIENT:" + clientId + "|folded and lost " + (-net) + " total.");
            List<Card> currentPlayerHand = pokerEngine.getClient().getHand();
            List<Card> currentDealerHand = pokerEngine.getDealer().getHand();

            PokerInfo response = new PokerInfo(
                    PokerInfo.Type.GAME_RESULT,
                    currentPlayerHand,
                    currentDealerHand,
                    resultAnte,
                    resultPairPlus
            );

            sendPokerInfo(response);
        }
        
        
    }
}