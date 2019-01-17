package allgoritm.com.centrifuge.v1.engine.old_centrifuge

import allgoritm.com.centrifuge.v1.contract.Messenger
import allgoritm.com.centrifuge.v1.contract.YCentrifugeEngine
import allgoritm.com.centrifuge.v1.data.*
import android.os.Handler
import android.os.Looper
import android.util.Log
import com.centrifugal.centrifuge.android.Centrifugo
import com.centrifugal.centrifuge.android.config.ReconnectConfig
import com.centrifugal.centrifuge.android.credentials.Token
import com.centrifugal.centrifuge.android.credentials.User
import com.centrifugal.centrifuge.android.listener.ConnectionListener
import com.centrifugal.centrifuge.android.listener.SubscriptionListener
import com.centrifugal.centrifuge.android.subscription.SubscriptionRequest
import io.reactivex.Flowable
import okhttp3.Request
import okhttp3.WebSocket
import okio.ByteString
import org.json.JSONObject
import org.reactivestreams.Processor
import java.util.concurrent.ConcurrentLinkedQueue
import java.util.concurrent.TimeUnit
import java.util.concurrent.atomic.AtomicBoolean

class OldCentrifugeEngine(private val connectionConfig: ConnectionConfig) : YCentrifugeEngine {

    private lateinit var publisher: Processor<Event, Event>
    private var centrifugo: Centrifugo? = null
    private val TAG = "OLD_CENTRIFUGE"
    private val subscribeQueue = ConcurrentLinkedQueue<Command.Subscribe>()
    private val mainHandler = Handler(Looper.getMainLooper())

    override fun init(eventPublisher: Processor<Event, Event>) {
        publisher = eventPublisher
    }


    override fun connect(url: String, data: Command.Connect) {
        if (centrifugo == null) {
            centrifugo = Centrifugo.Builder(url)
                .setUser(User(data.params.user, ""))
                .setToken(Token(data.params.token, data.params.timestamp))
                .build()
            centrifugo!!.setReconnectConfig(ReconnectConfig(connectionConfig.numTries, connectionConfig.connectTimeoutMs, TimeUnit.MILLISECONDS))
            centrifugo!!.setConnectionListener(object : ConnectionListener {
                override fun onConnected() {
                    Log.d(TAG, "websocket onConnected")
                    publishEvent(Event.Connected(JSONObject()))
                    subscribe()
                }

                override fun onDisconnected(code: Int, reason: String?, remote: Boolean) {
                    Log.d(TAG, "websocket onDisconnected")
                    publishEvent(Event.SocketClosed())
                }

                override fun onWebSocketOpen() {
                    Log.d(TAG, "websocket onWebSocketOpen")
                    //debug
                    publishEvent(Event.SocketOpened(object : WebSocket {
                        override fun queueSize(): Long {
                            TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
                        }

                        override fun send(text: String): Boolean {
                            TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
                        }

                        override fun send(bytes: ByteString): Boolean {
                            TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
                        }

                        override fun close(code: Int, reason: String?): Boolean {
                            TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
                        }

                        override fun cancel() {
                            TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
                        }

                        override fun request(): Request {
                            TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
                        }
                    }))
                }
            })

            centrifugo!!.setSubscriptionListener(object : SubscriptionListener {
                override fun onSubscriptionError(channelName: String?, error: String?) {
                    Log.e(LOG_TAG, "onSubscriptionError $channelName $error")
                    error?.let { publishEvent(Event.Error(METHOD_SUBSCRIBE, Exception(it))) }
                }

                override fun onSubscribed(channelName: String?) {
                    Log.d(LOG_TAG, "onSubscribed $channelName")
                    publishEvent(Event.Subscribed(channelName!!, object : Messenger {
                        override val channel: String
                            get() = channelName!!

                        override fun observe(): Flowable<Event> {
                            return Flowable.empty<Event>()
                        }
                    }))

                }

                override fun onUnsubscribed(channelName: String?) {
                    Log.d(LOG_TAG, "onSubscribed $channelName")
                }
            })

            centrifugo!!.setDataMessageListener {
                Log.d(LOG_TAG, "onNewDataMessage")
                publishEvent(Event.MessageReceived(Message.Text(it.data)))
            }
            centrifugo!!.connect()
        }
    }

    override fun disconnect(data: Command.Disconnect) {
        TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
    }

    private val isConnected = AtomicBoolean(false)

    override fun subscribe(data: Command.Subscribe) {
        if (!subscribeQueue.contains(data)) {
             subscribeQueue.offer(data)
        }
        if (isConnected.get()) {
            subscribe()
        }
    }

    private fun publishEvent(e: Event) {
        mainHandler.post {
            publisher.onNext(e)
        }
    }

    private fun subscribe() {
        subscribeQueue.forEach {
            Log.d(LOG_TAG, "try to subscribe $it")
            centrifugo!!.subscribe(SubscriptionRequest(it.params.channel))
        }
    }

    override fun unsubscribe(data: Command.Unsubscribe) {
        TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
    }
}