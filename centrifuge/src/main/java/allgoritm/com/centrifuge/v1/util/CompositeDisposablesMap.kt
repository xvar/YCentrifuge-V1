package allgoritm.com.centrifuge.v1.util

import io.reactivex.disposables.Disposable
import java.util.*


internal class CompositeDisposablesMap {

    private val subscriptions: MutableMap<String, Disposable>

    init {
        subscriptions = HashMap()
    }

    @Synchronized
    fun put(key: String, subscription: Disposable) {
        clear(key)
        subscriptions[key] = subscription
    }

    @Synchronized
    fun isUnsubscribed(key: String): Boolean {
        return subscriptions[key]!!.isDisposed
    }

    @Synchronized
    fun clear(key: String) {
        if (subscriptions.containsKey(key)) {
            clearSubscription(subscriptions[key])
            subscriptions.remove(key)
        }
    }

    @Synchronized
    fun containsKey(key: String): Boolean {
        return subscriptions.containsKey(key)

    }

    @Synchronized
    private fun clearSubscription(subscription: Disposable?) {
        if (subscription != null && !subscription.isDisposed) {
            subscription.dispose()
        }
    }

    @Synchronized
    fun clearAll() {
        for ((_, value) in subscriptions) {
            clearSubscription(value)
        }
        subscriptions.clear()
    }

}
