package allgoritm.com.centrifuge.v1.data

import org.json.JSONObject

sealed class Message {
    data class Text(val value: String) : Message()
    class Bytes(val value: ByteArray) : Message() {
        operator fun component1(): ByteArray = value
    }
    data class JSON(val value: JSONObject) : Message()
}