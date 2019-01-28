package allgoritm.com.centrifuge.ui.main

import allgoritm.com.centrifuge.BaseViewModel
import allgoritm.com.centrifuge.data.CentrifugeCredentials
import allgoritm.com.centrifuge.data.CentrifugeCredentialsService
import allgoritm.com.centrifuge.data.UiEvent
import allgoritm.com.centrifuge.v1.YCentrifugeFactory
import allgoritm.com.centrifuge.v1.contract.Messenger
import allgoritm.com.centrifuge.v1.data.*
import android.util.Log
import io.reactivex.Flowable
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.functions.Consumer
import io.reactivex.processors.BehaviorProcessor
import io.reactivex.schedulers.Schedulers
import org.json.JSONObject
import java.util.*
import javax.inject.Inject

class MainViewModel @Inject constructor(
    private val service: CentrifugeCredentialsService,
    private val cf: YCentrifugeFactory
) : BaseViewModel(), Consumer<UiEvent> {

    private val strProcessor = BehaviorProcessor.create<List<String>>()
    private val centrifuge = cf.create(ConnectionConfig())
    private val channelStack = Stack<String>()
    private val messengerStack = Stack<Messenger>()
    private val logs = ArrayList<String>()

    fun observe() : Flowable<List<String>> {
        return strProcessor
    }

    override fun accept(e: UiEvent) {
        when (e) {
            is UiEvent.CredentialsAndConnect -> {
                logs.clear()
                addDisposable("main_vm_cred", service.getCentrifugeCredentials()
                    .subscribeOn(Schedulers.io())
                    .observeOn(AndroidSchedulers.mainThread())
                    .subscribe {
                            c, exc ->
                        if (exc != null) {
                            publish(exc.toString())
                        } else {
                            startCentrifuge(c)
                        }
                    })
            }
            is UiEvent.Subscribe -> centrifuge.subscribe(SubscribeParams(channelStack.peek()))
            is UiEvent.Unsubscribe -> {
                val channel = channelStack.pop()
                if (channelStack.size == 0) {
                    channelStack.push(channel)
                }
                centrifuge.unsubscribe(channel)
            }
            is UiEvent.Publish -> messengerStack.peek().publish(JSONObject().apply { put("key", e.data) })
            is UiEvent.History -> messengerStack.peek().history()
            is UiEvent.Presence -> messengerStack.peek().presence()
            is UiEvent.Disconnect -> centrifuge.disconnect()
            is UiEvent.Refresh -> centrifuge.refresh()
        }
    }

    private fun startCentrifuge(c: CentrifugeCredentials) {
        channelStack.push(c.commonChannel)
        addDisposable("main_vm_ui_events",
            centrifuge.events().subscribe { event ->
                Log.e("client", "$event")
                publish("all_events $event")
                if (event is Event.Subscribed) {
                    Log.e("client", "event subscribed")
                    addMessenger(channelStack.peek(), event)
                }
            }
        )

        centrifuge.connect(c.url, ConnectionParams(c.userId, c.timestamp.toString(), "", c.token))
    }

    private fun publish(newStr: String) {
        logs.add(newStr)
        strProcessor.onNext(logs)
    }

    private val testChatChannel = "chat#5abcfaf55f9037628c786b22,5abb8f965f903765305c6e9b"
    private fun addMessenger(key: String, event: Event.Subscribed) {
        messengerStack.push(event.receiver)
        val messenger = messengerStack.peek()
        addDisposable(key,
            messenger.observe()
                .subscribe(
                    { data ->
                        val logStr = "channel = $key, data = $data"
                        Log.e("client", logStr)
                        publish(logStr)
                        if (data is Event.MessageReceived && channelStack.peek() != testChatChannel) {
                            channelStack.push(testChatChannel)
                        }
                    },
                    {
                        Log.e("client", "messenger, throwable = $it")
                    }
                )
        )
    }

}
