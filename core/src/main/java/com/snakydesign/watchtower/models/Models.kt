package com.snakydesign.watchtower.models

/**
 * @author Adib Faramarzi (adibfara@gmail.com)
 */

sealed class NetworkContent {
    abstract fun toJson(): String?
}

data class EmptyBody(val isUsed: Boolean = false) : NetworkContent() {
    override fun toJson(): String? {
        return null
    }
}

private fun String.toJson(): String {
    return this.replace("\\", "\\\\")
        .replace("\"", "\\\"")
        .replace("\b", "\\b")
        .replace("\\u000C", "\\f")
        .replace("\n", "\\n")
        .replace("\r", "\\r")
        .replace("\t", "\\t")
}

data class ContentBody(val contentLength: Long, val body: String, val gzippedLength: Long?) :
    NetworkContent() {
    override fun toJson(): String? {
        return """
            {
            "contentLength": $contentLength,
            "body": "${body.toJson()}",
            "gzippedLength": ${gzippedLength ?: "null"}
            }
        """.trimIndent()
    }

}

data class HeaderData(val key: String, val value: String) {
    fun toJson(): String {
        return """
            {
            "key": "${key.toJson()}",
            "value": "${value.toJson()}"
            }
        """.trimIndent()
    }
}

fun List<HeaderData>.toJson(): String {
    return """
        [
        ${this.map { it.toJson() }.joinToString(",")}
        ]
    """.trimIndent()
}

data class RequestData(
    val url: String,
    val headers: List<HeaderData>,
    val body: NetworkContent,
    val requestTime: Long,
    val method: String
) {
    fun toJson(): String {
        return """
            {
            "url": "$url",
            "headers": ${headers.toJson()},
            "body":  ${body.toJson()},
            "requestTime": $requestTime,
            "method":  "${method.toJson()}"

            }
        """.trimIndent()
    }
}

data class ResponseData(
    val requestData: RequestData,
    val headers: List<HeaderData>,
    val body: NetworkContent,
    val tookTime: Long,
    val responseCode: Int,
    val contentLength: Long
) {
    fun toJson(): String {
        return """
            {
            "requestData": ${requestData.toJson()},
            "headers": ${headers.toJson()},
            "body": ${body.toJson()},
            "tookTime": $tookTime,
            "contentLength": $contentLength,
            "responseCode": $responseCode

            }
        """.trimIndent()
    }
}

abstract class TowerObserver {
    protected var _isEnabled = true
        set(value) {
            if (field != value) {
                field = value
                if (value) {
                    start()
                } else {
                    stop()
                }
            }
        }

    fun logRequest(requestSent: RequestData) {
        if (_isEnabled) {
            showRequest(requestSent)
        }
    }

    fun logResponse(responseReceived: ResponseData) {
        if (_isEnabled) {
            showResponse(responseReceived)
        }
    }

    open fun start() {

    }

    open fun stop() {

    }

    fun setEnabled(isEnabled: Boolean) {
        this._isEnabled = isEnabled
    }

    abstract fun showRequest(requestSent: RequestData)
    abstract fun showResponse(responseReceived: ResponseData)
    abstract fun showAllResponses(responseReceived: List<ResponseData>)
    open fun shutDown() {

    }
}

data class WatchTowerServerConfig(val serverPort: Int, val webSocketPort: Int) {
    fun toJson(): String {
        return "{\n  \"serverPort\": $serverPort,\n  \"webSocketPort\": $webSocketPort\n}"
    }
}

sealed class WatchtowerLog {
    data class RequestLog(val request: RequestData) : WatchtowerLog()
    data class ResponseLog(val response: ResponseData) : WatchtowerLog()
}