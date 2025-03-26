package com.hotlinecornella;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input;
import com.badlogic.gdx.ScreenAdapter;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.glutils.ShapeRenderer;
import com.badlogic.gdx.math.Rectangle;
import com.badlogic.gdx.utils.ScreenUtils;
import com.badlogic.gdx.utils.Json;
import org.json.JSONObject;

import java.util.HashMap;
import java.util.Iterator;

public class GameScreen extends ScreenAdapter {
    private WsClient webSocketClient;
    private final Main game;
    private SpriteBatch batch;
    private Texture tileset;
    private GameMap gameMap;
    private Player player;
    private Player rival;
    private ShapeRenderer shapeRenderer;
    private boolean isShooting = false;
    int position;

    public GameScreen(Main game, int position) {
        this.position = position;
        this.game = game;
        webSocketClient = WsClient.getInstance(game);
        create();
    }

    private void create() {
        try {
            batch = new SpriteBatch();
            shapeRenderer = new ShapeRenderer();
            gameMap = MapParser.loadMap("game_data.json");
            if (gameMap == null) {
                System.out.println("Failed to load game map");
                return;
            }
            tileset = new Texture(Gdx.files.internal(gameMap.levels.getFirst().layers.getFirst().tilesSheetFile));

            if (position == 1) {
                player = new Player("images/player1_idle.png", "images/player1_run.png", 50, 150, 1.5f);
                rival = new Player("images/player2_idle.png", "images/player2_run.png", 700, 650, 1.5f);
            } else {
                rival = new Player("images/player1_idle.png", "images/player1_run.png", 50, 150, 1.5f);
                player = new Player("images/player2_idle.png", "images/player2_run.png", 700, 650, 1.5f);
            }

        } catch (Exception e) {
            System.out.println("Error during create " + e);
        }
    }

    @Override
    public void render(float delta) {
        try {
            handleInput();
            ScreenUtils.clear(0.15f, 0.15f, 0.2f, 1f);

            // Render the map and sprites
            batch.begin();
            renderMap();
            // Render player
            player.update(delta);
            player.render(batch);
            // Render rival
            rival.update(delta);
            rival.render(batch);
            batch.end();

            // Then render the health bars
            player.renderHealthBar(shapeRenderer);
            rival.renderHealthBar(shapeRenderer);

            // Continue with bullet collisions
            checkBulletCollisions();
        } catch (Exception e) {
            System.out.println("Error during render " + e);
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
                        batch.draw(tileset, x * tileWidth * scaleX,
                                Gdx.graphics.getHeight() - (y + 1) * tileHeight * scaleY, tileWidth * scaleX,
                                tileHeight * scaleY, srcX, srcY, tileWidth, tileHeight, false, false);
                    }
                }
            }
        } catch (Exception e) {
            System.out.println("Error during renderMap" + e);
        }
    }

    private void sendPlayerMoveMessage() {
        JSONObject message = new JSONObject();
        message.put("type", "playerMove");
        message.put("x", player.getX());
        message.put("y", player.getY());
        webSocketClient.send(message.toString());
    }
    private void sendPlayerStoppedMessage() {
        JSONObject message = new JSONObject();
        message.put("type", "playerStopped");
        message.put("running", "false");
        webSocketClient.send(message.toString());
    }
    private void sendPlayerShootMessage(Direction direction) {
        JSONObject message = new JSONObject();
        message.put("type", "playerShoot");
        message.put("direction", direction.toString());
        webSocketClient.send(message.toString());
    }
    private void sendPlayerHitMessage() {
        JSONObject message = new JSONObject();
        message.put("type", "playerHit");
        webSocketClient.send(message.toString());
    }
    private void sendGameOverMessage(boolean gameWon) {
        JSONObject message = new JSONObject();
        message.put("type", "gameOver");
        message.put("gameWon", gameWon);
        webSocketClient.send(message.toString());
    }

    public void updateRivalPosition(float x, float y) {
        // Calculate the difference in position
        float deltaX = x - rival.getX();
        float deltaY = y - rival.getY();

        // Move the rival by the calculated difference
        rival.move(deltaX, deltaY);
        rival.setRunning(true);
    }
    public void stopRival() {
        rival.setRunning(false);
    }
    public void rivalShoot(Direction direction) {
        rival.shoot(direction);
    }
    public void rivalHit() {
        rival.playerHit();
    }

    private void handleInput() {
        float moveSpeed = 100 * Gdx.graphics.getDeltaTime();
        float nextX = player.getX();
        float nextY = player.getY();

        Direction currentDirection = null;
        if (Gdx.input.isKeyPressed(Input.Keys.LEFT) || Gdx.input.isKeyPressed(Input.Keys.A)) {
            nextX -= moveSpeed;
            if (willCollide(nextX, nextY)) {
                player.move(-moveSpeed, 0);
                currentDirection = Direction.LEFT;
                sendPlayerMoveMessage();
            }
        } else if (Gdx.input.isKeyPressed(Input.Keys.RIGHT) || Gdx.input.isKeyPressed(Input.Keys.D)) {
            nextX += moveSpeed;
            if (willCollide(nextX, nextY)) {
                player.move(moveSpeed, 0);
                currentDirection = Direction.RIGHT;
                sendPlayerMoveMessage();
            }
        } else if (Gdx.input.isKeyPressed(Input.Keys.UP) || Gdx.input.isKeyPressed(Input.Keys.W)) {
            nextY += moveSpeed;
            if (willCollide(nextX, nextY)) {
                player.move(0, moveSpeed);
                currentDirection = Direction.UP;
                sendPlayerMoveMessage();
            }
        } else if (Gdx.input.isKeyPressed(Input.Keys.DOWN) || Gdx.input.isKeyPressed(Input.Keys.S)) {
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
                sendPlayerShootMessage(currentDirection);
            }
        } else {
            isShooting = false;
        }

        if (!(Gdx.input.isKeyPressed(Input.Keys.LEFT) || Gdx.input.isKeyPressed(Input.Keys.RIGHT) ||
                Gdx.input.isKeyPressed(Input.Keys.UP) || Gdx.input.isKeyPressed(Input.Keys.DOWN) ||
                Gdx.input.isKeyPressed(Input.Keys.W) || Gdx.input.isKeyPressed(Input.Keys.A) ||
                Gdx.input.isKeyPressed(Input.Keys.S) || Gdx.input.isKeyPressed(Input.Keys.D)) && player.isRunning()) {
            player.setRunning(false);
            sendPlayerStoppedMessage();
        }
    }
    private boolean willCollide(float nextX, float nextY) {
        float scaleX = (float) Gdx.graphics.getWidth() / (gameMap.levels.getFirst().layers.getFirst().tileMap[0].length
                * gameMap.levels.getFirst().layers.getFirst().tilesWidth);
        float scaleY = (float) Gdx.graphics.getHeight() / (gameMap.levels.getFirst().layers.getFirst().tileMap.length
                * gameMap.levels.getFirst().layers.getFirst().tilesHeight);

        Rectangle nextBounds = new Rectangle(nextX, nextY, player.getBounds().width, player.getBounds().height);

        for (GameMap.Level.Zone zone : gameMap.levels.getFirst().zones) {
            Rectangle zoneRect = new Rectangle(zone.x * scaleX,
                    (Gdx.graphics.getHeight() - (zone.y + zone.height) * scaleY), zone.width * scaleX,
                    zone.height * scaleY);

            if (zone.type.equals("GameZone")) {
                if (!zoneRect.contains(nextBounds)) {
                    System.out.println("Player movement restricted to GameZone bounds");
                    return false;
                }
            } else if (nextBounds.overlaps(zoneRect)) {
                System.out.println("Collision detected at (" + nextX + "," + nextY + ") with " + zone.type);
                return false;
            }
        }
        // Check collision with rival
        Rectangle rivalBounds = rival.getBounds();
        if (nextBounds.overlaps(rivalBounds)) {
            System.out.println("Collision detected with rival at (" + nextX + "," + nextY + ")");
            return false;
        }
        return true;
    }
    private void checkBulletCollisions() {
        float scaleX = (float) Gdx.graphics.getWidth() / (gameMap.levels.getFirst().layers.getFirst().tileMap[0].length
                * gameMap.levels.getFirst().layers.getFirst().tilesWidth);
        float scaleY = (float) Gdx.graphics.getHeight() / (gameMap.levels.getFirst().layers.getFirst().tileMap.length
                * gameMap.levels.getFirst().layers.getFirst().tilesHeight);

        // Check both players bullets collision
        checkPlayerBullets(player, scaleX, scaleY);
        checkPlayerBullets(rival, scaleX, scaleY);
    }
    private void checkPlayerBullets(Player playerChecked, float scaleX, float scaleY) {
        Iterator<Bullet> bulletIterator = playerChecked.getBullets().iterator();
        while (bulletIterator.hasNext()) {
            Bullet bullet = bulletIterator.next();
            Rectangle bulletBounds = bullet.getBounds();
            Rectangle rivalBounds = rival.getBounds();
            Rectangle playerBounds = player.getBounds();

            if (playerChecked.equals(player)) {
                if (bulletBounds.overlaps(rivalBounds)) {
                    System.out.println("Bullet collision detected with rival");
                    bulletIterator.remove();
                    break;
                }
            } else if (bulletBounds.overlaps(playerBounds)) {
                System.out.println("Bullet collision detected with player");
                bulletIterator.remove();
                player.playerHit();
                sendPlayerHitMessage();
                if (player.getHealth() <= 0) {
                    System.out.println("Player defeated");
                    // End game
                    sendGameOverMessage(false);
                }
                break;
            }
            for (GameMap.Level.Zone zone : gameMap.levels.getFirst().zones) {
                Rectangle zoneRect = new Rectangle(zone.x * scaleX,
                        (Gdx.graphics.getHeight() - (zone.y + zone.height) * scaleY), zone.width * scaleX,
                        zone.height * scaleY);
                if (bulletBounds.overlaps(zoneRect) && !zone.type.equals("GameZone")) {
                    System.out.println("Bullet collision detected with " + zone.type);
                    bulletIterator.remove();
                    break;
                } else if (!bulletBounds.overlaps(zoneRect) && zone.type.equals("GameZone")) {
                    bulletIterator.remove();
                    break;
                }
            }

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
