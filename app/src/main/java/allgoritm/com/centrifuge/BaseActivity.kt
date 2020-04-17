package allgoritm.com.centrifuge

import allgoritm.com.centrifuge.di.ViewModelFactory
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import dagger.android.AndroidInjector
import dagger.android.DispatchingAndroidInjector
import dagger.android.HasAndroidInjector
import io.reactivex.disposables.Disposable
import javax.inject.Inject

abstract class BaseActivity : AppCompatActivity(), HasAndroidInjector {

    private val compositeDisposable = CompositeDisposablesMap()
    protected fun addDisposable(key: String, d: Disposable) = compositeDisposable.put(key, d)

    @Inject lateinit var sFI: DispatchingAndroidInjector<Any>

    override fun androidInjector(): AndroidInjector<Any> = sFI

    inline fun <reified T : ViewModel> ViewModelFactory<T>.get(): T =
        ViewModelProvider(this@BaseActivity.viewModelStore, this)[T::class.java]


    override fun onDestroy() {
        super.onDestroy()
        compositeDisposable.clearAll()
    }
}