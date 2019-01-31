package allgoritm.com.centrifuge.v1.util.log

import allgoritm.com.centrifuge.v1.data.LOG_TAG

const val ERROR = -500
const val WARN = -501
const val INFO = -502
const val DEBUG = -503
const val VERBOSE = -504

internal interface Logger {
    fun log(level : Int = DEBUG, tag: String = LOG_TAG, msg: String, throwable: Throwable? = null)
    fun tag(level: Int): String = when (level) {
        ERROR -> "E"
        WARN -> "W"
        INFO -> "I"
        DEBUG -> "D"
        VERBOSE -> "V"
        else -> "WTF"
    }
}