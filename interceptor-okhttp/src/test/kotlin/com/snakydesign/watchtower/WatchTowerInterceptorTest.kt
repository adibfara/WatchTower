package com.snakydesign.watchtower

import com.snakydesign.watchtower.interceptor.WatchTowerInterceptor
import com.snakydesign.watchtower.models.*
import io.mockk.*
import okhttp3.HttpUrl
import okhttp3.OkHttpClient
import okhttp3.mockwebserver.MockResponse
import okhttp3.mockwebserver.MockWebServer
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Before
import org.junit.Test
import retrofit2.Retrofit
import retrofit2.converter.scalars.ScalarsConverterFactory


/**
 * @author Adib Faramarzi (adibfara@gmail.com)
 */
class WatchTowerInterceptorTest {
    lateinit var watchTowerInterceptorTest: TestWatchTowerAPI
    lateinit var mockWebServer: MockWebServer

    lateinit var mockObserver: TowerObserver
    lateinit var interceptor: WatchTowerInterceptor

    lateinit var url: HttpUrl
    @Before
    fun setUp() {
        mockObserver = spyk(TestObserver())
        interceptor = WatchTowerInterceptor()

//        mockkObject(WatchTower)
        mockWebServer = MockWebServer()
        url = mockWebServer.url("/")
        watchTowerInterceptorTest = Retrofit.Builder()
            .baseUrl(url)
            .client(
                OkHttpClient.Builder()
                    .addInterceptor(interceptor)
                    .build()
            )
            .addConverterFactory(ScalarsConverterFactory.create())
            .build()
            .create(TestWatchTowerAPI::class.java)

    }

    @Test
    fun `test when watch tower is paused, nothing is logged, and when is resumed, resumes its execution`() {
        WatchTower.start(mockObserver)
        WatchTower.pause()
        val requestContent = "test request"
        val responseContent = "hello, world!"

        mockWebServer.enqueue(MockResponse().setBody(responseContent))
        val response = watchTowerInterceptorTest.testPostFunction(requestContent)

        response.execute()

        verify {
            mockObserver.start()
            mockObserver.showAllResponses(any())

        }
        verify(exactly = 0) {
            mockObserver.logRequest(any())
        }
        verify(exactly = 0) {
            mockObserver.logResponse(any())
        }

        WatchTower.resume()

        mockWebServer.enqueue(MockResponse().setBody(responseContent))
        val response2 = watchTowerInterceptorTest.testPostFunction(requestContent)

        response2.execute()

        verify(exactly = 1) {
            mockObserver.logRequest(any())
        }
        verify(exactly = 1) {
            mockObserver.logResponse(any())
        }

    }

    @Test
    fun `test headers in request and response`() {
        WatchTower.start(mockObserver)
        val requestContent = "test request"
        val responseContent = "hello, world!"
        val requestHeader = "test request header"
        val responseHeader = "test response header"
        mockWebServer.enqueue(
            MockResponse().setBody(responseContent).setHeader(
                "testableResponseHeader",
                responseHeader
            )
        )
        val response = watchTowerInterceptorTest.testPostHeaderFunction(requestHeader, requestContent)

        response.execute()
        verify {
            mockObserver.logRequest(withArg {
                assert(it.headers.first { it.key == "testableRequestHeader" }.value == requestHeader)
            })
            mockObserver.logResponse(withArg {
                assert(it.headers.first { it.key == "testableResponseHeader" }.value == responseHeader)

            })
        }
    }

    @Test
    fun `test empty request and response`() {
        WatchTower.start(mockObserver)
        mockWebServer.enqueue(MockResponse())
        val response = watchTowerInterceptorTest.testGetFunction()

        response.execute()
        verify {
            mockObserver.logRequest(withArg {
                assert(it.body is EmptyBody)
            })
            mockObserver.logResponse(withArg {
                print(it)
                assertEquals(0, (it.body as ContentBody).contentLength)
            })
        }
    }

    @Test
    fun `test content length and body in request and response`() {
        WatchTower.start(mockObserver)
        val requestContent = "test request"
        val responseContent = "hello, world!"
        val requestContentContentLength: Long = requestContent.byteInputStream().available().toLong()
        val testContentContentLength: Long = responseContent.byteInputStream().available().toLong()
        mockWebServer.enqueue(MockResponse().setBody(responseContent))
        val response = watchTowerInterceptorTest.testPostFunction(requestContent)

        response.execute()
        verify {
            mockObserver.logRequest(withArg {
                assert(it.body is ContentBody)
                assert((it.body as ContentBody).contentLength == requestContentContentLength)
                assert((it.body as ContentBody).body == requestContent)
            })
            mockObserver.logResponse(withArg {
                assert(it.body is ContentBody)
                assert((it.body as ContentBody).contentLength == testContentContentLength)
                assert((it.body as ContentBody).body == responseContent)
            })
        }
    }


    @Test
    fun `test redacted headers in request and response`() {
        WatchTower.start(mockObserver)
        val requestContent = "test request"
        val responseContent = "hello, world!"
        val requestHeader = "test request header"
        val responseHeader = "test response header"
        interceptor.redactHeader("testableResponseHeader")
        interceptor.redactHeader("testableRequestHeader")
        mockWebServer.enqueue(
            MockResponse().setBody(responseContent).setHeader(
                "testableResponseHeader",
                responseHeader
            )
        )
        val response = watchTowerInterceptorTest.testPostHeaderFunction(requestHeader, requestContent)

        response.execute()
        verify {
            mockObserver.logRequest(withArg {
                assert(!it.headers.any { it.key == "testableRequestHeader" })
            })
            mockObserver.logResponse(withArg {
                assert(!it.headers.any { it.key == "testableResponseHeader" })

            })
        }
    }


    @Test
    fun `test request url`() {
        WatchTower.start(mockObserver)
        mockWebServer.enqueue(MockResponse())
        val response = watchTowerInterceptorTest.testGetFunction()

        response.execute()

        verify {
            mockObserver.logRequest(withArg {
                assertEquals(url.toString() + "test", it.url)
            })
        }
    }

    @Test
    fun `test GET, POST, DELETE, PUT, HEAD types `() {
        WatchTower.start(mockObserver)
        mockWebServer.enqueue(MockResponse())
        watchTowerInterceptorTest.testGetFunction().execute()
        verifyOrder {
            mockObserver.logRequest(withArg {
                assertEquals("GET", it.method.toUpperCase())
            })

        }

        clearMocks(mockObserver)
        mockWebServer.enqueue(MockResponse())
        watchTowerInterceptorTest.testDeleteFunction().execute()



        verifyOrder {

            mockObserver.logRequest(withArg {
                assertEquals("DELETE", it.method.toUpperCase())
            })
        }

        clearMocks(mockObserver)
        mockWebServer.enqueue(MockResponse())
        watchTowerInterceptorTest.testHeadFunction().execute()

        verify {
            mockObserver.logRequest(withArg {
                assertEquals("HEAD", it.method.toUpperCase())
            })
        }


        clearMocks(mockObserver)
        mockWebServer.enqueue(MockResponse())
        watchTowerInterceptorTest.testPostFunction("").execute()

        verify {
            mockObserver.logRequest(withArg {
                assertEquals("POST", it.method.toUpperCase())
            })
        }


        clearMocks(mockObserver)
        mockWebServer.enqueue(MockResponse())
        watchTowerInterceptorTest.testPutFunction().execute()

        verify {
            mockObserver.logRequest(withArg {
                assertEquals("PUT", it.method.toUpperCase())
            })
        }


    }

    @After
    fun tearDown() {
        WatchTower.shutDown()
        mockWebServer.shutdown()
    }
}