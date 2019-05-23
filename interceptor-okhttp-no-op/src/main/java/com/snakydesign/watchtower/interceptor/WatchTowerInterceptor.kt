package com.snakydesign.watchtower.interceptor

import okhttp3.Interceptor
import okhttp3.Response

import java.io.IOException

/**
 * @author Adib Faramarzi (adibfara@gmail.com)
 */
class WatchTowerInterceptor : Interceptor {

    override fun intercept(chain: Interceptor.Chain): Response {
        return chain.proceed(chain.request()) // no op
    }
}
