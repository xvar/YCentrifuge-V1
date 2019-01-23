package allgoritm.com.centrifuge.v1.contract

import allgoritm.com.centrifuge.v1.data.Command
import allgoritm.com.centrifuge.v1.data.Event
import io.reactivex.processors.BehaviorProcessor
import org.reactivestreams.Processor

interface YCentrifugeEngine {
    fun init(eventPublisher: BehaviorProcessor<Event>)

    fun connect(url: String, data: Command.Connect)

    fun disconnect(data: Command.Disconnect)

    fun subscribe(data: Command.Subscribe)

    fun unsubscribe(data: Command.Unsubscribe)

    fun refresh(data: Command.Refresh)
}