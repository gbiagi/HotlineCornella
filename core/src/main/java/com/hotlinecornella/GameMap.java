package com.hotlinecornella;

import java.util.List;

public class GameMap {
    public String name;
    public List<Level> levels;

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
}
