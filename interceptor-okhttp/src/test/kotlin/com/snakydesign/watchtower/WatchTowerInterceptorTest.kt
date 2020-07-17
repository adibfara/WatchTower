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

    lateinit var interceptor: WatchTowerInterceptor

    lateinit var url: HttpUrl
    @Before
    fun setUp() {
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
        val mockObserver = TestWatchTowerObserver()
        WatchTower.start(mockObserver)
        WatchTower.pause()
        val requestContent = "test request"
        val responseContent = "hello, world!"

        mockWebServer.enqueue(MockResponse().setBody(responseContent))
        val response = watchTowerInterceptorTest.testPostFunction(requestContent)

        response.execute()

        mockObserver.assertIsStarted()
        mockObserver.assertShowAllResponsesCalled()
        assertEquals(0, mockObserver.getRequests().size)
        assertEquals(0, mockObserver.getResponses().size)
        WatchTower.resume()

        mockWebServer.enqueue(MockResponse().setBody(responseContent))
        val response2 = watchTowerInterceptorTest.testPostFunction(requestContent)

        response2.execute()
        assertEquals(1, mockObserver.getRequests().size)
        assertEquals(1, mockObserver.getResponses().size)

    }

    @Test
    fun `test headers in request and response`() {
        val mockObserver = TestWatchTowerObserver()
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
        mockObserver.getRequests().first().let {
            assert(it.headers.first { it.key == "testableRequestHeader" }.value == requestHeader)
        }
        mockObserver.getResponses().first().let {
            assert(it.headers.first { it.key == "testableResponseHeader" }.value == responseHeader)
        }
    }

    @Test
    fun `test empty request and response`() {
        val mockObserver = TestWatchTowerObserver()
        WatchTower.start(mockObserver)
        mockWebServer.enqueue(MockResponse())
        val response = watchTowerInterceptorTest.testGetFunction()

        response.execute()
        mockObserver.getRequests().first().let {
            assert(it.body is EmptyBody)
        }
        mockObserver.getResponses().first().let {
            print(it)
            assertEquals(0, (it.body as ContentBody).contentLength)
        }
    }

    @Test
    fun `test content length and body in request and response`() {
        val mockObserver = TestWatchTowerObserver()
        WatchTower.start(mockObserver)
        val requestContent = "test request"
        val responseContent = "hello, world!"
        val requestContentContentLength: Long = requestContent.byteInputStream().available().toLong()
        val testContentContentLength: Long = responseContent.byteInputStream().available().toLong()
        mockWebServer.enqueue(MockResponse().setBody(responseContent))
        val response = watchTowerInterceptorTest.testPostFunction(requestContent)

        response.execute()
        mockObserver.getRequests().first().let {
            assert(it.body is ContentBody)
            assert((it.body as ContentBody).contentLength == requestContentContentLength)
            assert((it.body as ContentBody).body == requestContent)
        }
        mockObserver.getResponses().first().let {
            assert(it.body is ContentBody)
            assert((it.body as ContentBody).contentLength == testContentContentLength)
            assert((it.body as ContentBody).body == responseContent)
        }
    }


    @Test
    fun `test redacted headers in request and response`() {
        val mockObserver = TestWatchTowerObserver()
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
        mockObserver.getRequests().first().let {
            assert(!it.headers.any { it.key == "testableRequestHeader" })
        }
        mockObserver.getResponses().first().let {
            assert(!it.headers.any { it.key == "testableResponseHeader" })

        }
    }


    @Test
    fun `test request url`() {
        val mockObserver = TestWatchTowerObserver()
        WatchTower.start(mockObserver)
        mockWebServer.enqueue(MockResponse())
        val response = watchTowerInterceptorTest.testGetFunction()

        response.execute()
        mockObserver.getRequests().first().let {
            assertEquals(url.toString() + "test", it.url)
        }
    }

    @Test
    fun `test GET, POST, DELETE, PUT, HEAD types `() {
        val mockObserver = TestWatchTowerObserver()
        WatchTower.start(mockObserver)
        mockWebServer.enqueue(MockResponse())
        watchTowerInterceptorTest.testGetFunction().execute()
        mockObserver.getRequests().first().let {
            assertEquals("GET", it.method.toUpperCase())
        }

        mockWebServer.enqueue(MockResponse())
        watchTowerInterceptorTest.testDeleteFunction().execute()


        mockObserver.getRequests()[1].let {
            assertEquals("DELETE", it.method.toUpperCase())
        }

        mockWebServer.enqueue(MockResponse())
        watchTowerInterceptorTest.testHeadFunction().execute()

        mockObserver.getRequests()[2].let {
            assertEquals("HEAD", it.method.toUpperCase())
        }


        mockWebServer.enqueue(MockResponse())
        watchTowerInterceptorTest.testPostFunction("").execute()
        mockObserver.getRequests()[3].let {
            assertEquals("POST", it.method.toUpperCase())
        }


        mockWebServer.enqueue(MockResponse())
        watchTowerInterceptorTest.testPutFunction().execute()

        mockObserver.getRequests()[4].let {
            assertEquals("PUT", it.method.toUpperCase())
        }


    }

    @After
    fun tearDown() {
        WatchTower.shutDown()
        mockWebServer.shutdown()
    }
}