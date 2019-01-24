package allgoritm.com.centrifuge.v1.util.log

import allgoritm.com.centrifuge.v1.BuildConfig
import android.annotation.SuppressLint
import android.content.Context

/*
* Actually, this is not a leak,
* because we use only our YCentrifugeProvider's context
*/
@SuppressLint("StaticFieldLeak")
internal object LoggerProvider {

    private lateinit var context: Context
    fun init(contentProviderContext: Context) {
        this.context = contentProviderContext
    }

    private val dummyLogger: Logger by lazy { DummyLogger }
    private val debugLogger: Logger by lazy {
        CompositeLogger(listOf(AndroidLogger(), FileLogger(context, "centrifuge_log")))
    }

    fun get() = if (BuildConfig.LOG_ENABLED) debugLogger else dummyLogger

}