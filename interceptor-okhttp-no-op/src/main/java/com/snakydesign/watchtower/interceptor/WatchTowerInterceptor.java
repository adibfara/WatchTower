package com.snakydesign.watchtower.interceptor;

import com.snakydesign.watchtower.models.WatchTowerDataHandler;
import okhttp3.Interceptor;
import okhttp3.Response;

import java.io.IOException;

/**
 * @author Adib Faramarzi (adibfara@gmail.com)
 */
public class WatchTowerInterceptor implements Interceptor {
    public WatchTowerInterceptor(WatchTowerDataHandler watchTowerDataHandler) {
    }

    @Override
    public Response intercept(Chain chain) throws IOException {
        return chain.proceed(chain.request()); // no op
    }
}
