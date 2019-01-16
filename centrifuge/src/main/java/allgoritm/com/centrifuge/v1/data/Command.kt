package allgoritm.com.centrifuge.v1.data

import java.util.*

const val LOG_TAG = "CENTRIFUGE"

const val METHOD_CONNECT = "connect"
const val METHOD_DISCONNECT = "disconnect"
const val METHOD_SUBSCRIBE = "subscribe"
const val METHOD_UNSUBSCRIBE = "unsubscribe"
const val METHOD_MESSAGE = "message"
const val METHOD_PING = "ping"
const val METHOD_PUBLISH = "publish"
const val MEHOD_HISTORY = "history"

sealed class Command(val uid: String = UUID.randomUUID().toString(), val method: String) {

    data class Connect(val params: ConnectionParams) : Command(method = METHOD_CONNECT)
    class Disconnect : Command(method = METHOD_DISCONNECT) //todo params
    data class Subscribe(val params : ChannelParams): Command(method = METHOD_SUBSCRIBE)
    data class Unsubscribe(val params : ChannelParams): Command(method = METHOD_UNSUBSCRIBE)
    object Ping : Command(method = METHOD_PING)
    data class Publish(val str: String) : Command(method = METHOD_PUBLISH)
    data class History(val params : ChannelParams) : Command(method = MEHOD_HISTORY)

}