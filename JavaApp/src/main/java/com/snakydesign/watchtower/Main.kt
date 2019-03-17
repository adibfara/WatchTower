package com.snakydesign.watchtower

import kotlinx.coroutines.*
import okhttp3.OkHttpClient
import retrofit2.Call
import retrofit2.Retrofit
import retrofit2.converter.scalars.ScalarsConverterFactory
import retrofit2.http.GET
import retrofit2.http.Header

val eventReporter by lazy {

    WebsocketWatchTower(
        8085
    )
}
val testJob = SupervisorJob()
val javaMainScope = CoroutineScope(Dispatchers.Main + testJob)
fun main() {

    runBlocking<Unit> {

        val interceptor = WatchTowerInterceptor(
            eventReporter
        )
        val retrofit = Retrofit.Builder()
            .client(
                OkHttpClient.Builder().addInterceptor(
                    interceptor
                ).build()
            )
            .baseUrl("https://tap33.me/api/")
            .addConverterFactory(ScalarsConverterFactory.create())
            .build().create(TestAPI::class.java)

        javaMainScope.launch(Dispatchers.IO + testJob) {
            withContext(Dispatchers.IO) {
                while (true) {
                    try {
                        val call = retrofit.getExample("of course").execute()
                        //   println(call.body() ?: " Empty response")
                        delay(6000)
                    } catch (t: Throwable) {
                        t.printStackTrace()
                    }
                }
            }
        }
        eventReporter.start(true)
        Unit
    }
}

interface TestAPI {
    @GET("v2")

    fun getExample(@Header("khiar") header: String): Call<String>
}