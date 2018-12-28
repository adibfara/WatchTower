package com.snakydesign.watchdog.html

/**
 * @author Adib Faramarzi (adibfara@gmail.com)
 */
internal object MainJs {
    val mainJs = """
        var somePackage = {};
somePackage.connect = function()  {
    var ws = new WebSocket('ws://'+document.location.host+'/ws');
    ws.onopen = function() {
        console.log('ws connected');
        somePackage.ws = ws;
    };
    ws.onerror = function() {
        console.log('ws error');
    };
    ws.onclose = function() {
        console.log('ws closed');
    };
    ws.onmessage = function(msgevent) {
        console.log(msgevent.data);
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
    """.trimIndent()
}