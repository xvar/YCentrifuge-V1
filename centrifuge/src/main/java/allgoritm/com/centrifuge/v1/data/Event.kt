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
    data class Connected(val data: JSONObject) : Event()
    class Disconnected : Event()

    data class Subscribed(val channel: String, val receiver: Messenger) : Event()
    data class Unsubscribed(val channel: String) : Event()

    //data class MessageReceived(val message: Message) : Event()
    data class Error(val method: String, val throwable: Throwable) //? разделить на отдельные классы ошибок
}