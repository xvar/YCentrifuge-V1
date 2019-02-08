package allgoritm.com.centrifuge.v1.engine.scarlet

import allgoritm.com.centrifuge.v1.data.ConnectionConfig
import com.tinder.scarlet.retry.BackoffStrategy
import java.util.concurrent.atomic.AtomicBoolean

class CustomReconnectStrategy(private val cfg: ConnectionConfig) : BackoffStrategy {

    override fun backoffDurationMillisAt(retryCount: Int): Long {
        return if (retryCount < cfg.numTries && !isForcedReconnect.get()) cfg.connectTimeoutMs else cfg.failedConnectTimeoutMs
    }

    val isForcedReconnect = AtomicBoolean(false)
}