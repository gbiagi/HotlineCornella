const express = require('express');
const GameLogic = require('./gameLogic.js');
const webSockets = require('./utilsWebSockets.js');
const GameLoop = require('./utilsGameLoop.js');

const debug = true;
const port = process.env.PORT || 8888;
let players = [];

// Inicialitzar WebSockets i la lògica del joc
const ws = new webSockets();


// Inicialitzar servidor Express
const app = express();
app.use(express.static('public'));
app.use(express.json());

// Inicialitzar servidor HTTP
const httpServer = app.listen(port, () => {
    console.log(`Servidor HTTP escoltant a: http://localhost:${port}`);
});

// Gestionar WebSockets
ws.init(httpServer, port);

ws.onConnection = (socket, id) => {
    if (debug) console.log("WebSocket client connected: " + id);
    // Wait 3s before sending the welcome message
    socket.send(JSON.stringify({ type: "connected", from: "server" }));
    players.push(socket);
    console.log("Players connected: " + players.length);
    if (players.length === 2) {
        launchGame();
    }
};

ws.onMessage = (socket, id, msg) => {
    if (debug) console.log(`New message from ${id}: ${msg}`);
    const message = JSON.parse(msg);
    switch (message.type) {
        case "playerMove":
            players.forEach(player => {
                if (player !== socket) {
                    player.send(msg);
                }
            });
            break;
        case "playerStopped":
            players.forEach(player => {
                if (player !== socket) {
                    player.send(msg);
                }
            });
            break;
        case "playerShoot":
            players.forEach(player => {
                if (player !== socket) {
                    player.send(msg);
                }
            });
            break;
    }
};

ws.onClose = (socket, id) => {
    if (debug) console.log("WebSocket client disconnected: " + id);
    players = players.filter(player => player !== socket);
    console.log("Players connected: " + players.length);
    ws.broadcast(JSON.stringify({ type: "disconnected", from: "server" }));
};

function launchGame() {
    console.log("Launching game...");
    ws.broadcast(JSON.stringify({ type: "gameStart", from: "server" }));
}


// Gestionar el tancament del servidor
process.on('SIGTERM', shutDown);
process.on('SIGINT', shutDown);

function shutDown() {
    console.log('Rebuda senyal de tancament, aturant el servidor...');
    httpServer.close();
    ws.end();
    process.exit(0);
}
