package allgoritm.com.centrifuge.data

import allgoritm.com.centrifuge.BuildConfig
import android.os.Build
import com.google.gson.Gson
import okhttp3.OkHttpClient
import okhttp3.Request

class RequestManager(val client: OkHttpClient, val gson: Gson) {

    fun getRequestBuilder(): Request.Builder {

        val builder = Request.Builder()
            .header("User-Agent", getUserAgent())

        builder.header("X-Auth-Token", getAuthToken())

        return builder
    }

    private fun getUserAgent(): String {
        return "Youla/" + BuildConfig.VERSION_NAME + " (Android Version " + Build.VERSION.RELEASE + ")"
    }

    private fun getAuthToken(): String {
        return BuildConfig.AUTH_TOKEN
    }


}