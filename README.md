WatchDog - Monitor Network calls made in Android and Java right in your browser
-------------------------------------------------------------------------------
[![Build Status](https://travis-ci.org/adibfara/Lives.svg?branch=master)](https://travis-ci.org/adibfara/Lives) [![Latest Version](https://img.shields.io/bintray/v/adibfara/watchtower/watchtower.svg?label=version)](https://github.com/adibfara/WatchTower)

Observe Android and Java network calls, requests, responses right in your browser
Download
--------
Add the dependencies to your project:

```groovy
implementation 'com.snakydesign.watchdog:core:0.9.0'
debugImplementation 'com.snakydesign.watchdog:interceptor-okhttp:0.9.0'
releaseImplementation 'com.snakydesign.watchdog:interceptor-okhttp-no-op:0.9.0' //add no-op dependency for non-debug build variants
```

Setup
-----

#### OKHttp And Retrofit
**Add the interceptor to your OKHttp Client**
```kotlin
        val watchTower = WatchTower(listOf(WebSocketTowerObserver(8085)))
        val watchTowerInterceptor = WatchTowerInterceptor(watchTower)

        val retrofit = Retrofit.Builder()
                // ...
                .client( // add the interceptor to your OKHttp client for Retrofit
                        OkHttpClient.Builder()
                                .addInterceptor(watchTowerInterceptor)
                                .build()
                )
                .build()

        watchTower.start()
```

Note: you should call `watchTower.shutDown()` whenever your application is terminating to close the WatchTower service and its related thread. It's best to start and shut down the WatchTower inside an android service (using `onCreate` and `onDestroy` of an Android Service)

**Observing the events using your browser**
Connect the device that is running the app to the same network that you want to observe the data first. So if you are on a WiFi, both devices should be connected to the same WiFi so they can communicate with each other.
After running your application, you can navigate to `http://yourip:8085/` and view the network calls. If you don't know `yourip`, It will be printed out to the console with the message of `WatchTower started, listening on ... `.


#### Notes
PRs are more than welcome, and please file an issue If you encounter something 🍻.

You can also ping me on twitter [@TheSNAKY](http://twitter.com/TheSNAKY).


License
=======

    Copyright 2018 Adib Faramarzi.

    Licensed under the Apache License, Version 2.0 (the "License");
    you may not use this file except in compliance with the License.
    You may obtain a copy of the License at

       http://www.apache.org/licenses/LICENSE-2.0

    Unless required by applicable law or agreed to in writing, software
    distributed under the License is distributed on an "AS IS" BASIS,
    WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
    See the License for the specific language governing permissions and
    limitations under the License.
