package allgoritm.com.centrifuge.v1.data

import java.util.concurrent.TimeUnit

data class ConnectionConfig(
    val numTries : Int = 5,
    val connectTimeoutMs : Long = TimeUnit.SECONDS.toMillis(5),
    val pingIntervalMs : Long = TimeUnit.SECONDS.toMillis(30),
    val pongTimeoutMs: Long = TimeUnit.SECONDS.toMillis(5)
)