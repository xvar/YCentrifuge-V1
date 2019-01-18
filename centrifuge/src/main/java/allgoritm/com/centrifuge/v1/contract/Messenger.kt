package allgoritm.com.centrifuge.v1.contract

import allgoritm.com.centrifuge.v1.data.Event
import io.reactivex.Flowable
import org.json.JSONObject

interface Messenger {
    val channel: String
    //will return only data events
    fun observe() : Flowable<Event>

    fun publish(data: JSONObject)
    fun presence()
    fun history()
}