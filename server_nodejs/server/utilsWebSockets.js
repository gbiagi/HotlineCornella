// Description: WebSocket server for the app

const WebSocket = require('ws')
const { v4: uuidv4 } = require('uuid')

class WebSocketManager {
    constructor() {
        this.clients = new Map(); // Store connected clients
    }

    // Send a message to a specific client
    sendToClient(clientId, message) {
        const client = this.clients.get(clientId);
        if (client && client.readyState === WebSocket.OPEN) {
            client.send(message);
        }
    }

    // Broadcast a message to all clients except the sender
    broadcastToOthers(excludeClientId, message) {
        this.clients.forEach((client, clientId) => {
            if (clientId !== excludeClientId && client.readyState === WebSocket.OPEN) {
                client.send(message);
            }
        });
    }

    // Add other WebSocket methods...
}

class Obj {

    init(httpServer, port) {

        // Define empty callbacks
        this.onConnection = (socket, id) => { }
        this.onMessage = (socket, id, obj) => { }
        this.onClose = (socket, id) => { }

        // Run WebSocket server
        this.ws = new WebSocket.Server({ server: httpServer })
        this.socketsClients = new Map()
        console.log(`Listening for WebSocket queries on ${port}`)

        // What to do when a websocket client connects
        this.ws.on('connection', (ws) => { this.newConnection(ws) })
    }

    end() {
        this.ws.close()
    }

    // A websocket client connects
    newConnection(con) {
        console.log("Client connected");
    
        // Generar ID únic per al client
        const id = "C" + uuidv4().substring(0, 5).toUpperCase();
        const metadata = { id };
        this.socketsClients.set(con, metadata);
    
        // Enviar missatge de benvinguda amb ID únic
        con.send(JSON.stringify({
            type: "welcome",
            id: id,
            message: "Welcome to the server"
        }));
    
        // Informar tots els clients de la nova connexió
        this.broadcast(JSON.stringify({
            type: "newClient",
            id: id
        }));
    
        if (this.onConnection && typeof this.onConnection === "function") {
            this.onConnection(con, id);
        }
    
        con.on("close", () => {
            this.closeConnection(con);
            this.socketsClients.delete(con);
        });
    
        con.on('message', (bufferedMessage) => { 
            this.newMessage(con, id, bufferedMessage);
        });
    }

    closeConnection(con) {
        if (this.onClose && typeof this.onClose === "function") {
            var id = this.socketsClients.get(con).id
            this.onClose(con, id)
        }
    }


    // Send a message to all websocket clients
    broadcast(message, excludeClientId = null) {
        this.ws.clients.forEach((client) => {
            const metadata = this.socketsClients.get(client);
            if (client.readyState === WebSocket.OPEN && (!excludeClientId || metadata.id !== excludeClientId)) {
                client.send(message);
            }
        });
    }

    // A message is received from a websocket client
    newMessage(ws, id, bufferedMessage) {
        const messageAsString = bufferedMessage.toString('utf8'); // Convert the message to a UTF-8 string
        if (this.onMessage && typeof this.onMessage === "function") {
            this.onMessage(ws, id, messageAsString);
        }
    }

    getClientData(id) {
        for (let [client, metadata] of this.socketsClients.entries()) {
            if (metadata.id === id) {
                return metadata;
            }
        }
        return null;
    }

    getClientsIds() {
        let clients = [];
        this.socketsClients.forEach((value, key) => {
            clients.push(value.id);
        });
        return clients;
    }

    getClientsData() {
        let clients = [];
        for (let [client, metadata] of this.socketsClients.entries()) {
            clients.push(metadata);
        }
        return clients;
    }
}

module.exports = Obj