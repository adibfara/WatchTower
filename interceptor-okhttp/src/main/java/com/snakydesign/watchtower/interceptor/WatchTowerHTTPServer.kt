package com.snakydesign.watchtower.interceptor

import com.snakydesign.watchtower.models.WatchTowerServerConfig
import fi.iki.elonen.NanoHTTPD

/**
 * @author Adib Faramarzi (adibfara@gmail.com)
 */
internal class WatchTowerHTTPServer(
    private val watchTowerServerConfig: WatchTowerServerConfig,
    html: String,
    cssFile: String,
    javascriptFile: String,
    jqueryFile: String
) : NanoHTTPD(watchTowerServerConfig.serverPort) {
    private val htmls = mapOf(
        "/" to html
    )
    private val files = mapOf(
        "/main.css" to Pair(cssFile, "text/css"),
        "/main.js" to Pair(javascriptFile, "application/javascript"),
        "/jquery.min.js" to Pair(jqueryFile, "application/javascript")
    )

    override fun serve(session: IHTTPSession?): Response {
        session?.let { session ->
            if (session.uri.endsWith("config")) {
                return newFixedLengthResponse(
                    fi.iki.elonen.NanoHTTPD.Response.Status.OK,
                    "application/javascript",
                    watchTowerServerConfig.toJson()
                )
            } else {
                htmls.get(session.uri)?.let {
                    return newFixedLengthResponse(it)
                } ?: run {
                    files[session.uri]?.let {
                        return newFixedLengthResponse(
                            fi.iki.elonen.NanoHTTPD.Response.Status.OK,
                            it.second,
                            it.first
                        )
                    }
                }
            }

        }

        return super.serve(session)
    }

}