package com.hotlinecornella;

import com.badlogic.gdx.Game;

public class Main extends Game {
    private WsClient webSocketClient;

    @Override
    public void create() {
        try {
            // Get the singleton instance
            webSocketClient = WsClient.getInstance(this);

            // Set initial screen to waiting room
            setScreen(new WaitingRoom(this));
        } catch (Exception e) {
            System.out.println("Error during create: " + e);
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
