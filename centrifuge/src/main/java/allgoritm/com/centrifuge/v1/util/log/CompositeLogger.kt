package allgoritm.com.centrifuge.v1.util.log

internal class CompositeLogger(list: List<Logger>) : Logger {
    private val loggerList = ArrayList<Logger>()

    init {
        loggerList.clear()
        loggerList.addAll(list)
    }

    override fun log(level: Int, tag: String, msg: String, throwable: Throwable?) {
        loggerList.forEach { it.log(level, tag, msg, throwable) }
    }
}