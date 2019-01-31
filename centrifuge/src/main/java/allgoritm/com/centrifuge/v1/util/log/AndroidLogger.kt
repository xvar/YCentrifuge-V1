package allgoritm.com.centrifuge.v1.util.log

import android.util.Log

internal class AndroidLogger: Logger {

    override fun log(level: Int, tag: String, msg: String, throwable: Throwable?) {
        when (level) {
            ERROR -> Log.e(tag, msg, throwable)
            WARN -> Log.w(tag, msg, throwable)
            INFO -> Log.i(tag, msg, throwable)
            DEBUG -> Log.d(tag, msg, throwable)
            VERBOSE -> Log.v(tag, msg, throwable)
            else -> Log.wtf(tag, msg, throwable)
        }
    }
}