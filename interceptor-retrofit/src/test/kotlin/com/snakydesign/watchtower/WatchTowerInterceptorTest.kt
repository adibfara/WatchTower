package com.snakydesign.watchtower

import com.snakydesign.watchtower.interceptor.WatchTowerInterceptor
import com.snakydesign.watchtower.models.ContentBody
import com.snakydesign.watchtower.models.EmptyBody
import com.snakydesign.watchtower.models.RequestData
import com.snakydesign.watchtower.models.ResponseData
import io.mockk.clearMocks
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

    class TestEventLogger : WatchTower {
        override fun logRequest(requestSent: RequestData, logLevel: WatchTowerInterceptor.LogLevel) {
        }

        override fun logResponse(responseReceived: ResponseData, logLevel: WatchTowerInterceptor.LogLevel) {
        }
    }

    val mockkEventLogger = spyk(TestEventLogger())
    val interceptor = WatchTowerInterceptor(mockkEventLogger)

    lateinit var url: HttpUrl
    @Before
    fun setUp() {

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
            mockkEventLogger.logRequest(withArg {
                assert(it.body is ContentBody)
                assert((it.body as ContentBody).contentLength == requestContentContentLength)
                assert((it.body as ContentBody).body == requestContent)
            }, any())
            mockkEventLogger.logResponse(withArg {
                assert(it.body is ContentBody)
                assert((it.body as ContentBody).contentLength == testContentContentLength)
                assert((it.body as ContentBody).body == responseContent)
            }, any())
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
            mockkEventLogger.logRequest(withArg {
                assert(it.headers.first { it.key == "testableRequestHeader" }.value == requestHeader)
            }, any())
            mockkEventLogger.logResponse(withArg {
                assert(it.headers.first { it.key == "testableResponseHeader" }.value == responseHeader)

            }, any())
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
            mockkEventLogger.logRequest(withArg {
                assert(!it.headers.any { it.key == "testableRequestHeader" })
            }, any())
            mockkEventLogger.logResponse(withArg {
                assert(!it.headers.any { it.key == "testableResponseHeader" })

            }, any())
        }
    }

    @Test
    fun `test empty request and response`() {
        mockWebServer.enqueue(MockResponse())
        val response = watchdogInterceptorTest.testGetFunction()

        response.execute()
        verify {
            mockkEventLogger.logRequest(withArg {
                assert(it.body is EmptyBody)
            }, any())
            mockkEventLogger.logResponse(withArg {
                assert(it.body is EmptyBody)
            }, any())
        }
    }

    @Test
    fun `test request url`() {
        mockWebServer.enqueue(MockResponse())
        val response = watchdogInterceptorTest.testGetFunction()

        response.execute()

        verify {
            mockkEventLogger.logRequest(withArg {
                assertEquals(url.toString() + "test", it.url)
            }, any())
        }
    }

    @Test
    fun `test GET, POST, DELETE, PUT, HEAD types `() {
        mockWebServer.enqueue(MockResponse())
        watchdogInterceptorTest.testGetFunction().execute()
        verify {
            mockkEventLogger.logRequest(withArg {
                assertEquals("GET", it.method.toUpperCase())
            }, any())
        }
        clearMocks(mockkEventLogger)

        mockWebServer.enqueue(MockResponse())
        watchdogInterceptorTest.testDeleteFunction().execute()

        verify {
            mockkEventLogger.logRequest(withArg {
                assertEquals("DELETE", it.method.toUpperCase())
            }, any())
        }
        clearMocks(mockkEventLogger)

        mockWebServer.enqueue(MockResponse())
        watchdogInterceptorTest.testHeadFunction().execute()

        verify {
            mockkEventLogger.logRequest(withArg {
                assertEquals("HEAD", it.method.toUpperCase())
            }, any())
        }
        clearMocks(mockkEventLogger)

        mockWebServer.enqueue(MockResponse())
        watchdogInterceptorTest.testPostFunction("").execute()

        verify {
            mockkEventLogger.logRequest(withArg {
                assertEquals("POST", it.method.toUpperCase())
            }, any())
        }
        clearMocks(mockkEventLogger)

        mockWebServer.enqueue(MockResponse())
        watchdogInterceptorTest.testPutFunction().execute()

        verify {
            mockkEventLogger.logRequest(withArg {
                assertEquals("PUT", it.method.toUpperCase())
            }, any())
        }
        clearMocks(mockkEventLogger)


    }

    @After
    fun tearDown() {
        mockWebServer.shutdown()
    }
}