package com.snakydesign.watchtower.javaapp

import com.snakydesign.watchdog.WatchdogInterceptor
import com.snakydesign.watchdog.WebSocketEventReporter
import kotlinx.coroutines.*
import okhttp3.OkHttpClient
import retrofit2.Call
import retrofit2.Retrofit
import retrofit2.converter.scalars.ScalarsConverterFactory
import retrofit2.http.GET
import sun.rmi.runtime.Log

object Main {

    val eventReporter by lazy {
        WebSocketEventReporter(
            8085
        )
    }
    val testJob = SupervisorJob()
    val activityScope = CoroutineScope(Dispatchers.Main + testJob)
    @JvmStatic
    fun main(args: Array<String>) = runBlocking {

        val interceptor = WatchdogInterceptor(
            eventReporter
        )
        val retrofit = Retrofit.Builder()
            .client(
                OkHttpClient.Builder().addInterceptor(
                    interceptor
                ).build()
            ).baseUrl("http://google.com/")
            .addConverterFactory(ScalarsConverterFactory.create())
            .build().create(TestAPI::class.java)
        activityScope.launch(Dispatchers.IO + testJob) {
            withContext(Dispatchers.IO) {
                while (true) {
                    try {
                        val call = retrofit.getExample().execute()
                        println(call.body() ?: " Empty response")
                        delay(60000)
                    } catch (t: Throwable) {
                        t.printStackTrace()
                    }
                }
            }


        }
        eventReporter.start(true)
    }
}

interface TestAPI {
    @GET("test")
    fun getExample(): Call<String>
}