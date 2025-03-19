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
import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.Iterator;

public class Main extends ApplicationAdapter {
    private static final Logger logger = new Logger(Main.class.getName(), Logger.DEBUG);
    private SpriteBatch batch;
    private Texture tileset;
    private GameMap gameMap;
    private Player player;
    private ShapeRenderer shapeRenderer;
    private boolean isShooting = false;
    private WebSocketClient webSocketClient;
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
            webSocketClient = new WebSocketClient(new URI("ws://localhost:8888")) {
                @Override
                public void onOpen(ServerHandshake handshakedata) {
                    logger.debug("WebSocket connected");
                }

                @Override
                public void onMessage(String message) {
                    handleServerMessage(message);
                }

                @Override
                public void onClose(int code, String reason, boolean remote) {
                    logger.debug("WebSocket closed: " + reason);
                }

                @Override
                public void onError(Exception ex) {
                    logger.error("WebSocket error", ex);
                }
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
        batch.end();

        // Check bullet collisions
        checkBulletCollisions();
    }

    private void handleInput() {
        float moveSpeed = 100 * Gdx.graphics.getDeltaTime();
        float nextX = player.getX();
        float nextY = player.getY();
        boolean moved = false;

        Direction currentDirection = null;

        if (Gdx.input.isKeyPressed(Input.Keys.LEFT)) {
            nextX -= moveSpeed;
            if (willCollide(nextX, nextY)) {
                player.move(-moveSpeed, 0);
                currentDirection = Direction.LEFT;
                moved = true;
            }
        }
        if (Gdx.input.isKeyPressed(Input.Keys.RIGHT)) {
            nextX += moveSpeed;
            if (willCollide(nextX, nextY)) {
                player.move(moveSpeed, 0);
                currentDirection = Direction.RIGHT;
                moved = true;
            }
        }
        if (Gdx.input.isKeyPressed(Input.Keys.UP)) {
            nextY += moveSpeed;
            if (willCollide(nextX, nextY)) {
                player.move(0, moveSpeed);
                currentDirection = Direction.UP;
                moved = true;
            }
        }
        if (Gdx.input.isKeyPressed(Input.Keys.DOWN)) {
            nextY -= moveSpeed;
            if (willCollide(nextX, nextY)) {
                player.move(0, -moveSpeed);
                currentDirection = Direction.DOWN;
                moved = true;
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

        // Send the updated position to the server if the player moved
        if (moved && webSocketClient != null && webSocketClient.isOpen()) {
            // Manually construct the JSON string to match the static message format
            String message = String.format(
                "{\"type\":\"playerMove\",\"id\":\"%s\",\"x\":%.2f,\"y\":%.2f}",
                player.getId(),
                player.getX(),
                player.getY()
            );
            logger.debug("Sending movement message: " + message);
            webSocketClient.send(message.getBytes(StandardCharsets.UTF_8)); // Send the message
        }
    }

    private boolean willCollide(float nextX, float nextY) {
        float scaleX = (float) Gdx.graphics.getWidth() / (gameMap.levels.getFirst().layers.getFirst().tileMap[0].length * gameMap.levels.getFirst().layers.getFirst().tilesWidth);
        float scaleY = (float) Gdx.graphics.getHeight() / (gameMap.levels.getFirst().layers.getFirst().tileMap.length * gameMap.levels.getFirst().layers.getFirst().tilesHeight);

        Rectangle nextBounds = new Rectangle(nextX, nextY, player.getBounds().width, player.getBounds().height);

        for (GameMap.Level.Zone zone : gameMap.levels.getFirst().zones) {
            Rectangle zoneRect = new Rectangle(zone.x * scaleX, (Gdx.graphics.getHeight() - (zone.y + zone.height) * scaleY), zone.width * scaleX, zone.height * scaleY);

            if (zone.type.equals("GameZone")) {
                if (!zoneRect.contains(nextBounds)) {
                    return false;
                }
            } else if (nextBounds.overlaps(zoneRect)) {
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

    private void handleServerMessage(String message) {
        try {
            JsonValue json = new JsonReader().parse(message);
            String type = json.getString("type");

            if (type.equals("update")) {
                JsonValue players = json.get("gameState").get("players");

                for (JsonValue playerJson : players) {
                    String playerId = playerJson.getString("id");
                    float x = playerJson.getFloat("x");
                    float y = playerJson.getFloat("y");

                    if (!playerId.equals(player.getId())) { // Ignore updates for the local player
                        Player otherPlayer = otherPlayers.get(playerId);
                        if (otherPlayer == null) {
                            // Schedule the creation of the Player object on the main thread
                            Gdx.app.postRunnable(() -> {
                                Player newPlayer = new Player("images/player2_idle.png", "images/player2_run.png", x, y, 1.5f);
                                otherPlayers.put(playerId, newPlayer);
                            });
                        } else {
                            otherPlayer.setPosition(x, y);
                        }
                    }
                }
            }
        } catch (Exception e) {
            logger.error("Error handling server message", e);
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
