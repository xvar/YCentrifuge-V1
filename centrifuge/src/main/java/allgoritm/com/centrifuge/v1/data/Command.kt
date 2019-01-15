package allgoritm.com.centrifuge.v1.data

import java.util.*

const val METHOD_CONNECT = "connect"
const val METHOD_DISCONNECT = "disconnect"
const val METHOD_SUBSCRIBE = "subscribe"
const val METHOD_UNSUBSCRIBE = "unsubscribe"
const val METHOD_MESSAGE = "message"

sealed class Command(val uid: String = UUID.randomUUID().toString(), val method: String) {

    data class Connect(val url: String, val params: ConnectionParams) : Command(method = METHOD_CONNECT)
    class Disconnect : Command(method = METHOD_DISCONNECT) //todo params
    data class Subscribe(val params : ChannelParams): Command(method = METHOD_SUBSCRIBE)
    data class Unsubscribe(val params : ChannelParams): Command(method = METHOD_UNSUBSCRIBE)

}