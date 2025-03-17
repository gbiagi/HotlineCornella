package com.hotlinecornella;

import com.badlogic.gdx.ApplicationAdapter;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.glutils.ShapeRenderer;
import com.badlogic.gdx.math.Rectangle;
import com.badlogic.gdx.utils.ScreenUtils;
import com.badlogic.gdx.utils.Logger;

public class Main extends ApplicationAdapter {
    private static final Logger logger = new Logger(Main.class.getName(), Logger.DEBUG);
    private SpriteBatch batch;
    private Texture tileset;
    private GameMap gameMap;
    private Player player;
    private ShapeRenderer shapeRenderer;

    @Override
    public void create() {
        try {
            batch = new SpriteBatch();
            shapeRenderer = new ShapeRenderer();
            gameMap = MapParser.loadMap("game_data.json");
            if (gameMap == null) {
                logger.error("Failed to load game map");
                return;
            }
            tileset = new Texture(Gdx.files.internal(gameMap.levels.getFirst().layers.getFirst().tilesSheetFile));
            player = new Player("images/player1_idle.png", "images/player1_run.png", 50, 150, 1.5f);
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
            player.render(batch);
            batch.end();

            shapeRenderer.begin(ShapeRenderer.ShapeType.Line);
            shapeRenderer.setColor(1, 1, 1, 1);
            float scaleX = (float) Gdx.graphics.getWidth() / (gameMap.levels.getFirst().layers.getFirst().tileMap[0].length * gameMap.levels.getFirst().layers.getFirst().tilesWidth);
            float scaleY = (float) Gdx.graphics.getHeight() / (gameMap.levels.getFirst().layers.getFirst().tileMap.length * gameMap.levels.getFirst().layers.getFirst().tilesHeight);
            for (GameMap.Level.Zone zone : gameMap.levels.getFirst().zones) {
                shapeRenderer.rect(zone.x * scaleX, (Gdx.graphics.getHeight() - (zone.y + zone.height) * scaleY), zone.width * scaleX, zone.height * scaleY);
            }
            shapeRenderer.end();
        } catch (Exception e) {
            logger.error("Error during render", e);
        }
    }

    private void handleInput() {
        float moveSpeed = 100 * Gdx.graphics.getDeltaTime();
        float nextX = player.getX();
        float nextY = player.getY();

        Direction currentDirection = null;
        if (Gdx.input.isKeyPressed(Input.Keys.LEFT)) {
            nextX -= moveSpeed;
            if (willCollide(nextX, nextY)) {
                player.move(-moveSpeed, 0);
                currentDirection = Direction.LEFT;
            }
        }
        if (Gdx.input.isKeyPressed(Input.Keys.RIGHT)) {
            nextX += moveSpeed;
            if (willCollide(nextX, nextY)) {
                player.move(moveSpeed, 0);
                currentDirection = Direction.RIGHT;
            }
        }
        if (Gdx.input.isKeyPressed(Input.Keys.UP)) {
            nextY += moveSpeed;
            if (willCollide(nextX, nextY)) {
                player.move(0, moveSpeed);
                currentDirection = Direction.UP;
            }
        }
        if (Gdx.input.isKeyPressed(Input.Keys.DOWN)) {
            nextY -= moveSpeed;
            if (willCollide(nextX, nextY)) {
                player.move(0, -moveSpeed);
                currentDirection = Direction.DOWN;
            }
        }

        if (!(Gdx.input.isKeyPressed(Input.Keys.LEFT) || Gdx.input.isKeyPressed(Input.Keys.RIGHT) ||
            Gdx.input.isKeyPressed(Input.Keys.UP) || Gdx.input.isKeyPressed(Input.Keys.DOWN))) {
            player.setRunning(false);
        }
    }
    private boolean willCollide(float nextX, float nextY) {
        float scaleX = (float) Gdx.graphics.getWidth() / (gameMap.levels.getFirst().layers.getFirst().tileMap[0].length * gameMap.levels.getFirst().layers.getFirst().tilesWidth);
        float scaleY = (float) Gdx.graphics.getHeight() / (gameMap.levels.getFirst().layers.getFirst().tileMap.length * gameMap.levels.getFirst().layers.getFirst().tilesHeight);

        Rectangle nextBounds = new Rectangle(nextX, nextY, player.getBounds().width, player.getBounds().height);

        for (GameMap.Level.Zone zone : gameMap.levels.getFirst().zones) {
            Rectangle zoneRect = new Rectangle(zone.x * scaleX, (Gdx.graphics.getHeight() - (zone.y + zone.height) * scaleY), zone.width * scaleX, zone.height * scaleY);

            if (nextBounds.overlaps(zoneRect) && !zone.type.equals("GameZone")) {
                logger.debug("Collision detected at (" + nextX + "," + nextY + ") with " + zone.type);
                return false;
            }
        }
        return true;
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
        player.dispose();
        shapeRenderer.dispose();
    }
}
