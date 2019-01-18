package allgoritm.com.centrifuge.v1.util

import io.reactivex.Flowable
import io.reactivex.functions.Function
import java.util.concurrent.TimeUnit

class RetryWithExponentialDelay(
    private val maxRetries: Int,
    private val retryDelay: Long,
    private val delayUnit: TimeUnit
) : Function<Flowable<out Throwable>, Flowable<*>> {
    private var retryCount: Int = 0

    override fun apply(attempts: Flowable<out Throwable>): Flowable<*> {
        return attempts.flatMap { throwable ->
            if (++retryCount < maxRetries) {
                Flowable.timer(Math.pow(retryDelay.toDouble(), retryCount.toDouble()).toLong(), delayUnit)
            } else {
                Flowable.error(throwable)
            }
        }
    }
}

//class DelayStrategy