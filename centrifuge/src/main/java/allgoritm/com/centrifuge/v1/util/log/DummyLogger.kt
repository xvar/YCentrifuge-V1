package allgoritm.com.centrifuge.v1.util.log

object DummyLogger : Logger {
    override fun log(level: Int, tag: String, msg: String, throwable: Throwable?) {} //do nothing
}