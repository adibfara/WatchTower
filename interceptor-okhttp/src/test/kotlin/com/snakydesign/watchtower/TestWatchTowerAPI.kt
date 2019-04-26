package com.snakydesign.watchtower

import retrofit2.Call
import retrofit2.http.*

/**
 * @author Adib Faramarzi (adibfara@gmail.com)
 */
interface TestWatchTowerAPI {
    @POST("/test")
    fun testPostFunction(@Body body: String): Call<Unit>

    @POST("/test")
    fun testPostHeaderFunction(@Header("testableRequestHeader") requestHeader: String, @Body body: String): Call<Unit>

    @GET("/test")
    fun testGetFunction(): Call<String>

    @DELETE("/test")
    fun testDeleteFunction(): Call<String>

    @HEAD("/test")
    fun testHeadFunction(): Call<Void>

    @PUT("/test")
    fun testPutFunction(): Call<String>
}