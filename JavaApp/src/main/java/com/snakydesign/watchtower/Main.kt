package com.snakydesign.watchtower

import com.snakydesign.watchdog.WatchdogInterceptor
import com.snakydesign.watchtower.java.JavaWebsocketLogger
import jdk.nashorn.internal.codegen.CompilerConstants
import kotlinx.coroutines.*
import okhttp3.OkHttpClient
import retrofit2.Call
import retrofit2.Retrofit
import retrofit2.converter.scalars.ScalarsConverterFactory
import retrofit2.http.GET
import java.io.File

val eventReporter by lazy {

    JavaWebsocketLogger(
        8085
    )
}
val testJob = SupervisorJob()
val javaMainScope = CoroutineScope(Dispatchers.Main + testJob)
fun main() {

    runBlocking<Unit> {

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