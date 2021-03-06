package allgoritm.com.centrifuge.v1.engine.scarlet

import allgoritm.com.centrifuge.v1.data.Command
import allgoritm.com.centrifuge.v1.data.Response
import com.tinder.scarlet.WebSocket
import com.tinder.scarlet.ws.Receive
import com.tinder.scarlet.ws.Send
import io.reactivex.Flowable

internal interface CentrifugeService {
    @Receive
    fun observeWebSocketEvent(): Flowable<WebSocket.Event>
    @Send
    fun sendConnect(connect: Command.Connect)
    @Send
    fun sendDisconnect(connect: Command.Disconnect)
    @Send
    fun sendSubscribe(connect: Command.Subscribe)
    @Send
    fun sendUnsubscribe(connect: Command.Unsubscribe)
    @Send
    fun sendPing(command: Command.Ping)
    @Send
    fun sendPublish(command: Command.Publish)
    @Send
    fun sendHistory(command: Command.History)
    @Send
    fun sendPresence(command: Command.Presence)
    @Send
    fun sendRefresh(command: Command.Refresh)
    @Receive
    fun observeResponses(): Flowable<Response>
}