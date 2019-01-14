package allgoritm.com.centrifuge

import androidx.lifecycle.ViewModel
import allgoritm.com.centrifuge.di.ViewModelFactory
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentActivity
import androidx.lifecycle.ViewModelProviders
import allgoritm.com.centrifuge.di.Injectable
import io.reactivex.disposables.CompositeDisposable
import io.reactivex.disposables.Disposable

abstract class BaseFragment : Fragment(), Injectable {

    private val compositeDisposable = CompositeDisposable()
    protected fun addDisposable(d: Disposable) = compositeDisposable.add(d)

    inline fun <reified T : ViewModel> ViewModelFactory<T>.get(): T =
        ViewModelProviders.of(this@BaseFragment, this)[T::class.java]

    inline fun <reified T : ViewModel> ViewModelFactory<T>.getForActivity(a : FragmentActivity): T =
        ViewModelProviders.of(a, this)[T::class.java]

    override fun onDestroy() {
        super.onDestroy()
        compositeDisposable.clear()
    }
}