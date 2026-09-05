package dev.infernity.rollplayer.ipc;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.BindException;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.net.ServerSocket;
import java.net.Socket;
import java.nio.charset.StandardCharsets;
import java.util.Objects;

/**
 * Coordinates a local Rollplayer handoff over a loopback TCP socket.
 *
 * <p>The first process owns the port. A later process sends {@code TAKEOVER},
 * and the first process waits for {@code READY} before shutting down.</p>
 */
public final class InstanceIpc implements AutoCloseable {
    private static final String TAKEOVER = "TAKEOVER";
    private static final String WAITING = "WAITING";
    private static final String READY = "READY";
    private static final String GRANTED = "GRANTED";
    private static final int HANDSHAKE_TIMEOUT_MILLIS = 5_000;
    private static final int PORT_RETRIES = 20;

    private final int port;
    private final Runnable takeoverHandler;
    private volatile boolean closed;
    private volatile ServerSocket serverSocket;
    private volatile Socket handoffSocket;
    private BufferedReader handoffInput;
    private PrintWriter handoffOutput;

    private InstanceIpc(int port, Runnable takeoverHandler) {
        this.port = port;
        this.takeoverHandler = Objects.requireNonNull(takeoverHandler, "takeoverHandler");
    }

    /** Starts the primary listener or asks the current process for a handoff. */
    public static InstanceIpc acquire(int port, Runnable takeoverHandler) throws IOException {
        var ipc = new InstanceIpc(port, takeoverHandler);
        try {
            ipc.serverSocket = openServer(port);
            ipc.startListening();
        } catch (BindException ignored) {
            ipc.connectToPrimary();
        }
        return ipc;
    }

    private static ServerSocket openServer(int port) throws IOException {
        return new ServerSocket(port, 1, InetAddress.getLoopbackAddress());
    }

    private void connectToPrimary() throws IOException {
        var socket = new Socket();
        try {
            socket.connect(new InetSocketAddress(InetAddress.getLoopbackAddress(), port), 1_000);
            socket.setSoTimeout(HANDSHAKE_TIMEOUT_MILLIS);
            handoffInput = new BufferedReader(new InputStreamReader(socket.getInputStream(), StandardCharsets.UTF_8));
            handoffOutput = new PrintWriter(socket.getOutputStream(), true, StandardCharsets.UTF_8);
            handoffOutput.println(TAKEOVER);
            if (!WAITING.equals(handoffInput.readLine())) {
                throw new IOException("The existing instance rejected the takeover request.");
            }
            socket.setSoTimeout(0);
            handoffSocket = socket;
        } catch (IOException e) {
            closeQuietly(socket);
            throw new IOException("Could not connect to the existing instance on IPC port " + port + ".", e);
        }
    }

    private void startListening() {
        var listener = new Thread(this::listen, "rollplayer-ipc");
        listener.setDaemon(true);
        listener.start();
    }

    private void listen() {
        while (!closed) {
            try (Socket socket = serverSocket.accept()) {
                try {
                    handleTakeover(socket);
                } catch (IOException ignored) {
                    // A replacement that disappears before READY leaves this
                    // process in control, so keep listening.
                }
            } catch (IOException ignored) {
                return;
            }
        }
    }

    private void handleTakeover(Socket socket) throws IOException {
        var input = new BufferedReader(new InputStreamReader(socket.getInputStream(), StandardCharsets.UTF_8));
        var output = new PrintWriter(socket.getOutputStream(), true, StandardCharsets.UTF_8);
        if (!TAKEOVER.equals(input.readLine())) {
            return;
        }
        output.println(WAITING);
        if (READY.equals(input.readLine())) {
            output.println(GRANTED);
            takeoverHandler.run();
        }
    }

    public boolean isWaitingForTakeover() {
        return handoffSocket != null && !closed;
    }

    /** Signals readiness and claims the IPC port after the primary releases it. */
    public void signalReadyAndTakeControl() throws IOException {
        if (!isWaitingForTakeover()) {
            return;
        }

        handoffOutput.println(READY);
        if (!GRANTED.equals(handoffInput.readLine())) {
            throw new IOException("The existing instance did not grant the takeover.");
        }
        closeHandoff();
        serverSocket = openServerWithRetry();
        startListening();
    }

    private ServerSocket openServerWithRetry() throws IOException {
        BindException lastFailure = null;
        for (int attempt = 0; attempt < PORT_RETRIES; attempt++) {
            try {
                return openServer(port);
            } catch (BindException e) {
                lastFailure = e;
                try {
                    Thread.sleep(100);
                } catch (InterruptedException interrupted) {
                    Thread.currentThread().interrupt();
                    throw new IOException("Interrupted while acquiring the IPC port.", interrupted);
                }
            }
        }
        throw new IOException("The IPC port " + port + " was not released after takeover.", lastFailure);
    }

    private void closeHandoff() {
        closeQuietly(handoffSocket);
        handoffSocket = null;
        handoffInput = null;
        handoffOutput = null;
    }

    @Override
    public void close() {
        if (closed) {
            return;
        }
        closed = true;
        closeHandoff();
        closeQuietly(serverSocket);
    }

    private static void closeQuietly(AutoCloseable closeable) {
        if (closeable == null) {
            return;
        }
        try {
            closeable.close();
        } catch (Exception ignored) {
            // Closing IPC is best effort during shutdown.
        }
    }
}
