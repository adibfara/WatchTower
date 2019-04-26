package com.snakydesign.watchtower

import com.snakydesign.watchtower.interceptor.WatchTowerInterceptor
import com.snakydesign.watchtower.interceptor.WebSocketTowerObserver
import kotlinx.coroutines.*
import okhttp3.OkHttpClient
import retrofit2.Call
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import retrofit2.converter.scalars.ScalarsConverterFactory
import retrofit2.http.GET
import retrofit2.http.Header

val websocketTowerObserver by lazy {
    WebSocketTowerObserver(
        8085
    )
}
val watchTower by lazy {
    WatchTower(listOf(websocketTowerObserver))

}
val testJob = SupervisorJob()
val javaMainScope = CoroutineScope(Dispatchers.Main + testJob)
fun main() {

    runBlocking<Unit> {

        val interceptor = WatchTowerInterceptor(
            watchTower
        )
        val retrofit = Retrofit.Builder()
            .client(
                OkHttpClient.Builder().addInterceptor(
                    interceptor
                )

                    .build()
            )
            .baseUrl("https://tap33.me/api/")
            .addConverterFactory(ScalarsConverterFactory.create())
            .addConverterFactory(GsonConverterFactory.create())
            .build().create(TestAPI::class.java)

        javaMainScope.launch(Dispatchers.IO + testJob) {
            withContext(Dispatchers.IO) {
                while (true) {
                    try {
                        val call =
                            retrofit.getExample("of course").execute()
                        println(call.body() ?: " Empty response:\n" + call.message())
                        delay(3000)
                    } catch (t: Throwable) {
                        t.printStackTrace()
                    }
                }
            }
        }
        watchTower.start()
        Thread.currentThread().join() //

        Unit
    }
}

data class SampleRequestBody(val id: String, val name: String)
interface TestAPI {
    @GET("v2/init/passenger")
    fun getExample(@Header("khiar") header: String): Call<String>
}