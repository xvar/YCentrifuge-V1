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
import javax.inject.Inject

class MainViewModel @Inject constructor(
    private val service: CentrifugeCredentialsService,
    private val cf: YCentrifugeFactory
) : BaseViewModel(), Consumer<UiEvent> {

    private val strProcessor = BehaviorProcessor.create<List<String>>()
    private val centrifuge = cf.create(ConnectionConfig())
    private lateinit var centrifugeCredentials : CentrifugeCredentials
    private val logs = ArrayList<String>()

    fun observe() : Flowable<List<String>> {
        return strProcessor
    }

    override fun accept(e: UiEvent) {
        when (e) {
            is UiEvent.CredentialsAndConnect -> {
                addDisposable(service.getCentrifugeCredentials()
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
            is UiEvent.Subscribe -> centrifuge.subscribe(SubscribeParams(centrifugeCredentials.commonChannel))
            is UiEvent.Unsubscribe -> centrifuge.unsubscribe(centrifugeCredentials.commonChannel)
            is UiEvent.Publish -> messenger.publish(JSONObject().apply { put("key", e.data) })
            is UiEvent.History -> messenger.history()
            is UiEvent.Presence -> messenger.presence()
        }
    }

    private fun startCentrifuge(c: CentrifugeCredentials) {
        centrifugeCredentials = c
        addDisposable(
            centrifuge.events().subscribe { event ->
                Log.e("client", "$event")
                publish(event.toString())
                if (event is Event.Subscribed) {
                    Log.e("client", "event subscribed")
                    addMessenger(event)
                }
            }
        )

        centrifuge.connect(c.url, ConnectionParams(c.userId, c.timestamp.toString(), "", c.token))
    }

    private fun publish(newStr: String) {
        logs.add(newStr)
        strProcessor.onNext(logs)
    }

    private lateinit var messenger: Messenger
    private fun addMessenger(event: Event.Subscribed) {
        messenger = event.receiver
        addDisposable(
            messenger.observe()
                .subscribe(
                    { data ->
                        Log.e("client", "messenger = $data")
                        publish(data.toString())
                    },
                    {
                        Log.e("client", "messenger, throwable = $it")
                    }
                )
        )
    }

}
