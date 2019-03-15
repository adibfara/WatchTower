package com.snakydesign.watchtower

import com.snakydesign.watchdog.WatchdogInterceptor
import com.snakydesign.watchtower.java.JavaWebsocketLogger
import kotlinx.coroutines.*
import okhttp3.OkHttpClient
import retrofit2.Call
import retrofit2.Retrofit
import retrofit2.converter.scalars.ScalarsConverterFactory
import retrofit2.http.GET

object Main {

    val eventReporter by lazy {
        JavaWebsocketLogger(
            8085
        )
    }
    val testJob = SupervisorJob()
    val javaMainScope = CoroutineScope(Dispatchers.Main + testJob)
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
        eventReporter.start(false)

        javaMainScope.launch(Dispatchers.IO + testJob) {
            withContext(Dispatchers.IO) {
                while (true) {
                    try {
                        val call = retrofit.getExample().execute()
                        println(call.body() ?: " Empty response")
                        delay(6000)
                    } catch (t: Throwable) {
                        t.printStackTrace()
                    }
                }
            }


        }


        Unit
    }
}

interface TestAPI {
    @GET("test")
    fun getExample(): Call<String>
}