package com.snakydesign.watchtower

import fi.iki.elonen.NanoHTTPD
import java.io.File
import java.io.InputStream

/**
 * @author Adib Faramarzi (adibfara@gmail.com)
 */
internal class WatchTowerHTTPServer(
    port: Int,
    html: String,
    cssFile: String,
    javascriptFile: String,
    jqueryFile: String
) : NanoHTTPD(port) {
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
            println(session)
        }

        return super.serve(session)
    }

}