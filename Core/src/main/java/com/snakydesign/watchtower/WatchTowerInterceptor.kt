package com.snakydesign.watchtower

/**
 * @author Adib Faramarzi (adibfara@gmail.com)
 */

import com.snakydesign.watchtower.models.*
import okhttp3.*
import okhttp3.internal.http.HttpHeaders
import okio.Buffer
import java.nio.charset.Charset
import java.util.*
import java.util.Collections.emptySet
import java.util.concurrent.TimeUnit

/**
 * An OkHttp interceptor which logs request and response information to a websocket client (by default). Can be applied as an
 * [application interceptor][OkHttpClient.interceptors] or as a [ ][OkHttpClient.networkInterceptors].
 */
class WatchTowerInterceptor @JvmOverloads constructor(private val watchtower: WatchTower, @Volatile private var level: LogLevel = LogLevel.FULL) :
    Interceptor {

    @Volatile
    private var headersToRedact = emptySet<String>()

    enum class LogLevel {
        /**
         * Does not log anything.
         */
        NONE,
        /**
         * Logs request and response lines and their respective headers.
         */
        HEADERS,
        /**
         * Logs request and response lines and their respective headers and bodies (if present).
         */
        FULL
    }

    fun redactHeader(name: String) {
        val newHeadersToRedact = TreeSet(String.CASE_INSENSITIVE_ORDER)
        newHeadersToRedact.addAll(headersToRedact)
        newHeadersToRedact.add(name)
        headersToRedact = newHeadersToRedact
    }

    @Throws(java.io.IOException::class)
    override fun intercept(chain: Interceptor.Chain): Response {

        val request = chain.request()
        val startNs = System.nanoTime()
        val requestData = request.asData(startNs, chain.connection())
        watchtower.logRequest(requestData, level)
        val response: Response
        try {
            response = chain.proceed(request)
        } catch (e: Exception) {
            throw e
        }
        val responseData = response.asData(requestData)
        watchtower.logResponse(responseData, level)

        return response
    }

    private fun Request.asData(requestTime: Long, connection: Connection?): RequestData {
        val request = this
        val requestBody = body()
        val hasRequestBody = requestBody != null
        val method = request.method()
        val url = request.url()
        val body: NetworkContent = if (hasRequestBody) {
            val buffer = Buffer()
            requestBody!!.writeTo(buffer)

            val charset = requestBody.contentType()?.charset(UTF8) ?: UTF8

            val body = try {
                val bufferedSink = Buffer()
                request.body()?.writeTo(bufferedSink)
                bufferedSink.readString(charset)
            } catch (e: java.lang.Exception) {
                ""
            }
            ContentBody(requestBody.contentLength(), body, requestBody.contentLength())
        } else {
            EmptyBody()
        }

        val headers = request.headers()

        val requestHeaders = headers.asDataHeaders(headersToRedact)

        return RequestData(url.toString(), requestHeaders, body, requestTime, method)
    }

    private fun Response.asData(requestData: RequestData): ResponseData {
        val response = this

        val tookTime = TimeUnit.NANOSECONDS.toMillis(System.nanoTime() - requestData.requestTime)

        val body = response.body()
        val contentLength = body?.contentLength() ?: 0

        val responseHeaders = response.headers()
        val responseDataHeaders = response.headers().asDataHeaders(headersToRedact)
        val responseBody: NetworkContent = if (HttpHeaders.hasBody(response) && contentLength > 0) {
            if (response.headers().bodyHasUnknownEncoding() && contentLength > 0) {
                ContentBody(contentLength, "[Body omitted, because it has unknown encoding]", null)
            } else {
                val source = body!!.source()
                source.request(java.lang.Long.MAX_VALUE) // Buffer the entire body.
                val buffer = source.buffer()
                val gzipLength: Long? = if ("gzip".equals(responseHeaders.get("Content-Encoding"), ignoreCase = true)) {
                    buffer.size()
                } else {
                    null
                }
                val encoding = body.contentType()?.charset() ?: UTF8
                val bodyString = try {
                    buffer.clone().readString(encoding)
                } catch (e: Throwable) {
                    ""
                }
                ContentBody(contentLength, bodyString, gzipLength)
            }


        } else {
            EmptyBody()
        }


        return ResponseData(
            requestData,
            responseDataHeaders,
            responseBody,
            tookTime,
            response.code(),
            response.body()?.contentLength() ?: 0L
        )
    }

    private fun Headers.bodyHasUnknownEncoding(): Boolean {
        val contentEncoding = get("Content-Encoding")
        return (contentEncoding != null
                && !contentEncoding.equals("identity", ignoreCase = true)
                && !contentEncoding.equals("gzip", ignoreCase = true))
    }

    private fun Headers.asDataHeaders(headersToRedact: Set<String>): List<HeaderData> {
        val finalList = mutableListOf<HeaderData>()
        names().filterNot { headersToRedact.contains(it) }.forEach { name ->
            values(name).forEach {
                finalList.add(
                    HeaderData(
                        name, it
                    )
                )
            }

        }
        return finalList
    }

    companion object {
        private val UTF8 = Charset.forName("UTF-8")
    }
}
