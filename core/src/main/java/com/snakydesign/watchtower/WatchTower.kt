package com.snakydesign.watchtower

import com.snakydesign.watchtower.models.RequestData
import com.snakydesign.watchtower.models.ResponseData
import com.snakydesign.watchtower.models.TowerObserver
import com.snakydesign.watchtower.models.WatchTowerDataHandler
import java.util.concurrent.ArrayBlockingQueue

/**
 * @author Adib Faramarzi (adibfara@gmail.com)
 */
/**
 * @param observers: observers of the request and responses
 * @param numberOfCachedResponses: number of the responses to cache, to send to each observer that gets attached
 */
class WatchTower(private val observers: List<TowerObserver>, private val numberOfCachedResponses: Int = 100) :
    WatchTowerDataHandler {
    private var isRunning: Boolean = true
    private val latestResponses = ArrayBlockingQueue<ResponseData>(numberOfCachedResponses)
    /**
     * returns the last responses received by the WatchTower service
     */
    fun getLatestResponses(): List<ResponseData> {
        return latestResponses.toList()
    }

    /**
     * dispatches a request to all WatchTower observers
     */
    override fun logRequest(requestSent: RequestData) {
        if (isRunning) {
            observers.forEach {
                it.logRequest(requestSent)
            }
        }
    }

    /**
     * dispatches a response to all WatchTower observers
     */
    override fun logResponse(responseReceived: ResponseData) {
        if (isRunning) {

            observers.forEach {
                it.logResponse(responseReceived)
            }
            latestResponses.put(responseReceived)
        }
    }

    /**
     * pauses tracking events
     */
    fun pause() {
        isRunning = false
    }

    /**
     * resumes a previously paused event tracking
     */
    fun resume() {
        isRunning = true
    }

    /**
     * shuts down the WatchTower and all observers
     */
    fun shutDown() {
        isRunning = false
        observers.forEach {
            it.shutDown()
        }
    }

    /**
     * starts WatchTower and all observers
     */
    fun start() {
        isRunning = true
        observers.forEach {
            it.start()
            it.showAllResponses(getLatestResponses())
        }
    }
}
