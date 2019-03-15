package com.snakydesign.watchtower

import android.os.Bundle
import android.support.v7.app.AppCompatActivity
import android.util.Log
import com.snakydesign.watchdog.WatchdogInterceptor
import com.snakydesign.watchtower.java.JavaWebsocketLogger
import kotlinx.coroutines.*
import okhttp3.OkHttpClient
import retrofit2.Retrofit
import retrofit2.converter.scalars.ScalarsConverterFactory

class WatchtowerActivity : AppCompatActivity() {

    val eventReporter by lazy {
        JavaWebsocketLogger(
            8085
        )
    }
    val testJob = SupervisorJob()
    val activityScope = CoroutineScope(Dispatchers.Main + testJob)

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_watchtower)


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
                        Log.e("call", call.body() ?: " Empty response")
                        delay(4000)
                    } catch (t: Throwable) {
                        t.printStackTrace()
                    }
                }
            }


        }
        eventReporter.start()


    }

    override fun onDestroy() {
        super.onDestroy()
        testJob.cancel()
        eventReporter.stop()
    }
}
