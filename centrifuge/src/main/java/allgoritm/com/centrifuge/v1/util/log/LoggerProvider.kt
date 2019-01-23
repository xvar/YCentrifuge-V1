package allgoritm.com.centrifuge.v1.util.log

import allgoritm.com.centrifuge.v1.BuildConfig

object LoggerProvider {

    private val androidLogger: Logger by lazy { AndroidLogger() }
    private val dummyLogger: Logger by lazy { DummyLogger }

    fun get() = if (BuildConfig.DEBUG) androidLogger else dummyLogger

}