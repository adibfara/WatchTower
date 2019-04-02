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
class WatchTowerInterceptor constructor(private val watchtower: WatchTower) : Interceptor {

    @Volatile
    private var headersToRedact = emptySet<String>()


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
        watchtower.logRequest(requestData)
        val response: Response
        try {
            response = chain.proceed(request)
        } catch (e: Exception) {
            throw e
        }
        val responseData = response.asData(requestData)
        watchtower.logResponse(responseData)

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

            val charset = requestBody.contentType()?.charset(Charset.forName("UTF-8")) ?: Charset.forName("UTF-8")

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
        val responseDataHeaders = response.headers().asDataHeaders(headersToRedact)
        val responseBody: NetworkContent
        if (HttpHeaders.hasBody(response)) {

            val bodyString = (body!!.getAsString() ?: "")
            val contentLength = body.contentLength()

            responseBody = ContentBody(contentLength, bodyString, null)

        } else {
            val content = response.message()
            val contentLength = content.length.toLong()
            responseBody = ContentBody(contentLength, content, null)
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

    private fun ResponseBody.getAsString(): String? {
        val source = source()
        source.request(java.lang.Long.MAX_VALUE) // Buffer the entire body.
        val buffer = source.buffer()
        val encoding = contentType()?.charset() ?: Charset.forName("UTF-8")
        return try {
            buffer.clone().readString(encoding)
        } catch (e: Throwable) {
            null
        }

    }

}

