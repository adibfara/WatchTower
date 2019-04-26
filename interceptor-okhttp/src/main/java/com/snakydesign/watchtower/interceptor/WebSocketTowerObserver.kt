package com.snakydesign.watchtower.interceptor

import com.snakydesign.watchtower.models.RequestData
import com.snakydesign.watchtower.models.ResponseData
import com.snakydesign.watchtower.models.TowerObserver
import org.java_websocket.WebSocket
import java.net.InetSocketAddress
import java.nio.charset.Charset

/**
 * @author Adib Faramarzi (adibfara@gmail.com)
 */
class WebSocketTowerObserver constructor(private val port: Int) : TowerObserver(),
    WatchTowerWebSocketServer.MessageHandler {


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
    private lateinit var server: WatchTowerHTTPServer
    private lateinit var websocketServer: WatchTowerWebSocketServer
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
                server = WatchTowerHTTPServer(
                    port,
                    html,
                    cssFile,
                    javascriptFile,
                    jqueryFile
                )
                server.start(3000, false)
            }.apply {
                start()
            }

            websocketThread = Thread {
                websocketServer = WatchTowerWebSocketServer(
                    InetSocketAddress(5003),
                    this@WebSocketTowerObserver
                )
                websocketServer.start()
            }.apply {
                start()
            }

        } else {
            throw Exception("Server is already started. Please stop if before starting it again, using `stop()` method.")
        }
    }

    private fun checkIfEngineStarted() {
        if (!isStarted) throw Exception("Engine is not started! use WebSocketEventReported.start() to start it.")
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


    override fun showRequest(requestSent: RequestData) {
        checkIfEngineStarted()
        sendMessage(WebSocketMessage.Request(requestSent))
    }

    override fun showResponse(responseReceived: ResponseData) {
        checkIfEngineStarted()
        allResponses.add(responseReceived)
        sendMessage(
            WebSocketMessage.Response(
                responseReceived
            )
        )
    }

    override fun showAllResponses(responseReceived: List<ResponseData>) {
        allResponses.clear()
        allResponses.addAll(responseReceived)
    }

    override fun onOpened(connection: WebSocket) {
        checkIfEngineStarted()
        connection.send(
            WebSocketMessage.BatchResponse(
                allResponses
            ).toJson()
        )
    }

    private fun sendMessage(message: WebSocketMessage) {
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

    internal sealed class WebSocketMessage(val type: String, open val data: String) {
        data class Response(val response: ResponseData) : WebSocketMessage("RESPONSE", response.toJson())
        data class Request(val request: RequestData) : WebSocketMessage("REQUEST", request.toJson())
        data class BatchResponse(val responses: List<ResponseData>) :
            WebSocketMessage("BATCH_RESPONSE", responses.joinToString(",", prefix = "[", postfix = "]") { it.toJson() })

        fun toJson(): String {
            return """
            {
            "type": "$type",
            "data": $data
            }
        """.trimIndent()
        }
    }
}

