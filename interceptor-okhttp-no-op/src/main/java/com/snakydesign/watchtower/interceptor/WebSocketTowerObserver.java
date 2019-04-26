package com.snakydesign.watchtower.interceptor;

import com.snakydesign.watchtower.models.RequestData;
import com.snakydesign.watchtower.models.ResponseData;
import com.snakydesign.watchtower.models.TowerObserver;

import java.util.List;

/**
 * @author Adib Faramarzi (adibfara@gmail.com)
 */
public class WebSocketTowerObserver extends TowerObserver {
    public WebSocketTowerObserver(int port) {

    }

    @Override
    public void showRequest(RequestData requestSent) {

    }

    @Override
    public void showResponse(ResponseData responseReceived) {

    }

    @Override
    public void showAllResponses(List<ResponseData> responseReceived) {

    }

}
