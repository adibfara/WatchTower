package com.snakydesign.watchtower

import com.snakydesign.watchtower.models.RequestData
import com.snakydesign.watchtower.models.ResponseData

/**
 * @author Adib Faramarzi (adibfara@gmail.com)
 */
interface WatchTower {
    fun logRequest(requestSent: RequestData, logLevel: WatchTowerInterceptor.LogLevel)
    fun logResponse(responseReceived: ResponseData, logLevel: WatchTowerInterceptor.LogLevel)
}
