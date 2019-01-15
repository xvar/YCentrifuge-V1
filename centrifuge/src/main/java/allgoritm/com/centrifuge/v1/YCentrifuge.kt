package allgoritm.com.centrifuge.v1

import allgoritm.com.centrifuge.v1.contract.YCentrifugeEngine
import allgoritm.com.centrifuge.v1.data.Command
import allgoritm.com.centrifuge.v1.data.ConnectionConfig
import allgoritm.com.centrifuge.v1.data.Event
import io.reactivex.Flowable
import io.reactivex.processors.PublishProcessor

class YCentrifuge(
    private val connectionConfig: ConnectionConfig,
    private val engine : YCentrifugeEngine
) {

    private val eventPublisher = PublishProcessor.create<Event>()
    init {
        engine.init(connectionConfig, eventPublisher)
    }

    fun events() : Flowable<Event> = eventPublisher
    fun connect(data: Command.Connect) = engine.connect(data)
    fun disconnect(data: Command.Disconnect) = engine.disconnect(data)
    fun subscribe(data: Command.Subscribe) = engine.subscribe(data)
    fun unsubscribe(data: Command.Unsubscribe) = engine.unsubscribe(data)
}