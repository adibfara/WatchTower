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
    return this.replace("\"", "\\\"")
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
