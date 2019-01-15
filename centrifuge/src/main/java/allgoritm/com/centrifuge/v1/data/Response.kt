package allgoritm.com.centrifuge.v1.data

import org.json.JSONObject

data class Response(val uid: String, val method: String, val body: JSONObject?, val error: Throwable?)