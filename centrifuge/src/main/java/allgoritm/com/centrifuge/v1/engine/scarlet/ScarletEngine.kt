package allgoritm.com.centrifuge.v1.engine.scarlet

import allgoritm.com.centrifuge.v1.contract.YCentrifugeEngine
import allgoritm.com.centrifuge.v1.data.Command
import allgoritm.com.centrifuge.v1.data.ConnectionConfig
import allgoritm.com.centrifuge.v1.data.Event
import org.reactivestreams.Processor

class ScarletEngine : YCentrifugeEngine {
    override fun init(connectionConfig: ConnectionConfig, eventPublisher: Processor<Event, Event>) {
        TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
    }

    override fun connect(data: Command.Connect) {
        TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
    }

    override fun disconnect(data: Command.Disconnect) {
        TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
    }

    override fun subscribe(data: Command.Subscribe) {
        TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
    }

    override fun unsubscribe(data: Command.Unsubscribe) {
        TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
    }
}