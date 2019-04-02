package com.snakydesign.watchtower

import org.java_websocket.WebSocket
import org.java_websocket.handshake.ClientHandshake
import org.java_websocket.server.WebSocketServer
import java.lang.Exception
import java.net.InetSocketAddress

/**
 * @author Adib Faramarzi (adibfara@gmail.com)
 */
class WatchTowerWebSocketServer(address: InetSocketAddress, private val messageHandler: MessageHandler) :
    WebSocketServer(address) {
    override fun onOpen(conn: WebSocket?, handshake: ClientHandshake?) {
        conn?.let { messageHandler.onOpened(it) }
    }

    override fun onClose(conn: WebSocket?, code: Int, reason: String?, remote: Boolean) {
        conn?.let { messageHandler.onClosed(it) }
    }

    override fun onMessage(conn: WebSocket?, message: String?) {

    }

    override fun onStart() {
    }

    override fun onError(conn: WebSocket?, ex: Exception?) {
        println("Websocket error occured:")
        ex?.printStackTrace()
    }

    interface MessageHandler {
        fun onOpened(connection: WebSocket) {

        }

        fun onClosed(connection: WebSocket) {

        }
    }
}

