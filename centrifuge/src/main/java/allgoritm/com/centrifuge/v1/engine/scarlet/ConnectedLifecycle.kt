package allgoritm.com.centrifuge.v1.engine.scarlet

import com.tinder.scarlet.Lifecycle
import com.tinder.scarlet.lifecycle.LifecycleRegistry
import java.util.concurrent.atomic.AtomicBoolean

internal class ConnectedLifecycle(private val lifecycleRegistry: LifecycleRegistry = LifecycleRegistry())
    : Lifecycle by lifecycleRegistry
{
    private val isConnected = AtomicBoolean(false)
    private val isSocketOpened = AtomicBoolean(false)

    fun isConnected() = isConnected.get() && isSocketOpened.get()

    init {
        lifecycleRegistry.onNext(Lifecycle.State.Started)
    }

    fun onStart() {
        if (!isSocketOpened.get()) {
            lifecycleRegistry.onNext(Lifecycle.State.Started)
            isSocketOpened.set(true)
        }
    }

    fun onStop() {
        if (isSocketOpened.get()) {
            lifecycleRegistry.onNext(Lifecycle.State.Stopped.WithReason())
            isSocketOpened.set(false)
            isConnected.set(false)
        }
    }

    fun onConnected() = isConnected.set(true)

}