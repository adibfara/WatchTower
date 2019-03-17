package com.snakydesign.watchtower

import com.snakydesign.watchtower.models.RequestData
import com.snakydesign.watchtower.models.ResponseData
import java.io.File
import java.io.InputStream
import java.net.InetSocketAddress
import java.nio.charset.Charset

/**
 * @author Adib Faramarzi (adibfara@gmail.com)
 */
class WebsocketWatchTower constructor(private val port: Int) : WatchTower {
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

    private lateinit var serverThread: Thread
    private lateinit var websocketThread: Thread

    init {


    }

    @Synchronized
    fun start(wait: Boolean = false) {
        if (!isStarted) {
            isStarted = true
            serverThread = Thread {
                server = WatchTowerServer(port, html, cssFile, javascriptFile, jqueryFile)
                server.start(1000, false)
            }.apply {
                start()
            }

            websocketThread = Thread {
                websocketServer = WatchTowerWebsocketServer(InetSocketAddress(5003))
                websocketServer.start()
            }.apply {
                start()
            }

            if (wait) {
                serverThread.join()
            }
        } else {
            throw Exception("Server is already started. Please stop if before starting using `stop()` function.")
        }
    }

    private fun checkIfEngineStarted() {
        if (!isStarted) throw Exception("Engine was never started! use WebtSocketEventReported.start() to start it.")
    }

    @Synchronized
    private fun stop() {
        if (isStarted) {
            isStarted = false
            server.stop()
            websocketServer.stop()
        } else {
            throw java.lang.Exception("Server is not started. You can start it by using the `start()` function.")
        }
    }

    override fun logRequest(requestSent: RequestData, logLevel: WatchTowerInterceptor.LogLevel) {
        checkIfEngineStarted()
        sendMessage(requestSent.toJson())
    }

    override fun logResponse(
        responseReceived: ResponseData, logLevel: WatchTowerInterceptor.LogLevel
    ) {
        checkIfEngineStarted()
        sendMessage(responseReceived.toJson())
    }

    private fun sendMessage(message: String) {
        websocketServer.broadcast(message)
    }

}