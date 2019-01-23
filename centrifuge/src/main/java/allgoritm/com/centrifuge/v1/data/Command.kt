package allgoritm.com.centrifuge.v1.data

import java.util.*

const val LOG_TAG = "YCENTRIFUGE"

const val CHANNEL = "channel"
const val DATA = "data"

const val METHOD_CONNECT = "connect"
const val METHOD_DISCONNECT = "disconnect"
const val METHOD_SUBSCRIBE = "subscribe"
const val METHOD_UNSUBSCRIBE = "unsubscribe"
const val METHOD_PUBLISH = "publish"
const val METHOD_PRESENCE = "presence"
const val METHOD_HISTORY = "history"
const val METHOD_JOIN = "join"
const val METHOD_LEAVE = "leave"
const val METHOD_MESSAGE = "message"
const val METHOD_REFRESH = "refresh"
const val METHOD_PING = "ping"

sealed class Command(val uid: String = UUID.randomUUID().toString(), val method: String) {

    data class Connect(val params: ConnectionParams) : Command(method = METHOD_CONNECT)
    class Disconnect : Command(method = METHOD_DISCONNECT)
    data class Subscribe(val params : SubscribeParams): Command(method = METHOD_SUBSCRIBE)
    data class Unsubscribe(val params : ChannelParams): Command(method = METHOD_UNSUBSCRIBE)
    object Ping : Command(method = METHOD_PING)
    data class Publish(val params: PublishParams) : Command(method = METHOD_PUBLISH)
    data class History(val params : ChannelParams) : Command(method = METHOD_HISTORY)
    object Presence: Command(method = METHOD_PRESENCE)
    object Refresh: Command(method = METHOD_REFRESH)

}