package allgoritm.com.centrifuge.v1

import allgoritm.com.centrifuge.v1.util.log.LoggerProvider
import android.content.Context

object Initializer {
    //will receive application level context
    //(currently from content provider for initialization purpose
    fun init(context: Context) {
        LoggerProvider.init(context)
    }
}