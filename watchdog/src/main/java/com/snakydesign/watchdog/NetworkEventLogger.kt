package com.snakydesign.watchdog

/**
 * @author Adib Faramarzi (adibfara@gmail.com)
 */
interface NetworkEventLogger {
    fun logRequest(requestSent: RequestData, logLevel: WatchdogInterceptor.LogLevel)
    fun logResponse(responseReceived: ResponseData, logLevel: WatchdogInterceptor.LogLevel)
}
