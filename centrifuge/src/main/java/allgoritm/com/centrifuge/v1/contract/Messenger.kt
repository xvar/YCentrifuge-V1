package allgoritm.com.centrifuge.v1.contract

import allgoritm.com.centrifuge.v1.data.Command
import allgoritm.com.centrifuge.v1.data.Event
import io.reactivex.Flowable

interface Messenger {
    val channel: String
    //will return only data events
    fun observe() : Flowable<Event>

    fun publish(command: Command.Publish)
    fun presence(command: Command.Presence)
    fun history(command: Command.History)
}