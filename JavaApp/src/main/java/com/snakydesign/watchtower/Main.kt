package com.snakydesign.watchtower

import com.snakydesign.watchtower.interceptor.WatchTowerInterceptor
import com.snakydesign.watchtower.interceptor.WebWatchTowerObserver
import kotlinx.coroutines.*
import okhttp3.OkHttpClient
import retrofit2.Call
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import retrofit2.converter.scalars.ScalarsConverterFactory
import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.Header
import retrofit2.http.POST
import java.util.*

val websocketTowerObserver by lazy {
    WebWatchTowerObserver(
        8085
    )
}
val testJob = SupervisorJob()
val javaMainScope = CoroutineScope(Dispatchers.Main + testJob)
fun main() {

    runBlocking<Unit> {

        val retrofit = Retrofit.Builder()
            .client(
                OkHttpClient.Builder().addInterceptor(WatchTowerInterceptor())
                    .build()
            )
            .baseUrl("https://reqres.in/")
            .addConverterFactory(ScalarsConverterFactory.create())
            .addConverterFactory(GsonConverterFactory.create())
            .build().create(TestAPI::class.java)

        javaMainScope.launch(Dispatchers.IO + testJob) {
            withContext(Dispatchers.IO) {
                while (true) {
                    try {
                        val call = if (Random().nextBoolean()) {

                            retrofit.getExample(
                                "of course", SampleRequestBody(
                                    "test id", "TheSNAKY"
                                )
                            ).execute()
                        } else {
                            retrofit.getAnotherExample().execute()
                        }
                        println(call.body() ?: " Empty response:\n" + call.message())

                    } catch (t: Throwable) {
                        t.printStackTrace()
                    } finally {
                        delay(3000)
                    }
                }
            }
        }
        WatchTower.start(WebWatchTowerObserver(port = 8085))
        Thread.currentThread().join()

    }
}

data class SampleRequestBody(val id: String, val name: String)
interface TestAPI {
    @POST("api/users")
    fun getExample(@Header("test") header: String, @Body requestBody: SampleRequestBody): Call<String>

    @GET("404URL")
    fun getAnotherExample(): Call<String>
}