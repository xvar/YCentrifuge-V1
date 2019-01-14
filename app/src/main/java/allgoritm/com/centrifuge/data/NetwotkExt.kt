package allgoritm.com.centrifuge.data

import allgoritm.com.centrifuge.BuildConfig
import allgoritm.com.centrifuge.BuildConfig.API_HOST
import allgoritm.com.centrifuge.BuildConfig.API_VERSION
import com.google.gson.Gson
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.Response
import org.json.JSONObject
import java.util.HashMap



inline fun <reified T> RequestManager.get(url: String, params: Map<String, String>? = null): T =
        this.getRequestBuilder()
                .url(getUrl(url, params))
                .get()
                .build()
                .execute(client, gson)

inline fun <reified T> Request.execute(client: OkHttpClient, gson: Gson): T =
        client.newCall(this).execute().use { it.parseData(gson) }

inline fun <reified T> Response.parseData(gson: Gson): T {
    val resp = this.getResp()
    if(this.isSuccessful){
        val data = resp!!.getJSONObject("data").toString()
        return gson.fromJson(data, T::class.java)
    } else throw Exception(resp.toString())

}

fun getUrl(url: String, parameters: Map<String, String>?): String {
    val params = addBaseParams(parameters)
    return API_URL + check(url) + parameters(params)
}

fun parameters(parameters: Map<String, String>): String {
    val keys = parameters.keys.iterator()
    val builder = StringBuilder()
    var isFirst = true
    while (keys.hasNext()) {
        builder.append(if (isFirst) "?" else "&")
        isFirst = false
        val key = keys.next()
        builder.append(key)
        builder.append("=")
        builder.append(parameters[key])
    }
    return builder.toString()
}

fun check(string: String): String {
    var str = string
    return if (str.startsWith("/")) {
        str = str.substring(1)
        check(str)
    } else {
        str
    }
}

fun Response.getResp() = this.body()?.let { JSONObject(it.string()) }

private val API_URL = "https://$API_HOST/api/v$API_VERSION/"
private val APP_ID = "android/" + BuildConfig.VERSION_CODE

private fun addBaseParams(parameters: Map<String, String>?): Map<String, String> {
    val params = HashMap<String, String>()
    parameters?.let { params.putAll(it) }
    params["app_id"] = APP_ID
    params["uid"] = "0000111122222333344444"
    params["adv_id"] = "1231231231230"

    return params
}