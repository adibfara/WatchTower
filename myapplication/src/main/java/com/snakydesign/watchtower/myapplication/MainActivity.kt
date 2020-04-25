package com.snakydesign.watchtower.myapplication

import android.app.NotificationManager
import android.content.Context
import android.os.Bundle
import android.support.v7.app.AppCompatActivity
import com.snakydesign.watchtower.WatchTower
import com.snakydesign.watchtower.android.WatchTowerAndroid
import com.snakydesign.watchtower.interceptor.WatchTowerInterceptor
import com.snakydesign.watchtower.interceptor.WebWatchTowerObserver
import kotlinx.coroutines.*
import kotlinx.coroutines.flow.collect
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

class MainActivity : AppCompatActivity() {

    val websocketTowerObserver by lazy {
        WebWatchTowerObserver(
            8085
        )
    }
    val testJob = SupervisorJob()
    val javaMainScope = CoroutineScope(Dispatchers.Main + testJob)
    val retrofit = Retrofit.Builder()
        .client(
            OkHttpClient.Builder().addInterceptor(WatchTowerInterceptor())
                .build()
        )
        .baseUrl("https://reqres.in/")
        .addConverterFactory(ScalarsConverterFactory.create())
        .addConverterFactory(GsonConverterFactory.create())
        .build().create(TestAPI::class.java)

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        runBlocking<Unit> {
            javaMainScope.launch(Dispatchers.IO + testJob) {
                withContext(Dispatchers.IO) {
                    while (true) {
                        launch {
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
                            }
                        }
                        delay(2000)

                    }
                }
            }
            WatchTower.start(WebWatchTowerObserver(port = 8085, websocketPort = 12000))
            javaMainScope.launch {
                WatchTowerAndroid.notificationFlow(this@MainActivity.application, 8085).collect {
                    val notificationManager = getSystemService(
                        Context.NOTIFICATION_SERVICE
                    ) as NotificationManager
                    notificationManager.notify(999, it)
                }
            }

        }


    }
}

data class SampleRequestBody(val id: String, val name: String)
interface TestAPI {
    @POST("api/users")
    fun getExample(
        @Header("test") header: String,
        @Body requestBody: SampleRequestBody
    ): Call<String>

    @GET("404URL")
    fun getAnotherExample(): Call<String>
}