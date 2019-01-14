package allgoritm.com.centrifuge

import allgoritm.com.centrifuge.di.ViewModelFactory
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProviders
import dagger.android.AndroidInjector
import dagger.android.DispatchingAndroidInjector
import dagger.android.support.HasSupportFragmentInjector
import io.reactivex.disposables.CompositeDisposable
import io.reactivex.disposables.Disposable
import javax.inject.Inject

abstract class BaseActivity : AppCompatActivity(), HasSupportFragmentInjector {

    private val compositeDisposableMap = CompositeDisposable()
    protected fun addDisposable(d: Disposable) = compositeDisposableMap.add(d)

    @Inject
    lateinit var sFI: DispatchingAndroidInjector<Fragment>
    override fun supportFragmentInjector(): AndroidInjector<Fragment> = sFI

    inline fun <reified T : ViewModel> ViewModelFactory<T>.get(): T =
        ViewModelProviders.of(this@BaseActivity, this)[T::class.java]


    override fun onDestroy() {
        super.onDestroy()
        compositeDisposableMap.clear()
    }
}