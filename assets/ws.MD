To display other players in your game and show their movements, you need to implement the following changes:

1. Create a player identification system
2. Enhance your message classes
3. Update the `handleServerMessage` method in `WsClient` class
4. Process incoming messages in the `Main` class

Here's how to implement these changes:

First, create a new message class for player identification:

```java
package com.hotlinecornella;

public class PlayerConnectMessage {
    public String type = "playerConnect";
    public String playerId;

    public PlayerConnectMessage(String playerId) {
        this.playerId = playerId;
    }
}
```

Then modify your `PlayerMoveMessage` class to include player ID:

```java
package com.hotlinecornella;

public class PlayerMoveMessage {
    public String type = "playerMove";
    public String playerId;
    public float x;
    public float y;

    public PlayerMoveMessage(String playerId, float x, float y) {
        this.playerId = playerId;
        this.x = x;
        this.y = y;
    }
}
```

Update the `WsClient` class to handle messages and pass them to the main game:

```java
package com.hotlinecornella;

import org.java_websocket.client.WebSocketClient;
import org.java_websocket.handshake.ServerHandshake;
import com.badlogic.gdx.utils.Json;
import com.badlogic.gdx.utils.JsonReader;
import com.badlogic.gdx.utils.JsonValue;

import java.net.URI;

import static com.hotlinecornella.Main.logger;

public class WsClient extends WebSocketClient {
    private Main gameInstance;
    private String playerId;

    public WsClient(URI serverUri, Main gameInstance) {
        super(serverUri);
        this.gameInstance = gameInstance;
        this.playerId = java.util.UUID.randomUUID().toString();
    }

    @Override
    public void onOpen(ServerHandshake handshakedata) {
        logger.debug("WebSocket connected");
        // Send initial connect message with player ID
        PlayerConnectMessage connectMsg = new PlayerConnectMessage(playerId);
        send(new Json().toJson(connectMsg));
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

    private void handleServerMessage(String message) {
        System.out.println("Received message: " + message);
        JsonValue jsonValue = new JsonReader().parse(message);
        String type = jsonValue.getString("type");

        if ("playerMove".equals(type)) {
            String msgPlayerId = jsonValue.getString("playerId");
            if (!playerId.equals(msgPlayerId)) {
                float x = jsonValue.getFloat("x");
                float y = jsonValue.getFloat("y");
                gameInstance.updatePlayerPosition(msgPlayerId, x, y);
            }
        } else if ("playerConnect".equals(type)) {
            String msgPlayerId = jsonValue.getString("playerId");
            if (!playerId.equals(msgPlayerId)) {
                gameInstance.addNewPlayer(msgPlayerId);
            }
        }
    }

    public String getPlayerId() {
        return playerId;
    }
}
```

Finally, update the `Main` class to handle other players:

```java
package com.hotlinecornella;

// imports remain the same

public class Main extends ApplicationAdapter {
    // Existing fields remain the same
    private String playerId;

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

            // Initialize WebSocket connection passing this instance
            webSocketClient = new WsClient(new URI("ws://localhost:8888"), this);
            webSocketClient.connect();
            playerId = webSocketClient.getPlayerId();
        } catch (URISyntaxException e) {
            logger.error("WebSocket URI error", e);
        } catch (Exception e) {
            logger.error("Error during create", e);
        }
    }

    // Methods for handling other players
    public void addNewPlayer(String id) {
        // Create new player at a default position
        if (!otherPlayers.containsKey(id)) {
            logger.debug("Adding new player with ID: " + id);
            Player newPlayer = new Player("images/player1_idle.png", "images/player1_run.png", 100, 100, 1.5f);
            otherPlayers.put(id, newPlayer);
        }
    }

    public void updatePlayerPosition(String id, float x, float y) {
        if (!otherPlayers.containsKey(id)) {
            // Create player if they don't exist yet
            addNewPlayer(id);
        }

        Player otherPlayer = otherPlayers.get(id);
        if (otherPlayer != null) {
            // Calculate movement direction
            float deltaX = x - otherPlayer.getX();
            float deltaY = y - otherPlayer.getY();

            // Update position and animation state
            otherPlayer.setPosition(x, y);
            otherPlayer.setRunning(deltaX != 0 || deltaY != 0);
        }
    }

    private void sendPlayerMoveMessage() {
        PlayerMoveMessage message = new PlayerMoveMessage(playerId, player.getX(), player.getY());
        webSocketClient.send(new Json().toJson(message));
    }

    // Rest of the class remains the same
}
```

Update the `WsClient` constructor call in `Main.create()` method to pass the `this` reference:

```java
webSocketClient = new WsClient(new URI("ws://localhost:8888"), this);
```

These changes will:
1. Give each player a unique ID
2. Send player position updates to the server
3. Process incoming player updates
4. Add and update other players in your game

The server will broadcast all messages to all clients, enabling you to see other connected players and their movements.
