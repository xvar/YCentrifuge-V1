package allgoritm.com.centrifuge.v1.engine.scarlet

import allgoritm.com.centrifuge.v1.contract.Messenger
import allgoritm.com.centrifuge.v1.data.*
import io.reactivex.Flowable
import io.reactivex.processors.BehaviorProcessor
import org.json.JSONObject

internal class ScarletMessenger(
    override val channel: String,
    private val centrifugeService: CentrifugeService,
    private val publisher: BehaviorProcessor<Event>
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
                        if (e is Event.MessageReceived) {
                            return@map e.copy(data = data) as Event
                        }
                        if (e is Event.Leave) {
                            return@map e.copy(data = data) as Event
                        }
                        if (e is Event.Join) {
                            return@map e.copy(data = data) as Event
                        }
                        throw IllegalArgumentException("event is not supported")
                }
    }
    override fun observe(): Flowable<Event> = messengerEvents

    override fun publish(data: JSONObject) = centrifugeService.sendPublish(Command.Publish(PublishParams(channel, data)))

    override fun presence() = centrifugeService.sendPresence(Command.Presence)

    override fun history() = centrifugeService.sendHistory(Command.History(ChannelParams(channel)))

}