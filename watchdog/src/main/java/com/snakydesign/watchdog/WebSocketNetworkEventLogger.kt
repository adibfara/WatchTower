package com.snakydesign.watchdog

import io.ktor.application.call
import io.ktor.application.install
import io.ktor.http.ContentType
import io.ktor.http.cio.websocket.CloseReason
import io.ktor.http.cio.websocket.Frame
import io.ktor.http.cio.websocket.close
import io.ktor.http.cio.websocket.readText
import io.ktor.response.respondFile
import io.ktor.response.respondText
import io.ktor.routing.get
import io.ktor.routing.routing
import io.ktor.server.engine.ApplicationEngine
import io.ktor.server.engine.embeddedServer
import io.ktor.server.netty.Netty
import io.ktor.websocket.WebSockets
import io.ktor.websocket.webSocket
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import java.io.File
import java.util.concurrent.TimeUnit

/**
 * @author Adib Faramarzi (adibfara@gmail.com)
 */
abstract class WebSocketNetworkEventLogger constructor(private val port: Int) : NetworkEventLogger {
    protected abstract val html: String
    protected abstract val cssFile: File
    protected abstract val javascriptFile: File
    protected abstract val jqueryFile: File

    private lateinit var engine: ApplicationEngine
    private var outgoingMessageHandler: (suspend (String) -> Unit)? = null
    private var isStarted = false

    init {


    }

    fun start(wait: Boolean = false) {
        engine = embeddedServer(Netty, port) {
            install(WebSockets)

            routing {
                get("/") {
                    call.respondText(
                        html, ContentType.Text.Html
                    )
                }
                get("/main.css") {
                    call.respondFile(
                        cssFile, {}
                    )
                }
                get("/main.js") {
                    call.respondFile(
                        javascriptFile, {}
                    )
                }
                get("/jquery.min.js") {
                    call.respondFile(
                        jqueryFile, {}
                    )
                }
                webSocket("/ws") {
                    // websocketSession
                    outgoingMessageHandler = {
                        outgoing.send(Frame.Text(it))
                    }
                    while (true) {
                        val frame = incoming.receive()
                        when (frame) {
                            is Frame.Text -> {
                                val text = frame.readText()
                                println(text)
                                outgoing.send(Frame.Text("YOU SAID: $text"))
                                if (text.equals("bye", ignoreCase = true)) {
                                    close(CloseReason(CloseReason.Codes.NORMAL, "Client said BYE"))
                                }
                            }
                        }
                    }
                }
            }
        }
        engine.start(wait = wait)
        isStarted = true
    }

    fun stop() {
        checkIfEngineStarted()
        engine.stop(1, 1, TimeUnit.SECONDS)
        isStarted = false
    }

    private fun checkIfEngineStarted() {
        if (!::engine.isInitialized || !isStarted) throw Exception("Engine was never started! use WebtSocketEventReported.start() to start it.")
    }

    override fun logRequest(requestSent: RequestData, logLevel: WatchdogInterceptor.LogLevel) {
        checkIfEngineStarted()
        sendMessage(requestSent.toString())

    }

    override fun logResponse(
        responseReceived: ResponseData, logLevel: WatchdogInterceptor.LogLevel
    ) {
        checkIfEngineStarted()
        sendMessage(responseReceived.toString())
    }

    fun sendMessage(message: String) {
        GlobalScope.launch {
            outgoingMessageHandler?.invoke(message)
        }
    }
}