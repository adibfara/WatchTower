package com.snakydesign.watchtower

import com.snakydesign.watchtower.models.RequestData
import com.snakydesign.watchtower.models.ResponseData
import com.snakydesign.watchtower.models.TowerObserver
import com.sun.jmx.remote.internal.ArrayQueue
import java.util.*
import java.util.concurrent.ArrayBlockingQueue

/**
 * @author Adib Faramarzi (adibfara@gmail.com)
 */
class WatchTower(private val callObservers: List<TowerObserver>, private val numberOfCachedResponses: Int = 100) {
    private var isRunning: Boolean = true
    private val latestResponses = ArrayBlockingQueue<ResponseData>(numberOfCachedResponses)
    fun getLatestResponses(): List<ResponseData> {
        return latestResponses.toList()
    }

    fun logRequest(requestSent: RequestData, logLevel: WatchTowerInterceptor.LogLevel) {
        if (isRunning) {
            callObservers.forEach {
                it.logRequest(requestSent, logLevel)
            }
        }
    }

    fun logResponse(responseReceived: ResponseData, logLevel: WatchTowerInterceptor.LogLevel) {
        if (isRunning) {

            callObservers.forEach {
                it.logResponse(responseReceived, logLevel)
            }
            latestResponses.put(responseReceived)
        }
    }

    fun pause() {
        isRunning = false
    }

    fun resume() {
        isRunning = true
    }

    fun shutDown() {
        isRunning = false
        callObservers.forEach {
            it.shutDown()
        }
    }

    fun start() {
        isRunning = true
        callObservers.forEach {
            it.start()
            it.showAllResponses(getLatestResponses())
        }
    }
}
