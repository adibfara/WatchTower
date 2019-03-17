package com.snakydesign.watchtower

import com.snakydesign.watchtower.models.RequestData
import com.snakydesign.watchtower.models.ResponseData
import com.snakydesign.watchtower.models.TowerObserver
import org.java_websocket.WebSocket
import java.net.InetSocketAddress
import java.nio.charset.Charset

/**
 * @author Adib Faramarzi (adibfara@gmail.com)
 */
class WebsocketTowerObserver constructor(private val port: Int) : TowerObserver(), WSMessageHandler {


    private val html: String by lazy {
        val html = (this.javaClass.classLoader.getResourceAsStream("index.html")).readBytes()
            .toString(Charset.defaultCharset())
        return@lazy html
    }
    private val cssFile: String =
        this.javaClass.classLoader.getResourceAsStream("main.css").readBytes().toString(Charset.defaultCharset())
    private val javascriptFile: String =
        this.javaClass.classLoader.getResourceAsStream("main.js").readBytes().toString(Charset.defaultCharset())
    private val jqueryFile: String =
        this.javaClass.classLoader.getResourceAsStream("jquery.min.js").readBytes().toString(Charset.defaultCharset())
    private lateinit var server: WatchTowerServer
    private lateinit var websocketServer: WatchTowerWebsocketServer
    private var isStarted = false
    private var allResponses = mutableListOf<ResponseData>()

    private lateinit var serverThread: Thread
    private lateinit var websocketThread: Thread

    init {


    }

    @Synchronized
    override fun start() {
        if (!isStarted) {
            isStarted = true
            serverThread = Thread {
                server = WatchTowerServer(port, html, cssFile, javascriptFile, jqueryFile)
                server.start(1000, false)
            }.apply {
                start()
            }

            websocketThread = Thread {
                websocketServer = WatchTowerWebsocketServer(InetSocketAddress(5003), this@WebsocketTowerObserver)
                websocketServer.start()
            }.apply {
                start()
            }

        } else {
            throw Exception("Server is already started. Please stop if before starting using `stop()` function.")
        }
    }

    private fun checkIfEngineStarted() {
        if (!isStarted) throw Exception("Engine was never started! use WebtSocketEventReported.start() to start it.")
    }

    fun blockForRequests() {
        serverThread.join()
    }

    @Synchronized
    override fun stop() {
        if (isStarted) {
            isStarted = false
            server.stop()
            websocketServer.stop()
        } else {
            println("Server is not started. You can start it by using the `start()` function.")
        }
    }


    override fun showRequest(requestSent: RequestData, logLevel: WatchTowerInterceptor.LogLevel) {
        checkIfEngineStarted()
        sendMessage(WebsocketMessage.Request(requestSent))
    }

    override fun showResponse(responseReceived: ResponseData, logLevel: WatchTowerInterceptor.LogLevel) {
        checkIfEngineStarted()
        allResponses.add(responseReceived)
        sendMessage(WebsocketMessage.Response(responseReceived))
    }

    override fun showAllResponses(responseReceived: List<ResponseData>) {
        allResponses.clear()
        allResponses.addAll(responseReceived)
    }

    override fun onOpened(conn: WebSocket) {
        checkIfEngineStarted()
        conn.send(WebsocketMessage.BatchResponse(allResponses).toJson())
    }

    private fun sendMessage(message: WebsocketMessage) {
        websocketServer.broadcast(message.toJson())
    }

    override fun shutDown() {
        super.shutDown()
        try {
            server.stop()
            websocketServer.stop()
        } catch (e: Throwable) {
            e.printStackTrace()
        }


    }

}

internal sealed class WebsocketMessage(val type: String, open val data: String) {
    data class Response(val response: ResponseData) : WebsocketMessage("RESPONSE", response.toJson())
    data class Request(val request: RequestData) : WebsocketMessage("REQUEST", request.toJson())
    data class BatchResponse(val responses: List<ResponseData>) :
        WebsocketMessage("BATCH_RESPONSE", responses.map { it.toJson() }.joinToString(",", prefix = "[", postfix = "]"))

    fun toJson(): String {
        return """
            {
            "type": "$type",
            "data": $data
            }
        """.trimIndent()
    }
}