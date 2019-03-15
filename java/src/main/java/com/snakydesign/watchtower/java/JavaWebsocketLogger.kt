package com.snakydesign.watchtower.java

import com.snakydesign.watchdog.WebSocketNetworkEventLogger
import java.io.File

/**
 * @author Adib Faramarzi (adibfara@gmail.com)
 */
class JavaWebsocketLogger(port: Int) : WebSocketNetworkEventLogger(port) {

    public override val html: String by lazy {
        val html = File(this.javaClass.classLoader.getResource("index.html").file).readText()
        return@lazy html
    }
    override val cssFile: File = File(this.javaClass.classLoader.getResource("main.css").file)
    override val javascriptFile: File = File(this.javaClass.classLoader.getResource("main.js").file)
    override val jqueryFile: File = File(this.javaClass.classLoader.getResource("jquery.min.js").file)
}
