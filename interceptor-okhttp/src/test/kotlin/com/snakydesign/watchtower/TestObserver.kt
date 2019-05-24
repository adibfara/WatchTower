package com.snakydesign.watchtower

import com.snakydesign.watchtower.models.RequestData
import com.snakydesign.watchtower.models.ResponseData
import com.snakydesign.watchtower.models.TowerObserver

/**
 * @author Adib Faramarzi (adibfara@gmail.com)
 */
class TestObserver : TowerObserver() {
    override fun showRequest(requestSent: RequestData) {
    }

    override fun showResponse(responseReceived: ResponseData) {
    }

    override fun showAllResponses(responseReceived: List<ResponseData>) {
    }

}