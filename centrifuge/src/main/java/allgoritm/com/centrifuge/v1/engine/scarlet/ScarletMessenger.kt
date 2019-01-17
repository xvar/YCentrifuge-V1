package allgoritm.com.centrifuge.v1.engine.scarlet

import allgoritm.com.centrifuge.v1.contract.Messenger
import allgoritm.com.centrifuge.v1.data.*
import io.reactivex.Flowable
import org.json.JSONObject
import org.reactivestreams.Processor
import java.lang.IllegalArgumentException

class ScarletMessenger(
    override val channel: String,
    private val centrifugeService: CentrifugeService,
    private val publisher: Processor<Event, Event>
) : Messenger {

    private fun isSupportedEvent(event: Event) : Boolean {
        return (event is DataEvent) && (event is Event.MessageReceived || event is Event.Leave || event is Event.Join)
    }

    private val messengerEvents by lazy {
        Flowable.fromPublisher(publisher)
                .filter{ e -> isSupportedEvent(e) && (e as DataEvent).data[CHANNEL] == channel }
                .map { e: Event ->
                    val dataEvent = e as DataEvent
                    val data = dataEvent.data[DATA] as JSONObject
                    if (e is Event.MessageReceived) { return@map e.copy(data = data) as Event }
                    if (e is Event.Leave) { return@map e.copy(data = data) as Event }
                    if (e is Event.Join) { return@map e.copy(data = data) as Event }
                    throw IllegalArgumentException("event is not supported")
                }
    }
    override fun observe(): Flowable<Event> = messengerEvents

    override fun publish(command: Command.Publish) = centrifugeService.sendPublish(command)

    override fun presence(command: Command.Presence) = centrifugeService.sendPresence(command)

    override fun history(command: Command.History) = centrifugeService.sendHistory(command)

}