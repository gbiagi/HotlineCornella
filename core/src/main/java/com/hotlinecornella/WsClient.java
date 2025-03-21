package com.hotlinecornella;

import org.java_websocket.client.WebSocketClient;
import org.java_websocket.handshake.ServerHandshake;
import org.json.JSONObject;

import java.net.URI;

public class WsClient extends WebSocketClient {

    public WsClient(URI serverUri) {
        super(serverUri);
    }

    @Override
    public void onOpen(ServerHandshake handshakedata) {
        System.out.println("WebSocket connected");
    }

    @Override
    public void onMessage(String message) {
        System.out.println("Hola?");
        System.out.println("Received message: " + message);
        JSONObject obj = new JSONObject(message);
        if (obj.has("type")) {
            String type = obj.getString("type");
            if (type.equals("ok")) {
                System.out.println("Received OK");
            }
        }
    }

    @Override
    public void onClose(int code, String reason, boolean remote) {
        System.out.println("WebSocket closed: " + reason);
    }

    @Override
    public void onError(Exception ex) {
        System.out.println("WebSocket error" + ex);
    }

}
