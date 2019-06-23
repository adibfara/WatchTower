package com.snakydesign.watchtower;

import com.snakydesign.watchtower.interceptor.WebWatchTowerObserver;
import com.snakydesign.watchtower.models.TowerObserver;
import retrofit2.Call;
import retrofit2.http.Body;
import retrofit2.http.GET;
import retrofit2.http.Header;
import retrofit2.http.POST;

/**
 * @author Adib Faramarzi (adibfara@gmail.com)
 */
class JavaMain {
    public static void main(String[] args) throws InterruptedException {
        TowerObserver[] observers = new WebWatchTowerObserver[1];
        observers[0] = new WebWatchTowerObserver(8085, 5003);
        WatchTower.INSTANCE.start(observers, 100);
        Thread.sleep(60000);
    }
}
