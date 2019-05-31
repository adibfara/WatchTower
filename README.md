WatchTower - Observe OKHttp API Calls With Request And Response Details Right In Your Browser!
----------------------------------------------------------------------------------------------
[![Build Status](https://travis-ci.org/adibfara/Watchtower.svg?branch=master)](https://travis-ci.org/adibfara/Watchtower) [ ![Download](https://api.bintray.com/packages/adibfara/WatchTower/WatchTower/images/download.svg) ](https://bintray.com/adibfara/WatchTower/WatchTower/)

![Screenshot](https://raw.githubusercontent.com/adibfara/Watchtower/master/screenshots/screenshot-1.jpg "Watchtower Screenshot")


Download
--------
Add the dependencies to your project:

```groovy
debugImplementation 'com.snakyapps.watchtower:interceptor-okhttp:1.0.0'
releaseImplementation 'com.snakyapps.watchtower:interceptor-okhttp-no-op:1.0.0' // no-op dependency for non-debug build variants
```

Setup
-----
**Add the interceptor to your OKHttp Client**
```kotlin
        val retrofit = Retrofit.Builder()
                        .client(
                            OkHttpClient.Builder()
                            .addInterceptor(WatchTowerInterceptor())
                            .build()
                        )
                        // ...
                .build()

        WatchTower.start(WebWatchTowerObserver(port = 8085)) // in Application class
```

Note: you should call `watchTower.shutDown()` whenever your application/activity is terminating, which shuts down WatchTower-related services.
If you're using it on Android, It's best to start and shut down the WatchTower inside an Android service (using `onCreate` and `onDestroy` of an Android Service)

Features
--------
![Video](https://raw.githubusercontent.com/adibfara/Watchtower/master/screenshots/video.gif "Watchtower Video")

- Track and observe all API calls made through OKHttp's client
- GET, POST, PUT, DELETE, PATCH methods
- Query parameters, request and response body and headers
- Response success and failure status, size, date and latency
- Adjustable port for the server and the websocket server
- API call history, even If no browsers were open
- Search in the URLs of all requests
- Fully responsive UI
- Fully extensible


**Observing the events using your browser**
Connect the device that is running the app to the same network that you want to observe the data. If you are on a WiFi, both devices should be connected to the same WiFi so they can communicate with each other.
After running your application, you can navigate to `http://yourip:8085/` and view the network calls. If you don't know what `yourip` is, It will be printed out to the console with the message of `WatchTower started, listening on ... `  when `Watchtower.start()` is called.

#### Notes
PRs are more than welcome, and please file an issue If you encounter something 🍻.

You can also ping me on twitter [@TheSNAKY](http://twitter.com/TheSNAKY).


License
=======

    Copyright 2019 Adib Faramarzi.

    Licensed under the Apache License, Version 2.0 (the "License");
    you may not use this file except in compliance with the License.
    You may obtain a copy of the License at

       http://www.apache.org/licenses/LICENSE-2.0

    Unless required by applicable law or agreed to in writing, software
    distributed under the License is distributed on an "AS IS" BASIS,
    WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
    See the License for the specific language governing permissions and
    limitations under the License.
