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
import io.reactivex.Scheduler
import io.reactivex.disposables.CompositeDisposable
import okhttp3.OkHttpClient
import org.reactivestreams.Processor
import java.util.concurrent.ConcurrentLinkedQueue
import java.util.concurrent.TimeUnit
import java.util.concurrent.atomic.AtomicBoolean

class ScarletEngine(
    private val builder: OkHttpClient.Builder,
    private val gson: Gson,
    private val cfg: ConnectionConfig,
    private val workScheduler : Scheduler,
    private val resultScheduler: Scheduler
) : YCentrifugeEngine {

    private var scarletInstance : Scarlet? = null
    private lateinit var publisher : Processor<Event, Event>
    private lateinit var cs : CentrifugeService
    private val compositeDisposable = CompositeDisposable()

    private val subscribeQueue = ConcurrentLinkedQueue<Command.Subscribe>()
    private var isConnected = AtomicBoolean(false)

    private lateinit var client : OkHttpClient

    override fun init(eventPublisher: Processor<Event, Event>) {
        publisher = eventPublisher
    }

    private fun initClient(token: String): OkHttpClient {
        return builder
            .readTimeout(0, TimeUnit.NANOSECONDS)
            .connectTimeout(cfg.connectTimeoutMs, TimeUnit.MILLISECONDS)
            .pingInterval(cfg.pingIntervalMs, TimeUnit.MILLISECONDS)
            .addInterceptor(LoggingInterceptor())
            .build()
    }

    override fun connect(url: String, data: Command.Connect) {
        if (scarletInstance == null) {
            client = initClient(data.params.token)
            scarletInstance = Scarlet.Builder()
                .webSocketFactory(client.newWebSocketFactory(url))
                .addMessageAdapterFactory(GsonMessageAdapter.Factory(gson))
                .addStreamAdapterFactory(RxJava2StreamAdapterFactory())
                .build()
            cs = scarletInstance!!.create<CentrifugeService>()
        }

        compositeDisposable.add(
        cs.observeResponses()
            .subscribeOn(workScheduler)
            .observeOn(resultScheduler)
            .subscribe ({
                Log.d(LOG_TAG, "response = $it")
                when (it.method) {
                    METHOD_CONNECT -> {
                        isConnected.set(true)
                        subscribe()

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
            },
                {err -> Log.e(LOG_TAG, "parsed error", err)}
            ))


        compositeDisposable.add(cs.observeWebSocketEvent()
            .subscribeOn(workScheduler)
            .observeOn(resultScheduler)
            .subscribe ({ event ->
                Log.d(LOG_TAG, "websocket event = $event")
                when (event) {
                    is WebSocket.Event.OnConnectionOpened<*> -> {
                        val webSocket = event.webSocket as okhttp3.WebSocket
                        publisher.onNext(Event.SocketOpened(webSocket))
                        cs.sendConnect(data)
                        compositeDisposable.add(
                            Flowable.interval(25, TimeUnit.SECONDS)
                                .doOnNext { Log.d("timer", "ping") }
                                .subscribe {
                                    cs.sendPing(Command.Ping)
                                }
                        )
                    }
                    is WebSocket.Event.OnConnectionClosed -> publisher.onNext(Event.SocketClosed())
                    is WebSocket.Event.OnConnectionFailed -> publisher.onNext(Event.SocketConnectionFailed(event.throwable))
                }
            },
                {err -> Log.e(LOG_TAG, "event error", err)}
            ))
    }

    override fun disconnect(data: Command.Disconnect) {
        //sendDisconnect
        isConnected.set(false)
        subscribeQueue.clear()
        compositeDisposable.clear()
    }

    override fun subscribe(data: Command.Subscribe) {
        if (!subscribeQueue.contains(data)) {
            subscribeQueue.offer(data)
        }
        if (isConnected.get()) {
            subscribe()
        }
    }

    private fun subscribe() {
        subscribeQueue.forEach {
            cs.sendSubscribe(it)
        }
    }

    override fun unsubscribe(data: Command.Unsubscribe) {
        TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
    }
}