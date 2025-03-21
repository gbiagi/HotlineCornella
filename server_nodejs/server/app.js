const express = require('express');
const GameLogic = require('./gameLogic.js');
const webSockets = require('./utilsWebSockets.js');
const GameLoop = require('./utilsGameLoop.js');

const debug = true;
const port = process.env.PORT || 8888;

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
    setTimeout(() => {
        console.log("Sending welcome message ");
        socket.send(JSON.stringify({ type: "welcome", from: "server" }));
    }, 5000);
    ws.broadcast(JSON.stringify({ type: "ok", from: "server" }));
    socket.send(JSON.stringify({ type: "welcome", from: "server" }));

};

ws.onMessage = (socket, id, msg) => {
    if (debug) console.log(`New message from ${id}: ${msg}`);
    ws.broadcast(msg)
};

ws.onClose = (socket, id) => {
    if (debug) console.log("WebSocket client disconnected: " + id);
    ws.broadcast(JSON.stringify({ type: "disconnected", from: "server" }));
};



// Gestionar el tancament del servidor
process.on('SIGTERM', shutDown);
process.on('SIGINT', shutDown);

function shutDown() {
    console.log('Rebuda senyal de tancament, aturant el servidor...');
    httpServer.close();
    ws.end();
    process.exit(0);
}
