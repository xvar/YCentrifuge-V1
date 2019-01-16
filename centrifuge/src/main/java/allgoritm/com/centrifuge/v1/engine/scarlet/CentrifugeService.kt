package allgoritm.com.centrifuge.v1.engine.scarlet

import allgoritm.com.centrifuge.v1.data.Command
import allgoritm.com.centrifuge.v1.data.Response
import com.tinder.scarlet.WebSocket
import com.tinder.scarlet.ws.Receive
import com.tinder.scarlet.ws.Send
import io.reactivex.Flowable

interface CentrifugeService {
    @Receive
    fun observeWebSocketEvent(): Flowable<WebSocket.Event>
    @Send
    fun sendConnect(connect: Command.Connect)
    @Send
    fun sendSubscribe(connect: Command.Subscribe)
    @Receive
    fun observeResponses(): Flowable<Response>
}