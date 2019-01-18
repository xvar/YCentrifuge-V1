package allgoritm.com.centrifuge

import androidx.lifecycle.ViewModel
import io.reactivex.disposables.CompositeDisposable
import io.reactivex.disposables.Disposable

open class BaseViewModel : ViewModel() {

    private val compositeDisposable = CompositeDisposablesMap()

    fun addDisposable(key: String, d: Disposable) = compositeDisposable.put(key, d)

    override fun onCleared() {
        super.onCleared()
        compositeDisposable.clearAll()
    }

}