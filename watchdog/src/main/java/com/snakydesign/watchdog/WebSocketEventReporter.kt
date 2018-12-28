package com.snakydesign.watchdog

import com.snakydesign.watchdog.html.MainHtml.mainHtml
import io.ktor.application.call
import io.ktor.application.install
import io.ktor.http.ContentType
import io.ktor.http.cio.websocket.CloseReason
import io.ktor.http.cio.websocket.Frame
import io.ktor.http.cio.websocket.close
import io.ktor.http.cio.websocket.readText
import io.ktor.response.respondText
import io.ktor.routing.get
import io.ktor.routing.routing
import io.ktor.server.engine.ApplicationEngine
import io.ktor.server.engine.embeddedServer
import io.ktor.server.netty.Netty
import io.ktor.websocket.WebSockets
import io.ktor.websocket.webSocket
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.channels.SendChannel
import kotlinx.coroutines.launch
import java.util.concurrent.TimeUnit

/**
 * @author Adib Faramarzi (adibfara@gmail.com)
 */
class WebSocketEventReporter(port: Int = 8293) : RetrofitEventReporter {
    private val engine: ApplicationEngine
    private var outgoingMessageHandler: (suspend (String) -> Unit)? = null

    init {

        engine = embeddedServer(Netty, port) {
            install(WebSockets)

            routing {
                get("/") {
                    call.respondText(
                        mainHtml, ContentType.Text.Html
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


    }

    fun start(wait: Boolean = false) {
        engine.start(wait = wait)
    }

    fun stop() {
        engine.stop(1, 1, TimeUnit.SECONDS)
    }

    override fun logRequest(requestSent: RequestData, logLevel: WatchdogInterceptor.LogLevel) {
        sendMessage(requestSent.toString())

    }

    override fun logResponse(
        responseReceived: ResponseData, logLevel: WatchdogInterceptor.LogLevel
    ) {
        sendMessage(responseReceived.toString())
    }

    fun sendMessage(message: String) {
        GlobalScope.launch {
            outgoingMessageHandler?.invoke(message)
        }
    }
}