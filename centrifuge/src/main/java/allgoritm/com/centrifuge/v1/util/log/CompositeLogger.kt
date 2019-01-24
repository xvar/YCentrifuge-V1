package allgoritm.com.centrifuge.v1.util.log

class CompositeLogger : Logger {
    private val loggerList = ArrayList<Logger>()

    constructor(list: List<Logger>) {
        loggerList.clear()
        loggerList.addAll(list)
    }

    override fun log(level: Int, tag: String, msg: String, throwable: Throwable?) {
        loggerList.forEach { it.log(level, tag, msg, throwable) }
    }
}