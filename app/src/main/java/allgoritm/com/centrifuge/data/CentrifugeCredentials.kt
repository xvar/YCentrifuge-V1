package allgoritm.com.centrifuge.data

import com.google.gson.annotations.SerializedName

data class CentrifugeCredentials(
        @SerializedName("websocket_url")
        val url: String,
        @SerializedName("url")
        val httpUrl: String,
        @SerializedName("user")
        val userId: String,
        @SerializedName("timestamp")
        val timestamp: Long,
        @SerializedName("token")
        val token: String,
        @SerializedName("common_channel")
        val commonChannel: String
)