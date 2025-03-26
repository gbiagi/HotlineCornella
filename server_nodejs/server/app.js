const express = require('express');
const webSockets = require('./utilsWebSockets.js');
const e = require('express');

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
    players.push(socket);
    console.log("Players connected: " + players.length);
    if (players.length === 2) {
        launchGame();
    }
};

ws.onMessage = (socket, id, msg) => {
    //if (debug) console.log(`New message from ${id}: ${msg}`);
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
        case "playerHit":
            players.forEach(player => {
                if (player !== socket) {
                    player.send(msg);
                }
            });
            break;
        case "playerDead":
            players.forEach(player => {
                if (player !== socket) {
                    player.send(msg);
                }
            });
            break;
        case "gameOver":
            players.forEach(player => {
                if (player !== socket) {
                    player.send(JSON.stringify({ type: "gameOver", gameWon: true }));
                } else {
                    player.send(JSON.stringify({ type: "gameOver", gameWon: false }));
                }
            });
            endGame();
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
    players.forEach(player => {
        player.send(JSON.stringify({ type: "gameStart", position: players.indexOf(player) + 1 }));
    });
    //ws.broadcast(JSON.stringify({ type: "gameStart", from: "server" }));
}

function endGame() {
    console.log("Game over...");
    //ws.broadcast(JSON.stringify({ type: "gameOver", from: "server" }));
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
