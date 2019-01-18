package allgoritm.com.centrifuge.data

const val CREDENTIALS = 1
const val HISTORY = 2
const val SUBSCRIBE = 3
const val UNSUBSCRIBE = 4
const val DISCONNECT = 5
const val PUBLISH = 6
const val PRESENCE = 7
sealed class UiEvent(val id: Int) {
    class CredentialsAndConnect: UiEvent(CREDENTIALS)
    class Subscribe : UiEvent(SUBSCRIBE)
    class Unsubscribe : UiEvent(UNSUBSCRIBE)
    class Disconnect : UiEvent(DISCONNECT)
    class Publish(val data : String) : UiEvent(PUBLISH)
    class Presence : UiEvent(PRESENCE)
    class History : UiEvent(HISTORY)
}