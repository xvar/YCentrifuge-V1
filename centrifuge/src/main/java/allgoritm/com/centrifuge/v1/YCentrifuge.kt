package allgoritm.com.centrifuge.v1

import allgoritm.com.centrifuge.v1.contract.YCentrifugeEngine
import allgoritm.com.centrifuge.v1.data.*
import allgoritm.com.centrifuge.v1.engine.scarlet.ConnectedLifecycle
import allgoritm.com.centrifuge.v1.engine.scarlet.ScarletEngine
import allgoritm.com.centrifuge.v1.util.log.LoggerProvider
import com.google.gson.GsonBuilder
import io.reactivex.Flowable
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.processors.BehaviorProcessor
import io.reactivex.processors.PublishProcessor
import io.reactivex.schedulers.Schedulers
import okhttp3.OkHttpClient

object YCentrifugeFactory {
    fun create(connectionConfig: ConnectionConfig) = YCentrifuge(
        ScarletEngine(
            OkHttpClient.Builder(),
            GsonBuilder()
                .registerTypeAdapter(Body::class.java, BodyDeserializer())
                .create(),
            connectionConfig,
            Schedulers.io(),
            AndroidSchedulers.mainThread(),
            ConnectedLifecycle(),
            LoggerProvider.get()
        )
    )
}

class YCentrifuge internal constructor(
    private val engine : YCentrifugeEngine
) {

    private val eventPublisher = BehaviorProcessor.create<Event>()
    init { engine.init(eventPublisher) }

    fun events() : Flowable<Event> = eventPublisher
    fun connect(url: String, params: ConnectionParams) = engine.connect(url, Command.Connect(params))
    fun disconnect() = engine.disconnect(Command.Disconnect())
    fun subscribe(params: SubscribeParams) = engine.subscribe(Command.Subscribe(params))
    fun unsubscribe(channel: String) = engine.unsubscribe(Command.Unsubscribe(ChannelParams(channel)))
    fun refresh() = engine.refresh(Command.Refresh)
}