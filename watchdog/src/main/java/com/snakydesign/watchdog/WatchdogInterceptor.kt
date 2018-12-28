package com.snakydesign.watchdog

/**
 * @author Adib Faramarzi (adibfara@gmail.com)
 */
/*
 * Copyright (C) 2015 Square, Inc.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */


import okhttp3.*
import okhttp3.internal.http.HttpHeaders
import okio.Buffer
import java.io.IOException
import java.nio.charset.Charset
import java.util.*
import java.util.concurrent.TimeUnit

/**
 * An OkHttp interceptor which logs request and response information. Can be applied as an
 * [application interceptor][OkHttpClient.interceptors] or as a [ ][OkHttpClient.networkInterceptors].
 *
 * The format of the logs created by
 * this class should not be considered stable and may change slightly between releases. If you need
 * a stable logging format, use your own interceptor.
 */
class WatchdogInterceptor @JvmOverloads constructor(@Volatile private var level: LogLevel = LogLevel.FULL, private val eventReporter: RetrofitEventReporter = WebSocketEventReporter()) :
    Interceptor {

    @Volatile
    private var headersToRedact = emptySet<String>()

    enum class LogLevel {
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

    @Throws(IOException::class)
    override fun intercept(chain: Interceptor.Chain): Response {

        val request = chain.request()
        val startNs = System.nanoTime()
        val requestData = request.asData(startNs, chain.connection())
        eventReporter.logRequest(requestData, level)
        val response: Response
        try {
            response = chain.proceed(request)
        } catch (e: Exception) {
            throw e
        }
        val responseData = response.asData(requestData)
        eventReporter.logResponse(responseData, level)

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
            EmptyBody
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
            EmptyBody
        }


        return ResponseData(requestData, responseDataHeaders, responseBody, tookTime)
    }

    private fun Headers.bodyHasUnknownEncoding(): Boolean {
        val contentEncoding = get("Content-Encoding")
        return (contentEncoding != null
                && !contentEncoding.equals("identity", ignoreCase = true)
                && !contentEncoding.equals("gzip", ignoreCase = true))
    }

    private fun Headers.asDataHeaders(headersToRedact: Set<String>): Map<String, List<String>> {
        return names().filterNot { headersToRedact.contains(it) }.map { name ->
            Pair<String, List<String>>(
                name,
                values(name)
            )
        }.toMap()
    }

    companion object {
        private val UTF8 = Charset.forName("UTF-8")
    }
}
