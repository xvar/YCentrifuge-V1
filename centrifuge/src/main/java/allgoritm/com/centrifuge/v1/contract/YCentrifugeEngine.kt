package allgoritm.com.centrifuge.v1.contract

import allgoritm.com.centrifuge.v1.data.Command
import allgoritm.com.centrifuge.v1.data.Event
import org.reactivestreams.Processor

interface YCentrifugeEngine {
    fun init(eventPublisher: Processor<Event, Event>)

    fun connect(url: String, data: Command.Connect)

    fun disconnect(data: Command.Disconnect)

    fun subscribe(data: Command.Subscribe)

    fun unsubscribe(data: Command.Unsubscribe)
}