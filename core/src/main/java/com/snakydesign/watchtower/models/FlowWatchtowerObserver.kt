package com.snakydesign.watchtower.models

import kotlinx.coroutines.channels.ConflatedBroadcastChannel
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.asFlow


/**
 * @author Adib Faramarzi (adibfara@gmail.com)
 */
internal class FlowWatchtowerObserver : TowerObserver() {

    private val logChannel = ConflatedBroadcastChannel<WatchtowerLog>()
    fun flow(): Flow<WatchtowerLog> {
        return logChannel.asFlow()
    }

    override fun showRequest(requestSent: RequestData) {
        logChannel.offer(WatchtowerLog.RequestLog(requestSent))
    }

    override fun showResponse(responseReceived: ResponseData) {
        logChannel.offer(WatchtowerLog.ResponseLog(responseReceived))
    }

    override fun showAllResponses(responseReceived: List<ResponseData>) {
    }

}