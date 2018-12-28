package com.snakydesign.watchtower

import retrofit2.Call
import retrofit2.Response
import retrofit2.http.GET

/**
 * @author Adib Faramarzi (adibfara@gmail.com)
 */
interface TestAPI {
    @GET("test")
    fun getExample(): Call<String>
}