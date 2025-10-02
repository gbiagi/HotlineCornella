# HotlineCornella

HotlineCornella is a small 2D top-down shooter built with libGDX. Players control a character that navigates tile-based maps, avoids hazards, and battles other players or AI. The repo contains a desktop client (LWJGL3) and a lightweight Node.js server used for testing and multiplayer prototypes.

Highlights
- Fast, arcade-style top-down shooting gameplay
- Tile-based levels and sprite animations
- Small, modular codebase structured for desktop and server platforms

Technologies
- Java and libGDX for core game logic (`core` module)
- LWJGL3 for the desktop platform (`lwjgl3` module)
- Node.js for the server (`server_nodejs`) with simple WebSocket utilities
- Gradle as the build system with an included Gradle Wrapper

Client (desktop)
- The `lwjgl3` module builds a desktop launcher using LWJGL3. It runs the libGDX application which depends on the shared `core` module for game logic, maps, sprites, and input handling.

Server
- The `server_nodejs` folder contains a minimal Node.js server used to coordinate multiplayer sessions and simplify local testing. It uses WebSockets to exchange player inputs and game state (positions, bullets, simple events). The Node server is intentionally lightweight:
	- Accepts WebSocket connections from clients
	- Broadcasts simple JSON messages (player join/leave, position updates, actions)
	- Contains basic game-loop or tick utilities to keep state updates regular (see `server/utilsGameLoop.js`)
	- Designed for local development and prototyping — not hardened for production use (no auth, no persistent storage, limited sanitization)

Where to look
- Shared game logic: `core/src/main/java/com/hotlinecornella`
- Desktop launcher: `lwjgl3/src/main/java` and `lwjgl3/build.gradle`
- Node server: `server_nodejs/server` (see `app.js`, `utilsGameLoop.js`, `utilsWebSockets.js`)

Running (PowerShell / Windows)
1. Build the Java project (from repo root):

```powershell
./gradlew.bat build
```

2. Run the desktop client (from repo root):

```powershell
./gradlew.bat lwjgl3:run
```

3. Run the Node.js server (requires Node.js installed). Open a terminal in `server_nodejs` and run:

```powershell
cd server_nodejs
node server/app.js
```

