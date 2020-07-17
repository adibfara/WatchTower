package com.snakydesign.watchtower

import com.snakydesign.watchtower.models.RequestData
import com.snakydesign.watchtower.models.ResponseData
import com.snakydesign.watchtower.models.TowerObserver
import org.junit.Assert.assertEquals
import org.junit.runner.Request

/**
 * @author Adib Faramarzi (adibfara@gmail.com)
 */
class TestWatchTowerObserver : TowerObserver() {
    private val requests = mutableListOf<RequestData>()
    private val responses = mutableListOf<ResponseData>()
    private val showAllResponses = mutableListOf<Unit>()
    private var isStarted = false
    override fun start() {
        super.start()
        isStarted = true
    }

    override fun stop() {
        super.stop()
        isStarted = false
    }
    override fun showRequest(requestSent: RequestData) {
        requests.add(requestSent)
    }

    override fun showResponse(responseReceived: ResponseData) {
        responses.add(responseReceived)
    }

    override fun showAllResponses(responseReceived: List<ResponseData>) {
        showAllResponses.add(Unit)
    }

    fun assertShowAllResponsesCalled(){
        assert(showAllResponses.isNotEmpty())
    }

    fun assertIsStarted() {
        assertEquals(true, isStarted)
    }

    fun getRequests(): List<RequestData> {
        return requests
    }
    fun getResponses(): List<ResponseData> {
        return responses
    }
}