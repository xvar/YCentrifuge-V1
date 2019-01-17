package allgoritm.com.centrifuge.v1

import allgoritm.com.centrifuge.v1.contract.YCentrifugeEngine
import allgoritm.com.centrifuge.v1.data.*
import allgoritm.com.centrifuge.v1.engine.old_centrifuge.OldCentrifugeEngine
import io.reactivex.Flowable
import io.reactivex.processors.PublishProcessor

object YCentrifugeFactory {
    fun create(connectionConfig: ConnectionConfig) = YCentrifuge(
        OldCentrifugeEngine(connectionConfig)
    )
}

class YCentrifuge internal constructor(
    private val engine : YCentrifugeEngine
) {

    private val eventPublisher = PublishProcessor.create<Event>()
    init { engine.init(eventPublisher) }

    fun events() : Flowable<Event> = eventPublisher
    fun connect(url: String, params: ConnectionParams) = engine.connect(url, Command.Connect(params))
    fun disconnect() = engine.disconnect(Command.Disconnect())
    fun subscribe(channel: String) = engine.subscribe(Command.Subscribe(ChannelParams(channel)))
    fun unsubscribe(channel: String) = engine.unsubscribe(Command.Unsubscribe(ChannelParams(channel)))
}