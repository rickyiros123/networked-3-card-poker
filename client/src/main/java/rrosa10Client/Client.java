package rrosa10Client;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.Serializable;
import java.net.Socket;
import java.util.function.Consumer;

import javafx.application.Platform;

/**
 * Client - simple socket client thread for the 3 Card Poker client.
 *
 * Responsibilities:
 * - Connect to a server using a hostname and port.
 * - Maintain ObjectOutputStream/ObjectInputStream for object-based communication.
 * - Read incoming Serializable objects on a background thread and forward them
 *   to a UI-safe callback (wrapped with Platform.runLater).
 * - Provide a send() method to write objects to the server.
 *
 */
public class Client extends Thread {

    private Socket socketClient;
    private ObjectOutputStream out;
    private ObjectInputStream in;

    private final String host;
    private final int port;
    private final Consumer<Serializable> callback;

    // track closed state to make close()
    private volatile boolean closed = false;

    public Client(String host, int port, Consumer<Serializable> callback) {
        this.host = host;
        this.port = port;
        this.callback = callback;
    }

    @Override
    public void run() {
        try {
            socketClient = new Socket(host, port);
            out = new ObjectOutputStream(socketClient.getOutputStream());
            out.flush();
            in = new ObjectInputStream(socketClient.getInputStream());
            socketClient.setTcpNoDelay(true);

            while (!socketClient.isClosed() && !closed) {
                try {
                    Object obj = in.readObject();
                    System.out.println("Client received from socket: " + obj);

                    if (callback != null && obj instanceof Serializable) {
                        Serializable msg = (Serializable) obj;
                        Platform.runLater(() -> callback.accept(msg));
                    }
                } catch (ClassNotFoundException e) {
                    e.printStackTrace();
                } catch (IOException e) {
                    // socket closed or stream error -> break loop and cleanup
                    e.printStackTrace();
                    break;
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
            if (callback != null) {
                Platform.runLater(() ->
                    callback.accept("CONNECT_FAILED: " + e.getMessage())
                );
            }
        } finally {
            // Ensure resources are closed on exit
            close();
        }
    }

    /**
     * Send an object to the server.
     */
    public void send(Object obj) {
        try {
            if (out == null) {
                System.err.println("Send failed: output stream not initialized");
                return;
            }
            synchronized (out) {
                out.writeObject(obj);
                out.flush();
            }
        } catch (IOException e) {
            System.err.println("Send failed: " + e.getMessage());
            e.printStackTrace();
        }
    }

    /**
     * Close streams and socket. Safe to call multiple times.
     * This will unblock readObject() and allow the thread to exit.
     */
    public void close() {
        synchronized (this) {
            if (closed) {
                System.out.println("[CLIENT] close() called but already closed");
                return;
            }
            closed = true;
        }
        System.out.println("[CLIENT] close() beginning - closing streams and socket");
        try { if (in != null) { in.close(); } } catch (IOException ignored) {}
        in = null;
        try { if (out != null) { out.close(); } } catch (IOException ignored) {}
        out = null;
        try { if (socketClient != null && !socketClient.isClosed()) socketClient.close(); } catch (IOException ignored) {}
        socketClient = null;
        this.interrupt();
        System.out.println("[CLIENT] close() finished");
    }
}