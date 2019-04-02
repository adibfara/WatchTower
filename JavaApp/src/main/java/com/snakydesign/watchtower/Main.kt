package com.snakydesign.watchtower

import kotlinx.coroutines.*
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Call
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import retrofit2.converter.scalars.ScalarsConverterFactory
import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.Header
import retrofit2.http.POST

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
                )/*.addInterceptor(HttpLoggingInterceptor().apply {
                    setLevel(HttpLoggingInterceptor.Level.BODY)
                } )*/

                    .build()
            )
            .baseUrl("https://tap33.me/apis/")
            .addConverterFactory(ScalarsConverterFactory.create())
            .addConverterFactory(GsonConverterFactory.create())
            .build().create(TestAPI::class.java)

        javaMainScope.launch(Dispatchers.IO + testJob) {
            withContext(Dispatchers.IO) {
                while (true) {
                    try {
                        val call =
                            retrofit.getExample("of course", SampleRequestBody("sample id", " sample name")).execute()
                        println(call.body() ?: " Empty response:\n" + call.message())
                        delay(3000)
                    } catch (t: Throwable) {
                        t.printStackTrace()
                    }
                }
            }
        }
        watchTower.start()
        websocketTowerObserver.blockForRequests()
        Unit
    }
}

data class SampleRequestBody(val id: String, val name: String)
interface TestAPI {
    @POST("v2.1/faq/ticket?adib=true&khiar=1")
    fun getExample(@Header("khiar") header: String, @Body requestBody: SampleRequestBody): Call<String>
}