package allgoritm.com.centrifuge.v1.engine.scarlet

import allgoritm.com.centrifuge.v1.contract.Messenger
import allgoritm.com.centrifuge.v1.contract.YCentrifugeEngine
import allgoritm.com.centrifuge.v1.data.*
import allgoritm.com.centrifuge.v1.util.CompositeDisposablesMap
import android.util.Log
import com.google.gson.Gson
import com.tinder.scarlet.Scarlet
import com.tinder.scarlet.WebSocket
import com.tinder.scarlet.messageadapter.gson.GsonMessageAdapter
import com.tinder.scarlet.streamadapter.rxjava2.RxJava2StreamAdapterFactory
import com.tinder.scarlet.websocket.okhttp.newWebSocketFactory
import io.reactivex.Flowable
import io.reactivex.Scheduler
import io.reactivex.processors.BehaviorProcessor
import okhttp3.OkHttpClient
import java.util.concurrent.ConcurrentHashMap
import java.util.concurrent.TimeUnit
import java.util.concurrent.atomic.AtomicBoolean

class ScarletEngine(
    private val builder: OkHttpClient.Builder,
    private val gson: Gson,
    private val cfg: ConnectionConfig,
    private val workScheduler : Scheduler,
    private val resultScheduler: Scheduler
) : YCentrifugeEngine {

    private val keyPing = "scarlet_engine_ping"
    private val keyResponses = "scarlet_engine_responses"
    private val keyEvents = "scarlet_engine_events"

    private var scarletInstance : Scarlet? = null
    private lateinit var publisher : BehaviorProcessor<Event>
    private lateinit var cs : CentrifugeService
    private val compositeDisposable = CompositeDisposablesMap()

    private val subscribeMap = ConcurrentHashMap<String, Command.Subscribe>()
    private var isConnected = AtomicBoolean(false)
    private val messengerMap = ConcurrentHashMap<String, Messenger>()

    private lateinit var client : OkHttpClient

    override fun init(eventPublisher: BehaviorProcessor<Event>) {
        publisher = eventPublisher
    }

    private fun initClient(): OkHttpClient {
        return builder
            .readTimeout(0, TimeUnit.NANOSECONDS)
            .connectTimeout(cfg.connectTimeoutMs, TimeUnit.MILLISECONDS)
            .pingInterval(cfg.pingIntervalMs, TimeUnit.MILLISECONDS)
            .addInterceptor(LoggingInterceptor())
            .build()
    }


    override fun connect(url: String, data: Command.Connect) {
        if (scarletInstance == null) {
            client = initClient()
            scarletInstance = Scarlet.Builder()
                .webSocketFactory(client.newWebSocketFactory(url))
                .addMessageAdapterFactory(GsonMessageAdapter.Factory(gson))
                .addStreamAdapterFactory(RxJava2StreamAdapterFactory())
                .build()
            cs = scarletInstance!!.create<CentrifugeService>()
        }

        val responses = cs.observeResponses()
        compositeDisposable.put(keyResponses, responses
            .subscribeOn(workScheduler)
            .observeOn(resultScheduler)
            .subscribe ({
                handle(it)
            },
                {err -> Log.e(LOG_TAG, "parsed error", err)}
            ))


        compositeDisposable.put(keyEvents, cs.observeWebSocketEvent()
            .subscribeOn(workScheduler)
            .observeOn(resultScheduler)
            .subscribe ({ event ->
                handleEvent(event, data)
            },
                {err -> Log.e(LOG_TAG, "event error", err)}
            ))
    }

    private fun handleEvent(
        event: WebSocket.Event?,
        data: Command.Connect
    ) {
        Log.d(LOG_TAG, "websocket event = $event")
        when (event) {
            is WebSocket.Event.OnConnectionOpened<*> -> {
                val webSocket = event.webSocket as okhttp3.WebSocket
                publisher.onNext(Event.SocketOpened(webSocket))
                cs.sendConnect(data)
                schedulePing()
            }
            is WebSocket.Event.OnConnectionClosed -> publisher.onNext(Event.SocketClosed())
            is WebSocket.Event.OnConnectionFailed -> publisher.onNext(Event.SocketConnectionFailed(event.throwable))
        }
    }

    private fun schedulePing() {
        compositeDisposable.put(keyPing,
            Flowable.interval(cfg.pingIntervalMs, TimeUnit.MILLISECONDS)
                .doOnNext { Log.d("timer", "ping") }
                .subscribe {
                    cs.sendPing(Command.Ping)
                }
        )
    }

    private fun handle(it: Response) {
        Log.d(LOG_TAG, "response = $it")
        if (it.error != null) {
            handleError(it)
        } else {
            handleResponse(it)
        }
    }

    private fun handleResponse(it: Response) {
        when (it.method) {
            METHOD_CONNECT -> {
                val jsonBody = it.body!!.value
                isConnected.set(true)
                subscribe()
                publisher.onNext(Event.Connected(jsonBody))
            }
            METHOD_DISCONNECT -> {
                isConnected.set(false)
                subscribeMap.clear()
                compositeDisposable.clearAll()
                publisher.onNext(Event.Disconnected())
            }
            METHOD_SUBSCRIBE -> {
                val jsonBody = it.body!!.value
                val channel = jsonBody[CHANNEL] as String
                val messenger = messengerMap.getOrPut(channel) {
                    ScarletMessenger(channel, cs, publisher)
                }
                publisher.onNext(Event.Subscribed(channel, messenger))
            }
            METHOD_UNSUBSCRIBE -> {
                synchronized(this) {
                    val jsonBody = it.body!!.value
                    val channel = jsonBody[CHANNEL] as String
                    subscribeMap.remove(channel)
                    messengerMap.remove(channel)
                    publisher.onNext(Event.Unsubscribed(channel))
                }
            }
            METHOD_MESSAGE -> {
                val jsonBody = it.body!!.value
                val messageReceived = Event.MessageReceived(jsonBody)
                publisher.onNext(messageReceived)
                Log.d(LOG_TAG, "message sent, $messageReceived")
            }
            METHOD_LEAVE -> {
                val jsonBody = it.body!!.value
                publisher.onNext(Event.Leave(jsonBody))
            }
            METHOD_JOIN -> {
                val jsonBody = it.body!!.value
                publisher.onNext(Event.Join(jsonBody))
            }
        }
    }

    private fun handleError(it: Response) {
        publisher.onNext(Event.Error(it.method, Exception(it.error)))
        when (it.method) {
            METHOD_DISCONNECT -> {

            }
            METHOD_UNSUBSCRIBE -> {
            }
            METHOD_CONNECT -> {
            }
            METHOD_SUBSCRIBE -> {
            }
            else -> {
            } //ignore
        }
    }

    override fun disconnect(data: Command.Disconnect) {
        if (!isConnected.get()) {
            return
        }
        if (messengerMap.size > 0) {

        }
        cs.sendDisconnect(data)
    }

    override fun subscribe(data: Command.Subscribe) {
        if (!subscribeMap.contains(data)) {
            subscribeMap[data.params.channel] = data
        }
        if (isConnected.get()) {
            subscribe()
        }
    }

    private fun subscribe() {
        subscribeMap.values.forEach {
            cs.sendSubscribe(it)
        }
    }

    private fun unsubscribeAll() = subscribeMap.forEach {
        val channel = it.value.params.channel
        cs.sendUnsubscribe(Command.Unsubscribe(ChannelParams(channel)))
    }

    override fun unsubscribe(data: Command.Unsubscribe) = cs.sendUnsubscribe(data)
}