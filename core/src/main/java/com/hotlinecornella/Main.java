package com.hotlinecornella;

import com.badlogic.gdx.ApplicationAdapter;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.utils.ScreenUtils;
import com.badlogic.gdx.utils.Logger;
import com.badlogic.gdx.math.Rectangle;

public class Main extends ApplicationAdapter {
    private static final Logger logger = new Logger(Main.class.getName(), Logger.DEBUG);
    private SpriteBatch batch;
    private Texture tileset;
    private GameMap gameMap;
    private Player player;

    @Override
    public void create() {
        try {
            batch = new SpriteBatch();
            gameMap = MapLoader.loadMap("game_data.json");
            if (gameMap == null) {
                logger.error("Failed to load game map");
                return;
            }
            tileset = new Texture(Gdx.files.internal(gameMap.levels.get(0).layers.get(0).tilesSheetFile));
            player = new Player("player_texture.png", 50, 50, 200); // Adjust the initial position and speed as needed
        } catch (Exception e) {
            logger.error("Error during create", e);
        }
    }

    @Override
    public void render() {
        try {
            ScreenUtils.clear(0.15f, 0.15f, 0.2f, 1f);
            batch.begin();
            renderMap();
            player.update(Gdx.graphics.getDeltaTime());
            checkCollisions();
            player.render(batch);
            batch.end();
        } catch (Exception e) {
            logger.error("Error during render", e);
        }
    }

    private void renderMap() {
        try {
            GameMap.Level.Layer layer = gameMap.levels.get(0).layers.get(0);
            int tileWidth = layer.tilesWidth;
            int tileHeight = layer.tilesHeight;
            int mapWidth = layer.tileMap[0].length * tileWidth;
            int mapHeight = layer.tileMap.length * tileHeight;

            float scaleX = (float) Gdx.graphics.getWidth() / mapWidth;
            float scaleY = (float) Gdx.graphics.getHeight() / mapHeight;

            for (int y = 0; y < layer.tileMap.length; y++) {
                for (int x = 0; x < layer.tileMap[y].length; x++) {
                    int tileId = layer.tileMap[y][x];
                    if (tileId != 0) {
                        int srcX = (tileId % (tileset.getWidth() / tileWidth)) * tileWidth;
                        int srcY = (tileId / (tileset.getWidth() / tileWidth)) * tileHeight;
                        batch.draw(tileset, x * tileWidth * scaleX, Gdx.graphics.getHeight() - (y + 1) * tileHeight * scaleY, tileWidth * scaleX, tileHeight * scaleY, srcX, srcY, tileWidth, tileHeight, false, false);
                    }
                }
            }
        } catch (Exception e) {
            logger.error("Error during renderMap", e);
        }
    }

    private void checkCollisions() {
        for (GameMap.Level.Zone zone : gameMap.levels.get(0).zones) {
            Rectangle zoneRect = new Rectangle(zone.x, zone.y, zone.width, zone.height);
            if (player.getBounds().overlaps(zoneRect)) {
                // Handle collision (e.g., stop player movement, adjust position, etc.)
                logger.debug("Collision detected with zone: " + zone.type);
            }
        }
    }

    @Override
    public void dispose() {
        batch.dispose();
        tileset.dispose();
        player.dispose();
    }
}
