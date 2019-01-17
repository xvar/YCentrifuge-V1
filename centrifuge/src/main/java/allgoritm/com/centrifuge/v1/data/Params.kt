package allgoritm.com.centrifuge.v1.data

import org.json.JSONObject

data class ConnectionParams(val user: String, val timestamp: String, val info: String = "", val token: String)
data class ChannelParams(val channel : String)
data class PublishParams(val channel: String, val data: JSONObject)
data class SubscribeParams(val channel: String, val client: String? = null, val info: String? = null, val sign: String? = null)