package com.snakydesign.watchdog

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
                        """
                        <!DOCTYPE html>
<html>
<body>

<h1>This is heading 1</h1>
<h2>This is heading 2</h2>
<h3>This is heading 3</h3>
<h4>This is heading 4</h4>
<h5>This is heading 5</h5>
<h6>This is heading 6</h6>

<script>
var somePackage = {};
somePackage.connect = function()  {
    var ws = new WebSocket('ws://'+document.location.host+'/ws');
    ws.onopen = function() {
        alert('ws connected');
        somePackage.ws = ws;
    };
    ws.onerror = function() {
        alert('ws error');
    };
    ws.onclose = function() {
        alert('ws closed');
    };
    ws.onmessage = function(msgevent) {
        alert(msgevent.data);
    };
};

somePackage.send = function(msg) {
    if (!this.ws) {
        console.log('no connection');
        return;
    }
    console.log('out:', msg)
    this.ws.send(window.JSON.stringify(msg));
};
somePackage.connect()
</script>
</body>
</html>

                    """.trimIndent(), ContentType.Text.Html
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

    fun start() {
        engine.start(wait = false)
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