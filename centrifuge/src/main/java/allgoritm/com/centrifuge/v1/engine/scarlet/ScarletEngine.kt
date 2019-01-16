package allgoritm.com.centrifuge.v1.engine.scarlet

import allgoritm.com.centrifuge.v1.contract.Messenger
import allgoritm.com.centrifuge.v1.contract.YCentrifugeEngine
import allgoritm.com.centrifuge.v1.data.*
import android.util.Log
import com.google.gson.Gson
import com.tinder.scarlet.Scarlet
import com.tinder.scarlet.WebSocket
import com.tinder.scarlet.messageadapter.gson.GsonMessageAdapter
import com.tinder.scarlet.streamadapter.rxjava2.RxJava2StreamAdapterFactory
import com.tinder.scarlet.websocket.okhttp.newWebSocketFactory
import io.reactivex.Flowable
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.disposables.CompositeDisposable
import io.reactivex.schedulers.Schedulers
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import org.json.JSONObject
import org.reactivestreams.Processor
import java.lang.Exception
import java.util.*
import java.util.concurrent.TimeUnit

class ScarletEngine(
    builder: OkHttpClient.Builder,
    private val gson: Gson,
    cfg: ConnectionConfig
) : YCentrifugeEngine {

    private var scarletInstance : Scarlet? = null
    private lateinit var publisher : Processor<Event, Event>
    private lateinit var cs : CentrifugeService
    private val compositeDisposable = CompositeDisposable()

    private val subscribeQueue = LinkedList<Command.Subscribe>()
    private var isConnected = false

    private val client : OkHttpClient = builder
        .readTimeout(0, TimeUnit.NANOSECONDS)
        .connectTimeout(cfg.connectTimeoutMs, TimeUnit.MILLISECONDS)
        .pingInterval(cfg.pingIntervalMs, TimeUnit.MILLISECONDS)
        .addInterceptor(HttpLoggingInterceptor())
        .build()

    override fun init(eventPublisher: Processor<Event, Event>) {
        publisher = eventPublisher
    }

    override fun connect(url: String, data: Command.Connect) {
        if (scarletInstance == null) {
            scarletInstance = Scarlet.Builder()
                .webSocketFactory(client.newWebSocketFactory(url))
                .addMessageAdapterFactory(GsonMessageAdapter.Factory(gson))
                .addStreamAdapterFactory(RxJava2StreamAdapterFactory())
                .build()
            cs = scarletInstance!!.create<CentrifugeService>()
        }

        compositeDisposable.add(
        cs.observeResponses()
            .subscribeOn(Schedulers.io())
            .observeOn(AndroidSchedulers.mainThread())
            .subscribe {
                Log.d(LOG_TAG, "response = $it")
                when (it.method) {
                    METHOD_CONNECT -> {
                        isConnected = true
                        subscribeQueue.forEach {
                            cs.sendSubscribe(it)
                        }

                        if (it.error != null) {
                            publisher.onNext(Event.Error(it.method, Exception(it.error)))
                        } else {
                            publisher.onNext(Event.Connected(it.body!!.value))
                        }
                    }
                    METHOD_SUBSCRIBE -> {
                        if (it.error != null) {
                            publisher.onNext(Event.Error(it.method, Exception(it.error)))
                        } else {
                            publisher.onNext(Event.Subscribed("parse_channel", object : Messenger {
                                override val channel: String
                                    get() = "parse_channel"

                                override fun observe(): Flowable<Event> {
                                    return Flowable.empty<Event>()
                                }

                            }))
                        }
                    }
                    METHOD_MESSAGE -> {
                        if (it.error != null) {
                            publisher.onNext(Event.Error(it.method, Exception(it.error)))
                        } else {
                            publisher.onNext(Event.MessageReceived(Message.JSON(it.body!!.value)))
                        }
                    }
                }
            })


        compositeDisposable.add(cs.observeWebSocketEvent()
            .subscribeOn(Schedulers.io())
            .observeOn(AndroidSchedulers.mainThread())
            .subscribe { event ->
                Log.d(LOG_TAG, "websocket event = $event")
                when (event) {
                    is WebSocket.Event.OnConnectionOpened<*> -> {
                        publisher.onNext(Event.SocketOpened(event.webSocket as okhttp3.WebSocket))
                        cs.sendConnect(data)
                        compositeDisposable.add(
                            Flowable.interval(5, TimeUnit.SECONDS)
                                .doOnNext { Log.d("timer", "ping") }
                                .subscribe {
                                    Log.d("interval", "int = ${it % 5}")
                                    when (it % 5) {
                                        0L -> cs.sendPing(Command.Ping)
                                        //1L -> cs.sendPublish(Command.Publish("str"))
                                        2L -> cs.sendHistory(Command.History(commonChannelParams))
                                    }
                                }
                        )
                    }
                    is WebSocket.Event.OnConnectionClosed -> publisher.onNext(Event.SocketClosed())
                    is WebSocket.Event.OnConnectionFailed -> publisher.onNext(Event.SocketConnectionFailed(event.throwable))
                }
            })
    }

    override fun disconnect(data: Command.Disconnect) {
        //sendDisconnect
        isConnected = false
        subscribeQueue.clear()
        compositeDisposable.clear()
    }

    private lateinit var commonChannelParams : ChannelParams
    override fun subscribe(data: Command.Subscribe) {
        commonChannelParams = data.params
        if (isConnected) {
            cs.sendSubscribe(data)
        } else {
            subscribeQueue.offer(data)
        }
    }

    override fun unsubscribe(data: Command.Unsubscribe) {
        TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
    }
}