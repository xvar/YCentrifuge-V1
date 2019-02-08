package allgoritm.com.centrifuge.v1.contract

import allgoritm.com.centrifuge.v1.data.Command
import allgoritm.com.centrifuge.v1.data.Event
import io.reactivex.processors.BehaviorProcessor

internal interface YCentrifugeEngine {
    fun init(eventPublisher: BehaviorProcessor<Event>)

    fun connect(url: String, data: Command.Connect, force : Boolean = false)

    fun disconnect(data: Command.Disconnect)

    fun subscribe(data: Command.Subscribe)

    fun unsubscribe(data: Command.Unsubscribe)

    fun refresh(data: Command.Refresh)
}