package com.snakydesign.watchdog

/**
 * @author Adib Faramarzi (adibfara@gmail.com)
 */
internal class WebSocketEventReporter : RetrofitEventReporter {
    override fun logRequest(requestSent: RequestData, logLevel: com.snakydesign.watchdog.WatchdogInterceptor.LogLevel) {

    }

    override fun logResponse(
        responseReceived: ResponseData,
        logLevel: com.snakydesign.watchdog.WatchdogInterceptor.LogLevel
    ) {

    }
}