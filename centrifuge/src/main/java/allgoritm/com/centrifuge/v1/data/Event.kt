package allgoritm.com.centrifuge.v1.data

import allgoritm.com.centrifuge.v1.contract.Messenger
import okhttp3.WebSocket
import org.json.JSONObject

sealed class Event {
    //socket events
    data class SocketOpened(val webSocket: WebSocket) : Event()
    class SocketClosed : Event()
    data class SocketConnectionFailed(val throwable: Throwable) : Event()

    //centrifuge events
    data class Connected(override val data: JSONObject) : Event(), DataEvent
    class Disconnected : Event()

    data class Subscribed(val channel: String, val receiver: Messenger) : Event()
    data class Unsubscribed(val channel: String) : Event()

    data class MessageReceived(override val data: JSONObject) : Event(), DataEvent
    data class Leave(override val data: JSONObject) : Event(), DataEvent
    data class Join(override val data: JSONObject) : Event(), DataEvent

    data class Error(val method: String, val throwable: Throwable) : Event() //? разделить на отдельные классы ошибок
}

interface DataEvent {
    val data: JSONObject
}