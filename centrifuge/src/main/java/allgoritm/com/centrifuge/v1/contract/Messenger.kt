package allgoritm.com.centrifuge.v1.contract

import allgoritm.com.centrifuge.v1.data.Event
import io.reactivex.Flowable

interface Messenger {
    val channel: String
    fun observe() : Flowable<Event> //will return only message events
    //publish()
    //precense()
    //history()
}