package com.hotlinecornella;

import java.util.List;
import java.util.HashMap;

public class GameMap {
    public String name;
    public List<Level> levels;
    private HashMap<String, Player> players = new HashMap<>();

    public static class Level {
        public String name;
        public String description;
        public List<Layer> layers;
        public List<Zone> zones;
        public List<Sprite> sprites;

        public static class Layer {
            public String name;
            public int x;
            public int y;
            public int depth;
            public String tilesSheetFile;
            public int tilesWidth;
            public int tilesHeight;
            public int[][] tileMap;
            public boolean visible;
        }

        public static class Zone {
            public String type;
            public int x;
            public int y;
            public int width;
            public int height;
            public String color;
        }

        public static class Sprite {
            // Define sprite properties if any
        }
    }

    public Player getPlayerById(String playerId) {
        return players.get(playerId);
    }

    public void addPlayer(String playerId, Player player) {
        players.put(playerId, player);
    }

    public void updateOtherPlayerPosition(String playerId, float x, float y) {
        Player otherPlayer = getPlayerById(playerId);
        if (otherPlayer == null) {
            otherPlayer = new Player("images/player2_idle.png", "images/player2_run.png", x, y, 1.5f);
            addPlayer(playerId, otherPlayer);
        }
        otherPlayer.setPosition(x, y);
    }
}
