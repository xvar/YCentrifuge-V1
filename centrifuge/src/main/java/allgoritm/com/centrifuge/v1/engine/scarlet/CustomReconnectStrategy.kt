package allgoritm.com.centrifuge.v1.engine.scarlet

import allgoritm.com.centrifuge.v1.data.ConnectionConfig
import com.tinder.scarlet.retry.BackoffStrategy

class CustomReconnectStrategy(private val cfg: ConnectionConfig) : BackoffStrategy {

    override fun backoffDurationMillisAt(retryCount: Int): Long {
        return if (retryCount < cfg.numTries) cfg.connectTimeoutMs else cfg.failedConnectTimeoutMs
    }
}