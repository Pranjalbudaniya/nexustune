package com.nexus.tune.core.extractor

import okhttp3.OkHttpClient
import org.schabi.newpipe.extractor.downloader.Downloader
import org.schabi.newpipe.extractor.downloader.Request
import org.schabi.newpipe.extractor.downloader.Response
import java.io.IOException

class OkHttpDownloader(
    private val client: OkHttpClient,
    private val userAgent: String = "Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/120.0.0.0 Safari/537.36"
) : Downloader() {

    @Throws(IOException::class)
    override fun execute(request: Request): Response {
        val builder = okhttp3.Request.Builder()
            .url(request.url())
            .header("User-Agent", userAgent)

        request.headers()?.forEach { (headerName, headerValues) ->
            headerValues.forEach { headerValue ->
                builder.addHeader(headerName, headerValue)
            }
        }

        val httpMethod = request.httpMethod()
        val dataToSend = request.dataToSend()

        if (httpMethod.equals("POST", ignoreCase = true)) {
            val body = if (dataToSend != null) {
                okhttp3.RequestBody.create(null, dataToSend)
            } else {
                okhttp3.RequestBody.create(null, ByteArray(0))
            }
            builder.post(body)
        } else {
            builder.get()
        }

        val call = client.newCall(builder.build())
        val okHttpResponse = call.execute()

        val responseBody = okHttpResponse.body?.string()
        return Response(
            okHttpResponse.code,
            okHttpResponse.message,
            okHttpResponse.headers.toMultimap(),
            responseBody,
            okHttpResponse.request.url.toString()
        )
    }
}

