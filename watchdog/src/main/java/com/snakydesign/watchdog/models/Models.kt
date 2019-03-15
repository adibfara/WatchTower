package com.snakydesign.watchdog.models

import kotlinx.serialization.Serializable
/**
 * @author Adib Faramarzi (adibfara@gmail.com)
 */

sealed class NetworkContent

@Serializable
data class EmptyBody(val isUsed: Boolean = false) : NetworkContent()

@Serializable
data class ContentBody(val contentLength: Long, val body: String, val gzippedLength: Long?) :
    NetworkContent()

@Serializable
data class HeaderData(val key: String, val value: String)

@Serializable
data class RequestData(
    val url: String,
    val headers: List<HeaderData>,
    val body: NetworkContent,
    val requestTime: Long,
    val method: String
)

@Serializable
data class ResponseData(
    val requestData: RequestData,
    val headers: List<HeaderData>,
    val body: NetworkContent,
    val tookTime: Long,
    val responseCode: Int,
    val contentLength: Long
)
