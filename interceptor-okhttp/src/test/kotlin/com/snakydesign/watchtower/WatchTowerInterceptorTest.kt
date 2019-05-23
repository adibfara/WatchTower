package com.snakydesign.watchtower

import com.snakydesign.watchtower.interceptor.WatchTowerInterceptor
import com.snakydesign.watchtower.models.*
import io.mockk.clearMocks
import io.mockk.mockkObject
import io.mockk.spyk
import io.mockk.verify
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
    lateinit var watchdogInterceptorTest: TestWatchTowerAPI
    lateinit var mockWebServer: MockWebServer

    val interceptor = WatchTowerInterceptor()

    lateinit var url: HttpUrl
    @Before
    fun setUp() {

        mockkObject(WatchTower)
        mockWebServer = MockWebServer()
        url = mockWebServer.url("/")
        watchdogInterceptorTest = Retrofit.Builder()
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
    fun `test content length and body in request and response`() {
        val requestContent = "test request"
        val responseContent = "hello, world!"
        val requestContentContentLength: Long = requestContent.byteInputStream().available().toLong()
        val testContentContentLength: Long = responseContent.byteInputStream().available().toLong()
        mockWebServer.enqueue(MockResponse().setBody(responseContent))
        val response = watchdogInterceptorTest.testPostFunction(requestContent)

        response.execute()
        verify {
            WatchTower.logRequest(withArg {
                assert(it.body is ContentBody)
                assert((it.body as ContentBody).contentLength == requestContentContentLength)
                assert((it.body as ContentBody).body == requestContent)
            })
            WatchTower.logResponse(withArg {
                assert(it.body is ContentBody)
                assert((it.body as ContentBody).contentLength == testContentContentLength)
                assert((it.body as ContentBody).body == responseContent)
            })
        }
    }

    @Test
    fun `test headers in request and response`() {
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
        val response = watchdogInterceptorTest.testPostHeaderFunction(requestHeader, requestContent)

        response.execute()
        verify {
            WatchTower.logRequest(withArg {
                assert(it.headers.first { it.key == "testableRequestHeader" }.value == requestHeader)
            })
            WatchTower.logResponse(withArg {
                assert(it.headers.first { it.key == "testableResponseHeader" }.value == responseHeader)

            })
        }
    }

    @Test
    fun `test redacted headers in request and response`() {
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
        val response = watchdogInterceptorTest.testPostHeaderFunction(requestHeader, requestContent)

        response.execute()
        verify {
            WatchTower.logRequest(withArg {
                assert(!it.headers.any { it.key == "testableRequestHeader" })
            })
            WatchTower.logResponse(withArg {
                assert(!it.headers.any { it.key == "testableResponseHeader" })

            })
        }
    }

    @Test
    fun `test empty request and response`() {
        mockWebServer.enqueue(MockResponse())
        val response = watchdogInterceptorTest.testGetFunction()

        response.execute()
        verify {
            WatchTower.logRequest(withArg {
                assert(it.body is EmptyBody)
            })
            WatchTower.logResponse(withArg {
                print(it)
                assertEquals(0, (it.body as ContentBody).contentLength)
            })
        }
    }

    @Test
    fun `test request url`() {
        mockWebServer.enqueue(MockResponse())
        val response = watchdogInterceptorTest.testGetFunction()

        response.execute()

        verify {
            WatchTower.logRequest(withArg {
                assertEquals(url.toString() + "test", it.url)
            })
        }
    }

    @Test
    fun `test GET, POST, DELETE, PUT, HEAD types `() {
        mockWebServer.enqueue(MockResponse())
        watchdogInterceptorTest.testGetFunction().execute()
        verify {
            WatchTower.logRequest(withArg {
                assertEquals("GET", it.method.toUpperCase())
            })
        }
        clearMocks(WatchTower)

        mockWebServer.enqueue(MockResponse())
        watchdogInterceptorTest.testDeleteFunction().execute()

        verify {
            WatchTower.logRequest(withArg {
                assertEquals("DELETE", it.method.toUpperCase())
            })
        }
        clearMocks(WatchTower)

        mockWebServer.enqueue(MockResponse())
        watchdogInterceptorTest.testHeadFunction().execute()

        verify {
            WatchTower.logRequest(withArg {
                assertEquals("HEAD", it.method.toUpperCase())
            })
        }
        clearMocks(WatchTower)

        mockWebServer.enqueue(MockResponse())
        watchdogInterceptorTest.testPostFunction("").execute()

        verify {
            WatchTower.logRequest(withArg {
                assertEquals("POST", it.method.toUpperCase())
            })
        }
        clearMocks(WatchTower)

        mockWebServer.enqueue(MockResponse())
        watchdogInterceptorTest.testPutFunction().execute()

        verify {
            WatchTower.logRequest(withArg {
                assertEquals("PUT", it.method.toUpperCase())
            })
        }
        clearMocks(WatchTower)


    }

    @After
    fun tearDown() {
        mockWebServer.shutdown()
    }
}