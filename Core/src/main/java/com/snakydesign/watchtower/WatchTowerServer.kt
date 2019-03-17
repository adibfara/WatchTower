package com.snakydesign.watchtower

import fi.iki.elonen.NanoHTTPD
import java.io.File
import java.io.InputStream

/**
 * @author Adib Faramarzi (adibfara@gmail.com)
 */
internal class WatchTowerServer(
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
        "/main.css" to cssFile,
        "/main.js" to javascriptFile,
        "/jquery.min.js" to jqueryFile
    )

    override fun serve(session: IHTTPSession?): Response {
        session?.let { session ->
            htmls.get(session.uri)?.let {
                return newFixedLengthResponse(it)
            } ?: run {
                files[session.uri]?.let {
                    return newFixedLengthResponse(
                        fi.iki.elonen.NanoHTTPD.Response.Status.OK,
                        "text/css",
                        it
                    )
                }
            }
            println(session)
        }

        return super.serve(session)
    }

}