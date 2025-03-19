package com.hotlinecornella;

import org.java_websocket.client.WebSocketClient;
import org.java_websocket.handshake.ServerHandshake;

import java.net.URI;

import static com.hotlinecornella.Main.logger;

public class WsClient extends WebSocketClient {

    public WsClient(URI serverUri) {
        super(serverUri);
    }

    @Override
    public void onOpen(ServerHandshake handshakedata) {
        logger.debug("WebSocket connected");
    }

    @Override
    public void onMessage(String message) {
        handleServerMessage(message);
    }

    @Override
    public void onClose(int code, String reason, boolean remote) {
        logger.debug("WebSocket closed: " + reason);
    }

    @Override
    public void onError(Exception ex) {
        logger.error("WebSocket error", ex);
    }
    private void handleServerMessage(String message) {
        System.out.println("Received message: " + message);
    }
}
