package allgoritm.com.centrifuge.ui.main

import allgoritm.com.centrifuge.BaseViewModel
import allgoritm.com.centrifuge.data.CentrifugeCredentials
import allgoritm.com.centrifuge.data.CentrifugeCredentialsService
import allgoritm.com.centrifuge.data.UiEvent
import allgoritm.com.centrifuge.v1.YCentrifugeFactory
import allgoritm.com.centrifuge.v1.contract.Messenger
import allgoritm.com.centrifuge.v1.data.ConnectionConfig
import allgoritm.com.centrifuge.v1.data.ConnectionParams
import allgoritm.com.centrifuge.v1.data.Event
import allgoritm.com.centrifuge.v1.data.SubscribeParams
import android.util.Log
import io.reactivex.Flowable
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.functions.Consumer
import io.reactivex.processors.BehaviorProcessor
import io.reactivex.schedulers.Schedulers
import javax.inject.Inject

class MainViewModel @Inject constructor(
    private val service: CentrifugeCredentialsService,
    private val cf: YCentrifugeFactory
) : BaseViewModel(), Consumer<UiEvent> {

    private val strProcessor = BehaviorProcessor.create<String>()
    private val centrifuge = cf.create(ConnectionConfig())

    fun observe() : Flowable<String> {
        return strProcessor
    }

    override fun accept(e: UiEvent) {
        when (e) {
            is UiEvent.GetCredentials -> {
                addDisposable(service.getCentrifugeCredentials()
                    .subscribeOn(Schedulers.io())
                    .observeOn(AndroidSchedulers.mainThread())
                    .subscribe {
                            c, exc ->
                        if (exc != null) {
                            strProcessor.onNext(exc.toString())
                        } else {
                            startCentrifuge(c)
                        }
                    })
            }
        }
    }

    private fun startCentrifuge(c: CentrifugeCredentials) {
        addDisposable(
            centrifuge.events().subscribe { event ->
                Log.e("client", "$event")
                strProcessor.onNext(event.toString())
                if (event is Event.Subscribed) {
                    Log.e("client", "event subscribed")
                    addMessenger(event)
                }
            }
        )
        strProcessor.onNext(c.toString())
        centrifuge.connect(c.url, ConnectionParams(c.userId, c.timestamp.toString(), "", c.token))
        centrifuge.subscribe(SubscribeParams(c.commonChannel))

    }

    private fun addMessenger(event: Event.Subscribed) {
        val messenger = event.receiver
        addDisposable(
            messenger.observe()
                .subscribe(
                    { data ->
                        Log.e("client", "messenger = $data")
                        strProcessor.onNext(data.toString())
                    },
                    {
                        Log.e("client", "messenger, throwable = $it")
                    }
                )
        )
    }

}
