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
import org.java_websocket.client.WebSocketClient;
import org.java_websocket.handshake.ServerHandshake;
import com.badlogic.gdx.utils.Json;
import com.badlogic.gdx.utils.JsonValue;
import com.badlogic.gdx.utils.JsonReader;

import java.net.URI;
import java.net.URISyntaxException;
import java.util.HashMap;
import java.util.Iterator;

public class Main extends ApplicationAdapter {
    static final Logger logger = new Logger(Main.class.getName(), Logger.DEBUG);
    private SpriteBatch batch;
    private Texture tileset;
    private GameMap gameMap;
    private Player player;
    private ShapeRenderer shapeRenderer;
    private boolean isShooting = false;
    private WsClient webSocketClient;
    private HashMap<String, Player> otherPlayers = new HashMap<>(); // Store other players

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

            // Initialize WebSocket connection
            webSocketClient = new WsClient(new URI("ws://localhost:8888")) {
            };
            webSocketClient.connect();
        } catch (URISyntaxException e) {
            logger.error("WebSocket URI error", e);
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

            // Render other players
            for (Player otherPlayer : otherPlayers.values()) {
                otherPlayer.update(Gdx.graphics.getDeltaTime());
                otherPlayer.render(batch);
            }
            checkBulletCollisions();
            batch.end();

            // Show map hitbox
            /*shapeRenderer.begin(ShapeRenderer.ShapeType.Line);
            shapeRenderer.setColor(1, 1, 1, 1);
            float scaleX = (float) Gdx.graphics.getWidth() / (gameMap.levels.getFirst().layers.getFirst().tileMap[0].length * gameMap.levels.getFirst().layers.getFirst().tilesWidth);
            float scaleY = (float) Gdx.graphics.getHeight() / (gameMap.levels.getFirst().layers.getFirst().tileMap.length * gameMap.levels.getFirst().layers.getFirst().tilesHeight);
            for (GameMap.Level.Zone zone : gameMap.levels.getFirst().zones) {
                shapeRenderer.rect(zone.x * scaleX, (Gdx.graphics.getHeight() - (zone.y + zone.height) * scaleY), zone.width * scaleX, zone.height * scaleY);
            }
            shapeRenderer.end();*/

        } catch (Exception e) {
            logger.error("Error during render", e);
        }
    }

    private void handleInput() {
        float moveSpeed = 100 * Gdx.graphics.getDeltaTime();
        float nextX = player.getX();
        float nextY = player.getY();

        Direction currentDirection = null;
        if (Gdx.input.isKeyPressed(Input.Keys.LEFT) || Gdx.input.isKeyPressed(Input.Keys.A)){
            nextX -= moveSpeed;
            if (willCollide(nextX, nextY)) {
                player.move(-moveSpeed, 0);
                currentDirection = Direction.LEFT;
                sendPlayerMoveMessage();
            }
        }
        else if (Gdx.input.isKeyPressed(Input.Keys.RIGHT) || Gdx.input.isKeyPressed(Input.Keys.D)) {
            nextX += moveSpeed;
            if (willCollide(nextX, nextY)) {
                player.move(moveSpeed, 0);
                currentDirection = Direction.RIGHT;
                sendPlayerMoveMessage();
            }
        }
        else if (Gdx.input.isKeyPressed(Input.Keys.UP) || Gdx.input.isKeyPressed(Input.Keys.W)) {
            nextY += moveSpeed;
            if (willCollide(nextX, nextY)) {
                player.move(0, moveSpeed);
                currentDirection = Direction.UP;
                sendPlayerMoveMessage();
            }
        }
        else if (Gdx.input.isKeyPressed(Input.Keys.DOWN) || Gdx.input.isKeyPressed(Input.Keys.S)) {
            nextY -= moveSpeed;
            if (willCollide(nextX, nextY)) {
                player.move(0, -moveSpeed);
                currentDirection = Direction.DOWN;
                sendPlayerMoveMessage();
            }
        }

        if (Gdx.input.isKeyPressed(Input.Keys.SPACE)) {
            if (!isShooting && currentDirection != null) {
                player.shoot(currentDirection);
                isShooting = true;
            }
        } else {
            isShooting = false;
        }

        if (!(Gdx.input.isKeyPressed(Input.Keys.LEFT) || Gdx.input.isKeyPressed(Input.Keys.RIGHT) ||
            Gdx.input.isKeyPressed(Input.Keys.UP) || Gdx.input.isKeyPressed(Input.Keys.DOWN) ||
            Gdx.input.isKeyPressed(Input.Keys.W) || Gdx.input.isKeyPressed(Input.Keys.A) ||
            Gdx.input.isKeyPressed(Input.Keys.S) || Gdx.input.isKeyPressed(Input.Keys.D))) {
            player.setRunning(false);
        }
    }
    private void sendPlayerMoveMessage() {
        String message = player.getX() + ", " + player.getY();
        webSocketClient.send(new Json().toJson(message));
    }

    private boolean willCollide(float nextX, float nextY) {
        float scaleX = (float) Gdx.graphics.getWidth() / (gameMap.levels.getFirst().layers.getFirst().tileMap[0].length * gameMap.levels.getFirst().layers.getFirst().tilesWidth);
        float scaleY = (float) Gdx.graphics.getHeight() / (gameMap.levels.getFirst().layers.getFirst().tileMap.length * gameMap.levels.getFirst().layers.getFirst().tilesHeight);

        Rectangle nextBounds = new Rectangle(nextX, nextY, player.getBounds().width, player.getBounds().height);

        for (GameMap.Level.Zone zone : gameMap.levels.getFirst().zones) {
            Rectangle zoneRect = new Rectangle(zone.x * scaleX, (Gdx.graphics.getHeight() - (zone.y + zone.height) * scaleY), zone.width * scaleX, zone.height * scaleY);

            if (zone.type.equals("GameZone")) {
                if (!zoneRect.contains(nextBounds)) {
                    logger.debug("Player movement restricted to GameZone bounds");
                    return false;
                }
            } else if (nextBounds.overlaps(zoneRect)) {
                logger.debug("Collision detected at (" + nextX + "," + nextY + ") with " + zone.type);
                return false;
            }
        }
        return true;
    }

    private void checkBulletCollisions() {
        float scaleX = (float) Gdx.graphics.getWidth() / (gameMap.levels.getFirst().layers.getFirst().tileMap[0].length * gameMap.levels.getFirst().layers.getFirst().tilesWidth);
        float scaleY = (float) Gdx.graphics.getHeight() / (gameMap.levels.getFirst().layers.getFirst().tileMap.length * gameMap.levels.getFirst().layers.getFirst().tilesHeight);

        Iterator<Bullet> bulletIterator = player.getBullets().iterator();
        while (bulletIterator.hasNext()) {
            Bullet bullet = bulletIterator.next();
            Rectangle bulletBounds = bullet.getBounds();

            for (GameMap.Level.Zone zone : gameMap.levels.getFirst().zones) {
                Rectangle zoneRect = new Rectangle(zone.x * scaleX, (Gdx.graphics.getHeight() - (zone.y + zone.height) * scaleY), zone.width * scaleX, zone.height * scaleY);
                if (bulletBounds.overlaps(zoneRect) && !zone.type.equals("GameZone")) {
                    logger.debug("Bullet collision detected with " + zone.type);
                    bulletIterator.remove();
                    break;
                } else if (!bulletBounds.overlaps(zoneRect) && zone.type.equals("GameZone")) {
                    bulletIterator.remove();
                    break;
                }
            }
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
        player.dispose();
        shapeRenderer.dispose();
    }
}
