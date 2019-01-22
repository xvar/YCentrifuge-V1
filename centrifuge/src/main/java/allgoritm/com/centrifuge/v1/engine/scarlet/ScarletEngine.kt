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
import com.tinder.scarlet.retry.LinearBackoffStrategy
import com.tinder.scarlet.streamadapter.rxjava2.RxJava2StreamAdapterFactory
import com.tinder.scarlet.websocket.okhttp.newWebSocketFactory
import io.reactivex.Flowable
import io.reactivex.Scheduler
import io.reactivex.processors.BehaviorProcessor
import okhttp3.OkHttpClient
import java.util.concurrent.ConcurrentHashMap
import java.util.concurrent.TimeUnit
import java.util.concurrent.atomic.AtomicBoolean
import java.util.concurrent.atomic.AtomicInteger
import java.util.concurrent.atomic.AtomicReference

class ScarletEngine(
    private val builder: OkHttpClient.Builder,
    private val gson: Gson,
    private val cfg: ConnectionConfig,
    private val workScheduler : Scheduler,
    private val resultScheduler: Scheduler,
    private val connectedLifecycle: ConnectedLifecycle = ConnectedLifecycle()
) : YCentrifugeEngine {

    private val keyPing = "scarlet_engine_ping"
    private val keyResponses = "scarlet_engine_responses"
    private val keyEvents = "scarlet_engine_events"

    private var scarletInstance : Scarlet? = null
    private lateinit var publisher : BehaviorProcessor<Event>
    private lateinit var cs : CentrifugeService
    private val compositeDisposable = CompositeDisposablesMap()

    private val subscribeMap = ConcurrentHashMap<String, Command.Subscribe>()
    private val messengerMap = ConcurrentHashMap<String, Messenger>()
    private val isDisconnecting = AtomicBoolean(false)
    private val connectErrorCount = AtomicInteger(0)
    private val lastConnectionParams = AtomicReference<ConnectionParams>()

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
        lastConnectionParams.set(data.params)
        if (scarletInstance == null) {
            client = initClient()
            scarletInstance = Scarlet.Builder()
                .webSocketFactory(client.newWebSocketFactory(url))
                .lifecycle(connectedLifecycle)
                .addMessageAdapterFactory(GsonMessageAdapter.Factory(gson))
                .addStreamAdapterFactory(RxJava2StreamAdapterFactory())
                .backoffStrategy(LinearBackoffStrategy(5000))
                .build()
            cs = scarletInstance!!.create<CentrifugeService>()
        }
        connectedLifecycle.onStart()

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
            is WebSocket.Event.OnConnectionClosed -> {
                publisher.onNext(Event.SocketClosed())

                if (isDisconnecting.get()) {
                    //disconnect by hands
                    subscribeMap.clear()
                    compositeDisposable.clearAll()
                    publisher.onNext(Event.Disconnected())
                    connectedLifecycle.onStop()
                    isDisconnecting.set(false)
                }
            }
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
                connectedLifecycle.onConnected()
                subscribe()
                publisher.onNext(Event.Connected(jsonBody))
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
        when (it.method) {
            METHOD_CONNECT -> {
                if (connectErrorCount.addAndGet(1) <= cfg.numTries) {
                    cs.sendConnect(Command.Connect(lastConnectionParams.get()))
                } else {
                    publisher.onNext(Event.Error(it.method, Exception(it.error)))
                    connectedLifecycle.onStop()
                }
            }
            else -> {
                publisher.onNext(Event.Error(it.method, Exception(it.error)))
            }
        }
    }

    override fun disconnect(data: Command.Disconnect) {
        if (!connectedLifecycle.isConnected()) {
            return
        }
        isDisconnecting.set(true)
        unsubscribeAll()
        cs.sendDisconnect(data)
    }

    override fun subscribe(data: Command.Subscribe) {
        if (!subscribeMap.contains(data)) {
            subscribeMap[data.params.channel] = data
        }
        if (connectedLifecycle.isConnected()) {
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
        unsubscribe(Command.Unsubscribe(ChannelParams(channel)))
    }

    override fun unsubscribe(data: Command.Unsubscribe) {
        if (!connectedLifecycle.isConnected()) {
            return
        }
        synchronized(this) {
            val channel = data.params.channel
            if (!messengerMap.containsKey(channel) || !subscribeMap.containsKey(channel)) {
                return
            }
            cs.sendUnsubscribe(data)
        }
    }
}