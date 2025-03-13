package com.hotlinecornella;

import com.badlogic.gdx.ApplicationAdapter;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.utils.ScreenUtils;
import com.badlogic.gdx.utils.Logger;

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
            tileset = new Texture(Gdx.files.internal(gameMap.levels.getFirst().layers.getFirst().tilesSheetFile));
            player = new Player("images/player1_idle.png", "images/player1_run.png", 50, 150); // Initialize player with texture and position
        } catch (Exception e) {
            logger.error("Error during create", e);
        }
    }

    @Override
    public void render() {
        try {
            handleInput();
            ScreenUtils.clear(0.15f, 0.15f, 0.2f, 1f);
            batch.begin();
            renderMap();
            player.update(Gdx.graphics.getDeltaTime());
            player.render(batch); // Render the player
            batch.end();
        } catch (Exception e) {
            logger.error("Error during render", e);
        }
    }
    private void handleInput() {
        float moveSpeed = 100 * Gdx.graphics.getDeltaTime();
        if (Gdx.input.isKeyPressed(Input.Keys.LEFT)) {
            player.move(-moveSpeed, 0);
        }
        if (Gdx.input.isKeyPressed(Input.Keys.RIGHT)) {
            player.move(moveSpeed, 0);
        }
        if (Gdx.input.isKeyPressed(Input.Keys.UP)) {
            player.move(0, moveSpeed);
        }
        if (Gdx.input.isKeyPressed(Input.Keys.DOWN)) {
            player.move(0, -moveSpeed);
        }
    }
    private void renderMap() {
        try {
            GameMap.Level.Layer layer = gameMap.levels.getFirst().layers.getFirst();
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

    @Override
    public void dispose() {
        batch.dispose();
        tileset.dispose();
        player.dispose(); // Dispose of player resources
    }
}
