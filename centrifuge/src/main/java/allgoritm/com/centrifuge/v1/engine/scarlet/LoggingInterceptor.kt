package allgoritm.com.centrifuge.v1.engine.scarlet

import allgoritm.com.centrifuge.v1.data.LOG_TAG
import allgoritm.com.centrifuge.v1.util.log.Logger
import okhttp3.Interceptor
import okhttp3.Response

internal class LoggingInterceptor(private val logger: Logger) : Interceptor {
    private val TAG = "${LOG_TAG}_WebOkHttp"
    override fun intercept(chain: Interceptor.Chain): Response {
        val request = chain.request()
        val newRequestBuilder = request
            .newBuilder()
        newRequestBuilder
            .addHeader("Sec-WebSocket-Extensions", "permessage-deflate; client_max_window_bits; server_max_window_bits=15")

        val newRequest = newRequestBuilder.build()
        val t1 = System.nanoTime()
        logger.log(tag = TAG, msg = String.format("Sending request %s on %s%n%s",
            newRequest.url, chain.connection(), newRequest.headers
        ))

        val response = chain.proceed(newRequest)

        val t2 = System.nanoTime()
        logger.log(tag = TAG, msg = String.format("Received response for %s in %.1fms%n%s",
            response.request.url, (t2 - t1) / 1e6, response.headers
        ))

        return response
    }
}