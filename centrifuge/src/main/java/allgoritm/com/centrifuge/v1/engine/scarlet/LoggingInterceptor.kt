package allgoritm.com.centrifuge.v1.engine.scarlet

import android.util.Log
import okhttp3.Interceptor
import okhttp3.Response

class LoggingInterceptor(val token: String) : Interceptor {
    override fun intercept(chain: Interceptor.Chain): Response {
        val request = chain.request()
        val newRequestBuilder = request
            .newBuilder()
            //.url("wss://api.youla.io/c/connection/websocket")
        newRequestBuilder
            .addHeader("Sec-WebSocket-Extensions", "permessage-deflate; client_max_window_bits; server_max_window_bits=15")

        val newRequest = newRequestBuilder.build()
        val t1 = System.nanoTime()
        Log.d("WebOkHttp", String.format("Sending request %s on %s%n%s",
            newRequest.url(), chain.connection(), newRequest.headers()))

        val response = chain.proceed(newRequest)

        val t2 = System.nanoTime()
        Log.d("WebOkHttp", String.format("Received response for %s in %.1fms%n%s",
            response.request().url(), (t2 - t1) / 1e6, response.headers()))

        return response
    }
}