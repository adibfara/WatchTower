package com.snakydesign.watchtower

import com.snakydesign.watchtower.models.*
import com.snakydesign.watchtower.models.FlowWatchTowerObserver
import kotlinx.coroutines.flow.Flow
import java.util.concurrent.CopyOnWriteArrayList
import java.util.concurrent.atomic.AtomicBoolean

/**
 * @author Adib Faramarzi (adibfara@gmail.com)
 */
/**
 * @param observers: observers of the request and responses
 * @param numberOfCachedResponses: number of the responses to cache, to send to each observer that gets attached
 */
object WatchTower {

    private var isStarted: AtomicBoolean = AtomicBoolean(false)
    private var isActive = true
    private var numberOfCachedResponses = 100
    private val latestResponses = CopyOnWriteArrayList<ResponseData>()
    private lateinit var observers: List<TowerObserver>

    private val flowObserver = FlowWatchTowerObserver()

    /**
     * returns the last responses received by the WatchTower service
     */
    internal fun getLatestResponses(): List<ResponseData> {
        return latestResponses.toList()
    }

    /**
     * dispatches a request to all WatchTower observers
     */
    fun logRequest(requestSent: RequestData) {
        if (isStarted.get() && isActive) {
            observers.forEach {
                it.logRequest(requestSent)
            }
        }
    }

    /**
     * dispatches a response to all WatchTower observers
     */
    fun logResponse(responseReceived: ResponseData) {
        if (isStarted.get() && isActive) {

            observers.forEach {
                it.logResponse(responseReceived)
            }

            latestResponses.add(0, responseReceived)
            if (latestResponses.size >= numberOfCachedResponses)
                latestResponses.removeAt(numberOfCachedResponses - 1)
        }
    }

    /**
     * pauses tracking events
     */
    fun pause() {
        isActive = false
    }

    /**
     * resumes a previously paused event tracking
     */
    fun resume() {
        isActive = true
    }

    /**
     * shuts down the WatchTower and all observers
     */
    fun shutDown() {
        if (isStarted.get()) {
            isStarted.set(false)
            observers.forEach {
                it.shutDown()
            }
        } else {
            System.err.println("Watch tower `shutDown` called when it has not started.")
        }
    }

    /**
     * starts WatchTower and all observers
     */
    fun start(vararg observers: TowerObserver, numberOfCachedResponses: Int = 100) {
        isActive = true
        if (!isStarted.get()) {
            this.numberOfCachedResponses = numberOfCachedResponses
            this.observers = listOf(flowObserver) + observers.toList()
            isStarted.set(true)
            observers.forEach {
                it.start()
                it.showAllResponses(getLatestResponses())
            }
        } else {
            System.err.println("Watch tower `start` called when it is already started.")
        }
    }

    fun networkFlow(): Flow<WatchTowerLog> {
        return flowObserver.flow()
    }
}
