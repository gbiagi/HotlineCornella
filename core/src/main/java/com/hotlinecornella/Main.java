package com.hotlinecornella;

import com.badlogic.gdx.Game;

import java.net.URI;
import java.net.URISyntaxException;

public class Main extends Game {
    private WsClient webSocketClient;

    @Override
    public void create() {
        try {
            // Initialize WebSocket connection
            webSocketClient = new WsClient(new URI("ws://localhost:8888")) {
                @Override
                public void onMessage(String message) {
                    if (message.equals("START_GAME")) {
                        setScreen(new GameScreen(Main.this));
                    }
                }
            };
            webSocketClient.connect();

            // Set initial screen to waiting room
            setScreen(new WaitingRoom(this));

        } catch (URISyntaxException e) {
            System.out.println("WebSocket URI error " + e);
        } catch (Exception e) {
            System.out.println("Error during create " + e);
        }
    }

    public WsClient getWebSocketClient() {
        return webSocketClient;
    }


    @Override
    public void dispose() {
        super.dispose();
        if (getScreen() != null) {
            getScreen().dispose();
        }
    }
}
