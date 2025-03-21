package com.hotlinecornella;

import org.java_websocket.client.WebSocketClient;
import org.java_websocket.handshake.ServerHandshake;
import org.json.JSONObject;
import com.badlogic.gdx.Gdx;

import java.net.URI;
import java.net.URISyntaxException;

public class WsClient extends WebSocketClient {
    private static WsClient instance;
    private final Main gameInstance;

    private WsClient(URI serverUri, Main gameInstance) {
        super(serverUri);
        this.gameInstance = gameInstance;
    }

    public static WsClient getInstance(Main gameInstance) {
        if (instance == null) {
            try {
                instance = new WsClient(new URI("ws://localhost:8888"), gameInstance);
                instance.connect();
            } catch (URISyntaxException e) {
                System.out.println("WebSocket URI error: " + e);
            }
        }
        return instance;
    }

    @Override
    public void onOpen(ServerHandshake handshakedata) {
        System.out.println("WebSocket connected");
    }

    @Override
    public void onMessage(String message) {
        System.out.println("Received message: " + message);
        // Post to the main render thread
        Gdx.app.postRunnable(() -> {
            try {
                JSONObject obj = new JSONObject(message);
                if (obj.has("type")) {
                    String type = obj.getString("type");
                    if (type.equals("gameStart")) {
                        System.out.println("Starting game...");
                        if (!(gameInstance.getScreen() instanceof GameScreen)){
                            gameInstance.setScreen(new GameScreen(gameInstance));
                        }
                    }
                    else if (type.equals("playerMove")) {
                        // Handle player movement message
                        float x = (float) obj.getDouble("x");
                        float y = (float) obj.getDouble("y");

                        if (gameInstance.getScreen() instanceof GameScreen gameScreen) {
                            gameScreen.updateRivalPosition(x, y);
                        }
                    }
                    else if (type.equals("playerStopped")) {
                        if (gameInstance.getScreen() instanceof GameScreen gameScreen) {
                            gameScreen.stopRival();
                        }
                    }
                }
            } catch (Exception e) {
                System.out.println("Error processing message: " + e);
            }
        });
    }

    @Override
    public void onClose(int code, String reason, boolean remote) {
        System.out.println("WebSocket closed: " + reason);
    }

    @Override
    public void onError(Exception ex) {
        System.out.println("WebSocket error: " + ex);
    }
}
