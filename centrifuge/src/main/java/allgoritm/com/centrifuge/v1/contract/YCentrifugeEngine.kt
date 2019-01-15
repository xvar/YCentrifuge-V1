package allgoritm.com.centrifuge.v1.contract

import allgoritm.com.centrifuge.v1.data.Command
import allgoritm.com.centrifuge.v1.data.ConnectionConfig
import allgoritm.com.centrifuge.v1.data.Event
import org.reactivestreams.Processor

interface YCentrifugeEngine {
    fun init(connectionConfig: ConnectionConfig, eventPublisher: Processor<Event, Event>)

    fun connect(data: Command.Connect)

    fun disconnect(data: Command.Disconnect)

    fun subscribe(data: Command.Subscribe)

    fun unsubscribe(data: Command.Unsubscribe)
}