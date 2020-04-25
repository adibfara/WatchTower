package com.snakydesign.watchtower.models

import kotlinx.coroutines.channels.ConflatedBroadcastChannel
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.asFlow


/**
 * @author Adib Faramarzi (adibfara@gmail.com)
 */
internal class FlowWatchTowerObserver : TowerObserver() {

    private val logChannel = ConflatedBroadcastChannel<WatchTowerLog>()
    fun flow(): Flow<WatchTowerLog> {
        return logChannel.asFlow()
    }

    override fun showRequest(requestSent: RequestData) {
        logChannel.offer(WatchTowerLog.RequestLog(requestSent))
    }

    override fun showResponse(responseReceived: ResponseData) {
        logChannel.offer(WatchTowerLog.ResponseLog(responseReceived))
    }

    override fun showAllResponses(responseReceived: List<ResponseData>) {
    }

}