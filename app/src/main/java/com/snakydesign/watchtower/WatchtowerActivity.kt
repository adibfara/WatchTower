package com.snakydesign.watchtower

import android.os.Bundle
import android.support.v7.app.AppCompatActivity
import android.util.Log
import com.snakydesign.watchdog.WatchdogInterceptor
import com.snakydesign.watchdog.WebSocketEventReporter
import kotlinx.coroutines.*
import okhttp3.OkHttpClient
import retrofit2.Retrofit
import retrofit2.converter.scalars.ScalarsConverterFactory
import java.lang.Exception

class WatchtowerActivity : AppCompatActivity() {

    val eventReporter by lazy {
        WebSocketEventReporter(
            8085
        )
    }

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
        try {
            GlobalScope.launch {
                try {
                    withContext(Dispatchers.IO) {
                        try {
                            while (true) {
                                val call = retrofit.getExample().execute()
                                Log.e("call", call.body() ?: " Empty response")
                                delay(4000)
                            }
                        } catch (t: Throwable) {
                            t.printStackTrace()
                        }

                    }
                } catch (t: Throwable) {
                    t.printStackTrace()

                }


            }
        } catch (e: Throwable) {

            e.printStackTrace()
        }

        eventReporter.start()


    }

    override fun onDestroy() {
        super.onDestroy()
        eventReporter.stop()
    }
}
