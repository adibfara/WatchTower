package com.snakydesign.watchtower

import org.java_websocket.WebSocket
import org.java_websocket.handshake.ClientHandshake
import org.java_websocket.server.WebSocketServer
import java.lang.Exception
import java.net.InetSocketAddress

/**
 * @author Adib Faramarzi (adibfara@gmail.com)
 */
class WatchTowerWebsocketServer(address: InetSocketAddress, val messageHandler: WSMessageHandler) :
    WebSocketServer(address) {
    override fun onOpen(conn: WebSocket?, handshake: ClientHandshake?) {
        conn?.let { messageHandler.onOpened(it) }
        log("on open")
    }

    override fun onClose(conn: WebSocket?, code: Int, reason: String?, remote: Boolean) {
        log("on onClose")
    }

    override fun onMessage(conn: WebSocket?, message: String?) {

        log("on onMessage")
    }

    override fun onStart() {
        log("on onStart")
    }

    override fun onError(conn: WebSocket?, ex: Exception?) {
        log("on onError")
    }

    private fun log(log: String) {
        println(log)
    }
}

interface WSMessageHandler {
    fun onOpened(conn: WebSocket)
}