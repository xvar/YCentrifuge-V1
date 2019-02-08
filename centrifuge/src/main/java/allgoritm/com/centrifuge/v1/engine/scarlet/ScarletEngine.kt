package allgoritm.com.centrifuge.v1.engine.scarlet

import allgoritm.com.centrifuge.v1.contract.Messenger
import allgoritm.com.centrifuge.v1.contract.YCentrifugeEngine
import allgoritm.com.centrifuge.v1.data.*
import allgoritm.com.centrifuge.v1.util.CompositeDisposablesMap
import allgoritm.com.centrifuge.v1.util.log.ERROR
import allgoritm.com.centrifuge.v1.util.log.Logger
import com.google.gson.Gson
import com.tinder.scarlet.Scarlet
import com.tinder.scarlet.WebSocket
import com.tinder.scarlet.messageadapter.gson.GsonMessageAdapter
import com.tinder.scarlet.retry.BackoffStrategy
import com.tinder.scarlet.streamadapter.rxjava2.RxJava2StreamAdapterFactory
import com.tinder.scarlet.websocket.okhttp.newWebSocketFactory
import io.reactivex.Completable
import io.reactivex.Flowable
import io.reactivex.Scheduler
import io.reactivex.processors.BehaviorProcessor
import okhttp3.OkHttpClient
import java.util.concurrent.ConcurrentHashMap
import java.util.concurrent.TimeUnit
import java.util.concurrent.atomic.AtomicBoolean
import java.util.concurrent.atomic.AtomicReference

internal class ScarletEngine(
    private val builder: OkHttpClient.Builder,
    private val gson: Gson,
    private val cfg: ConnectionConfig,
    private val workScheduler : Scheduler,
    private val resultScheduler: Scheduler,
    private val connectedLifecycle: ConnectedLifecycle,
    private val logger: Logger,
    private val backoffStrategy: BackoffStrategy
) : YCentrifugeEngine {

    private val keyPing = "scarlet_engine_ping"
    private val keyResponses = "scarlet_engine_responses"
    private val keyEvents = "scarlet_engine_events"
    private val keyPingReconnect = "scarlet_engine_ping_reconnect"

    private var scarletInstance : Scarlet? = null
    private lateinit var publisher : BehaviorProcessor<Event>
    private lateinit var cs : CentrifugeService
    private val compositeDisposable = CompositeDisposablesMap()

    private val subscribeMap = ConcurrentHashMap<String, Command.Subscribe>()
    private val messengerMap = ConcurrentHashMap<String, Messenger>()
    private val isDisconnecting = AtomicBoolean(false)
    private val lastConnectionCommand = AtomicReference<Command.Connect>()
    private val lastUrl = AtomicReference<String>()

    private val reconnect : Completable = Completable.fromCallable { reconnect() }

    private lateinit var client : OkHttpClient

    override fun init(eventPublisher: BehaviorProcessor<Event>) {
        publisher = eventPublisher
    }

    private fun initClient(): OkHttpClient {
        return builder
            .readTimeout(0, TimeUnit.NANOSECONDS)
            .connectTimeout(cfg.connectTimeoutMs, TimeUnit.MILLISECONDS)
            .pingInterval(cfg.pingIntervalMs, TimeUnit.MILLISECONDS)
            .addInterceptor(LoggingInterceptor(logger))
            .build()
    }


    override fun connect(url: String, data: Command.Connect, force : Boolean) {
        lastConnectionCommand.set(data)
        lastUrl.set(url)

        if (scarletInstance == null || force) {
            client = initClient()
            scarletInstance = Scarlet.Builder()
                .webSocketFactory(client.newWebSocketFactory(url))
                .lifecycle(connectedLifecycle)
                .addMessageAdapterFactory(GsonMessageAdapter.Factory(gson))
                .addStreamAdapterFactory(RxJava2StreamAdapterFactory())
                .backoffStrategy(backoffStrategy)
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
                {err -> logger.log(level = ERROR, msg = "[Parsed response error: $err]", throwable = err)}
            ))


        compositeDisposable.put(keyEvents, cs.observeWebSocketEvent()
            .subscribeOn(workScheduler)
            .observeOn(resultScheduler)
            .doOnNext { logger.log(msg = "[WebSocket event: $it]") }
            .subscribe ({ event ->
                handleEvent(event, data)
            },
                {err -> logger.log(level = ERROR, msg = "[WebSocket event error: $err]", throwable = err)}
            ))
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
            logger.log(msg = "[send Unsubscribe with $data]")
            cs.sendUnsubscribe(data)
        }
    }

    override fun refresh(data: Command.Refresh) {
        logger.log(msg = "[send Refresh with $data]")
        cs.sendRefresh(data)
    }

    private fun handleEvent(
        event: WebSocket.Event?,
        data: Command.Connect
    ) {
        when (event) {
            is WebSocket.Event.OnConnectionOpened<*> -> {
                val webSocket = event.webSocket as okhttp3.WebSocket
                publisher.onNext(Event.SocketOpened(webSocket))
                logger.log(msg = "[send Connect with $data]")
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
                .doOnNext { logger.log(msg = "[send Ping]") }
                .subscribe {
                    cs.sendPing(Command.Ping)
                    scheduleReconnect()
                }
        )
    }

    private fun reconnect() {
        val url = lastUrl.get()
        val data = lastConnectionCommand.get()
        if (url != null && data != null) {
            connect(url, data, true)
        }
    }

    private fun scheduleReconnect() {
        compositeDisposable.put(keyPingReconnect,
            reconnect
                .delay(cfg.pingIntervalMs, TimeUnit.MILLISECONDS, workScheduler)
                .observeOn(resultScheduler)
                .subscribe()
        )
    }

    private fun handle(it: Response) {
        logger.log(msg = "[Response from socket: $it]")
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
                logger.log(msg = "[Received message: $messageReceived]")
            }
            METHOD_LEAVE -> {
                val jsonBody = it.body!!.value
                publisher.onNext(Event.Leave(jsonBody))
            }
            METHOD_JOIN -> {
                val jsonBody = it.body!!.value
                publisher.onNext(Event.Join(jsonBody))
            }
            METHOD_PING -> {
                compositeDisposable.clear(keyPingReconnect)
            }
        }
    }

    private fun handleError(it: Response) {
        val error = Event.Error(it.method, Exception(it.error))
        logger.log(level = ERROR, msg = "$error")
        publisher.onNext(error)
    }

    override fun disconnect(data: Command.Disconnect) {
        if (!connectedLifecycle.isConnected()) {
            return
        }
        isDisconnecting.set(true)
        unsubscribeAll()
        logger.log(msg = "[send Disconnect with $data]")
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
            logger.log(msg = "[send Subscribe with $it]")
            cs.sendSubscribe(it)
        }
    }

    private fun unsubscribeAll() = subscribeMap.forEach {
        val channel = it.value.params.channel
        unsubscribe(Command.Unsubscribe(ChannelParams(channel)))
    }

}